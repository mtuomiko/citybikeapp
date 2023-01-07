package com.mtuomiko.citybikeapp.dao

import io.micronaut.configuration.jdbi.JdbiCustomizer
import io.micronaut.context.annotation.Requires
import jakarta.inject.Named
import jakarta.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.statement.Slf4JSqlLogger

@Singleton
@Requires(env = ["dev"])
@Named("default")
class JdbiLoggingCustomizer : JdbiCustomizer {
    override fun customize(jdbi: Jdbi) {
        jdbi.setSqlLogger(Slf4JSqlLogger())
    }
}
