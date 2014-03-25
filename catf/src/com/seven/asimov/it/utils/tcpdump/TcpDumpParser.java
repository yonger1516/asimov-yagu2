package com.seven.asimov.it.utils.tcpdump;

import android.content.Context;
import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.utils.IOUtil;
import junit.framework.Assert;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.BitSet;

import static com.seven.asimov.it.base.constants.TFConstantsIF.*;
import static com.seven.asimov.it.utils.tcpdump.TcpDumpHelper.*;

class TcpDumpParser {

    private static final Logger logger = LoggerFactory.getLogger(TcpDumpParser.class.getSimpleName());
    private static final String TRACE_FILE_PATH = "/mnt/sdcard/OCIntegrationTestsResults/tcpdump_log.trace";

    public String traceFile7TT;

    private static final long TIME_TO_COMPLETE_PARSING = 2 * 60 * 1000;
    private static final int TCPDUMP_REFRESH_RATE = 50;

    private final DbAdapter dbAdapter;

    private ParsingThread parsingThread;
    private static boolean parsingIsRunning = false;

    public TcpDumpParser(Context context) {
        dbAdapter = DbAdapter.getInstance(context);
    }

    void start(long startTime) {
        BufferedInputStream inputStream;
        try {

            if(traceFile7TT != null) {
                inputStream = new BufferedInputStream(new FileInputStream(traceFile7TT + "tcpdump_log.trace"));
            } else {
                inputStream = new BufferedInputStream(new FileInputStream(TRACE_FILE_PATH));
            }
            parsingThread = new ParsingThread(startTime, inputStream);
            parsingThread.start();
        } catch (FileNotFoundException e) {
            logger.error("Failed to open trace file!");
            throw new AssertionError("Failed to obtain tcpdump trace file!");
        }
    }

    void stop(long endTime) throws IOException {
        if (parsingThread == null) return;
        parsingThread.stop(endTime);
        ping();
        try {
            parsingThread.join(TIME_TO_COMPLETE_PARSING);
        } catch (InterruptedException e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            dbAdapter.flushTcpPackets();
        }
    }

    private class ParsingThread extends Thread {
        private final long parsingStartTime;
        private long parsingEndTime = Long.MAX_VALUE;
        private final BufferedInputStream inputStream;
        private final int IPVERSION_MASK=0x000000F0;
        private final int IPVERSION_SHIFT=4;
        private final int IPV4_HEADER_LEN=20;
        private final int IPV6_HEADER_LEN=40;
        private final int IP4_PROTOID=0x0800;
        private final int IP6_PROTOID=0x86dd;
        private final int IP4_VERSION=4;
        private final int IP6_VERSION=6;
        int parsedBytes=0;

        public ParsingThread(long parsingStartTime, BufferedInputStream inputStream) {
            this.parsingStartTime = parsingStartTime;
            this.inputStream = inputStream;
        }

        public void stop(long parsingEndTime) {
            this.parsingEndTime = parsingEndTime;
        }

        @Override
        public void run() {
            logger.debug("Parsing thread started! parsing starttime: " + parsingStartTime);
            try {
                if (!parsingIsRunning) {
                    parsingIsRunning = true;
                    safeSkip(inputStream, 6 * UINT32_SIZE); // skip global header
                    while (!isInterrupted()) {
                        readFrame();
                    }
                }
            } catch (IOException e) {
                logger.error("Error occurred during tcpdump parsing: " + ExceptionUtils.getStackTrace(e));
            } catch (InterruptedException e) {
                logger.error("Parsing thread was interrupted unexpectedly! " + ExceptionUtils.getStackTrace(e));
                interrupt();
            } finally {
                IOUtil.safeClose(inputStream);
                parsingIsRunning = false;
            }
            logger.debug("Parsing thread stopped");
        }

        private void readFrame() throws IOException, InterruptedException {
            byte[] buffer = new byte[UINT32_SIZE];
            safeRead(inputStream, buffer); // timestamp seconds, 4 bytes
            int timestampSec = convertByteArrayToIntLittleEndian(buffer);
            safeRead(inputStream, buffer); // timestamp milliseconds, 4 bytes
            int timestampMs = convertByteArrayToIntLittleEndian(buffer);
            long timestamp = timestampSec * 1000L + timestampMs / 1000L; // full timestamp ms
            safeRead(inputStream, buffer); // packet length (captured), 4 bytes
            int frameLength = convertByteArrayToIntLittleEndian(buffer);
            safeSkip(inputStream, UINT32_SIZE); // packet length (actual), 4 bytes
            if (timestamp > parsingEndTime) {
                dbAdapter.flushTcpPackets();
                interrupt();
                parsingIsRunning = false;
                logger.debug("Interrupting parsing thread");
            } else if (timestamp >= parsingStartTime) {
                processPacket(frameLength, timestamp);
            } else {
                safeSkip(inputStream, frameLength);
            }
        }

