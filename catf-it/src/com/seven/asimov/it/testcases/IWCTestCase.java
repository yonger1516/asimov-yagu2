package com.seven.asimov.it.testcases;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.base.TestCaseThread;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.ScreenUtils;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.pollingTasks.StartPollTask;
import com.seven.asimov.it.utils.logcat.wrappers.StartPollWrapper;
import com.seven.asimov.it.utils.pms.Policy;
import com.seven.asimov.it.utils.sa.SaRestUtil;
import com.seven.asimov.it.utils.sms.SmsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IWCTestCase extends TcpDumpTestCase {
    private static final Logger logger= LoggerFactory.getLogger(IWOCTestCase.class.getSimpleName());
    private static final String AGGRESSIVENESS_REST_PROPERTY_NAME = "cacheInvalidateAggressiveness";


    protected void checkAggressiveIWC(String resource, int aggressivenessLevel, ScreenState screenState,
                                      RadioState radioState, boolean isLongPolling, boolean invalidateReceived)
            throws Throwable {
        Policy policy=new Policy(AGGRESSIVENESS_REST_PROPERTY_NAME, String.valueOf(aggressivenessLevel), AGGRESSIVENESS_REST_PROPERTY_PATH, true);
        ScreenUtils.ScreenSpyResult spy = null;

        try {
            SaRestUtil.updateParameter(policy);

            logger.trace("setting screen spy...");
            spy = ScreenUtils.switchScreenAndSpy(getContext(),
                    screenState == ScreenState.SCREEN_ON);

            logger.trace("start business action");
            if (radioState == RadioState.RADIO_UP) {
                TestCaseThread radioUpKeeperThread = createRadioUpKeeperThread();
                if (isLongPolling) {
                    executeThreads(radioUpKeeperThread, createLongPollingThread(resource, LP_DELAY_MS,
                            LP_REQUEST_INTERVAL_MS, invalidateReceived, radioUpKeeperThread));
                } else {
                    executeThreads(radioUpKeeperThread,
                            createRegularPollingThread(resource, RI_REQUEST_INTERVAL_MS, invalidateReceived,
                                    radioUpKeeperThread));
                }
            } else {
                if (isLongPolling) {
                    executeThreads(createLongPollingThread(resource, LP_DELAY_MS,
                            LP_REQUEST_INTERVAL_MS, invalidateReceived));
                } else {
                    executeThreads(createRegularPollingThread(resource, RI_REQUEST_INTERVAL_MS, invalidateReceived));
                }
            }
            assertTrue("Screen state is not as expected ", spy.isScreenAsExpected());
        } finally {
            if (null != spy) {
                ScreenUtils.finishScreenSpy(getContext(), spy);
            }
            //SaRestUtil.cleanParameter(policy);
        }
    }

    private TestCaseThread createRadioUpKeeperThread() {
        return new TestCaseThread() {
            @Override
            public void run() throws Throwable {
                final String uri = createTestResourceUri("asimov_it_test_radio_up", false);
                while (!isInterruptedSoftly()) {

                    logger.debug("Sending a ping...");
                    sendRequestWithoutLogging(createRequest().setUri(uri).setMethod("GET").getRequest());
                    TestUtil.sleep(RADIO_KEEPER_DELAY_MS);
                }
            }
        };
    }

    private TestCaseThread createRegularPollingThread(String resource, final int requestInterval,
                                                      boolean invalidateReceived) {
        return createRegularPollingThread(resource, requestInterval, invalidateReceived, null);
    }

    private TestCaseThread createRegularPollingThread(String resource, final int requestInterval,
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
                    int requestId = 0;
                    HttpResponse response = checkMiss(request, ++requestId, VALID_RESPONSE, requestInterval); // (1) MISS
                    response = checkMiss(request, ++requestId, VALID_RESPONSE, requestInterval);// (2) MISS
                    response = checkMiss(request, ++requestId, VALID_RESPONSE, requestInterval);// (3) MISS
                    response = checkHit(request, ++requestId, VALID_RESPONSE); // (4) HIT
                    // invalidate
                    long preparationStart = System.currentTimeMillis();
                    logcatUtil.stop();
                    assertTrue("Start of polling should be reported in client log", !startPollTask.getLogEntries().isEmpty());
                    StartPollWrapper startPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
                    logger.info( "Start poll wrapper object" + startPoll);


                    PrepareResourceUtil.prepareResourceWithDelayedChange(uri, DORMANCY_TIMEOUT_SEC);
                    long preparationEnd = System.currentTimeMillis();
                    long preparationDelay = preparationEnd - preparationStart;
                    logSleeping(requestInterval - preparationDelay - response.getDuration() - SMS_SEND_DELAY);
                    (new SmsUtil(getContext())).sendInvalidationSms(Integer.parseInt(startPoll.getSubscriptionId()), SmsUtil.InvalidationType.INVALIDATE_WITH_CACHE.byteVal);
                    logSleeping(10 * 1000);
                    if (invalidateReceived) {
                        // (5) HIT, body should be changed
                        response = checkHit(request, ++requestId);
                        assertFalse("Cache entry should be invalidated", response.getBody().equals(VALID_RESPONSE));
                    } else {
                        // (5) HIT, body is the same
                        checkHit(request, ++requestId, VALID_RESPONSE);
                    }
                } finally {
                    PrepareResourceUtil.invalidateResourceSafely(uri);
                    if (radioUpKeeperThread != null) radioUpKeeperThread.interruptSoftly();
                }
            }
        };
    }

    private TestCaseThread createLongPollingThread(String resource, final int delay, final int requestInterval,
                                                   boolean invalidateReceived) {
        return createLongPollingThread(resource, delay, requestInterval, invalidateReceived, null);
    }

    private TestCaseThread createLongPollingThread(String resource, final int delay, final int requestInterval,
                                                   final boolean invalidateReceived,
                                                   final TestCaseThread radioUpKeeperThread) {
        final String uri = createTestResourceUri(resource);
        final HttpRequest request = createRequest().setUri(uri)
                .setMethod("GET").addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Stateless-Sleep", "true")
                .addHeaderField("X-OC-Sleep", Integer.toString(delay / 1000))
                .getRequest();

        final StartPollTask startPollTask = new StartPollTask();
        final LogcatUtil logcatUtil = new LogcatUtil(getContext(), startPollTask);
        logcatUtil.start();

        return new TestCaseThread() {
            @Override
            public void run() throws Throwable {
                try {
                    PrepareResourceUtil.prepareResource(uri, false);
                    int requestId = 0;
                    // (1) MISS
                    HttpResponse response = checkMiss(request, ++requestId, VALID_RESPONSE);
                    logSleeping(requestInterval - response.getDuration());
                    // (2) MISS, start polling
                    response = checkMiss(request, ++requestId, VALID_RESPONSE);
                    logSleeping(requestInterval - response.getDuration());

                    // Start invalidate thread
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                PrepareResourceUtil.prepareResourceWithDelayedChange(uri, 30);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }).start();
                    // (3) HIT
                    response = checkHit(request, ++requestId, VALID_RESPONSE);

                    long preparationStart = System.currentTimeMillis();
                    logcatUtil.stop();
                    assertTrue("Start of polling should be reported in client log", !startPollTask.getLogEntries().isEmpty());
                    final StartPollWrapper startPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
                    logger.info("Start poll wrapper object" + startPoll);
                    long preparationEnd = System.currentTimeMillis();
                    long preparationDelay = preparationEnd - preparationStart;

                    logSleeping(requestInterval - preparationDelay - response.getDuration());

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                logSleeping(60 * 1000);
                                (new SmsUtil(getContext())).sendInvalidationSms(Integer.parseInt(startPoll.getSubscriptionId()), SmsUtil.InvalidationType.INVALIDATE_WITH_CACHE.byteVal);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }).start();

                    if (invalidateReceived) {
                        // (4) HIT, body should be changed
                        response = checkHit(request, ++requestId);
                        assertFalse("Cache entry should be invalidated", response.getBody().equals(VALID_RESPONSE));
                    } else {
                        // (4) HIT, body is the same
                        checkHit(request, ++requestId, VALID_RESPONSE);
                    }
                } finally {
                    PrepareResourceUtil.invalidateResourceSafely(uri);
                    if (radioUpKeeperThread != null) radioUpKeeperThread.interruptSoftly();
                }
            }
        };
    }
}
