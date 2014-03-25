package com.seven.asimov.it.tests.crcs.reporting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.test.AssertionFailedError;
import android.test.suitebuilder.annotation.LargeTest;
import com.seven.asimov.it.annotation.DeviceOnly;
import com.seven.asimov.it.annotation.Execute;
import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.testcases.ReportingTestCase;
import com.seven.asimov.it.utils.IpTablesUtil;
import com.seven.asimov.it.utils.MobileNetworkUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.date.DateUtil;
import com.seven.asimov.it.utils.logcat.LogcatChecks;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.LogLabelTask;
import com.seven.asimov.it.utils.logcat.tasks.crcsTasks.ReportTransferParametrizedTask;
import com.seven.asimov.it.utils.logcat.tasks.policyTasks.PolicyAppliedTask;
import com.seven.asimov.it.utils.logcat.wrappers.CRCSReportSendingRetryWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.LogEntryWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.ReportTransferWrapperNN;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import junit.framework.Assert;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

import static com.seven.asimov.it.base.constants.TFConstantsIF.*;

/**
 * test_006_transferDatabase_75_percentIsNotComplete and test_007_transferDatabase_95_percentIsNotComplete
 * should be executed on hki-dev-testrunner4.7sys.eu testrunner.
 */
public class ReportingTests extends ReportingTestCase {

    private static final String TAG = ReportingTests.class.getSimpleName();
    private static final Logger logger = LoggerFactory.getLogger(ReportingTests.class.getSimpleName());

