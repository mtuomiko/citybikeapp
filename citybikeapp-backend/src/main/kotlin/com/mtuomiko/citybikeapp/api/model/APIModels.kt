package com.mtuomiko.citybikeapp.api.model

import io.micronaut.serde.annotation.Serdeable
import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "StationDetails")
@Serdeable
data class APIStationDetails(
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
    val latitude: Double
)

@Schema(name = "StationStatistics")
@Serdeable
class APIStationStatistics(
    val departureCount: Long,
    val arrivalCount: Long,
    val departureAverageDistance: Double,
    val arrivalAverageDistance: Double,
    val topStationsForArrivingHere: List<APITopStation>,
    val topStationsForDepartingTo: List<APITopStation>
)

@Schema(name = "TopStation")
@Serdeable
class APITopStation(
    val id: Int,
    val nameFinnish: String,
    val nameSwedish: String,
    val nameEnglish: String,
    val journeyCount: Long
)
