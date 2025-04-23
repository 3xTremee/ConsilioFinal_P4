package org.example.semantic;

import org.example.ast.*;

import java.util.List;
import java.util.Map;

public class HelperFunctions {

    // Get the type of an expression
    public String getExpressionType(ExpressionNode node, List<ParameterNode> parameters, Map<String, TypeNode> typeNodes) {
        if (node instanceof IdentifierNode) {
            return getIdentifierType((IdentifierNode) node, parameters);
        } else if (node instanceof DotNode) {
            return getDotNodeType((DotNode) node, parameters, typeNodes);
        } else if (node instanceof BinaryOpNode) {
            return getBinaryOpType((BinaryOpNode) node, parameters, typeNodes);
        } else if (node instanceof ConstantNode) {
            return ((ConstantNode) node).getValueConstant();
        }
        throw new SemanticException("Unsupported expression type: " + node.getClass().getSimpleName());
    }

    // Check the body of an action
    public void actionBodyCheck(StatementNode body, List<ParameterNode> parameters, Map<String, TypeNode> typeNodes) {
        if (body instanceof IfNode) {
            IfNode ifBody = (IfNode) body;
            traverseExpression(ifBody.getCondition(), parameters, typeNodes);
            traverseStatement(ifBody.getThenBranch(), parameters, typeNodes);
        } else {
            traverseStatement(body, parameters, typeNodes);
        }
    }

    // Traverse an expression
    public void traverseExpression(ExpressionNode node, List<ParameterNode> parameters, Map<String, TypeNode> typeNodes) {
        if (node instanceof DotNode) {
            validateDotNode((DotNode) node, parameters, typeNodes);
        }
    }

    // Traverse a statement
    public void traverseStatement(StatementNode node, List<ParameterNode> parameters, Map<String, TypeNode> typeNodes) {
        if (node instanceof AssignmentNode) {
            validateAssignment((AssignmentNode) node, parameters, typeNodes);
        } else if (node instanceof StatementListNode) {
            for (StatementNode statement : ((StatementListNode) node).getStatements()) {
                traverseStatement(statement, parameters, typeNodes);
            }
        } else if (node instanceof IfNode) {
            IfNode ifNode = (IfNode) node;
            traverseExpression(ifNode.getCondition(), parameters, typeNodes);
            traverseStatement(ifNode.getThenBranch(), parameters, typeNodes);
        }
    }

    // Helper: Get the type of an IdentifierNode
    private String getIdentifierType(IdentifierNode node, List<ParameterNode> parameters) {
        return parameters.stream()
                .filter(p -> p.getName().equals(node.getName()))
                .map(ParameterNode::getType)
                .findFirst()
                .orElseThrow(() -> new SemanticException("Undefined variable: " + node.getName()));
    }

    // Helper: Get the type of a DotNode
    private String getDotNodeType(DotNode node, List<ParameterNode> parameters, Map<String, TypeNode> typeNodes) {
        String targetType = getExpressionType(node.getTarget(), parameters, typeNodes);
        TypeNode typeDefinition = typeNodes.get(targetType);
        if (typeDefinition != null && typeDefinition.hasAttribute(node.getField())) {
            return String.join(", ", typeDefinition.getAttributeType(node.getField()));
        }
        throw new SemanticException("Field '" + node.getField() + "' does not exist in type '" + targetType + "'.");
    }

    // Helper: Get the type of a BinaryOpNode
    private String getBinaryOpType(BinaryOpNode node, List<ParameterNode> parameters, Map<String, TypeNode> typeNodes) {
        String leftType = getExpressionType(node.getLeft(), parameters, typeNodes);
        String rightType = getExpressionType(node.getRight(), parameters, typeNodes);
        if (!leftType.equals(rightType)) {
            throw new SemanticException("Type mismatch in binary operation: " + leftType + " and " + rightType);
        }
        return leftType;
    }

    // Helper: Validate a DotNode
    private void validateDotNode(DotNode node, List<ParameterNode> parameters, Map<String, TypeNode> typeNodes) {
        String targetType = getExpressionType(node.getTarget(), parameters, typeNodes);
        TypeNode typeDefinition = typeNodes.get(targetType);
        if (typeDefinition != null && !typeDefinition.hasAttribute(node.getField())) {
            throw new SemanticException("Semantic Error: Attribute '" + node.getField() + "' does not exist in type '" + targetType + "'.");
        }
    }

    // Helper: Validate an AssignmentNode
    private void validateAssignment(AssignmentNode node, List<ParameterNode> parameters, Map<String, TypeNode> typeNodes) {
        DotNode lhs = node.getTarget();
        ExpressionNode rhs = node.getExpression();
        String lhsType = getExpressionType(lhs, parameters, typeNodes);
        String rhsType = getExpressionType(rhs, parameters, typeNodes);
        switch (lhsType) {
            case "int":
                // Check if the RHS is a numeric type or a numeric constant
                if (rhs instanceof ConstantNode) {
                    String value = ((ConstantNode) rhs).getValueConstant();
                    try {
                        Integer.parseInt(value); // Ensure the value is a valid integer
                    } catch (NumberFormatException e) {
                        throw new SemanticException("Type mismatch: Cannot assign non-integer value '" + value + "' to int");
                    }
                } else if (!rhsType.equals("int")) {
                    throw new SemanticException("Type mismatch: Cannot assign type '" + rhsType + "' to int");
                }
                break;
            case "boolean":
                if (rhs instanceof ConstantNode) {
                    String value = ((ConstantNode) rhs).getValueConstant();
                    if (!value.equals("true") && !value.equals("false")) {
                        throw new SemanticException("Type mismatch: Cannot assign non-boolean value '" + value + "' to boolean");
                    }
                } else if (!lhsType.contains(rhsType)) {
                    throw new SemanticException("Type mismatch: Cannot assign " + rhsType + " to " + lhsType);
                }
                break;
            default:
                if (!lhsType.contains(rhsType)) {
                    throw new SemanticException("Type mismatch: Cannot assign " + rhsType + " to " + lhsType);
                }
        }
    }
}