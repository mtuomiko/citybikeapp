package com.mtuomiko.citybikeapp.svc.model

enum class QueryParameter(val value: String) {
    ORDER_BY("orderBy"),
    DIRECTION("direction"),
    PAGE_SIZE("pageSize"),
    NEXT_CURSOR("nextCursor")
}
