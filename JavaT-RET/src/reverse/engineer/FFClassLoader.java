package reverse.engineer;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public class FFClassLoader extends ClassLoader {
    
    public static ClassContainer cc = null;
    
    @Override
    public Class<?> loadClass(final String name) throws ClassNotFoundException {
        if (cc != null) {
            final ClassNode ccClazz = cc.getClass(name.replace("/", "."));
            
            if (ccClazz != null) {
                final ClassWriter cw = new ClassWriter(0);
                ccClazz.accept(cw);
                
                final byte[] b = cw.toByteArray();
                
                return super.defineClass(name, b, 0, b.length);
            }
        }
        return super.loadClass(name);
    }
}
