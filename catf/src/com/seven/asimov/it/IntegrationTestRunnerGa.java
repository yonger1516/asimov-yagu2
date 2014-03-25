package com.seven.asimov.it;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.ServerLogsDownloader;
import com.seven.asimov.it.base.androidtest.FixtureSharingTestRunner;
import com.seven.asimov.it.base.constants.BaseConstantsIF;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.*;
import com.seven.asimov.it.utils.date.DateUtil;
import com.seven.asimov.it.utils.date.TimeZones;
import com.seven.asimov.it.utils.exceptionhandler.ExceptionSearchEngine;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.tcpdump.DbAdapter;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.conn.util.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

/**
 * Instrumentation test runner that performs the following actions while setting up a shared test fixture:
 * <ul>
 * <li>Extracts tcpdump binary</li>
 * <li>Extracts default zipping dictionary</li>
 * <li>Starts Asimov service by sending an intent
 * </ul>
 * After all tests get executed this runner executes the following actions:
 * <ul>
 * <li> Stops tcpdump processes </li>
 * <li> Copies all testing artifacts to SD Card </li>
 * </ul>
 * This test runner must be used instead of the default one for executing all integration tests, i.e. tests under
 * <code>com.seven.asimov.it</code> package.
 */

public class IntegrationTestRunnerGa extends FixtureSharingTestRunner {
    private static final Logger logger = LoggerFactory.getLogger(IntegrationTestRunnerGa.class.getSimpleName());
    public static final String RESULTS_DIR = BaseConstantsIF.SD_CARD + "/OCIntegrationTestsResults/";
    public static final String LOGCATS = "logcats/";
    public static final String SERVER_LOGS = "serverLogs/";
    private static final String POLICY_UPDATE_PATH = "@asimov";
    private static final String JUNIT_XML_FILE = "TEST-all.xml";
    private static final String TCPDUMP_BIN = "bin/tcpdump-arm";
    private static final String TEST_MEDIA = "media/test.mp3";
    private static final String TCPDUMP_BIN_NEW = "bin/tcpdump-arm-new";
    private static final String TCPKILL_BIN = "bin/tcpkill";
    private static volatile IntegrationTestRunnerGa instance;
    private static Context staticContext;
    private Writer mWriter;
    private XmlSerializer mTestSuiteSerializer;
    private String mFilesDir;
    private String mTcpdumpDatabasePath;
    private String mTcpdumpPath;
    private String mTcpdumpPathNew;
    public static String mTcpkillPath;

    private List<Integer> initialLogcatPids = new ArrayList<Integer>();

    public IntegrationTestRunnerGa() {
        super();
        instance = this;
    }

    public void setFilesDir(Context context) {
        mFilesDir = context.getFilesDir().getAbsolutePath();
    }

