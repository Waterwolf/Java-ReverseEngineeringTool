package decompiler.md.stackComponents;

import org.objectweb.asm.tree.TypeInsnNode;

public class ANewArray implements Nameable {
    public TypeInsnNode type;
    public int size;
    public String localArrayName = null;
    @Override
    public String getCallingName() {
        return localArrayName;
    }
}
