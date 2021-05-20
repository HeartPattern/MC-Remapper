package io.heartpattern.mcremapper.preprocess

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import java.io.File
import java.util.*
import java.util.zip.ZipFile

/**
 * Resolve super type of class
 */
class SuperTypeResolver(private val directSuperNames: Map<String, List<String>>) {

    companion object {
        /**
         * Create [SuperTypeResolver] from given JAR [file]
         */
        fun fromFile(file: File) : SuperTypeResolver {
            val directSuperTypes = mutableMapOf<String, MutableList<String>>().withDefault { mutableListOf() }

            val zipFile = ZipFile(file)
            for (entry in zipFile.entries()) {
                if (!entry.name.endsWith(".class")) continue
                zipFile.getInputStream(entry).use {
                    ClassReader(it)
                }.accept(object : ClassVisitor(Opcodes.ASM9) {
                    override fun visit(
                        version: Int,
                        access: Int,
                        name: String,
                        signature: String?,
                        superName: String?,
                        interfaces: Array<out String>?
                    ) {
                        if (superName != null || !interfaces.isNullOrEmpty()) {
                            val list = mutableListOf<String>()
                            if (superName != null) {
                                list.add(superName)
                            }
                            if (interfaces != null) {
                                list.addAll(interfaces)
                            }
                            directSuperTypes[name] = list
                        }
                    }
                }, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
            }
            zipFile.close()
            return SuperTypeResolver(directSuperTypes)
        }
    }

    /**
     * Iterate through all supertype names of given class [name] including [name] itself
     */
    fun iterateSuperNames(name: String): Iterator<String> {
        return iterator {
            val queue = ArrayDeque<String>()
            val queued = mutableSetOf<String>()

            queue.addLast(name)
            while (queue.isNotEmpty()) {
                val target = queue.removeFirst()
                yield(target)

                for (superclass in directSuperNames[target] ?: continue) {
                    if (superclass !in queued) {
                        queue.addLast(superclass)
                    }
                }
            }
        }
    }

}
