package io.heartpattern.mcremapper.parser.csrg

import io.heartpattern.mcremapper.model.MethodMapping
import io.heartpattern.mcremapper.model.MethodSignature
import io.heartpattern.mcremapper.model.TypeSignature
import java.util.regex.Pattern

object MethodCsrgParser {
    private val regex = Pattern.compile("(\\S+) (\\S+) \\((\\S*)\\)(\\S+) (\\S+)")

    fun parse(raw: String): MethodMappingWithClass {
        val matcher = regex.matcher(raw)
        matcher.find()

        return MethodMappingWithClass(
            matcher.group(1),
            MethodMapping(
                MethodSignature(
                    TypeSignature(matcher.group(4)),
                    matcher.group(3)!!.let { paramCombined ->
                        val split = mutableListOf<String>()
                        var i = 0
                        var paramStart = i
                        var isWaitingForSemicolon = false
                        while (i < paramCombined.length) {
                            if (isWaitingForSemicolon && paramCombined[i] != ';') {
                                i++
                            } else {
                                when (paramCombined[i]) {
                                    'L' -> {
                                        isWaitingForSemicolon = true
                                        i++
                                    }
                                    '[' -> {
                                        i++
                                    }
                                    else -> {
                                        split.add(paramCombined.substring(paramStart, i + 1))
                                        i++
                                        paramStart = i
                                        isWaitingForSemicolon = false
                                    }
                                }
                            }
                        }
                        split
                    }.map {
                        TypeSignature(it)
                    },
                    matcher.group(2)
                ),
                matcher.group(5)
            )
        )
    }
}

data class MethodMappingWithClass(
    val mappedClass: String,
    val methodMapping: MethodMapping
)
