package reverse.engineer.api;

import org.objectweb.asm.tree.ClassNode;

public interface ClassVisitor {
    public void visitClass(ClassNode clazz, LogNotifier ln);
    public String toString();
}
