package com.seven.asimov.it.utils.tcpdump;

import android.content.Context;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.seven.asimov.it.base.constants.TFConstantsIF.HTTPS_PORT;
import static com.seven.asimov.it.base.constants.TFConstantsIF.HTTPS_SPLIT_PORT;

public class TcpDumpUtil {

    private static final Logger logger = LoggerFactory.getLogger(TcpDumpUtil.class.getSimpleName());

    private static TcpDumpUtil instance;

    private boolean isRunning;

    private long startTime;
    private long endTime;
    Context context;

    private final TcpDumpParser parser;
    private final HttpSessionCollector httpSessionsCollector;
    private final DnsSessionCollector dnsSessionsCollector;
    private final Z7TpSessionsCollector z7TpSessionsCollector;
    private static String tcpDumpTempPath;

    public void setTcpDumpTempPath(String temp) {
        tcpDumpTempPath = temp;
    }

    public static TcpDumpUtil getInstance(Context context) {
        if (instance == null) {
            instance = new TcpDumpUtil(context);
        }
        return instance;
    }

    private TcpDumpUtil(Context context) {
        parser = new TcpDumpParser(context);
        httpSessionsCollector = new HttpSessionCollector(context);
        dnsSessionsCollector = new DnsSessionCollector(context);
        z7TpSessionsCollector = new Z7TpSessionsCollector(context);
        this.context = context;
    }

    public void start() {
        logger.trace("start()");
        parser.traceFile7TT = tcpDumpTempPath;
        isRunning = true;
        startTime = System.currentTimeMillis();
        parser.start(startTime);
    }

    public void start(long parsingStartTime) {
        isRunning = true;
        parser.start(parsingStartTime);
    }

    public void stop() throws IOException {
        if (!isRunning) return;
        endTime = System.currentTimeMillis();
        logger.debug("Asking parsing thread to stop. Endtime: " + endTime);
        parser.stop(endTime);
        isRunning = false;
    }

    public Set<String> getResolvedHosts(String hosts[]) {
        return getResolvedHosts(hosts, startTime, endTime);
    }

    public Set<String> getResolvedHosts(String hosts[], long startTime, long endTime) {
        //logger.trace("startTime=" + startTime);
        //logger.trace("endTime=" + endTime);
        //logger.trace("hosts[]=" + Arrays.toString(hosts));
        Set<String> result = new HashSet<String>();
        for (String host : hosts) {
            logger.info("Processing host: " + host);
            List<DnsSession> dnsSessions = dnsSessionsCollector.getSessions(host, startTime, endTime);
            for (DnsSession session : dnsSessions) {
                logger.info("Processing DNS session: " + session);
                List<DnsPacket> packets = session.getPackets();
                for (DnsPacket packet : packets) {
                    if (packet.getInterface() == Interface.NETWORK) {
                        logger.info("Network packets found! for host " + host);
                        result.add(host);
                    }
                }
            }
        }
        //logger.trace("getResolvedHostsr result:");
        //for (String res : result) {
        //    logger.trace(res);
        //}
        return result;
    }

    public List<HttpSession> getHttpSessions(long startTime, long endTime) {
        return httpSessionsCollector.getSessions(startTime, endTime);
    }

    public List<HttpSession> getHttpSessions(String uri, long startTime, long endTime) {
        return httpSessionsCollector.getSessions(uri, startTime, endTime);
    }

    public List<HttpSession> getHttpSessions(String uri, String host, Interface iface, long startTime, long endTime) {
        return httpSessionsCollector.getSessions(uri, host, iface, startTime, endTime, TFConstantsIF.HTTP_PORT);
    }

    public List<HttpSession> getHttpsSessions(long startTime, long endTime) {
        return httpSessionsCollector.getSessions(startTime, endTime, HTTPS_PORT, HTTPS_SPLIT_PORT);
    }

    public List<HttpSession> getHttpsSessions(String host, long startTime, long endTime) {
        return httpSessionsCollector.getSessions(host, startTime, endTime, HTTPS_PORT, HTTPS_SPLIT_PORT);
    }

    public List<HttpSession> getCustomSessions(String host, Interface iface, long startTime, long endTime, int... ports) {
        return httpSessionsCollector.getSessions(host, iface, startTime, endTime, ports);
    }

    public List<HttpSession> getHttpsSessions(String host, Interface iface, long startTime, long endTime) {
        return httpSessionsCollector.getSessions(host, iface, startTime, endTime, HTTPS_PORT, HTTPS_SPLIT_PORT);
    }

    public List<DnsSession> getDnsSessions(String host, long startTime, long endTime) {
        return dnsSessionsCollector.getSessions(host, startTime, endTime);
    }

    public List<DnsSession> getDnsSessions(String host) {
        return dnsSessionsCollector.getSessions(host, startTime, endTime);
    }

    public List<HttpSession> getZ7TPSession(long starTime, long endTime) {
        return z7TpSessionsCollector.getSessions(starTime, endTime);
    }

    public int getZ7TpPacketsCount(long startTime, long endTime) {
        return z7TpSessionsCollector.getZ7TpPacketsCount(startTime, endTime);
    }

    public int getHttpSessionCount(long startTime, long endTime) {
        return httpSessionsCollector.getHttpSessionCount(startTime, endTime);
    }

    public int getDnsSessionCount(long startTime, long endTime) {
        return dnsSessionsCollector.getDnsSessionCount(startTime, endTime);
    }

    public long getTcpTraffic(long startTime, long endTime, Interface packInterface, Direction packDirection, String sourceIP, String destinationIP, Integer sourcePort, Integer destinationPort) {
        //logger.trace("sourcePort=" + sourcePort + " destinationPort=" + destinationPort);
        List<TcpPacket> packets = DbAdapter.getInstance(context).getTcpPackets(startTime, endTime, false);
        long result = 0;
        for (TcpPacket packet : packets) {
            //logger.trace("getTcpTraffic: " + packet.toString());
            if (packInterface != null && packet.getInterface() != packInterface) continue;
            //logger.trace("getTcpTraffic: packInterface passed");
            if (packDirection == null) packDirection = Direction.BOTH;
            if (packDirection != Direction.BOTH && packet.getDirection() != packDirection) continue;
            //logger.trace("getTcpTraffic: packDirection passed");
            if (packDirection == Direction.BOTH) {
                if (sourceIP != null && destinationIP == null) destinationIP = sourceIP;
                if (destinationIP != null && sourceIP == null) sourceIP = destinationIP;
                if (!sourceIP.equals(packet.getSourceAddress()) && !destinationIP.equals(packet.getDestinationAddress()))
                    continue;
            } else {
                if (sourceIP != null && !sourceIP.equals(packet.getSourceAddress())) continue;
                //logger.trace("getTcpTraffic: sourceIP passed");
                if (destinationIP != null && !destinationIP.equals(packet.getDestinationAddress())) continue;
                //logger.trace("getTcpTraffic: destinationIP passed");
            }
            if (sourcePort != null && !(sourcePort == packet.getSourcePort())) continue;
            //logger.trace("getTcpTraffic: sourcePort passed");
            if (destinationPort != null && !(destinationPort == packet.getDestinationPort())) continue;
            //logger.trace("getTcpTraffic: destinationPort passed");
            //logger.trace("getTcpTraffic: found: ");
            result += packet.getPayloadLength();
        }
        return result;
    }

    public static void logHttpSessions(List<HttpSession> sessions, String message) {
        if (message != null)
            logger.info(message);
        for (HttpSession session : sessions) {
            logger.info(session.toString());
        }
    }
}
