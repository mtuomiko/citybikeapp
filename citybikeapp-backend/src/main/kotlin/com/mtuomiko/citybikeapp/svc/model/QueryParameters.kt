package com.mtuomiko.citybikeapp.svc.model

data class JourneyQueryParameters(
    val orderBy: String? = null,
    val direction: Direction? = null,
    val pageSize: Int? = null,
    val nextCursor: String? = null,
)

data class StationQueryParameters(
    val orderBy: String? = null,
    val direction: Direction? = null,
    val searchTokens: List<String> = emptyList(),
    val page: Int? = null,
    val pageSize: Int? = null,
)

enum class Direction(
    val text: String,
) {
    ASC("asc"),
    DESC("desc"),
}