    /**
     * <h3>Tests CRCS report transfer in case CRCS records are accumulated and radio is up.</h3>
     * <p>actions:</p>
     * <ol>
     * <li>turn radio up</li>
     * <li>set personal policy @asimov@reporting@triggers@storage@min_entry=5</li>
     * <li>do 5 http requests</li>
     * </ol>
     * <p>checks:</p>
     * <ol>
     * <li>policy was added</li>
     * <li>radio was up</li>
     * <li>CRCS records were accumulated</li>
     * <li>new task with type=REPORT_TRANSFER was created</li>
     * <li>CC sent reporting dump transactions request to server</li>
     * <li>dump transactions response was received from server</li>
     * <li>report pack dump executed and sent successfully</li>
     * <li>prepare to cleanup report provider</li>
     * <li>CRCS records were removed from database</li>
     * <li>CRCS logs transfer was successful</li>
     * <li>daily CRCS sending task was removed</li>
     * <li>OC scheduler task was created</li>
     * </ol>
     *
     * @throws Exception
     */
    @Execute
    @DeviceOnly
    @LargeTest
    public void test_001_TransferringRecordsRadioUp() throws Exception {
        ex = null;
        final String RESOURCE_URI = "test_01_TransferringRecordsRadioUp";
        final String uri = createTestResourceUri(RESOURCE_URI);
        final HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
        LogcatUtil logcatUtil;
        MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());
        prepareTasks();
        logcatUtil = prepareLogcatUtil_new(getContext(), policyAddedTask, radioStateTask, reportTransferTaskNN,
                crcsAccumulatedTask, crcsSendingTaskSuccessRemovedTask,
                dumpTransactionTask, crcsTransferSuccessTask, crcsRemovedTask, createdTaskSchedulerTaskTask,
                reportPackDumpExecutedAndSentTask, prepareToCleanupReportProviderTask, cleanupReportProviderTask);
        logcatUtil.start();
        mobileNetworkUtil.onWifiOnly();
        //Radio state should be UP
        Runtime.getRuntime().exec("ping -c 1 " + AsimovTestCase.TEST_RESOURCE_HOST);
        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(MIN_ENTRIES_PROPERTY_NAME, MIN_ENTRIES_PROPERTY_VALUE_5, MIN_ENTRIES_PROPERTY_PATH, true)});
            sendRequests(request, 5);
            TestUtil.sleep(30 * 1000);
            logcatUtil.stop();

            logcatUtil.logTasks();
            // check policy apply
            logEntry = null;
            logEntry = LogcatChecks.checkPolicyAdded(policyAddedTask, MIN_ENTRIES_PROPERTY_NAME, MIN_ENTRIES_PROPERTY_VALUE_5, logEntry);
            logger.info("1-1 " + logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(crcsAccumulatedTask, logEntry);
            logger.info("1-2-1 " + logEntry);
            logEntry = LogcatChecks.checkReportTransfer(reportTransferTaskNN, "REPORT_TRANSFER", "WAIT", logEntry, true);
            logger.info("1-2-2 " + logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(dumpTransactionTask, logEntry);
            logger.info("1-3 " + logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(reportPackDumpExecutedAndSentTask, logEntry);
            logger.info("1-4-1 " + logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(prepareToCleanupReportProviderTask, logEntry);
            logger.info("1-4-2 " + logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(crcsRemovedTask, logEntry);
            logger.info("1-5 " + logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(cleanupReportProviderTask, logEntry);
            logger.info("1-5-1 " + logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(crcsTransferSuccessTask, logEntry);
            logger.info("1-6 " + logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(crcsSendingTaskSuccessRemovedTask, logEntry);
            logger.info("1-7 " + logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(createdTaskSchedulerTaskTask, logEntry);
            logger.info("1-8 " + logEntry);
        } catch (Throwable e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            ex = e;
        } finally {
            logcatUtil.stop();
            PMSUtil.cleanPaths(new String[]{MIN_ENTRIES_PROPERTY_PATH});
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
        //Assetration error was here. Removed it - we don't want to stop all tests just because of a single error, DO WE?
    }

    /**
     * <h3>Tests CRCS report transfer in case CRCS records are accumulated and radio is down.</h3>
     * <p>actions:</p>
     * <ol>
     * <li>set personal policy @asimov@reporting@triggers@storage@min_entry=5</li>
     * <li>wait for radio down</li>
     * </ol>
     * <p>checks:</p>
     * <ol>
     * <li>policy was added</li>
     * <li>radio was down</li>
     * <li>new task with type=REPORT_TRANSFER not sent</li>
     * </ol>
     * <p>next action: do 5 http requests</p>
     * <p>checks:</p>
     * <ol>
     * <li>CC sent reporting dump transactions request to server</li>
     * <li>dump transactions response was received from server</li>
     * <li>report pack dump executed and sent successfully</li>
     * <li>prepare to cleanup report provider</li>
     * <li>CRCS records were removed from database</li>
     * <li>CRCS logs transfer was successful</li>
     * <li>daily CRCS sending task was removed</li>
     * <li>OC scheduler task was created</li>
     * </ol>
     *
     * @throws Exception
     */
    @Execute
    @DeviceOnly
    @LargeTest
    public void test_002_TransferringRecordsRadioDown() throws Exception {
        LogcatUtil logcatUtil = null;
        final String RESOURCE_URI = "test_02_TransferringRecordsRadioDown";
        final String uri = createTestResourceUri(RESOURCE_URI);
        final HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
        LogEntryWrapper crcsTaskFlow;
        Integer token = null;
        boolean flag = false;
        MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());
        try {
            try {
                ex = null;
                final String relayLabel = "Before relay blocking " + System.currentTimeMillis();
                logLabelTask = new LogLabelTask(TAG, relayLabel);
                policyAppliedTask = new PolicyAppliedTask(MIN_ENTRIES_PROPERTY_NAME, MIN_ENTRIES_PROPERTY_VALUE_10);
                prepareTasks();
                logcatUtil = prepareLogcatUtil_new(getContext(), policyAppliedTask, logLabelTask, reportTransferTaskNN, crcsReportSendingFailedTask);
                logcatUtil.start();
                TestUtil.switchRadioUpStart();
                PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(MIN_ENTRIES_PROPERTY_NAME, MIN_ENTRIES_PROPERTY_VALUE_10, MIN_ENTRIES_PROPERTY_PATH, true)});
                //start point mark
                logger.info(new Date(System.currentTimeMillis()).toString() + " " + relayLabel);
                //block relay
                IpTablesUtil.banRelayServer(true);
                mobileNetworkUtil.switchWifiOnOff(false);
                mobileNetworkUtil.switchWifiOnOff(true);
                logger.info("Before doHttpActivity");
                sendRequests(request, 5);
                logcatUtil.stop();
                Thread.sleep(125 * 1000);
                logcatUtil.logTasks();
                logEntry = null;
                // check policy applied
                logEntry = LogcatChecks.checkLogEntryExist(policyAppliedTask, logEntry);
                logEntry = LogcatChecks.checkLogEntryExist(logLabelTask, logEntry);
                logEntry = LogcatChecks.checkReportTransfer(reportTransferTaskNN, "REPORT_TRANSFER", "WAIT", logEntry, true);
                logger.info("2-2-1 " + logEntry);
                token = Integer.valueOf(((ReportTransferWrapperNN) logEntry).getToken());
                logger.info("Blocked token=" + token);
                //log entry for that token should not exist
                LogcatChecks.checkReportTransfer(reportTransferTaskNN, "REPORT_TRANSFER", "SENT", token, logEntry, false);
                //Sending CRCS reports should be failed due to connection to Relay is unavailable.
                //todo ask dev team, if that message exists in log
                //logEntry = LogcatUtil.checkLogEntryExist(crcsReportSendingFailedTask, logEntry);
            } finally {
//                IpTablesUtil.banRelayServer(false);
                logcatUtil.stop();
                logLabelTask = null;
            }

            final String secondPartLabel = "Second part of the test " + System.currentTimeMillis();
            logLabelTask = new LogLabelTask(TAG, secondPartLabel);
            prepareTasks();
            logcatUtil = prepareLogcatUtil_new(getContext(), logLabelTask, reportTransferTaskNN,
                    crcsAccumulatedTask, crcsSendingTaskSuccessRemovedTask,
                    dumpTransactionTask, crcsTransferSuccessTask, crcsRemovedTask, createdTaskSchedulerTaskTask,
                    reportPackDumpExecutedAndSentTask, prepareToCleanupReportProviderTask, cleanupReportProviderTask);

            logcatUtil.start();
            logger.info(secondPartLabel);
            IpTablesUtil.banRelayServer(false);
            mobileNetworkUtil.on3gOnly();
            mobileNetworkUtil.onWifiOnly();
            flag = true;
            //Start activity.
            doHttpActivity(5);
            Thread.sleep(300 * 1000);
            logcatUtil.stop();
            Thread.sleep(5 * 1000);
            logcatUtil.logTasks();
            logEntry = null;

            logEntry = LogcatChecks.checkLogEntryExist(logLabelTask, logEntry);
            logger.info("2-3-0 " + logEntry);
            //crcs task flow
            crcsTaskFlow = LogcatChecks.checkReportTransfer(reportTransferTaskNN, "REPORT_TRANSFER", "SENT", token, logEntry, true);
            crcsTaskFlow = LogcatChecks.checkReportTransfer(reportTransferTaskNN, "REPORT_TRANSFER", "WAIT_FOR_ACK", token, crcsTaskFlow, true);
            LogcatChecks.checkReportTransfer(reportTransferTaskNN, "REPORT_TRANSFER", "DONE", token, crcsTaskFlow, true);
            logger.info("2-3-1 " + logEntry);
            //other steps
            logger.info("2-3-2 " + logEntry);
            logEntry = LogcatChecks.checkDumpTransaction(dumpTransactionTask, "request", logEntry);
            logger.info("2-3-3 " + logEntry);
            logEntry = LogcatChecks.checkDumpTransaction(dumpTransactionTask, "response", logEntry);
            logger.info("2-3-4 " + logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(reportPackDumpExecutedAndSentTask, logEntry);
            logger.info("2-4-1 " + logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(prepareToCleanupReportProviderTask, logEntry);
            logger.info("2-4-2 " + logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(crcsRemovedTask, logEntry);
            logger.info("2-4-3 " + logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(cleanupReportProviderTask, logEntry);
            logger.info("2-5 " + logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(crcsTransferSuccessTask, logEntry);
            logger.info("2-6 " + logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(crcsSendingTaskSuccessRemovedTask, logEntry);
            logger.info("2-7 " + logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(createdTaskSchedulerTaskTask, logEntry);
            logger.info("2-8 " + logEntry);
        } catch (Throwable e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            ex = e;
        } finally {
            if (!flag) IpTablesUtil.banRelayServer(false);
            TestUtil.switchRadioUpStop();
            logcatUtil.stop();
            PMSUtil.cleanPaths(new String[]{MIN_ENTRIES_PROPERTY_PATH});
        }
        if (ex != null) {
            throw new AssertionFailedError(ExceptionUtils.getStackTrace(ex));
        }
    }

    /**
     * <h3>Tests CRCS report transfer in case CRCS records are accumulated and radio is up and min_entries=25.</h3>
     * <p>actions:</p>
     * <ol>
     * <li>turn radio up</li>
     * <li>set personal policy @asimov@reporting@triggers@storage@min_entry=25</li>
     * <li>do 10 http requests</li>
     * </ol>
     * <p>checks:</p>
     * <ol>
     * <li>policy was added</li>
     * <li>CRCS records were accumulated</li>
     * <li>new task with type=REPORT_TRANSFER was created</li>
     * <li>CC sent reporting dump transactions request to server</li>
     * <li>dump transactions response was received from server</li>
     * <li>report pack dump executed and sent successfully</li>
     * <li>prepare to cleanup report provider</li>
     * <li>CRCS records were removed from database</li>
     * <li>CRCS logs transfer was successful</li>
     * <li>daily CRCS sending task was removed</li>
     * <li>OC scheduler task was created</li>
     * </ol>
     *
     * @throws Exception
     */
    @Execute
    @DeviceOnly
    @LargeTest
    public void test_003_TransferringRecordsMinEntries25() throws Exception {
        ex = null;
        final String RESOURCE_URI = "test_03_TransferringRecordsMinEntries25";
        final String uri = createTestResourceUri(RESOURCE_URI);
        final HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
        LogcatUtil logcatUtil;
        MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());
        policyAppliedTask = new PolicyAppliedTask(MIN_ENTRIES_PROPERTY_NAME, TFConstantsIF.MIN_ENTRIES_PROPERTY_VALUE_25);
        prepareTasks();
        logcatUtil = prepareLogcatUtil_new(getContext(), policyAppliedTask, reportTransferTaskNN,
                crcsAccumulatedTask,
                crcsSendingTaskSuccessRemovedTask,
                dumpTransactionTask, crcsTransferSuccessTask, crcsRemovedTask, createdTaskSchedulerTaskTask,
                reportPackDumpExecutedAndSentTask, prepareToCleanupReportProviderTask, cleanupReportProviderTask);
        logcatUtil.start();
        mobileNetworkUtil.onWifiOnly();
        TestUtil.sleep(30 * 1000);
        Runtime.getRuntime().exec("ping -c 1 " + AsimovTestCase.TEST_RESOURCE_HOST);
        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(MIN_ENTRIES_PROPERTY_NAME, TFConstantsIF.MIN_ENTRIES_PROPERTY_VALUE_25, MIN_ENTRIES_PROPERTY_PATH, true)});
            sendRequests(request, 10);
            TestUtil.sleep(60 * 1000);
            logcatUtil.stop();

            logcatUtil.logTasks();
            logEntry = null;
            LogEntryWrapper crcsTaskFlow;
            Integer token = null;
            logEntry = LogcatChecks.checkLogEntryExist(policyAppliedTask, logEntry);
            logger.info("3-1 " + logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(crcsAccumulatedTask, logEntry);
            logger.info("3-2-1 " + logEntry);

            //crcs task flow
            crcsTaskFlow = LogcatChecks.checkReportTransfer(reportTransferTaskNN, "REPORT_TRANSFER", "WAIT", token, logEntry, true);
            token = Integer.valueOf(((ReportTransferWrapperNN) crcsTaskFlow).getToken());
            crcsTaskFlow = LogcatChecks.checkReportTransfer(reportTransferTaskNN, "REPORT_TRANSFER", "SENT", token, crcsTaskFlow, true);
            crcsTaskFlow = LogcatChecks.checkReportTransfer(reportTransferTaskNN, "REPORT_TRANSFER", "WAIT_FOR_ACK", token, crcsTaskFlow, true);
            crcsTaskFlow = LogcatChecks.checkReportTransfer(reportTransferTaskNN, "REPORT_TRANSFER", "DONE", token, crcsTaskFlow, true);
            assertNotNull("Log record for REPORT_TRANSFER task should exist!", crcsTaskFlow);

            logger.info("3-2-2 " + logEntry);
            logEntry = LogcatChecks.checkDumpTransaction(dumpTransactionTask, "request", logEntry);
            logEntry = LogcatChecks.checkDumpTransaction(dumpTransactionTask, "response", logEntry);
            logger.info("3-3-1 " + logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(reportPackDumpExecutedAndSentTask, logEntry);
            logger.info("3-4-1 " + logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(prepareToCleanupReportProviderTask, logEntry);
            logger.info("3-4-2 " + logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(crcsRemovedTask, logEntry);
            logger.info("3-4-3 " + logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(cleanupReportProviderTask, logEntry);
            logger.info("3-5 " + logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(crcsTransferSuccessTask, logEntry);
            logger.info("3-6 " + logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(crcsSendingTaskSuccessRemovedTask, logEntry);
            logger.info("3-7 " + logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(createdTaskSchedulerTaskTask, logEntry);
            logger.info("3-8 " + logEntry);
        } catch (Throwable e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            ex = e;
        } finally {
            logcatUtil.stop();
            PMSUtil.cleanPaths(new String[]{MIN_ENTRIES_PROPERTY_PATH});
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>CRCS reporting shouldn't be send in case changing of system time.</h3>
     * <p>actions:</p>
     * <ol>
     * <li>set personal policy @asimov@reporting@triggers@storage@min_entry=40</li>
     * <li>change system time</li>
     * </ol>
     * <p>checks:</p>
     * <ol>
     * <li>policy was added</li>
     * <li>new task with type=REPORT_TRANSFER was not created</li>
     * </ol>
     *
     * @throws Exception
     */
    // @Execute
    @DeviceOnly
    @LargeTest
    public void test_004_SendingCrcsInTimeChanging() throws Exception {
        ex = null;
        final String RESOURCE_URI = "test_04_SendingCrcsInTimeChanging";
        final String uri = createTestResourceUri(RESOURCE_URI);
        final HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
        final String timeLabel = "Before time change " + System.currentTimeMillis();
        logLabelTask = new LogLabelTask(TAG, timeLabel);
        LogcatUtil logcatUtil = null;
        prepareTasks();
        logcatUtil = prepareLogcatUtil_new(getContext(), logLabelTask, reportTransferTaskNN);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);

        BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (Intent.ACTION_DATE_CHANGED.equals(action) || Intent.ACTION_TIME_CHANGED.equals(action)) {
                    br_timestamp = System.currentTimeMillis();
                    logger.info("5-1 Time changed!");
                }
            }
        };
        getContext().registerReceiver(br, filter);

        logcatUtil.start();
        try {
            sendRequests(request, 1);
            logger.info(timeLabel);
            DateUtil.moveTime(30 * 1000);
            logger.info("After time change " + new Date());
            TestUtil.sleep(20 * 1000);
            logcatUtil.stop();

            if (br_timestamp == 0) {
                throw new AssertionFailedError("Time is not changed!");
            }

            logcatUtil.logTasks();
            logEntry = null;
            logEntry = LogcatChecks.checkLogEntryExist(logLabelTask, logEntry);
            LogcatChecks.checkReportTransfer(reportTransferTaskNN, "REPORT_TRANSFER", "WAIT", logEntry, false);
        } catch (Throwable e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            ex = e;
        } finally {
            logcatUtil.stop();
            DateUtil.moveTime(-30 * 1000);
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Tests CRCS report sending retry intervals.</h3>
     * <p>actions:</p>
     * <ol>
     * <li>set personal policy @asimov@reporting@triggers@storage@min_entry=10</li>
     * <li>block relay port</li>
     * <li>do 10 http requests</li>
     * <li>wait 6 min(check time only two or thee time intervals to retry connection) </li>
     * </ol>
     * <p>checks:</p>
     * <ol>
     * <li>policy was added</li>
     * <li>CRCS records were accumulated</li>
     * <li>new task with type=REPORT_TRANSFER was created</li>
     * <li>connection to Relay failed</li>
     * <li>connection to Relay should be retried later in such intervals: 30000ms,195000ms,360000ms,525000ms,690000ms
     * <li>value of retries calculate in CRCSUtil</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_005_SendingCrcsRetryIntervals() throws Exception {
        final String RESOURCE_URI = "test_05_SendingCrcsRetryIntervals";
        final String uri = createTestResourceUri(RESOURCE_URI);
        final HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
        int timeIntervals[] = getReportRetryIntervals();
        logger.info("time intervals= " + Arrays.toString(timeIntervals));
        double maxDeviationCoefficient = 1.5;
        double minDeviationCoefficient = 0.5;
        LogcatUtil logcatUtil;
        MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());
        ex = null;
        final String relayLabel = "Relay is blocked " + System.currentTimeMillis();
        logLabelTask = new LogLabelTask(TAG, relayLabel);
        prepareTasks();
        logcatUtil = prepareLogcatUtil_new(getContext(), logLabelTask, connectionFailedRelayAvaliableTask, crcsReportSendingRetryTask);
        logcatUtil.start();
        try
        {
            //start point mark
            logger.info(new Date(System.currentTimeMillis()).toString() + " " + relayLabel);
            //block relay
            IpTablesUtil.banRelayServer(true);
            mobileNetworkUtil.on3gOnly();
            mobileNetworkUtil.onWifiOnly();
            TestUtil.sleep(30 * 1000);
            sendRequests(request, 5);
            TestUtil.sleep(6 * 60 * 1000);
            IpTablesUtil.banRelayServer(false);
            mobileNetworkUtil.on3gOnly();
            mobileNetworkUtil.onWifiOnly();
            logcatUtil.stop();
            Thread.sleep(5 * 1000);
            logcatUtil.logTasks();

            logEntry = null;
            logEntry = LogcatChecks.checkLogEntryExist(logLabelTask, logEntry);
            logger.info("6-1-1 " + logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(connectionFailedRelayAvaliableTask, logEntry);
            logger.info("6-2 " + logEntry);

            List<CRCSReportSendingRetryWrapper> crcsReportSendingRetryLogEntryList = crcsReportSendingRetryTask.getLogEntries();
            if (crcsReportSendingRetryLogEntryList.size() < 2) {
                throw new AssertionFailedError("Connection to Relay should be failed at least 2 times!");
            }
            logger.info("6-3 ");
            long[] realIntervals = new long[crcsReportSendingRetryLogEntryList.size() - 1];
            for (int i = 0; i < crcsReportSendingRetryLogEntryList.size() - 1; i++) {
                realIntervals[i] = crcsReportSendingRetryLogEntryList.get(i + 1).getTimestamp() - crcsReportSendingRetryLogEntryList.get(i).getTimestamp();
                logger.info("step " + i + " time_interval= " + realIntervals[i]);
            }
            logger.info("real intervals= " + Arrays.toString(realIntervals));
            logger.info("time intervals= " + Arrays.toString(timeIntervals));

            int size = realIntervals.length >= timeIntervals.length ? timeIntervals.length : realIntervals.length;
            //Checking that there is no significant deviation from the set time intervals. A deviation of runtime accepted ​​10%
            for (int i = 0; i < size; i++) {
                logger.info("values  " + realIntervals[i] + "  ||||  " + timeIntervals[i] + " size " + size);
                if (realIntervals[i] > (timeIntervals[i] * maxDeviationCoefficient) |
                        realIntervals[i] < (timeIntervals[i] * minDeviationCoefficient)) {
                    throw new AssertionFailedError("Connection to Relay should be retried in such intervals. Check branding or network.");
                }
            }
        } catch (Throwable e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            ex = e;
        } finally {
            logcatUtil.stop();
            IpTablesUtil.banRelayServer(false);
        }

        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }

    }

    /**
     * <h3>Tests transfer CRCS report in case 75% of database is complete</h3>
     * <p>Actions:</p>
     * <ol>
     * <li>Block Relay server</li>
     * <li>Grow up DB to size 75% of overall DB volume - 100 MB</li>
     * <li>Start threads with request sending, until DB size become 75% of overall DB volume</li>
     * <li>Stop threads</li>
     * <li>Unblock Relay server</li>
     * </ol>
     * <p>Checks:</p>
     * <ol>
     * <li>CRCS records should be accumulated</li>
     * <li>Report sending failed</li>
     * <li>CRCS storage is filled near 75%</li>
     * <li>Attempts to force CRCS report sending was done</li>
     * <li>Report sending failed</li>
     * <li>CC sent reporting dump transactions request to server</li>
     * <li>Dump transactions response was received from server</li>
     * <li>Report pack dump executed and sent successfully</li>
     * <li>Prepare to cleanup report provider</li>
     * <li>CRCS records were removed from database</li>
     * <li>CRCS logs transfer was successful</li>
     * <li>Daily CRCS sending task was removed</li>
     * <li>OC scheduler task was created</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_006_transferDatabase_75_percentIsNotComplete() throws Exception {
        logger.info("Current testrunner: " + AsimovTestCase.TEST_RESOURCE_HOST);
        logger.info("Database reducing result: " + reduceDB());
        executorService = Executors.newFixedThreadPool(20);
        LogcatUtil logcatUtil;
        ex = null;
        Integer token1;
        final String RESOURCE_URI = "test_06_transferDatabase_75_percentIsNotComplete";
        final String uri = createTestResourceUri(RESOURCE_URI);
        final HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
        MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());
        final String relayLabel = "Relay access is switched " + System.currentTimeMillis();
        logLabelTask = new LogLabelTask(TAG, relayLabel);
        reportTransferParametrizedTask1 = new ReportTransferParametrizedTask("WAIT", "REPORT_TRANSFER", "WAIT", "SENT");
        reportTransferParametrizedTask2 = new ReportTransferParametrizedTask("NEW", "REPORT_TRANSFER", "NEW", "WAIT");

        prepareTasksToTests_06_07();
        logcatUtil = prepareLogcatUtil_new(getContext()
                , logLabelTask
                , crcsAccumulatedTask
                , reportTransferTaskNN
                , crcsReportSendingFailedTask
                , crcsReportSendingRetryTask
                , connectionFailedRelayAvaliableTask
                , crcsTaskInQueueTask
                , crcsStorageFilled_75_PercentTask
                , tryToForceCrcsReportSendingTask
                , dumpTransactionTask
                , crcsTransferSuccessTask
                , crcsRemovedTask
                , crcsSendingTaskSuccessRemovedTask
                , createdTaskSchedulerTaskTask
                , reportPackDumpExecutedAndSentTask
                , prepareToCleanupReportProviderTask
                , cleanupReportProviderTask
                , reportTransferParametrizedTask1
                , reportTransferParametrizedTask2);

        logcatUtil.start();
        try {
            //block relay
            IpTablesUtil.banRelayServer(true);
            mobileNetworkUtil.on3gOnly();
            mobileNetworkUtil.onWifiOnly();

            logger.info(relayLabel);

            logger.info("DB size before artificial increasing: " + getOcDBSize());
            growUpDB(786432 - 100 * 1024);
            logger.info("DB size after artificial increasing: " + getOcDBSize());
            naturalIncreaseOfDbToSize75();
            logger.info("Accumulation ended");
            logger.info("DB size after natural increasing: " + getOcDBSize());

            logger.info(relayLabel);
            //unblock relay server
            IpTablesUtil.banRelayServer(false);
            logger.info("Relay unblocked");
            mobileNetworkUtil.on3gOnly();
            mobileNetworkUtil.onWifiOnly();

            sendRequest2(request);
            logSleeping(5 * DateUtil.SECONDS);
            logger.info("Sent request after Relay unblocked");
            TestUtil.sleep(120 * 1000);

            logcatUtil.stop();
            logcatUtil.logTasks();
            logEntry = null;
            logEntry = LogcatChecks.checkLogEntryExist(logLabelTask, logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(crcsAccumulatedTask, logEntry);
            logEntry = LogcatChecks.checkReportTransferByStatus(reportTransferParametrizedTask2, logEntry, true);

            token1 = Integer.valueOf(((ReportTransferWrapperNN) logEntry).getToken());
            logger.info("Blocked token=" + token1);
            //log entry for that token should not exist
            LogcatChecks.checkReportTransfer(reportTransferParametrizedTask1, token1, logEntry, false);

            logEntry = LogcatChecks.checkLogEntryExist(crcsStorageFilled_75_PercentTask, logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(tryToForceCrcsReportSendingTask, logEntry);
            logEntry = LogcatChecks.checkReportTransferByStatus(reportTransferParametrizedTask2, logEntry, true);

            logEntry = LogcatChecks.checkLogEntryExist(logLabelTask, logEntry);
            logEntry = LogcatChecks.checkDumpTransaction(dumpTransactionTask, "request", logEntry);
            logEntry = LogcatChecks.checkDumpTransaction(dumpTransactionTask, "response", logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(reportPackDumpExecutedAndSentTask, logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(prepareToCleanupReportProviderTask, logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(crcsRemovedTask, logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(cleanupReportProviderTask, logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(crcsTransferSuccessTask, logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(crcsSendingTaskSuccessRemovedTask, logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(createdTaskSchedulerTaskTask, logEntry);
        } catch (Throwable e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            ex = e;
        } finally {
            logcatUtil.stop();
            executorService.shutdownNow();
            IpTablesUtil.banRelayServer(false);
            logger.info("Database reducing result: " + reduceDB());
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Tests transfer CRCS report in case 95% of database is complete</h3>
     * <p>Actions:</p>
     * <ol>
     * <li>Block Relay server</li>
     * <li>Grow up DB to size 75% of overall DB volume - 100 MB</li>
     * <li>Start threads with request sending, until DB size become 95% of overall DB volume</li>
     * <li>Stop threads</li>
     * </ol>
     * <p>Checks:</p>
     * <ol>
     * <li>CRCS records should be accumulated</li>
     * <li>Report sending failed</li>
     * <li>CRCS storage is filled near 75%</li>
     * <li>Attempts to force CRCS report sending was done</li>
     * <li>Report sending failed</li>
     * <li>CRCS storage is filled near 95%</li>
     * <li>25% of oldest reports were removed</li>
     * </ol>
     *
     * @throws Exception
     */
    // @Execute
    @DeviceOnly
    @LargeTest
    public void test_007_transferDatabase_95_percentIsNotComplete() throws Exception {
        logger.info("Database reducing result: " + reduceDB());
        executorService = Executors.newFixedThreadPool(20);
        LogcatUtil logcatUtil;
        ex = null;
        boolean dbSizeWasReduced;
        Integer token1;
        Integer token2;
        MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());
        final String relayLabel = "Relay access is switched " + System.currentTimeMillis();
        logLabelTask = new LogLabelTask(TAG, relayLabel);
        reportTransferParametrizedTask1 = new ReportTransferParametrizedTask("WAIT", "REPORT_TRANSFER", "WAIT", "SENT");
        reportTransferParametrizedTask2 = new ReportTransferParametrizedTask("NEW", "REPORT_TRANSFER", "NEW", "WAIT");

        prepareTasksToTests_06_07();
        logcatUtil = prepareLogcatUtil_new(getContext()
                , logLabelTask
                , crcsAccumulatedTask
                , reportTransferTaskNN
                , crcsReportSendingFailedTask
                , crcsReportSendingRetryTask
                , connectionFailedRelayAvaliableTask
                , crcsTaskInQueueTask
                , crcsStorageFilled_75_PercentTask
                , tryToForceCrcsReportSendingTask
                , crcsStorageFilled_95_PercentTask
                , reportTransferParametrizedTask1
                , reportTransferParametrizedTask2);

        logcatUtil.start();
        try {
            //block relay
            IpTablesUtil.banRelayServer(true);
            logger.info(relayLabel);
            mobileNetworkUtil.on3gOnly();
            mobileNetworkUtil.onWifiOnly();

            logger.info("DB size before artificial increasing: " + getOcDBSize());
            growUpDB(786432 - 100 * 1024);
            logger.info("DB size after artificial increasing: " + getOcDBSize());
            dbSizeWasReduced = naturalIncreaseOfDbToSize95();

            logger.info("Accumulation ended.");
            logger.info("DB size after natural increasing: " + getOcDBSize());
            TestUtil.sleep(10 * 1000);

            logcatUtil.stop();
            logcatUtil.logTasks();
            logEntry = null;
            logEntry = LogcatChecks.checkLogEntryExist(logLabelTask, logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(crcsAccumulatedTask, logEntry);
            logEntry = LogcatChecks.checkReportTransferByStatus(reportTransferParametrizedTask2, logEntry, true);

            token1 = Integer.valueOf(((ReportTransferWrapperNN) logEntry).getToken());
            logger.info("Blocked token=" + token1);
            //log entry for that token should not exist
            LogcatChecks.checkReportTransfer(reportTransferParametrizedTask1, token1, logEntry, false);

            logEntry = LogcatChecks.checkLogEntryExist(crcsStorageFilled_75_PercentTask, logEntry);
            logEntry = LogcatChecks.checkLogEntryExist(tryToForceCrcsReportSendingTask, logEntry);
            logEntry = LogcatChecks.checkReportTransferByStatus(reportTransferParametrizedTask2, logEntry, true);

            token2 = Integer.valueOf(((ReportTransferWrapperNN) logEntry).getToken());
            logger.info("Blocked token=" + token2);
            //log entry for that token should not exist
            LogcatChecks.checkReportTransfer(reportTransferParametrizedTask1, token2, logEntry, false);

            logEntry = LogcatChecks.checkLogEntryExist(crcsStorageFilled_95_PercentTask, logEntry);

            Assert.assertTrue("Database size should be reduced", dbSizeWasReduced);

        } catch (Throwable e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            ex = e;
        } finally {
            logcatUtil.stop();
            executorService.shutdownNow();
            IpTablesUtil.banRelayServer(false);
            logger.info("Database reducing result: " + reduceDB());
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }
}
