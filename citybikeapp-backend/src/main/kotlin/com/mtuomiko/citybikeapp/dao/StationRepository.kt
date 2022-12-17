package com.mtuomiko.citybikeapp.dao

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.repository.PageableRepository

@JdbcRepository
interface StationRepository : PageableRepository<StationEntity, Int>
