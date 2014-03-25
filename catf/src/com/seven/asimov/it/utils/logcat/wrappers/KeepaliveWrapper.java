package com.seven.asimov.it.utils.logcat.wrappers;


public class KeepaliveWrapper extends LogEntryWrapper {

    private String AppUid;
    private String TRXkey;
    private String TrxID;
    private String Msg;
    private String Delay;
    private String KAweight;
    private String KAstate;
    private String csmId;
    private int bfc;

    public String getCsmId() {
        return csmId;
    }

    public void setCsmId(String csmId) {
        this.csmId = csmId;
    }

    public String getAppUid() {
        return this.AppUid;
    }

    public void setAppUid(String AppUid) {
        this.AppUid = AppUid;
    }

    public void setTRXkey(String TRXkey) {
        this.TRXkey = TRXkey;
    }

    public void setTrxID(String TrxID) {
        this.TrxID = TrxID;
    }

    public void setMsg(String Msg) {
        this.Msg = Msg;
    }

    public void setDelay(String delay) {
        this.Delay = delay;
    }

    public void setKAweight(String KAweight) {
        this.KAweight = KAweight;
    }

    public void setKAstate(String KAstate) {
        this.KAstate = KAstate;
    }

    public String getKAstate() {
        return KAstate;
    }

    public String getTRXkey() {
        return this.TRXkey;
    }

    public String getTrxID() {
        return this.TrxID;
    }

    public String getMsg() {
        return this.Msg;
    }

    public String getDelay() {
        return this.Delay;
    }

    public String getKAweight() {
        return this.KAweight;
    }

    public int getBfc() {
        return bfc;
    }

    public void setBfc(int bfc) {
        this.bfc = bfc;
    }
}
