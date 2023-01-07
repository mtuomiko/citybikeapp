package com.mtuomiko.citybikeapp.dao

import com.mtuomiko.citybikeapp.common.model.Station
import com.mtuomiko.citybikeapp.common.model.StationDetails
import com.mtuomiko.citybikeapp.common.model.StationLimited
import com.mtuomiko.citybikeapp.dao.entity.StationEntity
import com.mtuomiko.citybikeapp.dao.model.StationProjection
import com.mtuomiko.citybikeapp.dao.repository.StationRepository
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class StationDao(
    @Inject private val stationRepository: StationRepository,
    @Inject private val paginationConfig: PaginationConfig
) {
    fun getStationById(stationId: Int): StationDetails? = stationRepository.findById(stationId)
        .orElse(null)?.toDetailsModel()

    fun getAllStationsLimited() = stationRepository.getAll().map { StationLimited(it.id, it.nameFinnish) }

    fun getStations(searchTokens: List<String>, page: Int): List<Station> {
        if (searchTokens.isEmpty()) return getStations(page)

        // TODO: Escape regex or otherwise sanitize!
        return stationRepository.searchUsingRegex(
            pattern = searchTokens.joinToString("|"),
            limit = paginationConfig.defaultPageSize,
            offset = paginationConfig.defaultPageSize * page
        )
    }

    private fun getStations(page: Int): List<Station> {
        val pageable = Pageable.from(page, paginationConfig.defaultPageSize, Sort.of(Sort.Order.asc("id")))
        return stationRepository.list(pageable).map { it.toModel() }
    }

    private fun StationEntity.toDetailsModel(): StationDetails = StationDetails(
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

    private fun StationProjection.toModel() = Station(
        id,
        nameFinnish,
        addressFinnish,
        cityFinnish,
        operator,
        capacity
    )
}
