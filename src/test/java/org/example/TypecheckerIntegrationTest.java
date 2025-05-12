package org.example;

import static org.junit.jupiter.api.Assertions.*;

import org.example.ast.ProgramNode;
import org.example.ast.ExpressionNode;
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
    public void testExpressionCheckIntegration() throws Exception {
        ProgramNode program = InterpreterIntegrationTest.parseAndBuildAST("program.co");
        SemanticAnalyzer sem = buildSymbols(program);
        ExpressionCheck checker = new ExpressionCheck(sem);
        for (ExpressionNode expr : program.getProblem().getExpression()) {
            String type = checker.typeEvaluation(expr);
            assertEquals("boolean", type, "Goal expression should type-check to boolean");
        }
    }
}
