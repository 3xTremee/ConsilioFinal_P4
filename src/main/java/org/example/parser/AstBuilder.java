package org.example.parser;

import org.example.ConsilioBaseVisitor;
import org.example.ConsilioParser;
import org.example.ast.*;
import org.antlr.v4.runtime.tree.ParseTree;
import java.util.*;
import java.util.stream.Collectors;

public class AstBuilder extends ConsilioBaseVisitor<AstNode> {

    @Override
    public ProgramNode visitProgram(ConsilioParser.ProgramContext ctx) {
        DomainNode domain = (DomainNode) visit(ctx.domain());
        ProblemNode problem = (ProblemNode) visit(ctx.problem());

        return new ProgramNode(domain, problem);
    }

    @Override
    public DomainNode visitDomain(ConsilioParser.DomainContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        List<TypeNode> types = ctx.type().stream()
                .map(t -> (TypeNode) visit(t)).collect(Collectors.toList());
        List<ActionNode> actions = ctx.action().stream()
                .map(a -> (ActionNode) visit(a)).collect(Collectors.toList());
        return new DomainNode(name, types, actions);
    }

    @Override
    public ProblemNode visitProblem(ConsilioParser.ProblemContext ctx) {
        String name = ctx.getChild(2).getText();
        String importName = ctx.getChild(4).getText();

        List<ArrayInitializerNode> arrays = ctx.objects()
                .objectBody()
                .arrayInitializer()
                .stream()
                .map(ai -> (ArrayInitializerNode) visit(ai))
                .toList();

        List<ObjectNode> objects = ctx.objects().objectBody().arrayInitializer().stream()
                .flatMap(ai -> {
                    ArrayInitializerNode arrayInit = (ArrayInitializerNode) visit(ai);
                    return arrayInit.getElements().stream()
                            .map(elem -> new ObjectNode(arrayInit.getType(), arrayInit.getName(), elem, new IdentifierNode(elem)));
                })
                .toList();

        StatementListNode initList = ctx.init().statementList() != null
                ? (StatementListNode) visit(ctx.init().statementList())
                : new StatementListNode(Collections.emptyList());  // Sker ikke da vi aldrig kan have et tomt statement
        List<StatementNode> initStatements = initList.getStatements();

        List<ExpressionNode> goals = ctx.goal().expression().stream()
                .map(e -> (ExpressionNode) visit(e))
                .toList();

        return new ProblemNode(name, importName, arrays, objects, initStatements, goals);
    }

    @Override
    public TypeNode visitType(ConsilioParser.TypeContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        List<AttributeNode> attributes = ctx.attribute().stream()
                .map(a -> (AttributeNode) visit(a)).toList();

        return new TypeNode(name, attributes);
    }

    @Override
    public AttributeNode visitAttribute(ConsilioParser.AttributeContext ctx) {
        String id = ctx.IDENTIFIER().getText();
        ValueNode value = (ValueNode) visit(ctx.value());

        return new AttributeNode(id, value);
    }

    @Override
    public ValueNode visitBaseValue(ConsilioParser.BaseValueContext ctx) {
        String value = ctx.valueType().getText();

        return new BaseValueNode(value);
    }

    @Override
    public ValueNode visitOrValue(ConsilioParser.OrValueContext ctx) {
        List<ValueNode> values = new ArrayList<>();
        for (ParseTree child : ctx.children) {
            if (child instanceof ConsilioParser.ValueTypeContext) {
                values.add((ValueNode) visit(child));
            }
            else if (child instanceof ConsilioParser.BaseValueContext) {
                values.add((ValueNode) visit(child));
            }
            else if (child instanceof ConsilioParser.OrValueContext) {
                values.add((ValueNode) visit(child));
            }
        }
        return new OrValueNode(values);
    }

    // Delete if end up not being used
    @Override
    public ValueNode visitArrayValue(ConsilioParser.ArrayValueContext ctx) {
        ValueNode elementType = (ValueNode) visit(ctx.valueType());

        //System.out.print("visitArrayValue" + elementType);

        return new ArrayValueNode(elementType, 0);
    }

    @Override
    public ValueNode visitValueType(ConsilioParser.ValueTypeContext ctx) {
        String type = ctx.getText();

        return new ValueTypeNode(type);
    }

    @Override
    public ActionNode visitAction(ConsilioParser.ActionContext ctx) {
        String name = ctx.IDENTIFIER().getText();

        List<ParameterNode> parameters = new ArrayList<>();
        ConsilioParser.ParameterContext pctx = ctx.parameter();
        while (pctx != null) {
            parameters.add((ParameterNode) visit(pctx));
            pctx = pctx.parameter();
        }

        StatementNode body = (StatementNode) visit(ctx.statement());

        return new ActionNode(name, parameters, body);
    }

    @Override
    public ParameterNode visitParameter(ConsilioParser.ParameterContext ctx) {
        String type = ctx.getChild(0).getText();
        String name = ctx.getChild(1).getText();

        return new ParameterNode(type, name);
    }


