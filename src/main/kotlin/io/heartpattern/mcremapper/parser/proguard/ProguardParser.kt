package io.heartpattern.mcremapper.parser.proguard

interface ProguardParser<T>{
    fun parse(raw: String): T
}