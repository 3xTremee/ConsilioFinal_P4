package org.example.semantic;

import org.example.ast.*;
import org.example.semantic.symbols.*;

import java.util.*;

public class ExpressionCheck {

    SemanticAnalyzer semanticAnalyzer;
    Map<String, Symbol> symbolTable;

    // Constructor to create its own SemanticAnalyzer
    public ExpressionCheck(SemanticAnalyzer semanticAnalyzer) {
        this.semanticAnalyzer = semanticAnalyzer;
        this.symbolTable = semanticAnalyzer.getSymbolTable();
    }

    public boolean checkInt(ConstantNode c) {
        String v = c.getValueConstant();

        try {
            Integer.parseInt(v);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean checkBool(ConstantNode c) {
        String v = c.getValueConstant();
        return "true".equals(v) || "false".equals(v);
    }

    // Method to check if the identifier is found in the symbolTable
    public String checkIdentifier(IdentifierNode id) {
        String name = id.getName();

        if (symbolTable.containsKey(name)) {
            return name;
        }
        throw new NullPointerException("Identifier " + name + " not found in symbol table");
    }


    // Finds the type of an object (from SymbolObject) in the symbolTable based on the name of the suiting identifier
    public SymbolType checkObject(IdentifierNode id) {
        String name = checkIdentifier(id);
        Symbol symbol = symbolTable.get(name);

        if (!(symbol instanceof SymbolObject)) {
            throw new RuntimeException("Undeclared type in objects declaration: " + id);
        }
        return ((SymbolObject) symbol).getSymbolType();
    }

    public Boolean typeExists(String typeName) {
        return symbolTable.containsKey(typeName);
    }

    public Boolean arrayInitializerCheck(Symbol sym, String typeName){
        if (sym == null || !typeExists(typeName)) {
            throw new SemanticException("Undeclared type in objects declaration: " + typeName);
        }
        return true;
    }

    // Method checking which types the values of the attribute must have
    public List<String> attributeCheck(SymbolType sym, SymbolAttribute attribute) {
        Map<String, SymbolAttribute> attributes = sym.getAttributes();

        SymbolAttribute symbolAttribute = attributes.get(attribute.getName());

        if (symbolAttribute == null){
            throw new SemanticException("Undeclared attribute in objects declaration: " + attribute.getName());
        } else {
            return symbolAttribute.getPossibleTypes();
        }
    }

    public boolean checkArrayDot(ArrayAccessNode arr, String fieldName){
        SymbolArray symArr = (SymbolArray) symbolTable.get(arr.getArrayName());
        if (symArr == null){
            throw new SemanticException("Array '" + arr.getArrayName() + "' not defined");
        }
        // for each constant index, check that object has that field
        for (ExpressionNode idxExpr : arr.getIndices()) {

            // Throws and exception if index is not a constant or matches, where d is a digit and + Kleene plus (one or more). \\d+ is a regex
            if (!(idxExpr instanceof ConstantNode c) || !c.getValueConstant().matches("\\d+")){
                throw new SemanticException("Invalid index in initializer for '" + arr.getArrayName() + "'");
            }
            int i = Integer.parseInt(c.getValueConstant());
            if (i < 0 || i >= symArr.getObjects().size()) {
                throw new SemanticException("Index " + i + " out of bounds for '" + arr.getArrayName() + "'");
            }
            SymbolObject obj = symArr.getObjects().get(i);
            SymbolType objType = obj.getSymbolType();
            if (!objType.getAttributes().containsKey(fieldName)) {
                throw new SemanticException("Attribute '" + fieldName + "' not on object '" + obj.getName() + "' of type " + objType.getName());
            }
        }
        return true;
    }

    public boolean checkDot(IdentifierNode id, String fieldName, ExpressionNode target){
        SymbolType symbolType = checkObject(id);

        /*
         * Create a datastructure for all keys from the symbolTypes maps attributes. The keys are names on the attributes
         * The Set is used as a quick look up to see if a fieldName is an attribute for the type
         */
        Set<String> attributeNames = symbolType.getAttributes().keySet();
        String targetName = ((IdentifierNode) target).getName(); // Prints the name, could be rob
        String typeName = symbolType.getName(); // prints the type, could be robot

        if (!typeExists(symbolType.getName())) {
            throw new SemanticException("Object: " + targetName + " is of unrecognized type: " + typeName);
        }

        if (attributeNames.contains(fieldName)) {
            //System.out.println("Attribute: " + fieldName + " is possible for type: " + symbolType.getName());
            return true;
        } else {
            //System.out.println("Attribute: " + fieldName + " not possible for type: " + symbolType.getName());
            throw new RuntimeException("Attribute: " + fieldName + " not possible for object: " + targetName + " of type: " + typeName);
        }
    }

    /*
     * Method that checks if the target is part of objects, and find the types on the object
     * Checks if attribute can be used on the object of that type
     */
    public boolean checkDotNodeExpr (DotNode dotNode) {
        ExpressionNode target = dotNode.getTarget();
        String fieldName = dotNode.getField();
        if (target instanceof IdentifierNode id) {
            return checkDot(id, fieldName, target);
        }
        else if (target instanceof ArrayAccessNode arr) {
            return checkArrayDot(arr, fieldName);
        } else {
            throw new SemanticException("Unsupported dot target: " + target);
        }
    }

    private String typeEvalIdentifier(IdentifierNode id) {
        String name = checkIdentifier(id);
        Symbol sym = symbolTable.get(name);
        if (sym == null) {
            throw new RuntimeException("Identifier " + name + " not found in symbol table");
        }
        if (sym instanceof SymbolObject) {
            // returns the type of the object/parameter, not the name itself
            return checkObject(id).getName();
        } else {
            // returns the name of the identifier represents something other than an object. (Before it would throw an exception)
            return name;
        }
    }

    private String typeEvalArray(ArrayAccessNode arr) {
        SymbolArray sa = (SymbolArray) symbolTable.get(arr.getArrayName());
        Set<String> types = new LinkedHashSet<>();
        for (ExpressionNode idx : arr.getIndices()) {
            int i = Integer.parseInt(((ConstantNode)idx).getValueConstant());
            types.add(sa.getObjects().get(i).getSymbolType().getName());
        }
        return (types.size() == 1)
                ? types.iterator().next()
                : String.join("||", types);
    }

    private String typeEvalDot(DotNode dn, ExpressionNode expr){
        // Checks if the attribute exists
        if (!checkDotNodeExpr(dn)) {
            throw new SemanticException("Invalid dot expression: " + dn);
        }
        ExpressionNode target = dn.getTarget();

        SymbolType ownerType;

        if (target instanceof IdentifierNode id) {
            // Find the type of the object. Could be 'robot', from robot robots[]...
            ownerType = checkObject(id);
        }
        else if (target instanceof ArrayAccessNode arr) {
            SymbolArray symArr = (SymbolArray) symbolTable.get(arr.getArrayName());
            int idx = Integer.parseInt(((ConstantNode)arr.getIndices().getFirst()).getValueConstant());
            SymbolObject elem = symArr.getObjects().get(idx);
            ownerType = elem.getSymbolType();
        }
        else {
            throw new SemanticException("Unsupported target in expr: " + expr);
        }

        // Finds the SymbolAttribute for the field. Could be an attribute like 'carrying'
        SymbolAttribute attr = ownerType.getAttributes().get(dn.getField());
        List<String> poss = attr.getPossibleTypes();
        if (poss.size() == 1) {
            return poss.getFirst();
        } else {
            // Joins union types seperated by '||'
            return String.join("||", poss);
        }
    }

    public String typeEvaluation(ExpressionNode expr){
        if (expr instanceof ConstantNode){
            if (checkBool((ConstantNode) expr)){
                return "boolean";
            } else if (checkInt((ConstantNode) expr)) {
                return "int";
            }
        }
        else if (expr instanceof IdentifierNode id) {
            return typeEvalIdentifier(id);
        }
        else if (expr instanceof ArrayAccessNode arr) {
            return typeEvalArray(arr);
        }
        else if (expr instanceof DotNode dn) {
            return typeEvalDot(dn, expr);
        }
        else if(expr instanceof ParenExpressionNode) {
            ExpressionNode inner = ((ParenExpressionNode) expr).getInner();
            return typeEvaluation(inner);

        } else if (expr instanceof BinaryOpNode) {
            return binOpEval((BinaryOpNode) expr);
        }
        throw new RuntimeException("Invalid ExpressionNode in Expression: " + expr.getClass().getSimpleName());
    }

    // Method which returns the type of the evaluation
    public String binOpEval(BinaryOpNode opNode) {
        String op = opNode.getOperator();
        String leftType = typeEvaluation(opNode.getLeft());
        String rightType = typeEvaluation(opNode.getRight());
        switch (op) {
            case "+", "-":
                if ("int".equals(leftType) && "int".equals(rightType)) {
                    return "int";
                }
                else{
                    throw new SemanticException("Invalid types: '" + leftType + "' and: '" + rightType + "' for arithmetic operation");
                }
            case ">", "<", ">=", "<=":
                if ("int".equals(leftType) && "int".equals(rightType)) {
                    return "boolean";
                }
                break;

            case "&&", "||":
                if ("boolean".equals(leftType) && "boolean".equals(rightType)) {
                    return "boolean";
                }
                break;

            case "==", "!=":
                // Splits union types on "||"
                Set<String> leftSet = new HashSet<>(Arrays.asList(leftType.split("\\|\\|")));
                Set<String> rightSet = new HashSet<>(Arrays.asList(rightType.split("\\|\\|")));

                // Keep the types that reoccurs on both sides. Like the intersection from DTG course.
                leftSet.retainAll(rightSet);
                if (!leftSet.isEmpty()) {
                    return "boolean";
                }
                break;

            default:
                throw new SemanticException("Invalid operator: " + op);
        }
        throw new RuntimeException("Not compatible types: " + leftType + " and " + rightType);
    }
}