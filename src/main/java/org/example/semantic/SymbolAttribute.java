package org.example.semantic;

import org.example.ast.*;

import java.util.ArrayList;
import java.util.List;

public class SymbolAttribute extends Symbol {

    public SymbolAttribute(String name, AttributeNode node) {
        super(name, node);
    }

    @Override
    public String getKind() {
        return "Attribute";
    }

    public AttributeNode getAttributeNode() {
        return (AttributeNode) getNode();
    }

    public ValueNode getValueNode() {
        return getAttributeNode().getValue();
    }

    public List<String> getPossibleTypes() {
        List<String> possibleTypes = new ArrayList<>();
        ValueNode value = getValueNode();
        extractTypes(value, possibleTypes);
        return possibleTypes;
    }

    private void extractTypes(ValueNode node, List<String> types) {
        if (node instanceof ValueTypeNode) {
            types.add(((ValueTypeNode) node).getTypeName());
        } else if (node instanceof BaseValueNode) {
            types.add(((BaseValueNode) node).getValue());
        } else if (node instanceof ArrayValueNode) {
            ValueNode elementType = ((ArrayValueNode) node).getElementType();
            String elementTypeName = ((ValueTypeNode) elementType).getTypeName();
            types.add(elementTypeName + "[]");
        } else if (node instanceof OrValueNode) {
            for (ValueNode type : ((OrValueNode) node).getTypes()) {
                extractTypes(type, types); // Rekursivt kald for nested OrValueNodes
            }
        }
    }

    @Override
    public String toString(){
        return " type of value: " + getPossibleTypes();
    }
}