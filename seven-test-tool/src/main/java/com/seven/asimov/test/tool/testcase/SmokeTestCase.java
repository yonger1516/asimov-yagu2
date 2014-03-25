package com.seven.asimov.test.tool.testcase;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.util.Log;
import com.seven.asimov.it.asserts.CATFAssert;
import com.seven.asimov.it.base.*;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.*;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.cachingTasks.CERemovedFromBDTask;
import com.seven.asimov.it.utils.logcat.tasks.cachingTasks.CERemovedFromCashTask;
import com.seven.asimov.it.utils.logcat.tasks.cachingTasks.CERemovedFromFSTask;
import com.seven.asimov.it.utils.logcat.tasks.httpsTasks.FCLTask;
import com.seven.asimov.it.utils.logcat.tasks.policyTasks.PolicyAppliedTask;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import com.seven.asimov.it.utils.sms.SmsUtil;
import com.seven.asimov.it.utils.tcpdump.Interface;
import com.seven.asimov.it.utils.tcpdump.TcpDumpUtil;
import com.seven.asimov.test.tool.activity.AutomationTestsTab;
import com.seven.asimov.test.tool.utils.Assert7TestToolFailure;
import junit.framework.AssertionFailedError;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SmokeTestCase extends TcpDumpTestCase {
    private static final String TAG = SmokeTestCase.class.getSimpleName();

    protected TcpDumpUtil tcpDump = TcpDumpUtil.getInstance(AutomationTestsTab.context);

    protected static final String uriRI = createTestResourceUri("test_011_Pollings_RI", false);
    protected static final String uriLP = createTestResourceUri("test_011_Pollings_LP", false);
    protected static final String uriRLP = createTestResourceUri("test_011_Pollings_RLP", false);

    protected static String ocApkPath;
    protected ArrayList<String> processes = new ArrayList<String>();

    private static final String HTTPS_URI_SCHEME = "https://";
    public static String mPmsServerIp = "";
    public static String EXTERNAL_IP = "";
    public static int RELAY_PORT = 0;

    protected int Z7TP_SESSION_COUNT = 0;
    protected int HTTP_SESSION_COUNT = 0;
    protected final String DISPATCHER_MESSAGE = "Controller %s was crashed. Process";
    protected final String DISPATCHER_MESSAGE_PREFIX = " does not exists. ";

    protected int getZ7TpSessionCount() {
        Z7TP_SESSION_COUNT = tcpDump.getZ7TpPacketsCount(testStartTimestamp, testEndTimestamp);
        return Z7TP_SESSION_COUNT;
    }

    protected int getHttpSessionCount() {
        HTTP_SESSION_COUNT = tcpDump.getHttpSessionCount(testStartTimestamp, testEndTimestamp);
        return HTTP_SESSION_COUNT;
    }

    public static List<HttpRequest> createNumberOfrandomRequests(int number, String uri) throws Throwable {
        List<HttpRequest> requests = new ArrayList<HttpRequest>();
        for (int i = 0; i < number; i++) {
            requests.add(createRandomRequest(uri));
        }
        return requests;
    }

    protected TestCaseThread invalidatingResources(final long delay, final String... uries) {
        return new TestCaseThread() {
            @Override
            public void run() throws Throwable {
                Thread.sleep(delay);
                for (String uri : uries) {
                    PrepareResourceUtil.prepareResource(uri, true);
                }
            }
        };
    }

    protected static void killOcc() {
        Integer pid;
        Map<String, Integer> processes = OCUtil.getRunningProcesses(true);
        if (processes.get("occ") != null) {
            pid = processes.get("occ");
            String[] killPid = {"su", "-c", "kill " + pid};

            try {
                Runtime.getRuntime().exec(killPid);
                TestUtil.sleep(10 * 1000);
            } catch (IOException io) {
                Log.e(TAG, "Killing process is failed due to : " + ExceptionUtils.getStackTrace(io));
            }
        }
    }

    protected void apkInstalledCheck(String name) {
        try {
            AutomationTestsTab.pm.getPackageInfo(name, 0);
            if (name.equals("com.seven.asimov")) {
                File f = new File("/data/data/" + name + "/files");
                if (!f.exists()) {
                    throw new AssertionFailedError("Apk " + AutomationTestsTab.getApkName().getText().toString() + " was not installed");
                }
            }
            Log.i(TAG, "Apk with package name " + name + " installed");
        } catch (PackageManager.NameNotFoundException e) {
            throw new AssertionFailedError("Apk " + AutomationTestsTab.getApkName().getText().toString() + " was not installed. Exception: " +
                    e.getMessage());
        }
        Log.i(TAG, "Apk" + AutomationTestsTab.getApkName().getText().toString() + " was installed successfully");

        try {
            PackageInfo pi = AutomationTestsTab.pm.getPackageInfo(name, 0);
            if (pi == null) {
                throw new AssertionFailedError("Apk" + AutomationTestsTab.getApkName().getText().toString() + " was not installed");
            } else {
                Log.i(TAG, "Apk" + AutomationTestsTab.getApkName().getText().toString() + " was installed successfully");
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new AssertionFailedError("Apk" + AutomationTestsTab.getApkName().getText().toString() + " was not installed. Exception: " +
                    e.getMessage());
        }
    }

    public static void assertDispatcherCrash(String message, String prefix, String expected, String actual) {
        assertEquals(message, prefix, expected, actual, true, false);
    }

    public void cleanCheck() {
        checks.clear();
    }

    protected void processStartCheck(String name) {
        boolean result = false;
        List<ActivityManager.RunningAppProcessInfo> list2 = AutomationTestsTab.am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo ti : list2) {
            if (ti.processName.equals(name)) {
                result = true;
                Log.i(TAG, "Process " + name + " was found.");
            }
        }
        if (!result) {
            throw new AssertionFailedError("Process " + name + " was not found. Check that engine process was started");
        }
    }

    protected HttpResponse checkMiss(HttpRequest request, int requestId, int sleepTime) throws IOException, URISyntaxException {
        long startTime = System.currentTimeMillis();
        HttpResponse response = checkMiss(request, requestId);
        TestUtil.sleep(sleepTime - System.currentTimeMillis() + startTime);
        return response;
    }

    protected HttpResponse checkMiss(HttpRequest request, int requestId) throws IOException, URISyntaxException {
        return checkMiss(request, requestId, null);
    }

    protected HttpResponse checkMiss(HttpRequest request, int requestId, String expectedBody)
            throws IOException, URISyntaxException {
        return checkMiss(request, requestId, HttpStatus.SC_OK, expectedBody);
    }

    protected HttpResponse checkMiss(HttpRequest request, int requestId, String expectedBody, int sleepTime)
            throws IOException, URISyntaxException {
        long startTime = System.currentTimeMillis();
        HttpResponse response = checkMiss(request, requestId, HttpStatus.SC_OK, expectedBody);
        TestUtil.sleep(sleepTime - System.currentTimeMillis() + startTime);
        return response;
    }

    protected HttpResponse checkMiss(HttpRequest request, int requestId, int statusCode, String expectedBody)
            throws IOException, URISyntaxException {
        return checkMiss(request, requestId, statusCode, expectedBody, false, TIMEOUT);
    }

    protected HttpResponse checkMiss(HttpRequest request, int requestId, String expectedBody, boolean keepAlive,
                                     int timeout) throws IOException, URISyntaxException {
        return checkMiss(request, requestId, HttpStatus.SC_OK, expectedBody, keepAlive, timeout);
    }

    protected HttpResponse checkMiss(HttpRequest request, int requestId, int statusCode, String expectedBody,
                                     boolean keepAlive, int timeout) throws IOException, URISyntaxException {
        boolean isSslModeOn = false;
        String uri = request.getUri();
        if (uri.startsWith(HTTPS_URI_SCHEME)) isSslModeOn = true; // we are going to send HTTPS request
        logRequest(requestId, uri);
        HttpResponse response;
        long startTime = System.currentTimeMillis();
        Log.w(TAG, "Session start:" + startTime);
        if (isSslModeOn) {
            response = sendHttpsRequest(request, this);
        } else {
            response = sendRequest2(request, keepAlive, false, timeout);
        }
        long endTime = System.currentTimeMillis();
        long checkStartTime = System.currentTimeMillis();
        Log.w(TAG, "Session end:" + endTime);
        logResponse(requestId, ResponseLocation.NETWORK, response);
        CATFAssert.assertStatusCode(requestId, statusCode, response);
        if (expectedBody != null) {
            assertResponseBody(requestId, expectedBody, response, false);
            CATFAssert.assertResponseBody(requestId, expectedBody, response);
        }
        addMissCheck(requestId, request.getUri(), startTime, endTime);
        long checkEndTime = System.currentTimeMillis();
        response.setDuration(response.getDuration() + (checkEndTime - checkStartTime));
        return response;
    }

    protected HttpResponse checkHit(HttpRequest request, int requestId, int sleepTime) throws IOException, URISyntaxException {
        long startTime = System.currentTimeMillis();
        HttpResponse response = checkHit(request, requestId);
        TestUtil.sleep(sleepTime - System.currentTimeMillis() + startTime);
        return response;
    }


    protected HttpResponse checkHit(HttpRequest request, int requestId) throws IOException, URISyntaxException {
        return checkHit(request, requestId, null);
    }

    protected HttpResponse checkHit(HttpRequest request, int requestId, String expectedBody, boolean isInvalidate)
            throws IOException, URISyntaxException {
        return checkHit(request, requestId, HttpStatus.SC_OK, expectedBody, isInvalidate);
    }

    protected HttpResponse checkHit(HttpRequest request, int requestId, String expectedBody)
            throws IOException, URISyntaxException {
        return checkHit(request, requestId, HttpStatus.SC_OK, expectedBody, false);
    }

    protected HttpResponse checkHit(HttpRequest request, int requestId, String expectedBody, int sleepTime)
            throws IOException, URISyntaxException {
        long startTime = System.currentTimeMillis();
        HttpResponse response = checkHit(request, requestId, HttpStatus.SC_OK, expectedBody);
        TestUtil.sleep(sleepTime - System.currentTimeMillis() + startTime);
        return response;
    }


    protected HttpResponse checkHit(HttpRequest request, int requestId, int statusCode, String expectedBody, boolean isInvalidate)
            throws IOException, URISyntaxException {
        return checkHit(request, requestId, statusCode, expectedBody, false, TIMEOUT, isInvalidate);
    }

    protected HttpResponse checkHit(HttpRequest request, int requestId, String expectedBody, boolean keepAlive,
                                    int timeout, boolean isInvalidate) throws IOException, URISyntaxException {
        return checkHit(request, requestId, HttpStatus.SC_OK, expectedBody, keepAlive, timeout, isInvalidate);
    }

    protected HttpResponse checkHit(HttpRequest request, int requestId, int statusCode, String expectedBody,
                                    boolean keepAlive, int timeout, boolean isInvalidate) throws IOException, URISyntaxException {
        boolean isSslModeOn = false;
        String uri = request.getUri();
        if (uri.startsWith(HTTPS_URI_SCHEME)) isSslModeOn = true; // we are going to send HTTPS request
        logRequest(requestId, request.getUri());
        HttpResponse response;
        long startTime = System.currentTimeMillis();
        Log.w(TAG, "Session start:" + startTime);
        if (isSslModeOn) {
            response = sendHttpsRequest(request, this);
        } else {
            response = sendRequest2(request, keepAlive, false, timeout);
        }
        long endTime = System.currentTimeMillis();
        Log.w(TAG, "Session end:" + endTime);
        logResponse(requestId, ResponseLocation.NETWORK, response);
        CATFAssert.assertStatusCode(requestId, statusCode, response);
        if (expectedBody != null) {
            assertResponseBody(requestId, expectedBody, response, isInvalidate);
        }
        addHitCheck(requestId, request.getUri(), startTime, endTime);
        return response;
    }

    protected HttpResponse checkTransient(HttpRequest request, int requestId, String expectedBody, long startTime)
            throws IOException, URISyntaxException {
        return checkTransient(request, requestId, HttpStatus.SC_OK, expectedBody, startTime);
    }

    protected HttpResponse checkTransient(HttpRequest request, int requestId, int statusCode, String expectedBody,
                                          long startTime) throws IOException, URISyntaxException {
        // There should be no http activity
        HttpResponse response = checkHit(request, requestId, statusCode, expectedBody);
        addCheckTransient(requestId, startTime, System.currentTimeMillis());
        return response;
    }

    protected HttpResponse checkTransient(HttpRequest request, int requestId, long startTime) throws IOException, URISyntaxException {
        return checkTransient(request, requestId, null, startTime);
    }

    protected int checkMissHit(List<HttpRequest> requests, int startRequestId) throws Throwable {
        return checkMissHit(requests, startRequestId, null);
    }

    protected int checkMissHit(List<HttpRequest> requests, int startRequestId, String expectedBody) throws Throwable {
        return checkMissHit(requests, startRequestId, expectedBody, false);
    }

    protected int checkMissHit(List<HttpRequest> requests, int startRequestId, String expectedBody,
                               boolean checkDnsActivity) throws Throwable {
        return checkMissHit(requests, startRequestId, HttpStatus.SC_OK, expectedBody, checkDnsActivity);
    }

    protected int checkMissHit(List<HttpRequest> requests, int startRequestId, int statusCode, String expectedBody,
                               boolean checkDnsActivity) throws Throwable {
        for (HttpRequest request : requests) {
            startRequestId = checkMissHit(request, startRequestId, statusCode, expectedBody, checkDnsActivity);
        }
        return startRequestId;
    }

    protected int checkMissHit(HttpRequest request, int startRequestId, int statusCode, String expectedBody,
                               boolean checkDnsActivity) throws Throwable {
        // this request shall be cached
        HttpResponse resp = checkMiss(request, startRequestId, statusCode, expectedBody);
        if (checkDnsActivity) {
//            addDnsReqActivityCheck(startRequestId, resp, true);  // TODO:
        }
        startRequestId++;
        // this request shall be returned from cache
        resp = checkHit(request, startRequestId, statusCode, expectedBody);
        if (checkDnsActivity) {
//            addDnsReqActivityCheck(startRequestId, resp, false); // TODO:
        }
        startRequestId++;
        return startRequestId;
    }

    protected int checkMissHit(HttpRequest request, List<String> uris, int startRequestId, String expectedBody,
                               boolean checkDnsActivity) throws Throwable {
        for (String uri : uris) {
            request.setUri(uri);
            startRequestId = checkMissHit(request, startRequestId, HttpStatus.SC_OK, expectedBody, checkDnsActivity);
        }
        return startRequestId;
    }

    protected int checkPoll(HttpRequest request, int startRequestId, int hitcounts, long sleepTime) throws Throwable {
        return checkPoll(request, startRequestId, hitcounts, sleepTime, null);
    }

    protected int checkPoll(HttpRequest request, int startRequestId, int hitcounts, long sleepTime, String expectedBody)
            throws Throwable {
        return checkPoll(request, startRequestId, hitcounts, sleepTime, HttpStatus.SC_OK, expectedBody);
    }

    protected int checkPoll(HttpRequest request, int startRequestId, int hitcounts, long sleepTime, int statusCode,
                            String expectedBody) throws Throwable {
        HttpResponse response = checkMiss(request, startRequestId++, statusCode, expectedBody);
        logSleeping(sleepTime - response.getDuration());

        response = checkMiss(request, startRequestId++, statusCode, expectedBody);
        logSleeping(sleepTime - response.getDuration());

        response = checkMiss(request, startRequestId++, statusCode, expectedBody);
        logSleeping(sleepTime - response.getDuration());

        for (int i = 0; i < hitcounts; i++) {
            // this request shall be returned from cache
            response = checkHit(request, startRequestId++, statusCode, expectedBody);
            logSleeping(sleepTime - response.getDuration());
        }
        return startRequestId;
    }

    public static void assertResponseBody(int responseId, String expected, HttpResponse response, boolean isInvalidate) {
        if (isInvalidate) {
            assertEquals(String.format("On response " + responseId + " expected new data in cache but IWC was not " +
                    "received or processed", expected, response.getBody()), "", expected, response.getBody(), false, false);
        } else {
            assertEquals(String.format("Response " + responseId + " expected with body \"%s\" but was received response " +
                    "contains body \"%s\"", expected, response.getBody()), "", expected, response.getBody(), false, false);
        }
    }

    protected void switchNetwork(int type) throws Throwable {
        MobileNetworkUtil mobileNetworkHelper = MobileNetworkUtil.init(AutomationTestsTab.context);

        Log.e(TAG, "Enter to SwitchNetwork");
        WifiManager wifiManager = (WifiManager) AutomationTestsTab.context.getSystemService(Context.WIFI_SERVICE);
        Log.e(TAG, "Step 1");
        switch (type) {
            case 1:
                Log.e(TAG, "WIFI");
                if (!wifiManager.isWifiEnabled()) {
                    mobileNetworkHelper.switchMobileDataOnOff(false);
                    Log.e(TAG, "Activate WIFI");
                    wifiManager.setWifiEnabled(true);
                }
                Thread.sleep(25 * 1000);
                Log.e(TAG, "WIFI request send");
                sendHttpRequests(2);
                break;
            case 2:
                Log.e(TAG, "3G");
                if (wifiManager.isWifiEnabled()) {
                    Log.e(TAG, "Activate 3G");
                    wifiManager.setWifiEnabled(false);
                    mobileNetworkHelper.switchMobileDataOnOff(true);
                }
                Thread.sleep(25 * 1000);
                Log.e(TAG, "3G request send");
                sendHttpRequests(2);
                break;
            default:
                Log.v(TAG, "Chosen network type is unreacheable in your region");
                break;
        }
    }

    protected String name = "bypass_list";
    protected String path = "@asimov@interception";
    protected static HashMap<Integer, HttpRequest> requests = new HashMap();
    public static List<CustomService> services = new ArrayList<CustomService>();

    /**
     * @param requests            - http requests to different ports
     * @param configValue         - policy value
     * @param expectedHttpCount   - expected count of loopback http sessions
     * @param expectedHttpsCount  - // -
     * @param expectedCustomCount -//-
     * @param checks              - configured set of needed checks
     * @throws Exception
     */
    protected void bypathPortTest(HashMap<Integer, HttpRequest> requests, String configValue, int expectedHttpCount,
                                  int expectedHttpsCount, int expectedCustomCount, boolean[] checks) throws Exception {

        boolean configurateByPolicy = checks[0];
        boolean removePolicy = checks[1];
        boolean shouldBeInDispatCfg = checks[2];
        boolean shouldbeInIptables = checks[3];

        FCLTask fclHttpsTask = new FCLTask();
        LogcatUtil logcat;
        try {
            if (configValue != null) {
                if (configurateByPolicy) {
                    PMSUtil.addPolicies(new Policy[]{new Policy(name, configValue, path, true)});
                    TestUtil.sleep(27000);
                }
                if (removePolicy && checkIpTableCfg()) {
                    PMSUtil.cleanPaths(new String[]{path});
                    (new SmsUtil(AsimovTestCase.getStaticContext())).sendPolicyUpdate((byte) 1);
                }
            }
            TestUtil.sleep(TFConstantsIF.MINUTE);

            int local_hs = 0;
            if (requests != null) {
                long startTime = System.currentTimeMillis();
                for (Map.Entry<Integer, HttpRequest> entry : requests.entrySet()) {
                    logger.info("KEY:" + entry.getKey().intValue());
                    switch (entry.getKey().intValue()) {
                        case 443: {
                            logger.info("case 443");
                            sendHttpsRequest(entry.getValue());
                            break;
                        }
                        case 80: {
                            logger.info("case 80");
                            sendRequest2(entry.getValue());
                            break;
                        }
                        default: {
                            logger.info("case default");
                            logcat = new LogcatUtil(AutomationTestsTab.context, fclHttpsTask);
                            logcat.start();
                            sendHttpsRequest(entry.getValue());
                            logcat.stop();
                            if (fclHttpsTask.getLogEntries() != null) {
                                local_hs += fclHttpsTask.getLogEntries().size();
                            }
                        }
                    }
                }

                long stopTime = System.currentTimeMillis();

                int actualHttpCount = 0;
                int actualHttpsCount = 0;

                for (Map.Entry<Integer, HttpRequest> entry : requests.entrySet()) {
                    if (entry.getKey().equals(80)) {
                        actualHttpCount += tcpDump.getHttpSessions(entry.getValue().getUri(), AsimovTestCase.TEST_RESOURCE_HOST, Interface.LOOPBACK, startTime, stopTime).size();
                    } else if (entry.getKey().equals(443)) {
                        actualHttpsCount += tcpDump.getHttpsSessions(AsimovTestCase.TEST_RESOURCE_HOST, Interface.LOOPBACK, startTime, stopTime).size();
                    }
                }
                assertEquals("HttpSession check", expectedHttpCount, actualHttpCount);
                assertEquals("HttpsSession check", expectedHttpsCount, actualHttpsCount);
                assertEquals("CustomSession check", expectedCustomCount, local_hs);
            }
            assertEquals("Dispatchers check", shouldBeInDispatCfg, checkDispatcherCfg(configValue));
            assertEquals("IpTables check", shouldbeInIptables, checkIpTableCfg());

        } finally {
            PMSUtil.cleanPaths(new String[]{path});
            services.clear();
        }
    }

    protected boolean checkDispatcherCfg(String policy) throws IOException, InterruptedException {

        BufferedReader bufferedReader = new BufferedReader(new FileReader(TFConstantsIF.DISPATCHERS_CFG_PATH));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.equals(policy + ";")) return true;
        }
        return false;
    }

    protected boolean checkIpTableCfg() throws IOException {

        String UID = null;
        String psRegexsp = "(u0_[a-z0-9]*).*com.seven.asimov.it";

        Matcher matcher;
        Process process = Runtime.getRuntime().exec("ps");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            logger.info(line);
            matcher = Pattern.compile(psRegexsp).matcher(line);
            if (matcher.find()) {
                UID = matcher.group(1);
            }
        }

        logger.info("UID_com.seven.asimov.test.tool" + UID);
        return findIptableRule(UID, true) && findIptableRule(UID, false);
    }

    /**
     * @param UID      - application UID
     * @param ip4table - if true - search in iptables, else - in ip6tables
     * @return
     */
    protected boolean findIptableRule(String UID, boolean ip4table) {

        Matcher matcher;
        String ipTableRegexsp = "CONNMARK\\s*all.*owner\\sUID\\smatch\\s" + UID + ".*";
        logger.info("UID_com.seven.asimov.test.tool" + UID);
        List<String> command = new ArrayList<String>();
        if (ip4table) {
            command.add(TFConstantsIF.IPTABLES_PATH + " -t mangle -L Z7BASECHAIN-prior");
        } else {
            command.add(TFConstantsIF.IP6TABLES_PATH + " -t mangle -L Z7BASECHAIN-prior");
        }
        List<String> output = ShellUtil.execWithCompleteResultWithListOutput(command, true);
        for (String str : output) {
            logger.info(str);
            matcher = Pattern.compile(ipTableRegexsp).matcher(str);
            if (matcher.find()) {
                return true;
            }
        }
        return false;
    }

    private static final Logger logger = LoggerFactory.getLogger(SmokeTestCase.class.getSimpleName());
    private final String POLICY_NAMESPACE = "@asimov";
    private final String POLICY_NAME = "transparent";
    private HttpResponse response;

    private Callable<Void> transparentModeOn(final long sleep) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                PolicyAppliedTask policyAppliedTask = new PolicyAppliedTask("transparent", "1");
                LogcatUtil chekPolicy = new LogcatUtil(AutomationTestsTab.context, policyAppliedTask);
                logger.info("transparentModeOn log start");
                chekPolicy.start();

                final Policy transparent = new Policy(POLICY_NAME, "1", POLICY_NAMESPACE, true);
                PMSUtil.addPolicies(new Policy[]{transparent});
                SmsUtil.sendPolicyUpdate(AutomationTestsTab.context, (byte) 1);
                logSleeping(sleep);

                logger.info("transparentModeOn log stop");
                chekPolicy.stop();
                assertFalse("policies wasn't set right", policyAppliedTask.getLogEntries().isEmpty());
                logger.info("transparentModeOn end");
                return null;
            }
        };
    }

    private Callable<Void> transparentModeOff(final long sleep) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                PolicyAppliedTask policyAppliedTask = new PolicyAppliedTask("transparent", "0");
                LogcatUtil chekPolicy = new LogcatUtil(AutomationTestsTab.context, policyAppliedTask);
                logger.info("transparentModeOff log start");
                chekPolicy.start();

                final Policy transparent = new Policy(POLICY_NAME, "0", POLICY_NAMESPACE, true);
                PMSUtil.addPolicies(new Policy[]{transparent});
                SmsUtil.sendPolicyUpdate(AutomationTestsTab.context, (byte) 1);
                logSleeping(sleep);

                logger.info("transparentModeOff log stop");
                chekPolicy.stop();
                assertFalse("policies wasn't set right", policyAppliedTask.getLogEntries().isEmpty());
                logger.info("transparentModeOff end");
                return null;
            }
        };
    }


    protected void funcForSwithchingTransparentMode(final String uri, final String encodedRawHeadersDef, final String encodedRawHeadersDef304, final HttpRequest request, final long sleepOn, final long sleepOff,
                                                    int reqCount, long sleepIT) throws Throwable {
        CERemovedFromBDTask cebd = new CERemovedFromBDTask();
        CERemovedFromCashTask cecash = new CERemovedFromCashTask();
        CERemovedFromFSTask fsTask = new CERemovedFromFSTask();

        LogcatUtil logcat = new LogcatUtil(AutomationTestsTab.context, cebd, cecash, fsTask);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        //Thread with change policy
        Callable<Void> setPolicy = transparentModeOn(sleepOn);
        Callable<Void> trasparentOff = transparentModeOff(sleepOff);

        Runnable status304 = new Runnable() {
            @Override
            public void run() {
                try {
                    PrepareResourceUtil.prepareResource304(uri, encodedRawHeadersDef304);
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            }
        };
        Runnable status200 = new Runnable() {
            @Override
            public void run() {
                try {
                    PrepareResourceUtil.prepareResource200(uri, false, encodedRawHeadersDef);
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            }
        };
        Thread status;
        try {
            logcat.start();
            if (sleepOff > 0) {
                PrepareResourceUtil.prepareResource200(uri, false, encodedRawHeadersDef);
            } else {
                PrepareResourceUtil.prepareResource(uri, false);
            }
            int requestId = 1;
            if (encodedRawHeadersDef304 != null) {
                // 1 MISS - 200 OK
                checkMiss(request, requestId++);
                status = new Thread(status304);
                status.start();
                logSleeping(40 * 1000);

                // 2 MISS - 304
                checkMiss304(request, requestId++);
                logSleeping(40 * 1000);

                // 3 MISS - 304
                checkMiss304(request, requestId++);
                logSleeping(40 * 1000);

                // 4 HIT - 304
                checkHit304(request, requestId++);
            } else {
                for (int i = 0; i < reqCount; i++) {
                    response = checkMiss(request, requestId++);
                    TestUtil.sleep(sleepIT - response.getDuration());
                }
                response = checkHit(request, requestId++);
            }
            //start changing policy thread
            Future<Void> policyResult = executorService.submit(setPolicy);
            executorService.shutdown();
            logger.info("get future");
            policyResult.get();

            if (sleepOff > 0) {
                // transparent OFF
                executorService = Executors.newSingleThreadExecutor();
                Future<Void> offResult = executorService.submit(trasparentOff);
                executorService.shutdown();
                TestUtil.sleep(60 * 1000);
                logger.info("get \"OFF\"future");
                offResult.get();
            }
            checkMiss(request, requestId);
            TestUtil.sleep(60 * 1000);
            logcat.stop();

            assertFalse("CE should be deleted", cecash.getLogEntries().isEmpty());
            assertTrue("CE wasn't removed from all resources " + cecash.getLogEntries().get(0), cecash.getLogEntries().get(0).isAllRemoved());

        } finally {
            if (logcat.isRunning()) {
                logcat.stop();
            }
            final Policy transparent = new Policy(POLICY_NAME, "0", POLICY_NAMESPACE, true);
            PMSUtil.addPolicies(new Policy[]{transparent});
            //SmsUtil.sendPolicyUpdate(AutomationTestsTab.context, (byte) 1);
            TestUtil.sleep(27 * 1000);
            executorService.shutdownNow();
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    public static HttpRequest createRandomRequest(String uri) throws Throwable {

        String[] characters = {"This is the house that Jack built.",
                "This is the malt",
                "That lay in the house that Jack built.",
                "This is the rat",
                "That ate the malt",
                "That lay in the house that Jack built",
                "This is the cat",
                "That killed the rat",
                "That ate the malt",
                "That lay in the house that Jack built"};
        Random randomNumberGenerator = new Random();

        String header = characters[randomNumberGenerator.nextInt(characters.length)];

        HttpRequest request = createRequest()
                .setMethod("GET")
                .setUri(uri)
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("SomeHeader", header)
                .getRequest();

        return request;
    }

    protected void sendHttpRequests(int number) throws Throwable {

        String pathEnd = "test_asimov";
        String uri = createTestResourceUri(pathEnd);
        PrepareResourceUtil.prepareResource(uri, false);
        try {
            for (int i = 1; i < number + 1; i++) {
                sendRequest2(createRandomRequest(uri));
            }
        } catch (SocketTimeoutException socketTimeoutException) {
            Log.e(TAG, "Connection failed due to " + ExceptionUtils.getStackTrace(socketTimeoutException));
        } catch (AssertionFailedError assertationFailedError) {
            Log.e(TAG, "Assertation failed error " + ExceptionUtils.getStackTrace(assertationFailedError));
        } catch (UnknownHostException unknownHostException) {
            Log.e(TAG, "UnknownHostException" + ExceptionUtils.getStackTrace(unknownHostException));
        } finally {
//            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    protected static Thread killHttpProcess = new Thread() {
        @Override
        public void run() {
            TestUtil.sleep(300);
            Integer pid;
            Map<String, Integer> processes = OCUtil.getRunningProcesses(true);
            if (processes.get("ochttpd") != null || processes.get("http") != null) {
                pid = processes.get("ochttpd");
                Log.i(TAG, "Process ochttpd = " + pid);
                String[] killPid = {"su", "-c", "kill " + pid};

                try {
                    Runtime.getRuntime().exec(killPid);
                    TestUtil.sleep(400);
                } catch (IOException io) {
                    Log.e(TAG, "Killing process is failed due to : " + ExceptionUtils.getStackTrace(io));
                }
            }
        }
    };

    protected TestCaseThread threadRMP = new TestCaseThread() {
        @Override
        public void run() throws Throwable {
            HttpRequest httpRequest = createRequest().setUri(uriRLP).addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
            HttpResponse response;
            int counter = 0;
            long DELAY = 34 * 1000;
            //#1
            response = checkMiss(httpRequest, ++counter, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());
            //#2
            response = checkMiss(httpRequest, ++counter, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());
            //#3
            response = checkMiss(httpRequest, ++counter, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());
            //#4
            response = checkHit(httpRequest, ++counter, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());
            //#5
            response = checkHit(httpRequest, ++counter, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());
            //#6
            response = checkHit(httpRequest, ++counter, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());
            //#7
            response = checkHit(httpRequest, ++counter, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());
            //#8
            response = checkHit(httpRequest, ++counter, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());
            //#9
            response = checkHit(httpRequest, ++counter, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());
            //#10
            response = checkHit(httpRequest, ++counter, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());
            //#11 with new body
            response = checkHit(httpRequest, ++counter, INVALIDATED_RESPONSE);
            logSleeping(DELAY - response.getDuration());
            //#12
            response = checkMiss(httpRequest, ++counter, INVALIDATED_RESPONSE);
            logSleeping(DELAY - response.getDuration());
            //#13
            response = checkHit(httpRequest, ++counter, INVALIDATED_RESPONSE);
            logSleeping(DELAY - response.getDuration());
            //#14
            response = checkHit(httpRequest, ++counter);
            logSleeping(DELAY - response.getDuration());
        }
    };


    //    private TestCaseThread threadLP(final String uri, final int DELAY) {
    protected TestCaseThread threadLP = new TestCaseThread() {
        @Override
        public void run() throws Throwable {
            HttpRequest httpRequest = createRequest().setUri(uriLP).addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("X-OC-Sleep", "61").addHeaderField("X-OC-Stateless-Sleep", "true").getRequest();
            HttpResponse response;
            int counter = 0;
            long DELAY = 68 * 1000;
            try {
                //#1
                response = checkMiss(httpRequest, ++counter, VALID_RESPONSE);
                logSleeping(DELAY - response.getDuration());
                //#2
                response = checkMiss(httpRequest, ++counter, VALID_RESPONSE);
                logSleeping(DELAY - response.getDuration());
                //#3
                response = checkHit(httpRequest, ++counter, VALID_RESPONSE);
                logSleeping(DELAY - response.getDuration());
                //#4
                response = checkHit(httpRequest, ++counter, VALID_RESPONSE);
                logSleeping(DELAY - response.getDuration());
                //#5 with new body
                response = checkHit(httpRequest, ++counter, INVALIDATED_RESPONSE);
                logSleeping(DELAY - response.getDuration());
                //#6
                response = checkMiss(httpRequest, ++counter, INVALIDATED_RESPONSE);
                logSleeping(DELAY - response.getDuration());
            } finally {
                interruptSoftly();
            }
        }
    };
//    }

    protected TestCaseThread threadRI = new TestCaseThread() {
        @Override
        public void run() throws Throwable {
            HttpRequest httpRequest = createRequest().setUri(uriRI).addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
            HttpResponse response;
            int counter = 0;
            long DELAY = 68 * 1000;
            try {
                //#1
                response = checkMiss(httpRequest, ++counter, VALID_RESPONSE);
                logSleeping(DELAY - response.getDuration());
                //#2
                response = checkMiss(httpRequest, ++counter, VALID_RESPONSE);
                logSleeping(DELAY - response.getDuration());
                //#3
                response = checkMiss(httpRequest, ++counter, VALID_RESPONSE);
                logSleeping(DELAY - response.getDuration());
                //#4
                response = checkHit(httpRequest, ++counter, VALID_RESPONSE);
                logSleeping(DELAY - response.getDuration());
                //#5
                response = checkHit(httpRequest, ++counter, VALID_RESPONSE);
                logSleeping(DELAY - response.getDuration());
                //#5 with new body
                response = checkHit(httpRequest, ++counter, INVALIDATED_RESPONSE);
                logSleeping(DELAY - response.getDuration());
                //#6
                response = checkMiss(httpRequest, ++counter, INVALIDATED_RESPONSE);
                logSleeping(DELAY - response.getDuration());
                //#7
                response = checkHit(httpRequest, ++counter, INVALIDATED_RESPONSE);
                logSleeping(DELAY - response.getDuration());

            } finally {
                interruptSoftly();
            }
        }
    };

    /**
     * Asserts that two Strings are equal.
     */
    public static void assertEquals(String message, String prefix, String expected, String actual, boolean firstValue, boolean secondValue) {
        if (expected == null && actual == null)
            return;
        if (expected != null && expected.equals(actual))
            return;
        throw new Assert7TestToolFailure(message, prefix, expected, actual, firstValue, secondValue);
    }

    public static void setOcApkPath(String ocApkPath) {
        SmokeTestCase.ocApkPath = ocApkPath;
    }

    public static void checkDispatcersConfiguration(boolean ShouldBeChanged) throws IOException, InterruptedException {

        String octcpd = "1:24,26:109,111:219,221:464,466:586,588:992,994,996:7734,7736:8086,8088:8098,8100:8110,8112:65535";
        String ocshttpd = "443";
        String ochttpd = "80";
        String ocdnsd = "53";
        String neededLine = "([a-z]*);([1-9]);(.*);;([0-9]*)";

        if (ShouldBeChanged) {
            ocshttpd = "80";
            ochttpd = "443";
        }
        Pattern neededLinePattern = Pattern.compile(neededLine);
        Matcher neededLineMatcher;
        String line;

        String[] chmodDirectory = {"su", "-c", "chmod -R 777 /data/"};
        Runtime.getRuntime().exec(chmodDirectory).waitFor();
        Thread.sleep(5000);
        BufferedReader bufferedReader = new BufferedReader(new FileReader("/data/misc/openchannel/dispatchers.cfg"));
        try {
            while ((line = bufferedReader.readLine()) != null) {
                neededLineMatcher = neededLinePattern.matcher(line);
                if (neededLineMatcher.find()) {
                    String dispatcherName = neededLineMatcher.group(1);
                    if (dispatcherName.equals("octcpd")) {
                        assertTrue("The configuration of octcpd dispatcher is not correct", neededLineMatcher.group(3).equals(octcpd));
                    }
                    if (dispatcherName.equals("ocshttpd")) {
                        assertTrue("The configuration of ocshttpd dispatcher is not correct", neededLineMatcher.group(3).equals(ocshttpd));
                    }
                    if (dispatcherName.equals("ochttpd")) {
                        assertTrue("The configuration of ochttpd dispatcher is not correct", neededLineMatcher.group(3).equals(ochttpd));
                    }
                    if (dispatcherName.equals("ocdnsd")) {
                        assertTrue("The configuration of ocdnsd dispatcher is not correct", neededLineMatcher.group(3).equals(ocdnsd));
                    }
                }
            }
        } catch (IOException io) {
            throw new IOException();
        } finally {
            bufferedReader.close();
        }
    }


}