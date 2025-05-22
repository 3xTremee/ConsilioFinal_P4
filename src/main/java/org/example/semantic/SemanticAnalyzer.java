package org.example.semantic;

import java.util.*;

import org.example.ast.*;
import org.example.planner.ExpressionEvaluator;
import org.example.semantic.symbols.*;

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
        // Add types and their attributes to the symbol table
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

        // Check if the domain matches the import name
        if (!domain.getName().equals(problem.getImportName())) {
            throw new SemanticException("Import name mismatch: " + problem.getImportName() + ", Expected: " + domain.getName());
        }

        // Add Object arrays and objects in the same manner
        for (ArrayInitializerNode ai : problem.getArrayInitializers()) {
            String typeName = ai.getType();
            String arrayName = ai.getName();

            // Get the type symbol from the symbol table such that its attributes can be added to the object
            SymbolType typeSymbol = (SymbolType) symbolTable.get(typeName);
            Symbol sym = symbolTable.get(typeName);

            // Check if the type of the array is found in the symbol table
            if (!expressionCheck.arrayInitializerCheck(sym, typeName)) {
                return;
            }

            // Continue if the type exists
            SymbolArray arraySymbol = new SymbolArray(arrayName, ai);
            enterSymbol(arrayName, arraySymbol);

            for (String elementName : ai.getElements()) {
                ObjectNode objectNode = new ObjectNode(typeName, arrayName, elementName, new IdentifierNode(elementName));
                SymbolObject objSymbol = new SymbolObject(elementName, objectNode, typeSymbol);
                    for (SymbolAttribute attrDef : typeSymbol.getAttributes().values()) {
                        objSymbol.setAttributeValue(attrDef.getName(), null);
                }
                arraySymbol.addObjects(objSymbol);
                enterSymbol(elementName, objSymbol);
            }
        }
    }

    // Method for adding the initial values to the objects
    public void addObjectValues(ProblemNode problem) {
        ExpressionEvaluator evaluator = new ExpressionEvaluator(this);

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
                String name = id.getName();
                SymbolObject objSymbol = (SymbolObject) symbolTable.get(name);
                objSymbol.setAttributeValue(field, val);
            }
            else if (exprNode instanceof ArrayAccessNode arr) {
                String arrayName = arr.getArrayName();
                SymbolArray symArr = (SymbolArray) symbolTable.get(arrayName);

                for (ExpressionNode idxExpr : arr.getIndices()) {
                    int i = Integer.parseInt(((ConstantNode)idxExpr).getValueConstant());
                    SymbolObject element = symArr.getObjects().get(i);
                    element.setAttributeValue(field, val);
                }
            }
        }
        // Check if all attributes of all objects are initialized
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
        // Add parameters as temporary SymbolObjects
        for (ParameterNode p : action.getParameters()) {
            // Check if the type is found in the symbol table (Should already be there from the buildSymbolTable method)
            SymbolType typeSym = (SymbolType) symbolTable.get(p.getType());
            if (typeSym == null) {
                throw new SemanticException("Unknown type for parameter: " + p.getType());
            }

            // Making a temporary ObjectNode
            ObjectNode tempNode = new ObjectNode(p.getType(), null, p.getName(), new IdentifierNode(p.getName()));
            SymbolObject parameterSym = new SymbolObject(p.getName(), tempNode, typeSym);

            // Initialize all attributes to null, because there is another check that says objects must be initialized
            for (SymbolAttribute attr : typeSym.getAttributes().values()) {
                parameterSym.setAttributeValue(attr.getName(), null);
            }

            // Put the parameter into the table
            symbolTable.put(p.getName(), parameterSym);
        }

        // Check if the body is an ifNode
        StatementNode body = action.getBody();
        if (!(body instanceof IfNode ifn)) {
            throw new SemanticException("Body of action: '" + action.getName() + "' does not contain an if statement at entry: " + body.getClass());
        }

        // Guard has to be boolean
        String guardType = expressionCheck.typeEvaluation(ifn.getCondition());
        if (!"boolean".equals(guardType)) {
            throw new SemanticException("If-condition should be boolean, received: " + guardType);
        }

        // Check all assignments in the then-branch
        for (StatementNode stNode : statementCheck.checkThenBranch(ifn)) {
            if (stNode instanceof AssignmentNode asNode) {
                statementCheck.checkAssignment(asNode);
            } else {
                throw new SemanticException("Invalid statement in action-body: '" + action.getName() + "' (nested-ifs not currently supported)");
            }
        }

        // Remove the parameters again
        for (ParameterNode p : action.getParameters()) {
            symbolTable.remove(p.getName());
        }
    }
}