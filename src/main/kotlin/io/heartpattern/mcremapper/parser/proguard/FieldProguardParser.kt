package io.heartpattern.mcremapper.parser.proguard

import io.heartpattern.mcremapper.model.FieldMapping
import io.heartpattern.mcremapper.model.FieldSignature
import java.util.regex.Pattern

object FieldProguardParser {
    private val regex = Pattern.compile("(\\S+) (\\S+) -> (\\S+)")

    fun parse(raw: String): FieldMapping {
        val matcher = regex.matcher(raw)
        matcher.find()
        return FieldMapping(
            FieldSignature(
                TypeSignatureProguardParser.parse(matcher.group(1)),
                matcher.group(2)
            ),
            matcher.group(3)
        )
    }
}
