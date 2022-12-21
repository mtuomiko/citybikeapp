package com.mtuomiko.citybikeapp.dao

import com.mtuomiko.citybikeapp.model.StationNew
import java.time.Instant

data class StationInsert(
    val id: Int?,
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
) {
    companion object {
        fun from(stationNew: StationNew, time: Instant) = with(stationNew) {
            StationInsert(
                id,
                nameFinnish,
                nameSwedish,
                nameEnglish,
                addressFinnish,
                addressSwedish,
                cityFinnish,
                citySwedish,
                operator,
                capacity,
                longitude,
                latitude,
                time,
                time
            )
        }
    }
}
