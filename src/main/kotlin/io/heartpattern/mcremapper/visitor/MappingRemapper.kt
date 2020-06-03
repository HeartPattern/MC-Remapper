package io.heartpattern.mcremapper.visitor

import io.heartpattern.mcremapper.fromInternal
import io.heartpattern.mcremapper.resolver.SuperTypeResolver
import io.heartpattern.mcremapper.model.Mappings
import io.heartpattern.mcremapper.toInternal
import org.objectweb.asm.commons.Remapper

/**
 * Apply given [mapping]
 */
class MappingRemapper(
    private val mapping: Mappings,
    private val superResolver: SuperTypeResolver
) : Remapper() {
    override fun map(internalName: String): String {
        val externalName = internalName.fromInternal()
        return mapping.classMapping[externalName]?.mapped?.toInternal() ?: internalName
    }

    override fun mapFieldName(owner: String, name: String, descriptor: String): String {
        val externalOwner = owner.fromInternal()
        val externalDescriptor = descriptor.fromInternal()

        for (superName in superResolver.getAllSuperClass(externalOwner)) {
            val superMapping = mapping.classMapping[superName] ?: continue
            for (field in superMapping.fields) {
                if (field.original.type.name == externalDescriptor && field.original.name == name) {
                    return field.mapped
                }
            }
        }

        return name
    }

    override fun mapMethodName(owner: String, name: String, descriptor: String): String {
        val externalOwner = owner.fromInternal()
        val externalDescriptor = descriptor.fromInternal()

        for (superName in superResolver.getAllSuperClass(externalOwner)) {
            val superMapping = mapping.classMapping[superName] ?: continue
            for (method in superMapping.methods) {
                if (method.original.methodDescriptor == externalDescriptor && method.original.name == name) {
                    return method.mapped
                }
            }
        }

        return name
    }
}
