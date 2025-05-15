package org.example.semantic.symbols;

import org.example.ast.TypeNode;
import java.util.LinkedHashMap;
import java.util.Map;

public class SymbolType extends Symbol {
    private final Map<String, SymbolAttribute> attributes = new LinkedHashMap<>();

    public SymbolType(String name, TypeNode typeNode) {
        super(name, typeNode);
    }

    @Override
    public String getKind() {
        return "Type";
    }

    public void addAttribute(String attributeName, SymbolAttribute attributeSymbol) {
        attributes.put(attributeName, attributeSymbol);
    }

    public Map<String, SymbolAttribute> getAttributes() {
        return attributes;
    }

    @Override
    public String toString() {
        return "SymbolType(attributes=" + attributes + ")";
    }
}
