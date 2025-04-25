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

    public boolean hasAttribute(String attributeName) {
        if (attributes == null) {
            return false;
        }
        for (AttributeNode attribute : attributes) {
            if (attribute.getIdentifier().equals(attributeName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Type(name=" + name + ", attributes=" + attributes + ")";
    }

    /* muligvis lettes
    public List<String> getAttributeType(String field) {
        List<String> returnValue = new ArrayList<>(); // Use a mutable list
        if (attributes == null) {
            return returnValue; // Return an empty list if attributes are null
        }
        for (AttributeNode attribute : attributes) {
             if (attribute.getIdentifier().equals(field)) {
                ValueNode value = attribute.getValue();
                if (value == null) {
                    continue;
                }
                if (value instanceof ValueTypeNode) {
                    returnValue.add(((ValueTypeNode) value).getTypeName());
                } else if (value instanceof BaseValueNode) {
                    returnValue.add(((BaseValueNode) value).getValue());
                } else if (value instanceof OrValueNode) {
                    List<ValueNode> types = ((OrValueNode) value).getTypes();
                    for (ValueNode type : types) {
                        if (type instanceof ValueTypeNode) {
                            returnValue.add(((ValueTypeNode) type).getTypeName());
                        } else if (type instanceof BaseValueNode) {
                            returnValue.add(((BaseValueNode) type).getValue());
                        }
                    }
                } else {
                    System.out.println("Unexpected ValueNode type: " + value.getClass().getSimpleName());
                }
            }
        }
        return returnValue; // Return the populated list
    }
    */

}

