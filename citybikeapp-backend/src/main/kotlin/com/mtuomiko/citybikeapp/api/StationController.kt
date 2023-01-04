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
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import java.time.LocalDateTime

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
     * @param from Earliest journey time to include in station statistics
     * @param to Latest journey time to include in station statistics
     */
    @Get("/{id}")
    @Operation(
        summary = "Get single station information with statistics",
        description = "Single station information including journey statistics. Statistics are filtered using the " +
            "optional query parameters."
    )
    fun getStationWithStatistics(
        @PathVariable id: Int,
        @QueryValue @Nullable
        from: LocalDateTime?,
        @QueryValue @Nullable
        to: LocalDateTime?
    ): StationWithStatistics {
        if (from != null && to != null && from > to) {
            throw BadRequestException("query parameter `from` timestamp cannot be after `to` timestamp")
        }
        val station = stationRepository.findById(id).orElseThrow { NotFoundException("Station not found") }

        // Interpret query timestamps to be in local Helsinki time
        val fromInstant = from?.atZone(TIMEZONE)?.toInstant()
        val toInstant = to?.atZone(TIMEZONE)?.toInstant()

        // TODO: Async handling of queries?
        val stats = journeyRepository.getTripStatisticsByStationId(id, fromInstant, toInstant)
        val topStations = journeyRepository.getTopStationsByStationId(id, fromInstant, toInstant)

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
