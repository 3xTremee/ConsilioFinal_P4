package org.example.unitTest;

import org.example.ast.*;
import org.example.planner.ExpressionEvaluator;
import org.example.planner.State;
import org.example.semantic.SemanticAnalyzer;
import org.example.semantic.symbols.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ExpressionEvaluatorTest {

    private SemanticAnalyzer analyzer;
    private ExpressionEvaluator evaluator;

    @BeforeEach
    public void setUp() {
        analyzer = new SemanticAnalyzer();
        evaluator = new ExpressionEvaluator(analyzer);
    }



// korrekt evaluering af konst
    @Test
    public void testEvaluateConstantInt() {
        ConstantNode node = new ConstantNode("45");
        Object result = evaluator.evaluate(node,null);
        assertEquals(45, result);
    }
// korrekt evaluering af boolsk true
    @Test
    public void testEvaluateConstantBooleanTrue() {
        ConstantNode node = new ConstantNode("true");
        Object result = evaluator.evaluate(node,null);
        assertEquals(true, result);
    }
// korrekt evaluering af heltals addition
    @Test
    public void testEvaluateAddition() {
        ExpressionNode left = new ConstantNode("45");
        ExpressionNode right = new ConstantNode("4");
        BinaryOpNode op = new BinaryOpNode(left, "+", right);

        assertEquals(49, evaluator.evaluate(op,null));


    }

//tester sammenligning/cmp mellem to heltal
    @Test
    public void testEvaluateComparison() {
        BinaryOpNode cmp = new BinaryOpNode(new ConstantNode("45"), ">", new ConstantNode("4"));
        assertEquals(true, evaluator.evaluate(cmp, null));
    }
//tester and operation mellem to boolske værdier
    @Test
    public void testEvaluateLogicalAnd() {
        BinaryOpNode and = new BinaryOpNode(new ConstantNode("true"), "&&", new ConstantNode("true"));
        assertEquals(true , evaluator.evaluate(and, null));

    }
// terster evaluering af attribut access via DotNode, hvor objekt og attribut findes i starten
    @Test
    public void testEvaluateDotAttribute(){
        AttributeNode attr = new AttributeNode("carrying", new ValueTypeNode("boolean"));
        SymbolType robotType = new SymbolType("robot", new TypeNode("robot", List.of(attr)));
        SymbolObject rob = new SymbolObject("rob", new ObjectNode("robot","robots","rob", new IdentifierNode("rob")),robotType);
        rob.setAttributeValue("carrying", true);

        analyzer.getSymbolTable().put("rob", rob);
        analyzer.getSymbolTable().put("robots", robotType);

        //lav dummy state map her
        Map<String, Map<String, Object>> stateMap = new HashMap<>();
        Map<String, Object> robAttributes = new HashMap<>();
        robAttributes.put("carrying", true);
        stateMap.put("rob", robAttributes);

        State state = new State(stateMap);

        DotNode dot = new DotNode(new IdentifierNode("rob"), "carrying");
        assertEquals(true, evaluator.evaluate(dot, state));

    }


// tester DotNode evaluering hvor attributten ikke findes for objektet . Det skal returnere null
    @Test
    public void testEvaluateDotNodeWithMissingFieldReturnsNull() {
        Map<String, Map<String, Object>> stateMap = new HashMap<>();
        stateMap.put("rob", Map.of("location", "A"));
        State state = new State(stateMap);

        DotNode node = new DotNode(new IdentifierNode("rob"), "carrying");
        Object result = evaluator.evaluate(node, state);
        assertNull(result);


    }
// tester DotNode evaluering hvor OBJEKTET ikke findes i starten . Det skal retunere null
    @Test
    public void testEvaluateDotNodeWithUnknownObjectReturnsNull() {
        Map<String, Map<String, Object>> stateMap = new HashMap<>();
        State state = new State(stateMap);

        DotNode node = new DotNode(new IdentifierNode("robFail"), "field");
        Object result = evaluator.evaluate(node, state);
        assertNull(result);


    }
}
