package org.example.ast;

import java.util.List;

public class ProblemNode extends AstNode{
    private final String name;
    private final String importName;
    private final List<ArrayInitializerNode> arrayInitializers;
    private final List<ObjectNode> objects;
    private final List<StatementNode> init;
    private final List<ExpressionNode> goal;

    public ProblemNode(String name, String importName,
                       List<ArrayInitializerNode> arrayInitializers,
                          List<ObjectNode> objects,
                       List<StatementNode> init,
                       List<ExpressionNode> goal) {
        this.name = name;
        this.importName = importName;
        this.arrayInitializers = arrayInitializers;
        this.objects = objects;
        this.init = init;
        this.goal = goal;
    }

    //getters
    public String getName() {
        return name;
    }

    public String getImportName() {
        return importName;
    }

    public List<ArrayInitializerNode> getArrayInitializers() {
        return arrayInitializers;
    }

    public List<ObjectNode> getObjects() {
        return objects;
    }

    public List<StatementNode> getStatement() {
        return init;
    }

    public List<ExpressionNode> getExpression() {
        return goal;
    }

    public List<StatementNode> getInit() {
        return init;
    }

    @Override
    public String toString() {
        return "Problem(Name = " + name + ", import = " + importName + ", arrays = " + arrayInitializers + ", init = " + init + ", goal = " + goal + ")";
    }
}
