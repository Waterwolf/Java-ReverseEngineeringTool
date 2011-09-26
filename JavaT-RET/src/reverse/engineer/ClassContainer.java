package reverse.engineer;

import java.util.HashMap;
import java.util.Map.Entry;

import org.objectweb.asm.tree.ClassNode;

public class ClassContainer {
    private final HashMap<String, ClassNode> cn;

    public ClassContainer(final HashMap<String, ClassNode> cn) {
        this.cn = cn;
    }
    
    public ClassNode getClass(final String name) {
        return cn.get(name);
    }
    
    public void visit(final CCVisitor ccv) {
        for (final Entry<String, ClassNode> entry : cn.entrySet()) {
            ccv.visit(entry.getKey(), entry.getValue());
        }
    }
    
    public static interface CCVisitor {
        public void visit(String name, ClassNode cn);
    }
}
