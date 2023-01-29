package com.mtuomiko.citybikeapp

import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton
import mu.KotlinLogging
import java.io.InputStream

private val logger = KotlinLogging.logger {}

@Singleton
@Requires(env = ["e2e"])
class ClassPathFileProvider : FileProvider {
    override fun getLocalInputStream(url: String): InputStream {
        logger.info { "Loading $url from class path" }
        return ClassLoader.getSystemResource(url).openStream()
    }

    override fun deleteFiles() {
        // NOOP
    }
}
