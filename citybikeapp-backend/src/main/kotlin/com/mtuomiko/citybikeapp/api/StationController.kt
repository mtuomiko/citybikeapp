package com.mtuomiko.citybikeapp.api

import com.mtuomiko.citybikeapp.api.ErrorMessages.INVALID_QUERY_PARAMETER
import com.mtuomiko.citybikeapp.common.model.Station
import com.mtuomiko.citybikeapp.common.model.StationDetails
import com.mtuomiko.citybikeapp.common.model.StationLimited
import com.mtuomiko.citybikeapp.common.model.TopStation
import com.mtuomiko.citybikeapp.gen.api.StationApi
import com.mtuomiko.citybikeapp.gen.model.Meta
import com.mtuomiko.citybikeapp.gen.model.StationDetailsResponse
import com.mtuomiko.citybikeapp.gen.model.StationsLimitedResponse
import com.mtuomiko.citybikeapp.gen.model.StationsResponse
import com.mtuomiko.citybikeapp.gen.model.StatisticsResponse
import com.mtuomiko.citybikeapp.svc.StationService
import com.mtuomiko.citybikeapp.svc.model.Direction
import com.mtuomiko.citybikeapp.svc.model.StationQueryParameters
import com.mtuomiko.citybikeapp.svc.model.StationStatistics
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import com.mtuomiko.citybikeapp.gen.model.Direction as APIDirection
import com.mtuomiko.citybikeapp.gen.model.Station as APIStation
import com.mtuomiko.citybikeapp.gen.model.StationDetails as APIStationDetails
import com.mtuomiko.citybikeapp.gen.model.StationLimited as APIStationLimited
import com.mtuomiko.citybikeapp.gen.model.StationStatistics as APIStationStatistics
import com.mtuomiko.citybikeapp.gen.model.TopStation as APITopStation

private val logger = KotlinLogging.logger {}

@CrossOrigin
@RestController
class StationController(
    private val stationService: StationService,
    private val apiConfig: ApiConfig,
) : StationApi {
    override fun getAllStationsLimited(): ResponseEntity<StationsLimitedResponse> {
        val stationsLimited = stationService.getAllStationsLimited()

        val returnContent = StationsLimitedResponse(stationsLimited.map { s -> s.toApi() })

        return ResponseEntity.ok(returnContent)
    }

    override fun getStationDetails(id: Int): ResponseEntity<StationDetailsResponse> {
        val station = stationService.getStationById(id) ?: throw NotFoundError("Station not found")

        val returnContent =
            StationDetailsResponse(
                station = station.toApi(),
            )

        return ResponseEntity.ok(returnContent)
    }

    override fun getStationStatistics(
        id: Int,
        from: Instant?,
        to: Instant?,
    ): ResponseEntity<StatisticsResponse> {
        logger.trace { "getStationStatistics: from '$from' to '$to'" }
        validateDates(from, to)
        if (!stationService.stationExists(id)) throw NotFoundError("Station not found")

        val statistics = stationService.getStationStatistics(id, from, to)

        val returnContent = StatisticsResponse(statistics.toApi())

        return ResponseEntity.ok(returnContent)
    }

    override fun getStations(
        orderBy: String?,
        sort: APIDirection,
        search: String?,
        page: Int?,
        pageSize: Int?,
    ): ResponseEntity<StationsResponse> {
        logger.trace { "Fetching stations with search: $search, page: $page, pageSize: $pageSize" }

        // '+' delimited search terms, but no reason to proceed with empty tokens (e.g. foo++bar -> foo,bar)
        val searchTokens =
            search
                ?.split('+')
                ?.filter { it.isNotBlank() }
                ?: emptyList()
        if (
            searchTokens.size > apiConfig.maxSearchTermCount ||
            searchTokens.any { it.length < apiConfig.minSearchTermLength }
        ) {
            throw BadRequestError("check search terms")
        }
        if (page != null && page < 0) throw BadRequestError("page cannot be negative")
        if (pageSize != null && pageSize < 0) throw BadRequestError("pageSize cannot be negative")

        val params =
            StationQueryParameters(
                orderBy,
                Direction.valueOf(sort.value.uppercase()),
                searchTokens,
                page,
                pageSize,
            )

        val stations = stationService.getStations(params)

        val returnContent =
            StationsResponse(
                stations = stations.content.map { it.toApi() },
                meta = Meta(totalPages = stations.totalPages),
            )

        return ResponseEntity.ok(returnContent)
    }

    private fun validateDates(
        fromDate: Instant?,
        toDate: Instant?,
    ) {
        if (fromDate != null && toDate != null && fromDate > toDate) {
            throw BadRequestError(
                "query parameter `fromDate` date cannot be after `toDate` date",
                innerErrors =
                    listOf(
                        InnerError(INVALID_QUERY_PARAMETER, "fromDate"),
                        InnerError(INVALID_QUERY_PARAMETER, "toDate"),
                    ),
            )
        }
    }

    fun StationLimited.toApi() =
        APIStationLimited(
            id = id.toString(),
            nameFinnish,
        )

    fun StationDetails.toApi() =
        APIStationDetails(
            id = id.toString(),
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

    fun StationStatistics.toApi() =
        APIStationStatistics(
            departureCount,
            arrivalCount,
            departureAverageDistance,
            arrivalAverageDistance,
            topStationsForArrivingHere.map { it.toApi() },
            topStationsForDepartingTo.map { it.toApi() },
        )

    fun TopStation.toApi() =
        APITopStation(
            id.toString(),
            journeyCount,
        )

    fun Station.toApi() =
        APIStation(
            id.toString(),
            nameFinnish,
            addressFinnish,
            cityFinnish,
            operator,
            capacity,
        )
}
