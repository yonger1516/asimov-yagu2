package com.seven.asimov.it.utils.logcat.wrappers;

public class ParametrizedSystemWrapper extends LogEntryWrapper {
    private String formatVersionId;
    private String key;
    private String value;
    private String sequenceNumber;

    public String getFormatVersionId() {
        return formatVersionId;
    }

    public void setFormatVersionId(String formatVersionId) {
        this.formatVersionId = formatVersionId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(String sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String toString() {
        return String.format("ParametrizedSystemWrapper: [formatVersionId= %s, key= %s, value= %s, sequenceNumber=%s]", formatVersionId, key, value, sequenceNumber);
    }
}
