package io.github.readymadeprogrammer.mcremapper

import java.net.URL

fun parseMapping(url: URL): Set<ClassMapping> {
    val mappings = HashSet<ClassMapping>()
    val content = url.readText().lines()
    var type: Mapping<ClassInfo>? = null
    var field: MutableSet<Mapping<FieldInfo>>? = null
    var method: MutableSet<Mapping<MethodInfo>>? = null

    var line = 0
    while(line < content.size){
        val current = content[line++]

        if(current.isBlank() || current.trim().startsWith('#'))
            continue

        try{
            if(current[0].isWhitespace()){ // Field or Method
                if(current.trim().first().isDigit()){ //Method
                    val parsed = parseMethodMapping(current.trim())
                    method!! += parsed
                } else { //Field
                    val parsed = parseFieldMapping(current.trim())
                    field!! += parsed
                }
            } else{ //Class
                if(type!=null){
                    mappings += ClassMapping(
                        type,
                        field!!,
                        method!!
                    )
                }
                type = parseClassMapping(current)
                field = HashSet()
                method = HashSet()
            }
        }catch(e: Exception){
            throw Exception("Exception on parsing: $current", e)
        }
    }
    return mappings
}