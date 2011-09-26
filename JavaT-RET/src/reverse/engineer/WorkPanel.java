package reverse.engineer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import jsyntaxpane.DefaultSyntaxKit;

import org.objectweb.asm.tree.ClassNode;

import decompiler.ClassDecompiler;
import decompiler.MethodDecompiler;
import decompiler.md.BytecodeDecompiler;
import decompiler.md.ClassDecompilerImpl;
import decompiler.md.MethodDecompilerImpl;


public class WorkPanel extends VisibleComponent implements FileDrop.Listener {
    
    FileChangeNotifier fcn;
    ClassContainer cc = null;
    JTabbedPane tabs;
    
    ArrayList<String> workingOn = new ArrayList<String>();
    
    public WorkPanel(final FileChangeNotifier fcn) {
        super("WorkPanel", true, false, true, true);
        
        DefaultSyntaxKit.initKit();
        
        this.tabs = new JTabbedPane();
        this.fcn = fcn;
        
        this.add(tabs);
        
        tabs.addContainerListener(new ContainerListener() {

            @Override
            public void componentAdded(final ContainerEvent e) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void componentRemoved(final ContainerEvent e) {
                final Component c = e.getChild();
                if (c instanceof ClassViewer) {
                    workingOn.remove(((ClassViewer)c).name);
                }
            }

        });
        
        final int width = RefactorMain.size.width;
        final int height = RefactorMain.size.height;
        
        final int myWidth = width - 200;
        final int myHeight = height;
        
        final Dimension mySize = new Dimension(myWidth, myHeight);
        
        this.setVisible(true);
        
        this.setSize(mySize);
        this.setPreferredSize(mySize);
        
        this.setLocation(250, (height - myHeight) / 2);
    
        new FileDrop(this, this);
        
    }

    @Override
    public void filesDropped(final File[] files) {
        if (files.length < 1)
            return;
        final HashMap<String, ClassNode> clazzez = new HashMap<String, ClassNode>();
        for (final File f : files) {
            final String fn = f.getName();
            if (fn.endsWith(".jar")) {
                
                try {
                    JarUtils.put(f, clazzez);
                } catch (final IOException e) {
                    e.printStackTrace();
                }
                
            }
            else if (fn.endsWith(".class")) {
                
                try {
                    final ClassNode cn = JarUtils.getNode(JarUtils.getBytes(new FileInputStream(f)));
                    clazzez.put(cn.name, cn);
                } catch (final FileNotFoundException e) {
                    e.printStackTrace();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
                
            }
        }
        if (clazzez.size() > 0) {
            cc = new ClassContainer(clazzez);
            fcn.workedFileSetChanged(cc);
        }
    }
    
    int tabCount = 0;
    
    public void addWorkingFile(final String name, final ClassNode cn) {
        if (!workingOn.contains(name)) {
            final Component tabComp = new ClassViewer(name, cn);
            tabs.add(tabComp);
            workingOn.add(name);
            final int tabCount = tabs.indexOfComponent(tabComp);
            tabs.setTabComponentAt(tabCount, new ButtonTabComponent(tabs));
        }
    }
    
    @Override
    public void workedFileChanged(final String name, final ClassNode cn) {
        addWorkingFile(name, cn);
    }
    
    public class ClassViewer extends JPanel {
        String name;
        ClassNode cn;
        
        JSplitPane sp;
        
        JEditorPane bytecode = new JEditorPane(), decomp = new JEditorPane();
        
        public ClassViewer(final String name, final ClassNode cn) {
            this.name = name;
            this.cn = cn;
            this.setName(name);
            this.setLayout(new BorderLayout());
            
            final JPanel dcPanel = new JPanel(new BorderLayout());
            final JScrollPane dcScroll = new JScrollPane(decomp);
            dcScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            dcScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            dcPanel.add(dcScroll, BorderLayout.CENTER);
            
            final JPanel bcPanel = new JPanel(new BorderLayout());
            final JScrollPane bcScroll = new JScrollPane(bytecode);
            //bcScroll.setVisible(false);
            bcScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            bcScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            bcScroll.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
                @Override
                public void adjustmentValueChanged(final AdjustmentEvent arg0) {
                    dcScroll.getVerticalScrollBar().setValue(arg0.getValue());
                }
            });
            bcPanel.add(bcScroll, BorderLayout.CENTER);
            
            bytecode.setContentType("text/java");
            decomp.setContentType("text/java");
            
            this.sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dcPanel, bcPanel);
            this.add(sp, BorderLayout.CENTER);
            
           
            
            loadDecompilations();
        }
        
        public void loadDecompilations() {
            final ClassDecompiler decomp = new ClassDecompilerImpl();
            final MethodDecompiler bc_md = new BytecodeDecompiler();
            final MethodDecompiler dc_md = new MethodDecompilerImpl();
            bytecode.setText(decomp.get(bc_md, cn));
            this.decomp.setText(decomp.get(dc_md, cn));
            //this.decomp.setText(decomp.get(dc_md, cn));
        }
    }
}
