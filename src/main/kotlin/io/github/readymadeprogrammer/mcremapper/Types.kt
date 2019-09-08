package io.github.readymadeprogrammer.mcremapper

inline class TypeDescriptor(val value: String)

data class ClassInfo(val value: String)
data class FieldInfo(val type: TypeDescriptor, val name: String)
data class MethodInfo(val returnType: TypeDescriptor, val parameters: List<TypeDescriptor>, val name: String)

data class Mapping<T>(
    val from: T,
    val mapped: String
)

data class ClassMapping(
    val classMapping: Mapping<ClassInfo>,
    val fieldMappings: Set<Mapping<FieldInfo>>,
    val methodMappings: Set<Mapping<MethodInfo>>
)

fun MethodInfo.toMethodDescriptor(): String{
    return "(${parameters.joinToString(separator = ""){it.value}})${returnType.value}"
}