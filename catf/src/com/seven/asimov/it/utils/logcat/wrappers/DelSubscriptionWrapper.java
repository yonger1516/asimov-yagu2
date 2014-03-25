package com.seven.asimov.it.utils.logcat.wrappers;

import java.lang.Override;
import java.lang.String;

public class DelSubscriptionWrapper extends LogEntryWrapper {
    private String subscriptionId;
    private String resourceId;

    public DelSubscriptionWrapper(long timestamp, String subscriptionId, String resourceId) {
        setTimestamp(timestamp);
        this.subscriptionId = subscriptionId;
        this.resourceId = resourceId;
    }

       public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public String toString() {
        return "DelSubscriptionWrapper{" +
                "subscriptionId='" + subscriptionId + '\'' +
                ", resourceId='" + resourceId + '\'' +
                '}';
    }
}
