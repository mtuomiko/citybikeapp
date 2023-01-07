package com.mtuomiko.citybikeapp

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.mtuomiko.citybikeapp.common.TIMEZONE
import com.mtuomiko.citybikeapp.common.model.JourneyNew
import com.mtuomiko.citybikeapp.common.model.StationNew
import com.mtuomiko.citybikeapp.dao.repository.JourneyRepository
import com.mtuomiko.citybikeapp.dao.repository.StationRepository
import io.micronaut.context.env.Environment
import jakarta.inject.Inject
import mu.KotlinLogging
import picocli.CommandLine.Command
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

private val CHARS_TO_TRIM = arrayOf(' ', ',', '"')

/**
 * Single run data load to database using configured CSV file URLs.
 */
@Command
class DataLoader : Runnable {
    @Inject
    private lateinit var config: DataLoaderConfig

    @Inject
    private lateinit var stationRepository: StationRepository

    @Inject
    private lateinit var journeyRepository: JourneyRepository

    @Inject
    private lateinit var fileProvider: FileProvider

    @Inject
    private lateinit var environment: Environment

    private val reader = csvReader()
    private lateinit var validStationIds: List<Int>

    override fun run() {
        logger.info { "Starting data load" }
        try {
            val millis = measureTimeMillis {
                processStations(config.stationUrl)
                validStationIds = stationRepository.findAll().map { it.id }
                processJourneys(config.journeyUrls)
            }
            val duration = Duration.ofMillis(millis)
            logger.info { "Data load complete in ${duration.toMinutes()}m and ${duration.toSecondsPart()}s" }
        } finally {
            if (Environments.PROD.id in environment.activeNames) fileProvider.deleteFiles()
        }
    }

    private fun processStations(url: String) {
        logger.info { "Processing stations" }

        val inputStream = fileProvider.getLocalInputStream(url)

        reader.open(inputStream) {
            readAllWithHeaderAsSequence()
                .mapNotNull { parseStation(it) }
                .chunked(config.batchSize)
                .forEach { stationRepository.saveInBatchIgnoringConflicts(it) }
        }
        logger.info { "Stations loaded" }
    }

    private fun processJourneys(urls: List<String>) {
        logger.info { "Processing journeys" }
        urls.forEach { url ->
            val inputStream = fileProvider.getLocalInputStream(url)

            reader.open(inputStream) {
                readAllWithHeaderAsSequence()
                    .mapNotNull(::parseJourney)
                    .filter(::isJourneyValid)
                    .chunked(config.batchSize)
                    .forEach { journeyRepository.saveInBatchIgnoringConflicts(it) }
            }
        }
        logger.info { "Journeys loaded" }
    }

    private fun isJourneyValid(journey: JourneyNew) =
        journey.distance >= config.minimumJourneyDistance &&
            journey.duration >= config.minimumJourneyDuration &&
            journey.departureStation in validStationIds &&
            journey.arrivalStation in validStationIds

    private fun parseStation(entry: Map<String, String>) = parseIgnoringMalformedData {
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
            latitude = entry["y"]!!.toDouble()
        )
    }

    private fun parseJourney(entry: Map<String, String>) = parseIgnoringMalformedData {
        JourneyNew(
            departureAt = LocalDateTime.parse(entry["Departure"]!!).atZone(TIMEZONE).toInstant(),
            arrivalAt = LocalDateTime.parse(entry["Return"]!!).atZone(TIMEZONE).toInstant(),
            departureStation = entry["Departure station id"]!!.toInt(),
            arrivalStation = entry["Return station id"]!!.toInt(),
            distance = entry["Covered distance (m)"]!!.toDouble().toInt(),
            duration = entry["Duration (sec.)"]!!.toDouble().toInt()
        )
    }

    private fun <T> parseIgnoringMalformedData(block: () -> T): T? {
        return try {
            block()
        } catch (e: NumberFormatException) {
            null
        } catch (e: DateTimeParseException) {
            null
        }
    }
}

private fun shouldBeTrimmed(char: Char) = char in CHARS_TO_TRIM
private fun String.trimJunk() = this.trim(::shouldBeTrimmed)