    @Override
    public void onStart() {
        try {
            staticContext = getContext();
            ExceptionSearchEngine.init(getContext());
            PropertyLoadUtil.init(getContext());
            PropertyLoadUtil.initIPAndTestRunner();
            DateUtil.setTimeZoneOnDevice(getContext(), TimeZone.getTimeZone("GMT"));
            DateUtil.syncTimeWithTestRunner();
            mFilesDir = getTargetContext().getFilesDir().getAbsolutePath();
            logger.debug("TargetDir" + mFilesDir);
            mTcpdumpDatabasePath = getTargetContext().getDatabasePath("tcpdump.db").toString();
            mTcpdumpPath = mFilesDir + File.separator + TCPDUMP_BIN;
            mTcpdumpPathNew = mFilesDir + File.separator + TCPDUMP_BIN_NEW;
            mTcpkillPath = mFilesDir + File.separator + TCPKILL_BIN;
            File binDir = new File(mFilesDir + File.separator + "bin/");
            binDir.mkdirs();
            extractFromAssets(TCPDUMP_BIN, mTcpdumpPath);
            extractFromAssets(TCPDUMP_BIN_NEW, mTcpdumpPathNew);
            extractFromAssets(TCPKILL_BIN, mTcpkillPath);

            initialLogcatPids = LogcatUtil.getLogcatPids();

            String[] chmodBinDir = {"su", "-c", "chmod -R 777 " + binDir};
            String[] chmodTestResults = {"su", "-c", "chmod -R 777 " + RESULTS_DIR};
            String[] mkDirPcaps = {"su", "-c", "mkdir " + RESULTS_DIR + "pcaps/"};
            String[] mkDirMedia = {"su", "-c", "mkdir " + RESULTS_DIR + "media/"};
            String[] mkDirLogcats = {"su", "-c", "mkdir " + RESULTS_DIR + LOGCATS};
            String[] mkDirServerLogs = {"su", "-c", "mkdir " + RESULTS_DIR + LOGCATS + SERVER_LOGS};
            String[] startFullTcpdump = {"su", "-c", mTcpdumpPathNew + " -i any -s 0 -Uw " + RESULTS_DIR + "tcpdump_log.trace not tcp port 5555 &"};
            String[] startFullLogcat = {"su", "-c", "logcat -v time  > " + RESULTS_DIR + LOGCATS + "logcat.txt &"};

            ShellUtil.killAll(mTcpdumpPath, 9);
            Runtime.getRuntime().exec(chmodBinDir).waitFor();
            clearResultDirs();
            Runtime.getRuntime().exec(chmodTestResults).waitFor();
            Runtime.getRuntime().exec(mkDirPcaps).waitFor();
            Runtime.getRuntime().exec(mkDirMedia).waitFor();
            Runtime.getRuntime().exec(mkDirLogcats).waitFor();
            Runtime.getRuntime().exec(mkDirServerLogs).waitFor();
            Runtime.getRuntime().exec(startFullTcpdump).waitFor();
            Runtime.getRuntime().exec(startFullLogcat).waitFor();
            Runtime.getRuntime().exec(chmodTestResults).waitFor();
            extractFromAssets(TEST_MEDIA, RESULTS_DIR + TEST_MEDIA);

            startJUnitOutput(new FileWriter(RESULTS_DIR + JUNIT_XML_FILE, false));

            logger.trace("Checking time zones: \nHTK" + TimeZones.valueOf("HKT").getId());
            logger.trace("\nGMT" + TimeZones.valueOf("GMT").getId());
            logger.trace("\nHTK" + TimeZones.valueOf("HKT").getId());
            logger.trace("\nPDT" + TimeZones.valueOf("PDT").getId());
            logger.trace("\nEET" + TimeZones.valueOf("EET").getId());
            logger.trace("\nEEST" + TimeZones.valueOf("EEST").getId());
            logger.trace("\nCAT" + TimeZones.valueOf("CAT").getId());
            logger.trace("\nCST" + TimeZones.valueOf("CST").getId());
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }

        logger.error(">>>>>Constants");
        logger.error(">>>>>" + TFConstantsIF.IP_VERSION);
        logger.error(">>>>>Constants");

        super.onStart();
    }

    private String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    String sAddr = addr.getHostAddress().toUpperCase();
                    boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                    if (!addr.isAnyLocalAddress() &&
                            !addr.isLinkLocalAddress() &&
                            !addr.isLoopbackAddress() &&
                            !addr.isMCGlobal() &&
                            !addr.isMCLinkLocal() &&
                            !addr.isMCNodeLocal() &&
                            !addr.isMCOrgLocal() &&
                            !addr.isMCSiteLocal() &&
                            !addr.isMulticastAddress()) {
                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if ((!isIPv4) && (!addr.isSiteLocalAddress())) {
                                int delim = sAddr.indexOf('%'); // drop ip6 port suffix
                                return delim < 0 ? sAddr : sAddr.substring(0, delim);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
        } // for now eat exceptions
        return "";
    }

