package org.example.semantic;

import org.example.ast.*;

import java.util.*;

public class ExpressionCheck {

    //kaldes injektion
    SemanticAnalyzer semanticAnalyzer;
    Map<String, Symbol> symbolTable;

    // Constructor for at den har sin egen SemanticAnalyzer
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

    // Metode til at tjekke om
    public String checkIdentifier(IdentifierNode id) {       
        String name = id.getName();                                                 // get name of IdentifierNode

        if (symbolTable.containsKey(name)) {
            //System.out.println("Identifier " + name + " exists");
            return name;
        }
        throw new NullPointerException("Identifier " + name + " not found in symbol table");
    }

    //Her findes typen (type) af et object (SymbolObject) i symboltabellen baseret på navnet
    // på en identifier-node (id)
    public SymbolType checkObject(IdentifierNode id) {

        String name = checkIdentifier(id);
        Symbol symbol = symbolTable.get(name);                            //find tilhørende Symbol for targetName

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
            // Kaster exception hvis index ikke er en konstant eller matcher (\d+) hvor 'd' er integer og '+' er en eller flere.
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

            /* Laver en datastruktur over alle nøgler fra symbolTypes map attributes. Nøglerne er navne på attributterne
               Set bruges som hurtigt opslag for at se om fieldName er en attribut for typen*/
        Set<String> attributeNames = symbolType.getAttributes().keySet();

        if (attributeNames.contains(fieldName)) {
            //System.out.println("Attribute: " + fieldName + " is possible for type: " + symbolType.getName());
            return true;
        } else {
            //System.out.println("Attribute: " + fieldName + " not possible for type: " + symbolType.getName());
            String targetName = ((IdentifierNode) target).getName();     //Printer kun navnet fx rob
            throw new RuntimeException("Attribute: " + fieldName + " not possible for object: " + targetName + " of type: " + symbolType.getName());
        }
    }

    //metode tjekker om target er i objekts, samt finder typen på objekt
    //derudover tjekker den om attributten må bruges på objekt af den type
    //hvis den må så returneres true.
    public boolean checkDotNodeExpr (DotNode dotNode) {
        ExpressionNode target = dotNode.getTarget();                 //Printer: targetName: IdentifierNode= Name:rob
        String fieldName = dotNode.getField();                       //printer field navn
        if (target instanceof IdentifierNode id) {
            return checkDot(id, fieldName, target);
        }
        else if (target instanceof ArrayAccessNode arr) {
            return checkArrayDot(arr, fieldName);
        } else {
            throw new SemanticException("Unsupported dot‐target: " + target);
        }
    }

    private String typeEvalIdentifier(IdentifierNode id) {
        String name = checkIdentifier(id);
        Symbol sym = symbolTable.get(name);
        if (sym == null) {
            throw new RuntimeException("Identifier " + name + " not found in symbol table");
        }
        if (sym instanceof SymbolObject) {
            // Returner typen på objektet/parameteren, ikke selve navnet
            return checkObject(id).getName();
        } else {
            // returnerer bare navnet hvis nu identifieren repræsenterer noget andet end et objekt (før lavede den en exception)
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
        // Tjek først at attributten findes
        if (!checkDotNodeExpr(dn)) {
            throw new SemanticException("Invalid dot‐expression: " + dn);
        }
        ExpressionNode target = dn.getTarget();

        SymbolType ownerType;

        if (target instanceof IdentifierNode id) {
            // Find typen på objektet (f.eks. robot)
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

        // Find SymbolAttribute for feltet (f.eks. carrying)
        SymbolAttribute attr = ownerType.getAttributes().get(dn.getField());
        List<String> poss = attr.getPossibleTypes();
        if (poss.size() == 1) {
            return poss.getFirst();
        } else {
            // Fletter union typer sammen
            return String.join("||", poss);
        }
    }

    public String typeEvaluation(ExpressionNode expr){
        if (expr instanceof ConstantNode){
            if (checkBool((ConstantNode) expr)){
                //System.out.println("bool: " + v);
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
        //System.out.println("Comparing types for op " + op + ": left=" + typeEvaluation(opNode.getLeft()) + " right=" + typeEvaluation(opNode.getRight()));
        //System.out.println(">> binOpEval " + op + ": left=" + leftType +  " right=" + rightType);
        //System.out.println(">> binOpEval " + op + ": venstre=" + leftType + ", højre=" + rightType + " (expression: " + opNode + ")");
        switch (op) {
            case "+", "-":
                if ("int".equals(leftType) && "int".equals(rightType)) {
                    return "int";
                }
                break;

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
                // Split union-typer på "||"
                Set<String> leftSet  = new HashSet<>(Arrays.asList(leftType.split("\\|\\|")));
                Set<String> rightSet = new HashSet<>(Arrays.asList(rightType.split("\\|\\|")));
                // Behold kun de typer, der går igen. Ligesom intersection fra DTG
                leftSet.retainAll(rightSet);
                if (!leftSet.isEmpty()) {
                    return "boolean";
                }
                break;

            default:
                throw new SemanticException("Ukendt operator: " + op);
        }

        throw new RuntimeException(
                "Not compatible types: " + leftType + " and " + rightType
        );
    }

}