package io.github.readymadeprogrammer.mcremapper.mapping

import java.util.regex.Pattern

data class ClassSignature(
    val type: String
)

private val classRegex = Pattern.compile("(\\S+) -> (\\S+):")

fun parseClassMapping(raw: String): Pair<ClassSignature, String> {
    val matcher = classRegex.matcher(raw)
    matcher.find()
    return ClassSignature(matcher.group(1)) to matcher.group(2)
}