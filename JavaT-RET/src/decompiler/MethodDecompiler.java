package decompiler;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public interface MethodDecompiler {
    public void put(ClassStringBuffer buffer, TypeAndName[] args, MethodNode mn, ClassNode parent);
}
