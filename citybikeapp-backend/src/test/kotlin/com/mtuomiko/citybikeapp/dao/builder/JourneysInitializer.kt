package com.mtuomiko.citybikeapp.dao.builder

import com.mtuomiko.citybikeapp.common.model.JourneyNew
import com.mtuomiko.citybikeapp.dao.entity.JourneyEntity
import com.mtuomiko.citybikeapp.dao.repository.JourneyRepository
import java.time.Instant
import kotlin.random.Random

class JourneysInitializer(private val journeyRepository: JourneyRepository) {
    var departureStationIds: List<Int> = emptyList()
    var arrivalStationIds: List<Int> = emptyList()
    var earliestDeparture: Instant = Instant.parse("2019-12-31T22:00:00+02:00")
    var latestDeparture: Instant = Instant.parse("2020-06-30T20:59:59+03:00")
    var minDistance: Int = 10
    var maxDistance: Int = 22000
    var minDuration: Int = 10
    var maxDuration: Int = 3600

    fun departureStationIds(stationIds: List<Int>) = apply { this.departureStationIds = stationIds }
    fun arrivalStationIds(stationIds: List<Int>) = apply { this.arrivalStationIds = stationIds }
    fun departureStationId(stationId: Int) = apply { this.departureStationIds = listOf(stationId) }
    fun arrivalStationId(stationId: Int) = apply { this.arrivalStationIds = listOf(stationId) }
    fun earliestDeparture(instant: Instant) = apply { this.earliestDeparture = instant }
    fun latestDeparture(instant: Instant) = apply { this.latestDeparture = instant }
    fun minDistance(distance: Int) = apply { this.minDistance = distance }
    fun maxDistance(distance: Int) = apply { this.maxDistance = distance }

    fun saveRandomJourneys(count: Int): JourneysInitializer {
        val newJourneys = List(count) { buildRandomNewJourney() }
        journeyRepository.saveAllNewJourneys(newJourneys)

        return this
    }

    private fun buildRandomNewJourney(): JourneyNew {
        val departureStation = departureStationIds.random()
        val arrivalStation = arrivalStationIds.random()
        val departureAt = Instant.ofEpochSecond(
            Random.nextLong(earliestDeparture.epochSecond, latestDeparture.epochSecond)
        )
        val duration = if (minDuration != maxDuration) Random.nextInt(minDuration, maxDuration) else minDuration
        val arrivalAt = departureAt.plusSeconds(duration.toLong())
        val distance = if (minDistance != maxDistance) Random.nextInt(minDistance, maxDistance) else minDistance

        return JourneyEntityBuilder()
            .departureAt(departureAt)
            .arrivalAt(arrivalAt)
            .departureStationId(departureStation)
            .arrivalStationId(arrivalStation)
            .distance(distance)
            .duration(duration)
            .build()
            .toNewJourney()
    }

    private fun JourneyEntity.toNewJourney() = JourneyNew(
        departureAt,
        arrivalAt,
        departureStationId,
        arrivalStationId,
        distance,
        duration
    )
}
