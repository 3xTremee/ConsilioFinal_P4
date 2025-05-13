package org.example.unittests;

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

    @Test
    public void testValidComparisonSameTypes() {
        BinaryOpNode comparison = new BinaryOpNode(
                new ConstantNode("42"), "==", new ConstantNode("24")
        );
        assertTrue(checker.checkComparison(comparison));
    }

    @Test
    public void testInvalidComparisonDifferentTypesThrows() {
        BinaryOpNode comparison = new BinaryOpNode(
                new ConstantNode("true"), "==", new ConstantNode("24")
        );
        assertThrows(RuntimeException.class, () -> checker.checkComparison(comparison));
    }

    @Test
    public void testCheckIfConditionReturnsCorrectCondition() {
        ExpressionNode condition = new ConstantNode("true");
        IfNode ifNode = new IfNode(condition, new StatementListNode(List.of()));
        assertEquals(condition, checker.checkIfCondition(ifNode));
    }

    @Test
    public void testCheckThenBranchReturnsCorrectList() {
        StatementNode stmt1 = new AssignmentNode(
                new DotNode(new IdentifierNode("rob"), "carrying"),
                new ConstantNode("false")
        );
        StatementNode stmt2 = new AssignmentNode(
                new DotNode(new IdentifierNode("rob"), "carrying"),
                new ConstantNode("true")
        );
        StatementListNode stmtList = new StatementListNode(List.of(stmt1, stmt2));
        IfNode ifNode = new IfNode(new ConstantNode("true"), stmtList);

        List<StatementNode> result = checker.checkThenBranch(ifNode);
        assertEquals(2, result.size());
    }

    @Test
    public void testAssignmentWithMatchingTypesPasses() {
        DotNode lhs = new DotNode(new IdentifierNode("rob"), "carrying");
        AssignmentNode node = new AssignmentNode(lhs, new ConstantNode("false"));

        TypeNode robotTypeNode = new TypeNode("robot",
                List.of(new AttributeNode("carrying", new ValueTypeNode("boolean"))));

        SymbolType robotSymbol = new SymbolType("robot", robotTypeNode);
        SymbolObject dummy = new SymbolObject(
                "rob",
                new ObjectNode("robot", "robots", "rob", new IdentifierNode("rob")),
                robotSymbol
        );

        analyzer.getSymbolTable().put("rob", dummy);
        analyzer.getSymbolTable().put("robot", robotSymbol);

        assertTrue(checker.checkAssignment(node));
    }


    @Test
    public void testAssignmentWithMismatchedTypesFails() {
        DotNode lhs = new DotNode(new IdentifierNode("rob"), "carrying");
        AssignmentNode node = new AssignmentNode(lhs, new ConstantNode("42"));

        SymbolObject dummy = new SymbolObject(
                "rob",
                new ObjectNode("robot", "robots", "rob", new IdentifierNode("rob")),
                new SymbolType("robot", new TypeNode("robot",
                        List.of(new AttributeNode("carrying", new ValueTypeNode("boolean")))))
        );
        analyzer.getSymbolTable().put("rob", dummy);

        assertThrows(RuntimeException.class, () -> checker.checkAssignment(node));
    }

    @Test
    public void testAssignmentWithInvalidLhsFails() {
        AssignmentNode node = new AssignmentNode(null, new ConstantNode("5"));
        assertThrows(RuntimeException.class, () -> checker.checkAssignment(node));
    }

    @Test
    public void testCheckAssignmentReturnsTrueOnSuccess() {
        DotNode lhs = new DotNode(new IdentifierNode("rob"), "carrying");
        AssignmentNode node = new AssignmentNode(lhs, new ConstantNode("true"));

        SymbolObject dummy = new SymbolObject(
                "rob",
                new ObjectNode("robot", "robots", "rob", new IdentifierNode("rob")),
                new SymbolType("robot", new TypeNode("robot",
                        List.of(new AttributeNode("carrying", new ValueTypeNode("boolean")))))
        );
        analyzer.getSymbolTable().put("rob", dummy);

        assertTrue(checker.checkAssignment(node));
    }

    @Test
    public void testCheckThenBranchHandlesEmptyList() {
        IfNode ifNode = new IfNode(new ConstantNode("true"), new StatementListNode(List.of()));
        List<StatementNode> result = checker.checkThenBranch(ifNode);
        assertTrue(result.isEmpty());
    }
}
