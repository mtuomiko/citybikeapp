package com.mtuomiko.citybikeapp.model

import io.micronaut.serde.annotation.Serdeable

@Serdeable
class TopStation(
    val id: Int,
    val nameFinnish: String,
    val nameSwedish: String,
    val nameEnglish: String,
    val journeyCount: Long
)
