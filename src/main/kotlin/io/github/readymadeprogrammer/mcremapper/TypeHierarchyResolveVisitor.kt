package io.github.readymadeprogrammer.mcremapper

import com.google.common.collect.MultimapBuilder
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.analysis.Frame
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.LinkedBlockingQueue
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

class TypeHierarchyResolveVisitor() : ClassVisitor(Opcodes.ASM5) {
    val hierarchy = MultimapBuilder.SetMultimapBuilder.hashKeys().hashSetValues().build<String, String>()!!

    fun visitAll(file: File){
        val zipFile = ZipFile(file)
        for(entry in zipFile.entries()){
            if(!entry.name.endsWith(".class")) continue
            val bytes = zipFile.getInputStream(entry).readAllBytes()
            val reader = ClassReader(bytes)
            reader.accept(this, ClassReader.EXPAND_FRAMES)
        }
        zipFile.close()
    }
    override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String?, interfaces: Array<out String>?) {
        if(superName!=null)  hierarchy.put(name, superName)
        if(interfaces != null) hierarchy.putAll(name, interfaces.asIterable())
    }

    fun getAllSuperClass(classInfo: ClassInfo): Iterable<ClassInfo> = object : Iterable<ClassInfo> {
        override fun iterator(): Iterator<ClassInfo> = object : Iterator<ClassInfo> {
            val queue = LinkedBlockingQueue<String>()
            val iterated = HashSet<String>()
            var next: String? = null

            init {
                next = classInfo.value
            }

            override fun hasNext(): Boolean {
                return next != null
            }

            override fun next(): ClassInfo {
                val result = next ?: throw IndexOutOfBoundsException("No more superclass")
                iterated.add(result)
                hierarchy[result].asSequence().filter(iterated::contains).forEach(queue::put)
                next = null
                while (next == null && !queue.isEmpty()) {
                    next = if (iterated.contains(queue.peek())) null else queue.poll()
                }
                return ClassInfo(result)
            }
        }
    }
}