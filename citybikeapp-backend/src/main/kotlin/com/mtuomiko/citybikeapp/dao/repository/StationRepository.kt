package com.mtuomiko.citybikeapp.dao.repository

import com.mtuomiko.citybikeapp.common.model.StationNew
import com.mtuomiko.citybikeapp.dao.entity.StationEntity
import com.mtuomiko.citybikeapp.dao.model.StationLimitedProjection
import com.mtuomiko.citybikeapp.dao.model.StationSearchResult
import com.mtuomiko.citybikeapp.jooq.tables.records.StationRecord
import com.mtuomiko.citybikeapp.jooq.tables.references.STATION
import jakarta.inject.Singleton
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.jooq.impl.DSL.concat
import org.jooq.impl.DSL.count
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.inline
import org.jooq.impl.DSL.lateral
import org.jooq.impl.DSL.lower
import org.jooq.impl.DSL.select
import org.jooq.impl.DSL.table
import java.time.Instant
import java.time.InstantSource

@Singleton
class StationRepository(
    private val ctx: DSLContext,
    private val instantSource: InstantSource
) {
    fun saveAll(stations: List<StationEntity>): List<StationEntity> {
        return stations.map(::save)
    }

    fun save(station: StationEntity): StationEntity {
        return ctx.insertInto(STATION).columns(STATION.fields().toList()).values(station.toRecord()).returning()
            .fetchOne()!!.toEntity()
    }

    fun findAll(): List<StationEntity> {
        return ctx.selectFrom(STATION).fetch().map { it.toEntity() }
    }

    fun deleteAll() {
        ctx.delete(STATION).execute()
    }

    fun findById(id: Int): StationEntity? = ctx.selectFrom(STATION).where(STATION.ID.eq(id)).fetchOne()?.toEntity()

    fun getAllStationIds(): List<Int> = with(STATION) {
        ctx.select(ID).from(STATION).fetch().getValues(ID).map { it!! }
    }

    fun getAllStationsLimited(): List<StationLimitedProjection> = with(STATION) {
        ctx.select(ID, NAME_FINNISH).from(STATION).fetchInto(StationLimitedProjection::class.java)
    }

    fun saveInBatchIgnoringConflicts(stations: List<StationNew>) {
        val now = instantSource.instant()
        val records = stations.map { it.toRecord(now) }

        ctx.loadInto(STATION)
            .onDuplicateKeyIgnore()
            .batchAll()
            .loadRecords(records)
            .fieldsCorresponding()
            .execute()
    }

    private fun StationNew.toRecord(time: Instant) = StationRecord(
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
        time
    )

    private fun StationEntity.toRecord() = StationRecord(
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
        createdAt
    )

    private fun StationRecord.toEntity() = StationEntity(
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
        createdAt!!
    )

    /**
     * trgm_ops (gin/gist) indexing (for speeding up regex matching) is not setup as of V002 of migrations, since the
     * amount of station rows is in practice fairly small and planner will not use the index. Also, said indexing cannot
     * not be used for keyset pagination as match count depends on the search pattern.
     */
    fun searchUsingRegex(pattern: String, limit: Int, offset: Int): List<StationSearchResult> {
        val matchCount = count().`as`("match_count")
        val searchable = lower(
            concat(
                STATION.NAME_FINNISH,
                inline(" "),
                STATION.ADDRESS_FINNISH,
                inline(" "),
                STATION.NAME_SWEDISH,
                inline(" "),
                STATION.ADDRESS_SWEDISH,
                inline(" "),
                STATION.NAME_ENGLISH
            )
        )
        val matchCountTable = table("regexp_matches({0}, {1}, 'g')", searchable, DSL.`val`(pattern))

        return ctx.select(
            field("match_count"),
            count().over(),
            STATION.ID,
            STATION.NAME_FINNISH,
            STATION.ADDRESS_FINNISH,
            STATION.CITY_FINNISH,
            STATION.OPERATOR,
            STATION.CAPACITY
        ).from(
            STATION,
            lateral(
                select(matchCount).from(matchCountTable)
            )
        )
            .where(searchable.likeRegex(pattern))
            .orderBy(matchCount.desc(), STATION.ID.asc())
            .limit(limit)
            .offset(offset)
            .fetch()
            .map {
                StationSearchResult(
                    totalCount = it.component2(),
                    id = it.component3()!!,
                    nameFinnish = it.component4()!!,
                    addressFinnish = it.component5()!!,
                    cityFinnish = it.component6()!!,
                    operator = it.component7()!!,
                    capacity = it.component8()!!
                )
            }
    }

    fun listStations(limit: Int, offset: Int): List<StationSearchResult> {
        return ctx.select(
            count().over(),
            STATION.ID,
            STATION.NAME_FINNISH,
            STATION.ADDRESS_FINNISH,
            STATION.CITY_FINNISH,
            STATION.OPERATOR,
            STATION.CAPACITY
        ).from(STATION)
            .orderBy(STATION.ID.asc())
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
                    capacity = it.component7()!!
                )
            }
    }
}
