package org.example.ast;

import org.example.ast.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TypeNode extends AstNode {
    private final String name;
    private final List<AttributeNode> attributes;

    public TypeNode(String name, List<AttributeNode> attributes) {
        this.name = name;
        this.attributes = attributes;
    }

    //getters
    public String getName() {
        return name;
    }

    public List<AttributeNode> getAttributes() {
        return attributes;
    }


    @Override
    public String toString() {
        return "Type(Name = " + name + ", Attributes = " + attributes + ")";
    }

}

