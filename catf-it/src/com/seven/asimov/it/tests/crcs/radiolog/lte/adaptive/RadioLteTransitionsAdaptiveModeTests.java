package com.seven.asimov.it.tests.crcs.radiolog.lte.adaptive;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.*;
import com.seven.asimov.it.utils.logcat.LogcatChecks;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.LogLabelTask;
import com.seven.asimov.it.utils.logcat.tasks.crcsTasks.DataActivityTrackerNotificationTask;
import com.seven.asimov.it.utils.logcat.tasks.crcsTasks.RadiologTask;
import com.seven.asimov.it.utils.logcat.wrappers.LogEntryWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.RadioStateType;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>This test case test LTE radio state tracking in adaptive mode.</p>
 * <p>All tests should be launched on LTE-network. OCC application should be built with next branding parameters:</p>
 * <ul>
 * <li>client.openchannel.lte_radio_logs=1</li>
 * <li>client.openchannel.lte_radio_log.mode=1</li>
 * <li>client.openchannel.lte_radio_log.dormancy_timer=5</li>
 * </ul>
 * <p>Recommended target branding to TF - teng056_nozip_ga_qa_test_it_rooted_sms_trigger_1_log777_lte_mode_1</p>
 */
public class RadioLteTransitionsAdaptiveModeTests extends TcpDumpTestCase {

    private static final Logger logger = LoggerFactory.getLogger(RadioLteTransitionsAdaptiveModeTests.class.getSimpleName());
    LogEntryWrapper logEntry;

    /**
     * <p>Verification of adaptive mode: demotion before calculated dormancy timer</p>
     * <p>Steps:</p>
     * <ul>
     *     <li>Ban all applications traffic except OC by iptables</li>
     *     <li>Start LogcatUtil</li>
     *     <li>Write LTE_LABEL and send one request</li>
     *     <li>Turn screen off and wait some time</li>
     *     <li>Stop LogcatUtil</li>
     * </ul>
     * <p>Expected results:</p>
     * <ul>
     *     <li>After LTE_LABEL should be DATA_ACTIVITY_CONNECTED notification</li>
     *     <li>Radio state should be RRC_CONNECTED</li>
     *     <li>After screen off should be DATA_ACTIVITY_NONE notification</li>
     *     <li>Radio state should be RRC_IDLE</li>
     *     <li>Time difference between last two events should be near dormancy timer value (default - 5 sec)</li>
     * </ul>
     * @throws Exception
     */
    public void test_004_LTE_ST() throws Exception {
        LogEntryWrapper lew;
        String resource = "asimov_lte_st_04";
        String uri = createTestResourceUri(resource);
        MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());
        final String LTE_LABEL = "LTE_LABEL" + System.currentTimeMillis();
        final String SWITCH_OFF_SCREEN_LABEL = "SWITCH_OFF_SCREEN_LABEL" + System.currentTimeMillis();
        LogLabelTask logLabelTask1 = new LogLabelTask(RadioLteTransitionsAdaptiveModeTests.class.getSimpleName(), LTE_LABEL);
        LogLabelTask logLabelTask2 = new LogLabelTask(RadioLteTransitionsAdaptiveModeTests.class.getSimpleName(), SWITCH_OFF_SCREEN_LABEL);
        RadiologTask radiologTask = new RadiologTask();
        DataActivityTrackerNotificationTask dataActivityTrackerNotificationTask1 =
                new DataActivityTrackerNotificationTask("DATA_ACTIVITY_CONNECTED");
        DataActivityTrackerNotificationTask dataActivityTrackerNotificationTask2 =
                new DataActivityTrackerNotificationTask("DATA_ACTIVITY_NONE");
        LogcatUtil logcatUtil1 = new LogcatUtil(getContext(), logLabelTask1, logLabelTask2, radiologTask,
                dataActivityTrackerNotificationTask1, dataActivityTrackerNotificationTask2);
        ScreenUtils.ScreenSpyResult spy = ScreenUtils.switchScreenAndSpy(getContext(), false);
        ScreenUtils.screenOn();

