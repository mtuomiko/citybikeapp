package com.mtuomiko.citybikeapp.common.model

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class StationDetails(
    val id: Int,
    val nameFinnish: String,
    val nameSwedish: String,
    val nameEnglish: String,
    val addressFinnish: String,
    val addressSwedish: String,
    val cityFinnish: String,
    val citySwedish: String,
    val operator: String,
    val capacity: Int,
    val longitude: Double,
    val latitude: Double
)
