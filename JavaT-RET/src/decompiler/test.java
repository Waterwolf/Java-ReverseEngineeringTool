package decompiler;

import org.objectweb.asm.Type;

/**
 * 
 *  Test class that should be used to test decompiler
 * 
 * @author Waterwolf
 *
 */
public class test {
    
    public static final String[] forTesting = new String[] {"hi", "what's", "up"};
    
    /**
     * @param args
     */
    public static void main(final String[] args) {
        // TODO Auto-generated method stub

    }
    
    public int testMethod(final String string, final int int_, final Type type) {
        return 42 + int_;
    }
    
    public void forLoopTest() {
        for (int i = 0;i < 10; i++) {
            System.out.println("ASM is fun!");
        }
    }
    
    public void stringLoop() {
        for (int i = 0;i < forTesting.length; i++) {
            final String string = forTesting[i];
            System.out.println(string);
        }
    }
    
    public void enhancedStringLoop() {
        for (final String string : forTesting) {
            System.out.println(string);
        }
    }
    
}
