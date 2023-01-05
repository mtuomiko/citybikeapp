package com.mtuomiko.citybikeapp.common.model

import java.time.Instant

data class JourneyNew(
    val departureAt: Instant,
    val arrivalAt: Instant,
    val departureStation: Int,
    val arrivalStation: Int,
    val distance: Int,
    val duration: Int
)
