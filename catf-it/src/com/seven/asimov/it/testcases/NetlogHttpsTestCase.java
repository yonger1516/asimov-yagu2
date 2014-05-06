package com.seven.asimov.it.testcases;

import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.SocketFactoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetlogHttpsTestCase extends TcpDumpTestCase {
    private static final Logger logger = LoggerFactory.getLogger(NetlogHttpsTestCase.class.getSimpleName());

    private static SocketFactoryUtil.CustomSocketFactory socketFactory = new SocketFactoryUtil.CustomSocketFactory();

  /* protected TimeInfoTransaction testNetlogHttpsSuite(NetlogHttpsTests.CipherSuite suite) throws Exception {

        TimeInfoTransaction timeInfoTransaction = new TimeInfoTransaction();

        NetlogTask netlogTask = new NetlogTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), netlogTask);
        logcatUtil.start();
        TcpDumpUtil tcpDump = TcpDumpUtil.getInstance(getContext());
        BufferedReader br = null;
        HttpsURLConnection connection = null;
        try {
            timeInfoTransaction.setTimeStart(System.currentTimeMillis());
            socketFactory.setSupportedCipherSuites(new String[]{suite.name()});
            String resourceUrl = "https://" + AsimovTestCase.TEST_RESOURCE_HOST + "/asimov_it_cv_suite_" + suite.name();
            URL url = new URL(resourceUrl);
            PrepareResourceUtil.prepareResource(resourceUrl, false);
            tcpDump.start();
            connection = (HttpsURLConnection) url.openConnection();
            connection.setReadTimeout(30 * 1000);
            connection.setSSLSocketFactory(socketFactory);
            connection.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });
            connection.connect();
            br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                body.append(line).append("\n");
            }
            logger.trace("Body received by test app : " + body);
            logger.trace("Cipher suite " + suite.name() + " applied:" + connection.getCipherSuite());
        } catch (Exception e) {
            logger.error("Cipher suite : " + suite.toString() + " not supported by OC!");
            logger.error(ExceptionUtils.getStackTrace(e));
        } finally {
            timeInfoTransaction.setTimeEnd(System.currentTimeMillis());
            tcpDump.stop();
            //wait for neltog records
            TestUtil.sleep(5 * 1000);
            logcatUtil.stop();
            logger.trace(netlogTask.toString());
            if (br != null) br.close();
            if (connection != null) connection.disconnect();
            executeCheck(tcpDump, timeInfoTransaction, netlogTask.getLogEntries());
        }
        return timeInfoTransaction;
    }

    public void executeCheck(TcpDumpUtil tcpDumpUtil, TimeInfoTransaction timeInfoTransaction, List<NetlogEntry> entries) {

        long clientInNetlog = 0;
        long clientOutNetlog = 0;
        long serverInNetlog = 0;
        long serverOutNetlog = 0;

        long clientInTcpdump = 0;
        long clientOutTcpdump = 0;
        long serverInTcpdump = 0;
        long serverOutTcpdump = 0;

        List<HttpSession> localloopSessions = getLocaloopSessions(tcpDumpUtil, timeInfoTransaction);
        List<Integer> loPorts = new ArrayList<Integer>();
        List<Integer> netPorts = new ArrayList<Integer>();
        if (localloopSessions != null) {
            for (HttpSession session : localloopSessions) {
                logger.trace("localloopSession: " + session.toString());
                loPorts.add(session.getClientPort());
                logger.trace("Founded session with loPort = " + session.getClientPort());
            }
        }

        logger.trace("StartTime=" + timeInfoTransaction.getTimeStart() + " TimeEnd=" + timeInfoTransaction.getTimeEnd());
        for (NetlogEntry entry : entries) {
            logger.trace("Entry=" + entry);
        }
        for (Integer loPort : loPorts) {
            int netPort = -1;
            for (NetlogEntry entry : entries) {
                if (entry.getLoport() == loPort) {
                    if (entry.getNetport() > 0 && entry.getLoport() != TFConstantsIF.RELAY_PORT) {
                        netPort = entry.getNetport();
                    }
                }
            }
            netPorts.add(netPort);
        }

        int index = 0;
        boolean isPassed = false;
        for (Integer loPort : loPorts) {
            int netPort = netPorts.get(index++);
            clientInNetlog = 0;
            clientOutNetlog = 0;
            serverInNetlog = 0;
            serverOutNetlog = 0;
            for (NetlogEntry entry : entries) {
                if (entry.getLoport() == loPort) {
                    clientInNetlog += entry.getClient_in();
                    clientOutNetlog += entry.getClient_out();
                }
                if (entry.getNetport() == netPort) {
                    serverInNetlog += entry.getServer_in();
                    serverOutNetlog += entry.getServer_out();
                }
            }
            HttpSession localloopSession = getLocaloopSession(tcpDumpUtil, timeInfoTransaction, loPort);
            if (localloopSession != null) {
                logger.trace("localloopSession found: " + localloopSession.toString());
                clientInTcpdump = localloopSession.getUpstreamPayloadLength();
                clientOutTcpdump = localloopSession.getDownstreamPayloadLength();
            }
            HttpSession networkSession = getNetworkSession(tcpDumpUtil, timeInfoTransaction, netPort);
            if (networkSession != null) {
                logger.trace("networkSession found: " + networkSession.toString());
                serverInTcpdump = networkSession.getDownstreamPayloadLength();
                serverOutTcpdump = networkSession.getUpstreamPayloadLength();
            }

            logger.trace("ClientIn extracted from netlogs for tcp sessions (loPort = " + loPort + " ,netPort = " + netPort + ") equals : " + clientInNetlog);
            logger.trace("ClientOut extracted from netlogs for tcp sessions (loPort = " + loPort + " ,netPort = " + netPort + ") equals : " + clientOutNetlog);
            logger.trace("ServerIn extracted from netlogs for tcp sessions (loPort = " + loPort + " ,netPort = " + netPort + ") equals : " + serverInNetlog);
            logger.trace("ServerOut extracted from netlogs for tcp sessions (loPort = " + loPort + " ,netPort = " + netPort + ") equals : " + serverOutNetlog);

            logger.trace("ClientIn calculated by tcpdump  for tcp sessions (loPort = " + loPort + " ,netPort = " + netPort + ") equals : " + clientInTcpdump);
            logger.trace("ClientOut calculated by tcpdump for tcp sessions (loPort = " + loPort + " ,netPort = " + netPort + ") equals : " + clientOutTcpdump);
            logger.trace("ServerIn calculated by tcpdump for tcp sessions (loPort = " + loPort + " ,netPort = " + netPort + ") equals : " + serverInTcpdump);
            logger.trace("ServerOut calculated by tcpdump for tcp sessions (loPort = " + loPort + " ,netPort = " + netPort + ") equals : " + serverOutTcpdump);

            if (index < loPorts.size()) {
                if (clientInNetlog == clientInTcpdump &&
                        clientOutNetlog == clientOutTcpdump &&
                        serverInNetlog == serverInTcpdump &&
                        serverOutNetlog == serverOutTcpdump) {
                    isPassed = true;
                }
            }
        }

        if (!isPassed) {
            assertTrue(String.format("ClientIn form tcpdump %d not equals clientIn from netlog %d !", clientInTcpdump, clientInNetlog), trafficCorresponds(clientInTcpdump, clientInNetlog));
            assertTrue(String.format("ClientOut form tcpdump %d not equals clientOut from netlog %d ! ", clientOutTcpdump, clientOutNetlog), trafficCorresponds(clientOutTcpdump, clientOutNetlog));
            assertTrue(String.format("ServerIn form tcpdump %d not equals serverIn from netlog %d ! ", serverInTcpdump, serverInNetlog), trafficCorresponds(serverInTcpdump, serverInNetlog));
            assertTrue(String.format("ServerOut form tcpdump %d not equals serverOut from netlog %d ! ", serverOutTcpdump, serverOutNetlog), trafficCorresponds(serverOutTcpdump, serverOutNetlog));
        }

        if (loPorts.size() == 0) {
            throw new AssertionFailedError("Can't find loPort for : " + timeInfoTransaction);
        }
    }

    protected HttpSession getLocaloopSession(TcpDumpUtil tcpDump, TimeInfoTransaction timeInfoTransaction, int loPort) {
        List<HttpSession> sessions = tcpDump.getHttpSessions(
                timeInfoTransaction.getTimeStart(), timeInfoTransaction.getTimeEnd());
        logger.info("Localloop sessions size : " + Integer.toString(sessions.size()));
        for (HttpSession session : sessions) {
            if (session.getInterface() == Interface.LOOPBACK &&
                    session.getClientPort() == loPort) {
                logger.info(session.toString());
                return session;
            }
        }
        return null;
    }

    protected List<HttpSession> getLocaloopSessions(TcpDumpUtil tcpDump, TimeInfoTransaction timeInfoTransaction) {
        List<HttpSession> sessions = tcpDump.getHttpSessions(
                timeInfoTransaction.getTimeStart(), timeInfoTransaction.getTimeEnd());
        logger.info("sessions size : " + Integer.toString(sessions.size()));
        List<HttpSession> result = new ArrayList<HttpSession>();
        for (HttpSession session : sessions) {
            logger.trace("getLocaloopSessions: All: " + session.toString());
            if (session.getInterface() == Interface.LOOPBACK &&
//                    (session.getClientPort() >= TFConstants.MIN_DISPATCHER_PORT ||
//                            session.getServerPort() >= TFConstants.MIN_DISPATCHER_PORT)
                    (TcpDumpHelper.isKnownServerPort(session.getServerPort()))) {
                logger.info(session.toString());
                result.add(session);
                logger.trace("getLocaloopSessions: Localoop: " + session.toString());
            }
        }
        if (result.size() > 0)
            return result;
        return null;
    }

   /* protected HttpSession getNetworkSession(TcpDumpUtil tcpDump, TimeInfoTransaction timeInfoTransaction) {
        List<HttpSession> sessions = tcpDump.getHttpSessions(timeInfoTransaction.getTimeStart(), timeInfoTransaction.getTimeEnd());
        logger.info("Network session size : " + sessions.size());
        for (HttpSession session : sessions) {
            if (session.getInterface() == Interface.NETWORK && session.getClientPort() != TFConstantsIF.OC_BYPASS_PORT && session.getServerPort() != TFConstantsIF.OC_BYPASS_PORT) {
                return session;
            }
        }
        return null;
    }

    protected HttpSession getNetworkSession(TcpDumpUtil tcpDump, TimeInfoTransaction timeInfoTransaction, int port) {
        List<HttpSession> sessions = tcpDump.getHttpSessions(timeInfoTransaction.getTimeStart(), timeInfoTransaction.getTimeEnd());
        logger.info("Network session size : " + sessions.size());
        for (HttpSession session : sessions) {
            if (session.getInterface() == Interface.NETWORK &&
                    session.getClientPort() != TFConstantsIF.OC_BYPASS_PORT &&
                    session.getServerPort() != TFConstantsIF.OC_BYPASS_PORT &&
                    session.getClientPort() != TFConstantsIF.RELAY_PORT &&
                    session.getServerPort() != TFConstantsIF.RELAY_PORT &&
                    (session.getClientPort() == port || session.getServerPort() == port)) {
                logger.info(session.toString());
                logger.trace("-------------------------------------");
                for (Packet p : session.getPackets()) {
                    logger.trace("PayloadLength=" + p.getPayloadLength() + " Direction" + p.getDirection());
                }
                logger.trace("-------------------------------------");
                return session;
            }
        }
        return null;
    }   */

    private boolean trafficCorresponds(long a, long b) {
        return (Math.abs(a - b) <= 1);
    }
}
