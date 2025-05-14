package org.example.ast;

import java.util.List;

public class ArrayInitializerNode extends AstNode{
    private final String type;
    private final String name;
    private final List<String> elements;

    public ArrayInitializerNode(String type, String name, List<String> elements) {
        this.type = type;
        this.name = name;
        this.elements = elements;
    }

    //getters
    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public List<String> getElements() {
        return elements;
    }

    @Override
    public String toString() {
        return "ArrayInitializer(" + type + " " + name + " = {" + String.join(",", elements) + "})";
    }
}
