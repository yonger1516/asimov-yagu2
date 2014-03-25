package com.seven.asimov.it.tests.e2e.policyupdate;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.test.AssertionFailedError;
import android.test.suitebuilder.annotation.LargeTest;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.testcases.E2EPolicyUpdateTestCase;
import com.seven.asimov.it.utils.IpTablesUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.e2eTasks.*;
import com.seven.asimov.it.utils.logcat.tasks.failoverTasks.FailoverStartTask;
import com.seven.asimov.it.utils.logcat.tasks.failoverTasks.FailoverStopTask;
import com.seven.asimov.it.utils.logcat.tasks.policyTasks.*;
import com.seven.asimov.it.utils.logcat.wrappers.PolicyWrapper;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import com.seven.asimov.it.utils.sms.SmsUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


/**
 * !!!WARNING!!!
 * Suite must be executed on 3G network type!!!
 * ALL TESTS FROM THIS TEST SUITE SHOULD BE EXECUTED ONE BY ONE!!!
 * MSISDN validation must be enabled!!!
 * Roaming/WIFI Failover must be enabled!!!
 * !!!WARNING!!!
 * E2E Policy Update Tests: ({@link com.seven.asimov.it.tests.e2e.policyupdate.E2EPolicyUpdateTests#test_001_E2E_PolicyUpdate() Test 1},
 * {@link com.seven.asimov.it.tests.e2e.policyupdate.E2EPolicyUpdateTests#test_002_E2E_PolicyUpdate() Test 2},
 * {@link com.seven.asimov.it.tests.e2e.policyupdate.E2EPolicyUpdateTests#test_003_E2E_PolicyUpdate() Test 3},
 * {@link com.seven.asimov.it.tests.e2e.policyupdate.E2EPolicyUpdateTests#test_004_E2E_PolicyUpdate() Test 4},
 * {@link com.seven.asimov.it.tests.e2e.policyupdate.E2EPolicyUpdateTests#test_005_E2E_PolicyUpdate() Test 5},
 *
 * @author amykytenko
 */
public class E2EPolicyUpdateTests extends E2EPolicyUpdateTestCase {
    private static final Logger logger = LoggerFactory.getLogger(E2EPolicyUpdateTests.class.getSimpleName());

