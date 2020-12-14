package org.openownership.rdf

import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.zip.ZipInputStream

/**
 * @author Cosmin Marginean
 */

const val BUFFER_1MB = 1024 * 1024

internal fun unzip(zip: File, destDir: File, charset: Charset = StandardCharsets.UTF_8) {
    val log = LoggerFactory.getLogger("org.openownership.rdf.unzip")
    zip.inputStream().use { fileInputStream ->
        val zis = ZipInputStream(fileInputStream, charset)
        val outputFiles: MutableList<File> = ArrayList()
        var zipEntry = zis.nextEntry
        while (zipEntry != null) {
            val name = zipEntry.name
            val file = File(destDir, name)
            log.info("Unzipping entry $name to ${file.absolutePath}")
            if (name.endsWith("/")) {
                check(!(!file.exists() && !file.mkdirs())) { "Could not create ${file.absolutePath}" }
            } else {
                val parentFile = file.parentFile
                check(!(!parentFile.exists() && !parentFile.mkdirs())) { "Could not create parent ${parentFile.absolutePath}" }
                FileOutputStream(file).use { fos -> IOUtils.copy(zis, fos) }
                outputFiles.add(file)
            }
            zipEntry = zis.nextEntry
        }
    }
}

fun resourceAsInput(classpathLocation: String): InputStream {
    val classLoader = Thread.currentThread().contextClassLoader
    return classLoader.getResourceAsStream(classpathLocation)
}
