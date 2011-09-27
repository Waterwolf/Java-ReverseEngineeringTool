package decompiler.md.external;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.sound.sampled.Line;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import reverse.engineer.Settings;
import reverse.engineer.Settings.JadType;

import de.fernflower.main.decompiler.ConsoleDecompiler;
import decompiler.ClassDecompiler;
import decompiler.MethodDecompiler;

public class JadDecompiler implements ClassDecompiler {

    @Override
    public String get(final MethodDecompiler md, final ClassNode cn) {
        
        if (Settings.jadType == null)
            return "Invalid jadtype";
        
        final File jadBase = new File("lib/jad/");
        
        System.out.println("JadBase: " + jadBase.getAbsolutePath());
        
        final ClassWriter cw = new ClassWriter(0);
        cn.accept(cw);
        
        final File tempClass = new File(jadBase, "tempDecClass.class");
        
        try {
            final FileOutputStream fos = new FileOutputStream(tempClass);
            
            fos.write(cw.toByteArray());
            
            fos.close();
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        final StringBuffer javaSrc = new StringBuffer();
        
        try {
            String execString = null;
            switch (Settings.jadType) {
            case Windows:
                execString = "jad.exe"; // que?
                break;
            case Linux:
                execString = "./jad";
                break;
            }
            final Process p = Runtime.getRuntime().exec(execString + " -p tempDecClass.class", null, jadBase);
            //final int exitCode = p.waitFor();
            final BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            final String nl = System.getProperty("line.separator");
            while ((line = br.readLine()) != null) {
                javaSrc.append(line + nl);
            }
            br.close();
        } catch (final IOException e1) {
            e1.printStackTrace();
        } /*catch (final InterruptedException e) {
            e.printStackTrace();
        }*/
        
        tempClass.delete();

        
        return javaSrc.toString();
    }

}
