package com.mtuomiko.citybikeapp

import com.mtuomiko.citybikeapp.dao.JourneyRepository
import com.mtuomiko.citybikeapp.dao.StationRepository
import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Replaces
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.InputStream

private const val stationDummyUrl = "stationDummyUrl"
private const val journeysDummyUrl = "journeyDummyUrl1,journeyDummyUrl2,journeyDummyUrl3"
private val splitJourneyUrls = journeysDummyUrl.split(',')

@MicronautTest
@Property(name = "citybikeapp.dataLoader.stationUrl", value = stationDummyUrl)
@Property(name = "citybikeapp.dataLoader.journeyUrls", value = journeysDummyUrl)
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
        // TODO: magic numbers based on csv file, should make this more reasonable
        assertThat(allJourneys).hasSize(8)
        assertThat(allStations).hasSize(10)
    }
}

@Replaces(FileProvider::class)
@Singleton
class MockFileProvider : FileProvider {
    override fun getLocalInputStream(url: String): InputStream =
        when (url) {
            stationDummyUrl -> ClassLoader.getSystemResource("stations.csv").openStream()
            in splitJourneyUrls -> ClassLoader.getSystemResource("journeys.csv").openStream()
            else -> throw Exception("no preset source for $url")
        }
}
