package org.example.ast;

public class ValueTypeNode extends ValueNode {
    private final String typeName;

    public ValueTypeNode(String typeName) {
        this.typeName = typeName;
    }

    //getter
    public String getTypeName() {
        return typeName;
    }

    @Override
    public String toString() {
        return "ValueTypeNode(typeName=" + typeName + ")";
    }
}
