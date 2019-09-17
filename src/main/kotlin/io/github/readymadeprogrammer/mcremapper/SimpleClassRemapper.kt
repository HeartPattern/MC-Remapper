package io.github.readymadeprogrammer.mcremapper

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.MethodRemapper
import org.objectweb.asm.commons.Remapper

// Also remap inner class name
class SimpleClassRemapper(cv: ClassVisitor, remapper: Remapper) : ClassRemapper(cv, remapper) {
    override fun visitInnerClass(name: String, outerName: String?, innerName: String?, access: Int) {
        if (outerName == null || innerName == null)
            super.visitInnerClass(name, outerName, innerName, access)
        val remapped = remapper.mapType(name)
        val outest = remapped.lastIndexOf('$')
        super.visitInnerClass(
            remapped,
            remapped.substring(0, outest),
            remapped.substring(outest + 1, remapped.length),
            access
        )
    }

    override fun createMethodRemapper(mv: MethodVisitor): MethodVisitor {
        return LocalVarFixMethodRemapper(mv, remapper)
    }
}

class LocalVarFixMethodRemapper(mv: MethodVisitor, remapper: Remapper) : MethodRemapper(mv, remapper) {
    override fun visitLocalVariable(name: String?, desc: String?, signature: String?, start: Label?, end: Label?, index: Int) {
        if (name != null && name == "\u2603");
//            super.visitLocalVariable("debug$index", desc, signature, start, end, index)
        else
            super.visitLocalVariable(name, desc, signature, start, end, index)
    }
}