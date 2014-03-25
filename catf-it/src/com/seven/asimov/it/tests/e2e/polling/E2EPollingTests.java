package com.seven.asimov.it.tests.e2e.polling;

import android.test.suitebuilder.annotation.LargeTest;
import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.base.*;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.testcases.E2EPollingTestCase;
import com.seven.asimov.it.utils.HttpRequestMd5CalculatorUtil;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.e2eTasks.DelSubscriptionTask;
import com.seven.asimov.it.utils.logcat.tasks.pollingTasks.StartPollParamsTask;
import com.seven.asimov.it.utils.logcat.tasks.pollingTasks.StartPollTask;
import com.seven.asimov.it.utils.logcat.wrappers.DelSubscriptionWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.StartPollParamsWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.StartPollWrapper;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import junit.framework.AssertionFailedError;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.*;

/**
 * E2E Polling Tests: ({@link com.seven.asimov.it.tests.e2e.polling.E2EPollingTests#test_001_E2E_Polling() Test 1},
 * {@link com.seven.asimov.it.tests.e2e.polling.E2EPollingTests#test_002_E2E_Polling() Test 2},
 * {@link com.seven.asimov.it.tests.e2e.polling.E2EPollingTests#test_003_E2E_Polling() Test 3},
 * {@link com.seven.asimov.it.tests.e2e.polling.E2EPollingTests#test_004_E2E_Polling() Test 4},
 * {@link com.seven.asimov.it.tests.e2e.polling.E2EPollingTests#test_005_E2E_Polling() Test 5},
 * {@link com.seven.asimov.it.tests.e2e.polling.E2EPollingTests#test_006_E2E_Polling() Test 6},
 * {@link com.seven.asimov.it.tests.e2e.polling.E2EPollingTests#test_007_E2E_Polling() Test 7})
 * {@link com.seven.asimov.it.tests.e2e.polling.E2EPollingTests#test_008_E2E_Polling() Test 8})
 * {@link com.seven.asimov.it.tests.e2e.polling.E2EPollingTests#test_009_E2E_Polling() Test 9})
 *
 * @author ykorchomakha, amykytenko, yrushchak, mpisotskiy, aokolnychyi
 */
public class E2EPollingTests extends E2EPollingTestCase {
    private static final Logger logger = LoggerFactory.getLogger(E2EPollingTests.class.getSimpleName());

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

    @LargeTest
    public void test_000_E2E_Init() {
        try {
            notifyRestForTestsStart(SUITE_NAME);
        } catch (Exception e) {
            logger.debug("Tests start REST notification failed");
            e.printStackTrace();
        }
    }

