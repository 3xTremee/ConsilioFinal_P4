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

        // tjek fields (vi kalder det attributes) for type definitions.
        for (TypeNode type : domain.getTypes()) {
            for (AttributeNode attr : type.getAttributes()) {
                checkValueNode(attr.getValue());
            }
        }


        // type check ide
/*
        checkStatement(); Kage
        checkExpression(); Astrid
        checkObjects(); Tilde
        checkInitial();
        checkGoal();

 */
    }

    // Helper method which checks the value the fields of the types can take.
    /* location: door || room, in this example, there needs to be a valid type definition named door and room for it
    *  to pass the type check. */
    private void checkValueNode(ValueNode value) {
        if (value instanceof ValueTypeNode) {
            String typeName = ((ValueTypeNode) value).getTypeName();
            if (!(types.containsKey(typeName)
                    || "int".equals(typeName)
                    || "boolean".equals(typeName)))
                throw new SemanticException("Unknown type in attribute: " + typeName);
        } else if (value instanceof BaseValueNode) {
            String typeName = ((BaseValueNode) value).getValue();
            if (!(types.containsKey(typeName)
                    || "int".equals(typeName)
                    || "boolean".equals(typeName)))
                throw new SemanticException("Unknown type in attribute: " + typeName);
        } else if (value instanceof ArrayValueNode) {
            checkValueNode(((ArrayValueNode) value).getElementType());
        } else if (value instanceof OrValueNode) {
            checkValueNode(((OrValueNode) value).getLeft());
            checkValueNode(((OrValueNode) value).getRight());
        }
    }
}
