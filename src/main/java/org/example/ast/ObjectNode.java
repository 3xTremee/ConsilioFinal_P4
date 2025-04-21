package org.example.ast;

public class ObjectNode extends AstNode {
    private final String type;
    private final String arrayName;
    private final String elementName;

    public ObjectNode(String type, String arrayName, String elementName) {
        this.type = type;
        this.arrayName = arrayName;
        this.elementName = elementName;
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

    @Override
    public String toString() {
        return "Object(type=)" + type + ", arrayName=" + arrayName + ", elementName=" + elementName +")";
    }
}
