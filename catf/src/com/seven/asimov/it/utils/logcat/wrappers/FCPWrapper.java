package com.seven.asimov.it.utils.logcat.wrappers;

import java.sql.Timestamp;

public class FCPWrapper extends LogEntryWrapper {

    private int id;
    private int log_id;
    private String originator;

    public String getOriginator() {
        return originator;
    }

    public void setOriginator(String originator) {
        this.originator = originator;
    }

    private String trx;

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
}
