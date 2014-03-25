package com.seven.asimov.it.utils.tcpdump;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Message;

import java.io.IOException;
import java.util.BitSet;

import static com.seven.asimov.it.base.constants.TFConstantsIF.LOOPBACK_ADDRESS_TYPE;
import static com.seven.asimov.it.base.constants.TFConstantsIF.PACKET_TYPE_TO_US;

class PacketBuilder {

    private static final Logger logger = LoggerFactory.getLogger(PacketBuilder.class.getSimpleName());
    private static final boolean DEBUG = false;

    public static TcpPacket buildTcpPacket(long timestamp, int totalLength, int payloadLength,
                                           int packetType, int addressType,
                                           String sourceAddress, String destinationAddress,
                                           int sourcePort, int destinationPort,
                                           int sequenceNumber, int acknowledgementNumber,
                                           BitSet flags,
                                           byte[] dataBytes) {
        TcpPacket packet = new TcpPacket();
        packet.setTimestamp(timestamp);
        packet.setTotalLength(totalLength);
        packet.setPayloadLength(payloadLength);
        packet.setInterface((addressType == LOOPBACK_ADDRESS_TYPE) ? Interface.LOOPBACK : Interface.NETWORK);
        packet.setSourceAddress(sourceAddress);
        packet.setDestinationAddress(destinationAddress);
        packet.setSourcePort(sourcePort);
        packet.setDestinationPort(destinationPort);
        packet.setSequenceNumber(sequenceNumber);
        packet.setAcknowledgementNumber(acknowledgementNumber);
        packet.setFlags(flags);
        packet.setDataBytes(dataBytes);
        detectPacketDirection(packet, packetType);
        if (DEBUG) {
            logger.debug(packet.toString());
        }
        return packet;
    }

    public static TcpPacket buildTcpPacket(long timestamp, int totalLength, int payloadLength,
                                           Interface anInterface, Direction direction,
                                           String sourceAddress, String destinationAddress,
                                           int sourcePort, int destinationPort,
                                           long sequenceNumber, long acknowledgementNumber,
                                           int syn, int ack, int psh, int fin, int rst,
                                           byte[] dataBytes) {
        TcpPacket packet = new TcpPacket();
        packet.setTimestamp(timestamp);
        packet.setTotalLength(totalLength);
        packet.setPayloadLength(payloadLength);
        packet.setInterface(anInterface);
        packet.setDirection(direction);
        packet.setSourceAddress(sourceAddress);
        packet.setDestinationAddress(destinationAddress);
        packet.setSourcePort(sourcePort);
        packet.setDestinationPort(destinationPort);
        packet.setSequenceNumber(sequenceNumber);
        packet.setAcknowledgementNumber(acknowledgementNumber);
        packet.setSyn(syn == 1);
        packet.setAck(ack == 1);
        packet.setPsh(psh == 1);
        packet.setFin(fin == 1);
        packet.setRst(rst == 1);
        packet.setDataBytes(dataBytes);
        if (DEBUG) {
            logger.debug(packet.toString());
        }
        return packet;
    }

    public static DnsPacket buildDnsPacket(long timestamp, int totalLength, int payloadLength,
                                           int packetType, int addressType,
                                           String sourceAddress, String destinationAddress,
                                           int sourcePort, int destinationPort, int transactionId,
                                           byte[] dataBytes) throws IOException {
        DnsPacket packet = new DnsPacket();
        try {
            packet.setTimestamp(timestamp);
            packet.setTotalLength(totalLength);
            packet.setPayloadLength(payloadLength);
            packet.setInterface((addressType == LOOPBACK_ADDRESS_TYPE) ? Interface.LOOPBACK : Interface.NETWORK);
            packet.setSourceAddress(sourceAddress);
            packet.setDestinationAddress(destinationAddress);
            packet.setSourcePort(sourcePort);
            packet.setDestinationPort(destinationPort);
            packet.setDataBytes(dataBytes);
            detectPacketDirection(packet, packetType);
            Message message = new Message(dataBytes);
            String host = message.getQuestion().getName().toString()
                    .substring(0, message.getQuestion().getName().toString().length() - 1);
            packet.setHost(host);
            packet.setTransactionId(transactionId);
            if (DEBUG) {
                logger.debug(packet.toString());
            }
        } catch (Exception iae) {
            ExceptionUtils.getStackTrace(iae);
            logger.error("Error when processing bytes: ");
            for (int i = 0; i < dataBytes.length; i++) {
                logger.error(i + " : " + dataBytes[i]);
            }
            return null;
        }
        return packet;
    }

    public static DnsPacket buildDnsPacket(long timestamp, int totalLength, int payloadLength,
                                           Interface anInterface, Direction direction,
                                           String sourceAddress, String destinationAddress,
                                           int sourcePort, int destinationPort, int transactionId,
                                           String host, byte[] dataBytes) {
        DnsPacket packet = new DnsPacket();
        packet.setTimestamp(timestamp);
        packet.setTotalLength(totalLength);
        packet.setPayloadLength(payloadLength);
        packet.setInterface(anInterface);
        packet.setDirection(direction);
        packet.setSourceAddress(sourceAddress);
        packet.setDestinationAddress(destinationAddress);
        packet.setSourcePort(sourcePort);
        packet.setDestinationPort(destinationPort);
        packet.setHost(host);
        packet.setTransactionId(transactionId);
        packet.setDataBytes(dataBytes);
        if (DEBUG) {
            logger.debug(packet.toString());
        }
        return packet;
    }

    private static void detectPacketDirection(Packet packet, int packetType) {
        if (packet.getInterface() == Interface.NETWORK) {
            if (packetType == PACKET_TYPE_TO_US) {
                packet.setDirection(Direction.TO_US);
            } else {
                packet.setDirection(Direction.FROM_US);
            }
        } else {
            if (TcpDumpHelper.isKnownServerPort(packet.getDestinationPort())) {
                packet.setDirection(Direction.FROM_US);
            } else {
                packet.setDirection(Direction.TO_US);
            }
        }
    }
}
