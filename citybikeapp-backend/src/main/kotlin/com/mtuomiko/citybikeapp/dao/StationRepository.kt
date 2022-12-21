package com.mtuomiko.citybikeapp.dao

import com.mtuomiko.citybikeapp.dao.entity.StationEntity
import com.mtuomiko.citybikeapp.model.StationNew
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.repository.PageableRepository
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.withHandleUnchecked
import java.time.InstantSource
import javax.transaction.Transactional

@JdbcRepository
abstract class StationRepository(
    private val jdbi: Jdbi,
    private val instantSource: InstantSource
) : PageableRepository<StationEntity, Int> {
    private val stationMapping = linkedMapOf(
        "id" to ":id",
        "name_finnish" to ":nameFinnish",
        "name_swedish" to ":nameSwedish",
        "name_english" to ":nameEnglish",
        "address_finnish" to ":addressFinnish",
        "address_swedish" to ":addressSwedish",
        "city_finnish" to ":cityFinnish",
        "city_swedish" to ":citySwedish",
        "operator" to ":operator",
        "capacity" to ":capacity",
        "longitude" to ":longitude",
        "latitude" to ":latitude",
        "modified_at" to ":modifiedAt",
        "created_at" to ":createdAt"
    )
    private val columns = stationMapping.keys.toList()
    private val bindValues = stationMapping.values.toList()

    @Transactional
    fun saveInBatch(stations: List<StationNew>): Int {
        val now = instantSource.instant()
        val insertStations = stations.map { StationInsert.from(it, now) }

        return jdbi.withHandleUnchecked { handle ->
            val batch = handle
                .prepareBatch("INSERT INTO station (<columns>) VALUES (<values>) ON CONFLICT (id) DO NOTHING")
                .defineList("columns", columns)
                .defineList("values", bindValues)
            insertStations.forEach {
                batch.bindBean(it).add()
            }
            batch.execute().size
        }
    }
}
