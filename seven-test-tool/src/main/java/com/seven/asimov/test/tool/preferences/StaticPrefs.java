package com.seven.asimov.test.tool.preferences;

import org.apache.commons.lang.StringUtils;

/**
 * SharedPrefs - place to store session variables.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */
public abstract class StaticPrefs {

    // private static final String LOG = "StaticPrefs";

    private static String sLatestMd5 = StringUtils.EMPTY;

    public static void setLatestMd5(String latestMd5) {
        sLatestMd5 = latestMd5;
    }

    public static String getLatestMd5() {
        return sLatestMd5;
    }
}
