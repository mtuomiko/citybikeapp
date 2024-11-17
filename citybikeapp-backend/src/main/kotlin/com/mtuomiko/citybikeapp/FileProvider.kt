package com.mtuomiko.citybikeapp

import java.nio.file.Path

interface FileProvider {
    fun getByURI(uri: String): Path

    fun deleteFiles()

    fun getFilenameFromURL(url: String) = url.split('/').last()
}
