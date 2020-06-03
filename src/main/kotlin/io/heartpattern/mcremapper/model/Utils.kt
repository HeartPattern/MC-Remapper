package io.heartpattern.mcremapper.model

fun TypeSignature.reverse(mappings: Mappings): TypeSignature {
    fun reverseTypeSignature(mappings: Mappings, raw: String): String {
        return when (raw[0]) {
            'B', 'C', 'D', 'F', 'I', 'J', 'S', 'Z', 'V' -> raw
            '[' -> '[' + reverseTypeSignature(mappings, raw.substring(1))
            'L' -> 'L' + mappings.mapClassName(raw.substring(1, raw.length - 1)) + ';'
            else -> error("Illegal type signature: $raw")
        }
    }

    return TypeSignature(reverseTypeSignature(mappings, name))
}