package com.mtuomiko.citybikeapp.svc

import com.mtuomiko.citybikeapp.common.TIMEZONE
import com.mtuomiko.citybikeapp.dao.StationDao
import com.mtuomiko.citybikeapp.dao.StatisticsDao
import io.mockk.coEvery
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test
import java.sql.SQLException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class StationServiceTest {
    private val stationDao = mockk<StationDao>()
    private val statisticsDao = mockk<StatisticsDao>()
    private val paginationConfig = mockk<SvcConfig>()
    private val stationService = StationService(stationDao, statisticsDao, paginationConfig)
    private val timezone = ZoneId.of("Europe/Helsinki")

    @Test
    fun `When station statistics are called with local date time, DAO is called with Helsinki zoned timestamps`() {
        val id = 66
        val fromDate = LocalDateTime.parse("2018-07-03T08:30:15")
        val toDate = LocalDateTime.parse("2019-01-09T12:24:24")
        val intCaptor = mutableListOf<Int>()
        val instantCaptor = mutableListOf<Instant>()

        coEvery {
            statisticsDao.getJourneyStatisticsByStationId(
                capture(intCaptor),
                capture(instantCaptor),
                capture(instantCaptor),
            )
        } returns mockk(relaxed = true)
        coEvery {
            statisticsDao.getTopStationsByStationId(
                capture(intCaptor),
                capture(instantCaptor),
                capture(instantCaptor),
            )
        } returns mockk(relaxed = true)

        stationService.getStationStatistics(stationId = id, from = fromDate, to = toDate)

        val expectedFrom = fromDate.atZone(timezone).toInstant()
        val expectedTo = toDate.atZone(TIMEZONE).toInstant()
        assertThat(intCaptor).hasSize(2)
        assertThat(intCaptor).allMatch { it == id }
        assertThat(instantCaptor).hasSize(4)
        assertThat(listOf(instantCaptor[0], instantCaptor[2])).allMatch { it == expectedFrom }
        assertThat(listOf(instantCaptor[1], instantCaptor[3])).allMatch { it == expectedTo }
    }

    @Test
    fun `Exception from async scope is propagated`() {
        val id = 66
        val date = LocalDateTime.parse("2018-07-03T08:30:15")

        coEvery {
            statisticsDao.getJourneyStatisticsByStationId(any(), any(), any())
        } returns mockk(relaxed = true)
        coEvery {
            statisticsDao.getTopStationsByStationId(any(), any(), any())
        } throws SQLException("this is a test")

        val thrown =
            catchThrowable {
                stationService.getStationStatistics(stationId = id, from = date, to = date)
            }

        assertThat(thrown)
            .isInstanceOf(SQLException::class.java)
            .hasMessageContaining("this is a test")
    }
}
