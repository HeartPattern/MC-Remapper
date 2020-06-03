package io.heartpattern.mcremapper

/**
 * Convert internal class name to familiar class name, which simply replace '/' to ','
 */
fun String.fromInternal(): String {
    return replace('/', '.')
}

/**
 * Convert familiar class name to internal class name, which simply replace '.' to ','
 */
fun String.toInternal(): String {
    return replace('.', '/')
}