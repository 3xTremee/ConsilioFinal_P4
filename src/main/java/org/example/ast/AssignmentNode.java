package org.example.ast;

public class AssignmentNode extends StatementNode {
    private final DotNode target;
    private final ExpressionNode expression;

    public AssignmentNode(DotNode target, ExpressionNode expression) {
        this.target = target;
        this.expression = expression;
    }

    // getters
    public DotNode getTarget() {
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
