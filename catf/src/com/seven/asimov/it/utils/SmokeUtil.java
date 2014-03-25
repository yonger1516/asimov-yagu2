package com.seven.asimov.it.utils;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.Base64;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.logcat.wrappers.LogEntryWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.StartPollWrapper;
import com.seven.asimov.it.utils.sms.SmsUtil;
import com.seven.asimov.it.utils.tcpdump.HttpSession;
import com.seven.asimov.it.utils.tcpdump.TcpDumpUtil;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.*;

public final class SmokeUtil {

    private static SmokeUtil instance;

    private static final List<String> envProblems = new ArrayList<String>();
    private static final List<TestResult> smokeTests = new ArrayList<TestResult>();
    private static Map<String, Integer> processes;
    private static List<Throwable> exceptions = new ArrayList<Throwable>();
//    private static final LogUtil logger = LogUtil.getInstance();
    private static Random random = new Random();
    private Context context;

    private final static String CRASH_MESSAGE = "OC crash was detected!";
    private static final Logger logger = LoggerFactory.getLogger(SmokeUtil.class.getSimpleName());

    private final static int INFO = 0;
    private final static int WARNING = 1;
    private final static int ERROR = 2;

    private static boolean environmentReady = true;
    private static boolean environmentChecked = false;
//    private long testStartTimestamp;
//    private long testEndTimestamp;

    private SmokeUtil(Context context) {
        this.context = context;
    }

    /**
     * getiing instance of SmokeHelper
     *
     * @return instance of SmokeHelper
     */
    public static SmokeUtil getInstance(Context context) {
        if (instance == null) {
            synchronized (SmokeUtil.class) {
                if (instance == null) {
                    instance = new SmokeUtil(context);
                }
            }
        }
        return instance;
    }


    /**
     * Method which check state of environment
     *
     * @return <code>true</code> if environment is ready,<code>false</code> id detect crash
     */
    public boolean isEnvironmentReady(final Context context) {
        if (!environmentChecked) {
            environmentReady = checkEnvReady(context);
            environmentChecked = true;
        }
        return environmentReady;
    }

    /**
     * Method which run command of update IPtables
     *
     * @return updated iptables
     */
    private static String getIptablesChainState() {
        String[] getOutputCmdArr = new String[]{"/system/bin/iptables", "-t", "nat", "-L", "Z7BASECHAIN", "-n"};
        return ShellUtil.execWithCompleteResult(Arrays.asList(getOutputCmdArr), true);
    }

    /**
     * Method which block relay server for roaming failover test
     *
     * @throws Exception
     */
    public void blockRelayServer() throws Exception {
        final String[] UNREACHABLE_RELAY_SERVER = {"su", "-c", TFConstantsIF.IPTABLES_PATH + " -t filter -I INPUT -m conntrack --ctorigdstport 7735 -j REJECT "};
        Runtime.getRuntime().exec(UNREACHABLE_RELAY_SERVER).waitFor();
        TestUtil.sleep(3000);
    }

    /**
     * Method which unblock relay server, after blocking
     *
     * @throws Exception
     */
    public void unblockRelayServer() throws Exception {
        String[] avaliableRelayServer = {"su", "-c", TFConstantsIF.IPTABLES_PATH + " -t filter -D -m conntrack --ctorigdstport 7735 -j REJECT "};
        Runtime.getRuntime().exec(avaliableRelayServer).waitFor();
        TestUtil.sleep(3000);
    }

