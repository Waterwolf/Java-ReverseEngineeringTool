package reverse.engineer.searching;

import javax.swing.JPanel;

import org.objectweb.asm.tree.ClassNode;

public interface SearchTypeDetails {
    public void putComponents(JPanel panel);

    public void search(ClassNode node, SearchResultNotifier srn);
}
