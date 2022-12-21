package com.mtuomiko.citybikeapp.api

import com.mtuomiko.citybikeapp.dao.JourneyRepository
import com.mtuomiko.citybikeapp.dao.StationRepository
import com.mtuomiko.citybikeapp.model.TopStation
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.Get
import io.micronaut.http.hateoas.JsonError
import io.micronaut.http.hateoas.Link
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import jakarta.inject.Inject

@ExecuteOn(TaskExecutors.IO)
@Controller("/station")
class StationController(
    @Inject
    private val journeyRepository: JourneyRepository,
    @Inject
    private val stationRepository: StationRepository
) {

    @Get("/{id}")
    fun getStationWithStatistics(id: Int): StationWithStatistics {
        val station = stationRepository.findById(id).orElseThrow { NotFoundException("Station not found") }
        // TODO: Async handling of queries?
        val stats = journeyRepository.getTripStatisticsByStationId(id)
        val topStations = journeyRepository.getTopStationsByStationId(id)

        val topStationsForArrivals = topStations
            .filter { it.arrivalStationId == id }
            .sortedByDescending { it.journeyCount }
            .map {
                TopStation(
                    id = it.departureStationId,
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
                    id = it.arrivalStationId,
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
            topStationsForArrivals = topStationsForArrivals,
            topStationsForDepartures = topStationsForDepartures
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

    class NotFoundException(message: String = "Not found", cause: Throwable? = null) : Throwable(message, cause)
}
