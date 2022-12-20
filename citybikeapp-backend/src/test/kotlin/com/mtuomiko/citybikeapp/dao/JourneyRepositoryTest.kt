package com.mtuomiko.citybikeapp.dao

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Test

@MicronautTest
class JourneyRepositoryTest(
    @Inject
    private val journeyRepository: JourneyRepository
) {
    /**
     * TODO: Add test data generation for meaningful testing
     */
    @Test
    fun `Trip statistics query can be run`() {
        val result = journeyRepository.getTripStatisticsByStationId(100)
    }

    @Test
    fun `Top stations query can be run`() {
        val result = journeyRepository.getTopStationsByStationId(100)
    }
}
