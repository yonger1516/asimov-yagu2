package com.seven.asimov.it.testcases;

import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.MobileNetworkUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.conditionTasks.RadioTimerAddedByRadioTask;
import com.seven.asimov.it.utils.logcat.tasks.conditionTasks.RadioTimerAddedByTimerTask;
import com.seven.asimov.it.utils.logcat.wrappers.RadioTimerWrapper;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimerConditionTestCase extends TcpDumpTestCase {
    private static final Logger logger = LoggerFactory.getLogger(TimerConditionTestCase.class.getSimpleName());

    protected final int ACTIVATED_BY_TIMER = 1;
    protected final int ACTIVATED_BY_RADIO = 2;
    protected final int DEACTIVATED_BY_RADIO = 3;
    protected static final int TIME_PAUSE = 2 * 60 * 1000;
    protected static final long MINUTE = 60 * 1000;
    protected static final long MIN_PERIOD = 30 * 1000;

    protected final String POLICY_TIMER_CONDITIONS = "@asimov@application@com.seven.asimov.it@scripts@script_timer@conditions";
    protected final String POLICY_TIMER_EXIT_CONDITIONS = "@asimov@application@com.seven.asimov.it@scripts@script_timer@exit_conditions";
    protected final String POLICY_NAME = "radio_timer";
    protected final String POLICY_PATH = "@asimov@application@com.seven.asimov.it@scripts@script_radio_timer@conditions";
    protected final String POLICY_EXIT_PATH = "@asimov@application@com.seven.asimov.it@scripts@script_radio_timer@exit_conditions";

    protected RadioTimerAddedByRadioTask timerAddedTaskByRadio = null;
    protected RadioTimerAddedByTimerTask timerAddedTaskByTimer = null;
    protected Policy[] policiesToAdd = null;
    protected LogcatUtil logcatUtil;

    protected void prepareRadioTimerConditionTest() throws Throwable {
        MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());
        timerAddedTaskByRadio = new RadioTimerAddedByRadioTask();
        timerAddedTaskByTimer = new RadioTimerAddedByTimerTask();
        logcatUtil = new LogcatUtil(getContext(), timerAddedTaskByRadio, timerAddedTaskByTimer);
        mobileNetworkUtil.on3gOnly();
    }

    protected void pushPolicy(String policyName, String policyValue, String policyPath, long timePause) throws Exception {
        policiesToAdd = new Policy[]{new Policy(policyName, policyValue, policyPath, true)};
        PMSUtil.addPolicies(policiesToAdd);
        Thread.sleep(timePause);
    }

    protected boolean isRadioTimerActivatedByTimer() {
        return checkRadioTimerConditionState(ACTIVATED_BY_TIMER);
    }

    protected boolean isRadioTimerActivatedByRadio() {
        return checkRadioTimerConditionState(ACTIVATED_BY_RADIO);
    }

    protected boolean isRadioTimerDeactivatedByRadio() {
        return checkRadioTimerConditionState(DEACTIVATED_BY_RADIO);
    }


    protected boolean checkRadioTimerConditionState(int radioTimerState) {
        boolean result = false;
        switch (radioTimerState) {
            case 1:
                if ((timerAddedTaskByTimer.getLogEntries() != null) && (!timerAddedTaskByTimer.getLogEntries().isEmpty())) {
                    for (RadioTimerWrapper someEntry : timerAddedTaskByTimer.getLogEntries()) {
                        logger.info("Radio-timer condition triggered by timer timestamp" + someEntry.getTimestamp());
                        if (someEntry.getAlreadyActive() != null)
                            result = true;
                    }
                }
                break;
            case 2:
                if (timerAddedTaskByRadio.getLogEntries() != null) {
                    for (RadioTimerWrapper someEntry : timerAddedTaskByRadio.getLogEntries()) {
                        logger.info("Radio-timer condition activated by radio: is_timer_scheduled=" + someEntry.getTimerScheduled());
                        if (someEntry.getState().equals("activated") && someEntry.getTimerScheduled().equals("true"))
                            result = true;
                    }
                }
                break;
            case 3:
                if (timerAddedTaskByRadio.getLogEntries() != null) {
                    for (RadioTimerWrapper someEntry : timerAddedTaskByRadio.getLogEntries()) {
                        logger.info("Radio-timer condition deactivated by radio: is_timer_scheduled=" + someEntry.getTimerScheduled());
                        if (someEntry.getState().equals("deactivated") && someEntry.getTimerScheduled().equals("true"))
                            result = true;
                    }
                }
                break;
        }
        return result;
    }

    protected Thread getRadioKeep() {
        final String host = PMSUtil.getPmsServerIp();
        return new Thread() {
            @Override
            public void run() {
                int i = 0;
                while (i++ < 30) {
                    try {
                        try {
                            Runtime.getRuntime().exec("ping -s1 -c1 -W3 -n " + host).waitFor();
                            logger.info("Ping radioUP " + host);
                        } catch (InterruptedException interruptedExceprion) {
                            ExceptionUtils.getStackTrace(interruptedExceprion);
                        }
                        TestUtil.sleep(14 * 1000);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        };
    }
}
