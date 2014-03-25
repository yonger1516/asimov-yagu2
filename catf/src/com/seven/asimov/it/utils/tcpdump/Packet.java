package com.seven.asimov.it.utils.tcpdump;

public abstract class Packet {

    private long timestamp;
    private int totalLength;
    private int payloadLength;

    private Interface anInterface;
    private Direction direction;

    private String sourceAddress;
    private String destinationAddress;

    private int sourcePort;
    private int destinationPort;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(int totalLength) {
        this.totalLength = totalLength;
    }

    public int getPayloadLength() {
        return payloadLength;
    }

    public void setPayloadLength(int payloadLength) {
        this.payloadLength = payloadLength;
    }

    public Interface getInterface() {
        return anInterface;
    }

    public void setInterface(Interface anInterface) {
        this.anInterface = anInterface;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public String getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(int sourcePort) {
        this.sourcePort = sourcePort;
    }

    public int getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(int destinationPort) {
        this.destinationPort = destinationPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Packet)) return false;

        Packet packet = (Packet) o;

        if (destinationPort != packet.destinationPort) return false;
        if (payloadLength != packet.payloadLength) return false;
        if (sourcePort != packet.sourcePort) return false;
        if (timestamp != packet.timestamp) return false;
        if (totalLength != packet.totalLength) return false;
        if (anInterface != packet.anInterface) return false;
        if (destinationAddress != null ? !destinationAddress.equals(packet.destinationAddress) : packet.destinationAddress != null)
            return false;
        if (direction != packet.direction) return false;
        if (sourceAddress != null ? !sourceAddress.equals(packet.sourceAddress) : packet.sourceAddress != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + totalLength;
        result = 31 * result + payloadLength;
        result = 31 * result + (anInterface != null ? anInterface.hashCode() : 0);
        result = 31 * result + (direction != null ? direction.hashCode() : 0);
        result = 31 * result + (sourceAddress != null ? sourceAddress.hashCode() : 0);
        result = 31 * result + (destinationAddress != null ? destinationAddress.hashCode() : 0);
        result = 31 * result + sourcePort;
        result = 31 * result + destinationPort;
        return result;
    }
}
