package org.example.semantic.symbols;

import org.example.ast.ActionNode;

public class SymbolAction extends Symbol {
    public SymbolAction(String name, ActionNode actionNode) {
        super(name, actionNode);
    }

    @Override
    public String getKind() {
        return "Action";
    }
}
