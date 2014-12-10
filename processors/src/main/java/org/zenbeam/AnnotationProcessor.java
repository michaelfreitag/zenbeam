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
import javax.tools.JavaFileObject;
import java.io.IOException;
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
        } catch (IOException e) {
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

        return result;
    }

    private ExecutableElement findMethod(String methodName, VariableElement element) {

        ExecutableElement result = null;

        List<Element> elements = new ArrayList<Element>();
        elements.add(processingEnv.getTypeUtils().asElement(element.asType()));

        for (TypeMirror tm : processingEnv.getTypeUtils().directSupertypes(element.asType())) {
            elements.add(processingEnv.getTypeUtils().asElement(tm));
        }

        for (Element e : elements) {
            for (ExecutableElement method : ElementFilter.methodsIn(e.getEnclosedElements())) {

                if (methodName.equalsIgnoreCase(method.getSimpleName().toString().toLowerCase())) {
                    result = method;
                }

            }
        }

        return result;
    }


    private MethodInfo getMethodInfo(String property, VariableElement element, MethodType methodType) {

        MethodInfo result = new MethodInfo();
        result.setFieldName(element.getSimpleName().toString());

        if (property != null && !property.isEmpty()) {

            if (property.contains(".")) {
                //get first property element
                result.setChild(getMethodInfo(property.substring(property.indexOf(".") + 1, property.length())
                        , findField(property.substring(0, property.indexOf(".")), element), methodType));

                property = property.substring(0, property.indexOf("."));

            }

            ExecutableElement method = findMethod(methodType.name() + property, element);

            if (method != null) {

                result.setName(method.getSimpleName().toString());

                if (methodType == MethodType.SET) {
                    if (method.getParameters() != null && method.getParameters().size() > 0) {
                        //it is a setter, save parameter type as type
                        for (VariableElement va : method.getParameters()) {
                            result.setType(va.asType());
                        }
                    }
                }

                if (methodType == MethodType.GET) {
                    //it is a get, save return type as type
                    result.setType(method.getReturnType());
                }
            }

        }

        return result;
    }

    private String buildNullSaveCondition(MethodInfo methodInfo) {


        StringBuffer nullSaveCondition = new StringBuffer();

        if (methodInfo.getChild() != null) {

            if (!nullSaveCondition.toString().isEmpty()) {
                nullSaveCondition.append(" &&");
            }
            nullSaveCondition.append(buildNullSaveCondition(methodInfo.getChild()));
            nullSaveCondition.append(methodInfo.getFieldName()).append(".").append(methodInfo.getName()).append("() != null");

        }

        return nullSaveCondition.toString();
    }

    private String buildGetter(MethodInfo methodInfo) {

        StringBuffer getter = new StringBuffer();

        getter.append(methodInfo.getName()).append("()");


        if (methodInfo.getChild() != null) {

            if (!getter.toString().isEmpty()) {
                getter.append(".");
            }
            getter.append(buildGetter(methodInfo.getChild()));
        }



        return getter.toString();
    }


    private String buildProjections(Projections projections, VariableElement source, VariableElement target) {

        //collection meta information getter/setter

        StringBuffer sb = new StringBuffer();

        for (Projection p : projections.value()) {

            MethodInfo getter = getMethodInfo(p.source().toLowerCase(), source, MethodType.GET);
            String nullSaveCondition =  buildNullSaveCondition(getter);
            String getterCommand = source.getSimpleName() + "." + buildGetter(getter);

            MethodInfo setter = getMethodInfo(p.target().toLowerCase(), target, MethodType.SET);

            //check if getter can be applied
            if (!processingEnv.getTypeUtils().isAssignable(getter.getType(), setter.getType())) {

                if (setter.getType().toString().equalsIgnoreCase(String.class.getCanonicalName())) {
                    getterCommand += ".toString()";
                }

            }

            Map<String, String> templateModelTargetSet = new HashMap<String, String>();
            templateModelTargetSet.put("setterName", setter.getName());
            templateModelTargetSet.put("getProperty", getterCommand);

            final String setCommand = Mustache.compiler().compile(getTemplate("templates/set.mustache")).execute(templateModelTargetSet);

            //build getter/setter block surrounded by null safe condition
            Map<String, String> commandModel = new HashMap<String, String>();
            if (nullSaveCondition != null && !nullSaveCondition.isEmpty()) {
                commandModel.put("condition", nullSaveCondition);
            }
            commandModel.put("body", setCommand);

            final String command = Mustache.compiler().compile(getTemplate("templates/if.mustache")).execute(commandModel);



            sb.append(command).append("\r\n");

        }

        return sb.toString();
    }


}
