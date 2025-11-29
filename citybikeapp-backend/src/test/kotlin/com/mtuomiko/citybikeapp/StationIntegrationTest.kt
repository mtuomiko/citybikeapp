package com.mtuomiko.citybikeapp

import com.mtuomiko.citybikeapp.dao.builder.StationEntityBuilder
import com.mtuomiko.citybikeapp.dao.entity.StationEntity
import com.mtuomiko.citybikeapp.dao.repository.JourneyRepository
import com.mtuomiko.citybikeapp.dao.repository.StationRepository
import com.mtuomiko.citybikeapp.gen.model.StationDetailsResponse
import com.mtuomiko.citybikeapp.gen.model.StationsLimitedResponse
import com.mtuomiko.citybikeapp.gen.model.StationsResponse
import com.mtuomiko.citybikeapp.gen.model.StatisticsResponse
import com.mtuomiko.citybikeapp.svc.SvcConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.client.RestTestClient
import com.mtuomiko.citybikeapp.gen.model.Station as APIStation
import com.mtuomiko.citybikeapp.gen.model.StationDetails as APIStationDetails
import com.mtuomiko.citybikeapp.gen.model.StationLimited as APIStationLimited
import com.mtuomiko.citybikeapp.gen.model.StationStatistics as APIStationStatistics

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class StationIntegrationTest {
    @Autowired
    private lateinit var client: RestTestClient

    @Autowired
    lateinit var journeyRepository: JourneyRepository

    @Autowired
    lateinit var stationRepository: StationRepository

    @Autowired
    lateinit var svcConfig: SvcConfig

    private lateinit var testStations: List<StationEntity>

    @BeforeEach
    fun setup() {
        journeyRepository.deleteAll()
        stationRepository.deleteAll()

        val newStations =
            List(100) { StationEntityBuilder().id(it + 1).build() } +
                List(100) {
                    StationEntityBuilder()
                        .id(it + 101)
                        .nameFinnish("Toinen Paikka")
                        .nameSwedish("Andra Platsen")
                        .nameEnglish("Other Place")
                        .build()
                }
        val createdStations = stationRepository.saveAll(newStations)
        testStations = createdStations.sortedBy { it.id }.toList()
    }

    @Test
    fun `All stations with limited information endpoint responds successfully`() {
        val response =
            client
                .get()
                .uri("/station/limited")
                .exchange()
                .expectBody(StationsLimitedResponse::class.java)
                .returnResult()
        val expected = stationRepository.findAll().map { APIStationLimited(it.id.toString(), it.nameFinnish) }
        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.responseBody!!.stations).hasSize(testStations.size)
        assertThat(response.responseBody!!.stations).containsExactlyInAnyOrderElementsOf(expected)
    }

    @Test
    fun `Single station endpoint responds with station details`() {
        val testStation = testStations.last()
        val response =
            client
                .get()
                .uri("/station/${testStation.id}")
                .exchange()
                .expectBody(StationDetailsResponse::class.java)
                .returnResult()

        val expectedStationsDetails =
            with(testStation) {
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
                    latitude,
                )
            }

        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.responseBody!!.station).isEqualTo(expectedStationsDetails)
    }

    @Test
    fun `Stations endpoint can be used without params and response uses default pagination`() {
        val response =
            client
                .get()
                .uri("/station")
                .exchange()
                .expectBody(StationsResponse::class.java)
                .returnResult()

        val expected =
            testStations
                .sortedBy { it.id }
                .take(svcConfig.defaultPageSize)
                .map { it.toApi() }

        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.responseBody!!.stations).containsExactlyInAnyOrderElementsOf(expected)
        assertThat(response.responseBody!!.meta.totalPages).isEqualTo(4)
    }

    @Test
    fun `Stations endpoint can be used with search term and response uses default pagination`() {
        val response =
            client
                .get()
                .uri("/station?search=toinen")
                .exchange()
                .expectBody(StationsResponse::class.java)
                .returnResult()

        val expected =
            testStations
                .filter { it.nameFinnish.contains("Toinen") }
                .sortedBy { it.id }
                .take(svcConfig.defaultPageSize)
                .map { it.toApi() }

        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.responseBody!!.stations).containsExactlyInAnyOrderElementsOf(expected)
        assertThat(response.responseBody!!.meta.totalPages).isEqualTo(2)
    }

    @Test
    fun `Stations endpoint can be used with upper case search term and response finds non-exact-case matches`() {
        val response =
            client
                .get()
                .uri("/station?search=PaIkKa")
                .exchange()
                .expectBody(StationsResponse::class.java)
                .returnResult()

        val expected =
            testStations
                .filter { it.nameFinnish.contains("Paikka") }
                .sortedBy { it.id }
                .take(svcConfig.defaultPageSize)
                .map { it.toApi() }

        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.responseBody!!.stations).containsExactlyInAnyOrderElementsOf(expected)
        assertThat(response.responseBody!!.meta.totalPages).isEqualTo(4)
    }

    @Test
    fun `Stations endpoint can be used with page number and response uses default pagination`() {
        val response =
            client
                .get()
                .uri("/station?page=1")
                .exchange()
                .expectBody(StationsResponse::class.java)
                .returnResult()

        val expected =
            testStations
                .sortedBy { it.id }
                .drop(svcConfig.defaultPageSize)
                .take(svcConfig.defaultPageSize)
                .map { it.toApi() }

        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.responseBody!!.stations).containsExactlyInAnyOrderElementsOf(expected)
        assertThat(response.responseBody!!.meta.totalPages).isEqualTo(4)
    }

    @Test
    fun `Stations endpoint can be used with search term and page number and response uses default pagination`() {
        val response =
            client
                .get()
                .uri("/station?search=toinen&page=1")
                .exchange()
                .expectBody(StationsResponse::class.java)
                .returnResult()

        val expected =
            testStations
                .filter { it.nameFinnish.contains("toinen", ignoreCase = true) }
                .sortedBy { it.id }
                .drop(svcConfig.defaultPageSize)
                .take(svcConfig.defaultPageSize)
                .map { it.toApi() }

        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.responseBody!!.stations).containsExactlyInAnyOrderElementsOf(expected)
        assertThat(response.responseBody!!.meta.totalPages).isEqualTo(2)
    }

    @Test
    fun `Stations endpoint can be used with search term and page size`() {
        val pageSize = 25
        val response =
            client
                .get()
                .uri("/station?search=toinen&pageSize=$pageSize")
                .exchange()
                .expectBody(StationsResponse::class.java)
                .returnResult()

        val expected =
            testStations
                .filter { it.nameFinnish.contains("toinen", ignoreCase = true) }
                .sortedBy { it.id }
                .take(pageSize)
                .map { it.toApi() }

        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.responseBody!!.stations).containsExactlyInAnyOrderElementsOf(expected)
        assertThat(response.responseBody!!.meta.totalPages).isEqualTo(4)
    }

    @Test
    fun `Stations endpoint can be used with search term, page number and page size`() {
        val page = 2
        val pageSize = 25
        val response =
            client
                .get()
                .uri("/station?search=toinen&page=$page&pageSize=$pageSize")
                .exchange()
                .expectBody(StationsResponse::class.java)
                .returnResult()

        val expected =
            testStations
                .filter { it.nameFinnish.contains("toinen", ignoreCase = true) }
                .sortedBy { it.id }
                .drop(page * pageSize)
                .take(pageSize)
                .map { it.toApi() }

        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.responseBody!!.stations).containsExactlyInAnyOrderElementsOf(expected)
        assertThat(response.responseBody!!.meta.totalPages).isEqualTo(4)
    }

    @Test
    fun `Station statistics endpoint`() {
        val testStation = testStations.last()
        val response =
            client
                .get()
                .uri("/station/${testStation.id}/statistics")
                .exchange()
                .expectBody(StatisticsResponse::class.java)
                .returnResult()

        // no journeys in test data so stats should be zero/empty
        val expectedStatistics = APIStationStatistics(0, 0, 0.0, 0.0, emptyList(), emptyList())

        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.responseBody!!.statistics).isEqualTo(expectedStatistics)
    }

    private fun StationEntity.toApi() = APIStation(id.toString(), nameFinnish, addressFinnish, cityFinnish, operator, capacity)
}
