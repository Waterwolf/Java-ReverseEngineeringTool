package decompiler.md;

import java.lang.reflect.Modifier;
import java.util.Stack;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import commons.InstructionSearcher;

import decompiler.ClassStringBuffer;
import decompiler.MethodDecompiler;
import decompiler.TypeAndName;
import decompiler.Util;
import decompiler.md.stackComponents.ANewArray;
import decompiler.md.stackComponents.Ldc;
import decompiler.md.stackComponents.NamedReference;
import decompiler.md.stackComponents.SelfReference;


public class MethodDecompilerImpl implements MethodDecompiler {

    @Override
    public void put(final ClassStringBuffer buffer, final TypeAndName[] args, final MethodNode mn, final ClassNode parent) {
        final InstructionSearcher is = new InstructionSearcher(mn);
        AbstractInsnNode next = is.getCurrent();
        
        int localVarId = 0;
        
        final Object[] localVarStore = new Object[mn.maxLocals];
        
        final Stack<Object> javaStack = new Stack<Object>();
        System.out.println("Going through " + mn.name);
        while (next != null) {
            switch (next.getOpcode()) {
            case Opcodes.ICONST_0:
                javaStack.push(0);
                break;
            case Opcodes.ICONST_1:
                javaStack.push(1);
                break;
            case Opcodes.ICONST_2:
                javaStack.push(2);
                break;
            case Opcodes.ICONST_3:
                javaStack.push(3);
                break;
            case Opcodes.ICONST_4:
                javaStack.push(4);
                break;
            case Opcodes.ICONST_5:
                javaStack.push(5);
                break;
            case Opcodes.BIPUSH:
                javaStack.push(((IntInsnNode)next).operand);
                break;
            case Opcodes.ANEWARRAY:
                final int arraySize = (Integer) javaStack.pop();
                System.out.println("Making new array of size " + arraySize);
                final ANewArray ana = new ANewArray();
                ana.size = arraySize;
                ana.type = (TypeInsnNode) next;
                ana.localArrayName = "localArray" + localVarId++;
                
                final String typeName = Type.getObjectType(ana.type.desc).getClassName();
                
                javaStack.push(ana);
                buffer.appendnl(typeName + "[] = new " + typeName + "[" + ana.size + "];");
                break;
            case Opcodes.DUP:
                final Object o = javaStack.pop();
                javaStack.push(o);
                javaStack.push(o);
                break;
            case Opcodes.LDC:
                javaStack.push(new Ldc(((LdcInsnNode)next).cst));
                break;
            case Opcodes.AASTORE:
                final Object value = javaStack.pop();
                final int index = (Integer) javaStack.pop();
                final Object array = javaStack.pop();
                if (array instanceof ANewArray) {
                    final ANewArray ana2 = (ANewArray) array;
                    buffer.appendnl(ana2.localArrayName + "[" + index + "] = " + Util.getValue(value) + ";");
                }
                break;
            case Opcodes.PUTSTATIC:
                final FieldInsnNode fin = (FieldInsnNode) next;
                buffer.appendnl(fin.owner.replace('/', '.') + "." + fin.name + " = " + Util.getValue(javaStack.pop()) + ";");
                break;
            case Opcodes.GETSTATIC:
                final FieldInsnNode fin2 = (FieldInsnNode) next;
                javaStack.push(new NamedReference(fin2.owner.replace('/', '.') + "." + fin2.name));
                //buffer.appendnl(fin2.owner.replace('/', '.') + "." + fin2.name);
                break;
            case Opcodes.INVOKESPECIAL:
                final MethodInsnNode min = (MethodInsnNode) next;
                final Type[] methodArgs = Type.getArgumentTypes(min.desc);
                final Object[] argVals = new Object[methodArgs.length];
                for (int i = 0;i < argVals.length; i++) {
                    argVals[i] = javaStack.pop();
                }
                
                final Object reference = javaStack.pop();
                
                if (reference instanceof SelfReference) {
                    buffer.append("super");
                }
                else {
                    buffer.append(Util.getValue(reference));
                }
                
                if (min.name.equals("<init>")) {
                    buffer.append("(");
                }
                else {
                    buffer.append("." + min.name + "(");
                }
                
                for (int i = 0;i < argVals.length; i++) {
                    final Object obj = argVals[i];
                    buffer.append(Util.getValue(obj) + (i < argVals.length-1 ? ", " : ""));
                }
                
                buffer.appendnl(");");
                //buffer.appendnl(min.owner.replace('/', '.') + "." + fin.name + " = " + Util.getValue(javaStack.pop()) + ";");
                break;
            case Opcodes.INVOKEVIRTUAL:
                final MethodInsnNode min2 = (MethodInsnNode) next;
                final Type[] methodArgs2 = Type.getArgumentTypes(min2.desc);
                final Object[] argVals2 = new Object[methodArgs2.length];
                for (int i = 0;i < argVals2.length; i++) {
                    argVals2[i] = javaStack.pop();
                }
                
                final Object reference2 = javaStack.pop();
                
                buffer.append(Util.getValue(reference2) + "." + min2.name + "(");
                
                for (int i = 0;i < argVals2.length; i++) {
                    final Object obj = argVals2[i];
                    buffer.append(Util.getValue(obj) + (i < argVals2.length-1 ? ", " : ""));
                }
                
                buffer.appendnl(");");
                break;
            case Opcodes.ALOAD:
            case Opcodes.ILOAD:
            case Opcodes.FLOAD:
            case Opcodes.DLOAD:
                final int var = ((VarInsnNode)next).var;
                if (next.getOpcode() == Opcodes.ALOAD && var == 0 && !Modifier.isStatic(mn.access)) {
                    javaStack.push(new SelfReference());
                    break;
                }
                final int refIndex = var - (Modifier.isStatic(mn.access) ? 0 : 1);
                if (refIndex >= 0 && refIndex < args.length-1) {
                    javaStack.push(new NamedReference(args[refIndex].name));
                    break;
                }
                
                javaStack.push(new NamedReference(Util.getValue(localVarStore[var])));
                break;
            case Opcodes.ASTORE:
            case Opcodes.ISTORE:
            case Opcodes.FSTORE:
            case Opcodes.DSTORE:
                final int vars = ((VarInsnNode)next).var;
                
                localVarStore[vars] = javaStack.pop();
                
                break;
            case Opcodes.IADD:
                final String lvarName = "addResult" + localVarId++;
                buffer.appendnl("int " + lvarName + " = " + Util.getValue(javaStack.pop()) + " + " + Util.getValue(javaStack.pop()) + ";");
                javaStack.push(new NamedReference(lvarName));
                break;
            case Opcodes.RETURN:
                buffer.appendnl("return;");
                break;
            case Opcodes.LRETURN:
            case Opcodes.FRETURN:
            case Opcodes.DRETURN:
            case Opcodes.IRETURN:
            case Opcodes.ARETURN:
                buffer.appendnl("return " + Util.getValue(javaStack.pop()) + ";");
                break;
            case Opcodes.IF_ICMPLT:
                final AbstractInsnNode nnext = ((JumpInsnNode)next).label.getNext();
                System.out.println("linfo " + next);
                break;
            }
            next = next.getNext();
        }
    }

}
