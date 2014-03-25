package com.seven.asimov.it.tests.connectivity.https.handshake.tests1;

import com.seven.asimov.it.testcases.HttpsHandshakeTestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.date.DateUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.httpsTasks.FcFcnTask;
import com.seven.asimov.it.utils.logcat.tasks.httpsTasks.FclLHSRTask;
import com.seven.asimov.it.utils.logcat.tasks.httpsTasks.HostBlacklistedTask;
import com.seven.asimov.it.utils.logcat.tasks.httpsTasks.HttpsFCLTask;
import com.seven.asimov.it.utils.logcat.tasks.streamTasks.StreamTask;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RejectFakeCertDetectorTests1 extends HttpsHandshakeTestCase {
    private static final Logger logger = LoggerFactory.getLogger(RejectFakeCertDetectorTests1.class.getSimpleName());

    /**
     * <p>Runtime detect of mock certificate rejecting by app in case custom branding properties: blacklisting period > 0
     * </p>
     * <p>Pre-requisites:
     * Default branding properties:
     * client.openchannel.fakecert.max_reject_number=4
     * client.openchannel.fakecert.blacklisting_period=5
     * </p>
     * <p>Steps:
     * Point 1:
     * 1. Install OC
     * 2. Generate 4 https connections one by one and close each of them after local handshake performing.
     * Point 2:
     * 4. Generate 1 https request.
     * Point 3:
     * 5. Change device system time to current time + 5 hours
     * 6. Generate 1 https connection.
     * 7. Close this connection after local handshake performing.
     * Point 4:
     * 8. Generate 1 https request.
     * </p>
     * <p>Expected reults:
     * Point 1:
     * 1. 4 FCL tasks should be created and verdict for each should be FCN.
     * 2. FC should be generated successfully.
     * 3. local_hs_res should be -1 for each connection
     * 4. Appropriate host should be blacklisted till current time + 5 hours
     * Point 2:
     * 5. FCL task should be created, host should be recognized as blacklisted, verdict should be FCN.
     * 6. Then traffic to this host should go in stream:
     * NAQ should be received by Engine from https dispatcher and NAR should be sent to https dispatcher.
     * Point 3:
     * 7. FCL task should be created and verdict should be FCN.
     * 8. FC should be generated successfully.
     * 9. local_hs_res should be -1
     * 10. Appropriate host should be blacklisted till current time + 5 hours
     * Point 4:
     * 11. FCL task should be created, host should be recognized as blacklisted, verdict should be FCN.
     * 12. Then traffic to this host should go in stream:
     * NAQ should be received by Engine from https dispatcher and NAR should be sent to https dispatcher.
     * </p>
     *
     * @throws Throwable
     */

    public void test_002_FcReject() throws Throwable {
        if ((getMaxRejectNumber() == 4) && (getBlacklistPeriod() == 5)) {
            String resourceUrl = "https://hki-dev-testrunner2.7sys.eu/asimov_it_reject_mock_002" + SUITE;
            PrepareResourceUtil.prepareResource(resourceUrl, false);
            HostBlacklistedTask hostBlacklistedTask = new HostBlacklistedTask();
            HttpsFCLTask httpsFCLTask = new HttpsFCLTask();
            FclLHSRTask fclLHSRTask = new FclLHSRTask();
            FcFcnTask fcFcnTask = new FcFcnTask();
            StreamTask streamTask = new StreamTask();
            LogcatUtil logcatUtil = null;

            try {
                PMSUtil.addPolicies(new Policy[]{new Policy("enabled", "true", "@asimov@application@com.seven.asimov.it@ssl", true)});
                Thread.sleep(MIN_PERIOD);

                logger.info("Point #1");
                logcatUtil = new LogcatUtil(getContext(), httpsFCLTask, fcFcnTask, fclLHSRTask, hostBlacklistedTask);
                logcatUtil.start();
                sendRejectedHttpsRequest(4, resourceUrl);
                logcatUtil.stop();
                assertTrue("FCL tasks should be created and verdict for each should be FCN", isVerdictFCN(httpsFCLTask, fcFcnTask));
                assertTrue("local_hs_res should be -1 for 4 connection", checkLocalHsRes(fclLHSRTask, "-1", 4));
                assertFalse("Appropriate host should be blacklisted", hostBlacklistedTask.getLogEntries().isEmpty());

                logger.info("Point #2");
                logcatUtil = new LogcatUtil(getContext(), httpsFCLTask, fcFcnTask, hostBlacklistedTask, streamTask);
                logcatUtil.start();
                sendHttpsRequest(1, resourceUrl);
                logcatUtil.stop();
                assertTrue("FCL tasks should be created and verdict for each should be FCN", isVerdictFCN(httpsFCLTask, fcFcnTask));
                assertFalse("Appropriate host should be blacklisted", hostBlacklistedTask.getLogEntries().isEmpty());
                assertTrue("Traffic to this host should go in stream", isTrafficInStream(streamTask.getLogEntries()));

                logger.info("Point #3");
                logcatUtil = new LogcatUtil(getContext(), httpsFCLTask, fcFcnTask, hostBlacklistedTask, fclLHSRTask);
                logcatUtil.start();
                DateUtil.moveTime(5 * 60 * 60 * 1000);
                sendRejectedHttpsRequest(1, resourceUrl);
                logcatUtil.stop();
                assertTrue("FCL tasks should be created and verdict for each should be FCN", isVerdictFCN(httpsFCLTask, fcFcnTask));
                assertTrue("local_hs_res should be -1", checkLocalHsRes(fclLHSRTask, "-1"));
                assertFalse("Appropriate host should be blacklisted", hostBlacklistedTask.getLogEntries().isEmpty());

                logger.info("Point #4");
                logcatUtil = new LogcatUtil(getContext(), httpsFCLTask, fcFcnTask, hostBlacklistedTask, streamTask);
                logcatUtil.start();
                sendHttpsRequest(1, resourceUrl);
                logcatUtil.stop();
                assertTrue("FCL tasks should be created and verdict for each should be FCN", isVerdictFCN(httpsFCLTask, fcFcnTask));
                assertFalse("Appropriate host should be blacklisted", hostBlacklistedTask.getLogEntries().isEmpty());
                assertTrue("Traffic to this host should go in stream", isTrafficInStream(streamTask.getLogEntries()));
            } finally {
                if (logcatUtil != null) {
                    logcatUtil.stop();
                }
                PMSUtil.cleanPaths(new String[]{"@asimov@application@com.seven.asimov.it@ssl"});
                PrepareResourceUtil.prepareResource(resourceUrl, true);
            }
        } else {
            throw new IllegalArgumentException("This test need to be executed with following parameters:\n"
                    + "client.openchannel.fakecert.max_reject_number=4\n" +
                    "client.openchannel.fakecert.blacklisting_period=5");
        }

    }
}
