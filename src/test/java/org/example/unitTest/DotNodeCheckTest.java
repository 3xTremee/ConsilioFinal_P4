package org.example.unitTest;

import org.example.ast.*;
import org.example.semantic.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DotNodeCheckTest {

    private SemanticAnalyzer analyzer;
    private ExpressionCheck checker;

    @BeforeEach
    public void setup() {
        analyzer = new SemanticAnalyzer();
        checker = new ExpressionCheck(analyzer);
    }

    // VALID SIMPLE DOT - attribute found
    @Test
    public void testCheckDotNodeExpressionWithValidSimpleDot(){
        AttributeNode attr = new AttributeNode("carrying", new ValueTypeNode("boolean"));
        TypeNode typenode = new TypeNode("robot", List.of(attr));

        SymbolType type = new SymbolType("robot", typenode);
        type.addAttribute("carrying", new SymbolAttribute("carrying", attr));

        SymbolObject obj = new SymbolObject("rob", new ObjectNode("robot", "robots", "rob", new IdentifierNode("rob")), type);

        analyzer.getSymbolTable().put("rob", obj);
        analyzer.getSymbolTable().put("robot", type);

        DotNode dot = new DotNode(new IdentifierNode("rob"), "carrying");

        assertTrue(checker.checkDotNodeExpr(dot));
    }
    // INVALID SIMPLE DOT - attribute not found
    @Test
    public void testCheckDotNodeExprWithInvalidSimpleDot() {
        SymbolType type = new SymbolType("robot", new TypeNode("robot", List.of()));
        SymbolObject rob = new SymbolObject("rob",
                new ObjectNode("robot", "robots", "rob", new IdentifierNode("rob")),
                type);

        analyzer.getSymbolTable().put("rob", rob);
        analyzer.getSymbolTable().put("robot", type);

        DotNode dot = new DotNode(new IdentifierNode("rob"), "missing");

        assertThrows(RuntimeException.class, () -> checker.checkDotNodeExpr(dot));
    }


    // VALID ARRAY DOT - index in bounds
    @Test
    public void testCheckDotNodeExpressionWithValidArrayDot() {
        AttributeNode attr = new AttributeNode("carrying", new ValueTypeNode("boolean"));
        TypeNode typenode = new TypeNode("robot", List.of(attr));
        SymbolType robotType = new SymbolType("robot", typenode);
        robotType.addAttribute("carrying", new SymbolAttribute("carrying", attr));


        SymbolObject rob0 = new SymbolObject("rob0", new ObjectNode("robot", "robots", "rob0", new IdentifierNode("rob0")), robotType);
        SymbolArray robots = new SymbolArray("robots", new ArrayInitializerNode("robot","robots", List.of("rob0")));
        robots.getObjects().add(rob0);

        analyzer.getSymbolTable().put(("robots"), robots);
        analyzer.getSymbolTable().put("rob0", rob0);
        analyzer.getSymbolTable().put("robot", robotType);

        ArrayAccessNode acces = new ArrayAccessNode("robots", List.of(new ConstantNode("0")));
        DotNode dot = new DotNode(acces, "carrying");

        assertTrue(checker.checkDotNodeExpr(dot));




    }
    // INVALID ARRAY DOT - index out of bounds
    @Test
    public void testCheckDotNodeExprWithInvalidArrayDotIndex() {
        SymbolType type = new SymbolType("robot", new TypeNode("robot", List.of(
                new AttributeNode("carrying", new ValueTypeNode("boolean"))
        )));
        SymbolObject rob0 = new SymbolObject("rob0",
                new ObjectNode("robot", "robots", "rob0", new IdentifierNode("rob0")),
                type);
        SymbolArray robots = new SymbolArray("robots",
                new ArrayInitializerNode("robot", "robots", List.of("rob0")));
        robots.getObjects().add(rob0);

        analyzer.getSymbolTable().put("robots", robots);
        analyzer.getSymbolTable().put("robot", type);
        analyzer.getSymbolTable().put("rob0", rob0);

        ArrayAccessNode access = new ArrayAccessNode("robots", List.of(new ConstantNode("1")));
        DotNode dot = new DotNode(access, "carrying");

        assertThrows(SemanticException.class, () -> checker.checkDotNodeExpr(dot));
    }


}
