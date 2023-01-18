package com.mtuomiko.citybikeapp.api.model

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class StationDetailsWithStatisticsResponse(
    val station: APIStationDetails,
    val statistics: APIStationStatistics
)

@Serdeable
data class StationsLimitedResponse(
    val stations: List<APIStationLimited>
)

@Serdeable
class StationsResponse(
    val stations: List<APIStation>,
    val meta: Meta
)

@Serdeable
class JourneysResponse(
    val journeys: List<APIJourney>,
    val meta: CursorMeta
)

@Serdeable
class ErrorResponse(
    val message: String?,
    val status: Int,
    val errors: List<APIError>?
)
