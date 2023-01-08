package com.mtuomiko.citybikeapp.dao

import com.mtuomiko.citybikeapp.common.model.Station
import com.mtuomiko.citybikeapp.common.model.StationDetails
import com.mtuomiko.citybikeapp.common.model.StationLimited
import com.mtuomiko.citybikeapp.common.model.TotalPagesWith
import com.mtuomiko.citybikeapp.dao.entity.StationEntity
import com.mtuomiko.citybikeapp.dao.model.StationProjection
import com.mtuomiko.citybikeapp.dao.model.StationSearchResult
import com.mtuomiko.citybikeapp.dao.repository.StationRepository
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlin.math.ceil

@Singleton
class StationDao(
    @Inject private val stationRepository: StationRepository
) {
    fun getStationById(stationId: Int): StationDetails? = stationRepository.findById(stationId)
        .orElse(null)?.toDetailsModel()

    fun getAllStationsLimited() = stationRepository.getAll().map { StationLimited(it.id, it.nameFinnish) }

    fun getStations(searchTokens: List<String>, page: Int, pageSize: Int): TotalPagesWith<List<Station>> {
        if (searchTokens.isEmpty()) return getStations(page, pageSize)

        // TODO: Escape regex or otherwise sanitize!
        val pattern = searchTokens.joinToString("|")
        val searchResult = stationRepository.searchUsingRegex(
            pattern = pattern,
            limit = pageSize,
            offset = pageSize * page
        )

        return if (searchResult.isEmpty()) {
            TotalPagesWith(content = emptyList(), totalPages = 1)
        } else {
            TotalPagesWith(
                content = searchResult.map { it.toModel() },
                totalPages = totalPageCount(searchResult.first().totalCount, pageSize)
            )
        }
    }

    private fun getStations(page: Int, pageSize: Int): TotalPagesWith<List<Station>> {
        val pageable = Pageable.from(page, pageSize, Sort.of(Sort.Order.asc("id")))
        val stationsPage = stationRepository.list(pageable)

        return TotalPagesWith(content = stationsPage.content.map { it.toModel() }, totalPages = stationsPage.totalPages)
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

    private fun StationSearchResult.toModel() =
        Station(id, nameFinnish, addressFinnish, cityFinnish, operator, capacity)

    private fun StationProjection.toModel() = Station(
        id,
        nameFinnish,
        addressFinnish,
        cityFinnish,
        operator,
        capacity
    )

    private fun totalPageCount(count: Int, pageSize: Int) = if (count == 0) {
        1
    } else {
        ceil(count.toDouble() / pageSize).toInt()
    }
}
