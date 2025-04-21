package org.example.ast;

public class ParameterNode extends AstNode {
    private final String type;
    private final String name;

    public ParameterNode(String type, String name) {
        this.type = type;
        this.name = name;
    }

    //getters
    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

   @Override
   public String toString() {
        return "Parameter(type=" + type + ", name=" + name + ")";
   }
}