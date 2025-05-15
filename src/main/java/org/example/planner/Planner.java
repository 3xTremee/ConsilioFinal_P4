package org.example.planner;

import org.example.ast.*;
import org.example.semantic.*;

import java.util.*;
import java.util.stream.Collectors;


// BFS search planner
public class Planner {
    private final List<GroundedAction> allActions;
    private final SemanticAnalyzer semanticAnalyzer;

    public Planner(DomainNode domain, List<ObjectNode> objects, SemanticAnalyzer semanticAnalyzer) {
        this.allActions = groundAll(domain, objects);
        this.semanticAnalyzer = semanticAnalyzer;
    }

    private List<GroundedAction> groundAll(DomainNode domain, List<ObjectNode> objs) {
        Map<String, List<String>> byType = new HashMap<>();
        for (ObjectNode o : objs) {
            byType.computeIfAbsent(o.getType(), k -> new ArrayList<>())
                    .add(o.getElementName());
        }
        List<GroundedAction> out = new ArrayList<>();
        for (ActionNode a : domain.getActions()) {
            backtrackBind(a.getParameters(), byType, 0, new HashMap<>(), a, out);
        }
        return out;
    }

    private void backtrackBind(
            List<ParameterNode> params,
            Map<String, List<String>> byType,
            int idx,
            Map<String, String> curr,
            ActionNode schema,
            List<GroundedAction> out
    ) {
        if (idx == params.size()) {
            out.add(new GroundedAction(schema, curr));
            return;
        }
        ParameterNode p = params.get(idx);
        for (String obj : byType.getOrDefault(p.getType(), List.of())) {
            curr.put(p.getName(), obj);
            backtrackBind(params, byType, idx + 1, curr, schema, out);
            curr.remove(p.getName());
        }
    }

    /*
     * Method for applying grounded actions to a State s
     * Returns the new state s if the guard(condition) passes and null otherwise
     */
    private State apply(GroundedAction ga, State s) {
        // Bind parametre
        Map<String,String> binding = ga.getBinding();
        binding.forEach((var, obj) -> {
            SymbolObject symObj = (SymbolObject) semanticAnalyzer.getSymbolTable().get(obj);
            semanticAnalyzer.getSymbolTable().put(var, symObj);
        });

        try {
            ExpressionEvaluator eval = new ExpressionEvaluator(binding, semanticAnalyzer);

            IfNode ifn = (IfNode) ga.getSchema().getBody();

            // Only evaluating here, no type checking
            boolean guard = (Boolean) eval.evaluate(ifn.getCondition(), s);
            if (!guard) return null;

            State next = s;
            for (StatementNode st : ((StatementListNode) ifn.getThenBranch()).getStatements()) {
                AssignmentNode asn = (AssignmentNode) st;
                DotNode target = asn.getTarget();
                String field = target.getField();
                Object val = eval.evaluate(asn.getExpression(), next);
                ExpressionNode t = target.getTarget();

                if (t instanceof IdentifierNode id) {
                    String objName =
                            binding.getOrDefault(id.getName(), id.getName());
                    next = next.with(objName, field, val);
                }
                else if (t instanceof ArrayAccessNode arr) {
                    List<Integer> idxs = new ArrayList<>();
                    for (ExpressionNode idxExpr : arr.getIndices()) {
                        idxs.add((Integer) eval.evaluate(idxExpr, next));
                    }

                    SymbolArray symArr = (SymbolArray) semanticAnalyzer.getSymbolTable().get(arr.getArrayName());

                    for (int i : idxs) {
                        String objName = symArr.getObjects().get(i).getName();
                        next = next.with(objName, target.getField(), val);
                    }
                }
                else {
                    throw new UnsupportedOperationException(
                            "Cannot assign to target: " + t);
                }
            }
            return next;
        } finally {
            // Fjern bindinger
            binding.keySet().forEach(k -> semanticAnalyzer.getSymbolTable().remove(k));
        }
    }

    // Chech if every goal expression is true in state s
    private boolean isGoal(State s, List<ExpressionNode> goals) {
        ExpressionEvaluator eval = new ExpressionEvaluator(this.semanticAnalyzer); // no bindings
        for (ExpressionNode g : goals) {
            Object v = eval.evaluate(g, s);
            if (!(v instanceof Boolean b) || !b) {
                return false;
            }
        }
        return true;
    }

    // BFS algorithm used to search for a plan
    public Optional<List<GroundedAction>> bfs(State init, List<ExpressionNode> goals) {
        record Node(State state, List<GroundedAction> plan) {}
        Queue<Node> frontier = new ArrayDeque<>();
        Set<State> seen = new HashSet<>();

        frontier.add(new Node(init, List.of()));
        seen.add(init);
        System.out.print("\n Init state: " + init + "\n");

        if (isGoal(init, goals)) {
            throw new SemanticException("Initial state is already in goal state");
        }

        while (!frontier.isEmpty()) {
            Node n = frontier.poll();

            // Try each action from this state
            for (GroundedAction ga : allActions) {
                State nxt = apply(ga, n.state);
                if (nxt != null) {
                    if (seen.add(nxt)) {
                        List<GroundedAction> p2 = new ArrayList<>(n.plan);
                        p2.add(ga);

                        //Checks goal state
                        if (isGoal(nxt, goals)) {
                            System.out.print("\n Goal state: " + nxt + "\n\n");
                            return Optional.of(p2);
                        }
                        frontier.add(new Node(nxt, p2));
                    }
                }
            }
        }
        return Optional.empty();
    }
}