    private void checkNetworkAvailability() throws Exception {
        ConnectivityManager connManager = (ConnectivityManager) getContext().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mMobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        logger.trace("###INIT WIFI NetworkInfo###\n" + mWifi.toString());
        logger.trace("###INIT MOBILE NetworkInfo###\n" + mMobile.toString());

        StringBuilder builder = new StringBuilder();
        String line;
        Process ipaddrProcess = null;
        try {
            ipaddrProcess = Runtime.getRuntime().exec("ip addr");
            BufferedReader reader = new BufferedReader(new InputStreamReader(ipaddrProcess.getInputStream()));
            while ((line = reader.readLine()) != null) {
                builder.append(line + "\n");
            }
        } catch (IOException e) {
            logger.error("Failed to list local ip addresses with 'ip addr' util");
        }

        logger.trace("###INIT LOCAL IP ADDRESSES###\n" + builder.toString() + "\n###INIT LOCAL IP ADDRESSES END###");

        String ipv4 = getIPAddress(true);
        String ipv6 = getIPAddress(false);
        logger.trace("###INIT Found IPv4: " + ipv4 + "\n" + "Found IPv6: " + ipv6);
        if ((TFConstantsIF.IP_VERSION == TFConstantsIF.IP4_VERSION) && (ipv4.equals("")))
            endSuite(new Exception("\n\n\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n" +
                    "Your device doesn't have usable IPv4 address.\n" +
                    "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n\n\n"));


        if ((TFConstantsIF.IP_VERSION == TFConstantsIF.IP6_VERSION) && (ipv6.equals("")))
            endSuite(new Exception("\n\n\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n" +
                    "Your device doesn't have usable IPv6 address,\n" +
                    "but this TF if built with IPv6 enabled.\n" +
                    "Please check your network settings.\n" +
                    "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n\n\n"));

        if (TFConstantsIF.EXTERNAL_IP.equals(""))
            endSuite(new Exception("\n\n\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n" +
                    "Failed to resolve IPv4 address for '" + PropertyLoadUtil.getProperty("system.relay_host") + "'.\n" +
                    "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n\n\n"));

        if (AsimovTestCase.TEST_RESOURCE_HOST.trim().equals(""))
            endSuite(new Exception("\n\n\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n" +
                    "Testrunner hostname is empty.\n" +
                    "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n\n\n"));
    }

