package decompiler;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * 
 *  Abstract class for method body decompilers
 * 
 * @author Waterwolf
 *
 */
public interface MethodDecompiler {
    public void put(ClassStringBuffer buffer, TypeAndName[] args, MethodNode mn, ClassNode parent);
}
