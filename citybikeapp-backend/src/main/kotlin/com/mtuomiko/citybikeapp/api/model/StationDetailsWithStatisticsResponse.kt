package com.mtuomiko.citybikeapp.api.model

import io.micronaut.serde.annotation.Serdeable

@Serdeable
class StationDetailsWithStatisticsResponse(
    val station: APIStationDetails,
    val statistics: APIStationStatistics
)
