package com.mtuomiko.citybikeapp.dao

import com.mtuomiko.citybikeapp.dao.builder.StationEntityBuilder
import com.mtuomiko.citybikeapp.dao.entity.StationEntity
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
        val stationEntity = StationEntityBuilder().build()

        val returnedStation = stationRepository.save(stationEntity)

        val queriedStation = stationRepository.findById(returnedStation.id).get()

        assertThat(stationsEqualExceptIDAndTimestamps(queriedStation, stationEntity)).isTrue
        assertThat(queriedStation.id).isNotEqualTo(0)
        assertThat(queriedStation.modifiedAt).isNotNull
        assertThat(queriedStation.createdAt).isNotNull
    }

    private fun stationsEqualExceptIDAndTimestamps(station: StationEntity, other: StationEntity): Boolean {
        val toCompare = station.copy(id = other.id, modifiedAt = other.modifiedAt, createdAt = other.createdAt)
        return toCompare == other
    }
}
