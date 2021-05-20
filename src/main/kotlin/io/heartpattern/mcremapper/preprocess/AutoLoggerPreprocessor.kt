package io.heartpattern.mcremapper.preprocess

import io.heartpattern.mcremapper.model.FieldRef
import io.heartpattern.mcremapper.model.Mapping
import io.heartpattern.mcremapper.model.MemberMappingValue
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Opcodes
import java.io.File
import java.lang.reflect.Modifier
import java.util.zip.ZipFile

/**
 * Add mappings that automatically renames logger fields to LOGGER
 */
object AutoLoggerPreprocessor {

    /**
     * Preprocess [mapping] with given JAR [file] and [superResolver]
     */
    fun preprocess(mapping: Mapping, file: File, superResolver: SuperTypeResolver) : Mapping {
        val fieldMapping = mapping.fieldMapping.toMutableMap()
        val zipFile = ZipFile(file)
        for (entry in zipFile.entries()) {
            if (!entry.name.endsWith(".class")) continue
            zipFile.getInputStream(entry).use {
                ClassReader(it)
            }.accept(object : ClassVisitor(Opcodes.ASM9) {
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
                    for (superName in superResolver.iterateSuperNames(className!!)) {
                        val ref = FieldRef(superName, descriptor, name)
                        val mappingValue = mapping.fieldMapping[ref]
                        if (mappingValue != null && (superName == className || mappingValue.inheritable)) {
                            return null
                        }
                        val refWithoutType = FieldRef(superName, null, name)
                        val mappingValueMatchedWithoutType = mapping.fieldMapping[refWithoutType]
                        if (mappingValueMatchedWithoutType != null && (superName == className || mappingValueMatchedWithoutType.inheritable)) {
                            return null
                        }
                    }
                    if (Modifier.isStatic(access) && Modifier.isFinal(access) && descriptor == "Lorg.apache.logging.log4j.Logger;") {
                        fieldMapping[FieldRef(className!!, "Lorg.apache.logging.log4j.Logger", name)] = MemberMappingValue("LOGGER")
                    }
                    return null
                }
            }, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
        }
        zipFile.close()
        return mapping.copy(fieldMapping = fieldMapping)
    }

}
