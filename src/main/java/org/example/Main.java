package org.example;

import org.example.parser.AstBuilder;
import org.example.ast.*;
import org.example.semantic.*;
import org.example.planner.*;
import org.antlr.v4.runtime.*;
import org.w3c.dom.Attr;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Main {
    public static void main(String[] args) throws Exception {
        String domainFilePath = "./src/main/java/org/example/files/domain.co";
        String problemFilePath = "./src/main/java/org/example/files/problem.co";
        String outputFilePath = "./src/main/java/org/example/files/program.co";
        concatenateFiles(
                domainFilePath,
                problemFilePath,
                outputFilePath
        );
        CharStream input = CharStreams.fromFileName(outputFilePath);
        ConsilioLexer lexer = new ConsilioLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ConsilioParser parser = new ConsilioParser(tokens);

        // Fjerner standard‐error listeners som er lavet af ANTLR
        parser.removeErrorListeners();

        // Tilføj en listener som kaster exception ved FØRSTE syntax fejl
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?,?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
                    String msg, RecognitionException e)
            {
                throw new RuntimeException("Syntaxfejl ved linje " + line + ":" + charPositionInLine + " – " + msg);
            }
        });

        // Parser og bygger AST
        AstBuilder astBuilder = new AstBuilder();
        ProgramNode program = astBuilder.visitProgram(parser.program());

        // Bare for at printe AST. Skal bare slettes igen på et tidspunkt så den ikke printer AST
        //System.out.println(program);

        // Semantic tjek
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
        semanticAnalyzer.buildSymbolTable(program.getDomain(), program.getProblem());
        // Typechecking
        semanticAnalyzer.addObjectValues(program.getProblem());

        //Type tjekker goals: alle goal-expressions SKAL være boolean
        ExpressionCheck goalCheck = new ExpressionCheck(semanticAnalyzer);
        for (ExpressionNode goal : program.getProblem().getExpression()) {
            String eval = goalCheck.typeEvaluation(goal);
            if (!"boolean".equals(eval)) {
                throw new RuntimeException("Type-fejl i goalState: udtryk '" + goal + "' er ikke boolean men " + eval);
            }
        }

        // Type tjekker hver action inden planneren bliver kaldt
        for (ActionNode action : program.getDomain().getActions()) {
            semanticAnalyzer.analyzeAction(action);
        }

        // Print af tabeller
        //semanticAnalyzer.printTables();

        /*
        // Test af expressioncheck
        ExpressionCheck expressionCheck = new ExpressionCheck(semanticAnalyzer);
        ProblemNode problem = program.getProblem();

        List<ArrayInitializerNode> arraysInProblem = problem.getArrayInitializers();

        for (ArrayInitializerNode array : arraysInProblem) {
            IdentifierNode identifierNode = new IdentifierNode(array.getName());
            expressionCheck.checkIdentifier(identifierNode);
        }

        List<ObjectNode> objectsInProblem = problem.getObjects();

        for (ObjectNode object : objectsInProblem) {
            String objectType = expressionCheck.checkObject(object.getIdentifier());
            expressionCheck.typeExists(objectType);
        }

         */


        // Planning
        Map<String, Symbol> symbolTable = semanticAnalyzer.getSymbolTable();
        State init = State.fromSymbolTable(symbolTable, semanticAnalyzer);
        Planner planner = new Planner(
                program.getDomain(),
                program.getProblem().getObjects(),
                //new StatementCheck(semanticAnalyzer), // Denne var med før, men StatementCheck er fjernet fra planner constructor
                semanticAnalyzer
        );
        Optional<List<GroundedAction>> plan = planner.bfs(
                init,
                program.getProblem().getExpression()
        );
        String resultFilePath = "./src/main/java/org/example/files/result.co";

        plan.ifPresentOrElse(
                p -> {
                    try {
                        String planContent = p.stream()
                                .map(Object::toString)
                                .reduce("", (a, b) -> a + b + System.lineSeparator());
                        Files.writeString(Path.of(resultFilePath), planContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                        System.out.println("Plan written to result.co");
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