package org.example.ast;

public class IfNode extends StatementNode {
    private final ExpressionNode condition;
    private final StatementListNode thenBranch;

    public IfNode(ExpressionNode condition, StatementListNode thenBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
    }

    //getters
    public ExpressionNode getCondition() {
        return condition;
    }

    public StatementNode getThenBranch() {
        return thenBranch;
    }

    @Override
    public String toString() {
        return "IfNode(condition = " + condition + ", thenBranch = " + thenBranch + ")";
    }
}
