package reverse.engineer.searching;

import javax.swing.JPanel;

import org.objectweb.asm.tree.ClassNode;

public interface SearchTypeDetails {
    public JPanel getPanel();

    public void search(ClassNode node, SearchResultNotifier srn);
}
