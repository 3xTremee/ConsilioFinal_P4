package org.example.unitTest;

import org.example.ast.*;
import org.example.semantic.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StatementCheckTest {

    private SemanticAnalyzer analyzer;
    private StatementCheck checker;

    @BeforeEach
    public void setup() {
        analyzer = new SemanticAnalyzer();
        checker = new StatementCheck(analyzer);
    }
//VALID COMPARISON - compare two integers
    @Test
    public void testValidComparisonSamTypes() {
        BinaryOpNode comparison = new BinaryOpNode(
                new ConstantNode("45"), "==", new ConstantNode("100")
        );
        assertTrue(checker.checkComparison(comparison));


    }
//INVALID COMPARISON - compare integer and boolean
    @Test
    public void testInvalidComparisonDifferentTypes() {
        BinaryOpNode comparison = new BinaryOpNode(
                new ConstantNode("45"), "==", new ConstantNode("true")
        );
        assertThrows(SemanticException.class, () -> checker.checkComparison(comparison));

    }
/*
//Returns if-statements condition
    @Test
    public void testCheckIfStatementReturnsCorrectCondition() {
        ExpressionNode condition = new ConstantNode("true");
        IfNode ifNode = new IfNode(condition,new StatementListNode(List.of()));
        assertEquals(condition, checker.checkIfCondition(ifNode));
    }
*/
//Returns all statements from then-branch
    @Test
    public void testCheckIfStatementReturnsCorrectThenBranch(){
        StatementNode stmt1 = new AssignmentNode(
                new DotNode(new IdentifierNode("rob"),"carrying"), new ConstantNode("true")
        );
        StatementNode stmt2 = new AssignmentNode(
                new DotNode(new IdentifierNode("rob"), "carrying"), new ConstantNode("false")
        );
        StatementListNode stmtList = new StatementListNode(List.of(stmt1,stmt2));
        IfNode ifNode = new IfNode(new ConstantNode("true"), stmtList);

        List<StatementNode> result = checker.checkThenBranch(ifNode);
        assertEquals(2, result.size());
    }


//VALID ASSIGNMENT - boolean to boolean field
    @Test
    public void testAssignWithMatchingTypesPasses(){
        DotNode lhs = new DotNode(new IdentifierNode("rob"), "carrying");
        AssignmentNode node = new AssignmentNode(lhs, new ConstantNode("false"));


        AttributeNode attr = new AttributeNode("carrying", new ValueTypeNode("boolean"));
        TypeNode robotTypeNode = new TypeNode("robot", List.of(new AttributeNode("carrying",new ValueTypeNode("boolean"))));
        SymbolType robotSymbol = new SymbolType("robot", robotTypeNode);
        robotSymbol.addAttribute("carrying", new SymbolAttribute("carrying", attr));


        SymbolObject dummy = new SymbolObject(
                "rob", new ObjectNode("robot", "robots", "rob", new IdentifierNode("rob")), robotSymbol
        );

        dummy.setAttributeValue("carrying", null);

        analyzer.getSymbolTable().put("rob", dummy);
        analyzer.getSymbolTable().put("robot", robotSymbol);

        assertTrue(checker.checkAssignment(node));

    }
// INVALID ASSIGNMENT - wrong object type to attribute
    @Test
    public void testAssignWrongObjectTypeToAttributeFails() {
        DotNode lhs = new DotNode(new IdentifierNode("rob"), "target");
        AssignmentNode node = new AssignmentNode(lhs, new IdentifierNode("loc1"));

        TypeNode robotType = new TypeNode("robot", List.of(new AttributeNode("target", new ValueTypeNode("location"))));
        SymbolType robotSymbol = new SymbolType("robot", robotType);

        SymbolType locationSymbol = new SymbolType("location", new TypeNode("location",List.of()));

        SymbolObject rob = new SymbolObject("rob", new ObjectNode("robot", "robots", "rob",new IdentifierNode("rob")),robotSymbol);
        SymbolObject loc = new SymbolObject("loc1",new ObjectNode("location", "locations","loc1", new IdentifierNode("loc1")), locationSymbol);

        analyzer.getSymbolTable().put("rob", rob);
        analyzer.getSymbolTable().put("loc1", loc);
        analyzer.getSymbolTable().put("robot", robotSymbol);
        analyzer.getSymbolTable().put("location", locationSymbol);

        assertThrows(RuntimeException.class, () -> checker.checkAssignment(node));
    }

    // VALID ASSIGNMENT - correct object type to attribute
    @Test
    public void testAssignCorrectObjectTypeToAttributePasses() {
        DotNode lhs = new DotNode(new IdentifierNode("rob"), "target");
        AssignmentNode node = new AssignmentNode(lhs, new IdentifierNode("loc1"));

        TypeNode robotType = new TypeNode("robot", List.of(new AttributeNode("target", new ValueTypeNode("location"))));
        SymbolType robotSymbol = new SymbolType("robot", robotType);
        robotSymbol.addAttribute("target", new SymbolAttribute("target", new AttributeNode("target", new ValueTypeNode("location"))));

        SymbolType locationSymbol = new SymbolType("location", new TypeNode("location",List.of()));

        SymbolObject rob = new SymbolObject("rob", new ObjectNode("robot", "robots", "rob",new IdentifierNode("rob")),robotSymbol);
        SymbolObject loc = new SymbolObject("loc1",new ObjectNode("location", "locations","loc1", new IdentifierNode("loc1")), locationSymbol);

        analyzer.getSymbolTable().put("rob", rob);
        analyzer.getSymbolTable().put("loc1", loc);
        analyzer.getSymbolTable().put("robot", robotSymbol);
        analyzer.getSymbolTable().put("location", locationSymbol);

        assertTrue(checker.checkAssignment(node));


    }

// INVALID ASSIGNMENT - field does not exist
    @Test
    public void testAssignToUnknownAttributeFails() {
        DotNode lhs = new DotNode(new IdentifierNode("rob"), "nonexistent");
        AssignmentNode node = new AssignmentNode(lhs,  new ConstantNode("true"));

        TypeNode robotTypeNode = new TypeNode("robot", List.of(new AttributeNode("carrying", new ValueTypeNode("boolean"))));
        SymbolType robotSymbol = new SymbolType("robot", robotTypeNode);
        SymbolObject rob = new SymbolObject("rob", new ObjectNode("robot", "robots", "rob", new IdentifierNode("rob")), robotSymbol);

        analyzer.getSymbolTable().put("rob", rob);
        analyzer.getSymbolTable().put("robot", robotSymbol);

        assertThrows(RuntimeException.class, () -> checker.checkAssignment(node));


    }

    //VALID ASSIGNMENT - field  exists
    @Test
    public void testAssignToKnownAttributePasses() {
        DotNode lhs = new DotNode(new IdentifierNode("rob"), "carrying");
        AssignmentNode node = new AssignmentNode(lhs,  new ConstantNode("true"));

        TypeNode robotTypeNode = new TypeNode("robot", List.of(new AttributeNode("carrying", new ValueTypeNode("boolean"))));
        SymbolType robotSymbol = new SymbolType("robot", robotTypeNode);
        SymbolObject rob = new SymbolObject("rob", new ObjectNode("robot", "robots", "rob", new IdentifierNode("rob")), robotSymbol);
        robotSymbol.addAttribute("carrying", new SymbolAttribute("carrying", new AttributeNode("carrying", new ValueTypeNode("boolean")))); //altså idk om man burde dele sådanne op, men er bare rare at lave når det bare er ud af en smøre


        analyzer.getSymbolTable().put("rob", rob);
        analyzer.getSymbolTable().put("robot", robotSymbol);

        assertTrue(checker.checkAssignment(node));


    }


}
