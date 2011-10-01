package reverse.engineer.api;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.objectweb.asm.tree.ClassNode;

import reverse.engineer.ClassContainer;
import reverse.engineer.ClassContainer.CCVisitor;
import reverse.engineer.FileChangeNotifier;
import reverse.engineer.VisibleComponent;
import reverse.engineer.api.test.ZKMStringDecryption;


public class APIPanel extends VisibleComponent implements ActionListener, LogNotifier {
   
    public static final Dimension size = new Dimension(900, 70);

    private static final ExecutorService pluginRunner = Executors.newFixedThreadPool(1);
    
    private static final Object[] defaultPlugins = new Object[] {
        new ZKMStringDecryption()
    };
    
    ClassContainer cc;
    LogView logView;
    
    DefaultComboBoxModel pluginSelModel;
    JComboBox pluginSelector;
    JButton openPlugin, runPlugin;
    JLabel pluginStatus;
    
    public APIPanel(final FileChangeNotifier fcn) {
        super("APIPanel", true, false, true, true);
        
        this.logView = new LogView();
        
        final JPanel buttons = new JPanel();
        
        pluginSelModel = new DefaultComboBoxModel();
        
        for (final Object o : defaultPlugins) {
            pluginSelModel.addElement(new PluginContainer(o));
        }
        
        pluginSelector = new JComboBox(pluginSelModel);
        
        openPlugin = new JButton("Open plugin");
        runPlugin = new JButton("Run plugin");
        
        runPlugin.addActionListener(this);
        openPlugin.addActionListener(this);
        
        buttons.add(pluginSelector);
        buttons.add(openPlugin);
        buttons.add(runPlugin);
        
        pluginStatus = new JLabel("-");
        
        this.setLayout(new BorderLayout());
        this.add(buttons, BorderLayout.WEST);
        this.add(pluginStatus, BorderLayout.CENTER);
    }
    
    File lastRanFile = null;
    
    @Override
    public void actionPerformed(final ActionEvent arg0) {
        final JButton src = (JButton) arg0.getSource();
        if (src == openPlugin) {
            
            final JFileChooser fc = new JFileChooser();
            fc.setAcceptAllFileFilterUsed(false);
            fc.setFileFilter(new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(final File f) {
                    return f.isDirectory() || f.getName().endsWith(".jar");
                }
                @Override
                public String getDescription() {
                    return "Jar that should contain ffclazz";
                }
            });
            fc.setCurrentDirectory(new File("."));
            
            final int returnVal = fc.showOpenDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                
                try {
                    final File file = fc.getSelectedFile();
                    final URLClassLoader classLoader = new URLClassLoader(
                            new URL[] {
                                    new URL("file:" + file.getAbsolutePath())
                                    });
                    final JarFile jarFile = new JarFile(file);
                    final Enumeration<JarEntry> contents = jarFile.entries();

                    while (contents.hasMoreElements()) {
                        final JarEntry content = contents.nextElement();

                        if (content.getName().endsWith(".class")) {
                            final Class<?> clazz = classLoader.loadClass(content
                                    .getName().replace(".class", "")
                                    .replace("/", "."));
                            
                            if (ClassVisitor.class.isAssignableFrom(clazz)) {
                                final ClassVisitor instance = clazz.asSubclass(ClassVisitor.class).newInstance();
                                pluginSelModel.addElement(new PluginContainer(instance));
                            }
                            
                        }
                    }
                }
                catch (final Exception e) {
                    e.printStackTrace();
                }
                
            }
            
        }
        else if (src == runPlugin) {
            runPlugin();
        }
        
        
    }
    
    public void runPlugin() {
        if (cc == null) {
            this.log("CC Null. Please load a jar before running a plugin");
            return;
        }
        final Object o = pluginSelector.getSelectedItem();
        if (o == null) {
            this.log("Please select a plugin");
            return;
        }
        
        logView.setVisible(true);
         
        pluginRunner.submit(new Runnable() {
            @Override
            public void run() {
                final PluginContainer pc = (PluginContainer) o;
                final Object plugin = pc.plugin;
                if (plugin instanceof ClassVisitor) {
                    final ClassVisitor cv = (ClassVisitor) plugin;
                    cc.visit(new CCVisitor() {
                        @Override
                        public void visit(final String name, final ClassNode cn) {
                            cv.visitClass(cn, APIPanel.this);
                        }
                    });
                }
            }
        });
    }

    @Override
    public Dimension getWantedSize() {
        return size;
    }
    
    @Override
    public void workedFileSetChanged(final ClassContainer cc) {
        this.cc = cc;
    }

    @Override
    public void log(final String s) {
        logView.log(s);
        pluginStatus.setText(s);
        //System.out.println(s);
    }
    
    public class PluginContainer {
        public Object plugin;
        public PluginContainer(final Object plugin) {
            this.plugin = plugin;
        }
        @Override
        public String toString() {
            return plugin.getClass().getSimpleName();
        }
    }
    
    public class LogView extends JFrame {
        
        DefaultListModel model;
        JList list;
        JScrollPane listScroll;
        Dimension size = new Dimension(600, 400);
        
        public LogView() {
            this.setAlwaysOnTop(true);
            this.setDefaultCloseOperation(HIDE_ON_CLOSE);
            
            this.setLayout(new BorderLayout());
            
            this.model = new DefaultListModel();
            this.list= new JList(model);
            
            this.setSize(size);
            this.setPreferredSize(size);
            
            this.add(listScroll = new JScrollPane(list), BorderLayout.CENTER);
        }
        
        public void log(final Object s) {
            model.addElement(s);
            list.ensureIndexIsVisible(model.size()-1);
        }
        
        @Override
        public void setVisible(final boolean b) {
            if (!this.isVisible() && b) {
                model.clear();
            }
            super.setVisible(b);
        }

    }
}
