package com.mtuomiko.citybikeapp.model

import java.time.Instant

data class JourneyNew(
    val departureAt: Instant,
    val returnAt: Instant,
    val departureStation: Int,
    val returnStation: Int,
    val distance: Int,
    val duration: Int
)
