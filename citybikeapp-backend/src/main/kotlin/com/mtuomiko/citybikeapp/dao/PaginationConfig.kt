package com.mtuomiko.citybikeapp.dao

import io.micronaut.context.annotation.ConfigurationInject
import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("citybikeapp")
class PaginationConfig @ConfigurationInject constructor(
    val defaultPageSize: Int
)
