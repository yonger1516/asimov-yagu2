package com.seven.asimov.it.utils.logcat.wrappers;

public class ReportTransferWrapperNN extends LogEntryWrapper {
    private String status;
    private String token;
    private String type;
    private String fromStatus;
    private String toStatus;
    private static final String TAG = ReportTransferWrapperNN.class.getSimpleName();
    /*
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    */

    public String getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(String status) {
        this.fromStatus = status;
    }

    public String getToStatus(){
        return toStatus;
    }

    public void setToStatus(String toStatus){
        this.toStatus = toStatus;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(super.toString()).
                append("; type: ").append(type).
                append("; token=").append(token).
                append("; from status: ").append(fromStatus).
                append("; to status: ").append(toStatus).
                toString();
    }
    /*
    enum ReportTransferStatus {
        NEW,
        WAIT,
        WAIT_FOR_ACK,
        SENT,
        DONE
    }
    */
}
