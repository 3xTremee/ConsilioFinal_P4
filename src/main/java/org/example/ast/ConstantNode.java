package org.example.ast;

public class ConstantNode extends ExpressionNode {
    private final String value;

    public ConstantNode(String value) {
        this.value = value;
    }

    //getters
    public String getValueConstant() {
        return value;
    }

    @Override
    public String toString() {
        return "Constant(value = " + value + ")";
    }
}
