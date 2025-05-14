package org.example.ast;

import java.util.List;

public class StatementListNode extends StatementNode {
    private final List<StatementNode> statements;

    public StatementListNode(List<StatementNode> statements) {
        this.statements = statements;
    }

    //getters
    public List<StatementNode> getStatements() {
        return statements;
    }

    @Override
    public String toString() {
        return "StatementListNode(Statements = " + statements + ")";
    }
}
