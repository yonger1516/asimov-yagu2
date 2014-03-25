package com.seven.asimov.it.tests.crcs.radiolog.wcdma;

import android.content.Context;
import android.test.AssertionFailedError;
import android.test.suitebuilder.annotation.LargeTest;
import com.seven.asimov.it.annotation.DeviceOnly;
import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.TestCaseThread;
import com.seven.asimov.it.testcases.RadioWcdmaTestCase;
import com.seven.asimov.it.tests.crcs.radiolog.lte.fixed.RadioLteTransitionsFixedModeTests;
import com.seven.asimov.it.utils.MobileNetworkUtil;
import com.seven.asimov.it.utils.ScreenUtils;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.crcsTasks.RadiologTask;
import com.seven.asimov.it.utils.logcat.wrappers.RadioLogEntry;
import com.seven.asimov.it.utils.logcat.wrappers.RadioStateType;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import com.seven.asimov.it.utils.sms.SmsUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

/**
 * <h2>This class tests OCC's Radiolog functionality for DNS.</h2>
 */
public class RadioWcdmaTransitionsTests extends RadioWcdmaTestCase {

    private static final Logger logger = LoggerFactory.getLogger(RadioLteTransitionsFixedModeTests.class.getSimpleName());

    private static final int DCH_FACH_TRANSITION_TIME = 5 * 1000;
    private static final int FACH_PCH_TRANSITION_TIME = 30 * 1000;

    private static final int MOBILE_NETWORK_TYPE_NAME_UMTS = 7;

    @Ignore
    //@Execute
    @LargeTest
    @DeviceOnly
    public void test_000_init() throws Throwable {
        final String POLICY_NAMESPACE = "@asimov@reporting@radiosettings@wcdma@wcdma_a";
        final String POLICY_NAME_T1 = "t1";
        final String POLICY_NAME_T2 = "t2";
        final String POLICY_VALUE_T1 = "30";
        final String POLICY_VALUE_T2 = "60";

        final Policy t1 = new Policy(POLICY_NAME_T1, POLICY_VALUE_T1, POLICY_NAMESPACE, true);
        final Policy t2 = new Policy(POLICY_NAME_T2, POLICY_VALUE_T2, POLICY_NAMESPACE, true);
        PMSUtil.addPolicies(new Policy[]{t1, t2});
        TestUtil.sleep(10 * 1000);
        SmsUtil smsUtil = new SmsUtil(getContext());
        smsUtil.sendPolicyUpdate((byte) 0);
        TestUtil.sleep(60 * 1000);
    }

