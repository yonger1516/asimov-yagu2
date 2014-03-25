package com.seven.asimov.it.tests.connectivity.https.handshake.tests3;


import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.testcases.HttpsHandshakeTestCase;
import com.seven.asimov.it.utils.DnsUtil;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.ShellUtil;
import com.seven.asimov.it.utils.date.DateUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.httpsTasks.*;
import com.seven.asimov.it.utils.logcat.tasks.streamTasks.StreamTask;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RejectFakeCertDetectorTests3 extends HttpsHandshakeTestCase {
    private static final Logger logger = LoggerFactory.getLogger(RejectFakeCertDetectorTests3.class.getSimpleName());

    /**
     * <p>Runtime detect of mock certificate rejecting by app in case default branding properties
     * </p>
     * <p>Pre-requisites:
     * Default branding properties:
     * client.openchannel.fakecert.max_reject_number=3
     * client.openchannel.fakecert.blacklisting_period=24
     * </p>
     * <p>Steps:
     * Point 1:
     * 1. Install OC
     * 2. Generate 3 https connections one by one and close each of them after local handshake performing.
     * Point 2:
     * 4. Generate 1 https request.
     * Point 3:
     * 5. Change device system time to current time + 24 hours
     * 6. Generate 1 https connection.
     * 7. Close this connection after local handshake performing.
     * Point 4:
     * 8. Generate 1 https request.
     * </p>
     * <p>Expected reults:
     * Point 1:
     * 1. 3 FCL tasks should be created and verdict for each should be FCN.
     * 2. FC should be generated successfully.
     * 3. local_hs_res should be -1 for each connection
     * 4. Appropriate host should be blacklisted till current time + 24 hours
     * Point 2:
     * 5. FCL task should be created, host should be recognized as blacklisted, verdict should be FCN.
     * 6. Then traffic to this host should go in stream:
     * NAQ should be received by Engine from https dispatcher and NAR should be sent to https dispatcher.
     * Point 3:
     * 7. FCL task should be created and verdict should be FCN.
     * 8. FC should be generated successfully.
     * 9. local_hs_res should be -1
     * 10. Appropriate host should be blacklisted till current time + 24 hours
     * Point 4:
     * 11. FCL task should be created, host should be recognized as blacklisted, verdict should be FCN.
     * 12. Then traffic to this host should go in stream:
     * NAQ should be received by Engine from https dispatcher and NAR should be sent to https dispatcher.
     * </p>
     *
     * @throws Throwable
     */

    public void test_001_FcReject() throws Throwable {
        if ((getMaxRejectNumber() == 3) && (getBlacklistPeriod() == 24)) {
            String resourceUrl = "https://tln-dev-testrunner1.7sys.eu/asimov_it_reject_mock_001" + SUITE;
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
                sendRejectedHttpsRequest(3, resourceUrl);
                logcatUtil.stop();
                assertTrue("FCL tasks should be created and verdict for each should be FCN", isVerdictFCN(httpsFCLTask, fcFcnTask));
                assertTrue("local_hs_res should be -1 for 3 connection", checkLocalHsRes(fclLHSRTask, "-1", 3));
                assertFalse("Appropriate host should be blacklisted", hostBlacklistedTask.getLogEntries().isEmpty());

                logger.info("Point #2");
                logcatUtil = new LogcatUtil(getContext(), httpsFCLTask, fcFcnTask, streamTask);
                logcatUtil.start();
                sendHttpsRequest(1, resourceUrl);
                logcatUtil.stop();
                assertTrue("FCL tasks should be created and verdict for each should be FCN", isVerdictFCN(httpsFCLTask, fcFcnTask));
                assertTrue("Traffic to this host should go in stream", isTrafficInStream(streamTask.getLogEntries()));

                logger.info("Point #3");
                logcatUtil = new LogcatUtil(getContext(), httpsFCLTask, fcFcnTask, fclLHSRTask, hostBlacklistedTask);
                logcatUtil.start();
                DateUtil.moveTime(24 * 60 * 60 * 1000);
                sendRejectedHttpsRequest(1, resourceUrl);
                logcatUtil.stop();
                assertTrue("FCL tasks should be created and verdict for each should be FCN", isVerdictFCN(httpsFCLTask, fcFcnTask));
                assertTrue("local_hs_res should be -1", checkLocalHsRes(fclLHSRTask, "-1"));
                assertFalse("Appropriate host should be blacklisted", hostBlacklistedTask.getLogEntries().isEmpty());

                logger.info("Point #4");
                logcatUtil = new LogcatUtil(getContext(), hostBlacklistedTask, httpsFCLTask, fcFcnTask, streamTask);
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
                DateUtil.moveTime(25 * 60 * 60 * 1000);
                sendHttpsRequest(1, resourceUrl);
                PrepareResourceUtil.prepareResource(resourceUrl, true);
            }
        } else {
            throw new IllegalArgumentException("This test need to be executed with following parameters:\n"
                    + "client.openchannel.fakecert.max_reject_number=3\n" +
                    "client.openchannel.fakecert.blacklisting_period=24");
        }
    }

    /**
     * <p>Runtime detect of mock certificate rejecting by app in case default branding properties after device reboot
     * </p>
     * <p>Pre-requisites:
     * 1. Default branding properties:
     * client.openchannel.fakecert.max_reject_number=3
     * client.openchannel.fakecert.blacklisting_period=24
     * </p>
     * <p>Steps:
     * Point 1:
     * 1. Install OC
     * 2. Generate 3 https connections one by one and close each of them after local handshake performing.
     * Point 2:
     * 3. Generate 1 https request.
     * Point 3:
     * 4. Reboot device.
     * 5.  Generate 1 https request.
     * </p>
     * <p>Expected reults:
     * Point 1:
     * 1. 3 FCL tasks should be created and verdict for each should be FCN.
     * 2. FC should be generated successfully.
     * 3. local_hs_res should be -1 for each connection
     * 4. Appropriate host should be blacklisted till  current time + 24 hours.
     * Point 2:
     * 5. FCL task should be created, host should be recognized as blacklisted, verdict should be FCN.
     * 6. Then traffic to this host should go in stream:
     * NAQ should be received by Engine from https dispatcher and NAR should be sent to https dispatcher.
     * Point 3:
     * 7. FCL task should be created and verdict should be FCN.
     * 8. FC should be generated successfully.
     * 9. local_hs_res should be 0
     * 10. Request should be sent to network and response should be received successfully
     * </p>
     *
     * @throws Throwable
     */

    public void test_004_FcReject() throws Throwable {
        if ((getMaxRejectNumber() == 3) && (getBlacklistPeriod() == 24)) {
            String resourceUrl = "https://hki-dev-testrunner4.7sys.eu/asimov_it_reject_mock_004" + SUITE;
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
                logcatUtil = new LogcatUtil(getContext(), hostBlacklistedTask, httpsFCLTask, fclLHSRTask, fcFcnTask);
                logcatUtil.start();
                sendRejectedHttpsRequest(3, resourceUrl);
                logcatUtil.stop();
                assertTrue("FCL tasks should be created and verdict for each should be FCN", isVerdictFCN(httpsFCLTask, fcFcnTask));
                assertTrue("local_hs_res should be -1 for 3 connection", checkLocalHsRes(fclLHSRTask, "-1", 3));
                assertFalse("Appropriate host should be blacklisted", hostBlacklistedTask.getLogEntries().isEmpty());

                logger.info("Point #2");
                logcatUtil = new LogcatUtil(getContext(), hostBlacklistedTask, httpsFCLTask, streamTask, fcFcnTask);
                logcatUtil.start();
                sendHttpsRequest(1, resourceUrl);
                logcatUtil.stop();
                assertTrue("FCL tasks should be created and verdict for each should be FCN", isVerdictFCN(httpsFCLTask, fcFcnTask));
                assertFalse("Appropriate host should be blacklisted", hostBlacklistedTask.getLogEntries().isEmpty());
                assertTrue("Traffic to this host should go in stream", isTrafficInStream(streamTask.getLogEntries()));

                logger.info("Point #3");
                PMSUtil.cleanPaths(new String[]{"@asimov@application@com.seven.asimov.it@ssl"});
                rebootOpenChannel().start();
                Thread.sleep(MIN_PERIOD);
                PMSUtil.addPolicies(new Policy[]{new Policy("enabled", "true", "@asimov@application@com.seven.asimov.it@ssl", true)});
                Thread.sleep(MIN_PERIOD);
                logcatUtil = new LogcatUtil(getContext(), httpsFCLTask, fclLHSRTask, fcFcnTask);
                logcatUtil.start();
                sendHttpsRequest(1, resourceUrl);
                logcatUtil.stop();
                assertTrue("FCL tasks should be created and verdict for each should be FCN", isVerdictFCN(httpsFCLTask, fcFcnTask));
                assertTrue("local_hs_res should be 0", checkLocalHsRes(fclLHSRTask, "0"));
            } finally {
                if (logcatUtil != null) {
                    logcatUtil.stop();
                }
                PMSUtil.cleanPaths(new String[]{"@asimov@application@com.seven.asimov.it@ssl"});
                DateUtil.moveTime(25 * 60 * 60 * 1000);
                sendHttpsRequest(1, resourceUrl);
                PrepareResourceUtil.prepareResource(resourceUrl, true);
            }
        } else {
            throw new IllegalArgumentException("This test need to be executed with following parameters:\n"
                    + "client.openchannel.fakecert.max_reject_number=3\n" +
                    "client.openchannel.fakecert.blacklisting_period=24");
        }
    }

    /**
     * <p>Runtime detect of mock certificate rejecting by app when device was rebooted during detection
     * </p>
     * <p>Pre-requisites:
     * 1. Default branding properties:
     * client.openchannel.fakecert.max_reject_number=3
     * client.openchannel.fakecert.blacklisting_period=24
     * </p>
     * <p>Steps:
     * Point 1:
     * 1. Install OC
     * 2. Generate 2 https connections one by one and close each of them after local handshake performing.
     * Point 2:
     * 3. Reboot device.
     * 4. Generate 1 https connection.
     * 5. Close this connection after local handshake performing.
     * Point 3:
     * 6. Generate 1 https request.
     * </p>
     * <p>Expected reults:
     * Point 1:
     * 1. 2 FCL tasks should be created and verdict for each should be FCN.
     * 2. FC should be generated successfully.
     * 3. local_hs_res should be -1 for each connection
     * Point 2:
     * 4. FCL task should be created,  verdict should be FCN.
     * 5. FC should be generated successfully.
     * 6. local_hs_res should be -1
     * Point 3:
     * 7. FCL task should be created and verdict should be FCN.
     * 8. FC should be generated successfully.
     * 9. local_hs_res should be 0
     * 10. Request should be sent to network and response should be received successfully
     * </p>
     *
     * @throws Throwable
     */

    public void test_005_FcReject() throws Throwable {
        if ((getMaxRejectNumber() == 3) && (getBlacklistPeriod() == 24)) {
            String resourceUrl = "https://hki-qa-testrunner1.7sys.eu/asimov_it_reject_mock_005" + SUITE;
            PrepareResourceUtil.prepareResource(resourceUrl, false);
            HttpsFCLTask httpsFCLTask = new HttpsFCLTask();
            FclLHSRTask fclLHSRTask = new FclLHSRTask();
            FcFcnTask fcFcnTask = new FcFcnTask();
            LogcatUtil logcatUtil = null;
            try {
                PMSUtil.addPolicies(new Policy[]{new Policy("enabled", "true", "@asimov@application@com.seven.asimov.it@ssl", true)});
                Thread.sleep(MIN_PERIOD);
                logger.info("Point #1");
                logcatUtil = new LogcatUtil(getContext(), httpsFCLTask, fclLHSRTask, fcFcnTask);
                logcatUtil.start();
                sendRejectedHttpsRequest(1, resourceUrl);
                logcatUtil.stop();
                assertTrue("FCL tasks should be created and verdict for each should be FCN", isVerdictFCN(httpsFCLTask, fcFcnTask));
                assertTrue("local_hs_res should be -1 for 2 connection", checkLocalHsRes(fclLHSRTask, "-1", 2));

                logger.info("Point #2");
                PMSUtil.cleanPaths(new String[]{"@asimov@application@com.seven.asimov.it@ssl"});
                rebootOpenChannel().start();
                PMSUtil.addPolicies(new Policy[]{new Policy("enabled", "true", "@asimov@application@com.seven.asimov.it@ssl", true)});
                Thread.sleep(MIN_PERIOD);
                logcatUtil = new LogcatUtil(getContext(), httpsFCLTask, fclLHSRTask, fcFcnTask);
                logcatUtil.start();
                sendRejectedHttpsRequest(1, resourceUrl);
                logcatUtil.stop();
                assertTrue("FCL tasks should be created and verdict for each should be FCN", isVerdictFCN(httpsFCLTask, fcFcnTask));
                assertTrue("local_hs_res should be -1", checkLocalHsRes(fclLHSRTask, "-1"));

                logger.info("Point #3");
                logcatUtil = new LogcatUtil(getContext(), httpsFCLTask, fclLHSRTask, fcFcnTask);
                logcatUtil.start();
                sendHttpsRequest(1, resourceUrl);
                logcatUtil.stop();
                assertTrue("FCL tasks should be created and verdict for each should be FCN", isVerdictFCN(httpsFCLTask, fcFcnTask));
                assertTrue("local_hs_res should be 0", checkLocalHsRes(fclLHSRTask, "0"));

            } finally {
                if (logcatUtil != null) {
                    logcatUtil.stop();
                }
                PMSUtil.cleanPaths(new String[]{"@asimov@application@com.seven.asimov.it@ssl"});
                DateUtil.moveTime(25 * 60 * 60 * 1000);
                sendHttpsRequest(1, resourceUrl);
                PrepareResourceUtil.prepareResource(resourceUrl, true);
            }
        } else {
            throw new IllegalArgumentException("This test need to be executed with following parameters:\n"
                    + "client.openchannel.fakecert.max_reject_number=3\n" +
                    "client.openchannel.fakecert.blacklisting_period=24");
        }
    }

    /**
     * <p>Runtime detect of mock certificate rejecting by app with default branding properties in case FCL tasks postponing
     * </p>
     * <p>Pre-requisites:
     * 1. Default branding properties:
     * client.openchannel.fakecert.max_reject_number=3
     * client.openchannel.fakecert.blacklisting_period=24
     * </p>
     * <p>Steps:
     * Point 1:
     * 1. Install OC
     * 2. Generate 5 simultaneous https connections and close each of them after local handshake performing.
     * Point 2:
     * 3. Generate 1 https request.
     * </p>
     * <p>Expected reults:
     * Point 1:
     * 1. FCL task should be created and verdict should be FCN.
     * 2. Next 4 FCL tasks should be postponed.
     * 3. FC should be generated successfully.
     * 4. local_hs_res should be -1 for each connection
     * 5. Appropriate host should be blacklisted till  current time + 24 hours.
     * Point 2:
     * 6. FCL task should be created, host should be recognized as blacklisted, verdict should be FCN.
     * 7. Then traffic to this host should go in stream:
     * NAQ should be received by Engine from https dispatcher and NAR should be sent to https dispatcher.
     * </p>
     *
     * @throws Throwable
     */

    public void test_006_FcReject() throws Throwable {
        if ((getMaxRejectNumber() == 3) && (getBlacklistPeriod() == 24)) {
            resetDB();
            ShellUtil.killAll(TFConstantsIF.OC_PROCESS_NAME);
            ShellUtil.killAll("occ");
            Thread.sleep(MIN_PERIOD);
            String resourceUrl = "https://hki-dev-testrunner4.7sys.eu/asimov_it_reject_mock_006" + SUITE;
            try {
                DnsUtil.resolveHost("hki-dev-testrunner4.7sys.eu");
            } catch (Exception e) {
                logger.info("Failed to resolve host \'hki-dev-testrunner4.7sys.eu\'");
            }
            PrepareResourceUtil.prepareResource(resourceUrl, false);
            HostBlacklistedTask hostBlacklistedTask = new HostBlacklistedTask();
            FclPostponed fclPostponed = new FclPostponed();
            HttpsFCLTask httpsFCLTask = new HttpsFCLTask();
            FclLHSRTask fclLHSRTask = new FclLHSRTask();
            FcFcnTask fcFcnTask = new FcFcnTask();
            StreamTask streamTask = new StreamTask();
            LogcatUtil logcatUtil = null;
            try {
                PMSUtil.addPolicies(new Policy[]{new Policy("enabled", "true", "@asimov@application@com.seven.asimov.it@ssl", true)});
                Thread.sleep(MIN_PERIOD);

                logger.info("Point #1");
                logcatUtil = new LogcatUtil(getContext(), hostBlacklistedTask, httpsFCLTask, fclLHSRTask, fcFcnTask, fclPostponed);
                logcatUtil.start();
                simultaneousRejectedHttpsConnection(5, resourceUrl);
                logcatUtil.stop();
                assertTrue("FCL tasks should be created and verdict for each should be FCN", isVerdictFCN(httpsFCLTask, fcFcnTask));
                assertTrue("4 FCL tasks should be postponed", checkFclPostponed(fclPostponed, 4));
                assertTrue("local_hs_res should be -1 for 5 connection", checkLocalHsRes(fclLHSRTask, "-1", 5));
                assertFalse("Appropriate host should be blacklisted", hostBlacklistedTask.getLogEntries().isEmpty());

                logger.info("Point #2");
                logcatUtil = new LogcatUtil(getContext(), hostBlacklistedTask, httpsFCLTask, fcFcnTask, streamTask);
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
                DateUtil.moveTime(25 * 60 * 60 * 1000);
                sendHttpsRequest(1, resourceUrl);
                PrepareResourceUtil.prepareResource(resourceUrl, true);
            }
        } else {
            throw new IllegalArgumentException("This test need to be executed with following parameters:\n"
                    + "client.openchannel.fakecert.max_reject_number=3\n" +
                    "client.openchannel.fakecert.blacklisting_period=24");
        }
    }
    /**
     * <p>Runtime detect of mock certificate rejecting by app with default branding properties in case ref_count != 0
     * </p>
     * <p>Pre-requisites:
     * 1. Default branding properties:
     * client.openchannel.fakecert.max_reject_number=3
     * client.openchannel.fakecert.blacklisting_period=24
     * </p>
     * <p>Steps:
     * Point 1:
     * 1. Install OC
     * 2. Generate 5 simultaneous https connections and close last 3 of them after local handshake performing.
     * Point 2:
     * 3. Generate 1 https request.
     * </p>
     * <p>Expected reults:
     * Point 1:
     * 1. FCL task should be created and verdict should be FCN.
     * 2. Next FCL tasks should be postponed.
     * 3. FC should be generated successfully.
     * 4. local_hs_res should be 0 for first 2 connection
     * 5. ref_count should be incremented to 2
     * 6.  local_hs_res should be -1 for last 3 connection
     * Point 2:
     * 7. FC should be generated successfully.
     * 8. local_hs_res should be 0
     * 9. Request should be sent to network and response should be received successfully.
     * </p>
     *
     * @throws Throwable
     */
    /**
     * Ignored due to bug in OC.
     * http://jira.seven.com/browse/ASMV-20697
     */

    public void test_007_FcReject() throws Throwable {
        if ((getMaxRejectNumber() == 3) && (getBlacklistPeriod() == 24)) {
            resetDB();
            ShellUtil.killAll(TFConstantsIF.OC_PROCESS_NAME);
            ShellUtil.killAll("occ");
            Thread.sleep(MIN_PERIOD);
            String resourceUrl = "https://tln-dev-testrunner1.7sys.eu/asimov_it_reject_mock_007" + SUITE;
            try {
                DnsUtil.resolveHost("tln-dev-testrunner1.7sys.eu");
            } catch (Exception e) {
                logger.info("Failed to resolve host \'tln-dev-testrunner1.7sys.eu\'");
            }
            PrepareResourceUtil.prepareResource(resourceUrl, false);
            FclPostponed fclPostponed = new FclPostponed();
            HttpsFCLTask httpsFCLTask = new HttpsFCLTask();
            FclLHSRTask fclLHSRTask = new FclLHSRTask();
            FcFcnTask fcFcnTask = new FcFcnTask();
            FcFckTask fcFckTask = new FcFckTask(2);
            LogcatUtil logcatUtil = null;
            try {
                PMSUtil.addPolicies(new Policy[]{new Policy("enabled", "true", "@asimov@application@com.seven.asimov.it@ssl", true)});
                Thread.sleep(MIN_PERIOD);

                logger.info("Point #1");
                logcatUtil = new LogcatUtil(getContext(), httpsFCLTask, fclLHSRTask, fcFcnTask, fclPostponed, fcFckTask);
                logcatUtil.start();
                sendSimultaneousHttpsRequest(2, 2, resourceUrl);
                logcatUtil.stop();
                assertTrue("FCL tasks should be postponed", checkFclPostponed(fclPostponed, 1));
                assertTrue("local_hs_res should be 0 for 2 connection", checkLocalHsRes(fclLHSRTask, "0", 2));
                assertTrue("local_hs_res should be -1 for 3 connection", checkLocalHsRes(fclLHSRTask, "-1", 3));
                assertFalse("ref_count should be incremented to 2", fcFckTask.getLogEntries().isEmpty());

                logger.info("Point #2");
                logcatUtil = new LogcatUtil(getContext(), fclLHSRTask);
                logcatUtil.start();
                sendHttpsRequest(1, resourceUrl);
                logcatUtil.stop();
                assertTrue("local_hs_res should be 0 for 1 connection", checkLocalHsRes(fclLHSRTask, "0"));

            } finally {
                if (logcatUtil != null) {
                    logcatUtil.stop();
                }
                PMSUtil.cleanPaths(new String[]{"@asimov@application@com.seven.asimov.it@ssl"});
                DateUtil.moveTime(25 * 60 * 60 * 1000);
                sendHttpsRequest(1, resourceUrl);
                PrepareResourceUtil.prepareResource(resourceUrl, true);
            }
        } else {
            throw new IllegalArgumentException("This test need to be executed with following parameters:\n"
                    + "client.openchannel.fakecert.max_reject_number=3\n" +
                    "client.openchannel.fakecert.blacklisting_period=24");
        }
    }

    /**
     * <p>Runtime detect of mock certificate rejecting by app should be performed only in case consecutive failings of local handshake
     * </p>
     * <p>Pre-requisites:
     * 1. Default branding properties:
     * client.openchannel.fakecert.max_reject_number=3
     * client.openchannel.fakecert.blacklisting_period=24
     * </p>
     * <p>Steps:
     * Point 1:
     * 1. Install OC
     * 2. Generate 1 https connection and close it after local handshake performing.
     * Point 2:
     * 3. Generate 1 https request.
     * Point 3:
     * 4. Generate 2 https connections one by one and close each of them after local handshake performing.
     * Point 4:
     * 5. Generate 1 https request.
     * </p>
     * <p>Expected reults:
     * Point 1:
     * 1. FCL task should be created and verdict should be FCN.
     * 2. FC should be generated successfully.
     * 3.  local_hs_res should be -1
     * Point 2:
     * 4. FCL task should be created and verdict should be FCN.
     * 5. FC should be generated successfully.
     * 6. local_hs_res should be 0
     * 7. Request should be sent to network and response should be received successfully.
     * Point 3:
     * 8. 2 FCL task should be created and verdict should be FCN.
     * 9. FC should be generated successfully.
     * 10. local_hs_res should be -1 for each connection
     * Point 4:
     * 11. FCL task should be created and verdict should be FCN.
     * 12. FC should be generated successfully.
     * 13. local_hs_res should be 0
     * 14. Request should be sent to network and response should be received successfully.
     * </p>
     *
     * @throws Throwable
     */

    public void test_008_FcReject() throws Throwable {
        if ((getMaxRejectNumber() == 3) && (getBlacklistPeriod() == 24)) {
            resetDB();
            ShellUtil.killAll(TFConstantsIF.OC_PROCESS_NAME);
            ShellUtil.killAll("occ");
            Thread.sleep(MIN_PERIOD);
            String resourceUrl = "https://hki-dev-testrunner4.7sys.eu/asimov_it_reject_mock_008" + SUITE;
            try {
                DnsUtil.resolveHost("hki-dev-testrunner4.7sys.eu");
            } catch (Exception e) {
                logger.info("Failed to resolve host \'hki-dev-testrunner4.7sys.eu\'");
            }
            PrepareResourceUtil.prepareResource(resourceUrl, false);
            HttpsFCLTask httpsFCLTask = new HttpsFCLTask();
            FclLHSRTask fclLHSRTask = new FclLHSRTask();
            FcFcnTask fcFcnTask = new FcFcnTask();
            LogcatUtil logcatUtil = null;
            try {
                PMSUtil.addPolicies(new Policy[]{new Policy("enabled", "true", "@asimov@application@com.seven.asimov.it@ssl", true)});
                Thread.sleep(MIN_PERIOD);

                logger.info("Point #1");
                logcatUtil = new LogcatUtil(getContext(), httpsFCLTask, fclLHSRTask, fcFcnTask);
                logcatUtil.start();
                sendRejectedHttpsRequest(1, resourceUrl);
                logcatUtil.stop();
                assertTrue("FCL tasks should be created and verdict for each should be FCN", isVerdictFCN(httpsFCLTask, fcFcnTask));
                assertTrue("local_hs_res should be -1", checkLocalHsRes(fclLHSRTask, "-1"));

                logger.info("Point #2");
                logcatUtil = new LogcatUtil(getContext(), httpsFCLTask, fclLHSRTask, fcFcnTask);
                logcatUtil.start();
                sendHttpsRequest(1, resourceUrl);
                logcatUtil.stop();
                assertTrue("FCL tasks should be created and verdict for each should be FCN", isVerdictFCN(httpsFCLTask, fcFcnTask));
                assertTrue("local_hs_res should be 0", checkLocalHsRes(fclLHSRTask, "0"));

                logger.info("Point #3");
                logcatUtil = new LogcatUtil(getContext(), httpsFCLTask, fclLHSRTask, fcFcnTask);
                logcatUtil.start();
                sendRejectedHttpsRequest(1, resourceUrl);
                logcatUtil.stop();
                assertTrue("FCL tasks should be created and verdict for each should be FCN", isVerdictFCN(httpsFCLTask, fcFcnTask));
                assertTrue("local_hs_res should be -1 for each connection", checkLocalHsRes(fclLHSRTask, "-1", 2));

                logger.info("Point #4");
                logcatUtil = new LogcatUtil(getContext(), httpsFCLTask, fclLHSRTask, fcFcnTask);
                logcatUtil.start();
                sendHttpsRequest(1, resourceUrl);
                logcatUtil.stop();
                assertTrue("FCL tasks should be created and verdict for each should be FCN", isVerdictFCN(httpsFCLTask, fcFcnTask));
                assertTrue("local_hs_res should be 0", checkLocalHsRes(fclLHSRTask, "0"));
            } finally {
                if (logcatUtil != null) {
                    logcatUtil.stop();
                }
                PMSUtil.cleanPaths(new String[]{"@asimov@application@com.seven.asimov.it@ssl"});
                DateUtil.moveTime(25 * 60 * 60 * 1000);
                sendHttpsRequest(1, resourceUrl);
                PrepareResourceUtil.prepareResource(resourceUrl, true);
            }
        } else {
            throw new IllegalArgumentException("This test need to be executed with following parameters:\n"
                    + "client.openchannel.fakecert.max_reject_number=3\n" +
                    "client.openchannel.fakecert.blacklisting_period=24");
        }
    }
}
