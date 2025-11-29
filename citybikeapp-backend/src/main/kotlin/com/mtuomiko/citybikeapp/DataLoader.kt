package com.mtuomiko.citybikeapp

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.mtuomiko.citybikeapp.common.TIMEZONE
import com.mtuomiko.citybikeapp.common.model.JourneyNew
import com.mtuomiko.citybikeapp.common.model.StationNew
import com.mtuomiko.citybikeapp.dao.repository.JourneyRepository
import com.mtuomiko.citybikeapp.dao.repository.StationRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import kotlin.io.path.inputStream
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

// log once every this many batches, just to show that loading is proceeding
private const val BATCH_LOGGING_INTERVAL = 100

/**
 * Single run data load to database using configured CSV file URLs.
 */
@Component
@Profile("dataloader")
class DataLoader : CommandLineRunner {
    @Autowired
    private lateinit var config: DataLoaderConfig

    @Autowired
    private lateinit var stationRepository: StationRepository

    @Autowired
    private lateinit var journeyRepository: JourneyRepository

    @Autowired
    private lateinit var fileProvider: FileProvider

    @Autowired
    private lateinit var environment: Environment

    private val reader = csvReader()
    private lateinit var validStationIds: List<Int>
    private val charsToTrim = arrayOf(' ', ',', '"') // ignore these leading or trailing chars in text-like fields

    override fun run(vararg args: String?) {
        //
        logger.info { "Starting data load" }
        try {
            val millis =
                measureTimeMillis {
                    processStations(config.stationUrl)

                    // read stations, journeys will be filtered in this class allowing use of only existing station ids
                    validStationIds = stationRepository.getAllStationIds()

                    var totalJourneyRows = 0
                    var validJourneyRows = 0

                    logger.info { "Processing all journeys" }
                    config.journeyUrls.forEach {
                        val result = processJourneys(it)
                        totalJourneyRows += result.total
                        validJourneyRows += result.validUnique
                    }

                    logger.info {
                        "All journeys processed. Total rows $totalJourneyRows, valid rows $validJourneyRows"
                    }
                }
            val duration = Duration.ofMillis(millis)
            logger.info { "Data load complete in ${duration.toMinutes()}m and ${duration.toSecondsPart()}s" }
        } finally {
            if (Environments.PROD.id in environment.activeProfiles) fileProvider.deleteFiles()
        }
    }

    private fun processStations(url: String) {
        logger.info { "Processing stations" }

        val path = fileProvider.getByURI(url)

        reader.open(path.inputStream()) {
            readAllWithHeaderAsSequence()
                .mapNotNull { parseStation(it) }
                .chunked(config.batchSize)
                .forEach { stationRepository.saveInBatchIgnoringConflicts(it) }
        }
        logger.info { "Stations loaded" }
    }

    private fun processJourneys(url: String): JourneyStats {
        val path = fileProvider.getByURI(url)
        var journeyRows = 0
        var validJourneyRows = 0

        logger.info { "${path.fileName}: Starting processing" }
        reader.open(path.inputStream()) {
            readAllWithHeaderAsSequence()
                .onEach { journeyRows++ }
                .mapNotNull(::parseJourney)
                .filter(::isJourneyValid)
                .chunked(config.batchSize)
                .onEach { validJourneyRows += it.size }
                .forEachIndexed { i, batch ->
                    run {
                        journeyRepository.saveInBatchIgnoringConflicts(batch)
                        if (i % BATCH_LOGGING_INTERVAL == 0) {
                            logger.info { "${path.fileName}: Batch index $i processed" }
                        }
                    }
                }
        }

        logger.info { "${path.fileName}: done. Total rows $journeyRows, valid rows $validJourneyRows" }
        return JourneyStats(journeyRows, validJourneyRows)
    }

    private fun isJourneyValid(journey: JourneyNew) =
        journey.distance >= config.minimumJourneyDistance &&
            journey.duration >= config.minimumJourneyDuration &&
            journey.departureStationId in validStationIds &&
            journey.arrivalStationId in validStationIds

    private fun parseStation(entry: Map<String, String>) =
        parseIgnoringMalformedData {
            StationNew(
                id = entry["ID"]!!.toInt(),
                nameFinnish = entry["Nimi"]!!.trimJunk(),
                nameSwedish = entry["Namn"]!!.trimJunk(),
                nameEnglish = entry["Name"]!!.trimJunk(),
                addressFinnish = entry["Osoite"]!!.trimJunk(),
                addressSwedish = entry["Adress"]!!.trimJunk(),
                cityFinnish = entry["Kaupunki"]!!.trimJunk(),
                citySwedish = entry["Stad"]!!.trimJunk(),
                operator = entry["Operaattor"]!!.trimJunk(),
                capacity = entry["Kapasiteet"]!!.toInt(),
                longitude = entry["x"]!!.toDouble(),
                latitude = entry["y"]!!.toDouble(),
            )
        }

    private fun parseJourney(entry: Map<String, String>) =
        parseIgnoringMalformedData {
            JourneyNew(
                departureAt = LocalDateTime.parse(entry["Departure"]!!).atZone(TIMEZONE).toInstant(),
                arrivalAt = LocalDateTime.parse(entry["Return"]!!).atZone(TIMEZONE).toInstant(),
                departureStationId = entry["Departure station id"]!!.toInt(),
                arrivalStationId = entry["Return station id"]!!.toInt(),
                distance = entry["Covered distance (m)"]!!.toDouble().toInt(),
                duration = entry["Duration (sec.)"]!!.toDouble().toInt(),
            )
        }

    /**
     * Runs the given block catching exceptions from unexpected data. Meaning anything weird in the input data will
     * result in null, that can then be skipped.
     */
    private fun <T> parseIgnoringMalformedData(block: () -> T): T? =
        try {
            block()
        } catch (e: NumberFormatException) {
            null
        } catch (e: DateTimeParseException) {
            null
        }

    private fun shouldBeTrimmed(char: Char) = char in charsToTrim

    private fun String.trimJunk() = this.trim(::shouldBeTrimmed)

    class JourneyStats(
        val total: Int,
        val validUnique: Int,
    )
}
