package io.heartpattern.mcremapper.model

/**
 * Signature of some element
 */
interface Signature {
    val name: String
}

/**
 * Signature of type
 */
data class TypeSignature(
    override val name: String
) : Signature

/**
 * Signature of class
 */
data class ClassSignature(
    override val name: String
) : Signature

/**
 * Signature of field
 */
data class FieldSignature(
    val type: TypeSignature?,
    override val name: String
) : Signature

/**
 * Signature of method
 */
data class MethodSignature(
    val returnType: TypeSignature,
    val parameters: List<TypeSignature>,
    override val name: String
) : Signature {
    val methodDescriptor: String
        get() = "(${parameters.joinToString(separator = "") { it.name }})${returnType.name}"
}
