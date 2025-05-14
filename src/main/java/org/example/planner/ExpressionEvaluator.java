package org.example.planner;

import org.example.ast.BinaryOpNode;
import org.example.ast.ConstantNode;
import org.example.ast.DotNode;
import org.example.ast.ExpressionNode;
import org.example.ast.IdentifierNode;
import org.example.ast.ParenExpressionNode;
import org.example.ast.ArrayAccessNode;
import org.example.semantic.*;

import java.util.*;


// Evaluate the AST expression in the context of the State.
public class ExpressionEvaluator {

    private final Map<String,String> binding;
    SemanticAnalyzer semanticAnalyzer;
    ExpressionCheck expressionCheck;
    StatementCheck statementCheck;

    public ExpressionEvaluator(SemanticAnalyzer semanticAnalyzer) {
        this.binding = Map.of();
        this.semanticAnalyzer = semanticAnalyzer;
        this.expressionCheck = new ExpressionCheck(semanticAnalyzer);
        this.statementCheck = new StatementCheck(semanticAnalyzer);
    }

    public ExpressionEvaluator(Map<String,String> binding, SemanticAnalyzer semanticAnalyzer) {
        this.binding = Map.copyOf(binding);
        this.semanticAnalyzer = semanticAnalyzer;
        this.expressionCheck = new ExpressionCheck(semanticAnalyzer);
        this.statementCheck = new StatementCheck(semanticAnalyzer);
    }

    public Object evaluate(ExpressionNode expr, State s) {
        if (expr instanceof DotNode dn && dn.getTarget() instanceof ArrayAccessNode aa) {
            List <ExpressionNode> indices = aa.getIndices();
            List<Object> results = new ArrayList<>();

            SymbolArray current = (SymbolArray) semanticAnalyzer.getSymbolTable().get(aa.getArrayName());

            for (ExpressionNode ix : indices) {
                int i = (Integer) evaluate(ix, s);
                SymbolObject elem = current.getObjects().get(i);
                results.add(s.get(elem.getName(), dn.getField()));
            }

            // If the list only has 1 element, get that. Otherwise returns the entire list
            return (results.size() == 1) ? results.get(0) : results;
        }

        else if (expr instanceof ConstantNode c) {
            String v = c.getValueConstant();

            if (expressionCheck.checkBool(c)){
                return Boolean.parseBoolean(v);
            } else if (expressionCheck.checkInt(c)) {
                return Integer.parseInt(v);
            }
            return null;
        }
        else if (expr instanceof IdentifierNode id) {
            try {
                String name = id.getName();
                return binding.getOrDefault(name, name);
            } catch (NullPointerException e) {
                System.err.println("Semantic error: " + e.getMessage());
                return null;
            }
        }

        // Handling DotNode object. Exmaple coule be x.y
         else if (expr instanceof DotNode dn) {
            String varName = ((IdentifierNode) dn.getTarget()).getName();
            String object  = binding.getOrDefault(varName, varName);

            return s.get(object, dn.getField());
        }

        else if (expr instanceof BinaryOpNode bin) {
            Object L = evaluate(bin.getLeft(), s);
            Object R = evaluate(bin.getRight(), s);
            String op = bin.getOperator();

            // Handling arrays robots[0,1,2].location == A;
            if (L instanceof List<?> list) {

                if (R instanceof Integer ri) {
                    return switch (op) {
                        case "==" ->
                                list.stream().allMatch(v -> v instanceof Integer && v.equals(ri));
                        case "!=" ->
                                list.stream().anyMatch(v -> v instanceof Integer && !v.equals(ri));
                        case ">"  ->
                                list.stream().allMatch(v -> v instanceof Integer && ((Integer)v) > ri);
                        case "<"  ->
                                list.stream().allMatch(v -> v instanceof Integer && ((Integer)v) < ri);
                        case ">=" ->
                                list.stream().allMatch(v -> v instanceof Integer && ((Integer)v) >= ri);
                        case "<=" ->
                                list.stream().allMatch(v -> v instanceof Integer && ((Integer)v) <= ri);
                        default ->
                                throw new UnsupportedOperationException("Unsupported operator for int-list: " + op);
                    };
                }

                if (R instanceof Boolean rb) {
                    return switch (op) {
                        case "==" ->
                                list.stream().allMatch(v -> v instanceof Boolean && v.equals(rb));
                        case "!=" ->
                                list.stream().anyMatch(v -> v instanceof Boolean && !v.equals(rb));
                        case "&&" ->
                                list.stream().allMatch(v -> v instanceof Boolean && ((Boolean)v) && rb);
                        case "||" ->
                                list.stream().anyMatch(v -> v instanceof Boolean && (((Boolean)v) || rb));
                        default ->
                                throw new UnsupportedOperationException("Unsupported operator for bool-list: " + op);
                    };
                }

                if (op.equals("==") || op.equals("!=")) {
                    boolean allEqual = list.stream().allMatch(v -> v != null && v.equals(R));
                    return op.equals("==") ? allEqual : !allEqual;
                }
            }


            if (L instanceof Integer li && R instanceof Integer ri) {
                return switch (op) {
                    case "+"  -> li + ri;
                    case "-"  -> li - ri;
                    case ">"  -> li > ri;
                    case "<"  -> li < ri;
                    case ">=" -> li >= ri;
                    case "<=" -> li <= ri;
                    case "==" -> li.equals(ri);
                    case "!=" -> !li.equals(ri);
                    default   -> throw new UnsupportedOperationException("Unknown int operator: " + op);
                };
            }

            if (L instanceof Boolean lb && R instanceof Boolean rb) {
                return switch (op) {
                    case "&&" -> lb && rb;
                    case "||" -> lb || rb;
                    case "==" -> {
                        if (statementCheck.checkComparison(bin)) {
                            yield lb.equals(rb);
                        } else {
                            yield null;
                        }
                    }
                    case "!=" -> {
                        if (statementCheck.checkComparison(bin)) {
                            yield !lb.equals(rb);
                        } else {
                            yield null;
                        }
                    }
                    default   -> throw new UnsupportedOperationException("Unknown bool operator: " + op);
                };
            }


            return switch (op) {
                case "==" -> (L != null) && L.equals(R);
                case "!=" -> (L == null) || !L.equals(R);
                default   -> throw new UnsupportedOperationException("Unknown operator: " + op);
            };
        }
        else if (expr instanceof ParenExpressionNode p) {
            return evaluate(p.getInner(), s);
        }
        else {
            throw new UnsupportedOperationException("Cannot evaluate node type: " + expr.getClass().getSimpleName());
        }
    }

}
