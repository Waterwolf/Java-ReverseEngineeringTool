package reverse.engineer.searching;

import java.awt.GridLayout;
import java.util.Iterator;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class RegexSearch implements SearchTypeDetails {
    
    JTextField searchText = new JTextField("");
    JPanel myPanel = null;
    
    private static RegexInsnFinder regexFinder;
    
    @Override
    public JPanel getPanel() {
        if (myPanel == null) {
            myPanel = new JPanel(new GridLayout(1, 2));
            myPanel.add(new JLabel("SearchRegex: "));
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
            
            if (regexFinder == null) {
                regexFinder = new RegexInsnFinder(node, method);
            }
            else {
                regexFinder.setMethod(node, method);
            }
            
            if (regexFinder.find(srchText).length > 0) {
                srn.notifyOfResult(node, method, null);
            }
            
        }
    }
}