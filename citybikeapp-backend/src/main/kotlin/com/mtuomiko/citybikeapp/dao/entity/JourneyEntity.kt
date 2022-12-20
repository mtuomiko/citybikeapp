package com.mtuomiko.citybikeapp.dao.entity

import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.Relation
import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.Instant

@MappedEntity(value = "journey")
data class JourneyEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val departureAt: Instant,
    val arrivalAt: Instant,
    @Relation(value = Relation.Kind.MANY_TO_ONE)
    @Column(name = "departure_station_id")
    val departureStation: StationEntity,
    @Relation(value = Relation.Kind.MANY_TO_ONE)
    @Column(name = "arrival_station_id")
    val arrivalStation: StationEntity,
    val distance: Int,
    val duration: Int
)