        private void processPacket(int frameLength, long timestamp) throws IOException, InterruptedException {
            parsedBytes=0;
            byte[] buffer16 = new byte[UINT16_SIZE];
            safeRead(inputStream, buffer16); // sll packet type, 2 bytes
            int packetType = convertByteArrayToIntBigEndian(buffer16);
            if (!(packetType == 0 || packetType == 4)) {
                safeSkip(inputStream, frameLength - 2);  // skip arp
                Assert.assertEquals("processPacket processed wrong number of bytes for packet time:"+timestamp+" (PT1)", frameLength, parsedBytes);
                return;
            }
            safeRead(inputStream, buffer16); // sll address type
            int addressType = convertByteArrayToIntBigEndian(buffer16);
            safeSkip(inputStream, UINT16_SIZE); // address length, 2 bytes
            safeSkip(inputStream, 4 * UINT16_SIZE); // the rest of SLL + IP version + IP header length // 12 bytes

            safeRead(inputStream, buffer16);
            int sllProtocol = convertByteArrayToIntBigEndian(buffer16);
            int verlen=safeRead(inputStream);
            if(!((sllProtocol==IP4_PROTOID)||(sllProtocol==IP6_PROTOID))){
                safeSkip(inputStream, frameLength - (SLL_LENGTH+1));  // skip arp
                Assert.assertEquals("processPacket processed wrong number of bytes for packet time:"+timestamp+" (PT2)", frameLength, parsedBytes);
                return;
            }

            int ipLength=0;
            int IPheaderLen=0;
            int protocolId=0;
            String sourceAddress="";
            String destinationAddress="";
            if(((verlen&IPVERSION_MASK)>>IPVERSION_SHIFT)==IP4_VERSION){
                IPheaderLen=IPV4_HEADER_LEN;
                safeSkip(inputStream,1);
                safeRead(inputStream, buffer16); // ip length, 2 bytes
                ipLength = convertByteArrayToIntBigEndian(buffer16);
                safeSkip(inputStream, UINT32_SIZE + BYTE_SIZE); // 5
                protocolId = safeRead(inputStream); // proto id, 1 byte
                safeSkip(inputStream, UINT16_SIZE); // checksum, 2 bytes
                byte[] addressBytes = new byte[UINT32_SIZE];
                safeRead(inputStream, addressBytes); // source address, 4 bytes
                sourceAddress = convertBytesToIpAddress(addressBytes);
                safeRead(inputStream, addressBytes); // destination address, 4 bytes
                destinationAddress = convertBytesToIpAddress(addressBytes);
                if (!(protocolId == TCP_PROTO_ID || protocolId == UDP_PROTO_ID)) {
                    safeSkip(inputStream, frameLength - SLL_LENGTH - IPheaderLen);
                    Assert.assertEquals("processPacket processed wrong number of bytes for packet time:"+timestamp+" (PT3)", frameLength, parsedBytes);
                    return;
                }
            }else if(((verlen&IPVERSION_MASK)>>IPVERSION_SHIFT)==IP6_VERSION){
                IPheaderLen=IPV6_HEADER_LEN;
                safeSkip(inputStream,3);
                safeRead(inputStream, buffer16); // ip length, 2 bytes
                ipLength = convertByteArrayToIntBigEndian(buffer16)+IPheaderLen; //+40 bytes of ipv6 header
                protocolId = safeRead(inputStream); // proto id, 1 byte
                safeSkip(inputStream,1);
                byte[] addressBytes = new byte[UINT32_SIZE*4];
                safeRead(inputStream, addressBytes);
                sourceAddress=convertBytesToIp6Address(addressBytes);
                safeRead(inputStream, addressBytes); // destination address, 4 bytes
                destinationAddress = convertBytesToIp6Address(addressBytes);
                if (!(protocolId == TCP_PROTO_ID || protocolId == UDP_PROTO_ID)) {
                    safeSkip(inputStream, frameLength - SLL_LENGTH - IPheaderLen);
                    Assert.assertEquals("processPacket processed wrong number of bytes for packet time:"+timestamp+" (PT4)", frameLength, parsedBytes);
                   return;
                }
            }
            safeRead(inputStream, buffer16); // source port, 2 bytes
            int sourcePort = convertByteArrayToIntBigEndian(buffer16);
            safeRead(inputStream, buffer16); // destination port, 2 bytes
            int destinationPort = convertByteArrayToIntBigEndian(buffer16);

            if (protocolId == TCP_PROTO_ID) {
                byte[] buffer32 = new byte[UINT32_SIZE];
                safeRead(inputStream, buffer32); // seq number, 4 bytes
                int sequenceNumber = convertByteArrayToIntBigEndian(buffer32);
                safeRead(inputStream, buffer32); // ack number, 4 bytes
                int acknowledgementNumber = convertByteArrayToIntBigEndian(buffer32);
                safeRead(inputStream, buffer16);
                final int tcpFlagsStartPos = 10;
                final int tcpFlagsEndPos = 16;
                BitSet flags = fromByteArray(buffer16).get(tcpFlagsStartPos, tcpFlagsEndPos);
                int tcpHeaderLength = bitSetToInt(getReversedBitSet(fromByteArray(buffer16), 0, 4)) * 4;
                safeSkip(inputStream, tcpHeaderLength - 7 * UINT16_SIZE);
                int payloadLength = ipLength - tcpHeaderLength - IPheaderLen;
                byte[] dataBytes = null;
                if (payloadLength > 0) {
                    dataBytes = new byte[payloadLength];
                    safeRead(inputStream, dataBytes);
                }
                dbAdapter.storeTcpPacket(PacketBuilder.buildTcpPacket(timestamp, frameLength, payloadLength, packetType,
                        addressType, sourceAddress, destinationAddress, sourcePort, destinationPort, sequenceNumber,
                        acknowledgementNumber, flags, dataBytes), true);
            } else if (protocolId == UDP_PROTO_ID) {
                safeSkip(inputStream, UINT32_SIZE);
                int payloadLength = ipLength - IPheaderLen - 2 * UINT32_SIZE;
                if (!(sourcePort == DNS_PORT || sourcePort >= MIN_DISPATCHER_PORT || sourcePort == DNS_SERVER_PORT
                        || destinationPort == DNS_PORT || destinationPort >= MIN_DISPATCHER_PORT || destinationPort == DNS_SERVER_PORT)) {
                    safeSkip(inputStream, payloadLength);
                } else {
                    byte[] dataBytes = new byte[payloadLength];
                    safeRead(inputStream, dataBytes);
                    int transactionId = convertByteArrayToIntBigEndian(Arrays.copyOfRange(dataBytes, 0, 2));
                    DnsPacket dnsPacket = PacketBuilder.buildDnsPacket(timestamp, frameLength, payloadLength,
                            packetType, addressType, sourceAddress, destinationAddress, sourcePort, destinationPort,
                            transactionId, dataBytes);
                    if (dnsPacket != null) {
                        dbAdapter.storeDnsPacket(dnsPacket);
                    }
                }
            }
            int trailerLength = frameLength - SLL_LENGTH - ipLength;
            if (trailerLength > 0) safeSkip(inputStream, trailerLength);

            Assert.assertEquals("processPacket processed wrong number of bytes for packet time:"+timestamp+" (PT5)", frameLength, parsedBytes);
        }

