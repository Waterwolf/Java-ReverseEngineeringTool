package decompiler;

import java.lang.reflect.Modifier;

import decompiler.md.stackComponents.Ldc;
import decompiler.md.stackComponents.Nameable;

/**
 * 
 *  Utilities for various bytecode -> source actions
 * 
 * @author Waterwolf
 *
 */
public class Util {
    public static String getModifierStrings(final int access) {
        return Modifier.toString(access);
    }
    public static String getValue(final Object o) {
        if (o instanceof Nameable)
            return ((Nameable)o).getCallingName();
        if (o instanceof Ldc)
            return "\"" + ((Ldc)o).o + "\"";
        return o.toString();
    }
}
