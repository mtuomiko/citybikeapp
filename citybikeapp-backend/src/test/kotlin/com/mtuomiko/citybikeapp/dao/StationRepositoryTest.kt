package com.mtuomiko.citybikeapp.dao

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test

@MicronautTest
private class StationRepositoryTest(
    private val stationRepository: StationRepository
) {
    @Test
    fun `Valid station entity can be saved and queried`() {
        val stationEntity = StationBuilder().build()
        val returnedStation = stationRepository.save(stationEntity)

        stationRepository.findById(returnedStation.id).get()
    }
}
