package com.mtuomiko.citybikeapp.dao.entity

import java.time.Instant

data class StationEntity(
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
    val latitude: Double,
    val modifiedAt: Instant,
    val createdAt: Instant
)
