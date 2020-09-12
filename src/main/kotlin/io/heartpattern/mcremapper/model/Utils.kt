package io.heartpattern.mcremapper.model

import org.objectweb.asm.Type

fun <T> Map<T, T>.getOrKey(key: T): T =
    getOrDefault(key, key)

data class FieldRef(
    val owner: String,
    val type: String?,
    val name: String
)

data class MethodRef(
    val owner: String,
    val descriptor: String,
    val name: String
)

data class ParsedMethodDescriptor(
    val returnType: String,
    val parameters: List<String>
) {
    constructor(s: String) : this(Type.getReturnType(s).descriptor, Type.getArgumentTypes(s).map { it.descriptor })

    override fun toString(): String =
        "(${parameters.joinToString(separator = "") { it }})${returnType}"
}

data class MemberMappingValue(
    val mapped: String,
    val inheritable: Boolean = true
)
