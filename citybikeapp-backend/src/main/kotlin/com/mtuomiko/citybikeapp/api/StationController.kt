package com.mtuomiko.citybikeapp.api

import com.mtuomiko.citybikeapp.api.mapper.StationAPIMapper
import com.mtuomiko.citybikeapp.api.model.Meta
import com.mtuomiko.citybikeapp.api.model.StationDetailsResponse
import com.mtuomiko.citybikeapp.api.model.StationsLimitedResponse
import com.mtuomiko.citybikeapp.api.model.StationsResponse
import com.mtuomiko.citybikeapp.api.model.StatisticsResponse
import com.mtuomiko.citybikeapp.common.BadRequestError
import com.mtuomiko.citybikeapp.common.ErrorMessages.INVALID_QUERY_PARAMETER
import com.mtuomiko.citybikeapp.common.InnerError
import com.mtuomiko.citybikeapp.common.NotFoundError
import com.mtuomiko.citybikeapp.svc.StationService
import io.micronaut.core.annotation.Nullable
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import mu.KotlinLogging
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

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
            "Optional search string can be used to limit matches. Note that if paginating query params result in " +
            "no stations, the total pages count might not hold true! That is, there could still be possible " +
            "earlier results."
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
        logger.debug { "Fetching stations with search: $search, page: $page, pageSize: $pageSize" }

        val searchTokens = search?.split('+') ?: emptyList()
        if (searchTokens.size > MAX_SEARCH_TERM_COUNT || searchTokens.any { it.length < MIN_SEARCH_TERM_LENGTH }) {
            throw BadRequestError("check search terms")
        }
        if (page != null && page < 0) throw BadRequestError("page cannot be negative")
        if (pageSize != null && pageSize < 0) throw BadRequestError("pageSize cannot be negative")

        val stations = stationService.getStations(searchTokens, page, pageSize)

        return StationsResponse(
            stations = stations.content.map { mapper.toApi(it) },
            meta = Meta(totalPages = stations.totalPages)
        )
    }

    /**
     * @param id Station ID
     */
    @Get("/{id}")
    @Operation(
        summary = "Get single station detailed information"
    )
    fun getStationDetails(@PathVariable id: Int): StationDetailsResponse {
        val station = stationService.getStationById(id) ?: throw NotFoundError("Station not found")

        return StationDetailsResponse(
            station = mapper.toApi(station)
        )
    }

    /**
     * @param id Station ID
     * @param from Earliest moment in time to include in statistics. Timestamp without timezone. Will be
     * interpreted as local Helsinki time internally.
     * @param to Latest moment in time to include in statistics. Timestamp without timezone. Will be
     * interpreted as local Helsinki time internally.
     */
    @Get("/{id}/statistics")
    @Operation(
        summary = "Get statistics for a single station",
        description = "Statistics are filtered using the optional query parameters. Note that malformed query " +
            "parameters do not result in failure. Journey is included in statistics if its departure timestamp " +
            "is between the from and to parameters. Timestamps must be without timezone or UTC designator (Z)." +
            "They are interpreted in local Helsinki time internally. Example: if you want journey statistics " +
            "until end of June 2022, use format 2022-06-30T23:59:59"
    )
    fun getStationStatistics(
        @PathVariable id: Int,
        @Parameter(
            schema = Schema(
                type = "string",
                format = "timestamp without time zone",
                nullable = true,
                example = "2023-01-29T13:47:51"
            )
        ) @QueryValue @Nullable
        from: LocalDateTime?,
        @Parameter(
            schema = Schema(
                type = "string",
                format = "timestamp without time zone",
                nullable = true,
                example = "2023-01-29T13:47:51"
            )
        ) @QueryValue @Nullable
        to: LocalDateTime?
    ): StatisticsResponse {
        validateDates(from, to)
        if (!stationService.stationExists(id)) throw NotFoundError("Station not found")

        val statistics = stationService.getStationStatistics(id, from, to)

        return StatisticsResponse(statistics = mapper.toApi(statistics))
    }

    private fun validateDates(fromDate: LocalDateTime?, toDate: LocalDateTime?) {
        if (fromDate != null && toDate != null && fromDate > toDate) {
            throw BadRequestError(
                "query parameter `fromDate` date cannot be after `toDate` date",
                innerErrors = listOf(
                    InnerError(INVALID_QUERY_PARAMETER, "fromDate"),
                    InnerError(INVALID_QUERY_PARAMETER, "toDate")
                )
            )
        }
    }
}

const val MAX_SEARCH_TERM_COUNT = 3
const val MIN_SEARCH_TERM_LENGTH = 3