        // Skips bytesToSkip bytes in this stream. Blocks until required amount of bytes will not be skipped
        private void safeSkip(InputStream is, long bytesToSkip) throws IOException, InterruptedException {
            waitForData(is, bytesToSkip);
            int skipped = 0;
            while (skipped < bytesToSkip) {
                skipped += is.skip(bytesToSkip - skipped);
            }
            parsedBytes+=  bytesToSkip;
        }

        // Reads bytes from InputStream into byte array. Blocks until required amount of bytes (passed array length)
        // will not be read
        private void safeRead(InputStream is, byte[] bytes) throws IOException, InterruptedException {
            waitForData(is, bytes.length);
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) is.read();
            }
            parsedBytes+=bytes.length;
        }

        // Reads single byte from InputStream. Blocks until required amount of bytes (passed array length)
        // will not be read
        private byte safeRead(InputStream is) throws IOException, InterruptedException {
            waitForData(is, 1);
            parsedBytes+=1;
            return (byte) is.read();
        }

        // Blocks until required amount of bytes not will be available
        private void waitForData(InputStream is, long requiredLength) throws IOException, InterruptedException {
            long startTime = System.currentTimeMillis();
            while (is.available() < requiredLength) {
                Thread.sleep(TCPDUMP_REFRESH_RATE);
            }
            long endTime = System.currentTimeMillis();
        }
    }

    private void ping() throws IOException {
        Runtime.getRuntime().exec("ping -c 1 " + AsimovTestCase.TEST_RESOURCE_HOST);
    }
}
