package com.mtuomiko.citybikeapp.dao

import com.mtuomiko.citybikeapp.model.JourneyNew
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.jdbc.runtime.JdbcOperations
import io.micronaut.data.repository.PageableRepository
import java.sql.Timestamp
import javax.transaction.Transactional

@JdbcRepository
abstract class JourneyRepository(private val jdbcOperations: JdbcOperations) : PageableRepository<JourneyEntity, Long> {

    @Suppress("MagicNumber")
    @Transactional
    fun saveInBatch(journeys: List<JourneyNew>) {
        val duplicateIgnoringInsertSQL = """
            INSERT INTO journey (
                departure_at,
                return_at,
                departure_station_id,
                return_station_id,
                distance,
                duration
            ) VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT ON CONSTRAINT journey_content_unique DO NOTHING;
        """.trimIndent()
        jdbcOperations.prepareStatement(duplicateIgnoringInsertSQL) { ps ->
            journeys.forEach {
                ps.setTimestamp(1, Timestamp.from(it.departureAt))
                ps.setTimestamp(2, Timestamp.from(it.returnAt))
                ps.setInt(3, it.departureStation)
                ps.setInt(4, it.returnStation)
                ps.setInt(5, it.distance)
                ps.setInt(6, it.duration)
                ps.addBatch()
            }
            ps.executeBatch()
        }
    }
}
