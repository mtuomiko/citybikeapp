package com.mtuomiko.citybikeapp.dao.builder

import com.mtuomiko.citybikeapp.dao.model.TopStationsQueryResult

class TopStationsQueryResultBuilder {
    var departureStationId: Int = 1
    var arrivalStationId: Int = 2
    var journeyCount: Long = (1..100).random().toLong()

    fun build() = TopStationsQueryResult(
        departureStationId,
        arrivalStationId,
        journeyCount
    )

    fun departureStationId(id: Int) = apply { this.departureStationId = id }
    fun arrivalStationId(id: Int) = apply { this.arrivalStationId = id }
    fun journeyCount(count: Long) = apply { this.journeyCount = count }
}