    /**
     * <h3>Verify that radio channel changes state from cell_dch to cell_fach.</h3>
     * actions:
     * <ol>
     * <li>send requests to sina.com, engadget.com, github.com</li>
     * </ol>
     * checks:
     * <ol>
     * <li>check that state of radio channel should be changed from cell_dch to cell_fach</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @LargeTest
    @DeviceOnly
    //TODO: This test should be investigated
    public void test_002_DchFachTransition() throws Exception {
        enableMobileNetwork();
        RadiologTask radiologTask = new RadiologTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), radiologTask);
        logcatUtil.start();

        String uri1 = "http://sina.com";
        String uri2 = "http://engadget.com";
        String uri3 = "http://github.com";

        HttpRequest request1 = createRequest().setMethod(HttpGet.METHOD_NAME).setUri(uri1).getRequest();
        HttpRequest request2 = createRequest().setMethod(HttpGet.METHOD_NAME).setUri(uri2).getRequest();
        HttpRequest request3 = createRequest().setMethod(HttpGet.METHOD_NAME).setUri(uri3).getRequest();

        int startedState = -1;
        int endedState = -1;
        long stateChangedTime = 0L;
        int sleepTime = 40 * 1000;

        try {
            sendRequest2(request1);
            sendRequest2(request2);
            sendRequest2(request3);
            startedState = getActiveNetworkType();
            logger.info("Started state type : " + startedState);
            stateChangedTime = System.currentTimeMillis();
            TestUtil.sleep(sleepTime);
            endedState = getActiveNetworkType();
            logger.info("Ended state type : " + startedState);
            sendRequest2(request3);

            logcatUtil.stop();
            logcatUtil.logTasks();
            if (startedState != MOBILE_NETWORK_TYPE_NAME_UMTS &&
                    endedState == MOBILE_NETWORK_TYPE_NAME_UMTS) {
                if (radiologTask.getLogEntries().size() > 0) {

                    RadioLogEntry entryFirstState = radiologTask.getLogEntries().get(0);
                    RadioLogEntry entryLastState = radiologTask.getLogEntries().get(0);

                    for (RadioLogEntry entry : radiologTask.getLogEntries()) {
                        if (entry.getCurrentState() == RadioStateType.cell_pch &&
                                entry.getTimestamp() < stateChangedTime) {
                            entryFirstState = entry;
                        }
                    }
                    for (RadioLogEntry entry : radiologTask.getLogEntries()) {
                        if (entry.getCurrentState() == RadioStateType.cell_fach &&
                                entry.getTimestamp() > stateChangedTime) {
                            entryLastState = entry;
                        }
                    }

                    if (entryFirstState == null || entryLastState == null)
                        throw new AssertionFailedError("Cant' find dch-fach transition.");

                } else {
                    throw new AssertionFailedError("Can't find radiologs.");
                }
            }

        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            assertFalse("Exception when running test", e == null ? true : false);
        } finally {
            logcatUtil.stop();
        }
    }

    /**
     * <h3>Verify that radio channel changes state from cell_fach to cell_pch.</h3>
     * actions:
     * <ol>
     * <li>send 1 request to testrunner</li>
     * <li>wait for 45 sec</li>
     * </ol>
     * checks:
     * <ol>
     * <li>check that state of radio channel should be changed from cell_fach to cell_pch</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @LargeTest
    @DeviceOnly
    public void test_003_FachPchTransition() throws Exception {
        enableMobileNetwork();
        String resource = "asimov_wcdma_03_FachPchTransition";
        String uri = createTestResourceUri(resource);
        HttpRequest request = createRequest().setMethod(HttpGet.METHOD_NAME).setUri(uri).getRequest();
        LogcatUtil logcatUtil = null;

        int MAX_RETIRES = 6;
        boolean isPchState = false;
        try {
            for (int i = 1; i <= MAX_RETIRES; i++) {
                RadiologTask radiologTask = new RadiologTask();
                logcatUtil = new LogcatUtil(getContext(), radiologTask);
                logcatUtil.start();

                checkMiss(request, 1);
                TestUtil.sleep(DCH_FACH_TRANSITION_TIME + FACH_PCH_TRANSITION_TIME + 10 * 1000);
                logcatUtil.stop();
                logcatUtil.logTasks();
                List<RadioLogEntry> wcdmaEntries = getWcdmaEntries(radiologTask.getLogEntries());

                for (RadioLogEntry entry : wcdmaEntries) {
                    //Log.i(TAG, entry.toString());
                    if (entry.getCurrentState() == RadioStateType.cell_pch &&
                            entry.getPreviousState() == RadioStateType.cell_fach) {
                        isPchState = true;
                    }
                }
                if (isPchState) {
                    break;
                }
            }
            if (!isPchState) {
                throw new AssertionFailedError("There isn't fach-pch transition.");
            }
        } finally {
            logcatUtil.stop();
        }
    }

    /**
     * <h3>Verify that radio channel changes state from cell_fach to cell_dch.</h3>
     * actions:
     * <ol>
     * <li>send requests to home.sina.com, engadget.com/events, github.com/blog</li>
     * </ol>
     * checks:
     * <ol>
     * <li>check that state of radio channel should be changed from cell_fach to cell_dch</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @LargeTest
    @DeviceOnly
    public void test_005_FachDchTransition() throws Exception {
        enableMobileNetwork();

        RadiologTask radiologTask = new RadiologTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), radiologTask);
        logcatUtil.start();

        try {
            sendSimpleRequest();
            TestUtil.sleep(2 * DCH_FACH_TRANSITION_TIME);

            String uri1 = "http://home.sina.com";
            String uri2 = "http://www.engadget.com/events";
            String uri3 = "http://github.com/blog";

            HttpRequest request1 = createRequest().setMethod(HttpGet.METHOD_NAME).setUri(uri1).getRequest();
            HttpRequest request2 = createRequest().setMethod(HttpGet.METHOD_NAME).setUri(uri2).getRequest();
            HttpRequest request3 = createRequest().setMethod(HttpGet.METHOD_NAME).setUri(uri3).getRequest();

            sendRequest2(request1);
            sendRequest2(request2);
            sendRequest2(request3);
            TestUtil.sleep(DCH_FACH_TRANSITION_TIME);

            logcatUtil.stop();
            logcatUtil.logTasks();
            boolean isFachDchTranstion = false;
            List<RadioLogEntry> wcdmaEntries = getWcdmaEntries(radiologTask.getLogEntries());
            Iterator<RadioLogEntry> iterator = wcdmaEntries.iterator();
            while (iterator.hasNext()) {
                RadioLogEntry entry = iterator.next();
                if (entry.getCurrentState() == RadioStateType.cell_fach) {
                    if (iterator.hasNext()) {
                        RadioLogEntry nextEntry = iterator.next();
                        if (nextEntry.getCurrentState() == RadioStateType.cell_dch) {
                            isFachDchTranstion = true;
                        }
                    }
                }
            }
            if (!isFachDchTranstion) {
                throw new AssertionFailedError("Can't find fach-dch transition.");
            }

        } finally {
            logcatUtil.stop();
        }
    }

    /**
     * <h3>Verify that radio channel changes state from cell_pch to cell_dch.</h3>
     * actions:
     * <ol>
     * <li>switch off screen for 45 sec</li>
     * <li>switch no screen</li>
     * <li>send 3 requests to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     * <li>check that state of radio channel should be changed from cell_pch to cell_dch</li>
     * </ol>
     *
     * @throws Exception
     */
    @LargeTest     // need to be investigated
    @DeviceOnly
    public void test_006_PchDchTransitionScreenOfOn() throws Exception {
        enableMobileNetwork();

        RadiologTask radiologTask = new RadiologTask();
        LogcatUtil logcatUtil = null;
        boolean isPchEnable = false;
        boolean isDchEnable = false;
        int MAX_RETIRES = 6;

        try {
            for (int i = 1; i <= MAX_RETIRES; i++) {

                logcatUtil = new LogcatUtil(getContext(), radiologTask);
                logcatUtil.start();

                ScreenUtils.screenOff();
                TestUtil.sleep(DCH_FACH_TRANSITION_TIME + FACH_PCH_TRANSITION_TIME + 15 * 1000);
                ScreenUtils.screenOn();

                String resource1 = "asimov_pch_dch_transition_screen_of_on1";
                String resource2 = "asimov_pch_dch_transition_screen_of_on2";
                String resource3 = "asimov_pch_dch_transition_screen_of_on3";
                String uri1 = createTestResourceUri(resource1);
                String uri2 = createTestResourceUri(resource2);
                String uri3 = createTestResourceUri(resource3);

                HttpRequest request1 = createRequest().setUri(uri1)
                        .setMethod(HttpGet.METHOD_NAME).addHeaderField("X-OC-ContentEncoding", "identity")
                        .addHeaderField("X-OC-AddHeader_Last-Modified", "-86400")
                        .addHeaderField("X-OC-AddHeader_Date", "GMT")
                        .addHeaderField("X-OC-ResponseContentSize", "31415,l").getRequest();
                HttpRequest request2 = createRequest().setUri(uri2)
                        .setMethod(HttpGet.METHOD_NAME).addHeaderField("X-OC-ContentEncoding", "identity")
                        .addHeaderField("X-OC-AddHeader_Last-Modified", "-86400")
                        .addHeaderField("X-OC-AddHeader_Date", "GMT")
                        .addHeaderField("X-OC-ResponseContentSize", "31415,l").getRequest();
                HttpRequest request3 = createRequest().setUri(uri3)
                        .setMethod(HttpGet.METHOD_NAME).addHeaderField("X-OC-ContentEncoding", "identity")
                        .addHeaderField("X-OC-AddHeader_Last-Modified", "-86400")
                        .addHeaderField("X-OC-AddHeader_Date", "GMT")
                        .addHeaderField("X-OC-ResponseContentSize", "31415,l").getRequest();

                sendRequest2(request1);
                sendRequest2(request2);
                sendRequest2(request3);

                logcatUtil.stop();
                logcatUtil.logTasks();
                List<RadioLogEntry> wcdmaEntries = getWcdmaEntries(radiologTask.getLogEntries());
                for (RadioLogEntry entry : wcdmaEntries) {
                    logger.info(entry.toString());
                    if (entry.getCurrentState() == RadioStateType.cell_pch)
                        isPchEnable = true;
                    if (entry.getCurrentState() == RadioStateType.cell_dch)
                        isDchEnable = true;
                }
                if (isPchEnable && isDchEnable) {
                    break;
                }
            }
            if (!isDchEnable)
                throw new AssertionFailedError("Can't find dch state in radiologs.");
            if (!isPchEnable)
                throw new AssertionFailedError("Can't find pch state in radiologs.");
        } finally {
            if (logcatUtil != null) {
                logcatUtil.stop();
            }
        }
    }

