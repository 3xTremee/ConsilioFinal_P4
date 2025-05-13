package org.example.unittests;

import org.example.ast.*;
import org.example.semantic.ExpressionCheck;
import org.example.semantic.SemanticAnalyzer;
import org.example.semantic.Symbol;
import org.example.semantic.SymbolObject;
import org.example.semantic.SymbolType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ExpressionCheckTest {

    private SemanticAnalyzer analyzer;
    private ExpressionCheck checker;

    @BeforeEach
    public void setup() {
        analyzer = new SemanticAnalyzer();
        checker = new ExpressionCheck(analyzer);
    }

    @Test
    public void testCheckIntWithValidInteger() {
        ConstantNode node = new ConstantNode("42");
        assertTrue(checker.checkInt(node));
    }

    @Test
    public void testCheckIntWithInvalidInteger() {
        ConstantNode node = new ConstantNode("syv");
        assertFalse(checker.checkInt(node));
    }

    @Test
    public void testCheckBoolWithTrue() {
        ConstantNode node = new ConstantNode("true");
        assertTrue(checker.checkBool(node));
    }

    @Test
    public void testCheckBoolWithFalse() {
        ConstantNode node = new ConstantNode("false");
        assertTrue(checker.checkBool(node));
    }

    @Test
    public void testCheckBoolWithInvalidString() {
        ConstantNode node = new ConstantNode("test");
        assertFalse(checker.checkBool(node));
    }

    @Test
    public void testCheckIdentifierReturnsNameIfExists() {
        ObjectNode dummyObject = new ObjectNode("robot", "robots", "rob", new IdentifierNode("rob"));
        TypeNode dummyType = new TypeNode("robot", List.of(new AttributeNode("carrying", new ValueTypeNode("boolean"))));
        SymbolType symbolType = new SymbolType("robot", dummyType);
        SymbolObject dummy = new SymbolObject("rob", dummyObject, symbolType);

        analyzer.getSymbolTable().put("rob", dummy);

        IdentifierNode node = new IdentifierNode("rob");
        assertEquals("rob", checker.checkIdentifier(node));
    }

    @Test
    public void testCheckIdentifierThrowsIfNotFound() {
        IdentifierNode node = new IdentifierNode("ghost");
        assertThrows(NullPointerException.class, () -> checker.checkIdentifier(node));
    }

    @Test
    public void testCheckObjectReturnsCorrectSymbolType() {
        ObjectNode dummyObject = new ObjectNode("robot", "robots", "rob", new IdentifierNode("rob"));
        TypeNode dummyType = new TypeNode("robot", List.of(new AttributeNode("carrying", new ValueTypeNode("boolean"))));
        SymbolType symbolType = new SymbolType("robot", dummyType);
        SymbolObject dummy = new SymbolObject("rob", dummyObject, symbolType);

        analyzer.getSymbolTable().put("rob", dummy);

        IdentifierNode node = new IdentifierNode("rob");
        SymbolType type = checker.checkObject(node);
        assertEquals("robot", type.getName());
    }

    @Test
    public void testCheckObjectThrowsIfSymbolIsNotObject() {
        TypeNode dummyType = new TypeNode("int", List.of());
        Symbol dummy = new SymbolType("int", dummyType);
        analyzer.getSymbolTable().put("val", dummy);

        IdentifierNode node = new IdentifierNode("val");
        assertThrows(RuntimeException.class, () -> checker.checkObject(node));
    }
}
