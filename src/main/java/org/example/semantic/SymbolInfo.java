package org.example.semantic;

public class SymbolInfo {
    public enum Kind { TYPE, ACTION, OBJECT, PARAM, ATTRIBUTE, VARIABLE, ARRAY }
    public final Kind    kind;
    public final Object  payload;   // e.g. a TypeNode, ActionNode, ObjectNode, FieldNode, etc.

    public SymbolInfo(Kind kind, Object payload) {
        this.kind    = kind;
        this.payload = payload;
    }
}
