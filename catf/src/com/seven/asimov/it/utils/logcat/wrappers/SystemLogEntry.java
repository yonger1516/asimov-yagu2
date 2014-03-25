package com.seven.asimov.it.utils.logcat.wrappers;

class SystemLogEntry extends CrcsEntry {

    private String systemKey;
    private String systemValues;

    public String getSystemKey() {
        return systemKey;
    }

    public void setSystemKey(String systemKey) {
        this.systemKey = systemKey;
    }

    public String getSystemValues() {
        return systemValues;
    }

    public void setSystemValues(String systemValues) {
        this.systemValues = systemValues;
    }

    @Override
    public String toString() {
        return "[]";
    }
}
