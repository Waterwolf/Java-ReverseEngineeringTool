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

// NEVER USE PRIMTIVES IN THIS CLASS thanks :P
public class Settings {
    
    public static final File propertiesFile = new File(".jretProperties");
    public static JadType jadType = null;
    
    // Main
    public static Boolean START_FULLSCREEN = true;
    
    @Changeable(name = "Tooltip appearing time", shortName = "tooltipTime", desc = "Time before tooltips appear in milliseconds, -1 to disable tooltips", reqRestart = true)
    public static Integer TOOLTIP_APPEAR_TIME = 500;
    
    // BytecodeDecompiler
    @Changeable(name = "Bytecode helpers", shortName = "bytecodeHelpers", desc = "Show bytecode instruction explanations")
    public static Boolean HELPERS = true;
    
    @Changeable(name = "Bytecode instruction types", shortName = "bytecodeInsnTypes", desc = "Show bytecode instruction types after bytecodes in bytecode view")
    public static Boolean SHOWTYPES = false;
    
    // WorkPanel
    @Changeable(name = "Used decompiler", shortName = "usedDecompile", desc = "Select decompiler to use with decompiler window", reqRestart = true)
    public static Decompiler USED_DECOMPILER = null;
    
    @Changeable(name = "Highlight syntax", shortName = "highlightSyntax", desc = "Select syntaxes to highlight. More syntaxes make code easier to read but also slow down the program.", reqRestart = true)
    public static SyntaxHighlightType SYNTAX_HIGHLIGHT_TYPE = SyntaxHighlightType.Decompilation;
    
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
                            else if (fType == Integer.class) {
                                f.set(null, Integer.valueOf(stringProp));
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
        
        final String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            jadType = JadType.Windows;
        } else if (osName.contains("linux")) {
            jadType = JadType.Linux;
        }
        
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
                
                if (jadType == null) {
                    USED_DECOMPILER = Decompiler.Own;
                }
                else {
                    USED_DECOMPILER = Decompiler.Jad;
                }
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
    
    public enum Decompiler implements UseabilityCheck {
        Fernflower (fernflowerCheck()),
        Jad (jadCheck()),
        Own (true);
        
        private final boolean isUsable;
        
        Decompiler(final boolean usable) {
            this.isUsable = usable;
        }
        
        private static boolean fernflowerCheck() {
            try {
                Class.forName("de.fernflower.main.decompiler.ConsoleDecompiler");
                return true;
            }
            catch (final Exception e) {
                return false;
            }
        }
        
        private static boolean jadCheck() {
            return jadType != null;
        }

        @Override
        public boolean isUseable() {
            return isUsable;
        }
    }
    
    public static interface UseabilityCheck {
        public boolean isUseable();
    }
    
    public enum JadType {
        Windows, Linux
    }
    
    public enum SyntaxHighlightType {
        None, Bytecode, Decompilation, All
    }
}
