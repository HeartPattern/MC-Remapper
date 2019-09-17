package io.github.readymadeprogrammer.mcremapper

import com.google.common.collect.MultimapBuilder
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import java.io.File
import java.util.concurrent.LinkedBlockingQueue
import java.util.zip.ZipFile

class TypeHierarchyResolveVisitor : ClassVisitor(Opcodes.ASM5) {
    val hierarchy = MultimapBuilder.SetMultimapBuilder.hashKeys().hashSetValues().build<String, String>()!!

    fun visitAll(file: File) {
        val zipFile = ZipFile(file)
        for (entry in zipFile.entries()) {
            if (!entry.name.endsWith(".class")) continue
            val bytes = zipFile.getInputStream(entry).readBytes()
            val reader = ClassReader(bytes)
            reader.accept(this, ClassReader.EXPAND_FRAMES)
        }
        zipFile.close()
    }

    override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String?, interfaces: Array<out String>?) {
        if (superName != null) hierarchy.put(name.replace('/','.'), superName.replace('/','.'))
        if (interfaces != null) hierarchy.putAll(name.replace('/','.'), interfaces.map{it.replace('/','.')})
    }

    fun getAllSuperClass(classInfo: ClassInfo): Iterable<ClassInfo> = object : Iterable<ClassInfo> {
        override fun iterator(): Iterator<ClassInfo> = object : Iterator<ClassInfo> {
            val queue = LinkedBlockingQueue<String>()
            val iterated = HashSet<String>()
            var next: String? = classInfo.value

            override fun hasNext(): Boolean {
                return next != null
            }

            override fun next(): ClassInfo {
                val result = next ?: throw IndexOutOfBoundsException("No more superclass")
                iterated.add(result)
                hierarchy[result].asSequence().filterNot(iterated::contains).forEach(queue::put)
                next = null
                while (next == null && !queue.isEmpty()) {
                    val candidate = queue.poll()
                    next = if (iterated.contains(candidate)) null else candidate
                }
                return ClassInfo(result)
            }
        }
    }
}