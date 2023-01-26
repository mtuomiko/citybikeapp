package com.mtuomiko.citybikeapp

import com.mtuomiko.citybikeapp.api.model.APIJourney
import com.mtuomiko.citybikeapp.api.model.JourneysResponse
import com.mtuomiko.citybikeapp.dao.builder.JourneyEntityBuilder
import com.mtuomiko.citybikeapp.dao.builder.StationEntityBuilder
import com.mtuomiko.citybikeapp.dao.entity.JourneyEntity
import com.mtuomiko.citybikeapp.dao.repository.JourneyRepository
import com.mtuomiko.citybikeapp.dao.repository.StationRepository
import com.mtuomiko.citybikeapp.svc.PaginationConfig
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
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
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant

@MicronautTest
class JourneyApiTest {
    @Inject
    @field:Client("/journey")
    private lateinit var stationClient: HttpClient

    @Inject
    private lateinit var journeyRepository: JourneyRepository

    @Inject
    private lateinit var stationRepository: StationRepository

    @Inject
    lateinit var paginationConfig: PaginationConfig

    private lateinit var client: BlockingHttpClient
    private lateinit var testJourneys: List<JourneyEntity>

    @BeforeEach
    fun setup() {
        client = stationClient.toBlocking()

        val start = Instant.parse("2020-02-03T11:00:00Z")
        stationRepository.saveAll(List(2) { StationEntityBuilder().id(it + 1).build() })
        val newJourneys = List(150) {
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

    @AfterEach
    fun cleanup() {
        journeyRepository.deleteAll()
        stationRepository.deleteAll()
    }

    @Test
    fun `Given no parameters, journey endpoint returns stations using defaults`() {
        val request = HttpRequest.GET<Any>("")

        val response = client.exchange(request, JourneysResponse::class.java)

        val expected = testJourneys
            .sortedWith(defaultDescendingDepartureComparator)
            .take(paginationConfig.defaultPageSize)
            .map { it.toApi() }

        assertOrderedResponse(expected, response)
    }

    @Test
    fun `Given ascending parameter, journey endpoint returns stations in ascending order`() {
        val request = HttpRequest.GET<Any>("?direction=asc")

        val response = client.exchange(request, JourneysResponse::class.java)

        val expected = testJourneys
            .sortedWith(compareBy<JourneyEntity> { it.departureAt }.thenBy { it.id })
            .take(paginationConfig.defaultPageSize)
            .map { it.toApi() }

        assertOrderedResponse(expected, response)
    }

    @Test
    fun `Given orderBy parameter, journey endpoint returns stations ordered by parameter`() {
        val request = HttpRequest.GET<Any>("?orderBy=duration")

        val response = client.exchange(request, JourneysResponse::class.java)

        val expected = testJourneys
            .sortedWith(compareByDescending<JourneyEntity> { it.duration }.thenByDescending { it.id })
            .take(paginationConfig.defaultPageSize)
            .map { it.toApi() }

        assertOrderedResponse(expected, response)
    }

    @Test
    fun `Given orderBy and ascending parameters, journey endpoint returns stations in ascending order by parameter`() {
        val request = HttpRequest.GET<Any>("?orderBy=duration&direction=asc")

        val response = client.exchange(request, JourneysResponse::class.java)

        val expected = testJourneys
            .sortedWith(compareBy<JourneyEntity> { it.duration }.thenBy { it.id })
            .take(paginationConfig.defaultPageSize)
            .map { it.toApi() }

        assertOrderedResponse(expected, response)
    }

    @Test
    fun `Given page size parameter, journey endpoint returns journeys limited by parameter`() {
        val request = HttpRequest.GET<Any>("?pageSize=5")

        val response = client.exchange(request, JourneysResponse::class.java)

        val expected = testJourneys
            .sortedWith(defaultDescendingDepartureComparator)
            .take(5)
            .map { it.toApi() }

        assertOrderedResponse(expected, response)
    }

    @Test
    fun `Given too large page size parameter, journey endpoint returns journeys limited by maximum`() {
        val request = HttpRequest.GET<Any>("?pageSize=500")

        val response = client.exchange(request, JourneysResponse::class.java)

        val expected = testJourneys
            .sortedWith(defaultDescendingDepartureComparator)
            .take(paginationConfig.maxPageSize)
            .map { it.toApi() }

        assertOrderedResponse(expected, response)
    }

    @Test
    fun `Given cursor parameter after first request, journey endpoint can be paginated`() {
        var cursor: String? = null
        val results = mutableListOf<List<APIJourney>>()

        do {
            val encodedCursor = cursor?.let { URLEncoder.encode(it, StandardCharsets.UTF_8) }
            val parameter = encodedCursor?.let { "?nextCursor=$it" } ?: ""
            val request = HttpRequest.GET<Any>(parameter)
            val response = client.exchange(request, JourneysResponse::class.java)
            cursor = response.body()!!.meta.nextCursor
            results.add(response.body()!!.journeys)
        } while (response.body()!!.journeys.isNotEmpty() && cursor != null)

        val expected = testJourneys
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
            val encodedCursor = cursor?.let { "&nextCursor=${URLEncoder.encode(it, StandardCharsets.UTF_8)}" } ?: ""
            val parameters = "?pageSize=$pageSize$encodedCursor"
            val request = HttpRequest.GET<Any>(parameters)
            val response = client.exchange(request, JourneysResponse::class.java)
            cursor = response.body()!!.meta.nextCursor
            results.add(response.body()!!.journeys)
        } while (response.body()!!.journeys.isNotEmpty() && cursor != null)

        val expected = testJourneys
            .sortedWith(defaultDescendingDepartureComparator)
            .map { it.toApi() }
            .chunked(pageSize)

        assertThat(results).hasSize(expected.size)
        assertThat(results).containsExactlyElementsOf(expected)
    }

    private fun JourneyEntity.toApi() = APIJourney(
        id.toString(),
        departureAt,
        arrivalAt,
        departureStationId.toString(),
        arrivalStationId.toString(),
        distance,
        duration
    )

    private fun assertOrderedResponse(expected: List<APIJourney>, response: HttpResponse<JourneysResponse>) {
        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.body()!!.journeys).hasSize(expected.size)
        assertThat(response.body()!!.journeys).containsExactlyElementsOf(expected)
        assertThat(response.body()!!.meta.nextCursor).isNotNull
    }

    companion object {
        val defaultDescendingDepartureComparator =
            compareByDescending<JourneyEntity> { it.departureAt }.thenByDescending { it.id }
    }
}
