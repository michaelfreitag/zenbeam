package org.zenbeam;

import javax.lang.model.type.TypeMirror;

public class MethodInfo {

    private MethodInfo child;

    private String fieldName;

    private String name;

    private TypeMirror type;

    public MethodInfo getChild() {
        return child;
    }

    public void setChild(MethodInfo child) {
        this.child = child;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TypeMirror getType() {
        return type;
    }

    public void setType(TypeMirror type) {
        this.type = type;
    }
}
