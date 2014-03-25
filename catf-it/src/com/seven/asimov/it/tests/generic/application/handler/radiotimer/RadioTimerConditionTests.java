package com.seven.asimov.it.tests.generic.application.handler.radiotimer;

import com.seven.asimov.it.testcases.TimerConditionTestCase;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.pms.PMSUtil;
import junit.framework.AssertionFailedError;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RadioTimerConditionTests extends TimerConditionTestCase {
    private static final Logger logger = LoggerFactory.getLogger(RadioTimerConditionTests.class.getSimpleName());

    /**
     * <p>Verify if radio timer state is changed to active with delay
     * </p>
     * <p>Pre-requisites:
     * 1. Rooted, or EOM pre-installed device.
     * 2. Specify policy rule:
     * asimov@application@<package_name>@scripts@<script_name>@conditions@radio_timer=30
     * </p>
     * <p>Steps:
     * 1. Install OC
     * 2. 3g connection is active connection.
     * 3. Wait for 30 sec and ensure that Radio is Down.
     * 4. Observe client log.
     * </p>
     * <p>Expected result:
     * 1. Policy should be received and should be applied.
     * 2. Radio timer state should be changed to active in 30 sec.
     * </p>
     *
     * @throws Throwable
     */

    public void test_001_RadioTimerConditionChangedToActive() throws Throwable {
        prepareRadioTimerConditionTest();
        logger.info("Start logcat");
        try {
            logcatUtil.start();
            pushPolicy(POLICY_NAME, "30", POLICY_PATH, TIME_PAUSE);
            logcatUtil.stop();
            assertTrue("Radio timer state has not changed to active in 30 sec", isRadioTimerActivatedByTimer());
        } finally {
            logger.info("Clear and stop");
            PMSUtil.cleanPaths(new String[]{POLICY_PATH});
        }
    }

    /**
     * <p>Verify if radio timer state is changed to active with delay
     * </p>
     * <p>Pre-requisites:
     * 1. Rooted, or EOM pre-installed device.
     * 2. Specify policy rule:
     * asimov@application@<package_name>@scripts@<script_name>@conditions@radio_timer=60
     * </p>
     * <p>Steps:
     * 1. Install OC
     * 2. 3g connection is active connection.
     * 3. Edit available policy in such way and push changes:
     * asimov@application@<package_name>@scripts@<script_name>@conditions@radio_timer=30
     * 4. Wait for 30 sec
     * 5. Observe client log.
     * </p>
     * <p>Expected result:
     * 1. Policy should be received and should be applied.
     * 2. Radio timer state should be changed to active in 30 sec after new policy adding.
     * </p>
     *
     * @throws Throwable
     */

    public void test_002_RadioTimerConditionChangedToActive() throws Throwable {
        prepareRadioTimerConditionTest();
        logger.info("Start logcat");
        try {
            logcatUtil.start();
            pushPolicy(POLICY_NAME, "60", POLICY_PATH, TIME_PAUSE);
            PMSUtil.cleanPaths(new String[]{POLICY_PATH});
            timerAddedTaskByTimer.reset();
            pushPolicy(POLICY_NAME, "30", POLICY_PATH, TIME_PAUSE);
            logcatUtil.stop();
            assertTrue("Radio timer state has not changed to active in 30 sec after new policy adding", isRadioTimerActivatedByTimer());
        } finally {
            logger.info("Clear and stop");
            PMSUtil.cleanPaths(new String[]{POLICY_PATH});
        }
    }


    /**
     * <p>Verify if radio timer state is not changed to active
     * </p>
     * <p>Pre-requisites:
     * 1. Rooted, or EOM pre-installed device.
     * 2. Specify policy rule:
     * asimov@application@<package_name>@scripts@<script_name>@conditions@radio_timer=60
     * </p>
     * <p>Steps:
     * 1. Install OC
     * 2. 3g connection is active connection.
     * 3. Delete policy rule for radio timer conditions
     * 4. Wait for 60 sec.
     * 5. Observe client log.
     * </p>
     * <p>Expected result:
     * 1. Policy should be received and should be applied.
     * 2. Radio timer state should not be changed to active in 60 sec after policy deleting.
     * </p>
     *
     * @throws Throwable
     */

    public void test_003_RadioTimerConditionIsNotChangedToActive() throws Throwable {
        prepareRadioTimerConditionTest();
        logger.info("Start logcat");
        try {
            logcatUtil.start();
            pushPolicy(POLICY_NAME, "60", POLICY_PATH, TIME_PAUSE);
            logcatUtil.stop();
            PMSUtil.cleanPaths(new String[]{POLICY_PATH});
            logcatUtil = new LogcatUtil(getContext(), timerAddedTaskByTimer);
            logcatUtil.start();
            Thread.sleep(60 * 1000);
            logcatUtil.stop();
            assertFalse("Radio timer state has been changed to active in 60 sec after policy deleting", isRadioTimerActivatedByTimer());
        } finally {
            logger.info("Clear and stop");
            PMSUtil.cleanPaths(new String[]{POLICY_PATH});
        }
    }

    /**
     * <p>Verify if radio timer state is not changed to active
     * </p>
     * <p>Pre-requisites:
     * 1. Rooted, or EOM pre-installed device.
     * 2. Specify policy rule:
     * asimov@application@<package_name>@scripts@<script_name>@conditions@radio_timer=30
     * </p>
     * <p>Steps:
     * 1. Install OC
     * 2. 3g connection is active connection.
     * 3. Edit available policy in such way and push changes:
     * asimov@application@<package_name>@scripts@<script_name>@conditions@radio_timer=60
     * 4. Wait for 30 sec.
     * 5. Observe client log.
     * </p>
     * <p>Expected result:
     * 1. Policy should be received and should be applied.
     * 2. Radio timer state should not be changed to active in 30 sec after policy deleting.
     * </p>
     *
     * @throws Throwable
     */

    public void test004_RadioTimerConditionIsNotChangedToActive() throws Throwable {
        prepareRadioTimerConditionTest();
        logger.info("Start logcat");
        try {
            logcatUtil.start();
            pushPolicy(POLICY_NAME, "30", POLICY_PATH, TIME_PAUSE);
            PMSUtil.cleanPaths(new String[]{POLICY_PATH});
            pushPolicy(POLICY_NAME, "60", POLICY_PATH, TIME_PAUSE);
            logcatUtil.stop();
            PMSUtil.cleanPaths(new String[]{POLICY_PATH});
            logcatUtil = new LogcatUtil(getContext(), timerAddedTaskByTimer);
            logcatUtil.start();
            Thread.sleep(30 * 1000);
            logcatUtil.stop();
            assertFalse("Radio timer state has been changed to active in 30 sec after policy deleting", isRadioTimerActivatedByTimer());
        } finally {
            logger.info("Clear and stop");
            PMSUtil.cleanPaths(new String[]{POLICY_PATH});
        }
    }

    /**
     * <p>Verify if radio timer state is changed to active when Radio is Up
     * </p>
     * <p>Pre-requisites:
     * 1. Rooted, or EOM pre-installed device.
     * 2. Specify policy rule:
     * asimov@application@<package_name>@scripts@<script_name>@conditions@radio_timer=30
     * </p>
     * <p>Steps:
     * 1. Install OC
     * 2. 3g connection is active connection.
     * 3. Start active browsing
     * 4. Ensure that Radio is Up before 30 sec expired.
     * 5. Observe client log.
     * </p>
     * <p>Expected result:
     * 1. Policy should be received and should be applied.
     * 2. Radio timer state should be changed to active when Radio is Up.
     * </p>
     *
     * @throws Throwable
     */
    //@Execute
    public void test_010_RadioTimerActiveWhenRadioUp() throws Throwable {
        Thread radioUp;
        prepareRadioTimerConditionTest();
        logger.info("Start logcat");
        try {
            logcatUtil.start();
            radioUp = getRadioKeep();
            radioUp.start();
            pushPolicy(POLICY_NAME, "30", POLICY_PATH, TIME_PAUSE);
            logcatUtil.stop();
            assertTrue("Radio timer state has not changed to active when Radio is Up", isRadioTimerActivatedByRadio());
            radioUp.interrupt();
        } catch (InterruptedException e) {
            //Ignored
        } finally {
            logger.info("Clear and stop");
            PMSUtil.cleanPaths(new String[]{POLICY_PATH});
        }
    }

    /**
     * <p>Verify if radio timer state is changed to inactive when Radio is Down
     * </p>
     * <p>Pre-requisites:
     * 1. Rooted, or EOM pre-installed device.
     * 2. Specify policy rule:
     * asimov@application@<package_name>@scripts@<script_name>@conditions@radio_timer=60
     * </p>
     * <p>Steps:
     * 1. Install OC
     * 2. 3g connection is active connection.
     * 3. Start active browsing and ensure that Radio is Up.
     * 4. Stop browsing.
     * 5. Ensure that Radio is Down before 60 sec expired.
     * 6. Observe client log.
     * </p>
     * <p>Expected result:
     * 1. Policy should be received and should be applied.
     * 2. Radio timer state should be changed to inactive when Radio is Down.
     * </p>
     *
     * @throws Throwable
     */

    public void test011_RadioTimerInactiveWhenRadioDown() throws Throwable {
        prepareRadioTimerConditionTest();
        logger.info("Start logcat");
        try {
            logcatUtil.start();
            pushPolicy(POLICY_NAME, "60", POLICY_PATH, TIME_PAUSE);
            logcatUtil.stop();
            assertTrue("Radio timer state has not changed to inactive when Radio is Down", isRadioTimerDeactivatedByRadio());
        } finally {
            logger.info("Clear and stop");
            PMSUtil.cleanPaths(new String[]{POLICY_PATH});

        }
    }

    /**
     * <p>Verify if radio timer state is changed to active when Radio is Up
     * </p>
     * <p>Pre-requisites:
     * 1. Rooted, or EOM pre-installed device.
     * 2. Specify policy rule:
     * asimov@application@<package_name>@scripts@<script_name>@conditions@radio_timer=60
     * asimov@application@<package_name>@scripts@<script_name>@exit_conditions@radio_timer=30
     * </p>
     * <p>Steps:
     * 1. Install OC
     * 2. WiFi or 3g connection is active connection.
     * 3. Wait for 90 sec and ensure that timer state is changed from active state to inactive state.
     * 4. Start active browsing
     * 5. Ensure that Radio is Up before 60 sec expired.
     * </p>
     * <p>Expected result:
     * 1. Policy should be received and should be applied.
     * 2. Radio timer state should be changed to active when Radio is Up at step 5.
     * </p>
     *
     * @throws Throwable
     */

    public void test012_RadioTimerChangedToActiveWhenRadioUp() throws Throwable {
        Thread radioUp;
        prepareRadioTimerConditionTest();
        logger.info("Start logcat");
        try {
            logcatUtil.start();
            radioUp = getRadioKeep();
            radioUp.start();
            pushPolicy(POLICY_NAME, "60", POLICY_PATH, TIME_PAUSE);
            pushPolicy(POLICY_NAME, "30", POLICY_EXIT_PATH, TIME_PAUSE);
            logcatUtil.stop();
            assertTrue("Radio timer state has not changed to active when Radio is Up", isRadioTimerActivatedByRadio());
            radioUp.interrupt();
        } catch (InterruptedException e) {
            //Ignored
        } finally {
            logger.info("Clear and stop");
            PMSUtil.cleanPaths(new String[]{POLICY_PATH, POLICY_EXIT_PATH});

        }
    }

    /**
     * <p>Verify if radio timer state is changed to inactive when Radio is Down
     * </p>
     * <p>Pre-requisites:
     * 1. Rooted, or EOM pre-installed device.
     * 2. Specify policy rule:
     * asimov@application@<package_name>@scripts@<script_name>@conditions@radio_timer=60
     * asimov@application@<package_name>@scripts@<script_name>@exit_conditions@radio_timer=30
     * </p>
     * <p>Steps:
     * 1. Install OC
     * 2. WiFi or 3g connection is active connection.
     * 3. Wait for 90 sec and ensure that timer state is changed from active state to inactive state.
     * 4. Start active browsing
     * 5. Stop browsing.
     * </p>
     * <p>Expected result:
     * 1. Policy should be received and should be applied.
     * 2. Radio timer state should be changed to inactive when Radio is Down
     * </p>
     *
     * @throws Throwable
     */
    //@Execute
    public void test013_RadioTimerChangedWhenRadioDown() throws Throwable {
        prepareRadioTimerConditionTest();
        logger.info("Start logcat");
        try {
            logcatUtil.start();
            pushPolicy(POLICY_NAME, "60", POLICY_PATH, TIME_PAUSE);
            pushPolicy(POLICY_NAME, "30", POLICY_EXIT_PATH, TIME_PAUSE);
            logcatUtil.stop();
            assertTrue("Radio timer state has not changed to inactive when Radio is Down", isRadioTimerDeactivatedByRadio());
        } finally {
            logger.info("Clear and stop");
            PMSUtil.cleanPaths(new String[]{POLICY_PATH, POLICY_EXIT_PATH});
        }
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
