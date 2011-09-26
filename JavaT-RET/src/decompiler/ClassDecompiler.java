package decompiler;


import org.objectweb.asm.tree.ClassNode;

/**
 * 
 *  Abstract class used for class and method head decompiling
 * 
 * @author Waterwolf
 *
 */
public interface ClassDecompiler {
    public String get(MethodDecompiler md, final ClassNode cn);
}
