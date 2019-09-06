package io.github.readymadeprogrammer.mcremapper

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import io.github.readymadeprogrammer.mcremapper.mapping.parseMapping
import io.github.readymadeprogrammer.mcremapper.mapping.reverseMapping
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.system.exitProcess

class App : CliktCommand() {
    val mapping: URL by option().convert {
        URL(it)
    }.required()

    val input: File by option().file(exists = true).required()
    val output: File by option().file(exists = false).required()
    val reobf: Boolean by option().flag()

    override fun run() {
        val rawMapping = parseMapping(mapping)
        for (clazz in rawMapping) {
            println(clazz)
        }
        val mapping = if (reobf) rawMapping else reverseMapping(rawMapping)
        val inputFile = ZipFile(input)
        val outputFile = output.let {
            if (output.exists()) output.delete()
            ZipOutputStream(FileOutputStream(it))
        }

        val remapper = SimpleRemapper(mapping.map { it.type.first.type to it }.toMap())

        // Make Type Hierarchy first
        for (entry in inputFile.entries()) {
            val name = entry.name
            try {
                if (name.endsWith(".class")) {
                    val bytes = inputFile.getInputStream(entry).readBytes()
                    ClassReader(bytes).accept(remapper.hierarchyReader, ClassReader.EXPAND_FRAMES)
                }
            } catch (e: Exception) {
                println("Exception occurred while resolve type hierarchy of $name")
                e.printStackTrace()
                exitProcess(1)
            }
        }
        // Remap classes
        for (entry in inputFile.entries()) {
            val name = entry.name
            try {
                if (name.endsWith(".class")) {
                    println("Patch: $name")
                    val reader = ClassReader(inputFile.getInputStream(entry).readBytes())
                    val writer = ClassWriter(0)
                    val adapter = ClassRemapper(writer, remapper)
                    reader.accept(adapter, ClassReader.EXPAND_FRAMES)
                    writer.visitEnd()
                    val className = reader.className
                    outputFile.putNextEntry(ZipEntry("${(mapping.find { it.type.first.type == className }?.type?.second
                        ?: className).replace('.', '/')}.class"))
                    outputFile.write(writer.toByteArray())
                    outputFile.closeEntry()
                    println("$name -> ${mapping.find { it.type.first.type == className }?.type?.second ?: className}")
                } else {
                    println("Copy: $name")
                    outputFile.putNextEntry(ZipEntry(name))
                    ByteArrayInputStream(inputFile.getInputStream(entry).readBytes()).copyTo(outputFile)
                    outputFile.closeEntry()
                }
            } catch (e: Exception) {
                println("Exception occurred while processing file: $name")
                e.printStackTrace()
                exitProcess(1)
            }
        }
        outputFile.close()
    }
}

fun main(args: Array<String>) = App().main(args)