package com.mtuomiko.citybikeapp.dao

import com.mtuomiko.citybikeapp.dao.entity.JourneyEntity
import com.mtuomiko.citybikeapp.model.JourneyNew
import io.micronaut.data.annotation.Join
import io.micronaut.data.annotation.repeatable.JoinSpecifications
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.jdbc.runtime.JdbcOperations
import io.micronaut.data.repository.PageableRepository
import java.sql.Timestamp
import javax.transaction.Transactional

@Suppress("MagicNumber")
@JdbcRepository
@JoinSpecifications(
    Join(value = "departureStation"),
    Join(value = "arrivalStation")
)
abstract class JourneyRepository(private val jdbcOperations: JdbcOperations) : PageableRepository<JourneyEntity, Long> {

    @Transactional
    fun saveInBatch(journeys: List<JourneyNew>) {
        val duplicateIgnoringInsertSQL = """
            INSERT INTO journey (
                departure_at,
                arrival_at,
                departure_station_id,
                arrival_station_id,
                distance,
                duration
            ) VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT ON CONSTRAINT journey_content_unique DO NOTHING;
        """.trimIndent()
        jdbcOperations.prepareStatement(duplicateIgnoringInsertSQL) { ps ->
            journeys.forEach {
                ps.setTimestamp(1, Timestamp.from(it.departureAt))
                ps.setTimestamp(2, Timestamp.from(it.arrivalAt))
                ps.setInt(3, it.departureStation)
                ps.setInt(4, it.arrivalStation)
                ps.setInt(5, it.distance)
                ps.setInt(6, it.duration)
                ps.addBatch()
            }
            ps.executeBatch()
        }
    }

    @Transactional
    fun getTripStatisticsByStationId(stationId: Int): JourneyStatistics {
        val sql = """
            SELECT 
                COUNT(*) FILTER (WHERE departure_station_id = ?) as departure_count,
                COUNT(*) FILTER (WHERE arrival_station_id = ?) as arrival_count, 
                AVG(distance) FILTER (WHERE departure_station_id = ?) as avg_departure_dist,
                AVG(distance) FILTER (WHERE arrival_station_id = ?) as avg_arrival_dist
            FROM (
                SELECT distance, departure_station_id, arrival_station_id FROM journey
                WHERE departure_station_id = ? OR arrival_station_id = ?
            ) as filtered_stations;
        """.trimIndent()
        return jdbcOperations.prepareStatement(sql) { ps ->
            ps.setInt(1, stationId)
            ps.setInt(2, stationId)
            ps.setInt(3, stationId)
            ps.setInt(4, stationId)
            ps.setInt(5, stationId)
            ps.setInt(6, stationId)
            val resultSet = ps.executeQuery()
            // TODO: handle empty results
            resultSet.next()
            JourneyStatistics(
                departureCount = resultSet.getLong(1),
                arrivalCount = resultSet.getLong(2),
                departureAverageDistance = resultSet.getDouble(3),
                arrivalAverageDistance = resultSet.getDouble(4)
            )
        }
    }

    /**
     * TODO: Support timestamp filtering, use JDBI or jOOQ?
     */
    @Transactional
    fun getTopStationsByStationId(stationId: Int): List<TopStations> {
        val sql = """
            SELECT departure_station_id, arrival_station_id, journeys, name_finnish, name_swedish, name_english
            FROM (
                SELECT departure_station_id, arrival_station_id, COUNT(*) as journeys
                FROM journey
                WHERE arrival_station_id = ?
                GROUP BY departure_station_id, arrival_station_id
                ORDER BY journeys DESC
                LIMIT 5
            ) as top_departure_stations
            JOIN station ON station.id = top_departure_stations.departure_station_id
            
            UNION
            
            SELECT departure_station_id, arrival_station_id, journeys, name_finnish, name_swedish, name_english
            FROM (
                SELECT departure_station_id, arrival_station_id, COUNT(*) as journeys
                FROM journey
                WHERE departure_station_id = ?
                GROUP BY departure_station_id, arrival_station_id
                ORDER BY journeys DESC
                LIMIT 5
            ) as top_arrival_stations
            JOIN station ON station.id = top_arrival_stations.arrival_station_id;
        """.trimIndent()
        return jdbcOperations.prepareStatement(sql) { ps ->
            ps.setInt(1, stationId)
            ps.setInt(2, stationId)
            val resultSet = ps.executeQuery()
            val result = mutableListOf<TopStations>()
            while (resultSet.next()) {
                result.add(
                    TopStations(
                        departureStationId = resultSet.getInt(1),
                        arrivalStationId = resultSet.getInt(2),
                        journeyCount = resultSet.getLong(3),
                        nameFinnish = resultSet.getString(4),
                        nameSwedish = resultSet.getString(5),
                        nameEnglish = resultSet.getString(6)
                    )
                )
            }
            result.toList()
        }
    }
}
