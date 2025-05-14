package org.example.semantic;

import java.util.*;

import org.example.ast.*;
import org.example.planner.ExpressionEvaluator;

public class SemanticAnalyzer {

    private final Map<String, Symbol> symbolTable = new LinkedHashMap<>();
    private final ExpressionCheck expressionCheck = new ExpressionCheck(this);
    private final StatementCheck statementCheck = new StatementCheck(this);

    public void enterSymbol(String name, Symbol symbol) {
        if (symbolTable.containsKey(name)) {
            throw new SemanticException("Duplicate declaration of " + symbol.getKind() + ": " + name);
        }
        symbolTable.put(name, symbol);
    }

    public void buildSymbolTable (DomainNode domain, ProblemNode problem) {
        for (TypeNode typeNode : domain.getTypes()) {
            SymbolType typeSymbol = new SymbolType(typeNode.getName(), typeNode);
            enterSymbol(typeNode.getName(), typeSymbol);
            for (AttributeNode attrNode : typeNode.getAttributes()) {
                SymbolAttribute attrSymbol = new SymbolAttribute(
                        attrNode.getIdentifier(),
                        attrNode
                );
                typeSymbol.addAttribute(attrNode.getIdentifier(), attrSymbol);
            }
        }

        for (ActionNode actionNode : domain.getActions()) {
            SymbolAction actionSymbol = new SymbolAction(actionNode.getName(), actionNode);
            enterSymbol(actionNode.getName(), actionSymbol);
        }

        // Check domain import
        if (!domain.getName().equals(problem.getImportName())) {
            throw new SemanticException("Import name mismatch: " + problem.getImportName() + ", Expected: " + domain.getName());
        }

        // Add Object Arrays and Objects
        for (ArrayInitializerNode ai : problem.getArrayInitializers()) {
            String typeName = ai.getType();
            String arrayName = ai.getName();

            //får fat i attributterne og navnet for typen
            SymbolType typeSymbol = (SymbolType) symbolTable.get(typeName);
            Symbol sym = symbolTable.get(typeName);

            // typetjek for om typen som man bruger i et array findes
            if (!expressionCheck.arrayInitializerCheck(sym, typeName)) {
                return;
            }

            // fortsæt hvis typen findes
            SymbolArray arraySymbol = new SymbolArray(arrayName, ai);
            enterSymbol(arrayName, arraySymbol);

            for (String elementName : ai.getElements()) {
                ObjectNode objectNode = new ObjectNode(typeName, arrayName, elementName, new IdentifierNode(elementName));
                SymbolObject objSymbol = new SymbolObject(elementName, objectNode, typeSymbol);
                    for (SymbolAttribute attrDef : typeSymbol.getAttributes().values()) {
                        objSymbol.setAttributeValue(attrDef.getName(), null); // Initialize with null Value
                }
                arraySymbol.addObjects(objSymbol);
                enterSymbol(elementName, objSymbol);
            }
        }
    }

    // Funktion til at tilføje værdier til objekter
    public void addObjectValues(ProblemNode problem) {
        ExpressionEvaluator evaluator = new ExpressionEvaluator(this);
        // Noget der Værdier til attributes til objekter
        // Baseret på initial state listen fra AST builder
        List<StatementNode> initialState = problem.getInit();

        for (StatementNode statement : initialState) {
            if (!(statement instanceof AssignmentNode asn)) {
                continue;
            }
            DotNode dotNode = asn.getTarget();
            ExpressionNode exprNode = dotNode.getTarget();

            expressionCheck.checkDotNodeExpr(dotNode);
            expressionCheck.typeEvaluation(exprNode);
            statementCheck.checkAssignment(asn);

            String field  = dotNode.getField();
            Object val = evaluator.evaluate(asn.getExpression(), null);

            if (exprNode instanceof IdentifierNode id) {
                // Single-object case
                String name = id.getName();
                SymbolObject objSymbol = (SymbolObject) symbolTable.get(name);
                objSymbol.setAttributeValue(field, val);
            }
            else if (exprNode instanceof ArrayAccessNode arr) {
                // Array-of-objects case
                String arrayName = arr.getArrayName();
                SymbolArray symArr = (SymbolArray) symbolTable.get(arrayName);

                for (ExpressionNode idxExpr : arr.getIndices()) {
                    int i = Integer.parseInt(((ConstantNode)idxExpr).getValueConstant());
                    SymbolObject element = symArr.getObjects().get(i);
                    element.setAttributeValue(field, val);
                }
            }
            /* Noget der baseret på assignmentsne fra initial state listen
             Finder værdierne af expressions noden på højre side og tillægger den
             Den rigtige attribut i objektet objSymbol.*/
        }
        for (SymbolObject objSymbol : symbolTable.values().stream()
                .filter(symbol -> symbol instanceof SymbolObject)
                .map(symbol -> (SymbolObject) symbol)
                .toList()) {
            for (String attrName : objSymbol.getAttributes().keySet()) {
                Object value = objSymbol.getAttributeValue(attrName);
                if (value == null) {
                    throw new SemanticException("Attribute " + attrName + " of object " + objSymbol.getName() + " is not initialized.");
                }
            }
        }
    }

