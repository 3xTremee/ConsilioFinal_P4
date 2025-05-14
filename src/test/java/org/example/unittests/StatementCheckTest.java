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

//Returns if-statements condition
    @Test
    public void testCheckIfStatementReturnsCorrectCondition() {
        ExpressionNode condition = new ConstantNode("true");
        IfNode ifNode = new IfNode(condition,new StatementListNode(List.of()));
        assertEquals(condition, checker.checkIfCondition(ifNode));
    }

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

        TypeNode robotTypeNode = new TypeNode("robot", List.of(new AttributeNode("carrying",new ValueTypeNode("boolean"))));

        SymbolType robotSymbol = new SymbolType("robot", robotTypeNode);
        SymbolObject dummy = new SymbolObject(
                "rob", new ObjectNode("robot", "robots", "rob", new IdentifierNode("rob")), robotSymbol
        );

        analyzer.getSymbolTable().put("rob", dummy);
        analyzer.getSymbolTable().put("robot", robotSymbol);



    }


}
