package org.example.ast;

import java.util.List;

public class AttributeNode extends AstNode{
    private final String identifier;
    private final ValueNode value;

    public AttributeNode(String identifier, ValueNode value) {
        this.identifier = identifier;
        this.value = value;
    }

    //getters
    public String getIdentifier() {
        return identifier;
    }

    public ValueNode getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "identifier: " + identifier + " value: " + value;
    }
}