    /**
     * <h1>Rapid manual polling (RMP) pattern detection and start polling.</h1>
     * <p>The test checks that rapid manual polling (RMP) pattern is detected and polling is started.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>MISS, get response immediately, sleep on 35 seconds</li>
     * <li>MISS, get response immediately, sleep on 35 seconds</li>
     * <li>MISS, get response immediately, the OC Client detects the RMP and sends start polling request to the traffic harmonizer.</li>
     * <li>Check Start Poll Request on TH. {@link E2EPollingTestCase#checkThPollRequest}</li>
     * <li>Check Start Poll Response on TH. {@link E2EPollingTestCase#checkThPollResponse}</li>
     * <li>Check Subscription&Resource in Cassandra. {@link E2EPollingTestCase#checkSubscriptionInCassandra}</li>
     * <li>Check poll starting on UKKO. {@link E2EPollingTestCase#checkUkkoPolling}</li>
     * <li>Check polling via Test Runner. {@link E2EPollingTestCase#checkTestrunnerPolling}</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void test_001_E2E_Polling() throws Throwable {
        final StartPollTask startPollTask = new StartPollTask();
        final StartPollParamsTask startPollParamsTask = new StartPollParamsTask();
        final LogcatUtil logcatUtil = new LogcatUtil(getContext(), startPollTask, startPollParamsTask);
        final int requestIntervalSeconds = 35;
        final int requestIntervalMs = requestIntervalSeconds * 1000;
        final int requestsCount = 4;
        prepareResource(request);
        try {
            logcatUtil.start();
            HttpResponse response = null;
            for (int i = 1; i <= requestsCount; i++) {
                if (i < 4) {
                    response = checkMiss(request, i);
                    if (i < 3) logSleeping(requestIntervalMs - response.getD());
                } else {
                    long restChecksStart = System.currentTimeMillis();
                    logSleeping(LOGCAT_ENTRY_WAIT_TIMEOUT);
                    logcatUtil.stop();
                    assertTrue("Start of polling should be reported in client log", !startPollTask.getLogEntries().isEmpty());
                    StartPollWrapper startPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
                    assertTrue("StartPollParams of polling should be reported in client log", !startPollParamsTask.getLogEntries().isEmpty());
                    StartPollParamsWrapper startPollParams = startPollParamsTask.getLogEntries().get(startPollParamsTask.getLogEntries().size() - 1);
                    String resourceKey = new HttpRequestMd5CalculatorUtil(request, Collections.singletonList((long) startPollParams.getRi())).getRequestMd5();
                    checkPollingStarted(startPoll.getSubscriptionId(), resourceKey);
                    long restChecksEnd = System.currentTimeMillis();
                    if (response != null) {
                        logSleeping(response.getD() - (restChecksEnd - restChecksStart));
                    }
                    checkHit(request, i);
                    logSleeping(requestIntervalMs - response.getD());
                    checkUkkoPolling(resourceKey, 2);
                    checkTestrunnerPolling(5, String.format("2@%d@%d@%d@%d", requestIntervalSeconds, requestIntervalSeconds, (int) (0.75 * startPollParams.getRi()), startPollParams.getRi()), request.getUri());
                }
            }
        } finally {
            logcatUtil.stop();
            invalidateResource();
        }
    }

    /**
     * <h1>Rapid long polling (RLP) pattern detection and start polling.</h1>
     * <p>The test checks that Rapid long polling (RLP) pattern detection and start polling.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>MISS, get response after 21 seconds, sleep on 35 seconds</li>
     * <li>MISS, get response after 21 seconds, sleep on 35 seconds</li>
     * <li>MISS, get response after 21 seconds, the OC Client detects the RLP and sends start polling request to the traffic harmonizer.</li>
     * <li>Check Start Poll Request on TH. {@link E2EPollingTestCase#checkThPollRequest}</li>
     * <li>Check Start Poll Response on TH. {@link E2EPollingTestCase#checkThPollResponse}</li>
     * <li>Check Subscription&Resource in Cassandra. {@link E2EPollingTestCase#checkSubscriptionInCassandra}</li>
     * <li>Check poll starting on UKKO. {@link E2EPollingTestCase#checkUkkoPolling}</li>
     * <li>Check polling via Test Runner. {@link E2EPollingTestCase#checkTestrunnerPolling}</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void test_002_E2E_Polling() throws Throwable {
        final StartPollTask startPollTask = new StartPollTask();
        final StartPollParamsTask startPollParamsTask = new StartPollParamsTask();
        final LogcatUtil logcatUtil = new LogcatUtil(getContext(), startPollTask, startPollParamsTask);
        prepareResource(request);
        final HttpRequest longPollRequest = request.copy();
        final int requestIntervalSeconds = 35;
        final int requestIntervalMs = requestIntervalSeconds * 1000;
        final int requestsCount = 4;
        longPollRequest.addHeaderField(new HttpHeaderField("X-OC-Sleep", "22"));
        try {
            HttpResponse response = null;
            logcatUtil.start();
            for (int i = 1; i <= requestsCount; i++) {
                if (i < 4) {
                    response = checkMiss(longPollRequest, i);
                    if (i < 3) logSleeping(requestIntervalMs - response.getDuration());
                } else {
                    long restChecksStart = System.currentTimeMillis();
                    logSleeping(LOGCAT_ENTRY_WAIT_TIMEOUT);
                    logcatUtil.stop();
                    assertTrue("Start of polling should be reported in client log", !startPollTask.getLogEntries().isEmpty());
                    StartPollWrapper startPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
                    assertTrue("StartPollParams of polling should be reported in client log", !startPollParamsTask.getLogEntries().isEmpty());
                    StartPollParamsWrapper startPollParams = startPollParamsTask.getLogEntries().get(startPollParamsTask.getLogEntries().size() - 1);
                    String resourceKey = new HttpRequestMd5CalculatorUtil(longPollRequest, Collections.singletonList((long) startPollParams.getRi())).getRequestMd5();
                    checkPollingStarted(startPoll.getSubscriptionId(), resourceKey);
                    long restChecksEnd = System.currentTimeMillis();
                    if (response != null) {
                        logSleeping(response.getD() - (restChecksEnd - restChecksStart));
                    }
                    checkHit(longPollRequest, i);
                    logSleeping(requestIntervalMs - response.getD());

                    checkUkkoPolling(resourceKey, 2);
                    checkTestrunnerPolling(6, String.format("2@%d@%d@%d@%d@%d", requestIntervalSeconds, requestIntervalSeconds, startPollParams.getTo(), startPollParams.getTo(), startPollParams.getTo()), request.getUri());
                }
            }
        } finally {
            logcatUtil.stop();
            invalidateResource();
        }
    }

    /**
     * <h1>Long polling (LP) pattern detection and start polling.</h1>
     * <p>The test checks that Long polling (LP) pattern detection and start polling.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>MISS, get response after 68 seconds, sleep on 70 seconds</li>
     * <li>MISS, get response after 68 seconds, the OC Client detects the RLP and sends start polling request to the traffic harmonizer.</li>
     * <li>Check Start Poll Request on TH. {@link E2EPollingTestCase#checkThPollRequest}</li>
     * <li>Check Start Poll Response on TH. {@link E2EPollingTestCase#checkThPollResponse}</li>
     * <li>Check Subscription&Resource in Cassandra. {@link E2EPollingTestCase#checkSubscriptionInCassandra}</li>
     * <li>Check poll starting on UKKO. {@link E2EPollingTestCase#checkUkkoPolling}</li>
     * <li>Check polling via Test Runner. {@link E2EPollingTestCase#checkTestrunnerPolling}</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void test_003_E2E_Polling() throws Throwable {
        final StartPollParamsTask startPollParamsTask = new StartPollParamsTask();
        final StartPollTask startPollTask = new StartPollTask();
        final LogcatUtil logcatUtil = new LogcatUtil(getContext(), startPollTask, startPollParamsTask);
        prepareResource(request);
        final HttpRequest longPollRequest = request.copy();
        final int requestIntervalSeconds = 80;
        final int requestIntervalMs = requestIntervalSeconds * 1000;
        longPollRequest.addHeaderField(new HttpHeaderField("X-OC-Sleep", "66"));

        try {
            HttpResponse response = null;
            logcatUtil.start();
            for (int i = 1; i <= 3; i++) {
                if (i < 3) {
                    response = checkMiss(longPollRequest, i);
                    if (i < 2) logSleeping(requestIntervalMs - response.getDuration());
                } else {
                    long restChecksStart = System.currentTimeMillis();
                    logSleeping(LOGCAT_ENTRY_WAIT_TIMEOUT);
                    logcatUtil.stop();
                    assertTrue("Start of polling should be reported in client log", !startPollTask.getLogEntries().isEmpty());
                    StartPollWrapper startPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
                    assertTrue("StartPollParams of polling should be reported in client log", !startPollParamsTask.getLogEntries().isEmpty());
                    StartPollParamsWrapper startPollParams = startPollParamsTask.getLogEntries().get(startPollParamsTask.getLogEntries().size() - 1);
                    String resourceKey = new HttpRequestMd5CalculatorUtil(longPollRequest, Collections.singletonList(SERVER_LONG_POLLING_INTERVAL)).getRequestMd5();
                    checkPollingStarted(startPoll.getSubscriptionId(), resourceKey);
                    long restChecksEnd = System.currentTimeMillis();
                    if (response != null) {
                        logSleeping(response.getD() - (restChecksEnd - restChecksStart));
                    }
                    checkHit(longPollRequest, i);
                    logSleeping(requestIntervalMs - response.getD());
                    checkUkkoPolling(resourceKey, 2);
                    checkTestrunnerPolling(5, String.format("1@%d@%d@%d@%d", requestIntervalSeconds, startPollParams.getTo(), startPollParams.getTo(), startPollParams.getTo()), request.getUri());
                }
            }
        } finally {
            logcatUtil.stop();
            invalidateResource();
        }
    }

    /**
     * <h1>RI based pattern detection and start polling.</h1>
     * <p>The test checks that RI based pattern detection and start polling.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>MISS, get response immediately, sleep on 65 seconds</li>
     * <li>MISS, get response immediately, sleep on 65 seconds</li>
     * <li>MISS, get response immediately, the OC Client detects the RI and sends start polling request to the traffic harmonizer.</li>
     * <li>Check Start Poll Request on TH. {@link E2EPollingTestCase#checkThPollRequest}</li>
     * <li>Check Start Poll Response on TH. {@link E2EPollingTestCase#checkThPollResponse}</li>
     * <li>Check Subscription&Resource in Cassandra. {@link E2EPollingTestCase#checkSubscriptionInCassandra}</li>
     * <li>Check poll starting on UKKO. {@link E2EPollingTestCase#checkUkkoPolling}</li>
     * <li>Check polling via Test Runner. {@link E2EPollingTestCase#checkTestrunnerPolling}</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void test_004_E2E_Polling() throws Throwable {
        final StartPollParamsTask startPollParamsTask = new StartPollParamsTask();
        final StartPollTask startPollTask = new StartPollTask();
        final LogcatUtil logcatUtil = new LogcatUtil(getContext(), startPollTask, startPollParamsTask);
        final int requestIntervalSeconds = 65;
        final int requestIntervalMs = requestIntervalSeconds * 1000;
        prepareResource(request);
        int requestId = 0;
        try {
            logcatUtil.start();
            HttpResponse response = null;
            checkMiss(request, ++requestId, requestIntervalMs);
            checkMiss(request, ++requestId, requestIntervalMs);
            response = checkMiss(request, ++requestId);
            long restChecksStart = System.currentTimeMillis();
            logSleeping(LOGCAT_ENTRY_WAIT_TIMEOUT);
            logcatUtil.stop();
            assertTrue("Start of polling should be reported in client log", !startPollTask.getLogEntries().isEmpty());
            StartPollWrapper startPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
            assertTrue("StartPollParams of polling should be reported in client log", !startPollParamsTask.getLogEntries().isEmpty());
            StartPollParamsWrapper startPollParams = startPollParamsTask.getLogEntries().get(startPollParamsTask.getLogEntries().size() - 1);
            String resourceKey = new HttpRequestMd5CalculatorUtil(request, Collections.singletonList((long) startPollParams.getRi())).getRequestMd5();
            checkPollingStarted(startPoll.getSubscriptionId(), resourceKey);
            long restChecksEnd = System.currentTimeMillis();
            logSleeping(requestIntervalMs - response.getD() - (restChecksEnd - restChecksStart));
            checkHit(request, ++requestId);
            logSleeping(requestIntervalMs - response.getD());
            checkHit(request, ++requestId);
            checkUkkoPolling(resourceKey, 3);
            checkTestrunnerPolling(5, String.format("1@%d@%d@%d@%d", requestIntervalSeconds, requestIntervalSeconds, (int) (0.75 * startPollParams.getRi()), startPollParams.getRi()), request.getUri());
        } finally {
            logcatUtil.stop();
            invalidateResource();
        }
    }

    @Ignore
    /**
     * <h1>This test is ignored due to http://jira.seven.com/browse/ASMV-18590</h1>
     * <h1>RLP pattern  converting to LP pattern and start polling.</h1>
     * <p>The test checks that RLP pattern  converting to LP pattern and start polling.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>MISS, get response after 21 seconds, sleep on 35 seconds</li>
     * <li>MISS, get response after 21 seconds, sleep on 35 seconds</li>
     * <li>MISS, get response after 21 seconds, the OC Client detects the RLP and sends start polling request to the traffic harmonizer.</li>
     * <li>Check Start Poll Request on TH. {@link E2ETestCase#checkThPollRequest}</li>
     * <li>Check Start Poll Response on TH. {@link E2ETestCase#checkThPollResponse}</li>
     * <li>Check Subscription&Resource in Cassandra. {@link E2ETestCase#checkSubscriptionInCassandra}</li>
     * <li>Check poll starting on UKKO. {@link E2ETestCase#checkUkkoPolling}</li>
     * <li>Check polling via Test Runner. {@link E2ETestCase#checkTestrunnerPolling}</li>
     * <li>MISS, get response after 65 seconds, sleep on 70 seconds</li>
     * <li>MISS, get response after 65 seconds, sleep on 70 seconds</li>
     * <li>MISS, get response after 65 seconds, the OC Client detects the LP and sends start polling request to the traffic harmonizer.</li>
     * <li>Check Start Poll Request on TH. {@link E2ETestCase#checkThPollRequest}</li>
     * <li>Check Start Poll Response on TH. {@link E2ETestCase#checkThPollResponse}</li>
     * <li>Check Subscription&Resource in Cassandra. {@link E2ETestCase#checkSubscriptionInCassandra}</li>
     * <li>Check poll starting on UKKO. {@link E2ETestCase#checkUkkoPolling}</li>
     * <li>Check polling via Test Runner. {@link E2ETestCase#checkTestrunnerPolling}</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void test_005_E2E_Polling() throws Throwable {
        final StartPollParamsTask startPollParamsTask = new StartPollParamsTask();
        final StartPollTask startPollTask = new StartPollTask();
        startPollTask.setPrintEntries(true);
        startPollParamsTask.setPrintEntries(true);
        final int rlpRequestIntervalSecond = 35;
        final int rlpRequestIntervalMs = rlpRequestIntervalSecond * 1000;
        final int lpRequestIntervalSecond = 70;
        final int lpRequestIntervalMs = lpRequestIntervalSecond * 1000;
        prepareResource(request);
        logSleeping(10 * 1000);
        setResourceDelay(25);
        logSleeping(10 * 1000);
        try {

            final HttpRequest statelessRequest = request.copy();
            statelessRequest.addHeaderField(new HttpHeaderField("X-OC-Stateless-Sleep", "true"));

            LogcatUtil logcatUtil = new LogcatUtil(getContext(), startPollTask, startPollParamsTask);
            logcatUtil.start();

            int requestId = 0;
            // 1
            checkMiss(statelessRequest, ++requestId, rlpRequestIntervalMs);
            // 2
            checkMiss(statelessRequest, ++requestId, rlpRequestIntervalMs);
            // 3
            final HttpResponse response = checkMiss(statelessRequest, ++requestId);
            // 4
            logSleeping(rlpRequestIntervalMs - response.getDuration());
            setResourceDelay(65);
            final HttpResponse response1 = checkHit(statelessRequest, ++requestId);


            logcatUtil.stop();
            long startRlpChecking = System.currentTimeMillis();
            assertTrue("Start of polling should be reported in client log", !startPollTask.getLogEntries().isEmpty());
            StartPollWrapper startPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
            assertTrue("StartPollParams of polling should be reported in client log", !startPollParamsTask.getLogEntries().isEmpty());
            StartPollParamsWrapper startPollParams = startPollParamsTask.getLogEntries().get(startPollParamsTask.getLogEntries().size() - 1);

            final String rlpResourceKey = new HttpRequestMd5CalculatorUtil(statelessRequest, Collections.singletonList((long) startPollParams.getRi())).getRequestMd5();

            final String rlpSubscriptionId = startPoll.getSubscriptionId();
            startPollTask.reset();
            startPollParamsTask.reset();
            checkPollingStarted(rlpSubscriptionId, rlpResourceKey);
            long endRlpChecking = System.currentTimeMillis();

            logSleeping(lpRequestIntervalMs - response1.getDuration() - (endRlpChecking - startRlpChecking));
            // 5
            checkHit(statelessRequest, ++requestId, lpRequestIntervalMs);
            logcatUtil = new LogcatUtil(getContext(), startPollTask, startPollParamsTask);
            logcatUtil.start();
            // 6
            checkMiss(statelessRequest, ++requestId, lpRequestIntervalMs);
            // 7
            checkMiss(statelessRequest, ++requestId, lpRequestIntervalMs);
            // 8
            checkHit(statelessRequest, ++requestId);
            logSleeping(10 * 1000);
            logcatUtil.stop();
            assertTrue("Start of second polling should be reported in client log", !startPollTask.getLogEntries().isEmpty());
            startPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
            assertTrue("StartPollParams of second polling should be reported in client log", !startPollParamsTask.getLogEntries().isEmpty());
            startPollParams = startPollParamsTask.getLogEntries().get(startPollParamsTask.getLogEntries().size() - 1);
            final String lpResourceKey = new HttpRequestMd5CalculatorUtil(statelessRequest, Collections.singletonList(SERVER_LONG_POLLING_INTERVAL)).getRequestMd5();
            final String lpSubscriptionId = startPoll.getSubscriptionId();
            checkPollingStarted(lpSubscriptionId, lpResourceKey);
            checkUkkoPolling(rlpResourceKey, 4);
            checkUkkoPolling(lpResourceKey, 1);
        } finally {
            setResourceDelay(0);
            invalidateResource();
        }
    }

    /**
     * <h1>Repolling with new class, period (RI to TRMP to RI with new period).</h1>
     * <p>The test checks that RI pattern converting to TRMP pattern, start polling and RI pattern converting to RI pattern with new period.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>MISS, get response immediately, sleep on 85 seconds</li>
     * <li>MISS, get response immediately, sleep on 85 seconds</li>
     * <li>MISS, get response immediately, the OC Client detects the RI and sends start polling request to the traffic harmonizer.</li>
     * <li>Check Start Poll Request on TH. {@link E2EPollingTestCase#checkThPollRequest}</li>
     * <li>Check Start Poll Response on TH. {@link E2EPollingTestCase#checkThPollResponse}</li>
     * <li>Check Subscription&Resource in Cassandra. {@link E2EPollingTestCase#checkSubscriptionInCassandra}</li>
     * <li>Check poll starting on UKKO. {@link E2EPollingTestCase#checkUkkoPolling}</li>
     * <li>Check polling via Test Runner. {@link E2EPollingTestCase#checkTestrunnerPolling}</li>
     * <li>HIT, get response immediately from cache, sleep on 32 seconds</li>
     * <li>MISS, get response immediately, sleep on 10 seconds</li>
     * <li>MISS, get response immediately, sleep on 43 seconds</li>
     * <li>MISS, get response immediately, sleep on 30 seconds</li>
     * <li>MISS, get response immediately, the OC Client detects the Rapid Manual Poll and sends start polling request to the traffic harmonizer.</li>
     * <li>Check Start Poll Request on TH. {@link E2EPollingTestCase#checkThPollRequest}</li>
     * <li>Check Start Poll Response on TH. {@link E2EPollingTestCase#checkThPollResponse}</li>
     * <li>Check Subscription&Resource in Cassandra. {@link E2EPollingTestCase#checkSubscriptionInCassandra}</li>
     * <li>Check poll starting on UKKO. {@link E2EPollingTestCase#checkUkkoPolling}</li>
     * <li>Check polling via Test Runner. {@link E2EPollingTestCase#checkTestrunnerPolling}</li>
     * <li>HIT, get response immediately from cache, sleep on 50 seconds</li>
     * <li>HIT, get response immediately from cache, sleep on 50 seconds</li>
     * <li>HIT, get response immediately from cache, sleep on 27 seconds</li>
     * <li>HIT, get response immediately from cache, sleep on 65 seconds</li>
     * <li>HIT, get response immediately from cache, sleep on 65 seconds</li>
     * <li>HIT, get response immediately from cache, the OC Client detects the RI with interval 65 and sends start polling request to the traffic harmonizer.</li>
     * <li>Check Start Poll Request on TH. {@link E2EPollingTestCase#checkThPollRequest}</li>
     * <li>Check Start Poll Response on TH. {@link E2EPollingTestCase#checkThPollResponse}</li>
     * <li>Check Subscription&Resource in Cassandra. {@link E2EPollingTestCase#checkSubscriptionInCassandra}</li>
     * <li>Check poll starting on UKKO. {@link E2EPollingTestCase#checkUkkoPolling}</li>
     * <li>Check polling via Test Runner. {@link E2EPollingTestCase#checkTestrunnerPolling}</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void test_006_E2E_Polling() throws Throwable {
        clearProperties(); // Revert min rapid polling intervals values
        final StartPollParamsTask startPollParamsTask = new StartPollParamsTask();
        final StartPollTask startPollTask = new StartPollTask();
        final int riRequestIntervalSecond = 85;
        final int riRequestIntervalMs = riRequestIntervalSecond * 1000;
        prepareResource(request);
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), startPollTask, startPollParamsTask);
        logcatUtil.start();
        try {
            int requestId = 0;
            HttpResponse response;
            // 1-3
            for (int i = 1; i <= 3; i++) {
                response = checkMiss(request, ++requestId);
                if (i < 3) {
                    logSleeping(riRequestIntervalMs - response.getDuration());
                } else {
                    long restChecksStart = System.currentTimeMillis();
                    logSleeping(LOGCAT_ENTRY_WAIT_TIMEOUT);
                    logcatUtil.stop();
                    assertTrue("Start of polling should be reported in client log", !startPollTask.getLogEntries().isEmpty());
                    StartPollWrapper startPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
                    assertTrue("StartPollParams of polling should be reported in client log", !startPollParamsTask.getLogEntries().isEmpty());
                    for (StartPollParamsWrapper wrapper : startPollParamsTask.getLogEntries()) {
                        logger.info(wrapper.toString());
                    }
                    StartPollParamsWrapper startPollParams = startPollParamsTask.getLogEntries().get(startPollParamsTask.getLogEntries().size() - 1);
                    String resourceKey = new HttpRequestMd5CalculatorUtil(request, Collections.singletonList((long) startPollParams.getRi())).getRequestMd5();
                    checkPollingStarted(startPoll.getSubscriptionId(), resourceKey);
                    long restChecksEnd = System.currentTimeMillis();
                    startPollParamsTask.reset();
                    startPollTask.reset();
                    logcatUtil = new LogcatUtil(getContext(), startPollTask, startPollParamsTask);
                    logcatUtil.start();
                    logSleeping(riRequestIntervalMs - (restChecksEnd - restChecksStart));
                }
            }
            // 4
            response = checkHit(request, ++requestId);
            logSleeping(32 * 1000 - response.getDuration());
            // 5
            response = checkMiss(request, ++requestId);
            logSleeping(10 * 1000 - response.getDuration());
            // 6
            response = checkMiss(request, ++requestId);
            logSleeping(43 * 1000 - response.getDuration());
            // 7
            checkHit(request, ++requestId);
            long restChecksStart = System.currentTimeMillis();
            logcatUtil.stop();
            assertTrue("Start of second polling should be reported in client log", !startPollTask.getLogEntries().isEmpty());
            StartPollWrapper startPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
            assertTrue("StartPollParams of second polling should be reported in client log", !startPollParamsTask.getLogEntries().isEmpty());
            for (StartPollParamsWrapper wrapper : startPollParamsTask.getLogEntries()) {
                logger.info(wrapper.toString());
            }
            StartPollParamsWrapper startPollParams = startPollParamsTask.getLogEntries().get(startPollParamsTask.getLogEntries().size() - 1);
            String resourceKey = new HttpRequestMd5CalculatorUtil(request, Collections.singletonList((long) startPollParams.getRi())).getRequestMd5();
            checkThPollRequest(startPoll.getSubscriptionId(), resourceKey);
            checkThPollResponse(startPoll.getSubscriptionId(), resourceKey);
            startPollTask.reset();
            startPollParamsTask.reset();
            logcatUtil = new LogcatUtil(getContext(), startPollTask, startPollParamsTask);
            logcatUtil.start();
            //      checkTestrunnerPolling(-1, startPollParams.getRi(), request.getUri());
            long restChecksEnd = System.currentTimeMillis();
            logSleeping(31 * 1000 - (restChecksEnd - restChecksStart));
            // 8
            response = checkHit(request, ++requestId);
            logSleeping(50 * 1000 - response.getDuration());
            // 9
            response = checkHit(request, ++requestId);
            logSleeping(50 * 1000 - response.getDuration());
            // 10
            response = checkHit(request, ++requestId);
            logSleeping(27 * 1000 - response.getDuration());
            // 11
            response = checkHit(request, ++requestId);
            logSleeping(65 * 1000 - response.getDuration());
            // 12
            response = checkMiss(request, ++requestId);
            logSleeping(65 * 1000 - response.getDuration());
            //13
            checkHit(request, ++requestId);
            logSleeping(LOGCAT_ENTRY_WAIT_TIMEOUT);
            logcatUtil.stop();
            assertTrue("Start of third polling should be reported in client log", !startPollTask.getLogEntries().isEmpty());
            startPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
            for (StartPollParamsWrapper wrapper : startPollParamsTask.getLogEntries()) {
                logger.info(wrapper.toString());
            }
            assertTrue("StartPollParams of third polling should be reported in client log", !startPollParamsTask.getLogEntries().isEmpty());
            startPollParams = startPollParamsTask.getLogEntries().get(startPollParamsTask.getLogEntries().size() - 1);
            resourceKey = new HttpRequestMd5CalculatorUtil(request, Collections.singletonList((long) startPollParams.getRi())).getRequestMd5();
            checkPollingStarted(startPoll.getSubscriptionId(), resourceKey);
            checkUkkoPolling(resourceKey, 1);
            checkTestrunnerPolling(16, "1@85@85@60@60@11@1@30@30@30@30@30@30@84@1@48", request.getUri());
        } finally {
            invalidateResource();
            logcatUtil.stop();
            logSleeping(30 * 1000);
        }
    }

    //TODO check intervals

    /**
     * <h1>Switching LP to normal poll on server side.</h1>
     * <p>The test checks that RI pattern converting to TRMP pattern, start polling and RI pattern converting to RI pattern with new period.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>MISS, get response after 65 seconds, sleep on 70 seconds</li>
     * <li>MISS, get response after 65 seconds, the OC Client detects the LP and sends start polling request to the traffic harmonizer.</li>
     * <li>Check Start Poll Request on TH. {@link E2EPollingTestCase#checkThPollRequest}</li>
     * <li>Check Start Poll Response on TH. {@link E2EPollingTestCase#checkThPollResponse}</li>
     * <li>Check Subscription&Resource in Cassandra. {@link E2EPollingTestCase#checkSubscriptionInCassandra}</li>
     * <li>Check poll starting on UKKO. {@link E2EPollingTestCase#checkUkkoPolling}</li>
     * <li>Check polling via Test Runner. {@link E2EPollingTestCase#checkTestrunnerPolling}</li>
     * <li>HIT, get response after 65 seconds from cache, sleep on 70 seconds</li>
     * <li>HIT, get response after 65 seconds from cache, sleep on 70 seconds</li>
     * <li>HIT, get response after 65 seconds from cache, the OC Client detects that .</li>
     * <li>Check Start Poll Request on TH. {@link E2EPollingTestCase#checkThPollRequest}</li>
     * <li>Check Start Poll Response on TH. {@link E2EPollingTestCase#checkThPollResponse}</li>
     * <li>Check Subscription&Resource in Cassandra. {@link E2EPollingTestCase#checkSubscriptionInCassandra}</li>
     * <li>Check poll starting on UKKO. {@link E2EPollingTestCase#checkUkkoPolling}</li>
     * <li>Check polling via Test Runner. {@link E2EPollingTestCase#checkTestrunnerPolling}</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void test_007_E2E_Polling() throws Throwable {
        final StartPollParamsTask startPollParamsTask = new StartPollParamsTask();
        final StartPollTask startPollTask = new StartPollTask();
        final long requestIntervalMs = 70 * 1000;
        prepareResource(request);
        logger.info("delay setted to 65");
        setResourceDelay(65);
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), startPollTask, startPollParamsTask);
        logcatUtil.start();
        try {
            HttpResponse response = null;
            String resourceKey;
            int requestId = 0;

            response = checkMiss(request, ++requestId);
            logSleeping(requestIntervalMs - response.getDuration());
            response = checkMiss(request, ++requestId);
            long restChecksStart = System.currentTimeMillis();
            logSleeping(LOGCAT_ENTRY_WAIT_TIMEOUT);
            logcatUtil.stop();
            assertTrue("Start of polling should be reported in client log", !startPollTask.getLogEntries().isEmpty());
            StartPollWrapper startPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
            resourceKey = new HttpRequestMd5CalculatorUtil(request, Collections.singletonList(SERVER_LONG_POLLING_INTERVAL)).getRequestMd5();
            checkPollingStarted(startPoll.getSubscriptionId(), resourceKey);
            logcatUtil = new LogcatUtil(getContext(), startPollTask, startPollParamsTask);
            logcatUtil.start();
            long restChecksEnd = System.currentTimeMillis();

            logSleeping(requestIntervalMs - response.getDuration() - (restChecksEnd - restChecksStart));

            response = checkHit(request, ++requestId);
            long start = System.currentTimeMillis();
            logger.info("delay setted to 0");
            setResourceDelay(0);
            logSleeping(requestIntervalMs - response.getDuration() - (System.currentTimeMillis() - start));
            checkHit(request, ++requestId);
            logSleeping(requestIntervalMs - response.getDuration());
            checkHit(request, ++requestId);
            start = System.currentTimeMillis();
            logger.info("delay setted to 55");
            setResourceDelay(55);
            checkUkkoPolling(resourceKey, 4);
            logSleeping(requestIntervalMs - response.getDuration() - (System.currentTimeMillis() - start));
            checkHit(request, ++requestId);
            logSleeping(requestIntervalMs - response.getDuration());
            checkHit(request, ++requestId);
            checkTestrunnerPolling(11, "1@7@68@68@68@30@36@68@38@30@68", request.getUri());
        } finally {
            setResourceDelay(0);
            invalidateResource();
            logcatUtil.stop();
            logSleeping(30 * 1000);
        }
    }

    @Ignore
    /**
     * <h1>This test is ignored since OC does not temporarily maintain revalidation polling.</h1>
     * <h1>OC should stop active Revalidation polling when switching  to the transparent mode.</h1>
     * <p>The test checks that OC should stop active Revalidation polling when switching  to the transparent mode.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>MISS, sleep on 35 seconds.</li>
     * <li>HIT, because responce came with header "304 Not Modified".</li>
     * <li>HIT, because responce came with header "304 Not Modified", , the OC Client detects the poll class 16 and sends start polling request to the traffic harmonizer.</li>
     * <li>Check Start Poll Request on TH. {@link E2ETestCase#checkThPollRequest}</li>
     * <li>Check Start Poll Response on TH. {@link E2ETestCase#checkThPollResponse}</li>
     * <li>Check Subscription&Resource in Cassandra. {@link E2ETestCase#checkSubscriptionInCassandra}</li>
     * <li>Check poll starting on UKKO. {@link E2ETestCase#checkUkkoPolling}</li>
     * <li>Adding policy "transparent" with value "1", OC should stop all active pollings</li>
     * <li>Check that all subscriptions are deleted</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void test_008_E2E_Polling() throws Throwable {
        final int POLLING_CLASS = 16;
        final String RESOURCE_URI = "E2E_Polling_008";
        final String URI = createTestResourceUri(RESOURCE_URI);

        String requestHeader = "Cache-Control: max-age=40" + TFConstantsIF.CRLF + "Etag: 123";
        String encodedHeader = URLEncoder.encode(Base64.encodeToString(requestHeader.getBytes(), Base64.DEFAULT));

        HttpRequest prepareRequest = createRequest().setMethod(HttpGet.METHOD_NAME).
                setUri(URI).addHeaderField("X-OC-AddRawHeadersPermanently", encodedHeader).getRequest();
        final HttpRequest request = createRequest().setUri(URI).setMethod(HttpGet.METHOD_NAME).getRequest();
        final HttpRequest request304 = createRequest().setMethod(HttpGet.METHOD_NAME).setUri(URI).addHeaderField("X-OC-ChangeResponseStatus", "304").getRequest();

        sendRequest2(prepareRequest, false, true);

        final StartPollTask startPollTask = new StartPollTask();
        final StartPollParamsTask startPollParamsTask = new StartPollParamsTask();
        final DelSubscriptionTask delSubscriptionTask = new DelSubscriptionTask();
        final LogcatUtil logcatUtil = new LogcatUtil(getContext(), startPollTask, startPollParamsTask, delSubscriptionTask);
        final int requestIntervalSeconds = 35;
        final int maxAge = 40;
        final int requestIntervalMs = requestIntervalSeconds * 1000;
        prepareResource(prepareRequest);

        TestCaseThread mainThread = new TestCaseThread() {
            @Override
            public void run() throws Throwable {
                logcatUtil.start();
                int requestId = 0;
                HttpResponse response = null;
                checkMiss(request, ++requestId, requestIntervalMs);
                response = checkHit(request, ++requestId);
                long duration = changeResource(request304);
                logger.info(String.format("Going to wait: %d", requestIntervalMs - duration - response.getDuration()));
                logSleeping(requestIntervalMs - duration - response.getDuration());
                checkMiss(request, ++requestId, requestIntervalMs);
                checkHit(request, ++requestId, requestIntervalMs);
                long restChecksStart = System.currentTimeMillis();
                logSleeping(LOGCAT_ENTRY_WAIT_TIMEOUT);
                assertTrue("Start of polling should be reported in client log", !startPollTask.getLogEntries().isEmpty());
                StartPollWrapper startPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
                assertTrue("StartPollParams of polling should be reported in client log", !startPollParamsTask.getLogEntries().isEmpty());
                StartPollParamsWrapper startPollParams = startPollParamsTask.getLogEntries().get(startPollParamsTask.getLogEntries().size() - 1);
                assertTrue("Polling should be " + POLLING_CLASS + " and RR must be revalidated", startPollParams.getPollClass() == POLLING_CLASS);
                String resourceKey = new HttpRequestMd5CalculatorUtil(request, Collections.singletonList((long) maxAge)).
                        getRequestMd5();
                // TODO resourse key calculation for poll class 16
                //checkPollingStarted(startPoll.getSubscriptionId(), resourceKey);
                //checkUkkoPolling(resourceKey, 1);
                long restChecksEnd = System.currentTimeMillis();
                if (response != null) {
                    logSleeping(response.getD() - (restChecksEnd - restChecksStart));
                }
                addProperty(PMS_PATH, TRANSPARENT, "1");
                logSleeping(LOGCAT_ENTRY_WAIT_TIMEOUT * 4);
                logcatUtil.stop();
                assertTrue("No subscriptions were deleted after transparent policy was applied", !delSubscriptionTask.getLogEntries().isEmpty());
                assertTrue("Subscription " + startPoll.getSubscriptionId() + " must be deleted after transparent policy was applied", delSubscriptionTask.getLogEntries().get(delSubscriptionTask.getLogEntries().size() - 1).getSubscriptionId().equals(startPoll.getSubscriptionId()));

            }
        };

        TestCaseThread radioUpKeeperThread = new TestCaseThread() {
            @Override
            public void run() throws Throwable {
                int count = 0;
                while (!isInterruptedSoftly()) {
                    Runtime.getRuntime().exec("ping -c 1 " + PMSUtil.getPmsServerIp());
                    count++;
                    if (count > 60) this.interruptSoftly();
                    TestUtil.sleep(5 * 1000);
                }
            }
        };

        try {
            executeThreads(1800000, mainThread, radioUpKeeperThread);
        } finally {
            logcatUtil.stop();
            clearProperties();
            PrepareResourceUtil.invalidateResourceSafely(URI);
            radioUpKeeperThread.interruptSoftly();
        }
    }

    @Ignore
    /**
     * <h1>This test is ignored since OC does not temporarily maintain revalidation polling.</h1>
     * <h1>OC should stop active Revalidation polling when switching  to the transparent mode.</h1>
     * <p>The test checks that OC should stop active Revalidation polling when switching  to the transparent mode.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Three different pollings are started concurrently - RI with interval 35, RI with interval 70 and LP with interval 70</li>
     * <li>For all pollings client and server side checks are used</li>
     * <li>Check Start Poll Request on TH. {@link E2ETestCase#checkThPollRequest}</li>
     * <li>Check Start Poll Response on TH. {@link E2ETestCase#checkThPollResponse}</li>
     * <li>Check Subscription&Resource in Cassandra. {@link E2ETestCase#checkSubscriptionInCassandra}</li>
     * <li>Check poll starting on UKKO. {@link E2ETestCase#checkUkkoPolling}</li>
     * <li>Adding policy "transparent" with value "1", OC should stop all active pollings</li>
     * <li>Check that all subscriptions are deleted</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void test_009_E2E_Polling() throws Throwable {
        final StartPollTask startPollTask = new StartPollTask();
        final StartPollParamsTask startPollParamsTask = new StartPollParamsTask();
        final DelSubscriptionTask delSubscriptionTask = new DelSubscriptionTask();
        final LogcatUtil logcatUtil = new LogcatUtil(getContext(), startPollTask, delSubscriptionTask, startPollParamsTask);
        final HttpRequest requestRi35 = createRequest().addHeaderField("X-OC-Encoding", "identity").getRequest();
        final HttpRequest requestRi70 = createRequest().addHeaderField("X-OC-Encoding", "identity").getRequest();
        final HttpRequest requestLp = createRequest().addHeaderField("X-OC-Encoding", "identity").getRequest();
        logcatUtil.start();
        PMSUtil.cleanPaths(new String[]{PMS_PATH, "@asimov@http"});
        logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
        final Policy ofo = new Policy("out_of_order_aggressiveness", "0", HTTP_PMS_PATH, true);
        final Policy transparent = new Policy("transparent", "0", "@asimov", true);
        PMSUtil.addPolicies(new Policy[]{ofo, transparent});
        logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);

        TestCaseThread ri35Thread = new TestCaseThread() {
            @Override
            public void run() throws Throwable {
                int requestId = 0;
                final int requestIntervalSeconds = 35;
                final int requestIntervalMs = requestIntervalSeconds * 1000;
                requestRi35.setUri(createUri());
                PrepareResourceUtil.prepareResource(requestRi35.getUri(), false);
                checkMiss(requestRi35, ++requestId, requestIntervalMs);
                checkMiss(requestRi35, ++requestId, requestIntervalMs);
                checkMiss(requestRi35, ++requestId, requestIntervalMs);
                logSleeping(LOGCAT_ENTRY_WAIT_TIMEOUT);
                if (!startPollParamsTask.getLogEntries().isEmpty() & !startPollTask.getLogEntries().isEmpty()) {
                    StartPollParamsWrapper startPollParams = startPollParamsTask.getLogEntries().get(startPollParamsTask.getLogEntries().size() - 1);
                    StartPollWrapper startPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
                    String resourceKey = new HttpRequestMd5CalculatorUtil(requestRi35, Collections.singletonList((long) startPollParams.getRi())).getRequestMd5();
                    logger.info("RI 35 StartPollTask " + startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1) + " StartPollParams " + startPollParamsTask.getLogEntries().get(startPollParamsTask.getLogEntries().size() - 1));
                    checkPollingStarted(startPoll.getSubscriptionId(), resourceKey);
                }
            }
        };

        TestCaseThread ri70Thread = new TestCaseThread(1 * 60 * 1000) {
            @Override
            public void run() throws Throwable {
                int requestId = 0;
                final int requestIntervalSeconds = 70;
                final int requestIntervalMs = requestIntervalSeconds * 1000;
                requestRi70.setUri(createUri());
                PrepareResourceUtil.prepareResource(requestRi70.getUri(), false);
                checkMiss(requestRi70, ++requestId, requestIntervalMs);
                checkMiss(requestRi70, ++requestId, requestIntervalMs);
                checkMiss(requestRi70, ++requestId, requestIntervalMs);
                logSleeping(LOGCAT_ENTRY_WAIT_TIMEOUT);
                if (!startPollParamsTask.getLogEntries().isEmpty() & !startPollTask.getLogEntries().isEmpty()) {
                    StartPollParamsWrapper startPollParams = startPollParamsTask.getLogEntries().get(startPollParamsTask.getLogEntries().size() - 1);
                    StartPollWrapper startPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
                    String resourceKey = new HttpRequestMd5CalculatorUtil(requestRi70, Collections.singletonList((long) startPollParams.getRi())).getRequestMd5();
                    logger.info("RI 70 StartPollTask " + startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1) + " StartPollParams " + startPollParamsTask.getLogEntries().get(startPollParamsTask.getLogEntries().size() - 1));
                    checkPollingStarted(startPoll.getSubscriptionId(), resourceKey);
                }
            }
        };

        TestCaseThread lpThread = new TestCaseThread(4 * 60 * 1000) {
            @Override
            public void run() throws Throwable {
                int requestId = 0;
                final int requestIntervalSeconds = 70;
                final int requestIntervalMs = requestIntervalSeconds * 1000;
                requestLp.setUri(createUri());
                PrepareResourceUtil.prepareResource(requestLp.getUri(), false);
                HttpRequest modificationRequest = requestLp.copy();
                modificationRequest.addHeaderField(new HttpHeaderField("X-OC-Stateless-Sleep", "true"));
                modificationRequest.addHeaderField(new HttpHeaderField("X-OC-ChangeSleep", Integer.toString(65)));
                sendRequest2(modificationRequest, false, true);
                checkMiss(requestLp, ++requestId, requestIntervalMs);
                checkMiss(requestLp, ++requestId, requestIntervalMs);
                logSleeping(LOGCAT_ENTRY_WAIT_TIMEOUT);
                if (!startPollParamsTask.getLogEntries().isEmpty() & !startPollTask.getLogEntries().isEmpty()) {
                    StartPollParamsWrapper startPollParams = startPollParamsTask.getLogEntries().get(startPollParamsTask.getLogEntries().size() - 1);
                    StartPollWrapper startPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
                    String resourceKey = new HttpRequestMd5CalculatorUtil(requestLp, Collections.singletonList(SERVER_LONG_POLLING_INTERVAL)).getRequestMd5();
                    logger.info("LP StartPollTask " + startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1) + " StartPollParams " + startPollParamsTask.getLogEntries().get(startPollParamsTask.getLogEntries().size() - 1));
                    checkPollingStarted(startPoll.getSubscriptionId(), resourceKey);
                }
            }
        };

        try {
            executeThreads(ri35Thread, ri70Thread, lpThread);
            addProperty(PMS_PATH, TRANSPARENT, "1");
            logSleeping(2 * 60 * 1000);
            logcatUtil.stop();
            assertTrue("Three different pollings should be started", startPollTask.getLogEntries().size() >= 3);
            assertTrue("Three different subscription should be deleted", delSubscriptionTask.getLogEntries().size() >= 3);

            logger.info("Subscription numbers:");
            Set<String> subscriptions = new HashSet<String>();
            for (StartPollWrapper startPollWrapper : startPollTask.getLogEntries()) {
                subscriptions.add(startPollWrapper.getSubscriptionId());
                logger.info(startPollWrapper.getSubscriptionId());
            }
            for (DelSubscriptionWrapper delSubscriptionWrapper : delSubscriptionTask.getLogEntries()) {
                assertFalse("There must be three different pairs of subscriptions", subscriptions.add(delSubscriptionWrapper.getSubscriptionId()));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(requestRi35.getUri());
            PrepareResourceUtil.invalidateResourceSafely(requestRi70.getUri());
            PrepareResourceUtil.invalidateResourceSafely(requestLp.getUri());
            logcatUtil.stop();
            clearProperties();
        }
    }

    @LargeTest
    public void test_999_E2E_CleanUp() {
        clearProperties();
        try {
            notifyRestForTestEnd(SUITE_NAME);
        } catch (Exception e) {
            logger.debug("Tests end REST notification failed");
            e.printStackTrace();
        }
    }

    private long changeResource(HttpRequest request) {
        HttpResponse response;
        response = sendRequest2(request, false, true);
        return response.getDuration();
    }

    private void setResourceDelay(final int delaySeconds) throws InterruptedException {
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

    private void invalidateResource() {
        PrepareResourceUtil.invalidateResourceSafely(request.getUri());
    }
}