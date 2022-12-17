package com.mtuomiko.citybikeapp.dao

import io.micronaut.data.annotation.DateCreated
import io.micronaut.data.annotation.DateUpdated
import io.micronaut.data.annotation.MappedEntity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.Instant

@MappedEntity(value = "station")
data class StationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    val nameFinnish: String,
    val nameSwedish: String,
    val nameEnglish: String,
    val addressFinnish: String,
    val addressSwedish: String,
    val cityFinnish: String? = null,
    val citySwedish: String? = null,
    val operator: String? = null,
    val capacity: Int,
    val longitude: Double,
    val latitude: Double
) {
    @field:DateUpdated
    lateinit var modifiedAt: Instant

    @field:DateCreated
    lateinit var createdAt: Instant
}
