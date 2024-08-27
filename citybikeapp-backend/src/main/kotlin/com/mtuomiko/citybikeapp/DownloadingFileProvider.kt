package com.mtuomiko.citybikeapp

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.io.IOException
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories

private val logger = KotlinLogging.logger {}

private const val DOWNLOAD_DIR = "temp"

@Component
@Profile(value = ["!test"])
class DownloadingFileProvider : FileProvider {
    private val localPath = Path.of(DOWNLOAD_DIR)
    private val filePathList = mutableListOf<Path>()

    init {
        localPath.createDirectories()
    }

    override fun getByURI(uri: String): Path {
        logger.info { "Handling URL $uri" }

        val path = localPath.resolve(getFilenameFromURL(uri))
        if (!Files.exists(path)) {
            downloadToPath(uri, path)
        } else {
            logger.info { "File $path already exists on filesystem" }
        }

        filePathList.add(path)
        logger.info { "Providing $path" }
        return path
    }

    override fun deleteFiles() {
        filePathList.forEach {
            logger.debug { "Deleting $it" }
            try {
                Files.deleteIfExists(it)
            } catch (e: IOException) {
                logger.error { e }
            }
        }
        filePathList.clear()
        logger.info { "All temp files deleted" }
    }

    private fun downloadToPath(
        url: String,
        path: Path,
    ) {
        logger.info { "Downloading $url to $path" }
        URI(url).toURL().openStream().use {
            Files.copy(it, path)
        }
    }
}
