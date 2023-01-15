package com.mtuomiko.citybikeapp.dao

import com.mtuomiko.citybikeapp.common.model.TopStation
import com.mtuomiko.citybikeapp.dao.model.JourneyStatistics
import com.mtuomiko.citybikeapp.dao.model.TopStations
import com.mtuomiko.citybikeapp.dao.repository.JourneyRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.time.Instant

@Singleton
class StatisticsDao(
    @Inject private val journeyRepository: JourneyRepository
) {
    fun getJourneyStatisticsByStationId(stationId: Int, from: Instant?, to: Instant?): JourneyStatistics {
        return journeyRepository.getJourneyStatisticsByStationId(stationId, from, to)
    }

    fun getTopStationsByStationId(stationId: Int, from: Instant?, to: Instant?): TopStations {
        val topStationsQueryResult = journeyRepository.getTopStationsByStationId(stationId, from, to)

        val arrivalStations = topStationsQueryResult
            .filter { it.arrivalStationId == stationId }
            .sortedByDescending { it.journeyCount }
            .map { TopStation(it.departureStationId, it.journeyCount) }

        val departureStations = topStationsQueryResult
            .filter { it.departureStationId == stationId }
            .sortedByDescending { it.journeyCount }
            .map { TopStation(it.arrivalStationId, it.journeyCount) }

        return TopStations(forArrivingHere = arrivalStations, forDepartingTo = departureStations)
    }
}
