package com.seven.asimov.it.utils.logcat.wrappers;

public class GetCachedResponseWrapper extends LogEntryWrapper{
    private int subscriptionId;

    public void setSubscriptionId(int subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public int getSubscriptionId() {
        return subscriptionId;
    }
}
