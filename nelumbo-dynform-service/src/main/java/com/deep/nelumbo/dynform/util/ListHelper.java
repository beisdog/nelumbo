package com.deep.nelumbo.dynform.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Helper functions for lists.
 *
 * @author X200531
 */
public class ListHelper {

    public static List<String> stringToList(String string, String sep) {
        if (string == null) {
            return new ArrayList<String>();
        }
        List<String> list = Arrays.asList(string.split(sep));
        return list;
    }

    /**
     * remove elements that you consider empty from a list. Used for BRM values
     * where "-" is also null
     *
     * @param list
     * @param removeNull                 : do you want to remove nulls
     * @param removeEmpty                : do you want to remove empty strings
     * @param stringsToBeConsideredEmpty : array of strings that you consider empty
     */
    public static void removeEmptyElements(
            List<String> list,
            boolean removeNull, boolean removeEmpty,
            String... stringsToBeConsideredEmpty
    ) {
        for (Iterator<String> it = list.iterator(); it.hasNext(); ) {
            String s = it.next();
            // remove all empty and "-" elements
            if ((removeNull && s == null)
                    || (removeEmpty && s.trim().length() == 0)) {
                it.remove();
            }
            for (String empty : stringsToBeConsideredEmpty) {
                if (empty.contentEquals(empty)) {
                    it.remove();
                }
            }
        }
    }
}
