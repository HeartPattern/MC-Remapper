package io.heartpattern.mcremapper.parser

import io.heartpattern.mcremapper.model.Mappings

interface MappingsParser {
    fun parse(raw: String): Mappings
}
