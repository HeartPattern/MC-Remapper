package io.heartpattern.mcremapper.preprocess

import io.heartpattern.mcremapper.model.FieldRef
import io.heartpattern.mcremapper.model.Mapping
import io.heartpattern.mcremapper.model.MethodRef
import org.objectweb.asm.*
import java.io.File
import java.lang.reflect.Modifier
import java.util.zip.ZipFile

/**
 * Add mappings that automatically renames logger fields to LOGGER
 */
object InheritabilityPreprocessor {

    /**
     * Preprocess [mapping] with given JAR [file]
     */
    fun preprocess(mapping: Mapping, file: File) : Mapping {
        val fieldMapping = mapping.fieldMapping.toMutableMap()
        val methodMapping = mapping.methodMapping.toMutableMap()
        val zipFile = ZipFile(file)
        for (entry in zipFile.entries()) {
            if (!entry.name.endsWith(".class")) continue
            zipFile.getInputStream(entry).use {
                ClassReader(it)
            }.accept(object : ClassVisitor(Opcodes.ASM8) {
                var className: String? = null

                override fun visit(
                    version: Int,
                    access: Int,
                    name: String,
                    signature: String?,
                    superName: String?,
                    interfaces: Array<out String>?
                ) {
                    className = name
                }

                override fun visitField(
                    access: Int,
                    name: String,
                    descriptor: String,
                    signature: String?,
                    value: Any?
                ): FieldVisitor? {
                    val ref = FieldRef(className!!, descriptor, name)
                    val mappingValue = fieldMapping[ref]
                    if (mappingValue != null && mappingValue.inheritable) {
                        fieldMapping[ref] = mappingValue.copy(inheritable = !Modifier.isPrivate(access))
                    }

                    val refWithoutType = FieldRef(className!!, null, name)
                    val mappingValueMatchedWithoutType = fieldMapping[refWithoutType]
                    if (mappingValueMatchedWithoutType != null && mappingValueMatchedWithoutType.inheritable) {
                        fieldMapping[refWithoutType] = mappingValueMatchedWithoutType.copy(inheritable = !Modifier.isPrivate(access))
                    }
                    return null
                }

                override fun visitMethod(
                    access: Int,
                    name: String,
                    descriptor: String,
                    signature: String?,
                    exceptions: Array<out String>?
                ): MethodVisitor? {
                    val ref = MethodRef(className!!, descriptor, name)
                    val mappingValue = methodMapping[ref]
                    if (mappingValue != null && mappingValue.inheritable) {
                        methodMapping[ref] = mappingValue.copy(inheritable = !Modifier.isPrivate(access))
                    }
                    return null
                }
            }, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
        }
        zipFile.close()
        return mapping.copy(fieldMapping = fieldMapping, methodMapping = methodMapping)
    }

}
