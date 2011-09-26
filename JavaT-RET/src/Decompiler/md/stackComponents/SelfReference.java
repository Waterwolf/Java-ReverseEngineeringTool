package Decompiler.md.stackComponents;

public class SelfReference implements Nameable {

    @Override
    public String getCallingName() {
        return "this";
    }

}
