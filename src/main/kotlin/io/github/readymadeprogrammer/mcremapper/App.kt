package io.github.readymadeprogrammer.mcremapper

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import java.io.File
import java.net.URL

class App : CliktCommand() {
    val mapping: URL by option().convert {
        URL(it)
    }.required()

    val input: File by option().file(exists = true).required()
    val output: File by option().file(exists = false).required()
    val reobf: Boolean by option().flag()
    val thread: Int by option().int().default(8)

    override fun run() {
        val mapping = if (reobf) parseMapping(mapping) else parseMapping(mapping).map { map ->
            ClassMapping(
                Mapping(ClassInfo(map.classMapping.mapped), map.classMapping.from.value),
                map.fieldMappings.map { Mapping(FieldInfo(it.from.type, it.mapped), it.from.name) }.toSet(),
                map.methodMappings.map { Mapping(MethodInfo(it.from.returnType, it.from.parameters, it.mapped), it.from.name) }.toSet()
            )
        }.toSet()

        val hierarchy = TypeHierarchyResolveVisitor()
        hierarchy.visitAll(input)

        applyMapping(input, output, mapping, hierarchy)
    }
}

fun main(args: Array<String>) = App().main(args)