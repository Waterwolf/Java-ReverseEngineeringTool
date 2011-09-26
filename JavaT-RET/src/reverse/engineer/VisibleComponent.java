package reverse.engineer;

import javax.swing.JInternalFrame;

import org.objectweb.asm.tree.ClassNode;

public abstract class VisibleComponent extends JInternalFrame implements FileChangeNotifier {

    public VisibleComponent(final String title, final boolean one, final boolean two, final boolean three, final boolean four) {
        super(title, one, two, three, four);
    }
    
    @Override
    public void workedFileSetChanged(final ClassContainer cc) {}
    
    @Override
    public void workedFileChanged(final String name, final ClassNode cn) {}

}
