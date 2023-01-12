package com.mtuomiko.citybikeapp.dao.builder

import com.mtuomiko.citybikeapp.dao.entity.StationEntity
import java.time.Instant

class StationEntityBuilder {
    var id: Int = 0
    var nameFinnish: String = "Joku Paikka"
    var nameSwedish: String = "Någon Plats"
    var nameEnglish: String = "Some Place"
    var addressFinnish: String = "Katukatu 8"
    var addressSwedish: String = "Gatagatan 8"
    var cityFinnish: String = "Kaupunni"
    var citySwedish: String = "Stad"
    var operator: String = "BOFH"
    var capacity: Int = 10
    var longitude: Double = 24.9897538768656
    var latitude: Double = 60.2807560752423
    var modifiedAt: Instant = Instant.parse("2022-11-15T18:35:24Z")
    var createdAt: Instant = Instant.parse("2022-11-15T18:35:24Z")

    fun id(id: Int) = apply { this.id = id }
    fun nameFinnish(name: String) = apply { this.nameFinnish = name }
    fun nameSwedish(name: String) = apply { this.nameSwedish = name }
    fun nameEnglish(name: String) = apply { this.nameEnglish = name }

    fun build() = StationEntity(
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
        modifiedAt,
        createdAt
    )
}
