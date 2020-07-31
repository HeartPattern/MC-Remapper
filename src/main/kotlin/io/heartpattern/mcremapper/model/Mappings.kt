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
    override fun reverse(mappings: Mappings): ClassMapping =
        ClassMapping(
            ClassSignature(mapped),
            original.name,
            fields.map { it.reverse(mappings) }.toSet(),
            methods.map { it.reverse(mappings) }.toSet()
        )
}

/**
 * Field mapping
 */
data class FieldMapping(
    val original: FieldSignature,
    val mapped: String
) : Mapping {
    override fun reverse(mappings: Mappings): FieldMapping =
        FieldMapping(
            FieldSignature(
                original.type?.reverse(mappings),
                mapped
            ),
            original.name
        )
}

/**
 * Method mapping
 */
data class MethodMapping(
    val original: MethodSignature,
    val mapped: String
) : Mapping {
    override fun reverse(mappings: Mappings): MethodMapping =
        MethodMapping(
            MethodSignature(
                original.returnType.reverse(mappings),
                original.parameters.map { it.reverse(mappings) },
                mapped
            ),
            original.name
        )
}


/**
 * Package mapping
 */
data class PackageMapping(
    val original: String,
    val mapped: String
) : Mapping {
    override fun reverse(mappings: Mappings): PackageMapping =
        PackageMapping(
            mapped,
            original
        )
}

/**
 * Set of mappings
 */
data class Mappings(
    val classMapping: Map<String, ClassMapping>,
    val packageMapping: Map<String, PackageMapping> = mapOf()
) : Mapping {
    fun mapClassName(name: String): String =
        classMapping[name]?.mapped ?: name

    override fun reverse(mappings: Mappings): Mappings =
        Mappings(
            classMapping.asSequence()
                .map { it.value.reverse(this) }
                .map { it.original.name to it }
                .toMap(),
            packageMapping.asSequence()
                .map { it.value.reverse(this) }
                .map { it.original to it }
                .toMap()
        )
}
