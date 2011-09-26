package reverse.engineer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

public class JarUtils {
    public static void put(final File jarFile, final HashMap<String, ClassNode> clazzList) throws IOException {
        final JarInputStream jis = new JarInputStream(new FileInputStream(
                jarFile));
        JarEntry entry;
        while ((entry = jis.getNextJarEntry()) != null) {
            final String name = entry.getName();
            if (!name.endsWith(".class")) {
                jis.closeEntry();
                continue;
            }

            final ClassNode cn = getNode(getBytes(jis));
            
            // System.out.println(cn.name + " loaded");
            clazzList.put(cn.name, cn);

            jis.closeEntry();
        }
        jis.close();

    }
    
    public static byte[] getBytes(final InputStream is) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final byte[] buffer = new byte[1024];
        int a = 0;
        while ((a = is.read(buffer)) != -1) {
            baos.write(buffer, 0, a);
        }
        return baos.toByteArray();
    }
    
    public static ClassNode getNode(final byte[] bytez) {
        final ClassReader cr = new ClassReader(bytez);
        final ClassNode cn = new ClassNode();
        cr.accept(cn, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return cn;
    }
}
