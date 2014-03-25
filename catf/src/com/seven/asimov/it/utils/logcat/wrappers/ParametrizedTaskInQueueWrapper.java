package com.seven.asimov.it.utils.logcat.wrappers;

public class ParametrizedTaskInQueueWrapper extends LogEntryWrapper {
    private String status;
    private String type;
    private String token;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return String.format("ParametrizedTaskInQueueWrapper: [status= %s, type= %s, token= %s]", status, type, token);
    }
}
