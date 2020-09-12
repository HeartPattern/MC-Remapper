package io.heartpattern.mcremapper.model

/**
 * Set of mappings
 */
data class Mapping(
    val classMapping: Map<String, String>,
    val fieldMapping: Map<FieldRef, MemberMappingValue>,
    val methodMapping: Map<MethodRef, MemberMappingValue>,
    val packageMapping: Map<String, String>
) {
    fun reversed(): Mapping =
        Mapping(
            classMapping.map { (original, mapped) ->
                mapped to original
            }.toMap(),
            fieldMapping.map { (ref, mappingValue) ->
                FieldRef(
                    classMapping.getOrKey(ref.owner),
                    ref.type?.let { mapType(it) }, mappingValue.mapped
                ) to MemberMappingValue(ref.name, mappingValue.inheritable)
            }.toMap(),
            methodMapping.map { (ref, mappingValue) ->
                val descriptor = ParsedMethodDescriptor(ref.descriptor)
                MethodRef(
                    classMapping.getOrKey(ref.owner),
                    ParsedMethodDescriptor(mapType(descriptor.returnType), descriptor.parameters.map { mapType(it) }).toString(),
                    mappingValue.mapped
                ) to MemberMappingValue(ref.name, mappingValue.inheritable)
            }.toMap(),
            packageMapping.map { (key, value) ->
                value to key
            }.toMap()
        )

    fun mapType(type: String): String =
        when (type.getOrNull(0)) {
            'B', 'C', 'D', 'F', 'I', 'J', 'S', 'Z', 'V' -> type
            '[' -> '[' + mapType(type.substring(1))
            'L' -> 'L' + classMapping.getOrKey(type.substring(1, type.length - 1)) + ';'
            else -> error("Illegal type signature: $type")
        }
}
