package Decompiler.md.stackComponents;

public class NamedReference implements Nameable {

    String t;
    
    public NamedReference(final String t) {
        this.t = t;
    }
    
    @Override
    public String getCallingName() {
        return t;
    }

}
