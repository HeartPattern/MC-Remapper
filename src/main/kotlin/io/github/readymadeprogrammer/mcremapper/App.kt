package io.github.readymadeprogrammer.mcremapper

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import java.io.File
import java.net.URL
import kotlin.system.exitProcess

lateinit var app: App

class App : CliktCommand() {
    val mapping: URL by option().convert {
        URL(it)
    }.required()

    val input: File by option().file(exists = true).required()
    val output: File by option().file(exists = false).required()
    val reobf: Boolean by option().flag()
    val thread: Int by option().int().default(8)
    val fixlocalvar: LocalVarFixType by option().choice("no", "rename", "delete").convert {
        when (it) {
            "no" -> LocalVarFixType.NO_FIX
            "rename" -> LocalVarFixType.RENAME
            "delete" -> LocalVarFixType.DELETE
            else -> error("")
        }
    }.default(LocalVarFixType.NO_FIX)

    override fun run() {
        app = this
        val mapping = if (reobf) parseMapping(mapping) else reverseMapping(parseMapping(mapping))

        val hierarchy = TypeHierarchyResolveVisitor()
        hierarchy.visitAll(input)

        applyMapping(mapping, hierarchy)
        println("Complete")
        exitProcess(0)
    }
}

fun main(args: Array<String>) = App().main(args)
