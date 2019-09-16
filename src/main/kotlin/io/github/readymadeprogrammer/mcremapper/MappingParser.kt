package io.github.readymadeprogrammer.mcremapper

import java.net.URL

fun parseMapping(url: URL): Set<ClassMapping> {
    val mappings = HashSet<ClassMapping>()
    var type: Mapping<ClassInfo>? = null
    var field: MutableSet<Mapping<FieldInfo>>? = null
    var method: MutableSet<Mapping<MethodInfo>>? = null

    println("Read mapping file from url")
    val content = url.readText().lines()

    println("Parse mapping file")
    var line = 0
    while (line < content.size) {
        val current = content[line++]

        if (current.isBlank() || current.trim().startsWith('#'))
            continue

        try {
            if (current[0].isWhitespace()) { // Field or Method
                if (current.contains('(')) { //Method
                    val parsed = parseMethodMapping(current.trim())
                    method!! += parsed
                } else { //Field
                    val parsed = parseFieldMapping(current.trim())
                    field!! += parsed
                }
            } else { //Class
                if (type != null) {
                    mappings += ClassMapping(
                        type,
                        field!!,
                        method!!
                    )
                }
                type = parseClassMapping(current)
                field = HashSet()
                method = HashSet()
            }
        } catch (e: Exception) {
            throw Exception("Exception on parsing: $current", e)
        }
    }
    println("Total ${mappings.size} class mapping found")
    return mappings
}