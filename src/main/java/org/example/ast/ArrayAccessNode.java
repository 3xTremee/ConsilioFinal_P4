package org.example.ast;

import java.util.List;

public class ArrayAccessNode extends ExpressionNode {
    private final String arrayName;
    private final List<ExpressionNode> indices;

    public ArrayAccessNode(String arrayName, List<ExpressionNode> indices) {
        this.arrayName = arrayName;
        this.indices = indices;
    }

    //getters
    public String getArrayName() {
        return arrayName;
    }

    public List<ExpressionNode> getIndices() {
        return indices;
    }

    @Override
    public String toString() {
        return "ArrayAccessNode(ArrayName = " + arrayName + ", indices = " + indices + ")";
    }
}
