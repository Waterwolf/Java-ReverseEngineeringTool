package reverse.engineer.searching;

import java.awt.GridLayout;
import java.util.Iterator;
import java.util.ListIterator;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class LDCSearch implements SearchTypeDetails {
    
    JTextField searchText = new JTextField("");
    JPanel myPanel = null;
    
    @Override
    public JPanel getPanel() {
        if (myPanel == null) {
            myPanel = new JPanel(new GridLayout(1, 2));
            myPanel.add(new JLabel("SearchText: "));
            myPanel.add(searchText);
        }
            
        return myPanel;
    }
    @Override
    public void search(final ClassNode node, final SearchResultNotifier srn) {
        final Iterator<MethodNode> methods = node.methods.iterator();
        final String srchText = searchText.getText();
        while (methods.hasNext()) {
            final MethodNode method = methods.next();
            
            final InsnList insnlist = method.instructions;
            final ListIterator<AbstractInsnNode> instructions = insnlist.iterator();
            while (instructions.hasNext()) {
                final AbstractInsnNode insnNode = instructions.next();
                if (insnNode instanceof LdcInsnNode) {
                    final Object ldcObject = ((LdcInsnNode) insnNode).cst;
                    final String ldcString = ldcObject.toString();
                    if (ldcString.equals(srchText) || ldcString.contains(srchText)) {
                        srn.notifyOfResult(node, method, insnNode);
                    }
                }
            }
            
        }
    }
}