package com.seven.asimov.test.tool.activity;

/**
 * Tabs - Activity enum.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */
public enum Tabs {

    MAIN_TAB, ADMIN_TAB, TESTS_TAB, AUTOMATION_TESTS_TAB;

    public static Tabs valueOf(int sCurrentTab) {

        for (Tabs v : Tabs.values()) {
            if (v.ordinal() == sCurrentTab) {
                return v;
            }
        }
        return null;
    }

}
