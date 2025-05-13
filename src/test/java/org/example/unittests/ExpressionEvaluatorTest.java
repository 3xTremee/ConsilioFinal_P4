package org.example.unittests;

import org.example.ast.*;
import org.example.planner.ExpressionEvaluator;
import org.example.planner.State;
import org.example.semantic.SemanticAnalyzer;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ExpressionEvaluatorTest {

    private SemanticAnalyzer dummyAnalyzer() {
        return new SemanticAnalyzer();
    }

    private State makeState(Map<String, Map<String, Object>> values) {
        return new State(values, dummyAnalyzer());
    }

    @Test
    public void testEvaluateConstantInt() {
        ConstantNode node = new ConstantNode("42");
        Object result = new ExpressionEvaluator(dummyAnalyzer()).evaluate(node, makeState(Map.of()));
        assertEquals(42, result);
    }

    @Test
    public void testEvaluateConstantBooleanTrue() {
        ConstantNode node = new ConstantNode("true");
        Object result = new ExpressionEvaluator(dummyAnalyzer()).evaluate(node, makeState(Map.of()));
        assertEquals(true, result);
    }

    @Test
    public void testEvaluateDotNodeAccess() {
        Map<String, Map<String, Object>> values = Map.of(
                "rob", Map.of("carrying", true, "location", "A")
        );
        State state = makeState(values);
        DotNode node = new DotNode(new IdentifierNode("rob"), "carrying");
        Object result = new ExpressionEvaluator(dummyAnalyzer()).evaluate(node, state);
        assertEquals(true, result);
    }

    @Test
    public void testEvaluateDotNodeWithMissingFieldReturnsNull() {
        Map<String, Map<String, Object>> values = Map.of(
                "rob", Map.of("location", "A")
        );
        State state = makeState(values);
        DotNode node = new DotNode(new IdentifierNode("rob"), "carrying");
        Object result = new ExpressionEvaluator(dummyAnalyzer()).evaluate(node, state);
        assertNull(result);
    }

    @Test
    public void testEvaluateDotNodeWithUnknownObjectReturnsNull() {
        Map<String, Map<String, Object>> values = Map.of();
        State state = makeState(values);
        DotNode node = new DotNode(new IdentifierNode("ghost"), "field");
        Object result = new ExpressionEvaluator(dummyAnalyzer()).evaluate(node, state);
        assertNull(result);
    }
}
