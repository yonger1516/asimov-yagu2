package com.seven.asimov.it.tests.generic.application.handler.keepalive;

import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.testcases.KeepAliveConditionTestCase;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.conditionTasks.FTMmessageTask;
import com.seven.asimov.it.utils.logcat.tasks.conditionTasks.KAStreamHistoryTask;
import com.seven.asimov.it.utils.logcat.tasks.conditionTasks.KAUpdateProfileTask;
import com.seven.asimov.it.utils.logcat.tasks.crcsTasks.NetlogTask;
import com.seven.asimov.it.utils.logcat.tasks.streamTasks.NaqForTrxTask;
import com.seven.asimov.it.utils.logcat.tasks.streamTasks.NarForHtrxTask;
import com.seven.asimov.it.utils.logcat.wrappers.NarForHtrxWrapper;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import junit.framework.AssertionFailedError;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class KaDetectLogicTests extends KeepAliveConditionTestCase {
    private static final Logger logger = LoggerFactory.getLogger(KaDetectLogicTests.class.getSimpleName());

    final static String KA_POLICY = "@asimov@application@com.seven.asimov.it@keepalive_params";
    private final String enginePath = "@asimov@failovers@restart@engine";
    private final String controllerPath = "@asimov@failovers@restart@controller";
    private final String dispatchersPath = "@asimov@failovers@restart@dispatchers";

    public void test_000_KaDetectLogic() throws Throwable {
        PMSUtil.addPolicies(new Policy[]{new Policy("enabled", "false", enginePath, true),
                new Policy("enabled", "false", controllerPath, true),
                new Policy("enabled", "false", dispatchersPath, true)});
        TestUtil.sleep(2 * 60 * 1000);
    }

    /**
     * Steps:
     * <p/>
     * 1. Set "csm_min_life_time" in "@asimov@application@com.seven.asimov.it@keepalive_params"
     * to value "60".
     * 2. Send two 10 bytes keep-alive packets with 300 sec interval (time between establishing connection
     * and sending keep-alive request is 60 sec).
     * <p/>
     * Expected results:
     * <p/>
     * 1. All packets belonging to the application should be marked as "KA" in the stream event history
     */

    public void test_001_KaDetectLogic() throws Throwable {
        performStreamHistoryCleanup();
        KAStreamHistoryTask kaStreamHistoryTask = new KAStreamHistoryTask();
        NetlogTask netlogTask = new NetlogTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), kaStreamHistoryTask, netlogTask);

        try {
            PMSUtil.addPolicies(new Policy[]{new Policy("csm_min_life_time", "60", KA_POLICY, true)});
            TestUtil.sleep(2 * 60 * 1000);
            logcatUtil.start();

            TestUtil.sleep(5 * 1000);

            int i = 1;
            sendKeepAlive(10, 60, i++);

            TestUtil.sleep(240 * 1000);

            sendKeepAlive(7, 60, i++);

            logcatUtil.stop();

            checkKaPresent(kaStreamHistoryTask, netlogTask, "KA");
        } finally {
            PMSUtil.cleanPaths(new String[]{KA_POLICY});
            TestUtil.sleep(3 * 1000);

            if (logcatUtil.isRunning())
                logcatUtil.stop();
        }

    }

    /**
     * Steps:
     * <p/>
     * 1. Set "csm_idle_time_after_stream" in "@asimov@application@com.seven.asimov.it@keepalive_params"
     * to value "0".
     * 2. Send 3 10 bytes keep-alive packets with 300 sec interval (time between establishing connection
     * and sending keep-alive request is 100 sec). The application should become keepalive.
     * 3. Send 200 bytes keep-alive packet.
     * <p/>
     * Expected results:
     * <p/>
     * 1. Stream history should contain (starting from most recent): ["NOT KA", "KA", "KA", "KA"]
     * 2. After receiving NAR(NSR) for first NOT KA dispatcher should continue acquire network access
     * with NAQ(NSQ) immediately.
     */

    public void test_002_KaDetectLogic() throws Throwable {
        performStreamHistoryCleanup();

        KAStreamHistoryTask kaStreamHistoryTask = new KAStreamHistoryTask();
        NetlogTask netlogTask = new NetlogTask();
        NaqForTrxTask naqForTrxTask = new NaqForTrxTask();
        NarForHtrxTask narForHtrxTask = new NarForHtrxTask();
        KAUpdateProfileTask kaUpdateProfileTask = new KAUpdateProfileTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), kaStreamHistoryTask, netlogTask, kaUpdateProfileTask,
                naqForTrxTask, narForHtrxTask);

        try {
            PMSUtil.addPolicies(new Policy[]{new Policy("csm_idle_time_after_stream", "0", KA_POLICY, true)});
            TestUtil.sleep(2 * 60 * 1000);

            TestUtil.sleep(5 * 1000);
            for (int i = 0; i < 3; i++) {
                sendKeepAlive(10, 100, i + 1);
                if (i != 2)
                    TestUtil.sleep(200 * 1000);
            }
            logcatUtil.start();
            TestUtil.sleep(5 * 1000);
            sendKeepAlive(200, 10, 4);

            TestUtil.sleep(7 * 1000);
            logcatUtil.stop();

            assertTrue("OC should update App Profile with KA detection status", !kaUpdateProfileTask.getLogEntries().isEmpty());
            assertTrue("There were no NAQ messages in the log", !naqForTrxTask.getLogEntries().isEmpty());
            assertTrue("There were no NAR messages in the log", !narForHtrxTask.getLogEntries().isEmpty());
            checkKaPresent(kaStreamHistoryTask, netlogTask, new String[]{"NOT KA", "KA", "KA", "KA"});

        } finally {
            PMSUtil.cleanPaths(new String[]{KA_POLICY});
            TestUtil.sleep(3 * 1000);

            if (logcatUtil.isRunning())
                logcatUtil.stop();
        }
    }

    /**
     * Steps:
     * <p/>
     * 1. Set "ka_idle_time" in "@asimov@application@com.seven.asimov.it@keepalive_params"
     * to value "60".
     * 2. Send two 10 bytes keep-alive packets with 60 sec interval (time between establishing connection
     * and sending keep-alive request is 100 sec).
     * <p/>
     * Expected results:
     * <p/>
     * 1. All packets belonging to the application should be marked as "KA" in the stream event history
     */

    public void test_003_KaDetectLogic() throws Throwable {
        performStreamHistoryCleanup();

        KAStreamHistoryTask kaStreamHistoryTask = new KAStreamHistoryTask();
        NetlogTask netlogTask = new NetlogTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), kaStreamHistoryTask, netlogTask);

        try {
            PMSUtil.addPolicies(new Policy[]{new Policy("ka_idle_time", "60", KA_POLICY, true)});
            TestUtil.sleep(2 * 60 * 1000);
            logcatUtil.start();

            TestUtil.sleep(5 * 1000);

            int i = 1;
            sendKeepAlive(10, 100, i++);

            TestUtil.sleep(60 * 1000);

            sendKeepAlive(10, 100, i++);

            TestUtil.sleep(7 * 1000);

            logcatUtil.stop();

            checkKaPresent(kaStreamHistoryTask, netlogTask, "KA");
        } finally {
            PMSUtil.cleanPaths(new String[]{KA_POLICY});
            TestUtil.sleep(3 * 1000);

            if (logcatUtil.isRunning())
                logcatUtil.stop();
        }

    }

    /**
     * Steps:
     * <p/>
     * 1. Set "csm_max_bytes_amount_after_stream" in "@asimov@application@com.seven.asimov.it@keepalive_params"
     * to value "0".
     * 2. Send 3 10 bytes keep-alive packets with 300 sec interval (time between establishing connection
     * and sending keep-alive request is 100 sec). The application should become keepalive.
     * 3. Send 200 bytes keep-alive packet.
     * <p/>
     * Expected results:
     * <p/>
     * 1. Stream history should contain (starting from most recent): ["NOT KA", "KA", "KA", "KA"]
     * 2. NAR and NAQ should be separated by 950 seconds in the log.
     * <p/>
     * IGNORED: due to the fact testrunner has 60 sec socket timeout while at least 300 seconds
     * required between keepalives
     */
    @Ignore
    public void test_004_KaDetectLogic() throws Throwable {
        performStreamHistoryCleanup();

        KAStreamHistoryTask kaStreamHistoryTask = new KAStreamHistoryTask();
        NetlogTask netlogTask = new NetlogTask();
        NaqForTrxTask naqForTrxTask = new NaqForTrxTask();
        NarForHtrxTask narForHtrxTask = new NarForHtrxTask();
        KAUpdateProfileTask kaUpdateProfileTask = new KAUpdateProfileTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), kaStreamHistoryTask, netlogTask, kaUpdateProfileTask,
                naqForTrxTask, narForHtrxTask);

        try {
            PMSUtil.addPolicies(new Policy[]{new Policy("csm_max_bytes_amount_after_stream", "0", KA_POLICY, true)});
            TestUtil.sleep(2 * 60 * 1000);

            for (int i = 0; i < 3; i++) {
                sendKeepAlive(10, 100, i + 1);
                TestUtil.sleep(200 * 1000);
            }

            logcatUtil.start();

            sendKeepAlive(200, 100, 4);

            TestUtil.sleep(955 * 1000);

            logcatUtil.stop();

            checkKaPresent(kaStreamHistoryTask, netlogTask, "KA");
            assertTrue("OC should update App Profile with KA detection status", !kaUpdateProfileTask.getLogEntries().isEmpty());
            assertTrue("There were no NAR messages in the log", !narForHtrxTask.getLogEntries().isEmpty());
            assertTrue("There were no NAQ messages in the log", !naqForTrxTask.getLogEntries().isEmpty());

            checkNoNaqAfterNar(kaStreamHistoryTask, netlogTask, naqForTrxTask, narForHtrxTask, 950 * 1000);
        } finally {
            PMSUtil.cleanPaths(new String[]{KA_POLICY});
            TestUtil.sleep(3 * 1000);

            if (logcatUtil.isRunning())
                logcatUtil.stop();
        }
    }

    /**
     * Steps:
     * <p/>
     * 1. Set "csm_max_bytes_amount" in "@asimov@application@com.seven.asimov.it@keepalive_params"
     * to value "300".
     * 2. Send two 100 bytes keep-alive packets with 300 sec interval (time between establishing connection
     * and sending keep-alive request is 100 sec).
     * <p/>
     * Expected results:
     * <p/>
     * 1. All packets belonging to the application should be marked as "KA" in the stream event history
     */

    public void test_005_KaDetectLogic() throws Throwable {
        performStreamHistoryCleanup();

        KAStreamHistoryTask kaStreamHistoryTask = new KAStreamHistoryTask();
        NetlogTask netlogTask = new NetlogTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), kaStreamHistoryTask, netlogTask);

        try {
            PMSUtil.addPolicies(new Policy[]{new Policy("csm_max_bytes_amount", "300", KA_POLICY, true)});
            TestUtil.sleep(2 * 60 * 1000);
            logcatUtil.start();

            TestUtil.sleep(5 * 1000);

            int i = 1;
            sendKeepAlive(100, 100, i++);

            TestUtil.sleep(200 * 1000);

            sendKeepAlive(100, 100, i++);

            TestUtil.sleep(7 * 1000);

            logcatUtil.stop();

            checkKaPresent(kaStreamHistoryTask, netlogTask, "KA");
        } finally {
            PMSUtil.cleanPaths(new String[]{KA_POLICY});
            TestUtil.sleep(3 * 1000);

            if (logcatUtil.isRunning())
                logcatUtil.stop();
        }

    }

    /**
     * Steps:
     * <p/>
     * 1. Set "min_ka_weight" in "@asimov@application@com.seven.asimov.it@keepalive_params"
     * to value "6".
     * 2. Send 6 keep-alive packets of 10 bytes with 300 sec interval (time between establishing connection
     * and sending keep-alive request is 100 sec).
     * <p/>
     * Expected results:
     * - All packets belonging to the application should be marked as "KA" in the stream event history
     * - FTM messages to dispatchers should be present
     * - OC should calculate filter id for current uid
     * - OC should update App Profile with KA detection status
     */

    public void test_006_KaDetectLogic() throws Throwable {
        performStreamHistoryCleanup();

        KAStreamHistoryTask kaStreamHistoryTask = new KAStreamHistoryTask();
        NetlogTask netlogTask = new NetlogTask();
        KAUpdateProfileTask kaUpdateProfileTask = new KAUpdateProfileTask();
        FTMmessageTask ftMmessageTask = new FTMmessageTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), kaStreamHistoryTask, netlogTask, kaUpdateProfileTask, ftMmessageTask);

        try {
            PMSUtil.addPolicies(new Policy[]{new Policy("min_ka_weight", "6", KA_POLICY, true)});
            TestUtil.sleep(2 * 60 * 1000);
            logcatUtil.start();

            TestUtil.sleep(5 * 1000);

            for (int i = 0; i < 6; i++) {
                sendKeepAlive(10, 100, i + 1);
                if (i != 5)
                    TestUtil.sleep(200 * 1000);
            }

            TestUtil.sleep(7 * 1000);

            logcatUtil.stop();

            checkKaPresent(kaStreamHistoryTask, netlogTask, "KA");
            assertTrue("There were no FTM messages to dispatchers.", !ftMmessageTask.getLogEntries().isEmpty());
            assertTrue("OC should update App Profile with KA detection status", !kaUpdateProfileTask.getLogEntries().isEmpty());
        } finally {
            PMSUtil.cleanPaths(new String[]{KA_POLICY});
            TestUtil.sleep(3 * 1000);

            if (logcatUtil.isRunning())
                logcatUtil.stop();
        }

    }

    /**
     * Steps:
     * <p/>
     * 1. Set "csm_idle_time_after_stream" in "@asimov@application@com.seven.asimov.it@keepalive_params"
     * to value "4" and "csm_max_bytes_amount_after_stream" to "0".
     * 2. Send 3 10 bytes keep-alive packets with 300 sec interval (time between establishing connection
     * and sending keep-alive request is 100 sec). The application should become keepalive.
     * 3. Send 200 bytes keep-alive packet.
     * <p/>
     * Expected results:
     * 1. Stream history should contain (starting from most recent): ["NOT KA", "KA", "KA", "KA"]
     * 2. NAR with idle time entry equal to "4" should appear in the log.
     */

    public void test_007_KaDetectLogic() throws Throwable {
        performStreamHistoryCleanup();

        KAStreamHistoryTask kaStreamHistoryTask = new KAStreamHistoryTask();
        NetlogTask netlogTask = new NetlogTask();
        NarForHtrxTask narForHtrxTask = new NarForHtrxTask();
        KAUpdateProfileTask kaUpdateProfileTask = new KAUpdateProfileTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), kaStreamHistoryTask, netlogTask, kaUpdateProfileTask, narForHtrxTask);

        try {
            PMSUtil.addPolicies(new Policy[]{new Policy("csm_idle_time_after_stream", "4", KA_POLICY, true),
                    new Policy("csm_max_bytes_amount_after_stream", "0", KA_POLICY, true)});
            TestUtil.sleep(2 * 60 * 1000);

            for (int i = 0; i < 3; i++) {
                sendKeepAlive(10, 100, i + 1);
                if (i != 2)
                    TestUtil.sleep(200 * 1000);
            }

            logcatUtil.start();

            sendKeepAlive(200, 3, 4);

            TestUtil.sleep(7 * 1000);
            logcatUtil.stop();

            checkKaPresent(kaStreamHistoryTask, netlogTask, new String[]{"NOT KA", "KA", "KA", "KA"});
            assertTrue("OC should update App Profile with KA detection status", !kaUpdateProfileTask.getLogEntries().isEmpty());
            assertTrue("There were no NAR messages in the log", !narForHtrxTask.getLogEntries().isEmpty());

            NarForHtrxWrapper narForHtrxWrapper = narForHtrxTask.getLogEntries().get(0);
            assertEquals("4", narForHtrxWrapper.getIdleTime());
        } finally {
            PMSUtil.cleanPaths(new String[]{KA_POLICY});
            TestUtil.sleep(3 * 1000);

            if (logcatUtil.isRunning())
                logcatUtil.stop();
        }
    }

    public void test_008_KaDetectLogic() throws Throwable {
        PMSUtil.cleanPaths(new String[]{enginePath});
        PMSUtil.cleanPaths(new String[]{controllerPath});
        PMSUtil.cleanPaths(new String[]{dispatchersPath});
        TestUtil.sleep(60 * 1000);
    }

    @Override
    protected void runTest() throws Throwable {
        boolean isPassed;
        int numberOfAttempts = 0;
        List<String> counts = new ArrayList<String>();
        do {
            isPassed = true;
            numberOfAttempts++;
            try {
                super.runTest();
            } catch (AssertionFailedError assertionFailedError) {
                logger.debug("Test failed due to " + ExceptionUtils.getStackTrace(assertionFailedError));
                isPassed = false;
                counts.add("Test failed due to AssertionFailedError in " + numberOfAttempts + " attempt");
                Thread.sleep(2 * 60 * 1000);
            }
        } while (!isPassed && numberOfAttempts < 3);
        assertTrue("The test was failed three times ", counts.size() != 3);
    }

}