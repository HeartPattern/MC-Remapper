package io.heartpattern.mcremapper

/**
 * Emulates `--auto-member TOKENS` behavior of SpecialSource-2, which is used by SpigotMC
 */
object JavaTokens {
    val TOKENS: Set<String> = setOf(
        "abstract",
        "assert",
        "boolean",
        "break",
        "byte",
        "case",
        "catch",
        "char",
        "class",
        "const",
        "continue",
        "default",
        "do",
        "double",
        "else",
        "enum",
        "extends",
        "final",
        "finally",
        "float",
        "for",
        "goto",
        "if",
        "implements",
        "import",
        "instanceof",
        "int",
        "interface",
        "long",
        "native",
        "new",
        "package",
        "private",
        "protected",
        "public",
        "return",
        "short",
        "static",
        "strictfp",
        "super",
        "switch",
        "synchronized",
        "this",
        "throw",
        "throws",
        "transient",
        "try",
        "void",
        "volatile",
        "while",
        "true",
        "false",
        "null"
    )

    fun appendIfToken(name: String): String? {
        val found = TOKENS.find { token ->
            name.startsWith(token)
        } ?: return null

        val rest = name.substring(found.length)
        if (rest.any {c -> c != '_'}) {
            return null
        }
        return name + "_"
    }
}
