package com.mtuomiko.citybikeapp

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("citybikeapp.data-loader")
class DataLoaderConfig(
    val batchSize: Int,
    val minimumJourneyDistance: Int,
    val minimumJourneyDuration: Int,
    val stationUrl: String,
    val journeyUrls: List<String>,
)
