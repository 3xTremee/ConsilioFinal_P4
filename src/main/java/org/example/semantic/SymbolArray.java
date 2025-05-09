package org.example.semantic;

import org.example.ast.ArrayInitializerNode;

import java.util.List;

public class SymbolArray extends Symbol {
    private final List<SymbolObject> objects = new java.util.ArrayList<>();

    public SymbolArray(String name, ArrayInitializerNode Ai) {
        super(name, Ai);}

    @Override
    public String getKind() {
        return "ObjectArray";
    }

    public ArrayInitializerNode getArrayInitializerNode() {
        return (ArrayInitializerNode) getNode();
    }

    public void addObjects(SymbolObject object) {
        objects.add(object);
    }

    public List<SymbolObject> getObjects() {
        return objects;
    }

}
