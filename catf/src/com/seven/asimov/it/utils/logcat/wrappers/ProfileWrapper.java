package com.seven.asimov.it.utils.logcat.wrappers;

import com.seven.asimov.it.utils.logcat.wrappers.LogEntryWrapper;

import java.lang.Integer;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;

public class ProfileWrapper extends LogEntryWrapper{

    private static final String TAG = ProfileWrapper.class.getSimpleName();
    private String logLevel;
    private String os;
    private String model;
    private Long imei;
    private Long msisdn;

    public ProfileWrapper() {
    }

    public Long getImei() {
        return imei;
    }

    public void setImei(Long imei) {
        this.imei = imei;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Long getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(Long msisdn) {
        this.msisdn = msisdn;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }

    @Override
    public String toString() {
        return  new StringBuilder().append(super.toString()).append(" ProfileWrapper{")
                .append("logLevel=").append(logLevel)
                .append(", os=").append(os)
                .append(", model").append(model)
                .append(", imei=").append(imei)
                .append(", msisdn=").append(msisdn)
                .toString();
    }
}