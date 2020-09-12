package io.heartpattern.mcremapper.parser.proguard

import io.heartpattern.mcremapper.toInternal

fun parseProguardType(raw: String): String {
    return when (raw) {
        "void" -> "V"
        "byte" -> "B"
        "char" -> "C"
        "double" -> "D"
        "float" -> "F"
        "int" -> "I"
        "long" -> "J"
        "short" -> "S"
        "boolean" -> "Z"
        "" -> throw IllegalArgumentException("Empty type signature")
        else -> {
            if (raw.endsWith("[]"))
                '[' + parseProguardType(raw.substring(0, raw.length - 2))
            else
                "L${raw.toInternal()};"
        }
    }
}
