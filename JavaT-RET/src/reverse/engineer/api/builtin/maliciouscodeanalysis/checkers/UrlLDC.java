package reverse.engineer.api.builtin.maliciouscodeanalysis.checkers;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import reverse.engineer.api.LogNotifier;

import commons.InstructionSearcher;

public class UrlLDC implements Checker {

    @Override
    public int check(final MethodNode mn, final InstructionSearcher search, final LogNotifier logger) {
        int found = 0;
        
        AbstractInsnNode node;
        while ((node = search.getNext()) != null) {
            if (node instanceof LdcInsnNode) {
                final Object o = ((LdcInsnNode)node).cst;
                if (o.toString().contains("www.") || o.toString().matches("\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b")) {
                    logger.log("Found LDC Call to " + o);
                    found++;
                }
            }
            else if (node instanceof MethodInsnNode) {
                final MethodInsnNode min = (MethodInsnNode) node;
                if (min.owner.startsWith("java/net")) {
                    found++;
                    logger.log("Found MethodCall to " + min.owner + "." + min.name + "(" + min.desc + ")");
                }
            }
        }
        
        return found;
    }

}
