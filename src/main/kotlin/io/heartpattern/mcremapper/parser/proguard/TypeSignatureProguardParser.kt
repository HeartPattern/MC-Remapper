package io.heartpattern.mcremapper.parser.proguard

import io.heartpattern.mcremapper.model.TypeSignature

object TypeSignatureProguardParser : ProguardParser<TypeSignature> {
    override fun parse(raw: String): TypeSignature {
        return TypeSignature(when (raw) {
            "void" -> "V"
            "byte" -> "B"
            "char" -> "C"
            "double" -> "D"
            "float" -> "F"
            "int" -> "I"
            "long" -> "J"
            "short" -> "S"
            "boolean" -> "Z"
            "" -> throw IllegalArgumentException("Empty type signature")
            else -> {
                if (raw.endsWith("[]"))
                    '[' + parse(raw.substring(0, raw.length - 2)).name
                else
                    "L$raw;"
            }
        })
    }
}