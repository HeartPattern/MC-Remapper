package io.github.readymadeprogrammer.mcremapper

import io.github.readymadeprogrammer.mcremapper.mapping.ClassMapping
import io.github.readymadeprogrammer.mcremapper.mapping.FieldSignature
import io.github.readymadeprogrammer.mcremapper.mapping.TypeSignature
import io.github.readymadeprogrammer.mcremapper.mapping.parseMethodDescriptor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.Remapper
import java.util.*

class SimpleRemapper(val mapping: Map<String, ClassMapping>) : Remapper() {
    val hierarchy = HashMap<String, ClassHierarchy>()
    val hierarchyReader = SimpleHierarchyReader()
    inner class SimpleHierarchyReader : ClassVisitor(Opcodes.ASM5) {
        override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String?, interfaces: Array<out String>?) {
            hierarchy[name] = ClassHierarchy(superName, interfaces)
        }
    }

    override fun map(typeName: String): String? = (mapping[typeName]?.type?.second?.replace('.','/') ?: typeName)

    override fun mapFieldName(owner: String, name: String, desc: String): String? {
        mapping[owner]
            ?.let { it.field[FieldSignature(TypeSignature(desc), name)] }
            ?.let { return it }
        hierarchy[owner]?.superName?.let { return mapFieldName(it, name, desc) }
        return name
    }

    override fun mapMethodName(owner: String, name: String, desc: String) =
        mapMethodNameInternal(owner, name, desc) ?: name

    private fun mapMethodNameInternal(owner: String, name: String, desc: String): String? {
        mapping[owner]?.let { it.method[parseMethodDescriptor(desc, name)] }?.let { return it }
        val h = hierarchy[owner] ?: return null
        if (h.superName != null) mapMethodNameInternal(h.superName, name, desc)?.let { return it }
        if (h.interfaces != null) for (interfaceName in h.interfaces) {
            mapMethodNameInternal(interfaceName, name, desc)?.let { return it }
        }
        return null
    }
}

class ClassHierarchy(val superName: String?, val interfaces: Array<out String>?)