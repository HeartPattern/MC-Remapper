package io.heartpattern.mcremapper

import java.io.File
import java.net.URL

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
