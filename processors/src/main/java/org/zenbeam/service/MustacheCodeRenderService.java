package org.zenbeam.service;

import com.samskivert.mustache.Mustache;
import org.zenbeam.CodeTemplates;
import org.zenbeam.model.ServiceResource;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MustacheCodeRenderService implements CodeRenderService {

    private Mustache.Compiler compiler = Mustache.compiler().escapeHTML(false);

    @Override
    public String renderIfCondition(String condition, String body) {
        return renderIfCondition(condition, body, null);
    }

    @Override
    public String renderIfCondition(String condition, String body, String elseBody) {

        Map<String, String> commandModel = new HashMap<String, String>();
        if (condition != null && !condition.isEmpty()) {
            commandModel.put("condition", condition);
        }
        commandModel.put("body", body);
        commandModel.put("elseBody", elseBody);

        return compiler.compile(CodeTemplates.getIfTemplate()).execute(commandModel);

    }

    @Override
    public String renderMethod(String returnType, String visibility, String name, String signature, String body) {

        Map<String, Object> methodContext = new HashMap<String, Object>();
        methodContext.put("name", name);
        methodContext.put("visibility", visibility);
        methodContext.put("returnType", returnType);
        methodContext.put("signature", signature);
        methodContext.put("body", body);

        return compiler.compile(CodeTemplates.getMethodTemplate()).execute(methodContext);


    }

    @Override
    public String renderClass(String packageName, List<String> imports, String className, String interfaceName, Collection<ServiceResource> attributes, List<String> methods) {

        Map<String, Object> context = new HashMap<String, Object>();
        context.put("packageName", packageName);
        context.put("imports", imports);
        context.put("generatedClassName", className);
        context.put("interfaceName", interfaceName);
        context.put("attributes", attributes);
        context.put("methods", methods);

        return compiler.compile(CodeTemplates.getClassTemplate()).execute(context);


    }

}
