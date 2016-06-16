package org.zenbeam;

import org.zenbeam.enums.DepthMode;
import org.zenbeam.model.FieldCommand;
import org.zenbeam.model.FieldInfo;
import org.zenbeam.model.ServiceResource;
import org.zenbeam.service.CodeRenderService;
import org.zenbeam.service.MustacheCodeRenderService;
import org.zenbeam.util.*;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.beans.Introspector;
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

    private enum ConditionMode {
        SOURCE, TARGET
    }

    private CodeRenderService codeRenderService = new MustacheCodeRenderService();

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

                    //import statements
                    List<String> imports = new ArrayList<String>();
                    List<String> methods = new ArrayList<String>();
                    Map<String, ServiceResource> serviceAttributes = new HashMap<String, ServiceResource>();

                    //process methods
                    for (ExecutableElement ee : ElementFilter.methodsIn(element.getEnclosedElements())) {

                        //get projection definitions
                        Projections projections = ee.getAnnotation(Projections.class);

                        //get all service resources
                        serviceAttributes.putAll(getEntityServices(projections));

                        //method attributes
                        List<String> methodAttributes = new ArrayList<String>();
                        VariableElement source = null;
                        VariableElement target = null;

                        for (VariableElement va : ee.getParameters()) {
                            //add type to import
                            imports.add(va.asType().toString());
                            //add method attribute
                            methodAttributes.add(va.asType() + " " + va.getSimpleName());

                            //detect source element by name
                            if (va.getSimpleName().toString().equalsIgnoreCase("source")) {
                                source = va;
                            }

                            //detect target element by name
                            if (va.getSimpleName().toString().equalsIgnoreCase("target")) {
                                target = va;
                            }

                        }


                        final String method = codeRenderService.renderMethod(ee.getReturnType().toString(),
                                ee.getReturnType().toString(),
                                ee.getSimpleName().toString(),
                                StringUtils.join(methodAttributes, ", "),
                                buildProjections(projections, source, target));

                        methods.add(method);


                    }

                    final String fileContents = codeRenderService.renderClass(packageElement.getQualifiedName().toString(),
                            imports,
                            generatedClassName,
                            element.getSimpleName().toString(),
                            serviceAttributes.values(),
                            methods);
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


    public List<TypeMirror> getAllSuperTypes(TypeMirror type) {

        List<TypeMirror> result = new ArrayList<>();

        try {
            List<? extends TypeMirror> superTypes = processingEnv.getTypeUtils().directSupertypes(type);
            while (superTypes != null && !superTypes.isEmpty()) {
                TypeMirror superClass = superTypes.get(0);
                result.add(superClass);
                superTypes = processingEnv.getTypeUtils().directSupertypes(superClass);
            }
        } catch (RuntimeException e) {
            printError("Failed to find all super classes of type '" + type.toString() + ". Possibly missing classes?");
        }

        return result;
    }

    private VariableElement findField(String name, TypeMirror typeMirror) {

        VariableElement result = null;
        List<Element> elements = new ArrayList<Element>();

        elements.add(processingEnv.getTypeUtils().asElement(typeMirror));

        List<TypeMirror> superTypes = getAllSuperTypes(typeMirror);
        for (TypeMirror tm : superTypes) {
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
            printError(String.format("Field [%s] not found in [%s]", name, typeMirror.toString()));
        }

        return result;
    }

    private ExecutableElement findMethodByName(VariableElement owner, String name) {

        ExecutableElement result = null;

        List<Element> elements = new ArrayList<Element>();

        elements.add(processingEnv.getTypeUtils().asElement(owner.asType()));

        List<TypeMirror> superTypes = getAllSuperTypes(owner.asType());
        for (TypeMirror tm : superTypes) {
            elements.add(processingEnv.getTypeUtils().asElement(tm));
        }

        for (Element e : elements) {
            for (ExecutableElement method : ElementFilter.methodsIn(e.getEnclosedElements())) {

                if (name.equalsIgnoreCase(method.getSimpleName().toString().toLowerCase())) {
                    result = method;
                }

            }
        }

        if (result == null) {
            printError(String.format("No Method [%s] found in [%]", name, owner.getSimpleName()));
        }


        return result;
    }



    private ExecutableElement findMethodByField(VariableElement owner, VariableElement field, MethodType methodType) {

        ExecutableElement result = null;

        List<Element> elements = new ArrayList<Element>();

        if (FieldInfoUtils.isList(owner.asType().toString())) {
            elements.add(processingEnv.getTypeUtils().asElement(FieldInfoUtils.getListType(owner.asType())));
        } else {
            elements.add(processingEnv.getTypeUtils().asElement(owner.asType()));
        }

        List<TypeMirror> superTypes = getAllSuperTypes(owner.asType());
        for (TypeMirror tm : superTypes) {
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
            printError(String.format("No Method [%s] found in [%s]", methodName, field.getSimpleName()));
        }


        return result;
    }


    private FieldInfo getFieldInfo(String property, VariableElement element, FieldInfo parent, String collectionKey) {

        FieldInfo result = new FieldInfo();
        result.setParent(parent);
        result.setOwner(element);

        if (parent != null) {
            parent.setChild(result);
        }


        TypeMirror elementType = element.asType();

        //is List?
        FieldInfo listGetter = new FieldInfo();
        if (FieldInfoUtils.isList(elementType.toString())) {

            listGetter.setOwner(element);
            listGetter.setCollectionKey(collectionKey);
            listGetter.setField(result.getField());

            // linking parent/child
            listGetter.setChild(result);
            listGetter.setParent(parent);
            result.setParent(listGetter);

            if (parent != null) {
                parent.setChild(listGetter);
            }

            elementType = FieldInfoUtils.getListType(elementType);
        }


        if (property != null && !property.isEmpty()) {

            if (property.contains(".")) {

                //get first property element
                getFieldInfo(property.substring(property.indexOf(".") + 1, property.length())
                        , findField(FieldInfoUtils.getFirstFieldName(property), elementType), result, FieldInfoUtils.getFirstCollectionKey(property));

                //cut property name
                property = property.substring(0, property.indexOf("."));

            }

            VariableElement field = findField(FieldInfoUtils.getFirstFieldName(property), elementType);
            result.setField(field);
            listGetter.setField(field);


        }

        return result;
    }


    private String buildCondition(FieldInfo fieldInfo, DepthMode depthMode, ComparisonType comparisonType, String comparisionValue, ConditionMode conditionMode) {

        StringBuffer nullSaveCondition = new StringBuffer();

        //default equals
        String comparisonMethod = ".equals(";
        String comparisonPrefix = "";
        String comparisonSuffix = ")";
        if (comparisonType == ComparisonType.NOT_EQUAL) {
            comparisonPrefix = "!";
        }

        if (comparisionValue.equals("null")) {

            comparisonMethod = " == ";
            comparisonPrefix = "";
            comparisonSuffix = "";
            if (comparisonType == ComparisonType.NOT_EQUAL) {
                comparisonMethod = " != ";
            }
        }


        FieldInfo childestChild = FieldInfoUtils.getDeepestChild(fieldInfo);

        if (childestChild.getParent() != null && conditionMode == ConditionMode.SOURCE) {
            /* in case of source the whole property path must be null save */
            nullSaveCondition.append(buildCondition(FieldInfoUtils.cloneUp(childestChild.getParent()), depthMode, comparisonType, comparisionValue, conditionMode)).append(" && ");
        }

        if (FieldInfoUtils.isList(childestChild.getOwner().asType().toString()) && childestChild.getCollectionKey() != null && !childestChild.getCollectionKey().isEmpty() && conditionMode == ConditionMode.TARGET) {

            Long listIndex = Long.valueOf(childestChild.getCollectionKey());
            if (listIndex != null && listIndex >= 0) {
                nullSaveCondition.append(buildFullGetter(FieldInfoUtils.getRoot(childestChild), depthMode, "size")).append(" <= ").append(childestChild.getCollectionKey());
            }

        } else {
            nullSaveCondition.append(comparisonPrefix).append(buildFullGetter(FieldInfoUtils.getRoot(fieldInfo), depthMode, MethodType.GET.name())).append(comparisonMethod).append(comparisionValue).append(comparisonSuffix);
        }

        return nullSaveCondition.toString();

    }

    private String buildGetter(FieldInfo fieldInfo) {

        StringBuffer getter = new StringBuffer();

        ExecutableElement method = findMethodByField(fieldInfo.getOwner(), fieldInfo.getField(), MethodType.GET);
        getter.append(method.getSimpleName()).append("()");

        return getter.toString();
    }


    private String buildFullGetter(FieldInfo fieldInfo, DepthMode depthMode, String methodName) {

        StringBuffer result = new StringBuffer();

        //in case of root node set add owner attribute name
        if (fieldInfo.getParent() == null) {
            result.append(fieldInfo.getOwner()).append(".");
        }

        ExecutableElement method;
        if (fieldInfo.getParent() != null && FieldInfoUtils.isList(fieldInfo.getParent().getField().asType().toString())) {
            method = findMethodByName(fieldInfo.getOwner(), methodName);
        } else {
            method = findMethodByField(fieldInfo.getOwner(), fieldInfo.getField(), MethodType.GET);
        }
        result.append(method.getSimpleName());


        if (fieldInfo.getCollectionKey() != null && !fieldInfo.getCollectionKey().isEmpty() && !method.getParameters().isEmpty()) {
            result.append("(").append(fieldInfo.getCollectionKey()).append(")");
        } else {
            result.append("()");
        }

        if (FieldInfoUtils.shouldTraverse(fieldInfo, depthMode)) {
            result.append(".").append(buildFullGetter(fieldInfo.getChild(), depthMode, methodName));
        }

        return result.toString();
    }

    private String buildInstanciation(FieldInfo fieldInfo) {

        StringBuffer result = new StringBuffer();

        //if fieldtype is list
        if (fieldInfo.getField() != null && FieldInfoUtils.isList(fieldInfo.getField().asType().toString())) {
            result.append("new java.util.ArrayList<").append(FieldInfoUtils.getListType(fieldInfo.getField().asType())).append(">()");
        } else if (fieldInfo.getParent() != null && FieldInfoUtils.isList(fieldInfo.getParent().getField().asType().toString())) {
            result.append("new ").append(FieldInfoUtils.getListType(fieldInfo.getParent().getField().asType()).toString()).append("()");
        } else {
            result.append("new ").append(fieldInfo.getField().asType().toString()).append("()");
        }


        return result.toString();

    }

    private String buildSetter(FieldInfo fieldInfo, String command) {

        StringBuffer result = new StringBuffer();
        ExecutableElement method = findMethodByField(fieldInfo.getOwner(), fieldInfo.getField(), MethodType.SET);

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
        ExecutableElement method;
        if (childestChild.getParent() != null && FieldInfoUtils.isList(childestChild.getParent().getField().asType().toString())) {
            method = findMethodByName(childestChild.getOwner(), "add");
        } else {
            method = findMethodByField(childestChild.getOwner(), childestChild.getField(), MethodType.SET);
        }



        if (fieldInfo.getChild() != null) {
            result.append(buildFullGetter(fieldInfo, DepthMode.GROUND_FLOOR, MethodType.GET.name())).append(".");
        } else {
            result.append(fieldInfo.getOwner()).append(".");
        }

        result.append(method.getSimpleName())
                .append("(")
                .append(command)
                .append(");");

        return result.toString();

    }


    private List<FieldCommand> buildSetterPreparation(FieldInfo fieldInfo) {

        List<FieldCommand> commands = new ArrayList<FieldCommand>();

        String setter;
        String nullSaveCondition;

        if (fieldInfo.getChild() != null) {

            //null save prepare
            FieldInfo fieldInfoWithOutChild = FieldInfoUtils.getRoot(FieldInfoUtils.cloneUp(fieldInfo));
            nullSaveCondition = buildCondition(fieldInfoWithOutChild, DepthMode.BASEMENT, ComparisonType.EQUAL, "null", ConditionMode.TARGET);
            setter = buildFullSetter(fieldInfoWithOutChild, buildInstanciation(fieldInfo));

            commands.add(new FieldCommand(FieldInfoUtils.getPath(fieldInfoWithOutChild), codeRenderService.renderIfCondition(nullSaveCondition, setter)));

            if (fieldInfo.getChild().getChild() != null) {
                commands.addAll(buildSetterPreparation(fieldInfo.getChild()));
            }

        } else {


            nullSaveCondition = buildCondition(fieldInfo, DepthMode.BASEMENT, ComparisonType.EQUAL, "null", ConditionMode.TARGET);
            setter = buildSetter(fieldInfo, buildInstanciation(fieldInfo));

            commands.add(new FieldCommand(FieldInfoUtils.getPath(fieldInfo), codeRenderService.renderIfCondition(nullSaveCondition, setter)));

        }


        return commands;
    }


    private Map<String, ServiceResource> getEntityServices(Projections projections) {

        Map<String, ServiceResource> result = new HashMap<String, ServiceResource>();

        for (Projection p : projections.value()) {
            ServiceResource serviceResource = getServiceResource(p);
            if (serviceResource != null) {
                result.put(serviceResource.getClazz(), serviceResource);
            }
        }

        return result;
    }

    private ServiceResource getServiceResource(Projection p) {

        ServiceResource result = null;

        String entityServiceClass = null;
        String simpleNameOfEntityServiceClass = null;

        try {
            p.entityService();
        }  catch( MirroredTypeException mte ) {
            TypeMirror tm =  mte.getTypeMirror();
            TypeElement te = (TypeElement)processingEnv.getTypeUtils().asElement(tm);
            entityServiceClass = te.getQualifiedName().toString();
            simpleNameOfEntityServiceClass = te.getSimpleName().toString();
        }

        if (entityServiceClass != null && !entityServiceClass.isEmpty() && !entityServiceClass.equalsIgnoreCase(ProjectionEntityService.class.getName())) {
            result = new ServiceResource();
            result.setName(Introspector.decapitalize(simpleNameOfEntityServiceClass));
            result.setClazz(entityServiceClass);

        }

        return result;
    }

    private String buildProjections(Projections projections, VariableElement source, VariableElement target) {

        List<FieldCommand> commandBlockList = new ArrayList<FieldCommand>();

        //go throw every definition
        for (Projection p : projections.value()) {

            FieldInfo fieldInfoSource = FieldInfoUtils.getRoot(getFieldInfo(p.source().toLowerCase(), source, null, null));
            FieldInfo fieldInfoTarget = FieldInfoUtils.getRoot(getFieldInfo(p.target().toLowerCase(), target, null, null));


            //if list, get subtree under list, create separate object
            FieldInfo fieldInfoList = FieldInfoUtils.getFirstListProperty(fieldInfoTarget);

            if (fieldInfoList != null) {
                commandBlockList.addAll(buildSetterPreparation(fieldInfoTarget));
            }

            /* setter preparation is only required if depth > 1 */
            if (fieldInfoTarget.getChild() != null) {
                commandBlockList.addAll(buildSetterPreparation(fieldInfoTarget));
            }


            /* instantiate new on update if not null */
            if (p.instantiateNewIfNotNull()) {
                commandBlockList.add(new FieldCommand(FieldInfoUtils.getPath(fieldInfoTarget, true), buildInstanciationIfNotNull(fieldInfoSource, fieldInfoTarget)));
            }

            ServiceResource serviceResource = getServiceResource(p);
            if (serviceResource != null) {
                commandBlockList.add(new FieldCommand(FieldInfoUtils.getPath(fieldInfoTarget, true), loadFromServiceResource(fieldInfoSource, fieldInfoTarget, serviceResource)));
            }

            /* null save condition is only required if source has depth > 1 */
            StringBuffer condition = new StringBuffer();

            if (fieldInfoSource.getChild() != null) {
                condition.append(buildCondition(fieldInfoSource, DepthMode.GROUND_FLOOR, ComparisonType.NOT_EQUAL, "null", ConditionMode.SOURCE));
            }

            String command = buildFullSetter(fieldInfoTarget, buildTypeConversion(fieldInfoSource, fieldInfoTarget, buildFullGetter(fieldInfoSource, DepthMode.BASEMENT, MethodType.GET.name())));
            if (condition.length() > 0) {
                command = codeRenderService.renderIfCondition(condition.toString(), command);
            }

            commandBlockList.add(new FieldCommand(FieldInfoUtils.getPath(fieldInfoTarget), command));

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


        //enclosed condition if property is different target
        StringBuffer condition = new StringBuffer();
        condition.append(buildCondition(target, DepthMode.BASEMENT, ComparisonType.NOT_EQUAL, "null", ConditionMode.TARGET));
        condition.append(" && ");
        condition.append(buildCondition(target, DepthMode.BASEMENT, ComparisonType.NOT_EQUAL, buildTypeConversion(source, target, buildFullGetter(source, DepthMode.BASEMENT, MethodType.GET.name())), ConditionMode.TARGET));

        String instantiationBlock = buildFullSetter(targetField, buildInstanciation(FieldInfoUtils.getDeepestChild(targetField)));

        StringBuffer sb = new StringBuffer();
        sb.append("/* instantiate if not null */").append("\r\n");
        sb.append(codeRenderService.renderIfCondition(condition.toString(), instantiationBlock));

        return sb.toString();
    }

    private String loadFromServiceResource(FieldInfo source, FieldInfo target, ServiceResource serviceResource) {

        FieldInfo targetField = FieldInfoUtils.getDeepestChild(target);
        if (targetField.getParent() != null) {
            targetField = FieldInfoUtils.getRoot(FieldInfoUtils.cloneUp(targetField.getParent()));
        } else {
            targetField = FieldInfoUtils.getRoot(targetField);
        }

        //enclosed condition if property is different target
        StringBuffer condition = new StringBuffer();
        condition.append(buildCondition(source, DepthMode.BASEMENT, ComparisonType.NOT_EQUAL, "null", ConditionMode.TARGET));
        String instantiationBlock = "";

        //handle resource Service
        if (serviceResource != null) {
            String getObjectFromEntityService = serviceResource.getName() + ".get(" + buildFullGetter(source, DepthMode.BASEMENT, MethodType.GET.name()) + ")";
            instantiationBlock = buildFullSetter(targetField, getObjectFromEntityService);
        }

        StringBuffer sb = new StringBuffer();
        if (!instantiationBlock.equals("")) {
            sb.append("/* load from service resource */").append("\r\n");
            sb.append(codeRenderService.renderIfCondition(condition.toString(), instantiationBlock));
        }

        return sb.toString();
    }


    private String buildTypeConversion(FieldInfo source, FieldInfo target, String getterCommand) {

        FieldInfo sourceField = FieldInfoUtils.getDeepestChild(source);
        FieldInfo targetField = FieldInfoUtils.getDeepestChild(target);


        if (!processingEnv.getTypeUtils().isAssignable(sourceField.getField().asType(), targetField.getField().asType())) {

            //check collections
            if (targetField.getField().asType().toString().startsWith("java.util.List")) {

                TypeMirror genericTypeArgument = null;

                if (targetField.getField().asType() instanceof DeclaredType) {
                    DeclaredType declaredType = (DeclaredType) targetField.getField().asType();
                    if (!declaredType.getTypeArguments().isEmpty()) {
                        for (TypeMirror genericMirrorType : declaredType.getTypeArguments()) {
                            genericTypeArgument = genericMirrorType;
                        }
                    }
                }

                getterCommand = "propertyConverter.convertToList(" + genericTypeArgument.toString() + ".class , " + getterCommand + ")";
            } else {
                getterCommand = "propertyConverter.convert(" + targetField.getField().asType().toString() + ".class , " + getterCommand + ")";
            }

        }


        return getterCommand;

    }

}
