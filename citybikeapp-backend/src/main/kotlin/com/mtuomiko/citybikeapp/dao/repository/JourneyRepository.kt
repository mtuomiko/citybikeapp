package com.mtuomiko.citybikeapp.dao.repository

import com.mtuomiko.citybikeapp.common.model.JourneyNew
import com.mtuomiko.citybikeapp.common.model.PaginationKeyset
import com.mtuomiko.citybikeapp.dao.entity.JourneyEntity
import com.mtuomiko.citybikeapp.dao.model.JourneyStatistics
import com.mtuomiko.citybikeapp.dao.model.TopStationsQueryResult
import com.mtuomiko.citybikeapp.jooq.tables.Journey.Companion.JOURNEY
import com.mtuomiko.citybikeapp.jooq.tables.records.JourneyRecord
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.future.asDeferred
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Record3
import org.jooq.SelectLimitPercentStep
import org.jooq.impl.DSL.avg
import org.jooq.impl.DSL.count
import org.springframework.stereotype.Repository
import java.security.InvalidParameterException
import java.time.Instant

@Suppress("TooManyFunctions") // Repository class, I'm okay with this
@Repository
class JourneyRepository(
    private val ctx: DSLContext,
) {
    // null offset id argument position to use for fields insert of new journeys without ids
    private val nullOffsetWithoutIdFields = listOf(null) + JOURNEY.fields().filter { it != JOURNEY.ID }
    private val fieldsExcludingId = JOURNEY.fields().filter { it != JOURNEY.ID }

    fun saveAll(journeys: List<JourneyEntity>): List<JourneyEntity> = journeys.map(::save)

    fun save(journey: JourneyEntity): JourneyEntity =
        ctx
            .insertInto(JOURNEY)
            .columns(JOURNEY.fields().toList())
            .values(journey.toRecord())
            .returning()
            .fetchOne()!!
            .toEntity()

    fun saveAllNewJourneys(journeys: List<JourneyNew>): List<JourneyEntity> = journeys.map(::saveNewJourney)

    fun saveNewJourney(journey: JourneyNew): JourneyEntity =
        ctx
            .insertInto(JOURNEY)
            .columns(fieldsExcludingId)
            .values(journey.toRecord().toValuesExcludingId())
            .returning()
            .fetchOne()!!
            .toEntity()

    // test usage
    fun deleteAll() {
        ctx.delete(JOURNEY).execute()
    }

    fun findAll(): List<JourneyEntity> = ctx.selectFrom(JOURNEY).fetch().map { it.toEntity() }

    fun getCount(): Int = ctx.fetchCount(JOURNEY)

    fun saveInBatchIgnoringConflicts(journeys: List<JourneyNew>) {
        val records = journeys.map { it.toRecord() }

        ctx
            .loadInto(JOURNEY)
            .onDuplicateKeyIgnore()
            .batchAll()
            .loadRecords(records)
            .fields(nullOffsetWithoutIdFields)
            .execute()
    }

    /**
     * keyset
     */
    fun listJourneys(
        orderBy: String,
        descending: Boolean,
        pageSize: Int,
        keyset: PaginationKeyset<out Any, Long>?,
    ): List<JourneyEntity> {
        val orderFields =
            if (descending) {
                listOf(getJourneyField(orderBy).desc(), JOURNEY.ID.desc())
            } else {
                listOf(getJourneyField(orderBy).asc(), JOURNEY.ID.asc())
            }

        return ctx
            .selectFrom(JOURNEY)
            .orderBy(orderFields)
            .apply { if (keyset != null) seek(keyset.value, keyset.id) }
            .limit(pageSize)
            .fetch {
                it.toEntity()
            }
    }

    /**
     *
     */
    private fun getJourneyField(field: String) =
        with(JOURNEY) {
            when (field) {
                "id" -> ID
                "departureAt" -> DEPARTURE_AT
                "arrivalAt" -> ARRIVAL_AT
                "departureStationId" -> DEPARTURE_STATION_ID
                "arrivalStationId" -> ARRIVAL_STATION_ID
                "distance" -> DISTANCE
                "duration" -> DURATION
                else -> throw InvalidParameterException("unknown journey field")
            }
        }

    fun getJourneyStatisticsByStationId(
        stationId: Int,
        from: Instant? = null,
        to: Instant? = null,
    ): Deferred<JourneyStatistics> =
        with(JOURNEY) {
            val condition =
                DEPARTURE_STATION_ID
                    .eq(stationId)
                    .or(ARRIVAL_STATION_ID.eq(stationId))
                    .addDepartureTimestampConditions(from, to)

            ctx
                .select(
                    count().filterWhere(DEPARTURE_STATION_ID.eq(stationId)),
                    count().filterWhere(ARRIVAL_STATION_ID.eq(stationId)),
                    avg(DISTANCE).filterWhere(DEPARTURE_STATION_ID.eq(stationId)),
                    avg(DISTANCE).filterWhere(ARRIVAL_STATION_ID.eq(stationId)),
                ).from(JOURNEY)
                .where(condition)
                .fetchAsync()
                .thenApply { result ->
                    result.first().let {
                        JourneyStatistics(
                            departureCount = it.component1().toLong(),
                            arrivalCount = it.component2().toLong(),
                            departureAverageDistance = (it.component3() ?: 0).toDouble(), // avg can have null results
                            arrivalAverageDistance = (it.component4() ?: 0).toDouble(),
                        )
                    }
                }.asDeferred()
        }

    fun getTopStationsByStationId(
        stationId: Int,
        limitPerDirection: Int,
        from: Instant? = null,
        to: Instant? = null,
    ): Deferred<List<TopStationsQueryResult>> {
        val departureStationsCondition =
            JOURNEY.ARRIVAL_STATION_ID.eq(stationId).addDepartureTimestampConditions(from, to)
        val arrivalStationsCondition =
            JOURNEY.DEPARTURE_STATION_ID.eq(stationId).addDepartureTimestampConditions(from, to)

        return createJourneyStatisticSelect(departureStationsCondition, limitPerDirection)
            .union(createJourneyStatisticSelect(arrivalStationsCondition, limitPerDirection))
            .fetchAsync()
            .thenApply { result ->
                result.map { TopStationsQueryResult(it.component1()!!, it.component2()!!, it.component3()!!.toLong()) }
            }.asDeferred()
    }

    private fun createJourneyStatisticSelect(
        condition: Condition,
        limitPerDirection: Int,
    ): SelectLimitPercentStep<Record3<Int?, Int?, Int>> {
        val journeyCount = count()
        return ctx
            .select(
                JOURNEY.DEPARTURE_STATION_ID,
                JOURNEY.ARRIVAL_STATION_ID,
                journeyCount,
            ).from(JOURNEY)
            .where(condition)
            .groupBy(JOURNEY.DEPARTURE_STATION_ID, JOURNEY.ARRIVAL_STATION_ID)
            .orderBy(journeyCount.desc())
            .limit(limitPerDirection)
    }

    private fun Condition.addDepartureTimestampConditions(
        from: Instant?,
        to: Instant?,
    ) = this
        .run {
            if (from != null) {
                and(JOURNEY.DEPARTURE_AT.greaterOrEqual(from))
            } else {
                this
            }
        }.run {
            if (to != null) {
                and(JOURNEY.DEPARTURE_AT.lessThan(to))
            } else {
                this
            }
        }

    private fun JourneyNew.toRecord() =
        JourneyRecord(
            null,
            departureAt,
            arrivalAt,
            departureStationId,
            arrivalStationId,
            distance,
            duration,
        )

    private fun JourneyEntity.toRecord() =
        JourneyRecord(
            id,
            departureAt,
            arrivalAt,
            departureStationId,
            arrivalStationId,
            distance,
            duration,
        )

    private fun JourneyRecord.toEntity() =
        JourneyEntity(
            id!!,
            departureAt!!,
            arrivalAt!!,
            departureStationId!!,
            arrivalStationId!!,
            distance!!,
            duration!!,
        )

    private fun JourneyRecord.toValuesExcludingId() = this.intoList().drop(1)
}
