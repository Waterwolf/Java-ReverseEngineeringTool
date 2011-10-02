package reverse.engineer.api.builtin.maliciouscodeanalysis.checkers;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import reverse.engineer.api.LogNotifier;

import commons.InstructionSearcher;

public class ClassModFinder implements Checker {

    @Override
    public int check(final MethodNode mn, final InstructionSearcher search, final LogNotifier logger) {
        int found = 0;
        
        AbstractInsnNode node;
        while ((node = search.getNext()) != null) {
            if (node instanceof MethodInsnNode) {
                final MethodInsnNode min = (MethodInsnNode) node;
                if (min.owner.startsWith("java/lang/reflect")) {
                    found++;
                    logger.log("Found MethodCall to " + min.owner + "." + min.name + "(" + min.desc + ")");
                }
            }
        }
        
        return found;
    }

}
