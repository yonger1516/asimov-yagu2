package com.seven.asimov.it.utils.logcat.wrappers;

/**
 * Created with IntelliJ IDEA.
 * User: amykytenko_cv
 * Date: 6/4/13
 * Time: 3:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class PolicySMSnotificationWrapper extends LogEntryWrapper{
    private String triggerCommandId;
    private String addressInfo;
    private String appData;
    private String origPacketId;
    private String payloadLength;

    public PolicySMSnotificationWrapper() {
    }

    public PolicySMSnotificationWrapper(String triggerCommandId, String addressInfo, String appData, String origPacketId, String payloadLength) {
        this.triggerCommandId = triggerCommandId;
        this.addressInfo = addressInfo;
        this.appData = appData;
        this.origPacketId = origPacketId;
        this.payloadLength = payloadLength;
    }

    public String getTriggerCommandId() {
        return triggerCommandId;
    }

    public void setTriggerCommandId(String triggerCommandId) {
        this.triggerCommandId = triggerCommandId;
    }

    public String getAddressInfo() {
        return addressInfo;
    }

    public void setAddressInfo(String addressInfo) {
        this.addressInfo = addressInfo;
    }

    public String getAppData() {
        return appData;
    }

    public void setAppData(String appData) {
        this.appData = appData;
    }

    public String getOrigPacketId() {
        return origPacketId;
    }

    public void setOrigPacketId(String origPacketId) {
        this.origPacketId = origPacketId;
    }

    public String getPayloadLength() {
        return payloadLength;
    }

    public void setPayloadLength(String payloadLength) {
        this.payloadLength = payloadLength;
    }

    @Override
    public String toString() {
        return "PolicySMSnotificationWrapper{timestamp= " + getTimestamp() +
                " triggerCommandId='" + triggerCommandId + '\'' +
                ", addressInfo='" + addressInfo + '\'' +
                ", appData='" + appData + '\'' +
                ", origPacketId='" + origPacketId + '\'' +
                ", payloadLength='" + payloadLength + '\'' +
                '}';
    }
}