    @Override
    public StatementNode visitIfStatement(ConsilioParser.IfStatementContext ctx) {
        ExpressionNode exprCondition = (ExpressionNode) visit(ctx.ifBlock().expression());
        StatementListNode thenPart = (StatementListNode) visit(ctx.ifBlock().statementList());

        return new IfNode(exprCondition, thenPart);
    }

    @Override
    public StatementNode visitAssignmentStatement(ConsilioParser.AssignmentStatementContext ctx) {
        return (StatementNode) visit(ctx.assignment());
    }

    @Override
    public StatementListNode visitStatementList(ConsilioParser.StatementListContext ctx) {
        List<StatementNode> stmts = ctx.statement().stream()
                .map(s -> (StatementNode) visit(s))
                .collect(Collectors.toList());
        return new StatementListNode(stmts);
    }

    @Override
    public StatementNode visitDotNotationAssignment(ConsilioParser.DotNotationAssignmentContext ctx) {
        DotNode target =  (DotNode) visit(ctx.dotNotation());
        ExpressionNode expr = (ExpressionNode) visit(ctx.expression());

        return new AssignmentNode(target, expr);
    }

    @Override
    public ExpressionNode visitSimpleDot(ConsilioParser.SimpleDotContext ctx) {
        ExpressionNode target = new IdentifierNode(ctx.IDENTIFIER(0).getText());
        String field = ctx.IDENTIFIER(1).getText();

        return new DotNode(target, field);
    }

    @Override
    public ExpressionNode visitArrayDot(ConsilioParser.ArrayDotContext ctx) {
        String arrayName = ctx.IDENTIFIER(0).getText();

        List<ExpressionNode> indices = new ArrayList<>();
        collectArrayBody(ctx.arrayBodyInt(), indices);
        ExpressionNode arrayAccess = new ArrayAccessNode(arrayName, indices);
        String field = ctx.IDENTIFIER(1).getText();

        return new DotNode(arrayAccess, field);
    }

    private void collectArrayBody(ConsilioParser.ArrayBodyIntContext ctx, List<ExpressionNode> indices) {
        indices.add((ExpressionNode) visit(ctx.expression()));
        if(ctx.arrayBodyInt() != null) {
            collectArrayBody(ctx.arrayBodyInt(), indices);
        }
    }

    @Override
    public ArrayInitializerNode visitArrayInitializer(ConsilioParser.ArrayInitializerContext ctx) {
        String type = ctx.IDENTIFIER(0).getText();
        String name = ctx.IDENTIFIER(1).getText();
        List<String> elements = new ArrayList<>();
        collectArrayInitElements(ctx.arrayBody(), elements);

        return new ArrayInitializerNode(type, name, elements);
    }

    private void collectArrayInitElements(ConsilioParser.ArrayBodyContext ctx, List<String> list) {
        list.add(ctx.IDENTIFIER().getText());
        if (ctx.arrayBody() != null) {
            collectArrayInitElements(ctx.arrayBody(), list);
        }
    }

    @Override
    public ExpressionNode visitConstantExpression(ConsilioParser.ConstantExpressionContext ctx) {
        String value = ctx.getText();

        return new ConstantNode(value);
    }

    @Override
    public ExpressionNode visitIdentifierExpression(ConsilioParser.IdentifierExpressionContext ctx) {
        String name = ctx.IDENTIFIER().getText();

        return new IdentifierNode(name);
    }

    @Override
    public ExpressionNode visitParenthesisExpression(ConsilioParser.ParenthesisExpressionContext ctx) {
        ExpressionNode inner = (ExpressionNode) visit(ctx.expression());

        return new ParenExpressionNode(inner);
    }

    // Delete if end up not being used "rob.location = rooms[0];"
    @Override
    public ExpressionNode visitArrayAccessExpression(ConsilioParser.ArrayAccessExpressionContext ctx) {
        String arrayName = ctx.IDENTIFIER().getText();
        List<ExpressionNode> indices = new ArrayList<>();
        collectArrayBody(ctx.arrayBodyInt(), indices);

        //System.out.print("visitArrayAccessExpression" + arrayName);

        return new ArrayAccessNode(arrayName, indices);
    }

    @Override
    public ExpressionNode visitAdditionExpression(ConsilioParser.AdditionExpressionContext ctx) {
        ExpressionNode left = (ExpressionNode) visit(ctx.expression(0));
        String operator = ctx.addOp().getText();
        ExpressionNode right = (ExpressionNode) visit(ctx.expression(1));

        return new BinaryOpNode(left, operator, right);
    }

    @Override
    public ExpressionNode visitCompareExpression(ConsilioParser.CompareExpressionContext ctx) {
        ExpressionNode left = (ExpressionNode) visit(ctx.expression(0));
        String operator = ctx.compOp().getText();
        ExpressionNode right = (ExpressionNode) visit(ctx.expression(1));

        return new BinaryOpNode(left, operator, right);
    }

    @Override
    public ExpressionNode visitLogicalExpression(ConsilioParser.LogicalExpressionContext ctx) {
        ExpressionNode left = (ExpressionNode) visit(ctx.expression(0));
        String operator = ctx.locOp().getText();
        ExpressionNode right = (ExpressionNode) visit(ctx.expression(1));

        return new BinaryOpNode(left, operator, right);
    }
}
