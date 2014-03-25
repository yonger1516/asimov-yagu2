package com.seven.asimov.it.tests.generic.application.handler.timer;

import com.seven.asimov.it.testcases.TimerConditionTestCase;
import com.seven.asimov.it.utils.MobileNetworkUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.conditionTasks.*;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import junit.framework.AssertionFailedError;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TimerConditionTests extends TimerConditionTestCase {
    private static final Logger logger = LoggerFactory.getLogger(TimerConditionTestCase.class.getSimpleName());
    private MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());

    /**
     * <p>Verify if timer state is changed to  Entered with delay
     * </p>
     * <p>Pre-requisites:
     * 1. Rooted, or EOM pre-installed device.
     * 2. Specify policy rule:
     * asimov@application@com.seven.asimov@scripts@script1@conditions@timer=30
     * </p>
     * <p>Steps:
     * 1. Set WiFi active connection
     * 3. Wait for 30 sec.
     * 4. Observe client log.
     * </p>
     * <p>Expected results:
     * 1. Policy should be received and should be applied.
     * 2. Script should be initialized and timer state should be changed from Disabled to Exited.
     * 3. Timer state should be changed from Exited to Entered in 30 sec.
     * </p>
     *
     * @throws Throwable
     */

    public void test_001_TimerConditionASimpleActivating() throws Throwable {
        mobileNetworkUtil.onWifiOnly();
        TimerConditionDisabledExitedStateTask timerConditionDisabledExitedStateTask = new TimerConditionDisabledExitedStateTask();
        TimerConditionExitedEnteredStateTask timerConditionExitedEnteredStateTask = new TimerConditionExitedEnteredStateTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), timerConditionDisabledExitedStateTask, timerConditionExitedEnteredStateTask);
        logcatUtil.start();
        try {
            PMSUtil.addPolicies(new Policy[]{new Policy("timer", "30", POLICY_TIMER_CONDITIONS, true)});
            Thread.sleep(MINUTE + MIN_PERIOD);
            logcatUtil.stop();
            assertFalse("Script should be initialized and timer state should be changed from Disabled to Exited",
                    timerConditionDisabledExitedStateTask.getLogEntries().isEmpty());
            assertFalse("Timer state should be changed from Exited to Entered in 30 sec",
                    timerConditionExitedEnteredStateTask.getLogEntries().isEmpty());
        } finally {
            PMSUtil.cleanPaths(new String[]{POLICY_TIMER_CONDITIONS});
        }
    }

    /**
     * <p>Verify if timer state is changed to  Entered with new delay
     * </p>
     * <p>Pre-requisites:
     * 1. Specify policy rule:
     * asimov@application@com.seven.asimov@scripts@script1@conditions@timer=60
     * </p>
     * <p>Steps:
     * 1. Set WiFi active connection
     * 2. Edit available policy in such way and push changes:
     * asimov@application@com.seven.asimov@scripts@script1@conditions@timer=30
     * 3. Wait for 30 sec.
     * 4. Observe client log.
     * </p>
     * <p>Expected results:
     * 1. Policy should be received and should be applied.
     * 2. Script should be initialized and timer state should be changed from Disabled to Exited.
     * 3. New policy should be received and should be applied.
     * 4. Script should be destroyed and timer state should be changed from Exited to Disabled.
     * 5. Script should be initialized and timer state should be changed from Disabled to Exited.
     * 6. Timer state should be changed from Exited to Entered in 30 sec.
     * </p>
     *
     * @throws Throwable
     */

    public void test_002_TimerConditionChangingRule() throws Throwable {
        mobileNetworkUtil.onWifiOnly();
        TimerConditionDisabledExitedStateTask timerConditionDisabledExitedStateTask = new TimerConditionDisabledExitedStateTask();
        TimerConditionExitedEnteredStateTask timerConditionExitedEnteredStateTask = new TimerConditionExitedEnteredStateTask();
        TimerConditionExitedDisabledStateTask timerConditionExitedDisabledStateTask = new TimerConditionExitedDisabledStateTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), timerConditionDisabledExitedStateTask);
        logcatUtil.start();
        try {
            PMSUtil.addPolicies(new Policy[]{new Policy("timer", "600", POLICY_TIMER_CONDITIONS, true)});
            Thread.sleep(MINUTE + MIN_PERIOD);
            logcatUtil.stop();
            assertFalse("Script should be initialized and timer state should be changed from Disabled to Exited",
                    timerConditionDisabledExitedStateTask.getLogEntries().isEmpty());

            logcatUtil = new LogcatUtil(getContext(), timerConditionExitedDisabledStateTask);
            logcatUtil.start();
            PMSUtil.cleanPaths(new String[]{POLICY_TIMER_CONDITIONS});
            Thread.sleep(MINUTE);
            logcatUtil.stop();
            assertFalse("Script should be destroyed and timer state should be changed from Exited to Disabled",
                    timerConditionExitedDisabledStateTask.getLogEntries().isEmpty());

            logcatUtil = new LogcatUtil(getContext(), timerConditionDisabledExitedStateTask, timerConditionExitedEnteredStateTask);
            logcatUtil.start();
            PMSUtil.addPolicies(new Policy[]{new Policy("timer", "30", POLICY_TIMER_CONDITIONS, true)});
            Thread.sleep(MINUTE + MIN_PERIOD);
            logcatUtil.stop();
            assertFalse("Script should be initialized and timer state should be changed from Disabled to Exited",
                    timerConditionDisabledExitedStateTask.getLogEntries().isEmpty());
            assertFalse("Timer state should be changed from Exited to Entered in 30 sec",
                    timerConditionExitedEnteredStateTask.getLogEntries().isEmpty());
        } finally {
            PMSUtil.cleanPaths(new String[]{POLICY_TIMER_CONDITIONS});
        }
    }

    /**
     * <p>Verify if timer state is not changed to  Entered
     * </p>
     * <p>Pre-requisites:
     * 1.Specify policy rule:
     * asimov@application@com.seven.asimov@scripts@script1@conditions@timer=60
     * </p>
     * <p>Steps:
     * 1. Delete policy rule for timer conditions
     * 2. Wait for 60 sec.
     * 3. Observe client log.
     * </p>
     * <p>Expected results:
     * 1. Policy should be received and should be applied.
     * 2. Script should be initialized and timer state should be changed from Disabled to Exited.
     * 3. New policy should be received and should be applied.
     * 4. Script should be destroyed and timer state should be changed from Exited to Disabled.
     * 5. Timer state should not be changed from Exited to Entered
     * </p>
     *
     * @throws Throwable
     */

    public void test_003_TimerConditionConditionAfterRemoving() throws Throwable {
        mobileNetworkUtil.onWifiOnly();
        TimerConditionDisabledExitedStateTask timerConditionDisabledExitedStateTask = new TimerConditionDisabledExitedStateTask();
        TimerConditionExitedEnteredStateTask timerConditionExitedEnteredStateTask = new TimerConditionExitedEnteredStateTask();
        TimerConditionExitedDisabledStateTask timerConditionExitedDisabledStateTask = new TimerConditionExitedDisabledStateTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), timerConditionDisabledExitedStateTask, timerConditionExitedEnteredStateTask);
        logcatUtil.start();
        try {
            PMSUtil.addPolicies(new Policy[]{new Policy("timer", "600", POLICY_TIMER_CONDITIONS, true)});
            Thread.sleep(MINUTE + MIN_PERIOD);
            logcatUtil.stop();
            assertFalse("Script should be initialized and timer state should be changed from Disabled to Exited",
                    timerConditionDisabledExitedStateTask.getLogEntries().isEmpty());
            assertTrue("Timer state should not be changed from Exited to Entered",
                    timerConditionExitedEnteredStateTask.getLogEntries().isEmpty());
            logcatUtil = new LogcatUtil(getContext(), timerConditionExitedDisabledStateTask, timerConditionExitedEnteredStateTask);
            logcatUtil.start();
            PMSUtil.cleanPaths(new String[]{POLICY_TIMER_CONDITIONS});
            Thread.sleep(MINUTE);
            logcatUtil.stop();
            assertFalse("Script should be destroyed and timer state should be changed from Exited to Disabled",
                    timerConditionExitedDisabledStateTask.getLogEntries().isEmpty());
            assertTrue("Timer state should not be changed from Exited to Entered",
                    timerConditionExitedEnteredStateTask.getLogEntries().isEmpty());
        } finally {
            PMSUtil.cleanPaths(new String[]{POLICY_TIMER_CONDITIONS});
        }
    }

    /**
     * <p>Verify if timer state is not changed to  Entered
     * </p>
     * <p>Pre-requisites:
     * 1. Specify policy rule:
     * asimov@application@<package_name>@scripts@<script_name>@conditions@timer=30
     * </p>
     * <p>Steps:
     * 1. Install OC
     * 2. WiFi or 3g connection is active connection.
     * 3. Edit available policy in such way and push changes:
     * asimov@application@<package_name>@scripts@<script_name>@conditions@timer=60
     * 3. Wait for 60 sec.
     * 4. Observe client log.
     * </p>
     * <p>Expected results:
     * 1. Policy should be received and should be applied.
     * 2. Script should be initialized and timer state should be changed from Disabled to Exited.
     * 3. New policy should be received and should be applied.
     * 4. Script should be destroyed and timer state should be changed from Exited to Disabled.
     * 5. Script should be initialized and timer state should be changed from Disabled to Exited.
     * 6. Timer state should be changed from Exited to Entered in 60 sec
     * </p>
     *
     * @throws Throwable
     */

    public void test_004_TimerConditionChangingRule() throws Throwable {
        mobileNetworkUtil.onWifiOnly();
        TimerConditionDisabledExitedStateTask timerConditionDisabledExitedStateTask = new TimerConditionDisabledExitedStateTask();
        TimerConditionExitedEnteredStateTask timerConditionExitedEnteredStateTask = new TimerConditionExitedEnteredStateTask();
        TimerConditionExitedDisabledStateTask timerConditionExitedDisabledStateTask = new TimerConditionExitedDisabledStateTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), timerConditionDisabledExitedStateTask);
        logcatUtil.start();
        try {
            PMSUtil.addPolicies(new Policy[]{new Policy("timer", "300", POLICY_TIMER_CONDITIONS, true)});
            Thread.sleep(MINUTE + MIN_PERIOD);
            logcatUtil.stop();
            assertFalse("Script should be initialized and timer state should be changed from Disabled to Exited",
                    timerConditionDisabledExitedStateTask.getLogEntries().isEmpty());

            logcatUtil = new LogcatUtil(getContext(), timerConditionExitedDisabledStateTask);
            logcatUtil.start();
            PMSUtil.cleanPaths(new String[]{POLICY_TIMER_CONDITIONS});
            Thread.sleep(MINUTE);
            logcatUtil.stop();
            assertFalse("Script should be destroyed and timer state should be changed from Exited to Disabled",
                    timerConditionExitedDisabledStateTask.getLogEntries().isEmpty());

            logcatUtil = new LogcatUtil(getContext(), timerConditionDisabledExitedStateTask, timerConditionExitedEnteredStateTask);
            logcatUtil.start();
            PMSUtil.addPolicies(new Policy[]{new Policy("timer", "60", POLICY_TIMER_CONDITIONS, true)});
            Thread.sleep(2 * MINUTE + MIN_PERIOD);
            logcatUtil.stop();
            assertFalse("Script should be initialized and timer state should be changed from Disabled to Exited",
                    timerConditionDisabledExitedStateTask.getLogEntries().isEmpty());
            assertFalse("Timer state should be changed from Exited to Entered in 60 sec",
                    timerConditionExitedEnteredStateTask.getLogEntries().isEmpty());
        } finally {
            PMSUtil.cleanPaths(new String[]{POLICY_TIMER_CONDITIONS});
        }
    }

    /**
     * <p>Verify if timer state is not changed to  Exited
     * </p>
     * <p>Pre-requisites:
     * 1.Specify policy rule: asimov@application@<package_name>@scripts@<script_name>@conditions@timer=30
     * </p>
     * <p>Steps:
     * 1.Wait for 2 hours.
     * </p>
     * <p>Expected results:
     * 1. Policy should be received and should be applied.
     * 2. Script should be initialized and timer state should be changed from Disabled to Exited.
     * 3. Timer state should be changed from Exited to Entered in 30 sec.
     * 4. Timer state should not be changed from Entered to Exited
     * </p>
     *
     * @throws Throwable
     */
    //@Execute
    public void test_005_TimerConditionLargeDelay() throws Throwable {
        mobileNetworkUtil.onWifiOnly();
        TimerConditionDisabledExitedStateTask timerConditionDisabledExitedStateTask = new TimerConditionDisabledExitedStateTask();
        TimerConditionExitedEnteredStateTask timerConditionExitedEnteredStateTask = new TimerConditionExitedEnteredStateTask();
        TimerConditionEnteredExitedStateTask timerConditionEnteredExitedStateTask = new TimerConditionEnteredExitedStateTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), timerConditionDisabledExitedStateTask,
                timerConditionExitedEnteredStateTask, timerConditionEnteredExitedStateTask);
        logcatUtil.start();
        try {
            PMSUtil.addPolicies(new Policy[]{new Policy("timer", "30", POLICY_TIMER_CONDITIONS, true)});
            Thread.sleep(15 * MINUTE + MIN_PERIOD);
            logcatUtil.stop();
            assertFalse("Script should be initialized and timer state should be changed from Disabled to Exited",
                    timerConditionDisabledExitedStateTask.getLogEntries().isEmpty());
            assertFalse("Timer state should be changed from Exited to Entered in 30 sec",
                    timerConditionExitedEnteredStateTask.getLogEntries().isEmpty());
            assertTrue("Timer state should not be changed from Entered to Exited", timerConditionEnteredExitedStateTask.getLogEntries().isEmpty());
        } finally {
            PMSUtil.cleanPaths(new String[]{POLICY_TIMER_CONDITIONS});
        }
    }

    /**
     * <p>Verify if timer state is changed to  Exited with delay
     * </p>
     * <p>Pre-requisites:
     * 1.Specify policy rule:
     * asimov@application@<package_name>@scripts@<script_name>@conditions@timer=30
     * asimov@application@<package_name>@scripts@<script_name>@exit_conditions@timer=60
     * </p>
     * <p>Steps:
     * 1.Wait for 30 sec and ensure that timer state is active.
     * 4. Wait for 60 sec.
     * </p>
     * <p>Expected results:
     * 1. Policy should be received and should be applied.
     * 2. Script should be initialized and timer state should be changed from Disabled to Exited.
     * 3. Timer state should be changed from Exited to Entered in 30 sec.
     * 4. Timer state should be changed from Entered to Exited in 60 sec.
     * </p>
     *
     * @throws Throwable
     */

    public void test_006_TimerConditionSimpleExitCondition() throws Throwable {
        mobileNetworkUtil.onWifiOnly();
        TimerConditionDisabledExitedStateTask timerConditionDisabledExitedStateTask = new TimerConditionDisabledExitedStateTask();
        TimerConditionExitedEnteredStateTask timerConditionExitedEnteredStateTask = new TimerConditionExitedEnteredStateTask();
        TimerConditionEnteredExitedStateTask timerConditionEnteredExitedStateTask = new TimerConditionEnteredExitedStateTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), timerConditionDisabledExitedStateTask,
                timerConditionExitedEnteredStateTask, timerConditionEnteredExitedStateTask);
        logcatUtil.start();
        try {
            PMSUtil.addPolicies(new Policy[]{new Policy("timer", "30", POLICY_TIMER_CONDITIONS, true)});
            PMSUtil.addPolicies(new Policy[]{new Policy("timer", "60", POLICY_TIMER_EXIT_CONDITIONS, true)});
            Thread.sleep(2 * MINUTE + MIN_PERIOD);
            logcatUtil.stop();
            assertFalse("Script should be initialized and timer state should be changed from Disabled to Exited",
                    timerConditionDisabledExitedStateTask.getLogEntries().isEmpty());
            assertFalse("Timer state should be changed from Exited to Entered in 30 sec",
                    timerConditionExitedEnteredStateTask.getLogEntries().isEmpty());
            assertFalse("Timer state should be changed from Entered to Exited in 60 sec",
                    timerConditionEnteredExitedStateTask.getLogEntries().isEmpty());
        } finally {
            PMSUtil.cleanPaths(new String[]{POLICY_TIMER_CONDITIONS, POLICY_TIMER_EXIT_CONDITIONS});
        }
    }

    /**
     * <p>Verify if timer state is changed to Exited with delay
     * </p>
     * <p>Pre-requisites:
     * 1.Specify policy rule:
     * asimov@application@<package_name>@scripts@<script_name>@conditions@timer=30
     * asimov@application@<package_name>@scripts@<script_name>@exit_conditions@timer=120
     * </p>
     * <p>Steps:
     * 1. Wait for 30 sec.
     * 2. Edit available policy in such way and push changes:
     * asimov@application@<package_name>@scripts@<script_name>@exit_conditions@timer=60
     * 3. Wait for 90 sec.
     * </p>
     * <p>Expected results:
     * 1. Policy should be received and should be applied.
     * 2. Script should be initialized and timer state should be changed from Disabled to Exited.
     * 3. Timer state should be changed from Exited to Entered in 30 sec
     * 4. New policy should be received and should be applied.
     * 5. Script should be destroyed and timer state should be changed from Entered to Disabled.
     * 5. Script should be initialized and timer state should be changed from Disabled to Exited.
     * 6. Timer state should be changed from Exited to Entered in 30 sec
     * 7. Timer state should be changed from Entered to Exited in 60 sec.
     * </p>
     *
     * @throws Throwable
     */

    public void test_007_TimerConditionChangingExitCondition() throws Throwable {
        mobileNetworkUtil.onWifiOnly();
        TimerConditionDisabledExitedStateTask timerConditionDisabledExitedStateTask = new TimerConditionDisabledExitedStateTask();
        TimerConditionExitedEnteredStateTask timerConditionExitedEnteredStateTask = new TimerConditionExitedEnteredStateTask();
        TimerConditionEnteredExitedStateTask timerConditionEnteredExitedStateTask = new TimerConditionEnteredExitedStateTask();
        TimerConditionEnteredDisabledStateTask timerConditionEnteredDisabledStateTask = new TimerConditionEnteredDisabledStateTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), timerConditionDisabledExitedStateTask,
                timerConditionExitedEnteredStateTask);
        logcatUtil.start();
        try {
            PMSUtil.addPolicies(new Policy[]{new Policy("timer", "30", POLICY_TIMER_CONDITIONS, true)});
            PMSUtil.addPolicies(new Policy[]{new Policy("timer", "1200", POLICY_TIMER_EXIT_CONDITIONS, true)});
            Thread.sleep(2 * MINUTE + MIN_PERIOD);
            logcatUtil.stop();
            assertFalse("Script should be initialized and timer state should be changed from Disabled to Exited",
                    timerConditionDisabledExitedStateTask.getLogEntries().isEmpty());
            assertFalse("Timer state should be changed from Exited to Entered in 30 sec",
                    timerConditionExitedEnteredStateTask.getLogEntries().isEmpty());

            logcatUtil = new LogcatUtil(getContext(), timerConditionEnteredDisabledStateTask);
            logcatUtil.start();
            PMSUtil.cleanPaths(new String[]{POLICY_TIMER_EXIT_CONDITIONS});
            Thread.sleep(MINUTE);
            logcatUtil.stop();
            assertFalse("Script should be destroyed and timer state should be changed from Entered to Disabled",
                    timerConditionEnteredDisabledStateTask.getLogEntries().isEmpty());

            logcatUtil = new LogcatUtil(getContext(), timerConditionDisabledExitedStateTask,
                    timerConditionExitedEnteredStateTask, timerConditionEnteredExitedStateTask);
            logcatUtil.start();
            PMSUtil.addPolicies(new Policy[]{new Policy("timer", "60", POLICY_TIMER_EXIT_CONDITIONS, true)});
            Thread.sleep(2 * MINUTE);
            logcatUtil.stop();
            assertFalse("Script should be initialized and timer state should be changed from Disabled to Exited",
                    timerConditionDisabledExitedStateTask.getLogEntries().isEmpty());
            assertFalse("Timer state should be changed from Exited to Entered in 30 sec",
                    timerConditionExitedEnteredStateTask.getLogEntries().isEmpty());
            assertFalse("Timer state should be changed from Entered to Exited in 60 sec",
                    timerConditionEnteredExitedStateTask.getLogEntries().isEmpty());

        } finally {
            PMSUtil.cleanPaths(new String[]{POLICY_TIMER_CONDITIONS, POLICY_TIMER_EXIT_CONDITIONS});
        }
    }

    /**
     * <p>Verify if timer state is not changed to Exited
     * </p>
     * <p>Pre-requisites:
     * 1.Specify policy rule:
     * asimov@application@<package_name>@scripts@<script_name>@conditions@timer=30
     * asimov@application@<package_name>@scripts@<script_name>@exit_conditions@timer=60
     * </p>
     * <p>Steps:
     * 1.Wait for 30 sec and ensure that timer state is active.
     * 3. Delete policy rule for timer exit conditions
     * 4. Wait for 90 sec.
     * </p>
     * <p>Expected results:
     * 1. Policy should be received and should be applied.
     * 2. Script should be initialized and timer state should be changed from Disabled to Exited.
     * 3. Timer state should be changed from Exited to Entered in 30 sec
     * 4. New policy should be received and should be applied.
     * 5. Script should be destroyed and timer state should be changed from Entered to Disabled.
     * 5. Script should be initialized and timer state should be changed from Disabled to Exited.
     * 6. Timer state should be changed from Exited to Entered in 30 sec
     * 7. Timer state should not be changed from Entered to Exited.
     * </p>
     *
     * @throws Throwable
     */

    public void test_008_TimerConditionExitConditionAfterRemoving() throws Throwable {
        mobileNetworkUtil.onWifiOnly();
        TimerConditionDisabledExitedStateTask timerConditionDisabledExitedStateTask = new TimerConditionDisabledExitedStateTask();
        TimerConditionExitedEnteredStateTask timerConditionExitedEnteredStateTask = new TimerConditionExitedEnteredStateTask();
        TimerConditionEnteredExitedStateTask timerConditionEnteredExitedStateTask = new TimerConditionEnteredExitedStateTask();
        TimerConditionEnteredDisabledStateTask timerConditionEnteredDisabledStateTask = new TimerConditionEnteredDisabledStateTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), timerConditionDisabledExitedStateTask,
                timerConditionExitedEnteredStateTask);
        logcatUtil.start();
        try {
            PMSUtil.addPolicies(new Policy[]{new Policy("timer", "30", POLICY_TIMER_CONDITIONS, true)});
            PMSUtil.addPolicies(new Policy[]{new Policy("timer", "600", POLICY_TIMER_EXIT_CONDITIONS, true)});
            Thread.sleep(2 * MINUTE + MIN_PERIOD);
            logcatUtil.stop();
            assertFalse("Script should be initialized and timer state should be changed from Disabled to Exited",
                    timerConditionDisabledExitedStateTask.getLogEntries().isEmpty());
            assertFalse("Timer state should be changed from Exited to Entered in 30 sec",
                    timerConditionExitedEnteredStateTask.getLogEntries().isEmpty());

            logcatUtil = new LogcatUtil(getContext(), timerConditionEnteredDisabledStateTask,
                    timerConditionDisabledExitedStateTask, timerConditionExitedEnteredStateTask, timerConditionEnteredExitedStateTask);
            logcatUtil.start();
            PMSUtil.cleanPaths(new String[]{POLICY_TIMER_EXIT_CONDITIONS});
            Thread.sleep(MINUTE + MIN_PERIOD);
            logcatUtil.stop();
            assertFalse("Script should be destroyed and timer state should be changed from Entered to Disabled",
                    timerConditionEnteredDisabledStateTask.getLogEntries().isEmpty());
            assertFalse("Script should be initialized and timer state should be changed from Disabled to Exited",
                    timerConditionDisabledExitedStateTask.getLogEntries().isEmpty());
            assertFalse("Timer state should be changed from Exited to Entered in 30 sec",
                    timerConditionExitedEnteredStateTask.getLogEntries().isEmpty());
            assertTrue("Timer state should not be changed from Entered to Exited",
                    timerConditionEnteredExitedStateTask.getLogEntries().isEmpty());
        } finally {
            PMSUtil.cleanPaths(new String[]{POLICY_TIMER_CONDITIONS, POLICY_TIMER_EXIT_CONDITIONS});
        }
    }

    /**
     * <p>Verify if timer state is changed back to Entered
     * </p>
     * <p>Pre-requisites:
     * 1. Specify policy rule:
     * asimov@application@<package_name>@scripts@<script_name>@conditions@timer=30
     * asimov@application@<package_name>@scripts@<script_name>@exit_conditions@timer=60
     * </p>
     * <p>Steps:
     * 1. Wait for 120 sec.
     * </p>
     * <p>Expected results:
     * 1. Policy should be received and should be applied.
     * 2. Script should be initialized and timer state should be changed from Disabled to Exited.
     * 3. Timer state should be changed from Exited to Entered in 30 sec.
     * 4. Timer state should be changed from Entered to Exited in 60 sec.
     * 5. Timer state should be changed back to Entered in 30 sec.
     * </p>
     *
     * @throws Throwable
     */
    //@Execute
    public void test_009_TimerConditionExitConditionDelay() throws Throwable {
        mobileNetworkUtil.onWifiOnly();
        TimerConditionDisabledExitedStateTask timerConditionDisabledExitedStateTask = new TimerConditionDisabledExitedStateTask();
        TimerConditionExitedEnteredStateTask timerConditionExitedEnteredStateTask = new TimerConditionExitedEnteredStateTask();
        TimerConditionEnteredExitedStateTask timerConditionEnteredExitedStateTask = new TimerConditionEnteredExitedStateTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), timerConditionDisabledExitedStateTask,
                timerConditionExitedEnteredStateTask, timerConditionEnteredExitedStateTask);
        logcatUtil.start();
        try {
            PMSUtil.addPolicies(new Policy[]{new Policy("timer", "30", POLICY_TIMER_CONDITIONS, true)});
            PMSUtil.addPolicies(new Policy[]{new Policy("timer", "60", POLICY_TIMER_EXIT_CONDITIONS, true)});
            Thread.sleep(2 * MINUTE + MIN_PERIOD);
            logcatUtil.stop();
            assertFalse("Script should be initialized and timer state should be changed from Disabled to Exited",
                    timerConditionDisabledExitedStateTask.getLogEntries().isEmpty());
            assertFalse("Timer state should be changed from Exited to Entered in 30 sec",
                    timerConditionExitedEnteredStateTask.getLogEntries().isEmpty());
            assertFalse("Timer state should be changed from Entered to Exited in 60 sec",
                    timerConditionEnteredExitedStateTask.getLogEntries().isEmpty());
            assertEquals("Timer state should be changed back to Entered in 30 sec", timerConditionExitedEnteredStateTask.getLogEntries().size(),
                    2);
        } finally {
            PMSUtil.cleanPaths(new String[]{POLICY_TIMER_CONDITIONS, POLICY_TIMER_EXIT_CONDITIONS});
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
