package reverse.engineer;

import java.awt.BorderLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.objectweb.asm.tree.ClassNode;

import decompiler.ClassDecompiler;
import decompiler.MethodDecompiler;
import decompiler.md.BytecodeDecompiler;
import decompiler.md.ClassDecompilerImpl;
import decompiler.md.MethodDecompilerImpl;
import decompiler.md.external.FernflowerDecompiler;
import decompiler.md.external.JadDecompiler;

public class ClassViewer extends JPanel implements Runnable {
    
    private static final ExecutorService decompilerThreadPool  = Executors.newCachedThreadPool();
    
    HashMap<MethodData, LineNumberPair> lnData = new HashMap<MethodData, LineNumberPair>();
    
    String name;
    ClassNode cn;
    
    JSplitPane sp;
    
    JEditorPane bytecode = new JEditorPane(), decomp = new JEditorPane();
    
    JScrollPane bcScroll;
    
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
        bcScroll = new JScrollPane(bytecode);
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
        
        if (Settings.SYNTAX_HIGHLIGHT_TYPE == Settings.SyntaxHighlightType.All) {
            bytecode.setContentType("text/java");
            decomp.setContentType("text/java");
        } else if (Settings.SYNTAX_HIGHLIGHT_TYPE == Settings.SyntaxHighlightType.Decompilation) {
            decomp.setContentType("text/java");
        } else if (Settings.SYNTAX_HIGHLIGHT_TYPE == Settings.SyntaxHighlightType.Bytecode) {
            bytecode.setContentType("text/java");
        }
        
        this.sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dcPanel, bcPanel);
        this.add(sp, BorderLayout.CENTER);
        
        sp.setResizeWeight(0.5);
        
        bytecode.setText("Working..");
        decomp.setText("Working..");
        
        //decompilerThreadPool.submit(this);
        run();
        
    }
    
    public void loadDecompilations() {
        final ClassDecompiler decomp = new ClassDecompilerImpl();
        final MethodDecompiler bc_md = new BytecodeDecompiler();
        
        bytecode.setText(decomp.get(bc_md, cn));
        
        switch (Settings.USED_DECOMPILER) {
        case Fernflower:
            final ClassDecompiler ff_dc = new FernflowerDecompiler();
            this.decomp.setText(ff_dc.get(null, cn));
            break;
        case Jad:
            
            this.decomp.setText(new JadDecompiler().get(null, cn));
            
            break;
        case Own:
        default:
            final MethodDecompiler dc_md = new MethodDecompilerImpl();
            this.decomp.setText(decomp.get(dc_md, cn));
            break;
        }
        
        this.decomp.setCaretPosition(0);
        
    }

    @Override
    public void run() {
        loadDecompilations();
    }
    
    public class LineNumberPair {
        public int src, bytecode;
    }
    
    public class MethodData {
        public String name, desc;
        @Override
        public boolean equals(final Object o) {
            return equals((MethodData)o);
        }
        public boolean equals(final MethodData md) {
            return this.name.equals(md.name) && this.desc.equals(md.desc);
        }
    }
}