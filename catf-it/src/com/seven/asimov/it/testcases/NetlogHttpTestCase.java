package com.seven.asimov.it.testcases;

import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.Base64;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.TimeInfoTransaction;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.crcsTasks.NetlogTask;
import com.seven.asimov.it.utils.logcat.wrappers.NetlogEntry;
import com.seven.asimov.it.utils.logcat.wrappers.OperationType;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import com.seven.asimov.it.utils.tcpdump.HttpSession;
import com.seven.asimov.it.utils.tcpdump.Interface;
import com.seven.asimov.it.utils.tcpdump.TcpDumpUtil;
import junit.framework.AssertionFailedError;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetlogHttpTestCase extends AsimovTestCase {

    private static final Logger logger = LoggerFactory.getLogger(NetlogHttpTestCase.class.getSimpleName());

    protected LogcatUtil logcatUtil;
    protected TcpDumpUtil tcpDump;

    protected void setUp() throws Exception {
        //Log.v(TAG, "setUp()");
        super.setUp();
        Thread.currentThread().setName("0");
        netlogTask = new NetlogTask();
        logcatUtil = new LogcatUtil(getContext(), netlogTask);
        logcatUtil.start();
        tcpDump = TcpDumpUtil.getInstance(getContext());
        tcpDump.start();
    }

    @Override
    protected void tearDown() throws Exception {
        //Log.v(TAG, "tearDown()");
        tcpDump.stop();
        logcatUtil.stop();
        logcatUtil.logTasks();
        super.tearDown();
    }

    @Override
    protected void runTest() throws Throwable {
        super.runTest();
        executeChecks();
    }

    private void executeChecks() {
        try {
            tcpDump.stop();
            logcatUtil.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (ExecuteCheck check : mChecksQueue) {
            check.executeCheck();
        }
        mChecksQueue.clear();
    }

    private class DataCheck implements ExecuteCheck {

        private TimeInfoTransaction timeInfoTransaction;
        private boolean isHit = false;

        private long clientInTcpdump;
        private int clientInNetlog;
        private long clientOutTcpdump;
        private int clientOutNetlog;
        private long serverInTcpdump;
        private int serverInNetlog;
        private long serverOutTcpdump;
        private int serverOutNetlog;

        public DataCheck(TimeInfoTransaction timeInfoTransaction) {
            this.timeInfoTransaction = timeInfoTransaction;
        }

        public DataCheck(TimeInfoTransaction timeInfoTransaction, boolean isHit) {
            this(timeInfoTransaction);
            this.isHit = isHit;
        }

        public void executeCheck() {

            HttpSession localloopSession = getLocaloopSession(this.timeInfoTransaction);
            HttpSession networkSession = getNetworkSession(timeInfoTransaction);
            int netPort = -1;
            int loPort = -1;
            if (networkSession != null) {
                netPort = networkSession.getClientPort();
            }
            if (localloopSession != null) {
                loPort = localloopSession.getClientPort();
            }

            logger.debug("StartTime=" + timeInfoTransaction.getTimeStart() + " TimeEnd=" + timeInfoTransaction.getTimeEnd());
            logger.debug("LoPort=" + loPort + " NetPort=" + netPort);

            for (NetlogEntry entry : netlogTask.getLogEntries()) {
                logger.debug("Entry=" + entry);
                if (entry.getLoport() == loPort) {
                    clientInNetlog += entry.getClient_in();
                    clientOutNetlog += entry.getClient_out();
                    serverInNetlog += entry.getServer_in();
                    serverOutNetlog += entry.getServer_out();
                }
            }
            if (localloopSession != null) {
                clientInTcpdump = localloopSession.getUpstreamPayloadLength();
                clientOutTcpdump = localloopSession.getDownstreamPayloadLength();
            }
            if (networkSession != null) {
                serverInTcpdump = networkSession.getDownstreamPayloadLength();
                serverOutTcpdump = networkSession.getUpstreamPayloadLength();
            }

            logger.debug("ClientIn extracted from netlogs for tcp sessions (loPort = " + loPort + " ,netPort = " + netPort + ") equals : " + clientInNetlog);
            logger.debug("ClientOut extracted from netlogs for tcp sessions (loPort = " + loPort + " ,netPort = " + netPort + ") equals : " + clientOutNetlog);
            logger.debug("ServerIn extracted from netlogs for tcp sessions (loPort = " + loPort + " ,netPort = " + netPort + ") equals : " + serverInNetlog);
            logger.debug("ServerOut extracted from netlogs for tcp sessions (loPort = " + loPort + " ,netPort = " + netPort + ") equals : " + serverOutNetlog);

            logger.debug("ClientIn calculated by tcpdump  for tcp sessions (loPort = " + loPort + " ,netPort = " + netPort + ") equals : " + clientInTcpdump);
            logger.debug("ClientOut calculated by tcpdump for tcp sessions (loPort = " + loPort + " ,netPort = " + netPort + ") equals : " + clientOutTcpdump);
            logger.debug("ServerIn calculated by tcpdump for tcp sessions (loPort = " + loPort + " ,netPort = " + netPort + ") equals : " + serverInTcpdump);
            logger.debug("ServerOut calculated by tcpdump for tcp sessions (loPort = " + loPort + " ,netPort = " + netPort + ") equals : " + serverOutTcpdump);

            assertTrue("ClientIn from tcpdump not equals clientIn from netlog. ", trafficCorresponds(clientInTcpdump, clientInNetlog));
            assertTrue("ClientOut from tcpdump not equals clientOut from netlog. ", trafficCorresponds(clientOutTcpdump, clientOutNetlog));
            assertTrue("ServerIn from tcpdump not equals serverIn from netlog. ", trafficCorresponds(serverInTcpdump, serverInNetlog));
            assertTrue("ServerOut from tcpdump not equals serverOut from netlog. ", trafficCorresponds(serverOutTcpdump, serverOutNetlog));

            if (loPort == -1) {
                throw new AssertionFailedError("Can't find loPort for : " + timeInfoTransaction);
            }
            if (!isHit && netPort == -1) {
                throw new AssertionFailedError("Can't find netPort for : " + timeInfoTransaction);
            }

        }
    }

    private boolean trafficCorresponds(long a, long b) {
        return (Math.abs(a - b) <= 1);
    }

    protected HttpSession getLocaloopSession(TimeInfoTransaction timeInfoTransaction) {
        List<HttpSession> sessions = tcpDump.getHttpSessions(
                timeInfoTransaction.getTimeStart(), timeInfoTransaction.getTimeEnd());
        for (HttpSession session : sessions) {
            if (session.getInterface() == Interface.LOOPBACK) {
                logger.info(session.toString());
                return session;
            }
        }
        return null;
    }

    protected HttpSession getNetworkSession(TimeInfoTransaction timeInfoTransaction) {
        List<HttpSession> sessions = tcpDump.getHttpSessions(timeInfoTransaction.getTimeStart(), timeInfoTransaction.getTimeEnd());
        for (HttpSession session : sessions) {
            if (session.getInterface() == Interface.NETWORK) {
                return session;
            }
        }
        return null;
    }

    private class GeneralCheck implements ExecuteCheck {

        private TimeInfoTransaction timeInfoTransaction;
        private String fieldName;
        private String controlValue;
        private String testValue;
        private String message;

        public GeneralCheck(TimeInfoTransaction timeInfoTransaction, String fieldName, String controlValue, String message) {
            this.timeInfoTransaction = timeInfoTransaction;
            this.fieldName = fieldName;
            this.controlValue = controlValue;
            this.message = message;
        }

        @Override
        public void executeCheck() {

            HttpSession localloopSession = getLocaloopSession(this.timeInfoTransaction);
            HttpSession networkSession = getNetworkSession(timeInfoTransaction);
            int netPort = -1;
            int loPort = -1;
            final String REG_EXP = "[\\w\\-]*/[\\w\\-]*/[\\w\\-]*/[\\w\\-]*";
            final Pattern localProtocolStack = Pattern.compile(REG_EXP, Pattern.CASE_INSENSITIVE);
            Matcher matcher;

            if (networkSession != null) {
                netPort = networkSession.getClientPort();
            }
            if (localloopSession != null) {
                loPort = localloopSession.getClientPort();
            }

            logger.debug("StartTime=" + timeInfoTransaction.getTimeStart() + " TimeEnd=" + timeInfoTransaction.getTimeEnd());
            logger.debug("LoPort=" + loPort + " NetPort=" + netPort);

            NetlogEntry entry = null;
            for (NetlogEntry entryInner : netlogTask.getLogEntries()) {
                logger.debug("Entry=" + entryInner);
                if (entryInner.getLoport() == loPort &&
                        entryInner.getClient_in() != 0 &&
                        entryInner.getClient_out() != 0) {
                    entry = entryInner;
                }
            }

            if (fieldName == null)
                throw new RuntimeException("Incorrect parameter.");
            if (entry != null) {
                Class clazz = NetlogEntry.class;
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.getName().contains("get") &&
                            method.getName().contains(fieldName)) {
                        try {
                            Object result = method.invoke(entry, null);
                            testValue = result.toString();
                            logger.debug("Expected value : " + controlValue);
                            logger.debug("Real Value = " + testValue);
                            break;
                        } catch (IllegalAccessException ignored) {

                        } catch (InvocationTargetException ignored) {

                        }
                    }
                }

                matcher = localProtocolStack.matcher(testValue);

                if (('4' == testValue.charAt(testValue.length() - 1) | '6' == testValue.charAt(testValue.length() - 1))
                        && matcher.matches())
                    assertEquals(message, controlValue, testValue.substring(0, testValue.length() - 1));
                else
                    assertEquals(message, controlValue, testValue);
            } else {
                throw new AssertionFailedError("Can't find netlog for TimeInfoObject : " + timeInfoTransaction + " LoPort = " +
                        loPort + " NetPort = " + netPort);
            }
        }
    }

    private class DataInRangeCheck implements ExecuteCheck {

        private TimeInfoTransaction timeInfoTransaction;
        private String fieldName;
        int minControlValue;
        int maxControlValue;
        private int testValue;
        private String message;

        public DataInRangeCheck(TimeInfoTransaction timeInfoTransaction, String fieldName, int minControlValue,
                                int maxControlValue, String message) {
            this.timeInfoTransaction = timeInfoTransaction;
            this.fieldName = fieldName;
            this.minControlValue = minControlValue;
            this.maxControlValue = maxControlValue;
            this.message = message;
        }

        public void executeCheck() {
            HttpSession localloopSession = getLocaloopSession(timeInfoTransaction);
            HttpSession networkSession = getNetworkSession(timeInfoTransaction);
            int netPort = -1;
            int loPort = -1;
            if (networkSession != null) {
                netPort = networkSession.getClientPort();
            }
            if (localloopSession != null) {
                loPort = localloopSession.getClientPort();
            }
            logger.debug("StartTime=" + timeInfoTransaction.getTimeStart() / 1000 + " TimeEnd=" + timeInfoTransaction.getTimeEnd() / 1000);
            logger.debug("LoPort=" + loPort + " NetPort=" + netPort);

            if (fieldName == null)
                throw new RuntimeException("Incorrect parameter.");
            NetlogEntry entry = null;
            for (NetlogEntry entryInner : netlogTask.getLogEntries()) {
                logger.debug("Entry=" + entryInner);
                if (entryInner.getLoport() == loPort &&
                        entryInner.getClient_in() != 0 &&
                        entryInner.getClient_out() != 0) {
                    entry = entryInner;
                }
            }
            if (entry != null) {
                Class clazz = NetlogEntry.class;
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.getName().contains("get") &&
                            method.getName().contains(fieldName)) {
                        try {
                            Object result = method.invoke(entry, null);
                            testValue = Integer.parseInt(result.toString());
                            System.out.println("TestValue = " + testValue);
                            break;
                        } catch (IllegalAccessException ignored) {

                        } catch (InvocationTargetException ignored) {

                        }
                    }
                }
                logger.debug("TestValue extracted from netlog = " + testValue);
                logger.debug("MinTestValue setted by test = " + minControlValue);
                logger.debug("MaxTestValue setted by test = " + maxControlValue);
                assertTrue(message, testValue <= maxControlValue);
                assertTrue(message, testValue >= minControlValue);
            } else if (networkSession != null && localloopSession != null) {
                throw new AssertionFailedError("Can't find netlog for TimeInfoObject : " + timeInfoTransaction + " LoPort = " + localloopSession.getClientPort()
                        + " NetPort = " + networkSession.getClientPort());
            } else {
                throw new AssertionFailedError("Can't find netlog for TimeInfoObject : " + timeInfoTransaction);
            }
        }
    }

    private class DnsCheck implements ExecuteCheck {
        private TimeInfoTransaction timeInfoTransaction;
        private String host;
        private String errorCode;
        private boolean loportZeroControl;
        private boolean netportZeroControl;
        private boolean cacheInZeroControl;
        private boolean cacheOutZeroControl;

        private int loPort;
        private int netPort;

        public DnsCheck(TimeInfoTransaction timeInfoTransaction, String host, boolean loportZeroControl, boolean netportZeroControl,
                        boolean cacheInZeroControl, boolean cacheOutZeroControl, String errorCode) {
            this.timeInfoTransaction = timeInfoTransaction;
            this.host = host;
            this.loportZeroControl = loportZeroControl;
            this.netportZeroControl = netportZeroControl;
            this.cacheInZeroControl = cacheInZeroControl;
            this.cacheOutZeroControl = cacheOutZeroControl;
            this.errorCode = errorCode;
        }

        @Override
        public void executeCheck() {
            logger.debug("StartTime=" + timeInfoTransaction.getTimeStart() + " TimeEnd=" + timeInfoTransaction.getTimeEnd());

            NetlogEntry entry = null;
            for (NetlogEntry entryInner : netlogTask.getLogEntries()) {
                logger.debug("EntryAll= " + entryInner);
                if (entryInner.getHost().equals(this.host) &&
                        entryInner.getTimestamp() <= this.timeInfoTransaction.getTimeEnd() &&
                        entryInner.getTimestamp() >= this.timeInfoTransaction.getTimeStart()) {
                    logger.debug("Entry= " + entryInner);
                    entry = entryInner;
                }
            }

            if (entry != null) {
                String errorCode = entry.getErrorCode();

                this.loPort = entry.getLoport();
                this.netPort = entry.getNetport();

                assertEquals("Incorrect ApplicationName value in netlog", "dns", entry.getApplicationName());
                assertEquals("Incorrect Operation type value in netlog", OperationType.proxy_dns, entry.getOpType());
                assertEquals("Incorrect LocalProtocolStack value in netlog", "-/-/dns/udp",
                        entry.getLocalProtocolStack().substring(0, entry.getLocalProtocolStack().length() - 1));

                logger.debug("Loport extracted from netlogs for dns sessions: " + this.loPort);
                logger.debug("Netport extracted from netlogs for dns sessions: " + this.netPort);

                if (loportZeroControl) {
                    assertTrue("Loport is not equals '0'. Loport: " + this.loPort, this.loPort == 0);
                } else {
                    assertTrue("Loport is equals '0', but it shouldn't.", this.loPort != 0);
                }
                if (netportZeroControl) {
                    assertTrue("Netport is not equals '0'. Netport: " + this.netPort, this.netPort == 0);
                } else {
                    assertTrue("Netport is equals '0', but it shouldn't.", this.netPort != 0);
                }

                if (cacheInZeroControl) {
                    assertTrue("CacheIn is not equals '0'. CacheIn: " + entry.getCache_in(), entry.getCache_in() == 0);
                } else {
                    assertTrue("CacheIn is equals '0', but it shouldn't.", entry.getCache_in() != 0);
                }
                if (cacheOutZeroControl) {
                    assertTrue("CacheOut is not equals '0'. CacheIn: " + entry.getCache_out(), entry.getCache_out() == 0);
                } else {
                    assertTrue("CacheOut is equals '0', but it shouldn't.", entry.getCache_out() != 0);
                }

                if (!(this.errorCode == null)) {
                    assertEquals("ErrorCode is not as expected. Expected: " + this.errorCode + ", but was: " + errorCode,
                            this.errorCode, errorCode);
                }
            } else {
                throw new AssertionFailedError("Can't find netlog for TimeInfoObject : " + timeInfoTransaction + " LoPort = " +
                        loPort + " NetPort = " + netPort);
            }
        }
    }

    private interface ExecuteCheck {
        public void executeCheck();
    }

    protected String resource;
    protected String uri;
    protected HttpRequest request1;
    protected HttpRequest request2;
    protected int requestId;
    protected int sleepTime;
    protected int size;
    protected Random random = new Random();
    protected int maxSize;
    protected TimeInfoTransaction timeInfoTransaction1;
    protected TimeInfoTransaction timeInfoTransaction2;
    protected static final String NETWORK_PROTOCOL_STACK_FIELD_NAME = "NetworkProtocolStack";
    protected static final String NETWORK_PROTOCOL_STACK_ERROR_MESSAGE = "Network Protocol Stack from netlog is incorrect : ";
    protected static final int TIME_NETLOG_STARTED_AWAITING = 10 * 1000;
    protected NetlogTask netlogTask;
    private Queue<ExecuteCheck> mChecksQueue = new ConcurrentLinkedQueue<ExecuteCheck>();

    private final String enginePath = "@asimov@failovers@restart@engine";
    private final String controllerPath = "@asimov@failovers@restart@controller";
    private final String dispatchersPath = "@asimov@failovers@restart@dispatchers";
    private String paramName = "enabled";
    private String paramValue = "false";


    public TimeInfoTransaction checkHit(HttpRequest request) {
        TimeInfoTransaction result = new TimeInfoTransaction();
        result.setTimeStart(System.currentTimeMillis());
        try {
            sendRequest2(request);
        } catch (Exception e) {
            logger.error("Error with send request : " + ExceptionUtils.getStackTrace(e));
        }
        result.setTimeEnd(System.currentTimeMillis());
        return result;
    }

    public TimeInfoTransaction checkMiss(HttpRequest request) {
        TimeInfoTransaction result = new TimeInfoTransaction();
        result.setTimeStart(System.currentTimeMillis());
        try {
            sendRequest2(request);
        } catch (Exception e) {
            logger.error("Error with send request : " + ExceptionUtils.getStackTrace(e));
        }
        result.setTimeEnd(System.currentTimeMillis());
        return result;
    }

    public TimeInfoTransaction checkMiss(String uri, int statusCode, int sleepTime) throws Throwable {

        HttpRequest request = createRequest().setUri(createTestResourceUri(uri + Integer.toString(statusCode)))
                .setMethod(HttpGet.METHOD_NAME).addHeaderField("X-OC-ResponseContent", "")
                .addHeaderField("X-OC-ResponseStatus", Integer.toString(statusCode)).getRequest();

        TimeInfoTransaction timeInfoTransaction = new TimeInfoTransaction();
        timeInfoTransaction.setTimeStart(System.currentTimeMillis());
        try {
            sendRequest2(request);
        } catch (Exception e) {
            logger.error("Error with send request : " + ExceptionUtils.getStackTrace(e));
        }
        timeInfoTransaction.setTimeEnd(System.currentTimeMillis());
        TestUtil.sleep(sleepTime);
        return timeInfoTransaction;
    }

    public TimeInfoTransaction checkMiss(String uri, int statusCode, boolean keepAlive, int sleepTime) throws Throwable {

        HttpRequest request = createRequest().setUri(createTestResourceUri(uri + Integer.toString(statusCode)))
                .setMethod(HttpGet.METHOD_NAME).addHeaderField("X-OC-ResponseContent", "")
                .addHeaderField("X-OC-ResponseStatus", Integer.toString(statusCode)).getRequest();

        TimeInfoTransaction timeInfoTransaction = new TimeInfoTransaction();
        timeInfoTransaction.setTimeStart(System.currentTimeMillis());
        try {
            sendRequest2(request, true, false);
        } catch (Exception e) {
            logger.error("Error with send request : " + ExceptionUtils.getStackTrace(e));
        }
        timeInfoTransaction.setTimeEnd(System.currentTimeMillis());
        TestUtil.sleep(sleepTime);
        return timeInfoTransaction;
    }

    public TimeInfoTransaction checkMissSleep(String uri, int responseSleep, int requestId) throws Throwable {

        HttpRequest request = createRequest().setUri(createTestResourceUri(uri + Integer.toString(responseSleep)))
                .setMethod(HttpGet.METHOD_NAME)
                .addHeaderField("X-OC-Sleep", Integer.toString(responseSleep)).getRequest();

        TimeInfoTransaction timeInfoTransaction = checkMiss(request);

        TestUtil.sleep(TIME_NETLOG_STARTED_AWAITING);
        return timeInfoTransaction;
    }

    public TimeInfoTransaction checkMiss(byte[] request) {
        TimeInfoTransaction result = new TimeInfoTransaction();
        result.setTimeStart(System.currentTimeMillis());
        try {
            sendRequest(request, AsimovTestCase.TEST_RESOURCE_HOST);
        } catch (Exception e) {
            logger.error("Error with send request : " + ExceptionUtils.getStackTrace(e));
        }
        result.setTimeEnd(System.currentTimeMillis());
        return result;
    }

    protected String getExpectedHeaderLength(TimeInfoTransaction timeInfoTransaction) {
        HttpSession localoopSession = getLocaloopSession(timeInfoTransaction);
        long expectedHeaderLength = -1;
        if (localoopSession != null) {
            expectedHeaderLength = localoopSession.getDownstreamPayloadLength() - 4;
        }
        return Long.toString(expectedHeaderLength);
    }

    protected List<TimeInfoTransaction> prepareTransactions(long period) {
        final String uri = "ga_asimov_it_proxy_future_version";
        final String expected = "HTTP/1.2 200 OK" + TFConstantsIF.CRLF + "Connection: close" + TFConstantsIF.CRLF +
                "Content-length: 4" + TFConstantsIF.CRLF + TFConstantsIF.CRLF + "body";

        final String encoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));
        final String request = "GET /" + uri + " HTTP/1.2" + TFConstantsIF.CRLF + "Connection: close" +
                TFConstantsIF.CRLF + "Host: " + AsimovTestCase.TEST_RESOURCE_HOST + TFConstantsIF.CRLF + "X-OC-Raw: " +
                encoded + TFConstantsIF.CRLF + TFConstantsIF.CRLF;

        ArrayList<TimeInfoTransaction> transactions = new ArrayList<TimeInfoTransaction>();

        for (int i = 0; i < 4; i++) {
            transactions.add(checkMiss(request.getBytes()));
            logSleeping(period);
        }
        return transactions;
    }

    protected void checkNetworkSession_test29(TimeInfoTransaction timeInfoTransaction) {
        HttpSession networkSession = getNetworkSession(timeInfoTransaction);
        int serverPort = -1;
        if (networkSession != null) {
            serverPort = networkSession.getServerPort();
        }
        if (serverPort == 7888 || serverPort == 41888) {
            addGeneralCheck(timeInfoTransaction1, NETWORK_PROTOCOL_STACK_FIELD_NAME, "-/http/tc/tcp", NETWORK_PROTOCOL_STACK_ERROR_MESSAGE);
        } else {
            addGeneralCheck(timeInfoTransaction1, NETWORK_PROTOCOL_STACK_FIELD_NAME, "-/-/http/tcp", NETWORK_PROTOCOL_STACK_ERROR_MESSAGE);
        }
    }

    protected void checkNetworkSession_test34(TimeInfoTransaction timeInfoTransaction) {
        HttpSession networkSession = getNetworkSession(timeInfoTransaction1);
        int serverPort = -1;
        if (networkSession != null) {
            serverPort = networkSession.getServerPort();
        }

        if (serverPort == TFConstantsIF.HTTP_TC_PORT_1) {
            addGeneralCheck(timeInfoTransaction1, NETWORK_PROTOCOL_STACK_FIELD_NAME, "-/http/tc/tcp", NETWORK_PROTOCOL_STACK_ERROR_MESSAGE);
        } else {
            addGeneralCheck(timeInfoTransaction1, NETWORK_PROTOCOL_STACK_FIELD_NAME, "-/-/http/tcp", NETWORK_PROTOCOL_STACK_ERROR_MESSAGE);
        }
    }

    public void addGeneralCheck(TimeInfoTransaction timeInfoTransaction, String fieldName, String controlValue, String message) {
        mChecksQueue.add(new GeneralCheck(timeInfoTransaction, fieldName, controlValue, message));
    }

    public void addDataCheck(TimeInfoTransaction timeInfoTransaction) {
        mChecksQueue.add(new DataCheck(timeInfoTransaction));
    }

    public void addDataCheck(TimeInfoTransaction timeInfoTransaction, boolean isHit) {
        mChecksQueue.add(new DataCheck(timeInfoTransaction, isHit));
    }

    public void addDnsCheck(TimeInfoTransaction timeInfoTransaction, String host, boolean loportZeroControl, boolean netportZeroControl,
                            boolean cacheInZeroControl, boolean cacheOutZeroControl, String errorCode) {
        mChecksQueue.add(new DnsCheck(timeInfoTransaction, host, loportZeroControl, netportZeroControl,
                cacheInZeroControl, cacheOutZeroControl, errorCode));
    }

    public void addDataInrangeCheck(TimeInfoTransaction timeInfoTransaction, String fieldName,
                                    int minControlValue, int maxControlValue, String message) {
        mChecksQueue.add(new DataInRangeCheck(timeInfoTransaction, fieldName, minControlValue, maxControlValue, message));
    }

    public HttpRequest createBasicNetLogRequest(String uri, boolean ssl, int port) {
        return createRequest().setMethod(HttpGet.METHOD_NAME).setUri(createTestResourceUri(uri, ssl, port)).getRequest();
    }

    public HttpRequest createNetLogRequest(String uri) {
        return createRequest().setMethod(HttpGet.METHOD_NAME).setUri(uri).getRequest();
    }

    public HttpRequest createNetLogRequestWithRemoveContentLength(String uri) {
        return createRequest().setUri(createTestResourceUri(uri))
                .addHeaderField("X-OC-RemoveContentLength", "true")
                .setMethod(HttpGet.METHOD_NAME).getRequest();
    }

    public HttpRequest createNetLogRequest(String uri, int size, String fillWithLetter) {
        return createRequest().setUri(createTestResourceUri(uri))
                .setMethod(HttpGet.METHOD_NAME).addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-AddHeader_Last-Modified", "-86400")
                .addHeaderField("X-OC-AddHeader_Date", "GMT")
                .addHeaderField("X-OC-ResponseContentSize", size + "," + fillWithLetter).getRequest();
    }

    protected void switchRestartFailover(boolean enabled) throws Exception {
        if (!enabled) {
            try {
                PMSUtil.addPolicies(new Policy[]{
                        new Policy(paramName, paramValue, enginePath, true),
                        new Policy(paramName, paramValue, controllerPath, true),
                        new Policy(paramName, paramValue, dispatchersPath, true)});
            } catch (Throwable e) {
                logger.error("Exception while switching Reset Failover: " + ExceptionUtils.getStackTrace(e));
            }
            TestUtil.sleep(TFConstantsIF.WAIT_FOR_POLICY_UPDATE * 2);
        } else {
            PMSUtil.cleanPaths(new String[]{enginePath, controllerPath, dispatchersPath});
        }
    }
}