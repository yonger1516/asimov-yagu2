package com.seven.asimov.it.tests.dispatchers.configurable;

import android.util.Log;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.testcases.ConfigurableDispatchersTestCase;
import com.seven.asimov.it.utils.*;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.CLQConstructedTask;
import com.seven.asimov.it.utils.logcat.tasks.crcsTasks.NetlogTask;
import com.seven.asimov.it.utils.logcat.tasks.dispatcherTasks.CLQConstructedDnsTask;
import com.seven.asimov.it.utils.logcat.tasks.dispatcherTasks.DefaultConfigReInstalledTask;
import com.seven.asimov.it.utils.logcat.tasks.dispatcherTasks.DispatcherConfigurationInvalidTask;
import com.seven.asimov.it.utils.logcat.tasks.dispatcherTasks.DispatcherStateTask;
import com.seven.asimov.it.utils.logcat.tasks.failoverTasks.FailoverStartTask;
import com.seven.asimov.it.utils.logcat.tasks.failoverTasks.FailoverStopTask;
import com.seven.asimov.it.utils.logcat.tasks.policyTasks.PolicyAddedTask;
import com.seven.asimov.it.utils.logcat.wrappers.*;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.tcpdump.TcpDumpUtil;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Pre-request:
 * <p/>Switch ON Debug Mode - Keep Alive on device<p/>
 * <p/>Install and configure com.accuweather.android<p/>
 * <p/>Remove pre-configured dispatchers @asimov@interception@ in personal scope on pms server<p/>
 * Configurable Dispatchers test:
 * ({@link com.seven.asimov.it.tests.dispatchers.configurable.ConfigurableDispatchers#test_001_checkAccessRightsToDefaultConfig()}   Test1)
 * ({@link com.seven.asimov.it.tests.dispatchers.configurable.ConfigurableDispatchers#test_002_NonDefaultConfigFileShouldBeCreated()}   Test2)
 * ({@link com.seven.asimov.it.tests.dispatchers.configurable.ConfigurableDispatchers#test_003_configFileWithWrongPortShouldNotBeAppliedToOC()}   Test3)
 * ({@link com.seven.asimov.it.tests.dispatchers.configurable.ConfigurableDispatchers#test_004_configFileWithEmptyPortShouldNotBeAppliedToOC()}   Test4)
 * ({@link com.seven.asimov.it.tests.dispatchers.configurable.ConfigurableDispatchers#test_005_configFileWithEmptyZOrderShouldBeAppliedToOC()}   Test5)
 * ({@link com.seven.asimov.it.tests.dispatchers.configurable.ConfigurableDispatchers#test_006_configFileWithWrongZOrderShouldNotBeAppliedToOC()}   Test6)
 * ({@link com.seven.asimov.it.tests.dispatchers.configurable.ConfigurableDispatchers#test_007_configFileWithWrongTypeShouldNotBeAppliedToOC()}   Test7)
 * ({@link com.seven.asimov.it.tests.dispatchers.configurable.ConfigurableDispatchers#test_008_configFileWithEmptyTypeShouldNotBeAppliedToOC()}   Test8)
 * ({@link com.seven.asimov.it.tests.dispatchers.configurable.ConfigurableDispatchers#test_009_defaultConfigFileShouldBeReinstalled()}   Test9)
 * ({@link com.seven.asimov.it.tests.dispatchers.configurable.ConfigurableDispatchers#test_010_HttpTrafficCanBeBypassedInWiFiMode()}   Test10)
 * ({@link com.seven.asimov.it.tests.dispatchers.configurable.ConfigurableDispatchers#test_011_HttpTrafficCanBeBypassedIn3GMode()}   Test11)
 * ({@link com.seven.asimov.it.tests.dispatchers.configurable.ConfigurableDispatchers#test_012_HTTPTrafficForApplicationCanBeBypassedInWiFiMode()}   Test12)
 * ({@link com.seven.asimov.it.tests.dispatchers.configurable.ConfigurableDispatchers#test_013_HTTPTrafficForApplicationCanBeBypassedIn3GMode()}   Test13)
 * ({@link com.seven.asimov.it.tests.dispatchers.configurable.ConfigurableDispatchers#test_014_KillingOfOCProcessShouldNotSkipBypassList()}   Test14)
 * ({@link com.seven.asimov.it.tests.dispatchers.configurable.ConfigurableDispatchers#test_015_ChanginqgOfNetworkShouldNotSkipBypassList()}   Test15)
 * ({@link com.seven.asimov.it.tests.dispatchers.configurable.ConfigurableDispatchers#test_016_blacklistedHttpTrafficShouldNotBeOptimized()}   Test16)
 * ({@link com.seven.asimov.it.tests.dispatchers.configurable.ConfigurableDispatchers#test_017_sendKillSignalToEngineWhenOCisInFailover()}   Test17)
 * ({@link com.seven.asimov.it.tests.dispatchers.configurable.ConfigurableDispatchers#test_018_sendKillSignalToControllerWhenOCisInFailover()}   Test18)
 * ({@link com.seven.asimov.it.tests.dispatchers.configurable.ConfigurableDispatchers#test_019_KillEngineInCaseStopOfFailover()}   Test19)
 * ({@link com.seven.asimov.it.tests.dispatchers.configurable.ConfigurableDispatchers#test_020_KillControllerInCaseStopOfFailover()}   Test20)
 * ({@link com.seven.asimov.it.tests.dispatchers.configurable.ConfigurableDispatchers#test_021_afterReceivingPolicyOptDispatcherNotChangeItState()}   Test21)
 * ({@link com.seven.asimov.it.tests.dispatchers.configurable.ConfigurableDispatchers#test_022_InterceptionPortsForTcpDispatcher()}   Test22)
 * ({@link com.seven.asimov.it.tests.dispatchers.configurable.ConfigurableDispatchers#test_023_InterceptionPortsForUdpDispatcher()}   Test23)
 * ({@link com.seven.asimov.it.tests.dispatchers.configurable.ConfigurableDispatchers#test_024_UdbAndTcpTrafficShouldBeOptimizedByUdpAndTcpAccordingly()}   Test24)
 * ({@link com.seven.asimov.it.tests.dispatchers.configurable.ConfigurableDispatchers#test_025_secondDispatcherShouldStartToProceedAllTransactions()}   Test25)
 * ({@link com.seven.asimov.it.tests.dispatchers.configurable.ConfigurableDispatchers#test_026_ReconfigurationFromTwoToOneDispatcher()}   Test26)
 * ({@link com.seven.asimov.it.tests.dispatchers.configurable.ConfigurableDispatchers#test_027_AfterSwitchingTransparentModeOnDispatcherStopped()}   Test27)
 * ({@link com.seven.asimov.it.tests.dispatchers.configurable.ConfigurableDispatchers#test_028_newConfigurationShouldNotBeApplied()}   Test28)
 * ({@link com.seven.asimov.it.tests.dispatchers.configurable.ConfigurableDispatchers#test_029_EveryTcpDispatcherShouldListenToItsPort()}   Test29)
 * ({@link com.seven.asimov.it.tests.dispatchers.configurable.ConfigurableDispatchers#test_030_OnlyOneOfTheSameTcpDispatchersShouldProcessTraffic()}   Test30)
 * ({@link com.seven.asimov.it.tests.dispatchers.configurable.ConfigurableDispatchers#test_031_TcpDispatcherWithHigherPriorityShouldProcessTraffic()}   Test31)
 * ({@link com.seven.asimov.it.tests.dispatchers.configurable.ConfigurableDispatchers#test_032_BehaviorOfDispatcherAfterReceivingKillNine()}   Test32)
 * ({@link com.seven.asimov.it.tests.dispatchers.configurable.ConfigurableDispatchers#test_033_BehaviorOfDispatcherAfterReceivingKillOne()}   Test33)
 * ({@link com.seven.asimov.it.tests.dispatchers.configurable.ConfigurableDispatchers#test_034_defaultConfigFileShouldBeUninstalledWithOC}   Test34)
 */

public class ConfigurableDispatchers extends ConfigurableDispatchersTestCase {

    private static final String TAG = ConfigurableDispatchers.class.getSimpleName();
    private static final String RESULTS_PATH = TFConstantsIF.OC_INTEGRATION_TESTS_RESULTS_DIR;
    private final String OC_HOME_DIRECTORY = "/data/misc/openchannel/";
    private final String DEFAULT_CFG = "dispatchers_default.cfg";
    private final String NON_DEFAULT_CFG = "dispatchers.cfg";
    private static final String OC_APK_FILE_NAME = "/sdcard/asimov-signed.apk";
    private static final int OC_INSTALL_TIME = 3 * 60 * 1000;
    private static final String ROAMING_FAILOVER_PATH = "@asimov@failovers@roaming_wifi";
    private static final String MOBILE_FAILOVER_PATH = "@asimov@failovers@mobile";
    private static final String BROWSER_PACKAGE = "com.android.browser";
    private static final String ASIMOV_PATH = "@asimov";
    private static final String BY_PASS_LIST = "bypass_list";
    private static final String WHITE_LIST = "blacklist";
    private static final String ENABLED = "enabled";
    private static final String ACTIONS = "actions";
    private static final String RETRIES = "retries";
    private static final String TIMEOUT = "timeout";
    private static final String ATTEMPT_INTERVAL = "attempt_interval";
    private static final long oneMinute = 60000L;
    private static final long twoMinutes = 120000L;
    private static final String ENGINE_PROCESS = "com.seven.asimov";
    private static final String CONTROLLER_PROCESS = "occ";


    /**
     * Config file “dispatchers_default.cfg” should be unpacked to OC home directory with permissions -r--r-----
     * TestLab test case name: DISPCONFFILE_01
     */
    public void test_001_checkAccessRightsToDefaultConfig() {
        final String expectedRights = "-r--r-----";
        String rights = ShellUtil.getAccessRightsOfFile(OC_HOME_DIRECTORY, DEFAULT_CFG);
        assertNotNull("File " + DEFAULT_CFG + " not found or unable to access it", rights);
        assertEquals("Access rights of file should be equals to expected", expectedRights, rights);
    }


    /**
     * Non-default config file should be created in OC home directory
     * Tcp and udp dispatchers should shutdown and start again.
     * <ol>
     * <li>Create namespace “tcp” and add properties  “interception ports” with value 1,
     * “type” with value 1 in interception branch of PMS</li>
     * <li>Create namespace “udp” and add properties  “interception ports” with value 1,
     * “type” with value 2 in interception branch of PMS</li>
     * <li>Be sure that policies arrive and they are applied</li>
     * </ol>
     * <p/>
     * TestLab test case name: DISPCONFFILE_08
     *
     * @throws Exception
     */
    public void test_002_NonDefaultConfigFileShouldBeCreated() throws Exception {
        List<String> idTcpList = null;
        List<String> idUdpList = null;
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), policyAddedTask);
        logcatUtil.start();
        try {
            idTcpList = addPersonalInterceptionProperty("tcp", "1", "1", "0");
            idUdpList = addPersonalInterceptionProperty("udp", "1", "2", "0");
            logSleeping(30 * 1000); //wait for policy
            logcatUtil.stop();
            boolean policyReceived = checkInterceptionPolicyReceived(policyAddedTask, "1", "1", "0");
            assertTrue("Interception policy wasn't received", policyReceived);
            File file = new File(OC_HOME_DIRECTORY + NON_DEFAULT_CFG);
            assertTrue("Interception policy has applied but non-default config was not exists", file.exists());
        } finally {
            logcatUtil.stop();
            removePersonalInterceptionProperties(idTcpList);
            removePersonalInterceptionProperties(idUdpList);
            logSleeping(15 * 1000);
        }
    }

    /**
     * Config file with wrong port shouldn't be applied to OC
     * <ol>
     * <li>Create namespace “tcp” and add properties  “interception ports” with value %!#%@,
     * “type” with value 1 in interception branch of PMS</li>
     * <li>Validation of non-default config file should fail</li>
     * </ol>
     * <p/>
     * TestLab test case name: DISPCONFFILE_13, DISPCONFFILE_22
     *
     * @throws InterruptedException
     */
    public void test_003_configFileWithWrongPortShouldNotBeAppliedToOC() throws InterruptedException {
        List<String> idTcpList = null;
        DispatcherConfigurationInvalidTask disConfInvTask = new DispatcherConfigurationInvalidTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), disConfInvTask);
        logcatUtil.start();
        try {
            idTcpList = addPersonalInterceptionProperty("tcp", "%!#%@", "1", "0");
            logSleeping(30 * 1000);
            logcatUtil.stop();
            int entriesSize = disConfInvTask.getLogEntries().size();
            assertTrue("Config file with wrong port should not be applied to OC. At least one message of invalid dispatcher's " +
                    "configuration should be present in log, but was " + entriesSize, entriesSize > 0);
        } finally {
            removePersonalInterceptionProperties(idTcpList);
            logSleeping(15 * 1000);
            logcatUtil.stop();
        }

    }

    /**
     * Config file with empty port shouldn't be applied to OC
     * <ol>
     * <li>Create namespace “tcp” and add properties  “interception ports” without value,
     * “type” with value 1 in interception branch of PMS</li>
     * <li>Validation of non-default config file should fail</li>
     * <li>Settings from default config file should be applied to OC.</li>
     * </ol>
     * <p/>
     * TestLab test case name:  DISPCONFFILE_14
     *
     * @throws InterruptedException
     */
    public void test_004_configFileWithEmptyPortShouldNotBeAppliedToOC() throws InterruptedException {
        List<String> idTcpList = null;
        DispatcherConfigurationInvalidTask disConfInvTask = new DispatcherConfigurationInvalidTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), disConfInvTask);
        logcatUtil.start();
        try {
            idTcpList = addPersonalInterceptionProperty("tcp", "", "1", "0");
            logSleeping(30 * 1000);
            logcatUtil.stop();
            boolean msgFound = false;
            List<DispatchersConfigurationInvalidWrapper> list = disConfInvTask.getLogEntries();
            assertFalse("Invalid dispatcher's configuration messages not present in log at all", list.isEmpty());
            for (DispatchersConfigurationInvalidWrapper entry : list) {
                if (entry.getPorts().equals("")) {
                    msgFound = true;
                    break;
                }
            }
            assertTrue("Config file with empty port should not be applied to OC. A message of invalid dispatcher's " +
                    "configuration should be present in log ", msgFound);
        } finally {
            removePersonalInterceptionProperties(idTcpList);
            logSleeping(15 * 1000);
            logcatUtil.stop();
        }
    }

    /**
     * Config file with empty z_order should be applied to OC
     * <ol>
     * <li>Create namespace “tcp” and add properties  “interception ports” with value 6,
     * “type” with value 1 and z_order without value in interception branch of PMS</li>
     * <li>Be sure that policies arrive and they are applied</li>
     * <li>Non-default config file should be created in OC home directory</li>
     * <li> Z_order should be 0 in non-default config file.</li>
     * </ol>
     * <p/>
     * TestLab test case name: DISPCONFFILE_15
     *
     * @throws InterruptedException
     */
    public void test_005_configFileWithEmptyZOrderShouldBeAppliedToOC() throws InterruptedException {
        List<String> idTcpList = null;
        Scanner scanner = null;
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), policyAddedTask);
        logcatUtil.start();
        try {
            idTcpList = addPersonalInterceptionProperty("tcp", "6", "1", "");
            logSleeping(oneMinute);
            logcatUtil.stop();
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, INTERCEPTION_PORTS, "6") &&
                    checkPolicyReceived(policyAddedTask, TYPE, "1"));
            ShellUtil.copyFile(OC_HOME_DIRECTORY + NON_DEFAULT_CFG, RESULTS_PATH + NON_DEFAULT_CFG);
            scanner = new Scanner(new FileReader(new File(RESULTS_PATH + NON_DEFAULT_CFG)));
            while (scanner.hasNext()) {
                String str = scanner.nextLine();
                if (str.startsWith("tcp;")) {
                    String[] arr = str.split(";");
                    assertEquals("Z_order should be 0 in non-default config file, but was \"" + arr[3] + "\"",
                            "0", arr[3]);
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
            throw new AssertionError("File " + NON_DEFAULT_CFG + " not found");
        } finally {
            if (scanner != null) {
                scanner.close();
            }
            removePersonalInterceptionProperties(idTcpList);
            logSleeping(15 * 1000);
            logcatUtil.stop();
        }
    }

    /**
     * Config file with wrong z_order should not be applied to OC
     * <ol>
     * <li>Create namespace “tcp” and add properties  “interception ports” with value 7,
     * “type” with value 1  and z_order with value -1 in interception branch of PMS</li>
     * <li>Be sure that policies arrive and they are applied</li>
     * <li>Validation of non-default config file should fail</li>
     * <li>Settings from default config file should be applied to OC</li>
     * </ol>
     * TestLab test case name:  DISPCONFFILE_16
     */
    public void test_006_configFileWithWrongZOrderShouldNotBeAppliedToOC() throws InterruptedException {
        List<String> idTcpList = null;
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        DispatcherConfigurationInvalidTask disConfInvTask = new DispatcherConfigurationInvalidTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), policyAddedTask, disConfInvTask);
        logcatUtil.start();
        try {
            idTcpList = addPersonalInterceptionProperty("tcp", "7", "1", "-1");
            logSleeping(oneMinute);
            logcatUtil.stop();
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, INTERCEPTION_PORTS, "7") &&
                    checkPolicyReceived(policyAddedTask, TYPE, "1"));
            boolean msgFound = false;
            List<DispatchersConfigurationInvalidWrapper> list = disConfInvTask.getLogEntries();
            assertFalse("Invalid dispatcher's configuration messages not present in log at all", list.isEmpty());
            for (DispatchersConfigurationInvalidWrapper e : list) {
                Log.i(TAG, e.toString());
            }
            for (DispatchersConfigurationInvalidWrapper entry : list) {
                if (entry.isInvalidMsgFound()) {
                    msgFound = true;
                    break;
                }
            }
            assertTrue("Config file with wrong z_order should not be applied to OC. A message of invalid dispatcher's " +
                    "configuration should be present in log ", msgFound);
        } finally {
            removePersonalInterceptionProperties(idTcpList);
            logSleeping(15 * 1000);
            logcatUtil.stop();
        }
    }

    /**
     * Config file with wrong type should not be applied to OC
     * <ol>
     * <li>Create namespace “tcp” and add properties  “interception ports” with value 8, “type” with value 3 in interception branch of PMS</li>
     * <li>Validation of non-default config file should fail</li>
     * <li>Settings from default config file should be applied to OC</li>
     * </ol>
     * <p/>
     * TestLab test case name: DISPCONFFILE_17
     *
     * @throws InterruptedException
     */
    public void test_007_configFileWithWrongTypeShouldNotBeAppliedToOC() throws InterruptedException {
        List<String> idTcpList = null;
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        DispatcherConfigurationInvalidTask disConfInvTask = new DispatcherConfigurationInvalidTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), policyAddedTask, disConfInvTask);
        logcatUtil.start();
        try {
            idTcpList = addPersonalInterceptionProperty("tcp", "8", "3", "0");
            logSleeping(oneMinute);
            logcatUtil.stop();
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, INTERCEPTION_PORTS, "8") &&
                    checkPolicyReceived(policyAddedTask, Z_ORDER, "0") && checkPolicyReceived(policyAddedTask, TYPE, "3"));
            boolean msgFound = false;
            List<DispatchersConfigurationInvalidWrapper> list = disConfInvTask.getLogEntries();
            assertFalse("Invalid dispatcher's configuration messages not present in log at all", list.isEmpty());
            for (DispatchersConfigurationInvalidWrapper entry : list) {
                if (entry.getType().equals("0")) {
                    msgFound = true;
                    break;
                }
            }
            assertTrue("Config file with wrong type should not be applied to OC. A message of invalid dispatcher's " +
                    "configuration should be present in log ", msgFound);
        } finally {
            removePersonalInterceptionProperties(idTcpList);
            logSleeping(15 * 1000);
            logcatUtil.stop();
        }
    }

    /**
     * Config file with empty type should not be applied to OC
     * <ol>
     * <li>Create namespace “tcp” and add properties  “interception ports” with value 9, “type” without value in interception branch of PMS</li>
     * <li>Validation of non-default config file should fail</li>
     * <li> Settings from default config file should be applied to OC</li>
     * </ol>
     * <p/>
     * TestLab test case name: DISPCONFFILE_18
     *
     * @throws InterruptedException
     */
    public void test_008_configFileWithEmptyTypeShouldNotBeAppliedToOC() throws InterruptedException {
        List<String> idTcpList = null;
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        DispatcherConfigurationInvalidTask disConfInvTask = new DispatcherConfigurationInvalidTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), policyAddedTask, disConfInvTask);
        logcatUtil.start();
        try {
            idTcpList = addPersonalInterceptionProperty("tcp", "9", "", "0");
            logSleeping(oneMinute);
            logcatUtil.stop();
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, INTERCEPTION_PORTS, "9") &&
                    checkPolicyReceived(policyAddedTask, Z_ORDER, "0") && checkPolicyReceived(policyAddedTask, TYPE, ""));
            boolean msgFound = false;
            List<DispatchersConfigurationInvalidWrapper> list = disConfInvTask.getLogEntries();
            assertFalse("Invalid dispatcher's configuration messages not present in log at all", list.isEmpty());
            for (DispatchersConfigurationInvalidWrapper entry : list) {
                if (entry.getType().equals("0")) {
                    msgFound = true;
                    break;
                }
            }
            assertTrue("Config file with empty type should not be applied to OC. A message of invalid dispatcher's " +
                    "configuration should be present in log ", msgFound);
        } finally {
            removePersonalInterceptionProperties(idTcpList);
            logSleeping(15 * 1000);
            logcatUtil.stop();
        }
    }

    /**
     * Default config file should install after killing OC processes for rooted device
     * <ol>
     * <li>Kill process com.seven.asimov</li>
     * <li>Config file “dispatchers_default.cfg” should be deleted and then unpacked again to OC home directory by OC daemon installer in logcat.</li>
     * </ol>
     * TestLab test case name: DISPCONFFILE_04
     *
     * @throws InterruptedException
     */
    public void test_009_defaultConfigFileShouldBeReinstalled() throws InterruptedException {
        DefaultConfigReInstalledTask defaultConfigReInstalledTask = new DefaultConfigReInstalledTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), defaultConfigReInstalledTask);
        try {
            logcatUtil.start();
            ShellUtil.killAll("com.seven.asimov", 9);
            logSleeping(oneMinute);
            logcatUtil.stop();
            boolean configDeleted = false;
            boolean configInstalled = false;
            List<DefaultConfigReInstalledWrapper> list = defaultConfigReInstalledTask.getLogEntries();
            for (DefaultConfigReInstalledWrapper entry : list) {
                if (entry.isConfigDeleted()) configDeleted = true;
                if (entry.isConfigInstalled()) configInstalled = true;
            }
            assertTrue("Default config file was not deleted or installed", configDeleted && configInstalled);
            File file = new File(OC_HOME_DIRECTORY + DEFAULT_CFG);
            assertTrue("Default config file is not exists ", file.exists());
        } finally {
            logcatUtil.stop();
        }
    }


    /**
     * HTTP traffic can be bypassed in Wi-Fi mode
     * <ol>
     * <li>Add com.android.browser to bypass list</li>
     * <li>Policy should be received and added</li>
     * <li>Wi-Fi access should be enabled</li>
     * <li> In Browser go to ukr.net and google.com</li>
     * <li>Browsers traffic should not be optimized by OC</li>
     * <li>Transactions with resources from step 1 should be seen in tcpdump and should not be seen in logcat</li>
     * </ol>
     * <p/>
     * <p/>
     * TestLab test case name: BYPASS_01
     *
     * @throws InterruptedException
     */
//    @Ignore
    public void test_010_HttpTrafficCanBeBypassedInWiFiMode() throws Exception {
        MobileNetworkUtil mobileNetworkHelper = MobileNetworkUtil.init(getContext());
        assertEquals("Wi-Fi interface was not enabled", mobileNetworkHelper.onWifiOnly(), 1);
        assertTrue("Network is not accessible, after switching network interface", isNetworkAccessible());
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        NetlogTask netlogTask = new NetlogTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), policyAddedTask, netlogTask);
        String idProperty = null;
        String ukrNet = "www.ukr.net";
        String google = "google.com";
        TcpDumpUtil tcpDumpUtil = TcpDumpUtil.getInstance(getContext());
        try {
            logcatUtil.start();
            idProperty = PMSUtil.createPersonalScopeProperty(BY_PASS_LIST, INTERCEPTION_PATH, BROWSER_PACKAGE, true, true);
            logSleeping(oneMinute);
            logcatUtil.stop();
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, BY_PASS_LIST, BROWSER_PACKAGE));
            netlogTask.reset();
            tcpDumpUtil.start();
            logcatUtil.start();
            long startTime = System.currentTimeMillis();
            openBrowser(getContext(), google);
            openBrowser(getContext(), ukrNet);
            logSleeping(oneMinute);
            ShellUtil.killAll(BROWSER_PACKAGE);
            tcpDumpUtil.stop();
            logcatUtil.stop();
            long endTime = System.currentTimeMillis();
            assertTrue("Traffic to " + ukrNet + " was not found in tcp dump",
                    checkTrafficInTcpDumpByHost(tcpDumpUtil, startTime, endTime, ukrNet, false));
            assertFalse("Traffic to " + ukrNet + " was found in logcat, but should not to be",
                    checkTrafficInLogCutByHost(netlogTask.getLogEntries(), ukrNet, BROWSER_PACKAGE));

        } finally {
            if (idProperty != null) PMSUtil.deleteProperty(idProperty);
            tcpDumpUtil.stop();
            logcatUtil.stop();
        }
    }

    /**
     * HTTP traffic can be bypassed in 3G mode
     * <ol>
     * <li>Add com.android.browser to bypass list</li>
     * <li>3G access is enabled</li>
     * <li>Start tcpdump</li>
     * <li>In Browser go to ukr.net and google.com</li>
     * <li>Browsers traffic should not be optimized by OC</li>
     * <li>Transactions with resources should be seen in tcpdump and should not be seen in logcat</li>
     * </ol>
     * <p/>
     * TestLab test case name: BYPASS_02
     *
     * @throws Exception
     */
    public void test_011_HttpTrafficCanBeBypassedIn3GMode() throws Exception {
        MobileNetworkUtil mobileNetworkHelper = MobileNetworkUtil.init(getContext());
        assertEquals("3G interface was not enabled", mobileNetworkHelper.on3gOnly(), 1);
        assertTrue("Network is not accessible, after switching network interface", isNetworkAccessible());
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        NetlogTask netlogTask = new NetlogTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), policyAddedTask, netlogTask);
        String idProperty = null;
        String ukrNet = "www.ukr.net";
        String google = "www.google.com";
        TcpDumpUtil tcpDumpUtil = TcpDumpUtil.getInstance(getContext());

        try {
            logcatUtil.start();
            idProperty = PMSUtil.createPersonalScopeProperty(BY_PASS_LIST, INTERCEPTION_PATH, BROWSER_PACKAGE, true, true);
            logSleeping(oneMinute);
            logcatUtil.stop();
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, BY_PASS_LIST, BROWSER_PACKAGE));
            netlogTask.reset();
            tcpDumpUtil.start();
            logcatUtil.start();
            long startTime = System.currentTimeMillis();
            openBrowser(getContext(), google);
            openBrowser(getContext(), ukrNet);
            logSleeping(oneMinute);
            ShellUtil.killAll(BROWSER_PACKAGE);
            tcpDumpUtil.stop();
            logcatUtil.stop();
            long endTime = System.currentTimeMillis();
            assertTrue("Traffic to " + ukrNet + " was not found in tcp dump",
                    checkTrafficInTcpDumpByHost(tcpDumpUtil, startTime, endTime, ukrNet, false));
            assertFalse("Traffic to " + ukrNet + " was found in logcat, but should not to be",
                    checkTrafficInLogCutByHost(netlogTask.getLogEntries(), ukrNet, BROWSER_PACKAGE));
            assertTrue("Traffic to " + google + " was not found in tcp dump",
                    checkTrafficInTcpDumpByHost(tcpDumpUtil, startTime, endTime, ukrNet, false));
            assertFalse("Traffic to " + google + " was found in logcat, but should not to be",
                    checkTrafficInLogCutByHost(netlogTask.getLogEntries(), ukrNet, BROWSER_PACKAGE));
        } finally {
            if (idProperty != null) PMSUtil.deleteProperty(idProperty);
            tcpDumpUtil.stop();
            logcatUtil.stop();
        }
    }

    /**
     * HTTP traffic can be bypassed in Wi-Fi mode
     * <p/>
     * <p>Required installed com.accuweather.android</p>
     * <ol>
     * <li>Add com.accuweather.android to bypass list</li>
     * <li>Wi-Fi access is enabled</li>
     * <li>Process com.accuweather.android should not be optimized by OC</li>
     * <li>Transactions on process com.accuweather.android should be seen in tcpdump and should not be seen in logcat</li>
     * </ol>
     * <p/>
     * <p/>
     * TestLab test case name: BYPASS_05
     *
     * @throws Exception
     */
    public void test_012_HTTPTrafficForApplicationCanBeBypassedInWiFiMode() throws Exception {
        String weatherPackage = "com.accuweather.android";
        MobileNetworkUtil mobileNetworkHelper = MobileNetworkUtil.init(getContext());
        assertEquals("Wi-Fi interface was not enabled", mobileNetworkHelper.onWifiOnly(), 1);
        assertTrue("Network is not accessible, after switching network interface", isNetworkAccessible());
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        NetlogTask netlogTask = new NetlogTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), policyAddedTask, netlogTask);
        TcpDumpUtil tcpDumpUtil = TcpDumpUtil.getInstance(getContext());
        String idProperty = null;
        List<Integer> uidList = null;
        try {
            assertTrue("Application " + weatherPackage + "does not installed, please install it and rerun test again",
                    isApplicationInstalled(getContext(), weatherPackage));
            logcatUtil.start();
            idProperty = PMSUtil.createPersonalScopeProperty(BY_PASS_LIST, INTERCEPTION_PATH, weatherPackage, true, true);
            logSleeping(oneMinute);
            logcatUtil.stop();
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, BY_PASS_LIST, BROWSER_PACKAGE));
            uidList = addNetworkRules(getContext(), ocPackageName, tfPackageName, weatherPackage);
            netlogTask.reset();
            tcpDumpUtil.start();
            logcatUtil.start();
            long startTime = System.currentTimeMillis();
            launchAnotherApplication(getContext(), weatherPackage);
            logSleeping(oneMinute);
            ShellUtil.killAll(weatherPackage);
            tcpDumpUtil.stop();
            logcatUtil.stop();
            long endTime = System.currentTimeMillis();
            assertFalse("Traffic from " + weatherPackage + " was found in logcat, but should not to be",
                    checkTrafficInLogCutByPackage(netlogTask.getLogEntries(), weatherPackage));
            int sessionCount = tcpDumpUtil.getHttpSessions(startTime, endTime).size();
            assertTrue("Traffic from " + weatherPackage + " was not found in tcpdump, sessionCount="
                    + sessionCount, sessionCount > 0);
        } finally {
            if (uidList != null) removeNetworkRules(uidList);
            if (idProperty != null) PMSUtil.deleteProperty(idProperty);
            tcpDumpUtil.stop();
            logcatUtil.stop();
        }
    }

    /**
     * HTTP traffic can be bypassed in 3G mode
     * <ol>
     * <li>Add com.accuweather.android to bypass list</li>
     * <li>Wi-Fi access is enabled</li>
     * <li>Process com.accuweather.android should not be optimized by OC</li>
     * <li> Transactions on process com.accuweather.android should be seen in tcpdump and should not be seen in logcat</li>
     * </ol>
     * <p/>
     * <p/>
     * TestLab test case name: BYPASS_06
     *
     * @throws Exception
     */
    public void test_013_HTTPTrafficForApplicationCanBeBypassedIn3GMode() throws Exception {
        String weatherPackage = "com.accuweather.android";
        MobileNetworkUtil mobileNetworkHelper = MobileNetworkUtil.init(getContext());
        assertEquals("3G interface was not enabled", mobileNetworkHelper.on3gOnly(), 1);
        assertTrue("Network is not accessible, after switching network interface", isNetworkAccessible());
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        NetlogTask netlogTask = new NetlogTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), policyAddedTask, netlogTask);
        TcpDumpUtil tcpDumpUtil = TcpDumpUtil.getInstance(getContext());
        String idProperty = null;
        List<Integer> uidList = null;
        try {
            assertTrue("Application " + weatherPackage + "does not installed, please install it and rerun test again",
                    isApplicationInstalled(getContext(), weatherPackage));
            logcatUtil.start();
            idProperty = PMSUtil.createPersonalScopeProperty(BY_PASS_LIST, INTERCEPTION_PATH, weatherPackage, true, true);
            logSleeping(oneMinute);
            logcatUtil.stop();
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, BY_PASS_LIST, BROWSER_PACKAGE));
            uidList = addNetworkRules(getContext(), ocPackageName, tfPackageName, weatherPackage);
            netlogTask.reset();
            tcpDumpUtil.start();
            logcatUtil.start();
            long startTime = System.currentTimeMillis();
            launchAnotherApplication(getContext(), weatherPackage);
            logSleeping(oneMinute);
            ShellUtil.killAll(weatherPackage);
            tcpDumpUtil.stop();
            logcatUtil.stop();
            long endTime = System.currentTimeMillis();
            assertFalse("Traffic from " + weatherPackage + " was found in logcat, but should not to be",
                    checkTrafficInLogCutByPackage(netlogTask.getLogEntries(), weatherPackage));
            int sessionCount = tcpDumpUtil.getHttpSessions(startTime, endTime).size();
            assertTrue("Traffic from " + weatherPackage + " was not found in tcpdump, sessionCount="
                    + sessionCount, sessionCount > 0);
        } finally {
            if (uidList != null) removeNetworkRules(uidList);
            if (idProperty != null) PMSUtil.deleteProperty(idProperty);
            tcpDumpUtil.stop();
            logcatUtil.stop();
        }
    }

    /**
     * Killing of OC process should not skip bypass list
     * <ol>
     * <li>Add com.android.browser to bypass list</li>
     * <li>Policy should be received and added</li>
     * <li>Wi-Fi access is enabled</li>
     * <li> In Browser go to ukr.net and google.com</li>
     * <li>OC should restart</li>
     * <li> In Browser go to ukr.net and google.com</li>
     * <li>Transactions with resources should be seen in tcpdump and should not be seen in logcat</li>
     * </ol>
     * <p/>
     * TestLab test case name: BYPASS_10
     *
     * @throws Exception
     */
    public void test_014_KillingOfOCProcessShouldNotSkipBypassList() throws Exception {
        MobileNetworkUtil mobileNetworkHelper = MobileNetworkUtil.init(getContext());
        assertEquals("Wi-Fi interface was not enabled", mobileNetworkHelper.onWifiOnly(), 1);
        assertTrue("Network is not accessible, after switching network interface", isNetworkAccessible());
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        NetlogTask netlogTask = new NetlogTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), policyAddedTask, netlogTask);
        String idProperty = null;
        String ukrNet = "www.ukr.net";
        String google = "google.com";
        TcpDumpUtil tcpDumpUtil = TcpDumpUtil.getInstance(getContext());
        try {
            logcatUtil.start();
            idProperty = PMSUtil.createPersonalScopeProperty(BY_PASS_LIST, INTERCEPTION_PATH, BROWSER_PACKAGE, true, true);
            logSleeping(oneMinute);
            logcatUtil.stop();
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, BY_PASS_LIST, BROWSER_PACKAGE));
            netlogTask.reset();
            tcpDumpUtil.start();
            logcatUtil.start();
            long startTime = System.currentTimeMillis();
            openBrowser(getContext(), google);
            openBrowser(getContext(), ukrNet);
            ShellUtil.killAll(ENGINE_PROCESS);
            logSleeping(oneMinute);
            openBrowser(getContext(), google);
            openBrowser(getContext(), ukrNet);
            logSleeping(oneMinute);
            ShellUtil.killAll(BROWSER_PACKAGE);
            tcpDumpUtil.stop();
            logcatUtil.stop();
            long endTime = System.currentTimeMillis();
            assertTrue("Traffic to " + ukrNet + " was not found in tcp dump",
                    checkTrafficInTcpDumpByHost(tcpDumpUtil, startTime, endTime, ukrNet, false));
            assertFalse("Traffic to " + ukrNet + " was found in logcat, but should not be",
                    checkTrafficInLogCutByHost(netlogTask.getLogEntries(), ukrNet, BROWSER_PACKAGE));
            assertTrue("Traffic to " + google + " was not found in tcp dump",
                    checkTrafficInTcpDumpByHost(tcpDumpUtil, startTime, endTime, ukrNet, false));
            assertFalse("Traffic to " + google + " was found in logcat, but should not be",
                    checkTrafficInLogCutByHost(netlogTask.getLogEntries(), ukrNet, BROWSER_PACKAGE));
        } finally {
            if (idProperty != null) PMSUtil.deleteProperty(idProperty);
            tcpDumpUtil.stop();
            logcatUtil.stop();
        }
    }

    /**
     * Changing of network should not skip bypass list
     * <ol>
     * <li>Add com.android.browser to bypass list</li>
     * <li>Policy should be received and added</li>
     * <li>Wi-Fi access is enabled</li>
     * <li> In Browser go to ukr.net and google.com</li>
     * <li>Switch from Wi-Fi to 3G</li>
     * <li> In Browser go to ukr.net and google.com</li>
     * <li>Transactions with resources should be seen in tcpdump and should not be seen in logcat</li>
     * </ol>
     * <p/>
     * TestLab test case name: BYPASS_11
     *
     * @throws Exception
     */
    public void test_015_ChanginqgOfNetworkShouldNotSkipBypassList() throws Exception {
        MobileNetworkUtil mobileNetworkHelper = MobileNetworkUtil.init(getContext());
        assertEquals("Wi-Fi interface was not enabled", mobileNetworkHelper.onWifiOnly(), 1);
        assertTrue("Network is not accessible, after switching network interface", isNetworkAccessible());
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        NetlogTask netlogTask = new NetlogTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), policyAddedTask, netlogTask);
        String idProperty = null;
        String ukrNet = "www.ukr.net";
        String google = "google.com";
        TcpDumpUtil tcpDumpUtil = TcpDumpUtil.getInstance(getContext());
        try {
            logcatUtil.start();
            idProperty = PMSUtil.createPersonalScopeProperty(BY_PASS_LIST, INTERCEPTION_PATH, BROWSER_PACKAGE, true, true);
            logSleeping(oneMinute);
            logcatUtil.stop();
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, BY_PASS_LIST, BROWSER_PACKAGE));
            netlogTask.reset();
            tcpDumpUtil.start();
            logcatUtil.start();
            long startTime = System.currentTimeMillis();
            openBrowser(getContext(), google);
            openBrowser(getContext(), ukrNet);
            assertEquals("3G interface was not enabled", mobileNetworkHelper.on3gOnly(), 1);
            assertTrue("Network is not accessible, after switching network interface", isNetworkAccessible());
            openBrowser(getContext(), google);
            openBrowser(getContext(), ukrNet);
            logSleeping(oneMinute);
            ShellUtil.killAll(BROWSER_PACKAGE);
            tcpDumpUtil.stop();
            logcatUtil.stop();
            long endTime = System.currentTimeMillis();
            assertTrue("Traffic to " + ukrNet + " was not found in tcp dump",
                    checkTrafficInTcpDumpByHost(tcpDumpUtil, startTime, endTime, ukrNet, false));
            assertFalse("Traffic to " + ukrNet + " was found in logcat, but should not be",
                    checkTrafficInLogCutByHost(netlogTask.getLogEntries(), ukrNet, BROWSER_PACKAGE));
            assertTrue("Traffic to " + google + " was not found in tcp dump",
                    checkTrafficInTcpDumpByHost(tcpDumpUtil, startTime, endTime, ukrNet, false));
            assertFalse("Traffic to " + google + " was found in logcat, but should not be",
                    checkTrafficInLogCutByHost(netlogTask.getLogEntries(), ukrNet, BROWSER_PACKAGE));
        } finally {
            if (idProperty != null) PMSUtil.deleteProperty(idProperty);
            tcpDumpUtil.stop();
            logcatUtil.stop();
        }
    }

    /**
     * Regression: blacklisted HTTP traffic should not be optimized
     * <ol>
     * <li>Add "com.accuweather.android" to white list in http branch of PMS</li>
     * <li>Be sure that policies arrive and they are applied in logcat</li>
     * <li> Use AccuWeather for some period of time</li>
     * <li>Traffic for www.accuweather.com should go in stream in logcat</li>
     * </ol>
     * <p/>
     * TestLab test case name: DISPCONFFILE_25
     *
     * @throws InterruptedException
     */
    public void test_016_blacklistedHttpTrafficShouldNotBeOptimized() throws InterruptedException {
        String weatherHost = "www.accuweather.com";
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        NetlogTask netlogTask = new NetlogTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), policyAddedTask, netlogTask);
        String idProperty = null;
        try {
            logcatUtil.start();
            idProperty = PMSUtil.createPersonalScopeProperty(WHITE_LIST, ASIMOV_HTTP_PATH, weatherHost, true, true);
            logSleeping(oneMinute);
            logcatUtil.stop();
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, WHITE_LIST, weatherHost));
            netlogTask.reset();
            logcatUtil.start();
            openBrowser(getContext(), weatherHost);
            openBrowser(getContext(), "www.accuweather.com/en/us/united-states-weather");
            logSleeping(oneMinute);
            ShellUtil.killAll(BROWSER_PACKAGE);
            logcatUtil.stop();
            boolean isBlacklisted = false;
            for (NetlogEntry entry : netlogTask.getLogEntries()) {
                if (entry.getHost().equals(weatherHost) && entry.getOpType() == OperationType.proxy_blacklisted) {
                    isBlacklisted = true;
                }
            }
            assertTrue("Traffic for www.accuweather.com should go in stream in logcat", isBlacklisted);
        } finally {
            if (idProperty != null) PMSUtil.deleteProperty(idProperty);
            logcatUtil.stop();
        }
    }

    /**
     * Send kill signal to Engine when OC client is in Failover
     * <ol>
     * <li>At the beginning of test 3g interface should be connected</li>
     * <li>Set such policy: wifi/roaming failover  failovers@roaming_wifi@enabled=1  failovers@roaming_wifi@actions=1</li>
     * <li>Policy should be received and added</li>
     * <li>Switch on wifi interface</li>
     * <li>Wifi/Roaming failover should start due to Relay is unavailable. </li>
     * <li>Dispatcher should change state from RUNNING to PENDING_SHUTDOWN, HUB signal should be sent to child process (identified by PID)</li>
     * <li>After shu_message state should be STOPPED, deconfigure iptables and reinitialize state machine should be done</li>
     * <li>Send kill signal to Engine</li>
     * <li>After killing Engine Dispatcher should change it state again to INITIALIZED</li>
     * <li> Wifi/Roaming failover should start due to Relay is unavailable. Dispatcher should change state from RUNNING to STOPPED</li>
     * </ol>
     * <p/>
     * TestLab test case name: CONF_DISP_008
     *
     * @throws Exception
     */
    public void test_017_sendKillSignalToEngineWhenOCisInFailover() throws Exception {
        MobileNetworkUtil mobileNetworkHelper = MobileNetworkUtil.init(getContext());
        assertEquals("3G interface was not enabled", mobileNetworkHelper.on3gOnly(), 1);
        assertTrue("Network is not accessible, after switching network interface", isNetworkAccessible());
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        FailoverStartTask failoverStartTask = new FailoverStartTask();
        FailoverStopTask failoverStopTask = new FailoverStopTask();
        DispatcherStateTask dispatcherStateTask = new DispatcherStateTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), policyAddedTask, failoverStartTask, failoverStopTask);
        String idEnabled = null;
        String idActions = null;
        String value = "1";
        SysLogUtil sysLogUtil = new SysLogUtil(dispatcherStateTask);
        try {
            logcatUtil.start();
            idEnabled = PMSUtil.createPersonalScopeProperty(ENABLED, ROAMING_FAILOVER_PATH, value, true);
            idActions = PMSUtil.createPersonalScopeProperty(ACTIONS, ROAMING_FAILOVER_PATH, value, true);
            logSleeping(oneMinute);
            long startTime = System.currentTimeMillis();
            IpTablesUtil.banRelayServer(true);
            assertEquals("WiFi interface was not enabled", mobileNetworkHelper.onWifiOnly(), 1);
            assertTrue("Network is not accessible, after switching network interface", isNetworkAccessible());
            logSleeping(oneMinute);
            logcatUtil.stop();
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, ENABLED, value));
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, ACTIONS, value));
            assertFalse("Failover must be active at this moment", failoverStartTask.getLogEntries().isEmpty());
            logcatUtil.start();
            ShellUtil.killAll(ENGINE_PROCESS, 9);
            logSleeping(oneMinute);
            logcatUtil.stop();
            assertFalse("Failover must be active at this moment", failoverStartTask.getLogEntries().isEmpty());
            long endTime = System.currentTimeMillis();
            sysLogUtil.parseLog();
            boolean result = checkChangedDispatcherState(dispatcherStateTask.getLogEntries(), startTime, endTime,
                    DispatcherState.RUNNING, DispatcherState.PENDING_SHUTDOWN, DispatcherState.STOPPED);
            assertTrue("Dispatcher did not change it state as expected ", result);
            IpTablesUtil.banRelayServer(false);
            PMSUtil.deleteProperty(idEnabled);
            idEnabled = null;
            PMSUtil.deleteProperty(idActions);
            idActions = null;
            mobileNetworkHelper.on3gOnly();
        } finally {
            if (idEnabled != null) PMSUtil.deleteProperty(idEnabled);
            if (idActions != null) PMSUtil.deleteProperty(idActions);
            logcatUtil.stop();
        }

    }

    /**
     * Send kill signal to Controller when OC client is in Failover
     * <ol>
     * <li>At the beginning of test 3g interface should be connected</li>
     * <li>Set such policy: wifi/roaming failover   failovers@roaming_wifi@enabled=1  failovers@roaming_wifi@actions=1 </li>
     * <li>Policy should be received and added</li>
     * <li><Switch on wifi interface/li>
     * <li>Relay should be unavailable</li>
     * <li>Wifi/Roaming failover should start due to Relay is unavailable</li>
     * <li>Dispatcher should change state from RUNNING to PENDING_SHUTDOWN, HUB signal should be sent to child process (identified by PID)</li>
     * <li>After shu_message state should be STOPPED, deconfigure iptables and reinitialize state machine should be done</li>
     * <li>Send kill signal to Controller</li>
     * <li>After killing of Controller Dispatcher should not change it state</li>
     * </ol>
     * <p/>
     * TestLab test case name: CONF_DISP_009
     *
     * @throws Exception
     */
    public void test_018_sendKillSignalToControllerWhenOCisInFailover() throws Exception {
        MobileNetworkUtil mobileNetworkHelper = MobileNetworkUtil.init(getContext());
        assertEquals("3G interface was not enabled", mobileNetworkHelper.on3gOnly(), 1);
        assertTrue("Network is not accessible, after switching network interface", isNetworkAccessible());
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        FailoverStartTask failoverStartTask = new FailoverStartTask();
        FailoverStopTask failoverStopTask = new FailoverStopTask();
        DispatcherStateTask dispatcherStateTask = new DispatcherStateTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), policyAddedTask, failoverStartTask, failoverStopTask);
        SysLogUtil sysLogUtil = new SysLogUtil(dispatcherStateTask);
        String idEnabled = null;
        String idActions = null;
        String value = "1";
        try {
            logcatUtil.start();
            idEnabled = PMSUtil.createPersonalScopeProperty(ENABLED, ROAMING_FAILOVER_PATH, value, true);
            idActions = PMSUtil.createPersonalScopeProperty(ACTIONS, ROAMING_FAILOVER_PATH, value, true);
            logSleeping(oneMinute);
            long startTime = System.currentTimeMillis();
            IpTablesUtil.banRelayServer(true);
            assertEquals("WiFi interface was not enabled", mobileNetworkHelper.onWifiOnly(), 1);
            assertTrue("Network is not accessible, after switching network interface", isNetworkAccessible());
            logSleeping(oneMinute);
            logcatUtil.stop();
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, ENABLED, value));
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, ACTIONS, value));
            assertFalse("Failover must be active at this moment", failoverStartTask.getLogEntries().isEmpty());
            ShellUtil.killAll(CONTROLLER_PROCESS, 9);
            logSleeping(oneMinute);
            long endTime = System.currentTimeMillis();
            sysLogUtil.parseLog();
            boolean result = checkDispatcherNotChangeItState(dispatcherStateTask.getLogEntries(), startTime, endTime,
                    DispatcherState.RUNNING, DispatcherState.PENDING_SHUTDOWN, DispatcherState.STOPPED);
            assertTrue("Dispatcher did not change it state as expected ", result);
            IpTablesUtil.banRelayServer(false);
            PMSUtil.deleteProperty(idEnabled);
            idEnabled = null;
            PMSUtil.deleteProperty(idActions);
            idActions = null;
            mobileNetworkHelper.on3gOnly();
        } finally {
            if (idEnabled != null) PMSUtil.deleteProperty(idEnabled);
            if (idActions != null) PMSUtil.deleteProperty(idActions);
            logcatUtil.stop();
        }
    }

    /**
     * Kill Engine in case stop of Failover
     * <ol>
     * <li>At the beginning of test 3g interface should be connected</li>
     * <li>Set such policy: wifi/roaming failover failovers@roaming_wifi@enabled=1 failovers@roaming_wifi@actions=1 </li>
     * <li>Policy should be received and added</li>
     * <li>Switch on wifi interface</li>
     * <li> Wifi/Roaming failover should start due to Relay is unavailable</li>
     * <li>Dispatecher should change state from RUNNING to PENDING_SHUTDOWN, HUB signal should be sent to child process (identified by PID)</li>
     * <li>After shu_message state should be STOPPED, deconfigure iptables and reinitialize state machine should be done</li>
     * <li>After killing Engine, Dispatcher should change it state to INITIALIZED due to Wifi/Roaming failover is stopped</li>
     * </ol>
     * <p/>
     * <p/>
     * TestLab test case name: CONF_DISP_010
     *
     * @throws Exception
     */
    public void test_019_KillEngineInCaseStopOfFailover() throws Exception {
        MobileNetworkUtil mobileNetworkHelper = MobileNetworkUtil.init(getContext());
        assertEquals("3G interface was not enabled", mobileNetworkHelper.on3gOnly(), 1);
        assertTrue("Network is not accessible, after switching network interface", isNetworkAccessible());
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        FailoverStartTask failoverStartTask = new FailoverStartTask();
        FailoverStopTask failoverStopTask = new FailoverStopTask();
        DispatcherStateTask dispatcherStateTask = new DispatcherStateTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), policyAddedTask, failoverStartTask, failoverStopTask);
        String idEnabled = null;
        String idActions = null;
        String value = "1";
        try {
            logcatUtil.start();
            idEnabled = PMSUtil.createPersonalScopeProperty(ENABLED, ROAMING_FAILOVER_PATH, value, true);
            idActions = PMSUtil.createPersonalScopeProperty(ACTIONS, ROAMING_FAILOVER_PATH, value, true);
            logSleeping(oneMinute);
            long startTime = System.currentTimeMillis();
            IpTablesUtil.banRelayServer(true);
            assertEquals("WiFi interface was not enabled", mobileNetworkHelper.onWifiOnly(), 1);
            assertTrue("Network is not accessible, after switching network interface", isNetworkAccessible());
            logSleeping(twoMinutes);
            logcatUtil.stop();
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, ENABLED, value));
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, ACTIONS, value));
            assertFalse("Failover must be active at this moment", failoverStartTask.getLogEntries().isEmpty());
            IpTablesUtil.banRelayServer(false);
            ShellUtil.killAll(ENGINE_PROCESS, 9);
            logSleeping(oneMinute);
            long endTime = System.currentTimeMillis();
            SysLogUtil sysLogUtil = new SysLogUtil(dispatcherStateTask);
            sysLogUtil.parseLog();
            boolean result = checkChangedDispatcherState(dispatcherStateTask.getLogEntries(), startTime, endTime,
                    DispatcherState.RUNNING, DispatcherState.PENDING_SHUTDOWN, DispatcherState.STOPPED, DispatcherState.INITIALIZED);
            assertTrue("Dispatcher did not change it state as expected ", result);
        } finally {
            if (idEnabled != null) PMSUtil.deleteProperty(idEnabled);
            if (idActions != null) PMSUtil.deleteProperty(idActions);
            logcatUtil.stop();
        }
    }

    /**
     * Kill Controller in case stop of Failover
     * <ol>
     * <li>At the beginning of test 3g interface should be connected</li>
     * <li>Set such policy: failovers@roaming_wifi@enabled=1 and  failovers@roaming_wifi@actions=1</li>
     * <li>Policy should be received and added</li>
     * <li>Relay ought to be unavailable</li>
     * <li>Switch on wifi interface</li>
     * <li>Wifi/Roaming failover shoul start</li>
     * <li>Dispatecher should change state from RUNNING to PENDING_SHUTDOWN, HUB signal should be sent to child process (identified by PID)</li>
     * <li>After shu_message state should be STOPPED, deconfigure iptables and reinitialize state machine should be done</li>
     * <li>After killing of Controller, Dispatcher should change it state to INITIALIZED due to Wifi/Roaming failover is stopped</li>
     * </ol>
     * <p/>
     * TestLab test case name: CONF_DISP_011
     *
     * @throws Exception
     */
    public void test_020_KillControllerInCaseStopOfFailover() throws Exception {
        MobileNetworkUtil mobileNetworkHelper = MobileNetworkUtil.init(getContext());
        assertEquals("3G interface was not enabled", mobileNetworkHelper.on3gOnly(), 1);
        assertTrue("Network is not accessible, after switching network interface", isNetworkAccessible());
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        FailoverStartTask failoverStartTask = new FailoverStartTask();
        FailoverStopTask failoverStopTask = new FailoverStopTask();
        DispatcherStateTask dispatcherStateTask = new DispatcherStateTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), policyAddedTask, failoverStartTask, failoverStopTask);
        String idEnabled = null;
        String idActions = null;
        String value = "1";
        try {
            logcatUtil.start();
            idEnabled = PMSUtil.createPersonalScopeProperty(ENABLED, ROAMING_FAILOVER_PATH, value, true);
            idActions = PMSUtil.createPersonalScopeProperty(ACTIONS, ROAMING_FAILOVER_PATH, value, true);
            logSleeping(oneMinute);
            long startTime = System.currentTimeMillis();
            IpTablesUtil.banRelayServer(true);
            assertEquals("WiFi interface was not enabled", mobileNetworkHelper.onWifiOnly(), 1);
            assertTrue("Network is not accessible, after switching network interface", isNetworkAccessible());
            logSleeping(oneMinute);
            logcatUtil.stop();
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, ENABLED, value));
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, ACTIONS, value));
            assertFalse("Failover must be active at this moment", failoverStartTask.getLogEntries().isEmpty());
            IpTablesUtil.banRelayServer(false);
            ShellUtil.killAll(CONTROLLER_PROCESS, 9);
            logSleeping(oneMinute);
            long endTime = System.currentTimeMillis();
            SysLogUtil sysLogUtil = new SysLogUtil(dispatcherStateTask);
            sysLogUtil.parseLog();
            boolean result = checkChangedDispatcherState(dispatcherStateTask.getLogEntries(), startTime, endTime,
                    DispatcherState.RUNNING, DispatcherState.PENDING_SHUTDOWN, DispatcherState.STOPPED, DispatcherState.INITIALIZED);
            assertTrue("Dispatcher did not change it state as expected ", result);
        } finally {
            if (idEnabled != null) PMSUtil.deleteProperty(idEnabled);
            if (idActions != null) PMSUtil.deleteProperty(idActions);
            logcatUtil.stop();
        }
    }

    /**
     * After receiving policy optimization=1 when OC client is in Failover, Dispatcher shouldnt change it state.
     * <ol>
     * <li>Set such policy: asimov@enabled=0</li>
     * <li>Policy should be received and added</li>
     * <li>Dispatecher should change state from RUNNING to PENDING_SHUTDOWN,
     * HUB signal should be sent to child process (identified by PID)</li>
     * <li>After shu_message state should be STOPPED, deconfigure iptables and reinitialize state machine should be done</li>
     * <li>Set such policy: asimov@optimization=1</li>
     * <li>After receiving policy dispatcher shouldnt change it state</li>
     * </ol>
     * <p/>
     * TestLab test case name: CONF_DISP_012
     *
     * @throws InterruptedException
     */
    public void test_021_afterReceivingPolicyOptDispatcherNotChangeItState() throws InterruptedException {
        String idEnable = null;
        String idOptimization = null;
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), policyAddedTask);
        DispatcherStateTask dispatcherStateTask = new DispatcherStateTask();
        SysLogUtil sysLogUtil = new SysLogUtil(dispatcherStateTask);
        try {
            logcatUtil.start();
            long startTime = System.currentTimeMillis();
            idEnable = PMSUtil.createPersonalScopeProperty(ENABLED, ASIMOV_PATH, "0", true, true);
            logSleeping(oneMinute);
            logcatUtil.stop();
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, ENABLED, "0"));
            logcatUtil.start();
            idOptimization = PMSUtil.createPersonalScopeProperty("optimization", ASIMOV_PATH, "1", true);
            logSleeping(oneMinute);
            logcatUtil.stop();
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, "optimization", "1"));
            long endTime = System.currentTimeMillis();
            sysLogUtil.parseLog();
            assertTrue("Dispatcher did not change it states as expected", checkDispatcherNotChangeItState(
                    dispatcherStateTask.getLogEntries(), startTime, endTime, DispatcherState.RUNNING,
                    DispatcherState.PENDING_SHUTDOWN, DispatcherState.STOPPED));
        } finally {
            if (idEnable != null) PMSUtil.deleteProperty(idEnable);
            if (idOptimization != null) PMSUtil.deleteProperty(idOptimization);
            logcatUtil.stop();
        }
    }

    /**
     * Interception ports for TCP dispatcher can be configured in config file
     * <ol>
     * <li>Create namespace “tcp” and add properties  “interception ports” with value 1:65535,
     * “type” with value 1 in interception branch of PMS</li>
     * <li>Be sure that policies arrive and they are applied in logcat</li>
     * <li>UDP traffic should not be seen in logcat</li>
     * <li>TCP traffic should be seen in logcat</li>
     * </ol>
     * <p/>
     * TestLab test case name: DISPCONFFILE_10
     *
     * @throws Exception
     */
    public void test_022_InterceptionPortsForTcpDispatcher() throws Exception {
        List<String> idTcpList = null;
        String dispatcher = "tcp";
        String ports = "1:65535";
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        CLQConstructedTask clqConstructedTask = new CLQConstructedTask();
        CLQConstructedDnsTask clqConstructedDnsTask = new CLQConstructedDnsTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), policyAddedTask, clqConstructedDnsTask, clqConstructedTask);
        String uri = createTestResourceUri("test_023_InterceptionPortsForTcpDispatcher");
        HttpRequest request = createRequest().setUri(uri).addHeaderField("X-OC-Encoding", "identity")
                .addHeaderField("Cache-Control", "no-cache, no-store").getRequest();
        try {
            PrepareResourceUtil.prepareResource(uri, false);
            logcatUtil.start();
            idTcpList = addPersonalInterceptionProperty(dispatcher, ports, "1", "0");
            logSleeping(oneMinute);
            logcatUtil.stop();
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, INTERCEPTION_PORTS,
                    ports) && checkPolicyReceived(policyAddedTask, Z_ORDER, "0")
                    && checkPolicyReceived(policyAddedTask, TYPE, "1"));
            clqConstructedTask.reset();
            clqConstructedDnsTask.reset();
            logcatUtil.start();
            HttpResponse response;
            for (int i = 0; i < 5; i++) {
                response = sendRequest(request);
                logSleeping(MIN_NON_RMP_PERIOD - response.getDuration());
            }
            logcatUtil.stop();
            assertTrue("Transactions should be processed by \"" + dispatcher + "\"  + dispatcher",
                    checkTcpTrafficProcessedBySpecifiedDispatcher(dispatcher, clqConstructedTask));
            assertFalse("Transactions should not be processed by \"" + dispatcher + "\"  + dispatcher",
                    checkUdpTrafficProcessedBySpecifiedDispatcher(dispatcher, clqConstructedDnsTask));
        } finally {
            PrepareResourceUtil.prepareResource(uri, true);
            removePersonalInterceptionProperties(idTcpList);
            logcatUtil.stop();
        }
    }

    /**
     * Interception ports for UDP dispatcher can be configured in config file
     * <ol>
     * <li>Create namespace “udp” and add properties  “interception ports” with value 1:65535,
     * “type” with value 2 in interception branch of PMS</li>
     * <li>Be sure that policies arrive and they are applied in logcat</li>
     * <li>TCP traffic should not be seen in logcat</li>
     * <li>UDP traffic for port 53 should be optimized in logcat</li>
     * </ol>
     * <p/>
     * TestLab test case name: DISPCONFFILE_11
     *
     * @throws Exception
     */
    public void test_023_InterceptionPortsForUdpDispatcher() throws Exception {
        List<String> idUdpList = null;
        String dispatcher = "udp";
        String ports = "1:65535";
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        CLQConstructedTask clqConstructedTask = new CLQConstructedTask();
        CLQConstructedDnsTask clqConstructedDnsTask = new CLQConstructedDnsTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), policyAddedTask, clqConstructedDnsTask, clqConstructedTask);
        String uri = createTestResourceUri("test_024_InterceptionPortsForUdpDispatcher");
        HttpRequest request = createRequest().setUri(uri).addHeaderField("X-OC-Encoding", "identity")
                .addHeaderField("Cache-Control", "no-cache, no-store").getRequest();
        try {
            PrepareResourceUtil.prepareResource(uri, false);
            logcatUtil.start();
            idUdpList = addPersonalInterceptionProperty(dispatcher, ports, "2", "0");
            logSleeping(oneMinute);
            logcatUtil.stop();
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, INTERCEPTION_PORTS,
                    ports) && checkPolicyReceived(policyAddedTask, Z_ORDER, "0")
                    && checkPolicyReceived(policyAddedTask, TYPE, "2"));
            clqConstructedTask.reset();
            clqConstructedDnsTask.reset();
            logcatUtil.start();
            HttpResponse response;
            for (int i = 0; i < 5; i++) {
                response = sendRequest(request);
                logSleeping(MIN_NON_RMP_PERIOD - response.getDuration());
            }
            logcatUtil.stop();
            assertTrue("Transactions should be processed by \"" + dispatcher + "\"  + dispatcher",
                    checkUdpTrafficProcessedBySpecifiedDispatcher(dispatcher, clqConstructedDnsTask));
            assertFalse("Transactions should not be processed by \"" + dispatcher + "\"  + dispatcher",
                    checkTcpTrafficProcessedBySpecifiedDispatcher(dispatcher, clqConstructedTask));
        } finally {
            PrepareResourceUtil.prepareResource(uri, true);
            removePersonalInterceptionProperties(idUdpList);
            logcatUtil.stop();
        }
    }

    /**
     * UDP traffic should be optimized by udp dispatcher and TCP traffic should be optimized by tcp dispatcher,
     * when they have the same priority and  listen to all ports
     * <ol>
     * <li>Create namespace “tcp” and add properties  “interception ports” with value 1:65535,
     * “type” with value 1 in interception branch of PMS</li>
     * <li>Create namespace “udp” and add properties  “interception ports” with value 1:65535,
     * “type” with value 2 in interception branch of PMS</li>
     * <li>Be sure that policies arrive and they are applied in logcat</li>
     * <li>Traffic for port 53 should be optimized by udp dispatcher in logcat</li>
     * <li>Traffic for port 80 should be optimized by tcp dispatcher in logcat</li>
     * </ol>
     * <p/>
     * TestLab test case name: DISPCONFFILE_12
     *
     * @throws Exception
     */
    public void test_024_UdbAndTcpTrafficShouldBeOptimizedByUdpAndTcpAccordingly() throws Exception {
        List<String> idTcpList = null;
        List<String> idUdpList = null;
        String tcpDispatcher = "tcp";
        String udpDispatcher = "udp";
        String ports = "1:65535";
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        CLQConstructedTask clqConstructedTask = new CLQConstructedTask();
        CLQConstructedDnsTask clqConstructedDnsTask = new CLQConstructedDnsTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), policyAddedTask, clqConstructedDnsTask, clqConstructedTask);
        String uri = createTestResourceUri("test_025_UdbAndTcpTrafficShouldBeOptimizedByUdpAndTcpAccordingly");
        HttpRequest request = createRequest().setUri(uri).addHeaderField("X-OC-Encoding", "identity")
                .addHeaderField("Cache-Control", "no-cache, no-store").getRequest();
        try {
            PrepareResourceUtil.prepareResource(uri, false);
            logcatUtil.start();
            idTcpList = addPersonalInterceptionProperty(tcpDispatcher, ports, "1", "0");
            idUdpList = addPersonalInterceptionProperty(udpDispatcher, ports, "2", "0");
            logSleeping(oneMinute);
            logcatUtil.stop();
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, INTERCEPTION_PORTS,
                    ports) && checkPolicyReceived(policyAddedTask, Z_ORDER, "0")
                    && checkPolicyReceived(policyAddedTask, TYPE, "2") && checkPolicyReceived(policyAddedTask, TYPE, "1"));
            clqConstructedTask.reset();
            clqConstructedDnsTask.reset();
            logcatUtil.start();
            HttpResponse response;
            for (int i = 0; i < 5; i++) {
                response = sendRequest(request);
                logSleeping(MIN_NON_RMP_PERIOD - response.getDuration());
            }
            logcatUtil.stop();
            assertTrue("Transactions should be processed by \"" + udpDispatcher + "\"  + dispatcher",
                    checkUdpTrafficProcessedBySpecifiedDispatcher(udpDispatcher, clqConstructedDnsTask));
            assertFalse("Transactions should not be processed by \"" + udpDispatcher + "\"  + dispatcher",
                    checkTcpTrafficProcessedBySpecifiedDispatcher(udpDispatcher, clqConstructedTask));
            assertTrue("Transactions should be processed by \"" + tcpDispatcher + "\"  + dispatcher",
                    checkTcpTrafficProcessedBySpecifiedDispatcher(tcpDispatcher, clqConstructedTask));
            assertFalse("Transactions should not be processed by \"" + tcpDispatcher + "\"  + dispatcher",
                    checkUdpTrafficProcessedBySpecifiedDispatcher(tcpDispatcher, clqConstructedDnsTask));
        } finally {
            PrepareResourceUtil.prepareResource(uri, true);
            removePersonalInterceptionProperties(idTcpList);
            removePersonalInterceptionProperties(idUdpList);
            logcatUtil.stop();
        }
    }

    /**
     * Second dispatcher should start to proceed all transactions in case of killing the first one if they are the same one
     * <ol>
     * <li>Set such policy to configure two new dispatcher with the same configuration but different names (A,B)</li>
     * <li>Policy should be received and added</li>
     * <li>Two dispatchers should start up and set in RUNNING state</li>
     * <li>Verify in logcat that all transactions are processes in first priority dispatcher</li>
     * <li>Send kill -9 signal to A dispatcher</li>
     * <li>After kill in logcat second dispatcher should start to proceed all transactions</li>
     * </ol>
     * <p/>
     * TestLab test case name: CONF_DISP_016
     *
     * @throws Exception
     */
    public void test_025_secondDispatcherShouldStartToProceedAllTransactions() throws Exception {
        List<String> idAHttpList = null;
        List<String> idBHttpList = null;
        String dispatcherA = "ahttp";
        String dispatcherB = "bhttp";
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        CLQConstructedTask clqConstructedTask = new CLQConstructedTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), policyAddedTask);
        String uri = createTestResourceUri("test_026_secondDispatcherShouldStartToProceedAllTransactions");
        HttpRequest request = createRequest().setUri(uri).addHeaderField("X-OC-Encoding", "identity")
                .addHeaderField("Cache-Control", "no-cache, no-store").getRequest();
        try {
            PrepareResourceUtil.prepareResource(uri, false);
            logcatUtil.start();
            idAHttpList = addPersonalInterceptionProperty(dispatcherA, "80", "1", "0");
            idBHttpList = addPersonalInterceptionProperty(dispatcherB, "80", "1", "50");
            logSleeping(oneMinute);
            logcatUtil.stop();
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, INTERCEPTION_PORTS, "80") &&
                    checkPolicyReceived(policyAddedTask, Z_ORDER, "0") && checkPolicyReceived(policyAddedTask, TYPE, "1"));
            logcatUtil = new LogcatUtil(getContext(), clqConstructedTask);
            logcatUtil.start();
            sendRequest(request);
            logSleeping(oneMinute);
            logcatUtil.stop();
            assertTrue("Transactions should be processed by \"" + dispatcherA + "\" dispatcher",
                    checkTcpTrafficProcessedBySpecifiedDispatcher(dispatcherA, clqConstructedTask));
            clqConstructedTask.reset();
            logcatUtil.start();
            ShellUtil.killAll(dispatcherA, 9);
            sendRequest(request);
            logSleeping(oneMinute);
            logcatUtil.stop();
            assertTrue("Transactions should be processed by \"" + dispatcherB + "\" dispatcher",
                    checkTcpTrafficProcessedBySpecifiedDispatcher(dispatcherB, clqConstructedTask));
        } finally {
            PrepareResourceUtil.prepareResource(uri, true);
            removePersonalInterceptionProperties(idAHttpList);
            removePersonalInterceptionProperties(idBHttpList);
            logcatUtil.stop();
        }
    }

    /**
     * Reconfiguration from two to one dispatcher
     * <ol>
     * <li>Set such policy to configure two new dispatcher (AB) with the same configuration but different names and different priority</li>
     * <li>Policy should be received and added.</li>
     * <li>Two dispatchers should start up and set in RUNNING state</li>
     * <li>Verify in logcat that all transactions are processes in first priority dispatcher</li>
     * <li>Set such policy to configure one new dispatcher (A)</li>
     * <li>Policy should be received</li>
     * <li>B dispatcher should Dispatecher should change state from RUNNING to PENDING_SHUTDOWN,
     * HUB signal should be sent to child process (identified by PID)</li>
     * <li>After shu_message state should be STOPPED and reinitialize state machine should be done</li>
     * </ol>
     * <p/>
     * TestLab test case name: CONF_DISP_017
     *
     * @throws Exception
     */
    public void test_026_ReconfigurationFromTwoToOneDispatcher() throws Exception {
        List<String> idAHttpList = null;
        List<String> idBHttpList = null;
        String dispatcherA = "ahttp";
        String dispatcherB = "bhttp";
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        CLQConstructedTask clqConstructedTask = new CLQConstructedTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), policyAddedTask);
        DispatcherStateTask dispatcherStateTask = new DispatcherStateTask();
        SysLogUtil sysLogUtil = new SysLogUtil(dispatcherStateTask);
        String uri = createTestResourceUri("test_026_secondDispatcherShouldStartToProceedAllTransactions");
        HttpRequest request = createRequest().setUri(uri).addHeaderField("X-OC-Encoding", "identity")
                .addHeaderField("Cache-Control", "no-cache, no-store").getRequest();
        try {
            PrepareResourceUtil.prepareResource(uri, false);
            logcatUtil.start();
            idAHttpList = addPersonalInterceptionProperty(dispatcherA, "80", "1", "0");
            idBHttpList = addPersonalInterceptionProperty(dispatcherB, "80", "1", "50");
            logSleeping(oneMinute);
            logcatUtil.stop();
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, INTERCEPTION_PORTS, "80") &&
                    checkPolicyReceived(policyAddedTask, Z_ORDER, "0") && checkPolicyReceived(policyAddedTask, TYPE, "1"));
            logcatUtil = new LogcatUtil(getContext(), clqConstructedTask);
            logcatUtil.start();
            sendRequest(request);
            logSleeping(oneMinute);
            logcatUtil.stop();
            assertTrue("Transactions should be processed by \"" + dispatcherA + "\" dispatcher",
                    checkTcpTrafficProcessedBySpecifiedDispatcher(dispatcherA, clqConstructedTask));
            clqConstructedTask.reset();
            long startTime = System.currentTimeMillis();
            removePersonalInterceptionProperties(idBHttpList);
            idBHttpList = null;
            logSleeping(oneMinute);
            long endTime = System.currentTimeMillis();
            sysLogUtil.parseLog();
            assertTrue("Dispatcher did not change it states as expected", checkChangedDispatcherState(
                    dispatcherStateTask.getLogEntries(), dispatcherB, startTime, endTime, DispatcherState.RUNNING,
                    DispatcherState.PENDING_SHUTDOWN, DispatcherState.STOPPED));
        } finally {
            PrepareResourceUtil.prepareResource(uri, true);
            removePersonalInterceptionProperties(idAHttpList);
            removePersonalInterceptionProperties(idBHttpList);
            logcatUtil.stop();
        }
    }

    /**
     * After switching transparent mode on dispatchers should be stopped in case mobile failover doesnt start
     * <ol>
     * <li>Set policy socket = 10 hours</li>
     * <li> Set policy for mobile failover:
     * <ul>
     * <li>asimov@failovers@mobile@enabled=1 </li>
     * <li>asimov@failovers@mobile@actions=2 </li>
     * <li>asimov@failovers@mobile@attempt_interval=6 </li>
     * <li>asimov@failovers@mobile@retries=5 </li>
     * <li>asimov@failovers@mobile@timeout=2 </li>
     * </ul>
     * </li>
     * <li>Policy should be received and added.</li>
     * <li>Relay should be unavailable</li>
     * <li>Mobile failover should be started in case Relay is unavailable</li>
     * <li>Set policy asimov@transparent=1</li>
     * <li>At last retry Relay should become available. And new policy should be received</li>
     * <li>Dispatecher should change state from RUNNING to PENDING_SHUTDOWN, HUB signal should be sent to child process (identified by PID)</li>
     * <li>After shu_message state should be STOPPED, deconfigure iptables and reinitialize state machine should be done due to received policy for transparent mode</li>
     * </ol>
     * <p/>
     * <p/>
     * TestLab test case name: CONF_DISP_018
     *
     * @throws Exception
     */
    public void test_027_AfterSwitchingTransparentModeOnDispatcherStopped() throws Exception {
        List<String> idList = new ArrayList<String>();
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        FailoverStartTask failoverStartTask = new FailoverStartTask();
        DispatcherStateTask dispatcherStateTask = new DispatcherStateTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), policyAddedTask, failoverStartTask);
        String uri = createTestResourceUri("test_028_AfterSwitchingTransparentModeOnDispatcherStopped");
        HttpRequest request = createRequest().setUri(uri).addHeaderField("X-OC-Encoding", "identity").getRequest();
        try {
            PrepareResourceUtil.prepareResource(uri, false);
            logcatUtil.start();
            idList.add(PMSUtil.createPersonalScopeProperty(ENABLED, MOBILE_FAILOVER_PATH, "1", true, false));
            idList.add(PMSUtil.createPersonalScopeProperty(ACTIONS, MOBILE_FAILOVER_PATH, "2", true, false));
            idList.add(PMSUtil.createPersonalScopeProperty(RETRIES, MOBILE_FAILOVER_PATH, "5", true, false));
            idList.add(PMSUtil.createPersonalScopeProperty(TIMEOUT, MOBILE_FAILOVER_PATH, "2", true, false));
            idList.add(PMSUtil.createPersonalScopeProperty(ATTEMPT_INTERVAL, MOBILE_FAILOVER_PATH, "4", true, true));
            logSleeping(oneMinute);
            IpTablesUtil.banRelayServer(true);
            long startTime = System.currentTimeMillis();
            HttpResponse response;
            for (int i = 1; i <= 3; i++) {
                response = sendRequest(request);
                logSleeping(MIN_NON_RMP_PERIOD - response.getDuration());
            }
            logcatUtil.stop();
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, ENABLED, "1"));
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, ACTIONS, "2"));
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, RETRIES, "5"));
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, TIMEOUT, "2"));
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, ATTEMPT_INTERVAL, "4"));
            assertFalse("Failover must be active at this moment", failoverStartTask.getLogEntries().isEmpty());
            logSleeping(8 * 60 * 1000);
            IpTablesUtil.banRelayServer(false);
            logSleeping(oneMinute);
            idList.add(PMSUtil.createPersonalScopeProperty("transparent", "@asimov", "1", true));
            logSleeping(oneMinute);
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, "transparent", "1"));
            long endTime = System.currentTimeMillis();
            SysLogUtil sysLogUtil = new SysLogUtil(dispatcherStateTask);
            sysLogUtil.parseLog();
            boolean result = checkChangedDispatcherState(dispatcherStateTask.getLogEntries(), startTime, endTime,
                    DispatcherState.RUNNING, DispatcherState.PENDING_SHUTDOWN, DispatcherState.STOPPED);
            assertTrue("Dispatcher did not change it state as expected ", result);
        } finally {
            for (String id : idList) {
                if (id != null) {
                    PMSUtil.deleteProperty(id);
                }
            }
            PrepareResourceUtil.prepareResource(uri, true);
            logcatUtil.stop();
        }
    }

    /**
     * New configuration should not be applied in case it was received in case enable=0
     * <ol>
     * <li>Configure new dispatcher</li>
     * <li>Policy should be received and added</li>
     * <li>Set such policy: asimov@enable=0</li>
     * <li>Policy should be received and added</li>
     * <li>Dispatcher should change state from RUNNING to PENDING_SHUTDOWN, HUB signal should be sent to child process (identified by PID)</li>
     * <li>After shu_message state should be STOPPED, deconfigure iptables and reinitialize state machine should be done</li>
     * <li>Dispatcher's setting should be changed</li>
     * <li>After receiving policy with  new configuration for dispatcher, it should not change it state in case enable=0</li>
     * </ol>
     * <p/>
     * TestLab test case name: CONF_DISP_015
     *
     * @throws InterruptedException
     */
    public void test_028_newConfigurationShouldNotBeApplied() throws InterruptedException {
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), policyAddedTask);
        List<String> idTcpList = null;
        String idEnabled = null;
        DispatcherStateTask dispatcherStateTask = new DispatcherStateTask();
        SysLogUtil sysLogUtil = new SysLogUtil(dispatcherStateTask);
        try {
            logcatUtil.start();
            idTcpList = addPersonalInterceptionProperty("tcp", "25", "1", "0");
            logSleeping(oneMinute);
            logcatUtil.stop();
            boolean policyReceived = checkInterceptionPolicyReceived(policyAddedTask, "25", "1", "0");
            assertTrue("Interception policy wasn't received", policyReceived);
            long startTime = System.currentTimeMillis();
            logcatUtil.start();
            idEnabled = PMSUtil.createPersonalScopeProperty(ENABLED, ASIMOV_PATH, "0", true, true);
            logSleeping(oneMinute);
            logcatUtil.stop();
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, ENABLED, "0"));
            PMSUtil.deleteProperty(idTcpList.get(3));
            idTcpList.set(3, PMSUtil.createPersonalScopeProperty(Z_ORDER, INTERCEPTION_PATH + "tcp", "1", true, true));
            logSleeping(oneMinute);
            long endTime = System.currentTimeMillis();
            sysLogUtil.parseLog();
            assertTrue("Dispatcher did not change it states as expected", checkDispatcherNotChangeItState(
                    dispatcherStateTask.getLogEntries(), startTime, endTime, DispatcherState.RUNNING,
                    DispatcherState.PENDING_SHUTDOWN, DispatcherState.STOPPED));
        } finally {
            removePersonalInterceptionProperties(idTcpList);
            if (idEnabled != null) PMSUtil.deleteProperty(idEnabled);
        }
    }

    /**
     * Every tcp dispatcher should listen to its port in case of couple of tcp dispatchers
     * <ol>
     * <li> Create namespace “http” and add properties  “interception ports” with value 80,
     * “type” with value 1 and “z_order” with value 0 in interception branch of PMS</li>
     * <li>Create namespace “https” and add properties  “interception ports” with value 443,
     * “type” with value 1 and “z_order” with value 0 in interception branch of PMS</li>
     * <li>Traffic for port 80 should be listened and optimized by tcp dispatcher with name “http”</li>
     * <li>Traffic for port 443 should be listened and optimized by tcp dispatcher with name “https”</li>
     * </ol>
     * <p/>
     * TestLab test case name:
     *
     * @throws Exception
     */
    public void test_029_EveryTcpDispatcherShouldListenToItsPort() throws Exception {
        List<String> idHttpList = null;
        List<String> idHttpsList = null;
        String httpDispatcher = "http";
        String httpsDispatcher = "https";
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        CLQConstructedTask clqConstructedTask = new CLQConstructedTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), policyAddedTask, clqConstructedTask);
        String httpUri = createTestResourceUri("test_030_EveryTcpDispatcherShouldListenToItsPortHttp");
        String httpsUri = createTestResourceUri("test_030_EveryTcpDispatcherShouldListenToItsPortHttps", true);
        HttpRequest httpRequest = createRequest().setUri(httpUri).addHeaderField("X-OC-Encoding", "identity")
                .addHeaderField("Cache-Control", "no-cache, no-store").getRequest();
        HttpRequest httpsRequest = createRequest().setUri(httpsUri).addHeaderField("X-OC-Encoding", "identity").getRequest();
        try {
            PrepareResourceUtil.prepareResource(httpUri, false);
            logcatUtil.start();
            idHttpList = addPersonalInterceptionProperty(httpDispatcher, "80", "1", "0");
            idHttpsList = addPersonalInterceptionProperty(httpsDispatcher, "443", "1", "0");
            logSleeping(oneMinute);
            logcatUtil.stop();
            assertTrue("Policy does not received or applied",
                    checkPolicyReceived(policyAddedTask, INTERCEPTION_PORTS, "80")
                            && checkPolicyReceived(policyAddedTask, Z_ORDER, "0")
                            && checkPolicyReceived(policyAddedTask, TYPE, "1")
                            && checkPolicyReceived(policyAddedTask, INTERCEPTION_PORTS, "443"));
            clqConstructedTask.reset();
            logcatUtil.start();
            HttpResponse response;
            for (int i = 0; i < 10; i++) {
                if (i < 5) {
                    response = sendRequest(httpRequest);
                    logSleeping(MIN_NON_RMP_PERIOD - response.getDuration());
                } else {
                    response = sendRequest(httpsRequest);
                    logSleeping(MIN_NON_RMP_PERIOD - response.getDuration());
                }
            }
            logcatUtil.stop();
            for (CLQConstructedWrapper entry : clqConstructedTask.getLogEntries()) {
                if (entry.getDispatcher().toLowerCase().equals(httpDispatcher.toLowerCase())) {
                    assertFalse("HTTPS transactions should not be processed by HTTP dispatcher", entry.getDestinationPort() == 443);
                    assertTrue(httpDispatcher + " should optimise 80 port, but optimise port" +
                            entry.getDestinationPort(), entry.getDestinationPort() == 80);
                }
                if (entry.getDispatcher().toLowerCase().equals(httpsDispatcher.toLowerCase())) {
                    assertFalse("HTTP transactions should not be processed by HTTPS dispatcher", entry.getDestinationPort() == 80);
                    assertTrue(httpsDispatcher + " should optimise 443 port, but optimise port" +
                            entry.getDestinationPort(), entry.getDestinationPort() == 443);
                }
            }
        } finally {
            PrepareResourceUtil.prepareResource(httpUri, true);
            PrepareResourceUtil.prepareResource(httpsUri, true);
            removePersonalInterceptionProperties(idHttpList);
            removePersonalInterceptionProperties(idHttpsList);
            logcatUtil.stop();
        }
    }

    /**
     * Only one of the same tcp dispatchers should process traffic
     * <ol>
     * <li>Create namespace “first” and add properties  “interception ports” with value 1:65535,
     * “type” with value 1 and “z_order” with value 0 in interception branch of PMS</li>
     * <li>Create namespace “second” and add properties  “interception ports” with value 1:65535,
     * “type” with value 1 and “z_order” with value 0 in interception branch of PMS</li>
     * <li>All TCP traffic should be processed by tcp dispatcher with name “first”.</li>
     * </ol>
     * <p/>
     * TestLab test case name: DISPCONFFILE_20
     *
     * @throws Exception
     */
    public void test_030_OnlyOneOfTheSameTcpDispatchersShouldProcessTraffic() throws Exception {
        List<String> idFirstList = null;
        List<String> idSecondList = null;
        String firstDispatcher = "first";
        String secondDispatcher = "second";
        String ports = "1:65535";
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        CLQConstructedTask clqConstructedTask = new CLQConstructedTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), policyAddedTask, clqConstructedTask);
        String httpUri = createTestResourceUri("test_031_OnlyOneOfTheSameTcpDispatchersShouldProcessTraffic");
        HttpRequest httpRequest = createRequest().setUri(httpUri).addHeaderField("X-OC-Encoding", "identity")
                .addHeaderField("Cache-Control", "no-cache, no-store").getRequest();
        try {
            PrepareResourceUtil.prepareResource(httpUri, false);
            logcatUtil.start();
            idFirstList = addPersonalInterceptionProperty(firstDispatcher, ports, "1", "0");
            idSecondList = addPersonalInterceptionProperty(secondDispatcher, ports, "1", "0");
            logSleeping(oneMinute);
            logcatUtil.stop();
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, INTERCEPTION_PORTS, ports)
                    && checkPolicyReceived(policyAddedTask, Z_ORDER, "0") && checkPolicyReceived(policyAddedTask, TYPE, "1"));
            clqConstructedTask.reset();
            logcatUtil.start();
            HttpResponse response;
            for (int i = 0; i < 5; i++) {
                response = sendRequest(httpRequest);
                logSleeping(MIN_NON_RMP_PERIOD - response.getDuration());
            }
            logcatUtil.stop();
            assertTrue("Transactions should be processed by \"" + firstDispatcher + "\"  + dispatcher",
                    checkTcpTrafficProcessedBySpecifiedDispatcher(firstDispatcher, clqConstructedTask));
            assertFalse("Transactions should not be processed by \"" + secondDispatcher + "\"  + dispatcher",
                    checkTcpTrafficProcessedBySpecifiedDispatcher(secondDispatcher, clqConstructedTask));
        } finally {
            PrepareResourceUtil.prepareResource(httpUri, true);
            removePersonalInterceptionProperties(idFirstList);
            removePersonalInterceptionProperties(idSecondList);
            logcatUtil.stop();
        }
    }

    /**
     * Tcp dispatcher with higher priority should process traffic
     * <ol>
     * <li>Create namespace “first” and add properties  “interception ports” with value 1:65535,
     * “type” with value 1 and “z_order” with value 100 in interception branch of PMS</li>
     * <li>Create namespace “second” and add properties  “interception ports” with value 1:65535,
     * “type” with value 1 and “z_order” with value 0 in interception branch of PMS</li>
     * <li>All TCP traffic should be processed by tcp dispatcher with name “second”.</li>
     * </ol>
     * <p/>
     * TestLab test case name: DISPCONFFILE_21
     *
     * @throws Exception
     */
    public void test_031_TcpDispatcherWithHigherPriorityShouldProcessTraffic() throws Exception {
        List<String> idFirstList = null;
        List<String> idSecondList = null;
        String firstDispatcher = "first";
        String secondDispatcher = "second";
        String ports = "1:65535";
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        CLQConstructedTask clqConstructedTask = new CLQConstructedTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), policyAddedTask, clqConstructedTask);
        String uri = createTestResourceUri("test_032_TcpDispatcherWithHigherPriorityShouldProcessTraffic");
        HttpRequest httpRequest = createRequest().setUri(uri).addHeaderField("X-OC-Encoding", "identity")
                .addHeaderField("Cache-Control", "no-cache, no-store").getRequest();
        try {
            PrepareResourceUtil.prepareResource(uri, false);
            logcatUtil.start();
            idFirstList = addPersonalInterceptionProperty(firstDispatcher, ports, "1", "100");
            idSecondList = addPersonalInterceptionProperty(secondDispatcher, ports, "1", "0");
            logSleeping(oneMinute);
            logcatUtil.stop();
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, INTERCEPTION_PORTS, ports)
                    && checkPolicyReceived(policyAddedTask, Z_ORDER, "0") && checkPolicyReceived(policyAddedTask, TYPE, "1")
                    && checkPolicyReceived(policyAddedTask, Z_ORDER, "100"));
            clqConstructedTask.reset();
            logcatUtil.start();
            HttpResponse response;
            for (int i = 0; i < 5; i++) {
                response = sendRequest(httpRequest);
                logSleeping(MIN_NON_RMP_PERIOD - response.getDuration());
            }
            logcatUtil.stop();
            assertTrue("Transactions should be processed by \"" + secondDispatcher + "\"  + dispatcher",
                    checkTcpTrafficProcessedBySpecifiedDispatcher(secondDispatcher, clqConstructedTask));
            assertFalse("Transactions should not be processed by \"" + firstDispatcher + "\"  + dispatcher",
                    checkTcpTrafficProcessedBySpecifiedDispatcher(firstDispatcher, clqConstructedTask));
        } finally {
            PrepareResourceUtil.prepareResource(uri, true);
            removePersonalInterceptionProperties(idFirstList);
            removePersonalInterceptionProperties(idSecondList);
            logcatUtil.stop();
        }
    }

    /**
     * Behavior of Dispatcher in RUNNING state after receiving  kill -9 signal
     * <ol>
     * <li>Dispatcher should be in state RUNNING when it received kill -9 signal from Controller. </li>
     * <li>After receiving  chld_message from Controller, Dispatcher should change state to INITIALIZED</li>
     * </ol>
     * <p/>
     * TestLab test case name: CONF_DISP_007
     *
     * @throws InterruptedException
     */
    public void test_032_BehaviorOfDispatcherAfterReceivingKillNine() throws InterruptedException {
        List<String> idPropertyList = null;
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        DispatcherStateTask dispatcherStateTask = new DispatcherStateTask();
        SysLogUtil sysLogUtil = new SysLogUtil(dispatcherStateTask);
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), policyAddedTask);
        try {
            logcatUtil.start();
            idPropertyList = addPersonalInterceptionProperty("adns", "53", "2", "55");
            long startTime = System.currentTimeMillis();
            logSleeping(oneMinute);
            logcatUtil.stop();
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, INTERCEPTION_PORTS, "53")
                    && checkPolicyReceived(policyAddedTask, Z_ORDER, "55") && checkPolicyReceived(policyAddedTask, TYPE, "2"));
            ShellUtil.killAll("adns", 9);
            logSleeping(15 * 1000);
            long endTime = System.currentTimeMillis();
            sysLogUtil.parseLog();
            assertTrue("Dispatcher did not change it states as expected",
                    checkChangedDispatcherState(dispatcherStateTask.getLogEntries(), startTime, endTime,
                            DispatcherState.STARTED, DispatcherState.RUNNING, DispatcherState.UNCONTROLLED,
                            DispatcherState.CREATED, DispatcherState.INITIALIZED));
        } finally {
            removePersonalInterceptionProperties(idPropertyList);
            logcatUtil.stop();
        }
    }

    /**
     * Behavior of Dispatcher in RUNNING state after receiving  kill -1 signal
     * <ol>
     * <li>Dispatcher should be in state RUNNING when it received kill -1 signal from Controller. </li>
     * <li>After receiving  chld_message from Controller, Dispatcher should change state to KILLED</li>
     * </ol>
     * <p/>
     * TestLab test case name: CONF_DISP_006
     *
     * @throws InterruptedException
     */
    public void test_033_BehaviorOfDispatcherAfterReceivingKillOne() throws InterruptedException {
        List<String> idPropertyList = null;
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        DispatcherStateTask dispatcherStateTask = new DispatcherStateTask();
        SysLogUtil sysLogUtil = new SysLogUtil(dispatcherStateTask);
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), policyAddedTask);
        try {
            logcatUtil.start();
            idPropertyList = addPersonalInterceptionProperty("adns", "53", "2", "50");
            long startTime = System.currentTimeMillis();
            logSleeping(oneMinute);
            logcatUtil.stop();
            assertTrue("Policy does not received or applied", checkPolicyReceived(policyAddedTask, INTERCEPTION_PORTS, "53")
                    && checkPolicyReceived(policyAddedTask, Z_ORDER, "50") && checkPolicyReceived(policyAddedTask, TYPE, "2"));
            ShellUtil.killAll("adns", 1);
            logSleeping(15 * 1000);
            long endTime = System.currentTimeMillis();
            sysLogUtil.parseLog();
            assertTrue("Dispatcher did not change it states as expected",
                    checkDispatcherNotChangeItState(dispatcherStateTask.getLogEntries(), startTime, endTime,
                            DispatcherState.STARTED, DispatcherState.RUNNING, DispatcherState.KILLED));
        } finally {
            removePersonalInterceptionProperties(idPropertyList);
            logcatUtil.stop();
        }
    }

    /**
     * Default config file should be uninstalled with OC
     * Config file “dispatchers_default.cfg” should be deleted from OC home directory.
     * <p/>
     * TestLab test case name: DISPCONFFILE_02
     *
     * @throws Exception
     */
    public void test_034_defaultConfigFileShouldBeUninstalledWithOC() throws Exception {
        try {
            OCUtil.removeOCClient(false);
            File file = new File(OC_HOME_DIRECTORY + DEFAULT_CFG);
            assertFalse("Default config file exists after OC had uninstalled, but shouldn't be ", file.exists());
        } finally {
            OCUtil.installOCClient(OC_APK_FILE_NAME);
        }
    }
}