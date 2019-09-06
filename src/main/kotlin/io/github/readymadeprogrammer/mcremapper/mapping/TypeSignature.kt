package io.github.readymadeprogrammer.mcremapper.mapping

inline class TypeSignature(
    val value: String
) {
    constructor(clazz: ClassSignature) : this("L${clazz.type.replace(".", "/")};")
}

fun parseType(raw: String): TypeSignature {
    return TypeSignature(when (raw) {
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