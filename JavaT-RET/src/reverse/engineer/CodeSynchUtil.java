package reverse.engineer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import reverse.engineer.ClassViewer.MethodData;

public class CodeSynchUtil {
    public static int findLine(final String regex, final String target) {
        final Pattern pattern = Pattern.compile(regex);
        
        final String lines[] = target.split("\\r?\\n");
        
        for (int i = 0;i < lines.length; i++) {
            final Matcher m = pattern.matcher(lines[i]);
            if (m.find())
                return i;
        }

        return -1;
    }
    
    public static void main(final String[] testLab) {
        final MethodData md = new ClassViewer.MethodData();
        md.name = "check";
        md.desc = "(Lorg.objectweb.asm.tree.MethodNode;LMaliciousCodeAnalysis.InstructionSearcher;LMaliciousCodeAnalysis.Logger;)I";
        
        final String pattern = md.constructPattern();
        
        final String target = "public class Hello {\npublic int check   (MethodNode node, InstructionSearcher search, Logger logger) {\ncrap\n}\n}";
        
        System.out.println(pattern);
        System.out.println(findLine(pattern, target));
        System.out.println(target);
    }
}
