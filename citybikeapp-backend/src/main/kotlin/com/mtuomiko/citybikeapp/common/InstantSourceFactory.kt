package com.mtuomiko.citybikeapp.common

import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton
import java.time.InstantSource

@Factory
class InstantSourceFactory {
    /**
     * Common source for accessing system time. Should help testing.
     */
    @Singleton
    fun instantSource(): InstantSource = InstantSource.system()
}
