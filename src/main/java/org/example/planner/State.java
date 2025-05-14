package org.example.planner;

import org.example.ast.*;
import org.example.semantic.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// Class that creates the State s used by the planner
public class State {
    // Map of object names and their corrosponding concrete attributes. "r1 maps to location, which is A, and carrying which is false"
    private final Map<String, Map<String, Object>> store;

    SemanticAnalyzer semanticAnalyzer;

    public State(Map<String, Map<String, Object>> initial, SemanticAnalyzer semanticAnalyzer) {
            this.store = new HashMap<>();
            for (Map.Entry<String, Map<String, Object>> e : initial.entrySet()) {
                this.store.put(e.getKey(), new HashMap<>(e.getValue()));
        }
            this.semanticAnalyzer = semanticAnalyzer;
    }


    // Gets the field/attribute of the object
    public Object get(String object, String field) {
        Map<String, Object> objMap = store.get(object);
        if (objMap == null) {
            return null;
        }
        // may return null if this field was never set
        return objMap.get(field);
    }



    // Creates a new State with updated fields
    public State with(String object, String field, Object newValue) {
        Map<String, Map<String, Object>> copy = new HashMap<>();
        for (var e : store.entrySet()) {
            copy.put(e.getKey(), new HashMap<>(e.getValue()));
        }

        // Ensures that a map is present for the object name
        copy.computeIfAbsent(object, k -> new HashMap<>())
                .put(field, newValue);

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


    // Create the initial state from the objects and initial state/assignments (Delete if end up not being used)
    public static State fromProblem(ProblemNode problem, SemanticAnalyzer semanticAnalyzer) {
        Map<String, Map<String, Object>> init = new HashMap<>();
        for (ObjectNode obj : problem.getObjects()) {
            init.computeIfAbsent(obj.getElementName(), k -> new HashMap<>());
        }
        State state = new State(init, semanticAnalyzer);

        // Apply each assignmentNode
        ExpressionEvaluator eval = new ExpressionEvaluator(semanticAnalyzer);
        for (StatementNode stmt : problem.getStatement()) {
            if (stmt instanceof AssignmentNode asn) {
                DotNode dotNode = asn.getTarget();
                ExpressionNode exprNode = dotNode.getTarget();
                String objName = ((IdentifierNode) exprNode).getName();
                String field = dotNode.getField();
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
