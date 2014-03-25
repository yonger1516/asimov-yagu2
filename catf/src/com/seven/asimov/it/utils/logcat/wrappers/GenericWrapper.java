package com.seven.asimov.it.utils.logcat.wrappers;

import java.util.HashMap;
import java.util.Map;


public class GenericWrapper extends LogEntryWrapper {
    private Map<String, String> values;

    public GenericWrapper() {
        values = new HashMap<String, String>();
    }

    public void putValue(String group, String value) {
        values.put(group, value);
    }

    public String getString(String group) {
        return values.get(group);
    }

    public Short getShort(String group) {
        try {
            return Short.parseShort(values.get(group));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Integer getInteger(String group) {
        return Integer.getInteger(values.get(group));
    }

    public Long getLong(String group) {
        return Long.getLong(values.get(group));
    }
}