    /**
     * Method which check connection
     *
     * @param host checking host
     * @param port checking port
     * @return state of connection <code>true</code> - available, <code>false</code> - unavailable
     */
    private boolean checkConnectionAvailable(String host, int port) {
        boolean isConnectionsAvailable = true;
        int waitTimeout = 10 * 1000;
        Socket socket = new Socket();
        try {
            InetSocketAddress address = new InetSocketAddress(host, port);
            socket.connect(address, waitTimeout);
            if (!socket.isConnected() || address.isUnresolved()) {
                isConnectionsAvailable = false;
            }
        } catch (IOException e) {
            isConnectionsAvailable = false;
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
        return isConnectionsAvailable;
    }

    /**
     * Method which check that PIDs OC have not changed
     *
     * @param output <code>true</code> - write OC process to logcat
     */
    public static void verifyPidsNotChanged(boolean output) {
        Map<String, Integer> actualProcesses = OCUtil.getOcProcesses(output);
        for (String ps : processes.keySet()) {
            if (!(actualProcesses.containsKey(ps) && actualProcesses.get(ps).equals(processes.get(ps)))) {
                log(CRASH_MESSAGE, ERROR);
                smokeTests.add(new TestResult("Crash check", Result.FAILED, CRASH_MESSAGE));
                updateProcesses();
            }
        }
    }

    /**
     * Update OC process
     */
    public static void updateProcesses() {
        processes = OCUtil.getOcProcesses(true);
    }

    /**
     * Adding test result to list, if test failed
     *
     * @param testName - name of test
     * @param result - test result
     * @param failDescription - description of fail
     */
    public static void addResult(String testName, Result result, String failDescription) {
        smokeTests.add(new TestResult(testName, result, failDescription));
    }

    /**
     * Adding test results to list, if tests are passed
     *
     * @param testName - name of test
     */
    public static void addResult(String testName) {
        addResult(testName, Result.PASSED, "-");
    }

    /**
     * Printing environment problems
     *
     * @return problems
     */
    public static String printEnvProblemsSummary() {
        String message = "Test skipped by reason of environment problems: ";
        log(message, SmokeUtil.ERROR);
        logger.error(message);
        for (String s : envProblems) {
            log(s, SmokeUtil.ERROR);
            logger.error(s);
        }
        return envProblems.get(0);
    }

    /**
     *
     * Method which print summary of test to HTML report
     */
    public static void printSummary() {
        if (smokeTests.isEmpty()) {
            return;
        }
        Collections.sort(smokeTests);
        BufferedReader br = null;
        BufferedWriter writer = null;
        StringBuilder page = new StringBuilder();
        File reportIndex = new File(OCUtil.getHtmlReportTargetDir() + "/index.html");
        try {
            br = new BufferedReader(new FileReader(reportIndex));
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().equals("</body></html>")) {
                    page.append(line);
                }
            }
            page.append("<br><center><h3>Tests summary</h3><table border=\"1\" width=\"80%\">");
            page.append("<tr><td><b>Test name</b></td><td><b>Test result</b></td><td><b>Failure reason</b></td></tr>");
            for (TestResult result : smokeTests) {
                page.append("<tr><td>").append(result.getTestName()).append("</td><td>").append(result.getResult()).append("</td><td>").append(result.getFailDescription()).append("</td></tr>");
            }
            page.append("</table></center></body></html>");

            writer = new BufferedWriter(new FileWriter(reportIndex, false));
            writer.write(page.toString());
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Write error " + ExceptionUtils.getStackTrace(e));
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error("Error while close read stream " + ExceptionUtils.getStackTrace(e));
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error("Error while close write steam " + ExceptionUtils.getStackTrace(e));
                }
            }
        }
    }

    /**
     * SmokeHelper logger
     *
     * @param message which will print
     */
    public static void log(String message) {
        log(message, INFO);
    }

    /**
     * SmokeHelper logs level
     *
     * @param message - log message
     * @param warnLevel - log level
     */
    public static void log(String message, Integer warnLevel) {
        logger.debug(message);
        switch (warnLevel) {
            case 0:
                logerInfo(message);
                break;
            case 1:
                logerInfo("<b>" + message + "</b>");
                break;
            case 2:
                logerInfo("<b><font color=\"red\">" + message + "</font></b>");
                break;
            default:
        }
    }

    /**
     * Method which check state of environment
     *
     * @return <code>true</code> - if OK, <code>false</code> - detected crash
     */
    private boolean checkEnvReady(final Context context) {

        CheckedThread checkOcAlive = new CheckedThread() {
            boolean isOcAlive = true;

            @Override
            public void run() {
                int time = 0;
                // Check every 5 sec during 1 min that OC is running and there are no crashes
                while (time <= 60) {
                    long start = System.currentTimeMillis();
                    if (!OCUtil.isOpenChannelRunning()) {
                        String message = "OC crash detected!";
                        log(message, ERROR);
                        logger.error(message);
                        envProblems.add(message);
                        isOcAlive = false;
                        break;
                    }
                    time += 5;
                    TestUtil.sleep(5 * 1000 - (System.currentTimeMillis() - start));
                }
            }

            @Override
            public boolean isCheckSuccess() {
                if (isOcAlive) {
                    String message = "No crashes detected on startup";
                    log(message, INFO);
                    logger.info(message);
                }
                return isOcAlive;
            }
        };

        CheckedThread checkFreeSpace = new CheckedThread() {
            boolean isSpaceEnough = true;

            @Override
            public void run() {
                double minAcceptableCardRatio = 30;
                double minAcceptableInternalRatio = 30;

                StatFs sdCardStats = new StatFs("/mnt/sdcard");
                int sdCardBlockSize = sdCardStats.getBlockSize();
                int totalSdCardBlocks = sdCardStats.getBlockCount();
                int freeSdCardBlocks = sdCardStats.getAvailableBlocks();
                double freeSdCardRatio = (double) freeSdCardBlocks / totalSdCardBlocks * 100;

                StatFs internalStats = new StatFs(Environment.getDataDirectory().getPath());
                int internalBlockSize = internalStats.getBlockSize();
                int totalInternalBlocks = internalStats.getBlockCount();
                int freeInternalBlocks = internalStats.getAvailableBlocks();
                double freeInternalRatio = (double) freeInternalBlocks / totalInternalBlocks * 100;

                log("Total internal memory space: " + totalInternalBlocks * internalBlockSize / 1024 / 1024 + " Mb");
                log("Free internal memory space: " + freeInternalBlocks * internalBlockSize / 1024 / 1024 + " Mb");
                log("Free internal memory  space ratio is about: " + (int) freeInternalRatio + "%");
                logger.debug("Total internal memory space: " + totalInternalBlocks * internalBlockSize / 1024 / 1024 + " Mb");
                logger.debug("Free internal memory space: " + freeInternalBlocks * internalBlockSize / 1024 / 1024 + " Mb");
                logger.debug("Free internal memory  space ratio is about: " + (int) freeInternalRatio + "%");

                log("Total SD card space: " + totalSdCardBlocks * sdCardBlockSize / 1024 / 1024 + " Mb");
                log("Free SD card space: " + freeSdCardBlocks * sdCardBlockSize / 1024 / 1024 + " Mb");
                log("Free SD card space ratio is about: " + (int) freeSdCardRatio + "%");
                logger.debug("Total SD card space: " + totalSdCardBlocks * sdCardBlockSize / 1024 / 1024 + " Mb");
                logger.debug("Free SD card space: " + freeSdCardBlocks * sdCardBlockSize / 1024 / 1024 + " Mb");
                logger.debug("Free SD card space ratio is about: " + (int) freeSdCardRatio + "%");
                if (freeInternalRatio < minAcceptableInternalRatio) {
                    isSpaceEnough = false;
                    String message = "Too low free space left on internal memory!";
                    log(message, ERROR);
                    logger.error(message);
                    envProblems.add(message);
                }
                if (freeSdCardRatio < minAcceptableCardRatio) {
                    isSpaceEnough = false;
                    String message = "Too low free space left on SD card!";
                    log(message, ERROR);
                    logger.error(message);
                    envProblems.add(message);
                }
            }

            @Override
            public boolean isCheckSuccess() {
                logger.trace("No crashes detected");
                return isSpaceEnough;
            }
        };

        CheckedThread checkIpTables = new CheckedThread() {
            boolean isIptablesUpdated = true;

            @Override
            public void run() {
                if (!getIptablesChainState().contains("Z7BASECHAIN")) {
                    isIptablesUpdated = false;
                    String message = "IP tables is not updated!";
                    log(message, ERROR);
                    logger.error(message);
                    envProblems.add(message);
                }
            }

            @Override
            public boolean isCheckSuccess() {
                if (isIptablesUpdated) {
                    String message = "Iptables are ready";
                    log(message);
                    logger.info(message);
                }
                return isIptablesUpdated;
            }
        };

        CheckedThread checkConnections = new CheckedThread() {
            boolean isConnectionsAvailable = true;

            @Override
            public void run() {
                final String relayIp = /*"10.2.3.150";*/detectRelay(context);
                if (relayIp == null) {
                    isConnectionsAvailable = false;
                    String message = "Failed to detect relay server! Relay IP = ";
                    envProblems.add(message + relayIp);
                    logger.error(message + relayIp);
                    return;
                }
                Map<String, Integer> hosts = new HashMap<String, Integer>() {{
                    put(AsimovTestCase.TEST_RESOURCE_HOST, TFConstantsIF.HTTP_PORT);
                    put(AsimovTestCase.TEST_RESOURCE_HOST, TFConstantsIF.HTTPS_PORT);
                    put(relayIp, TFConstantsIF.DEFAULT_RELAY_PORT);
                    put(relayIp, (relayIp.contains(".162")) ? TFConstantsIF.RELAY_REDIRECTION : TFConstantsIF.ELSE_RELAY_REDIRECTION);
                    put(relayIp, TFConstantsIF.SSL_RELAY_REDIRECTION);
                }};

                for (String host : hosts.keySet()) {
                    try {
                        if (!checkConnectionAvailable(host, hosts.get(host))) {
                            String msg = "Connection on " + host + ":" + hosts.get(host) + " unavailable!";
                            log(msg, ERROR);
                            envProblems.add(msg);
                            isConnectionsAvailable = false;
                        }
                    } catch (Exception e) {
                        String msg = "Connection on " + host + ":" + hosts.get(host) + " unavailable!";
                        log(msg, ERROR);
                        envProblems.add(msg);
                        isConnectionsAvailable = false;
                    }
                }
            }

            @Override
            public boolean isCheckSuccess() {
                if (isConnectionsAvailable) {
                    String message = "All required connections are available";
                    log(message);
                    logger.info(message);
                }
                return isConnectionsAvailable;
            }
        };
        log("Test for environment readiness started", WARNING);
        logger.debug("Test for environment readiness started");
        try {
            checkOcAlive.start();
            checkConnections.start();
            checkFreeSpace.start();
            checkIpTables.start();
            TestUtil.sleep(80 * 1000);
        } catch (Throwable ignored) {
            logger.error("Interupt error " + ExceptionUtils.getStackTrace(ignored));
        }
        String message = "Test for environment readiness finished";
        log(message, WARNING);
        logger.debug(message);
        return checkConnections.isCheckSuccess() && checkFreeSpace.isCheckSuccess() && checkIpTables.isCheckSuccess()
                && checkOcAlive.isCheckSuccess();
    }

    /**
     * Method which detect relay server
     *
     * @return relay ip address
     */
    private String detectRelay(final Context context) {
        TcpDumpUtil tcpDumpUtil = TcpDumpUtil.getInstance(context);
        tcpDumpUtil.start();
        String uri = AsimovTestCase.createTestResourceUri("smoke_relay_detect_helper");
        try {
            PrepareResourceUtil.prepareResource(uri, false);
            HttpRequest request = AsimovTestCase.createRequest().setUri(uri)
                    .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
            long startTime = System.currentTimeMillis();
            for (int i = 0; i <= 5; i++) {
                AsimovTestCase.sendRequest2(request);
                TestUtil.sleep(5000);
            }
            long endTime = System.currentTimeMillis();
            tcpDumpUtil.stop();
            List<HttpSession> sessions = new ArrayList<HttpSession>(tcpDumpUtil.getZ7TPSession(startTime, endTime));
            for(HttpSession session: sessions) {
                if (session.getServerPort() == TFConstantsIF.RELAY_PORT) {
                    return session.getServerAddress();
                }
            }

        } catch (Exception e) {
            logger.error("Failed to detect relay server" + ExceptionUtils.getStackTrace(e));
        } finally {
            try {
                PrepareResourceUtil.prepareResource(uri, true);
                tcpDumpUtil.stop();
            } catch (Exception ignored) {
                logger.error("Resource can't sent" + ExceptionUtils.getStackTrace(ignored));
            }

        }
        return null;
    }

    private static void logerInfo(String message) {
//        logger.log("INFO", message);
    }

    public static int getWarning() {
        return WARNING;
    }

    public static int getError() {
        return ERROR;
    }

    /**
     * Test hosts for smoke tests, to testing DNS Caching
     */
    public static final String[] HOSTS1 = {"http://www.facebook.com", "http://www.youtube.com",
            "http://www.yahoo.com", "http://www.live.com", "http://www.msn.com", "http://www.wikipedia.org",
            "http://www.blogspot.com", "http://www.baidu.com", "http://www.microsoft.com", "http://www.qq.com",
            "http://www.bing.com", "http://www.ask.com", "http://www.adobe.com"};

    public static final String[] HOSTS2 = {"http://www.soso.com", "http://www.wordpress.com", "http://www.sohu.com",
            "http://www.hao123.com", "http://www.windows.com", "http://www.163.com", "http://www.tudou.com",
            "http://www.amazon.com", "http://www.apple.com", "http://www.ebay.com", "http://www.4399.com",
            "http://www.yahoo.co.jp", "http://www.linkedin.com", "http://www.go.com"};

    private abstract class CheckedThread extends Thread {
        public abstract boolean isCheckSuccess();
    }

    /**
     * Method which check OC for crashes
     *
     * @param log      - log level
     * @param message  - log message
     * @param expected - expected result
     * @param actual   - actual result
     */
    public static void assertOCCrash(String log, String message, boolean expected, boolean actual) {
        message = "Thread " + Thread.currentThread().getName() + ": " + message;
        try {
            Assert.assertEquals(message, expected, actual);
        } catch (AssertionFailedError error) {
            exceptions.add(error);
            Log.e(log, message);
            throw error;
        }
    }

    /**
     * Method which check for controller crashes
     *
     * @param log      - log level
     * @param message  - log message
     * @param expected - expected result
     * @param actual   - actual result
     */
    public static void assertControllerCrash(String log, String message, ArrayList<String> expected, ArrayList<String> actual) {
        message = "Thread " + Thread.currentThread().getName() + ": " + message;
        try {
            for (int i = 0; i < expected.size(); i++) {
                Assert.assertEquals(message + expected.get(i).substring(0, expected.get(i).indexOf("=")),
                        expected.get(i).substring(expected.get(i).indexOf("=") + 1), actual.get(i).substring(actual.get(i).indexOf("=") + 1));
            }
        } catch (AssertionFailedError error) {
            message = message + " True";
            exceptions.add(error);
            Log.e(log, message);
            throw error;
        }
    }

    /**
     * Method which parsing OC process
     *
     * @return list with OC process
     */
    public static ArrayList<String> processesParser() {
        Map<String, Integer> processes = OCUtil.getOcProcesses(false);
        ArrayList<String> result = new ArrayList<String>();
        String[] procs = {
                "ocdnsd",
                "ochttpd",
                "ocshttpd",
                "octcpd",
                "occ"
        };

        for (String prc : procs) {
            if (processes.get(prc) != null) {
                result.add(prc + "=" + processes.get(prc));
            }
        }
        //result.add("com.seven.asimov=" + processes.get("com.seven.asimov"));
        return result;
    }

    public static void sendInvSms(boolean withCache, StartPollWrapper startPoll) {
        SmsUtil sms = new SmsUtil(AsimovTestCase.getStaticContext());
        if (withCache){
            sms.sendInvalidationSms(Integer.parseInt(startPoll.getSubscriptionId()), SmsUtil.InvalidationType.INVALIDATE_WITH_CACHE.byteVal);
        }else
            sms.sendInvalidationSms(Integer.parseInt(startPoll.getSubscriptionId()), SmsUtil.InvalidationType.INVALIDATE_WITHOUT_CACHE.byteVal);
    }


    public static <T extends LogEntryWrapper> List<T> getOCDispathersCrashByTime(List<T> allEntries, long startTime, long endTime) {
        List<T> entries = new ArrayList<T>();
        for (T entry : allEntries) {
            if (entry.getTimestamp() >= startTime && entry.getTimestamp() <= endTime) {
                entries.add(entry);
            }
        }
        return entries;
    }

    /**
     * Encoding url to type <code>string</code>
     *
     * @param expected - string url
     * @return - encoded uri
     */
    public static String base64Encode(String expected) {
        return URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));
    }

    /**
     * Method which build default uri
     *
     * @param uri - base uri
     * @return - prepared uri for test
     */
    public static HttpRequest.Builder buildDefaultRequest(String uri) {
        return AsimovTestCase.createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .addHeaderField("Accept-Language", "ru-ru,ru;q=0.8,en-us;q=0.5,en;q=0.")
                .addHeaderField("Accept-Encoding", "identity");
    }

    /**
     * enum available test results
     */
    public enum Result {
        PASSED("Passed"),
        FAILED("Failed");

        private final String text;

        Result(String text) {
            this.text = text;
        }

        public String toString() {
            return this.text;
        }
    }

    /**
     * SmokeHelper class which provide HTML report
     */
    public static class TestResult implements Comparable<TestResult> {
        final String testName;
        final Result result;
        final String failDescription;

        public TestResult(String testName, Result result, String failDescription) {
            this.testName = testName;
            this.result = result;
            if (failDescription != null) {
                this.failDescription = failDescription;
            } else {
                this.failDescription = "-";
            }
        }

        public String getTestName() {
            return testName;
        }

        public Result getResult() {
            return result;
        }

        public String getFailDescription() {
            return failDescription;
        }

        @Override
        public int compareTo(TestResult testResult) {
            return this.testName.compareTo(testResult.testName);
        }
    }
}
