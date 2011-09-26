package Commons;

import org.objectweb.asm.tree.AbstractInsnNode;

public interface InsnAnalyzer {
    public boolean accept(AbstractInsnNode node);
}
