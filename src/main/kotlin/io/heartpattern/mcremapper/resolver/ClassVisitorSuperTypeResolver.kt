package io.heartpattern.mcremapper.resolver

import com.google.common.collect.MultimapBuilder
import io.heartpattern.mcremapper.fromInternal
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import java.io.File
import java.util.*
import java.util.zip.ZipFile
import kotlin.collections.HashSet

/**
 * [SuperTypeResolver] implementation by visiting class in jar
 */
class ClassVisitorSuperTypeResolver: ClassVisitor(Opcodes.ASM8), SuperTypeResolver {
    private val hierarchy = MultimapBuilder.SetMultimapBuilder
        .hashKeys()
        .hashSetValues()
        .build<String, String>()!!

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<String>?
    ) {
        if (superName != null)
            hierarchy.put(name.fromInternal(), superName.fromInternal())
        if (interfaces != null)
            hierarchy.putAll(name.fromInternal(), interfaces.map(String::fromInternal))
    }

    /**
     * Resolve super type in given [file]
     */
    fun resolve(file: File) {
        val zipFile = ZipFile(file)
        for (entry in zipFile.entries()) {
            if (!entry.name.endsWith(".class")) continue
            val bytes = zipFile.getInputStream(entry).readBytes()
            val reader = ClassReader(bytes)
            reader.accept(this, ClassReader.EXPAND_FRAMES)
        }
        zipFile.close()
    }

    override fun getAllSuperClass(name: String): Iterator<String> {
        return iterator {
            val queue = LinkedList<String>()
            val visited = HashSet<String>()

            queue.add(name)

            yield(name)
            while (queue.isNotEmpty()) {
                val target = queue.poll()
                if (!visited.add(target))
                    continue

                for (candidate in hierarchy[target]) {
                    if (candidate in visited)
                        continue

                    yield(candidate)
                    queue.add(candidate)
                }
            }
        }
    }
}