package org.example.ast;

public class OrValueNode extends ValueNode {
    private final ValueNode left;
    private final ValueNode right;

    public OrValueNode(ValueNode left, ValueNode right) {
        this.left = left;
        this.right = right;
    }

    //getters
    public ValueNode getLeft() {
        return left;
    }

    public ValueNode getRight() {
        return right;
    }

    @Override
    public String toString() {
        return "OrValueNode(left=" + left + ", right=" + right + ")";
    }
}
