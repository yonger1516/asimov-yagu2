package com.seven.asimov.it.utils.tcpdump;

import java.util.Arrays;
import java.util.BitSet;

public class TcpPacket extends Packet {

    private long acknowledgementNumber;
    private long sequenceNumber;

    private boolean isSyn;
    private boolean isAck;
    private boolean isPsh;
    private boolean isFin;
    private boolean isRst;

    private byte[] dataBytes;

    public long getAcknowledgementNumber() {
        return acknowledgementNumber;
    }

    public void setAcknowledgementNumber(long acknowledgementNumber) {
        this.acknowledgementNumber = acknowledgementNumber;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public void setFlags(BitSet flags) {
        isSyn = flags.get(4);
        isAck = flags.get(1);
        isPsh = flags.get(2);
        isFin = flags.get(5);
        isRst = flags.get(3);
    }

    public byte[] getDataBytes() {
        return dataBytes;
    }

    public void setDataBytes(byte[] dataBytes) {
        this.dataBytes = dataBytes;
    }

    public boolean isSyn() {
        return isSyn;
    }

    public void setSyn(boolean syn) {
        isSyn = syn;
    }

    public boolean isAck() {
        return isAck;
    }

    public void setAck(boolean ack) {
        isAck = ack;
    }

    public boolean isPsh() {
        return isPsh;
    }

    public void setPsh(boolean psh) {
        isPsh = psh;
    }

    public boolean isFin() {
        return isFin;
    }

    public void setFin(boolean fin) {
        isFin = fin;
    }

    public boolean isRst() {
        return isRst;
    }

    public void setRst(boolean rst) {
        isRst = rst;
    }

    @Override
    public String toString() {
        return "TcpPacket [" + " timestamp=" + getTimestamp() + " totalLength=" + getTotalLength() +
                " payloadLength=" + getPayloadLength() + " interface=" + getInterface() +
                " direction=" + getDirection() +
                " srcAddr=" + getSourceAddress() + " destAddr=" + getDestinationAddress() +
                " srcPort=" + getSourcePort() + " destPort=" + getDestinationPort() +
                " seqNumber=" + sequenceNumber + " ackNumber=" + acknowledgementNumber +
                " flags{ " +
                (isSyn ? "SYN " : "") + (isAck ? "ACK " : "") + (isPsh ? "PSH " : "") +
                (isFin ? "FIN " : "") + (isRst ? "RST " : "") +
                "}" + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        TcpPacket packet = (TcpPacket) o;

        if (acknowledgementNumber != packet.acknowledgementNumber) return false;
        if (isAck != packet.isAck) return false;
        if (isFin != packet.isFin) return false;
        if (isPsh != packet.isPsh) return false;
        if (isRst != packet.isRst) return false;
        if (isSyn != packet.isSyn) return false;
        if (sequenceNumber != packet.sequenceNumber) return false;
        if (!Arrays.equals(dataBytes, packet.dataBytes)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (acknowledgementNumber ^ (acknowledgementNumber >>> 32));
        result = 31 * result + (int) (sequenceNumber ^ (sequenceNumber >>> 32));
        result = 31 * result + (isSyn ? 1 : 0);
        result = 31 * result + (isAck ? 1 : 0);
        result = 31 * result + (isPsh ? 1 : 0);
        result = 31 * result + (isFin ? 1 : 0);
        result = 31 * result + (isRst ? 1 : 0);
        result = 31 * result + (dataBytes != null ? Arrays.hashCode(dataBytes) : 0);
        return result;
    }
}
