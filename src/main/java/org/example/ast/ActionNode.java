package org.example.ast;

import java.util.List;

public class ActionNode extends AstNode {
    private final String name;
    private final List<ParameterNode> parameters;
    private final StatementNode body;

    public ActionNode(String name,
                      List<ParameterNode> parameters,
                      StatementNode body) {
        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }

    //Getters
    public String getName() {
        return name;
    }

    public List<ParameterNode> getParameters() {
        return parameters;
    }

    public StatementNode getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "Action(name=" + name + ", parameters=" + parameters + ", body=" + body + ")";
    }
}
