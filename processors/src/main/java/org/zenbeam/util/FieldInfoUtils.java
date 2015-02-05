package org.zenbeam.util;

import org.zenbeam.enums.DepthMode;
import org.zenbeam.model.FieldInfo;

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

    public static FieldInfo getRoot(FieldInfo fieldInfo) {
        if (fieldInfo.getParent() == null) {
            return fieldInfo;
        } else {
            return getRoot(fieldInfo.getParent());
        }
    }


    public static String getPath(FieldInfo fieldInfo) {

        StringBuffer result = new StringBuffer();

        if (fieldInfo.getChild() != null) {
            result.append(fieldInfo.getField().getSimpleName()).append(".").append(getPath(fieldInfo.getChild()));
        } else {
            result.append(fieldInfo.getField().getSimpleName());
        }

        return result.toString();
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


}
