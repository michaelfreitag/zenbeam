package org.zenbeam.util;

import org.zenbeam.model.FieldCommand;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FieldCommandUtils {

    public static int countDots(String s) {

        int result = 0;

        if (s != null) {
            result = s.length() - s.replace(".", "").length();
        }

        return result;
    }


    public static String getCommandsAsString(List<FieldCommand> commands) {

        StringBuffer result = new StringBuffer();

        for (FieldCommand fc : commands) {
            result.append("/* field key: ").append(fc.getFieldKey()).append(" */\r\n").append(fc.getCommand()).append("\r\n");
        }

        return result.toString();

    }

    public static void sortFieldCommandListByFieldDepth(List<FieldCommand> commands) {

        Collections.sort(commands, new Comparator<FieldCommand>() {

            public int compare(FieldCommand o1, FieldCommand o2) {


                if (countDots(o1.getFieldKey()) == countDots(o2.getFieldKey())) {
                    return o1.getFieldKey().compareToIgnoreCase(o2.getFieldKey());
                } else {
                    return (countDots(o1.getFieldKey()) > countDots(o2.getFieldKey())) ? 1 : -1;
                }

            }
        });

    }


    public static int removeDuplicates(List<FieldCommand> commands) {

        int size = commands.size();
        int duplicates = 0;

        // not using a method in the check also speeds up the execution
        // also i must be less that size-1 so that j doesn't
        // throw IndexOutOfBoundsException
        for (int i = 0; i < size - 1; i++) {
            // start from the next item after strings[i]
            // since the ones before are checked
            for (int j = i + 1; j < size; j++) {
                // no need for if ( i == j ) here
                if (!commands.get(j).getCommand().equalsIgnoreCase(commands.get(i).getCommand())) {
                    continue;
                }
                duplicates++;
                commands.remove(j);
                // decrease j because the array got re-indexed
                j--;
                // decrease the size of the array
                size--;
            } // for j
        } // for i

        return duplicates;

    }

}
