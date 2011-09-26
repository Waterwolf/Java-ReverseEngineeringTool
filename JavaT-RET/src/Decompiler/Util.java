package Decompiler;

import java.lang.reflect.Modifier;

import Decompiler.md.stackComponents.Ldc;
import Decompiler.md.stackComponents.Nameable;

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
