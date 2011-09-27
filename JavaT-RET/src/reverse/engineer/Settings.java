package reverse.engineer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

import reverse.engineer.WorkPanel.Decompiler;

// NEVER USE PRIMTIVES IN THIS CLASS thanks :P
public class Settings {
    
    public static final File propertiesFile = new File(".jretProperties");

    // BytecodeDecompiler
    @Changeable(name = "Bytecode helpers", shortName = "bytecodeHelpers")
    public static Boolean HELPERS = true;
    
    @Changeable(name = "Bytecode instruction types", shortName = "bytecodeInsnTypes")
    public static Boolean SHOWTYPES = false;
    
    // WorkPanel
    @Changeable(name = "Used decompiler", shortName = "usedDecompile", reqRestart = true)
    public static Decompiler USED_DECOMPILER = null;
    
    @Changeable(name = "Highlight syntax", shortName = "highlightSyntax", reqRestart = true)
    public static Boolean FANCY_VIEWER = true;
    
    public static void loadProps() throws IOException {
        if (propertiesFile.exists()) {
            final Properties props = new Properties();
            final FileInputStream in = new FileInputStream(propertiesFile);
            props.load(in);
            in.close();

            for (final Field f : Settings.class.getFields()) {
                if (f.isAnnotationPresent(Changeable.class)) {

                    final String shortName = f.getAnnotation(Changeable.class)
                            .shortName();

                    if (props.containsKey(shortName)) {
                        final String stringProp = props.get(shortName).toString();
                        
                        final Class<?> fType = f.getType();
                        
                        try {
                            if (fType == Boolean.class) {
                                f.set(null, Boolean.valueOf(stringProp));
                            }
                            else if (Enum.class.isAssignableFrom(fType)) {
                                
                                try {
                                    final Method valueOfMet = fType.getMethod("valueOf", String.class);
                                    final Object o = valueOfMet.invoke(null, stringProp);
                                    if (o != null) {
                                        f.set(null, o);
                                    }
                                } catch (final SecurityException e) {
                                    e.printStackTrace();
                                } catch (final NoSuchMethodException e) {
                                    e.printStackTrace();
                                } catch (final InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                                
                                
                            }
                        } catch (final IllegalArgumentException e) {
                            e.printStackTrace();
                        } catch (final IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    
                }
            }
        }
    }
    
    public static void storeProps() throws IOException {
        final Properties props = new Properties();
        if (!propertiesFile.exists()) {
            propertiesFile.createNewFile();
        }
        for (final Field f : Settings.class.getFields()) {
            if (f.isAnnotationPresent(Changeable.class)) {
                final String shortName = f.getAnnotation(Changeable.class)
                .shortName();
                try {
                    props.setProperty(shortName, f.get(null).toString());
                } catch (final IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (final IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        final FileOutputStream fos = new FileOutputStream(propertiesFile);
        props.store(fos, "whatsup");
        fos.close();
    }
    
    static {
        
        // TODO load settings file
        
        try {
            loadProps();
        } catch (final IOException e1) {
            e1.printStackTrace();
        }
        
        if (USED_DECOMPILER == null) {
            try {
                Class.forName("de.fernflower.main.decompiler.ConsoleDecompiler");
                USED_DECOMPILER = Decompiler.Fernflower;
            }
            catch (final Exception e) {
                USED_DECOMPILER = Decompiler.Own;
            }
        }
        
        System.out.println("Using " + USED_DECOMPILER.name() + " for decompiling");
    }
 
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Changeable {
        public String name();
        public String shortName();
        public String desc() default "";
        public boolean reqRestart() default false;
    }
}
