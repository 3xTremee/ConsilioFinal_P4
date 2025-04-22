package org.example.semantic;

import java.util.*;
import org.example.ast.*;

public class SemanticAnalyzer {
    private final Map<String, TypeNode> types = new HashMap<>();
    private final Map<String, ActionNode> actions = new HashMap<>();
    private final Map<String, ObjectNode> objects = new HashMap<>();
    private final HelperFunctions helperFunctions = new HelperFunctions();

    public void analyze(DomainNode domain, ProblemNode problem) {
        // Tjek typer
        for (TypeNode type : domain.getTypes()) {
            if (types.containsKey(type.getName())) {
                throw new SemanticException("Type duplicate: " + type.getName());
            } else {
                types.put(type.getName(), type);
                System.out.println(types);
            }
        }

        // Tjek actions
        for (ActionNode action : domain.getActions()) {
            if (actions.containsKey(action.getName())) {
                throw new SemanticException("Action duplicate: " + action.getName());
            } else {
                List<ParameterNode> parameters = action.getParameters();
                for (ParameterNode parameter : parameters) {
                    String parametertype = parameter.getType();
                    if (!types.containsKey(parametertype)) {
                        throw new SemanticException("Type does not exist: " + parametertype);
                    }
                }
                StatementNode body = action.getBody();
                helperFunctions.actionBodyCheck(body, parameters, types);
                actions.put(action.getName(), action);
                System.out.println(actions);
            }
        }

        // Tjek import af domain
        if (!domain.getName().equals(problem.getImportName())) {
            throw new SemanticException("Import name mismatch: " + problem.getImportName() + " Expected: " + domain.getName());
        }

        // Tjek objects
        for (ObjectNode object : problem.getObjects()) {
            if (objects.containsKey(object.getElementName())) {
                throw new SemanticException("Object duplicate: " + object.getElementName());
            } else {
                objects.put(object.getElementName(), object);
            }
        }
    }
}


