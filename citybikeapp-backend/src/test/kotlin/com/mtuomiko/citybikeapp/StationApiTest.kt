package com.mtuomiko.citybikeapp

import com.mtuomiko.citybikeapp.api.model.APIStation
import com.mtuomiko.citybikeapp.api.model.APIStationDetails
import com.mtuomiko.citybikeapp.api.model.APIStationLimited
import com.mtuomiko.citybikeapp.api.model.APIStationStatistics
import com.mtuomiko.citybikeapp.api.model.StationDetailsWithStatisticsResponse
import com.mtuomiko.citybikeapp.api.model.StationsLimitedResponse
import com.mtuomiko.citybikeapp.api.model.StationsResponse
import com.mtuomiko.citybikeapp.dao.builder.StationEntityBuilder
import com.mtuomiko.citybikeapp.dao.entity.StationEntity
import com.mtuomiko.citybikeapp.dao.repository.StationRepository
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@MicronautTest
class StationApiTest {

    @Inject
    @field:Client("/station")
    private lateinit var stationClient: HttpClient

    @Inject
    lateinit var stationRepository: StationRepository

    private lateinit var client: BlockingHttpClient
    private lateinit var testStations: List<StationEntity>

    @BeforeEach
    fun setup() {
        client = stationClient.toBlocking()

        val newStations = List(10) { StationEntityBuilder().id(it + 1).build() } +
            List(10) {
                StationEntityBuilder().id(it + 11)
                    .nameFinnish("Toinen Paikka")
                    .nameSwedish("Andra Platsen")
                    .nameEnglish("Other Place")
                    .build()
            }
        val createdStations = stationRepository.saveAll(newStations)
        testStations = createdStations.toList()
    }

    @AfterEach
    fun cleanup() {
        stationRepository.deleteAll()
    }

    @Test
    fun `All stations with limited information endpoint responds successfully`() {
        val request = HttpRequest.GET<Any>("/limited")
        val response = client.exchange(request, StationsLimitedResponse::class.java)

        val expected = stationRepository.findAll().map { APIStationLimited(it.id, it.nameFinnish) }
        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.body()!!.stations).hasSize(testStations.size)
        assertThat(response.body()!!.stations).containsExactlyInAnyOrderElementsOf(expected)
    }

    @Test
    fun `Single station endpoint responds with details`() {
        val testStation = testStations.last()
        val request = HttpRequest.GET<Any>("/${testStation.id}")
        val response = client.exchange(request, StationDetailsWithStatisticsResponse::class.java)

        val expectedStationsDetails = with(testStation) {
            APIStationDetails(
                id,
                nameFinnish,
                nameSwedish,
                nameEnglish,
                addressFinnish,
                addressSwedish,
                cityFinnish,
                citySwedish,
                operator,
                capacity,
                longitude,
                latitude
            )
        }
        val expectedStatistics = APIStationStatistics(0, 0, 0.0, 0.0, emptyList(), emptyList())

        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.body()!!.station).isEqualTo(expectedStationsDetails)
        assertThat(response.body()!!.statistics).isEqualTo(expectedStatistics)
    }

    @Test
    fun `Stations endpoint can be used with search term`() {
        val request = HttpRequest.GET<Any>("?search=toinen")
        val response = client.exchange(request, StationsResponse::class.java)

        val expected = testStations
            .filter { it.nameFinnish.contains("toinen", ignoreCase = true) }
            .map { with(it) { APIStation(id, nameFinnish, addressFinnish, cityFinnish, operator, capacity) } }

        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.body()!!.stations).containsExactlyInAnyOrderElementsOf(expected)
    }
}
