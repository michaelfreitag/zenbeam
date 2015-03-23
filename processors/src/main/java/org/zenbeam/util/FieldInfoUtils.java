package org.zenbeam.util;

import org.zenbeam.enums.DepthMode;
import org.zenbeam.model.FieldInfo;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

public class FieldInfoUtils {

    public static FieldInfo cloneUp(FieldInfo fieldInfo) {

        FieldInfo result = fieldInfo.cloneWithoutParentChild();
        if (fieldInfo.getParent() != null) {
            FieldInfo parentCloned = cloneUp(fieldInfo.getParent());
            parentCloned.setChild(result);
            result.setParent(parentCloned);
        }

        return result;
    }

    public static FieldInfo cloneDown(FieldInfo fieldInfo) {

        FieldInfo result = fieldInfo.cloneWithoutParentChild();
        if (fieldInfo.getChild()!= null) {
            FieldInfo childCloned = cloneDown(fieldInfo.getChild());
            childCloned.setChild(result);
            result.setChild(childCloned);
        }

        return result;
    }



    public static FieldInfo getRoot(FieldInfo fieldInfo) {
        if (fieldInfo.getParent() == null) {
            return fieldInfo;
        } else {
            return getRoot(fieldInfo.getParent());
        }
    }


    public static String getPath(FieldInfo fieldInfo, boolean voteUp) {

        StringBuffer result = new StringBuffer();

        if (fieldInfo.getChild() != null) {
            result.append(fieldInfo.getField().getSimpleName()).append(".").append(getPath(fieldInfo.getChild(), voteUp));
        } else {

            if (voteUp) {
                result.append("!");
            }

            result.append(fieldInfo.getField().getSimpleName());
        }

        return result.toString();
    }

    public static String getPath(FieldInfo fieldInfo) {

        return getPath(fieldInfo, false);
    }


    public static FieldInfo getDeepestChild(FieldInfo fieldInfo) {
        if (fieldInfo.getChild() == null) {
            return fieldInfo;
        } else {
            return getDeepestChild(fieldInfo.getChild());
        }
    }

    public static boolean shouldTraverse(FieldInfo fieldInfo, DepthMode depthMode) {

        boolean traverse = false;
        if (depthMode == DepthMode.BASEMENT) {
            if (fieldInfo.getChild() != null) {
                traverse = true;
            }
        }

        if (depthMode == DepthMode.GROUND_FLOOR) {
            if (fieldInfo.getChild() != null && fieldInfo.getChild().getChild() != null) {
                traverse = true;
            }
        }

        return traverse;
    }


    public static String getFirstFieldName(String s) {

        String result = s;

        if (s.contains(".")) {

            if (s.contains("[") && s.indexOf("[") < s.indexOf(".")) {
                result = s.substring(0, s.indexOf("["));
            } else {
                result = s.substring(0, s.indexOf("."));
            }

        } else if (s.contains("[")) {
            result = s.substring(0, s.indexOf("["));
        }

        return result;
    }


    public static String getFirstCollectionKey(String s) {

        String result = null;

        if (s.contains("[")) {
            result = s.substring(s.indexOf("[") + 1, s.indexOf("]"));
        }

        return result;
    }

    public static boolean isList(String typeName) {
        return typeName.startsWith("java.util.List");
    }

    public static TypeMirror getListType(TypeMirror type) {

        TypeMirror result = null;

        if (type instanceof DeclaredType) {
            DeclaredType declaredType = (DeclaredType) type;
            if (!declaredType.getTypeArguments().isEmpty()) {
                for (TypeMirror genericMirrorType : declaredType.getTypeArguments()) {
                    result = genericMirrorType;
                }
            }
        }

        return result;

    }


    public static FieldInfo getFirstListProperty(FieldInfo fieldInfo) {

        if (isList(fieldInfo.getField().asType().toString())) {
            return fieldInfo;
        } else if (fieldInfo.getChild() != null) {
            return getFirstListProperty(fieldInfo.getChild());
        } else {
            return null;
        }

    }


}
