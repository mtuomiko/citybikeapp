package com.mtuomiko.citybikeapp.common

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.InstantSource

@Configuration
class InstantSourceFactory {
    /**
     * Common source for accessing system time. Should help testing.
     */
    @Bean
    fun instantSource(): InstantSource = InstantSource.system()
}
