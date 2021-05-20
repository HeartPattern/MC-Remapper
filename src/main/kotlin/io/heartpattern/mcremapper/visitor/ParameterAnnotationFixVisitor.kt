package io.heartpattern.mcremapper.visitor

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode

/**
 * ProGuard, the obfuscator used by Minecraft, invalidates information about the number of parameters
 * when there are synthetic ones, e.g., in the constructor of an inner class.
 * This class visitor fixes them.
 *
 * Originally written by Pokechu22 for MCP, translated to Kotlin for MC-Remapper by pdinklag.
 */
class ParameterAnnotationFixVisitor(cv: ClassNode) : ClassVisitor(Opcodes.ASM9, cv) {
    override fun visitEnd() {
        super.visitEnd()

        val cls = (cv as ClassNode)
        val syntheticParams = getExpectedSyntheticParams(cls)
        if (syntheticParams != null)
            for (mn in cls.methods)
                if (mn.name == "<init>")
                    processConstructor(cls, mn, syntheticParams)
    }

    /**
     * Checks if the given class might have synthetic parameters in the
     * constructor. There are two cases where this might happen:
     * <ol>
     * <li>If the given class is an inner class, the first parameter is the
     * instance of the outer class.</li>
     * <li>If the given class is an enum, the first parameter is the enum
     * constant name and the second parameter is its ordinal.</li>
     * </ol>
     *
     * @return An array of types for synthetic parameters if the class can have
     *         synthetic parameters, otherwise null.
     */
    private fun getExpectedSyntheticParams(cls: ClassNode): Array<Type>? {
        val info = cls.innerClasses.find { it.name == cls.name }

        return when {
            // Check for enum
            // http://hg.openjdk.java.net/jdk8/jdk8/langtools/file/1ff9d5118aae/src/share/classes/com/sun/tools/javac/comp/Lower.java#l2866
            // considering class for extra parameter annotations as it is an enum
            //return new Type[] { Type.getObjectType("java/lang/String"), Type.INT_TYPE };
            cls.access and Opcodes.ACC_ENUM != 0 -> arrayOf(Type.getObjectType("java/lang/String"), Type.INT_TYPE)

            // http://hg.openjdk.java.net/jdk8/jdk8/langtools/file/1ff9d5118aae/src/share/classes/com/sun/tools/javac/code/Symbol.java#l398
            // not considering class for extra parameter annotations as it is not an inner class
            info == null -> null // It's not an inner class

            // not considering class for extra parameter annotations as it is an interface or static
            info.access and (Opcodes.ACC_STATIC or Opcodes.ACC_INTERFACE) != 0 -> null // It's static or can't have a constructor

            // http://hg.openjdk.java.net/jdk8/jdk8/langtools/file/1ff9d5118aae/src/share/classes/com/sun/tools/javac/jvm/ClassReader.java#l2011
            // not considering class for extra parameter annotations as it is anonymous
            info.innerName == null -> null // It's an anonymous class

            // considering class extra parameter annotations as it is an inner class
            // pdinklag: not really sure why the hell this happens, but it does for some com.google classes
            info.outerName == null -> null

            else -> arrayOf(Type.getObjectType(info.outerName))
        }
    }

    /**
     * Removes the parameter annotations for the given synthetic parameters,
     * if there are parameter annotations and the synthetic parameters exist.
     */
    private fun processConstructor(cls: ClassNode, mn: MethodNode, syntheticParams: Array<Type>) {
        val methodInfo = mn.name + mn.desc + " in " + cls.name
        val params = Type.getArgumentTypes(mn.desc)

        @Suppress("ControlFlowWithEmptyBody")
        if (beginsWith(params, syntheticParams)) {
            mn.visibleParameterAnnotations = process(
                methodInfo,
                "RuntimeVisibleParameterAnnotations",
                params.size,
                syntheticParams.size,
                mn.visibleParameterAnnotations
            )

            mn.invisibleParameterAnnotations = process(
                methodInfo,
                "RuntimeInvisibleParameterAnnotations",
                params.size,
                syntheticParams.size,
                mn.invisibleParameterAnnotations
            )

            // ASM uses this value, not the length of the array
            // Note that this was added in ASM 6.1
            if (mn.visibleParameterAnnotations != null) {
                mn.visibleAnnotableParameterCount = mn.visibleParameterAnnotations.size
            }

            if (mn.invisibleParameterAnnotations != null) {
                mn.invisibleAnnotableParameterCount = mn.invisibleParameterAnnotations.size
            }
        } else {
            // Unexpected lack of synthetic args to the constructor
        }
    }

    private fun beginsWith(values: Array<Type>, prefix: Array<Type>): Boolean {
        if (values.size < prefix.size)
            return false

        for (i in prefix.indices)
            if (values[i] != prefix[i])
                return false

        return true
    }

    /**
     * Removes annotation nodes corresponding to synthetic parameters, after
     * the existence of synthetic parameters has already been checked.
     *
     * @param methodInfo
     *            A description of the method, for logging
     * @param attributeName
     *            The name of the attribute, for logging
     * @param numParams
     *            The number of parameters in the method
     * @param numSynthetic
     *            The number of synthetic parameters (should not be 0)
     * @param annotations
     *            The current array of annotation nodes, may be null
     * @return The new array of annotation nodes, may be null
     */
    private fun process(
        methodInfo: String,
        attributeName: String,
        numParams: Int,
        numSynthetic: Int,
        annotations: Array<List<AnnotationNode>>?
    ): Array<List<AnnotationNode>>? {
        if (annotations == null) {
            // method does not have the specified attribute
            return null
        }

        val numAnnotations = annotations.size
        return when (numParams) {
            // found extra attribute entries in method, removing numSynthetic of them
            numAnnotations -> annotations.copyOfRange(numSynthetic, numAnnotations)

            // number of attribute entries is already as we want
            numAnnotations - numSynthetic -> annotations

            // unexpected number of attribute entries
            else -> throw IllegalStateException("unexpected number of attribute entries for $attributeName of $methodInfo")
        }
    }
}
