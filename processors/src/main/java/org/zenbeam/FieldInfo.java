package org.zenbeam;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public class FieldInfo {

    private FieldInfo child;

    private FieldInfo parent;

    private TypeMirror type;

    private VariableElement field;

    private VariableElement owner;


    public FieldInfo cloneWithoutChild() {

        FieldInfo result = cloneWithoutParentChild();
        result.setParent(this.getParent());
        return result;

    }

    public FieldInfo cloneWithoutParentChild() {

        FieldInfo result = new FieldInfo();
        result.setOwner(this.getOwner());
        result.setType(this.getType());
        result.setField(this.getField());

        return result;

    }


    public FieldInfo cloneWithoutParent() {

        FieldInfo result = cloneWithoutParentChild();
        result.setChild(this.getChild());
        return result;

    }

    public FieldInfo getChild() {
        return child;
    }

    public void setChild(FieldInfo child) {
        this.child = child;
    }

    public FieldInfo getParent() {
        return parent;
    }

    public void setParent(FieldInfo parent) {
        this.parent = parent;
    }

    public TypeMirror getType() {
        return type;
    }

    public void setType(TypeMirror type) {
        this.type = type;
    }

    public VariableElement getField() {
        return field;
    }

    public void setField(VariableElement field) {
        this.field = field;
    }

    public VariableElement getOwner() {
        return owner;
    }

    public void setOwner(VariableElement owner) {
        this.owner = owner;
    }
}
