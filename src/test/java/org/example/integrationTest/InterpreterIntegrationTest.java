package org.example.integrationTest;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.example.ConsilioLexer;
import org.example.ConsilioParser;
import org.example.ast.*;
import org.example.parser.AstBuilder;
import org.example.planner.Planner;
import org.example.planner.State;
import org.example.planner.GroundedAction;
import org.example.semantic.SemanticAnalyzer;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class InterpreterIntegrationTest {

    public static ProgramNode parseAndBuildAST(String fileName) throws Exception {
        String content = Files.readString(Paths.get("src/main/java/org/example/files", fileName));
        ConsilioLexer lexer = new ConsilioLexer(CharStreams.fromString(content));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ConsilioParser parser = new ConsilioParser(tokens);
        AstBuilder astBuilder = new AstBuilder();
        ProgramNode program = astBuilder.visitProgram(parser.program());
        return program;
    }

    private List<String> readExpected(String fileName) throws Exception {
        return Files.readAllLines(Paths.get("src/main/java/org/example/files", fileName));
    }

    private List<String> planProgram(ProgramNode program) {
        DomainNode domain = program.getDomain();
        ProblemNode problem = program.getProblem();

        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        analyzer.buildSymbolTable(domain, problem);

        analyzer.addObjectValues(problem);

        Map<String, org.example.semantic.Symbol> symbolTable = analyzer.getSymbolTable();
        State init = State.fromSymbolTable(symbolTable, analyzer);

        Planner planner = new Planner(domain, problem.getObjects(), analyzer);
        Optional<List<GroundedAction>> maybePlan = planner.bfs(init, problem.getExpression());
        assertTrue(maybePlan.isPresent(), "Planner failed to find a plan");
        return maybePlan.get().stream().map(GroundedAction::toString).collect(Collectors.toList());
    }

    @Test
    public void testRobotDomain() throws Exception {
        ProgramNode program = parseAndBuildAST("robotProgram/program.co");
        List<String> expected = readExpected("robotProgram/result.co");

        List<String> actual = planProgram(program);
        assertEquals(expected, actual, "Plan for program.co should match expected result.co");
    }
}
