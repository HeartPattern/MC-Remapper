package io.heartpattern.mcremapper

import io.heartpattern.mcremapper.model.ArtifactType
import io.heartpattern.mcremapper.model.LocalVariableFixType
import io.heartpattern.mcremapper.model.Mapping
import io.heartpattern.mcremapper.parser.proguard.MappingProguardParser
import io.heartpattern.mcremapper.preprocess.SuperTypeResolver
import io.heartpattern.mcremapper.visitor.LocalVariableFixVisitor
import io.heartpattern.mcremapper.visitor.MappingRemapper
import io.heartpattern.mcremapper.visitor.ParameterAnnotationFixVisitor
import io.heartpattern.mcversions.MCVersions
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.tree.ClassNode
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * Utility class for applying mapping
 */
class MCRemapper(
    private val mapping: Mapping,
    private val superResolver: SuperTypeResolver,
    private val fixType: LocalVariableFixType
) {

    /**
     * Apply mapping to given [byteArray] of bytecode
     */
    @Suppress("unused")
    fun applyMapping(byteArray: ByteArray): ByteArray {
        val reader = ClassReader(byteArray)
        val node = applyMapping(reader)
        val writer = ClassWriter(0)
        node.accept(writer)
        return writer.toByteArray()
    }

    /**
     * Apply mapping to given [reader] and return mapped [ClassNode]
     */
    fun applyMapping(reader: ClassReader): ClassNode {
        val node = ClassNode()
        val visitor = ClassRemapper(
            LocalVariableFixVisitor(
                ParameterAnnotationFixVisitor(
                    node
                ),
                fixType
            ),
            MappingRemapper(mapping, superResolver)
        )

        reader.accept(visitor, 0)
        return node
    }

    /**
     * Apply mapping to given [input] and save to [output]
     */
    fun applyMapping(
        input: File,
        output: File,
        thread: Int = 8
    ) {
        val mappingExecutor = Executors.newFixedThreadPool(thread)
        val outputExecutor = Executors.newSingleThreadExecutor()

        output.delete()
        output.createNewFile()

        val zipInput = ZipFile(input)
        val zipOutput = ZipOutputStream(FileOutputStream(output))

        for (entry in zipInput.entries()) {
            val bytes = zipInput.getInputStream(entry).readBytes()
            if (entry.name.endsWith(".class")) {
                mappingExecutor.execute {
                    val reader = ClassReader(bytes)
                    val node = applyMapping(reader)
                    val writer = ClassWriter(0)
                    node.accept(writer)

                    outputExecutor.execute {
                        zipOutput.putNextEntry(ZipEntry("${node.name.toInternal()}.class"))
                        zipOutput.write(writer.toByteArray())
                        zipOutput.closeEntry()
                    }
                }
            } else {
                outputExecutor.execute {
                    zipOutput.putNextEntry(ZipEntry(entry.name))
                    ByteArrayInputStream(bytes).copyTo(zipOutput)
                    zipOutput.closeEntry()
                }
            }
        }

        mappingExecutor.shutdown()
        mappingExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.HOURS)
        outputExecutor.shutdown()
        outputExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.HOURS)

        zipOutput.close()
    }

    companion object {
        fun applyVersionMapping(
            output: File,
            version: String,
            type: ArtifactType,
            thread: Int = 8,
            fixLocalVar: LocalVariableFixType = LocalVariableFixType.NO
        ) {
            val client = MCVersions()
            val versionList = client.requestVersionSetAsync().get()
            val versionInfo = client.requestVersionAsync(
                versionList.versions.find { it.id == version }
                        ?: throw IllegalArgumentException("$version does not exists")
            ).get()

            val input = when (type) {
                ArtifactType.CLIENT -> versionInfo.downloads.client
                ArtifactType.SERVER -> versionInfo.downloads.server
            }?.url?.download("input")
                ?: throw IllegalArgumentException("$version does not provide $type")

            val rawMapping = when (type) {
                ArtifactType.CLIENT -> versionInfo.downloads.client_mappings
                ArtifactType.SERVER -> versionInfo.downloads.server_mappings
            }?.url?.readText()
                ?: throw IllegalArgumentException("$version does not provide mapping of $type")

            val mapping = MappingProguardParser.parse(rawMapping).reversed()
            val resolver = SuperTypeResolver.fromFile(input)

            val remapper = MCRemapper(mapping, resolver, fixLocalVar)

            remapper.applyMapping(input, output, thread)
        }
    }
}
