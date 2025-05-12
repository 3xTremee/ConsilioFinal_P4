package org.example;

import org.example.ast.*;

import org.example.semantic.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UnitTest {
    private SemanticAnalyzer semanticAnalyzer;

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
}
