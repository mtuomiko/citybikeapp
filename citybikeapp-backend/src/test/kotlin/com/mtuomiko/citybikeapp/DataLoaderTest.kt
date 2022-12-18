package com.mtuomiko.citybikeapp

import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.env.PropertySource
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Singleton
import org.junit.jupiter.api.Test
import java.io.InputStream

private const val stationDummyUrl = "stationDummyUrl"
private const val journeysDummyUrl = "journeyDummyUrl1,journeyDummyUrl2,journeyDummyUrl3"
private val splitJourneyUrls = journeysDummyUrl.split(',')

@MicronautTest
class DataLoaderTest {
    private val testPropertySource = PropertySource.of(
        "test",
        mapOf(
            "citybikeapp.dataLoader.stationUrl" to stationDummyUrl,
            "citybikeapp.dataLoader.journeyUrls" to journeysDummyUrl
        )
    )
    private val applicationContext = ApplicationContext.run(testPropertySource)

    @Test
    fun `Data Loader runs`() {
        PicocliRunner.run(DataLoader::class.java, applicationContext)
    }
}

@Replaces(FileProvider::class)
@Singleton
class MockFileProvider : FileProvider {
    override fun getLocalInputStream(url: String): InputStream {
        return when (url) {
            stationDummyUrl -> return ClassLoader.getSystemResource("stations.csv").openStream()
            in splitJourneyUrls -> return ClassLoader.getSystemResource("journeys.csv").openStream()
            else -> throw Exception("no preset source for $url")
        }
    }
}
