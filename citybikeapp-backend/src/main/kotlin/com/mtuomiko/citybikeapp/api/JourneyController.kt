package com.mtuomiko.citybikeapp.api

import com.mtuomiko.citybikeapp.api.model.APIJourney
import com.mtuomiko.citybikeapp.api.model.CursorMeta
import com.mtuomiko.citybikeapp.api.model.JourneysResponse
import com.mtuomiko.citybikeapp.common.model.Journey
import com.mtuomiko.citybikeapp.svc.JourneyService
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag

@ExecuteOn(TaskExecutors.IO)
@Controller("/journey")
@Tag(name = "journey")
class JourneyController(
    private val journeyService: JourneyService
) {
    @Operation(
        summary = "List journeys",
        parameters = [
            Parameter(
                name = "orderBy",
                `in` = ParameterIn.QUERY,
                description = "Sort results by this property",
                schema = Schema(
                    type = "string",
                    allowableValues = ["departureAt", "arrivalAt", "distance", "duration"],
                    defaultValue = "departureAt"
                )
            ),
            Parameter(
                name = "direction",
                `in` = ParameterIn.QUERY,
                description = "Sort results in this direction",
                schema = Schema(type = "string", allowableValues = ["asc", "desc"], defaultValue = "desc")
            ),
            Parameter(
                name = "pageSize",
                `in` = ParameterIn.QUERY,
                description = "Limit amount of results. Can be limited by application configuration.",
                schema = Schema(type = "number")
            ),
            Parameter(
                name = "nextCursor",
                `in` = ParameterIn.QUERY,
                description = "Cursor to fetch the next page. Can expose some implementation details, such as the " +
                    "values used in keyset pagination, but should be treated as an opaque string. Don't change " +
                    "other query parameters (except pageSize) when providing the cursor in order to receive " +
                    "a meaningful response, meaning the cursor is only valid for the same set of parameters.",
                schema = Schema(type = "string")
            )
        ]
    )
    @Get("{?queryParameters*}")
    fun getJourneys(@Parameter(hidden = true) queryParameters: Map<String, String>): JourneysResponse {
        val result = journeyService.searchJourneys(queryParameters)
        return JourneysResponse(
            journeys = result.content.map { it.toApi() },
            meta = CursorMeta(nextCursor = result.cursor)
        )
    }

    private fun Journey.toApi() =
        APIJourney(
            id.toString(),
            departureAt,
            arrivalAt,
            departureStationId.toString(),
            arrivalStationId.toString(),
            distance,
            duration
        )
}
