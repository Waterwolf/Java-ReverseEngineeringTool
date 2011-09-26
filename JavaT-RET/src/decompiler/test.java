package decompiler;

import org.objectweb.asm.Type;

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
    
}
