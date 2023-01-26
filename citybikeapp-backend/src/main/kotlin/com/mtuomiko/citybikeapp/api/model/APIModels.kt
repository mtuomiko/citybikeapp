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
@Schema(
    name = "StationStatistics",
    description = "Station statistics for a given station."
)
data class APIStationStatistics(
    @field:Schema(
        description = "Amount of journeys departing from the queried station",
        nullable = false,
        required = true
    )
    val departureCount: Long,
    @field:Schema(description = "Amount of journeys arriving to the queried station", nullable = false, required = true)
    val arrivalCount: Long,
    @field:Schema(description = "Average distance of departing journeys in meters", nullable = false, required = true)
    val departureAverageDistance: Double,
    @field:Schema(description = "Average distance of arriving journeys in meters", nullable = false, required = true)
    val arrivalAverageDistance: Double,
    @field:Schema(
        description = "Sorted list of top five most popular (by journey count) stations of journeys that end in " +
            "the queried station. Most popular station is first in the list.",
        nullable = false,
        required = true
    )
    val topStationsForArrivingHere: List<APITopStation>,
    @field:Schema(
        description = "Sorted list of top five most popular (by journey count) stations of journeys that start in " +
            "the queried station. Most popular station is first in the list.",
        nullable = false,
        required = true
    )
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
    @field:Schema(
        description = "Paging cursor. Treat as an opaque string. Does not include the query parameters " +
            "which produced the cursor so the next request using the cursor must provide the same parameters for " +
            "meaningful pagination."
    )
    val nextCursor: String?
)

@Serdeable
@Schema(name = "Journey")
data class APIJourney(
    val id: String,
    @field:Schema(format = "date-time", nullable = false, required = true)
    val departureAt: Instant,
    @field:Schema(format = "date-time", nullable = false, required = true)
    val arrivalAt: Instant,
    val departureStationId: String,
    val arrivalStationId: String,
    @field:Schema(description = "Distance in meters", nullable = false, required = true)
    val distance: Int,
    @field:Schema(description = "Duration in seconds", nullable = false, required = true)
    val duration: Int
)

@Serdeable
@Schema(name = "Error")
class APIError(
    val message: String,
    val target: String?
)
