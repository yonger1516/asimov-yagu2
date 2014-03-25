package com.seven.asimov.it.testcases;

import android.util.Log;
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
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import com.seven.asimov.it.utils.sms.SmsUtil;

public class IWCTestCase extends TcpDumpTestCase {
    private static final String TAG = IWCTestCase.class.getSimpleName();

    public static final int AGGRESSIVENESS_LEVEL_0 = 0;
    public static final int AGGRESSIVENESS_LEVEL_1 = 1;
    public static final int AGGRESSIVENESS_LEVEL_2 = 2;
    public static final int AGGRESSIVENESS_LEVEL_3 = 3;

    public static final long MIN_NON_RMP_PERIOD = 67000;
    private static final int RI_REQUEST_INTERVAL_MS = (int) MIN_NON_RMP_PERIOD;
    private static final int LP_REQUEST_INTERVAL_MS = 100 * 1000;
    private static final int LP_DELAY_MS = 70 * 1000;
    private static final int RADIO_KEEPER_DELAY_MS = 3 * 1000;
    private static final int DORMANCY_TIMEOUT_SEC = 15;

    private static final String AGGRESSIVENESS_REST_PROPERTY_PATH = "@asimov@http";
    private static final String AGGRESSIVENESS_REST_PROPERTY_NAME = "cache_invalidate_aggressiveness";
    public static String VALID_RESPONSE = "tere";

    public enum RadioState {
        RADIO_UP, RADIO_DOWN
    }

    public enum ScreenState {
        SCREEN_ON, SCREEN_OFF
    }

    protected void checkAggressiveIWC(String resource, int aggressivenessLevel, ScreenState screenState,
                                      RadioState radioState, boolean isLongPolling, boolean invalidateReceived)
            throws Throwable {

        ScreenUtils.ScreenSpyResult spy = ScreenUtils.switchScreenAndSpy(getContext(),
                screenState == ScreenState.SCREEN_ON);
        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(AGGRESSIVENESS_REST_PROPERTY_NAME, String.valueOf(aggressivenessLevel), AGGRESSIVENESS_REST_PROPERTY_PATH, true)});
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
            ScreenUtils.finishScreenSpy(getContext(), spy);
            PMSUtil.cleanPaths(new String[]{AGGRESSIVENESS_REST_PROPERTY_PATH});
        }
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
                    Log.i(TAG, "Start poll wrapper object" + startPoll);
                    PrepareResourceUtil.prepareResourceWithDelayedChange(uri, DORMANCY_TIMEOUT_SEC);
                    long preparationEnd = System.currentTimeMillis();
                    long preparationDelay = preparationEnd - preparationStart;
                    logSleeping(requestInterval - preparationDelay - response.getDuration() - 10 * 1000);
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
                    Log.i(TAG, "Start poll wrapper object" + startPoll);
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
