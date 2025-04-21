package org.example.semantic;

import java.util.*;
import org.example.ast.*;

public class SemanticAnalyzer {
    private final Map<String, TypeNode> types = new HashMap<>();
    private final Map<String, ActionNode> actions = new HashMap<>();

    public void analyze(DomainNode domain, ProblemNode problem) {
        // Tjek typer
        for (TypeNode type : domain.getTypes()) {
            if (types.containsKey(type.getName())) {
                throw new SemanticException("Type diplicate: " + type.getName());
            } else {
                types.put(type.getName(), type);
            }
        }

        // Tjek actions
        for (ActionNode action : domain.getActions()) {
            if (actions.containsKey(action.getName())) {
                throw new SemanticException("Action diplicate: " + action.getName());
            } else {
                actions.put(action.getName(), action);
            }
        }

        // Tjek import af domain
        if (!domain.getName().equals(problem.getImportName())) {
            throw new SemanticException("Import name mismatch: " + problem.getImportName() + " Expected: " + domain.getName());
        }
    }
}
