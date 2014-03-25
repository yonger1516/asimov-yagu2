package com.seven.asimov.test.tool.tests.sanity;


import android.util.Log;
import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.base.Base64;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.*;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.cachingTasks.CsaTask;
import com.seven.asimov.it.utils.logcat.tasks.crcsTasks.NetlogTask;
import com.seven.asimov.it.utils.logcat.tasks.failoverTasks.FailoverStartTask;
import com.seven.asimov.it.utils.logcat.tasks.policyTasks.PolicyAddedTask;
import com.seven.asimov.it.utils.logcat.tasks.pollingTasks.StartPollRequestTask;
import com.seven.asimov.it.utils.logcat.tasks.pollingTasks.StartPollTask;
import com.seven.asimov.it.utils.logcat.tasks.sessionBasicTasks.RequestMD5Task;
import com.seven.asimov.it.utils.logcat.tasks.sessionBasicTasks.ResponseMD5Task;
import com.seven.asimov.it.utils.logcat.wrappers.NetlogEntry;
import com.seven.asimov.it.utils.logcat.wrappers.OperationType;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import com.seven.asimov.test.tool.activity.AutomationTestsTab;
import com.seven.asimov.test.tool.testcase.SmokeTestCase;
import com.seven.asimov.test.tool.utils.SmokeHelperUtil;
import com.seven.asimov.test.tool.utils.Z7TestUtil;
import com.seven.asimov.test.tool.utils.logcat.wrapper.TestCaseEvents;
import junit.framework.AssertionFailedError;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

public class SanityTests extends SmokeTestCase {

    String TAG = SanityTests.class.getSimpleName();

    @Ignore
    public void invCheck() throws IOException {
        SmokeHelperUtil smokeHelper = SmokeHelperUtil.getInstance(AutomationTestsTab.context);
        smokeHelper.sanityGlobalCheck();
    }

    @Ignore
    public void test_000_ServerAvailability() throws IOException {
        if (!SmokeHelperUtil.restServerCheck()) {
            throw new AssertionFailedError("Rest server is not avaliable, we can't operate with client configuration");
        }
    }

    /**
     * 1. Send HTTP request with headers:
     * Content-lenght: 1024
     * Connection: Keep-Alive
     * 2. When response is receiving, kill process of http
     * 3. Check crach http ??
     * <p/>
     * 1. Error should be received.
     * 2. Valid response shouldnt be received
     *
     * @throws Throwable
     */
    public void test_001_CrashHttpDispatcher() throws Throwable {

        killHttpProcess.start();
        String uri = createTestResourceUri("sanity_suite_smoke_test002");
        HttpRequest request = createRequest().setUri(uri).addHeaderField("Content-lenght", "2048").addHeaderField("Connection", "Keep-alive").getRequest();
        try {
            HttpResponse response = sendRequest2(request);
            if (!response.getBody().equals("")) {
                Z7TestUtil.setAdditionalInfo("Delay between request and response is too big, so OC have already had time to restart dispatchers", TestCaseEvents.TESTCASE_EVENT.SUSPENDED);
            } else {
                assertEquals("The client shouldn't receive response", "", response.getBody());
            }
            Log.i(TAG, Integer.toString(SmokeUtil.processesParser().size()));
            assertTrue("Dispatchers should be reconnected but didn't it", SmokeUtil.processesParser().size() == 4);
        } finally {
            killHttpProcess.interrupt();
            Thread.sleep(60 * 1000);
        }

    }

    /**
     * Socket no close production
     * <p/>
     * 1. Send HTTP request #1 with header:
     * Connection: close.
     * 2. While socket is opened, send HTTP request #2 with header:
     * Content-lenght: 512
     * <p/>
     * 1. Response for request #1 should be received
     * 2. Response for request #2 should be received
     *
     * @throws Throwable
     */
    public void test_002_SocketOpened() throws Throwable {
        int i = 0;
        String uri = createTestResourceUri("sanity_suite_smoke_test003");
        HttpRequest request1 = createRequest().setUri(uri).getRequest();
        HttpRequest request2 = createRequest().setUri(uri).addHeaderField("Content-length", "512").getRequest();
        PrepareResourceUtil.prepareResource(uri, false);
        checkMiss(request1, ++i);
        checkMiss(request2, ++i);

    }

