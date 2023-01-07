package com.mtuomiko.citybikeapp.dao.repository

import com.mtuomiko.citybikeapp.common.model.Station
import com.mtuomiko.citybikeapp.common.model.StationNew
import com.mtuomiko.citybikeapp.dao.entity.StationEntity
import com.mtuomiko.citybikeapp.dao.mapper.StationRowMapper
import com.mtuomiko.citybikeapp.dao.model.StationInsert
import com.mtuomiko.citybikeapp.dao.model.StationLimitedProjection
import com.mtuomiko.citybikeapp.dao.model.StationProjection
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.Pageable
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
    private val stationMapper = StationRowMapper()

    @Transactional
    fun saveInBatchIgnoringConflicts(stations: List<StationNew>): Int {
        val now = instantSource.instant()
        val stationInserts = stations.map { StationInsert.from(it, now) }

        return jdbi.withHandleUnchecked { handle ->
            val batch = handle
                .prepareBatch("INSERT INTO station (<columns>) VALUES (<values>) ON CONFLICT (id) DO NOTHING")
                .defineList("columns", columns)
                .defineList("values", bindValues)
            stationInserts.forEach {
                batch.bindBean(it).add()
            }
            batch.execute().size
        }
    }

    /**
     * trgm_ops indexing (for speeding up regex matching) is not setup since the amount of station rows is in practice
     * fairly small and planner will not use the index. Also, said indexing cannot not be used for keyset pagination as
     * match_count depends on the search pattern.
     */
    @Transactional
    fun searchUsingRegex(pattern: String, limit: Int, offset: Int): List<Station> {
        val sql = """
        SELECT match_count, id, name_finnish, address_finnish, city_finnish, operator, capacity FROM station, LATERAL (
            SELECT count(*) as match_count
            FROM regexp_matches(lower(
                name_finnish || ' ' || address_finnish || ' ' || name_swedish || ' ' || address_swedish || ' ' ||
                name_english
            ), :pattern, 'g')
        ) as match_count_table
        WHERE name_finnish || ' ' || address_finnish || ' ' || name_swedish || ' ' || address_swedish || ' ' ||
            name_english ~* :pattern
        ORDER BY match_count DESC, id ASC
        LIMIT :limit OFFSET :offset;
        """.trimIndent()

        return jdbi.withHandleUnchecked { handle ->
            handle.createQuery(sql)
                .bind("pattern", pattern)
                .bind("limit", limit)
                .bind("offset", offset)
                .map(stationMapper)
                .list()
        }
    }

    abstract fun getAll(): List<StationLimitedProjection>

    abstract fun list(pageable: Pageable): List<StationProjection>
}
