package com.seven.asimov.it.utils.logcat.wrappers;


public class CpuUsageWrapper extends LogEntryWrapper {
    private int percentUsage;
    private String logLevel;
    private String cpuUsageOf;
    private String messageLevel;

    public int getPercentUsage() {
        return percentUsage;
    }

    public void setPercentUsage(int percentUsage) {
        this.percentUsage = percentUsage;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getCpuUsageOf() {
        return cpuUsageOf;
    }

    public void setCpuUsageOf(String cpuUsageOf) {
        this.cpuUsageOf = cpuUsageOf;
    }

    public String getMessageLevel() {
        return messageLevel;
    }

    public void setMessageLevel(String messageLevel) {
        this.messageLevel = messageLevel;
    }
}
