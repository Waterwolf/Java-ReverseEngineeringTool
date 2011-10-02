package reverse.engineer.api.builtin;

import java.lang.reflect.Modifier;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

import reverse.engineer.api.ClassVisitor;
import reverse.engineer.api.LogNotifier;

import commons.InstructionSearcher;

public class ZKMStringDecryption implements ClassVisitor {
    public String[] strings = null;
 
    public int[] findKeys(final ClassNode classNode) {
        final int[] keys = new int[5];
        for (final MethodNode mn : (List<MethodNode>) classNode.methods) {
            if (Modifier.isStatic(mn.access) && Modifier.isPrivate(mn.access)) {
                if (mn.desc.equals("([C)Ljava/lang/String;")) {
                    final InsnList insnList = mn.instructions;
                    final InstructionSearcher instructionSearcher = new InstructionSearcher(mn);
                    final AbstractInsnNode abstractInsnNode = instructionSearcher.getNext(Opcodes.TABLESWITCH);
                    if (abstractInsnNode != null) {
                        final TableSwitchInsnNode tableSwitchInsnNode = (TableSwitchInsnNode) abstractInsnNode;
                        final List<LabelNode> labelNodeList = tableSwitchInsnNode.labels;
                        int position = 0;
                        for (final LabelNode labelNode : labelNodeList) {
                            final AbstractInsnNode insnNode = labelNode.getNext();
                            keys[position++] = getValue(insnNode);
                        }
                        final AbstractInsnNode defaultKeynode = tableSwitchInsnNode.dflt.getNext();
                        keys[4] = getValue(defaultKeynode);
                    }
                }
            }
        }
        return keys;
    }
 
    public int findXorKey(final ClassNode classNode) {
        int xor = 0;
        for (final MethodNode mn : (List<MethodNode>) classNode.methods) {
            if (Modifier.isStatic(mn.access) && Modifier.isPrivate(mn.access) && mn.desc.equals("(Ljava/lang/String;)[C")) {
                final InstructionSearcher instructionSearcher = new InstructionSearcher(mn);
                if (instructionSearcher.getNext(Opcodes.IXOR) != null) {
                    instructionSearcher.reset();
                    final IntInsnNode intInsnNode = ((IntInsnNode) instructionSearcher.getNext(Opcodes.BIPUSH));
                    if (intInsnNode != null) {
                        xor = intInsnNode.operand;
                    }
                }
            }
        }
        return xor;
    }
 
    public int getValue(final AbstractInsnNode node) {
        if (node instanceof IntInsnNode)
            return ((IntInsnNode) node).operand;
        else if (node instanceof InsnNode) {
            switch (node.getOpcode()) {
                case Opcodes.ICONST_0:
                    return 0;
                case Opcodes.ICONST_1:
                    return 1;
                case Opcodes.ICONST_2:
                    return 2;
                case Opcodes.ICONST_3:
                    return 3;
                case Opcodes.ICONST_4:
                    return 4;
                case Opcodes.ICONST_5:
                    return 5;
            }
        }
 
        return -1;
    }
 
    public void deob(final ClassNode classNode) {
        
        final int[] keys = findKeys(classNode);
        final int xorKey = findXorKey(classNode);
        FieldInsnNode stringArray = null;
        for (final MethodNode mn : (List<MethodNode>) classNode.methods) {
            if (mn.name.equals("<clinit>")) {
                boolean look = false;
                final InsnList insnList = mn.instructions;
                final AbstractInsnNode[] abstractInsnNodes = insnList.toArray();
                for (final AbstractInsnNode insn : abstractInsnNodes) {
                    if (insn.getOpcode() == Opcodes.ANEWARRAY) {
                        final TypeInsnNode type = (TypeInsnNode) insn;
                        if (type.desc.equals("java/lang/String")) {
                            final AbstractInsnNode amountInsn = insn.getPrevious();
                            final int amount = getValue(amountInsn);
                            if (amount == -1) {
                                continue;
                            }
                            strings = new String[amount];
                            insnList.remove(amountInsn);
                            look = true;
                        }
                    } else if (((insn instanceof IntInsnNode) || (insn instanceof InsnNode)) && look) {
                        final AbstractInsnNode next = insn.getNext();
                        if (!(next instanceof LdcInsnNode)) {
                            insnList.remove(insn);
                            continue;
                        }
                        final LdcInsnNode ldc = (LdcInsnNode) next;
                        strings[getValue(insn)] = charArrayToString(stringToCharArray(ldc.cst.toString(), xorKey), keys);
                    } else if ((insn.getOpcode() == Opcodes.PUTSTATIC) && look) {
                        final FieldInsnNode field = (FieldInsnNode) insn;
                        if (field.desc.equals("[Ljava/lang/String;")) {
                            stringArray = field;
                            insnList.remove(insn);
                            break;
                        }
                    }
                    if (look) {
                        insnList.remove(insn);
                    }
                }
            }
        }
        if (stringArray != null) {
            
            for (final MethodNode mn : (List<MethodNode>) classNode.methods) {
                final InsnList insnList = mn.instructions;
                final AbstractInsnNode[] abstractInsnNodes = insnList.toArray();
                for (int i = 0; i < abstractInsnNodes.length; i++) {
                    if (abstractInsnNodes[i].getOpcode() == Opcodes.GETSTATIC) {
                        final FieldInsnNode field = (FieldInsnNode) abstractInsnNodes[i];
                        if (stringArray != null) {
                            if (stringArray.name.equals(field.name) && stringArray.owner.equals(field.owner)) {
                                final int idx = getValue(abstractInsnNodes[i + 1]);
                                if (idx != -1) {
                                    insnList.set(abstractInsnNodes[i++], new LdcInsnNode(strings[idx]));
                                    insnList.remove(abstractInsnNodes[i++]);
                                    insnList.remove(abstractInsnNodes[i]);
                                }
                            }
                        }
                    }
                }
                mn.instructions = insnList;
            }
            
            FieldNode f = null;
            for (final FieldNode field : (List<FieldNode>) classNode.fields) {
                if (field.name.equals(stringArray.name)) {
                    f = field;
                    break;
                }
            }
            if (f != null) {
                classNode.fields.remove(f);
            }
        }
 
    }
 
    public char[] stringToCharArray(final String string, final int key) {
        final char[] chars = string.toCharArray();
        if (chars.length < 2) {
            chars[0] ^= (char) key;
        }
        return chars;
    }
 
    public String charArrayToString(final char[] array, final int[] keys) {
        for (int i = 0; i < array.length; i++) {
            array[i] ^= (byte) keys[i % 5];
        }
        return new String(array).intern();
    }

    @Override
    public void visitClass(final ClassNode clazz, final LogNotifier ln) {
        deob(clazz);
        ln.log("String deobfuscation succesful for " + clazz.name);
    }
}