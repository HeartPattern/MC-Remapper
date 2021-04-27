package io.heartpattern.mcremapper.parser.srg

import io.heartpattern.mcremapper.model.FieldRef
import io.heartpattern.mcremapper.model.Mapping
import io.heartpattern.mcremapper.model.MemberMappingValue
import io.heartpattern.mcremapper.model.MethodRef
import io.heartpattern.mcremapper.parser.MappingParser

object MappingSrgParser : MappingParser {
    private const val INSTRUCTION_SEPARATOR = ' '

    override fun parse(raw: String): Mapping {
        val lines = raw.replace('\t', INSTRUCTION_SEPARATOR).lines()

        val classMapping = mutableMapOf<String, String>()
        val fieldMapping = mutableMapOf<FieldRef, MemberMappingValue>()
        val methodMapping = mutableMapOf<MethodRef, MemberMappingValue>()
        val packageMapping = mutableMapOf<String, String>()


        // there are 2 types of csrg mappings. I'll call this one v1, see:
        /*
        FD: net/minecraft/util/text/TextFormatting/AQUA net/minecraft/util/text/TextFormatting/AQUA
        FD: net/minecraft/util/text/TextFormatting/BLACK net/minecraft/util/text/TextFormatting/BLACK
         */

        // and the other one will be v2 now
        /*
        a net/minecraft/util/text/TextFormatting
         a BLACK
         b DARK_BLUE
         */


        fun parseV1() {
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.isEmpty() || trimmed.startsWith('#')) {
                    continue
                }
                var currentLineType: SrgLineTypes? = null

                for (lineType in SrgLineTypes.VALUES) {
                    if (line.startsWith(lineType.prefix)) {
                        currentLineType = lineType
                        break
                    }
                }
                check(currentLineType != null) { "Invalid mappings line: $line" }

                val lineInstructions = line.removePrefix(currentLineType.prefix).split(INSTRUCTION_SEPARATOR)


                fun getOriginalClassFieldAndMappedName(v2MappedIndex: Int = 1): Triple<String, String, String> {
                    val classFieldSplit = lineInstructions[0].split("/")
                    val originalName: String = classFieldSplit.last()
                    val originalClass: String = lineInstructions[0].removeSuffix("/$originalName")
                    val mappedName: String = lineInstructions[v2MappedIndex].split("/").last()
                    return Triple(originalClass, originalName, mappedName)
                }

                when (currentLineType) {
                    SrgLineTypes.PACKAGE -> {
                        // PK: net/minecraft net/minecraft
                        packageMapping[lineInstructions[0]] = lineInstructions[1]
                    }
                    SrgLineTypes.CLASS -> {
                        // CL: a net/minecraft/util/text/TextFormatting
                        classMapping[lineInstructions[0]] = lineInstructions[1]
                    }
                    SrgLineTypes.FIELD -> {
                        // FD: a/a net/minecraft/util/text/TextFormatting/BLACK
                        // FD: net/minecraft/util/text/TextFormatting/BLACK net/minecraft/util/text/TextFormatting/BLACK
                        val (originalClass, originalFieldName, mappedFieldName) = getOriginalClassFieldAndMappedName()
                        fieldMapping[FieldRef(originalClass, null, originalFieldName)] = MemberMappingValue(mappedFieldName)
                    }
                    SrgLineTypes.METHOD -> {
                        // MD: a/a (I)La; net/minecraft/util/text/TextFormatting/func_175744_a (I)Lnet/minecraft/util/text/TextFormatting;
                        // MD: net/minecraft/util/text/TextFormatting/func_175744_a (I)Lnet/minecraft/util/text/TextFormatting; net/minecraft/util/text/TextFormatting/fromColorIndex (I)Lnet/minecraft/util/text/TextFormatting;
                        val (originalClass, originalMethodName, mappedMethodName) = getOriginalClassFieldAndMappedName(v2MappedIndex = 2)

                        methodMapping[MethodRef(originalClass, lineInstructions[3], originalMethodName)] = MemberMappingValue(mappedMethodName)
                    }
                }
            }
        }

        fun parseV2() {
            var currentClass: String? = null
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.isEmpty() || trimmed.startsWith('#')) {
                    continue
                }

                when (line.count { it == INSTRUCTION_SEPARATOR }) {
                    1 -> {
                        val (original, mapped) = line.split(INSTRUCTION_SEPARATOR)
                        if (!original.endsWith('/')) {
                            classMapping[original] = mapped
                            currentClass = original
                        } else {
                            packageMapping[original.substring(0, original.length - 1)] = mapped
                        }
                    }
                    2 -> {
                        val (original, mapped) = trimmed.split(INSTRUCTION_SEPARATOR)
                        fieldMapping[FieldRef(currentClass!!, null, original)] = MemberMappingValue(mapped)
                    }
                    3 -> {
                        val (original, descriptor, mapped) = trimmed.split(INSTRUCTION_SEPARATOR)
                        methodMapping[MethodRef(currentClass!!, descriptor, original)] = MemberMappingValue(mapped)
                    }
                }
            }
        }

        var useV2 = true
        for (lineType in SrgLineTypes.VALUES) {
            if (raw.contains(lineType.prefix)) {
                // format is v1
                useV2 = false
                break
            }
        }


        if (useV2) {
            parseV2()
        } else {
            parseV1()
        }

        return Mapping(classMapping, fieldMapping, methodMapping, packageMapping)
    }

    private enum class SrgLineTypes(val prefix: String) {
        PACKAGE("PK: "),
        CLASS("CL: "),
        FIELD("FD: "),
        METHOD("MD: "),
        ;

        companion object {
            val VALUES = values()
        }
    }
}
