package io.heartpattern.mcremapper.visitor

import io.heartpattern.mcremapper.JavaTokens
import io.heartpattern.mcremapper.fromInternal
import io.heartpattern.mcremapper.model.Mappings
import io.heartpattern.mcremapper.resolver.SuperTypeResolver
import io.heartpattern.mcremapper.toInternal
import org.objectweb.asm.commons.Remapper
import java.lang.reflect.Modifier

/**
 * Apply given [mapping]
 */
class MappingRemapper(
    private val mapping: Mappings,
    private val superResolver: SuperTypeResolver,
    private val autoLogger: Boolean = false,
    private val autoToken: Boolean = false
) : Remapper() {
    override fun map(nameInternal: String): String {
        val name = nameInternal.fromInternal()
        val mappedName = (name.length downTo 0).asSequence()
            .filter { index -> index == name.length || name[index] == '$'}
            .firstOrNull { suffixStart ->
                val prefix = name.substring(0, suffixStart)
                prefix in mapping.classMapping
            }?.let { suffixStart ->
                val prefix = name.substring(0, suffixStart)
                val suffix = name.substring(suffixStart)
                mapping.classMapping.getValue(prefix).mapped + suffix
            } ?: name

        val packageName = if ('.' in mappedName) mappedName.substring(0, mappedName.lastIndexOf('.')) else ""
        return (if (packageName in mapping.packageMapping) {
            mapping.packageMapping.getValue(packageName).mapped.let { mapped -> if (mapped.isNotEmpty()) "$mapped." else ""} + mappedName.substring(if (packageName.isNotEmpty()) packageName.length + 1 else 0)
        } else {
            mappedName
        }).toInternal()
    }

    fun mapFieldName(ownerInternal: String, name: String, descriptorInternal: String, access: Int): String {
        val owner = ownerInternal.fromInternal()
        val descriptor = descriptorInternal.fromInternal()

        for (superName in superResolver.getAllSuperClass(owner)) {
            val superMapping = mapping.classMapping[superName] ?: continue
            for (field in superMapping.fields) {
                if ((field.original.type == null || field.original.type.name == descriptor) && field.original.name == name) {
                    return field.mapped
                }
            }
        }

        if (autoLogger && Modifier.isStatic(access) && Modifier.isFinal(access) && descriptor == "Lorg.apache.logging.log4j.Logger;") {
            return "LOGGER"
        }
        if (autoToken) {
            JavaTokens.appendIfToken(name)?.let { return it }
        }
        return name
    }

    override fun mapMethodName(ownerInternal: String, name: String, descriptorInternal: String): String {
        val owner = ownerInternal.fromInternal()
        val descriptor = descriptorInternal.fromInternal()

        for (superName in superResolver.getAllSuperClass(owner)) {
            val superMapping = mapping.classMapping[superName] ?: continue
            for (method in superMapping.methods) {
                if (method.original.methodDescriptor == descriptor && method.original.name == name) {
                    return method.mapped
                }
            }
        }

        if (autoToken) {
            JavaTokens.appendIfToken(name)?.let { return it }
        }
        return name
    }
}
