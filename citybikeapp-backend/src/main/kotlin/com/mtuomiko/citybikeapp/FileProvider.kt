package com.mtuomiko.citybikeapp

import java.io.InputStream

interface FileProvider {
    fun getLocalInputStream(url: String): InputStream
    fun deleteFiles()
}
