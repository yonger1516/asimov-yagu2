package com.seven.asimov.it.utils.tcpdump;

import java.util.ArrayList;
import java.util.List;

public class HttpSession {

    private final List<TcpPacket> packets = new ArrayList<TcpPacket>();

    private long requestStartTimestamp;
    private long requestEndTimestamp;
    private long responseStartTimestamp;
    private long responseEndTimestamp;

    private String clientAddress;
    private String serverAddress;
    private String uri;

    private int clientPort;
    private int serverPort;

    private Interface anInterface;

    private long upstreamTotalLength;
    private long downstreamTotalLength;
    private long upstreamPayloadLength;
    private long downstreamPayloadLength;

    public void addPacket(TcpPacket packet) {
        packets.add(packet);
    }

    public List<TcpPacket> getPackets() {
        return packets;
    }

    public long getRequestStartTimestamp() {
        return requestStartTimestamp;
    }

    public void setRequestStartTimestamp(long requestStartTimestamp) {
        this.requestStartTimestamp = requestStartTimestamp;
    }

    public long getRequestEndTimestamp() {
        return requestEndTimestamp;
    }

    public void setRequestEndTimestamp(long requestEndTimestamp) {
        this.requestEndTimestamp = requestEndTimestamp;
    }

    public long getResponseStartTimestamp() {
        return responseStartTimestamp;
    }

    public void setResponseStartTimestamp(long responseStartTimestamp) {
        this.responseStartTimestamp = responseStartTimestamp;
    }

    public long getResponseEndTimestamp() {
        return responseEndTimestamp;
    }

    public void setResponseEndTimestamp(long responseEndTimestamp) {
        this.responseEndTimestamp = responseEndTimestamp;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getClientPort() {
        return clientPort;
    }

    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public Interface getInterface() {
        return anInterface;
    }

    public void setInterface(Interface anInterface) {
        this.anInterface = anInterface;
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

    public long getUpstreamPayloadLength() {
        return upstreamPayloadLength;
    }

    public void setUpstreamPayloadLength(long upstreamPayloadLength) {
        this.upstreamPayloadLength = upstreamPayloadLength;
    }

    public long getDownstreamPayloadLength() {
        return downstreamPayloadLength;
    }

    public void setDownstreamPayloadLength(long downstreamPayloadLength) {
        this.downstreamPayloadLength = downstreamPayloadLength;
    }

    @Override
    public String toString() {
        return "HttpSession{" +
                "packetsCount=" + packets.size() +
                ", requestStartTimestamp=" + requestStartTimestamp +
                ", requestEndTimestamp=" + requestEndTimestamp +
                ", responseStartTimestamp=" + responseStartTimestamp +
                ", responseEndTimestamp=" + responseEndTimestamp +
                ", serverAddress='" + serverAddress + '\'' +
                ", uri='" + uri + '\'' +
                ", clientPort=" + clientPort +
                ", serverPort=" + serverPort +
                ", interface=" + anInterface +
                ", upstreamTotalLength=" + upstreamTotalLength +
                ", downstreamTotalLength=" + downstreamTotalLength +
                ", upstreamPayloadLength=" + upstreamPayloadLength +
                ", downstreamPayloadLength=" + downstreamPayloadLength +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HttpSession session = (HttpSession) o;

        if (clientPort != session.clientPort) return false;
        if (downstreamPayloadLength != session.downstreamPayloadLength) return false;
        if (downstreamTotalLength != session.downstreamTotalLength) return false;
        if (requestEndTimestamp != session.requestEndTimestamp) return false;
        if (requestStartTimestamp != session.requestStartTimestamp) return false;
        if (responseEndTimestamp != session.responseEndTimestamp) return false;
        if (responseStartTimestamp != session.responseStartTimestamp) return false;
        if (serverPort != session.serverPort) return false;
        if (upstreamPayloadLength != session.upstreamPayloadLength) return false;
        if (upstreamTotalLength != session.upstreamTotalLength) return false;
        if (anInterface != session.anInterface) return false;
        if (!serverAddress.equals(session.serverAddress)) return false;
        if (!packets.equals(session.packets)) return false;
        if (uri != null ? !uri.equals(session.uri) : session.uri != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = packets.hashCode();
        result = 31 * result + (int) (requestStartTimestamp ^ (requestStartTimestamp >>> 32));
        result = 31 * result + (int) (requestEndTimestamp ^ (requestEndTimestamp >>> 32));
        result = 31 * result + (int) (responseStartTimestamp ^ (responseStartTimestamp >>> 32));
        result = 31 * result + (int) (responseEndTimestamp ^ (responseEndTimestamp >>> 32));
        result = 31 * result + serverAddress.hashCode();
        result = 31 * result + (uri != null ? uri.hashCode() : 0);
        result = 31 * result + clientPort;
        result = 31 * result + serverPort;
        result = 31 * result + anInterface.hashCode();
        result = 31 * result + (int) (upstreamTotalLength ^ (upstreamTotalLength >>> 32));
        result = 31 * result + (int) (downstreamTotalLength ^ (downstreamTotalLength >>> 32));
        result = 31 * result + (int) (upstreamPayloadLength ^ (upstreamPayloadLength >>> 32));
        result = 31 * result + (int) (downstreamPayloadLength ^ (downstreamPayloadLength >>> 32));
        return result;
    }
}
