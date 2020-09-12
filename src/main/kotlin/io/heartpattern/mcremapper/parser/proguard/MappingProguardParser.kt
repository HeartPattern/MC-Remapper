package io.heartpattern.mcremapper.parser.proguard

import io.heartpattern.mcremapper.model.*
import io.heartpattern.mcremapper.parser.MappingParser
import io.heartpattern.mcremapper.toInternal

object MappingProguardParser : MappingParser {

    private val CLASS_REGEX = Regex("""(\S+) -> (\S+):""")
    private val FIELD_REGEX = Regex("""(\S+) ([^\s(]+) -> (\S+)""")
    private val METHOD_REGEX = Regex("""([^\s:]+) (\S+)\((\S*)\) -> (\S+)""")
    private val METHOD_WITH_LINE_NUMBER_REGEX = Regex("(\\d+):(\\d+):(\\S+) (\\S+)\\((\\S*)\\) -> (\\S+)")

    override fun parse(raw: String): Mapping {
        val lines = raw.lines()

        val classMapping = mutableMapOf<String, String>()
        val fieldMapping = mutableMapOf<FieldRef, MemberMappingValue>()
        val methodMapping = mutableMapOf<MethodRef, MemberMappingValue>()

        var currentClass: String? = null

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith('#') || trimmed.isEmpty()) {
                continue
            }

            if (!line.startsWith("    ")) {
                val (originalNotInternal, mappedNotInternal) = CLASS_REGEX.matchEntire(trimmed)!!.destructured
                val original = originalNotInternal.toInternal()
                val mapped = mappedNotInternal.toInternal()
                classMapping[original] = mapped
                currentClass = original
            } else {
                val fieldMatchResult = FIELD_REGEX.matchEntire(trimmed)
                if (fieldMatchResult != null) {
                    val (proguardType, original, mapped) = fieldMatchResult.destructured
                    val type = parseProguardType(proguardType)
                    fieldMapping[FieldRef(currentClass!!, type, original)] = MemberMappingValue(mapped)
                    continue
                }
                val methodMatchResult = METHOD_REGEX.matchEntire(trimmed)
                if (methodMatchResult != null){
                    val (proguardReturnType, original, proguardParameterTypes, mapped) = methodMatchResult.destructured
                    val returnType = parseProguardType(proguardReturnType)
                    val parameterTypes = proguardParameterTypes.split(',').filter { it.isNotBlank() }.map { parseProguardType(it.trim()) }
                    methodMapping[MethodRef(currentClass!!, ParsedMethodDescriptor(returnType, parameterTypes).toString(), original)] = MemberMappingValue(mapped)
                    continue
                }
                val methodWithLineNumberMatchResult = METHOD_WITH_LINE_NUMBER_REGEX.matchEntire(trimmed)
                if (methodWithLineNumberMatchResult != null) {
                    val (_, _, proguardReturnType, original, proguardParameterTypes, mapped) = methodWithLineNumberMatchResult.destructured
                    val returnType = parseProguardType(proguardReturnType)
                    val parameterTypes = proguardParameterTypes.split(',').filter { it.isNotBlank() }.map { parseProguardType(it.trim()) }
                    methodMapping[MethodRef(currentClass!!, ParsedMethodDescriptor(returnType, parameterTypes).toString(), original)] = MemberMappingValue(mapped)
                    continue
                }
                error("Invalid member mapping: $trimmed")
            }
        }

        return Mapping(classMapping, fieldMapping, methodMapping, mapOf())
    }

}
