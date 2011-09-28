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

import org.objectweb.asm.tree.ClassNode;

import reverse.engineer.searching.LDCSearch;
import reverse.engineer.searching.SearchTypeDetails;


public class InsnSearcher extends VisibleComponent {
    
    public static final Dimension size = new Dimension(200, RETMain.size.height);
    
    FileChangeNotifier fcn;
    ClassContainer cc = null;
    
    DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode("Root");
    JTree tree;
    
    SearchType searchType = null;
    
    public InsnSearcher(final FileChangeNotifier fcn) {
        super("InsnSearcher", true, false, true, true);
        
        this.fcn = fcn;
        
        final JPanel optionPanel = new JPanel(new GridLayout(4, 1));
        
        optionPanel.add(new JLabel("Search from all classes"));
        
        final DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (final SearchType st : SearchType.values()) {
            if (searchType == null) {
                searchType = st;
            }
            model.addElement(st);
        }
        
        final JComboBox typeBox = new JComboBox(model);
        final JPanel searchOptPanel = new JPanel();
        
        typeBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent arg0) {
                searchOptPanel.removeAll();
            }
        });
        
        optionPanel.add(typeBox);
        optionPanel.add(searchOptPanel);
        
        final JButton search = new JButton("Search");
        
        search.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                searchType = (SearchType) typeBox.getSelectedItem();
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
        LDC (new LDCSearch());
        
        public final SearchTypeDetails details;
        
        SearchType(final SearchTypeDetails details) {
            this.details = details;
        }
    }
    
}
