package reverse.engineer.api.builtin.maliciouscodeanalysis.checkers;

import org.objectweb.asm.tree.MethodNode;

import reverse.engineer.api.LogNotifier;

import commons.InstructionSearcher;

public interface Checker {
    public int check(MethodNode mn, InstructionSearcher search, LogNotifier logger);
}
