package com.seven.asimov.it.utils.tcpdump;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.seven.asimov.it.base.constants.TFConstantsIF.GET_METHOD;
import static com.seven.asimov.it.base.constants.TFConstantsIF.POST_METHOD;

class HttpSessionHandler {

    private static final Logger logger = LoggerFactory.getLogger(HttpSessionHandler.class.getSimpleName());
    private static final long MAX_PAYLOAD_LENGTH = 1000000l;

    public static void processSessions(List<HttpSession> sessions) {
        for (HttpSession session : sessions) {
            logger.warn(session.toString());
            processHttpSession(session);
        }
    }

    private static void processHttpSession(HttpSession session) {
        List<TcpPacket> packets = session.getPackets();
        long upstreamTotalLength = 0;
        long upstreamRelativeSequenceNumberOffset = 0;
        long upstreamMaxRelativeSequenceNumber = 0;
        long upstreamLastPacketPayloadLength = 0;
        long downstreamTotalLength = 0;
        long downstreamRelativeSequenceNumberOffset = 0;
        long downstreamMaxRelativeSequenceNumber = 0;
        long downstreamLastPacketPayloadLength = 0;
        long requestStartTimestamp = 0;
        long requestEndTimestamp = 0;
        long responseStartTimestamp = 0;
        long responseEndTimestamp = 0;
        boolean requestStartFound = false;
        boolean responseStartFound = false;
        long upstreamAckPayloadLength = 0;
        long downStreamAckPayloadLength = 0;

        for (TcpPacket packet : packets) {
            if (packet.getDirection() == Direction.FROM_US) {
                upstreamTotalLength += packet.getTotalLength();
                if (packet.getPayloadLength() > 0) {
                    String data = new String(packet.getDataBytes(), Charset.forName("ISO-8859-1"));
                    if (data.toUpperCase().contains(GET_METHOD)) {
                        session.setUri(composeUri(data, GET_METHOD));
                    } else if (data.toUpperCase().contains(POST_METHOD)) {
                        session.setUri(composeUri(data, POST_METHOD));
                    }
                    upstreamLastPacketPayloadLength = packet.getPayloadLength();
                    if (!requestStartFound) {
                        requestStartTimestamp = packet.getTimestamp();
                        requestStartFound = true;
                    }
                    requestEndTimestamp = packet.getTimestamp();
                }
                if (packet.isSyn()) {
                    upstreamRelativeSequenceNumberOffset = packet.getSequenceNumber() + 1;
                } else {
                    if (upstreamMaxRelativeSequenceNumber < (packet.getSequenceNumber() - upstreamRelativeSequenceNumberOffset)
                            && packet.getPayloadLength() > 0) {
                        upstreamMaxRelativeSequenceNumber = packet.getSequenceNumber() - upstreamRelativeSequenceNumberOffset;
                    }
                }
            } else {
                downstreamTotalLength += packet.getTotalLength();
                if (packet.getPayloadLength() > 0) {
                    downstreamLastPacketPayloadLength = packet.getPayloadLength();
                    if (!responseStartFound) {
                        responseStartTimestamp = packet.getTimestamp();
                        responseStartFound = true;
                    }
                    responseEndTimestamp = packet.getTimestamp();
                }

                if (session.getInterface() == Interface.NETWORK) {
                    downstreamLastPacketPayloadLength = 1;
                }

                if (packet.isSyn()) {
                    downstreamRelativeSequenceNumberOffset = packet.getSequenceNumber() + 1;
                } else {
                    if (downstreamMaxRelativeSequenceNumber < (packet.getSequenceNumber() - downstreamRelativeSequenceNumberOffset)
                            && packet.getPayloadLength() > 0) {
                        downstreamMaxRelativeSequenceNumber = packet.getSequenceNumber() - downstreamRelativeSequenceNumberOffset;
                    }
                }
            }
            logger.debug("downstreamRelativeSequenceNumberOffset=" + downstreamRelativeSequenceNumberOffset);
            if (downstreamRelativeSequenceNumberOffset != 0  && packet.getDirection() == Direction.FROM_US){
                downStreamAckPayloadLength = packet.getAcknowledgementNumber() - downstreamRelativeSequenceNumberOffset - 1;
                logger.debug("packet.getAcknowledgementNumber()=" + packet.getAcknowledgementNumber() + " downstreamRelativeSequenceNumberOffset=" + downstreamRelativeSequenceNumberOffset);
            }
            if (upstreamRelativeSequenceNumberOffset != 0 && packet.getDirection() == Direction.TO_US){
                upstreamAckPayloadLength = packet.getAcknowledgementNumber() - upstreamRelativeSequenceNumberOffset - 1;
            }
        }

        logger.debug("UpstreamPayloadLength calculated using acknowledgment number = " + upstreamAckPayloadLength);
        logger.debug("DownstreamPayloadLength calculated using acknowledgment number = " + downStreamAckPayloadLength);
        // TODO must be investigated and changed because upstreamAckPayloadLength and downStreamAckPayloadLength can be incorrect
        if (upstreamAckPayloadLength > MAX_PAYLOAD_LENGTH){
            logger.debug("upstreamAckPayloadLength =" + upstreamAckPayloadLength);
            upstreamAckPayloadLength = 0;
        }
        if (Math.abs(downStreamAckPayloadLength) > MAX_PAYLOAD_LENGTH){
            logger.debug("downStreamAckPayloadLength =" + downStreamAckPayloadLength);
            downStreamAckPayloadLength = 0;
        }
        logger.debug("UpstreamPayloadLength calculated using sequence number = "+(upstreamMaxRelativeSequenceNumber + upstreamLastPacketPayloadLength));
        logger.debug("DownstreamPayloadLength calculated using sequence number = "+(downstreamMaxRelativeSequenceNumber + downstreamLastPacketPayloadLength));

        logger.debug("downstreamMaxRelativeSequenceNumber="+(downstreamMaxRelativeSequenceNumber + " downstreamLastPacketPayloadLength=" + downstreamLastPacketPayloadLength));

        long upstreamPayloadLength = (upstreamAckPayloadLength >  (upstreamMaxRelativeSequenceNumber + upstreamLastPacketPayloadLength)) ?
                upstreamAckPayloadLength : (upstreamMaxRelativeSequenceNumber + upstreamLastPacketPayloadLength);
        long downstreamPayloadLength = ( downStreamAckPayloadLength > (downstreamMaxRelativeSequenceNumber + downstreamLastPacketPayloadLength)) ?
                downStreamAckPayloadLength : (downstreamMaxRelativeSequenceNumber + downstreamLastPacketPayloadLength);
        session.setUpstreamTotalLength(upstreamTotalLength);
        session.setUpstreamPayloadLength(upstreamPayloadLength);
        session.setDownstreamTotalLength(downstreamTotalLength);
        session.setDownstreamPayloadLength(downstreamPayloadLength);
        session.setRequestStartTimestamp(requestStartTimestamp);
        session.setRequestEndTimestamp(requestEndTimestamp);
        session.setResponseStartTimestamp(responseStartTimestamp);
        session.setResponseEndTimestamp(responseEndTimestamp);
    }

    private static String composeUri(String rawData, String method) {
        String uri = null;
        String host = null;
        Pattern uriPattern = Pattern.compile(method + " ([a-zA-Z0-9&#\\=\\?\\.\\/\\-\\_]*) HTTP", Pattern.CASE_INSENSITIVE);
        Pattern hostPattern = Pattern.compile("Host\\: ([a-zA-Z0-9\\.\\-\\_]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = uriPattern.matcher(rawData);
        if (matcher.find()) {
            uri = matcher.group(1);
            matcher = hostPattern.matcher(rawData);
            if (matcher.find()) host = matcher.group(1);
        }
        return "http://" + host + uri;
    }
}
