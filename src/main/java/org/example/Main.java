package org.example;

import org.example.parser.AstBuilder;
import org.example.ast.*;
import org.example.semantic.SemanticAnalyzer;
//import org.example.planner.*;
import org.antlr.v4.runtime.*;

public class Main {
    public static void main(String[] args) throws Exception {
        CharStream input = CharStreams.fromFileName("./src/main/java/org/example/test.co");
        ConsilioLexer lexer = new ConsilioLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ConsilioParser parser = new ConsilioParser(tokens);

        // Parser og bygger AST
        AstBuilder astBuilder = new AstBuilder();
        ProgramNode program = astBuilder.visitProgram(parser.program());

        // Bare for at printe AST. Skal bare slettes igen på et tidspunkt så den ikke printer AST
        //System.out.println(program);

        // Semantic tjek (pt uden typetjek)
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
        semanticAnalyzer.analyze(program.getDomain(), program.getProblem());

        // Print af tabeller
        //semanticAnalyzer.printTables();

    }
}