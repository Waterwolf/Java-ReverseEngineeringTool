package reverse.engineer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jsyntaxpane.DefaultSyntaxKit;

import org.objectweb.asm.tree.ClassNode;


public class WorkPanel extends VisibleComponent implements ActionListener {
   
    public static final Dimension size = new Dimension(950, RETMain.size.height);
    
    FileChangeNotifier fcn;
    JTabbedPane tabs;
    
    JPanel buttonPanel;
    JButton refreshClass;
    
    ArrayList<String> workingOn = new ArrayList<String>();
    
    public static int SyntaxFontHeight = 12;
    
    public WorkPanel(final FileChangeNotifier fcn) {
        super("WorkPanel", true, false, true, true);
        
        if (Settings.SYNTAX_HIGHLIGHT_TYPE != Settings.SyntaxHighlightType.None) {
            DefaultSyntaxKit.initKit();
            Font defFont = null;
            try {
                final Field defFontField = DefaultSyntaxKit.class.getDeclaredField("DEFAULT_FONT");
                defFontField.setAccessible(true);
                defFont = (Font) defFontField.get(null);
            }
            catch (final Exception e) {
                e.printStackTrace();
            }
            SyntaxFontHeight = defFont.getSize();
            System.out.println("Default syntax highlight font " + defFont);
        }
        
        this.tabs = new JTabbedPane();
        this.fcn = fcn;
        
        this.setLayout(new BorderLayout());
        
        this.add(tabs, BorderLayout.CENTER);
        
        buttonPanel = new JPanel(new FlowLayout());
        
        refreshClass = new JButton("Refresh class");
        refreshClass.addActionListener(this);
        
        buttonPanel.add(refreshClass);
        
        buttonPanel.setVisible(false);
        this.add(buttonPanel, BorderLayout.SOUTH);
        
        tabs.addContainerListener(new ContainerListener() {

            @Override
            public void componentAdded(final ContainerEvent e) {
            }

            @Override
            public void componentRemoved(final ContainerEvent e) {
                final Component c = e.getChild();
                if (c instanceof ClassViewer) {
                    workingOn.remove(((ClassViewer)c).name);
                }
            }

        });
        tabs.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent arg0) {
                buttonPanel.setVisible(tabs.getSelectedIndex() != -1);
            }
        });
        
        this.setVisible(true);
        
    }
    
    int tabCount = 0;
    
    public void addWorkingFile(final String name, final ClassNode cn) {
        if (!workingOn.contains(name)) {
            final Component tabComp = new ClassViewer(name, cn);
            tabs.add(tabComp);
            workingOn.add(name);
            final int tabCount = tabs.indexOfComponent(tabComp);
            tabs.setTabComponentAt(tabCount, new ButtonTabComponent(tabs));
            tabs.setSelectedIndex(tabCount);
        }
    }
    
    // TODO
    public void openClass(final String clazz, final String method) {
        if (workingOn.contains(clazz)) {
            
        }
    }
    
    @Override
    public void workedFileChanged(final String name, final ClassNode cn) {
        addWorkingFile(name, cn);
    }
    
    public ClassViewer getCurrentClass() {
        return (ClassViewer) tabs.getSelectedComponent();
    }
    
    @Override
    public void actionPerformed(final ActionEvent arg0) {
        final JButton src = (JButton) arg0.getSource();
        if (src == refreshClass) {
            final Component tabComp = tabs.getSelectedComponent();
            if (tabComp != null) {
                ((ClassViewer)tabComp).loadDecompilations();
            }
        }
    }

    @Override
    public Dimension getWantedSize() {
        return size;
    }
}
