package com.mtuomiko.citybikeapp

import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@MicronautTest
class CityBikeAppTest {

    @Inject
    lateinit var application: EmbeddedApplication<*>

    @Test
    fun `Application can be run`() {
        Assertions.assertTrue(application.isRunning)
    }
}
