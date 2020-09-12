package io.heartpattern.mcremapper.visitor

import io.heartpattern.mcremapper.model.FieldRef
import io.heartpattern.mcremapper.model.Mapping
import io.heartpattern.mcremapper.model.MethodRef
import io.heartpattern.mcremapper.model.getOrKey
import io.heartpattern.mcremapper.preprocess.SuperTypeResolver
import org.objectweb.asm.commons.Remapper

/**
 * Apply given [mapping]
 */
class MappingRemapper(
    private val mapping: Mapping,
    private val superResolver: SuperTypeResolver
) : Remapper() {
    override fun map(name: String): String {
        val mappedName = (name.length downTo 0).asSequence()
            .filter { index -> index == name.length || name[index] == '$'}
            .firstOrNull { suffixStart ->
                val prefix = name.substring(0, suffixStart)
                prefix in mapping.classMapping
            }?.let { suffixStart ->
                val prefix = name.substring(0, suffixStart)
                val suffix = name.substring(suffixStart)
                mapping.classMapping.getOrKey(prefix) + suffix
            } ?: name

        val packageName = if ('/' in mappedName) mappedName.substring(0, mappedName.lastIndexOf('/')) else ""
        return (if (packageName in mapping.packageMapping) {
            mapping.packageMapping.getValue(packageName).let { mapped -> if (mapped.isNotEmpty()) "$mapped/" else ""} + mappedName.substring(if (packageName.isNotEmpty()) packageName.length + 1 else 0)
        } else {
            mappedName
        })
    }

    override fun mapFieldName(owner: String, name: String, descriptor: String): String {
        for (superName in superResolver.iterateSuperNames(owner)) {
            val ref = FieldRef(superName, descriptor, name)
            val mappingValue = mapping.fieldMapping[ref]
            if (mappingValue != null && (superName == owner || mappingValue.inheritable)) {
                return mappingValue.mapped
            }
            val refWithoutType = FieldRef(superName, null, name)
            val mappedWithoutType = mapping.fieldMapping[refWithoutType]
            if (mappedWithoutType != null && (superName == owner || mappedWithoutType.inheritable)) {
                return mappedWithoutType.mapped
            }
        }

        return name
    }

    override fun mapMethodName(owner: String, name: String, descriptor: String): String {
        for (superName in superResolver.iterateSuperNames(owner)) {
            val ref = MethodRef(superName, descriptor, name)
            val mappingValue = mapping.methodMapping[ref]
            if (mappingValue != null && (superName == owner || mappingValue.inheritable)) {
                return mappingValue.mapped
            }
        }

        return name
    }
}
