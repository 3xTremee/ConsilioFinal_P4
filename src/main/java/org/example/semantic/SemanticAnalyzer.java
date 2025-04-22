package org.example.semantic;

import java.util.*;
import org.example.ast.*;

public class SemanticAnalyzer {
    private final Map<String, TypeNode> types = new HashMap<>();
    private final Map<String, ActionNode> actions = new HashMap<>();

    // Hjælper funktion til at enter et symbol i et given hashmap.
    private <T> void enterSymbol(Map<String, T> table, String name, T symbol, String kind) {
        if (table.containsKey(name)) {
            throw new SemanticException(kind + " duplicate: " + name);
        }
        table.put(name, symbol);
    }

    public void analyze(DomainNode domain, ProblemNode problem) {

        // Tjek typer
        for (TypeNode type : domain.getTypes()) {
            enterSymbol(types, type.getName(), type, "Type");
        }

        // Tjek actions
        for (ActionNode action : domain.getActions()) {
            enterSymbol(actions, action.getName(), action, "Action");
        }

        // Tjek import af domain
        if (!domain.getName().equals(problem.getImportName())) {
            throw new SemanticException("Import name mismatch: " + problem.getImportName() + " Expected: " + domain.getName());
        }

        // type check ide
/*
        checkProblem();
        checkTypes();
        checkAction();
        checkStatement();
        checkExpression();
        checkObjects();
        checkInitial();
        checkGoal();

 */
    }
}
