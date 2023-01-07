package com.mtuomiko.citybikeapp.api.model

import io.micronaut.serde.annotation.Serdeable
import io.swagger.v3.oas.annotations.media.Schema

@Serdeable
@Schema(name = "StationDetails")
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

@Serdeable
@Schema(name = "Station")
data class APIStation(
    val id: Int,
    val nameFinnish: String,
    val addressFinnish: String,
    val cityFinnish: String,
    val operator: String,
    val capacity: Int
)

@Serdeable
@Schema(name = "StationLimited")
data class APIStationLimited(
    val id: Int,
    val nameFinnish: String
)

@Serdeable
@Schema(name = "StationStatistics")
data class APIStationStatistics(
    val departureCount: Long,
    val arrivalCount: Long,
    val departureAverageDistance: Double,
    val arrivalAverageDistance: Double,
    val topStationsForArrivingHere: List<APITopStation>,
    val topStationsForDepartingTo: List<APITopStation>
)

@Serdeable
@Schema(name = "TopStation")
class APITopStation(
    val id: Int,
    val nameFinnish: String,
    val nameSwedish: String,
    val nameEnglish: String,
    val journeyCount: Long
)
