package org.zenbeam.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionsUtils {

    public static String getStacktraceAsString(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

}
