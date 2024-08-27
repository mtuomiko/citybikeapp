package com.mtuomiko.citybikeapp.svc

import org.springframework.boot.context.properties.ConfigurationProperties
import kotlin.math.min

@ConfigurationProperties(prefix = "citybikeapp")
data class SvcConfig(
    val defaultPageSize: Int,
    val maxPageSize: Int,
) {
    fun getMaxLimitedPageSize(size: Int?) =
        if (size == null) {
            defaultPageSize
        } else {
            min(size, maxPageSize)
        }
}
