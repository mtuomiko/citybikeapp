package com.mtuomiko.citybikeapp

import jakarta.inject.Singleton
import mu.KotlinLogging
import java.io.InputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.inputStream

private val logger = KotlinLogging.logger {}

private const val DOWNLOAD_DIR = "temp"

@Singleton
class DownloadingFileProvider : FileProvider {
    private val directoryPath = Path.of(DOWNLOAD_DIR)
    private val filePathList = mutableListOf<Path>()

    init {
        directoryPath.createDirectories()
    }

    override fun getLocalInputStream(url: String): InputStream {
        logger.info { "Handling URL $url" }

        val path = directoryPath.resolve(getFilename(url))
        if (!Files.exists(path)) {
            downloadToPath(url, path)
        } else {
            logger.info { "File $path already exists on filesystem" }
        }

        filePathList.add(path)
        logger.info { "Providing $path" }
        return path.inputStream()
    }

    override fun deleteFiles() {
        filePathList.forEach {
            logger.debug { "Deleting $it" }
            Files.deleteIfExists(it)
        }
        filePathList.clear()
        logger.info { "All temp files deleted" }
    }

    private fun downloadToPath(url: String, path: Path) {
        logger.info { "Downloading $url to $path" }
        URL(url).openStream().use {
            Files.copy(it, path)
        }
    }

    private fun getFilename(url: String) = url.split('/').last()
}
