package reverse.engineer;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.ToolTipManager;

import org.objectweb.asm.tree.ClassNode;

public class RETMain extends JFrame implements FileChangeNotifier {
    
    public static final Dimension size = new Dimension(1200, 600);
    static ArrayList<VisibleComponent> rfComps = new ArrayList<VisibleComponent>();
    
    JDesktopPane desktop;
    
    public void init() {
        
        this.setTitle("Java-ReverseEngineeringTool");
        
        final long tooltipMs = Settings.TOOLTIP_APPEAR_TIME;
        
        if (tooltipMs != -1) {
            ToolTipManager.sharedInstance().setInitialDelay((int) tooltipMs);
        }
        else {
            ToolTipManager.sharedInstance().setEnabled(false);
        }
        
        desktop = new JDesktopPane();
        this.add(desktop);
        
        final Point compLoc = new Point(0, 0);
        
        rfComps.add(new ClassNavigation(this));
        rfComps.add(new WorkPanel(this));
        rfComps.add(new InsnSearcher(this));
        
        int highestY = 0;
        
        final Dimension usedScreenSize = Settings.START_FULLSCREEN ? Toolkit.getDefaultToolkit().getScreenSize() : size;
        
        System.out.println("Used screen size: " + usedScreenSize);
        
        for (final VisibleComponent vc : rfComps) {
            final Dimension ws = vc.getWantedSize();
            
            vc.setSize(ws);
            vc.setPreferredSize(ws);
            vc.setLocation(compLoc);
            
            highestY = Math.max(highestY, ws.height);
            
            if (compLoc.x+ws.width > usedScreenSize.width) {
                compLoc.x = 0;
                compLoc.y = highestY;
                highestY = 0;
            }
            else {
                compLoc.x += ws.width;
            }
            
            desktop.add(vc);
        }
        
        this.setSize(size);
        this.setPreferredSize(size);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        if (Settings.START_FULLSCREEN) {
            this.setExtendedState(MAXIMIZED_BOTH);
        }
        
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
                java.awt.EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (SettingsGui.visibleInstance == null) {
                            new SettingsGui();
                        }
                        else {

                            SettingsGui.visibleInstance.toFront();
                            //SettingsGui.visibleInstance.repaint();
                        }
                    }
                });
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
    
    public static <T> T getComponent(final Class<T> clazz) {
        for (final VisibleComponent vc : rfComps) {
            if (vc.getClass() == clazz)
                return (T) vc;
        }
        return null;
    }
}
