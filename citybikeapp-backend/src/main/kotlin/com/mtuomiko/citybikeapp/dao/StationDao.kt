package com.mtuomiko.citybikeapp.dao

import com.mtuomiko.citybikeapp.common.model.Station
import com.mtuomiko.citybikeapp.dao.entity.StationEntity
import com.mtuomiko.citybikeapp.dao.repository.StationRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class StationDao(
    @Inject private val stationRepository: StationRepository
) {
    fun getStationById(stationId: Int): Station? = stationRepository.findById(stationId)
        .orElse(null)?.toModel()

    private fun StationEntity.toModel(): Station = Station(
        id,
        nameFinnish,
        nameSwedish,
        nameEnglish,
        addressFinnish,
        addressSwedish,
        cityFinnish,
        citySwedish,
        operator,
        capacity,
        longitude,
        latitude
    )
}
