package io.heartpattern.mcremapper

import io.heartpattern.mcremapper.model.LocalVariableFixType
import io.heartpattern.mcremapper.model.Mappings
import io.heartpattern.mcremapper.resolver.SuperTypeResolver
import io.heartpattern.mcremapper.visitor.InnerClassRemapper
import io.heartpattern.mcremapper.visitor.LocalVariableFixVisitor
import io.heartpattern.mcremapper.visitor.MappingRemapper
import io.heartpattern.mcremapper.visitor.ParameterAnnotationFixVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode

/**
 * Utility class for applying mapping
 */
class MappingApplier(
    private val mapping: Mappings,
    private val fixType: LocalVariableFixType,
    private val superResolver: SuperTypeResolver
) {
    /**
     * Apply mapping to given [byteArray] of bytecode
     */
    @Suppress("unused")
    fun applyMapping(byteArray: ByteArray): ByteArray {
        val reader = ClassReader(byteArray)
        val node = applyMapping(reader)
        val writer = ClassWriter(0)
        node.accept(writer)
        return writer.toByteArray()
    }

    /**
     * Apply mapping to given [reader] and return mapped [ClassNode]
     */
    fun applyMapping(reader: ClassReader): ClassNode {
        val node = ClassNode()
        val visitor = InnerClassRemapper(
            LocalVariableFixVisitor(
                ParameterAnnotationFixVisitor(
                    node
                ),
                fixType
            ),
            MappingRemapper(mapping, superResolver)
        )

        reader.accept(visitor, ClassReader.EXPAND_FRAMES)
        return node
    }
}
