package com.mtuomiko.citybikeapp

import io.micronaut.configuration.picocli.PicocliRunner
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
    /**
     * Main entrypoint. Decides between running the data loader or the actual server application.
     */
    @Suppress("SpreadOperator")
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.isNotEmpty() && args[0] == "dataloader") {
            val emptyArgs = arrayOf<String>() // picocli loader not configured for any actual arguments
            PicocliRunner.run(DataLoader::class.java, *emptyArgs)
        } else {
            run(*args)
        }
    }
}
