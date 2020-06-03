package io.heartpattern.mcremapper.parser.proguard

import io.heartpattern.mcremapper.model.ClassMapping
import io.heartpattern.mcremapper.model.Mappings

object MappingsProguardParser : ProguardParser<Mappings> {
    override fun parse(raw: String): Mappings {
        val lines = raw.lines()

        val map = HashMap<String, ClassMapping>()
        val builder = StringBuilder()

        for (line in lines) {
            if (line.startsWith('#'))
                continue


            if (!line.startsWith("    ")) {
                if (builder.isNotEmpty()) {
                    val classMapping = ClassProguardParser.parse(builder.toString().trim())
                    map[classMapping.original.name] = classMapping
                }
                builder.clear()
            }
            builder.appendln(line)
        }

        return Mappings(map)
    }
}