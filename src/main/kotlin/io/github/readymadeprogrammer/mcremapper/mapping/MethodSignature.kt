package io.github.readymadeprogrammer.mcremapper.mapping

import java.util.regex.Pattern

data class MethodSignature(
    val returnType: TypeSignature,
    val parameterType: List<TypeSignature>,
    val name: String
)

val methodRegex = Pattern.compile("(\\d+):(\\d+):(\\S+) (\\S+)\\((\\S*)\\) -> (\\S+)")
val methodDescriptorRegex = Pattern.compile("\\((\\S*)\\)(\\S+)")

fun parseMethodMapping(raw: String): Pair<MethodSignature, String> {
    val matcher = methodRegex.matcher(raw)
    matcher.find()
    val arguments = if(matcher.group(5).isEmpty()) emptyList() else matcher.group(5).split(',').map { parseType(it.trim()) }
    return MethodSignature(parseType(matcher.group(3)), arguments, matcher.group(4)) to matcher.group(6)
}

private fun parseNext(iterator: Iterator<Char>): String{
    return when(val char = iterator.next()){
        'B','C','D','F','I','J','S','Z' -> char.toString()
        '[' -> char + parseNext(iterator)
        'L' -> {
            val builder = StringBuilder("L")
            var content = iterator.next()
            while(content!=';'){
                builder.append(content)
                content = iterator.next()
            }
            builder.append(content)
            builder.toString()
        }
        else -> error("")
    }
}

fun parseParameter(raw: String): List<TypeSignature>{
    val iterator = raw.iterator()
    val params = ArrayList<TypeSignature>()
    while(iterator.hasNext()){
        params += TypeSignature(parseNext(iterator))
    }

    return params
}

fun parseMethodDescriptor(descriptor: String, name: String): MethodSignature{
    try{
        val matcher = methodDescriptorRegex.matcher(descriptor)
        matcher.find()
        val parameters = parseParameter(matcher.group(1))
        val returnType = matcher.group(2)

        return MethodSignature(TypeSignature(returnType), parameters, name)
    } catch(e: Exception){
        throw Exception("Error occurred while parsing method descriptor: $descriptor, $name", e)
    }
}