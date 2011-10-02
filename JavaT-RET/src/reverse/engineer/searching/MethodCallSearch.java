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
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class MethodCallSearch implements SearchTypeDetails {
    
    JTextField mOwner = new JTextField(""), mName = new JTextField(""), mDesc = new JTextField("");
    JPanel myPanel = null;
    
    @Override
    public JPanel getPanel() {
        if (myPanel == null) {
            myPanel = new JPanel(new GridLayout(3, 2));
            myPanel.add(new JLabel("Owner: "));
            myPanel.add(mOwner);
            myPanel.add(new JLabel("Name: "));
            myPanel.add(mName);
            myPanel.add(new JLabel("Desc: "));
            myPanel.add(mDesc);
        }
            
        return myPanel;
    }
    @Override
    public void search(final ClassNode node, final SearchResultNotifier srn) {
        final Iterator<MethodNode> methods = node.methods.iterator();
        String owner = mOwner.getText();
        if (owner.isEmpty()) {
            owner = null;
        }
        String name = mName.getText();
        if (name.isEmpty()) {
            name = null;
        }
        String desc = mDesc.getText();
        if (desc.isEmpty()) {
            desc = null;
        }
        System.out.println("Searching for min calls with " + owner + " " + name + " " + desc);
        while (methods.hasNext()) {
            final MethodNode method = methods.next();
            
            final InsnList insnlist = method.instructions;
            final ListIterator<AbstractInsnNode> instructions = insnlist.iterator();
            while (instructions.hasNext()) {
                final AbstractInsnNode insnNode = instructions.next();
                if (insnNode instanceof MethodInsnNode) {
                    final MethodInsnNode min = (MethodInsnNode) insnNode;
                    if (name != null && !name.equals(min.name)) {
                        continue;
                    }
                    if (owner != null && !owner.equals(min.owner)) {
                        continue;
                    }
                    if (desc != null && !desc.equals(min.desc)) {
                        continue;
                    }
                    srn.notifyOfResult(node, method, insnNode);
                }
            }
            
        }
    }
}