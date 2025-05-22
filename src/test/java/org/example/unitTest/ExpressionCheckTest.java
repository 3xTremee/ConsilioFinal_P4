package org.example.unitTest;

import org.example.ast.*;
import org.example.semantic.*;
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

    //Valid integer constant check
    @Test
    public void testCheckIntWithValidInteger() {
        ConstantNode node = new ConstantNode("45");
        assertTrue(checker.checkInt(node));
    }

    //Invalid integer string should return false
    @Test
    public void testCheckIntWithInvalidInteger() {
        ConstantNode node = new ConstantNode("not a number");
        assertFalse(checker.checkInt(node));
    }

    //Boolean literal "true" is valid
    @Test
    public void testCheckBoolWithTrue() {
        ConstantNode node = new ConstantNode("true");
        assertTrue(checker.checkBool(node));
    }

    //Boolean literal "false" is valid
    @Test
    public void testCheckBoolWithFalse() {
        ConstantNode node = new ConstantNode("false");
        assertTrue(checker.checkBool(node));
    }

    //Invalid string is not a boolean
    @Test
    public void testCheckBoolWithInvalidString() {
        ConstantNode node = new ConstantNode("maybe");
        assertFalse(checker.checkBool(node));
    }


// Indetifier exists in the symbol table
    @Test
    public void testIdentifierExists() {
        SymbolType type = new SymbolType("robot", new TypeNode("robot", List.of()));
        SymbolObject rob = new SymbolObject("rob", new ObjectNode("robot", "robots", "rob", new IdentifierNode("rob")), type);
        analyzer.getSymbolTable().put("rob", rob);

        assertEquals("rob", checker.checkIdentifier(new IdentifierNode("rob")));
    }

//identifier does not exist in symbol table and should throw NullPointerException
    @Test
    public void testIdentifierMissing() {
        IdentifierNode node = new IdentifierNode("robot");
        assertThrows(NullPointerException.class, () -> checker.checkIdentifier(node));
    }


    //Object node with known symbol type returns correct type
    @Test
    public void testCheckObjectReturnsSymbolType() {
        ObjectNode object = new ObjectNode("robot", "robots", "rob", new IdentifierNode("rob"));
        TypeNode type = new TypeNode("robot", List.of(new AttributeNode("carrying", new ValueTypeNode("boolean"))));
        SymbolType symbolType = new SymbolType("robot", type);
        SymbolObject symbol = new SymbolObject("rob", object, symbolType);

        analyzer.getSymbolTable().put("rob", symbol);

        IdentifierNode node = new IdentifierNode("rob");
        SymbolType result = checker.checkObject(node);
        assertEquals("robot", result.getName());
    }

    //Known type should be confirmed as existing
    @Test
    public void testTypeExistsTrue() {
        TypeNode typeNode = new TypeNode("room", List.of());
        analyzer.enterSymbol("room", new SymbolType("room", typeNode));
        assertTrue(checker.typeExists("room"));
    }

    //Unknown type should return false
    @Test
    public void testTypeExistsFalse() {
        assertFalse(checker.typeExists("person"));
    }

    // attribute not in type should throw exception
    @Test
    public void testAttributeCheckThrowsIfMissing() {
        AttributeNode attr = new AttributeNode("location", new ValueTypeNode("room"));
        TypeNode typeNode = new TypeNode("robot", List.of(attr));
        SymbolType symbolType = new SymbolType("robot", typeNode);

        SymbolAttribute fakeAttr = new SymbolAttribute("ghostField", attr);
        assertThrows(RuntimeException.class, () -> checker.attributeCheck(symbolType, fakeAttr));
    }

/* GAMLE TESTS, RYKKET TIL DOTNODECHECKTEST
    @Test
    public void testDotValidAttributeAcces(){
        AttributeNode attr = new AttributeNode("carrying", new ValueTypeNode("boolean"));
        SymbolType robotType = new SymbolType("robot", new TypeNode("robot", List.of(attr)));
        TypeNode typeNode = new TypeNode("robot", List.of(attr));
        robotType.addAttribute("carrying", new SymbolAttribute("carrying", attr));


        SymbolObject rob = new SymbolObject("rob", new ObjectNode("robot", "robots", "rob", new IdentifierNode("rob")), robotType);
        rob.setAttributeValue("carrying", "true");

        analyzer.getSymbolTable().put("robot", robotType);
        analyzer.getSymbolTable().put("rob", rob);

        DotNode dot = new DotNode(new IdentifierNode("rob"),"carrying");
        assertTrue(checker.checkDotNode(dot));
    }

    @Test
    public void testDotInvalidAttributeAcces() {
        SymbolType robotType = new SymbolType("robot", new TypeNode("robot", List.of()));
        SymbolObject rob = new SymbolObject("rob", new ObjectNode("robot", "robots", "rob", new IdentifierNode("rob")), robotType);

        analyzer.getSymbolTable().put("robot", robotType);
        analyzer.getSymbolTable().put("rob", rob);

        DotNode dot = new DotNode(new IdentifierNode("rob"), "unknown");
        assertThrows(RuntimeException.class, () -> checker.checkDotNode(dot));
    }
*/
    @Test
    public void testBinaryAdditionValid() {
        BinaryOpNode op = new BinaryOpNode(new ConstantNode("2"),"+", new ConstantNode("45"));
        assertEquals("int", checker.binOpEval(op));
    }

    @Test
    public void testBinaryInvalidTypeMismatch() {
        BinaryOpNode op = new BinaryOpNode(new ConstantNode("true"), "&&", new ConstantNode("45"));
        assertThrows(RuntimeException.class, () -> checker.binOpEval(op));
    }

    @Test
    public void testEqualityWithBooleans() {
        BinaryOpNode eq = new BinaryOpNode(new ConstantNode("true"), "==", new ConstantNode("false"));
        assertEquals(checker.binOpEval(eq), "boolean");
    }

    @Test
    public void testEqualityWithIntegers() {
        BinaryOpNode eq = new BinaryOpNode(new ConstantNode("45"), "==", new ConstantNode("455"));
        assertEquals(checker.binOpEval(eq), "boolean");
    }

    @Test
    public void testEqualityWithIncompatibleTypes() {
        BinaryOpNode eq = new BinaryOpNode(new ConstantNode("true"), "==", new ConstantNode("45"));
        assertThrows(RuntimeException.class, () -> checker.binOpEval(eq));
    }

    @Test
    public void testParenExpressionEvaluation() {
        ParenExpressionNode pex = new ParenExpressionNode(new ConstantNode("45"));
        assertEquals(checker.typeEvaluation(pex), "int");

    }
}
