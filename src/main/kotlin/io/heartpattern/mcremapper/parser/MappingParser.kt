package io.heartpattern.mcremapper.parser

import io.heartpattern.mcremapper.model.Mapping

interface MappingParser {
    fun parse(raw: String): Mapping
}
