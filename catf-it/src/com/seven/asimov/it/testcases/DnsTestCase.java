package com.seven.asimov.it.testcases;


import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.base.TestCaseThread;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.*;
import com.seven.asimov.it.utils.date.DateUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.sessionBasicTasks.ProcessingCLQTask;
import com.seven.asimov.it.utils.logcat.wrappers.ProcessingCLQWrapper;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import com.seven.asimov.it.utils.tcpdump.DnsSession;
import com.seven.asimov.it.utils.tcpdump.TcpDumpUtil;
import junit.framework.Assert;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.*;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DnsTestCase extends TcpDumpTestCase {
    private static final Logger logger = LoggerFactory.getLogger(DnsTestCase.class.getSimpleName());

    protected static final int RADIO_KEEPER_DELAY_MS = 5 * 1000;
    protected static final String DNS_REST_PROPERTY_PATH = "@asimov@dns";
    protected static final String AGGRESSIVENESS_REST_PROPERTY_NAME = "expiration_aggressiveness";
    protected static final String TTL_REST_PROPERTY_NAME = "default_cache_ttl";
    protected static final int DNS_TTL = 300;

    private static final String IPV4_DNS = "8.8.8.8";
    private static final String IPV6_DNS = "2001:4860:4860::8888";

    protected static final String[] hosts = new String[]{
            "www.asciitable.com",
            "www.indiangeek.net",
            "www.onlineconversion.com",
            "www.scala-lang.org",
            "www.lingvo.ua",
            "www.codecommit.com",
            "maven.apache.org",
            "www.slf4j.org",
            "stackoverflow.com",
            "sourceforge.net"
    };

    protected String getDnsAddr() {
        if (TFConstantsIF.IP_VERSION == TFConstantsIF.IP6_VERSION)
            return IPV6_DNS;

        return IPV4_DNS;
    }

    public enum RadioState {
        RADIO_UP, RADIO_DOWN
    }

    public enum ScreenState {
        SCREEN_ON, SCREEN_OFF
    }

    protected boolean wasHostResolvedFromNetwork(TcpDumpUtil tcpDump, String host) {
        boolean result = false;
        Set<String> hosts = tcpDump.getResolvedHosts(new String[]{host});
        for (String h : hosts) {

            if (h.contains(host)) {
                result = true;
            }
        }

        return result;
    }

    /**
     * This method removes OC caching DB and restarts OCC and com.seven.asimov. To clean all cached DNS records.
     *
     * @throws Exception
     */
    protected void cleanOCDnsCache() throws Exception {
        final String occProcess = "occ";
        final String asimovProcess = "com.seven.asimov";
        Integer occPID;
        Integer asimovPID;
        Map<String, Integer> ocProcesses = OCUtil.getOcProcesses(false);
        occPID = ocProcesses.get(occProcess);
        asimovPID = ocProcesses.get(asimovProcess);
        assertTrue("prepareDnsTestRun can't find " + occProcess + " process", occPID != null);
        assertTrue("prepareDnsTestRun can't find " + asimovProcess + " process", asimovPID != null);

        List<String> command = new ArrayList<String>();
        command.add("rm /data/misc/openchannel/oc_engine.db");
        ShellUtil.execWithCompleteResult(command, true);

        command.clear();
        command.add("kill " + occPID);
        ShellUtil.execWithCompleteResult(command, true);

        command.clear();
        command.add("kill " + asimovPID);
        ShellUtil.execWithCompleteResult(command, true);

        TestUtil.sleep(30 * 1000);   //Wait for OC to recover and recreate DB; Maybe need to check if all processes UP?
        for (int i = 0; i < 3; i++) {
            ocProcesses = OCUtil.getOcProcesses(false);
            occPID = ocProcesses.get(occProcess);
            asimovPID = ocProcesses.get(asimovProcess);
            if ((occPID != null) && (asimovPID != null))
                break;

            TestUtil.sleep(15 * 1000);
        }
        occPID = ocProcesses.get(occProcess);
        asimovPID = ocProcesses.get(asimovProcess);
        assertTrue("prepareDnsTestRun can't find " + occProcess + " process  after db removal", occPID != null);
        assertTrue("prepareDnsTestRun can't find " + asimovProcess + " process  after db removal", asimovPID != null);
    }

    private final String enginePath = "@asimov@failovers@restart@engine";
    private final String controllerPath = "@asimov@failovers@restart@controller";
    private final String dispatchersPath = "@asimov@failovers@restart@dispatchers";
    private String paramName = "enabled";
    private String paramValue = "false";

    protected void switchRestartFailover(boolean enabled) throws Exception {
        if (!enabled) {
            PMSUtil.addPoliciesWithCheck(new Policy[]{
                    new Policy(paramName, paramValue, enginePath, true),
                    new Policy(paramName, paramValue, controllerPath, true),
                    new Policy(paramName, paramValue, dispatchersPath, true)});
        } else {
            PMSUtil.cleanPaths(new String[]{enginePath, controllerPath, dispatchersPath});
        }

    }

    protected void pingHost(String host) {
        try {
            Process ping = Runtime.getRuntime().exec("ping -s1 -c1 -W3 -n " + host);
            ping.waitFor();
        } catch (Exception e) {
            logger.error("pingHost Exception:\n" + ExceptionUtils.getStackTrace(e));
        }
    }

    protected Thread createPingerThread() throws UnknownHostException {
        return new Thread() {
            private String pingableHost;

            {
                InetAddress[] hostToPing = InetAddress.getAllByName(PMSUtil.getPmsServerIp());
                assertTrue("Failed to resolve IPv4 address of PMS server to ping with radio keeper thread.",
                        hostToPing.length != 0);
                for (InetAddress addr : hostToPing) {
                    if (addr instanceof Inet4Address) {
                        pingableHost = addr.getHostAddress();
                    }
                }
            }

            @Override
            public void run() {
                while (true) {
                    long startTime = System.currentTimeMillis();
                    pingHost(pingableHost);
                    long sleepMS = RADIO_KEEPER_DELAY_MS > System.currentTimeMillis() - startTime ?
                            RADIO_KEEPER_DELAY_MS - (System.currentTimeMillis() - startTime) : 0;
                    TestUtil.sleep(sleepMS);
                }
            }
        };
    }


    protected void resolveHostByCustomDNS(String host, String server) throws IOException {
        SimpleResolver resolver = new SimpleResolver(server);
        resolver.setTimeout(180);
        Message msg = resolver.send(DnsUtil.createDnsQuery(host, Type.A));
        logger.info("resolveHostByAndroidDNS: received message:" + msg.toString());
    }

    /**
     * Performs Dns expiration test.
     *
     * @param host                Host to include in dns request.
     * @param aggressivenessLevel Level of aggressiveness to set in policy.
     * @param screenStates        Array, containing screen state to set at each step of test.
     * @param radioStates         Array, containing radio state to set at each step of test.
     * @param expectedFromNetwork Array, containing expected dns response sources: cache or network.
     * @param timeShifts          Array, containing time shifts to apply before each test iteration.
     * @throws Throwable
     */
    protected void checkAggressiveDnsExpiration(String host, int aggressivenessLevel, ScreenState[] screenStates,
                                                RadioState[] radioStates, boolean[] expectedFromNetwork, int[] timeShifts) throws Throwable {
        final TcpDumpUtil tcpDump = TcpDumpUtil.getInstance(getContext());
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final ProcessingCLQTask pclqTask = new ProcessingCLQTask(null, host, null, null);
        LogcatUtil logcat = null;

        final int littleDELAY = 10 * 1000;
        int timeTotalShift = 0;
        ScreenState lastScreenState = null;
        RadioState lastRadioState = null;
        Thread pingerThread = createPingerThread();
        PMSUtil.addPoliciesWithCheck(new Policy[]{
                new Policy(AGGRESSIVENESS_REST_PROPERTY_NAME, Integer.toString(aggressivenessLevel), DNS_REST_PROPERTY_PATH, true),
                new Policy(TTL_REST_PROPERTY_NAME, Integer.toString(DNS_TTL), DNS_REST_PROPERTY_PATH, true)});

        try {
            for (int i = 0; i < screenStates.length; i++) {
                if (screenStates[i] != lastScreenState) {
                    lastScreenState = screenStates[i];
                    if (screenStates[i] == ScreenState.SCREEN_ON)
                        ScreenUtils.screenOn();
                    else
                        ScreenUtils.screenOff();
                }
                //TestUtils.sleep(littleDELAY);

                if (radioStates[i] != lastRadioState) {
                    lastRadioState = radioStates[i];
                    if (radioStates[i] == RadioState.RADIO_UP) {
                        IpTablesUtil.banAllAppsButOC(false, getContext());
                        pingerThread = createPingerThread();
                        executorService.submit(pingerThread);
                    } else {
                        pingerThread.interrupt();
                        IpTablesUtil.banAllAppsButOC(true, getContext());
                    }

                    //TestUtils.sleep(littleDELAY);
                }

                if (timeShifts[i] != 0) {
                    DateUtil.moveTime(timeShifts[i]);
                    timeTotalShift += timeShifts[i];
                }

                TestUtil.sleep(littleDELAY);

                pclqTask.reset();
                logcat = new LogcatUtil(getContext(), pclqTask);
                logcat.start();
                tcpDump.start();
                TestUtil.sleep(littleDELAY / 2);
                resolveHostByCustomDNS(host, getDnsAddr());
                TestUtil.sleep(littleDELAY / 2);
                tcpDump.stop();
                logcat.stop();

                assertTrue("Dns request should be intercepted by OC", !pclqTask.getLogEntries().isEmpty());
                assertTrue("DNS should be resolved from " + (expectedFromNetwork[i] ? "network" : "cache") + " for request #" + i,
                        expectedFromNetwork[i] == wasHostResolvedFromNetwork(tcpDump, host));

            }
        } finally {
            tcpDump.stop();
            if (logcat != null)
                logcat.stop();
            if (timeTotalShift != 0)
                DateUtil.moveTime(-timeTotalShift);
            ScreenUtils.screenOn();
            pingerThread.interrupt();
            PMSUtil.cleanPaths(new String[]{DNS_REST_PROPERTY_PATH});
            IpTablesUtil.banAllAppsButOC(false, getContext());
            TestUtil.sleep(littleDELAY / 2);
        }
    }

    //DnsProxyGATests
    private static final String HOST_ADDRESS_1 = "موقع.وزارة-الأتصالات.مصر.";
    private static final String HOST_ADDRESS_2 = "êxâmplë.host.com.";

    protected enum DNSRR {
        CacheHitAndExpiration("habrahabr.ru.dnsproxytest.pp.ua.", Type.A) {
            @Override
            Message generateResponse(Message request) throws Exception {
                return generateResponseCommon(new ARecord(request.getQuestion().getName(),
                        DClass.IN,
                        86400,
                        InetAddress.getByName("127.0.0.4")));
            }
        },
        NonLatin1(HOST_ADDRESS_1, Type.A) {
            @Override
            Message generateResponse(Message request) throws Exception {
                return generateResponseCommon(new ARecord(request.getQuestion().getName(),
                        DClass.IN,
                        86400,
                        InetAddress.getByName("127.0.0.2")));
            }
        },
        NonLatin2(HOST_ADDRESS_2, Type.A) {
            @Override
            Message generateResponse(Message request) throws Exception {
                return generateResponseCommon(new ARecord(request.getQuestion().getName(),
                        DClass.IN,
                        86400,
                        InetAddress.getByName("127.0.0.3")));
            }
        },
        ATest("dnsproxytest.pp.ua.", Type.A) {
            @Override
            Message generateResponse(Message request) throws Exception {
                return generateResponseCommon(new ARecord(request.getQuestion().getName(),
                        DClass.IN,
                        86400,
                        InetAddress.getByName("127.0.0.1")));
            }
        },
        AAAATest("dnsproxytest.pp.ua.", Type.AAAA) {
            @Override
            Message generateResponse(Message request) throws Exception {
                return generateResponseCommon(new AAAARecord(request.getQuestion().getName(),
                        DClass.IN,
                        86400,
                        InetAddress.getByName("2001:1111:2222:3333:4444:5555:6666:7777")));
            }
        },
        NSTest("ns.dnsproxytest.pp.ua.", Type.NS) {
            @Override
            Message generateResponse(Message request) throws Exception {
                Message response = Message.newQuery(Record.newRecord(request.getQuestion().getName(),
                        request.getQuestion().getType(),
                        DClass.IN));
                response.getHeader().setID(request.getHeader().getID());
                response.addRecord(new NSRecord(request.getQuestion().getName(),
                        DClass.IN,
                        86400,
                        Name.fromString("ns.test.")), Section.ANSWER);
                response.getHeader().setFlag(Flags.QR);

                response.toWire();
                return response;
            }
        },
        NSAuthorityTest("ns.dnsproxytest.pp.ua.", Type.NS) {//Should be initialized after NSTest, requires ID from NSTest

            @Override
            Message generateResponse(Message request) throws Exception {
                Message response = Message.newQuery(Record.newRecord(request.getQuestion().getName(),
                        request.getQuestion().getType(),
                        DClass.IN));
                response.getHeader().setID(request.getHeader().getID());
                response.addRecord(new NSRecord(request.getQuestion().getName(),
                        DClass.IN,
                        86400,
                        Name.fromString("ns.test.")), Section.AUTHORITY);
                response.getHeader().setFlag(Flags.QR);
                response.getHeader().setID(DNSRR.NSTest.getResponse().getHeader().getID());

                response.toWire();
                return response;
            }
        },
        CNAMETest("cname.dnsproxytest.pp.ua.", Type.CNAME) {
            @Override
            Message generateResponse(Message request) throws Exception {
                return generateResponseCommon(new CNAMERecord(request.getQuestion().getName(),
                        DClass.IN,
                        86400,
                        Name.fromString("cname.test.")));
            }
        },
        SOATest("dnsproxytest.pp.ua.", Type.SOA) {
            @Override
            Message generateResponse(Message request) throws Exception {
                return generateResponseCommon(new SOARecord(request.getQuestion().getName(),
                        DClass.IN,
                        86400, Name.fromString("ns1.he.net."),
                        Name.fromString("hostmaster.he.net."), 2013110104, 10800, 1800, 604800,
                        86400));
            }
        },
        PTRATest("5.0.0.127.in-addr.arpa.dnsproxytest.pp.ua.", Type.A) {
            @Override
            Message generateResponse(Message request) throws Exception {
                return generateResponseCommon(new ARecord(request.getQuestion().getName(),
                        DClass.IN,
                        86400,
                        InetAddress.getByName("127.0.0.5")));
            }
        },
        PTR4Test("1.0.0.127.in-addr.arpa.dnsproxytest.pp.ua.", Type.PTR) {
            @Override
            Message generateResponse(Message request) throws Exception {
                return generateResponseCommon(new PTRRecord(request.getQuestion().getName(),
                        DClass.IN,
                        86400,
                        Name.fromString("ptr4.test.")));
            }
        },
        PTR6Test("7.7.7.7.6.6.6.6.5.5.5.5.4.4.4.4.3.3.3.3.2.2.2.2.1.1.1.0.1.0.0.2.ip6.arpa.dnsproxytest.pp.ua.", Type.PTR) {
            @Override
            Message generateResponse(Message request) throws Exception {
                return generateResponseCommon(new PTRRecord(request.getQuestion().getName(),
                        DClass.IN,
                        86400,
                        Name.fromString("ptr6.test.")));
            }
        },
        MXTest("dnsproxytest.pp.ua.", Type.MX) {
            @Override
            Message generateResponse(Message request) throws Exception {
                return generateResponseCommon(new MXRecord(request.getQuestion().getName(),
                        DClass.IN,
                        86400,
                        1,
                        Name.fromString("mx.test.")));
            }
        },
        TXTTest("dnsproxytest.pp.ua.", Type.TXT) {
            @Override
            Message generateResponse(Message request) throws Exception {
                List<String> txtData = new ArrayList<String>();
                txtData.add("Hello World!");

                return generateResponseCommon(new TXTRecord(request.getQuestion().getName(),
                        DClass.IN,
                        86400,
                        txtData));
            }
        },
        SRVTest("_srv._tcp.dnsproxytest.pp.ua.", Type.SRV) {
            @Override
            Message generateResponse(Message request) throws Exception {
                return generateResponseCommon(new SRVRecord(request.getQuestion().getName(),
                        DClass.IN,
                        86400,
                        1,
                        2,
                        80,
                        Name.fromString("srv.test.")));
            }
        },
        ANOQuestionRecord("", Type.A) {
            @Override
            Message generateRequest(String param, int type) {
                return new Message();
            }

            @Override
            Message generateResponse(Message request) throws Exception {
                Message resp = new Message();
                resp.getHeader().setID(request.getHeader().getID());
                resp.getHeader().setFlag(Flags.QR);
                return resp;
            }
        },
        Multi1Record("multi1.dnsproxytest.pp.ua.", Type.A) {
            @Override
            Message generateResponse(Message request) throws Exception {
                return generateResponseCommon(new ARecord(request.getQuestion().getName(),
                        DClass.IN,
                        86400,
                        InetAddress.getByName("127.0.127.1")));
            }
        },
        Multi2Record("multi2.dnsproxytest.pp.ua.", Type.A) {
            @Override
            Message generateResponse(Message request) throws Exception {
                return generateResponseCommon(new ARecord(request.getQuestion().getName(),
                        DClass.IN,
                        86400,
                        InetAddress.getByName("127.0.127.2")));
            }
        },
        Multi3Record("multi3.dnsproxytest.pp.ua.", Type.A) {
            @Override
            Message generateResponse(Message request) throws Exception {
                return generateResponseCommon(new ARecord(request.getQuestion().getName(),
                        DClass.IN,
                        86400,
                        InetAddress.getByName("127.0.127.3")));
            }
        },
        Multi4Record("multi4.dnsproxytest.pp.ua.", Type.A) {
            @Override
            Message generateResponse(Message request) throws Exception {
                return generateResponseCommon(new ARecord(request.getQuestion().getName(),
                        DClass.IN,
                        86400,
                        InetAddress.getByName("127.0.127.4")));
            }
        },
        Multi5Record("multi5.dnsproxytest.pp.ua.", Type.A) {
            @Override
            Message generateResponse(Message request) throws Exception {
                return generateResponseCommon(new ARecord(request.getQuestion().getName(),
                        DClass.IN,
                        86400,
                        InetAddress.getByName("127.0.127.5")));
            }
        };

        DNSRR(String param, int type) {
            try {
                request = generateRequest(param, type);
                response = generateResponse(request);
            } catch (Exception e) {
                Assert.fail("Exception while initializing DNSRR enum:\n" + e.toString());
            }
        }

        Message request, response;

        Message generateRequest(String param, int type) {
            return generateRequestCommon(param, type);
        }

        Message generateResponse(Message request) throws Exception {
            return null;
        }

        public Message getRequest() {
            return request;
        }

        public Message getResponse() {
            return response;
        }

        protected Message generateRequestCommon(String param, int type) {
            try {
                Name n = Name.fromString(param);
                Record r = Record.newRecord(n, type, DClass.IN);
                Message request = Message.newQuery(r);
                request.toWire();
                return request;
            } catch (Exception e) {
                e.printStackTrace();
            }
            ;
            return null;
        }

        protected Message generateResponseCommon(Record record) {
            Message response = Message.newQuery(Record.newRecord(request.getQuestion().getName(),
                    request.getQuestion().getType(),
                    DClass.IN));
            response.getHeader().setID(request.getHeader().getID());
            response.addRecord(record, Section.ANSWER);
            response.getHeader().setFlag(Flags.QR);
            response.getHeader().setFlag(Flags.AA);
            response.toWire();
            return response;
        }
    }

    protected String removeDnsTrailingDot(String hostname) {
        int pointIndex = 0;

        if ((pointIndex = hostname.lastIndexOf(((int) '.'))) > 0) {
            return hostname.substring(0, pointIndex);
        }
        return hostname;
    }

    protected static final int DNS_CLIENT_PORT = 6789;
    protected static final int READ_TIMEOUT = 180000;
    // protected static final String INET_DNS[]={"ns5.he.net","ns1.he.net","ns4.he.net","ns3.he.net","ns2.he.net"};
    protected static String IPv4_NS = "ns1.he.net";
    protected static String IPv6_NS = "ns5.he.net";

    Message sendRequestMessage(Message message, TcpDumpUtil tcpDump) throws Exception {
        final String DNS_SERVER = (TFConstantsIF.IP_VERSION == TFConstantsIF.IP4_VERSION) ? IPv4_NS : IPv6_NS;

        Message response = null;

        try {
            SimpleResolver resolver = new SimpleResolver(DNS_SERVER);
            resolver.setTimeout(180);
            tcpDump.start();

            TestUtil.sleep(5 * 1000);
            response = resolver.send(message);
            TestUtil.sleep(5 * 1000);


        } catch (SocketTimeoutException te) {
            logger.info("Request to server " + DNS_SERVER + " timed out.");
        } catch (Exception e) {
            Assert.fail("Failed in doDnsCachingTest with exception:\n" + e.toString());
        } finally {
            tcpDump.stop();
        }

        return response;
    }

    /**
     * Performs DNS record caching test. Sends request and checks received response against given response. Checks twice to test caching.
     *
     * @param rr DNSRR (DNS request response) to perform test on.
     * @throws Exception
     */
    protected void doDnsCachingTest(DNSRR rr) throws Exception {
        final TcpDumpUtil tcpDump = TcpDumpUtil.getInstance(getContext());
        String host = removeDnsTrailingDot(rr.request.getSectionArray(Section.QUESTION)[0].getName().toString());
        final ProcessingCLQTask pclqTask = new ProcessingCLQTask(null, host, null, null);
        LogcatUtil logcat = new LogcatUtil(getContext(), pclqTask);
        try {
            logcat.start();
            Message response = sendRequestMessage(rr.request, tcpDump);
            logcat.stop();

            assertNotNull("Not received response from DNS Server", response);
            logger.info("receivedMsg=" + response.toString());
            assertTrue("Expected dns request to be intercepted by OC", !pclqTask.getLogEntries().isEmpty());
            assertTrue("DNS should be resolved from network)", wasHostResolvedFromNetwork(tcpDump, host));
            assertTrue("Expected response:\n" + rr.response.toString() + "\n but received:\n" + response.toString(),
                    rr.response.toString().equals(response.toString()));
            pclqTask.reset();
            logcat = new LogcatUtil(getContext(), pclqTask);
            logcat.start();
            response = sendRequestMessage(rr.request, tcpDump);
            logcat.stop();

            assertNotNull("Not received response2", response);
            logger.info("receivedMsg2=" + response.toString());
            assertTrue("Expected dns request to be intercepted by OC", !pclqTask.getLogEntries().isEmpty());
            assertTrue("DNS should be resolved from cache)", !wasHostResolvedFromNetwork(tcpDump, host));
            assertTrue("Expected response:\n" + rr.response.toString() + "\n but received:\n" + response.toString(),
                    rr.response.toString().equals(response.toString()));
        } finally {
            logcat.stop();
        }
    }

    /**
     * Performs test form multiple dns requests. Sends each request twice to test caching.
     *
     * @param records   DNSRR records to test.
     * @param shouldHit Tells if requests should be resolved from cache or of network.
     * @throws Exception
     */
    protected void doDnsMultipleQueriesWithCheck(DNSRR[] records, boolean shouldHit) throws Exception {
        final TcpDumpUtil tcpDump = TcpDumpUtil.getInstance(getContext());
        final String DNS_SERVER = (TFConstantsIF.IP_VERSION == TFConstantsIF.IP4_VERSION) ? IPv4_NS : IPv6_NS;
        final ProcessingCLQTask pclqTask = new ProcessingCLQTask(null, null, null, null);
        LogcatUtil logcat = new LogcatUtil(getContext(), pclqTask);
        try {
            List<String> requestHosts = new ArrayList();

            for (DNSRR rr : records) {
                requestHosts.add(removeDnsTrailingDot(rr.request.getSectionArray(Section.QUESTION)[0].getName().toString()));
            }

            tcpDump.start();
            logcat.start();
            TestUtil.sleep(5 * 1000);
            executeMultipleDnsQueries(records, DNS_SERVER);
            TestUtil.sleep(5 * 1000);
            logcat.stop();
            tcpDump.stop();

            List<String> resolvedHosts = new ArrayList<String>();
            for (ProcessingCLQWrapper wrapper : pclqTask.getLogEntries()) {
                resolvedHosts.add(wrapper.getHost());
            }

            for (String host : requestHosts) {
                assertTrue("DNS request for host:'" + host + "' should be resolved from " + (shouldHit ? "cache" : "network"),
                        shouldHit != wasHostResolvedFromNetwork(tcpDump, host));
            }
            requestHosts.removeAll(resolvedHosts);
            assertTrue("Not all DNS requests were intercepted by OC\n" +
                    "These requests were not intercepted:" +
                    requestHosts.toString(), requestHosts.isEmpty());
        } finally {
            logcat.stop();
            tcpDump.stop();
        }
    }

    /**
     * Performs test form multiple dns requests.
     *
     * @param records    DNSRR records to test.
     * @param DNS_SERVER Dns server to send requests to.
     * @throws Exception
     */
    protected void executeMultipleDnsQueries(DNSRR records[], final String DNS_SERVER) throws Exception {
        class Requester implements Callable<Message> {
            private DNSRR record;
            private Message response;

            public Message getResponse() {
                return response;
            }

            public Requester(DNSRR record) {
                this.record = record;
            }

            @Override
            public Message call() {
                try {
                    SimpleResolver resolver = new SimpleResolver(DNS_SERVER);
                    resolver.setTimeout(180);
                    response = resolver.send(record.getRequest());
                    assertNotNull("Not received response from DNS Server", response);
                    logger.info("receivedMsg=" + response.toString());
                    assertTrue("Expected response:\n" + record.response.toString() + "\n but received:\n" + response.toString(),
                            record.response.toString().equals(response.toString()));
                } catch (Exception e) {
                    logger.info(ExceptionUtils.getStackTrace(e));
                }
                return response;
            }
        }
        List<Requester> requesters = new ArrayList<Requester>();
        for (DNSRR rr : records) {
            requesters.add(new Requester(rr));
        }
        ExecutorService service = Executors.newFixedThreadPool(requesters.size());

        service.invokeAll(requesters);
        service.shutdown();
        service.awaitTermination(200, TimeUnit.SECONDS);
    }

    /**
     * Performs DNS NS record caching test. Sends request and checks received response against given response.
     * Checks twice to test caching. Compares received response against two different possible responses.
     * Sometimes server answers with RESPONSE block and sometimes with AUTHORITY RESPONSE.
     *
     * @throws Exception
     */
    protected void doNSRecordCachingTest() throws Exception {
        final TcpDumpUtil tcpDump = TcpDumpUtil.getInstance(getContext());
        String host = removeDnsTrailingDot(DNSRR.NSTest.request.getSectionArray(Section.QUESTION)[0].getName().toString());
        final ProcessingCLQTask pclqTask = new ProcessingCLQTask(null, host, null, null);
        LogcatUtil logcat = new LogcatUtil(getContext(), pclqTask);
        try {
            logcat.start();
            Message response = sendRequestMessage(DNSRR.NSTest.request, tcpDump);
            logcat.stop();

            assertNotNull("Not received response from DNS Server", response);
            logger.info("receivedMsg=" + response.toString());
            assertTrue("Expected dns request to be intercepted by OC", !pclqTask.getLogEntries().isEmpty());
            assertTrue("DNS should be resolved from network)", wasHostResolvedFromNetwork(tcpDump, host));

            assertTrue("Expected response:\n" + DNSRR.NSTest.response.toString() + "\nor:\n" +
                    DNSRR.NSAuthorityTest.response.toString() + "\n but received:\n" + response.toString(),
                    DNSRR.NSTest.response.toString().equals(response.toString()) ||
                            DNSRR.NSAuthorityTest.response.toString().equals(response.toString()));

            pclqTask.reset();
            logcat = new LogcatUtil(getContext(), pclqTask);
            logcat.start();
            response = sendRequestMessage(DNSRR.NSTest.request, tcpDump);
            logcat.stop();

            assertNotNull("Not received response2", response);
            logger.info("receivedMsg2=" + response.toString());
            assertTrue("Expected dns request to be intercepted by OC", !pclqTask.getLogEntries().isEmpty());
            assertTrue("DNS should be resolved from cache)", !wasHostResolvedFromNetwork(tcpDump, host));
            assertTrue("Expected response:\n" + DNSRR.NSTest.response.toString() + "\nor:\n" +
                    DNSRR.NSAuthorityTest.response.toString() + "\n but received:\n" + response.toString(),
                    DNSRR.NSTest.response.toString().equals(response.toString()) ||
                            DNSRR.NSAuthorityTest.response.toString().equals(response.toString()));
        } finally {
            logcat.stop();
        }
    }

    //ExpirationTests

    public void runDnsTestWithParameters(final String uri,
                                         final int[] intervals,
                                         final int[] timeShifts,
                                         final String[] expectedResults)
            throws InterruptedException, IOException, IllegalAccessException, InstantiationException {
        runDnsTestWithParameters(uri, intervals, timeShifts, expectedResults, null);
    }

    /**
     * Runs a set of requests, defined with following rules. Only DNS checks (for miss/hit) are permitted.
     *
     * @param uri             - define URI to use.
     * @param intervals       - intervals between requests
     * @param timeShifts      - defines, if some time shifting is needet (+/- hours)
     * @param expectedResults - What results are expected for each request.
     * @param customThreads   - allows running several additional threads.
     * @throws InterruptedException
     * @throws IOException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public void runDnsTestWithParameters(final String uri,
                                         final int[] intervals,
                                         final int[] timeShifts,
                                         final String[] expectedResults,
                                         final List<TestCaseThread> customThreads)
            throws InterruptedException, IOException, IllegalAccessException, InstantiationException {
        TestCaseThread tct = new TestCaseThread() {
            @Override
            public void run() throws Throwable {
                runMainThread(uri, intervals, timeShifts, expectedResults);
            }
        };
        try {
            if (customThreads != null) {
                customThreads.add(tct);

                executeThreads(ThreadStopMode.INTERRUPT, 300000, customThreads);

            } else {
                executeThreads(ThreadStopMode.INTERRUPT_SOFTLY, 300000, tct);
            }
        } catch (Throwable throwable) {
            logger.info(ExceptionUtils.getStackTrace(throwable));
        }
    }


    @Deprecated
    protected void runMainThread(
            final String uri,
            final int[] intervals,
            final int[] timeShifts,
            final String[] expectedResults,
            final String host
    ) throws InterruptedException, IOException, IllegalAccessException, InstantiationException {

        long start, end;
        long totalTimeShift = 0;
        TcpDumpUtil tcpDump = null;
        List<HttpRequest> requests = new ArrayList<HttpRequest>();

        try {

            //Make all cache entry expired by setting clock to +24h
            DateUtil.moveTime(48 * DateUtil.HOURS);
            totalTimeShift += 48 * DateUtil.HOURS;
            Thread.sleep(30000);

            for (int interval : intervals) {
                requests.add(createRequest()
                        .setUri(uri)
                        .setMethod("GET")
                        .addHeaderField("X-OC-ContentEncoding", "identity")
                        .getRequest());
            }

            tcpDump = TcpDumpUtil.getInstance(getContext());
            start = System.currentTimeMillis();
            tcpDump.start();
            for (int i = 0; i < intervals.length; i++) {
                Thread.sleep(intervals[i] * 1000);
                logger.info("Expecting DNS response verdict: " + expectedResults[i]);
                sendRequest2(requests.get(i));
                if (timeShifts[i] != 0) {
                    DateUtil.moveTime(timeShifts[i] * DateUtil.HOURS);
                    totalTimeShift += timeShifts[i] * DateUtil.HOURS;
                }
            }
            tcpDump.stop();
            end = System.currentTimeMillis();
            List<DnsSession> sessions = tcpDump.getDnsSessions(host, start, end);

            logger.info("toltal dns sessions with requested host " + sessions.size());
            for (int i = 0; i < sessions.size(); i++) {
                if (expectedResults[i].equals(MISS)) {
                    Thread.sleep(500);
                    logger.info("Processing session with id " + i + " : " + sessions.get(i).toString());
                    Thread.sleep(500);
                    assertTrue("Expected MISS, but was HIT for request id: " + (i + 1), sessions.get(i).checkMiss());
                } else if (expectedResults[i].equals(HIT)) {
                    Thread.sleep(500);
                    logger.info("Processing session with id " + i + " : " + sessions.get(i).toString());
                    Thread.sleep(500);
                    assertTrue("Expected HIT, but was MISS for request id: " + (i + 1), sessions.get(i).checkHit());
                } else if (expectedResults[i].equals("Failover")) {
                    Thread.sleep(500);
                    logger.info("Processing session with id " + i + " : " + sessions.get(i).toString());
                    Thread.sleep(500);
                    assertTrue("Expected to bypass OC, byt reqest was processed: " + (i + 1), sessions.get(i).checkFailover());
                } else {
                    throw new AssertionError("Failed to get expected results! Please check input parameters");
                }
                Thread.sleep(500);
            }
        } finally {
            DateUtil.moveTime(-totalTimeShift);
            if (host.equals(AsimovTestCase.TEST_RESOURCE_HOST)) PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    private void runMainThread(
            final String uri,
            final int[] intervals,
            final int[] timeShifts,
            final String[] expectedResults
    ) throws InterruptedException, IOException, IllegalAccessException, InstantiationException {
        runMainThread(uri, intervals, timeShifts, expectedResults, AsimovTestCase.TEST_RESOURCE_HOST);
    }

}
