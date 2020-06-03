package io.heartpattern.mcremapper.resolver

/**
 * Resolve super type of class
 */
interface SuperTypeResolver{
    /**
     * Get all super name of given class [name] include [name] itself
     */
    fun getAllSuperClass(name: String): Iterator<String>
}