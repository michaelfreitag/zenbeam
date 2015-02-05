package org.zenbeam;

public class CodeTemplates {

    public static String LINE_FEED = "\n";

    public static String DOUBLE_LINE_FEED = LINE_FEED + LINE_FEED;

    public static String getClassTemplate() {

        StringBuffer result = new StringBuffer();

        result.append("package {{packageName}};").append(DOUBLE_LINE_FEED);
        result.append("import javax.ejb.Stateless;").append(DOUBLE_LINE_FEED);
        result.append("import org.zenbeam.converter.PropertyConverter;").append(DOUBLE_LINE_FEED);
        result.append("@Stateless").append(LINE_FEED);
        result.append("public class {{generatedClassName}} implements {{interfaceName}} {").append(DOUBLE_LINE_FEED);
        result.append("    private PropertyConverter propertyConverter = new PropertyConverter();").append(DOUBLE_LINE_FEED);
        result.append("    {{#methods}}").append(DOUBLE_LINE_FEED);
        result.append("    {{.}}").append(DOUBLE_LINE_FEED);
        result.append("    {{/methods}}").append(DOUBLE_LINE_FEED);
        result.append("}");

        return result.toString();
    }

    public static String getMethodTemplate() {

        StringBuffer result = new StringBuffer();
        result.append("     public {{returnType}} {{name}}({{signature}}) {").append(DOUBLE_LINE_FEED);
        result.append("         {{body}}").append(DOUBLE_LINE_FEED);
        result.append("     }");

        return result.toString();

    }

    public static String getIfTemplate() {

        StringBuffer result = new StringBuffer();
        result.append("{{#condition}}").append(DOUBLE_LINE_FEED);
        result.append("if ({{condition}}) {").append(LINE_FEED);
        result.append("     {{body}}").append(LINE_FEED);
        result.append("}").append(DOUBLE_LINE_FEED);
        result.append("{{/condition}}").append(DOUBLE_LINE_FEED);
        result.append("{{^condition}}").append(DOUBLE_LINE_FEED);
        result.append("     {{body}}").append(DOUBLE_LINE_FEED);
        result.append("{{/condition}}");

        return result.toString();

    }

}
