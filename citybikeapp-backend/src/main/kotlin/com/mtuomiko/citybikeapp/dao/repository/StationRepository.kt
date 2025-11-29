package com.mtuomiko.citybikeapp.dao.repository

import com.mtuomiko.citybikeapp.common.model.StationNew
import com.mtuomiko.citybikeapp.dao.entity.StationEntity
import com.mtuomiko.citybikeapp.dao.model.StationLimitedProjection
import com.mtuomiko.citybikeapp.dao.model.StationSearchResult
import com.mtuomiko.citybikeapp.jooq.tables.records.StationRecord
import com.mtuomiko.citybikeapp.jooq.tables.references.STATION
import org.jooq.DSLContext
import org.jooq.SortField
import org.jooq.impl.DSL.concat
import org.jooq.impl.DSL.count
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.inline
import org.jooq.impl.DSL.lower
import org.jooq.impl.DSL.select
import org.springframework.stereotype.Repository
import java.security.InvalidParameterException
import java.time.Instant
import java.time.InstantSource

/**
 * Generic note about Kotlin null handling and jOOQ: in some places we use the Kotlin not-null assertion (!!) since it's
 * guaranteed that the data is available (and we don't want to deal with null uncertainty elsewhere), but it's
 * impossible for jOOQ to give us that information (that it's not null). Based on the queries generated and the database
 * schema we, as developers (or I really, but anyway) can say that if there are results, they will have those fields as
 * non-null.
 */
