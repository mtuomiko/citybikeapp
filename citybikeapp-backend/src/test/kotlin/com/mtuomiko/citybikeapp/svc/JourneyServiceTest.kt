package com.mtuomiko.citybikeapp.svc

import com.mtuomiko.citybikeapp.common.model.PaginationKeyset
import com.mtuomiko.citybikeapp.dao.repository.JourneyRepository
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test

class JourneyServiceTest {
    private val paginationConfig = spyk(PaginationConfig(10, 20))
    private val journeyRepository = mockk<JourneyRepository>(relaxed = true)
    private val journeyService = JourneyService(paginationConfig, journeyRepository)
    private val defaultOrderBy = "departureAt"
    private val defaultDescending = true
    private val defaultPageSize = 10
    private val defaultKeyset = null

    @Test
    fun `Using empty query parameter map, service uses default values`() {
        val queryParameters = emptyMap<String, String>()

        journeyService.searchJourneys(queryParameters)

        verify(exactly = 1) {
            paginationConfig.getMaxLimitedPageSize(null)
            journeyRepository.listJourneys(defaultOrderBy, defaultDescending, defaultPageSize, defaultKeyset)
        }
    }

    @Test
    fun `Too large page size is limited by pagination config maximum`() {
        val queryParameters = mapOf("pageSize" to "25")

        journeyService.searchJourneys(queryParameters)

        verify(exactly = 1) { paginationConfig.getMaxLimitedPageSize(25) }
        verify(exactly = 1) {
            journeyRepository.listJourneys(
                orderBy = defaultOrderBy,
                descending = defaultDescending,
                pageSize = 20,
                keyset = defaultKeyset
            )
        }
    }

    @Test
    fun `Existing orderBy property is passed onwards`() {
        val queryParameters = mapOf("orderBy" to "distance")

        journeyService.searchJourneys(queryParameters)

        verify(exactly = 1) {
            journeyRepository.listJourneys(
                orderBy = "distance",
                descending = defaultDescending,
                pageSize = defaultPageSize,
                keyset = defaultKeyset
            )
        }
    }

    @Test
    fun `Unknown orderBy property throws exception`() {
        val queryParameters = mapOf("orderBy" to "foobar")

        val thrown = catchThrowable {
            journeyService.searchJourneys(queryParameters)
        }

        verify(exactly = 0) { journeyRepository.listJourneys(any(), any(), any(), any()) }
        assertThat(thrown).isInstanceOf(Exception::class.java).hasMessageContaining("unknown orderBy property")
    }

    @Test
    fun `Given integer field order by and integer based cursor, they are converted to pagination keyset`() {
        val queryParameters = mapOf(
            "orderBy" to "duration",
            "nextCursor" to "500|20"
        )

        journeyService.searchJourneys(queryParameters)

        verify(exactly = 1) {
            journeyRepository.listJourneys(
                orderBy = "duration",
                descending = defaultDescending,
                pageSize = defaultPageSize,
                keyset = PaginationKeyset(500, 20L)
            )
        }
    }

    @Test
    fun `Given timestamp order by and integer based cursor, exception is thrown`() {
        val queryParameters = mapOf(
            "orderBy" to "departureAt",
            "nextCursor" to "500|20"
        )

        val thrown = catchThrowable {
            journeyService.searchJourneys(queryParameters)
        }

        verify(exactly = 0) { journeyRepository.listJourneys(any(), any(), any(), any()) }
        assertThat(thrown).isInstanceOf(Exception::class.java)
            .hasMessageContaining("failed to parse `500` to type java.time.Instant")
    }

    @Test
    fun `Given ascending direction parameter, it is converted to descending false boolean`() {
        val queryParameters = mapOf(
            "direction" to "asc"
        )

        journeyService.searchJourneys(queryParameters)

        verify(exactly = 1) {
            journeyRepository.listJourneys(
                orderBy = defaultOrderBy,
                descending = false,
                pageSize = defaultPageSize,
                keyset = defaultKeyset
            )
        }
    }

    @Test
    fun `Given unknown direction parameter, default is used`() {
        val queryParameters = mapOf(
            "direction" to "moobar"
        )

        journeyService.searchJourneys(queryParameters)

        verify(exactly = 1) {
            journeyRepository.listJourneys(defaultOrderBy, defaultDescending, defaultPageSize, defaultKeyset)
        }
    }
}
