package decompiler.md;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import reverse.engineer.Settings;

import commons.InstructionSearcher;

import decompiler.ClassStringBuffer;
import decompiler.MethodDecompiler;
import decompiler.TypeAndName;


public class BytecodeDecompiler implements MethodDecompiler {
    
    public static String[] opcodeStrings;
    public static String[] typeStrings;
    
    static {
        opcodeStrings = new String[256];
        for (final Field f : Opcodes.class.getFields()) {
            try {
                final Object oo = f.get(null);
                if (oo instanceof Integer) {
                    final int oi = ((Integer)oo);
                    if (oi < 256 && oi >= 0) {
                        opcodeStrings[oi] = f.getName().toLowerCase();
                    }
                }
            } catch (final IllegalArgumentException e) {
                e.printStackTrace();
            } catch (final IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        typeStrings = new String[100];
        for (final Field f : AbstractInsnNode.class.getFields()) {
            if (!(f.getName().endsWith("_INSN"))) {
                continue;
            }
            try {
                final Object oo = f.get(null);
                if (oo instanceof Integer) {
                    final int oi = ((Integer)oo);
                    if (oi < 256 && oi >= 0) {
                        typeStrings[oi] = f.getName().toLowerCase();
                    }
                }
            } catch (final IllegalArgumentException e) {
                e.printStackTrace();
            } catch (final IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void put(final ClassStringBuffer buffer, final TypeAndName[] args, final MethodNode mn, final ClassNode parent) {
        final InstructionSearcher is = new InstructionSearcher(mn);
        AbstractInsnNode next = is.getCurrent();
        int index = 0;
        while (next != null) {
            
            if (next.getOpcode() == -1) {
                buffer.appendnl(index++ + ". -1 !!");
                next = is.getNext();
                continue;
            }
            
            
            buffer.append(index++ + ". " + opcodeStrings[next.getOpcode()] + " ");
            
            if (Settings.SHOWTYPES) {
                buffer.append("//" + typeStrings[next.getType()] + " ");
            }
            
            if (next instanceof FieldInsnNode) {
                final FieldInsnNode fin = (FieldInsnNode) next;
                buffer.append(fin.owner + " " + fin.name + " " + fin.desc);
            }
            else if (next instanceof MethodInsnNode) {
                final MethodInsnNode min = (MethodInsnNode) next;
                buffer.append(min.owner + " " + min.name + " " + min.desc);
            }
            else if (next instanceof VarInsnNode) {
                final VarInsnNode vin = (VarInsnNode) next;
                buffer.append(vin.var);
                if (Settings.HELPERS) {
                    if (vin.var == 0 && !Modifier.isStatic(mn.access)) {
                        buffer.append(" // reference to self");
                    }
                    else {
                        final int refIndex = vin.var - (Modifier.isStatic(mn.access) ? 0 : 1);
                        if (refIndex >= 0 && refIndex < args.length-1) {
                            buffer.append(" // reference to " + args[refIndex].name);
                        }
                    }
                }
            }
            else if (next instanceof IntInsnNode) {
                final IntInsnNode iin = (IntInsnNode) next;
                buffer.append(iin.operand);
            }
            else if (next instanceof JumpInsnNode) {
                final JumpInsnNode jin = (JumpInsnNode) next;
                buffer.append(is.computePosition(jin.label));
                switch (next.getOpcode()) {
                case Opcodes.IF_ICMPLT:
                    buffer.append(" // if val1 less than val2 jump");
                    break;
                }
            }
            else if (next instanceof LdcInsnNode) {
                final LdcInsnNode lin = (LdcInsnNode) next;
                buffer.append("\"" + lin.cst + "\"");
            }
            else if (next instanceof IincInsnNode) {
                final IincInsnNode iin = (IincInsnNode) next;
                buffer.append("var " + iin.var + " by " + iin.incr);
            }
            else {
                /*
                switch (next.getOpcode()) {
                case Opcodes.IF_ICMPLT:
                    buffer.append(" // ");
                    break;
                }
                */
            }
            
            
            buffer.appendnl();
            
            next = is.getNext();
        }
    }

}
