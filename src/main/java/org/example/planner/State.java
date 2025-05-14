package org.example.planner;

import org.example.ast.*;
import org.example.semantic.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable snapshot of all object-field values.
 */
public class State {
    private final Map<String, Map<String, Object>> store; //mapper objekt navne til deres konkrete attribut værdier fx "r1" → { "location" → "A", "carrying" → false }

    SemanticAnalyzer semanticAnalyzer;

    public State(Map<String, Map<String, Object>> initial, SemanticAnalyzer semanticAnalyzer) {
            this.store = new HashMap<>();
            for (Map.Entry<String, Map<String, Object>> e : initial.entrySet()) {
                this.store.put(e.getKey(), new HashMap<>(e.getValue()));
        }
            this.semanticAnalyzer = semanticAnalyzer;
    }

    /**
     * Read object.field, returning null if the object or the field isn’t present.
     */
    public Object get(String object, String field) {
        Map<String, Object> objMap = store.get(object);
        if (objMap == null) {
            // no such object in this state
            return null;
        }
        // may return null if this field was never set
        return objMap.get(field);
    }


//laver en ny state med opdateret fields
    public State with(String object, String field, Object newValue) {
        // 1) shallow‐copy the outer map, deep‐copy each inner map
        Map<String, Map<String, Object>> copy = new HashMap<>();
        for (var e : store.entrySet()) {
            copy.put(e.getKey(), new HashMap<>(e.getValue()));
        }

        // 2) ensure there’s a map for this objectName
        copy.computeIfAbsent(object, k -> new HashMap<>())
                // 3) set the field
                .put(field, newValue);

        // 4) return a fresh immutable State
        return new State(copy, semanticAnalyzer);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof State)) return false;
        State other = (State) o;
        return Objects.equals(store, other.store);
    }

    @Override
    public int hashCode() {
        return Objects.hash(store);
    }

    @Override
    public String toString() {
        return "State" + store;
    }

    /**
     * Build the initial State from the problem’s objects and init assignments.
     */
    // Bliver vidst ikke brugt (slet?)
    public static State fromProblem(ProblemNode problem, SemanticAnalyzer semanticAnalyzer) {
        // 1) empty slot for each object
        Map<String, Map<String, Object>> init = new HashMap<>();
        for (ObjectNode obj : problem.getObjects()) {
            init.computeIfAbsent(obj.getElementName(), k -> new HashMap<>());
        }
        State state = new State(init, semanticAnalyzer);

        // 2) apply each AssignmentNode
        ExpressionEvaluator eval = new ExpressionEvaluator(semanticAnalyzer);
        for (StatementNode stmt : problem.getStatement()) {
            if (stmt instanceof AssignmentNode asn) {
                DotNode dotNode = asn.getTarget();
                ExpressionNode exprNode = dotNode.getTarget();
                String objName = ((IdentifierNode) exprNode).getName();
                String field   = dotNode.getField();
                Object val = eval.evaluate(asn.getExpression(), state);
                state = state.with(objName, field, val);
            }
        }
        return state;
    }

    public static State fromSymbolTable(Map<String, Symbol> symbolTable, SemanticAnalyzer semanticAnalyzer) {
        Map<String, Map<String, Object>> initialStateMap = new HashMap<>();

        for (Symbol symbol : symbolTable.values()) {
            if (symbol instanceof SymbolObject) {
                SymbolObject objSymbol = (SymbolObject) symbol;
                initialStateMap.put(objSymbol.getName(), objSymbol.getAttributes());
            }
        }
        return new State(initialStateMap, semanticAnalyzer);
    }
}
