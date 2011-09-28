package reverse.engineer.searching;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public interface SearchResultNotifier {
    public void notifyOfResult(ClassNode clazz, MethodNode method,
            AbstractInsnNode insn);
}
