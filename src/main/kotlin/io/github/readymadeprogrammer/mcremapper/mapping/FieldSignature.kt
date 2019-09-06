package io.github.readymadeprogrammer.mcremapper.mapping

import java.util.regex.Pattern

data class FieldSignature(
    val type: TypeSignature,
    val name: String
)

private val fieldRegex = Pattern.compile("(\\S+) (\\S+) -> (\\S+)")

fun parseFieldMapping(raw: String): Pair<FieldSignature, String> {
    val matcher = fieldRegex.matcher(raw)
    matcher.find()
    return FieldSignature(parseType(matcher.group(1)), matcher.group(2)) to matcher.group(3)
}