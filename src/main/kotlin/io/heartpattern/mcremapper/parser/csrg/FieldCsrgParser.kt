package io.heartpattern.mcremapper.parser.csrg

import io.heartpattern.mcremapper.model.FieldMapping
import io.heartpattern.mcremapper.model.FieldSignature
import java.util.regex.Pattern

object FieldCsrgParser {
    private val regex = Pattern.compile("(\\S+) (\\S+) (\\S+)")

    fun parse(raw: String): FieldMappingWithClass {
        val matcher = regex.matcher(raw)
        matcher.find()
        return FieldMappingWithClass(
            matcher.group(1),
            FieldMapping(
                FieldSignature(
                    null,
                    matcher.group(2)
                ),
                matcher.group(3)
            )
        )
    }
}

data class FieldMappingWithClass(
    val mappedClass: String,
    val fieldMapping: FieldMapping
)
