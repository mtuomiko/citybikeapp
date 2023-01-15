package com.mtuomiko.citybikeapp.common.model

import io.micronaut.core.annotation.Introspected
import java.time.Instant

@Introspected
class Journey(
    val id: Long,
    val departureAt: Instant,
    val arrivalAt: Instant,
    val departureStationId: Int,
    val arrivalStationId: Int,
    val distance: Int,
    val duration: Int
)
