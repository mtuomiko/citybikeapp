package com.mtuomiko.citybikeapp.svc

import com.mtuomiko.citybikeapp.common.TIMEZONE
import com.mtuomiko.citybikeapp.dao.StationDao
import com.mtuomiko.citybikeapp.dao.StatisticsDao
import com.mtuomiko.citybikeapp.svc.model.StationStatistics
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.time.LocalDate
import java.time.LocalTime

@Singleton
class StationService(
    @Inject private val stationDao: StationDao,
    @Inject private val statisticsDao: StatisticsDao
) {
    fun getStationById(stationId: Int) = stationDao.getStationById(stationId)

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
}
