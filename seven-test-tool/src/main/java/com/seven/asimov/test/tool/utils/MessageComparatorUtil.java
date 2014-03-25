package com.seven.asimov.test.tool.utils;

public class MessageComparatorUtil {

    private String fExpected;
    private String fActual;

    public MessageComparatorUtil() {
    }

    public MessageComparatorUtil(String expected, String actual) {
        fExpected = expected;
        fActual = actual;
    }

    public String compactMessage(String message) {
        if (fExpected == null && fActual == null)
            return compactMessage(message, "", false, false);
        return compactMessage(message, null, true, true);
    }

    public String compactMessage(String message, String sPrefix, boolean firstValue, boolean secondValue) {
        return format(message, sPrefix, fExpected, fActual, firstValue, secondValue, false);
    }

    public static String format(String message, String sPrefix, Object expected, Object actual, boolean firstValue, boolean secondValue, boolean full) {
        String formatted = "";
        String prefix = "";
        String suffix = "";
        if (message != null)
            formatted = message + " ";
        if (sPrefix == null) {
            sPrefix = "> but was:<";
            prefix = "expected:<";
            suffix = ">";
        }
        if (full) {
            return formatted + prefix + (firstValue ? expected : "") + sPrefix + (secondValue ? actual : "") + suffix;
        } else return formatted;
    }
}
