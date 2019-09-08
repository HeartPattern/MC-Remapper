package io.github.readymadeprogrammer.mcremapper

typealias Converter = (String) -> String

fun TypeDescriptor.reverse(converter: Converter): TypeDescriptor {
    fun reverse(raw: String): String {
        return when (raw[0]) {
            'B', 'C', 'D', 'F', 'I', 'J', 'S', 'Z', 'V' -> raw
            '[' -> '[' + reverse(raw.substring(1))
            'L' -> 'L' + converter(raw.substring(1, raw.length - 1)) + ';'
            else -> error("Illegal type signature: $value")
        }
    }
    return TypeDescriptor(reverse(value))
}

fun Mapping<ClassInfo>.reverse(): Mapping<ClassInfo> {
    return Mapping(
        ClassInfo(mapped),
        from.value
    )
}

fun Mapping<FieldInfo>.reverseField(converter: Converter): Mapping<FieldInfo> {
    return Mapping(
        FieldInfo(
            from.type.reverse(converter),
            mapped
        ),
        from.name
    )
}

fun Mapping<MethodInfo>.reverseMethod(converter: Converter): Mapping<MethodInfo> {
    return Mapping(
        MethodInfo(
            from.returnType.reverse(converter),
            from.parameters.map { it.reverse(converter) },
            mapped
        ),
        from.name
    )
}

fun reverseMapping(original: Set<ClassMapping>): Set<ClassMapping> {
    val classMapping = HashMap<String, String>()
    for (mapping in original) {
        classMapping[mapping.classMapping.from.value] = mapping.classMapping.mapped
    }

    fun converter(raw: String): String = classMapping.getOrDefault(raw, raw)

    return original.map { orig ->
        ClassMapping(
            orig.classMapping.reverse(),
            orig.fieldMappings.map { it.reverseField(::converter) }.toSet(),
            orig.methodMappings.map { it.reverseMethod(::converter) }.toSet()
        )
    }.toSet()
}