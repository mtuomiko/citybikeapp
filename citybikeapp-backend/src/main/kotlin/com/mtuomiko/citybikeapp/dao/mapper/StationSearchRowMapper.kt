package com.mtuomiko.citybikeapp.dao.mapper

import com.mtuomiko.citybikeapp.dao.model.StationSearchResult
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

@Suppress("MagicNumber")
class StationSearchRowMapper : RowMapper<StationSearchResult> {
    override fun map(resultSet: ResultSet, ctx: StatementContext): StationSearchResult = StationSearchResult(
        totalCount = resultSet.getInt(2),
        id = resultSet.getInt(3),
        nameFinnish = resultSet.getString(4),
        addressFinnish = resultSet.getString(5),
        cityFinnish = resultSet.getString(6),
        operator = resultSet.getString(7),
        capacity = resultSet.getInt(8)
    )
}
