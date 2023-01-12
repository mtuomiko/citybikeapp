package com.mtuomiko.citybikeapp.dao

import com.mtuomiko.citybikeapp.dao.builder.StationEntityBuilder
import com.mtuomiko.citybikeapp.dao.repository.StationRepository
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@MicronautTest
private class StationRepositoryTest(
    private val stationRepository: StationRepository
) {
    @Test
    fun `Valid station entity can be saved and queried`() {
        val stationEntity = StationEntityBuilder().id(1).build()

        val returnedStation = stationRepository.save(stationEntity)

        val queriedStation = stationRepository.findById(returnedStation.id)!!

        assertThat(queriedStation == returnedStation)
        assertThat(stationEntity == returnedStation)
    }
}
