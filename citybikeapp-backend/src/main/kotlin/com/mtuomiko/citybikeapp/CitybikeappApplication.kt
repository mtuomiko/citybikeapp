package com.mtuomiko.citybikeapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [FlywayAutoConfiguration::class])
class CitybikeappApplication

fun main(args: Array<String>) {
    @Suppress("SpreadOperator") // run once, not a performance issue
    runApplication<CitybikeappApplication>(*args)
}
