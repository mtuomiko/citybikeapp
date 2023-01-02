package com.mtuomiko.citybikeapp.common

import io.micronaut.configuration.jdbi.JdbiCustomizer
import io.micronaut.context.annotation.Requires
import jakarta.inject.Named
import jakarta.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.statement.Slf4JSqlLogger

@Singleton
@Requires(notEnv = ["prod", "test"])
@Named("default")
class JdbiLoggingCustomizer : JdbiCustomizer {
    override fun customize(jdbi: Jdbi) {
        jdbi.setSqlLogger(Slf4JSqlLogger())
    }
}
