package org.example.planner;

import org.example.ast.*;
import org.example.semantic.*;

import java.util.*;
import java.util.stream.Collectors;



/**
 * Simple breadth‑first search planner, with debug prints on every action attempt.
 */
public class Planner {
    private final List<GroundedAction> allActions;
    //private final StatementCheck statementCheck;
    private final SemanticAnalyzer semanticAnalyzer;

//    public Planner(DomainNode domain, List<ObjectNode> objects, StatementCheck statementCheck, SemanticAnalyzer semanticAnalyzer) {
//        this.statementCheck = statementCheck;
//        this.allActions = groundAll(domain, objects);
//        this.semanticAnalyzer = semanticAnalyzer;
//    }

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

    /**
     * Apply one grounded action to state s.
     * Returns new State if guard passes, or null if guard fails.
     */
//    private State apply(GroundedAction ga, State s) {
//        //ga.getBinding() bruges typisk til mapping fra variabler til konkrete værdier eller objekter
//        ExpressionEvaluator eval = new ExpressionEvaluator(ga.getBinding(), this.semanticAnalyzer);
//
//        //System.out.println("ga: " + ga.getSchema().getBody());
//
//        IfNode ifn = (IfNode) ga.getSchema().getBody();
//
//        if((ga.getSchema().getBody() instanceof IfNode)) {
//            ExpressionNode ifCondition = ((IfNode) ga.getSchema().getBody()).getCondition();          //først castes der til ifNode og bagefter hentes condition
//
//            ExpressionNode ifCheck2 = statementCheck.checkIfCondition((IfNode) ga.getSchema().getBody());
//            Boolean guard2 = (Boolean) eval.evaluate(ifCheck2, s);
//
//            Boolean guard = (Boolean) eval.evaluate(ifCondition, s);
//            System.out.println("guard2: " + guard2);
//            if (!guard2) {
//                return null;
//            }
//        }
//
//        /*Boolean guard = (Boolean) eval.evaluate(ifn.getCondition(), s);
//        System.out.println("guard: " + guard);
//        if (!guard) {
//            return null;
//        }*/
//
//        State next = s;
//        for (StatementNode st : (statementCheck.checkThenBranch(ifn))) {    /*;ifn.getThenBranch()).getStatements()*/
//            AssignmentNode asn = (AssignmentNode) st;
//            boolean isOkay = statementCheck.checkAssignment(asn);
//            System.out.println("isOkay: " + isOkay);
//
//
//            DotNode dotNode = asn.getTarget();
//            ExpressionNode exprNode = dotNode.getTarget();
//            String varName = ((IdentifierNode) exprNode).getName();
//            String object  = ga.getBinding().getOrDefault(varName, varName);
//            String field   = dotNode.getField();
//            Object val     = eval.evaluate(asn.getExpression(), next);
//            next = next.with(object, field, val);
//        }
//        return next;
//    }


    //DET HER ER DEN SOM VIRKER PT. EFTER FROKOST ONSDAG D. 7/5
//    private State apply(GroundedAction ga, State s) {
//        // Bind parametre ind i symbolTable
//        Map<String,String> binding = ga.getBinding();
//        for (var entry : binding.entrySet()) {
//            String varName = entry.getKey();       // kunne være "r"
//            String objName = entry.getValue();     // kunne være "rob"
//            // hent det eksisterende SymbolObject for "rob"
//            SymbolObject symObj = (SymbolObject) semanticAnalyzer.getSymbolTable().get(objName);
//            semanticAnalyzer.getSymbolTable().put(varName, symObj);
//        }
//
//        try {
//            // Opret evaluator med binding
//            ExpressionEvaluator eval = new ExpressionEvaluator(binding, this.semanticAnalyzer);
//
//            IfNode ifn = (IfNode) ga.getSchema().getBody();
//            // Guard check
//            ExpressionNode ifCheck2 = statementCheck.checkIfCondition(ifn);
//            Boolean guard2 = (Boolean) eval.evaluate(ifCheck2, s);
//            System.out.println("guard2: " + guard2);
//            if (!guard2) {
//                return null;
//            }
//
//            // Kør body
//            State next = s;
//            for (StatementNode st : statementCheck.checkThenBranch(ifn)) {
//                AssignmentNode asn = (AssignmentNode) st;
//                //statementCheck.checkAssignment(asn);
//
//                DotNode dotNode = asn.getTarget();
//                // find objekt- og felt-navn
//                IdentifierNode targetObj = (IdentifierNode) dotNode.getTarget();
//                String varName = targetObj.getName();
//                String object = binding.getOrDefault(varName, varName);
//                String field = dotNode.getField();
//                Object val = eval.evaluate(asn.getExpression(), next);
//                next = next.with(object, field, val);
//            }
//            return next;
//
//        } finally {
//            // Fjern parametre igen uanset hvad
//            for (String varName : binding.keySet()) {
//                semanticAnalyzer.getSymbolTable().remove(varName);
//            }
//        }
//    }

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
            // ren evaluering, ingen type tjek
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



    /** Check whether every goal expression holds in state s. */
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

    /**
     * Breadth‑first search for a plan.
     * Prints every attempted action and result.
     */
    public Optional<List<GroundedAction>> bfs(State init, List<ExpressionNode> goals) {
        record Node(State state, List<GroundedAction> plan) {}
        Queue<Node> frontier = new ArrayDeque<>();
        Set<State> seen = new HashSet<>();

        frontier.add(new Node(init, List.of()));
        seen.add(init);

        if (isGoal(init, goals)) {
            throw new SemanticException("Initial state is already in goal state");
        }

        while (!frontier.isEmpty()) {
            Node n = frontier.poll();

            // Try each action from this state
            for (GroundedAction ga : allActions) {
                //System.out.println("Attempting action: " + ga + "\n  from state: " + n.state);
                State nxt = apply(ga, n.state);
                if (nxt != null) {
                    //System.out.println("\u001B[32m" + "  → Applied, new state: " + "\u001B[0m" + nxt);
                    if (seen.add(nxt)) {
                        List<GroundedAction> p2 = new ArrayList<>(n.plan);
                        p2.add(ga);
                        // Check goal right away
                        if (isGoal(nxt, goals)) {
                            //System.out.println("\u001B[34m" + "Reached goal with action: " + ga + "\u001B[0m");
                            return Optional.of(p2);
                        }
                        frontier.add(new Node(nxt, p2));
                    }
                } else {
                    //System.out.println("\u001B[31m" + "  → Guard failed, skipping" + "\u001B[0m");

                }
            }
        }
        return Optional.empty();
    }
}
