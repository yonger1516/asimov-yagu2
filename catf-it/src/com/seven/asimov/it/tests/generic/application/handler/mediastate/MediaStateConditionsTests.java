package com.seven.asimov.it.tests.generic.application.handler.mediastate;

import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.testcases.MediaStateConditionsTestCase;
import com.seven.asimov.it.utils.MediaUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.conditionTasks.ScriptLogTask;
import com.seven.asimov.it.utils.logcat.tasks.conditionTasks.TimerCondActivatedTask;
import com.seven.asimov.it.utils.logcat.wrappers.ScriptLogWrapper;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import junit.framework.AssertionFailedError;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.seven.asimov.it.base.constants.TFConstantsIF.*;

public class MediaStateConditionsTests extends MediaStateConditionsTestCase {
    private static final Logger logger = LoggerFactory.getLogger(MediaStateConditionsTests.class.getSimpleName());

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

    /**
     * 1. Create condition media=on under @asimov@application@com.seven.asimov.it@scripts@script_media@conditions<br/>
     * 2. Remove condition media=on under @asimov@application@com.seven.asimov.it@scripts@script_media@conditions<br/>
     * 3. Check policies received<br/>
     * 4. Check log for ScriptLog record with script state=0(SCRIPT_STATE_NOT_INITIALIZED),event=4(SCRIPT_EVENT_ERROR_OCCURRED)<br/>
     *
     * @throws Exception
     */
    public void test_009_checkErrorScript() throws Exception {
        final ScriptLogTask slTask = new ScriptLogTask();
        final LogcatUtil logcat = new LogcatUtil(getContext(), slTask);
        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(MEDIA_CONDITION, "on", ENTER_CONDITION, true)});
            TestUtil.sleep(WAIT_FOR_POLICY_UPDATE);
            logcat.start();

            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(MEDIA_CONDITION, "on", ENTER_CONDITION, false)});

            TestUtil.sleep(WAIT_FOR_POLICY_UPDATE);

            logcat.stop();

            assertNotNull("Not found ScriptLog record with script state=SCRIPT_STATE_NOT_INITIALIZED;" +
                    "event=SCRIPT_EVENT_ERROR_OCCURRED",
                    mscFindScriptWrapper(slTask.getLogEntries(), SCRIPT_NAME,
                            ScriptLogWrapper.STATES.SCRIPT_STATE_NOT_INITIALIZED.toInteger(),
                            ScriptLogWrapper.EVENTS.SCRIPT_EVENT_ERROR_OCCURRED.toInteger(), null));
        } finally {
            logcat.stop();
            PMSUtil.cleanPaths(new String[]{ENTER_CONDITION});
        }
    }

    /**
     * 1. Create condition media=on under @asimov@application@com.seven.asimov.it@scripts@script_media@conditions<br/>
     * 2. Create condition screen=off under @asimov@application@com.seven.asimov.it@scripts@script_media@conditions<br/>
     * 3. Check policies received<br/>
     * 4. Check log for ScriptLog record with state=1(SCRIPT_STATE_DISABLED) event=2(SCRIPT_EVENT_DESTROYED)<br/>
     *
     * @throws Exception
     */
    public void test_010_checkDestroyedScript() throws Exception {
        final ScriptLogTask slTask = new ScriptLogTask();
        final LogcatUtil logcat = new LogcatUtil(getContext(), slTask);
        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(MEDIA_CONDITION, "on", ENTER_CONDITION, true)});
            TestUtil.sleep(WAIT_FOR_POLICY_UPDATE);

            logcat.start();

            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(SCREEN_CONDITION, "off", ENTER_CONDITION, true)});
            TestUtil.sleep(WAIT_FOR_POLICY_UPDATE);

            logcat.stop();

            assertNotNull("Not found ScriptLog record with script state=SCRIPT_STATE_DISABLED;" +
                    "event=SCRIPT_EVENT_DESTROYED",
                    mscFindScriptWrapper(slTask.getLogEntries(), SCRIPT_NAME,
                            ScriptLogWrapper.STATES.SCRIPT_STATE_DISABLED.toInteger(),
                            ScriptLogWrapper.EVENTS.SCRIPT_EVENT_DESTROYED.toInteger(), null));
        } finally {
            logcat.stop();
            PMSUtil.cleanPaths(new String[]{ENTER_CONDITION});
        }
    }

    /**
     * 1. Create namespace @asimov@application@com.seven.asimov.it@scripts@script_media@conditions<br/>
     * 2. Set conditions:      media=on;timer=30;screen=on<br/>
     * 3. Set exit_conditions: radio=down;timer=45;screen=off<br/>
     * 4. Check policies received
     * 5. Check log for ScriptLog record with state=1(SCRIPT_STATE_DISABLED) event=1(SCRIPT_EVENT_INITIALIZED)<br/>
     * 6. Check ScriptLog's Script event data field to be sure that all conditions are correctly set:<br/>
     * F[0]C[5/on;1/30;3/on]EC[8/down;1/45;3/off]A[]EA[] Conditions order doesn't matter.
     *
     * @throws Exception
     */
    public void test_011_checkInitializedScript() throws Exception {
        final ScriptLogTask slTask = new ScriptLogTask();
        final LogcatUtil logcat = new LogcatUtil(getContext(), slTask);
        try {

            logcat.start();

            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(MEDIA_CONDITION, "on", ENTER_CONDITION, true),
                    new Policy(TIMER_CONDITION, "30", ENTER_CONDITION, true),
                    new Policy(SCREEN_CONDITION, "on", ENTER_CONDITION, true),
                    new Policy(RADIO_CONDITION, "down", EXIT_CONDITION, true),
                    new Policy(TIMER_CONDITION, "45", EXIT_CONDITION, true),
                    new Policy(SCREEN_CONDITION, "off", EXIT_CONDITION, true)});
            TestUtil.sleep(WAIT_FOR_POLICY_UPDATE);

            logcat.stop();

            assertNotNull("Not found ScriptLog record with script state=SCRIPT_STATE_DISABLED;" +
                    "event=SCRIPT_EVENT_INITIALIZED event data=" + EVENT_DATA,
                    mscFindScriptWrapper(slTask.getLogEntries(), SCRIPT_NAME,
                            ScriptLogWrapper.STATES.SCRIPT_STATE_DISABLED.toInteger(),
                            ScriptLogWrapper.EVENTS.SCRIPT_EVENT_INITIALIZED.toInteger(), EVENT_DATA));
        } finally {
            logcat.stop();
            PMSUtil.cleanPaths(new String[]{ENTER_CONDITION, EXIT_CONDITION});
        }
    }

    /**
     * 1. Create namespace @asimov@application@com.seven.asimov.it@scripts@script_media@conditions<br/>
     * 2. Set conditions: media=on;<br/>
     * 3. Set exit_conditions: media=off;<br/>
     * 4. Check policies received <br/>
     * 5. Check log for ScriptLog record with state=2(SCRIPT_STATE_WAITING) event=3(SCRIPT_EVENT_STATE_SWITCHED)<br/>
     * 6. Check log for ScriptLog record with state=3(SCRIPT_STATE_EXITED) event=3(SCRIPT_EVENT_STATE_SWITCHED)<br/>
     * 7. Start playing audio <br/>
     * 8. Check log for ScriptLog record with state=4(SCRIPT_STATE_ENTERED) event=3(SCRIPT_EVENT_STATE_SWITCHED)<br/>
     * 9. Stop playing audio<br/>
     * 10. Check log for ScriptLog record with state=3(SCRIPT_STATE_EXITED) event=3(SCRIPT_EVENT_STATE_SWITCHED)<br/>
     *
     * @throws Exception
     */

    public void test_012_checkStateSwitchScript() throws Exception {
        final ScriptLogTask slTask = new ScriptLogTask();
        final MediaUtil mediaUtil = MediaUtil.init();
        LogcatUtil logcat = new LogcatUtil(getContext(), slTask);
        try {
            logcat.start();

            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(MEDIA_CONDITION, "on", ENTER_CONDITION, true),
                    new Policy(MEDIA_CONDITION, "off", EXIT_CONDITION, true)});
            TestUtil.sleep(WAIT_FOR_POLICY_UPDATE);

            logcat.stop();

            assertNotNull("Not found ScriptLog record with script state=SCRIPT_STATE_WAITING;" +
                    "event=SCRIPT_EVENT_STATE_SWITCHED",
                    mscFindScriptWrapper(slTask.getLogEntries(), SCRIPT_NAME,
                            ScriptLogWrapper.STATES.SCRIPT_STATE_WAITING.toInteger(),
                            ScriptLogWrapper.EVENTS.SCRIPT_EVENT_STATE_SWITCHED.toInteger(), null));
            assertNotNull("Not found ScriptLog record with script state=SCRIPT_STATE_EXITED;" +
                    "event=SCRIPT_EVENT_STATE_SWITCHED",
                    mscFindScriptWrapper(slTask.getLogEntries(), SCRIPT_NAME,
                            ScriptLogWrapper.STATES.SCRIPT_STATE_EXITED.toInteger(),
                            ScriptLogWrapper.EVENTS.SCRIPT_EVENT_STATE_SWITCHED.toInteger(), null));

            slTask.reset();
            logcat = new LogcatUtil(getContext(), slTask);


            logcat.start();

            mediaUtil.play();
            TestUtil.sleep(5 * 1000);

            logcat.stop();

            assertNotNull("Not found ScriptLog record with script state=SCRIPT_STATE_ENTERED;" +
                    "event=SCRIPT_EVENT_STATE_SWITCHED",
                    mscFindScriptWrapper(slTask.getLogEntries(), SCRIPT_NAME,
                            ScriptLogWrapper.STATES.SCRIPT_STATE_ENTERED.toInteger(),
                            ScriptLogWrapper.EVENTS.SCRIPT_EVENT_STATE_SWITCHED.toInteger(), null));

            slTask.reset();
            logcat = new LogcatUtil(getContext(), slTask);

            logcat.start();

            mediaUtil.stop();
            TestUtil.sleep(5 * 1000);

            logcat.stop();

            assertNotNull("Not found ScriptLog record with script state=SCRIPT_STATE_ENTERED;" +
                    "event=SCRIPT_EVENT_STATE_SWITCHED",
                    mscFindScriptWrapper(slTask.getLogEntries(), SCRIPT_NAME,
                            ScriptLogWrapper.STATES.SCRIPT_STATE_EXITED.toInteger(),
                            ScriptLogWrapper.EVENTS.SCRIPT_EVENT_STATE_SWITCHED.toInteger(), null));
        } finally {
            mediaUtil.stop();
            logcat.stop();
            PMSUtil.cleanPaths(new String[]{ENTER_CONDITION, EXIT_CONDITION});
        }
    }

    /**
     * 1. Create namespace @asimov@application@com.seven.asimov.it@scripts@script_media@conditions<br/>
     * 2. Set conditions: media=on;timer=120;<br/>
     * 3. Set exit_conditions: media=off;timer=15;<br/>
     * 4. Check policies received <br/>
     * 5. Check log for ScriptLog record with state=2(SCRIPT_STATE_WAITING) event=3(SCRIPT_EVENT_STATE_SWITCHED)<br/>
     * 6. Check log for ScriptLog record with state=3(SCRIPT_STATE_EXITED) event=3(SCRIPT_EVENT_STATE_SWITCHED)<br/>
     * 7. Wait 130 seconds<br/>
     * 8. Check log for 'Timer condition activated (group=enter, script=0x........)' log entry<br/>
     * 9. Check log does NOT have ScriptLog record with state=4(SCRIPT_STATE_ENTERED) event=3(SCRIPT_EVENT_STATE_SWITCHED)<br/>
     * 10. Start playing audio <br/>
     * 11. Check log for ScriptLog record with state=4(SCRIPT_STATE_ENTERED) event=3(SCRIPT_EVENT_STATE_SWITCHED)<br/>
     * 12. Wait 30 seconds.<br/>
     * 13. Check log for 'Timer condition activated (group=exit, script=0x........)' log entry<br/>
     * 14. Check log does NOT have ScriptLog record with state=3(SCRIPT_STATE_EXITED) event=3(SCRIPT_EVENT_STATE_SWITCHED)<br/>
     * 15. Stop playing audio<br/>
     * 16. Check log for ScriptLog record with state=3(SCRIPT_STATE_EXITED) event=3(SCRIPT_EVENT_STATE_SWITCHED)<br/>
     *
     * @throws Exception
     */

    public void test_013_checkDualConditionSwitchScript() throws Exception {
        final ScriptLogTask slTask = new ScriptLogTask();
        final TimerCondActivatedTask tcaTask = new TimerCondActivatedTask();

        final MediaUtil mediaUtil = MediaUtil.init();
        LogcatUtil logcat = new LogcatUtil(getContext(), slTask, tcaTask);
        try {

            logcat.start();
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(MEDIA_CONDITION, "on", ENTER_CONDITION, true),
                    new Policy(TIMER_CONDITION, "120", ENTER_CONDITION, true),
                    new Policy(MEDIA_CONDITION, "off", EXIT_CONDITION, true),
                    new Policy(TIMER_CONDITION, "15", EXIT_CONDITION, true)});
            TestUtil.sleep(130 * 1000);

            logcat.stop();


            assertNotNull("Not found ScriptLog record with script state=SCRIPT_STATE_WAITING;" +
                    "event=SCRIPT_EVENT_STATE_SWITCHED",
                    mscFindScriptWrapper(slTask.getLogEntries(), SCRIPT_NAME,
                            ScriptLogWrapper.STATES.SCRIPT_STATE_WAITING.toInteger(),
                            ScriptLogWrapper.EVENTS.SCRIPT_EVENT_STATE_SWITCHED.toInteger(), null));
            assertNotNull("Not found ScriptLog record with script state=SCRIPT_STATE_EXITED;" +
                    "event=SCRIPT_EVENT_STATE_SWITCHED",
                    mscFindScriptWrapper(slTask.getLogEntries(), SCRIPT_NAME,
                            ScriptLogWrapper.STATES.SCRIPT_STATE_EXITED.toInteger(),
                            ScriptLogWrapper.EVENTS.SCRIPT_EVENT_STATE_SWITCHED.toInteger(), null));
            assertTrue("Expected timer condition to be activated", !tcaTask.getLogEntries().isEmpty());
            assertNull("Found unexpected ScriptLog record with script state=SCRIPT_STATE_ENTERED;" +
                    "event=SCRIPT_EVENT_STATE_SWITCHED",
                    mscFindScriptWrapper(slTask.getLogEntries(), SCRIPT_NAME,
                            ScriptLogWrapper.STATES.SCRIPT_STATE_ENTERED.toInteger(),
                            ScriptLogWrapper.EVENTS.SCRIPT_EVENT_STATE_SWITCHED.toInteger(), null));


            logcat.clear();
            logcat = new LogcatUtil(getContext(), slTask, tcaTask);

            logcat.start();
            mediaUtil.play();
            TestUtil.sleep(30 * 1000);

            logcat.stop();


            assertNotNull("Not found ScriptLog record with script state=SCRIPT_STATE_ENTERED;" +
                    "event=SCRIPT_EVENT_STATE_SWITCHED",
                    mscFindScriptWrapper(slTask.getLogEntries(), SCRIPT_NAME,
                            ScriptLogWrapper.STATES.SCRIPT_STATE_ENTERED.toInteger(),
                            ScriptLogWrapper.EVENTS.SCRIPT_EVENT_STATE_SWITCHED.toInteger(), null));
            assertTrue("Expected timer condition to be activated", !tcaTask.getLogEntries().isEmpty());
            assertNull("Found unexpected ScriptLog record with script state=SCRIPT_STATE_EXITED;" +
                    "event=SCRIPT_EVENT_STATE_SWITCHED ",
                    mscFindScriptWrapper(slTask.getLogEntries(), SCRIPT_NAME,
                            ScriptLogWrapper.STATES.SCRIPT_STATE_EXITED.toInteger(),
                            ScriptLogWrapper.EVENTS.SCRIPT_EVENT_STATE_SWITCHED.toInteger(), null));

            logcat.clear();
            logcat = new LogcatUtil(getContext(), slTask);

            logcat.start();
            mediaUtil.stop();
            TestUtil.sleep(5 * 1000);

            logcat.stop();

            assertNotNull("Not found ScriptLog record with script state=SCRIPT_STATE_EXITED;" +
                    "event=SCRIPT_EVENT_STATE_SWITCHED",
                    mscFindScriptWrapper(slTask.getLogEntries(), SCRIPT_NAME,
                            ScriptLogWrapper.STATES.SCRIPT_STATE_EXITED.toInteger(),
                            ScriptLogWrapper.EVENTS.SCRIPT_EVENT_STATE_SWITCHED.toInteger(), null));
        } finally {
            mediaUtil.stop();
            logcat.stop();
            PMSUtil.cleanPaths(new String[]{ENTER_CONDITION, EXIT_CONDITION});
        }
    }

    /**
     * TODO: @Ignore Tests ignored, cause those test related to PCF System. Ignore delete after ASMV-15516 will be resolved
     * <h1>Prepare test</h1>
     * <p>Preparing Test Framework for Media state condition tests</p>
     * <p>Creating namespace on server <b>com.seven.asimov.it</b>, at path <b>@asimov@ssl </b></p>
     * <p>Creating default property for caching and polling on HTTPS protocol</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Check available internet connection</li>
     * <li>Check available test server and PMS</li>
     * <li>Create default property for caching and polling over <b>HTTPS</b> protocol</li>
     * </ol>
     *
     * @throws Exception
     */
    @Ignore
    public void test_000_init() throws Exception {
        mediaInit();
    }

    /**
     * TODO: @Ignore Tests ignored, cause those test related to PCF System. Ignore delete after ASMV-15516 will be resolved
     * <h1>CheckOffCondition</h1>
     * <p>Check condition policy retrieving and correct work</p>
     * <p>Create namespace com.seven.asimov.it in namespace application</p>
     * <p>Create conditions <b>media</b> off for entry and exit</p>
     * <p>Check applying these conditions</p>
     * <p>Start media play, expected result nothing changed for network traffic</p>
     *
     * @throws Exception
     */
    @Ignore
    public void test_001_testCheckOffCondition() throws Exception {
        oncePlay(OFF, OFF, OFF, "test_001_testCheckOffCondition", 200, false);
    }

    /**
     * TODO: @Ignore Tests ignored, cause those test related to PCF System. Ignore delete after ASMV-15516 will be resolved
     * <h1>CheckOffCondition</h1>
     * <p>Check condition policy retrieving and correct work</p>
     * <p>Create namespace com.seven.asimov.it in namespace application</p>
     * <p>Create conditions <b>media</b> on for entry and off for exit</p>
     * <p>Check applying these conditions</p>
     * <p>Start media play, expected result nothing changed for network traffic</p>
     *
     * @throws Exception
     */
    @Ignore
    public void test_002_testCheckOnCondition() throws Exception {
        oncePlay(ON, OFF, OFF, "test_002_testCheckOnCondition", 200, true);
    }

    /**
     * TODO: @Ignore Tests ignored, cause those test related to PCF System. Ignore delete after ASMV-15516 will be resolved
     * <h1>CheckOffCondition</h1>
     * <p>Check condition policy retrieving and correct work</p>
     * <p>Create namespace com.seven.asimov.it in namespace application</p>
     * <p>Create conditions <b>media</b> off for entry and exit</p>
     * <p>Create rule on PCF for off media condition and block all traffic</p>
     * <p>Check applying these conditions</p>
     * <p>Start media play, expected result all traffic blocked</p>
     *
     * @throws Exception
     */
    @Ignore
    public void test_003_testCheckOffConditionsWithPCF() throws Exception {
        oncePlay(OFF, OFF, ON, "test_003_testCheckOffConditionsWithPCF", -1, false);
    }

    /**
     * TODO: @Ignore Tests ignored, cause those test related to PCF System. Ignore delete after ASMV-15516 will be resolved
     * <h1>CheckOffCondition</h1>
     * <p>Check condition policy retrieving and correct work</p>
     * <p>Create namespace com.seven.asimov.it in namespace application</p>
     * <p>Create conditions <b>media</b> on for entry and off for exit</p>
     * <p>Create rule for on condition and block all traffic</p>
     * <p>Check applying these conditions</p>
     * <p>Start media play, expected result all traffic blocked</p>
     *
     * @throws Exception
     */
    @Ignore
    public void test_004_testCheckOnConditionsWithPCF() throws Exception {
        oncePlay(ON, OFF, ON, "test_004_testCheckOnConditionsWithPCF", -1, true);
    }

    /**
     * TODO: @Ignore Tests ignored, cause those test related to PCF System. Ignore delete after ASMV-15516 will be resolved
     * <h1>CheckOffCondition</h1>
     * <p>Check condition policy retrieving and correct work</p>
     * <p>Create namespace com.seven.asimov.it in namespace application</p>
     * <p>Create conditions <b>media</b> off for entry and off for exit</p>
     * <p>Start media play expected result nothing changed for network traffic</p>
     * <p>Check applying these conditions</p>
     * <p>Create conditions <b>media</b> on for entry and off for exit</p>
     * <p>Create rule for on condition and block all traffic</p>
     * <p>Start media play, expected result all traffic are blocked</p>
     *
     * @throws Exception
     */
    @Ignore
    public void test_005_testOnPCFOffConditions() throws Exception {
        twicePlay(OFF, OFF, ON, ON, OFF, ON, "test_005_testOnPCFOffConditions", 200, -1, true, true);
    }

    /**
     * TODO: @Ignore Tests ignored, cause those test related to PCF System. Ignore delete after ASMV-15516 will be resolved
     * <h1>CheckOffCondition</h1>
     * <p>Check condition policy retrieving and correct work</p>
     * <p>Create namespace com.seven.asimov.it in namespace application</p>
     * <p>Create conditions <b>media</b> off for entry and off for exit</p>
     * <p>Start media play expected result nothing changed for network traffic</p>
     * <p>Check applying these conditions</p>
     * <p>Create conditions <b>media</b> on for entry and on for exit</p>
     * <p>Create rule for on condition and block all traffic</p>
     * <p>Start media play, expected result all traffic are blocked</p>
     *
     * @throws Exception
     */
    @Ignore
    public void test_006_testOffOnPCFConditions() throws Exception {
        twicePlay(OFF, OFF, ON, ON, OFF, ON, "test_006_testOffOnPCFConditions", 200, -1, false, true);
    }

    /**
     * TODO: @Ignore Tests ignored, cause those test related to PCF System. Ignore delete after ASMV-15516 will be resolved
     * <h1>CheckOffCondition</h1>
     * <p>Check condition policy retrieving and correct work</p>
     * <p>Create namespace com.seven.asimov.it in namespace application</p>
     * <p>Create conditions <b>media</b> on for entry and off for exit</p>
     * <p>Start media play, expected result all traffic are blocked</p>
     * <p>Check applying these conditions</p>
     * <p>Create conditions <b>media</b> on for entry and on for exit</p>
     * <p>Create rule for on condition and block all traffic</p>
     * <p>Start media play, expected result all traffic are blocked</p>
     *
     * @throws Exception
     */
    @Ignore
    public void test_007_testOnPCFOnPCFConditions() throws Exception {
        twicePlay(ON, OFF, ON, ON, OFF, ON, "test_007_testOnPCFOnPCFConditions", -1, -1, true, true);
    }

    /**
     * TODO: @Ignore Tests ignored, cause those test related to PCF System. Ignore delete after ASMV-15516 will be resolved
     * <h1>CheckOffCondition</h1>
     * <p>Check condition policy retrieving and correct work</p>
     * <p>Create namespace com.seven.asimov.it in namespace application</p>
     * <p>Create conditions <b>media</b> on for entry and off for exit</p>
     * <p>Start media play, expected result all traffic are blocked</p>
     * <p>Check applying these conditions</p>
     * <p>Create conditions <b>media</b> on for entry and on for exit</p>
     * <p>Create rule for on condition and block all traffic</p>
     * <p>Start media play, expected result all traffic are blocked</p>
     *
     * @throws Exception
     */
    @Ignore
    public void test_008_checkActionAfterStartMedia() throws Exception {
        playAndCheck(ON, OFF, ON, "test_008_checkActionAfterStartMedia", -1);
    }

    /**
     * TODO: @Ignore Tests ignored, cause those test related to PCF System. Ignore delete after ASMV-15516 will be resolved
     * <h1>CheckOffCondition</h1>
     * <p>Check condition policy retrieving and correct work</p>
     * <p>Create namespace com.seven.asimov.it in namespace application</p>
     * <p>Create conditions <b>media</b> off for entry and exit</p>
     * <p>Check applying these conditions</p>
     * <p>Start media play, expected result nothing changed for network traffic</p>
     *
     * @throws Exception
     */
    @Ignore
    public void test_999_cleanUp() throws Exception {

    }
}
