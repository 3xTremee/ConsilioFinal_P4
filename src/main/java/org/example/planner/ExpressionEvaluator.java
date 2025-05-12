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

/**
 * Evaluate AST expressions in the context of a State,
 * including boolean equality/inequality.
 */
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

            // unwrap from list if only 1 index, otherwise return entire list
            return (results.size() == 1) ? results.get(0) : results;
        }

        else if (expr instanceof ConstantNode c) {
            String v = c.getValueConstant();

            if (expressionCheck.checkBool(c)){
                //System.out.println("bool: " + v);
                return Boolean.parseBoolean(v);
            } else if (expressionCheck.checkInt(c)) {
                return Integer.parseInt(v);
            }
            return null;
        }
        else if (expr instanceof IdentifierNode id) {
            try {
                String name = id.getName();
                return binding.getOrDefault(name, name);          // hvis binding indeholder en værdi for key(name) returneres dens værdi og hvis ikke returneres bare navnet (uden værdi)
            } catch (NullPointerException e) {
                System.err.println("Semantisk fejl: " + e.getMessage());
                return null;
            }
        }
        // DotNode-objekt (f.eks. noget som x.y)
         else if (expr instanceof DotNode dn) {
            //denne returnerer det som står før punktumet dvs. i x.y er det x.
            //den castes til IdentifierNode, fordi man antager det er et simpelt variabelnavn.
            String varName = ((IdentifierNode) dn.getTarget()).getName();
            //hvis der findes en binding (fx "x" -> "user42") så bruges den værdi, ellers bruges x som det er.


            String object  = binding.getOrDefault(varName, varName);
            //s ligner en form for symboltabel, model eller et objektlager
            //dn.getField() er feltet (højresiden af punktumet) fx "y" i x.y
            //der slås altså op "user42".y og det returneres.
            return s.get(object, dn.getField());
        }

        else if (expr instanceof BinaryOpNode bin) {

            Object L = evaluate(bin.getLeft(), s);
            Object R = evaluate(bin.getRight(), s);
            String op = bin.getOperator();

            // MultiTarget comparisons "robots[0,1,2].location == A";
            if (L instanceof List<?> list) {
                // integer arithmetic & comparisons
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
                                throw new UnsupportedOperationException("Unsupported op for int-list: " + op);
                    };
                }
                // boolean logic & comparisons
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
                                throw new UnsupportedOperationException("Unsupported op for bool-list: " + op);
                    };
                }
                // Generic equality for any other element‐type (String)
                if (op.equals("==") || op.equals("!=")) {
                    boolean allEqual = list.stream().allMatch(v -> v != null && v.equals(R));
                    return op.equals("==") ? allEqual : !allEqual;
                }
            }

            // integer arithmetic & comparisons
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
                    default   -> throw new UnsupportedOperationException("Unknown int op: " + op);
                };
            }

            // boolean logic & comparisons
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
                    default   -> throw new UnsupportedOperationException("Unknown bool op: " + op);
                };
            }

            // fallback for any other types (null-safe equality)
            return switch (op) {
                case "==" -> (L != null) && L.equals(R);
                case "!=" -> (L == null) || !L.equals(R);
                default   -> throw new UnsupportedOperationException("Unknown op: " + op);
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
