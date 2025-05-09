package org.example.semantic;

import org.example.ast.*;

public abstract class Symbol {
    private final String name;
    private final AstNode node;

    public Symbol(String name, AstNode node) {
        this.name = name;
        this.node = node;
    }

    public String getName() {
        return name;
    }

    public AstNode getNode() {
        return node;
    }

    public abstract String getKind();
}
