package reverse.engineer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.objectweb.asm.tree.ClassNode;

import reverse.engineer.ClassContainer.CCVisitor;


public class ClassNavigation extends VisibleComponent implements FileDrop.Listener {
    
    public static final Dimension size = new Dimension(200, RETMain.size.height);
    
    FileChangeNotifier fcn;
    ClassContainer cc = null;
    
    DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode("Root");
    MyTree tree;
    
    public ClassNavigation(final FileChangeNotifier fcn) {
        super("ClassNavigation", true, false, true, true);
        
        this.fcn = fcn;
        
        this.tree = new MyTree(treeRoot);
        add(new JScrollPane(tree));
        
        this.tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(final TreeSelectionEvent arg0) {
                final TreePath path = arg0.getPath();
                if (cc == null || ((TreeNode)path.getLastPathComponent()).getChildCount() > 0)
                    return;
                final StringBuffer nameBuffer = new StringBuffer();
                for (int i = 1;i < path.getPathCount(); i++) {
                    nameBuffer.append(path.getPathComponent(i));
                    if (i < path.getPathCount()-1) {
                        nameBuffer.append("/");
                    }
                }
                final ClassNode fN = cc.getClass(nameBuffer.toString());
                if (fN != null) {
                    switchWorkedFile(nameBuffer.toString(), fN);
                }
            }
        });
        
        this.setVisible(true);
        
        new FileDrop(this, this);
        
    }
    
    public void switchWorkedFile(final String name, final ClassNode node) {
        fcn.workedFileChanged(name, node);
        System.out.println("Work file changed");
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
    
    @Override
    public void workedFileSetChanged(final ClassContainer cc) {
        treeRoot.removeAllChildren();
        cc.visit(new CCVisitor() {
            @Override
            public void visit(final String name, final ClassNode cn) {
                final String[] spl = name.split("\\/");
                if (spl.length < 2) {
                    treeRoot.add(new DefaultMutableTreeNode(name));
                }
                else {
                    DefaultMutableTreeNode parent = treeRoot;
                    for (final String s : spl) {
                        DefaultMutableTreeNode child = null;
                        for (int i = 0;i < parent.getChildCount(); i++) {
                            if (((DefaultMutableTreeNode) parent.getChildAt(i)).getUserObject().equals(s)) {
                                child = (DefaultMutableTreeNode) parent.getChildAt(i);
                                break;
                            }
                        }
                        if (child == null) {
                            child = new DefaultMutableTreeNode(s);
                            parent.add(child);
                        }
                        parent = child;
                    }
                }
            }
        });
        tree.expandPath(new TreePath(tree.getModel().getRoot()));
        //expandAll(tree, true);
    }
    
    // If expand is true, expands all nodes in the tree.
    // Otherwise, collapses all nodes in the tree.
    public void expandAll(final JTree tree, final boolean expand) {
        final TreeNode root = (TreeNode) tree.getModel().getRoot();

        // Traverse tree from root
        expandAll(tree, new TreePath(root), expand);
    }

    private void expandAll(final JTree tree, final TreePath parent,
            final boolean expand) {
        // Traverse children
        final TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (final Enumeration e = node.children(); e.hasMoreElements();) {
                final TreeNode n = (TreeNode) e.nextElement();
                final TreePath path = parent.pathByAddingChild(n);
                expandAll(tree, path, expand);
            }
        }

        // Expansion or collapse must be done bottom-up
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }
    
    public class MyTree extends JTree {

        DefaultMutableTreeNode treeRoot;
        
        public MyTree(final DefaultMutableTreeNode treeRoot) {
            super(treeRoot);
            this.treeRoot = treeRoot;
        }
        
        @Override
        public void paint(final Graphics g) {
            super.paint(g);
            if (treeRoot.getChildCount() < 1) {
                g.setColor(new Color(0, 0, 0, 100));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Color.white);
                g.drawString("Drag class/jar here", 10, 100);
            }
        }
        
    }

    @Override
    public Dimension getWantedSize() {
        return size;
    }
    
}
