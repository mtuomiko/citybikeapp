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
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import com.mtuomiko.citybikeapp.gen.model.Station as APIStation
import com.mtuomiko.citybikeapp.gen.model.StationDetails as APIStationDetails
import com.mtuomiko.citybikeapp.gen.model.StationLimited as APIStationLimited
import com.mtuomiko.citybikeapp.gen.model.StationStatistics as APIStationStatistics

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StationIntegrationTest {
    @Autowired
    private lateinit var testRestTemplate: TestRestTemplate

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
        val response = testRestTemplate.getForEntity("/station/limited", StationsLimitedResponse::class.java)
        val expected = stationRepository.findAll().map { APIStationLimited(it.id.toString(), it.nameFinnish) }
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body!!.stations).hasSize(testStations.size)
        assertThat(response.body!!.stations).containsExactlyInAnyOrderElementsOf(expected)
    }

    @Test
    fun `Single station endpoint responds with station details`() {
        val testStation = testStations.last()
        val response = testRestTemplate.getForEntity("/station/${testStation.id}", StationDetailsResponse::class.java)

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

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body!!.station).isEqualTo(expectedStationsDetails)
    }

    @Test
    fun `Stations endpoint can be used without params and response uses default pagination`() {
        val response = testRestTemplate.getForEntity("/station", StationsResponse::class.java)

        val expected =
            testStations
                .sortedBy { it.id }
                .take(svcConfig.defaultPageSize)
                .map { it.toApi() }

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body!!.stations).containsExactlyInAnyOrderElementsOf(expected)
        assertThat(response.body!!.meta.totalPages).isEqualTo(4)
    }

    @Test
    fun `Stations endpoint can be used with search term and response uses default pagination`() {
        val response = testRestTemplate.getForEntity("/station?search=toinen", StationsResponse::class.java)

        val expected =
            testStations
                .filter { it.nameFinnish.contains("toinen", ignoreCase = true) }
                .sortedBy { it.id }
                .take(svcConfig.defaultPageSize)
                .map { it.toApi() }

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body!!.stations).containsExactlyInAnyOrderElementsOf(expected)
        assertThat(response.body!!.meta.totalPages).isEqualTo(2)
    }

    @Test
    fun `Stations endpoint can be used with page number and response uses default pagination`() {
        val response = testRestTemplate.getForEntity("/station?page=1", StationsResponse::class.java)

        val expected =
            testStations
                .sortedBy { it.id }
                .drop(svcConfig.defaultPageSize)
                .take(svcConfig.defaultPageSize)
                .map { it.toApi() }

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body!!.stations).containsExactlyInAnyOrderElementsOf(expected)
        assertThat(response.body!!.meta.totalPages).isEqualTo(4)
    }

    @Test
    fun `Stations endpoint can be used with search term and page number and response uses default pagination`() {
        val response = testRestTemplate.getForEntity("/station?search=toinen&page=1", StationsResponse::class.java)

        val expected =
            testStations
                .filter { it.nameFinnish.contains("toinen", ignoreCase = true) }
                .sortedBy { it.id }
                .drop(svcConfig.defaultPageSize)
                .take(svcConfig.defaultPageSize)
                .map { it.toApi() }

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body!!.stations).containsExactlyInAnyOrderElementsOf(expected)
        assertThat(response.body!!.meta.totalPages).isEqualTo(2)
    }

    @Test
    fun `Stations endpoint can be used with search term and page size`() {
        val pageSize = 25
        val response = testRestTemplate.getForEntity("/station?search=toinen&pageSize=$pageSize", StationsResponse::class.java)

        val expected =
            testStations
                .filter { it.nameFinnish.contains("toinen", ignoreCase = true) }
                .sortedBy { it.id }
                .take(pageSize)
                .map { it.toApi() }

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body!!.stations).containsExactlyInAnyOrderElementsOf(expected)
        assertThat(response.body!!.meta.totalPages).isEqualTo(4)
    }

    @Test
    fun `Stations endpoint can be used with search term, page number and page size`() {
        val page = 2
        val pageSize = 25
        val response = testRestTemplate.getForEntity("/station?search=toinen&page=$page&pageSize=$pageSize", StationsResponse::class.java)

        val expected =
            testStations
                .filter { it.nameFinnish.contains("toinen", ignoreCase = true) }
                .sortedBy { it.id }
                .drop(page * pageSize)
                .take(pageSize)
                .map { it.toApi() }

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body!!.stations).containsExactlyInAnyOrderElementsOf(expected)
        assertThat(response.body!!.meta.totalPages).isEqualTo(4)
    }

    @Test
    fun `Station statistics endpoint`() {
        val testStation = testStations.last()
        val response = testRestTemplate.getForEntity("/station/${testStation.id}/statistics", StatisticsResponse::class.java)

        // no journeys in test data so stats should be zero/empty
        val expectedStatistics = APIStationStatistics(0, 0, 0.0, 0.0, emptyList(), emptyList())

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body!!.statistics).isEqualTo(expectedStatistics)
    }

    private fun StationEntity.toApi() = APIStation(id.toString(), nameFinnish, addressFinnish, cityFinnish, operator, capacity)
}
