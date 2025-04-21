package org.example.ast;

public class ParenExpressionNode extends ExpressionNode {
    private final ExpressionNode inner;

    public ParenExpressionNode(ExpressionNode inner) {
        this.inner = inner;
    }

    //getter
    public ExpressionNode getInner() {
        return inner;
    }
}
