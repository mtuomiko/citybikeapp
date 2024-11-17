package com.mtuomiko.citybikeapp.svc.model

import com.mtuomiko.citybikeapp.common.model.TopStation

class StationStatistics(
    val departureCount: Long,
    val arrivalCount: Long,
    val departureAverageDistance: Double,
    val arrivalAverageDistance: Double,
    val topStationsForArrivingHere: List<TopStation>,
    val topStationsForDepartingTo: List<TopStation>,
)
