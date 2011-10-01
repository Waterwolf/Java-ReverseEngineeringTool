package reverse.engineer;

import java.awt.BorderLayout;
import java.awt.FontMetrics;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.ParagraphView;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import decompiler.ClassDecompiler;
import decompiler.MethodDecompiler;
import decompiler.md.BytecodeDecompiler;
import decompiler.md.ClassDecompilerImpl;
import decompiler.md.MethodDecompilerImpl;
import decompiler.md.external.FernflowerDecompiler;
import decompiler.md.external.JadDecompiler;

public class ClassViewer extends JPanel implements Runnable {

    private static final ExecutorService decompilerThreadPool = Executors
            .newCachedThreadPool();

    ArrayList<MethodData> lnData = new ArrayList<MethodData>();

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
        // bcScroll.setVisible(false);
        bcScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        bcScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        
        bcScroll.getVerticalScrollBar().addAdjustmentListener(
                new AdjustmentListener() {
                    @Override
                    public void adjustmentValueChanged(
                            final AdjustmentEvent arg0) {
                        /*
                        final int curBcLine = arg0.getValue()/22;
                        
                        MethodData curMd = lnData.get(lnData.size()-1);
                        
                        for (final MethodData md : lnData) {
                            if (md.bytecodeLN > curBcLine) {
                                curMd = md;
                                break;
                            }
                        }
                        
                        
                        //System.out.println("We're approximately in method " +curMd.name);
                        //System.out.println("We're in row " + curBcLine);
                        
                        dcScroll.getVerticalScrollBar().setValue(
                                curMd.srcLN * 22);*/
                        dcScroll.getVerticalScrollBar().setValue(arg0.getValue());
                        //System.out.println("Scrolling to " + arg0.getValue());
                    }
                });
        bcPanel.add(bcScroll, BorderLayout.CENTER);

        if (Settings.SYNTAX_HIGHLIGHT_TYPE == Settings.SyntaxHighlightType.All) {
            bytecode.setContentType("text/java");
            decomp.setContentType("text/java");
        } else {
            if (Settings.SYNTAX_HIGHLIGHT_TYPE == Settings.SyntaxHighlightType.Decompilation) {
                decomp.setContentType("text/java");
            } else {
            }
            if (Settings.SYNTAX_HIGHLIGHT_TYPE == Settings.SyntaxHighlightType.Bytecode) {
                bytecode.setContentType("text/java");
            } else {
            }
        }

        this.sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dcPanel, bcPanel);
        this.add(sp, BorderLayout.CENTER);

        sp.setResizeWeight(0.5);

        bytecode.setText("Working..");
        decomp.setText("Working..");

        // decompilerThreadPool.submit(this);
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
        /* TODO
        for (final Object o : cn.methods) {
            final MethodNode method = (MethodNode) o;

            final MethodData md = new MethodData();
            if (method.name.equals("<init>")) {
                md.name = cn.name.substring(cn.name.lastIndexOf("/") + 1);
            } else {
                md.name = method.name;
            }
            md.desc = method.desc;

            final String pattern = md.constructPattern();

            final int bytecodePos = CodeSynchUtil.findLine(pattern,
                    this.bytecode.getText());
            final int srcPos = CodeSynchUtil.findLine(pattern,
                    this.decomp.getText());
            
            md.bytecodeLN = bytecodePos;
            md.srcLN = srcPos;

            lnData.add(md);
            
            System.out.println(pattern + ": " + md.name + " " + md.desc + " : "
                    + bytecodePos + "-" + srcPos);

        }*/

    }

    @Override
    public void run() {
        loadDecompilations();
    }

    public static class MethodData {
        public String name, desc;
        public int srcLN, bytecodeLN;

        @Override
        public boolean equals(final Object o) {
            return equals((MethodData) o);
        }

        public boolean equals(final MethodData md) {
            return this.name.equals(md.name) && this.desc.equals(md.desc);
        }

        public String constructPattern() {
            final StringBuffer pattern = new StringBuffer();
            pattern.append(name + " *\\(");
            final org.objectweb.asm.Type[] types = org.objectweb.asm.Type
                    .getArgumentTypes(desc);
            pattern.append("(.*)");
            for (int i = 0; i < types.length; i++) {
                final Type type = types[i];
                final String clazzName = type.getClassName();
                pattern.append(clazzName.substring(clazzName.lastIndexOf(".") + 1)
                        + "(.*)");
            }
            pattern.append("\\) *\\{");
            return pattern.toString();
        }
    }

    class WrapEditorKit extends StyledEditorKit {
        ViewFactory defaultFactory = new WrapColumnFactory();

        @Override
        public ViewFactory getViewFactory() {
            return defaultFactory;
        }
    }

    class WrapColumnFactory implements ViewFactory {
        public View create(final Element elem) {
            final String kind = elem.getName();
            if (kind != null) {
                if (kind.equals(AbstractDocument.ParagraphElementName))
                    return new NoWrapParagraphView(elem);
                else if (kind.equals(AbstractDocument.SectionElementName))
                    return new BoxView(elem, View.Y_AXIS);
                else if (kind.equals(StyleConstants.ComponentElementName))
                    return new ComponentView(elem);
                else if (kind.equals(StyleConstants.IconElementName))
                    return new IconView(elem);
            }

            // default to text display
            return new LabelView(elem);
        }
    }
    
    public class NoWrapParagraphView extends ParagraphView {
        public NoWrapParagraphView(final Element elem) {
            super(elem);
        }

        @Override
        public void layout(final int width, final int height) {
            super.layout(Short.MAX_VALUE, height);
        }

        @Override
        public float getMinimumSpan(final int axis) {
            return super.getPreferredSpan(axis);
        }
    }


}