    @LargeTest
    public void test_000_E2E_PolicyUpdate_Init() throws Throwable {
        PMSUtil.createNameSpace("@asimov@normalization@header", "*");
        logSleeping(sleepMs / 2);
        PMSUtil.createNameSpace("@asimov@normalization@header@*", ".*");
        logSleeping(sleepMs / 2);
        addProperty(NORMALIZATION_POLICY_PATH, RESPONSE_HEADER_RULES, generatePolicyName());
        logSleeping(sleepMs / 2);
        clearProperties();
        logSleeping(sleepMs);
        try {
            notifyRestForTestsStart(SUITE_NAME);
        } catch (Exception e) {
            logger.debug("Tests start REST notification failed " + ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * <h1>OC should send policy request after installation of new app.</h1>
     * <p>The test checks that after 7TestTool app was installed, package manager should update, policy should  read from Storage. Added app should be defined.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Start logcat.</li>
     * <li>Create such policy: asimov@normalization@header@com.seven.asimov.test.tool@*@.*@	response_header_rules=SVRNAME.</li>
     * <li>Install 7TestTool.</li>
     * <li>Parse logs about package manager should update.</li>
     * <li>Parse logs about policy should read from Storage.</li>
     * <li>Parse logs about added app should be defined.</li>
     * <li>Clear all personal policies</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void test_001_E2E_PolicyUpdate() throws Throwable {
        addProperty(NORMALIZATION_POLICY_PATH, RESPONSE_HEADER_RULES, generatePolicyName());
        logger.info("Installing z7TestTool...");
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        AddingAppTask addingAppTask = new AddingAppTask(COM_SEVEN_TEST);
        PackageManagerUpdatedTask packageManagerUpdatedTask = new PackageManagerUpdatedTask();
        AddedAppTask addedAppTask = new AddedAppTask();
        PolicyMGMTdataRequestTask policyMGMTdataRequestTask = new PolicyMGMTdataRequestTask();
        logcatUtil = new LogcatUtil(getContext(), addingAppTask, addedAppTask, policyMGMTdataRequestTask, packageManagerUpdatedTask, policyAddedTask);
        switchRadioUp();
        logcatUtil.start();
        try {
            File f = new File(TEST_TOOLS_APK_FILENAME);
            if (!f.exists()) throw new AssertionFailedError("TestTool APK does not exists on sdcard");
            String[] installTestTool = {"su", "-c", "pm install -r " + TEST_TOOLS_APK_FILENAME};
            logSleeping(sleepMs / 3);
            Runtime.getRuntime().exec(installTestTool).waitFor();
            logSleeping(sleepMs);
            logcatUtil.stop();
            if (addedAppTask.getLogEntries().isEmpty())
                throw new AssertionFailedError(COM_SEVEN_TEST + " was not added during test");
            if (addingAppTask.getLogEntries().isEmpty())
                throw new AssertionFailedError(COM_SEVEN_TEST + " was not adding during test");
            if (packageManagerUpdatedTask.getLogEntries().isEmpty())
                throw new AssertionFailedError("package manager must be updated after app was installed");
            if (policyMGMTdataRequestTask.getLogEntries().isEmpty())
                throw new AssertionFailedError("policy MGMT request should be sent after app was installed");
            if (policyAddedTask.getLogEntries().isEmpty())
                throw new AssertionFailedError("policies must be reached and applied by client");
        } finally {
            logcatUtil.stop();
            radioUp = false;
            clearProperties();
            logSleeping(sleepMs);
        }
    }

    /**
     * <h1>PMS should not send new policies, when app is removed.</h1>
     * <p>The test checks that after 7TestTool app was uninstalled, PMS should not send new policies.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Start logcat.</li>
     * <li>Create such policy: asimov@normalization@header@com.seven.asimov.test.tool@*@.*@	response_header_rules=SVRNAME.</li>
     * <li>Uninstall 7TestTool.</li>
     * <li>Parse logs about After 7TestTool app was uninstalled, package manager should be updated.</li>
     * <li>Parse logs about client should send a policy mgmt data reques to the server with information about what app was removed and Application list hash.</li>
     * <li>Clear all personal policies</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void test_002_E2E_PolicyUpdate() throws Throwable {
        addProperty(NORMALIZATION_POLICY_PATH, RESPONSE_HEADER_RULES, generatePolicyName());
        logger.info("Uninstalling z7TestTool...");
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        PackageManagerUpdatedTask packageManagerUpdatedTask = new PackageManagerUpdatedTask();
        PolicyMGMTdataRequestTask policyMGMTdataRequestTask = new PolicyMGMTdataRequestTask();
        AppListHashTask appListHashTask = new AppListHashTask();
        logcatUtil = new LogcatUtil(getContext(), packageManagerUpdatedTask, policyMGMTdataRequestTask, appListHashTask, policyAddedTask);
        switchRadioUp();
        logcatUtil.start();
        try {
            String[] uninstallTestTool = {"su", "-c", "pm uninstall " + TEST_TOOLS_PACKAGE_NAME};
            logSleeping(sleepMs / 3);
            Runtime.getRuntime().exec(uninstallTestTool).waitFor();
            logSleeping(sleepMs);
            logcatUtil.stop();
            if (packageManagerUpdatedTask.getLogEntries().isEmpty())
                throw new AssertionFailedError("package manager must be updated after app was uninstalled");
            if (policyMGMTdataRequestTask.getLogEntries().isEmpty())
                throw new AssertionFailedError("policy MGMT request should be sent after app was uninstalled");
            if (appListHashTask.getLogEntries().isEmpty())
                throw new AssertionFailedError("application list hash should be reported in client log");
            if (policyAddedTask.getLogEntries().isEmpty())
                throw new AssertionFailedError("policies must be reached and applied by client");
        } finally {
            logcatUtil.stop();
            clearProperties();
            radioUp = false;
            logSleeping(sleepMs);
        }
    }

    /**
     * <h1>OC shouldn't sent policy request in case: receiving SMS in radio state DOWN and importance: 1(personal policy).</h1>
     * <p>The test checks that OC shouldn't sent policy request in case: receiving SMS in radio state DOWN and importance: 1 (personal policy).</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Start logcat.</li>
     * <li>Schedule policy creating using REST FUNC {@link E2EPolicyUpdateTestCase#scheduleSetPolicyWithDelay}</li>
     * <li> Scheduled policy is added to PMS server due to REST FUNC</li>
     * <li>Parse client logs about client should receive notification SMS, after changes made on server.</li>
     * <li>Parse client logs about OC shouldn't sent request to sever due to radio DOWN and importance 0.</li>
     * <li>Switch radio state to UP.</li>
     * <li>Parse client logs about client should immediately establish connection to Relay and send a policy mgmt data request to sever.</li>
     * <li>Clear all personal policies</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void test_003_E2E_PolicyUpdate() throws Throwable {
        smppEmulator = new SmsUtil(getContext());
        String POLICY_VALUE = generatePolicyName();
        logger.info(String.format("Test 003 expects following value of the policy: %s", POLICY_VALUE));
        PolicyMGMTdataRequestTask policyMGMTdataRequestTask = new PolicyMGMTdataRequestTask();
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        PolicyMGMTUpdateTask policyMGMTUpdateTask = new PolicyMGMTUpdateTask();
        ReceivedPolicyTask receivedPolicyTask = new ReceivedPolicyTask();
        PolicySMSnotificationTask policySMSnotificationTask = new PolicySMSnotificationTask();
        PolicyChangedTask policyChangedTask = new PolicyChangedTask();
        Z7notExecutingRadioDownTask z7notExecutingRadioDownTask = new Z7notExecutingRadioDownTask();
        logcatUtil = new LogcatUtil(getContext(), policyMGMTdataRequestTask, policyMGMTUpdateTask, receivedPolicyTask, policySMSnotificationTask, policyChangedTask, policyAddedTask);
        logcatUtil.start();
        try {
            scheduleSetPolicyWithDelay(NORMALIZATION_POLICY_PATH, RESPONSE_HEADER_RULES, POLICY_VALUE, "60", SET_POLICY_WITH_DELAY_OPERATION_TYPE, false);
            boolean smsReachedClient = false;
            for (int i = 0; i < ITERATIONS; i++) {
                if (policyChangedTask.getLogEntries().isEmpty() || !policyChangedTask.getLogEntries().get(policyChangedTask.getLogEntries().size() - 1).getImportant().contains(TRUE)) {
                    smsReachedClient = waitForSms(policyMGMTUpdateTask, policySMSnotificationTask, COUNTER);
                }
            }
            if (smsReachedClient & !policySMSnotificationTask.getLogEntries().isEmpty())
                logger.info("SMS is already reached to client, switching radio to UP");
            else {
                logger.info("SMS has not reached to client, sending emulated sms");
                smppEmulator.sendPolicyUpdate((byte) 0);
            }
            switchRadioUp();
            logSleeping(sleepMs);
            properties.add(scheduleSetPolicyWithDelay(NORMALIZATION_POLICY_PATH, RESPONSE_HEADER_RULES, POLICY_VALUE, "60", POLICY_SENT_TO_PMS_OPERATION_TYPE, false));
            logcatUtil.stop();
            if (policyMGMTUpdateTask.getLogEntries().isEmpty())
                throw new AssertionFailedError("policy MGMT update notification should be reported in client log");
            if (policyMGMTdataRequestTask.getLogEntries().isEmpty())
                throw new AssertionFailedError("policy MGMT request should be sent after sms reached device");
            if (receivedPolicyTask.getLogEntries().isEmpty())
                throw new AssertionFailedError("policy receiving should be reported in client log");
            if (policySMSnotificationTask.getLogEntries().isEmpty())
                throw new AssertionFailedError("sms with policy changed notification should be reported in client log");
            if (z7notExecutingRadioDownTask.getLogEntries().isEmpty())
                logger.info("Z7 task was not scheduled, maybe radio was up");
            if (policyAddedTask.getLogEntries().isEmpty())
                throw new AssertionFailedError("policies must be reached and applied by client");
            boolean policyAppliedByClient = false;
            for (PolicyWrapper wrapper : policyAddedTask.getLogEntries()) {
                if (POLICY_VALUE.contains(wrapper.getValue())) policyAppliedByClient = true;
            }

            assertTrue("policies must be reached and applied by client", policyAppliedByClient);
        } finally {
            logcatUtil.stop();
            clearProperties();
            radioUp = false;
            logSleeping(sleepMs);
        }
    }

    /**
     * <h1>OC shouldn't sent policy request in case: receiving SMS in radio state DOWN and importance: 1(personal policy).</h1>
     * <p>The test checks that OC shouldn't sent policy request in case: receiving SMS in radio state DOWN and importance: 1 (personal policy).</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Start logcat.</li>
     * <li>Schedule policy creating using REST FUNC {@link E2EPolicyUpdateTestCase#scheduleSetPolicyWithDelay}</li>
     * <li> Scheduled policy is added to PMS server due to REST FUNC</li>
     * <li>Parse client logs about client should receive notification SMS, after changes made on server.</li>
     * <li>Parse client logs about OC should immediately send a policy mgmt data request to sever due to importance 1, despite of radio DOWN.</li>
     * <li>Parse client logs about client received policy and parse them</li>
     * <li>Clear all personal policies</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void test_004_E2E_PolicyUpdate() throws Throwable {
        String POLICY_VALUE = generatePolicyName();
        logger.info(String.format("Test 004 expects following value of the policy: %s", POLICY_VALUE));
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        PolicyMGMTUpdateTask policyMGMTUpdateTask = new PolicyMGMTUpdateTask();
        PolicyMGMTdataRequestTask policyMGMTdataRequestTask = new PolicyMGMTdataRequestTask();
        ReceivedPolicyTask receivedPolicyTask = new ReceivedPolicyTask();
        PolicySMSnotificationTask policySMSnotificationTask = new PolicySMSnotificationTask();
        PolicyChangedTask policyChangedTask = new PolicyChangedTask();
        logcatUtil = new LogcatUtil(getContext(), policyMGMTdataRequestTask, policyMGMTUpdateTask, receivedPolicyTask, policySMSnotificationTask, policyChangedTask, policyAddedTask);
        logcatUtil.start();
        try {
            scheduleSetPolicyWithDelay(NORMALIZATION_POLICY_PATH, RESPONSE_HEADER_RULES, POLICY_VALUE, "60", SET_POLICY_WITH_DELAY_OPERATION_TYPE, true);
            boolean smsReachedClient = false;
            for (int i = 0; i < ITERATIONS; i++) {
                if (policyChangedTask.getLogEntries().isEmpty() || !policyChangedTask.getLogEntries().get(policyChangedTask.getLogEntries().size() - 1).getImportant().contains(TRUE)) {
                    smsReachedClient = waitForSms(policyMGMTUpdateTask, policySMSnotificationTask, COUNTER);
                }
            }
            if (smsReachedClient & !policySMSnotificationTask.getLogEntries().isEmpty())
                logger.info("SMS is already reached to client, switching radio to UP");
            else {
                logger.info("SMS has not reached to client, sending emulated sms");
                smppEmulator.sendPolicyUpdate((byte) 1);
            }
            properties.add(scheduleSetPolicyWithDelay(NORMALIZATION_POLICY_PATH, RESPONSE_HEADER_RULES, POLICY_VALUE, "60", POLICY_SENT_TO_PMS_OPERATION_TYPE, true));
            logSleeping(sleepMs);
            logcatUtil.stop();
            if (policyMGMTUpdateTask.getLogEntries().isEmpty())
                throw new AssertionFailedError("policy MGMT update notification should be reported in client log");
            if (receivedPolicyTask.getLogEntries().isEmpty())
                throw new AssertionFailedError("policy receiving should be reported in client log");
            if (policySMSnotificationTask.getLogEntries().isEmpty())
                throw new AssertionFailedError("sms with policy changed notification should be reported in client log");
            if (policyMGMTdataRequestTask.getLogEntries().isEmpty())
                throw new AssertionFailedError("policy MGMT request should be sent after sms reached device");
            if (policyAddedTask.getLogEntries().isEmpty())
                throw new AssertionFailedError("policies must be reached and applied by client");
            boolean policyAppliedByClient = false;
            for (PolicyWrapper wrapper : policyAddedTask.getLogEntries()) {
                if (POLICY_VALUE.contains(wrapper.getValue())) policyAppliedByClient = true;
            }
            assertTrue("policies must be reached and applied by client", policyAppliedByClient);
        } finally {
            logcatUtil.stop();
            clearProperties();
            logSleeping(sleepMs);
        }
    }

    /**
     * <h1>OC should sent policy request when client back to the normal operation in case of policy update notification was received when client was in wifi failover.</h1>
     * <p>The test checks that OC should sent policy request when client back to the normal operation in case of policy update notification was received when client was in wifi failover.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Start logcat.</li>
     * <li>Schedule policy creating using REST FUNC {@link E2EPolicyUpdateTestCase#scheduleSetPolicyWithDelay}</li>
     * <li>Switch to Wi-Fi and relay should be unreachable.</li>
     * <li> Scheduled policy is added to PMS server due to REST FUNC</li>
     * <li>Parse client logs about client should start failover mode.</li>
     * <li>Parse client logs about client should receive SMS with policy notification.</li>
     * <li>Switch to 3G and enable connection to relay.</li>
     * <li>Switch to 3G and enable connection to relay.</li>
     * <li>Connection to Relay should be established immediately.</li>
     * <li>Parse client logs about client should receive “Received an unauthenticated challenge valid”.</li>
     * <li>Request for policy mgmt data should be sent.</li>
     * <li>Parse client logs about policy should be received and applied, client should send ACK message to server.</li>
     * <li>Client should receive “Received a policy mgmt server response”.</li>
     * <li>Clear all personal policies</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void test_005_E2E_PolicyUpdate() throws Throwable {
        String POLICY_VALUE = generatePolicyName();
        logger.info(String.format("Test 005 expects following value of the policy: %s", POLICY_VALUE));
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        PolicyMGMTdataRequestTask policyMGMTdataRequestTask = new PolicyMGMTdataRequestTask();
        PolicyMGMTUpdateTask policyMGMTUpdateTask = new PolicyMGMTUpdateTask();
        PolicySMSnotificationTask policySMSnotificationTask = new PolicySMSnotificationTask();
        FailoverStartTask failoverStartTask = new FailoverStartTask();
        FailoverStopTask failoverStopTask = new FailoverStopTask();
        final Policy enableFailover = new Policy("enabled", "true", "@asimov@failovers@roaming_wifi", true);
        PMSUtil.addPolicies(new Policy[]{enableFailover});
        logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
        try {
            logcatUtil = new LogcatUtil(getContext(), failoverStartTask, policySMSnotificationTask, policyMGMTUpdateTask, policyAddedTask);
            logcatUtil.start();
            scheduleSetPolicyWithDelay(NORMALIZATION_POLICY_PATH, RESPONSE_HEADER_RULES, POLICY_VALUE, "180", SET_POLICY_WITH_DELAY_OPERATION_TYPE, true);
            WifiManager wifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
            logger.info("Disabling wifi network.");
            wifiManager.setWifiEnabled(false);
            logSleeping(sleepMs);
            logger.info("Banning the relay port.");
            IpTablesUtil.banRelayServer(true, true);
            logSleeping(sleepMs);
            logger.info("Enabling wifi connection.");
            wifiManager.setWifiEnabled(true);
            logSleeping(sleepMs);
            logger.info("Adding a policy with corresponding delay");
            properties.add(scheduleSetPolicyWithDelay(NORMALIZATION_POLICY_PATH, RESPONSE_HEADER_RULES, POLICY_VALUE, "180", POLICY_SENT_TO_PMS_OPERATION_TYPE, true));
            //here will start failover
            logger.info("Waiting for the sms");
            waitForSms(policyMGMTUpdateTask, policySMSnotificationTask, COUNTER);
            logcatUtil.stop();
            logger.info("Checking that failover is active.");
            assertTrue("Failover must be active at this moment", !failoverStartTask.getLogEntries().isEmpty());
            assertTrue("Failover must be active at this moment", failoverStopTask.getLogEntries().isEmpty());
            logger.info("Failover was checked successfully.");
            logSleeping(sleepMs);
            logcatUtil = new LogcatUtil(getContext(), policyMGMTdataRequestTask, failoverStopTask, policyAddedTask);
            logcatUtil.start();
            //going out from failover
            logger.info("Unbanning the relay port.");
            IpTablesUtil.banRelayServer(false);
            logger.info("Disabling wifi network.");
            wifiManager.setWifiEnabled(false);
            //here failover will stop
            logSleeping(3 * sleepMs);
            logcatUtil.stop();
            logger.info("First entry: " + logcatUtil.getFirstEntry());
            logger.info("Last entry: " + logcatUtil.getLastEntry());
            if (failoverStopTask.getLogEntries().isEmpty())
                throw new AssertionFailedError("Failover must be stopped");
            if (policyMGMTdataRequestTask.getLogEntries().isEmpty())
                throw new AssertionFailedError("policy MGMT request should be sent after failover has been stopped");
            if (policyAddedTask.getLogEntries().isEmpty())
                throw new AssertionFailedError("policies must be reached and applied by client");
            boolean policyAppliedByClient = false;
            for (PolicyWrapper wrapper : policyAddedTask.getLogEntries()) {
                logger.info("Wrapper: " + wrapper);
                if (POLICY_VALUE.contains(wrapper.getValue())) policyAppliedByClient = true;
            }
            assertTrue("policies must be reached and applied by client", policyAppliedByClient);
        } finally {
            logcatUtil.stop();
            clearProperties();
            radioUp = false;
        }
    }

    @LargeTest
    public void test_999_E2E_Policy_Update_CleanUp() {
        try {
            notifyRestForTestEnd(SUITE_NAME);
        } catch (Exception e) {
            logger.debug("Tests end REST notification failed " + ExceptionUtils.getStackTrace(e));
        }
    }


}




