package io.github.readymadeprogrammer.mcremapper

import org.objectweb.asm.commons.Remapper

class SimpleRemapper(
    val mapping: Set<ClassMapping>,
    val hierarchy: TypeHierarchyResolveVisitor
) : Remapper() {
    override fun map(typeName: String): String {
        val nTypeName = typeName.replace('/', '.')
        return mapping.find { it.classMapping.from.value == nTypeName }?.classMapping?.mapped?.replace('.', '/')
            ?: typeName
    }

    override fun mapFieldName(owner: String, name: String, desc: String): String {
        val nOwner = owner.replace('/', '.')
        val nDesc = desc.replace('/', '.')
        val clazz = mapping.find { it.classMapping.from.value == nOwner } ?: return name
        val field = run {
            for (c in hierarchy.getAllSuperClass(clazz.classMapping.from)) {
                val cMapping = mapping.find { it.classMapping.from == c } ?: continue
                val found = findField(cMapping, name, nDesc)
                if (found != null) return@run found
            }
            return@run null
        }
        return field ?: name
    }

    override fun mapMethodName(owner: String, name: String, desc: String): String {
        val nOwner = owner.replace('/', '.')
        val nDesc = desc.replace('/', '.')
        val clazz = mapping.find { it.classMapping.from.value == nOwner } ?: return name
        val method = run {
            for (c in hierarchy.getAllSuperClass(clazz.classMapping.from)) {
                val cMapping = mapping.find { it.classMapping.from == c } ?: continue
                val found = findMethod(cMapping, nDesc, name)
                if (found != null) return@run found
            }
            return@run null
        }
        return method ?: name
    }

    private fun findMethod(mapping: ClassMapping, desc: String, name: String): String? {
        for (method in mapping.methodMappings) {
            if (method.from.toMethodDescriptor() == desc && method.from.name == name) {
                return method.mapped
            }
        }
        return null
    }

    private fun findField(mapping: ClassMapping, name: String, desc: String): String? {
        for (field in mapping.fieldMappings) {
            if (field.from.type.value == desc && field.from.name == name) {
                return field.mapped
            }
        }
        return null
    }
}