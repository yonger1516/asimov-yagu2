package com.seven.asimov.it.utils.logcat.wrappers;

public class MsisdnWrapper extends LogEntryWrapper {

    private long imsi = -1;
    private boolean validationNeeded = false;
    private String msisdn;
    private boolean msisdnSuccess = false;


    public long getImsi() {
        return imsi;
    }

    public void setImsi(long imsi) {
        this.imsi = imsi;
    }

    public boolean isValidationNeeded() {
        return validationNeeded;
    }

    public void setValidationNeeded(boolean validationNeeded) {
        this.validationNeeded = validationNeeded;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("Msidsn [ Timestamp = ");
        result.append(getTimestamp());
        result.append(" ,imsi = ");
        result.append(getImsi());
        result.append(" ,validationNeeded = ");
        result.append(validationNeeded);
        result.append(" ]");
        return result.toString();
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public boolean isMsisdnSuccess() {
        return msisdnSuccess;
    }

    public void setMsisdnSuccess(boolean msisdnSuccess) {
        this.msisdnSuccess = msisdnSuccess;
    }
}
