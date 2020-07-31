package io.heartpattern.mcremapper.parser.proguard

import io.heartpattern.mcremapper.model.ClassMapping
import io.heartpattern.mcremapper.model.ClassSignature
import io.heartpattern.mcremapper.model.FieldMapping
import io.heartpattern.mcremapper.model.MethodMapping
import java.util.regex.Pattern

object ClassProguardParser {
    private val regex = Pattern.compile("(\\S+) -> (\\S+):")

    fun parse(raw: String): ClassMapping {
        val lines = raw.lines()

        val matcher = regex.matcher(raw)
        matcher.find()

        val classSignature = ClassSignature(matcher.group(1))
        val mapped = matcher.group(2)

        val fields = mutableSetOf<FieldMapping>()
        val methods = mutableSetOf<MethodMapping>()

        for (line in lines.drop(1)) {
            val trimmed = line.trim()
            if (!trimmed.contains("(")) {
                fields += FieldProguardParser.parse(trimmed)
            } else {
                methods += MethodProguardParser.parse(trimmed)
            }
        }

        return ClassMapping(
            classSignature,
            mapped,
            fields,
            methods
        )
    }
}
