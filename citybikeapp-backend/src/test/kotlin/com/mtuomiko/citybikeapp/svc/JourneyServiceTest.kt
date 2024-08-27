package com.mtuomiko.citybikeapp.svc

import com.mtuomiko.citybikeapp.common.model.PaginationKeyset
import com.mtuomiko.citybikeapp.dao.builder.JourneyEntityBuilder
import com.mtuomiko.citybikeapp.dao.repository.JourneyRepository
import com.mtuomiko.citybikeapp.svc.model.Direction
import com.mtuomiko.citybikeapp.svc.model.JourneyQueryParameters
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test

class JourneyServiceTest {
    private val svcConfig = spyk(SvcConfig(10, 20))
    private val journeyRepository =
        mockk<JourneyRepository>(relaxed = true) {
            // hard coded 10 journeys response, tests
            every { listJourneys(any(), any(), any(), any()) } returns
                (1..10).map { JourneyEntityBuilder().id(it.toLong()).build() }
        }
    private val journeyService = JourneyService(svcConfig, journeyRepository)
    private val defaultOrderBy = "departureAt"
    private val defaultDescending = true
    private val defaultPageSize = 10
    private val defaultKeyset = null

    @Test
    fun `Using empty query parameters, service uses default values`() {
        val queryParameters = JourneyQueryParameters()

        val result = journeyService.searchJourneys(queryParameters)

        verify(exactly = 1) {
            svcConfig.getMaxLimitedPageSize(null)
            journeyRepository.listJourneys(defaultOrderBy, defaultDescending, defaultPageSize, defaultKeyset)
        }

        assertThat(result.content).hasSize(10)
    }

    @Test
    fun `Too large page size is limited by pagination config maximum`() {
        val queryParameters = JourneyQueryParameters(pageSize = 25)

        journeyService.searchJourneys(queryParameters)

        verify(exactly = 1) { svcConfig.getMaxLimitedPageSize(25) }
        verify(exactly = 1) {
            journeyRepository.listJourneys(
                orderBy = defaultOrderBy,
                descending = defaultDescending,
                pageSize = 20,
                keyset = defaultKeyset,
            )
        }
    }

    @Test
    fun `Existing orderBy property is passed onwards`() {
        val queryParameters = JourneyQueryParameters(orderBy = "distance")

        journeyService.searchJourneys(queryParameters)

        verify(exactly = 1) {
            journeyRepository.listJourneys(
                orderBy = "distance",
                descending = defaultDescending,
                pageSize = defaultPageSize,
                keyset = defaultKeyset,
            )
        }
    }

    @Test
    fun `Unknown orderBy property throws exception`() {
        val queryParameters = JourneyQueryParameters(orderBy = "foobar")

        val thrown =
            catchThrowable {
                journeyService.searchJourneys(queryParameters)
            }

        verify(exactly = 0) { journeyRepository.listJourneys(any(), any(), any(), any()) }
        assertThat(thrown)
            .isInstanceOf(Exception::class.java)
            .hasMessageContaining("unknown journey orderBy property `foobar`")
    }

    @Test
    fun `Given integer field order by and integer based cursor, they are converted to pagination keyset`() {
        val queryParameters =
            JourneyQueryParameters(
                orderBy = "duration",
                nextCursor = "500|20",
            )

        journeyService.searchJourneys(queryParameters)

        verify(exactly = 1) {
            journeyRepository.listJourneys(
                orderBy = "duration",
                descending = defaultDescending,
                pageSize = defaultPageSize,
                keyset = PaginationKeyset(500, 20L),
            )
        }
    }

    @Test
    fun `Given timestamp order by and non-integer based cursor, exception is thrown`() {
        val queryParameters =
            JourneyQueryParameters(
                orderBy = "departureAt",
                nextCursor = "foo|20",
            )

        val thrown =
            catchThrowable {
                journeyService.searchJourneys(queryParameters)
            }

        verify(exactly = 0) { journeyRepository.listJourneys(any(), any(), any(), any()) }

        assertThat(thrown)
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("failed to parse cursor value `foo` to type class java.time.Instant")
    }

    @Test
    fun `Given ascending direction parameter, it is converted to descending false boolean`() {
        val queryParameters =
            JourneyQueryParameters(
                direction = Direction.ASC,
            )

        journeyService.searchJourneys(queryParameters)

        verify(exactly = 1) {
            journeyRepository.listJourneys(
                orderBy = defaultOrderBy,
                descending = false,
                pageSize = defaultPageSize,
                keyset = defaultKeyset,
            )
        }
    }

    @Test
    fun `Given unknown direction parameter, default is used`() {
        val queryParameters =
            JourneyQueryParameters(
                direction = null,
            )

        journeyService.searchJourneys(queryParameters)

        verify(exactly = 1) {
            journeyRepository.listJourneys(defaultOrderBy, defaultDescending, defaultPageSize, defaultKeyset)
        }
    }

    @Test
    fun `Using empty query parameters, when results exceed page size, cursor is created`() {
        val queryParameters = JourneyQueryParameters()

        val result = journeyService.searchJourneys(queryParameters)

        assertThat(result.cursor).isEqualTo("1671129324|10")

        verify(exactly = 1) {
            svcConfig.getMaxLimitedPageSize(null)
            journeyRepository.listJourneys(defaultOrderBy, defaultDescending, defaultPageSize, defaultKeyset)
        }
    }
}
