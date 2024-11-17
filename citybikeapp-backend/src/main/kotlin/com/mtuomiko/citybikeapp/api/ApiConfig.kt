package com.mtuomiko.citybikeapp.api

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "citybikeapp")
data class ApiConfig(
    val maxSearchTermCount: Int,
    val minSearchTermLength: Int,
)
