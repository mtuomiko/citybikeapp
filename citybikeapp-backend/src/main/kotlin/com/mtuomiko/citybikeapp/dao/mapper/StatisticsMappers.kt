package com.mtuomiko.citybikeapp.dao.mapper

import com.mtuomiko.citybikeapp.dao.JourneyStatistics
import com.mtuomiko.citybikeapp.dao.TopStationsResult
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

@Suppress("MagicNumber")
class JourneyStatisticsMapper : RowMapper<JourneyStatistics> {
    override fun map(resultSet: ResultSet, ctx: StatementContext) = JourneyStatistics(
        departureCount = resultSet.getLong(1),
        arrivalCount = resultSet.getLong(2),
        departureAverageDistance = resultSet.getDouble(3),
        arrivalAverageDistance = resultSet.getDouble(4)
    )
}

@Suppress("MagicNumber")
class TopStationsMapper : RowMapper<TopStationsResult> {
    override fun map(resultSet: ResultSet, ctx: StatementContext) = TopStationsResult(
        departureStationId = resultSet.getInt(1),
        arrivalStationId = resultSet.getInt(2),
        journeyCount = resultSet.getLong(3),
        nameFinnish = resultSet.getString(4),
        nameSwedish = resultSet.getString(5),
        nameEnglish = resultSet.getString(6)
    )
}
