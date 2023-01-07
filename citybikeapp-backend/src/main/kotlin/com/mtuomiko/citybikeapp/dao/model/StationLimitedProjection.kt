package com.mtuomiko.citybikeapp.dao.model

import io.micronaut.core.annotation.Introspected

@Introspected
class StationLimitedProjection(
    val id: Int,
    val nameFinnish: String
)
