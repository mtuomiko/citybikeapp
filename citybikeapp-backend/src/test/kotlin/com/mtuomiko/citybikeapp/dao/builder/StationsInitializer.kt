package com.mtuomiko.citybikeapp.dao.builder

import com.mtuomiko.citybikeapp.dao.entity.StationEntity
import com.mtuomiko.citybikeapp.dao.repository.StationRepository

class StationsInitializer(private val repository: StationRepository) {
    var firstId: Int = 1
    var count: Int = 10
    var namePrefixFinnish: String = "Joku Paikka"
    var namePrefixSwedish: String = "Någon Plats"
    var namePrefixEnglish: String = "Some Place"

    fun firstId(id: Int) = apply { this.firstId = id }
    fun count(count: Int) = apply { this.count = count }
    fun finnishNamePrefix(prefix: String) = apply { this.namePrefixFinnish = prefix }

    fun save(): List<StationEntity> {
        val newStations = List(count) {
            StationEntityBuilder()
                .id(firstId + it)
                .nameFinnish("$namePrefixFinnish ${firstId + it}")
                .nameSwedish("$namePrefixSwedish ${firstId + it}")
                .nameEnglish("$namePrefixEnglish ${firstId + it}")
                .build()
        }
        return repository.saveAll(newStations)
    }
}
