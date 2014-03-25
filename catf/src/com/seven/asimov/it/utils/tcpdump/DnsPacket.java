package com.seven.asimov.it.utils.tcpdump;

public class DnsPacket extends Packet {

    private int transactionId;
    private String host;
    private byte[] dataBytes;

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public byte[] getDataBytes() {
        return dataBytes;
    }

    public void setDataBytes(byte[] dataBytes) {
        this.dataBytes = dataBytes;
    }

    @Override
    public String toString() {
        return "DnsPacket [" + " timestamp=" + getTimestamp() + " totalLength=" + getTotalLength() +
                " payloadLength=" + getPayloadLength() + " interface=" + getInterface() +
                " direction=" + getDirection() +
                " srcAddr=" + getSourceAddress() + " destAddr=" + getDestinationAddress() +
                " srcPort=" + getSourcePort() + " destPort=" + getDestinationPort() +
                " ID=" + transactionId + " host=" + host +
                "]";
    }
}
