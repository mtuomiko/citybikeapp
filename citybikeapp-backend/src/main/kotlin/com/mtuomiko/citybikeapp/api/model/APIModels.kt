package com.mtuomiko.citybikeapp.api.model

import io.micronaut.serde.annotation.Serdeable
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Serdeable
@Schema(name = "StationDetails")
data class APIStationDetails(
    val id: String,
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
    val id: String,
    val nameFinnish: String,
    val addressFinnish: String,
    val cityFinnish: String,
    val operator: String,
    val capacity: Int
)

@Serdeable
@Schema(name = "StationLimited")
data class APIStationLimited(
    val id: String,
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
    val id: String,
    val journeyCount: Long
)

@Serdeable
class Meta(
    val totalPages: Int
)

@Serdeable
class CursorMeta(
    val nextCursor: String?
)

@Serdeable
@Schema(name = "Journey")
data class APIJourney(
    val id: String,
    val departureAt: Instant,
    val arrivalAt: Instant,
    val departureStationId: Int,
    val arrivalStationId: Int,
    val distance: Int,
    val duration: Int
)

@Serdeable
@Schema(name = "Error")
class APIError(
    val message: String,
    val target: String?
)
