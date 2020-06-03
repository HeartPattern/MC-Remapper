package io.heartpattern.mcremapper.visitor

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.Remapper

/**
 * Remapper which also remap inner class properly
 */
class InnerClassRemapper(cv: ClassVisitor, remapper: Remapper) : ClassRemapper(cv, remapper) {
    override fun visitInnerClass(name: String?, outerName: String?, innerName: String?, access: Int) {
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
}
