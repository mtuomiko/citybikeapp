package com.mtuomiko.citybikeapp.dao.model

class StationSearchResult(
    val totalCount: Int,
    val id: Int,
    val nameFinnish: String,
    val addressFinnish: String,
    val cityFinnish: String,
    val operator: String,
    val capacity: Int
)
