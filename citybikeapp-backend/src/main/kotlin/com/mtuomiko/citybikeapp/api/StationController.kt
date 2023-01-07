package com.mtuomiko.citybikeapp.api

import com.mtuomiko.citybikeapp.api.mapper.StationAPIMapper
import com.mtuomiko.citybikeapp.api.model.StationDetailsWithStatisticsResponse
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

    /**
     * @param id Station ID
     * @param fromDate Earliest journey date to include in station statistics
     * @param toDate Latest journey date to include in station statistics
     */
    @Get("/{id}")
    @Operation(
        summary = "Get single station information with statistics",
        description = "Single station information including journey statistics. Statistics are filtered using the " +
            "optional query parameters. Note that malformed optional query parameters do not result in failure."
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
