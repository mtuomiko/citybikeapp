package com.mtuomiko.citybikeapp

import com.mtuomiko.citybikeapp.dao.repository.JourneyRepository
import com.mtuomiko.citybikeapp.dao.repository.StationRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.testcontainers.junit.jupiter.Testcontainers
import java.nio.file.Path

private const val TEST_ROOT = "src/test/resources"
private const val STATION_PATH = "$TEST_ROOT/stations.csv"
private const val JOURNEY_PATHS = "$TEST_ROOT/journeys.csv"

@Testcontainers
@SpringBootTest
@TestPropertySource(properties = ["citybikeapp.dataLoader.stationUrl=$STATION_PATH"])
@TestPropertySource(properties = ["citybikeapp.dataLoader.journeyUrls=$JOURNEY_PATHS"])
@ActiveProfiles(profiles = ["test", "dataloader"])
class DataLoaderTest {
    @Autowired
    private lateinit var dataLoader: DataLoader

    @Autowired
    private lateinit var journeyRepository: JourneyRepository

    @Autowired
    private lateinit var stationRepository: StationRepository

    @Test
    fun `Using CSV files including malformed and duplicate data, loader loads only unique and valid entities`() {
        dataLoader.run()

        val allJourneys = journeyRepository.getCount()
        val allStations = stationRepository.findAll()
        // TODO: Use more readable testing methods. Magic numbers below are based on the csv file contents
        assertThat(allJourneys).isEqualTo(8)
        assertThat(allStations).hasSize(10)
    }
}

@Component
@Profile(value = ["test"])
class TestFileProvider : FileProvider {
    override fun getByURI(uri: String): Path {
        val path = Path.of(uri)
        return path
    }

    override fun deleteFiles() {
        // NOOP
    }
}
