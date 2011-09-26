package decompiler.md;

import java.util.Iterator;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import decompiler.ClassDecompiler;
import decompiler.ClassStringBuffer;
import decompiler.MethodDecompiler;
import decompiler.TypeAndName;
import decompiler.Util;


public class ClassDecompilerImpl implements ClassDecompiler {

    @Override
    public String get(final MethodDecompiler md, final ClassNode cn) {
        final StringBuffer classBuffer = new StringBuffer();
        final ClassStringBuffer cb = new ClassStringBuffer(classBuffer);
        
        final String cnm = cn.name;
        String package_ = null;
        String class_ = null;
        if (cnm.contains("/")) {
            package_ = cnm.substring(0, cnm.lastIndexOf("/"));
            class_ = cnm.substring(cnm.lastIndexOf("/")+1);
        }
        else {
            class_ = cnm;
        }
        
        if (package_ != null) {
            cb.appendnl("package " + package_ + ";", 2);
        }
        
        cb.append(Util.getModifierStrings(cn.access) + " class " + class_ + " ");
        
        if (cn.superName != null) {
            cb.append("extends " + cn.superName + " ");
        }
        if (cn.interfaces.size() > 0) {
            cb.append("implements ");
            final Iterator<String> sit = cn.interfaces.iterator();
            while (sit.hasNext()) {
                final String s = sit.next();
                cb.append(s);
                if (sit.hasNext()) {
                    cb.append(", ");
                } else {
                    cb.append(" ");
                }
            }
        }
        
        cb.appendnl("{");
        cb.increase();
        cb.appendnl();
        
        final Iterator<FieldNode> fni = cn.fields.iterator();
        
        while (fni.hasNext()) {
            final FieldNode fn = fni.next();
            
            cb.appendnl(Util.getModifierStrings(fn.access) + " " + Type.getType(fn.desc).getClassName() + " " + fn.name + ";");
            
        }
        
        cb.appendnl();
        
        final Iterator<MethodNode> mni = cn.methods.iterator();
        while (mni.hasNext()) {
            final MethodNode mn = mni.next();
            final String mnm = mn.name;
            
            if (!mnm.equals("<clinit>")) {
                cb.append(Util.getModifierStrings(mn.access) + " ");
            }
            
            if (mnm.equals("<init>")) {
                cb.append(class_);
            }
            else if (mnm.equals("<clinit>")) {
                cb.appendnl("static {");
            }
            else {
                cb.append(Type.getReturnType(mn.desc).getClassName() + " ");
                cb.append(mnm);
            }
            
            TypeAndName[] args = new TypeAndName[0];
            
            if (!mnm.equals("<clinit>")) {
                cb.append("(");
                
                // TODO desc
                final Type[] argTypes = Type.getArgumentTypes(mn.desc);
                args = new TypeAndName[argTypes.length];
                
                for (int i = 0;i < argTypes.length; i++) {
                    final Type type = argTypes[i];
                    
                    final TypeAndName tan = new TypeAndName();
                    final String argName = "arg" + i;
                    
                    tan.name = argName;
                    tan.type = type;
                    
                    args[i] = tan;
                    
                    cb.append(type.getClassName() + " " + argName + (i < argTypes.length-1 ? ", " : ""));
                }
                
                cb.appendnl(") {");
            }
            
            cb.increase();
            
            md.put(cb, args, mn, cn);
            
            cb.decrease();
            cb.appendnl("}");
        }
        
        cb.decrease();
        cb.appendnl("}");
        
        
        return classBuffer.toString();
    }

}
