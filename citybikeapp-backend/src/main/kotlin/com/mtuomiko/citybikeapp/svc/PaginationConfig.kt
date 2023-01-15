package com.mtuomiko.citybikeapp.svc

import io.micronaut.context.annotation.ConfigurationInject
import io.micronaut.context.annotation.ConfigurationProperties
import kotlin.math.min

@ConfigurationProperties("citybikeapp")
class PaginationConfig @ConfigurationInject constructor(
    val defaultPageSize: Int,
    val maxPageSize: Int
) {
    fun getMaxLimitedPageSize(size: Int?) = if (size == null) {
        defaultPageSize
    } else {
        min(size, maxPageSize)
    }
}
