package org.example;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.example.ast.*;
import org.example.parser.AstBuilder;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ASTBuilderIntegrationTest {

    @Test
    public void testAstOfEmptyProgram(){
        String input = "define domain emptyDomain\n" +
                "define problem emptyProblem\n" +
                "import emptyDomain\n" +
                "objects {}\n" +
                "initialState {}\n" +
                "goalState {}";
        CharStream content = CharStreams.fromString(input);
        ConsilioLexer lexer = new ConsilioLexer(content);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ConsilioParser parser = new ConsilioParser(tokens);
        AstBuilder astBuilder = new AstBuilder();
        ProgramNode program = astBuilder.visitProgram(parser.program());

        DomainNode domain = program.getDomain();
        assertEquals("emptyDomain", domain.getName());
        assertTrue(domain.getTypes().isEmpty(), "Expected no types in domain");
        assertTrue(domain.getActions().isEmpty(), "Expected no actions in domain");

        ProblemNode problem = program.getProblem();
        assertEquals("emptyProblem", problem.getName());
        assertTrue(problem.getObjects().isEmpty(), "Expected no objects in problem");
        assertTrue(problem.getInit().isEmpty(), "Expected no initialState in problem");
        assertTrue(problem.getExpression().isEmpty(), "Expected no goals in problem");
    }

    @Test
    public void testAstWithTypesAndAttributes(){
        String input = "define domain testDomain\n" +
                "type testPerson{ age: int; exists: boolean; name: testPerson;}\n" +
                "type otherPerson{name: testPerson || otherPerson;}\n" +
                "define problem testProblem\n" +
                "import testDomain\n" +
                "objects {}\n" +
                "initialState {}\n" +
                "goalState {}";
        CharStream content = CharStreams.fromString(input);
        ConsilioLexer lexer = new ConsilioLexer(content);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ConsilioParser parser = new ConsilioParser(tokens);
        AstBuilder astBuilder = new AstBuilder();
        ProgramNode program = astBuilder.visitProgram(parser.program());

        DomainNode domain = program.getDomain();
        assertEquals(2, domain.getTypes().size(), "Expected two types in domain");
        TypeNode typeTest = domain.getTypes().getFirst();
        assertEquals("testPerson", typeTest.getName(), "Expected one type with name testPerson");
        assertEquals(3, typeTest.getAttributes().size(), "Expected three attributes in types");

        AttributeNode attrAge = typeTest.getAttributes().get(0);
        assertNotNull(attrAge, "Expected non null attribute age in types");
        ValueNode valueAge = attrAge.getValue();
        assertInstanceOf(BaseValueNode.class, valueAge);
        assertEquals("int", ((BaseValueNode) valueAge).getValue());

        AttributeNode attrExists = typeTest.getAttributes().get(1);
        assertNotNull(attrExists, "Expected non null attribute age in types");
        ValueNode valueExists = attrExists.getValue();
        assertInstanceOf(BaseValueNode.class, valueExists);
        assertEquals("boolean", ((BaseValueNode) valueExists).getValue());

        AttributeNode attrName = typeTest.getAttributes().get(2);
        assertNotNull(attrName, "Expected non null attribute age in types");
        ValueNode valueName = attrName.getValue();
        assertInstanceOf(BaseValueNode.class, valueName);
        assertEquals("testPerson", ((BaseValueNode) valueName).getValue());


        TypeNode typeOther = domain.getTypes().get(1);
        assertEquals("otherPerson", typeOther.getName(), "Expected one type with name otherPerson");
        assertEquals(1, typeOther.getAttributes().size(), "Expected 1 attributes in types");

        AttributeNode attrOtherName = typeOther.getAttributes().get(0);
        assertNotNull(attrOtherName, "Expected non null attribute age in types");
        ValueNode valueOtherName = attrOtherName.getValue();
        assertInstanceOf(OrValueNode.class, valueOtherName);
        List<String> otherTest = new ArrayList<>();
        otherTest.add("[testPerson, otherPerson]");
        assertLinesMatch(otherTest, Collections.singletonList(((OrValueNode) valueOtherName).getTypes().toString()));

    }
}
