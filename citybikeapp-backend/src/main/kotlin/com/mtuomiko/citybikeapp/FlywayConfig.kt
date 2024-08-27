package com.mtuomiko.citybikeapp

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.flywaydb.core.Flyway
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

private val logger = KotlinLogging.logger {}

@Configuration
class FlywayConfig(
    val dataSource: DataSource,
    val applicationContext: ApplicationContext,
) {
    @PostConstruct
    fun migrate() {
        logger.info { "Flyway starting" }

        val flyway =
            Flyway
                .configure()
                .dataSource(dataSource) // No need to configure flyway JDBC URL by using the original DataSource
                .locations("db/migration") // Default migration script path
                .executeInTransaction(true) // Performing migration as a transaction
                .installedBy(applicationContext.id) // Using microservice name as migration executor
                .schemas("citybikeapp")
                .load()

        val migrate = flyway.migrate()
        logger.info { "Flyway executed ${migrate.migrationsExecuted} migrations. success: ${migrate.success}" }
    }
}
