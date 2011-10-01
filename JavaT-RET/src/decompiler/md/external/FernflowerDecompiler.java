package decompiler.md.external;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import reverse.engineer.FFClassLoader;
import decompiler.ClassDecompiler;
import decompiler.MethodDecompiler;

public class FernflowerDecompiler implements ClassDecompiler {
    
    static FFClassLoader fcl = new FFClassLoader();

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
        
        try {
            final Class<?> consoleDecompiler = fcl.loadClass("de.fernflower.main.decompiler.ConsoleDecompiler");
            final Method mainMethod = consoleDecompiler.getMethod("main", String[].class);
            mainMethod.invoke(null, new Object[] {new String[] {tempClass.getName(), "."}});
        } catch (final ClassNotFoundException e1) {
            e1.printStackTrace();
        } catch (final SecurityException e) {
            e.printStackTrace();
        } catch (final NoSuchMethodException e) {
            e.printStackTrace();
        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
        } catch (final IllegalAccessException e) {
            e.printStackTrace();
        } catch (final InvocationTargetException e) {
            e.printStackTrace();
        }
        
        //ConsoleDecompiler.main(new String[] {tempClass.getName(), "."});
        
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
                e.printStackTrace();
            }
            
            outputJava.delete();
            
            return javaSrc.toString();
            
        }
        
        
        return "FernFlower error!";
    }

}
