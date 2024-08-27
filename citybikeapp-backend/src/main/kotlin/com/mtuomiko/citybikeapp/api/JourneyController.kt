package com.mtuomiko.citybikeapp.api

import com.mtuomiko.citybikeapp.common.model.Journey
import com.mtuomiko.citybikeapp.gen.api.JourneyApi
import com.mtuomiko.citybikeapp.gen.model.CursorMeta
import com.mtuomiko.citybikeapp.gen.model.JourneysResponse
import com.mtuomiko.citybikeapp.svc.JourneyService
import com.mtuomiko.citybikeapp.svc.model.Direction
import com.mtuomiko.citybikeapp.svc.model.JourneyQueryParameters
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RestController
import com.mtuomiko.citybikeapp.gen.model.Direction as APIDirection
import com.mtuomiko.citybikeapp.gen.model.Journey as APIJourney

private val logger = KotlinLogging.logger {}

@CrossOrigin
@RestController
class JourneyController(
    private val journeyService: JourneyService,
) : JourneyApi {
    override fun getJourneys(
        orderBy: String,
        sort: APIDirection,
        pageSize: Int?,
        nextCursor: String?,
    ): ResponseEntity<JourneysResponse> {
        val params = JourneyQueryParameters(orderBy, Direction.valueOf(sort.value.uppercase()), pageSize, nextCursor)
        logger.trace { "getJourney params: $params" }

        val result = journeyService.searchJourneys(params)

        val returnContent =
            JourneysResponse(
                journeys = result.content.map { it.toApi() },
                meta = CursorMeta(nextCursor = result.cursor),
            )

        return ResponseEntity.ok(returnContent)
    }

    fun Journey.toApi(): APIJourney =
        APIJourney(
            id.toString(),
            departureAt,
            arrivalAt,
            departureStationId.toString(),
            arrivalStationId.toString(),
            distance,
            duration,
        )
}
