package io.github.readymadeprogrammer.mcremapper.mapping

import java.net.URL

fun parseMapping(url: URL): MutableSet<ClassMapping> {
    val mappings = HashSet<ClassMapping>()
    val content = url.readText().lines()
    var type: Pair<ClassSignature, String>? = null
    var field: MutableMap<FieldSignature, String>? = null
    var method: MutableMap<MethodSignature, String>? = null
    var line = 1
    while (line < content.size) {
        val current = content[line++]
        try {
            if (current.isBlank())
                continue
            if (current.first().isWhitespace()) {
                if (current.trim().first().isDigit()) { //Method
                    val parsed = parseMethodMapping(current.trim())
                    method!![parsed.first] = parsed.second
                } else { //Field
                    val parsed = parseFieldMapping(current.trim())
                    field!![parsed.first] = parsed.second
                }
            } else {
                if (type != null) {
                    mappings += ClassMapping(
                        type,
                        field!!,
                        method!!
                    )
                }
                type = parseClassMapping(current)
                field = HashMap()
                method = HashMap()
            }
        } catch (e: Exception) {
            throw Exception("Exception on parsing: $current", e)
        }

    }
    return mappings
}