package org.example.unittests;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.example.ConsilioLexer;
import org.example.ConsilioParser;
import org.example.ast.*;

import org.example.parser.AstBuilder;
import org.example.semantic.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SemanticAnalyzerTest {
    private SemanticAnalyzer semanticAnalyzer;

    private ProgramNode loadProgram(String filename) throws Exception {
        String code = Files.readString(Paths.get("src/main/java/org/example/files/" + filename));
        CharStream input = CharStreams.fromString(code);
        ConsilioLexer lexer = new ConsilioLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ConsilioParser parser = new ConsilioParser(tokens);
        AstBuilder builder = new AstBuilder();
        return (ProgramNode) builder.visit(parser.program());
    }

    @BeforeEach
    public void setup() {
        semanticAnalyzer = new SemanticAnalyzer();
    }

    //Tests method enterSymbol for ActionNode
    @Test
    public void enterSymbolMethod_ActionTest() {
        assertTrue(semanticAnalyzer.getSymbolTable().isEmpty());

        ActionNode actionNode = new ActionNode("actionTest", Collections.emptyList(), null);
        SymbolAction symbolAction = new SymbolAction("actionTest", actionNode);
        semanticAnalyzer.enterSymbol("actionTest", symbolAction);

        Map<String, Symbol> table = semanticAnalyzer.getSymbolTable();
        assertEquals(1, table.size());
        assertTrue(table.containsKey("actionTest"));
        assertEquals(symbolAction, table.get("actionTest"));
    }

    //Tests method enterSymbol for TypeNode
    @Test
    public void enterSymbolMethod_TypeTest() {
        assertTrue(semanticAnalyzer.getSymbolTable().isEmpty());

        TypeNode typeNode = new TypeNode("typeTest", Collections.emptyList());
        SymbolType symbolType = new SymbolType("typeTest", typeNode);
        semanticAnalyzer.enterSymbol("typeTest", symbolType);

        Map<String, Symbol> table = semanticAnalyzer.getSymbolTable();
        assertEquals(1, table.size());
        assertTrue(table.containsKey("typeTest"));
        assertEquals(symbolType, table.get("typeTest"));
    }

    //Tests arraysInitizalizer with enterSymbol
    @Test
    public void enterSymbolMethod_ArrayInitializerTest() {
        assertTrue(semanticAnalyzer.getSymbolTable().isEmpty());

        ArrayInitializerNode aiNode = new ArrayInitializerNode("Test", "tests", List.of("t1", "t2"));
        SymbolArray symbolArray = new SymbolArray("testArray", aiNode);
        semanticAnalyzer.enterSymbol("testArray", symbolArray);

        Map<String, Symbol> table = semanticAnalyzer.getSymbolTable();
        assertEquals(1, table.size());
        assertTrue(table.containsKey("testArray"));
        assertEquals(symbolArray, table.get("testArray"));
    }

    //Tests exception gets thrown for action
    @Test
    public void exceptionThrown_ActionTest() {
        ActionNode actionNode = new ActionNode("duplicateA", Collections.emptyList(), null);
        SymbolAction symbolAction = new SymbolAction("duplicateA", actionNode);
        semanticAnalyzer.enterSymbol("duplicateA", symbolAction);
        Map<String, Symbol> table = semanticAnalyzer.getSymbolTable();

        assertEquals(1, table.size());

        SemanticException exc = assertThrows(SemanticException.class,
                () -> semanticAnalyzer.enterSymbol("duplicateA", symbolAction));
    }

    //Tests exception gets thrown for type
    @Test
    public void exceptionThrown_TypeTest() {
        TypeNode typeNode = new TypeNode("duplicateT", Collections.emptyList());
        SymbolType symbolType = new SymbolType("duplicateT", typeNode);
        semanticAnalyzer.enterSymbol("duplicateT", symbolType);
        Map<String, Symbol> table = semanticAnalyzer.getSymbolTable();

        assertEquals(1, table.size());

        assertThrows(SemanticException.class,
                () -> semanticAnalyzer.enterSymbol("duplicateT", symbolType));
    }

    //Tests exceptions gets thrown for array
    @Test
    public void exceptionThrown_ArrayTest() {
        ArrayInitializerNode aiNode = new ArrayInitializerNode("Test", "tests", List.of("t1", "t2"));
        SymbolArray symbolArray = new SymbolArray("duplicateArray", aiNode);
        semanticAnalyzer.enterSymbol("duplicateArray", symbolArray);
        Map<String, Symbol> table = semanticAnalyzer.getSymbolTable();
        assertEquals(1, table.size());

        assertThrows(SemanticException.class,
                () -> semanticAnalyzer.enterSymbol("duplicateArray", symbolArray));
    }


    //Tests the method buildSymbolTable in total
    @Test
    public void buildSymbolTableMethod_FullTest() {
        assertTrue(semanticAnalyzer.getSymbolTable().isEmpty());

        /* Man kan lave en stor test, hvor man opretter en action, type osv. */
        Map<String, Symbol> table = semanticAnalyzer.getSymbolTable();
        TypeNode typeNode = new TypeNode("typeTest", Collections.emptyList());
        ActionNode actionNode = new ActionNode("actionTest", Collections.emptyList(), null);
        ArrayInitializerNode aiNode = new ArrayInitializerNode("typeTest", "tests", List.of("t1", "t2"));
        DomainNode domainNode = new DomainNode("domainTest", List.of(typeNode), List.of(actionNode));
        ProblemNode problemNode = new ProblemNode("P1", "domainTest", List.of(aiNode),
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        semanticAnalyzer.buildSymbolTable(domainNode, problemNode);
        assertEquals(5, table.size());

        Set<String> expected = Set.of("typeTest", "actionTest", "tests", "t1", "t2");
        assertEquals(expected, semanticAnalyzer.getSymbolTable().keySet());
    }

    //Tests that you cannot declare an object of a type that does not exist
    @Test
    public void exceptionThrown_buildSymbolTableTest() {
        Map<String, Symbol> table = semanticAnalyzer.getSymbolTable();
        TypeNode typeNode = new TypeNode("typeTest", Collections.emptyList());
        ActionNode actionNode = new ActionNode("actionTest", Collections.emptyList(), null);
        ArrayInitializerNode aiNode = new ArrayInitializerNode("nonExistingType", "tests", List.of("t1", "t2"));
        DomainNode domainNode = new DomainNode("domainTest", List.of(typeNode), List.of(actionNode));
        ProblemNode problemNode = new ProblemNode("P1", "domainTest", List.of(aiNode),
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        assertThrows(SemanticException.class,
                () -> semanticAnalyzer.buildSymbolTable(domainNode, problemNode));
    }

// domain and problem build the symbol table
    @Test
    public void testValidProgramAnalysis() throws Exception {
        ProgramNode program = loadProgram("program.co");
        assertDoesNotThrow(() -> {
            semanticAnalyzer.buildSymbolTable(program.getDomain(), program.getProblem());
            semanticAnalyzer.addObjectValues(program.getProblem());
        });
    }

    @Test
    public void testMissingAttributInitializationThrows() throws Exception {
        ProgramNode program = loadProgram("program.co");
        semanticAnalyzer.buildSymbolTable(program.getDomain(), program.getProblem());
        assertThrows(SemanticException.class, () -> semanticAnalyzer.addObjectValues(program.getProblem()));
    }

// =================SEMANTIK UNIT TESTS - FYLDER MEGET=================================
    //====================================================================================
    @Test
    public void testValidProgramWorks() {
        String input = """
    define domain robotDomain

    type robot {
        location: room;
        carrying: boolean;
    }

    type package {
        location: room || robot;
    }

    type room {
        numberOfItems: int;
    }

    action pickUpPackage(robot r, package p) {
        if (r.carrying == false && r.location == p.location) {
            p.location = r;
            r.carrying = true;
        }
    }

    action move(robot r, room dest) {
        if (r.location != dest) {
            r.location = dest;
        }
    }

    action putDownPackage(robot r, package p, room rm) {
        if (r.carrying == true && p.location == r && r.location == rm) {
            p.location = rm;
            r.carrying = false;
        }
    }

    define problem movePackagesDemo
    import robotDomain

    objects {
        robot robots[] = {rob};
        package packages[] = {pack1, pack2};
        package redPackages[] = {rPack1, rPack2};
        room rooms[] = {A, B, C};
    }

    initialState {
        rob.location = A;
        rob.carrying = false;

        packages[0,1].location = A;
        redPackages[0,1].location = C;

        A.numberOfItems = 5 + 6 + 10 + 8;
        B.numberOfItems = 2;
        C.numberOfItems = 4;
    }

    goalState {
        packages[0].location == C;
        pack2.location == B;
        redPackages[0,1].location == A;
    }
    """;

        assertSemanticSuccess(input);
    }

    @Test
    public void testDuplicateActions() {
        String input = """
        define domain robotDomain

        type robot {
            location: room;
            carrying: boolean;
        }

        type room {
            numberOfItems: int;
        }

        action move(robot r, room d) {
            r.location = d;
        }

        action move(robot r, room d) {
            r.location = d;
        }

        define problem p
        import robotDomain

        objects {
            robot robots[] = {rob};
            room rooms[] = {A};
        }

        initialState {
            rob.location = A;
            rob.carrying = false;
            A.numberOfItems = 0;
        }

        goalState {
            rob.carrying == true;
        }
        """;
        assertSemanticError(input);
    }
/*
    @Test
    public void testDuplicateFieldsInType() {
        String input = """
        define domain robotDomain

        type robot {
            location: room;
            location: boolean;
        }

        type room {
            numberOfItems: int;
        }

        action set(robot r, room d) {
            r.location = d;
        }

        define problem p
        import robotDomain

        objects {
            robot robots[] = {rob};
            room rooms[] = {A};
        }

        initialState {
            rob.location = A;
            A.numberOfItems = 0;
        }

        goalState {
            rob.location == A;
        }
        """;
        assertSemanticError(input);
    }
*/
    @Test
    public void testMismatchedDomainNameInImport() {
        String input = """
        define domain domainA

        type robot {
            carrying: boolean;
            location: room;
        }

        type room {
            numberOfItems: int;
        }

        action set(robot r, room d) {
            r.location = d;
        }

        define problem p
        import robotDomain

        objects {
            robot robots[] = {rob};
            room rooms[] = {A};
        }

        initialState {
            rob.location = A;
            rob.carrying = false;
            A.numberOfItems = 0;
        }

        goalState {
            rob.carrying == true;
        }
        """;
        assertSemanticError(input);
    }

    @Test
    public void testDuplicateObjectArrays() {
        String input = """
        define domain robotDomain

        type robot {
            carrying: boolean;
            location: room;
        }

        type room {
            numberOfItems: int;
        }

        action set(robot r, room d) {
            if (r.location != d) {
                r.location = d;
            }
        }

        define problem p
        import robotDomain

        objects {
            robot robots[] = {rob};
            robot robots[] = {rob2};
            room rooms[] = {A};
        }

        initialState {
            rob.location = A;
            rob.carrying = false;
            A.numberOfItems = 0;
        }

        goalState {
            rob.carrying == true;
        }
        """;
        assertSemanticError(input);
    }




/* igen idk om skal checkes semantisk.
    //test for assign forkert type
    @Test
    public void testAssignmentTypeMismatch() {
        String input = """
        define domain robotDomain

        type robot {
            carrying: boolean;
            location: room;
        }

        type room {
            numberOfItems: int;
        }

        action assignBoolToInt(robot r, room d) {
            r.carrying = 42;
        }

        define problem p
        import robotDomain

        objects {
            robot robots[] = {rob};
            room rooms[] = {A};
        }

        initialState {
            rob.location = A;
            rob.carrying = false;
            A.numberOfItems = 0;
        }

        goalState {
            rob.carrying == true;
        }
        """;
        assertSemanticError(input);
    }
*/
    /* øhm den her test virker ikke fordi bliver vel bare fanget i expression evaluator istedet for. måske skal dette ændrefixes at some point eller bare slet tests.
    @Test
    public void testAssignmentToUndeclaredField() {
        String input = """
        define domain robotDomain

        type robot {
            carrying: boolean;
            location: room;
        }

        type room {
            numberOfItems: int;
        }

        action set(robot r, room d) {
            r.unknown = d;
        }

        define problem p
        import robotDomain

        objects {
            robot robots[] = {rob};
            room rooms[] = {A};
        }

        initialState {
            rob.location = A;
            rob.carrying = false;
            A.numberOfItems = 0;
        }

        goalState {
            rob.carrying == true;
        }
        """;
        assertSemanticError(input);
    }

     @Test
    public void testAssignmentToUndeclaredObject() {
        String input = """
        define domain robotDomain

        type robot {
            carrying: boolean;
            location: room;
        }

        type room {
            numberOfItems: int;
        }

        action set(robot r, room d) {
            ghost.carrying = true;
        }

        define problem p
        import robotDomain

        objects {
            robot robots[] = {rob};
            room rooms[] = {A};
        }

        initialState {
            rob.location = A;
            rob.carrying = false;
            A.numberOfItems = 0;
        }

        goalState {
            rob.carrying == true;
        }
        """;
        assertSemanticError(input);
    }
*/

    // Parses and builds the AST, then checks if semantic analysis fails as expected
    private void assertSemanticError(String input) {
        ConsilioLexer lexer = new ConsilioLexer(CharStreams.fromString(input));
        ConsilioParser parser = new ConsilioParser(new CommonTokenStream(lexer));
        ParseTree tree = parser.program();

        AstBuilder builder = new AstBuilder();
        ProgramNode program = builder.visitProgram((ConsilioParser.ProgramContext) tree);

        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        assertThrows(SemanticException.class, () ->
                analyzer.buildSymbolTable(program.getDomain(), program.getProblem()));
    }
    private void assertSemanticSuccess(String input) {
        ConsilioLexer lexer = new ConsilioLexer(CharStreams.fromString(input));
        ConsilioParser parser = new ConsilioParser(new CommonTokenStream(lexer));
        ParseTree tree = parser.program();

        AstBuilder builder = new AstBuilder();
        ProgramNode program = builder.visitProgram((ConsilioParser.ProgramContext) tree);

        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        analyzer.buildSymbolTable(program.getDomain(), program.getProblem());
    }

}
