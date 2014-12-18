package org.zenbeam;

import com.samskivert.mustache.Mustache;
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
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@SupportedAnnotationTypes("org.zenbeam.ZenBeamer")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class AnnotationProcessor extends AbstractProcessor {

    private enum MethodType {
        GET, SET
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            for (final Element e : roundEnv.getElementsAnnotatedWith(ZenBeamer.class))

                if (e.getKind() == ElementKind.INTERFACE) {

                    final TypeElement element = (TypeElement) e;
                    final PackageElement packageElement = (PackageElement) element.getEnclosingElement();

                    final String generatedClassName = element.getSimpleName().toString() + "Impl";
                    final JavaFileObject jfo = AnnotationProcessor.this.processingEnv.getFiler().createSourceFile(
                            packageElement.getQualifiedName() + "." + generatedClassName
                    );

                    try {

                        Map<String, Object> context = new HashMap<String, Object>();
                        context.put("packageName", packageElement.getQualifiedName().toString());
                        context.put("generatedClassName", generatedClassName);
                        context.put("interfaceName", element.getSimpleName().toString());

                        //import statements
                        List<String> imports = new ArrayList<String>();

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

                            final String method = Mustache.compiler().compile(getTemplate("templates/method.mustache")).execute(methodContext);

                            context.put("methods", method);

                        }

                        context.put("imports", imports);

                        final String fileContents = Mustache.compiler().compile(getTemplate("templates/class_implements.mustache")).execute(context);
                        jfo.openWriter().append(fileContents).close();

                    } catch (Exception exception) {
                        System.err.println(e);
                    }

                }
            return true;
        } catch (Exception e) {
            System.err.println(e);
            return true;
        }
    }


    private String getTemplate(String template) {

        String result = "";
        try {
            URL url = this.getClass().getClassLoader().getResource(template);
            List<String> lines = Files.readAllLines(Paths.get(url.toURI()), Charset.defaultCharset());
            result = StringUtils.join(lines, "\r\n");
        } catch (Exception e) {
            System.err.println(e);
        }


        return result;
    }

    private void printError(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg);
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

    private String getRootFieldName(FieldInfo fieldInfo) {
        if (fieldInfo.getParent() != null) {
            return getRootFieldName(fieldInfo.getParent());
        }

        return fieldInfo.getOwner().getSimpleName().toString();
    }

    private String buildNullSaveCondition(FieldInfo fieldInfo) {

        if (fieldInfo.getChild() != null) {
            return buildNullSaveCondition(fieldInfo.getChild());
        } else {
            StringBuffer nullSaveCondition = new StringBuffer();
            nullSaveCondition.append(getRootFieldName(fieldInfo)).append(".").append(buildFullGetter(fieldInfo)).append(" != null");
            return nullSaveCondition.toString();
        }

    }

    private String buildGetter(FieldInfo fieldInfo) {

        StringBuffer getter = new StringBuffer();

        ExecutableElement method = findMethod(fieldInfo.getOwner(), fieldInfo.getField(), MethodType.GET);
        getter.append(method.getSimpleName()).append("()");

        return getter.toString();
    }



    private String buildFullGetter(FieldInfo fieldInfo) {

        StringBuffer getter = new StringBuffer();

        if (fieldInfo.getParent() != null) {
            getter.append(buildFullGetter(fieldInfo.getParent().cloneWithoutChild())).append(".");
        }

        ExecutableElement method = findMethod(fieldInfo.getOwner(), fieldInfo.getField(), MethodType.GET);
        getter.append(method.getSimpleName()).append("()");


        if (fieldInfo.getChild() != null) {

            if (!getter.toString().isEmpty()) {
                getter.append(".");
            }
            getter.append(buildFullGetter(fieldInfo.getChild()));
        }

        return getter.toString();
    }

    private String buildInstanciation(FieldInfo fieldInfo) {

        StringBuffer result = new StringBuffer();
        result.append("new ").append(fieldInfo.getField().asType().toString()).append("()");
        return result.toString();

    }

    private String buildSetter(FieldInfo fieldInfo, String command) {

        StringBuffer result = new StringBuffer();
        ExecutableElement method = findMethod(fieldInfo.getOwner(), fieldInfo.getField(), MethodType.SET);


        result.append(getRootFieldName(fieldInfo)).append(".");

        if (fieldInfo.getParent() != null) {
            result.append(buildFullGetter(fieldInfo.getParent().cloneWithoutChild())).append(".");
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

        return Mustache.compiler().compile(getTemplate("templates/if.mustache")).execute(commandModel);

    }

    private String buildSetterPreparation(FieldInfo fieldInfo) {

        StringBuffer command = new StringBuffer();

        String setter = "";
        String nullSaveCondition = "";

        if (fieldInfo.getChild() != null) {

            //null save prepare
            FieldInfo fieldInfoWithOutChild = new FieldInfo();
            fieldInfoWithOutChild.setOwner(fieldInfo.getOwner());
            fieldInfoWithOutChild.setField(fieldInfo.getField());
            nullSaveCondition = buildNullSaveCondition(fieldInfoWithOutChild);
            setter = buildSetter(fieldInfoWithOutChild, buildInstanciation(fieldInfoWithOutChild));

            final String ifCommand = renderIfCondition(nullSaveCondition, setter);
            command.append(ifCommand).append("\r\n");

            final String childCommands = buildSetterPreparation(fieldInfo.getChild());
            command.append(childCommands).append("\r\n");

        } else {

            nullSaveCondition = buildNullSaveCondition(fieldInfo);
            setter = buildSetter(fieldInfo, buildInstanciation(fieldInfo));

            command.append(renderIfCondition(nullSaveCondition, setter)).append("\r\n");

        }



        return command.toString();
    }

    private String buildProjections(Projections projections, VariableElement source, VariableElement target) {

        //collection meta information getter/setter

        StringBuffer sb = new StringBuffer();

        for (Projection p : projections.value()) {

            FieldInfo fieldInfoSource = getFieldInfo(p.source().toLowerCase(), source, null);
            String nullSaveCondition =  buildNullSaveCondition(fieldInfoSource);
            String getterCommand = buildFullGetter(fieldInfoSource);

            FieldInfo setter = getFieldInfo(p.target().toLowerCase(), target, null);

            String setterCommand = buildSetterPreparation(setter);


            //check if getter can be applied
            if (!processingEnv.getTypeUtils().isAssignable(fieldInfoSource.getType(), setter.getType())) {

                if (setter.getType().toString().equalsIgnoreCase(String.class.getCanonicalName())) {
                    getterCommand += ".toString()";
                }

            }

            Map<String, String> templateModelTargetSet = new HashMap<String, String>();
            templateModelTargetSet.put("setterCommand", "");
            templateModelTargetSet.put("getterCommand", getterCommand);

            final String setCommand = Mustache.compiler().compile(getTemplate("templates/set.mustache")).execute(templateModelTargetSet);

            final String command = renderIfCondition(nullSaveCondition, setCommand);

            sb.append(command).append("\r\n");

        }

        return sb.toString();
    }


}
