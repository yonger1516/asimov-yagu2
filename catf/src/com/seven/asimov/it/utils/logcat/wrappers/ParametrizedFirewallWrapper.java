package com.seven.asimov.it.utils.logcat.wrappers;

public class ParametrizedFirewallWrapper extends LogEntryWrapper {
    private String version;
    private String type;
    private String chainId;
    private String name;
    private String action;
    private String event;
    private String interfaceType;
    private String ipVersion;
    private String sequenceNumber;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getChainId() {
        return chainId;
    }

    public void setChainId(String chainId) {
        this.chainId = chainId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getInterfaceType() {
        return interfaceType;
    }

    public void setInterfaceType(String interfaceType) {
        this.interfaceType = interfaceType;
    }

    public String getIpVersion() {
        return ipVersion;
    }

    public void setIpVersion(String ipVersion) {
        this.ipVersion = ipVersion;
    }

    public String getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(String sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public String toString() {
        return String.format("ParametrizedFirewallWrapper: [version= %s, type= %s, chainId= %s, name= %s, action= %s, event= %s, interfaceType= %s, ipVersion= %s, sequenceNumber= %s]", version, type, chainId, name, action, event, interfaceType, ipVersion, sequenceNumber);
    }
}
