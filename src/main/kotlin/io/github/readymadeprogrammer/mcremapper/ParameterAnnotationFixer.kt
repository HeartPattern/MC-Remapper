package io.github.readymadeprogrammer.mcremapper

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * ProGuard, the obfuscator used by Minecraft, invalidates information about the number of parameters
 * when there are synthetic ones, e.g., in the constructor of an inner class.
 * This class visitor fixes them.
 *
 * Originally written by Pokechu22 for MCP, translated to Kotlin for MC-Remapper by pdinklag.
 */
class ParameterAnnotationFixer(cv: ClassVisitor) : ClassVisitor(Opcodes.ASM6, cv) {
    override fun visitEnd() {
        super.visitEnd()
        
        val cls = (cv as ClassNode)
        val syntheticParams = getExpectedSyntheticParams(cls)
        if (syntheticParams != null) {
            for (mn in cls.methods) {
                if (mn.name.equals("<init>")) {
                    processConstructor(cls, mn, syntheticParams);
                }
            }
        }
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
    private fun getExpectedSyntheticParams(cls: ClassNode) : Array<Type>? {
        // Check for enum
        // http://hg.openjdk.java.net/jdk8/jdk8/langtools/file/1ff9d5118aae/src/share/classes/com/sun/tools/javac/comp/Lower.java#l2866
        if ((cls.access and Opcodes.ACC_ENUM != 0)) {
            // considering class for extra parameter annotations as it is an enum
            //return new Type[] { Type.getObjectType("java/lang/String"), Type.INT_TYPE };
            return arrayOf(Type.getObjectType("java/lang/String"), Type.INT_TYPE)
        }
        
        // Check for inner class
        var info : InnerClassNode? = null
        for (node in cls.innerClasses) { // note: cls.innerClasses is never null
            if (node.name.equals(cls.name)) {
                info = node;
                break
            }
        }
        
        // http://hg.openjdk.java.net/jdk8/jdk8/langtools/file/1ff9d5118aae/src/share/classes/com/sun/tools/javac/code/Symbol.java#l398
        if (info == null) {
            // not considering class for extra parameter annotations as it is not an inner class
            return null // It's not an inner class
        }
        
        if ((info.access and (Opcodes.ACC_STATIC or Opcodes.ACC_INTERFACE)) != 0) {
            // not considering class for extra parameter annotations as it is an interface or static
            return null // It's static or can't have a constructor
        }
        
        // http://hg.openjdk.java.net/jdk8/jdk8/langtools/file/1ff9d5118aae/src/share/classes/com/sun/tools/javac/jvm/ClassReader.java#l2011
        if (info.innerName == null) {
            // not considering class for extra parameter annotations as it is anonymous
            return null // It's an anonymous class
        }

        // considering class extra parameter annotations as it is an inner class
        if (info.outerName == null) {
            // pdinklag: not really sure why the hell this happens, but it does for some com.google classes
            return null
        }
        
        return arrayOf(Type.getObjectType(info.outerName))
    }
    
    /**
     * Removes the parameter annotations for the given synthetic parameters,
     * if there are parameter annotations and the synthetic parameters exist.
     */
    private fun processConstructor(cls: ClassNode, mn: MethodNode, syntheticParams: Array<Type>) {
        val methodInfo = mn.name + mn.desc + " in " + cls.name
        val params = Type.getArgumentTypes(mn.desc)
        
        if (beginsWith(params, syntheticParams)) {
            mn.visibleParameterAnnotations = process(methodInfo,
                "RuntimeVisibleParameterAnnotations", params.size,
                syntheticParams.size, mn.visibleParameterAnnotations);
                
            mn.invisibleParameterAnnotations = process(methodInfo,
                "RuntimeInvisibleParameterAnnotations", params.size,
                syntheticParams.size, mn.invisibleParameterAnnotations);
                
            // ASM uses this value, not the length of the array
            // Note that this was added in ASM 6.1
            if (mn.visibleParameterAnnotations != null) {
                mn.visibleAnnotableParameterCount = mn.visibleParameterAnnotations.size;
            }
            
            if (mn.invisibleParameterAnnotations != null) {
                mn.invisibleAnnotableParameterCount = mn.invisibleParameterAnnotations.size;
            }
        } else {
            // Unexpected lack of synthetic args to the constructor
        }
    }
    
    private fun beginsWith(values: Array<Type>, prefix: Array<Type>) : Boolean {
        if (values.size < prefix.size) {
            return false
        }
        
        for (i in 0 until prefix.size) {
            if (!values[i].equals(prefix[i])) {
                return false
            }
        }
        
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
            annotations: Array<List<AnnotationNode>>?) : Array<List<AnnotationNode>>?
    {
        if (annotations == null) {
            // method does not have the specified attribute
            return null
        }

        val numAnnotations = annotations.size;
        if (numParams == numAnnotations) {
            // found extra attribute entries in method, removing numSynthetic of them
            return annotations.copyOfRange(numSynthetic, numAnnotations)
        } else if (numParams == numAnnotations - numSynthetic) {
            // number of attribute entries is already as we want
            return annotations
        } else {
            // unexpected number of attribute entries
            println("unexpected number of attribute entries for " + attributeName + " of " + methodInfo)
            return annotations
        }
    }
}
