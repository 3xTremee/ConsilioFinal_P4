package org.example.ast;

public class BaseValueNode extends ValueNode {
    private final String value;

    public BaseValueNode(String value) {
        this.value = value;
    }

    //getter
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
