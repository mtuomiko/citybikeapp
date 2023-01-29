package com.mtuomiko.citybikeapp.dao.model

import com.mtuomiko.citybikeapp.common.model.TopStation

data class TopStationsQueryResult(
    val departureStationId: Int,
    val arrivalStationId: Int,
    val journeyCount: Long
)

class TopStations(
    val forArrivingHere: List<TopStation>,
    val forDepartingTo: List<TopStation>
)