@Suppress("TooManyFunctions") // Repository class, I'm okay with this
@Repository
class StationRepository(
    private val ctx: DSLContext,
    private val instantSource: InstantSource,
) {
    fun saveAll(stations: List<StationEntity>): List<StationEntity> = stations.map(::save)

    fun save(station: StationEntity): StationEntity =
        ctx
            .insertInto(STATION)
            .columns(STATION.fields().toList())
            .values(station.toRecord())
            .returning()
            .fetchOne()!!
            .toEntity()

    fun findAll(): List<StationEntity> = ctx.selectFrom(STATION).fetch().map { it.toEntity() }

    fun deleteAll() {
        ctx.delete(STATION).execute()
    }

    fun findById(id: Int): StationEntity? =
        ctx
            .selectFrom(STATION)
            .where(STATION.ID.eq(id))
            .fetchOne()
            ?.toEntity()

    fun existsById(id: Int): Boolean = ctx.fetchExists(ctx.selectOne().from(STATION).where(STATION.ID.eq(id)))

    fun getAllStationIds(): List<Int> =
        with(STATION) {
            ctx
                .select(ID)
                .from(STATION)
                .fetch()
                .getValues(ID)
                .map { it!! }
        }

    fun getAllStationsLimited(): List<StationLimitedProjection> =
        with(STATION) {
            ctx.select(ID, NAME_FINNISH).from(STATION).fetchInto(StationLimitedProjection::class.java)
        }

    fun saveInBatchIgnoringConflicts(stations: List<StationNew>) {
        val now = instantSource.instant()
        val records = stations.map { it.toRecord(now) }

        ctx
            .loadInto(STATION)
            .onDuplicateKeyIgnore()
            .batchAll()
            .loadRecords(records)
            .fieldsCorresponding()
            .execute()
    }

    private fun StationNew.toRecord(time: Instant) =
        StationRecord(
            id,
            nameFinnish,
            nameSwedish,
            nameEnglish,
            addressFinnish,
            addressSwedish,
            cityFinnish,
            citySwedish,
            operator,
            capacity,
            longitude,
            latitude,
            time,
            time,
        )

    private fun StationEntity.toRecord() =
        StationRecord(
            id,
            nameFinnish,
            nameSwedish,
            nameEnglish,
            addressFinnish,
            addressSwedish,
            cityFinnish,
            citySwedish,
            operator,
            capacity,
            longitude,
            latitude,
            modifiedAt,
            createdAt,
        )

    private fun StationRecord.toEntity() =
        StationEntity(
            id!!,
            nameFinnish!!,
            nameSwedish!!,
            nameEnglish!!,
            addressFinnish!!,
            addressSwedish!!,
            cityFinnish!!,
            citySwedish!!,
            operator!!,
            capacity!!,
            longitude!!,
            latitude!!,
            modifiedAt!!,
            createdAt!!,
        )

    /**
     * trgm_ops (gin/gist) indexing for speeding up regex matching was considered but is not setup, since the amount of
     * station rows is small and at least postgres 15.6 planner will not use the index. Also, said indexing cannot be
     * used for pagination as match count (variable representing how "good" the match is) depends on the search pattern.
     */
    fun searchUsingRegex(
        pattern: String,
        orderBy: String,
        descending: Boolean,
        limit: Int,
        offset: Int,
    ): List<StationSearchResult> {
        val orderFields = getOrderFields(orderBy, descending)

        // Form a concat string field of station info to search against. Not using other languages since not displayed.
        val searchable =
            lower(
                concat(
                    STATION.NAME_FINNISH,
                    inline(" "),
                    STATION.ADDRESS_FINNISH,
                ),
            )

        val lowercasePattern = pattern.lowercase() // just handle as lowercase, easier

        return ctx
            .select(
                count().over(),
                STATION.ID,
                STATION.NAME_FINNISH,
                STATION.ADDRESS_FINNISH,
                STATION.CITY_FINNISH,
                STATION.OPERATOR,
                STATION.CAPACITY,
            ).from(STATION)
            .where(searchable.likeRegex(lowercasePattern))
            .orderBy(orderFields)
            .limit(limit)
            .offset(offset)
            .fetch()
            .map {
                StationSearchResult(
                    totalCount = it.component1(),
                    id = it.component2()!!,
                    nameFinnish = it.component3()!!,
                    addressFinnish = it.component4()!!,
                    cityFinnish = it.component5()!!,
                    operator = it.component6()!!,
                    capacity = it.component7()!!,
                )
            }
    }

    fun listStations(
        orderBy: String,
        descending: Boolean,
        limit: Int,
        offset: Int,
    ): List<StationSearchResult> {
        val orderFields = getOrderFields(orderBy, descending)

        return ctx
            .select(
                count().over(),
                STATION.ID,
                STATION.NAME_FINNISH,
                STATION.ADDRESS_FINNISH,
                STATION.CITY_FINNISH,
                STATION.OPERATOR,
                STATION.CAPACITY,
            ).from(STATION)
            .orderBy(orderFields)
            .limit(limit)
            .offset(offset)
            .fetch()
            .map {
                StationSearchResult(
                    totalCount = it.component1(),
                    id = it.component2()!!,
                    nameFinnish = it.component3()!!,
                    addressFinnish = it.component4()!!,
                    cityFinnish = it.component5()!!,
                    operator = it.component6()!!,
                    capacity = it.component7()!!,
                )
            }
    }

    /**
     * Intrinsic sort by ID if nothing else available. Any other orderBy will be used additionally.
     */
    private fun getOrderFields(
        orderBy: String,
        descending: Boolean,
    ): MutableList<SortField<out Any?>> =
        if (descending) {
            if (orderBy == "id") {
                mutableListOf(STATION.ID.desc())
            } else {
                mutableListOf(getStationField(orderBy).desc(), STATION.ID.desc())
            }
        } else {
            if (orderBy == "id") {
                mutableListOf(STATION.ID.asc())
            } else {
                mutableListOf(getStationField(orderBy).asc(), STATION.ID.asc())
            }
        }

    /**
     *
     */
    private fun getStationField(field: String) =
        with(STATION) {
            when (field) {
                "id" -> ID
                "nameFinnish" -> NAME_FINNISH
                "addressFinnish" -> ADDRESS_FINNISH
                "cityFinnish" -> CITY_FINNISH
                "operator" -> OPERATOR
                "capacity" -> CAPACITY

                else -> throw InvalidParameterException("unknown station field: $field")
            }
        }
}
