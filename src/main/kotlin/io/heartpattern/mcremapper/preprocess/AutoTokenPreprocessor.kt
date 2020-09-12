package io.heartpattern.mcremapper.preprocess

import io.heartpattern.mcremapper.JavaTokens
import io.heartpattern.mcremapper.model.FieldRef
import io.heartpattern.mcremapper.model.Mapping
import io.heartpattern.mcremapper.model.MemberMappingValue
import io.heartpattern.mcremapper.model.MethodRef
import org.objectweb.asm.*
import java.io.File
import java.util.zip.ZipFile

/**
 * Add mappings that automatically renames members whose names are java keywords
 */
object AutoTokenPreprocessor {

    /**
     * Preprocess [mapping] with given JAR [file] and [superResolver]
     */
    fun preprocess(mapping: Mapping, file: File, superResolver: SuperTypeResolver) : Mapping {
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
                    JavaTokens.appendIfToken(name)?.let { fieldMapping[FieldRef(className!!, descriptor, name)] = MemberMappingValue(it) }
                    return null
                }

                override fun visitMethod(
                    access: Int,
                    name: String,
                    descriptor: String,
                    signature: String?,
                    exceptions: Array<out String>?
                ): MethodVisitor? {
                    for (superName in superResolver.iterateSuperNames(className!!)) {
                        val ref = MethodRef(superName, descriptor, name)
                        val mappingValue = mapping.methodMapping[ref]
                        if (mappingValue != null && (superName == className || mappingValue.inheritable)) {
                            return null
                        }
                    }
                    JavaTokens.appendIfToken(name)?.let { methodMapping[MethodRef(className!!, descriptor, name)] = MemberMappingValue(it) }
                    return null
                }
            }, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
        }
        zipFile.close()
        return mapping.copy(fieldMapping = fieldMapping, methodMapping = methodMapping)
    }

}
