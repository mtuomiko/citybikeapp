package com.mtuomiko.citybikeapp.dao

/**
 * List is in relation to a specific station. The names are always for the *other* station, not for the station which is
 * being queried (unless they happen to be the same station).
 */
class TopStationsResult(
    val departureStationId: Int,
    val arrivalStationId: Int,
    val journeyCount: Long,
    val nameFinnish: String,
    val nameSwedish: String,
    val nameEnglish: String
)
