package com.seven.asimov.it.utils.logcat.wrappers;

public class StartPollWrapper extends LogEntryWrapper {

    private String subscriptionId;
    private String resourceId;

    public StartPollWrapper(long timestamp, String subscriptionId, String resourceId) {
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
        return "StartPollWrapper{" +
                "subscriptionId='" + subscriptionId + '\'' +
                ", resourceId='" + resourceId + '\'' +
                '}';
    }
}
