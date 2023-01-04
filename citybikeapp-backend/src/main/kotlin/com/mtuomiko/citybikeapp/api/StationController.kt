package com.mtuomiko.citybikeapp.api

import com.mtuomiko.citybikeapp.TIMEZONE
import com.mtuomiko.citybikeapp.dao.JourneyRepository
import com.mtuomiko.citybikeapp.dao.StationRepository
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
import java.time.LocalTime

@ExecuteOn(TaskExecutors.IO)
@Controller("/station")
@Tag(name = "station")
class StationController(
    @Inject
    private val journeyRepository: JourneyRepository,
    @Inject
    private val stationRepository: StationRepository
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
    ): StationWithStatistics {
        if (fromDate != null && toDate != null && fromDate > toDate) {
            throw BadRequestException("query parameter `from` timestamp cannot be after `to` timestamp")
        }
        val station = stationRepository.findById(id).orElseThrow { NotFoundException("Station not found") }

        // Interpret query dates to be in local Helsinki time
        val from = fromDate?.atStartOfDay(TIMEZONE)?.toInstant()
        val to = toDate?.atTime(LocalTime.MAX)?.atZone(TIMEZONE)?.toInstant()

        // TODO: Async handling of queries?
        val stats = journeyRepository.getTripStatisticsByStationId(id, from, to)
        val topStations = journeyRepository.getTopStationsByStationId(id, from, to)

        val topStationsForArrivals = topStations
            .filter { it.arrivalStationId == id }
            .sortedByDescending { it.journeyCount }
            .map {
                TopStation(
                    it.departureStationId,
                    it.nameFinnish,
                    it.nameSwedish,
                    it.nameEnglish,
                    it.journeyCount
                )
            }
        val topStationsForDepartures = topStations
            .filter { it.departureStationId == id }
            .sortedByDescending { it.journeyCount }
            .map {
                TopStation(
                    it.arrivalStationId,
                    it.nameFinnish,
                    it.nameSwedish,
                    it.nameEnglish,
                    it.journeyCount
                )
            }
        val stationStatistics = StationStatistics(
            departureCount = stats.departureCount,
            arrivalCount = stats.arrivalCount,
            departureJourneyAverageDistance = stats.departureAverageDistance,
            arrivalJourneyAverageDistance = stats.arrivalAverageDistance,
            topStationsWhereJourneysArriveFrom = topStationsForArrivals,
            topStationsWhereJourneysDepartTo = topStationsForDepartures
        )

        return StationWithStatistics(
            id = station.id,
            nameFinnish = station.nameFinnish,
            nameSwedish = station.nameSwedish,
            nameEnglish = station.nameEnglish,
            addressFinnish = station.addressFinnish,
            addressSwedish = station.addressSwedish,
            cityFinnish = station.cityFinnish,
            citySwedish = station.citySwedish,
            operator = station.operator,
            capacity = station.capacity,
            longitude = station.longitude,
            latitude = station.latitude,
            statistics = stationStatistics
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
