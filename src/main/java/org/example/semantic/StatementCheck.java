package org.example.semantic;

import org.example.ast.*;

import java.util.List;
import java.util.*;

public class StatementCheck {

    SemanticAnalyzer semanticAnalyzer;
    ExpressionCheck expressionCheck;

    public StatementCheck(SemanticAnalyzer semanticAnalyzer) {
        this.semanticAnalyzer = semanticAnalyzer;
        this.expressionCheck = new ExpressionCheck(semanticAnalyzer);
    }

    public boolean checkComparison(BinaryOpNode node){
        String leftType = expressionCheck.typeEvaluation(node.getLeft());
        String rightType = expressionCheck.typeEvaluation(node.getRight());

        if(leftType.equals(rightType)){
            return true;
        } else {
            throw new SemanticException("Cannot compare " + leftType + " with " + rightType);
        }
    }

    public List<StatementNode> checkThenBranch(IfNode ifNode) {
        // Must cast to StatementListNode to get the statements
        StatementListNode thenBranch = (StatementListNode) ifNode.getThenBranch();

        return thenBranch.getStatements();
    }

    public boolean checkAssignment(AssignmentNode node){
        // Get the possible values of the attribute on left-hand side
        DotNode target = node.getTarget();
        ExpressionNode exprNode = target.getTarget();
        String field = target.getField();

        // Left side; possible types
        List<String> possibleValueType = getAssignableTypes(node);

        String objectName;
        if (exprNode instanceof IdentifierNode id) {
            objectName = id.getName();
        }
        else if (exprNode instanceof ArrayAccessNode arr) {
            objectName = arr.getArrayName() + arr.getIndices().toString()
                    .replace("Constant(value=", "").replace(")","");
        }
        else {
            throw new SemanticException("Cannot assign " + exprNode + " to " + field);
        }

        // Get value that is assigned to the attribute
        String valueType = getExpressionType(node.getExpression());
        if (valueType == null){
            throw new RuntimeException("Value assigned for attribute: " + target.getField() + ", in object: " + objectName + ", is not recognized.");
        }
        else if (!possibleValueType.contains(valueType)) {
            throw new RuntimeException("Value assigned for attribute: " + target.getField() + ", in object: " + objectName + ", is of wrong type.");

        }
        return true;
    }

    public List<String> getAssignableTypes(AssignmentNode node) {
        // Extract the field-access on the left-hand side
        DotNode target = node.getTarget();
        ExpressionNode exprNode = target.getTarget();
        String field = target.getField();

        // Check if the object has the attribute
        expressionCheck.checkDotNodeExpr(target);

        if (exprNode instanceof IdentifierNode objectName) {
            SymbolType typeSymbol = expressionCheck.checkObject(objectName);
            Map<String, SymbolAttribute> attributes = typeSymbol.getAttributes();
            SymbolAttribute attribute = attributes.get(field);
            List<String> types = expressionCheck.attributeCheck(typeSymbol, attribute);
            if (types == null) {
                throw new RuntimeException("Type: " + objectName + " does not have any attributes");
            }
            return types;
        }

        else if (exprNode instanceof ArrayAccessNode arr) {
            SymbolArray symArr = (SymbolArray) semanticAnalyzer.getSymbolTable().get(arr.getArrayName());

            List<String> intersection = null;
            for (ExpressionNode idxExpr : arr.getIndices()) {
                int i = Integer.parseInt(((ConstantNode)idxExpr).getValueConstant());
                SymbolObject obj = symArr.getObjects().get(i);
                SymbolType typeSymbol = obj.getSymbolType();
                SymbolAttribute attribute = typeSymbol.getAttributes().get(field);
                List<String> types = expressionCheck.attributeCheck(typeSymbol, attribute);
                if (types == null) {
                    throw new RuntimeException("Type: " + obj + " does not have any attributes");
                }

                if (intersection == null) {
                    intersection = new ArrayList<>(types);
                } else {
                    intersection.retainAll(types);
                }
            }
            return intersection;
        }
        else {
            throw new SemanticException("Cannot assign into expression '" + exprNode + "'");
        }
    }

    public String getExpressionType(ExpressionNode expression){

        if (expression instanceof IdentifierNode) {
            IdentifierNode leftSide = (IdentifierNode) expression;
            SymbolType symbolType = expressionCheck.checkObject(leftSide);
            return symbolType.getName();

        } else if (expression instanceof ConstantNode) {
            ConstantNode leftSide = (ConstantNode) expression;
            if (expressionCheck.checkBool(leftSide)){
                return "boolean";
            } else if (expressionCheck.checkInt(leftSide)){
                return "int";
            }

        } else if (expression instanceof BinaryOpNode){
            return expressionCheck.binOpEval((BinaryOpNode) expression);
        }
        // DotNode, in the case that one writes p.location == r.robot. That is, dotNode on each side of ==
        if (expression instanceof DotNode dotNode) {
            return expressionCheck.typeEvaluation(dotNode);
        }
        return null;
    }
}