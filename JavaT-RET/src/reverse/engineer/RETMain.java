package reverse.engineer;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;

import org.objectweb.asm.tree.ClassNode;

public class RETMain extends JFrame implements FileChangeNotifier {
    
    public static final Dimension size = new Dimension(1200, 600);
    ArrayList<VisibleComponent> rfComps = new ArrayList<VisibleComponent>();
    
    JDesktopPane desktop;
    
    public void init() {
        
        desktop = new JDesktopPane();
        this.add(desktop);
        
        rfComps.add(new WorkPanel(this));
        rfComps.add(new ClassNavigation(this));
        
        for (final VisibleComponent vc : rfComps) {
            desktop.add(vc);
        }
        
        this.setSize(size);
        this.setPreferredSize(size);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setExtendedState(MAXIMIZED_BOTH);
        
        this.setVisible(true);
    }
    
    public static void main(final String[] args) {
        new RETMain().init();
    }

    @Override
    public void workedFileSetChanged(final ClassContainer cc) {
        
        for (final VisibleComponent vc : rfComps) {
            vc.workedFileSetChanged(cc);
        }
        
    }

    @Override
    public void workedFileChanged(final String name, final ClassNode cn) {
        for (final VisibleComponent vc : rfComps) {
            vc.workedFileChanged(name, cn);
        }
    }
}
