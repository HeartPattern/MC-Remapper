package io.heartpattern.mcremapper.parser.csrg

import io.heartpattern.mcremapper.model.ClassSignature
import java.util.regex.Pattern

object ClassCsrgParser {
    private val regex = Pattern.compile("(\\S+) (\\S+)")

    fun parse(s: String): MutableClassMapping {
        val matcher = regex.matcher(s)
        matcher.find()

        val classSignature = ClassSignature(matcher.group(1))
        val mapped = matcher.group(2)

        return MutableClassMapping(classSignature, mapped)
    }
}

