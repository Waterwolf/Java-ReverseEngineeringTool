package decompiler.md.external;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import javax.sound.sampled.Line;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import de.fernflower.main.decompiler.ConsoleDecompiler;
import decompiler.ClassDecompiler;
import decompiler.MethodDecompiler;

public class FernflowerDecompiler implements ClassDecompiler {

    @Override
    public String get(final MethodDecompiler md, final ClassNode cn) {
        
        final ClassWriter cw = new ClassWriter(0);
        cn.accept(cw);
        
        final File tempClass = new File("tempDecClass.class");
        
        try {
            final FileOutputStream fos = new FileOutputStream(tempClass);
            
            fos.write(cw.toByteArray());
            
            fos.close();
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        ConsoleDecompiler.main(new String[] {tempClass.getName(), "."});
        
        tempClass.delete();
        
        final File outputJava = new File("tempDecClass.java");
        if (outputJava.exists()) {
            
            final String nl = System.getProperty("line.separator");
            final StringBuffer javaSrc = new StringBuffer();
            
            try {
                final BufferedReader br = new BufferedReader(new FileReader(outputJava));
                String line;
                while ((line = br.readLine()) != null) {
                    javaSrc.append(line + nl);
                }
                br.close();
            } catch (final IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            outputJava.delete();
            
            return javaSrc.toString();
            
        }
        
        
        return "FernFlower error!";
    }

}
