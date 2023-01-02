package com.mtuomiko.citybikeapp.dao

import com.mtuomiko.citybikeapp.dao.entity.JourneyEntity
import com.mtuomiko.citybikeapp.dao.mapper.JourneyStatisticsMapper
import com.mtuomiko.citybikeapp.dao.mapper.TopStationsMapper
import com.mtuomiko.citybikeapp.model.JourneyNew
import io.micronaut.data.annotation.Join
import io.micronaut.data.annotation.repeatable.JoinSpecifications
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.repository.PageableRepository
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.withHandleUnchecked
import org.jdbi.v3.stringtemplate4.StringTemplateEngine
import java.time.Instant
import javax.transaction.Transactional

@Suppress("MagicNumber")
@JdbcRepository
@JoinSpecifications(
    Join(value = "departureStation"),
    Join(value = "arrivalStation")
)
abstract class JourneyRepository(
    private val jdbi: Jdbi
) : PageableRepository<JourneyEntity, Long> {
    private val journeyMapping = linkedMapOf(
        "departure_at" to ":departureAt",
        "arrival_at" to ":arrivalAt",
        "departure_station_id" to ":departureStation",
        "arrival_station_id" to ":arrivalStation",
        "distance" to ":distance",
        "duration" to ":duration"
    )
    private val columns = journeyMapping.keys.toList()
    private val bindValues = journeyMapping.values.toList()
    private val journeyStatisticsMapper = JourneyStatisticsMapper()
    private val topStationsMapper = TopStationsMapper()
    private val journeyTimestampCondition =
        "<if(from)> AND departure_at >= :from<endif><if(to)> AND departure_at \\< :to<endif>"

    @Transactional
    fun saveInBatch(journeys: List<JourneyNew>): Int {
        val sql = """
            INSERT INTO journey (<columns>) VALUES (<values>) 
            ON CONFLICT ON CONSTRAINT journey_content_unique DO NOTHING
        """.trimIndent()

        return jdbi.withHandleUnchecked { handle ->
            val batch = handle.prepareBatch(sql)
                .defineList("columns", columns)
                .defineList("values", bindValues)
            journeys.forEach {
                batch.bindBean(it).add()
            }
            batch.execute().size
        }
    }

    @Transactional
    fun getTripStatisticsByStationId(stationId: Int, from: Instant? = null, to: Instant? = null): JourneyStatistics {
        val sql = """
            SELECT 
                COUNT(*) FILTER (WHERE departure_station_id = :stationId) as departure_count,
                COUNT(*) FILTER (WHERE arrival_station_id = :stationId) as arrival_count, 
                AVG(distance) FILTER (WHERE departure_station_id = :stationId) as avg_departure_dist,
                AVG(distance) FILTER (WHERE arrival_station_id = :stationId) as avg_arrival_dist
            FROM (
                SELECT distance, departure_station_id, arrival_station_id FROM journey
                WHERE (departure_station_id = :stationId OR arrival_station_id = :stationId)
                $journeyTimestampCondition
            ) as filtered_stations;
        """.trimIndent()

        return jdbi.withHandleUnchecked { handle ->
            handle.createQuery(sql)
                .setTemplateEngine(StringTemplateEngine())
                .define("from", from)
                .define("to", to)
                .bind("from", from)
                .bind("to", to)
                .bind("stationId", stationId)
                .map(journeyStatisticsMapper)
                .findFirst().get()
        }
    }

    @Transactional
    fun getTopStationsByStationId(
        stationId: Int,
        from: Instant? = null,
        to: Instant? = null,
        limitPerDirection: Int = 5
    ): List<TopStationsResult> {
        val sql = """
            SELECT departure_station_id, arrival_station_id, journeys, name_finnish, name_swedish, name_english
            FROM (
                SELECT departure_station_id, arrival_station_id, COUNT(*) as journeys
                FROM journey
                WHERE arrival_station_id = :stationId
                $journeyTimestampCondition
                GROUP BY departure_station_id, arrival_station_id
                ORDER BY journeys DESC
                LIMIT :limit
            ) as top_departure_stations
            JOIN station ON station.id = top_departure_stations.departure_station_id
            
            UNION
            
            SELECT departure_station_id, arrival_station_id, journeys, name_finnish, name_swedish, name_english
            FROM (
                SELECT departure_station_id, arrival_station_id, COUNT(*) as journeys
                FROM journey
                WHERE departure_station_id = :stationId
                $journeyTimestampCondition
                GROUP BY departure_station_id, arrival_station_id
                ORDER BY journeys DESC
                LIMIT :limit
            ) as top_arrival_stations
            JOIN station ON station.id = top_arrival_stations.arrival_station_id;
        """.trimIndent()

        return jdbi.withHandleUnchecked { handle ->
            handle.createQuery(sql)
                .setTemplateEngine(StringTemplateEngine())
                .define("from", from)
                .define("to", to)
                .bind("from", from)
                .bind("to", to)
                .bind("stationId", stationId)
                .bind("limit", limitPerDirection)
                .map(topStationsMapper)
                .list()
        }
    }
}
