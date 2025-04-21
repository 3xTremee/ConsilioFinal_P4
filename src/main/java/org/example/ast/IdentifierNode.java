package org.example.ast;

public class IdentifierNode extends ExpressionNode {
    private final String name;

    public IdentifierNode(String name) {
        this.name = name;
    }

    //getter
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "IdentifierNode= Name:" + name;
    }
}
