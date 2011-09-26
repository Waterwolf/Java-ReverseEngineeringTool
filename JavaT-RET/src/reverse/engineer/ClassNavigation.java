package reverse.engineer;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.objectweb.asm.tree.ClassNode;

import reverse.engineer.ClassContainer.CCVisitor;


public class ClassNavigation extends VisibleComponent implements FileDrop.Listener {
    
    FileChangeNotifier fcn;
    ClassContainer cc = null;
    
    DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode("Root");
    JTree tree;
    
    public ClassNavigation(final FileChangeNotifier fcn) {
        super("ClassNavigation", true, false, true, true);
        
        this.fcn = fcn;
        
        this.tree = new JTree(treeRoot);
        add(tree);
        
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
                    fcn.workedFileChanged(nameBuffer.toString(), fN);
                    System.out.println("Work file changed");
                }
            }
        });
        
        final int height = RETMain.size.height;
        
        final Dimension mySize = new Dimension(240, height);
        
        this.setVisible(true);
        
        this.setSize(mySize);
        this.setPreferredSize(mySize);
        
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
    }
    
}
