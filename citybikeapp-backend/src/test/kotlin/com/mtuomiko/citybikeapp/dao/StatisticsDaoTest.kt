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
    fun `Top stations are split and sorted according to arriving and departing station ids and journey count `() {
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

        every { journeyRepository.getTopStationsByStationId(testStationId) } returns CompletableDeferred(
            testQueryResults
        )

        runBlocking {
            val result = statisticsDao.getTopStationsByStationId(testStationId, null, null)

            val expectedStationIdsWhereDepartedFrom =
                testQueryResults.filter { it.arrivalStationId == testStationId }.sortedByDescending { it.journeyCount }
                    .map { it.departureStationId }
            val expectedStationIdsWhereArrivedTo =
                testQueryResults.filter { it.departureStationId == testStationId }
                    .sortedByDescending { it.journeyCount }
                    .map { it.arrivalStationId }

            // expect descending order
            assertThat(result.forArrivingHere).hasSize(6)
            assertThat(result.forArrivingHere).isSortedAccordingTo { o1, o2 -> (o2.journeyCount - o1.journeyCount).toInt() }
            assertThat(result.forArrivingHere.map { it.id }).containsExactlyElementsOf(
                expectedStationIdsWhereDepartedFrom
            )

            assertThat(result.forDepartingTo).hasSize(6)
            assertThat(result.forDepartingTo).isSortedAccordingTo { o1, o2 -> (o2.journeyCount - o1.journeyCount).toInt() }
            assertThat(result.forDepartingTo.map { it.id }).containsExactlyElementsOf(expectedStationIdsWhereArrivedTo)
        }
    }
}
