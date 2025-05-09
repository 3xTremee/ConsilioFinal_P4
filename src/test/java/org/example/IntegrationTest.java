package org.example;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.example.ast.*;
import org.example.parser.AstBuilder;
import org.example.planner.Planner;
import org.example.planner.State;
import org.example.planner.GroundedAction;
import org.example.semantic.SemanticAnalyzer;
import org.antlr.v4.runtime.tree.ParseTree;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class IntegrationTest {

    private ProgramNode buildProgram(String fileName) throws Exception {
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
        ProgramNode program = buildProgram("program.co");
        List<String> expected = readExpected("result.co");
        System.out.println("Expected plan: " + expected + "\n \n");
        System.out.println("Actual plan: " + planProgram(program));

        List<String> actual = planProgram(program);
        assertEquals(expected, actual, "Plan for program.co should match expected result.co");
    }

    /*
    @Test
    public void testDeliveryDomain() throws Exception {
        ProgramNode program = buildProgram("rescueProgram.co");
        List<String> expected = readExpected("rescueResult.co");
        List<String> actual = planProgram(program);
        assertEquals(expected, actual, "Plan for rescueProgram.co should match expected rescueResult.co");
    }
*/
}
