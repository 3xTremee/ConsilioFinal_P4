package org.example.ast;

import java.util.List;

public class DomainNode extends AstNode {
    private final String name;
    private final List<TypeNode> types;
    private final List<ActionNode> actions;

    public DomainNode(String name, List<TypeNode> types, List<ActionNode> action) {
        this.name = name;
        this.types = types;
        this.actions = action;
    }

    //getters
    public String getName() {
        return name;
    }

    public List<TypeNode> getTypes() {
        return types;
    }

    public List<ActionNode> getActions() {
        return actions;
    }

    @Override
    public String toString() {
        return "Domain(Name = " + name + ", Types = " + types + ", Actions = " + actions + ")";
    }
}
