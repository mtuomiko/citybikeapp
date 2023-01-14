package com.mtuomiko.citybikeapp.common.model

data class PaginationKeyset<T1, T2>(
    val value: T1,
    val id: T2
)
