package com.seven.asimov.it.utils.tcpdump;

import java.util.ArrayList;
import java.util.List;

public class DnsSession {

    private List<DnsPacket> packets = new ArrayList<DnsPacket>();

    private long requestTimestamp;
    private long responseTimestamp;

    private int transactionId;

    private Interface anInterface;

    private String host;

    private long upstreamTotalLength;
    private long downstreamTotalLength;

    private long upstreamPayloadLengthLocal;
    private long downstreamPayloadLengthLocal;
    private long upstreamPayloadLengthNetwork;
    private long downstreamPayloadLengthNetwork;

    boolean net=false, loop=false;

    public void addPacket(DnsPacket packet) {
        packets.add(packet);
    }

    public List<DnsPacket> getPackets() {
        return packets;
    }

    public void setPackets(List<DnsPacket> packets) {
        this.packets = packets;
    }

    public long getRequestTimestamp() {
        return requestTimestamp;
    }

    public void setRequestTimestamp(long requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    public long getResponseTimestamp() {
        return responseTimestamp;
    }

    public void setResponseTimestamp(long responseTimestamp) {
        this.responseTimestamp = responseTimestamp;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public Interface getInterface() {
        return anInterface;
    }

    public void setInterface(Interface anInterface) {
        this.anInterface = anInterface;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public long getUpstreamTotalLength() {
        return upstreamTotalLength;
    }

    public void setUpstreamTotalLength(long upstreamTotalLength) {
        this.upstreamTotalLength = upstreamTotalLength;
    }

    public long getDownstreamTotalLength() {
        return downstreamTotalLength;
    }

    public void setDownstreamTotalLength(long downstreamTotalLength) {
        this.downstreamTotalLength = downstreamTotalLength;
    }

    @Override
    public String toString(){
        StringBuilder result = new StringBuilder("DnsSession[ Host = ");
        result.append(host);
        result.append(" ; TransactionId = ");
        result.append(transactionId);
        result.append(" ; Timestamp(Request) = ");
        result.append(requestTimestamp);
        result.append(" ; Timestamp(Response) = ");
        result.append(responseTimestamp);
        result.append(" ; UpStream(Total) = ");
        result.append(upstreamTotalLength);
        result.append(" ; DownStream(Total) = ");
        result.append(downstreamTotalLength);
        result.append(" ; UpStreamLocal(Payload) = ");
        result.append(upstreamPayloadLengthLocal);
        result.append(" ; DownStreamLocal(Payload) = ");
        result.append(downstreamPayloadLengthLocal);
        result.append(" ; UpStreamNetwork(Payload) = ");
        result.append(upstreamPayloadLengthNetwork);
        result.append(" ; DownStreamNetwork(Payload) = ");
        result.append(downstreamPayloadLengthNetwork);
        result.append(" ; was Network :"+isNetwork());
        result.append(" ; was seen on loopbak"+isLoopback());

        return result.toString();
    }

    public long getUpstreamPayloadLengthLocal() {
        return upstreamPayloadLengthLocal;
    }

    public void setUpstreamPayloadLengthLocal(long upstreamPayloadLengthLocal) {
        this.upstreamPayloadLengthLocal = upstreamPayloadLengthLocal;
    }

    public long getDownstreamPayloadLengthLocal() {
        return downstreamPayloadLengthLocal;
    }

    public void setDownstreamPayloadLengthLocal(long downstreamPayloadLengthLocal) {
        this.downstreamPayloadLengthLocal = downstreamPayloadLengthLocal;
    }

    public long getUpstreamPayloadLengthNetwork() {
        return upstreamPayloadLengthNetwork;
    }

    public void setUpstreamPayloadLengthNetwork(long upstreamPayloadLengthNetwork) {
        this.upstreamPayloadLengthNetwork = upstreamPayloadLengthNetwork;
    }

    public long getDownstreamPayloadLengthNetwork() {
        return downstreamPayloadLengthNetwork;
    }

    public void setDownstreamPayloadLengthNetwork(long downstreamPayloadLengthNetwork) {
        this.downstreamPayloadLengthNetwork = downstreamPayloadLengthNetwork;
    }

    public void setNetwork(boolean value){
        net=value;
    }

    public void setLoopback(boolean value){
        loop=value;
    }

    public boolean isNetwork(){
        return net;
    }

    public boolean isLoopback(){
        return loop;
    }

    public boolean checkMiss(){
        if(isNetwork()&&isLoopback()){
            return true;
        }
        return false;
    }

    public boolean checkHit(){
        if(!isNetwork()&&isLoopback()){
            return true;
        }
        return false;
    }

    public boolean checkFailover(){
        if(!isLoopback()&&isNetwork()){
            return true;
        }
        return false;
    }
}
