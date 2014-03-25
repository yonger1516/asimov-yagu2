package com.seven.asimov.it.tests.e2e.crcs;

import android.test.suitebuilder.annotation.LargeTest;
import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.testcases.E2ECRCSTestCase;
import com.seven.asimov.it.utils.IpTablesUtil;
import com.seven.asimov.it.utils.MobileNetworkUtil;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.ScreenUtils;
import com.seven.asimov.it.utils.date.DateUtil;
import com.seven.asimov.it.utils.logcat.LogcatChecks;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.crcsTasks.ReportTransferTaskNN;
import com.seven.asimov.it.utils.logcat.wrappers.ReportingDumpTransactionWrapper;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import com.seven.asimov.it.utils.sms.SmsUtil;
import com.seven.asimov.it.utils.tcpdump.DbAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class E2ECRCSTests extends E2ECRCSTestCase {
    private static final Logger logger = LoggerFactory.getLogger(E2ECRCSTests.class.getSimpleName());
    private MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());

    /**
     * <h1>Testing that OC dumps CRCS entries into the reports of that size which is set in the policy correctly. Uploading of reports to server if radio state is UP.</h1>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Switch network to mobile.</li>
     * <li>Sleep for about one minute.</li>
     * <li>Notify e2e server that test_001 started.</li>
     * <li>Create an appropriate policy with custom parameters.</li>
     * <li>Wait for the policy update.</li>
     * <li>Create a thread to keep radio state UP.</li>
     * <li>Accumulate 115 crcs records.<li/>
     * <li>Send several requests in order to fill database correctly.</li>
     * <li>Check that reporting was correct.</li>
     * <li>Notify e2e server that test_001 ended.</li>
     * </ol>
     *
     * @throws Exception
     */
    @LargeTest
    public void test_001_E2E_CRCS() throws Exception {
        mobileNetworkUtil.on3gOnly();
        logSleeping(1 * DateUtil.MINUTES);
        init();
        final Policy minEntries = new Policy(CRCS_POLICY_NAME, CRCS_POLICY_VALUE, CRCS_POLICY_PATH, true);
        final Policy blacklist = new Policy(BLACKLIST_POLICY_NAME, AsimovTestCase.TEST_RESOURCE_HOST, BLACKLIST_POLICY_PATH, true);
        PMSUtil.addPoliciesWithCheck(new Policy[]{minEntries, blacklist});
        logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);

        final RadioUpKeeper ruk = new RadioUpKeeper();
        final Thread radioUp = new Thread(ruk);
        final String RESOURCE_URI = "e2e_crcs_001";
        final String uri = createTestResourceUri(RESOURCE_URI);
        final HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();

        wrapper = null;
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), tasks);
        logcatUtil.start();
        PrepareResourceUtil.prepareResource(uri, false);
        radioUp.start();
        try {
            int iteration = 1;
            while (DbAdapter.getReportEntries().size() < 115) {
                logger.info(String.format("Current iteration: %d", iteration++));
                sendRequest2(request);
                logSleeping(1 * DateUtil.SECONDS);
            }

            sendRequest2(request);
            logSleeping(5 * DateUtil.SECONDS);

            sendRequest2(request);
            logSleeping(5 * DateUtil.SECONDS);

            sendRequest2(request);
            logSleeping(5 * DateUtil.SECONDS);

            sendRequest2(request);
            logSleeping(5 * DateUtil.SECONDS);

            sendRequest2(request);
            logSleeping(5 * DateUtil.SECONDS);

            logger.info("Accumulation ended.");
            logSleeping(1 * DateUtil.MINUTES);
            logcatUtil.stop();
            logcatUtil.logTasks();
            logger.info("Checking correctness of the crcs report");
            logger.info("Checking that correspoding task was added to the queue");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(19), wrapper);
            long boundOfTime = wrapper.getTimestamp();
            logger.info("Checking dump transaction.");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(3), wrapper);
            ReportingDumpTransactionWrapper rdtWrapper = (ReportingDumpTransactionWrapper) wrapper;
            String bytesWereSent = rdtWrapper.getBytes();
            logger.info(String.format("Amount of bytes that were sent: %s", bytesWereSent));
            logger.info("Checking report transfer.");
            wrapper = LogcatChecks.checkReportTransfer((ReportTransferTaskNN) tasks.get(0), "REPORT_TRANSFER", "SENT", wrapper, true);
            logger.info("Checking report pack dump executed and sent.");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(7), wrapper);
            logger.info("Checking prepare to cleanup report provider.");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(8), wrapper);
            logger.info("Checking crcs removed.");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(5), wrapper);
            logger.info("Checking cleanup report provider.");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(9), wrapper);
            logger.info("Checking crcs transfer success.");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(4), wrapper);
            logger.info("Checking crcs sending task successfully removed.");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(2), wrapper);
            logger.info("Checking created task scheduler task.");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(6), wrapper);

            checkCrcsReporting(bytesWereSent, tasks.get(13).getLogEntries(), tasks.get(14).getLogEntries(), tasks.get(15).getLogEntries(), tasks.get(16).getLogEntries(), tasks.get(17).getLogEntries(), tasks.get(18).getLogEntries(), boundOfTime);
            clean();
        } finally {
            logcatUtil.stop();
            logcatUtil.clear();
            ruk.stopThread();
            PrepareResourceUtil.invalidateResourceSafely(uri);
            logSleeping(15 * DateUtil.SECONDS);
        }
    }

    /**
     * <h1>Testing that OC dumps CRCS entries into the reports of that size which is set in policy. Uploading of reports into server in case radio state is DOWN</h1>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Notify e2e server that test_002 started.</li>
     * <li>Block all traffic for other applications.</li>
     * <li>Create an appropriate policy with custom parameters.</li>
     * <li>Wait for the policy update.</li>
     * <li>Accumulation of crcs records.</li>
     * <li>Checking that radio was down and task was not executed.</li>
     * <li>Sending several requests in order to switch radio state to UP.</li>
     * <li>Check that reporting was correct.</li>
     * <li>Notify e2e server that test_002 ended.<li/>
     * </ol>
     *
     * @throws Exception
     */
    @LargeTest
    public void test_002_E2E_CRCS() throws Exception {
        init();
        final int csa = IpTablesUtil.getApplicationUid(getContext(), OC_CLIENT_PACKAGE_NAME);
        final int csat = IpTablesUtil.getApplicationUid(getContext(), OC_TEST_PACKAGE_NAME);
        IpTablesUtil.banNetworkForAllApplications(true);
        IpTablesUtil.allowNetworkForApplication(true, csa);
        IpTablesUtil.allowNetworkForApplication(true, csat);

        final RadioUpKeeper ruk = new RadioUpKeeper();
        final Thread radioUp = new Thread(ruk);
        final String RESOURCE_URI = "e2e_crcs_002";
        final String uriForMiss = createTestResourceUri(RESOURCE_URI);
        final String uriForHit = createTestResourceUri(RESOURCE_URI);
        final HttpRequest requestForMiss = createRequest().setUri(uriForMiss).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
        final HttpRequest requestForHit = createRequest().setUri(uriForHit).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();

        wrapper = null;
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), tasks);

        PrepareResourceUtil.prepareResource(uriForMiss, false);
        PrepareResourceUtil.prepareResource(uriForHit, false);
        try {

            int currentDbSize = DbAdapter.getReportEntries().size();
            logger.info(String.format("Current db size: %d", currentDbSize));

            final Policy minEntries = new Policy(CRCS_POLICY_NAME, String.valueOf(currentDbSize + 145), CRCS_POLICY_PATH, true);
            logger.info(String.format("Setting corresponding policy: %s", minEntries.toString()));
            final Policy blacklist = new Policy(BLACKLIST_POLICY_NAME, AsimovTestCase.TEST_RESOURCE_HOST, BLACKLIST_POLICY_PATH, true);
            PMSUtil.addPoliciesWithCheck(new Policy[]{minEntries, blacklist});
            logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
            logger.info(String.format("Size of the database after policies were set: %d", DbAdapter.getReportEntries().size()));
            int iteration = 1;
            logcatUtil.start();
            while (DbAdapter.getReportEntries().size() < currentDbSize + 105) {    //115
                logger.info(String.format("Current iteration: %d, current amount of records: %d", iteration++, DbAdapter.getReportEntries().size()));
                sendRequest2(requestForMiss);
                logSleeping(1 * DateUtil.SECONDS);
            }

            PMSUtil.cleanPaths(new String[]{BLACKLIST_POLICY_PATH});
            logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
            new SmsUtil(getContext()).sendPolicyUpdate((byte) 1);
            logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);

            boolean screenOn = false;
            while (DbAdapter.getReportEntries().size() < currentDbSize + 145 && iteration < 150) {
                screenOn = !screenOn;
                if (screenOn) {
                    logger.info("Setting screen on.");
                    ScreenUtils.screenOn();
                } else {
                    logger.info("Setting screen off.");
                    ScreenUtils.screenOff();
                }
                logSleeping(4 * DateUtil.SECONDS);
            }

            logger.info(String.format("Accumulation ended. Amount of records: %d", DbAdapter.getReportEntries().size()));

            logcatUtil.stop();
            logcatUtil.logTasks();
            logger.info("Checking correctness of the crcs report");
            logger.info("Checking report transfer.");
            wrapper = LogcatChecks.checkReportTransfer((ReportTransferTaskNN) tasks.get(0), "REPORT_TRANSFER", "WAIT", wrapper, true);
            logger.info("Checking that radio was down and task was not executed.");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(10), wrapper);

            logcatUtil = new LogcatUtil(getContext(), tasks);
            logcatUtil.clear();
            wrapper = null;
            logcatUtil.start();
            radioUp.start();

            final Policy policyToActivateConnection = new Policy("toActivate", "value", "@asimov", true);
            PMSUtil.addPoliciesWithCheck(new Policy[]{policyToActivateConnection});
            logSleeping(15 * DateUtil.SECONDS);
            PMSUtil.cleanPaths(new String[]{"@asimov"});

            logSleeping(1 * DateUtil.MINUTES);
            logcatUtil.stop();
            logcatUtil.logTasks();
            logger.info("Checking correctness of the crcs report");
            logger.info("Checking that correspoding task was added to the queue");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(19), wrapper);
            long boundOfTime = wrapper.getTimestamp();
            logger.info("Checking dump transaction.");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(3), wrapper);
            ReportingDumpTransactionWrapper rdtWrapper = (ReportingDumpTransactionWrapper) wrapper;
            String bytesWereSent = rdtWrapper.getBytes();
            logger.info("Checking report transfer.");
            wrapper = LogcatChecks.checkReportTransfer((ReportTransferTaskNN) tasks.get(0), "REPORT_TRANSFER", "SENT", wrapper, true);
            logger.info("Checking report pack dump executed and sent.");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(7), wrapper);
            logger.info("Checking prepare to cleanup report provider.");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(8), wrapper);
            logger.info("Checking crcs removed.");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(5), wrapper);
            logger.info("Checking cleanup report provider.");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(9), wrapper);
            logger.info("Checking crcs transfer success.");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(4), wrapper);
            logger.info("Checking crcs sending task successfully removed.");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(2), wrapper);
            logger.info("Checking created task scheduler task.");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(6), wrapper);

            checkCrcsReporting(bytesWereSent, tasks.get(13).getLogEntries(), tasks.get(14).getLogEntries(), tasks.get(15).getLogEntries(), tasks.get(16).getLogEntries(), tasks.get(17).getLogEntries(), tasks.get(18).getLogEntries(), boundOfTime);
            clean();
        } finally {
            IpTablesUtil.banNetworkForAllApplications(false);
            IpTablesUtil.allowNetworkForApplication(false, csa);
            IpTablesUtil.allowNetworkForApplication(false, csat);
            logcatUtil.stop();
            logcatUtil.clear();
            ruk.stopThread();
            PrepareResourceUtil.invalidateResourceSafely(uriForMiss);
            PrepareResourceUtil.invalidateResourceSafely(uriForHit);
            logSleeping(15 * DateUtil.SECONDS);
        }
    }

    /**
     * <h1>Testing that OC transfers CRCS in case 75% of database is complete correctly</h1>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Notify e2e server that test_003 started.</li>
     * <li>Create an appropriate policy with custom parameters.</li>
     * <li>Wait for the policy update.</li>
     * <li>Block relay port.</li>
     * <li>Accumulation of crcs records to fill database up to 75%.</li>
     * <li>Checking that force sending was detected.</li>
     * <li>Unblock relay port.</li>
     * <li>Sending several requests in order to switch radio state to UP.</li>
     * <li>Check that reporting was correct.</li>
     * <li>Notify e2e server that test_003 ended.<li/>
     * </ol>
     *
     * @throws Exception
     */
    @LargeTest
    public void test_003_E2E_CRCS() throws Exception {
        init();
        final Policy blacklist = new Policy(BLACKLIST_POLICY_NAME, AsimovTestCase.TEST_RESOURCE_HOST, BLACKLIST_POLICY_PATH, true);
        PMSUtil.addPolicies(new Policy[]{blacklist});
        logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);

        final String RESOURCE_URI = "e2e_crcs_003";
        final String uri = createTestResourceUri(RESOURCE_URI);
        final HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();

        wrapper = null;
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), tasks);
        logcatUtil.start();
        IpTablesUtil.banRelayServer(true, true);
        PrepareResourceUtil.prepareResource(uri, false);
        try {
            int iteration = 1;
            File reportDb = new File(DbAdapter.getReportDataBaseFilename());
            while (reportDb.length() < 786432 && iteration < 2050) {
                logger.info(String.format("Current iteration: %d, size of the database: %d", iteration++, reportDb.length()));
                sendRequest2(request);
                logSleeping(1 * DateUtil.SECONDS);
                if (!tasks.get(11).getLogEntries().isEmpty()) {
                    logger.info("Force sending was detected.");
                    break;
                }
            }
            IpTablesUtil.banRelayServer(false);
            sendRequest2(request);
            logSleeping(10 * DateUtil.SECONDS);

            sendRequest2(request);
            logSleeping(10 * DateUtil.SECONDS);

            sendRequest2(request);
            logSleeping(10 * DateUtil.SECONDS);

            logger.info("Accumulation ended.");
            logSleeping(1 * DateUtil.MINUTES);
            logcatUtil.stop();
            logger.info("Checking correctness of the crcs report");
            logger.info("Checking that it was force transfer");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(11), wrapper);
            logger.info("Checking that corresponding task was added to the queue");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(19), wrapper);
            long boundOfTime = wrapper.getTimestamp();
            logger.info("Checking dump transaction.");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(3), wrapper);
            ReportingDumpTransactionWrapper rdtWrapper = (ReportingDumpTransactionWrapper) wrapper;
            String bytesWereSent = rdtWrapper.getBytes();
            logger.info("Checking report transfer.");
            wrapper = LogcatChecks.checkReportTransfer((ReportTransferTaskNN) tasks.get(0), "REPORT_TRANSFER", "SENT", wrapper, true);
            logger.info("Checking report pack dump executed and sent.");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(7), wrapper);
            logger.info("Checking prepare to cleanup report provider.");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(8), wrapper);
            logger.info("Checking crcs removed.");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(5), wrapper);
            logger.info("Checking cleanup report provider.");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(9), wrapper);
            logger.info("Checking crcs transfer success.");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(4), wrapper);
            logger.info("Checking crcs sending task successfully removed.");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(2), wrapper);
            logger.info("Checking created task scheduler task.");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(6), wrapper);

            checkCrcsReporting(bytesWereSent, tasks.get(13).getLogEntries(), tasks.get(14).getLogEntries(), tasks.get(15).getLogEntries(), tasks.get(16).getLogEntries(), tasks.get(17).getLogEntries(), tasks.get(18).getLogEntries(), boundOfTime);
            clean();
        } finally {
            PMSUtil.cleanPaths(new String[]{BLACKLIST_POLICY_PATH});
            logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
            IpTablesUtil.banRelayServer(false);
            logcatUtil.stop();
            logcatUtil.clear();
            PrepareResourceUtil.invalidateResourceSafely(uri);
            logSleeping(15 * DateUtil.SECONDS);
        }
    }

    /**
     * <h1>Testing that OC transfers CRCS in case 95% of database is complete correctly</h1>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Notify e2e server that test_004 started.</li>
     * <li>Create an appropriate policy with custom parameters.</li>
     * <li>Wait for the policy update.</li>
     * <li>Block relay port.</li>
     * <li>Accumulation of crcs records to fill database up to 95%.</li>
     * <li>Checking that OC cleans some crcs records.</li>
     * <li>Unblock relay port.</li>
     * <li>Sending several requests in order to switch radio state to UP.</li>
     * <li>Check that reporting was correct.</li>
     * <li>Notify e2e server that test_004 ended.<li/>
     * </ol>
     *
     * @throws Exception
     */
    @LargeTest
    public void test_004_E2E_CRCS() throws Exception {
        init();
        final Policy blacklist = new Policy(BLACKLIST_POLICY_NAME, AsimovTestCase.TEST_RESOURCE_HOST, BLACKLIST_POLICY_PATH, true);
        PMSUtil.addPolicies(new Policy[]{blacklist});
        logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);

        final String RESOURCE_URI = "e2e_crcs_004";
        final String uri = createTestResourceUri(RESOURCE_URI);
        final HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();

        wrapper = null;
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), tasks);
        logcatUtil.start();
        IpTablesUtil.banRelayServer(true, true);
        PrepareResourceUtil.prepareResource(uri, false);
        try {
            File reportDb = new File(DbAdapter.getReportDataBaseFilename());
            int iteration = 1;
            while (reportDb.length() < 994148 && iteration < 3050) {
                logger.info(String.format("Current iteration: %d, size of the database: %d", iteration++, reportDb.length()));
                sendRequest2(request);
                logSleeping(1 * DateUtil.SECONDS);
                if (!tasks.get(12).getLogEntries().isEmpty()) {
                    logger.info("Crcs storage was filled up to 95 percents.");
                    break;
                }
            }
            IpTablesUtil.banRelayServer(false);
            sendRequest2(request);
            logSleeping(10 * DateUtil.SECONDS);

            sendRequest2(request);
            logSleeping(10 * DateUtil.SECONDS);

            sendRequest2(request);
            logSleeping(10 * DateUtil.SECONDS);

            logger.info("Accumulation ended.");
            logSleeping(1 * DateUtil.MINUTES);
            logcatUtil.stop();
            logcatUtil.clear();
            logger.info("Checking correctness of the crcs report");
            logger.info("Checking that crcs database was filled correctly.");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(12), wrapper);
            logger.info("Checking that it was force transfer");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(11), wrapper);
            logger.info("Checking that corresponding task was added to the queue");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(19), wrapper);
            long boundOfTime = wrapper.getTimestamp();
            logger.info("Checking dump transaction.");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(3), wrapper);
            ReportingDumpTransactionWrapper rdtWrapper = (ReportingDumpTransactionWrapper) wrapper;
            String bytesWereSent = rdtWrapper.getBytes();
            logger.info("Checking report transfer.");
            wrapper = LogcatChecks.checkReportTransfer((ReportTransferTaskNN) tasks.get(0), "REPORT_TRANSFER", "SENT", wrapper, true);
            logger.info("Checking report pack dump executed and sent.");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(7), wrapper);
            logger.info("Checking prepare to cleanup report provider.");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(8), wrapper);
            logger.info("Checking crcs removed.");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(5), wrapper);
            logger.info("Checking cleanup report provider.");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(9), wrapper);
            logger.info("Checking crcs transfer success.");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(4), wrapper);
            logger.info("Checking crcs sending task successfully removed.");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(2), wrapper);
            logger.info("Checking created task scheduler task.");
            wrapper = LogcatChecks.checkLogEntryExist(tasks.get(6), wrapper);

            checkCrcsReporting(bytesWereSent, tasks.get(13).getLogEntries(), tasks.get(14).getLogEntries(), tasks.get(15).getLogEntries(), tasks.get(16).getLogEntries(), tasks.get(17).getLogEntries(), tasks.get(18).getLogEntries(), boundOfTime);
            clean();
        } finally {
            IpTablesUtil.banRelayServer(false);
            logcatUtil.stop();
            logcatUtil.clear();
            PrepareResourceUtil.invalidateResourceSafely(uri);
            logSleeping(15 * DateUtil.SECONDS);
        }
    }
}
