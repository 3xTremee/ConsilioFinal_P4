package org.example.semantic;

import com.ibm.icu.text.SymbolTable;
import org.antlr.v4.parse.v4ParserException;
import org.example.ast.*;

import java.beans.Expression;
import java.util.*;
import java.util.stream.Collectors;

public class ExpressionCheck {

    //kaldes injektion
    SemanticAnalyzer semanticAnalyzer;

    // Constructor for at den har sin egen SemanticAnalyzer
    public ExpressionCheck(SemanticAnalyzer semanticAnalyzer) {
        this.semanticAnalyzer = semanticAnalyzer;
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
        Map<String, Symbol> symbolTable = semanticAnalyzer.getSymbolTable();        // get symbol table
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
        Map<String, Symbol> symbolTable = semanticAnalyzer.getSymbolTable();

        String name = checkIdentifier(id);
        Symbol symbol = symbolTable.get(name);                            //find tilhørende Symbol for targetName

        if (!(symbol instanceof SymbolObject)) {
            throw new RuntimeException("Undeclared type in objects declaration: " + id);
        }
        return ((SymbolObject) symbol).getSymbolType();
    }

    public Boolean typeExists(String typeName) {
        Map<String, Symbol> symbolTable = semanticAnalyzer.getSymbolTable();

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

    //metode tjekker om target er i objekts, samt finder typen på objekt
    //derudover tjekker den om attributten må bruges på objekt af den type
    //hvis den må så returneres true.
    public boolean checkDotNode (DotNode dotNode) {
        ExpressionNode target = dotNode.getTarget();                 //Printer: targetName: IdentifierNode= Name:rob
        String fieldName = dotNode.getField();                       //printer field navn

        if (target instanceof IdentifierNode id) {
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
        else if (target instanceof ArrayAccessNode arr) {
            SymbolArray symArr = (SymbolArray) semanticAnalyzer.getSymbolTable().get(arr.getArrayName());
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
        } else {
            throw new SemanticException("Unsupported dot‐target: " + target);
        }
    }

    //returnerer det udtryk som er inde i parentesen, så længe det er af ExpressionNode.
    public ExpressionNode checkParentheses (ParenExpressionNode p) {
        return p.getInner();
    }

    public String typeEvaluation(ExpressionNode expr){
        //System.out.println(">> typeEvaluation on: " + expr.getClass().getSimpleName() + " -> " + expr);

        if (expr instanceof ConstantNode){
            if (checkBool((ConstantNode) expr)){
                //System.out.println("bool: " + v);
                return "boolean";
            } else if (checkInt((ConstantNode) expr)) {
                return "int";
            }
        }
        else if (expr instanceof IdentifierNode id) {
            String name = id.getName();
            Symbol sym = semanticAnalyzer.getSymbolTable().get(name);
            if (sym == null) {
                throw new RuntimeException("Identifier " + name + " not found in symbol table");
            }
            if (sym instanceof SymbolObject objSym) {
                // Returner typen på objektet/parameteren, ikke selve navnet
                return objSym.getSymbolType().getName();
            } else {
                throw new RuntimeException("Identifier " + name + " is not an object or variable");
            }
        }
        else if (expr instanceof ArrayAccessNode arr) {
            SymbolArray sa = (SymbolArray) semanticAnalyzer.getSymbolTable().get(arr.getArrayName());
            Set<String> types = new LinkedHashSet<>();
            for (ExpressionNode idx : arr.getIndices()) {
                int i = Integer.parseInt(((ConstantNode)idx).getValueConstant());
                types.add(sa.getObjects().get(i).getSymbolType().getName());
            }
            return (types.size() == 1)
                    ? types.iterator().next()
                    : String.join("||", types);
        }
        else if (expr instanceof DotNode dn) {
            // Tjek først at attributten findes
            if (!checkDotNode(dn)) {
                throw new SemanticException("Invalid dot‐expression: " + dn);
            }
            ExpressionNode target = dn.getTarget();

            SymbolType ownerType;

            if (target instanceof IdentifierNode id) {
                // Find typen på objektet (f.eks. robot)
                ownerType = checkObject(id);
            }
            else if (target instanceof ArrayAccessNode arr) {
                SymbolArray symArr = (SymbolArray) semanticAnalyzer.getSymbolTable().get(arr.getArrayName());
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
        else if(expr instanceof ParenExpressionNode) {
            ExpressionNode inner = checkParentheses((ParenExpressionNode) expr);
            return typeEvaluation(inner);

        } else if (expr instanceof BinaryOpNode) {
            //System.out.println("Now evaluating Binary Operation Node! :)");
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