package io.heartpattern.mcremapper.parser.csrg

import io.heartpattern.mcremapper.model.*
import io.heartpattern.mcremapper.model.ClassMapping
import io.heartpattern.mcremapper.parser.MappingsParser

object MappingsCsrgParser : MappingsParser {
    override fun parse(raw: String): Mappings {
        val lines = raw.lines()

        val map = HashMap<String, MutableClassMapping>()
        val mappedToOriginalClassName = HashMap<String, String>()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith('#') )
                continue

            when (trimmed.count {it == ' '}) {
                1 -> ClassCsrgParser.parse(trimmed).let { classMappingBuilder ->
                    val className = classMappingBuilder.original.name
                    map.getOrPut(className) { MutableClassMapping(ClassSignature(className)) }.mapped = classMappingBuilder.mapped
                    mappedToOriginalClassName[classMappingBuilder.mapped] = className
                }
                2 -> FieldCsrgParser.parse(trimmed).let { fieldMappingWithClass ->
                    val className = mappedToOriginalClassName[fieldMappingWithClass.mappedClass] ?: fieldMappingWithClass.mappedClass
                    map.getOrPut(className) { MutableClassMapping(ClassSignature(className)) }.fields.add(fieldMappingWithClass.fieldMapping)
                }
                3 -> MethodCsrgParser.parse(trimmed).let { methodMappingWithClass ->
                    val className = mappedToOriginalClassName[methodMappingWithClass.mappedClass] ?: methodMappingWithClass.mappedClass
                    map.getOrPut(className) { MutableClassMapping(ClassSignature(className)) }.methods.add(methodMappingWithClass.methodMapping)
                }
            }
        }

        return Mappings(map.mapValues { it.value.toClassMapping() })
    }
}

/**
 * [ClassMapping] builder where fields are mutable
 */
data class MutableClassMapping(
    val original: ClassSignature,
    var mapped: String = original.name,
    val fields: MutableSet<FieldMapping> = mutableSetOf(),
    val methods: MutableSet<MethodMapping> = mutableSetOf()
) {
    fun toClassMapping(): ClassMapping {
        return ClassMapping(original, mapped, fields, methods)
    }
}
