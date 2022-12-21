package com.mtuomiko.citybikeapp.api

import com.mtuomiko.citybikeapp.model.TopStation
import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class StationWithStatistics(
    val id: Int,
    val nameFinnish: String,
    val nameSwedish: String,
    val nameEnglish: String,
    val addressFinnish: String,
    val addressSwedish: String,
    val cityFinnish: String,
    val citySwedish: String,
    val operator: String,
    val capacity: Int,
    val longitude: Double,
    val latitude: Double,
    val statistics: StationStatistics
)

@Serdeable
class StationStatistics(
    val departureCount: Long,
    val arrivalCount: Long,
    val departureJourneyAverageDistance: Double,
    val arrivalJourneyAverageDistance: Double,
    val topStationsForArrivals: List<TopStation>,
    val topStationsForDepartures: List<TopStation>
)
