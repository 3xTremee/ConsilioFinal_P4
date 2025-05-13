package org.example;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.example.ast.*;
import org.example.semantic.SemanticException;
import org.example.semantic.StatementCheck;
import org.example.semantic.SemanticAnalyzer;
import org.example.semantic.ExpressionCheck;

import org.junit.jupiter.api.Test;

public class TypecheckerIntegrationTest {

    private SemanticAnalyzer buildSymbols(ProgramNode program) {
        SemanticAnalyzer sem = new SemanticAnalyzer();
        sem.buildSymbolTable(program.getDomain(), program.getProblem());
        sem.addObjectValues(program.getProblem());
        return sem;
    }

    @Test
    public void testInitAssignmentsTypeCheck() throws Exception {
        ProgramNode program = InterpreterIntegrationTest.parseAndBuildAST("program.co");
        SemanticAnalyzer sem = buildSymbols(program);
        StatementCheck stmtChk = new StatementCheck(sem);

        for (StatementNode stmt : program.getProblem().getInit()) {
            assertTrue(stmt instanceof AssignmentNode, "expected only assignments in initialState, got " + stmt);
            AssignmentNode asn = (AssignmentNode) stmt;
            assertTrue(stmtChk.checkAssignment(asn), "initialState assignment failed type‐check: " + asn);
        }
    }

    @Test
    public void testGoalExpressionCheckIntegration() throws Exception {
        ProgramNode program = InterpreterIntegrationTest.parseAndBuildAST("program.co");
        SemanticAnalyzer sem = buildSymbols(program);
        StatementCheck stmtChk = new StatementCheck(sem);
        for (ExpressionNode expr : program.getProblem().getExpression()) {
            assertEquals("boolean",stmtChk.getExpressionType(expr), "Expected comparison to yield boolean: " + expr);
        }
    }

    @Test
    public void testBadComparisonThrows() throws Exception {
        ProgramNode program = InterpreterIntegrationTest.parseAndBuildAST("typeChecks/goalError.co");
        SemanticAnalyzer sem = buildSymbols(program);
        StatementCheck stmtChk = new StatementCheck(sem);

        // pick the first comparison in the goal and assert we get a SemanticException
        BinaryOpNode bad = program.getProblem()
                .getExpression()
                .stream()
                .filter(e -> e instanceof BinaryOpNode)
                .map(e -> (BinaryOpNode)e)
                .findFirst()
                .orElseThrow();

        assertThrows(SemanticException.class, () -> stmtChk.checkComparison(bad), "Expected a type‐mismatch when comparing " + bad);
    }

    @Test
    public void testBadAssignmentThrows() throws Exception {
        ProgramNode program = InterpreterIntegrationTest.parseAndBuildAST("typeChecks/initError.co");

        SemanticAnalyzer sem = new SemanticAnalyzer();
        sem.buildSymbolTable(program.getDomain(), program.getProblem());

        assertThrows(RuntimeException.class, () -> sem.addObjectValues(program.getProblem()),
                "Expected a bad‐type assignment in initialState to trigger an exception");
    }
}
