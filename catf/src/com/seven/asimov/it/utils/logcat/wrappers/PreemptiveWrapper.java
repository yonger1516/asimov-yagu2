package com.seven.asimov.it.utils.logcat.wrappers;

public class PreemptiveWrapper extends LogEntryWrapper {
    private String portRange;
    private String detectionTime;
    private String cooldownTime;

    public String getPortRange() {
        return portRange;
    }

    public void setPortRange(String portRange) {
        this.portRange = portRange;
    }

    public String getDetectionTime() {
        return detectionTime;
    }

    public void setDetectionTime(String detectionTime) {
        this.detectionTime = detectionTime;
    }

    public String getCooldownTime() {
        return cooldownTime;
    }

    public void setCooldownTime(String cooldownTime) {
        this.cooldownTime = cooldownTime;
    }
}
