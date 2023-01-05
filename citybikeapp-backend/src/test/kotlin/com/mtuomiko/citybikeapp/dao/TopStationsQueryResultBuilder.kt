package com.mtuomiko.citybikeapp.dao

import com.mtuomiko.citybikeapp.dao.model.TopStationsQueryResult

class TopStationsQueryResultBuilder {
    var departureStationId: Int = 1
    var arrivalStationId: Int = 2
    var journeyCount: Long = (1..100).random().toLong()
    var nameFinnish: String = "Asema"
    var nameSwedish: String = "Station"
    var nameEnglish: String = "Station"

    fun build() = TopStationsQueryResult(
        departureStationId,
        arrivalStationId,
        journeyCount,
        nameFinnish,
        nameSwedish,
        nameEnglish
    )

    fun departureStationId(id: Int) = apply { this.departureStationId = id }
    fun arrivalStationId(id: Int) = apply { this.arrivalStationId = id }
    fun journeyCount(count: Long) = apply { this.journeyCount = count }
}
