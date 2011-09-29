package reverse.engineer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import reverse.engineer.ClassContainer.CCVisitor;
import reverse.engineer.searching.LDCSearch;
import reverse.engineer.searching.RegexSearch;
import reverse.engineer.searching.SearchResultNotifier;
import reverse.engineer.searching.SearchTypeDetails;


public class InsnSearcher extends VisibleComponent {
    
    public static final Dimension size = new Dimension(200, RETMain.size.height);
    
    FileChangeNotifier fcn;
    ClassContainer cc = null;
    
    DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode("Root");
    JTree tree;
    
    SearchType searchType = null;
    JComboBox searchRadiusBox;
    
    public InsnSearcher(final FileChangeNotifier fcn) {
        super("InsnSearcher", true, false, true, true);
        
        this.fcn = fcn;
        
        final JPanel optionPanel = new JPanel(new GridLayout(4, 1));
        
        final JPanel searchOpts = new JPanel(new BorderLayout());
        
        searchOpts.add(new JLabel("Search from"), BorderLayout.WEST);
        
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (final SearchRadius st : SearchRadius.values()) {
            model.addElement(st);
        }
        
        searchRadiusBox = new JComboBox(model);
        
        searchOpts.add(searchRadiusBox, BorderLayout.CENTER);
        
        optionPanel.add(searchOpts);
        
        model = new DefaultComboBoxModel();
        for (final SearchType st : SearchType.values()) {
            model.addElement(st);
        }
        
        final JComboBox typeBox = new JComboBox(model);
        final JPanel searchOptPanel = new JPanel();
        
        final ItemListener il = new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent arg0) {
                searchOptPanel.removeAll();
                searchType = (SearchType) typeBox.getSelectedItem();
                searchOptPanel.add(searchType.details.getPanel());

                searchOptPanel.revalidate();
                searchOptPanel.repaint();
            }
        };
        
        typeBox.addItemListener(il);
        
        typeBox.setSelectedItem(SearchType.LDC);
        il.itemStateChanged(null);
        
        optionPanel.add(typeBox);
        optionPanel.add(searchOptPanel);
        
        final JButton search = new JButton("Search");
        
        search.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                treeRoot.removeAllChildren();
                searchType = (SearchType) typeBox.getSelectedItem();
                final SearchRadius radius = (SearchRadius) searchRadiusBox.getSelectedItem();
                final SearchResultNotifier srn = new SearchResultNotifier() {
                    @Override
                    public void notifyOfResult(final ClassNode clazz,
                            final MethodNode method, final AbstractInsnNode insn) {
                        treeRoot.add(new DefaultMutableTreeNode(clazz.name + "." + method.name));
                    }
                };
                if (radius == SearchRadius.AllClasses) { //  srch from all classes
                    if (cc != null) {
                        cc.visit(new CCVisitor() {
                            @Override
                            public void visit(final String name, final ClassNode cn) {
                                
                                searchType.details.search(cn, srn);
                                
                            }
                        });
                    }
                }
                else if (radius == SearchRadius.CurClass) {
                    final ClassViewer cv = RETMain.getComponent(WorkPanel.class).getCurrentClass();
                    if (cv != null) {
                        searchType.details.search(cv.cn, srn);
                    }
                }
                tree.expandPath(new TreePath(tree.getModel().getRoot()));
                tree.repaint();
            }
        });
        
        optionPanel.add(search);
        
        this.tree = new JTree(treeRoot);
        
        this.setLayout(new BorderLayout());
        
        add(optionPanel, BorderLayout.NORTH);
        add(new JScrollPane(tree), BorderLayout.CENTER);
        
        this.tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(final TreeSelectionEvent arg0) {
                final TreePath path = arg0.getPath();
                if (cc == null || ((TreeNode)path.getLastPathComponent()).getChildCount() > 0)
                    return;
                final String clazzName = path.getLastPathComponent().toString();
                final ClassNode fN = cc.getClass(clazzName);
                if (fN != null) {
                    RETMain.getComponent(ClassNavigation.class).switchWorkedFile(clazzName, fN);
                }
            }
        });
        
        this.setVisible(true);
        
    }
    
    @Override
    public void workedFileSetChanged(final ClassContainer cc) {
        treeRoot.removeAllChildren();
        this.cc = cc;
    }

    @Override
    public Dimension getWantedSize() {
        return size;
    }
    
    public enum SearchType {
        LDC (new LDCSearch()),
        Regex (new RegexSearch());
        
        public final SearchTypeDetails details;
        
        SearchType(final SearchTypeDetails details) {
            this.details = details;
        }
    }
    
    public enum SearchRadius {
        AllClasses,
        CurClass;
    }
    
}
