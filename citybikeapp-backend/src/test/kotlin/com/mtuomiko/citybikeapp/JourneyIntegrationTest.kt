package com.mtuomiko.citybikeapp

import com.mtuomiko.citybikeapp.dao.builder.JourneyEntityBuilder
import com.mtuomiko.citybikeapp.dao.builder.StationEntityBuilder
import com.mtuomiko.citybikeapp.dao.entity.JourneyEntity
import com.mtuomiko.citybikeapp.dao.repository.JourneyRepository
import com.mtuomiko.citybikeapp.dao.repository.StationRepository
import com.mtuomiko.citybikeapp.gen.model.JourneysResponse
import com.mtuomiko.citybikeapp.svc.SvcConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.util.UriComponentsBuilder
import java.time.Instant
import com.mtuomiko.citybikeapp.gen.model.Journey as APIJourney

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JourneyIntegrationTest {
    @Autowired
    private lateinit var testRestTemplate: TestRestTemplate

    @Autowired
    private lateinit var journeyRepository: JourneyRepository

    @Autowired
    private lateinit var stationRepository: StationRepository

    @Autowired
    private lateinit var paginationConfig: SvcConfig

    private lateinit var testJourneys: List<JourneyEntity>

    @BeforeEach
    fun setup() {
        journeyRepository.deleteAll()
        stationRepository.deleteAll()

        val start = Instant.parse("2020-02-03T11:00:00Z")
        // add two stations
        stationRepository.saveAll(List(2) { StationEntityBuilder().id(it + 1).build() })
        // add 150 journeys from station one to station two
        val newJourneys =
            List(150) {
                val durationSeconds = 600 + (it * 10)
                JourneyEntityBuilder()
                    .id(it + 1L)
                    .departureAt(start.plusSeconds(it * 10L))
                    .arrivalAt(start.plusSeconds(durationSeconds.toLong()))
                    .departureStationId(1)
                    .arrivalStationId(2)
                    .distance(1000 + 10 * it)
                    .duration(durationSeconds)
                    .build()
            }
        val createdJourneys = journeyRepository.saveAll(newJourneys)
        testJourneys = createdJourneys
    }

    @Test
    fun `journey endpoint returns with expected json format`() {
        // Checking the actual json string representation in case of unintentional serialization changes
        val response = testRestTemplate.getForObject("/journey?pageSize=1&sort=asc", String::class.java)

        val expected =
            "{\"journeys\":[{\"id\":\"1\"," +
                "\"departureAt\":\"2020-02-03T11:00:00Z\"," +
                "\"arrivalAt\":\"2020-02-03T11:10:00Z\"," +
                "\"departureStationId\":\"1\"," +
                "\"arrivalStationId\":\"2\"," +
                "\"distance\":1000," +
                "\"duration\":600}" +
                "]," +
                "\"meta\":{\"nextCursor\":\"1580727600|1\"}}"
        assertThat(response).isEqualTo(expected)
    }

    @Test
    fun `Given no parameters, journey endpoint returns using default options`() {
        val response = testRestTemplate.getForEntity("/journey", JourneysResponse::class.java)

        // expected default is ordered by departure timestamp, descending, i.e. latest first
        val expected =
            testJourneys
                .sortedWith(defaultDescendingDepartureComparator)
                .take(paginationConfig.defaultPageSize)
                .map { it.toApi() }

        assertOrderedResponse(expected, response)
    }

    @Test
    fun `Given ascending parameter, journey endpoint returns in ascending order`() {
        val response = testRestTemplate.getForEntity("/journey?sort=asc", JourneysResponse::class.java)

        val expected =
            testJourneys
                .sortedWith(compareBy<JourneyEntity> { it.departureAt }.thenBy { it.id })
                .take(paginationConfig.defaultPageSize)
                .map { it.toApi() }

        assertOrderedResponse(expected, response)
    }

    @Test
    fun `Given orderBy duration parameter, journey endpoint returns ordered by duration`() {
        val response = testRestTemplate.getForEntity("/journey?orderBy=duration", JourneysResponse::class.java)

        val expected =
            testJourneys
                .sortedWith(compareByDescending<JourneyEntity> { it.duration }.thenByDescending { it.id })
                .take(paginationConfig.defaultPageSize)
                .map { it.toApi() }

        assertOrderedResponse(expected, response)
    }

    @Test
    fun `Given orderBy and ascending parameters, journey endpoint returns stations in ascending order by parameter`() {
        val response =
            testRestTemplate.getForEntity("/journey?orderBy=duration&sort=asc", JourneysResponse::class.java)

        val expected =
            testJourneys
                .sortedWith(compareBy<JourneyEntity> { it.duration }.thenBy { it.id })
                .take(paginationConfig.defaultPageSize)
                .map { it.toApi() }

        assertOrderedResponse(expected, response)
    }

    @Test
    fun `Given page size parameter, journey endpoint returns journeys limited by parameter`() {
        val response = testRestTemplate.getForEntity("/journey?pageSize=5", JourneysResponse::class.java)

        val expected =
            testJourneys
                .sortedWith(defaultDescendingDepartureComparator)
                .take(5)
                .map { it.toApi() }

        assertOrderedResponse(expected, response)
    }

    @Test
    fun `Given too large page size parameter, journey endpoint returns journeys limited by maximum`() {
        val response = testRestTemplate.getForEntity("/journey?pageSize=500", JourneysResponse::class.java)

        val expected =
            testJourneys
                .sortedWith(defaultDescendingDepartureComparator)
                .take(paginationConfig.maxPageSize)
                .map { it.toApi() }

        assertOrderedResponse(expected, response)
    }

    @Test
    fun `Given cursor parameter after first request, using default pagination, journey endpoint can be paginated`() {
        var cursor: String? = null
        val results = mutableListOf<List<APIJourney>>()

        do {
            val urlBuilder = UriComponentsBuilder.fromPath("/journey")
            cursor?.apply {
                urlBuilder.queryParam("nextCursor", cursor)
            }
            val finalUri = urlBuilder.build().toUri()

            val response = testRestTemplate.getForEntity(finalUri, JourneysResponse::class.java)
            cursor = response.body!!.meta.nextCursor
            results.add(response.body!!.journeys)
        } while (response.body!!.journeys.isNotEmpty() && cursor != null)

        val expected =
            testJourneys
                .sortedWith(defaultDescendingDepartureComparator)
                .map { it.toApi() }
                .chunked(paginationConfig.defaultPageSize) + listOf(emptyList())
        // page size matches total count so last request results in an empty response

        assertThat(results).hasSize(expected.size)
        assertThat(results).containsExactlyElementsOf(expected)
    }

    @Test
    fun `Given page size, and cursor parameter after first request, journey endpoint can be paginated`() {
        val pageSize = 20
        var cursor: String? = null
        val results = mutableListOf<List<APIJourney>>()

        do {
            val urlBuilder = UriComponentsBuilder.fromPath("/journey")
            urlBuilder.queryParam("pageSize", pageSize)
            cursor?.apply {
                urlBuilder.queryParam("nextCursor", cursor)
            }
            val finalUri = urlBuilder.build().toUri()

            val response = testRestTemplate.getForEntity(finalUri, JourneysResponse::class.java)
            cursor = response.body!!.meta.nextCursor
            results.add(response.body!!.journeys)
        } while (response.body!!.journeys.isNotEmpty() && cursor != null)

        val expected =
            testJourneys
                .sortedWith(defaultDescendingDepartureComparator)
                .map { it.toApi() }
                .chunked(pageSize)

        assertThat(results).hasSize(expected.size)
        assertThat(results).containsExactlyElementsOf(expected)
    }

    private fun JourneyEntity.toApi() =
        APIJourney(
            id.toString(),
            departureAt,
            arrivalAt,
            departureStationId.toString(),
            arrivalStationId.toString(),
            distance,
            duration,
        )

    private fun assertOrderedResponse(
        expected: List<APIJourney>,
        response: ResponseEntity<JourneysResponse>,
    ) {
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body!!.journeys).hasSize(expected.size)
        assertThat(response.body!!.journeys).containsExactlyElementsOf(expected)
        assertThat(response.body!!.meta.nextCursor).isNotNull
    }

    companion object {
        val defaultDescendingDepartureComparator =
            compareByDescending<JourneyEntity> { it.departureAt }.thenByDescending { it.id }
    }
}