    /**
     * Caching by rfc
     * <p/>
     * 1. Send HTTP request #1 with header:
     * Cache-control:public
     * Content-lenght: 128
     * 2. Send HTTP request #2 with header:
     * Cache-control: max-stale
     * <p/>
     * 1. Response for request #1 should be received from network, cached by RFC
     * 2. Response for request #2 should be received from network, cached by RFC
     *
     * @throws Throwable
     */
    public void test_003_Rfc_CacheControl() throws Throwable {

        String Hamlet = "To be, or not to be, that is the Question:"
                + "Whether tis Nobler in the minde to suffer" + "The Slings and Arrowes of outragious Fortune,"
                + "Or to take Armes against a Sea of troubles," + "And by opposing end them: to dye, to sleepe"
                + "No more; and by a sleepe, to say we end" + "The Heart-ake, and the thousand Naturall shockes"
                + "That Flesh is heyre too? Tis a consummation" + "Deuoutly to be wishd. To dye to sleepe,"
                + "To sleepe, perchance to Dreame; I, theres the rub,"
                + "For in that sleepe of death, what dreames may come," + "When we haue shuffeld off this mortall coile,"
                + "Must giue vs pawse. Theres the respect" + "That makes Calamity of so long life:"
                + "For who would beare the Whips and Scornes of time," + "The Oppressors wrong, the poore mans Contumely,"
                + "The pangs of disprizd Loue, the Lawes delay," + "The insolence of Office, and the Spurnes"
                + "That patient merit of the vnworthy takes," + "When he himselfe might his Quietus make"
                + "With a bare Bodkin? Who would these Fardles beare" + "To grunt and sweat vnder a weary life,"
                + "But that the dread of something after death," + "The vndiscouered Countrey, from whose Borne"
                + "No Traueller returnes, Puzels the will," + "And makes vs rather beare those illes we haue,"
                + "Then flye to others that we know not of." + "Thus Conscience does make Cowards of vs all,"
                + "And thus the Natiue hew of Resolution" + "Is sicklied ore, with the pale cast of Thought,"
                + "And enterprizes of great pith and moment," + "With this regard their Currants turne away,"
                + "And loose the name of Action. Soft you now," + "The faire Ophelia? Nimph, in thy Orizons"
                + "Be all my sinnes remembred";


        assertTrue("Dispatchers should be reconnected but didn't it", SmokeUtil.processesParser().size() == 4);

        CsaTask csaTask = new CsaTask();
        LogcatUtil logcatUtil = new LogcatUtil(AutomationTestsTab.context, csaTask);
        String uri = createTestResourceUri("sanity_suite_smoke_test005");

        String raw = "Cache-Control: public" + TFConstantsIF.CRLF + "Connection: close" + TFConstantsIF.CRLF;
        String encoded = URLEncoder.encode(Base64.encodeToString(raw.getBytes(), Base64.DEFAULT));
        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ChangeResponseContent", Hamlet)
                .addHeaderField("X-OC-ChangeSleep", "0")
                .addHeaderField("X-OC-BelongsTo", TEST_RESOURCE_OWNER).getRequest();
        HttpResponse response = sendRequestParallel(request, false, true);
        assertEquals(response.getBody(), Hamlet);


        HttpRequest requestTest = createRequest().setUri(uri).addHeaderField("Cache-Control", "max-stale").getRequest();
        logcatUtil.start();
        try {
            checkMiss(requestTest, 1);
            checkHit(requestTest, 2);
        } finally {
            if (logcatUtil.isRunning()) {
                logcatUtil.stop();
            }
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * OC blacklisted
     * <p/>
     * 1. OC client is installed
     * 2. Add com.seven.asimov.test.tool to ssl branch of PMS
     * <p/>
     * 1. Send HTTPS request with headers:
     * Connection: Keep-Alive
     * <p/>
     * 1. Response for request should be received. SSL handshake should be done correctly.
     * 2. NetLog with proxy_ssl_local_hs operation type should be observed in logcat for com.seven.asimov.test.tool.it
     *
     * @throws Throwable
     */
    public void test_004_HTTPS_Blacklisted() throws Throwable {
        setUp();

        NetlogTask netlogTask = new NetlogTask();
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        List<NetlogEntry> netlogEntries;
        LogcatUtil logcatUtil = new LogcatUtil(AutomationTestsTab.context, netlogTask, policyAddedTask);
        String policyValue = "true";
        String policyName = "enabled";
        String policyPath = "@asimov@application@com.seven.asimov.test.tool@ssl";
        boolean appStatusChanged = false;

        String uri = createTestResourceUri("sanity_suite_test_004_HTTPS_Blacklisted", true);
        HttpRequest request = createRequest().setUri(uri).addHeaderField("Connection", "Keep-Alive").getRequest();
        PrepareResourceUtil.prepareResource(uri, false);
        logcatUtil.start();
        try {
            PMSUtil.addPolicies(new Policy[]{new Policy(policyName, policyValue, policyPath, true)});
            TestUtil.sleep(27000);
            checkMiss(request, 1);
            TestUtil.sleep(3000);
            logcatUtil.stop();
            netlogEntries = netlogTask.getLogEntries();
            Log.e(TAG, "netlogTask.getLogEntries()" + netlogTask.getLogEntries().size());
            for (NetlogEntry entry : netlogEntries) {
                Log.e(TAG, "entry: " + entry.getOpType());

                if (entry.getOpType().equals(OperationType.proxy_ssl_local_hs)) {
                    appStatusChanged = true;
                }
            }
            assertTrue("The status of application hasn't been changed for 'proxy_ssl_local_hs'", appStatusChanged);

        } finally {
            PMSUtil.cleanPaths(new String[]{policyPath});
            PrepareResourceUtil.prepareResource(uri, true);
            logcatUtil.stop();
        }
    }

    /**
     * Blacklisted HTTPS traffic should go in stream
     * <p/>
     * 1. OC client is installed
     * 2. Be sure that com.seven.asimov.test.tool is not added to ssl branch of PMS
     * <p/>
     * 1. Send 4 HTTPS requests
     * <p/>
     * 1. All NetLog records for com.seven.asimov.test.tool should have proxy_stream operation type in logcat
     *
     * @throws Throwable
     */
    public void test_005_HTTPS_NotBlacklisted() throws Throwable {
        setUp();

        NetlogTask netlogTask = new NetlogTask();
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        List<NetlogEntry> netlogEntries;
        List<HttpRequest> requests;
        LogcatUtil logcatUtil = new LogcatUtil(AutomationTestsTab.context, netlogTask, policyAddedTask);
        String policyValue = "false";
        String policyName = "enabled";
        String policyPath = "@asimov@application@com.seven.asimov.test.tool@ssl";
        boolean appStatusChanged = false;

        String uri = createTestResourceUri("sanity_suite_test_005_HTTPS_NotBlacklisted", true);
        requests = SmokeTestCase.createNumberOfrandomRequests(4, uri);
        PrepareResourceUtil.prepareResource(uri, false);
        logcatUtil.start();
        try {
            PMSUtil.addPolicies(new Policy[]{new Policy(policyName, policyValue, policyPath, true)});
            TestUtil.sleep(27000);
            int i = 0;
            for (HttpRequest request : requests) {
                i++;
                checkMiss(request, i);
            }
            TestUtil.sleep(3000);
            logcatUtil.stop();
            netlogEntries = netlogTask.getLogEntries();
            for (NetlogEntry entry : netlogEntries) {
                if (entry.getOpType().equals(OperationType.proxy_stream)) {
                    appStatusChanged = true;
                }
            }
            assertTrue("The status of application hasn't been changed for 'proxy_stream'", appStatusChanged);
        } finally {
            if (logcatUtil.isRunning()) {
                logcatUtil.stop();
            }
            PMSUtil.cleanPaths(new String[]{policyPath});
            PrepareResourceUtil.prepareResource(uri, true);
        }
    }

    /**
     * Application from bypass list should be bypassed by OC in 3G/Wi-Fi mode
     * <p/>
     * 1. OC client is installed
     * 2. Mobile network is avaliable
     * <p/>
     * 1. Set such policy:
     * asimov@interception@bypass_list = com.seven.asimov.test.tool
     * 2. Send HTTP request #1
     * 3. Switch to Wi-Fi
     * 4. Send HTTP request #2
     * <p/>
     * 1. Policy should be received and applied.
     * 2. Responses for both requests should be received.
     * 3. Both HTTP transactions should be bypassed by OC
     * 4. Content of the  dispatchers.cfg file should be following:
     * {bypass_list}
     * com.seven.asimov.test.tool
     * {dispatchers_cfg}
     * ocshttpd;1;443;0
     * ochttpd;1;80;0
     * ocdnsd;2;53;0
     * octcpd;1;1:24,26:109,111:219,221:464,466:992,994,996:7734,7736:8110,8112:65534;255
     * {cfg_end}
     *
     * @throws Throwable
     */
    public void test_006_AppByPathOC() throws Throwable {
        setUp();
        switchNetwork(2);

        String policyValue = "com.seven.asimov.test.tool";
        String policyName = "bypass_list";
        String policyPath = "@asimov@interception";
        String uri = createTestResourceUri("sanity_suite_test_006_AppByPathOC", true);
        PrepareResourceUtil.prepareResource(uri, false);
        RequestMD5Task requestMD5Task = new RequestMD5Task();
        ResponseMD5Task responseMD5Task = new ResponseMD5Task();
        LogcatUtil logcatUtil = new LogcatUtil(AutomationTestsTab.context, requestMD5Task, responseMD5Task);
        try {
            Log.i(TAG, "Adding policies");
            PMSUtil.addPolicies(new Policy[]{new Policy(policyName, policyValue, policyPath, true)});
            TestUtil.sleep(27000);
            logcatUtil.start();
            sendRequest2(SmokeTestCase.createRandomRequest(uri));
            TestUtil.sleep(3000);
            logcatUtil.stop();
            Log.i(TAG, "1-st check");
            assertTrue("1-st check", requestMD5Task.getLogEntries().size() == 0);
            assertTrue("2-nd check", responseMD5Task.getLogEntries().size() == 0);
            requestMD5Task = new RequestMD5Task();
            responseMD5Task = new ResponseMD5Task();

            logcatUtil = new LogcatUtil(AutomationTestsTab.context, requestMD5Task, responseMD5Task);
            logcatUtil.start();
            switchNetwork(1);
            Log.i(TAG, "2-nd check");
            sendRequest2(SmokeTestCase.createRandomRequest(uri));
            assertTrue("3-st check", requestMD5Task.getLogEntries().size() == 0);
            assertTrue("4-nd check", responseMD5Task.getLogEntries().size() == 0);
            SmokeTestCase.checkDispatcersConfiguration(false);
        } finally {
            try {
                PMSUtil.cleanPaths(new String[]{"@asimov@interception"});
                if (logcatUtil.isRunning()) {
                    logcatUtil.stop();
                }
            } catch (Exception exception) {
            }
        }
    }

    /**
     * 1. OC client is installed
     * 2. Policy should be configured:
     * asimov@enabled=0
     * asimov@interception@ochttpd@interception ports: 443
     * asimov@interception@ochttpd@type: 1
     * asimov@interception@ochttpd@z_order:0
     * asimov@interception@ocshttpd@interception ports: 80
     * asimov@interception@ocshttpd@type: 1
     * asimov@interception@ocshttpd@z_order:0
     * <p/>
     * 1. Wait 1 minute
     * 2. Configure policy rule:
     * asimov@enabled=1
     * 3. Send HTTP request
     * <p/>
     * <p/>
     * 1. Verify that policy are received and added and there are such record in logcat as:
     * Adding a policy node id 2, level 1:   'enabled':'0'
     * 2. Dispatchers should change state from RUNNING to KILLED,. Iptables should be unconfigured, OC1 should be destroyed.
     * From sys log:
     * SSM <disp_name> (id=xxxxxxxxx, pid=<pid>, state=RUNNING) iptables unconfigured
     * SSM <disp_name> (id=xxxxxxxxx, pid=<pid>, state=PENDING_SHUTDOWN) sent signal 1
     * SSM <disp_name> (id=xxxxxxxxx, pid=<pid>, state=STOPPED) stopped
     * SSM  <disp_name> (id=xxxxxxxxx, pid=<pid>,  state=KILLED) OC1 destroyed
     * 3. Verify from running processes (adb shell top) that dispatchers not run.
     * 4. Verify from running processes (adb shell top) that dispatchers are not running at step 2 but dispatchers config should be updated  (data/misc/openchannel/dispatchers.cfg):
     * {bypass_list}
     * com.seven.asimov.test.tool
     * {dispatchers_cfg}
     * ocshttpd;1;80;0
     * ochttpd;1;443;0
     * ocdnsd;2;53;0
     * octcpd;1;1:24,26:109,111:219,221:464,466:992,994,996:7734,7736:8110,8112:65534;255
     * {cfg_end}
     * 5.Verify from running processes (adb shell top) that dispatchers are running after policy asimov@enabled=1 received and applied.
     * 6. HTTP request should be processed by ocshttpd.
     * From logcat: Constructed HTTP CLQ from ocshttpd
     *
     * @throws Throwable
     */
    public void test_007_DisabledFailover() throws Throwable {
        setUp();
        IpTablesUtil.execute(new String[]{"su", "-c", "chmod -R 777 /data/"});
        HashMap<Integer, HttpRequest> requests = new HashMap();

        String value = "com.seven.asimov.test.tool;80";

        boolean[] checks = {true, false, true, true};

        String httpUri = createTestResourceUri("test_001_bypath_http", false);
        String httpsUri = createTestResourceUri("test_001_bypath_https", true);

        PrepareResourceUtil.prepareResource(httpUri, false);
        PrepareResourceUtil.prepareResource(httpsUri, false);
        requests.clear();
        requests.put(80, createRequest().setUri(httpUri).getRequest());
        requests.put(443, createRequest().setUri(httpsUri).getRequest());

        bypathPortTest(requests, value, 0, 1, 0, checks);

        PrepareResourceUtil.invalidateResourceSafely(httpUri);
        PrepareResourceUtil.invalidateResourceSafely(httpsUri);
        requests.clear();
    }


    /**
     * 1. OC client is installed
     * 2. Active interface is 3G
     * 3. Policy should be configured:
     * client.openchannel.roaming_wifi_failover.enabled=1 
     * client.openchannel.roaming_wifi_failover.actions=1 
     * <p/>
     * 1. Disable input traffic from relay via such command:
     * iptables -t filter -I INPUT -p tcp --sport 7735
     * 2. Immediately turn on wifi.
     * <p/>
     * 1. After one negative attempt to connect to Relay, client should start failovers. 
     * 2. Such records should be in logcat:
     * Trying to disable FireWall base chain
     * OC1: Sent IPC (type=6 chain_id=0)
     * OC1: Sent FLO (action=1 options=0)
     * 3. Dispatchers should not running.
     * 4. From iptables (iptables -L) firewall rule should be disabled
     *
     * @throws Throwable
     */
    public void test_008_WiFiFailoverActions1() throws Throwable {
        Policy[] configurationFailover = {
                new Policy("enabled", "true", "@asimov@failovers@wifi", true),
                new Policy("actions", "1", "@asimov@failovers@wifi", true)
        };

        String[] pathes = {
                "@asimov@failovers@wifi"
        };

        String[] unreacheableRelayServer = {"su", "-c", TFConstantsIF.IPTABLES_PATH + " -t filter -I INPUT -m conntrack --ctorigdstport 7735 -j REJECT"};
        String[] enableRelayServer = {"su", "-c", TFConstantsIF.IPTABLES_PATH + " -t filter -D INPUT -m conntrack --ctorigdstport 7735 -j REJECT"};

        switchNetwork(2);
        FailoverStartTask failoverStartTask = new FailoverStartTask();

        LogcatUtil logcatUtil = new LogcatUtil(AutomationTestsTab.context, failoverStartTask);


        try {
            Log.i(TAG, Integer.toString(SmokeUtil.processesParser().size()));
            assertTrue("Dispatchers should be started but didn't it", SmokeUtil.processesParser().size() == 4);

            PMSUtil.addPolicies(configurationFailover);
            TestUtil.sleep(27000);
            logcatUtil.start();
            Runtime.getRuntime().exec(unreacheableRelayServer).waitFor();

            assertTrue("IpTables should be updated bud didn't it", SmokeHelperUtil.ipTableUpdateCheck(false));

            switchNetwork(1);
            assertTrue("Failover didn't start but should be", failoverStartTask.getLogEntries().size() == 1);

            Log.i(TAG, Integer.toString(SmokeUtil.processesParser().size()));
            assertTrue("Dispatchers should be disabled but didn't it", SmokeUtil.processesParser().size() == 1);

        } finally {
            Runtime.getRuntime().exec(enableRelayServer).waitFor();
            try {
                switchNetwork(2);
                switchNetwork(1);
                Thread.sleep(25 * 1000);
                PMSUtil.cleanPaths(pathes);
                Thread.sleep(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
                logcatUtil.stop();
            } catch (Exception exception) {
            }
        }
    }

    /**
     * 1. OC client is installed
     * 2. Active interface is 3G
     * 3. Policy should be configured:
     * client.openchannel.roaming_wifi_failover.enabled=1 
     * client.openchannel.roaming_wifi_failover.actions=2
     * <p/>
     * <p/>
     * 1. Disable input traffic from relay via such command:
     * iptables -t filter -I INPUT -p tcp –s port 7735
     * 2. Immediately turn on wifi.
     * 3.  Enable input traffic from relay via such command:
     * iptables -t filter -D INPUT -p tcp –s port 7735
     * <p/>
     * <p/>
     * 1. After one negative attempt to connect to Relay, client should start failovers. 
     * 2. Such records should be in logcat:
     * Trying to disable FireWall base chain
     * OC1: Sent IPC (type=6 chain_id=0)
     * OC1: Sent FLO (action=2 options=0)
     * 3. Dispatchers should not running.
     * 4. From iptables (iptables -L) firewall rule should be disabled
     * 5.  Cache should be cleaned up.
     * 6. SMS with policy notification should be received from server, but client shouldn't do any attempts to connect to Relay
     *
     * @throws Throwable
     */
    public void test_009_WiFiFailoverActions2() throws Throwable {

        Policy[] configurationFailover = {
                new Policy("enabled", "true", "@asimov@failovers@wifi", true),
                new Policy("actions", "2", "@asimov@failovers@wifi", true)
        };
        String[] pathes = {
                "@asimov@failovers@wifi"
        };

        String[] unreacheableRelayServer = {"su", "-c", TFConstantsIF.IPTABLES_PATH + " -t filter -I INPUT -m conntrack --ctorigdstport 7735 -j REJECT"};
        String[] enableRelayServer = {"su", "-c", TFConstantsIF.IPTABLES_PATH + " -t filter -D INPUT -m conntrack --ctorigdstport 7735 -j REJECT"};

        FailoverStartTask failoverStartTask = new FailoverStartTask();
        switchNetwork(2);

        LogcatUtil logcatUtil = new LogcatUtil(AutomationTestsTab.context, failoverStartTask);
        try {
            Log.i(TAG, Integer.toString(SmokeUtil.processesParser().size()));
            assertTrue("Dispatchers should be started but didn't it", SmokeUtil.processesParser().size() == 4);

            PMSUtil.addPolicies(configurationFailover);
            TestUtil.sleep(27000);
            logcatUtil.start();
            Runtime.getRuntime().exec(unreacheableRelayServer).waitFor();

            assertTrue("IpTables should be updated bud didn't it", SmokeHelperUtil.ipTableUpdateCheck(false));

            switchNetwork(1);
            TestUtil.sleep(3000);
            logcatUtil.stop();
            assertTrue("Failover didn't start but should be", failoverStartTask.getLogEntries().size() == 1);

            Log.i(TAG, Integer.toString(SmokeUtil.processesParser().size()));
            assertTrue("Dispatchers should be disabled but didn't it", SmokeUtil.processesParser().size() == 1);

        } finally {
            Runtime.getRuntime().exec(enableRelayServer).waitFor();
            try {
                switchNetwork(2);
                PMSUtil.cleanPaths(pathes);
                Thread.sleep(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
                if (logcatUtil.isRunning()) {
                    logcatUtil.stop();
                }
            } catch (Exception exception) {
            }
        }
    }


    /**
     * 1. OC client is installed
     * 2. Policy should be configured:
     * asimov@http@cache_invalidate_aggressiveness=1
     * 3. Screen is switched off
     * <p/>
     * <p/>
     * 1.Send 4 HTTP requests with delay 30 sec between each other.
     * 2. Kill OC and dispatchers processes.
     * 3. Wait for OC to be restarted.
     * 4. Make 1 request for the same resource.
     * <p/>
     * 1. Responses for requests 1st – 3rd should be received from network. There should be such records in logcat:
     * RMP detected.
     * RR status pending.
     * RR status polling.
     * 2. Response for the 4th request should be received from cache.
     * 3. After OCE restart, all polling models CE should be loaded from database, and restored anew with cases:
     * - RR id;
     * - key;
     * - subscription id (RRR&RRC [-1]);
     * - CE id;
     * - expire time.
     * 4. Response for the 5th request should be received from cache.
     *
     * @throws Throwable
     */
    @Ignore
    public void test_010_RI_WithCrash() throws Throwable {

        Policy[] aggresiveness = {new Policy("cache_invalidate_aggressiveness", "1", "@asimov@http", true)};
        String[] pathes = {"@asimov@http"};
        StartPollRequestTask startPollRequestTask = new StartPollRequestTask();
        StartPollTask startPollTask = new StartPollTask();

        LogcatUtil logcatUtil = new LogcatUtil(AutomationTestsTab.context, startPollRequestTask, startPollTask);

        String uri = createTestResourceUri("test_010_RI_WithCrash_7TT", false);

        HttpRequest httpRequest = createRequest().setUri(uri).addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
        HttpResponse response;
        int counter = 0;
        long DELAY = 30 * 1000;

        PrepareResourceUtil.prepareResource(uri, false);

        try {
            PMSUtil.addPoliciesWithCheck(aggresiveness);

            //#1
            response = checkMiss(httpRequest, ++counter, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());
            //#2
            response = checkMiss(httpRequest, ++counter, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());
            //#3
            response = checkMiss(httpRequest, ++counter, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            logcatUtil.start();
            //#4
            response = checkHit(httpRequest, ++counter, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());
            logcatUtil.stop();
            assertTrue("Start poll didn't received", startPollRequestTask.getLogEntries().size() != 0);
            assertTrue("Status POLLING didn't detected", startPollTask.getLogEntries().size() != 0);

            SmokeTestCase.killOcc();
            assertTrue("Dispatchers should be reconnected but didn't it", SmokeUtil.processesParser().size() == 5);

            checkHit(httpRequest, ++counter, VALID_RESPONSE);
        } finally {
            PrepareResourceUtil.prepareResource(uri, true);
            if (logcatUtil.isRunning()) {
                logcatUtil.stop();
            }
            PMSUtil.cleanPaths(pathes);
            Thread.sleep(TFConstantsIF.WAIT_FOR_POLICY_UPDATE * 2);
        }
    }

    /**
     * 1. OC client is installed
     * <p/>
     * 1. Send 3 HTTP requests with parameters: Pattern [0,70,70]
     * Delay [65,65,65]
     * 2. Set such policy: asimov@transparent = 1
     * <p/>
     * 1. LP should be detected after 2nd  request and polling should start after receiving response.
     * 2. Response for the 3rd request should be received from cache.
     * 3. Policy should be received and applied.
     * 4. 3rd  request should be force HITed
     * 5. Stop poll should be sent to the server, RR should be deactivated
     *
     * @throws Throwable
     */
    public void test_011_LP_WithTransparent() throws Throwable {
        final String uri = createTestResourceUri("switching_to_the_transparent_mode_lp");

        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Sleep", "65").getRequest();

        funcForSwithchingTransparentMode(uri, null, null, request, 45 * 1000, 0, 2, 70 * 1000);
    }

    /**
     * 1. OC client is installed
     * 2. Policy should be configured:
     * asimov@http@cache_invalidate_aggressiveness=1
     * 3. Screen is switched off
     * <p/>
     * 1. Create 1st thread. Start polling with pattern [0,34,34]
     * 2.  Create 2nd  thread. Start polling with pattern [0,68,68]
     * 3.  Create 3rd   thread. Start polling with pattern [0,68,68], Delay [61,61,61]
     * 4.  After receiving response for 7th RMP request, change resources for all test suites.
     * 3. Turn screen ON in 15 sec after 10th RMP request
     * 4. Set such policy: asimov@transparent = 1 after receiving response for 14th  RMP request
     * 5. Observe client log.
     * <p/>
     * <p/>
     * 1. Policy should be received and added. 
     * 2. Verdicts for transactions and statuses for Subscriptions  in case RMP polls should be following:
     * -  3 MISS,  Start Poll, 7 HIT [with MD5(1)], 1 HIT [with MD5(2)], 1MISS, Start Poll, 2 HIT [with MD5(2)] Stop Poll
     * Verdicts for transactions and statuses for Subscriptions  in case RI polls should be following:
     * -  3 MISS,  Start Poll, 2 HIT [with MD5(1)], 1 HIT [with MD5(2)], 1MISS, Start Poll, Stop Poll
     * Verdicts for transactions and statuses for Subscriptions  in case LP polls should be following:
     * - 2 MISS,  Start Poll, 2 HIT [with MD5(1)], 1 HIT [with MD5(2)], 1MISS, Start Poll, Stop Poll
     *
     * @throws Throwable
     */
    @Ignore
    public void test_012_PollingsWithInvalidate() throws Throwable {
        Policy[] aggresiveness = {new Policy("cache_invalidate_aggresiveness", "1", "@asimov@http", true)};
        String[] pathes = {"@asimov@http", "@asimov"};


        PrepareResourceUtil.prepareResource(uriRI, false);
        PrepareResourceUtil.prepareResource(uriLP, false);
        PrepareResourceUtil.prepareResource(uriRLP, false);
        final long delay = 307 * 1000;
        try {
            PMSUtil.addPoliciesWithCheck(aggresiveness);
            ScreenUtils.startScreenSpy(AutomationTestsTab.context, new ScreenUtils.ScreenSpyResult(false));
            executeThreads(threadRMP, threadRI, threadLP,
                    invalidatingResources(delay, uriRI, uriLP, uriRLP));

        } finally {
            threadRMP.interruptSoftly();
            threadRI.interruptSoftly();
            threadLP.interruptSoftly();
            PMSUtil.cleanPaths(pathes);
            PrepareResourceUtil.invalidateResourceSafely(uriRI);
            PrepareResourceUtil.invalidateResourceSafely(uriLP);
            PrepareResourceUtil.invalidateResourceSafely(uriRLP);
        }
    }
}
