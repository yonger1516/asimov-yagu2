package com.seven.asimov.it.tests.crcs.radiolog.lte.fixed;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.*;
import com.seven.asimov.it.utils.logcat.LogcatChecks;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.LogLabelTask;
import com.seven.asimov.it.utils.logcat.tasks.crcsTasks.DataActivityTrackerNotificationTask;
import com.seven.asimov.it.utils.logcat.tasks.crcsTasks.RadiologTask;
import com.seven.asimov.it.utils.logcat.wrappers.LogEntryWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.RadioLogEntry;
import com.seven.asimov.it.utils.logcat.wrappers.RadioStateType;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * <p>This test case test LTE radio state tracking in fixed mode.</p>
 * <p>All tests should be launched on LTE-network. OCC application should be built with next branding parameters:</p>
 * <ul>
 * <li>client.openchannel.lte_radio_logs=1</li>
 * <li>client.openchannel.lte_radio_log.mode=0</li>
 * <li>client.openchannel.lte_radio_log.dormancy_timer=5</li>
 * </ul>
 * <p>This parameters are initialized in default branding<p/>
 * <p>Recommended target branding to TF - teng056_nozip_ga_qa_test_it_rooted_sms_trigger_1_log777</p>
 */
public class RadioLteTransitionsFixedModeTests extends TcpDumpTestCase {

    private static final Logger logger = LoggerFactory.getLogger(RadioLteTransitionsFixedModeTests.class.getSimpleName());

    LogEntryWrapper logEntry;