    public Map<String, Symbol> getSymbolTable() {
        return symbolTable;
    }

    public void analyzeAction(ActionNode action) {
        // Tilføj parametre som midlertidige SymbolObject
        for (ParameterNode p : action.getParameters()) {
            // slår typen op i table (skal allerede være der fra buildSymbolTable method)
            SymbolType typeSym = (SymbolType) symbolTable.get(p.getType());
            if (typeSym == null) {
                throw new SemanticException("Ukendt type for parameter: " + p.getType());
            }

            // laver en temp ObjectNode
            ObjectNode tempNode = new ObjectNode(p.getType(), null, p.getName(), new IdentifierNode(p.getName()));
            SymbolObject parameterSym = new SymbolObject(p.getName(), tempNode, typeSym);

            // initialiser alle attributter til null, fordi der er et andet tjek som siger at objects skal være initialiseret
            for (SymbolAttribute attr : typeSym.getAttributes().values()) {
                parameterSym.setAttributeValue(attr.getName(), null);
            }

            // put ind i table
            symbolTable.put(p.getName(), parameterSym);
        }

        // Tjek body’en er en ifNode
        StatementNode body = action.getBody();
        if (!(body instanceof IfNode ifn)) {
            throw new SemanticException("IfNode er ikke body, fik: " + body.getClass());
        }

        // guard skal være boolean
        String guardType = expressionCheck.typeEvaluation(ifn.getCondition());
        if (!"boolean".equals(guardType)) {
            throw new SemanticException("If-condition skal være boolean, fik: " + guardType);
        }

        // tjek alle assignments i then-branchen
        for (StatementNode stNode : statementCheck.checkThenBranch(ifn)) {
            if (stNode instanceof AssignmentNode asNode) {
                statementCheck.checkAssignment(asNode);
            } else {
                throw new SemanticException("Ukendt StatementNode i action-body: " + stNode.getClass());
            }
        }

        // Fjern parametre igen
        for (ParameterNode p : action.getParameters()) {
            symbolTable.remove(p.getName());
        }
    }

/*
    //  Print symbol table
    public void printTables() {
        System.out.println("\nSymbol Table:");
        for (Map.Entry<String, Symbol> entry : symbolTable.entrySet()) {
            String name = entry.getKey();
            Symbol symbol = entry.getValue();
            if (symbol instanceof SymbolType) {
                SymbolType typeSymbol = (SymbolType) symbol;
                System.out.println("Type: " + name);
                System.out.println(", Attributes:");
                for (SymbolAttribute attrSymbol : typeSymbol.getAttributes().values()) { // Iterate through values of the map
                    System.out.println("  - Name: " + attrSymbol.getName());
                    for (String possibleType : attrSymbol.getPossibleTypes()) {
                        System.out.println("    - Possible Type: " + possibleType);
                    }
                }
            } else if (symbol instanceof SymbolArray) {
                SymbolArray arraySymbol = (SymbolArray) symbol;
                System.out.println("Array: " + name);
                System.out.println(", Objects:");
                for (SymbolObject objSymbol : arraySymbol.getObjects()) {
                    System.out.println("  - Name: " + objSymbol.getName() +
                            ", Type: " + objSymbol.getType());
                    if (!objSymbol.getAttributes().isEmpty()) {
                        System.out.println("    - Attributes:");
                        for (Map.Entry<String, Object> attrEntry : objSymbol.getAttributes().entrySet()) {
                            System.out.println("      - Name: " + attrEntry.getKey() + ", Value: " + attrEntry.getValue());
                        }
                    }
                }
            }
        }
    }
    */
}