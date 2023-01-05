package com.mtuomiko.citybikeapp.common

import java.time.ZoneId

// Assuming data timestamps to be local Helsinki time.
private const val TIMEZONE_ID = "Europe/Helsinki"
val TIMEZONE: ZoneId = ZoneId.of(TIMEZONE_ID)
