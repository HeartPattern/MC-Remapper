package io.heartpattern.mcremapper.visitor

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.commons.ClassRemapper

class EnhancedClassRemapper(classVisitor: ClassVisitor, val mappingRemapper: MappingRemapper) : ClassRemapper(classVisitor, mappingRemapper) {
    override fun visitField(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        value: Any?
    ): FieldVisitor? =
        cv?.visitField(
            access,
            mappingRemapper.mapFieldName(className, name, descriptor, access),
            mappingRemapper.mapDesc(descriptor),
            mappingRemapper.mapSignature(signature, true),
            if (value == null) null else mappingRemapper.mapValue(value)
        )?.let { createFieldRemapper(it) }
}
