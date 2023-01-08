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
