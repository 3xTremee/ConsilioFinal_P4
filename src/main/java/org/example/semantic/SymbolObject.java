package org.example.semantic;

import org.example.ast.ObjectNode;

import java.util.LinkedHashMap;
import java.util.Map;

public class SymbolObject extends Symbol{
    private final SymbolType type;
    private Map<String, Object> attributes;

    public SymbolObject(String name, ObjectNode objectNode, SymbolType type) {
        super(name, objectNode);
        this.type = type;
        this.attributes = new LinkedHashMap<>();
    }

    @Override
    public String getKind() {
        return "Object";
    }

    public String getType (){
        return type.getName();
    }

    public SymbolType getSymbolType() {
        return type;
    }

    public void addAttributes(String attributeName, Object value) {
        if (attributes.containsKey(attributeName)) {
            throw new SemanticException("Duplicate attribute declaration: " + attributeName);
        }
        attributes.put(attributeName, value);
    }

    public void setAttributeValue(String attributeName, Object value) {
        attributes.put(attributeName, value);
    }

    public Object getAttributeValue(String attributeName) {
        return attributes.get(attributeName);
    }

    public void updateAttributes(String attributeName, Object value) {
        attributes.put(attributeName, value);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String toString() {
        return "SymbolObject(type=" + type + ", attributes=" + attributes + ")";
    }
}
