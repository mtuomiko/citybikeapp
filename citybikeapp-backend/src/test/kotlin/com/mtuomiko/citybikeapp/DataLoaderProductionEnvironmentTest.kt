package com.mtuomiko.citybikeapp

import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.context.ApplicationContext
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@MicronautTest(environments = ["test", "prod"])
class DataLoaderProductionEnvironmentTest {

    @Inject
    private lateinit var applicationContext: ApplicationContext

    @Inject
    private lateinit var fileProvider: FileProvider

    @Test
    fun `When running in production environment, data loader will call for file deletion`() {
        PicocliRunner.run(DataLoader::class.java, applicationContext)

        verify(fileProvider).deleteFiles()
    }

    @MockBean(DownloadingFileProvider::class)
    private fun fileProvider(): FileProvider {
        val mock: FileProvider = mock()
        whenever(mock.getLocalInputStream(any())).thenReturn("".byteInputStream())
        return mock
    }
}