        try {
            logcatUtil1.start();
            IpTablesUtil.banAllAppsButOC(true, getContext());
            ScreenUtils.screenOff();
            mobileNetworkUtil.switchWifiOnOff(true);
            mobileNetworkUtil.switchWifiOnOff(false);
            TestUtil.sleep(10 * 1000);
            logger.info(LTE_LABEL);
            HttpRequest request = createRequest().setMethod(HttpGet.METHOD_NAME).setUri(uri).getRequest();
            sendRequest2(request, false, false);
            ScreenUtils.screenOff();
            logger.info(SWITCH_OFF_SCREEN_LABEL);
            TestUtil.sleep(30 * 1000);
            logcatUtil1.stop();

            logcatUtil1.logTasks();

            logEntry = null;
            logEntry = LogcatChecks.checkLogEntryExist(logLabelTask1, logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(dataActivityTrackerNotificationTask1, logEntry);
            logEntry = LogcatChecks.checkRadioLog(radiologTask, RadioStateType.rrc_connected, logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(logLabelTask2, logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(dataActivityTrackerNotificationTask2, logEntry);
            lew = logEntry;
            logEntry = LogcatChecks.checkRadioLog(radiologTask, RadioStateType.rrc_idle, logEntry);

            assertTrue("There should be difference between timestamps near 5 sec",
                    (logEntry.getTimestamp() - lew.getTimestamp() < 6100) && (logEntry.getTimestamp() - lew.getTimestamp() > 3900));
        } finally {
            IpTablesUtil.banAllAppsButOC(false, getContext());
            logcatUtil1.stop();
            ScreenUtils.screenOn();
            ScreenUtils.finishScreenSpy(getContext(), spy);
        }
    }

    /**
     * <p>Verification of adaptive mode: calculation of dormancy timer</p>
     * <p>Steps:</p>
     * <ul>
     *     <li>Remove file on "/data/misc/openchannel/stat/" (to prevent restart failover)</li>
     *     <li>Restart OCC</li>
     *     <li>Block all applications traffic without OC</li>
     *     <li>Start LogcatUtil</li>
     *     <li>Write LTE_LABEL and send one request</li>
     *     <li>Sleep some time</li>
     *     <li>Turn screen off and wait some time</li>
     *     <li>Stop LogcatUtil</li>
     * </ul>
     * <p>Expected results:</p>
     * <ul>
     *     <li>After LTE_LABEL radio state should be RRC_CONNECTED because request was sent</li>
     *     <li>After DATA_ACTIVITY_NONE and DATA_ACTIVITY_DORMANT notifications dormancy timeout should be recalculated</li>
     *     <li>After receiving DATA_ACTIVITY_NONE notification and new dormancy timeout expired radio state should be
     *     changed to RRC_IDLE</li>
     * </ul>
     * <p>Note that dormancy timer calculated value validity not check because this step realization causes some
     * difficulties</p>
     * @throws Exception
     */

    public void test_005_LTE_ST() throws Exception {
        String resource = "asimov_lte_st_05";
        String uri = createTestResourceUri(resource);
        MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());
        final String LTE_LABEL = "LTE_LABEL" + System.currentTimeMillis();
        final String DORMANCY_TIMEOUT_LABEL = "LTE dormancy timeout set to";
        LogLabelTask logLabelTask1 = new LogLabelTask(RadioLteTransitionsAdaptiveModeTests.class.getSimpleName(), LTE_LABEL);
        LogLabelTask logLabelTask2 = new LogLabelTask("Asimov.*", DORMANCY_TIMEOUT_LABEL);
        RadiologTask radiologTask = new RadiologTask();
        LogcatUtil logcatUtil1 = new LogcatUtil(getContext(), logLabelTask1, logLabelTask2, radiologTask);
        ScreenUtils.ScreenSpyResult spy = ScreenUtils.switchScreenAndSpy(getContext(), false);
        ScreenUtils.screenOff();
        ShellUtil.removeDirectory("/data/misc/openchannel/stat/");
        OCUtil.restartOc();
        try {
            logcatUtil1.start();
            IpTablesUtil.banAllAppsButOC(true, getContext());
            mobileNetworkUtil.switchWifiOnOff(true);
            mobileNetworkUtil.switchWifiOnOff(false);
            TestUtil.sleep(10 * 1000);
            ScreenUtils.screenOn();
            logger.info(LTE_LABEL);
            HttpRequest request = createRequest().setMethod(HttpGet.METHOD_NAME).setUri(uri).getRequest();
            checkMiss(request, 1);
            TestUtil.sleep(60 * 1000);
            ScreenUtils.screenOff();
            TestUtil.sleep(30 * 1000);
            logcatUtil1.stop();

            logcatUtil1.logTasks();

            logEntry = null;
            logEntry = LogcatChecks.checkLogEntryExist(logLabelTask1, logEntry);
            logEntry = LogcatChecks.checkRadioLog(radiologTask, RadioStateType.rrc_connected, logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(logLabelTask2, logEntry);
            logEntry = LogcatChecks.checkRadioLog(radiologTask, RadioStateType.rrc_idle, logEntry);
        } finally {
            IpTablesUtil.banAllAppsButOC(false, getContext());
            logcatUtil1.stop();
            ScreenUtils.screenOn();
            ScreenUtils.finishScreenSpy(getContext(), spy);
        }
    }

    /**
     * <p>Verification of adaptive mode: calculation of dormancy timer</p>
     * <p>Steps:</p>
     * <ul>
     *     <li>Remove file on "/data/misc/openchannel/stat/" (to prevent restart failover)</li>
     *     <li>Restart OCC</li>
     *     <li>Block all applications traffic without OC</li>
     *     <li>Start LogcatUtil</li>
     *     <li>Write LTE_LABEL and send one request</li>
     *     <li>Sleep some time</li>
     *     <li>Turn screen off and wait some time</li>
     *     <li>Turn screen on, send request and wait some time</li>
     *     <li>Stop LogcatUtil</li>
     * </ul>
     * <p>Expected results:</p>
     * <ul>
     *     <li>After LTE_LABEL radio state should be RRC_CONNECTED because request was sent</li>
     *     <li>After DATA_ACTIVITY_NONE and DATA_ACTIVITY_DORMANT notifications dormancy timeout should be recalculated</li>
     *     <li>After receiving DATA_ACTIVITY_NONE notification and new dormancy timeout expired radio state should be
     *     changed to RRC_IDLE</li>
     *     <li>After second request sending radio state should be changed to rrc_idle and after previously calculated timer
     *     expired</li>
     * </ul>
     * <p>Note that dormancy timer calculated value validity not check because this step realization causes some
     * difficulties</p>
     * @throws Exception
     */
    public void test_006_LTE_ST() throws Exception {
        String resource = "asimov_lte_st_06";
        String uri = createTestResourceUri(resource);
        MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());
        final String LTE_LABEL = "LTE_LABEL" + System.currentTimeMillis();
        final String SCREEN_ON_LABEL = "SCREEN_ON_LABEL" + System.currentTimeMillis();
        final String SWITCH_OFF_SCREEN_LABEL = "SWITCH_OFF_SCREEN_LABEL" + System.currentTimeMillis();
        final String DORMANCY_TIMEOUT_LABEL = "LTE dormancy timeout set to";
        LogLabelTask logLabelTask1 = new LogLabelTask(RadioLteTransitionsAdaptiveModeTests.class.getSimpleName(), LTE_LABEL);
        LogLabelTask logLabelTask2 = new LogLabelTask("Asimov.*", DORMANCY_TIMEOUT_LABEL);
        LogLabelTask logLabelTask3 = new LogLabelTask(RadioLteTransitionsAdaptiveModeTests.class.getSimpleName(), SCREEN_ON_LABEL);
        LogLabelTask logLabelTask4 = new LogLabelTask(RadioLteTransitionsAdaptiveModeTests.class.getSimpleName(), SWITCH_OFF_SCREEN_LABEL);
        RadiologTask radiologTask = new RadiologTask();
        LogcatUtil logcatUtil1 = new LogcatUtil(getContext(), logLabelTask1, logLabelTask2, logLabelTask3, logLabelTask4, radiologTask);
        ScreenUtils.ScreenSpyResult spy = ScreenUtils.switchScreenAndSpy(getContext(), false);
        ScreenUtils.screenOff();
        ShellUtil.removeDirectory("/data/misc/openchannel/stat/");
        OCUtil.restartOc();
        try {
            logcatUtil1.start();
            IpTablesUtil.banAllAppsButOC(true, getContext());
            mobileNetworkUtil.switchWifiOnOff(true);
            mobileNetworkUtil.switchWifiOnOff(false);
            TestUtil.sleep(10 * 1000);
            ScreenUtils.screenOn();
            logger.info(LTE_LABEL);
            HttpRequest request = createRequest().setMethod(HttpGet.METHOD_NAME).setUri(uri).getRequest();
            checkMiss(request, 1);
            TestUtil.sleep(180 * 1000);
            ScreenUtils.screenOff();
            TestUtil.sleep(30 * 1000);
            ScreenUtils.screenOn();
            checkMiss(request, 2);
            TestUtil.sleep(30 * 1000);
            logcatUtil1.stop();

            logcatUtil1.logTasks();

            logEntry = null;
            logEntry = LogcatChecks.checkLogEntryExist(logLabelTask1, logEntry);
            logEntry = LogcatChecks.checkRadioLog(radiologTask, RadioStateType.rrc_connected, logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(logLabelTask2, logEntry);
            logEntry = LogcatChecks.checkRadioLog(radiologTask, RadioStateType.rrc_idle, logEntry);
            logEntry = LogcatChecks.checkRadioLog(radiologTask, RadioStateType.rrc_connected, logEntry);
            logEntry = LogcatChecks.checkRadioLog(radiologTask, RadioStateType.rrc_idle, logEntry);
        } finally {
            IpTablesUtil.banAllAppsButOC(false, getContext());
            logcatUtil1.stop();
            ScreenUtils.screenOn();
            ScreenUtils.finishScreenSpy(getContext(), spy);
        }
    }

    /**
     * <p>Verification of adaptive mode: demotion with calculated dormancy timer (screen OFF)</p>
     * <p>Steps:</p>
     * <ul>
     *     <li>Remove file on "/data/misc/openchannel/stat/" (to prevent restart failover)</li>
     *     <li>Restart OCC</li>
     *     <li>Block all applications traffic without OC</li>
     *     <li>Start LogcatUtil</li>
     *     <li>Write LTE_LABEL and send one request</li>
     *     <li>Send request and sleep some time</li>
     *     <li>Turn screen off and wait some time</li>
     *     <li>Turn screen on, send request, than turn screen off and wait some time</li>
     *     <li>Stop LogcatUtil</li>
     * </ul>
     * <p>Expected results:</p>
     * <ul>
     *     <li>After LTE_LABEL radio state should be RRC_CONNECTED because request was sent</li>
     *     <li>After DATA_ACTIVITY_NONE and DATA_ACTIVITY_DORMANT notifications dormancy timeout should be recalculated</li>
     *     <li>After receiving DATA_ACTIVITY_NONE notification and new dormancy timeout expired radio state should be
     *     changed to RRC_IDLE</li>
     *     <li>After second request sending radio state should be changed to rrc_idle and after previously calculated timer
     *     expired</li>
     * </ul>
     * <p>Note that dormancy timer calculated value validity not check because this step realization causes some
     * difficulties</p>
     * @throws Exception
     */
    public void test_007_LTE_ST() throws Exception {
        String resource = "asimov_lte_st_07";
        String uri = createTestResourceUri(resource);
        MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());
        final String LTE_LABEL = "LTE_LABEL" + System.currentTimeMillis();
        final String DORMANCY_TIMEOUT_LABEL = "LTE dormancy timeout set to";
        LogLabelTask logLabelTask1 = new LogLabelTask(RadioLteTransitionsAdaptiveModeTests.class.getSimpleName(), LTE_LABEL);
        LogLabelTask logLabelTask2 = new LogLabelTask("Asimov.*", DORMANCY_TIMEOUT_LABEL);
        RadiologTask radiologTask = new RadiologTask();
        LogcatUtil logcatUtil1 = new LogcatUtil(getContext(), logLabelTask1, logLabelTask2, radiologTask);
        ScreenUtils.ScreenSpyResult spy = ScreenUtils.switchScreenAndSpy(getContext(), false);
        ScreenUtils.screenOff();
        ShellUtil.removeDirectory("/data/misc/openchannel/stat/");
        OCUtil.restartOc();
        try {
            logcatUtil1.start();
            IpTablesUtil.banAllAppsButOC(true, getContext());
            ScreenUtils.screenOn();
            mobileNetworkUtil.switchWifiOnOff(true);
            mobileNetworkUtil.switchWifiOnOff(false);
            TestUtil.sleep(10 * 1000);
            logger.info(LTE_LABEL);
            HttpRequest request = createRequest().setMethod(HttpGet.METHOD_NAME).setUri(uri).getRequest();
            checkMiss(request, 1);
            TestUtil.sleep(180 * 1000);
            ScreenUtils.screenOff();
            TestUtil.sleep(30 * 1000);
            ScreenUtils.screenOn();
            sendRequest2(request, false, false);
            ScreenUtils.screenOff();
            TestUtil.sleep(120 * 1000);
            logcatUtil1.stop();

            logEntry = null;
            logEntry = LogcatChecks.checkLogEntryExist(logLabelTask1, logEntry);
            logEntry = LogcatChecks.checkRadioLog(radiologTask, RadioStateType.rrc_connected, logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(logLabelTask2, logEntry);
            logEntry = LogcatChecks.checkRadioLog(radiologTask, RadioStateType.rrc_idle, logEntry);
            logEntry = LogcatChecks.checkRadioLog(radiologTask, RadioStateType.rrc_connected, logEntry);
            logEntry = LogcatChecks.checkRadioLog(radiologTask, RadioStateType.rrc_idle, logEntry);
        } finally {
            IpTablesUtil.banAllAppsButOC(false, getContext());
            logcatUtil1.stop();
            ScreenUtils.screenOn();
            ScreenUtils.finishScreenSpy(getContext(), spy);
        }
    }

    /**
     * <p>Verification of adaptive mode: promotion</p>
     * <p>Steps:</p>
     * <ul>
     *     <li>Remove file on "/data/misc/openchannel/stat/" (to prevent restart failover)</li>
     *     <li>Restart OCC</li>
     *     <li>Block all applications traffic without OC</li>
     *     <li>Start LogcatUtil</li>
     *     <li>Write LTE_LABEL and send one request</li>
     *     <li>Send request and sleep some time</li>
     *     <li>Turn screen off and wait some time</li>
     *     <li>Turn screen on, send request, than turn screen off and wait some time</li>
     *     <li>Send request and wait some time</li>
     *     <li>Stop LogcatUtil</li>
     * </ul>
     * <p>Expected results:</p>
     * <ul>
     *     <li>After LTE_LABEL radio state should be RRC_CONNECTED because request was sent</li>
     *     <li>After DATA_ACTIVITY_NONE and DATA_ACTIVITY_DORMANT notifications dormancy timeout should be recalculated</li>
     *     <li>After receiving DATA_ACTIVITY_NONE notification and new dormancy timeout expired radio state should be
     *     changed to RRC_IDLE</li>
     *     <li>After second request sending radio state should be changed to rrc_idle and after previously calculated timer
     *     expired</li>
     * </ul>
     * <p>Note that dormancy timer calculated value validity not check because this step realization causes some
     * difficulties</p>
     * @throws Exception
     */
    public void test_008_LTE_ST() throws Exception {
        String resource = "asimov_lte_st_08";
        String uri = createTestResourceUri(resource);
        MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());
        final String LTE_LABEL = "LTE_LABEL" + System.currentTimeMillis();
        final String DORMANCY_TIMEOUT_LABEL = "LTE dormancy timeout set to";
        LogLabelTask logLabelTask1 = new LogLabelTask(RadioLteTransitionsAdaptiveModeTests.class.getSimpleName(), LTE_LABEL);
        LogLabelTask logLabelTask2 = new LogLabelTask("Asimov.*", DORMANCY_TIMEOUT_LABEL);
        RadiologTask radiologTask = new RadiologTask();
        LogcatUtil logcatUtil1 = new LogcatUtil(getContext(), logLabelTask1, logLabelTask2, radiologTask);
        ScreenUtils.ScreenSpyResult spy = ScreenUtils.switchScreenAndSpy(getContext(), false);
        ScreenUtils.screenOff();
        ShellUtil.removeDirectory("/data/misc/openchannel/stat/");
        OCUtil.restartOc();
        try {
            logcatUtil1.start();
            IpTablesUtil.banAllAppsButOC(true, getContext());
            ScreenUtils.screenOn();
            mobileNetworkUtil.switchWifiOnOff(true);
            mobileNetworkUtil.switchWifiOnOff(false);
            TestUtil.sleep(10 * 1000);
            logger.info(LTE_LABEL);
            HttpRequest request = createRequest().setMethod(HttpGet.METHOD_NAME).setUri(uri).getRequest();
            checkMiss(request, 1);
            TestUtil.sleep(180 * 1000);
            ScreenUtils.screenOff();
            TestUtil.sleep(30 * 1000);
            ScreenUtils.screenOn();
            sendRequest2(request, false, false);
            ScreenUtils.screenOff();
            TestUtil.sleep(120 * 1000);
            sendRequest2(request, false, false);
            TestUtil.sleep(120 * 1000);
            logcatUtil1.stop();

            logEntry = null;
            logEntry = LogcatChecks.checkLogEntryExist(logLabelTask1, logEntry);
            logEntry = LogcatChecks.checkRadioLog(radiologTask, RadioStateType.rrc_connected, logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(logLabelTask2, logEntry);
            logEntry = LogcatChecks.checkRadioLog(radiologTask, RadioStateType.rrc_idle, logEntry);
            logEntry = LogcatChecks.checkRadioLog(radiologTask, RadioStateType.rrc_connected, logEntry);
            logEntry = LogcatChecks.checkRadioLog(radiologTask, RadioStateType.rrc_idle, logEntry);
            logEntry = LogcatChecks.checkRadioLog(radiologTask, RadioStateType.rrc_connected, logEntry);
        } finally {
            IpTablesUtil.banAllAppsButOC(false, getContext());
            logcatUtil1.stop();
            ScreenUtils.screenOn();
            ScreenUtils.finishScreenSpy(getContext(), spy);
        }
    }
}