    @Override
    protected void setUp() throws Exception {
        checkNetworkAvailability();
        LogcatUtil.cleanup();

        try {
            if (TFConstantsIF.START_CHECKS) {
                OnStartChecks.INSTACE.installOC(getContext());
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            endSuite(e);
        }

        updateIpTables(true, true);

//        OpenChannelHelper.setCurrentClientType(OpenChannelHelper.ClientType.GA);
        logger.info("Current device is: " + Build.MODEL);
        logger.info("Current SDK is: " + Build.VERSION.SDK_INT);

        /*
         * PMSUtil.getPmsServerIp() starts TFProperties loading TF constants.
         * TFProperties performs checks and throws exception if needed
         */
        try {
            logger.info("PMSUtil.getPmsServerIp() = " + PMSUtil.getPmsServerIp());
        } catch (Throwable tr) {
            logger.error(ExceptionUtils.getStackTrace(tr));
            endSuite(new Exception(tr));
        }
        String[] mkdir = {"su", "-c", "mkdir /data/misc/openchannel"};
        String[] chmod = {"su", "-c", "chmod 777 /data/misc/openchannel"};
        String[] chmodIpTables = {"su", "-c", "chmod  777 " + TFConstantsIF.PATH};
        String install = "am broadcast -a android.intent.action.OC_ENGINE_INSTALL";
        String[] dispatchers = {"su", "-c", "/data/misc/openchannel/ocd &"};
        String run = "am broadcast -a android.intent.action.OC_ENGINE";

        String[] startService = new String[]{"su", "-c", "am startservice com.seven.asimov/.ocengine.OCEngineService"};

        try {
            if (TFConstantsIF.START_CHECKS) {
                TestUtil.sleep(4 * 30000);
                OnStartChecks.INSTACE.fullStartCheck();
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            endSuite(e);
        }

        if (Build.MODEL.equals("functional_tests") && !OCUtil.isOpenChannelRunning()) {
            Runtime.getRuntime().exec(mkdir).waitFor();
            Runtime.getRuntime().exec(chmod).waitFor();
            Runtime.getRuntime().exec(chmodIpTables).waitFor();
            Runtime.getRuntime().exec(install).waitFor();
            logger.debug("asimov ocengine installed");
            TestUtil.sleep(20000);
            Runtime.getRuntime().exec(dispatchers).waitFor();
            logger.debug("oc dispatchers started");
            TestUtil.sleep(25000);
            Runtime.getRuntime().exec(run).waitFor();
            logger.debug("asimov ocengine started");
            TestUtil.sleep(60000);
        } else {
            Runtime.getRuntime().exec(startService).waitFor();
            TestUtil.sleep(60000);
        }
        //-------//
        //Resolve host for fix problem:
        //[https_task.cpp:70] (-14) - FC [...]: failed to back-resolve IP ..., will bypass connection
        try {
            DnsUtil.resolveHost(AsimovTestCase.TEST_RESOURCE_HOST);
        } catch (Exception e) {
            logger.warn("The Host " + AsimovTestCase.TEST_RESOURCE_HOST + " was not resolved. May have problems in some tests");
        }
        //-------//
        PMSUtil.clearPoliciesByRegexp("forUpdate.*", POLICY_UPDATE_PATH);
    }

    @Override
    protected void tearDown() throws Exception {
        PMSUtil.clearPoliciesByRegexp("forUpdate.*", POLICY_UPDATE_PATH);
        DateUtil.resetDeviceTimeZoneToDefault(getContext());
        DateUtil.syncTimeWithTestRunner();
        ServerLogsDownloader.getServerLogs(getContext());
//        String[] killoc = { "su", "-c", "killall occ" };
//        Runtime.getRuntime().exec(killoc).waitFor();
        TestUtil.sleep(15 * 1000);
    }

    @Override
    public void sendStatus(int resultCode, Bundle results) {
        super.sendStatus(resultCode, results);
        switch (resultCode) {
            case REPORT_VALUE_RESULT_ERROR:
            case REPORT_VALUE_RESULT_FAILURE:
            case REPORT_VALUE_RESULT_OK:
                try {
                    recordTestResult(resultCode, results);
                } catch (IOException e) {
                    logger.error("Exception while recording test result: " + e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                break;
            case REPORT_VALUE_RESULT_START:
                recordTestStart(results);
                break;
            default:
                logger.error("Unknown result code: " + resultCode);
                break;
        }
    }

    @Override
    public void finish(int resultCode, Bundle results) {
        endTestSuites();
        try {
            ShellUtil.killAll(mTcpdumpPath);
            ShellUtil.killAll(mTcpdumpPathNew);
            updateIpTables(false, false);
            IOUtil.safeCopyDirectory(mFilesDir, RESULTS_DIR);
            IOUtil.safeMoveFile(RESULTS_DIR + JUNIT_XML_FILE, RESULTS_DIR + "surefire-reports/" + JUNIT_XML_FILE);
            IOUtil.safeMoveFile(RESULTS_DIR + "tcpdump_log.trace", RESULTS_DIR + "trace/" + "trace.pcap");
            IOUtil.safeMoveFile(mTcpdumpDatabasePath, RESULTS_DIR + "databases/" + "tcpdump.db");
            IOUtil.safeCopyFileToDir(getTargetContext().getDatabasePath(DbAdapter.DATABASE_NAME).getAbsolutePath(), RESULTS_DIR);
            IOUtil.safeCopyDirectory("/data/misc/openchannel/sys_log", RESULTS_DIR + "sys_log");
            IOUtil.safeCopyDirectory("/data/misc/openchannel/debug_log", RESULTS_DIR + "debug_log");
            LogcatUtil.killLogcatProcess(initialLogcatPids);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.finish(resultCode, results);
    }

    void endTestSuites() {
        try {
            mTestSuiteSerializer.startTag(null, "system-out");
            mTestSuiteSerializer.endTag(null, "system-out");
            mTestSuiteSerializer.startTag(null, "system-err");
            mTestSuiteSerializer.endTag(null, "system-err");
            mTestSuiteSerializer.endTag(null, "testsuite");

            mTestSuiteSerializer.endTag(null, "testsuites");
            mTestSuiteSerializer.endDocument();
            mTestSuiteSerializer.flush();
            mWriter.flush();
            mWriter.close();
        } catch (IOException e) {
            logger.error("Exception while recording end of testsuites : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    void startJUnitOutput(Writer writer) {
        try {
            mWriter = writer;
            mTestSuiteSerializer = newSerializer(mWriter);
            mTestSuiteSerializer.startDocument(null, null);
            mTestSuiteSerializer.startTag(null, "testsuites");
            mTestSuiteSerializer.startTag(null, "testsuite");
        } catch (Exception e) {
            logger.error("Exception starting JUnit output: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private XmlSerializer newSerializer(Writer writer) {
        try {
            XmlPullParserFactory pf = XmlPullParserFactory.newInstance();
            XmlSerializer serializer = pf.newSerializer();
            serializer.setOutput(writer);
            return serializer;
        } catch (Exception e) {
            logger.error("Exception creating XmlSerializer: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    void recordTestStart(Bundle results) {
        // TODO
    }

    void recordTestResult(int resultCode, Bundle results) throws IOException {
        long start_time = results.getLong(REPORT_KEY_START_TIME);
        float time = (System.currentTimeMillis() - start_time) / 1000.0f;
        String className = results.getString(REPORT_KEY_NAME_CLASS);
        String testMethod = results.getString(REPORT_KEY_NAME_TEST);
        String stack = results.getString(REPORT_KEY_STACK);

        synchronized (mTestSuiteSerializer) {
            mTestSuiteSerializer.startTag(null, "testcase");
            mTestSuiteSerializer.attribute(null, "classname", className);
            mTestSuiteSerializer.attribute(null, "name", testMethod);

            if (resultCode != REPORT_VALUE_RESULT_OK) {
                mTestSuiteSerializer.startTag(null, "failure");
                if (stack != null) {
                    String reason = stack.substring(0, stack.indexOf('\n'));
                    String message = "";
                    int index = reason.indexOf(':');
                    if (index > -1) {
                        message = reason.substring(index + 1);
                        reason = reason.substring(0, index);
                    }
                    mTestSuiteSerializer.attribute(null, "message", message);
                    mTestSuiteSerializer.attribute(null, "type", reason);
                    mTestSuiteSerializer.text(stack);
                }
                mTestSuiteSerializer.endTag(null, "failure");
            } else {
                mTestSuiteSerializer.attribute(null, "time", String.format("%.3f", time));
            }
            mTestSuiteSerializer.endTag(null, "testcase");
        }
    }

    private boolean extractFromAssets(String assetName, String dest) {
        AssetManager assetMgr = getTargetContext().getAssets();
        InputStream in = null;
        OutputStream out = null;
        try {
            File outFile = new File(dest);
            if (!outFile.exists()) {
                in = assetMgr.open(assetName);
                out = new FileOutputStream(dest);
                IOUtil.transfer(in, out, new byte[4096]);
                logger.info("extracted " + assetName + " to " + dest);
                return true;
            }
        } catch (Exception e) {
            logger.error("Error extracting " + assetName, e);
        } finally {
            IOUtil.safeClose(in);
            IOUtil.safeClose(out);
        }
        return false;
    }

    public static IntegrationTestRunnerGa getInstance() {
        if (instance == null)
            instance = new IntegrationTestRunnerGa();
        return instance;
    }

    public String getFilesDir() {
        return mFilesDir;
    }

    public String getTcpdumpPath() {
        return mTcpdumpPath;
    }

    public static String getTestMedia() {
        return RESULTS_DIR + TEST_MEDIA;
    }

    private static void updateIpTables(boolean add, boolean setUp) throws Exception {
        if (TFConstantsIF.TPROXY == 0) {
            IpTablesUtil.bypassPort(8087, add);
            IpTablesUtil.bypassPort(8099, add);
        } else if (setUp) {
            PMSUtil.addConfigurationBypassPorts(add);
        }
    }

    private void clearResultDirs() {
        String[] testResultDirs = {"pcaps", "media", "logcats", "databases", "bin", "res", "trace", "surefire-reports"};
        try {
            for (String dir : testResultDirs) {
                Runtime.getRuntime().exec(new String[]{"su", "-c", "rm -r " + RESULTS_DIR + dir}).waitFor();
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }
    }

    public static Context getStaticContext() {
        return staticContext;
    }
}