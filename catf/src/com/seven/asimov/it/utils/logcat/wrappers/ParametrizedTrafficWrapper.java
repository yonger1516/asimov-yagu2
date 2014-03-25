package com.seven.asimov.it.utils.logcat.wrappers;

public class ParametrizedTrafficWrapper extends LogEntryWrapper {
    private String formatVersionId;
    private String trafficType;
    private String in;
    private String out;
    private String optimization;
    private String sequenceNumber;

    public String getFormatVersionId() {
        return formatVersionId;
    }

    public void setFormatVersionId(String formatVersionId) {
        this.formatVersionId = formatVersionId;
    }

    public String getTrafficType() {
        return trafficType;
    }

    public void setTrafficType(String trafficType) {
        this.trafficType = trafficType;
    }

    public String getIn() {
        return in;
    }

    public void setIn(String in) {
        this.in = in;
    }

    public String getOut() {
        return out;
    }

    public void setOut(String out) {
        this.out = out;
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
        return String.format("ParametrizedTrafficWrapper: [formatVersionId= %s, trafficType= %s, in= %s, out= %s, optimization= %s, sequenceNumber= %s]", formatVersionId, trafficType, in, out, optimization, sequenceNumber);
    }
}
