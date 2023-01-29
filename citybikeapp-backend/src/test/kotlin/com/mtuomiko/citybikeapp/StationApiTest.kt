package com.mtuomiko.citybikeapp

import com.mtuomiko.citybikeapp.api.model.APIStation
import com.mtuomiko.citybikeapp.api.model.APIStationDetails
import com.mtuomiko.citybikeapp.api.model.APIStationLimited
import com.mtuomiko.citybikeapp.api.model.APIStationStatistics
import com.mtuomiko.citybikeapp.api.model.StationDetailsResponse
import com.mtuomiko.citybikeapp.api.model.StationsLimitedResponse
import com.mtuomiko.citybikeapp.api.model.StationsResponse
import com.mtuomiko.citybikeapp.api.model.StatisticsResponse
import com.mtuomiko.citybikeapp.dao.builder.StationEntityBuilder
import com.mtuomiko.citybikeapp.dao.entity.StationEntity
import com.mtuomiko.citybikeapp.dao.repository.StationRepository
import com.mtuomiko.citybikeapp.svc.PaginationConfig
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

    @Inject
    lateinit var paginationConfig: PaginationConfig

    private lateinit var client: BlockingHttpClient
    private lateinit var testStations: List<StationEntity>

    @BeforeEach
    fun setup() {
        client = stationClient.toBlocking()

        val newStations = List(100) { StationEntityBuilder().id(it + 1).build() } +
            List(100) {
                StationEntityBuilder().id(it + 101)
                    .nameFinnish("Toinen Paikka")
                    .nameSwedish("Andra Platsen")
                    .nameEnglish("Other Place")
                    .build()
            }
        val createdStations = stationRepository.saveAll(newStations)
        testStations = createdStations.sortedBy { it.id }.toList()
    }

    @AfterEach
    fun cleanup() {
        stationRepository.deleteAll()
    }

    @Test
    fun `All stations with limited information endpoint responds successfully`() {
        val request = HttpRequest.GET<Any>("/limited")
        val response = client.exchange(request, StationsLimitedResponse::class.java)

        val expected = stationRepository.findAll().map { APIStationLimited(it.id.toString(), it.nameFinnish) }
        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.body()!!.stations).hasSize(testStations.size)
        assertThat(response.body()!!.stations).containsExactlyInAnyOrderElementsOf(expected)
    }

    @Test
    fun `Single station endpoint responds with station details`() {
        val testStation = testStations.last()
        val request = HttpRequest.GET<Any>("/${testStation.id}")
        val response = client.exchange(request, StationDetailsResponse::class.java)

        val expectedStationsDetails = with(testStation) {
            APIStationDetails(
                id.toString(),
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

        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.body()!!.station).isEqualTo(expectedStationsDetails)
    }

    @Test
    fun `Stations endpoint can be used without params and response uses default pagination`() {
        val request = HttpRequest.GET<Any>("")
        val response = client.exchange(request, StationsResponse::class.java)

        val expected = testStations
            .sortedBy { it.id }
            .take(paginationConfig.defaultPageSize)
            .map { it.toApi() }

        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.body()!!.stations).containsExactlyInAnyOrderElementsOf(expected)
        assertThat(response.body()!!.meta.totalPages).isEqualTo(4)
    }

    @Test
    fun `Stations endpoint can be used with search term and response uses default pagination`() {
        val request = HttpRequest.GET<Any>("?search=toinen")
        val response = client.exchange(request, StationsResponse::class.java)

        val expected = testStations
            .filter { it.nameFinnish.contains("toinen", ignoreCase = true) }
            .sortedBy { it.id }
            .take(paginationConfig.defaultPageSize)
            .map { it.toApi() }

        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.body()!!.stations).containsExactlyInAnyOrderElementsOf(expected)
        assertThat(response.body()!!.meta.totalPages).isEqualTo(2)
    }

    @Test
    fun `Stations endpoint can be used with page number and response uses default pagination`() {
        val request = HttpRequest.GET<Any>("?page=1")
        val response = client.exchange(request, StationsResponse::class.java)

        val expected = testStations
            .sortedBy { it.id }
            .drop(paginationConfig.defaultPageSize)
            .take(paginationConfig.defaultPageSize)
            .map { it.toApi() }

        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.body()!!.stations).containsExactlyInAnyOrderElementsOf(expected)
        assertThat(response.body()!!.meta.totalPages).isEqualTo(4)
    }

    @Test
    fun `Stations endpoint can be used with search term and page number and response uses default pagination`() {
        val request = HttpRequest.GET<Any>("?search=toinen&page=1")
        val response = client.exchange(request, StationsResponse::class.java)

        val expected = testStations
            .filter { it.nameFinnish.contains("toinen", ignoreCase = true) }
            .sortedBy { it.id }
            .drop(paginationConfig.defaultPageSize)
            .take(paginationConfig.defaultPageSize)
            .map { it.toApi() }

        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.body()!!.stations).containsExactlyInAnyOrderElementsOf(expected)
        assertThat(response.body()!!.meta.totalPages).isEqualTo(2)
    }

    @Test
    fun `Stations endpoint can be used with search term and page size`() {
        val pageSize = 25
        val request = HttpRequest.GET<Any>("?search=toinen&pageSize=$pageSize")
        val response = client.exchange(request, StationsResponse::class.java)

        val expected = testStations
            .filter { it.nameFinnish.contains("toinen", ignoreCase = true) }
            .sortedBy { it.id }
            .take(pageSize)
            .map { it.toApi() }

        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.body()!!.stations).containsExactlyInAnyOrderElementsOf(expected)
        assertThat(response.body()!!.meta.totalPages).isEqualTo(4)
    }

    @Test
    fun `Stations endpoint can be used with search term, page number and page size`() {
        val page = 2
        val pageSize = 25
        val request = HttpRequest.GET<Any>("?search=toinen&page=$page&pageSize=$pageSize")
        val response = client.exchange(request, StationsResponse::class.java)

        val expected = testStations
            .filter { it.nameFinnish.contains("toinen", ignoreCase = true) }
            .sortedBy { it.id }
            .drop(page * pageSize)
            .take(pageSize)
            .map { it.toApi() }

        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.body()!!.stations).containsExactlyInAnyOrderElementsOf(expected)
        assertThat(response.body()!!.meta.totalPages).isEqualTo(4)
    }

    @Test
    fun `Station statistics endpoint`() {
        val testStation = testStations.last()
        val request = HttpRequest.GET<Any>("/${testStation.id}/statistics")
        val response = client.exchange(request, StatisticsResponse::class.java)

        // no journeys in test data so stats should be zero/empty
        val expectedStatistics = APIStationStatistics(0, 0, 0.0, 0.0, emptyList(), emptyList())

        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.body()!!.statistics).isEqualTo(expectedStatistics)
    }

    private fun StationEntity.toApi() =
        APIStation(id.toString(), nameFinnish, addressFinnish, cityFinnish, operator, capacity)
}
