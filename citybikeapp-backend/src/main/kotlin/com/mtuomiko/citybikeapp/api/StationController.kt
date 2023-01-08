package com.mtuomiko.citybikeapp.api

import com.mtuomiko.citybikeapp.api.mapper.StationAPIMapper
import com.mtuomiko.citybikeapp.api.model.Meta
import com.mtuomiko.citybikeapp.api.model.StationDetailsWithStatisticsResponse
import com.mtuomiko.citybikeapp.api.model.StationsLimitedResponse
import com.mtuomiko.citybikeapp.api.model.StationsResponse
import com.mtuomiko.citybikeapp.svc.StationService
import io.micronaut.core.annotation.Nullable
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.hateoas.JsonError
import io.micronaut.http.hateoas.Link
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import java.time.LocalDate

@ExecuteOn(TaskExecutors.IO)
@Controller("/station")
@Tag(name = "station")
class StationController(
    @Inject private val mapper: StationAPIMapper,
    @Inject private val stationService: StationService
) {

    @Operation(
        summary = "Get all stations",
        description = "Get all stations with limited information"
    )
    @Get("/limited")
    fun getAllStationsLimited(): StationsLimitedResponse {
        val stationsLimited = stationService.getAllStationsLimited()
        return StationsLimitedResponse(
            stations = stationsLimited.map { mapper.toApi(it) }
        )
    }

    /**
     * @param search Optional search string to limit station results. Will look for matches in station names and street
     * addresses. Separate search words with + symbol.
     * @param page Optional pagination offset.
     * @param pageSize Optional page size.
     */
    @Operation(
        summary = "Get stations using pagination and optional text search.",
        description = "Returns multiple stations with a maximum page size of given page size parameter. If page size" +
            " is not defined then application default is used. Page number can be provided to paginate results. " +
            "Optional search string can be used to limit matches. Note that if paginating query params result in no " +
            "stations, the total pages count might not hold true! That is, there could still be possible earlier " +
            "results."
    )
    @Get
    @Suppress("ThrowsCount")
    fun getStations(
        @Parameter(example = "kontu+tie") @QueryValue @Nullable
        search: String?,
        @Parameter(example = "3") @QueryValue @Nullable
        page: Int?,
        @Parameter(example = "25") @QueryValue @Nullable
        pageSize: Int?
    ): StationsResponse {
        val searchTokens = search?.split('+') ?: emptyList()
        if (searchTokens.size > MAX_SEARCH_TERM_COUNT || searchTokens.any { it.length < MIN_SEARCH_TERM_LENGTH }) {
            throw BadRequestException("check search terms")
        }
        if (page != null && page < 0) throw BadRequestException("page cannot be negative")
        if (pageSize != null && pageSize < 0) throw BadRequestException("pageSize cannot be negative")

        val stations = stationService.getStations(searchTokens, page, pageSize)

        return StationsResponse(
            stations = stations.content.map { mapper.toApi(it) },
            meta = Meta(totalPages = stations.totalPages)
        )
    }

    /**
     * @param id Station ID
     * @param fromDate Earliest journey date to include in station statistics
     * @param toDate Latest journey date to include in station statistics
     */
    @Get("/{id}")
    @Operation(
        summary = "Get single station information with statistics",
        description = "Single station information including journey statistics. Statistics are filtered using the " +
            "optional query parameters. Note that malformed query parameters do not result in failure."
    )
    fun getStationWithStatistics(
        @PathVariable id: Int,
        @Parameter(example = "2021-06-15") @QueryValue @Nullable
        fromDate: LocalDate?,
        @Parameter(example = "2021-07-02") @QueryValue @Nullable
        toDate: LocalDate?
    ): StationDetailsWithStatisticsResponse {
        if (fromDate != null && toDate != null && fromDate > toDate) {
            throw BadRequestException("query parameter `fromDate` date cannot be after `toDate` date")
        }
        val station = stationService.getStationById(id) ?: throw NotFoundException("Station not found")

        val stationStatistics = stationService.getStationStatistics(id, fromDate, toDate)

        return StationDetailsWithStatisticsResponse(
            station = mapper.toApi(station),
            statistics = mapper.toApi(stationStatistics)
        )
    }

    @Error
    fun notFound(request: HttpRequest<*>, e: NotFoundException): HttpResponse<JsonError> {
        val error = JsonError(e.message).link(Link.SELF, Link.of(request.uri))

        return HttpResponse.status<JsonError>(HttpStatus.NOT_FOUND).body(error)
    }

    @Error
    fun badRequest(request: HttpRequest<*>, e: BadRequestException): HttpResponse<JsonError> {
        val error = JsonError(e.message).link(Link.SELF, Link.of(request.uri))

        return HttpResponse.status<JsonError>(HttpStatus.BAD_REQUEST).body(error)
    }

    class NotFoundException(message: String = "Not found", cause: Throwable? = null) : Throwable(message, cause)
    class BadRequestException(message: String = "Bad request", cause: Throwable? = null) : Throwable(message, cause)
}

const val MAX_SEARCH_TERM_COUNT = 3
const val MIN_SEARCH_TERM_LENGTH = 3
