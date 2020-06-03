package io.heartpattern.mcremapper.model

/**
 * Represent obfuscate mapping
 */
interface Mapping {
    /**
     * Reverse mapping direction with original mapping [mappings]
     */
    fun reverse(mappings: Mappings): Mapping
}

/**
 * Class mapping
 */
data class ClassMapping(
    val original: ClassSignature,
    val mapped: String,
    val fields: Set<FieldMapping>,
    val methods: Set<MethodMapping>
) : Mapping {
    override fun reverse(mappings: Mappings): ClassMapping {
        return ClassMapping(
            ClassSignature(mapped),
            original.name,
            fields.map { it.reverse(mappings) }.toSet(),
            methods.map { it.reverse(mappings) }.toSet()
        )
    }
}

/**
 * Field mapping
 */
data class FieldMapping(
    val original: FieldSignature,
    val mapped: String
) : Mapping {
    override fun reverse(mappings: Mappings): FieldMapping {
        return FieldMapping(
            FieldSignature(
                original.type.reverse(mappings),
                mapped
            ),
            original.name
        )
    }
}

/**
 * Method mapping
 */
data class MethodMapping(
    val original: MethodSignature,
    val mapped: String
) : Mapping {
    override fun reverse(mappings: Mappings): MethodMapping {
        return MethodMapping(
            MethodSignature(
                original.returnType.reverse(mappings),
                original.parameters.map { it.reverse(mappings) },
                mapped
            ),
            original.name
        )
    }
}

/**
 * Set of mappings
 */
data class Mappings(
    val classMapping: Map<String, ClassMapping>
) : Mapping {
    fun mapClassName(name: String): String {
        return classMapping[name]?.mapped ?: name
    }

    override fun reverse(mappings: Mappings): Mappings{
        return Mappings(
            classMapping.asSequence()
                .map { it.value.reverse(this) }
                .map { it.original.name to it }
                .toMap()
        )
    }
}