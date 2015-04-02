package org.zenbeam.service;

import org.zenbeam.model.ServiceResource;

import java.util.Collection;
import java.util.List;

public interface CodeRenderService {
    String renderIfCondition(String condition, String body);

    String renderIfCondition(String condition, String body, String elseBody);

    String renderMethod(String returnType, String visibility, String name, String signature, String body);

    String renderClass(String packageName, List<String> imports, String className, String interfaceName, Collection<ServiceResource> attributes, List<String> methods);
}
