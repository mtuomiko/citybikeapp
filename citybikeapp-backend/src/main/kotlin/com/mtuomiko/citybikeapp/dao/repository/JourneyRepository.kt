package com.mtuomiko.citybikeapp.dao.repository

import com.mtuomiko.citybikeapp.common.model.JourneyNew
import com.mtuomiko.citybikeapp.dao.entity.JourneyEntity
import com.mtuomiko.citybikeapp.dao.model.JourneyStatistics
import com.mtuomiko.citybikeapp.dao.model.TopStationsQueryResult
import com.mtuomiko.citybikeapp.jooq.tables.records.JourneyRecord
import com.mtuomiko.citybikeapp.jooq.tables.references.JOURNEY
import jakarta.inject.Singleton
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Record3
import org.jooq.SelectLimitPercentStep
import org.jooq.impl.DSL.avg
import org.jooq.impl.DSL.count
import java.time.Instant

@Singleton
class JourneyRepository(private val ctx: DSLContext) {
    fun findAll(): List<JourneyEntity> {
        return ctx.selectFrom(JOURNEY).fetch().map { it.toEntity() }
    }

    private fun JourneyRecord.toEntity() = JourneyEntity(
        id!!,
        departureAt!!,
        arrivalAt!!,
        departureStationId!!,
        arrivalStationId!!,
        distance!!,
        duration!!
    )

    fun saveInBatchIgnoringConflicts(journeys: List<JourneyNew>) {
        val records = journeys.map { it.toRecord() }

        val insertFields = JOURNEY.fields().filter { it != JOURNEY.ID }
        val offsetInsertFields = listOf(null) + insertFields // offset id argument position to use fields insert
        ctx.loadInto(JOURNEY)
            .onDuplicateKeyIgnore()
            .batchAll()
            .loadRecords(records)
            .fields(offsetInsertFields)
            .execute()
    }

    fun getJourneyStatisticsByStationId(stationId: Int, from: Instant? = null, to: Instant? = null): JourneyStatistics {
        return with(JOURNEY) {
            val condition = DEPARTURE_STATION_ID.eq(stationId).or(ARRIVAL_STATION_ID.eq(stationId))
                .andDepartureTimestampCondition(from, to)

            ctx.select(
                count().filterWhere(DEPARTURE_STATION_ID.eq(stationId)),
                count().filterWhere(ARRIVAL_STATION_ID.eq(stationId)),
                avg(DISTANCE).filterWhere(DEPARTURE_STATION_ID.eq(stationId)),
                avg(DISTANCE).filterWhere(ARRIVAL_STATION_ID.eq(stationId))
            ).from(JOURNEY)
                .where(condition)
                .fetchSingle()
                .let {
                    JourneyStatistics(
                        departureCount = it.component1().toLong(),
                        arrivalCount = it.component2().toLong(),
                        departureAverageDistance = (it.component3() ?: 0).toDouble(), // avg can have null results
                        arrivalAverageDistance = (it.component4() ?: 0).toDouble()
                    )
                }
        }
    }

    fun getTopStationsByStationId(
        stationId: Int,
        from: Instant? = null,
        to: Instant? = null,
        limitPerDirection: Int = 5
    ): List<TopStationsQueryResult> {
        val departureStationsCondition =
            JOURNEY.ARRIVAL_STATION_ID.eq(stationId).andDepartureTimestampCondition(from, to)
        val arrivalStationsCondition =
            JOURNEY.DEPARTURE_STATION_ID.eq(stationId).andDepartureTimestampCondition(from, to)

        return createJourneyStatisticSelect(departureStationsCondition, limitPerDirection)
            .union(createJourneyStatisticSelect(arrivalStationsCondition, limitPerDirection))
            .fetch().map {
                TopStationsQueryResult(it.component1()!!, it.component2()!!, it.component3()!!.toLong())
            }
    }

    private fun createJourneyStatisticSelect(
        condition: Condition,
        limitPerDirection: Int
    ): SelectLimitPercentStep<Record3<Int?, Int?, Int>> {
        val journeyCount = count()
        return ctx.select(
            JOURNEY.DEPARTURE_STATION_ID,
            JOURNEY.ARRIVAL_STATION_ID,
            journeyCount
        )
            .from(JOURNEY)
            .where(condition)
            .groupBy(JOURNEY.DEPARTURE_STATION_ID, JOURNEY.ARRIVAL_STATION_ID)
            .orderBy(journeyCount.desc())
            .limit(limitPerDirection)
    }

    private fun Condition.andDepartureTimestampCondition(from: Instant?, to: Instant?) =
        this.apply { if (from != null) and(JOURNEY.DEPARTURE_AT.greaterOrEqual(from)) }
            .apply { if (to != null) and(JOURNEY.DEPARTURE_AT.lessThan(to)) }

    private fun JourneyNew.toRecord() = JourneyRecord(
        null,
        departureAt,
        arrivalAt,
        departureStation,
        arrivalStation,
        distance,
        duration
    )
}
