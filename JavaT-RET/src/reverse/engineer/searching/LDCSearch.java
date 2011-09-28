package reverse.engineer.searching;

import java.util.Iterator;
import java.util.ListIterator;

import javax.swing.JPanel;
import javax.swing.JTextField;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class LDCSearch implements SearchTypeDetails {
    
    JTextField searchText = new JTextField("Search text");
    
    @Override
    public void putComponents(final JPanel panel) {
        panel.add(searchText);
    }
    @Override
    public void search(final ClassNode node, final SearchResultNotifier srn) {
        final Iterator<MethodNode> methods = node.methods.iterator();
        while (methods.hasNext()) {
            final MethodNode method = methods.next();
            
            final InsnList insnlist = method.instructions;
            final ListIterator<AbstractInsnNode> instructions = insnlist.iterator();
            while (instructions.hasNext()) {
                final AbstractInsnNode insnNode = instructions.next();
                if (insnNode instanceof LdcInsnNode) {
                    final Object ldcObject = ((LdcInsnNode) insnNode).cst;
                    if (ldcObject.equals(searchText.getText())) {
                        srn.notifyOfResult(node, method, insnNode);
                    }
                }
            }
            
        }
    }
}