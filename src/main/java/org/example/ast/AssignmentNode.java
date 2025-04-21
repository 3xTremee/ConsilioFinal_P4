package org.example.ast;

public class AssignmentNode extends StatementNode {
    private final String target;
    private final ExpressionNode expression;

    public AssignmentNode(String target, ExpressionNode expression) {
        this.target = target;
        this.expression = expression;
    }

    // getters
    public String getTarget() {
        return target;
    }

    public ExpressionNode getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        return "Assignment(target=" + target + ", expression=" + expression + ")";
    }
}
