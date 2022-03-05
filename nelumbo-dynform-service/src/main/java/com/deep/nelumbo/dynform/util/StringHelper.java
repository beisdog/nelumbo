package com.deep.nelumbo.dynform.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;

/**
 * String utility methods.
 *
 * @author X200531
 */
public class StringHelper {

    public static String join(Object[] list, final String sSep) {
        return join(Arrays.asList(list), sSep);
    }

    public static String join(final Collection<? extends Object> list, final String sSep) {
        if (list.isEmpty()) {
            return null;
        }
        final StringBuilder sbStr = new StringBuilder();
        for (final Object item : list) {
            sbStr.append(sSep);
            sbStr.append(item);
        }
        return sbStr.substring(sSep.length());
    }

    public static String stackTraceToString(Throwable t) {
        StringWriter writer = new StringWriter();
        t.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    public static boolean isNotEmpty(String string) {
        if (string == null)
            return false;
        return string.trim().length() > 0;
    }

    public static String toStringOrNull(Object o) {
        if (o == null) {
            return null;
        }
        return String.valueOf(o);
    }
}
