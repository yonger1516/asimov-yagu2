package com.seven.asimov.test.tool.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import com.seven.asimov.it.IntegrationTestRunnerGa;
import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.NetStat;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.*;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.tcpdump.HttpSession;
import com.seven.asimov.it.utils.tcpdump.TcpDumpUtil;
import com.seven.asimov.test.tool.activity.AutomationTestsTab;
import com.seven.asimov.test.tool.testcase.SmokeTestCase;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

public class SmokeHelperUtil extends SmokeTestCase {
    private final static String TAG = SmokeHelperUtil.class.getSimpleName();

    private static final String[] S = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q",
            "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H",
            "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y",
            "Z", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
    private static Random random = new Random();

    private boolean isRevalidate = false;
    private static volatile boolean runThread;
    private static Thread radioKeep;

    private static SmokeHelperUtil instance;
    private static final ArrayList<String> envProblems = new ArrayList<String>();
    private static final List<SmokeUtil.TestResult> smokeTests = new ArrayList<SmokeUtil.TestResult>();
    private static final List<SmokeUtil.TestResult> sanityTests = new ArrayList<SmokeUtil.TestResult>();
    private static Map<String, Integer> processes;
    private static long maxResponseDuration = 0;

    private Context context;
    private List<BrandingLoaderUtil.Branding> brandings;
    private static boolean apkInstalled = false;

    private final static String CRASH_MESSAGE = "OC crash was detected!";
    public static String FAIL_BY_ENV = "Failed by reason of environment problems!";
    private final static int INFO = 0;
    public final static int WARNING = 1;
    public final static int ERROR = 2;

    private static boolean environmentReady = true;
    private static boolean environmentChecked = false;

    private static final String MSISDN_VALIDATION_STATE = PropertyLoaderUtil.getProperties().get("client.msisdn_validation_enabled");
    private static final String MSISDN_VALIDATION_PHONENUMBER = PropertyLoaderUtil.getProperties().get("system.msisdn_validation_phonenumber");

    private SmokeHelperUtil() {
        this.brandings = BrandingLoaderUtil.getBrandings();
    }

    private SmokeHelperUtil(Context context) {
        this.context = context;
        this.brandings = BrandingLoaderUtil.getBrandings();
    }

    public static void clearResults() {
        envProblems.clear();
        smokeTests.clear();
        sanityTests.clear();
        environmentReady = true;
        environmentChecked = false;
        processes = OCUtil.getOcProcesses(true);
    }

