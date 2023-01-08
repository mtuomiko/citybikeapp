package com.mtuomiko.citybikeapp.svc

import com.mtuomiko.citybikeapp.common.TIMEZONE
import com.mtuomiko.citybikeapp.common.model.Station
import com.mtuomiko.citybikeapp.common.model.TotalPagesWith
import com.mtuomiko.citybikeapp.dao.StationDao
import com.mtuomiko.citybikeapp.dao.StatisticsDao
import com.mtuomiko.citybikeapp.svc.model.StationStatistics
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.min

@Singleton
class StationService(
    @Inject private val stationDao: StationDao,
    @Inject private val statisticsDao: StatisticsDao,
    @Inject private val paginationConfig: PaginationConfig
) {
    fun getStationById(stationId: Int) = stationDao.getStationById(stationId)

    fun getAllStationsLimited() = stationDao.getAllStationsLimited()

    fun getStations(searchTokens: List<String>, page: Int?, pageSize: Int?): TotalPagesWith<List<Station>> {
        return stationDao.getStations(searchTokens, page ?: 0, getPageSizeUsingConfig(pageSize, paginationConfig))
    }

    fun getStationStatistics(stationId: Int, fromDate: LocalDate?, toDate: LocalDate?): StationStatistics {
        // Interpret query dates to be in local Helsinki time
        val from = fromDate?.atStartOfDay(TIMEZONE)?.toInstant()
        val to = toDate?.atTime(LocalTime.MAX)?.atZone(TIMEZONE)?.toInstant()

        // TODO: Async handling of queries?
        val journeyStatistics = statisticsDao.getJourneyStatisticsByStationId(stationId, from, to)
        val topStations = statisticsDao.getTopStationsByStationId(stationId, from, to)

        return StationStatistics(
            departureCount = journeyStatistics.departureCount,
            arrivalCount = journeyStatistics.arrivalCount,
            departureAverageDistance = journeyStatistics.departureAverageDistance,
            arrivalAverageDistance = journeyStatistics.arrivalAverageDistance,
            topStationsForArrivingHere = topStations.forArrivingHere,
            topStationsForDepartingTo = topStations.forDepartingTo
        )
    }

    private fun getPageSizeUsingConfig(size: Int?, config: PaginationConfig) = if (size == null) {
        config.defaultPageSize
    } else {
        min(size, config.maxPageSize)
    }
}
