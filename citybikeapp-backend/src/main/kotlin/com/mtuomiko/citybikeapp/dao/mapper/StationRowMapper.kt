package com.mtuomiko.citybikeapp.dao.mapper

import com.mtuomiko.citybikeapp.common.model.Station
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

@Suppress("MagicNumber")
class StationRowMapper : RowMapper<Station> {
    override fun map(resultSet: ResultSet, ctx: StatementContext): Station = Station(
        id = resultSet.getInt(2),
        nameFinnish = resultSet.getString(3),
        addressFinnish = resultSet.getString(4),
        cityFinnish = resultSet.getString(5),
        operator = resultSet.getString(6),
        capacity = resultSet.getInt(7)
    )
}
