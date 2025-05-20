package org.example;

import org.example.parser.AstBuilder;
import org.example.ast.*;
import org.example.semantic.*;
import org.example.planner.*;
import org.antlr.v4.runtime.*;
import org.example.semantic.symbols.Symbol;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Main {
    public static void main(String[] args) throws Exception {
        String domainFilePath = "./src/main/java/org/example/files/robotProgram/domain.co";
        String problemFilePath = "./src/main/java/org/example/files/robotProgram/problem.co";
        String outputFilePath = "./src/main/java/org/example/files/robotProgram/program.co";
        concatenateFiles(
                domainFilePath,
                problemFilePath,
                outputFilePath
        );
        CharStream input = CharStreams.fromFileName(outputFilePath);
        ConsilioLexer lexer = new ConsilioLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ConsilioParser parser = new ConsilioParser(tokens);

        // Removes standard error listeners made by ANTLR
        parser.removeErrorListeners();

        // Add a listener that throws an exception on the FIRST syntax error
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?,?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
                    String msg, RecognitionException e)
            {
                throw new RuntimeException("Syntax-error in line " + line + ":" + charPositionInLine + " - " + msg);
            }
        });

        // Parse and build AST
        AstBuilder astBuilder = new AstBuilder();
        ProgramNode program = astBuilder.visitProgram(parser.program());


        // Semantic check
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
        semanticAnalyzer.buildSymbolTable(program.getDomain(), program.getProblem());
        semanticAnalyzer.addObjectValues(program.getProblem());

        // Type checking goals: all goal-expressions MUST be boolean
        ExpressionCheck goalCheck = new ExpressionCheck(semanticAnalyzer);
        for (ExpressionNode goal : program.getProblem().getExpression()) {
            String eval = goalCheck.typeEvaluation(goal);
            if (!"boolean".equals(eval)) {
                throw new RuntimeException("Type-error in goalState: expression '" + goal + "' is not boolean but " + eval);
            }
        }

        // Type check all actions before calling the planner
        for (ActionNode action : program.getDomain().getActions()) {
            semanticAnalyzer.analyzeAction(action);
        }


        // Planning
        Map<String, Symbol> symbolTable = semanticAnalyzer.getSymbolTable();
        State init = State.fromSymbolTable(symbolTable);
        Planner planner = new Planner(
                program.getDomain(),
                program.getProblem().getObjects(),
                semanticAnalyzer
        );
        Optional<List<GroundedAction>> plan = planner.bfs(
                init,
                program.getProblem().getExpression()
        );
        String resultFilePath = "./src/main/java/org/example/files/robotProgram/result.co";

        plan.ifPresentOrElse(
                p -> {
                    try {
                        String planContent = p.stream()
                                .map(Object::toString)
                                .reduce("", (a, b) -> a + b + System.lineSeparator());
                        Files.writeString(Path.of(resultFilePath), planContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                        System.out.println("Plan written to result file:");
                    } catch (IOException e) {
                        System.err.println("An error occurred while writing the plan to the file: " + e.getMessage());
                    }
                },
                () -> {
                    try {
                        System.out.print("No plan found");
                        Files.writeString(
                                Path.of(resultFilePath),
                                "No plan found.",
                                StandardOpenOption.CREATE,
                                StandardOpenOption.TRUNCATE_EXISTING
                        );
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    public static void concatenateFiles(String domainFilePath, String problemFilePath, String outputFilePath) {
        try {
            // Read the contents of both files
            String domainContent = Files.readString(Path.of(domainFilePath));
            String problemContent = Files.readString(Path.of(problemFilePath));

            // Concatenate the contents
            String concatenatedContent = domainContent + System.lineSeparator() + problemContent;

            // Write the concatenated content back to the domain file
            Files.writeString(Path.of(outputFilePath), concatenatedContent, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException e) {
            System.err.println("An error occurred while concatenating files: " + e.getMessage());
        }
    }
}