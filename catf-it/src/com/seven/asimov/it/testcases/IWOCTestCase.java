package com.seven.asimov.it.testcases;


import com.seven.asimov.it.base.*;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.ScreenUtils;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.pollingTasks.StartPollTask;
import com.seven.asimov.it.utils.logcat.wrappers.StartPollWrapper;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import com.seven.asimov.it.utils.sa.SaRestUtil;
import com.seven.asimov.it.utils.sms.SmsUtil;
import com.seven.asimov.it.utils.sms.SmsUtil.InvalidationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class IWOCTestCase extends TcpDumpTestCase {
    private static final Logger logger= LoggerFactory.getLogger(IWOCTestCase.class.getSimpleName());
    private static final String AGGRESSIVENESS_REST_PROPERTY_NAME_IWOC = "noCacheInvalidateAggressiveness";

    public void checkAggressiveIWOC(String resource, int aggressivenessLevel, ScreenState screenState,
                                    RadioState radioState, boolean isLongPolling, boolean invalidateReceived)
            throws Throwable {


        Policy policy=new Policy(AGGRESSIVENESS_REST_PROPERTY_NAME_IWOC, String.valueOf(aggressivenessLevel), AGGRESSIVENESS_REST_PROPERTY_PATH, true);
        ScreenUtils.ScreenSpyResult spy = null;

        try {

            SaRestUtil.updateParameter(policy);

            logger.trace("setting screen spy...");
            spy = ScreenUtils.switchScreenAndSpy(getContext(),
                    screenState == ScreenState.SCREEN_ON);

            if (radioState == RadioState.RADIO_UP) {
                TestCaseThread radioUpKeeperThread = createRadioUpKeeperThread();
                if (isLongPolling) {
                    executeThreads(1000 * 1000, radioUpKeeperThread, createIWOCLongPollingThread(resource,
                            LP_REQUEST_INTERVAL_MS, invalidateReceived, radioUpKeeperThread));
                } else {
                    executeThreads(radioUpKeeperThread,
                            createIWOCRegularPollingThread(resource, RI_REQUEST_INTERVAL_MS, invalidateReceived,
                                    radioUpKeeperThread));
                }
            } else {
                if (isLongPolling) {
                    executeThreads(1000 * 1000, createIWOCLongPollingThread(resource,
                            LP_REQUEST_INTERVAL_MS, invalidateReceived));
                } else {
                    executeThreads(createIWOCRegularPollingThread(resource, RI_REQUEST_INTERVAL_MS, invalidateReceived));
                }
            }
        } finally {
            if (null != spy) {
                ScreenUtils.finishScreenSpy(getContext(), spy);
            }
        }
    }

    private TestCaseThread createIWOCRegularPollingThread(String resource, final int requestInterval,
                                                          boolean invalidateReceived) {
        return createIWOCRegularPollingThread(resource, requestInterval, invalidateReceived, null);
    }

    private TestCaseThread createIWOCRegularPollingThread(String resource, final int requestInterval,
                                                          final boolean invalidateReceived,
                                                          final TestCaseThread radioUpKeeperThread) {
        final String uri = createTestResourceUri(resource);
        final HttpRequest request = createRequest().setUri(uri)
                .setMethod("GET").addHeaderField("X-OC-ContentEncoding", "identity").getRequest();

        final StartPollTask startPollTask = new StartPollTask();
        final LogcatUtil logcatUtil = new LogcatUtil(getContext(), startPollTask);
        logcatUtil.start();
        return new TestCaseThread() {
            @Override
            public void run() throws Throwable {
                try {
                    PrepareResourceUtil.prepareResource(uri, false);
                    requestId = 0;

                    HttpResponse response = checkMiss(request, ++requestId, VALID_RESPONSE, requestInterval); // (1) MISS
                    response = checkMiss(request, ++requestId, VALID_RESPONSE, requestInterval);// (2) MISS
                    response = checkMiss(request, ++requestId, VALID_RESPONSE, requestInterval);// (3) MISS

                    // (4) HIT
                    response = checkHit(request, ++requestId, VALID_RESPONSE);

                    logcatUtil.stop();
                    assertTrue("Start of polling should be reported in client log", !startPollTask.getLogEntries().isEmpty());
                    StartPollWrapper startPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
                   logger.info("Start poll wrapper object" + startPoll);

                    logSleeping(requestInterval - SMS_SEND_DELAY - response.getDuration());
                    (new SmsUtil(getContext())).sendInvalidationSms(Integer.parseInt(startPoll.getSubscriptionId()), InvalidationType.INVALIDATE_WITHOUT_CACHE.byteVal);
                    logSleeping(SMS_SEND_DELAY);
                    if (invalidateReceived) {
                        checkMiss(request, ++requestId);
                    } else {
                        checkHit(request, ++requestId);
                    }
                } finally {
                    logcatUtil.stop();
                    PrepareResourceUtil.invalidateResourceSafely(uri);
                    if (radioUpKeeperThread != null) radioUpKeeperThread.interruptSoftly();
                }
            }
        };
    }

    private TestCaseThread createIWOCLongPollingThread(String resource, final int requestInterval,
                                                       boolean invalidateReceived) {
        return createIWOCLongPollingThread(resource, requestInterval, invalidateReceived, null);
    }

    private TestCaseThread createIWOCLongPollingThread(String resource, final int requestInterval,
                                                       final boolean invalidateReceived,
                                                       final TestCaseThread radioUpKeeperThread) {
        final String uri = createTestResourceUri(resource);
        final HttpRequest request = createRequest().setUri(uri)
                .setMethod("GET").addHeaderField("X-OC-ContentEncoding", "identity")
                .getRequest();
        final StartPollTask startPollTask = new StartPollTask();
        final LogcatUtil logcatUtil = new LogcatUtil(getContext(), startPollTask);
        logcatUtil.start();
        return new TestCaseThread() {
            @Override
            public void run() throws Throwable {
                try {
                    PrepareResourceUtil.prepareResourceWithDelay(uri, LP_DELAY_MS / 1000);
                    int requestId = 0;
                    // (1) MISS
                    HttpResponse response = checkMiss(request, ++requestId, VALID_RESPONSE);
                    logSleeping(requestInterval - response.getDuration());
                    // (2) MISS, start polling
                    response = checkMiss(request, ++requestId, VALID_RESPONSE);
                    long preparationStart = System.currentTimeMillis();
                    logSleeping((long) (requestInterval * 0.75));
                    logcatUtil.stop();
                    assertTrue("Start of polling should be reported in client log", !startPollTask.getLogEntries().isEmpty());
                    StartPollWrapper startPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
                    logger.info("Start poll wrapper object" + startPoll);
                    long preparationEnd = System.currentTimeMillis();
                    long preparationDelay = preparationEnd - preparationStart;
                    logSleeping(requestInterval - preparationDelay - response.getDuration());
                    // (3) HIT
                    response = checkHit(request, ++requestId, VALID_RESPONSE);
                    logSleeping(requestInterval - SMS_SEND_DELAY - response.getDuration());
                    (new SmsUtil(getContext())).sendInvalidationSms(Integer.parseInt(startPollTask.getLogEntries().get(0).getSubscriptionId()),(byte) 2);
                    logSleeping(SMS_SEND_DELAY);
                    if (invalidateReceived) {
                        checkMiss(request, ++requestId);
                    } else {
                        checkHit(request, ++requestId);
                    }
                } finally {
                    logcatUtil.stop();
                    PrepareResourceUtil.invalidateResourceSafely(uri);
                    if (radioUpKeeperThread != null) radioUpKeeperThread.interruptSoftly();
                }
            }
        };
    }

    private TestCaseThread createRadioUpKeeperThread() {
        return new TestCaseThread() {
            @Override
            public void run() throws Throwable {
                final String uri = createTestResourceUri("asimov_it_test_radio_up", false);
                while (!isInterruptedSoftly()) {
                    sendRequestWithoutLogging(createRequest().setUri(uri).setMethod("GET").getRequest());
                    TestUtil.sleep(RADIO_KEEPER_DELAY_MS);
                }
            }
        };
    }

    public void setDefaultAggressivenessProperty() throws Exception {
        setIWOCAggressivenessLevel(0);
    }

    private void setIWOCAggressivenessLevel(int level) throws Exception {
        PMSUtil.addPoliciesWithCheck(new Policy[] {new Policy(AGGRESSIVENESS_REST_PROPERTY_NAME_IWOC, Integer.toString(level), AGGRESSIVENESS_REST_PROPERTY_PATH, true)});
    }

    public void cleanAggressivenessLevel() throws Exception {
        PMSUtil.cleanPaths(new String[]{AGGRESSIVENESS_REST_PROPERTY_PATH});
    }



    protected String createUri() {
        UUID guid = UUID.randomUUID();
        String pathEnd = "asimov_" + guid.toString() + "_" + Thread.currentThread().getStackTrace()[4].getMethodName().toLowerCase();
        return createTestResourceUri(pathEnd);
    }

    public void executeIWOCCacheTest(int requestInterval, boolean isOrdinaryPoll, boolean invResponseEnabled, boolean isIntervalChanged) throws Throwable {
        StartPollTask startPollTask = new StartPollTask();
        final HttpRequest request = createRequest().addHeaderField("X-OC-Encoding", "identity").getRequest();
        String uri = createUri();
        request.setUri(uri);
        PrepareResourceUtil.prepareResource(uri, false);
        if (!isOrdinaryPoll) setResourceDelay(request, LP_DELAY_MS / 1000);
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), startPollTask);
        logcatUtil.start();
        HttpResponse response;
        try {
            int requestId = 0;
            if (isOrdinaryPoll) {
                response = checkMiss(request, ++requestId);
                logSleeping(requestInterval - response.getDuration());
            }
            response = checkMiss(request, ++requestId);
            logSleeping(requestInterval - response.getDuration());
            response = checkMiss(request, ++requestId);
            long preparationStart = System.currentTimeMillis();
            logSleeping((long) (requestInterval * 0.75));
            logcatUtil.stop();
            assertTrue("Start of polling should be reported in client log", !startPollTask.getLogEntries().isEmpty());
            StartPollWrapper startPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
            logger.info("Start poll wrapper object" + startPoll);
            long preparationEnd = System.currentTimeMillis();
            long preparationDelay = preparationEnd - preparationStart;
            logSleeping((int) (requestInterval * 0.25) - preparationDelay - response.getDuration());
            response = checkHit(request, ++requestId);
            int SMS_SEND_DELAY = 1000;
            logSleeping(requestInterval - SMS_SEND_DELAY - response.getDuration());
            (new SmsUtil(getContext())).sendInvalidationSms(Integer.parseInt(startPollTask.getLogEntries().get(0).getSubscriptionId()),(byte) 2);
            if (invResponseEnabled) PrepareResourceUtil.prepareResource(request.getUri(), true);
            logSleeping(SMS_SEND_DELAY);
            if (isIntervalChanged) requestInterval = LP_REQUEST_INTERVAL_MS;
            checkMiss(request, ++requestId);
            logSleeping(requestInterval - response.getDuration());
            checkHit(request, ++requestId);
        } finally {
            logcatUtil.stop();
            PrepareResourceUtil.invalidateResourceSafely(request.getUri());
        }
    }

    private void setResourceDelay(final HttpRequest request, final int delaySeconds) throws InterruptedException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                logger.info("Setting response delay to " + delaySeconds + " sec");
                HttpRequest modificationRequest = request.copy();
                modificationRequest.addHeaderField(new HttpHeaderField("X-OC-Stateless-Sleep", "true"));
                modificationRequest.addHeaderField(new HttpHeaderField("X-OC-ChangeSleep", Integer.toString(delaySeconds)));
                sendRequest2(modificationRequest, false, true);
            }
        }).start();
        Thread.sleep(5 * 1000);
    }

    protected void startThreadIWOCToCome(StartPollTask startPollTask, int interval) {
        startThreadIWOCToCome(startPollTask, interval, null);
    }

    protected void startThreadIWOCToCome(final StartPollTask startPollTask, final int interval, final String uriToChange) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    logSleeping(interval * 1000);
                    (new SmsUtil(getContext())).sendInvalidationSms(Integer.parseInt(startPollTask.getLogEntries().get(0).getSubscriptionId()), (byte) 2);
                    startPollTask.reset();
                    if (uriToChange != null) {
                        logSleeping(30 * 1000);
                        PrepareResourceUtil.prepareResource(uriToChange, true);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}

