package com.seven.asimov.it.utils.logcat.wrappers;

public class SubscriptionStatusWrapper extends LogEntryWrapper {
    @Override
    public String toString() {
        return "SubscriptionStatusWrapper{" +
                "RR='" + RR + '\'' +
                ", subscriptionID='" + subscriptionID + '\'' +
                ", status=" + status +
                '}';
    }

    public enum SubscriptionStatus{
        PENDING,
        POLLING,
        FAILED,
        TIMED_OUT,
        DELETE_PENDING,
        INVALIDATED_W_CACHE,
        INVALIDATED_WO_CACHE,
        DELETED,
        ACTIVE,
        UNKNOWN
    }


    private String subscriptionID;
    private String RR;
    private SubscriptionStatus status;

    public void setSubscriptionID(String subscriptionID) {
        this.subscriptionID = subscriptionID;
    }

    public String getSubscriptionID() {
        return subscriptionID;
    }

    public void setRR(String RR) {
        this.RR = RR;
    }

    public String getRR() {
        return RR;
    }

    public void setStatus(String status) {
        try{
            this.status= SubscriptionStatus.valueOf(status);
        }catch(Exception e){
            this.status = SubscriptionStatus.UNKNOWN;
        }
    }

    public SubscriptionStatus getStatus() {
        return status;
    }
}
