package io.github.readymadeprogrammer.mcremapper.mapping

import kotlin.collections.set

fun TypeSignature.convert(converter: (String) -> String): TypeSignature {
    return when (value[0]) {
        'B', 'C', 'D', 'F', 'I', 'J', 'S', 'Z', 'V' -> this
        '[' -> TypeSignature("[" + TypeSignature(value.substring(1)).convert(converter).value)
        'L' -> TypeSignature("L" + converter(value.substring(1, value.length - 1))+";")
        else -> error("Illegal type signature: $value")
    }
}

fun reverseMapping(original: Set<ClassMapping>): Set<ClassMapping> {
    val classMapping = HashMap<String, String>() // obf -> original
    val converter: (String) -> String = {
        classMapping.getOrDefault(it, it)
    }
    for (clazz in original) {
        classMapping[clazz.type.first.type] = clazz.type.second
    }


    return original.map { orig ->
        val type = ClassSignature(orig.type.second) to orig.type.first.type
        val method = orig.method.map {
            val signature = it.key
            val mapped = it.value
            MethodSignature(
                signature.returnType.convert(converter),
                signature.parameterType.map { param -> param.convert(converter) },
                mapped
            ) to signature.name
        }.toMap()
        val field = orig.field.map {
            val signature = it.key
            val mapped = it.value

            FieldSignature(
                signature.type.convert(converter),
                mapped
            ) to signature.name
        }.toMap()

        ClassMapping(
            type,
            field,
            method
        )
    }.toSet()
}