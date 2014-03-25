package com.seven.asimov.it.utils.logcat.wrappers;

public class ReportingDumpTransactionWrapper extends LogEntryWrapper {
    private String param1;
    private String param2;
    private String from7tp;
    private String to7tp;
    private String bytes;
    private String hint;

    public String getFrom7tp() {
        return from7tp;
    }

    public void setFrom7tp(String from7tp) {
        this.from7tp = from7tp;
    }

    public String getTo7tp() {
        return to7tp;
    }

    public void setTo7tp(String to7tp) {
        this.to7tp = to7tp;
    }

    public String getBytes() {
        return bytes;
    }

    public void setBytes(String bytes) {
        this.bytes = bytes;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public String getParam1() {
        return param1;
    }

    public void setParam1(String param1) {
        this.param1 = param1;
    }

    public String getParam2() {
        return param2;
    }

    public void setParam2(String param2) {
        this.param2 = param2;
    }

    @Override
    public String toString() {
        return String.format("ReportingDumpTransactionWrapper: [param1 = %s, param2 = %s, from7tp = %s, to7tp = %s, bytes = %s, hint = %s]", param1, param2, from7tp, to7tp, bytes, hint);
    }
}
