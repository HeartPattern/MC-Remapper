package io.heartpattern.mcremapper

import java.io.File
import java.io.InputStream
import java.net.URL
import java.util.zip.ZipFile

/**
 * Convert a binary name of a class or an interface in its internal form to the normal form. Simply replace '/' to '.'
 * @see "Java Virtual Machine Specification §4.2.1"
 */
fun String.fromInternal(): String {
    return replace('/', '.')
}

/**
 * Convert a binary name of a class or an interface in its normal form to the internal form. Simply replace '.' to '/'
 * @see "Java Virtual Machine Specification §4.2.1"
 */
fun String.toInternal(): String {
    return replace('.', '/')
}

fun URL.download(prefix: String): File {
    val tempFile = File.createTempFile(prefix, null)
    tempFile.outputStream().use { output ->
        this.openStream().use { input ->
            input.copyTo(output)
        }
    }

    tempFile.deleteOnExit()
    return tempFile
}

/**
 * Get jar containing minecraft source file.
 * Since 21w39a, minecraft server contains nested jar file.
 */
fun File.getMinecraftSourceZipInputStream(): InputStream {
    val zipFile = ZipFile(this)

    val versionEntry = zipFile.getEntry("META-INF/versions.list")
        ?: return inputStream() // If versions.list does not exist, it is plain-old-minecraft-server

    val nestedJarPath =
        "META-INF/versions/" + zipFile.getInputStream(versionEntry)
            .bufferedReader().use {
            it.readText()
        }.substringAfterLast("\t")

    return zipFile.getInputStream(zipFile.getEntry(nestedJarPath))
}
