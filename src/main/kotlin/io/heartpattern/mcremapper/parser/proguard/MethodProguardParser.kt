package io.heartpattern.mcremapper.parser.proguard

import io.heartpattern.mcremapper.model.MethodMapping
import io.heartpattern.mcremapper.model.MethodSignature
import java.util.regex.Pattern

object MethodProguardParser : ProguardParser<MethodMapping> {
    private val lineNumberMethodRegex = Pattern.compile("(\\d+):(\\d+):(\\S+) (\\S+)\\((\\S*)\\) -> (\\S+)")
    private val methodRegex = Pattern.compile("(\\S+) (\\S+)\\((\\S*)\\) -> (\\S+)")

    override fun parse(raw: String): MethodMapping {
        return parseLineNumberMethod(raw)
            ?: parseMethod(raw)
            ?: throw IllegalArgumentException("Malformed method mapping: \"$raw\"")
    }

    private fun parseLineNumberMethod(raw: String): MethodMapping? {
        val matcher = lineNumberMethodRegex.matcher(raw)
        if (!matcher.find())
            return null

        return MethodMapping(
            MethodSignature(
                TypeSignatureProguardParser.parse(matcher.group(3)),
                matcher.group(5)
                    .split(',')
                    .filter{it.isNotBlank()}
                    .map { TypeSignatureProguardParser.parse(it.trim()) },
                matcher.group(4)
            ),
            matcher.group(6)
        )
    }

    private fun parseMethod(raw: String): MethodMapping? {
        val matcher = methodRegex.matcher(raw)
        if (!matcher.find())
            return null

        return MethodMapping(
            MethodSignature(
                TypeSignatureProguardParser.parse(matcher.group(1)),
                matcher.group(3)
                    .split(',')
                    .filter{it.isNotBlank()}
                    .map { TypeSignatureProguardParser.parse(it.trim()) }
                ,
                matcher.group(2)
            ),
            matcher.group(4)
        )
    }
}