    private static void createRadioKeepUpThread() {
        radioKeep = new Thread(new Runnable() {
            @Override
            public void run() {
                while (runThread) {
                    try {
                        Log.d(TAG, "PING!!!");
                        pingHost(PMSUtil.getPmsServerIp());
                        TestUtil.sleep(3 * 1000);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
                Log.d(TAG, "Thread stop");
            }
        });
    }

    public static void startRadioKeepUpThread() {
        if (!runThread) {
            createRadioKeepUpThread();
            runThread = true;
            Log.d(TAG, "Start PING Thread");
            radioKeep.start();
        }
    }

    public static void stopRadioKeepUpThread() {
        Log.d(TAG, "Stop PING Thread");
        runThread = false;
        try {
            radioKeep.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void revalidationCheckMissHit(HttpRequest request, int requestID, boolean keepAlive, long delay, long revalidationTime) throws Throwable {
        HttpResponse response;
        if (revalidationTime < System.currentTimeMillis() && !isRevalidate) {
            response = checkMiss(request, requestID, "tere", keepAlive, TIMEOUT);
            isRevalidate = true;
        } else {
            response = checkHit(request, requestID, "tere", keepAlive, TIMEOUT);
        }
        logSleeping(delay - response.getDuration());
    }

    public static String generationRandomString() {
        char[] sb = new char[30];
        for (int i = 0; i < 30; i++) {
            sb[i] = S[random.nextInt(S.length - 1)].charAt(0);
        }
        return String.valueOf(sb);
    }

    public void executeServerRevalidation(String uri, String headers, boolean keepAlive, long delay,
                                          long revalidationTime) throws Throwable {

//        Thread radioKeep = createRadioKeepThread();
//        radioKeep.start();
        startRadioKeepUpThread();
        HttpResponse response;
        String encodedRawHeadersDef = SmokeUtil.base64Encode(headers);
        final HttpRequest request = SmokeUtil.buildDefaultRequest(uri).getRequest();
        PrepareResourceUtil.prepareResource200(uri, false, encodedRawHeadersDef);
        int requestId = 1;

        try {
            response = checkMiss(request, requestId++, "tere", keepAlive, TIMEOUT);
            logSleeping(delay - response.getDuration());

            revalidationCheckMissHit(request, requestId++, keepAlive, delay, revalidationTime);

            revalidationCheckMissHit(request, requestId++, keepAlive, delay, revalidationTime);

            revalidationCheckMissHit(request, requestId++, keepAlive, delay, revalidationTime);

            revalidationCheckMissHit(request, requestId++, keepAlive, delay, revalidationTime);

            revalidationCheckMissHit(request, requestId++, keepAlive, delay, revalidationTime);

            revalidationCheckMissHit(request, requestId, keepAlive, delay, revalidationTime);

        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        } finally {
            stopRadioKeepUpThread();
            PrepareResourceUtil.invalidateResourceSafely(uri, INVALIDATED_RESPONSE, false);
            isRevalidate = false;
            radioKeep.interrupt();
        }
    }


    /**
     * getiing instance of SmokeHelper
     *
     * @return instance of SmokeHelper
     */
    public static SmokeHelperUtil getInstance(Context context) {
        if (instance == null) {
            synchronized (SmokeHelperUtil.class) {
                if (instance == null) {
                    instance = new SmokeHelperUtil(context);
                }
            }
        }
        return instance;
    }


    public boolean isEnvironmentReady() {
        if (!environmentChecked) {
            environmentReady = checkEnvReady();
//            SmokeRestHelper.checkNamespace(PATH, NAME);
            environmentChecked = true;
        }
        return environmentReady;
    }

    private static String getIptablesChainState(boolean checkZBASECHAIN) {

        String[] getRejectRule = new String[]{"iptables", "-t", "filter", "-L", "INPUT", "-n"};
        String[] getOutputCmdArr = new String[]{"iptables", "-t", "filter", "-L", "Z7BASECHAIN", "-n"};
        if (checkZBASECHAIN) {
            return ShellUtil.execWithCompleteResult(Arrays.asList(getOutputCmdArr), true);
        }
        return ShellUtil.execWithCompleteResult(Arrays.asList(getRejectRule), true);
    }


    /**
     * Method which generate random header, for get random resource
     *
     * @return value
     */
    public static String generationRandomHeader() {
        char[] sb = new char[30];
        for (int i = 0; i < 30; i++) {
            sb[i] = S[random.nextInt(S.length - 1)].charAt(0);
        }
        return String.valueOf(sb);
    }

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

    public static void verifyPidsNotChanged(boolean output) {
        Map<String, Integer> actualProcesses = OCUtil.getOcProcesses(output);
        for (String ps : processes.keySet()) {
            if (!(actualProcesses.containsKey(ps) && actualProcesses.get(ps).equals(processes.get(ps)))) {
                log(CRASH_MESSAGE, ERROR);
                smokeTests.add(new SmokeUtil.TestResult("Crash check", SmokeUtil.Result.FAILED, CRASH_MESSAGE));
                updateProcesses();
            }
        }
    }

    public static void updateProcesses() {
        processes = OCUtil.getOcProcesses(true);
    }

    public static void addResult(String testName, SmokeUtil.Result result, String failDescription) {
        smokeTests.add(new SmokeUtil.TestResult(testName, result, failDescription));
    }

    public static void addResult(String testName) {
        addResult(testName, SmokeUtil.Result.PASSED, "-");
    }

    public static String printEnvProblemsSummary() {
        log("Test skipped by reason of environment problems: ", SmokeHelperUtil.ERROR);
        for (String s : envProblems) {
            log(s, SmokeHelperUtil.ERROR);
        }
        return envProblems.get(0);
    }

    public static void printSummary() {
        if (smokeTests.isEmpty()) return;
        Collections.sort(smokeTests);
        BufferedReader br = null;
        BufferedWriter writer = null;
        StringBuilder page = new StringBuilder();
//        File reportIndex = new File(OCUtil.getHtmlReportTargetDir() + "/index.html");
        File reportIndex = new File(IntegrationTestRunnerGa.RESULTS_DIR + "/index.html");
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
            for (SmokeUtil.TestResult result : smokeTests) {
                page.append("<tr><td>").append(result.getTestName()).append("</td><td>").append(result.getResult()).append("</td><td>").append(result.getFailDescription()).append("</td></tr>");
            }
            page.append("</table></center></body></html>");

            writer = new BufferedWriter(new FileWriter(reportIndex, false));
            writer.write(page.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void log(String message) {
        log(message, INFO);
    }

    public static void log(String message, Integer warnLevel) {
        //System.out.println(message);
        switch (warnLevel) {
            case 0:
                log(message);
                break;
            case 1:
                log("<b>" + message + "</b>");
                break;
            case 2:
                log("<b><font color=\"red\">" + message + "</font></b>");
                break;
            default:
        }
    }

    // Check for environment ready
    private boolean checkEnvReady() {

        CheckedThread checkOcAlive = new CheckedThread() {
            boolean isOcAlive = true;

            @Override
            public void run() {
                int time = 0;
                // Check every 5 sec during 1 min that OC is running and there are no crashes
                while (time <= 60) {
                    long start = System.currentTimeMillis();
                    if (!OCUtil.isOpenChannelRunning()) {
                        String msg = "OC crash detected!";
                        log(msg, ERROR);
                        envProblems.add(msg);
                        isOcAlive = false;
                        break;
                    }
                    time += 5;
                    TestUtil.sleep(5 * 1000 - (System.currentTimeMillis() - start));
                }
            }

            @Override
            public boolean isCheckSuccess() {
                if (isOcAlive) log("No crashes detected on startup");
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

                log("Total SD card space: " + totalSdCardBlocks * sdCardBlockSize / 1024 / 1024 + " Mb");
                log("Free SD card space: " + freeSdCardBlocks * sdCardBlockSize / 1024 / 1024 + " Mb");
                log("Free SD card space ratio is about: " + (int) freeSdCardRatio + "%");
                if (freeInternalRatio < minAcceptableInternalRatio) {
                    isSpaceEnough = false;
                    String msg = "Too low free space left on internal memory!";
                    log(msg, ERROR);
                    envProblems.add(msg);
                }
                if (freeSdCardRatio < minAcceptableCardRatio) {
                    isSpaceEnough = false;
                    String msg = "Too low free space left on SD card!";
                    log(msg, ERROR);
                    envProblems.add(msg);
                }
            }

            @Override
            public boolean isCheckSuccess() {
                return isSpaceEnough;
            }
        };

        CheckedThread checkIpTables = new CheckedThread() {
            boolean isIptablesUpdated = true;

            @Override
            public void run() {
                if (!getIptablesChainState(true).contains("Z7BASECHAIN")) {
                    isIptablesUpdated = false;
                    String msg = "IP tables is not updated!";
                    log(msg, ERROR);
                    envProblems.add(msg);
                }
            }

            @Override
            public boolean isCheckSuccess() {
                if (isIptablesUpdated) log("Iptables are ready");
                return isIptablesUpdated;
            }
        };

        CheckedThread checkConnections = new CheckedThread() {
            boolean isConnectionsAvailable = true;

            @Override
            public void run() {
                final BrandingLoaderUtil.Branding branding = detectBranding();
                System.out.println("Start testing connections!");
                if (branding != null) {
                    System.out.println("Detected branding : \n" + branding.toString());
                    EXTERNAL_IP = branding.getSystemRelayHost();
                    mPmsServerIp = branding.getSystemRelayHost();
                    RELAY_PORT = branding.getSystemClientRelayPort();
                } else {
                    isConnectionsAvailable = false;
                    Log.e("SmokeHelper", "Branding doesn't detected");
//                    envProblems.add("Failed to detect OC branding!");
                    envProblems.add("Failed to detect relay servers!");
                    return;
                }

//                if (branding == null || branding.getName() == BrandingLoaderUtil.Target.UNKNOWN){
//                    isConnectionsAvailable = false;
//                    envProblems.add("Failed to detect OC branding!");
//                    envProblems.add("Failed to detect servers!");
//                    return;
//                }

                Map<String, Integer> hosts = new LinkedHashMap<String, Integer>() {{
                    put(TEST_RESOURCE_HOST, 80);
                    put(TEST_RESOURCE_HOST, 443);
                    put(branding.getSystemRelayHost(), branding.getSystemClientRelayPort());

                    if (branding.getClientRedirectionServer1Port() != 0)
                        put(branding.getClientRedirectionServer1Host(), branding.getClientRedirectionServer1Port());
                    //put(relayIp, 7443);
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
                if (isConnectionsAvailable) log("All required connections are available");
                return isConnectionsAvailable;
            }
        };
        log("Test for environment readiness started", WARNING);
        try {
            checkOcAlive.start();
            checkConnections.start();
            checkFreeSpace.start();
            checkIpTables.start();
            TestUtil.sleep(120 * 1000);
        } catch (Throwable ignored) {
            System.out.println("Ignored exception: " + ignored.getMessage());
            ignored.printStackTrace();
        }
        log("Test for environment readiness finished", WARNING);
        return checkConnections.isCheckSuccess() && checkFreeSpace.isCheckSuccess() && checkIpTables.isCheckSuccess()
                && checkOcAlive.isCheckSuccess();
    }

    public static void updateIpTablesForPrepareResource(boolean ignorePrepareResourcePort) {
        try {
            String[] addPrepareResourcePortToIgnor = {"su", "-c", TFConstantsIF.IPTABLES_PATH + " -t nat -I OUTPUT -m conntrack --ctorigdstport 8099 -j ACCEPT"};
            String[] deletePrepareResourcePort = {"su", "-c", TFConstantsIF.IPTABLES_PATH + " -t nat -D OUTPUT -m conntrack --ctorigdstport 8099 -j ACCEPT"};
            if (ignorePrepareResourcePort) {
                Runtime.getRuntime().exec(addPrepareResourcePortToIgnor).waitFor();
            } else {
                Runtime.getRuntime().exec(deletePrepareResourcePort).waitFor();
            }
        } catch (InterruptedException e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        } catch (IOException e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
    }

    private BrandingLoaderUtil.Branding detectBranding() {
        updateIpTablesForPrepareResource(true);
        SmokeTcpDumpUtil tcpDump = new SmokeTcpDumpUtil("relayDetect");
        String uri = AsimovTestCase.createTestResourceUri("smoke_relay_detect_helper");
        try {
            PrepareResourceUtil.prepareResource(uri, false);
            HttpRequest request = AsimovTestCase.createRequest().setUri(uri)
                    .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
            tcpDump.start();
            for (int i = 0; i <= 5; i++) {
                AsimovTestCase.sendRequest2(request);
                TestUtil.sleep(5000);
            }
            //Thread.sleep(30000);
            tcpDump.stop(true);

            List<NetStat.SocketInfo> socketsRelay = new ArrayList<NetStat.SocketInfo>();
            List<NetStat.SocketInfo> socketsProxy = new ArrayList<NetStat.SocketInfo>();

            for (BrandingLoaderUtil.Branding branding : brandings) {
                socketsRelay.clear();
                socketsProxy.clear();
                socketsRelay.addAll(tcpDump.getSocketsOnPort(branding.getSystemClientRelayPort()));
                socketsProxy.addAll(tcpDump.getSocketsOnPort(branding.getClientRedirectionServer1Port()));
                if (branding.getClientRedirectionServer1Port() != 0) {
                    if (socketsRelay.size() > 0 && socketsRelay.get(0).getForeignAdress().equals(branding.getSystemRelayHost())
                            && socketsProxy.size() > 0 && socketsProxy.get(0).getForeignAdress().equals(branding.getClientRedirectionServer1Host())) {
                        return branding;
                    }
                } else {
                    if (socketsRelay.size() > 0 && socketsRelay.get(0).getForeignAdress().equals(branding.getSystemRelayHost())) {
                        return branding;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to detect OC branding!");
        } finally {
            try {
                PrepareResourceUtil.prepareResource(uri, true);
            } catch (Exception ignored) {
            }
            updateIpTablesForPrepareResource(false);
            tcpDump.stop(false);
        }
        return null;
    }

    private abstract class CheckedThread extends Thread {
        public abstract boolean isCheckSuccess();
    }

    //=========================NEW CHECKS===========================//
    SmokeTcpDumpUtil tcpDump;
    public static BrandingLoaderUtil.Branding br;

    public boolean ocRunCheck() {
        int time = 0;
        while (time <= 30) {
            long start = System.currentTimeMillis();
            if (!OCUtil.isOpenChannelRunning()) {
                String msg = "OC crash detected!";
                Log.i("Inv", msg);
                log(msg, ERROR);
                envProblems.add(msg);
                return false;
            }
            time += 5;
            TestUtil.sleep(5 * 1000 - (System.currentTimeMillis() - start));
        }
        return true;
    }

    public static boolean ipTableUpdateCheck(boolean checkZBASECHAIN) {
        boolean status;
        if (checkZBASECHAIN) {
            status = getIptablesChainState(checkZBASECHAIN).contains("Z7BASECHAIN");
        } else {
            status = getIptablesChainState(checkZBASECHAIN).contains("REJECT");
        }
        if (!status) {
            String msg = "IP tables is not updated!";
            Log.i("Inv", msg);
            log(msg, ERROR);
            envProblems.add(msg);
            return false;
        }
        return true;
    }

    public boolean memoryCheck() {
        double minAcceptableCard = 600;
        double minAcceptableInternal = 600;
        if (MemoryStatusUtil.externalMemoryAvailable()) {

            Log.d(TAG, "Total SD card space: " + MemoryStatusUtil.formatSizeMiB(MemoryStatusUtil.getTotalExternalMemorySize()));
            Log.d(TAG, "Free SD card space: " + MemoryStatusUtil.formatSizeMiB(MemoryStatusUtil.getAvailableExternalMemorySize()));

            Log.e("!!!", "" + MemoryStatusUtil.sizeMiB(MemoryStatusUtil.getAvailableInternalMemorySize()));
            Log.e("!!!", "" + MemoryStatusUtil.sizeMiB(MemoryStatusUtil.getAvailableExternalMemorySize()));

            if (MemoryStatusUtil.sizeMiB(MemoryStatusUtil.getAvailableInternalMemorySize()) < minAcceptableInternal) {
                String msg = "Too low free space left on internal memory!";
                Log.i("Inv", msg);
                log(msg, ERROR);
                envProblems.add(msg);
                return false;
            }

            if (MemoryStatusUtil.sizeMiB(MemoryStatusUtil.getAvailableExternalMemorySize()) < minAcceptableCard) {
                String msg = "Too low free space left on SD card!";
                Log.i("Inv", msg);
                log(msg, ERROR);
                envProblems.add(msg);
                return false;
            }
        } else {
            String msg = "Memory does not detect!";
            Log.i("Inv", msg);
            log(msg, ERROR);
            envProblems.add(msg);
            return false;
        }
        return true;
    }

    public boolean detectRelayCheck() {
        boolean result = true;
        String host = br.getSystemRelayHost();
        int port = br.getSystemClientRelayPort();
        String msg;
        Socket socket = null;
        try {
            socket = new Socket(host, port);
            if (!socket.isConnected()) {
                msg = "Could not connect to host " + AutomationTestsTab.b.getSystemRelayHost() + " on port " + port;
                envProblems.add(msg);
                return false;
            }
        } catch (UnknownHostException e) {
            msg = "UnknownHostException during check availability of servers: " + e.getMessage();
            envProblems.add(msg);
            result = false;
        } catch (IOException e) {
            msg = "IOException during check availability of relay: " + e.getMessage();
            envProblems.add(msg);
            result = false;
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
            }
        }
        return result;
    }

    public boolean checkBranding(boolean shouldBeValidation) {
        br = AutomationTestsTab.b;
        String packageName = br.getAndroidPackageName();
        Log.i(TAG, packageName);
        if (shouldBeValidation) {
            Log.i(TAG, MSISDN_VALIDATION_STATE + " - MSISDN_VALIDATION_STATE");
            Log.i(TAG, MSISDN_VALIDATION_PHONENUMBER + " - MSISDN_VALIDATION_PHONENUMBER");
            if (Integer.parseInt(MSISDN_VALIDATION_STATE) == 0) {
                String msg = "Branding without MSISDN validation";
                envProblems.add(msg);
                return false;
            }
        }
        try {
            AutomationTestsTab.pm.getPackageInfo(packageName, 0);
            if (packageName.equals("com.seven.asimov")) {
                File f = new File("/data/data/" + packageName + "/files");
                if (!f.exists()) {
                    String msg = "The selected Branding does not correspond to OC apk!";
                    envProblems.add(msg);
                    Log.e(TAG, msg);
                    return false;
                }
            }
            Log.i(TAG, "Apk with package name " + br.getAndroidPackageName() + " installed");
        } catch (PackageManager.NameNotFoundException e) {
            String msg = "The selected Branding does not correspond to OC apk!";
            envProblems.add(msg);
            Log.e(TAG, msg);
            return false;
        }

        boolean result = true;
        updateIpTablesForPrepareResource(true);
        TcpDumpUtil tcpDump = TcpDumpUtil.getInstance(AutomationTestsTab.context);
        String uri = AsimovTestCase.createTestResourceUri("smoke_relay_detect_helper");
        try {
            PrepareResourceUtil.prepareResource(uri, false);
            HttpRequest request = AsimovTestCase.createRequest().setUri(uri)
                    .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
//            tcpDump.start();
            long startTime = System.currentTimeMillis();
            for (int i = 0; i <= 5; i++) {
                AsimovTestCase.sendRequest2(request);
                TestUtil.sleep(5000);
            }
            long endTime = System.currentTimeMillis();
//            tcpDump.stop(true);

            List<HttpSession> sessions = tcpDump.getHttpSessions(uri, startTime, endTime);
            if (sessions.size() == 0) {
                Log.d(TAG, "No sessions found for URI: " + uri + ". Trying to check using timestamps only");
                sessions = tcpDump.getHttpSessions(startTime, endTime);
            }

            List<NetStat.SocketInfo> socketsRelay = new ArrayList<NetStat.SocketInfo>();
            List<NetStat.SocketInfo> socketsProxy = new ArrayList<NetStat.SocketInfo>();

            List<HttpSession> relay = new ArrayList<HttpSession>();

            for (HttpSession session : sessions) {
                if (session.getServerPort() == br.getSystemClientRelayPort() || session.getClientPort() == br.getSystemClientRelayPort()) {
                    relay.add(session);
                }
            }
            if (relay.size() > 0) {
                if (relay.get(0).getServerAddress().equals(br.getSystemRelayHost()) || relay.get(0).getClientAddress().equals(br.getSystemRelayHost())) {
                    Log.i(TAG, "The selected branding contains correct relay host");
                } else {
                    String msg = "The selected branding contains incorrect relay host!";
                    Log.e(TAG, msg);
                    envProblems.add(msg);
                    result = false;
                }
            }

//            socketsRelay.addAll(tcpDump.getSocketsOnPort(br.getSystemClientRelayPort()));
//            socketsProxy.addAll(tcpDump.getSocketsOnPort(br.getClientRedirectionServer1Port()));
//            if (br.getClientRedirectionServer1Port() != 0) {
//                if (socketsRelay.size() > 0 && socketsRelay.get(0).getForeignAdress().equals(br.getSystemRelayHost())
//                        && socketsProxy.size() > 0 && socketsProxy.get(0).getForeignAdress().equals(br.getClientRedirectionServer1Host())) {
//                    Log.i(TAG, "The selected branding contains correct relay host");
//                } else {
//                    String msg = "The selected branding contains incorrect relay host!";
//                    Log.e(TAG, msg);
//                    envProblems.add(msg);
//                    result = false;
//                }
//            } else {
//                if (!socketsRelay.isEmpty()) {
//                    if (socketsRelay.get(0).getForeignAdress().equals(br.getSystemRelayHost())) {
//                        Log.i(TAG, "The selected branding contains correct relay host");
//                    } else {
//                        String msg = "The selected branding contains incorrect relay host!";
//                        Log.e(TAG, msg);
//                        envProblems.add(msg);
//                        result = false;
//                    }
//                } else {
//                    String msg = "There have been no connections to the relay!";
//                    Log.e(TAG, msg);
//                    envProblems.add(msg);
//                    result = false;
//                }
//            }
        } catch (Exception e) {
            System.out.println("Failed to detect OC branding!");
        } finally {
            try {
                PrepareResourceUtil.invalidateResourceSafely(uri);
            } catch (Exception ignored) {
            }
            updateIpTablesForPrepareResource(false);
//            tcpDump.stop(false);
        }
        return result;
    }

    private boolean endCheck() {
        EXTERNAL_IP = br.getSystemRelayHost();
        mPmsServerIp = br.getSystemRelayHost();
        RELAY_PORT = br.getSystemClientRelayPort();

        Map<String, Integer> hosts = new LinkedHashMap<String, Integer>() {{
            put(AsimovTestCase.TEST_RESOURCE_HOST, 80);
            put(AsimovTestCase.TEST_RESOURCE_HOST, 443);
            put(br.getSystemRelayHost(), br.getSystemClientRelayPort());

            if (br.getClientRedirectionServer1Port() != 0)
                put(br.getClientRedirectionServer1Host(), br.getClientRedirectionServer1Port());
            //put(relayIp, 7443);
        }};

        for (String host : hosts.keySet()) {
            try {
                if (!checkConnectionAvailable(host, hosts.get(host))) {
                    String msg = "Connection on " + host + ":" + hosts.get(host) + " unavailable!";
                    log(msg, ERROR);
                    envProblems.add(msg);
                    return false;
                }
            } catch (Exception e) {
                String msg = "Connection on " + host + ":" + hosts.get(host) + " unavailable!";
                log(msg, ERROR);
                envProblems.add(msg);
                return false;
            }

        }
        return true;
    }

    //=========================SANITY CHECKS============================//

    private boolean correctServerCheck() {
        boolean flag = TFConstantsIF.EXTERNAL_IP.equals("teng055.seven.com") || TFConstantsIF.EXTERNAL_IP.equals("teng057.seven.com") || TFConstantsIF.EXTERNAL_IP.equals("teng060.seven.com");
        if (flag) {
            return true;
        } else {
            String msg = "Sanity Test suite should be executed to teng055.seven.com, teng057.seven.com or teng060.seven.com. Please, change apk branding and re-run tests.";
            envProblems.add(msg);
            return false;
        }
    }

    //=========================INITIAL CHECKS===========================//
    private boolean dnsResolveCheck() {
        boolean result = true;
        InetAddress inetAddress;
        int i = 0;
        try {
            inetAddress = InetAddress.getByName(AsimovTestCase.TEST_RESOURCE_HOST);
            i++;
            inetAddress = InetAddress.getByName(AutomationTestsTab.b.getSystemRelayHost());
        } catch (UnknownHostException e) {
            String msg;
            if (i == 1) {
                msg = "Unable to resolve host \"" + AutomationTestsTab.b.getSystemRelayHost() + "\": No address associated with hostname";
            } else {
                msg = "Unable to resolve host \"" + AsimovTestCase.TEST_RESOURCE_HOST + "\": No address associated with hostname";
            }
            envProblems.add(msg);
            result = false;
        }
        return result;
    }

    private boolean checkAvailabilityOfServers() {
        return checkAvailabilityOfServers(false);
    }

    private static boolean checkAvailabilityOfServers(boolean checkRest) {
        boolean result = true;
        String msg;
        int httpPort = 80;
        int httpsPort = 443;
        int preparePort = 8099;
        int relayPort = 7735;
        int restPort = 8087;
        Socket socket = null;
        try {
            socket = new Socket(AsimovTestCase.TEST_RESOURCE_HOST, httpPort);
            if (!socket.isConnected()) {
                Log.i(TAG, "Could not connect to host \" + AsimovTestCase.TEST_RESOURCE_HOST + \" on port \" + httpPort");
                msg = "Could not connect to host " + AsimovTestCase.TEST_RESOURCE_HOST + " on port " + httpPort;
                envProblems.add(msg);
                return false;
            }
            socket.close();
            socket = new Socket(AsimovTestCase.TEST_RESOURCE_HOST, httpsPort);
            if (!socket.isConnected()) {
                Log.i(TAG, "Could not connect to host \" + AsimovTestCase.TEST_RESOURCE_HOST + \" on port \" + httpPort");
                msg = "Could not connect to host " + AsimovTestCase.TEST_RESOURCE_HOST + " on port " + httpsPort;
                envProblems.add(msg);
                return false;
            }
            socket.close();
            socket = new Socket(AsimovTestCase.TEST_RESOURCE_HOST, preparePort);
            if (!socket.isConnected()) {
                Log.i(TAG, "Could not connect to host \" + AsimovTestCase.TEST_RESOURCE_HOST + \" on port \" + httpPort");
                msg = "Could not connect to host " + AsimovTestCase.TEST_RESOURCE_HOST + " on port " + preparePort;
                envProblems.add(msg);
                return false;
            }
            socket.close();
            socket = new Socket(AutomationTestsTab.b.getSystemRelayHost(), relayPort);
            if (!socket.isConnected()) {
                Log.i(TAG, "Could not connect to host \" + AsimovTestCase.TEST_RESOURCE_HOST + \" on port \" + httpPort");
                msg = "Could not connect to host " + AutomationTestsTab.b.getSystemRelayHost() + " on port " + relayPort;
                envProblems.add(msg);
                return false;
            }
            socket.close();
            if (checkRest) {
                socket = new Socket(AutomationTestsTab.b.getSystemRelayHost(), restPort);
                if (!socket.isConnected()) {
                    Log.i(TAG, "Could not connect to host \" + AsimovTestCase.TEST_RESOURCE_HOST + \" on port \" + httpPort");
                    msg = "Could not connect to host " + AutomationTestsTab.b.getSystemRelayHost() + " on port " + restPort;
                    envProblems.add(msg);
                    return false;
                }
                socket.close();
            }
        } catch (IOException e) {
            if (socket != null && !socket.isClosed()) {
                try {
                    Log.i(TAG, "IO1");
                    socket.close();
                } catch (IOException ex) {
                    Log.i(TAG, "IO2");
                }
            }
            Log.i(TAG, "IO3");
            msg = "IOException during check availability of servers: " + e.getMessage();
            envProblems.add(msg);
            result = false;
        }
        return result;
    }

    protected boolean connectionStabilityCheck() {
        boolean result = true;
        String resourceUri = "asimov_smoke_tests_HTTP";
        String uri = createTestResourceUri(resourceUri);
        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-ResponseContentSize", 25600 + ",c")
                .getRequest();
        HttpResponse response;
        long duration = 0;
        for (int i = 0; i < 40; i++) {
            response = sendRequest2(request, false, false, TIMEOUT);
            Log.e(TAG, "" + response.getDuration());
            if (response.getDuration() > duration) {
                duration = response.getDuration();
            }
        }
        Log.w(TAG, "Max response duration = " + duration + "ms");
        maxResponseDuration = duration;
        return result;
    }

    public void globalCheck() {
        environmentReady = ocRunCheck() && ipTableUpdateCheck(true) && checkBranding(false) && detectRelayCheck() &&
                endCheck();
    }

    public void initialGlobalCheck() {
        environmentReady = memoryCheck() && dnsResolveCheck() && checkAvailabilityOfServers() && connectionStabilityCheck();
    }

    public void sanityGlobalCheck() {
        environmentReady = checkBranding(false);
//        && correctServerCheck();
    }

    public static boolean restServerCheck() {
        return checkAvailabilityOfServers(true);
    }

    public static boolean invReady() {
        return environmentReady;
    }

    public static ArrayList<String> getEnvProblems() {
        return envProblems;
    }

    public static void assertOCCrash(String log, String message, boolean expected, boolean actual) {
        assertOCCrash(log, message, null, expected, actual);
    }

    /**
     * Method which check OC for crashes
     *
     * @param log      - log level
     * @param message  - log message
     * @param expected - expected result
     * @param actual   - actual result
     */
    public static void assertOCCrash(String log, String message, String prefix, boolean expected, boolean actual) {
        message = "Thread " + Thread.currentThread().getName() + ": " + message;
        assertEquals(message, expected, actual);
    }

    public static void assertControllerCrash(String log, String message, ArrayList<String> expected, ArrayList<String> actual) {
        assertControllerCrash(log, message, null, expected, actual);
    }

    /**
     * Method which check for controller crashes
     *
     * @param log      - log level
     * @param message  - log message
     * @param expected - expected result
     * @param actual   - actual result
     */
    public static void assertControllerCrash(String log, String message, String prefix, ArrayList<String> expected, ArrayList<String> actual) {
//        message = "Thread " + Thread.currentThread().getName() + ": " + message;
        for (int i = 0; i < expected.size(); i++) {
            assertDispatcherCrash(String.format(message, expected.get(i).substring(0, expected.get(i).indexOf("="))), prefix,
                    expected.get(i).substring(expected.get(i).indexOf("=") + 1), actual.get(i).substring(actual.get(i).indexOf("=") + 1));
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
        if (processes.get("ocdnsd") != null) {
            result.add("ocdnsd=" + processes.get("ocdnsd"));
            result.add("ochttpd=" + processes.get("ochttpd"));
            result.add("ocshttpd=" + processes.get("ocshttpd"));
        } else {
            result.add("dns=" + processes.get("dns"));
            result.add("http=" + processes.get("http"));
            result.add("https=" + processes.get("https"));
        }
        result.add("occ=" + processes.get("occ"));
//        result.add("com.seven.asimov=" + processes.get("com.seven.asimov"));
        return result;
    }

    private static void pingHost(String host) throws IOException {
        try {
            Runtime.getRuntime().exec("ping -c 1 " + host).waitFor();
        } catch (InterruptedException interruptedExceprion) {
            ExceptionUtils.getStackTrace(interruptedExceprion);
        }
    }

    public static void setApkInstalled(boolean apkInstalled) {
        SmokeHelperUtil.apkInstalled = apkInstalled;
    }

    public static boolean isApkInstalled() {
        return apkInstalled;
    }
}
