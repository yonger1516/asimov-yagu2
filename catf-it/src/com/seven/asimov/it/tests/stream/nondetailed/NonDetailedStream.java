package com.seven.asimov.it.tests.stream.nondetailed;

import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.testcases.StreamTestCase;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.crcsTasks.NetlogTask;
import com.seven.asimov.it.utils.logcat.tasks.streamTasks.StreamTask;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import com.seven.asimov.it.utils.sms.SmsUtil;

import static com.seven.asimov.it.base.constants.TFConstantsIF.DISPATCHERS_LOG_LEVEL;
import static com.seven.asimov.it.base.constants.TFConstantsIF.MINUTE;

public class NonDetailedStream extends StreamTestCase {
    StreamTask streamTask = new StreamTask();

    /**
     * HTTPS traffic should go in stream in case of host is located in whitelist
     * <p/>
     * Steps
     * <p/>
     * 1. Remove namespace
     *
     * @throws Throwable
     * @asimov@application@com.seven.asimov.it@ssl from server
     * 2. Wait for policy update after removing namespace
     * 3. Create HTTPS request
     * 4. Send request to testrunner
     * 5. Observe logcat
     * <p/>
     * Results
     * 1. Policy should be received and applied
     * 2. Valid response for request should be received
     * 3. From logcat:
     * Dispatcher should send NAQ to OC Engine and receive NAR from Engine.
     * Dispatcher should send NSQ to OC Engine and receive NSR from Engine.
     * Netlog should contain proxy_stream operation.
     * 4. From syslog: stream process creating should be observed
     */
    public void test_001_HttpsNotBlacklisted() throws Throwable {
        int SLEEP_DELAY = MINUTE * 2;
        if(DISPATCHERS_LOG_LEVEL > 5) {
            SLEEP_DELAY = MINUTE * 5;
        }

        String policyName = "enabled";
        String policyPath = "@asimov@application@com.seven.asimov.it@ssl";
        String pathEnd = "testStreamHttpsNotBlacklisted";
        NetlogTask netlogTask = new NetlogTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), netlogTask, streamTask);

        HttpRequest httpRequest = createRequest().setUri(createTestResourceUri(pathEnd, true)).getRequest();
        Policy[] policiesToAdd = new Policy[]{new Policy(policyName, "false", policyPath, true)};
        try {
            PMSUtil.addPoliciesWithCheck(policiesToAdd);
            logcatUtil.start();
            checkMiss(httpRequest, 1);
            Thread.sleep(SLEEP_DELAY);
            logcatUtil.stop();
            setEntriesToCheck(netlogTask.getLogEntries(), streamTask.getLogEntries(), AsimovTestCase.TEST_RESOURCE_HOST);
            checkTasks(true, true, false);

        } finally {
            if(logcatUtil.isRunning()) {
                logcatUtil.stop();
            }
            PMSUtil.cleanPaths(new String[]{policyPath});
            streamTask.reset();
            resetValues();
        }
    }

    /**
     * HTTP traffic should go in stream in case of host is located in whitelist
     * Steps
     * <p/>
     * 1. Add policy with name “blacklisted” and value “tln-dev-testrunner1.7sys.eu” to @asimov@http
     * 2. Wait for policy update
     * 3. Create HTTP request
     * 4. Send request to testrunner
     * 5. Observe client log, syslog and pcap file.
     * <p/>
     * Results
     * 1. Policy should be received and applied.
     * 2. Valid response for request should be received
     * Dispatcher should send NAQ to OC Engine and receive NAR from Engine.
     * Dispatcher should send NSQ to OC Engine and receive NSR from Engine.
     * Netlog should contain proxy_stream operation.
     * 4. From debug log: creation of stream processor should be observed
     *
     * @throws Throwable
     */
    public void test_002_HttpNotBlacklisted() throws Throwable {
        int SLEEP_DELAY = MINUTE * 2;
        if(DISPATCHERS_LOG_LEVEL > 5) {
            SLEEP_DELAY = MINUTE * 5;
        }

        NetlogTask netlogTask = new NetlogTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), netlogTask, streamTask);

        String policyPath = "@asimov@http";
        HttpRequest request = createRequest().setUri(createTestResourceUri("test_002_httpWhitelisted", false)).getRequest();
        Policy[] policiesToAdd = {new Policy("blacklist", AsimovTestCase.TEST_RESOURCE_HOST, policyPath, true)};

        try {
            PMSUtil.addPoliciesWithCheck(policiesToAdd);
            logcatUtil.start();
            sendRequest(request);
            Thread.sleep(SLEEP_DELAY);
            logcatUtil.stop();
            setEntriesToCheck(netlogTask.getLogEntries(), streamTask.getLogEntries(), AsimovTestCase.TEST_RESOURCE_HOST);
            checkTasks(false, false, true);

        } finally {
            if(logcatUtil.isRunning()) {
                logcatUtil.stop();
            }
            PMSUtil.cleanPaths(new String[]{policyPath});
            Thread.sleep(MINUTE * 2);
            SmsUtil.sendPolicyUpdate(getContext(), (byte) 0);
            Thread.sleep(MINUTE * 6);
        }
    }

    /**
     * Traffic should go in stream in case of sending non-HTTP over 80 port
     * Steps
     * <p/>
     * 1. Create random byte array request
     * 2. Open connection to “tln-dev-testunner1.7sys.eu” over 80 port.
     * 3. Send request
     * 4. Observe client log
     * <p/>
     * Results
     * 1. CRCS with -32039 error code should be observed in logcat.
     * 2. From debug log creation of stream processor should be observed after error.
     * 3. Request should be sent.
     * 4. Netlog should contain proxy_stream operation.
     * 5. From debug log: creation of stream processor should be observed
     *
     * @throws Throwable
     */
    public void test_003_nonHtpOver80() throws Throwable {

        byte[] byteRequest = {0x24, 0x60, 0x30, 0x54, 0x70, 0x60, 0x30, 0x54, 0x50, 0x63, 0x19, 0x55, 0x70};
        sendAnyRequestByChosenPort(null, byteRequest, AsimovTestCase.TEST_RESOURCE_HOST, 80, false, false, null);
    }

    /**
     * Traffic should go in stream in case of sending non-HTTP  over ssl over 80 port
     * Steps
     * <p/>
     * 1. Create random byte array request
     * 2. Open connection to “tln-dev-testunner1.7sys.eu” with tls sypher suite over 80 port.
     * 3. Send request
     * 4. Observe client log
     * <p/>
     * Results
     * 1.  From debug log creation of stream processor should be observed
     * 3. Dispatcher should send NAQ to OC Engine and receive NAR from Engine.
     * Dispatcher should send NSQ to OC Engine and receive NSR from Engine.
     * 4. Netlog should contain proxy_stream operation.
     * 5. From debug log: creation of stream processor should be observed
     *
     * @throws Throwable
     */
    public void test_004_nonHttpOverSslOver80() throws Throwable {

        byte[] byteRequest = {0x20, 0x60, 0x30, 0x54, 0x70};
        sendAnyRequestByChosenPort(null, byteRequest, AsimovTestCase.TEST_RESOURCE_HOST, 80, true, true, null);
    }

    /**
     * Traffic should go in stream in case of sending  incorrect HTTP over 80 port
     * Steps
     * <p/>
     * 1. Create http request with protocol version HTTP\2.1
     * 2. Open connection to “tln-dev-testunner1.7sys.eu” over 80 port.
     * 3. Send request
     * 4. Observe client log
     * <p/>
     * Results
     * 1.  From debug log creation of stream processor should be observed
     * 3. Dispatcher should send NAQ to OC Engine and receive NAR from Engine.
     * Dispatcher should send NSQ to OC Engine and receive NSR from Engine.
     * 4. Netlog should contain proxy_stream operation.
     * 5. From debug log: creation of stream processor should be observed
     *
     * @throws Throwable
     */
    public void test_005_incorrectHttpOver80() throws Throwable {

        HttpRequest request = createRequest().setUri(createTestResourceUri("test_005_incorrectHttpOver80", false)).getRequest();
        sendAnyRequestByChosenPort(request, null, AsimovTestCase.TEST_RESOURCE_HOST, 80, false, false, HTTP_VERSION.HTTP21);
    }

    /**
     * Traffic should go in stream in case of sending incorrect HTTP over ssl over 80 port
     * <p/>
     * Steps
     * <p/>
     * 1. Create http request with protocol version HTTP\2.1
     * 2. Open connection to “tln-dev-testunner1.7sys.eu” over 80 port with tls cypher suite.
     * 3. Send request
     * 4. Observe client log
     * <p/>
     * Results
     * 1.  From debug log creation of stream processor should be observed
     * 3. Dispatcher should send NAQ to OC Engine and receive NAR from Engine.
     * Dispatcher should send NSQ to OC Engine and receive NSR from Engine.
     * 4. Netlog should contain proxy_stream operation.
     * 5. From debug log: creation of stream processor should be observed
     *
     * @throws Throwable
     */
    public void test_006_incorrectHttpOverSslOver80() throws Throwable {

        HttpRequest request = createRequest().setUri(createTestResourceUri("test_006_incorrectHttpOverSslOver80", true)).getRequest();
        sendAnyRequestByChosenPort(request, null, AsimovTestCase.TEST_RESOURCE_HOST, 80, false, true, HTTP_VERSION.HTTP21);
    }

    /**
     * Traffic should go in stream in case of sending non-HTTP over 443 port
     * <p/>
     * Steps
     * <p/>
     * 1. Create random byte array request
     * 2. Open connection to “tln-dev-testunner1.7sys.eu” over port 443.
     * 3. Send request
     * 4. Observe client log
     * <p/>
     * Results
     * 1.  From debug log creation of stream processor should be observed
     * 3. Dispatcher should send NAQ to OC Engine and receive NAR from Engine.
     * Dispatcher should send NSQ to OC Engine and receive NSR from Engine.
     * 4. Netlog should contain proxy_stream operation.
     * 5. From debug log: creation of stream processor should be observed
     *
     * @throws Throwable
     */
    public void test_007_nonHttpOver443() throws Throwable {

        byte[] byteRequest = {0x21, 0x60, 0x30, 0x54, 0x70};
        sendAnyRequestByChosenPort(null, byteRequest, AsimovTestCase.TEST_RESOURCE_HOST, 443, true, true, null);
    }

    /**
     * Traffic should go in stream in case of sending non-HTTP over 443 port
     * <p/>
     * Steps
     * <p/>
     * 1. Create random byte array request
     * 2. Open connection to “tln-dev-testunner1.7sys.eu” with tls cypher suite over port 443.
     * 3. Send request
     * 4. Observe client log
     * <p/>
     * Results
     * 1.  From debug log creation of stream processor should be observed
     * 3. Dispatcher should send NAQ to OC Engine and receive NAR from Engine.
     * Dispatcher should send NSQ to OC Engine and receive NSR from Engine.
     * 4. Netlog should contain proxy_stream operation.
     * 5. From debug log: creation of stream processor should be observed
     *
     * @throws Throwable
     */
    public void test_008_nonHttpOverSslOver443() throws Throwable {

        byte[] byteRequest = {0x20, 0x60, 0x30, 0x54, 0x70};
        sendAnyRequestByChosenPort(null, byteRequest, AsimovTestCase.TEST_RESOURCE_HOST, 443, true, true, null);
    }

    /**
     * Traffic should go in stream in case of sending  incorrect HTTP over 443 port
     * <p/>
     * Steps
     * <p/>
     * 1. Create http request with protocol version HTTP\2.1
     * 2. Open connection to “tln-dev-testunner1.7sys.eu” over port 443.
     * 3. Send request
     * 4. Observe client log
     * <p/>
     * Results
     * 1.  From debug log creation of stream processor should be observed
     * 3. Dispatcher should send NAQ to OC Engine and receive NAR from Engine.
     * Dispatcher should send NSQ to OC Engine and receive NSR from Engine.
     * 4. Netlog should contain proxy_stream operation.
     * 5. From debug log: creation of stream processor should be observed
     *
     * @throws Throwable
     */
    public void test_009_incorrectHttpOver443() throws Throwable {

        HttpRequest request = createRequest().setUri(createTestResourceUri("test_009_incorrectHttpOver443", false)).getRequest();
        sendAnyRequestByChosenPort(request, null, AsimovTestCase.TEST_RESOURCE_HOST, 443, false, false, HTTP_VERSION.HTTP21);
    }

    /**
     * Traffic should go in stream in case of sending incorrect HTTP over ssl over 443 port
     * <p/>
     * Steps
     * <p/>
     * 1. Create http request with protocol version HTTP\2.1
     * 2. Open connection to “tln-dev-testunner1.7sys.eu” over port 443 with tls cypher suite.
     * 3. Send request
     * 4. Observe client log
     * <p/>
     * Results
     * 1.  From debug log creation of stream processor should be observed
     * 3. Dispatcher should send NAQ to OC Engine and receive NAR from Engine.
     * Dispatcher should send NSQ to OC Engine and receive NSR from Engine.
     * 4. Netlog should contain proxy_stream operation.
     * 5. From debug log: creation of stream processor should be observed
     *
     * @throws Throwable
     */
    public void test_010_incorrectHttpOverSslOver443() throws Throwable {

        HttpRequest request = createRequest().setUri(createTestResourceUri("test_010_incorrectHttpOverSslOver443", true)).getRequest();
        sendAnyRequestByChosenPort(request, null, AsimovTestCase.TEST_RESOURCE_HOST, 443, false, false, HTTP_VERSION.HTTP21);
    }

    /**
     * Traffic should go in stream in case of sending correct HTTP over port except for 80 or 443 port
     * <p/>
     * Steps
     * <p/>
     * 1. Create random byte array request
     * 2. Open connection to “tln-dev-testunner1.7sys.eu” over 81 port
     * 3. Send request
     * 4. Observe client log
     * <p/>
     * Results
     * 1.  From debug log creation of stream processor should be observed
     * 3. Dispatcher should send NAQ to OC Engine and receive NAR from Engine.
     * Dispatcher should send NSQ to OC Engine and receive NSR from Engine.
     * 4. Netlog should contain proxy_stream operation.
     * 5. From debug log: creation of stream processor should be observed
     *
     * @throws Throwable
     */
    public void test_011_correctHttpOverNon80or443() throws Throwable {

        String pathEnd = "test_011_correctHttpOverNon80or443";
        String uri = createTestResourceUri(pathEnd, false);
        HttpRequest httpRequest = createRequest().setUri(uri).getRequest();
        sendAnyRequestByChosenPort(httpRequest, null, AsimovTestCase.TEST_RESOURCE_HOST, 81, false, false, HTTP_VERSION.HTTP11);
    }

    /**
     * Traffic should go in stream in case of sending correct HTTP over ssl over port except for 80 or 443
     * <p/>
     * Steps
     * <p/>
     * 1. Create http request with protocol version HTTP\2.1
     * 2. Open connection to “tln-dev-testunner1.7sys.eu” over 81 port with tls cypher suite.
     * 3. Send request
     * 4. Observe client log
     * <p/>
     * Results
     * 1.  From debug log creation of stream processor should be observed
     * 3. Dispatcher should send NAQ to OC Engine and receive NAR from Engine.
     * Dispatcher should send NSQ to OC Engine and receive NSR from Engine.
     * 4. Netlog should contain proxy_stream operation.
     * 5. From debug log: creation of stream processor should be observed
     *
     * @throws Throwable
     */
    public void test_012_correctHttpOverSslOverNon80or443() throws Throwable {
        String pathEnd = "test_012_correctHttpOverSslOverNon80or443";
        String uri = createTestResourceUri(pathEnd, true);
        HttpRequest httpRequest = createRequest().setUri(uri).getRequest();
        sendAnyRequestByChosenPort(httpRequest, null, AsimovTestCase.TEST_RESOURCE_HOST, 81, false, true, HTTP_VERSION.HTTP11);
    }

    /**
     * Traffic should go in stream in case of sending incorrect HTTP over port except for 80 or 443
     * <p/>
     * Steps
     * <p/>
     * 1. Create random byte array request
     * 2. Open connection to “tln-dev-testunner1.7sys.eu” over 81 port.
     * 3. Send request
     * 4. Observe client log
     * <p/>
     * Results
     * 1.  From debug log creation of stream processor should be observed
     * 3. Dispatcher should send NAQ to OC Engine and receive NAR from Engine.
     * Dispatcher should send NSQ to OC Engine and receive NSR from Engine.
     * 4. Netlog should contain proxy_stream operation.
     * 5. From debug log: creation of stream processor should be observed
     *
     * @throws Throwable
     */
    public void test_013_incorrectHttpOverNon80or443() throws Throwable {
        String pathEnd = "test_013_incorrectHttpOverNon80or443";
        String uri = createTestResourceUri(pathEnd, false);
        HttpRequest httpRequest = createRequest().setUri(uri).getRequest();
        sendAnyRequestByChosenPort(null, httpRequest.getFullRequest(false).getBytes(), AsimovTestCase.TEST_RESOURCE_HOST, 81, false, false, HTTP_VERSION.HTTP11);
    }

    /**
     * Traffic should go in stream in case of sending incorrect HTTP over ssl over port except for 80 or 443
     * <p/>
     * Steps
     * <p/>
     * 1. Create random byte array request
     * 2. Open connection to “tln-dev-testunner1.7sys.eu” over 81 port with tls cypher suite.
     * 3. Send request
     * 4. Observe client log
     * <p/>
     * Results
     * 1.  From debug log creation of stream processor should be observed
     * 3. Dispatcher should send NAQ to OC Engine and receive NAR from Engine.
     * Dispatcher should send NSQ to OC Engine and receive NSR from Engine.
     * 4. Netlog should contain proxy_stream operation.
     * 5. From debug log: creation of stream processor should be observed
     *
     * @throws Throwable
     */
    public void test_014_incorrectHttpOverSslOverNon80or443() throws Throwable {
        String pathEnd = "test_012_correctHttpOverSslOverNon80or443";
        String uri = createTestResourceUri(pathEnd, true);
        HttpRequest httpRequest = createRequest().setUri(uri).getRequest();
        sendAnyRequestByChosenPort(null, httpRequest.getFullRequest().getBytes(), AsimovTestCase.TEST_RESOURCE_HOST, 81, false, true, HTTP_VERSION.HTTP21);
    }

    /**
     * Traffic should go in stream in case of sending non-HTTP over port except for 80 or 443
     * <p/>
     * Steps
     * <p/>
     * 1. Create http request with protocol version HTTP\2.1
     * 2. Open connection to “tln-dev-testunner1.7sys.eu” over 81 port.
     * 3. Send request
     * 4. Observe client log
     * <p/>
     * Results
     * 1.  From debug log creation of stream processor should be observed
     * 3. Dispatcher should send NAQ to OC Engine and receive NAR from Engine.
     * Dispatcher should send NSQ to OC Engine and receive NSR from Engine.
     * 4. Netlog should contain proxy_stream operation.
     * 5. From debug log: creation of stream processor should be observed
     *
     * @throws Throwable
     */
    public void test_015_nonHttpOverNon80or443() throws Throwable {

        byte[] byteRequest = {0x16, 0x60, 0x30, 0x54, 0x70};
        sendAnyRequestByChosenPort(null, byteRequest, AsimovTestCase.TEST_RESOURCE_HOST, 81, false, false, null);
    }

    /**
     * Traffic should go in stream in case of sending non-HTTP over port except for 80 or 443
     * <p/>
     * Steps
     * <p/>
     * 1.1. Create http request with protocol version HTTP\2.1
     * 2. Open connection to “tln-dev-testunner1.7sys.eu” over 81 port with tls cypher suite.
     * 3. Send request
     * 4. Observe client log
     * <p/>
     * Results
     * 1.  From debug log creation of stream processor should be observed
     * 3. Dispatcher should send NAQ to OC Engine and receive NAR from Engine.
     * Dispatcher should send NSQ to OC Engine and receive NSR from Engine.
     * 4. Netlog should contain proxy_stream operation.
     * 5. From debug log: creation of stream processor should be observed
     *
     * @throws Throwable
     */
    public void test_016_nonHttpOverSslOverNon80or443() throws Throwable {

        byte[] byteRequest = {0x20, 0x60, 0x30, 0x54, 0x70};
        sendAnyRequestByChosenPort(null, byteRequest, AsimovTestCase.TEST_RESOURCE_HOST, 81, false, true, null);
    }
}
