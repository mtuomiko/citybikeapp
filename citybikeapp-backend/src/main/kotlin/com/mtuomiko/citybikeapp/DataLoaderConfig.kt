package com.mtuomiko.citybikeapp

import io.micronaut.context.annotation.ConfigurationInject
import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("citybikeapp.dataLoader")
class DataLoaderConfig @ConfigurationInject constructor(
    val batchSize: Int,
    val minimumJourneyDistance: Int,
    val minimumJourneyDuration: Int,
    val stationUrl: String,
    val journeyUrls: List<String>
)
