package org.example.semantic;

import java.util.*;
import org.example.ast.*;

//Symbol table based on AST Nodes
public class SemanticAnalyzer {

    // Hashmaps er unordered, vi kan bruge LinkedHashmap hvis skal deres bruge deres insertion order til noget.
    private static final Map<String, TypeNode> types = new HashMap<>();
    private final Map<String, ActionNode> actions = new HashMap<>();
    private final Map<String, ArrayInitializerNode> arrays = new HashMap<>();
    private final Map<String, ObjectNode> objects = new HashMap<>();

    // Hjælper funktion til at enter et symbol i et given hashmap.
    private <T> void enterSymbol(Map<String, T> table, String name, T symbol, String kind) {
        if (table.containsKey(name)) {
            throw new SemanticException("Duplicate declaration of " + kind + ": " + name);
        }
        table.put(name, symbol);
    }

    public void analyze(DomainNode domain, ProblemNode problem) {
        // --- Symbol Table Construction ---

        // Check types if duplicate types and add them to the symbol table
        for (TypeNode type : domain.getTypes()) {
            enterSymbol(types, type.getName(), type, "Type");
        }

        // Check attributes for valid types - For the user defined types
        for (TypeNode type : domain.getTypes()) {
            for (AttributeNode attr : type.getAttributes()) {
                SemanticAnalyzer.checkValueNode(attr.getValue());
            }
        }

        // Check if duplicate actions and add them to the symbol table
        for (ActionNode action : domain.getActions()) {
            enterSymbol(actions, action.getName(), action, "Action");
            // Check action parameters for valid types
            for (ParameterNode par : action.getParameters()) {
                String typeName = par.getType();
                if (!types.containsKey(typeName)) {
                    throw new SemanticException("Unknown type in action parameter: " + typeName);
                }
            }
        }

        // Check import of domain
        if (!domain.getName().equals(problem.getImportName())) {
            throw new SemanticException("Import name mismatch: " + problem.getImportName() + " Expected: " + domain.getName());
        }


        // Tjek arrays / objects
        for (ArrayInitializerNode ai : problem.getArrayInitializers()) {
            String typeName = ai.getType();
            String arrayName = ai.getName();

            if (!types.containsKey(typeName)) {
                throw new SemanticException("Undeclared type in objects declaration: " + typeName);
            }

            enterSymbol(arrays, arrayName, ai, "array");

            for (String elem : ai.getElements()) {
                enterSymbol(objects, elem, new ObjectNode(ai.getType(), ai.getName(), elem), "object");
            }
        }
        System.out.println(types);
        System.out.println(actions);
        System.out.println(arrays);
        System.out.println(objects);
    }

        // Helper method which checks the value the fields of the types can take.
        /* location: door || room, in this example, there needs to be a valid type definition named door and room for it
         *  to pass the type check. */
        private static void checkValueNode(ValueNode value) {
            if (value instanceof ValueTypeNode) {
                String typeName = ((ValueTypeNode) value).getTypeName();
                if (!(types.containsKey(typeName)
                        || "int".equals(typeName)
                        || "boolean".equals(typeName)))
                    throw new SemanticException("Unknown type in attribute: " + typeName);
            } else if (value instanceof BaseValueNode) {
                String typeName = ((BaseValueNode) value).getValue();
                if (!(types.containsKey(typeName)
                        || "int".equals(typeName)
                        || "boolean".equals(typeName)))
                    throw new SemanticException("Unknown type in attribute: " + typeName);
            } else if (value instanceof ArrayValueNode) {
                checkValueNode(((ArrayValueNode) value).getElementType());
            } else if (value instanceof OrValueNode) {
                for (ValueNode type : ((OrValueNode) value).getTypes()) {
                    checkValueNode(type);
                }
            }
        }

        //For debugging purposes
        public void printTables() {
            System.out.println("Types Table:");
            for (Map.Entry<String, TypeNode> entry : types.entrySet()) {
                System.out.println("Type Name: " + entry.getKey() + ", Type Details: " + entry.getValue());
            }

            System.out.println("\nActions Table:");
            for (Map.Entry<String, ActionNode> entry : actions.entrySet()) {
                System.out.println("Action Name: " + entry.getKey() + ", Action Details: " + entry.getValue());
            }

            System.out.println("\nObjects Table:");
            for (Map.Entry<String, ObjectNode> entry : objects.entrySet()) {
                System.out.println("Object Name: " + entry.getKey() + ", Object Details: " + entry.getValue());
            }
        }
        
        /*
        // Check objects
        for (ObjectNode object : problem.getObjects()) {
            String typeName = object.getType();
            if (!types.containsKey(typeName)) {
                throw new SemanticException("Unknown object type: " + typeName + " for object(s) " + object.getArrayName());
            }
            enterSymbol(objects, object.getElementName(), object, "Object");
        }
        */

        // type check ide
/*
        checkStatement(); Kage
        checkExpression(); Astrid
        checkObjects(); Tilde
        checkInitial();
        checkGoal();

 */


    }
