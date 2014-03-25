package com.seven.asimov.it.utils.logcat.wrappers;

import java.sql.Timestamp;

public class EasWrapper {

    private int id;
    private int log_id;
    private String trx;
    private Timestamp ctime;
    private EasType easType;
    private String url;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLog_id() {
        return log_id;
    }

    public void setLog_id(int log_id) {
        this.log_id = log_id;
    }

    public String getTrx() {
        return trx;
    }

    public void setTrx(String trx) {
        this.trx = trx;
    }

    public Timestamp getCtime() {
        return ctime;
    }

    public void setCtime(Timestamp ctime) {
        this.ctime = ctime;
    }

    public EasType getEasType() {
        return easType;
    }

    public void setEasType(EasType easType) {
        this.easType = easType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
