package com.mtuomiko.citybikeapp.dao.entity

import java.time.Instant

data class JourneyEntity(
    val id: Long,
    val departureAt: Instant,
    val arrivalAt: Instant,
    val departureStationId: Int,
    val arrivalStationId: Int,
    val distance: Int,
    val duration: Int
)
