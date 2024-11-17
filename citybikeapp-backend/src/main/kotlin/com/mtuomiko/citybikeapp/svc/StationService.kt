package com.mtuomiko.citybikeapp.svc

import com.mtuomiko.citybikeapp.common.TIMEZONE
import com.mtuomiko.citybikeapp.common.model.Station
import com.mtuomiko.citybikeapp.common.model.TotalPagesWith
import com.mtuomiko.citybikeapp.dao.StationDao
import com.mtuomiko.citybikeapp.dao.StatisticsDao
import com.mtuomiko.citybikeapp.svc.model.StationStatistics
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDateTime

@Service
class StationService(
    private val stationDao: StationDao,
    private val statisticsDao: StatisticsDao,
    private val svcConfig: SvcConfig,
) {
    fun getStationById(stationId: Int) = stationDao.getStationById(stationId)

    fun stationExists(stationId: Int) = stationDao.stationExists(stationId)

    fun getAllStationsLimited() = stationDao.getAllStationsLimited()

    fun getStations(
        searchTokens: List<String>,
        page: Int?,
        pageSize: Int?,
    ): TotalPagesWith<List<Station>> {
        val validPage = page ?: 0
        val maxLimitedPageSize = svcConfig.getMaxLimitedPageSize(pageSize)
        return stationDao.getStations(searchTokens, validPage, maxLimitedPageSize)
    }

    fun getStationStatistics(
        stationId: Int,
        from: LocalDateTime?,
        to: LocalDateTime?,
    ): StationStatistics {
        // Interpret query dates to be in local Helsinki time
        val fromInstant = from?.atZone(TIMEZONE)?.toInstant()
        val toInstant = to?.atZone(TIMEZONE)?.toInstant()

        return getStationStatistics(stationId, fromInstant, toInstant)
    }

    fun getStationStatistics(
        stationId: Int,
        from: Instant?,
        to: Instant?,
    ): StationStatistics =
        runBlocking {
            val deferredStatistics = async { statisticsDao.getJourneyStatisticsByStationId(stationId, from, to) }
            val deferredTopStationsResult = async { statisticsDao.getTopStationsByStationId(stationId, from, to) }

            val journeyStatistics = deferredStatistics.await()
            val topStations = deferredTopStationsResult.await()

            StationStatistics(
                departureCount = journeyStatistics.departureCount,
                arrivalCount = journeyStatistics.arrivalCount,
                departureAverageDistance = journeyStatistics.departureAverageDistance,
                arrivalAverageDistance = journeyStatistics.arrivalAverageDistance,
                topStationsForArrivingHere = topStations.forArrivingHere,
                topStationsForDepartingTo = topStations.forDepartingTo,
            )
        }
}
