package com.mtuomiko.citybikeapp.svc.model

data class JourneyQueryParameters(
    val orderBy: String? = null,
    val direction: Direction? = null,
    val pageSize: Int? = null,
    val nextCursor: String? = null,
)

enum class Direction(
    val text: String,
) {
    ASC("asc"),
    DESC("desc"),
}
