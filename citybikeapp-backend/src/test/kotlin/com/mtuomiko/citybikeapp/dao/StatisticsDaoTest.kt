package com.mtuomiko.citybikeapp.dao

import com.mtuomiko.citybikeapp.dao.builder.TopStationsQueryResultBuilder
import com.mtuomiko.citybikeapp.dao.repository.JourneyRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class StatisticsDaoTest {
    private val journeyRepository = mockk<JourneyRepository>()
    private val statisticsDao = StatisticsDao(journeyRepository)

    @Test
    fun `Top stations are split and sorted according to arriving and departing station ids and journey count`() {
        val testStationId = 1
        val testQueryResults = List(6) {
            TopStationsQueryResultBuilder().departureStationId(testStationId).arrivalStationId(100 + it)
                .journeyCount(1000L * (it + 1))
                .build()
        } + List(6) {
            TopStationsQueryResultBuilder().arrivalStationId(testStationId).departureStationId(200 + it)
                .journeyCount(100L * (it + 1))
                .build()
        }

        every { journeyRepository.getTopStationsByStationId(testStationId, 6) } returns CompletableDeferred(
            testQueryResults
        )

        runBlocking {
            val result = statisticsDao.getTopStationsByStationId(testStationId, null, null, 6)

            val expectedStationIdsWhereDepartedFrom =
                testQueryResults.filter { it.arrivalStationId == testStationId }.sortedByDescending { it.journeyCount }
                    .map { it.departureStationId }
            val expectedStationIdsWhereArrivedTo =
                testQueryResults.filter { it.departureStationId == testStationId }
                    .sortedByDescending { it.journeyCount }
                    .map { it.arrivalStationId }

            // expect descending order
            assertThat(result.forArrivingHere).hasSize(6)
            assertThat(result.forArrivingHere).isSortedAccordingTo { o1, o2 ->
                (o2.journeyCount - o1.journeyCount).toInt()
            }
            assertThat(result.forArrivingHere.map { it.id }).containsExactlyElementsOf(
                expectedStationIdsWhereDepartedFrom
            )

            assertThat(result.forDepartingTo).hasSize(6)
            assertThat(result.forDepartingTo).isSortedAccordingTo { o1, o2 ->
                (o2.journeyCount - o1.journeyCount).toInt()
            }
            assertThat(result.forDepartingTo.map { it.id }).containsExactlyElementsOf(expectedStationIdsWhereArrivedTo)
        }
    }

    @Test
    fun `Given query result with self referring top station, top station response does not exceed count limit`() {
        val testStationId = 1
        val testQueryResults = listOf(
            TopStationsQueryResultBuilder().departureStationId(2).arrivalStationId(testStationId)
                .journeyCount(10).build(),
            TopStationsQueryResultBuilder().departureStationId(testStationId).arrivalStationId(testStationId)
                .journeyCount(5).build()
        )

        every { journeyRepository.getTopStationsByStationId(testStationId, 1) } returns CompletableDeferred(
            testQueryResults
        )

        runBlocking {
            val result = statisticsDao.getTopStationsByStationId(testStationId, null, null, 1)

            assertThat(result.forArrivingHere).hasSize(1)
            assertThat(result.forArrivingHere.first().id).isEqualTo(2)
            assertThat(result.forArrivingHere.first().journeyCount).isEqualTo(10)

            assertThat(result.forDepartingTo).hasSize(1)
            assertThat(result.forDepartingTo.first().id).isEqualTo(testStationId)
            assertThat(result.forDepartingTo.first().journeyCount).isEqualTo(5)
        }
    }
}
