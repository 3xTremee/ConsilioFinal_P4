package org.example.semantic;

import org.example.ast.ActionNode;
/*
public class SymbolAction extends Symbol {
    private final String name;
    private final SymbolType[] parameterTypes;
    private final SymbolStatement[] statements;
    private final ActionNode actionNode;


    public SymbolAction(String name, SymbolStatement[] statement, SymbolType[] parameterTypes, ActionNode actionNode) {
        this.name = name;
        this.statements = statement;
        this.parameterTypes = parameterTypes;
        this.actionNode = actionNode;
    }

    public String getName() {
        return name;
    }

    public SymbolStatement[] getStatements() {
        return statements;
    }

    public SymbolType[] getParameterTypes() {
        return parameterTypes;
    }

}*/

public class SymbolAction extends Symbol {
    public SymbolAction(String name, ActionNode actionNode) {
        super(name, actionNode);
    }

    @Override
    public String getKind() {
        return "Action";
    }
}
