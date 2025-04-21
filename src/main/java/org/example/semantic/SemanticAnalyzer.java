package org.example.semantic;

import java.util.*;
import org.example.ast.*;

public class SemanticAnalyzer {
    private final Map<String, TypeNode> types = new HashMap<>();
    private final Map<String, ActionNode> actions = new HashMap<>();
    private final SymbolTable symbols = new SymbolTable();

    public void analyze(DomainNode domain, ProblemNode problem) {

        for (TypeNode type : domain.getTypes()) {
            symbols.enterSymbol(type.getName(), new SymbolInfo(SymbolInfo.Kind.TYPE, type));
            for (AttributeNode attribute : type.getAttributes()) {
                symbols.enterSymbol(type.getName() + "." + attribute.getIdentifier(), new SymbolInfo(SymbolInfo.Kind.ATTRIBUTE, attribute));
            }
        }

        for (ActionNode action : domain.getActions()) {
            symbols.enterSymbol(action.getName(), new SymbolInfo(SymbolInfo.Kind.ACTION, action));
        }


        for (ObjectNode object : problem.getObjects()) {
            String typeName = object.getType();
            String arrayName = object.getArrayName();
            String elementName = object.getElementName();

            System.out.println(arrayName);

            symbols.retrieveSymbol(typeName);

            if (!symbols.isInCurrentScope(arrayName)) {
                symbols.enterSymbol(arrayName, new SymbolInfo(SymbolInfo.Kind.ARRAY, typeName));
            }

            // robot robots[] = {r1, r2}; room rooms[] = {r1}; -> error
            if (symbols.isInCurrentScope(elementName)) {
                throw new SemanticException("Element '" + elementName + "' is already declared");
            }
            symbols.enterSymbol(elementName, new SymbolInfo(SymbolInfo.Kind.OBJECT, object));

        }
        
        /* d
        // Tjek typer
        for (TypeNode type : domain.getTypes()) {
            if (types.containsKey(type.getName())) {
                throw new SemanticException("Type duplicate: " + type.getName());
            } else {
                types.put(type.getName(), type);
            }
        }

        // Tjek actions
        for (ActionNode action : domain.getActions()) {
            if (actions.containsKey(action.getName())) {
                throw new SemanticException("Action duplicate: " + action.getName());
            } else {
                actions.put(action.getName(), action);
            }
        }
         */

        // Tjek import af domain
        if (!domain.getName().equals(problem.getImportName())) {
            throw new SemanticException("Import name mismatch: " + problem.getImportName() + " Expected: " + domain.getName());
        }
    }
}
