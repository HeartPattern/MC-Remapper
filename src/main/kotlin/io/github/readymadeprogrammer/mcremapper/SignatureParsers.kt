package io.github.readymadeprogrammer.mcremapper

import java.util.regex.Pattern

private val fieldRegex = Pattern.compile("(\\S+) (\\S+) -> (\\S+)")
private val lineNumberMethodRegex = Pattern.compile("(\\d+):(\\d+):(\\S+) (\\S+)\\((\\S*)\\) -> (\\S+)")
private val methodRegex = Pattern.compile("(\\S+) (\\S+)\\((\\S*)\\) -> (\\S+)")
private val methodDescriptorRegex = Pattern.compile("\\((\\S*)\\)(\\S+)")
private val classRegex = Pattern.compile("(\\S+) -> (\\S+):")

fun parseClassMapping(raw: String): Mapping<ClassInfo> {
    val matcher = classRegex.matcher(raw)
    matcher.find()
    return Mapping(ClassInfo(matcher.group(1)), matcher.group(2))
}

fun parseFieldMapping(raw: String): Mapping<FieldInfo> {
    val matcher = fieldRegex.matcher(raw)
    matcher.find()
    return Mapping(
        FieldInfo(
            parseType(matcher.group(1)),
            matcher.group(2)
        ),
        matcher.group(3)
    )
}

fun parseType(raw: String): TypeDescriptor {
    return TypeDescriptor(when (raw) {
        "void" -> "V"
        "byte" -> "B"
        "char" -> "C"
        "double" -> "D"
        "float" -> "F"
        "int" -> "I"
        "long" -> "J"
        "short" -> "S"
        "boolean" -> "Z"
        else -> if (raw.endsWith("[]")) "[${parseType(raw.substring(0, raw.length - 2)).value}" else "L$raw;"
    })
}


fun parseMethodMapping(raw: String): Mapping<MethodInfo> {
    if (raw[0].isDigit()) {
        val matcher = lineNumberMethodRegex.matcher(raw)
        matcher.find()
        val arguments = if (matcher.group(5).isEmpty()) emptyList()
        else matcher.group(5).split(',').map { parseType(it.trim()) }
        return Mapping(
            MethodInfo(
                parseType(matcher.group(3)),
                arguments,
                matcher.group(4)),
            matcher.group(6))
    } else {
        val matcher = methodRegex.matcher(raw)
        matcher.find()
        val arguments = if (matcher.group(3).isEmpty()) emptyList()
        else matcher.group(3).split(',').map { parseType(it.trim()) }
        return Mapping(
            MethodInfo(
                parseType(matcher.group(1)),
                arguments,
                matcher.group(2)
            ),
            matcher.group(4)
        )
    }
}

private fun parseNext(iterator: Iterator<Char>): String {
    return when (val char = iterator.next()) {
        'B', 'C', 'D', 'F', 'I', 'J', 'S', 'Z' -> char.toString()
        '[' -> char + parseNext(iterator)
        'L' -> {
            val builder = StringBuilder("L")
            var content = iterator.next()
            while (content != ';') {
                builder.append(content)
                content = iterator.next()
            }
            builder.append(content)
            builder.toString()
        }
        else -> error("")
    }
}

fun parseParameter(raw: String): List<TypeDescriptor> {
    val iterator = raw.iterator()
    val params = ArrayList<TypeDescriptor>()
    while (iterator.hasNext()) {
        params += TypeDescriptor(parseNext(iterator))
    }

    return params
}

fun parseMethodDescriptor(descriptor: String, name: String): MethodInfo {
    try {
        val matcher = methodDescriptorRegex.matcher(descriptor)
        matcher.find()
        val parameters = parseParameter(matcher.group(1))
        val returnType = matcher.group(2)

        return MethodInfo(TypeDescriptor(returnType), parameters, name)
    } catch (e: Exception) {
        throw Exception("Error occurred while parsing method descriptor: $descriptor, $name", e)
    }
}