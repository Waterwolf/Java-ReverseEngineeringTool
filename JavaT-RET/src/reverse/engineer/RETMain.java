package reverse.engineer;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

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
        
        constructMenu();
        
        this.setVisible(true);
    }
    
    public void constructMenu() {
        
        final JMenuBar menuBar = new JMenuBar();
        
        final JMenu menu = new JMenu("Window");
        
        final JMenuItem menuItem = new JMenuItem("Preferences");
        
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                new SettingsGui();
            }
        });
        
        menu.add(menuItem);
        menuBar.add(menu);
        
        this.setJMenuBar(menuBar);
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
