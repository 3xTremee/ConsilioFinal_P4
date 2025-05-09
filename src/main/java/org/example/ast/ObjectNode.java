package org.example.ast;

public class ObjectNode extends AstNode {
    private final String type;
    private final String arrayName;
    private final String elementName;
    private final IdentifierNode identifier;

    public ObjectNode(String type, String arrayName, String elementName, IdentifierNode identifier) {
        this.type = type;
        this.arrayName = arrayName;
        this.elementName = elementName;
        this.identifier = identifier;
    }

    //getters
    public String getType() {
        return type;
    }

    public String getArrayName() {
        return arrayName;
    }

    public String getElementName() {
        return elementName;
    }

    public IdentifierNode getIdentifier() {
        return identifier;
    }

    @Override
    public String toString() {
        return "(ObjectType=" + type + ", arrayName=" + arrayName + ", elementName=" + elementName +")";
    }
}
