package org.example.ast;

public class ArrayValueNode extends ValueNode {
    private final ValueNode elementType;
    private final int size;

    public ArrayValueNode(ValueNode elementType, int size) {
        this.elementType = elementType;
        this.size = size;
    }

    //getters
    public ValueNode getElementType() {
        return elementType;
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        return "ArrayValueNode(elementType=" + elementType + ", size=" + size + ")";
    }
}
