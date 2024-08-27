package com.mtuomiko.citybikeapp.common.model

import java.time.Instant

/**
 * The name of these properties is currently tied to the keyset pagination functionality and the reflection that is done
 * to support it dynamically.
 *
 * Meaning they cannot be changed without handling the change at some layer for incoming journey searches. Basically
 * breaks division of responsibilities between API and SVC layers.
 */
class Journey(
    val id: Long,
    val departureAt: Instant,
    val arrivalAt: Instant,
    val departureStationId: Int,
    val arrivalStationId: Int,
    val distance: Int,
    val duration: Int,
)
