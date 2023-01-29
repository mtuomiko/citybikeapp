package com.mtuomiko.citybikeapp

import com.mtuomiko.citybikeapp.dao.repository.JourneyRepository
import com.mtuomiko.citybikeapp.dao.repository.StationRepository
import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.InputStream

private const val stationDummyUrl = "stationDummyUrl"
private const val journeysDummyUrls = "journeyDummyUrl1,journeyDummyUrl2,journeyDummyUrl3"

@MicronautTest
@Property(name = "citybikeapp.dataLoader.stationUrl", value = stationDummyUrl)
@Property(name = "citybikeapp.dataLoader.journeyUrls", value = journeysDummyUrls)
class DataLoaderTest {

    @Inject
    private lateinit var applicationContext: ApplicationContext

    @Inject
    private lateinit var journeyRepository: JourneyRepository

    @Inject
    private lateinit var stationRepository: StationRepository

    @Test
    fun `Using CSV files with malformed and duplicate data, loader loads only unique and valid entities`() {
        PicocliRunner.run(DataLoader::class.java, applicationContext)

        val allJourneys = journeyRepository.findAll()
        val allStations = stationRepository.findAll()
        // TODO: Use more readable testing methods. Magic numbers below are based on the csv file contents
        assertThat(allJourneys).hasSize(8)
        assertThat(allStations).hasSize(10)
    }

    @MockBean(FileProvider::class) // using MockBean for test class scope
    private fun fileProvider(): FileProvider {
        return MockFileProvider()
    }
}

class MockFileProvider : FileProvider {
    private val splitJourneyUrls = journeysDummyUrls.split(',')

    override fun getLocalInputStream(url: String): InputStream =
        when (url) {
            stationDummyUrl -> ClassLoader.getSystemResource("stations.csv").openStream()
            in splitJourneyUrls -> ClassLoader.getSystemResource("journeys.csv").openStream()
            else -> throw Exception("no preset source for $url")
        }

    override fun deleteFiles() {
        // NOOP
    }
}
