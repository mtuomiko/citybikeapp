package com.mtuomiko.citybikeapp.dao

import com.mtuomiko.citybikeapp.FlywayConfig
import com.mtuomiko.citybikeapp.common.InstantSourceFactory
import com.mtuomiko.citybikeapp.dao.builder.StationEntityBuilder
import com.mtuomiko.citybikeapp.dao.repository.JourneyRepository
import com.mtuomiko.citybikeapp.dao.repository.StationRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jooq.test.autoconfigure.JooqTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@JooqTest
@Import(value = [JourneyRepository::class, StationRepository::class, InstantSourceFactory::class, FlywayConfig::class])
@Transactional(propagation = Propagation.NOT_SUPPORTED) // don't run tests within a transaction
private class StationRepositoryTest {
    @Autowired
    private lateinit var journeyRepository: JourneyRepository

    @Autowired
    private lateinit var stationRepository: StationRepository

    @BeforeEach
    fun cleanup() {
        journeyRepository.deleteAll()
        stationRepository.deleteAll()
    }

    @Test
    fun `Valid station entity can be saved and queried`() {
        val stationEntity = StationEntityBuilder().id(1).build()

        val returnedStation = stationRepository.save(stationEntity)

        val queriedStation = stationRepository.findById(returnedStation.id)!!

        assertThat(queriedStation == returnedStation)
        assertThat(stationEntity == returnedStation)
    }
}
