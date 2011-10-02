package reverse.engineer.api.builtin;

import java.util.Iterator;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import commons.InstructionSearcher;

import reverse.engineer.api.ClassVisitor;
import reverse.engineer.api.LogNotifier;
import reverse.engineer.api.builtin.maliciouscodeanalysis.checkers.Checker;
import reverse.engineer.api.builtin.maliciouscodeanalysis.checkers.ClassModFinder;
import reverse.engineer.api.builtin.maliciouscodeanalysis.checkers.UrlLDC;

public class MaliciousCodeAnalysis implements ClassVisitor {

    public static final Checker[] checkers = new Checker[] {
        new UrlLDC(),
        new ClassModFinder()
    };
    
    @Override
    public void visitClass(final ClassNode clazz, final LogNotifier ln) {
        final Iterator<MethodNode> mit = clazz.methods.iterator();
        while (mit.hasNext()) {
            final MethodNode mn = mit.next();
            final InstructionSearcher search = new InstructionSearcher(mn);
            for (final Checker c : checkers) {
                final int found = c.check(mn, search, ln);
                if (found > 0) {
                    ln.log(" ^ Searched in " + clazz.name + "." + mn.name);
                }
            }
        }
    }

}