    /**
     * <p>Verification of fixed mode detection with dormancy timer > 0: from rrc_connect to idle</p>
     * <p>Steps:</p>
     * <ul>
     *     <li>Start LogcatUtil</li>
     *     <li>Block all applications traffic without OC</li>
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
    public void test_001_LTE_ST() throws Exception {
        LogEntryWrapper lew;
        String resource = "asimov_lte_st_01";
        String uri = createTestResourceUri(resource);
        MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());
        final String LTE_LABEL = "LTE_LABEL" + System.currentTimeMillis();
        final String SWITCH_OFF_SCREEN_LABEL = "SWITCH_OFF_SCREEN_LABEL" + System.currentTimeMillis();
        LogLabelTask logLabelTask1 = new LogLabelTask(RadioLteTransitionsFixedModeTests.class.getSimpleName(), LTE_LABEL);
        LogLabelTask logLabelTask2 = new LogLabelTask(RadioLteTransitionsFixedModeTests.class.getSimpleName(), SWITCH_OFF_SCREEN_LABEL);
        RadiologTask radiologTask = new RadiologTask();
        DataActivityTrackerNotificationTask dataActivityTrackerNotificationTask1 =
                new DataActivityTrackerNotificationTask("DATA_ACTIVITY_CONNECTED");
        DataActivityTrackerNotificationTask dataActivityTrackerNotificationTask2 =
                new DataActivityTrackerNotificationTask("DATA_ACTIVITY_NONE");
        LogcatUtil logcatUtil1 = new LogcatUtil(getContext(), logLabelTask1, logLabelTask2, radiologTask,
                dataActivityTrackerNotificationTask1, dataActivityTrackerNotificationTask2);
        ScreenUtils.ScreenSpyResult spy = ScreenUtils.switchScreenAndSpy(getContext(), false);

        try {
            logcatUtil1.start();
            IpTablesUtil.banAllAppsButOC(true, getContext());
            mobileNetworkUtil.switchWifiOnOff(true);
            mobileNetworkUtil.switchWifiOnOff(false);
            TestUtil.sleep(10 * 1000);
            logger.info(LTE_LABEL);
            HttpRequest request = createRequest().setMethod(HttpGet.METHOD_NAME).setUri(uri).getRequest();
            sendRequest2(request, false, false);
            logger.info(SWITCH_OFF_SCREEN_LABEL);
            ScreenUtils.screenOff();
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
                    (logEntry.getTimestamp() - lew.getTimestamp() < 6000) && (logEntry.getTimestamp() - lew.getTimestamp() > 4000));

        } finally {
            logcatUtil1.stop();
            IpTablesUtil.banAllAppsButOC(false, getContext());
            ScreenUtils.screenOn();
            ScreenUtils.finishScreenSpy(getContext(), spy);
        }
    }



    /**
     * <p>Verification of fixed mode detection with dormancy timer = 0: from rrc_connect to idle</p>
     * <p>Steps:</p>
     * <ul>
     *     <li>Add policy "dormancy" with value "0" on "@asimov@reporting@radiosettings@lte"</li>
     *     <li>Start LogcatUtil</li>
     *     <li>Block all applications traffic without OC</li>
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
     *     <li>Time difference between last two events should be near dormancy timer value</li>
     * </ul>
     * @throws Exception
     */
    public void test_002_LTE_ST() throws Exception {
        LogEntryWrapper lew;
        String resource = "asimov_lte_st_02";
        String uri = createTestResourceUri(resource);
        final String LTE_LABEL = "LTE_LABEL" + System.currentTimeMillis();
        final String SWITCH_OFF_SCREEN_LABEL = "SWITCH_OFF_SCREEN_LABEL" + System.currentTimeMillis();
        MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());
        LogLabelTask logLabelTask1 = new LogLabelTask(RadioLteTransitionsFixedModeTests.class.getSimpleName(), LTE_LABEL);
        LogLabelTask logLabelTask2 = new LogLabelTask(RadioLteTransitionsFixedModeTests.class.getSimpleName(), SWITCH_OFF_SCREEN_LABEL);
        RadiologTask radiologTask = new RadiologTask();
        DataActivityTrackerNotificationTask dataActivityTrackerNotificationTask1 =
                new DataActivityTrackerNotificationTask("DATA_ACTIVITY_CONNECTED");
        DataActivityTrackerNotificationTask dataActivityTrackerNotificationTask2 =
                new DataActivityTrackerNotificationTask("DATA_ACTIVITY_NONE");
        LogcatUtil logcatUtil1 = new LogcatUtil(getContext(), logLabelTask1, logLabelTask2, radiologTask,
                dataActivityTrackerNotificationTask1, dataActivityTrackerNotificationTask2);
        ScreenUtils.ScreenSpyResult spy = ScreenUtils.switchScreenAndSpy(getContext(), false);

        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy("dormancy", "0", "@asimov@reporting@radiosettings@lte", true)});
            logcatUtil1.start();
            IpTablesUtil.banAllAppsButOC(true, getContext());
            mobileNetworkUtil.switchWifiOnOff(true);
            mobileNetworkUtil.switchWifiOnOff(false);
            TestUtil.sleep(10 * 1000);
            logger.info(LTE_LABEL);
            HttpRequest request = createRequest().setMethod(HttpGet.METHOD_NAME).setUri(uri).getRequest();
            sendRequest2(request, false, false);
            logger.info(SWITCH_OFF_SCREEN_LABEL);
            ScreenUtils.screenOff();
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

            assertTrue("There should be difference between timestamps less then 1 sec", logEntry.getTimestamp() - lew.getTimestamp() < 1000);
        } finally {
            IpTablesUtil.banAllAppsButOC(false, getContext());
            logcatUtil1.stop();
            ScreenUtils.screenOn();
            ScreenUtils.finishScreenSpy(getContext(), spy);
            PMSUtil.cleanPaths(new String[] {"@asimov@reporting@radiosettings@lte"});
        }
    }

    /**
     * <p>Verify promotion for fixed mode</p>
     * <p>Steps:</p>
     * <ul>
     *     <li>Add policy "dormancy" with value "0" on "@asimov@reporting@radiosettings@lte"</li>
     *     <li>Start LogcatUtil</li>
     *     <li>Block all applications traffic without OC</li>
     *     <li>Write LTE_LABEL and send one request</li>
     *     <li>Turn screen off and wait some time</li>
     *     <li>Turn screen on, send request and wait some time</li>
     *     <li>Stop LogcatUtil</li>
     * </ul>
     * <p>Expected results:</p>
     * <ul>
     *     <li>After LTE_LABEL should be DATA_ACTIVITY_CONNECTED notification</li>
     *     <li>Radio state should be RRC_CONNECTED</li>
     *     <li>After screen off should be DATA_ACTIVITY_NONE notification</li>
     *     <li>Radio state should be RRC_IDLE</li>
     *     <li>Time difference between last two events should be near dormancy timer value</li>
     *     <li>After sending request should be DATA_ACTIVITY_CONNECTED notification and
     *     radio state should be RRC_CONNECTED</li>
     * </ul>
     * @throws Exception
     */
    public void test_003_LTE_ST() throws Exception {
        LogEntryWrapper lew1;
        LogEntryWrapper lew2;
        String resource = "asimov_lte_st_03";
        String uri = createTestResourceUri(resource);
        MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());
        final String LTE_LABEL = "LTE_LABEL" + System.currentTimeMillis();
        final String SWITCH_OFF_SCREEN_LABEL = "SWITCH_OFF_SCREEN_LABEL" + System.currentTimeMillis();
        LogLabelTask logLabelTask1 = new LogLabelTask(RadioLteTransitionsFixedModeTests.class.getSimpleName(), LTE_LABEL);
        LogLabelTask logLabelTask2 = new LogLabelTask(RadioLteTransitionsFixedModeTests.class.getSimpleName(), SWITCH_OFF_SCREEN_LABEL);
        RadiologTask radiologTask = new RadiologTask();
        DataActivityTrackerNotificationTask dataActivityTrackerNotificationTask1 =
                new DataActivityTrackerNotificationTask("DATA_ACTIVITY_CONNECTED");
        DataActivityTrackerNotificationTask dataActivityTrackerNotificationTask2 =
                new DataActivityTrackerNotificationTask("DATA_ACTIVITY_NONE");
        LogcatUtil logcatUtil1 = new LogcatUtil(getContext(), logLabelTask1, logLabelTask2, radiologTask,
                dataActivityTrackerNotificationTask1, dataActivityTrackerNotificationTask2);
        ScreenUtils.ScreenSpyResult spy = ScreenUtils.switchScreenAndSpy(getContext(), false);

        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy("dormancy", "0", "@asimov@reporting@radiosettings@lte", true)});
            logcatUtil1.start();
            IpTablesUtil.banAllAppsButOC(true, getContext());
            mobileNetworkUtil.switchWifiOnOff(true);
            mobileNetworkUtil.switchWifiOnOff(false);
            TestUtil.sleep(10 * 1000);
            logger.info(LTE_LABEL);
            HttpRequest request = createRequest().setMethod(HttpGet.METHOD_NAME).setUri(uri).getRequest();
            sendRequest2(request, false, false);
            logger.info(SWITCH_OFF_SCREEN_LABEL);
            ScreenUtils.screenOff();
            TestUtil.sleep(20 * 1000);
            ScreenUtils.screenOn();
            sendRequest2(request, false, false);
            TestUtil.sleep(10 * 1000);
            logcatUtil1.stop();

            logcatUtil1.logTasks();

            logEntry = null;
            logEntry = LogcatChecks.checkLogEntryExist(logLabelTask1, logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(dataActivityTrackerNotificationTask1, logEntry);
            logEntry = LogcatChecks.checkRadioLog(radiologTask, RadioStateType.rrc_connected, logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(logLabelTask2, logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(dataActivityTrackerNotificationTask2, logEntry);
            lew1 = logEntry;
            logEntry = LogcatChecks.checkRadioLog(radiologTask, RadioStateType.rrc_idle, logEntry);
            lew2 = logEntry;
            logEntry = LogcatChecks.checkLogEntryExist(dataActivityTrackerNotificationTask1, logEntry);
            logEntry = LogcatChecks.checkRadioLog(radiologTask, RadioStateType.rrc_connected, logEntry);

            assertTrue("There should be difference between timestamps less than 1 sec", lew2.getTimestamp() - lew1.getTimestamp() < 1000);
        } finally {
            IpTablesUtil.banAllAppsButOC(true, getContext());
            logcatUtil1.stop();
            ScreenUtils.screenOn();
            ScreenUtils.finishScreenSpy(getContext(), spy);
            PMSUtil.cleanPaths(new String[] {"@asimov@reporting@radiosettings@lte"});
        }
    }

    /**
     <p>Verification of disable ability </p>
     * <p>Steps:</p>
     * <ul>
     *     <li>Disable lte logging</li>
     *     <li>Send request</li>
     *     <li>Turn screen off and wait some time</li>
     * </ul>
     * <p>Expected results:</p>
     * <ul>
     *     <li>There shouldn't be LTE states in radiologs</li>
     * </ul>
     * @throws Exception
     */
    public void test_009_LTE_ST() throws Exception {
        String resource = "asimov_lte_st_9";
        String uri = createTestResourceUri(resource);
        ScreenUtils.ScreenSpyResult spy = ScreenUtils.switchScreenAndSpy(getContext(), false);
        List<RadioLogEntry> radioLogEntryList;
        RadiologTask radiologTask = new RadiologTask();
        LogcatUtil logcatUtil1 = new LogcatUtil(getContext(), radiologTask);
        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy("enabled", "false", "@asimov@reporting@radiosettings@lte", true)});
            logcatUtil1.start();
            TestUtil.sleep(10 * 1000);
            HttpRequest request = createRequest().setMethod(HttpGet.METHOD_NAME).setUri(uri).getRequest();
            checkMiss(request, 1);
            ScreenUtils.screenOff();
            TestUtil.sleep(60 * 1000);
            logcatUtil1.stop();

            radioLogEntryList = radiologTask.getLogEntries();

            for (RadioLogEntry r : radioLogEntryList) {
                assertFalse("There shouldn't be LTE radio tracking notices", r.getCurrentState().equals(RadioStateType.rrc_connected) || r.getCurrentState().equals(RadioStateType.rrc_idle));
            }

        } finally {
            ScreenUtils.screenOn();
            ScreenUtils.finishScreenSpy(getContext(), spy);
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy("enabled", "true", "@asimov@reporting@radiosettings@lte", true)});
            PMSUtil.cleanPaths(new String[] {"@asimov@reporting@radiosettings@lte"});
        }
    }

    /**
     <p>LTE state tracker in case of switching interface to wifi</p>
     * <p>Steps:</p>
     * <ul>
     *     <li>Send request</li>
     *     <li>Switch interface to wifi</li>
     * </ul>
     * <p>Expected results:</p>
     * <ul>
     *     <li>After switching to wifi state changed to rrc_idle</li>
     * </ul>
     * @throws Exception
     */
    public void test_010_LTE_ST() throws Exception {
        String resource = "asimov_lte_st_10";
        String uri = createTestResourceUri(resource);
        final String WIFI_LABEL = "WIFI_LABEL" + System.currentTimeMillis();
        LogLabelTask logLabelTask1 = new LogLabelTask(RadioLteTransitionsFixedModeTests.class.getSimpleName(), WIFI_LABEL);
        MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());
        RadiologTask radiologTask = new RadiologTask();
        LogcatUtil logcatUtil1 = new LogcatUtil(getContext(), logLabelTask1, radiologTask);
        try {
            logcatUtil1.start();
            TestUtil.sleep(30 * 1000);
            HttpRequest request = createRequest().setMethod(HttpGet.METHOD_NAME).setUri(uri).getRequest();
            sendRequest2(request, false, false);
            TestUtil.sleep(5 * 1000);
            logger.info(WIFI_LABEL);
            mobileNetworkUtil.onWifiOnly();
            TestUtil.sleep(30 * 1000);

            logcatUtil1.stop();

            logEntry = null;
            logEntry = LogcatChecks.checkRadioLog(radiologTask, RadioStateType.rrc_connected, logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(logLabelTask1, logEntry);
            logEntry = LogcatChecks.checkRadioLog(radiologTask, RadioStateType.rrc_idle, logEntry);

        } finally {
            logcatUtil1.stop();
        }
    }

    /**
     <p>LTE tracker in airplane mode</p>
     * <p>Steps:</p>
     * <ul>
     *     <li>Send request</li>
     *     <li>Switch interface to wifi</li>
     * </ul>
     * <p>Expected results:</p>
     * <ul>
     *     <li>After switching to wifi state changed to rrc_idle</li>
     * </ul>
     * @throws Exception
     */
    public void test_012_LTE_ST() throws Exception {
        String resource = "asimov_lte_st_12";
        AirplaneModeUtil airplaneModeUtil = new AirplaneModeUtil(getContext());
        String uri = createTestResourceUri(resource);
        LogLabelTask logLabelTask1 = new LogLabelTask(AirplaneModeUtil.class.getSimpleName(), "AIRPLANE_MODE_ON");
        RadiologTask radiologTask = new RadiologTask();
        LogcatUtil logcatUtil1 = new LogcatUtil(getContext(), logLabelTask1, radiologTask);
        MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());

        try {
            logcatUtil1.start();
            TestUtil.sleep(30 * 1000);
            mobileNetworkUtil.switchWifiOnOff(true);
            mobileNetworkUtil.switchWifiOnOff(false);
            TestUtil.sleep(30 * 1000);
            HttpRequest request = createRequest().setMethod(HttpGet.METHOD_NAME).setUri(uri).getRequest();
            TestUtil.sleep(5 * 1000);
            sendRequest2(request, false, false);
            airplaneModeUtil.setEnabled(true);
            TestUtil.sleep(30 * 1000);

            logcatUtil1.stop();

            logEntry = null;
            logEntry = LogcatChecks.checkRadioLog(radiologTask, RadioStateType.rrc_connected, logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(logLabelTask1, logEntry);
            logEntry = LogcatChecks.checkRadioLog(radiologTask, RadioStateType.rrc_idle, logEntry);
        } finally {
            logcatUtil1.stop();
            airplaneModeUtil.setEnabled(false);
        }
    }
}
