package org.zenbeam.model;

import javax.lang.model.element.VariableElement;

public class FieldInfo {

    private FieldInfo child;

    private FieldInfo parent;

    private VariableElement field;

    private VariableElement owner;

    private String collectionKey;


    public FieldInfo cloneWithoutChild() {

        FieldInfo result = cloneWithoutParentChild();
        result.setParent(this.getParent());
        return result;

    }

    public FieldInfo cloneWithoutParentChild() {

        FieldInfo result = new FieldInfo();
        result.setOwner(this.getOwner());
        result.setField(this.getField());
        result.setCollectionKey(this.getCollectionKey());
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

    public String getCollectionKey() {
        return collectionKey;
    }

    public void setCollectionKey(String collectionKey) {
        this.collectionKey = collectionKey;
    }
}
