package com.seven.asimov.it.utils.logcat.wrappers;

public class ParametrizedPowerWrapper extends LogEntryWrapper {
    private String formatVersionId;
    private String event;
    private String level;
    private String delta;
    private String interval;
    private String optimization;
    private String sequenceNumber;

    public String getFormatVersionId() {
        return formatVersionId;
    }

    public void setFormatVersionId(String formatVersionId) {
        this.formatVersionId = formatVersionId;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getDelta() {
        return delta;
    }

    public void setDelta(String delta) {
        this.delta = delta;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getOptimization() {
        return optimization;
    }

    public void setOptimization(String optimization) {
        this.optimization = optimization;
    }

    public String getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(String sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public String toString() {
        return String.format("ParametrizedPowerWrapper: [formatVersionId= %s, event= %s, level= %s, delta= %s, interval= %s, optimization= %s, sequenceNumber= %s]",
                formatVersionId, event, level, delta, interval, optimization, sequenceNumber);
    }
}
