package com.seven.asimov.it.utils.tcpdump;

import android.content.Context;
import com.seven.asimov.it.base.constants.TFConstantsIF;

import java.util.ArrayList;
import java.util.List;

import static com.seven.asimov.it.base.constants.TFConstantsIF.DEFAULT_RELAY_PORT;
import static com.seven.asimov.it.base.constants.TFConstantsIF.Z7TP_RELAY_PORT;


class Z7TpSessionsCollector {


    private final Context context;

    public Z7TpSessionsCollector(Context context) {
        this.context = context;
    }

    public int getZ7TpPacketsCount(long startTime, long endTime) {
        List<TcpPacket> packets = DbAdapter.getInstance(context).getTcpPackets(startTime, endTime, false);
        int z7TpPacketsCount = 0;
        for (TcpPacket packet : packets) {
            if (packet.getSourcePort() == Z7TP_RELAY_PORT || packet.getDestinationPort() == Z7TP_RELAY_PORT ||
                    packet.getSourcePort()==DEFAULT_RELAY_PORT || packet.getDestinationPort() == DEFAULT_RELAY_PORT) {
                z7TpPacketsCount++;
            }
        }
        return z7TpPacketsCount;
    }

    List<HttpSession> getSessions(long startTime, long endTime) {
        List<HttpSession> sessions = new ArrayList<HttpSession>();
        List<TcpPacket> packets = DbAdapter.getInstance(context).getTcpPackets(startTime, endTime, true);

        for (TcpPacket packet : packets) {
            if (packet.getDestinationPort() == TFConstantsIF.RELAY_PORT &&
                    packet.getDirection() == Direction.FROM_US
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
}

