package Decompiler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import Decompiler.md.ClassDecompilerImpl;
import Decompiler.md.MethodDecompilerImpl;

public class DecompilerLoader {
    
    public final static HashMap<String, ClassNode> classEntries = new HashMap<String, ClassNode>();
    
	public static void main(final String[] args) {

	    System.out.println("*** RUNNING Decompiler BY Waterwolf VERSION 0.1 ***");
	    
        try {
            start(args);
        } catch (final IOException e) {
            e.printStackTrace();
        }
		
	}
	
	public static void start(final String[] args) throws IOException {
	       if (args.length != 1) {
	            System.out.println("Please give only the input file as an argument");
	            return;
	        }
	        final File f = new File(args[0]);
	        if (!f.exists()) {
	            System.out.println("Given class file doesn't exist");
	            return;
	        }
	        final String fname = f.getName();
	        if (fname.endsWith(".class")) {
                final InputStream is = new FileInputStream(f);
                
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();

                final byte[] buffer = new byte[1024];
                int off = 0;
                while ((off = is.read(buffer)) > 0) {
                    baos.write(buffer, 0, off);
                }
                
                final ClassReader cr = new ClassReader(baos.toByteArray());
                final ClassNode cn = new ClassNode();
                cr.accept(cn, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                classEntries.put(fname, cn);
	        }
	        else if (fname.endsWith(".jar")) {
	            final JarFile jf = new JarFile(f);
	            final Enumeration<JarEntry> entries = jf.entries();
	            while (entries.hasMoreElements()) {
	                final JarEntry entry = entries.nextElement();
	                final InputStream is = jf.getInputStream(entry);
	                
	                final ByteArrayOutputStream baos = new ByteArrayOutputStream();

	                final byte[] buffer = new byte[1024];
	                int off = 0;
	                while ((off = is.read(buffer)) > 0) {
	                    baos.write(buffer, 0, off);
	                }
	                
	                if (entry.getName().endsWith(".class")) {
	                    final ClassReader cr = new ClassReader(baos.toByteArray());
	                    final ClassNode cn = new ClassNode();
	                    cr.accept(cn, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
	                    classEntries.put(entry.getName(), cn);
	                }

	            }
	        }
	        else {
	            System.err.println("Given file is not jar or class file");
	            return;
	        }
	        
	        
	        try {
	            new DecompilerLoader().startAnalysis();
	        } catch (final IOException e) {
	            e.printStackTrace();
	        }
	}
	
	public void startAnalysis() throws IOException {
		
	    final ClassDecompiler decomp = new ClassDecompilerImpl();
	    //final MethodDecompiler md = new BytecodeDecompiler();
	    final MethodDecompiler md = new MethodDecompilerImpl();
	    
        for (final Entry<String, ClassNode> entry : classEntries.entrySet()) {
            
            final String name = entry.getKey();
            final ClassNode cn = entry.getValue();
            
            System.out.println("Decompiled " + cn.name + ":");
            
            System.out.println(decomp.get(md, cn));
            
        }
		
        
	}
}
