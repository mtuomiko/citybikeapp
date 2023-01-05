package com.mtuomiko.citybikeapp

import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.context.ApplicationContext
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.inject.Inject
import org.junit.jupiter.api.Test

@MicronautTest(environments = ["test", "prod"])
class DataLoaderProductionEnvironmentTest {

    @Inject
    private lateinit var applicationContext: ApplicationContext

    @Inject
    private lateinit var fileProvider: FileProvider

    @Test
    fun `When running in production environment, data loader will call for file deletion`() {
        PicocliRunner.run(DataLoader::class.java, applicationContext)

        verify(exactly = 1) { fileProvider.deleteFiles() }
    }

    @MockBean(DownloadingFileProvider::class)
    private fun fileProvider(): FileProvider {
        val mockProvider = mockk<FileProvider>(relaxUnitFun = true)
        every { mockProvider.getLocalInputStream(any()) } returns "".byteInputStream()
        return mockProvider
    }
}
