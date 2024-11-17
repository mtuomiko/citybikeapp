package com.mtuomiko.citybikeapp.common.model

data class PaginationKeyset<T1, Long>(
    val value: T1,
    val id: Long,
)
