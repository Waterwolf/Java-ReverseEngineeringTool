package RefactorEverything;

import org.objectweb.asm.tree.ClassNode;


public interface FileChangeNotifier {
    public void workedFileSetChanged(ClassContainer cc);
    public void workedFileChanged(String name, ClassNode cn);
}
