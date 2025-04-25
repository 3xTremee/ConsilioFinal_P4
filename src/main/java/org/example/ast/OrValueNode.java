package org.example.ast;

import java.util.List;

public class OrValueNode extends ValueNode {
    private final List<ValueNode> types;

    public OrValueNode(List<ValueNode> types) {
        this.types = types;
    }

    public List<ValueNode> getTypes() {
        return types;
    }

    @Override
    public String toString() {
        return "OrValueNode(types=" + types + ")";
    }
}