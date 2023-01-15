package com.mtuomiko.citybikeapp.dao.builder

import com.mtuomiko.citybikeapp.dao.entity.JourneyEntity
import java.time.Instant

class JourneyEntityBuilder {
    var id: Long = 0
    var departureAt: Instant = Instant.parse("2022-12-15T18:35:24Z")
    var arrivalAt: Instant = Instant.parse("2022-12-15T18:45:24Z")
    var departureStationId: Int = 1
    var arrivalStationId: Int = 1
    var distance: Int = 2357
    var duration: Int = 600

    fun build() = JourneyEntity(id, departureAt, arrivalAt, departureStationId, arrivalStationId, distance, duration)

    fun id(id: Long) = apply { this.id = id }
    fun departureAt(departureAt: Instant) = apply { this.departureAt = departureAt }
    fun arrivalAt(arrivalAt: Instant) = apply { this.arrivalAt = arrivalAt }
    fun departureStationId(id: Int) = apply { this.departureStationId = id }
    fun arrivalStationId(id: Int) = apply { this.arrivalStationId = id }
    fun distance(distance: Int) = apply { this.distance = distance }
    fun duration(duration: Int) = apply { this.duration = duration }
}
