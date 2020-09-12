package io.heartpattern.mcremapper.parser.csrg

import io.heartpattern.mcremapper.model.FieldRef
import io.heartpattern.mcremapper.model.Mapping
import io.heartpattern.mcremapper.model.MemberMappingValue
import io.heartpattern.mcremapper.model.MethodRef
import io.heartpattern.mcremapper.parser.MappingParser

object MappingCsrgParser : MappingParser {

    override fun parse(raw: String): Mapping {
        val lines = raw.lines()

        val classMapping = mutableMapOf<String, String>()
        val fieldMapping = mutableMapOf<FieldRef, MemberMappingValue>()
        val methodMapping = mutableMapOf<MethodRef, MemberMappingValue>()
        val packageMapping = mutableMapOf<String, String>()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith('#') )
                continue

            when (trimmed.count {it == ' '}) {
                1 -> {
                    val (original, mapped) = line.split(' ')
                    if (!original.endsWith('/')) {
                        classMapping[original] = mapped
                    } else {
                        packageMapping[original.substring(0, original.length - 1)] = mapped
                    }
                }
                2 -> {
                    val (owner, original, mapped) = line.split(' ')
                    fieldMapping[FieldRef(owner, null, original)] = MemberMappingValue(mapped)
                }
                3 -> {
                    val (owner, original, descriptor, mapped) = line.split(' ')
                    methodMapping[MethodRef(owner, descriptor, original)] = MemberMappingValue(mapped)
                }
            }
        }

        return Mapping(classMapping, fieldMapping, methodMapping, packageMapping)
    }

}
