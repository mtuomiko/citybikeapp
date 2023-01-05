package com.mtuomiko.citybikeapp.common.model

import io.micronaut.serde.annotation.Serdeable
import java.time.Instant

@Serdeable
data class Journey(
    val id: Long,
    val departureAt: Instant,
    val returnAt: Instant,
    val departureStation: Station,
    val returnStation: Station,
    val distance: Int,
    val duration: Int
)
