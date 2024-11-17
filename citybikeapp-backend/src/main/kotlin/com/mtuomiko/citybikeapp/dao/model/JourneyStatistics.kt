package com.mtuomiko.citybikeapp.dao.model

data class JourneyStatistics(
    val departureCount: Long,
    val arrivalCount: Long,
    val departureAverageDistance: Double,
    val arrivalAverageDistance: Double,
)
