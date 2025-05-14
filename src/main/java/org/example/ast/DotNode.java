package org.example.ast;

public class DotNode extends ExpressionNode {
    private final ExpressionNode target;
    private final String field;

    public DotNode(ExpressionNode target, String field) {
        this.target = target;
        this.field = field;
    }

    //getters
    public ExpressionNode getTarget() {
        return target;
    }

    public String getField() {
        return field;
    }

    @Override
    public String toString() {
        return "DotNode(Target = " + target + ", field = " + field + ")";
    }
}
