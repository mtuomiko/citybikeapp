package com.mtuomiko.citybikeapp

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.mtuomiko.citybikeapp.dao.JourneyRepository
import com.mtuomiko.citybikeapp.dao.StationRepository
import com.mtuomiko.citybikeapp.model.JourneyNew
import com.mtuomiko.citybikeapp.model.StationNew
import jakarta.inject.Inject
import mu.KotlinLogging
import picocli.CommandLine.Command
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeParseException
import kotlin.io.path.createDirectories
import kotlin.io.path.inputStream

private val logger = KotlinLogging.logger {}

private const val DOWNLOAD_DIR = "temp"
private const val TIMEZONE_ID = "Europe/Helsinki" // assuming data timestamps to be local Helsinki time
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

    private val directoryPath = Path.of(DOWNLOAD_DIR)
    private val reader = csvReader()
    private val timezone = ZoneId.of(TIMEZONE_ID)
    private lateinit var validStationIds: List<Int>

    override fun run() {
        directoryPath.createDirectories()

        logger.info { "Starting data load" }

        processStations(config.stationUrl)
        validStationIds = stationRepository.findAll().map { it.id }
        processJourneys(config.journeyUrls)

        logger.info { "Data load complete" }
    }

    private fun processStations(url: String) {
        logger.info { "Processing station URL $url" }

        val path = directoryPath.resolve(getFilename(url))
        if (!Files.exists(path)) downloadToPath(url, path)

        val count = operateOnCSVChunks(path) { sequence ->
            sequence
                .map { chunk -> chunk.mapNotNull { parseStation(it) } }
                .map { stationRepository.saveInBatch(it) }
                .reduce { sum, count -> sum + count }
        }
        logger.info { "$count stations loaded" }
    }

    private fun processJourneys(urls: List<String>) {
        var count = 0L
        urls.forEach { url ->
            logger.info { "Processing journey URL $url" }

            val path = directoryPath.resolve(getFilename(url))
            if (!Files.exists(path)) downloadToPath(url, path)

            count += operateOnCSVChunks(path) { sequence ->
                sequence
                    .map { chunk ->
                        chunk.mapNotNull { parseJourney(it) }.filter { isJourneyValid(it) }
                    }
                    .map { journeyRepository.saveInBatch(it) }
                    .reduce { sum, count -> sum + count }
            }
        }
        logger.info { "$count journeys loaded" }
    }

    private fun getFilename(url: String) = url.split('/').last()

    private fun operateOnCSVChunks(path: Path, action: (Sequence<List<Map<String, String>>>) -> Int): Int =
        reader.open(path.inputStream()) {
            action(readAllWithHeaderAsSequence().chunked(config.batchSize))
        }

    private fun isJourneyValid(journey: JourneyNew) =
        journey.distance >= config.minimumJourneyDistance &&
            journey.duration >= config.minimumJourneyDuration &&
            journey.departureStation in validStationIds &&
            journey.returnStation in validStationIds

    private fun downloadToPath(url: String, path: Path) {
        logger.info { "Downloading $url to $path" }
        URL(url).openStream().use {
            Files.copy(it, path)
        }
    }

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
            departureAt = LocalDateTime.parse(entry["Departure"]!!).atZone(timezone).toInstant(),
            returnAt = LocalDateTime.parse(entry["Return"]!!).atZone(timezone).toInstant(),
            departureStation = entry["Departure station id"]!!.toInt(),
            returnStation = entry["Return station id"]!!.toInt(),
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
