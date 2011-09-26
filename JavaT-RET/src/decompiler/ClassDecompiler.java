package decompiler;


import org.objectweb.asm.tree.ClassNode;

public interface ClassDecompiler {
    public String get(MethodDecompiler md, final ClassNode cn);
}
