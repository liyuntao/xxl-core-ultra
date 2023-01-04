package com.xxl.job.core.util;

public class XxlAssert {
    private XxlAssert() {
    }

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notEmpty(CharSequence string, String message) {
        if (XxlAssert.isEmpty(string)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static boolean isEmpty(String cs) {
        return cs == null || cs.isEmpty();
    }

    public static boolean isEmpty(CharSequence cs) {
        if (cs instanceof String) {
            return isEmpty((String)cs);
        }
        return cs == null || cs.length() == 0;
    }

}
