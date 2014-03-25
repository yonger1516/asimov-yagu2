package com.seven.asimov.it.utils.logcat.wrappers;

import java.sql.Timestamp;

public class FCLWrapper extends LogEntryWrapper {

    private int id;
    private int log_id;
    private String trx;
    private Timestamp ctime;
    private String ip;
    private int port;
    private String csm;

    public String getCSM() {
        return csm;
    }

    public void setCSM(String csm) {
        this.csm = csm;
    }


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

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString(){
        return ("ID "+id+" log_id "+log_id+" trx "+trx+" IP "+ip+ " port "+port);
    }
}