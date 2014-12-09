package org.zenbeam.util;

import java.util.List;

public class StringUtils {

    public static String join(List<String> lines, String delimiter) {

        StringBuffer sb = new StringBuffer();
        if (lines != null) {
            int lineIndex = 1;
            for (String line : lines) {

                sb.append(line);

                if (lineIndex < lines.size()) {
                    sb.append(delimiter);
                }

                lineIndex++;
            }

        }

        return sb.toString();
    }
}
