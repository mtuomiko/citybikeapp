package com.mtuomiko.citybikeapp.svc

import com.mtuomiko.citybikeapp.common.TIMEZONE
import com.mtuomiko.citybikeapp.dao.StationDao
import com.mtuomiko.citybikeapp.dao.StatisticsDao
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class StationServiceTest {
    private val stationDao = mockk<StationDao>()
    private val statisticsDao = mockk<StatisticsDao>()
    private val paginationConfig = mockk<PaginationConfig>()
    private val stationService = StationService(stationDao, statisticsDao, paginationConfig)
    private val timezone = ZoneId.of("Europe/Helsinki")

    @Test
    fun `When station statistics are called with date args, DAO is called with Helsinki zoned timestamps`() {
        val id = 66
        val summerDate = LocalDate.parse("2018-07-03")
        val winterDate = LocalDate.parse("2019-01-09")
        val intCaptor = mutableListOf<Int>()
        val instantCaptor = mutableListOf<Instant>()

        every {
            statisticsDao.getJourneyStatisticsByStationId(
                capture(intCaptor),
                capture(instantCaptor),
                capture(instantCaptor)
            )
        } returns mockk(relaxed = true)
        every {
            statisticsDao.getTopStationsByStationId(
                capture(intCaptor),
                capture(instantCaptor),
                capture(instantCaptor)
            )
        } returns mockk(relaxed = true)

        stationService.getStationStatistics(stationId = id, fromDate = summerDate, toDate = winterDate)

        val expectedFrom = summerDate.atTime(LocalTime.MIDNIGHT).atZone(timezone).toInstant()
        val expectedTo = winterDate.atTime(LocalTime.MAX).atZone(TIMEZONE).toInstant()
        assertThat(intCaptor).hasSize(2)
        assertThat(intCaptor).allMatch { it == id }
        assertThat(instantCaptor).hasSize(4)
        assertThat(listOf(instantCaptor[0], instantCaptor[2])).allMatch { it == expectedFrom }
        assertThat(listOf(instantCaptor[1], instantCaptor[3])).allMatch { it == expectedTo }
    }
}