    /**
     * <h3>Verify that radio channel changes state to cell_fach.</h3>
     * actions:
     * <ol>
     * <li>switch off screen for 40 sec</li>
     * <li>switch no screen</li>
     * <li>send 1 request to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     * <li>check that last state of radio channel is cell_fach</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @LargeTest
    @DeviceOnly
    public void test_008_PchDchTransitionScreenOfOn() throws Exception {
        enableMobileNetwork();

        RadiologTask radiologTask = new RadiologTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), radiologTask);
        logcatUtil.start();

        ScreenUtils.screenOff();
        TestUtil.sleep(FACH_PCH_TRANSITION_TIME + 10 * 1000);
        ScreenUtils.screenOn();

        try {
            sendSimpleRequest();
            logcatUtil.stop();
            logcatUtil.logTasks();
            List<RadioLogEntry> wcdmaEntries = getWcdmaEntries(radiologTask.getLogEntries());
            boolean isFachEnable = false;
            for (RadioLogEntry entry : wcdmaEntries) {
                logger.info(entry.toString());
                if (entry.getCurrentState() == RadioStateType.cell_fach)
                    isFachEnable = true;
            }
            if (!isFachEnable)
                throw new AssertionFailedError("Can't find fach state in radiologs during test_08_PchDchTransitionScreenOfOn runtime.");
        } finally {
            logcatUtil.stop();
        }
    }

    /**
     * <h3>Verify that RadioLog is absent when wifi interface is on.</h3>
     * actions:
     * <ol>
     * <li>switch wifi ON</li>
     * <li>wait for 20 sec</li>
     * <li>send 3 requests to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     * <li>check that radio channel is not cell_dch,_fach, cell_pch or idle</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @LargeTest
    @DeviceOnly
    public void test_010_WifiUsing() throws Exception {
        disableMobileNetwork();
        TestUtil.sleep(20 * 1000);

        RadiologTask radiologTask = new RadiologTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), radiologTask);
        logcatUtil.start();

        try {
            sendSimpleRequest();
            TestUtil.sleep(DCH_FACH_TRANSITION_TIME);
            sendSimpleRequest();
            TestUtil.sleep(FACH_PCH_TRANSITION_TIME);
            sendSimpleRequest();
            logcatUtil.stop();
            logcatUtil.logTasks();
            List<RadioLogEntry> wcdmaEntries = getWcdmaEntries(radiologTask.getLogEntries());
            if (wcdmaEntries.size() > 0) {
                throw new AssertionFailedError("RadioLogs(wcdma) shouldn't be observe in log but was.");
            }
        } finally {
            logcatUtil.stop();
        }
    }

    /**
     * <h3>Verify that status in RadioLog after switching Airplane mode ON when radio chanel is busy.</h3>
     * actions:
     * <ol>
     * <li>switch mobile network ON</li>
     * <li>wait for 20 sec</li>
     * <li>send 1 request to testrunner</li>
     * <li>switch on Airplane mode</li>
     * </ol>
     * checks:
     * <ol>
     * <li>check that radio channel was not idle</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @LargeTest      // ? must be investigated
    @DeviceOnly
    public void test_011_DchIdleAirplaneMode() throws Throwable {
        enableMobileNetwork();
        TestUtil.sleep(20 * 1000);

        String resource = "asimov_airplane_mode_test";
        final String uri = createTestResourceUri(resource);
        final int timeModeChanged = 5 * 1000;
        final Context context = getContext();
        final HttpRequest request = createRequest().setUri(uri)
                .setMethod(HttpGet.METHOD_NAME).addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-ResponseContentSize", "714150,p").getRequest();


        TestCaseThread t1 = new TestCaseThread() {
            @Override
            public void run() throws Throwable {
                try {
                    sendRequest(request);
                } catch (Exception e) {
                    logger.error(ExceptionUtils.getStackTrace(e));
                }
            }
        };

        TestCaseThread t2 = new TestCaseThread(timeModeChanged) {
            @Override
            public void run() throws Throwable {
                setAirplaneMode(context, true);
            }
        };

        RadiologTask radiologTask = new RadiologTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), radiologTask);
        logcatUtil.start();
        int maxRetries = 5;
        boolean isIdleState = false;

        try {
            for (int i = 1; i <= maxRetries; i++) {
                executeThreads(t1, t2);
                setAirplaneMode(context, false);
                List<RadioLogEntry> wcdmaEntries = getWcdmaEntries(radiologTask.getLogEntries());

                for (RadioLogEntry entry : wcdmaEntries) {
                    logger.info(entry.toString());
                    if (entry.getCurrentState() == RadioStateType.idle) {
                        isIdleState = true;
                    }
                }
                if (isIdleState) {
                    break;
                }
            }
            if (!isIdleState) {
                throw new AssertionFailedError("There isn't idle state after switching to airplane mode.");
            }
        } finally {
            setAirplaneMode(context, false);
            logcatUtil.stop();
        }
    }

    /**
     * <h3>Verify that status in RadioLog after switching Airplane mode ON and OFF.</h3>
     * actions:
     * <ol>
     * <li>switch mobile network ON</li>
     * <li>wait for 20 sec</li>
     * <li>send 1 request to testrunner</li>
     * <li>wait for 5 sec</li>
     * <li>switch Airplane mode ON</li>
     * <li>wait for 20 sec</li>
     * <li>switch Airplane mode OFF</li>
     * </ol>
     * checks:
     * <ol>
     * <li>check that radio channel is idle</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @LargeTest
    @DeviceOnly
    public void test_012_FachIdleAirplaneMode() throws Throwable {
        enableMobileNetwork();
        TestUtil.sleep(20 * 1000);

        RadiologTask radiologTask = new RadiologTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), radiologTask);
        logcatUtil.start();

        try {
            sendSimpleRequest();
            TestUtil.sleep(5 * 1000);
            setAirplaneMode(getContext(), true);
            TestUtil.sleep(20 * 1000);

            setAirplaneMode(getContext(), false);
            logcatUtil.stop();
            logcatUtil.logTasks();
            List<RadioLogEntry> wcdmaEntries = getWcdmaEntries(radiologTask.getLogEntries());
            boolean isIdleState = false;
            for (RadioLogEntry entry : wcdmaEntries) {
                logger.info(entry.toString());
                if (entry.getCurrentState() == RadioStateType.idle && entry.getPreviousState() == RadioStateType.cell_fach) {
                    isIdleState = true;
                }
            }
            if (!isIdleState) {
                throw new AssertionFailedError("There isn't idle state after switching to airplane mode.");
            }

        } finally {
            setAirplaneMode(getContext(), false);
            logcatUtil.stop();
        }
    }

    /**
     * <h3>Verify that status pch - idle transition is done after switching to airplane mode.</h3>
     * actions:
     * <ol>
     * <li>switch mobile network ON</li>
     * <li>wait for 10 sec</li>
     * <li>do up to 5 times: screen OFF, wait for 40 sec, screen ON, Airplane mode ON, wait for 10 sec, Airplane mode OFF</li>
     * </ol>
     * checks:
     * <ol>
     * <li>check that radio channel switched from cell_pch to idle</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Ignore
    //@Execute
    @LargeTest
    @DeviceOnly
    public void test_013_PchIdleAirplaneMode() throws Throwable {
        MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());
        mobileNetworkUtil.on3gOnly();
        TestUtil.sleep(10 * 1000);

        RadiologTask radiologTask = new RadiologTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), radiologTask);
        boolean isIdleState = false;
        int maxRetries = 5;
        logcatUtil.start();
        try {
            ScreenUtils.screenOff();
            logger.info("Switching to screen of. Time: " + System.currentTimeMillis());
            TestUtil.sleep(FACH_PCH_TRANSITION_TIME + 10 * 1000);
            logger.info("Switching to screen on. Time: " + System.currentTimeMillis());
            ScreenUtils.screenOn();
            logger.info("Switching to airplane mode ON. Time: " + System.currentTimeMillis());
            setAirplaneMode(getContext(), true);

            TestUtil.sleep(10 * 1000);
            logger.info("Switching to airplane mode OFF. Time: " + System.currentTimeMillis());
            setAirplaneMode(getContext(), false);
            TestUtil.sleep(30 * 1000);
            mobileNetworkUtil.on3gOnly();
            TestUtil.sleep(15 * 1000);
            logcatUtil.stop();
            logcatUtil.logTasks();

            List<RadioLogEntry> wcdmaEntries = getWcdmaEntries(radiologTask.getLogEntries());
            for (RadioLogEntry entry : wcdmaEntries) {
                logger.info(entry.toString());
                if (entry.getCurrentState() == RadioStateType.idle
                    //&& entry.getPreviousState() == RadioStateType.cell_pch
                        ) {
                    isIdleState = true;
                }
            }
            if (!isIdleState) {
                throw new AssertionFailedError("There isn't pch - idle transition after switching to airplane mode.");
            }
        } finally {
            logcatUtil.stop();
            setAirplaneMode(getContext(), false);
        }
    }

    /**
     * <h3>Verify that status idle - cell_fach transition is done after switching to airplane mode.</h3>
     * actions:
     * <ol>
     * <li>switch mobile network ON</li>
     * <li>wait for 20 sec</li>
     * <li>switch Airplane mode ON</li>
     * <li>wait for 30 sec</li>
     * <li>switch Airplane mode OFF</li>
     * <li>wait for 15 sec</li>
     * <li>send 1 request to testrunner</li>
     * <li>wait for 15 sec</li>
     * <li>switch Airplane mode OFF</li>
     * </ol>
     * checks:
     * <ol>
     * <li>check that radio channel switched from idle to cell_fach</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @LargeTest
    @DeviceOnly
    public void test_014_IdleFachAirplaneMode() throws Throwable {
        MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());
        mobileNetworkUtil.on3gOnly();
        TestUtil.sleep(20 * 1000);

        RadiologTask radiologTask = new RadiologTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), radiologTask);
        logcatUtil.start();
        String uri = "";
        try {
            setAirplaneMode(getContext(), true);
            TestUtil.sleep(FACH_PCH_TRANSITION_TIME);
            setAirplaneMode(getContext(), false);
            TestUtil.sleep(30 * 1000);
            mobileNetworkUtil.on3gOnly();
            TestUtil.sleep(15 * 1000);
            sendSimpleRequest();
            TestUtil.sleep(15 * 1000);
            logcatUtil.stop();
            logcatUtil.logTasks();
            boolean isFachIdleState = false;
            List<RadioLogEntry> wcdmaEntries = getWcdmaEntries(radiologTask.getLogEntries());
            for (RadioLogEntry entry : wcdmaEntries) {
                logger.info(entry.toString());
                if (entry.getCurrentState() == RadioStateType.cell_fach && entry.getPreviousState() == RadioStateType.idle) {
                    isFachIdleState = true;
                }
            }
            if (!isFachIdleState)
                throw new AssertionFailedError("There isn't idle - fach transition after switching to airplane mode.");
        } finally {
            logcatUtil.stop();
            setAirplaneMode(getContext(), false);
        }
    }

    /**
     * <h3>Verify that status idle - cell_fach transition is done after switching to airplane mode.</h3>
     * actions:
     * <ol>
     * <li>switch mobile network ON</li>
     * <li>wait for 20 sec</li>
     * <li>switch Airplane mode ON</li>
     * <li>wait for 30 sec</li>
     * <li>switch Airplane mode OFF</li>
     * <li>wait for 15 sec</li>
     * <li>send 1 request to testrunner</li>
     * <li>wait for 15 sec</li>
     * <li>switch Airplane mode OFF</li>
     * </ol>
     * checks:
     * <ol>
     * <li>check that radio channel switched from idle to cell_fach</li>
     * <li>check that radio channel last state is cell_dch</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @LargeTest
    @DeviceOnly
    public void test_015_IdleFachDchAirplaneMode() throws Throwable {
        MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());
        mobileNetworkUtil.on3gOnly();
        //enableMobileNetwork();
        TestUtil.sleep(20 * 1000);

        RadiologTask radiologTask = new RadiologTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), radiologTask);
        logcatUtil.start();

        String resource = "asimov_test_15_idleFachDchAirplaneMode";
        String uri = createTestResourceUri(resource);
        final HttpRequest request = createRequest().setUri(uri)
                .setMethod(HttpGet.METHOD_NAME).addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-ResponseContentSize", "123456,p").getRequest();

        try {
            setAirplaneMode(getContext(), true);
            TestUtil.sleep(FACH_PCH_TRANSITION_TIME);
            setAirplaneMode(getContext(), false);
            TestUtil.sleep(30 * 1000);
            mobileNetworkUtil.on3gOnly();
            TestUtil.sleep(15 * 1000);
            checkMiss(request, 1);
            TestUtil.sleep(15 * 1000);
            setAirplaneMode(getContext(), false);
            logcatUtil.stop();
            logcatUtil.logTasks();
            boolean isFachIdleState = false;
            boolean isDchState = false;
            List<RadioLogEntry> wcdmaEntries = getWcdmaEntries(radiologTask.getLogEntries());
            for (RadioLogEntry entry : wcdmaEntries) {
                logger.info(entry.toString());
                if (entry.getCurrentState() == RadioStateType.cell_fach && entry.getPreviousState() == RadioStateType.idle) {
                    isFachIdleState = true;
                }
                if (entry.getCurrentState() == RadioStateType.cell_dch)
                    isDchState = true;
            }
            if (!isFachIdleState)
                throw new AssertionFailedError("There isn't idle - fach transition after switching to airplane mode.");
            if (!isDchState)
                throw new AssertionFailedError("There isn't dch state after switching to airplane mode.");

        } finally {
            setAirplaneMode(getContext(), false);
            logcatUtil.stop();
        }
    }

    /**
     * <h3>Verify correctness of T1.</h3>
     * actions:
     * <ol>
     * <li>switch mobile network ON</li>
     * <li>wait for 20 sec</li>
     * <li>switch off screen for 60 sec</li>
     * <li>switch no screen</li>
     * </ol>
     * checks:
     * <ol>
     * <li>check that radio channel switched to cell_fach state after screen turned ON </li>
     * </ol>
     *
     * @throws Exception
     * Reason of ignore: current functionality does not depend on screen.
     */
    @Ignore
    //@Execute
    @LargeTest
    @DeviceOnly
    public void test_016_TimingTurnedScreenOfOneMinute() throws Throwable {
        MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());
        mobileNetworkUtil.onWifiOnly();
        mobileNetworkUtil.on3gOnly();
        //enableMobileNetwork();
        //TestUtils.sleep(20 * 1000);

