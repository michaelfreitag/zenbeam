package org.zenbeam.util;

import com.samskivert.mustache.Mustache;

public class MustacheUtils {

    public static Mustache.Compiler getCompiler() {
        return Mustache.compiler().escapeHTML(false);
    }
}
