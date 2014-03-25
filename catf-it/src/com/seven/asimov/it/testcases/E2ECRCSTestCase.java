package com.seven.asimov.it.testcases;

import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.logcat.tasks.*;
import com.seven.asimov.it.utils.logcat.tasks.crcsTasks.*;
import com.seven.asimov.it.utils.logcat.tasks.crcsTasks.ReportTransferTaskNN;
import com.seven.asimov.it.utils.logcat.tasks.e2eTasks.NotExecuteTaskRadioDownTask;
import com.seven.asimov.it.utils.logcat.tasks.firewallTasks.ParametrizedFirewallTask;
import com.seven.asimov.it.utils.logcat.wrappers.LogEntryWrapper;
import com.seven.asimov.it.utils.pms.PMSUtil;
import junit.framework.AssertionFailedError;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class E2ECRCSTestCase extends E2ETestCase {
    private static final String TAG = E2ECRCSTestCase.class.getSimpleName();
    private static final Logger logger = LoggerFactory.getLogger(E2ECRCSTestCase.class.getSimpleName());
    protected static final String CRCS_POLICY_NAME = "min_entries";
    protected static final String CRCS_POLICY_VALUE = "120";
    protected static final String CRCS_POLICY_PATH = "@asimov@reporting@triggers@storage";
    protected static final String BLACKLIST_POLICY_NAME = "blacklist";
    protected static final String BLACKLIST_POLICY_PATH = "@asimov@http";
    protected static final String OC_CLIENT_PACKAGE_NAME = "com.seven.asimov";
    protected static final String OC_TEST_PACKAGE_NAME = "com.seven.asimov.it";
    protected static List<Task> tasks = new ArrayList<Task>();
    protected static LogEntryWrapper wrapper = null;

    //TODO change this point. Objects should be used.
    static {
        tasks.add(new ReportTransferTaskNN());                    //0
        tasks.add(new CrcsAccumulatedTask());                     //1
        tasks.add(new CrcsSendingTaskSuccessRemovedTask());       //2
        tasks.add(new ReportingDumpTransactionTask(ReportingDumpTransactionTask.ReportingType.OUT, null, null, null, null));//3
        tasks.add(new CrcsTransferSuccessTask());                 //4
        tasks.add(new CrcsRemovedTask());                         //5
        tasks.add(new CreatedTaskSchedulerTaskTask());            //6
        tasks.add(new ReportPackDumpExecutedAndSentTask());       //7
        tasks.add(new PrepareToCleanupReportProviderTask());      //8
        tasks.add(new CleanupReportProviderTask());               //9
        tasks.add(new NotExecuteTaskRadioDownTask());             //10
        tasks.add(new TryToForceCrcsReportSendingTask());         //11
        tasks.add(new CrcsStorageFilled_95_PercentTask());        //12
        tasks.add(new ParametrizedFirewallTask());                //13
        tasks.add(new ParametrizedPowerTask());                   //14
        tasks.add(new ParametrizedSystemTask());                  //15
        tasks.add(new ParametrizedTrafficTask());                 //16
        tasks.add(new RadiologTask());                            //17
        tasks.add(new ServiceLogTask(null, null, null, null));            //18
        tasks.add(new ParametrizedTaskInQueueTask("NEW", "REPORT_TRANSFER"));//19
    }

    protected class RadioUpKeeper implements Runnable {
        private volatile boolean run = true;

        public RadioUpKeeper() {
        }

        public void stopThread() {
            run = false;
        }

        @Override
        public void run() {
            final String uri = createTestResourceUri("asimov_it_test_radio_up", false);
            while (run) {
                try {
                    sendRequestWithoutLogging(createRequest().setUri(uri).setMethod("GET").getRequest());
                } catch (Exception e) {
                    logger.debug(ExceptionUtils.getStackTrace(e));
                }
                TestUtil.sleep(4 * 1000);
            }
        }
    }

    /**
     * <h1>Check CRCS Reporting at server side</h1>
     * This rest service API is used to check that crcs-reporting and z7-services server logs contain info about client CRCS transactions.
     * <br /><br />
     * <b>URL:</b> http://hostname//rest/crcsreport
     * <br />
     * <b>HTTP Method:</b> Post
     * <br />
     * <b>Request Parameters:</b>
     * <ul>
     * <li>testName: test name</li>
     * <li>z7TpId: client z7TP address</li>
     * </ul>
     * <b>Example:</b>
     * <br />
     * {@code <Request> }<br />
     * {@code <testName>E2E-CRCS</caseID> }<br />
     * {@code <time>3fb</time> }<br />
     * {@code </Request> }<br />
     * <br />
     * <b>Response Parameters:</b>
     * <ul>
     * <li>result:  success or fail</li>
     * <li>message:  if fail, the test server will provide some information</li>
     * </ul>
     * <b>Example:</b>
     * <br />
     * {@code <Response> }<br />
     * {@code <result>success</result> }<br />
     * {@code <message></message> }<br />
     * {@code </Response> }<br />
     *
     * @throws java.io.IOException
     * @throws java.net.URISyntaxException
     */

    protected void checkCrcsReporting(String sizeOfData, List<LogEntryWrapper> firewall, List<LogEntryWrapper> power, List<LogEntryWrapper> system, List<LogEntryWrapper> traffic, List<LogEntryWrapper> radio, List<LogEntryWrapper> service, long boundTime) throws IOException, URISyntaxException {
        String requestBody = String.format(CRCS_REPORTING_BODY_PATTERN, getName(), z7TpId, sizeOfData,
                !(firewall.isEmpty() || firewall.get(0).getTimestamp() > boundTime),
                !(power.isEmpty() || power.get(0).getTimestamp() > boundTime),
                !(system.isEmpty() || system.get(0).getTimestamp() > boundTime),
                !(traffic.isEmpty() || traffic.get(0).getTimestamp() > boundTime),
                !(radio.isEmpty() || radio.get(0).getTimestamp() > boundTime),
                !(service.isEmpty() || service.get(0).getTimestamp() > boundTime));

        logger.info(String.format("RequestBody: %s", requestBody));
        String response = sendPostRequestToRest(CRCS_REPORT_PATH_END, requestBody);
        boolean isSuccess = response != null && response.toLowerCase().contains(SUCCESS);
        assertTrue("REST check failed. CRCS reporting at server side failed ", isSuccess);
    }

    protected void init() {
        try {
            notifyRestForTestsStart(TAG);
        } catch (Exception e) {
            logger.debug("Tests start REST notification failed");
            e.printStackTrace();
        }
    }

    protected void clean() {
        try {
            notifyRestForTestEnd(TAG);
        } catch (Exception e) {
            logger.debug("Tests end REST notification failed");
            e.printStackTrace();
        }
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        if (PMSUtil.getDeviceZ7TpAddress() != null &&
                PMSUtil.getDeviceZ7TpAddress().length() > 2) {
            z7TpId = PMSUtil.getDeviceZ7TpAddress().substring(2, PMSUtil.getDeviceZ7TpAddress().length());
        } else {
            throw new AssertionFailedError("Some problems with OC. File transport_settings not found or corrupted.");
        }
    }

    @Override
    protected void runTest() throws Throwable {
        try {
            super.runTest();
        } catch (Exception e) {
            throw new AssertionFailedError(e.getMessage());
        }
    }
}
