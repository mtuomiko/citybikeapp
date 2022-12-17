package com.mtuomiko.citybikeapp

import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.context.event.StartupEvent
import jakarta.inject.Singleton
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

@Singleton
class DataLoader : ApplicationEventListener<StartupEvent> {
    override fun onApplicationEvent(event: StartupEvent?) {
        logger.info { "Data loading here" }
    }
}
