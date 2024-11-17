package com.mtuomiko.citybikeapp.dao

import com.mtuomiko.citybikeapp.common.model.TopStation
import com.mtuomiko.citybikeapp.dao.model.JourneyStatistics
import com.mtuomiko.citybikeapp.dao.model.TopStations
import com.mtuomiko.citybikeapp.dao.repository.JourneyRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class StatisticsDao(
    private val journeyRepository: JourneyRepository,
) {
    suspend fun getJourneyStatisticsByStationId(
        stationId: Int,
        from: Instant?,
        to: Instant?,
    ): JourneyStatistics = journeyRepository.getJourneyStatisticsByStationId(stationId, from, to).await()

    suspend fun getTopStationsByStationId(
        stationId: Int,
        from: Instant?,
        to: Instant?,
        limitPerDirection: Int = 5,
    ): TopStations {
        val topStationsQueryResult =
            journeyRepository.getTopStationsByStationId(stationId, limitPerDirection, from, to).await()

        val arrivalStations =
            topStationsQueryResult
                .filter { it.arrivalStationId == stationId }
                .sortedByDescending { it.journeyCount }
                .take(limitPerDirection)
                .map { TopStation(it.departureStationId, it.journeyCount) }

        val departureStations =
            topStationsQueryResult
                .filter { it.departureStationId == stationId }
                .sortedByDescending { it.journeyCount }
                .take(limitPerDirection)
                .map { TopStation(it.arrivalStationId, it.journeyCount) }

        return TopStations(forArrivingHere = arrivalStations, forDepartingTo = departureStations)
    }
}
