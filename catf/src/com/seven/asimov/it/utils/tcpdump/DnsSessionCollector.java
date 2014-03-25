package com.seven.asimov.it.utils.tcpdump;

import android.content.Context;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.seven.asimov.it.base.constants.TFConstantsIF.DNS_PORT;

class DnsSessionCollector {

    private final Context context;

    public DnsSessionCollector(Context context) {
        this.context = context;
    }

    List<DnsSession> getSessions(String host, long startTime, long endTime) {
        List<DnsSession> sessions = new ArrayList<DnsSession>();
        List<DnsPacket> packets = DbAdapter.getInstance(context).getDnsPackets(startTime, endTime, false);
        for (DnsPacket packet : packets) {
            if (packet.getDirection() == Direction.FROM_US && sessionNotExists(packet.getTransactionId(), sessions)) {
                DnsSession session = new DnsSession();
                sessions.add(session);
                if(packet.getInterface()==Interface.NETWORK)session.setNetwork(true);
                if(packet.getInterface()==Interface.LOOPBACK)session.setLoopback(true);
                session.addPacket(packet);
                session.setTransactionId(packet.getTransactionId());
                session.setInterface(packet.getInterface());
                session.setHost(packet.getHost());
                session.setRequestTimestamp(packet.getTimestamp());
            } else {
                DnsSession session = findSessionByTransactionId(packet.getTransactionId(), sessions);
                if (session != null){
                    session.addPacket(packet);
                    if(packet.getInterface()==Interface.NETWORK)session.setNetwork(true);
                    if(packet.getInterface()==Interface.LOOPBACK)session.setLoopback(true);
                }
            }
        }
        HostDnsSessionFilter.filterSessions(host, sessions);
        processDnsSessions(sessions);
        return sessions;
    }

    protected int getDnsSessionCount(long startTime, long endTime) {
        List<DnsPacket> packets = DbAdapter.getInstance(context).getDnsPackets(startTime, endTime, true);
        int dnsSessionsCount = 0;

        for (DnsPacket packet : packets) {
            if (packet.getSourcePort() == DNS_PORT || packet.getDestinationPort() == DNS_PORT) {
                dnsSessionsCount ++;
            }
        }
        return dnsSessionsCount;
    }

    private void processDnsSessions(List<DnsSession> dnsSessions){
        for (DnsSession dnsSession : dnsSessions){
            processDnsSession(dnsSession);
        }
    }

    private void processDnsSession(DnsSession dnsSession){
        int upstreamPayloadLengthLocal = 0;
        int downstreamPayloadLengthLocal = 0;
        int upstreamPayloadLengthNetwork = 0;
        int downstreamPayloadLengthNetwork = 0;

        int upstreamTotalLength = 0;
        int downstreamTotalLength = 0;
        for (Packet packet : dnsSession.getPackets()){
            if (packet.getDirection() == Direction.FROM_US){
                if (packet.getInterface() == Interface.LOOPBACK){
                    upstreamTotalLength += packet.getTotalLength();
                    upstreamPayloadLengthLocal += packet.getPayloadLength();
                } else {
                    upstreamTotalLength += packet.getTotalLength();
                    upstreamPayloadLengthNetwork += packet.getPayloadLength();
                }
            }else if (packet.getDirection() == Direction.TO_US){
                if (packet.getInterface() == Interface.LOOPBACK){
                    downstreamTotalLength += packet.getTotalLength();
                    downstreamPayloadLengthLocal += packet.getPayloadLength();
                } else {
                    downstreamTotalLength += packet.getTotalLength();
                    downstreamPayloadLengthNetwork += packet.getPayloadLength();
                }
            }
        }
        dnsSession.setUpstreamTotalLength(upstreamTotalLength);
        dnsSession.setUpstreamPayloadLengthLocal(upstreamPayloadLengthLocal);
        dnsSession.setUpstreamPayloadLengthNetwork(upstreamPayloadLengthNetwork);

        dnsSession.setDownstreamTotalLength(downstreamTotalLength);
        dnsSession.setDownstreamPayloadLengthLocal(downstreamPayloadLengthLocal);
        dnsSession.setDownstreamPayloadLengthNetwork(downstreamPayloadLengthNetwork);
    }

    private boolean sessionNotExists(int transactionId, List<DnsSession> sessions) {
        return findSessionByTransactionId(transactionId, sessions) == null;
    }

    private DnsSession findSessionByTransactionId(int transactionId, List<DnsSession> sessions) {
        for (DnsSession session : sessions) {
            if (session.getTransactionId() == transactionId) return session;
        }
        return null;
    }

    private static class HostDnsSessionFilter {
        // removes from session list sessions with URI distinct from defined
        private static void filterSessions(String host, List<DnsSession> sessions) {
            Iterator<DnsSession> iterator = sessions.iterator();
            while (iterator.hasNext()) {
                DnsSession session = iterator.next();
                if (session.getHost() == null || !session.getHost().equals(host)) iterator.remove();
            }
        }
    }
}
