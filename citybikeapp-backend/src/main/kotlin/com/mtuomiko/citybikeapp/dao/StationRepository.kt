package com.mtuomiko.citybikeapp.dao

import com.mtuomiko.citybikeapp.model.StationNew
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.jdbc.runtime.JdbcOperations
import io.micronaut.data.repository.PageableRepository
import java.sql.Timestamp
import java.time.InstantSource
import javax.transaction.Transactional

@JdbcRepository
abstract class StationRepository(
    private val jdbcOperations: JdbcOperations,
    private val instantSource: InstantSource
) : PageableRepository<StationEntity, Int> {

    @Suppress("MagicNumber")
    @Transactional
    fun saveInBatch(stations: List<StationNew>): Int {
        val now = instantSource.instant()
        val sql = """
            INSERT INTO "station" (
                "id",
                "name_finnish",
                "name_swedish",
                "name_english",
                "address_finnish",
                "address_swedish",
                "city_finnish",
                "city_swedish",
                "operator",
                "capacity",
                "longitude",
                "latitude",
                "modified_at",
                "created_at"
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()
        return jdbcOperations.prepareStatement(sql) { ps ->
            stations.forEach {
                ps.setInt(1, it.id!!)
                ps.setString(2, it.nameFinnish)
                ps.setString(3, it.nameSwedish)
                ps.setString(4, it.nameEnglish)
                ps.setString(5, it.addressFinnish)
                ps.setString(6, it.addressSwedish)
                ps.setString(7, it.cityFinnish)
                ps.setString(8, it.citySwedish)
                ps.setString(9, it.operator)
                ps.setInt(10, it.capacity)
                ps.setDouble(11, it.longitude)
                ps.setDouble(12, it.latitude)
                ps.setTimestamp(13, Timestamp.from(now))
                ps.setTimestamp(14, Timestamp.from(now))
                ps.addBatch()
            }
            val resultArray = ps.executeBatch()
            resultArray.size
        }
    }
}
