package org.zenbeam;

import com.samskivert.mustache.Mustache;
import org.zenbeam.enums.DepthMode;
import org.zenbeam.model.FieldCommand;
import org.zenbeam.model.FieldInfo;
import org.zenbeam.util.ExceptionsUtils;
import org.zenbeam.util.FieldCommandUtils;
import org.zenbeam.util.FieldInfoUtils;
import org.zenbeam.util.StringUtils;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.*;

@SupportedAnnotationTypes("org.zenbeam.ZenBeamer")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class AnnotationProcessor extends AbstractProcessor {

    private enum MethodType {
        GET, SET
    }

    private enum ComparisonType {
        EQUAL, NOT_EQUAL
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        boolean result = true;

        try {
            for (final Element e : roundEnv.getElementsAnnotatedWith(ZenBeamer.class))

                if (e.getKind() == ElementKind.INTERFACE) {

                    final TypeElement element = (TypeElement) e;
                    final PackageElement packageElement = (PackageElement) element.getEnclosingElement();

                    final String generatedClassName = element.getSimpleName().toString() + "Impl";
                    final JavaFileObject jfo = AnnotationProcessor.this.processingEnv.getFiler().createSourceFile(
                            packageElement.getQualifiedName() + "." + generatedClassName
                    );

                    Map<String, Object> context = new HashMap<String, Object>();
                    context.put("packageName", packageElement.getQualifiedName().toString());
                    context.put("generatedClassName", generatedClassName);
                    context.put("interfaceName", element.getSimpleName().toString());

                    //import statements
                    List<String> imports = new ArrayList<String>();
                    List<String> methods = new ArrayList<String>();

                    //process methods
                    for (ExecutableElement ee : ElementFilter.methodsIn(element.getEnclosedElements())) {

                        //get projection definitions
                        Projections projections = ee.getAnnotation(Projections.class);

                        //method name
                        Map<String, Object> methodContext = new HashMap<String, Object>();
                        methodContext.put("name", ee.getSimpleName().toString());

                        //method attributes
                        List<String> methodAttributes = new ArrayList<String>();
                        VariableElement source = null;
                        VariableElement target = null;

                        for (VariableElement va : ee.getParameters()) {
                            //add type to import
                            imports.add(va.asType().toString());
                            //add method attribute
                            methodAttributes.add(va.asType() + " " + va.getSimpleName());

                            if (va.getSimpleName().toString().equalsIgnoreCase("source")) {
                                source = va;
                            }

                            if (va.getSimpleName().toString().equalsIgnoreCase("target")) {
                                target = va;
                            }

                        }

                        methodContext.put("visibility", ee.getReturnType().toString());
                        methodContext.put("returnType", ee.getReturnType().toString());
                        methodContext.put("signature", StringUtils.join(methodAttributes, ", "));
                        methodContext.put("body", buildProjections(projections, source, target));

                        final String method = Mustache.compiler().compile(CodeTemplates.getMethodTemplate()).execute(methodContext);

                        methods.add(method);


                    }
                    context.put("methods", methods);
                    context.put("imports", imports);

                    final String fileContents = Mustache.compiler().compile(CodeTemplates.getClassTemplate()).execute(context);
                    jfo.openWriter().append(fileContents).close();

                    printInfo("Class [" + generatedClassName + "] generated!");

                }

        } catch (Exception e) {
            printError(ExceptionsUtils.getStacktraceAsString(e));
            return result;
        }

        return result;
    }


    private void printError(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg);
    }

    private void printInfo(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, msg);
    }


    private VariableElement findField(String name, VariableElement element) {

        VariableElement result = null;
        List<Element> elements = new ArrayList<Element>();

        elements.add(processingEnv.getTypeUtils().asElement(element.asType()));

        for (TypeMirror tm : processingEnv.getTypeUtils().directSupertypes(element.asType())) {
            elements.add(processingEnv.getTypeUtils().asElement(tm));
        }

        for (Element e : elements) {
            for (VariableElement ve : ElementFilter.fieldsIn(e.getEnclosedElements())) {

                if (ve.getSimpleName().toString().equalsIgnoreCase(name)) {
                    result = ve;
                }

            }
        }

        if (result == null) {
            printError(String.format("Field [%s] not found in [%]", name, element.getSimpleName()));
        }

        return result;
    }

    private ExecutableElement findMethod(VariableElement owner, VariableElement field, MethodType methodType) {

        ExecutableElement result = null;

        List<Element> elements = new ArrayList<Element>();
        elements.add(processingEnv.getTypeUtils().asElement(owner.asType()));

        for (TypeMirror tm : processingEnv.getTypeUtils().directSupertypes(owner.asType())) {
            elements.add(processingEnv.getTypeUtils().asElement(tm));
        }


        String methodName = field.getSimpleName().toString();

        if (methodType == MethodType.GET) {
            methodName = "get" + methodName;
        }

        if (methodType == MethodType.SET) {
            methodName = "set" + methodName;
        }


        for (Element e : elements) {
            for (ExecutableElement method : ElementFilter.methodsIn(e.getEnclosedElements())) {

                if (methodName.equalsIgnoreCase(method.getSimpleName().toString().toLowerCase())) {
                    result = method;
                }

            }
        }

        if (result == null) {
            printError(String.format("No Method [%s] found in [%]", methodName, field.getSimpleName()));
        }


        return result;
    }


    private FieldInfo getFieldInfo(String property, VariableElement element, FieldInfo parent) {

        FieldInfo result = new FieldInfo();
        result.setParent(parent);
        result.setOwner(element);

        if (property != null && !property.isEmpty()) {

            if (property.contains(".")) {

                //get first property element
                result.setChild(getFieldInfo(property.substring(property.indexOf(".") + 1, property.length())
                        , findField(property.substring(0, property.indexOf(".")), element), result));

                //cut property name
                property = property.substring(0, property.indexOf("."));

            }

            result.setField(findField(property, element));

        }

        return result;
    }


    private String buildCondition(FieldInfo fieldInfo, DepthMode depthMode, ComparisonType comparisonType, String comparisionValue) {

        StringBuffer nullSaveCondition = new StringBuffer();

        String comparisionSign = " == ";
        if (comparisonType == ComparisonType.NOT_EQUAL) {
            comparisionSign = " != ";
        }

        nullSaveCondition.append(buildFullGetter(fieldInfo, depthMode)).append(comparisionSign).append(comparisionValue);
        return nullSaveCondition.toString();

    }

    private String buildGetter(FieldInfo fieldInfo) {

        StringBuffer getter = new StringBuffer();

        ExecutableElement method = findMethod(fieldInfo.getOwner(), fieldInfo.getField(), MethodType.GET);
        getter.append(method.getSimpleName()).append("()");

        return getter.toString();
    }


    private String buildFullGetter(FieldInfo fieldInfo, DepthMode depthMode) {

        StringBuffer result = new StringBuffer();

        //in case of root node set add owner attribute name
        if (fieldInfo.getParent() == null) {
            result.append(fieldInfo.getOwner()).append(".");
        }

        ExecutableElement method = findMethod(fieldInfo.getOwner(), fieldInfo.getField(), MethodType.GET);
        result.append(method.getSimpleName()).append("()");

        if (FieldInfoUtils.shouldTraverse(fieldInfo, depthMode)) {

            result.append(".").append(buildFullGetter(fieldInfo.getChild(), depthMode));
        }

        return result.toString();
    }

    private String buildInstanciation(FieldInfo fieldInfo) {

        StringBuffer result = new StringBuffer();
        result.append("new ").append(fieldInfo.getField().asType().toString()).append("()");
        return result.toString();

    }

    private String buildSetter(FieldInfo fieldInfo, String command) {

        StringBuffer result = new StringBuffer();
        ExecutableElement method = findMethod(fieldInfo.getOwner(), fieldInfo.getField(), MethodType.SET);

        result.append(fieldInfo.getOwner()).append(".");

        result.append(method.getSimpleName())
                .append("(")
                .append(command)
                .append(");");

        return result.toString();

    }

    private String buildFullSetter(FieldInfo fieldInfo, String command) {

        StringBuffer result = new StringBuffer();
        FieldInfo childestChild = FieldInfoUtils.getDeepestChild(fieldInfo);
        ExecutableElement method = findMethod(childestChild.getOwner(), childestChild.getField(), MethodType.SET);

        if (fieldInfo.getChild() != null) {
            result.append(buildFullGetter(fieldInfo, DepthMode.GROUND_FLOOR)).append(".");
        } else {
            result.append(fieldInfo.getOwner()).append(".");
        }

        result.append(method.getSimpleName())
                .append("(")
                .append(command)
                .append(");");

        return result.toString();

    }


    private String renderIfCondition(String condition, String body) {

        Map<String, String> commandModel = new HashMap<String, String>();
        if (condition != null && !condition.isEmpty()) {
            commandModel.put("condition", condition);
        }
        commandModel.put("body", body);

        return Mustache.compiler().compile(CodeTemplates.getIfTemplate()).execute(commandModel);

    }

    private List<FieldCommand> buildSetterPreparation(FieldInfo fieldInfo) {

        List<FieldCommand> commands = new ArrayList<FieldCommand>();

        String setter;
        String nullSaveCondition;

        if (fieldInfo.getChild() != null) {

            //null save prepare
            FieldInfo fieldInfoWithOutChild = FieldInfoUtils.getRoot(FieldInfoUtils.cloneUp(fieldInfo));
            nullSaveCondition = buildCondition(fieldInfoWithOutChild, DepthMode.BASEMENT, ComparisonType.EQUAL, "null");
            setter = buildFullSetter(fieldInfoWithOutChild, buildInstanciation(fieldInfo));

            commands.add(new FieldCommand(FieldInfoUtils.getPath(fieldInfoWithOutChild), renderIfCondition(nullSaveCondition, setter)));

            if (fieldInfo.getChild().getChild() != null) {
                commands.addAll(buildSetterPreparation(fieldInfo.getChild()));
            }

        } else {


            nullSaveCondition = buildCondition(fieldInfo, DepthMode.BASEMENT, ComparisonType.EQUAL, "null");
            setter = buildSetter(fieldInfo, buildInstanciation(fieldInfo));

            commands.add(new FieldCommand(FieldInfoUtils.getPath(fieldInfo), renderIfCondition(nullSaveCondition, setter)));

        }


        return commands;
    }

    private String buildProjections(Projections projections, VariableElement source, VariableElement target) {

        List<FieldCommand> commandBlockList = new ArrayList<FieldCommand>();

        //go throw every definition
        for (Projection p : projections.value()) {

            FieldInfo fieldInfoSource = FieldInfoUtils.getRoot(getFieldInfo(p.source().toLowerCase(), source, null));
            FieldInfo fieldInfoTarget = FieldInfoUtils.getRoot(getFieldInfo(p.target().toLowerCase(), target, null));


            /* setter preparation is only required if depth > 1 */
            if (fieldInfoTarget.getChild() != null) {
                commandBlockList.addAll(buildSetterPreparation(fieldInfoTarget));
            }


            /* instantiate new on update if not null */
            if (p.instantiateNewIfNotNull()) {
                commandBlockList.add(new FieldCommand(FieldInfoUtils.getPath(fieldInfoTarget), buildInstanciationIfNotNull(fieldInfoSource, fieldInfoTarget)));
            }

            /* null save condition is only required if source has depth > 1 */
            StringBuffer condition = new StringBuffer();

            if (fieldInfoSource.getChild() != null) {
                condition.append(buildCondition(fieldInfoSource, DepthMode.GROUND_FLOOR, ComparisonType.NOT_EQUAL, "null"));
            }

            String command = buildFullSetter(fieldInfoTarget, buildTypeConversion(fieldInfoSource, fieldInfoTarget, buildFullGetter(fieldInfoSource, DepthMode.BASEMENT)));
            if (condition.length() > 0) {
                command = renderIfCondition(condition.toString(), command);
            }

            commandBlockList.add(new FieldCommand(FieldInfoUtils.getPath(fieldInfoTarget), command));

            /*
            String finalCommand = FieldCommandUtils.getCommandsAsString(commandBlockList);
            if (condition.length() > 0) {
                finalCommand = renderIfCondition(condition.toString(), FieldCommandUtils.getCommandsAsString(commandBlockList));
            }

            sb.append(finalCommand).append("\r\n");
            */

        }

        FieldCommandUtils.removeDuplicates(commandBlockList);
        FieldCommandUtils.sortFieldCommandListByFieldDepth(commandBlockList);


        return FieldCommandUtils.getCommandsAsString(commandBlockList);
    }


    private String buildInstanciationIfNotNull(FieldInfo source, FieldInfo target) {

        FieldInfo targetField = FieldInfoUtils.getDeepestChild(target);
        if (targetField.getParent() != null) {
            targetField = FieldInfoUtils.getRoot(FieldInfoUtils.cloneUp(targetField.getParent()));
        } else {
            targetField = FieldInfoUtils.getRoot(targetField);
        }



        StringBuffer condition = new StringBuffer();

        condition.append(buildCondition(target, DepthMode.BASEMENT, ComparisonType.NOT_EQUAL, buildTypeConversion(source, target, buildFullGetter(source, DepthMode.BASEMENT))));

        StringBuffer sb = new StringBuffer();
        sb.append("/* instantiate if not null */").append("\r\n");
        sb.append(renderIfCondition(condition.toString(), buildFullSetter(targetField, buildInstanciation(FieldInfoUtils.getDeepestChild(targetField)))));

        return sb.toString();


    }

    private String buildTypeConversion(FieldInfo source, FieldInfo target, String getterCommand) {

        FieldInfo sourceField = FieldInfoUtils.getDeepestChild(source);
        FieldInfo targetField = FieldInfoUtils.getDeepestChild(target);


        if (!processingEnv.getTypeUtils().isAssignable(sourceField.getField().asType(), targetField.getField().asType())) {


            getterCommand = "propertyConverter.convert(" + targetField.getField().asType().toString() + ".class , " + getterCommand + ")";

            /*
            // to String.class
            if (targetField.getField().asType().toString().equalsIgnoreCase(String.class.getCanonicalName())) {
                getterCommand += ".toString()";
            }

            // to Long.class
            if (targetField.getField().asType().toString().equalsIgnoreCase(Long.class.getCanonicalName())) {

            }
            */

        }


        return getterCommand;

    }

}
