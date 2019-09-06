package io.github.readymadeprogrammer.mcremapper.mapping

data class ClassMapping(
    val type: Pair<ClassSignature, String>,
    val field: Map<FieldSignature, String>,
    val method: Map<MethodSignature, String>
)