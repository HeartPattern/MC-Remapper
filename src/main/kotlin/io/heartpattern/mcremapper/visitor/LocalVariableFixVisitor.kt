package io.heartpattern.mcremapper.visitor

import io.heartpattern.mcremapper.model.LocalVariableFixType
import io.heartpattern.mcremapper.model.LocalVariableFixType.*
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * Fix local variable name is always \u2603(☃)
 */
class LocalVariableFixVisitor(
    cv: ClassVisitor,
    val type: LocalVariableFixType
) : ClassVisitor(Opcodes.ASM9, cv) {
    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)

        return object : MethodVisitor(Opcodes.ASM9, methodVisitor) {
            override fun visitLocalVariable(
                name: String?,
                descriptor: String?,
                signature: String?,
                start: Label?,
                end: Label?,
                index: Int
            ) {
                if (name == null || name != "\u2603") {
                    super.visitLocalVariable(name, descriptor, signature, start, end, index)
                    return
                }

                when (type) {
                    RENAME -> super.visitLocalVariable("debug$index", descriptor, signature, start, end, index)
                    NO -> super.visitLocalVariable(name, descriptor, signature, start, end, index)
                    DELETE -> Unit// Do nothing
                }
            }
        }
    }
}