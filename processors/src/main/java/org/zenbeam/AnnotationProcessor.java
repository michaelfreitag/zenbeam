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
                            methodContext.put("methodName", ee.getSimpleName().toString());

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


                            methodContext.put("methodSignature", StringUtils.join(methodAttributes, ", "));
                            methodContext.put("methodBody", buildProjections(projections, source, target));

                            final String method = Mustache.compiler().compile(getTemplate("templates/method.mustache")).execute(methodContext);
                            System.err.println(method);

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

    private Map<String, String> getGetterSetterInfo(VariableElement element) {

        Map<String, String> result = new HashMap<String, String>();
        List<Element> elements = new ArrayList<Element>();
        elements.add(processingEnv.getTypeUtils().asElement(element.asType()));

        for (TypeMirror tm : processingEnv.getTypeUtils().directSupertypes(element.asType())) {
           elements.add(processingEnv.getTypeUtils().asElement(tm));

        }

        for (Element e : elements) {
            for (Element method : ElementFilter.methodsIn(e.getEnclosedElements())) {
                String keyName = method.getSimpleName().toString().toLowerCase();
                String originalName = method.getSimpleName().toString();
                result.put(keyName, originalName);
            }
        }

        return result;
    }


    private String buildProjections(Projections projections, VariableElement source, VariableElement target) {

        //collection meta information getter/setter
        Map<String, String> targetSetter = getGetterSetterInfo(target);
        Map<String, String> sourceGetter = getGetterSetterInfo(source);

        StringBuffer sb = new StringBuffer();

        for (Projection p : projections.value()) {

            Map<String, String> templateModelSourceGet = new HashMap<String, String>();
            templateModelSourceGet.put("getterName", sourceGetter.get("get" + p.source().toLowerCase()));
            final String sourceGetProperty = Mustache.compiler().compile(getTemplate("templates/source_get.mustache")).execute(templateModelSourceGet);

            Map<String, String> templateModelTargetSet = new HashMap<String, String>();
            templateModelTargetSet.put("setterName", targetSetter.get("set" + p.target().toLowerCase()));
            templateModelTargetSet.put("getProperty", sourceGetProperty);

            final String method = Mustache.compiler().compile(getTemplate("templates/target_set.mustache")).execute(templateModelTargetSet);
            sb.append(method).append(";").append("\r\n");

        }



        /*
        Element parameterElement = processingEnv.getTypeUtils().asElement(source.asType());

        for (Element elem : ElementFilter.methodsIn(parameterElement.getEnclosedElements())) {
            ElementKind elementKind = element.getKind();
            System.err.println(elem);
            if (elementKind.equals(ElementKind.FIELD)) {

            }
        }
        */

        return sb.toString();
    }


}
