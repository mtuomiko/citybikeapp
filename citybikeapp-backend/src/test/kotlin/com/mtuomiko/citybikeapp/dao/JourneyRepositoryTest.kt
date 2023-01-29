package com.mtuomiko.citybikeapp.dao

import com.mtuomiko.citybikeapp.dao.builder.JourneysInitializer
import com.mtuomiko.citybikeapp.dao.builder.StationsInitializer
import com.mtuomiko.citybikeapp.dao.model.JourneyStatistics
import com.mtuomiko.citybikeapp.dao.model.TopStationsQueryResult
import com.mtuomiko.citybikeapp.dao.repository.JourneyRepository
import com.mtuomiko.citybikeapp.dao.repository.StationRepository
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.time.Instant

@MicronautTest
class JourneyRepositoryTest(
    private val journeyRepository: JourneyRepository,
    private val stationRepository: StationRepository
) {
    @AfterEach
    fun cleanup() {
        journeyRepository.deleteAll()
        stationRepository.deleteAll()
    }

    @Test
    fun `Trip statistics query does not return results if no journeys for selected station`() {
        val pair = initializeStationsAndJourneysExceptForSelectedTestStation(1, 3, 10)
        val testStationId = pair.first

        val result = runBlocking {
            journeyRepository.getJourneyStatisticsByStationId(testStationId).await()
        }

        assertThat(result).isEqualTo(JourneyStatistics(0, 0, 0.0, 0.0))
    }

    @Test
    fun `Trip statistics query returns correct trip counts when journeys exist for selected station`() {
        val (testStationId, otherStationIds) = initializeStationsAndJourneysExceptForSelectedTestStation(1, 10, 50)

        val journeysInitializer = JourneysInitializer(journeyRepository)
        journeysInitializer.departureStationId(testStationId).arrivalStationIds(otherStationIds).saveRandomJourneys(10)
        journeysInitializer.departureStationIds(otherStationIds).arrivalStationId(testStationId).saveRandomJourneys(5)

        val result = runBlocking {
            journeyRepository.getJourneyStatisticsByStationId(testStationId).await()
        }

        assertThat(result.departureCount).isEqualTo(10)
        assertThat(result.arrivalCount).isEqualTo(5)
    }

    @Test
    fun `Trip statistics query returns correct averages when journeys exist for selected station`() {
        val (testStationId, otherStationIds) = initializeStationsAndJourneysExceptForSelectedTestStation(1, 10, 50)

        val journeysInitializer = JourneysInitializer(journeyRepository)
        journeysInitializer.departureStationId(testStationId)
            .arrivalStationIds(otherStationIds)
            .minDistance(200).maxDistance(200)
            .saveRandomJourneys(10)
            .minDistance(400).maxDistance(400)
            .saveRandomJourneys(10)
            .departureStationIds(otherStationIds)
            .arrivalStationId(testStationId)
            .minDistance(400).maxDistance(400)
            .saveRandomJourneys(20)
            .minDistance(600).maxDistance(600)
            .saveRandomJourneys(20)

        val result = runBlocking { journeyRepository.getJourneyStatisticsByStationId(testStationId).await() }

        assertThat(result).isEqualTo(JourneyStatistics(20, 40, 300.0, 500.0))
    }

    @Test
    fun `Trip statistics query is limited by timestamp arguments`() {
        val (testStationId, otherStationIds) = initializeStationsAndJourneysExceptForSelectedTestStation(1, 10, 50)
        val targetStation = otherStationIds.first()

        JourneysInitializer(journeyRepository)
            .earliestDeparture(earliestButBeforeInclusion)
            .latestDeparture(latestButBeforeInclusion)
            .minDistance(42).maxDistance(42)
            .departureStationId(testStationId).arrivalStationId(targetStation).saveRandomJourneys(10)
            .departureStationId(targetStation).arrivalStationId(testStationId).saveRandomJourneys(10)
            .earliestDeparture(earliestIncluded)
            .latestDeparture(latestIncluded)
            .minDistance(100).maxDistance(100)
            .departureStationId(testStationId).arrivalStationId(targetStation).saveRandomJourneys(10)
            .departureStationId(targetStation).arrivalStationId(testStationId).saveRandomJourneys(10)
            .earliestDeparture(earliestButAfterInclusion)
            .latestDeparture(latestButAfterInclusion)
            .minDistance(999).maxDistance(999)
            .departureStationId(testStationId).arrivalStationId(targetStation).saveRandomJourneys(10)
            .departureStationId(targetStation).arrivalStationId(testStationId).saveRandomJourneys(10)

        val result = runBlocking {
            journeyRepository.getJourneyStatisticsByStationId(testStationId, earliestIncluded, latestIncluded).await()
        }

        assertThat(result).isEqualTo(JourneyStatistics(10, 10, 100.0, 100.0))
    }

    @Test
    fun `Top stations query returns empty results if no journeys for selected station`() {
        val pair = initializeStationsAndJourneysExceptForSelectedTestStation(1, 10, 50)
        val testStationId = pair.first

        val result = runBlocking { journeyRepository.getTopStationsByStationId(testStationId, 5).await() }

        assertThat(result.isEmpty())
    }

    @Test
    fun `Top stations query returns only relevant stations`() {
        val (testStationId, otherStationIds) = initializeStationsAndJourneysExceptForSelectedTestStation(1, 10, 50)
        val targetStations = otherStationIds.take(4)
        val journeysInitializer = JourneysInitializer(journeyRepository)
        journeysInitializer
            .departureStationId(testStationId)
            .arrivalStationId(targetStations[0]).saveRandomJourneys(5)
            .arrivalStationId(targetStations[1]).saveRandomJourneys(7)
            .arrivalStationId(testStationId)
            .departureStationId(targetStations[2]).saveRandomJourneys(9)
            .departureStationId(targetStations[3]).saveRandomJourneys(13)

        val result = runBlocking { journeyRepository.getTopStationsByStationId(testStationId, 5).await() }

        val expected = listOf(
            TopStationsQueryResult(departureStationId = testStationId, arrivalStationId = targetStations[0], 5),
            TopStationsQueryResult(departureStationId = testStationId, arrivalStationId = targetStations[1], 7),
            TopStationsQueryResult(departureStationId = targetStations[2], arrivalStationId = testStationId, 9),
            TopStationsQueryResult(departureStationId = targetStations[3], arrivalStationId = testStationId, 13)
        )
        assertThat(result).containsExactlyInAnyOrderElementsOf(expected)
    }

    @Test
    fun `Top stations query results are limited by timestamp arguments`() {
        val (testStationId, otherStationIds) = initializeStationsAndJourneysExceptForSelectedTestStation(1, 10, 50)
        val targetStation = otherStationIds.first()

        JourneysInitializer(journeyRepository)
            .earliestDeparture(earliestButBeforeInclusion)
            .latestDeparture(latestButBeforeInclusion)
            .departureStationId(testStationId).arrivalStationId(targetStation).saveRandomJourneys(13)
            .departureStationId(targetStation).arrivalStationId(testStationId).saveRandomJourneys(17)
            .earliestDeparture(earliestIncluded)
            .latestDeparture(latestIncluded)
            .departureStationId(testStationId).arrivalStationId(targetStation).saveRandomJourneys(19)
            .departureStationId(targetStation).arrivalStationId(testStationId).saveRandomJourneys(23)
            .earliestDeparture(earliestButAfterInclusion)
            .latestDeparture(latestButAfterInclusion)
            .departureStationId(testStationId).arrivalStationId(targetStation).saveRandomJourneys(29)
            .departureStationId(targetStation).arrivalStationId(testStationId).saveRandomJourneys(31)

        val result = runBlocking {
            journeyRepository.getTopStationsByStationId(testStationId, 5, earliestIncluded, latestIncluded).await()
        }

        val expected = listOf(
            TopStationsQueryResult(departureStationId = testStationId, arrivalStationId = targetStation, 19),
            TopStationsQueryResult(departureStationId = targetStation, arrivalStationId = testStationId, 23)
        )
        assertThat(result).containsExactlyInAnyOrderElementsOf(expected)
    }

    private fun initializeStationsAndJourneysExceptForSelectedTestStation(
        firstStationId: Int,
        stationCount: Int,
        journeyCount: Int
    ): Pair<Int, List<Int>> {
        val stationIds =
            StationsInitializer(stationRepository).firstId(firstStationId).count(stationCount).save().map { it.id }
        val testStationId = stationIds.random()
        val otherStationIds = stationIds.filter { it != testStationId }

        JourneysInitializer(journeyRepository)
            .departureStationIds(otherStationIds)
            .arrivalStationIds(otherStationIds)
            .saveRandomJourneys(journeyCount)

        return Pair(testStationId, otherStationIds)
    }

    companion object {
        val earliestButBeforeInclusion = Instant.parse("2020-01-01T00:00:00Z")
        val latestButBeforeInclusion = Instant.parse("2020-01-05T23:59:59Z")
        val earliestIncluded = Instant.parse("2020-01-06T00:00:00Z")
        val latestIncluded = Instant.parse("2020-01-10T23:59:59Z")
        val earliestButAfterInclusion = Instant.parse("2020-01-11T00:00:00Z")
        val latestButAfterInclusion = Instant.parse("2020-01-15T23:59:59Z")
    }
}
