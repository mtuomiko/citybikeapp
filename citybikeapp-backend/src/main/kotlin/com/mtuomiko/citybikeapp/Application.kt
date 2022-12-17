package com.mtuomiko.citybikeapp

import io.micronaut.runtime.Micronaut.run
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info

@OpenAPIDefinition(
    info = Info(
        title = "citybikeapp",
        version = "0.0"
    )
)
object Application {
    @Suppress("SpreadOperator")
    @JvmStatic
    fun main(args: Array<String>) {
        run(*args)
    }
}