        RadiologTask radiologTask = new RadiologTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), radiologTask);
        logcatUtil.start();

        try {
            ScreenUtils.screenOff();
            logger.info("Screen turned off : " + System.currentTimeMillis());
            TestUtil.sleep(60 * 1000);
            ScreenUtils.screenOn();
            logger.info("Screen turned on : " + System.currentTimeMillis());

            TestUtil.sleep(15 * 1000);
            ScreenUtils.screenOn();
            logcatUtil.stop();
            logcatUtil.logTasks();
            List<RadioLogEntry> wcdmaEntries = getWcdmaEntries(radiologTask.getLogEntries());
            boolean isPchState = false;
            for (RadioLogEntry entry : wcdmaEntries) {
                logger.info(entry.toString());
                if (entry.getCurrentState() == RadioStateType.cell_fach)
                    isPchState = true;
            }
            if (!isPchState)
                throw new AssertionFailedError("Can't find pch state after turned screen.");
        } finally {
            ScreenUtils.screenOn();
            logcatUtil.stop();
        }
    }

    /**
     * <h3>Verify restarting of T1 if any data-in,-out passed.</h3>
     * actions:
     * <ol>
     * <li>switch mobile network ON</li>
     * <li>wait for 20 sec</li>
     * <li>send 1 request to testrunner</li>
     * <li>do up to 5 times:send 1 request to testrunner, screen OFF, wait for 20 sec, screen ON, send 1 request to testrunner, wait for 45 secF</li>
     * </ol>
     * checks:
     * <ol>
     * <li>check that radio channel switched to cell_pch state during 30 sec</li>
     * </ol>
     *
     * @throws Exception
     * Reason of ignore: current functionality does not depend on screen.
     */
    @Ignore
    //@Execute
    @LargeTest
    @DeviceOnly
    public void test_017_TimingTurnedScreenOf20Sec() throws Throwable {
        enableMobileNetwork();
        TestUtil.sleep(20 * 1000);

        RadiologTask radiologTask = new RadiologTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), radiologTask);
        logcatUtil.start();

        String resource = "asimov_test_17_timingTurnedScreenOf20Sec";
        String uri = createTestResourceUri(resource);
        final HttpRequest request = createRequest().setUri(uri)
                .setMethod(HttpGet.METHOD_NAME).addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-ResponseContentSize", "15,d").getRequest();
        boolean isPchState = false;
        int maxRetries = 5;
        try {
            for (int i = 1; i <= maxRetries; i++) {
                sendSimpleRequest();

                ScreenUtils.screenOff();
                TestUtil.sleep(20 * 1000);
                ScreenUtils.screenOn();
                checkMiss(request, 1);
                TestUtil.sleep(30 * 1000 + 15 * 1000);
                logcatUtil.stop();
                logcatUtil.logTasks();
                List<RadioLogEntry> wcdmaEntries = getWcdmaEntries(radiologTask.getLogEntries());

                for (RadioLogEntry entry : wcdmaEntries) {
                    logger.info(entry.toString());
                    if (entry.getCurrentState() == RadioStateType.cell_pch) {
                        assertTrue("Time in previous state expected > 30 sec but was " + Integer.toString((int) entry.getTimeInPreviousState() / 1000),
                                entry.getTimeInPreviousState() > FACH_PCH_TRANSITION_TIME);
                        isPchState = true;
                    }
                }
                if (isPchState) {
                    break;
                }
            }
            if (!isPchState) {
                throw new AssertionFailedError("Can't find pch state in radiologs.");
            }
        } finally {
            logcatUtil.stop();
            ScreenUtils.screenOn();
        }
    }
}
