package com.seven.asimov.it.utils.tcpdump;

import android.content.Context;
import com.seven.asimov.it.utils.conn.ConnUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.seven.asimov.it.base.constants.TFConstantsIF.HTTP_PORT;
import static com.seven.asimov.it.base.constants.TFConstantsIF.MIN_DISPATCHER_PORT;

class HttpSessionCollector {

    private static final Logger logger = LoggerFactory.getLogger(HttpSessionCollector.class.getSimpleName());

    private final Context context;

    public HttpSessionCollector(Context context) {
        this.context = context;
    }

    List<HttpSession> getSessions(long startTime, long endTime) {
        List<HttpSession> sessions = new ArrayList<HttpSession>();
        List<TcpPacket> packets = DbAdapter.getInstance(context).getTcpPackets(startTime, endTime, true);

        for (TcpPacket packet : packets) {
            if (packet.isSyn() && packet.getDirection() == Direction.FROM_US
                    && sessionNotExists(packet.getSourcePort(), sessions)) {
                HttpSession session = new HttpSession();
                sessions.add(session);
                session.addPacket(packet);
                session.setClientPort(packet.getSourcePort());
                session.setServerPort(packet.getDestinationPort());
                session.setInterface(packet.getInterface());
                session.setClientAddress(packet.getSourceAddress());
                session.setServerAddress(packet.getDestinationAddress());
                session.setRequestStartTimestamp(packet.getTimestamp());
            } else {
                int clientPort = packet.getDirection() == Direction.FROM_US ? packet.getSourcePort()
                        : packet.getDestinationPort();
                HttpSession session = findSessionByClientPort(clientPort, sessions);
                if (session != null) session.addPacket(packet);
            }
        }
        HttpSessionHandler.processSessions(sessions);
        return sessions;
    }

    List<HttpSession> getSessions(String uri, long startTime, long endTime) {
        List<HttpSession> sessions = getSessions(startTime, endTime);
        UriHttpSessionFilter.filterSessions(uri, sessions);
        PortSessionFilter.filterSessions(new int[]{HTTP_PORT}, sessions);
        return sessions;
    }

    List<HttpSession> getSessions(long startTime, long endTime, int... serverPorts) {
        List<HttpSession> sessions = getSessions(startTime, endTime);
        logger.info("Checking!!!");
        for (HttpSession s : sessions) {
            s.toString();
        }
        //TcpDumpUtil.logHttpSessions(sessions, "no filter");
        PortSessionFilter.filterSessions(serverPorts, sessions);
        //TcpDumpUtil.logHttpSessions(sessions, "port filter");
        return sessions;
    }

    List<HttpSession> getSessions(String host, long startTime, long endTime, int... serverPorts) {
        String hostIP;
        try {
            hostIP = ConnUtils.getHostAddress(host);
        } catch (UnknownHostException e) {
            logger.debug(String.format("Host %s not found!", host));
            return null;
        }
        List<HttpSession> sessions = getSessions(startTime, endTime, serverPorts);
        filterHttpSessionsByIP(hostIP, sessions);
        //TcpDumpUtil.logHttpSessions(sessions, "host filter");
        return sessions;
    }

    List<HttpSession> getSessions(String host, Interface iface, long startTime, long endTime, int... serverPorts) {
        List<HttpSession> sessions = getSessions(host, startTime, endTime, serverPorts);
        for (HttpSession s : sessions) {
            s.toString();
        }
        filterHttpSessionsByInterface(iface, sessions);
        //TcpDumpUtil.logHttpSessions(sessions, "interface filter");
        return sessions;
    }

    List<HttpSession> getSessions(String uri, String host, Interface iface, long startTime, long endTime, int... serverPorts) {
        List<HttpSession> sessions = getSessions(host, startTime, endTime, serverPorts);
        UriHttpSessionFilter.filterSessions(uri, sessions);
        filterHttpSessionsByInterface(iface, sessions);
        //TcpDumpUtil.logHttpSessions(sessions, "interface filter");
        return sessions;
    }

    // Checks that session with defined clientPort not exists in sessions list
    private boolean sessionNotExists(int clientPort, List<HttpSession> sessions) {
        return findSessionByClientPort(clientPort, sessions) == null;
    }

    // Finds session with defined client port in sessions list.
    // Returns first found session or null in case session not found
    private HttpSession findSessionByClientPort(int clientPort, List<HttpSession> sessions) {
        for (HttpSession session : sessions) {
            if (session.getClientPort() == clientPort) return session;
        }
        return null;
    }

    private static class UriHttpSessionFilter {
        // removes from session list sessions with URI distinct from defined
        private static void filterSessions(String uri, List<HttpSession> sessions) {
            Iterator<HttpSession> iterator = sessions.iterator();
            while (iterator.hasNext()) {
                HttpSession session = iterator.next();
                if (session.getUri() == null || !session.getUri().equals(uri)) iterator.remove();
            }
        }
    }

    private static class PortSessionFilter {
        private static void filterSessions(int[] ports, List<HttpSession> sessions) {
            Iterator<HttpSession> iterator = sessions.iterator();
            while (iterator.hasNext()) {
                HttpSession session = iterator.next();
                boolean portExists = false;
                for (int port : ports) {
                    logger.info("port! " + port);
                    if (session.getServerPort() == port) portExists = true;
                }
                if (session.getServerPort() >= MIN_DISPATCHER_PORT) portExists = true;
                if (!portExists) iterator.remove();
            }
        }
    }

    private static void filterHttpSessionsByIP(String hostIP, List<HttpSession> sessions) {
        Iterator<HttpSession> iterator = sessions.iterator();
        while (iterator.hasNext()) {
            HttpSession session = iterator.next();
            if (!session.getServerAddress().equals(hostIP)
                    ) iterator.remove();
        }
    }

    private static void filterHttpSessionsByInterface(Interface iface, List<HttpSession> sessions) {
        Iterator<HttpSession> iterator = sessions.iterator();
        while (iterator.hasNext()) {
            HttpSession session = iterator.next();
            if (session.getInterface() != iface
                    ) iterator.remove();
        }
    }

    protected int getHttpSessionCount(long startTime, long endTime) {
        List<TcpPacket> packets = DbAdapter.getInstance(context).getTcpPackets(startTime, endTime, true);
        int httpSessionsCount = 0;

        for (TcpPacket packet : packets) {
            if (packet.getSourcePort() == HTTP_PORT || packet.getDestinationPort() == HTTP_PORT) {
                httpSessionsCount++;
            }
        }
        return httpSessionsCount;
    }
}
