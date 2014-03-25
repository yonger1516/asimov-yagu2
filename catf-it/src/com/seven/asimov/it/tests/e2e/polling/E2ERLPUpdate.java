package com.seven.asimov.it.tests.e2e.polling;

import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;
import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.testcases.E2EPollingTestCase;
import com.seven.asimov.it.utils.HttpRequestMd5CalculatorUtil;
import com.seven.asimov.it.utils.MobileNetworkUtil;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.date.DateUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.ThTask;
import com.seven.asimov.it.utils.logcat.tasks.pollingTasks.StartPollParamsTask;
import com.seven.asimov.it.utils.logcat.tasks.pollingTasks.StartPollTask;
import com.seven.asimov.it.utils.logcat.wrappers.StartPollParamsWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.StartPollWrapper;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import junit.framework.AssertionFailedError;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.concurrent.*;

//TODO ADD UKKO AND TESTRUNNER CHECKS
public class E2ERLPUpdate extends E2EPollingTestCase {
    private static final String TAG = E2ERLPUpdate.class.getSimpleName();
    private MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());

    public void test_000_E2E_Init() {
        try {
            notifyRestForTestsStart(TAG);
            final Policy invalidateWithCache = new Policy("cache_invalidate_aggressiveness", "0", "@asimov@http", true);
            final Policy invalidateWithoutCache = new Policy("no_cache_invalidate_aggressiveness", "0", "@asimov@http", true);
            final Policy transparent = new Policy("transparent", "0", "@asimov", true);
            PMSUtil.addPolicies(new Policy[]{invalidateWithCache, invalidateWithoutCache, transparent});
            logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
            mobileNetworkUtil.onWifiOnly();
            logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
        } catch (Exception e) {
            Log.e(TAG, "Tests start REST notification failed");
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * <h1>Verifying start_poll parameters on RLP detecting and re-detecting</h1>
     * <p>A test resource is needed for this test case that returns the same response for all requests.</p>
     * <p>Pattern: [0,30,30,30,30,30,30,40,50,50], Delays: [20,20,20,20,20,20,20,20,35,35].</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Thread that is responsible for poll verdicts should start.</li>
     * <li>MISS, get response with expected delay, sleep rest of the request interval
     * (The RI interval for this request has to be for about 30 seconds, the delay is 20 seconds). </li>
     * <li>MISS, get response with expected delay, sleep rest of the request interval
     * (The RI interval for this request has to be for about 30 seconds, the delay is 20 seconds).</li>
     * <li>Thread that is responsible for client log checks should start.</li>
     * <li>MISS, get response with expected delay, sleep rest of the request interval
     * (The RI interval for this request has to be for about 30 seconds, the delay is 20 seconds).</li>
     * <li>Checking start poll parameters during the interval on client side.
     * We anticipate the following start poll parameters: package com.seven.asimov.it, poll class: 4, RI: 0, IT: 0, TO: 20, temp poll: 0, RP RI: 30</li>
     * <li>Thread that is responsible for the server checks should start.</li>
     * <li>Checking server points for the start poll.
     * - Check Start Poll Request on TH.
     * - Check Start Poll Response on TH.
     * - Check Subscription&Resource in Cassandra.</li>
     * <li>HIT, get response with expected delay, sleep rest of the request interval
     * (The RI interval for this request has to be for about 30 seconds, the delay is 20 seconds).</li>
     * <li>HIT, get response with expected delay, sleep rest of the request interval
     * (The RI interval for this request has to be for about 30 seconds, the delay is 20 seconds).</li>
     * <li>HIT, get response with expected delay, sleep rest of the request interval
     * (The RI interval for this request has to be for about 30 seconds, the delay is 20 seconds).</li>
     * <li>HIT, get response with expected delay, sleep rest of the request interval
     * (The RI interval for this request has to be for about 30 seconds, the delay is 30 seconds).</li>
     * <li>HIT, get response with expected delay, sleep rest of the request interval
     * (The RI interval for this request has to be for about 40 seconds, the delay is 40 seconds).</li>
     * <li>An appropriate delay for the resource should be set to 35.</li>
     * <li>Thread that is responsible for the changing resource should start.</li>
     * <li>MISS, get response with expected delay, sleep rest of the request interval
     * (The RI interval for this request has to be for about 45 seconds, the delay is 35 seconds).
     * RLP should be re-detected and polling should start after receiving response with such start_poll parameters:
     * package com.seven.asimov.it, poll class: 4, RI: 0, IT: 0, TO: 40, temp poll: 0, RP RI: 35</li>
     * <li>Checking the second start poll.
     * - Check Start Poll Request on TH.
     * - Check Start Poll Response on TH.
     * - Check Subscription&Resource in Cassandra.</li>
     * <li>HIT, get response with expected delay, sleep rest of the request interval.</li>
     * </ol>
     *
     * @throws Exception
     */
    @LargeTest
    public void test_001_E2E_RLP_Update() throws Exception {

        error = null;
        final String RESOURCE_URI = "asimov_e2e_rlp_001";
        final String uri = createTestResourceUri(RESOURCE_URI);
        final HttpRequest request = createRequest().setUri(uri).setMethod("GET").addHeaderField("X-OC-Stateless-Sleep", "true").getRequest();

        final StartPollTask startPollTask = new StartPollTask();
        final StartPollParamsTask startPollParamsTask = new StartPollParamsTask();
        final Exchanger<ExchangeParameters> firstStartPollExchanger = new Exchanger<ExchangeParameters>();
        final Exchanger<ExchangeParameters> secondStartPollExchanger = new Exchanger<ExchangeParameters>();
        final long interval = 30 * DateUtil.SECONDS;
        final long delay = 20 * DateUtil.SECONDS;
        final ExecutorService service = Executors.newFixedThreadPool(3);
        final CountDownLatch secondStartPoll = new CountDownLatch(1);
        final CountDownLatch firstStartPoll = new CountDownLatch(1);
        PrepareResourceUtil.prepareResource(uri, false);

        Runnable pollVerdicts = new Runnable() {
            int requestId = 1;

            @Override
            public void run() {
                try {
                    Log.i(TAG, "Thread that is responsible for poll verdicts has started.");
                    setResourceDelay(delay / DateUtil.SECONDS, request);
                    checkMiss(request, requestId++, (int) (interval)); //1
                    checkMiss(request, requestId++, (int) (interval)); //2
                    firstStartPoll.countDown();
                    checkMiss(request, requestId++, (int) (interval)); //3
                    checkHit(request, requestId++, (int) (interval)); //4
                    checkHit(request, requestId++, (int) (interval)); //5
                    checkHit(request, requestId++, (int) (interval)); //6
                    checkHit(request, requestId++, (int) (interval)); //7
                    checkHit(request, requestId++, (int) (40 * DateUtil.SECONDS)); //8
                    setResourceDelay(35, request);
                    secondStartPoll.countDown();
                    checkMiss(request, requestId++, (int) (50 * DateUtil.SECONDS));//9
                    checkHit(request, requestId++, (int) (50 * DateUtil.SECONDS)); //10
                } catch (AssertionFailedError e) {
                    Log.e(TAG, "Thread that is responsible for poll verdicts has failed.");
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                    if (service != null) service.shutdownNow();
                    error = e;
                } catch (Exception e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } finally {
                    PrepareResourceUtil.invalidateResourceSafely(uri);
                    logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
                }
            }
        };

        Runnable clientLogChecks = new Runnable() {
            LogcatUtil logcatUtil = null;

            @Override
            public void run() {
                logcatUtil = new LogcatUtil(getContext(), startPollTask, startPollParamsTask);
                try {
                    firstStartPoll.await(operationTimeOut, TimeUnit.SECONDS);
                    Log.i(TAG, "Thread that is responsible for client log checks has started. Checking the first start poll...");
                    logcatUtil.start();
                    logSleeping(interval);
                    logcatUtil.stop();
                    advancedCheckOfStartPoll(startPollTask, startPollParamsTask, delay, interval, 4, 0, 0, 0);
                    Log.i(TAG, "The start poll was checked successfully on client side.");
                    StartPollWrapper firstStartPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
                    StartPollParamsWrapper firstStartPollParameters = startPollParamsTask.getLogEntries().get(startPollParamsTask.getLogEntries().size() - 1);
                    firstStartPollExchanger.exchange(new ExchangeParameters(firstStartPoll, firstStartPollParameters));

                    secondStartPoll.await(operationTimeOut, TimeUnit.SECONDS);
                    Log.i(TAG, "Checking the second start poll...");
                    startPollTask.reset();
                    startPollParamsTask.reset();
                    logcatUtil = new LogcatUtil(getContext(), startPollTask, startPollParamsTask);
                    logcatUtil.start();
                    logSleeping(50 * DateUtil.SECONDS);
                    logcatUtil.stop();
                    advancedCheckOfStartPoll(startPollTask, startPollParamsTask, 35 * DateUtil.SECONDS, 45 * DateUtil.SECONDS, 4, 0, 0, 0);
                    Log.i(TAG, "The start poll was checked successfully on client side.");
                    StartPollWrapper secondStartPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
                    StartPollParamsWrapper secondStartPollParameters = startPollParamsTask.getLogEntries().get(startPollParamsTask.getLogEntries().size() - 1);
                    secondStartPollExchanger.exchange(new ExchangeParameters(secondStartPoll, secondStartPollParameters));
                } catch (AssertionFailedError e) {
                    Log.e(TAG, "Thread that is responsible for client start polls has failed");
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                    if (service != null) service.shutdownNow();
                    error = e;
                } catch (Exception e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } finally {
                    stopLogcat(logcatUtil);
                }
            }
        };

        Runnable serverChecks = new Runnable() {

            public void run() {
                try {
                    ExchangeParameters firstParameters = firstStartPollExchanger.exchange(null, 200, TimeUnit.SECONDS);
                    StartPollWrapper firstPollWrapper = firstParameters.getStartPollWrapper();
                    StartPollParamsWrapper firstParamWrapper = firstParameters.getStartPollParamsWrapper();

                    Log.i(TAG, String.format("Thread that is responsible for the server checks has started. StartPollWrapper: %s.", firstPollWrapper.toString()));
                    String firstResourceKey = new HttpRequestMd5CalculatorUtil(request, Collections.singletonList((long) firstParamWrapper.getRi())).getRequestMd5();
                    Log.i(TAG, String.format("Resource key calculated by TF: %s", firstResourceKey));
                    checkPollingStarted(firstPollWrapper.getSubscriptionId(), firstResourceKey);

                    ExchangeParameters secondParameters = secondStartPollExchanger.exchange(null, 400, TimeUnit.SECONDS);
                    StartPollWrapper secondPollWrapper = secondParameters.getStartPollWrapper();
                    StartPollParamsWrapper secondParamWrapper = secondParameters.getStartPollParamsWrapper();

                    Log.i(TAG, String.format("Thread that is responsible for the server checks has started. StartPollWrapper: %s.", secondPollWrapper.toString()));
                    String secondResourceKey = new HttpRequestMd5CalculatorUtil(request, Collections.singletonList((long) secondParamWrapper.getRi())).getRequestMd5();
                    Log.i(TAG, String.format("Resource key calculated by TF: %s", secondResourceKey));
                    checkPollingStarted(secondPollWrapper.getSubscriptionId(), secondResourceKey);
                } catch (AssertionFailedError e) {
                    Log.e(TAG, "Thread that is responsible for server checks has failed");
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                    error = e;
                } catch (InterruptedException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } catch (URISyntaxException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } catch (IOException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } catch (TimeoutException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                }
            }
        };

        service.submit(pollVerdicts);
        service.submit(clientLogChecks);
        service.submit(serverChecks);

        service.awaitTermination(testTimeOut, TimeUnit.SECONDS);
        if (error != null) throw error;
    }

    /**
     * <h1>Verifying start_poll parameters on upgrading RMP to RLP.</h1>
     * <p>A test resource is needed for this test case that returns the same response for first 4 requests and another one for the rest.</p>
     * <p>Pattern: [0,55,55,55,55,55,55,55], Delays: [35,35,35,35,35,35,35,35].</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Thread that is responsible for poll verdicts should start.</li>
     * <li>MISS, get response with expected delay, sleep rest of the request interval
     * (The RI interval for this request has to be for about 55 seconds, the delay is 35 seconds).</li>
     * <li>MISS, get response with expected delay, sleep rest of the request interval
     * (The RI interval for this request has to be for about 55 seconds, the delay is 35 seconds).</li>
     * <li>Thread that is responsible for client log checks should start.</li>
     * <li>MISS, get response with expected delay, sleep rest of the request interval
     * (The RI interval for this request has to be for about 55 seconds, the delay is 35 seconds).</li>
     * <li>Checking start poll parameters during the interval on client side
     * We anticipate the following start poll parameters: package com.seven.asimov.it, poll class: 4, RI: 0, IT: 0, TO: 35, temp poll: 0, RP RI: 55</li>
     * <li>Thread that is responsible for the server checks should start.</li>
     * <li>Checking server points for the start poll:
     * - Check Start Poll Request on TH.
     * - Check Start Poll Response on TH.
     * - Check Subscription&Resource in Cassandra.</li>
     * <li>HIT, get response with expected delay, sleep rest of the request interval
     * (The RI interval for this request has to be for about 55 seconds, the delay is 35 seconds).</li>
     * <li>Thread that is responsible for the changing resource should start. Changing body to eret.</li>
     * <li>Checking correctness of the IWC.</li>
     * <li>HIT, get response with expected delay, BODY MUST BE NEW, sleep rest of the request interval
     * (The RI interval for this request has to be for about 55 seconds, the delay depends on the circumstances).</li>
     * <li>MISS, get response with expected delay, sleep rest of the request interval
     * (The RI interval for this request has to be for about 55 seconds, the delay is 35 seconds).</li>
     * <li>Checking the second start poll on the client side.</li>
     * <li>Rapid Manual Poll should be detected. After receiving response RMP should be upgraded to RLP and polling should start with such start_poll parameters:
     * package com.seven.asimov.it, poll class: 4, RI: 0, IT: 0, TO: 35, temp poll: 0, RP RI: 55.</li>
     * <li>Checking server points for the start poll.
     * - Check Start Poll Request on TH.
     * - Check Start Poll Response on TH.
     * - Check Subscription&Resource in Cassandra.</li>
     * </ol>
     *
     * @throws Exception
     */
    @LargeTest
    public void test_002_E2E_RLP_Update() throws Exception {

        error = null;
        final String RESOURCE_URI = "asimov_e2e_rlp_002";
        final String uri = createTestResourceUri(RESOURCE_URI);
        final HttpRequest request = createRequest().setUri(uri).setMethod("GET").addHeaderField("X-OC-Stateless-Sleep", "true").getRequest();

        final StartPollTask startPollTask = new StartPollTask();
        final StartPollParamsTask startPollParamsTask = new StartPollParamsTask();
        final ThTask thNotificationReceivedTask = new ThTask();
        final Exchanger<ExchangeParameters> firstStartPollExchanger = new Exchanger<ExchangeParameters>();
        final Exchanger<ExchangeParameters> secondStartPollExchanger = new Exchanger<ExchangeParameters>();
        final long interval = 55 * DateUtil.SECONDS;
        final long delay = 35 * DateUtil.SECONDS;
        final ExecutorService service = Executors.newFixedThreadPool(4);
        final CountDownLatch changeResourceLatch = new CountDownLatch(1);
        final CountDownLatch checkFirstPoll = new CountDownLatch(1);
        final CountDownLatch checkSecondPoll = new CountDownLatch(1);
        PrepareResourceUtil.prepareResource(uri, false);

        Runnable pollVerdicts = new Runnable() {
            int requestId = 1;

            @Override
            public void run() {
                try {
                    Log.i(TAG, "Thread that is responsible for poll verdicts has started.");
                    setResourceDelay(delay / DateUtil.SECONDS, request);
                    checkMiss(request, requestId++, (int) (interval)); //1
                    checkMiss(request, requestId++, (int) (interval)); //2
                    checkFirstPoll.countDown();
                    checkMiss(request, requestId++, (int) (interval)); //3
                    checkHit(request, requestId++, (int) (interval));  //4
                    changeResourceLatch.countDown();
                    checkHit(request, requestId++, (int) (interval));  //5
                    checkSecondPoll.countDown();
                    checkMiss(request, requestId++, (int) (interval)); //7
                    checkHit(request, requestId++, (int) (interval));  //8
                } catch (AssertionFailedError e) {
                    Log.e(TAG, "Thread that is responsible for poll verdicts has failed.");
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                    if (service != null) service.shutdownNow();
                    error = e;
                } catch (Exception e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } finally {
                    PrepareResourceUtil.invalidateResourceSafely(uri);
                    logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
                }
            }
        };

        Runnable clientLogChecks = new Runnable() {
            LogcatUtil logcatUtil = null;

            @Override
            public void run() {
                logcatUtil = new LogcatUtil(getContext(), startPollTask, startPollParamsTask);
                try {
                    checkFirstPoll.await(operationTimeOut, TimeUnit.SECONDS);
                    Log.i(TAG, "Thread that is responsible for client log checks has started. Checking first start poll...");
                    logcatUtil.start();
                    logSleeping(interval);
                    logcatUtil.stop();
                    advancedCheckOfStartPoll(startPollTask, startPollParamsTask, delay, interval, 4, 0, 0, 0);
                    Log.i(TAG, "The first poll was checked successfully.");
                    StartPollWrapper firstStartPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
                    StartPollParamsWrapper firstStartPollParameters = startPollParamsTask.getLogEntries().get(startPollParamsTask.getLogEntries().size() - 1);

                    firstStartPollExchanger.exchange(new ExchangeParameters(firstStartPoll, firstStartPollParameters));

                    changeResourceLatch.await(operationTimeOut, TimeUnit.SECONDS);
                    Log.i(TAG, "Checking client logcat in order to find IWC...");
                    logcatUtil = new LogcatUtil(getContext(), thNotificationReceivedTask);
                    logcatUtil.start();
                    logSleeping(interval);
                    logcatUtil.stop();
                    assertTrue("Client should receive IWC notification. Please, verify that your client has msisdn validation",
                            checkThEntriesListForReceivedInvalidateNotification(thNotificationReceivedTask, startPollTask) != null);
                    Log.i(TAG, "IWC was checked successfully.");

                    checkSecondPoll.await(operationTimeOut, TimeUnit.SECONDS);
                    startPollTask.reset();
                    startPollParamsTask.reset();
                    logcatUtil = new LogcatUtil(getContext(), startPollTask, startPollParamsTask);
                    Log.i(TAG, "Checking second start poll...");
                    logcatUtil.start();
                    logSleeping(2 * interval);
                    logcatUtil.stop();

                    advancedCheckOfStartPoll(startPollTask, startPollParamsTask, delay, interval, 4, 0, 0, 0);
                    Log.i(TAG, "The second poll was checked successfully.");

                    StartPollWrapper secondStartPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
                    StartPollParamsWrapper secondStartPollParameters = startPollParamsTask.getLogEntries().get(startPollParamsTask.getLogEntries().size() - 1);

                    secondStartPollExchanger.exchange(new ExchangeParameters(secondStartPoll, secondStartPollParameters));
                } catch (AssertionFailedError e) {
                    Log.e(TAG, "Thread that is responsible for client start polls has failed");
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                    if (service != null) service.shutdownNow();
                    error = e;
                } catch (Exception e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } finally {
                    stopLogcat(logcatUtil);
                }
            }
        };

        Runnable changeResource = new Runnable() {

            public void run() {
                try {
                    changeResourceLatch.await(operationTimeOut, TimeUnit.SECONDS);
                    logSleeping(2 * DateUtil.SECONDS);
                    Log.i(TAG, "Changing the resource");
                    HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                            .addHeaderField("X-OC-ChangeResponseContent", INVALIDATED_RESPONSE)
                            .addHeaderField("X-OC-Stateless-Sleep", "true")
                            .addHeaderField("X-OC-ChangeSleep", String.valueOf(delay / DateUtil.SECONDS)).getRequest();
                    sendRequest2(request, false, true);
                } catch (AssertionFailedError e) {
                    Log.e(TAG, "Thread that is responsible for changing resource has failed");
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                    error = e;
                } catch (InterruptedException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                }
            }
        };

        Runnable serverChecks = new Runnable() {

            public void run() {
                try {
                    ExchangeParameters firstParam = firstStartPollExchanger.exchange(null, 200, TimeUnit.SECONDS);
                    StartPollWrapper firstPollWrapper = firstParam.getStartPollWrapper();
                    StartPollParamsWrapper firstPollParamsWrapper = firstParam.getStartPollParamsWrapper();

                    Log.i(TAG, String.format("Thread that is responsible for the server checks has started. StartPollWrapper: %s.", firstPollWrapper.toString()));
                    String firstResourceKey = new HttpRequestMd5CalculatorUtil(request, Collections.singletonList((long) firstPollParamsWrapper.getRi())).getRequestMd5();
                    Log.i(TAG, String.format("Resource key calculated by TF: %s", firstResourceKey));
                    checkPollingStarted(firstPollWrapper.getSubscriptionId(), firstResourceKey);

                    ExchangeParameters secondParam = secondStartPollExchanger.exchange(null, 300, TimeUnit.SECONDS);
                    StartPollWrapper secondPollWrapper = secondParam.getStartPollWrapper();
                    StartPollParamsWrapper secondPollParamsWrapper = secondParam.getStartPollParamsWrapper();

                    Log.i(TAG, String.format("Thread that is responsible for the server checks has started. StartPollWrapper: %s.", secondPollWrapper.toString()));
                    String secondResourceKey = new HttpRequestMd5CalculatorUtil(request, Collections.singletonList((long) secondPollParamsWrapper.getRi())).getRequestMd5();
                    Log.i(TAG, String.format("Resource key calculated by TF: %s", secondResourceKey));
                    checkPollingStarted(secondPollWrapper.getSubscriptionId(), secondResourceKey);
                } catch (AssertionFailedError e) {
                    Log.e(TAG, "Thread that is responsible for server checks has failed");
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                    error = e;
                } catch (InterruptedException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } catch (URISyntaxException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } catch (IOException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } catch (TimeoutException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                }
            }
        };

        service.submit(pollVerdicts);
        service.submit(changeResource);
        service.submit(clientLogChecks);
        service.submit(serverChecks);

        service.awaitTermination(testTimeOut, TimeUnit.SECONDS);
        if (error != null) throw error;
    }

    /**
     * <h1>Verifying correctness of IWC notification receiving for RLP.</h1>
     * <p>A test resource is needed for this test case that returns the same response for first 4 requests and another one for the rest.</p>
     * <p>Pattern: [0,55,55,55,55], Delays: [30,30,30,30,30].</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Thread that is responsible for poll verdicts should start.</li>
     * <li>MISS, get response with expected delay, sleep rest of the request interval
     * (The RI interval for this request has to be for about 55 seconds, the delay is 35 seconds).</li>
     * <li>MISS, get response with expected delay, sleep rest of the request interval
     * (The RI interval for this request has to be for about 55 seconds, the delay is 35 seconds).</li>
     * <li>Thread that is responsible for client log checks should start.</li>
     * <li>MISS, get response with expected delay, sleep rest of the request interval
     * (The RI interval for this request has to be for about 55 seconds, the delay is 35 seconds).</li>
     * <li>Checking start poll parameters during the interval on client side
     * We anticipate the following start poll parameters: package com.seven.asimov.it, poll class: 4, RI: 0, IT: 0, TO: 35, temp poll: 0, RP RI: 55</li>
     * <li>Thread that is responsible for the server checks should start.</li>
     * <li>Checking server points for the start poll:
     * - Check Start Poll Request on TH.
     * - Check Start Poll Response on TH.
     * - Check Subscription&Resource in Cassandra.</li>
     * <li>HIT, get response with expected delay, sleep rest of the request interval.
     * (The RI interval for this request has to be for about 55 seconds, the delay is 35 seconds).</li>
     * <li>Thread that is responsible for the changing resource should start.</li>
     * <li>Checking correctness of the IWC.</li>
     * <li>HIT, get response with expected delay, BODY MUST BE NEW, sleep rest of the request interval
     * (The RI interval for this request has to be for about 55 seconds, the delay depends on the circumstances).</li>
     * </ol>
     *
     * @throws Exception
     */
    @LargeTest
    public void test_003_E2E_RLP_Update() throws Exception {

        error = null;
        final String RESOURCE_URI = "asimov_e2e_rlp_003";
        final String uri = createTestResourceUri(RESOURCE_URI);
        final HttpRequest request = createRequest().setUri(uri).setMethod("GET").addHeaderField("X-OC-Stateless-Sleep", "true").getRequest();

        final StartPollTask startPollTask = new StartPollTask();
        final StartPollParamsTask startPollParamsTask = new StartPollParamsTask();
        final ThTask thNotificationReceivedTask = new ThTask();
        final Exchanger<ExchangeParameters> exchanger = new Exchanger<ExchangeParameters>();
        final long interval = 55 * DateUtil.SECONDS;
        final long delay = 35 * DateUtil.SECONDS;
        final ExecutorService service = Executors.newFixedThreadPool(4);
        final CountDownLatch changeResourceLatch = new CountDownLatch(1);
        final CountDownLatch checkPoll = new CountDownLatch(1);
        PrepareResourceUtil.prepareResource(uri, false);

        Runnable pollVerdicts = new Runnable() {
            int requestId = 1;

            @Override
            public void run() {
                try {
                    Log.i(TAG, "Thread that is responsible for poll verdicts has started.");
                    setResourceDelay(delay / DateUtil.SECONDS, request);
                    checkMiss(request, requestId++, (int) (interval)); //1
                    checkMiss(request, requestId++, (int) (interval)); //2
                    checkPoll.countDown();
                    checkMiss(request, requestId++, (int) (interval)); //3
                    checkHit(request, requestId++, (int) (interval));  //4
                    changeResourceLatch.countDown();
                    checkHit(request, requestId++, (int) (interval));  //5 new response
                } catch (AssertionFailedError e) {
                    Log.e(TAG, "Thread that is responsible for poll verdicts has failed.");
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                    if (service != null) service.shutdownNow();
                    error = e;
                } catch (Exception e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } finally {
                    PrepareResourceUtil.invalidateResourceSafely(uri);
                    logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
                }
            }
        };

        Runnable clientLogChecks = new Runnable() {
            LogcatUtil logcatUtil = null;

            @Override
            public void run() {
                logcatUtil = new LogcatUtil(getContext(), startPollTask, startPollParamsTask);
                try {
                    checkPoll.await(operationTimeOut, TimeUnit.SECONDS);
                    Log.i(TAG, "Thread that is responsible for client log checks has started. Checking start poll...");
                    logcatUtil.start();
                    logSleeping(interval);
                    logcatUtil.stop();
                    advancedCheckOfStartPoll(startPollTask, startPollParamsTask, delay, interval, 4, 0, 0, 0);
                    Log.i(TAG, "The start poll was checked successfully on client side.");
                    StartPollWrapper firstStartPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
                    StartPollParamsWrapper firstStartPollParameters = startPollParamsTask.getLogEntries().get(startPollParamsTask.getLogEntries().size() - 1);
                    exchanger.exchange(new ExchangeParameters(firstStartPoll, firstStartPollParameters));

                    changeResourceLatch.await(operationTimeOut, TimeUnit.SECONDS);
                    Log.i(TAG, "Checking client logcat in order to find IWC...");
                    logcatUtil = new LogcatUtil(getContext(), thNotificationReceivedTask);
                    logcatUtil.start();
                    logSleeping(interval);
                    logcatUtil.stop();
                    assertTrue("Client should receive IWC notification. Please, verify that your client has msisdn validation",
                            checkThEntriesListForReceivedInvalidateNotification(thNotificationReceivedTask, startPollTask) != null);
                    Log.i(TAG, "IWC was checked successfully.");
                } catch (AssertionFailedError e) {
                    Log.e(TAG, "Thread that is responsible for client start polls has failed");
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                    if (service != null) service.shutdownNow();
                    error = e;
                } catch (Exception e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } finally {
                    stopLogcat(logcatUtil);
                }
            }
        };

        Runnable changeResource = new Runnable() {

            public void run() {
                try {
                    changeResourceLatch.await(operationTimeOut, TimeUnit.SECONDS);
                    logSleeping(2 * DateUtil.SECONDS);
                    Log.i(TAG, "Changing the resource");
                    HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                            .addHeaderField("X-OC-ChangeResponseContent", INVALIDATED_RESPONSE)
                            .addHeaderField("X-OC-Stateless-Sleep", "true")
                            .addHeaderField("X-OC-ChangeSleep", String.valueOf(delay / DateUtil.SECONDS)).getRequest();
                    sendRequest2(request, false, true);
                } catch (AssertionFailedError e) {
                    Log.e(TAG, "Thread that is responsible for changing resource has failed");
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                    error = e;
                } catch (InterruptedException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                }
            }
        };

        Runnable serverChecks = new Runnable() {

            public void run() {
                try {
                    ExchangeParameters exchangeParameters = exchanger.exchange(null, 200, TimeUnit.SECONDS);
                    StartPollWrapper startPollWrapper = exchangeParameters.getStartPollWrapper();
                    StartPollParamsWrapper startPollParamsWrapper = exchangeParameters.getStartPollParamsWrapper();

                    Log.i(TAG, String.format("Thread that is responsible for the server checks has started. StartPollWrapper: %s.", startPollWrapper.toString()));
                    String resourceKey = new HttpRequestMd5CalculatorUtil(request, Collections.singletonList((long) startPollParamsWrapper.getRi())).getRequestMd5();
                    Log.i(TAG, String.format("Resource key calculated by TF: %s", resourceKey));
                    checkPollingStarted(startPollWrapper.getSubscriptionId(), resourceKey);
                } catch (AssertionFailedError e) {
                    Log.e(TAG, "Thread that is responsible for server checks has failed");
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                    error = e;
                } catch (InterruptedException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } catch (URISyntaxException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } catch (IOException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } catch (TimeoutException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                }
            }
        };

        service.submit(pollVerdicts);
        service.submit(changeResource);
        service.submit(clientLogChecks);
        service.submit(serverChecks);

        service.awaitTermination(testTimeOut, TimeUnit.SECONDS);
        if (error != null) throw error;
    }

    @Ignore
    //TODO Should be updated after an apropriate client/server fix
    /**
     * <h1>This test is ignored due to http://jira.seven.com/browse/ASMV-18590.</h1>
     * <h1>Verifying correctness of IWOC notification receiving for RLP.</h1>
     * <p>A test resource is needed for this test case that returns the same response for all  requests.</p>
     * <p>Pattern: [0,55,55,55,55,55], Delays: [30,30,30,30,65,65].</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Thread that is responsible for poll verdicts should start.</li>
     * <li>MISS, get response with expected delay, sleep rest of the request interval.</li>
     * <li>MISS, get response with expected delay, sleep rest of the request interval.</li>
     * <li>Thread that is responsible for client log checks should start.</li>
     * <li>MISS, get response with expected delay, sleep rest of the request interval.</li>
     * <li>Checking start poll parameters during the interval on client side.</li>
     * <li>Thread that is responsible for the server checks should start.</li>
     * <li>Checking server points for the start poll.</li>
     * <li>HIT, get response with expected delay, sleep rest of the request interval.</li>
     * <li>Thread that is responsible for the changing resource should start.</li>
     * <li>Checking correctness of the IWOC.</li>
     * <li>HIT, get response with expected delay, sleep rest of the request interval.</li>
     * <li>MISS, get response with expected delay, sleep rest of the request interval.</li>
     * <li></li>
     * </ol>
     *
     * @throws Exception
     */
    @LargeTest
    public void test_004_E2E_RLP_Update() throws Exception {

        error = null;
        final String RESOURCE_URI = "asimov_e2e_rlp_004";
        final String uri = createTestResourceUri(RESOURCE_URI);
        final HttpRequest request = createRequest().setUri(uri).setMethod("GET").addHeaderField("X-OC-Stateless-Sleep", "true").getRequest();

        final StartPollTask startPollTask = new StartPollTask();
        final StartPollParamsTask startPollParamsTask = new StartPollParamsTask();
        final ThTask thNotificationReceivedTask = new ThTask();
        final Exchanger<ExchangeParameters> exchanger = new Exchanger<ExchangeParameters>();
        final long interval = 55 * DateUtil.SECONDS;
        final long delay = 30 * DateUtil.SECONDS;
        final ExecutorService service = Executors.newFixedThreadPool(3);
        final CountDownLatch changeResourceLatch = new CountDownLatch(1);
        final CountDownLatch checkFirstPoll = new CountDownLatch(1);
        PrepareResourceUtil.prepareResource(uri, false);

        Runnable pollVerdicts = new Runnable() {
            int requestId = 1;

            @Override
            public void run() {
                try {
                    Log.i(TAG, "Thread that is responsible for poll verdicts has started.");
                    setResourceDelay(delay / DateUtil.SECONDS, request);
                    checkMiss(request, requestId++, (int) (interval)); //1
                    checkMiss(request, requestId++, (int) (interval)); //2
                    checkFirstPoll.countDown();
                    checkMiss(request, requestId++, (int) (interval)); //3
                    checkHit(request, requestId++, (int) (interval));  //4
                    setResourceDelay(65, request);
                    changeResourceLatch.countDown();
                    checkHit(request, requestId++, (int) (interval));  //5
                    checkMiss(request, requestId++, (int) (interval)); //6
                } catch (AssertionFailedError e) {
                    Log.e(TAG, "Thread that is responsible for poll verdicts has failed.");
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                    if (service != null) service.shutdownNow();
                    error = e;
                } catch (Exception e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } finally {
                    PrepareResourceUtil.invalidateResourceSafely(uri);
                    logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
                }
            }
        };

        Runnable clientLogChecks = new Runnable() {
            LogcatUtil logcatUtil = null;

            @Override
            public void run() {
                logcatUtil = new LogcatUtil(getContext(), startPollTask, startPollParamsTask);
                try {
                    checkFirstPoll.await(operationTimeOut, TimeUnit.SECONDS);
                    Log.i(TAG, "Thread that is responsible for client log checks has started. Checking start poll...");
                    logcatUtil.start();
                    logSleeping(interval);
                    logcatUtil.stop();
                    advancedCheckOfStartPoll(startPollTask, startPollParamsTask, delay, interval, 4, 0, 0, 0);
                    Log.i(TAG, "The start poll was checked successfully on client side.");
                    StartPollWrapper firstStartPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
                    StartPollParamsWrapper firstStartPollParameters = startPollParamsTask.getLogEntries().get(startPollParamsTask.getLogEntries().size() - 1);
                    exchanger.exchange(new ExchangeParameters(firstStartPoll, firstStartPollParameters));

                    changeResourceLatch.await(operationTimeOut, TimeUnit.SECONDS);
                    Log.i(TAG, "Checking client logcat in order to find IWOC...");
                    logcatUtil = new LogcatUtil(getContext(), thNotificationReceivedTask);
                    logcatUtil.start();
                    logSleeping(70 * DateUtil.SECONDS);
                    logcatUtil.stop();
                    assertTrue("Client should receive IWOC notification. Please, verify that your client has msisdn validation",
                            checkThEntriesListForReceivedInvalidateNotification(thNotificationReceivedTask, startPollTask) != null);
                    Log.i(TAG, "IWOC was checked successfully.");
                } catch (AssertionFailedError e) {
                    Log.e(TAG, "Thread that is responsible for client start polls has failed");
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                    if (service != null) service.shutdownNow();
                    error = e;
                } catch (Exception e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } finally {
                    stopLogcat(logcatUtil);
                }
            }
        };

        Runnable serverChecks = new Runnable() {

            public void run() {
                try {
                    ExchangeParameters exchangeParameters = exchanger.exchange(null, 200, TimeUnit.SECONDS);
                    StartPollWrapper startPollWrapper = exchangeParameters.getStartPollWrapper();
                    StartPollParamsWrapper startPollParamsWrapper = exchangeParameters.getStartPollParamsWrapper();

                    Log.i(TAG, String.format("Thread that is responsible for the server checks has started. StartPollWrapper: %s.", startPollWrapper.toString()));
                    String resourceKey = new HttpRequestMd5CalculatorUtil(request, Collections.singletonList((long) startPollParamsWrapper.getRi())).getRequestMd5();
                    Log.i(TAG, String.format("Resource key calculated by TF: %s", resourceKey));
                    checkPollingStarted(startPollWrapper.getSubscriptionId(), resourceKey);
                } catch (AssertionFailedError e) {
                    Log.e(TAG, "Thread that is responsible for server checks has failed");
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                    error = e;
                } catch (InterruptedException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } catch (URISyntaxException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } catch (IOException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } catch (TimeoutException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                }
            }
        };

        service.submit(pollVerdicts);
        service.submit(clientLogChecks);
        service.submit(serverChecks);

        service.awaitTermination(testTimeOut, TimeUnit.SECONDS);
        if (error != null) throw error;
    }

    /**
     * <h1>Verifying start_poll parameters and correctness of IWC notification receiving for RMP.</h1>
     * <p>A test resource is needed for this test case that returns the same response for first 4 requests and another one for the rest.</p>
     * <p>Pattern: [0,55,55,55,55,55,55]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Thread that is responsible for poll verdicts should start.</li>
     * <li>MISS, get response without delay, sleep rest of the request interval
     * (The RI interval for this request has to be for about 55 seconds, the delay is 0 seconds).</li>
     * <li>MISS, get response without delay, sleep rest of the request interval
     * (The RI interval for this request has to be for about 55 seconds, the delay is 0 seconds).</li>
     * <li>Thread that is responsible for client log checks should start.</li>
     * <li>MISS, get response without delay, sleep rest of the request interval
     * (The RI interval for this request has to be for about 55 seconds, the delay is 0 seconds).</li>
     * <li>Checking start poll parameters during the interval on client side
     * We anticipate the following start poll parameters: package com.seven.asimov.it, poll class: 1, RI: 55, IT: 0, TO: 0, temp poll: 0, RP RI: 55</li>
     * <li>Thread that is responsible for the server checks should start.</li>
     * <li>Checking server points for the start poll:
     * - Check Start Poll Request on TH.
     * - Check Start Poll Response on TH.
     * - Check Subscription&Resource in Cassandra.</li>
     * <li>HIT, get response without delay, sleep rest of the request interval
     * (The RI interval for this request has to be for about 55 seconds, the delay is 0 seconds).</li>
     * <li>Thread that is responsible for the changing resource should start. Changing the body of the corresponding resource.</li>
     * <li>HIT, get response without delay, sleep rest of the request interval
     * (The RI interval for this request has to be for about 55 seconds, the delay is 0 seconds).</li>
     * <li>Checking correctness of the IWC.</li>
     * <li>HIT, get response with expected delay, BODY MUST BE NEW, sleep rest of the request interval
     * (The RI interval for this request has to be for about 55 seconds, the delay is 0 seconds).</li>
     * <li>MISS, get response with expected delay, sleep rest of the request interval
     * (The RI interval for this request has to be for about 55 seconds, the delay is 0 seconds).</li>
     * </ol>
     *
     * @throws Exception
     */
    @LargeTest
    public void test_005_E2E_RLP_Update() throws Exception {

        error = null;
        final String RESOURCE_URI = "asimov_e2e_rlp_005";
        final String uri = createTestResourceUri(RESOURCE_URI);
        final HttpRequest request = createRequest().setUri(uri).setMethod("GET").addHeaderField("X-OC-Stateless-Sleep", "true").getRequest();

        final StartPollTask startPollTask = new StartPollTask();
        final StartPollParamsTask startPollParamsTask = new StartPollParamsTask();
        final ThTask thNotificationReceivedTask = new ThTask();
        final Exchanger<ExchangeParameters> exchanger = new Exchanger<ExchangeParameters>();
        final long interval = 55 * DateUtil.SECONDS;
        final ExecutorService service = Executors.newFixedThreadPool(4);
        final CountDownLatch changeResourceLatch = new CountDownLatch(1);
        final CountDownLatch checkFirstPoll = new CountDownLatch(1);
        PrepareResourceUtil.prepareResource(uri, false);

        Runnable pollVerdicts = new Runnable() {
            int requestId = 1;

            @Override
            public void run() {
                try {
                    Log.i(TAG, "Thread that is responsible for poll verdicts has started.");
                    checkMiss(request, requestId++, (int) (interval)); //1
                    checkFirstPoll.countDown();
                    checkMiss(request, requestId++, (int) (interval)); //2
                    checkMiss(request, requestId++, (int) (interval)); //3
                    checkHit(request, requestId++, (int) (interval));  //4
                    changeResourceLatch.countDown();
                    checkHit(request, requestId++, (int) (interval));  //5
                    checkHit(request, requestId++, (int) (interval));  //6
                    checkMiss(request, requestId++, (int) (interval)); //7
                } catch (AssertionFailedError e) {
                    Log.e(TAG, "Thread that is responsible for poll verdicts has failed.");
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                    if (service != null) service.shutdownNow();
                    error = e;
                } catch (Exception e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } finally {
                    PrepareResourceUtil.invalidateResourceSafely(uri);
                    logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
                }
            }
        };

        Runnable clientLogChecks = new Runnable() {
            LogcatUtil logcatUtil = null;

            @Override
            public void run() {
                logcatUtil = new LogcatUtil(getContext(), startPollTask, startPollParamsTask);
                try {
                    checkFirstPoll.await(operationTimeOut, TimeUnit.SECONDS);
                    Log.i(TAG, "Thread that is responsible for client log checks has started. Checking start poll...");
                    logcatUtil.start();
                    logSleeping(2 * interval);
                    logcatUtil.stop();
                    advancedCheckOfStartPoll(startPollTask, startPollParamsTask, 0, interval, 1, 55, 0, 0);
                    Log.i(TAG, "The start poll was checked successfully on client side.");
                    StartPollWrapper firstStartPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
                    StartPollParamsWrapper firstStartPollParameters = startPollParamsTask.getLogEntries().get(startPollParamsTask.getLogEntries().size() - 1);
                    exchanger.exchange(new ExchangeParameters(firstStartPoll, firstStartPollParameters));

                    changeResourceLatch.await(operationTimeOut, TimeUnit.SECONDS);
                    Log.i(TAG, "Checking client logcat in order to find IWC...");
                    logcatUtil = new LogcatUtil(getContext(), thNotificationReceivedTask);
                    logcatUtil.start();
                    logSleeping(interval);
                    logcatUtil.stop();
                    assertTrue("Client should receive IWC notification. Please, verify that your client has msisdn validation",
                            checkThEntriesListForReceivedInvalidateNotification(thNotificationReceivedTask, startPollTask) != null);
                    Log.i(TAG, "IWC was checked successfully.");
                } catch (AssertionFailedError e) {
                    Log.e(TAG, "Thread that is responsible for client start polls has failed");
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                    if (service != null) service.shutdownNow();
                    error = e;
                } catch (Exception e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } finally {
                    stopLogcat(logcatUtil);
                }
            }
        };

        Runnable changeResource = new Runnable() {

            public void run() {
                try {
                    changeResourceLatch.await(operationTimeOut, TimeUnit.SECONDS);
                    logSleeping(2 * DateUtil.SECONDS);
                    Log.i(TAG, "Changing the resource...");
                    HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                            .addHeaderField("X-OC-ChangeResponseContent", INVALIDATED_RESPONSE)
                            .addHeaderField("X-OC-Stateless-Sleep", "true")
                            .addHeaderField("X-OC-ChangeSleep", String.valueOf(0)).getRequest();
                    sendRequest2(request, false, true);
                } catch (AssertionFailedError e) {
                    Log.e(TAG, "Thread that is responsible for changing resource has failed");
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                    error = e;
                } catch (InterruptedException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                }
            }
        };

        Runnable serverChecks = new Runnable() {

            public void run() {
                try {
                    ExchangeParameters exchangeParameters = exchanger.exchange(null, 200, TimeUnit.SECONDS);
                    StartPollWrapper startPollWrapper = exchangeParameters.getStartPollWrapper();
                    StartPollParamsWrapper startPollParamsWrapper = exchangeParameters.getStartPollParamsWrapper();

                    Log.i(TAG, String.format("Thread that is responsible for the server checks has started. StartPollWrapper: %s.", startPollWrapper.toString()));
                    String resourceKey = new HttpRequestMd5CalculatorUtil(request, Collections.singletonList((long) startPollParamsWrapper.getRi())).getRequestMd5();
                    Log.i(TAG, String.format("Resource key calculated by TF: %s", resourceKey));
                    checkPollingStarted(startPollWrapper.getSubscriptionId(), resourceKey);
                } catch (AssertionFailedError e) {
                    Log.e(TAG, "Thread that is responsible for server checks has failed");
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                    error = e;
                } catch (InterruptedException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } catch (URISyntaxException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } catch (IOException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } catch (TimeoutException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                }
            }
        };

        service.submit(pollVerdicts);
        service.submit(clientLogChecks);
        service.submit(serverChecks);
        service.submit(changeResource);

        service.awaitTermination(testTimeOut, TimeUnit.SECONDS);
        if (error != null) throw error;
    }

    /**
     * <h1>Verifying correctness of IWOC notification receiving for RMP.</h1>
     * <p>A test resource is needed for this test case that returns the same response for all requests.</p>
     * <p>Pattern: [0,55,55,55,55,55], Delays: [0,0,0,0,65,65].</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Thread that is responsible for poll verdicts should start.</li>
     * <li>MISS, get response with expected delay, sleep rest of the request interval
     * (The RI interval for this request has to be for about 55 seconds, the delay is 0 seconds).</li>
     * <li>MISS, get response with expected delay, sleep rest of the request interval
     * (The RI interval for this request has to be for about 55 seconds, the delay is 0 seconds).</li>
     * <li>Thread that is responsible for client log checks should start.</li>
     * <li>MISS, get response with expected delay, sleep rest of the request interval
     * (The RI interval for this request has to be for about 55 seconds, the delay is 0 seconds).</li>
     * <li>Checking start poll parameters during the interval on client side
     * We anticipate the following start poll parameters: package com.seven.asimov.it, poll class: 1, RI: 55, IT: 0, TO: 0, temp poll: 0, RP RI: 55</li>
     * <li>Thread that is responsible for the server checks should start.</li>
     * <li>Checking server points for the start poll:
     * - Check Start Poll Request on TH.
     * - Check Start Poll Response on TH.
     * - Check Subscription&Resource in Cassandra.</li>
     * <li>HIT, get response with expected delay, sleep rest of the request interval
     * (The RI interval for this request has to be for about 55 seconds, the delay is 0 seconds).</li>
     * <li>Thread that is responsible for the changing resource should start. Setting a new delay to 65 seconds.</li>
     * <li>HIT, get response with expected delay, sleep rest of the request interval
     * (The RI interval for this request has to be for about 55 seconds, the delay is 0 seconds).</li>
     * <li>HIT, get response with expected delay, sleep rest of the request interval
     * (The RI interval for this request has to be for about 55 seconds, the delay is 0 seconds).</li>
     * <li>HIT, get response with expected delay, sleep rest of the request interval
     * (The RI interval for this request has to be for about 55 seconds, the delay is 0 seconds).</li>
     * <li>HIT, get response with expected delay, sleep rest of the request interval
     * (The RI interval for this request has to be for about 55 seconds, the delay is 0 seconds).</li>
     * <li>Checking correctness of the IWOC.</li>
     * <li>MISS, get response with expected delay, sleep rest of the request interval
     * (The RI interval for this request has to be for about 55 seconds, the delay is 0 seconds).</li>
     * <li></li>
     * </ol>
     *
     * @throws Exception
     */
    @LargeTest
    public void test_006_E2E_RLP_Update() throws Exception {

        error = null;
        final String RESOURCE_URI = "asimov_e2e_rlp_006";
        final String uri = createTestResourceUri(RESOURCE_URI);
        final HttpRequest request = createRequest().setUri(uri).setMethod("GET").addHeaderField("X-OC-Stateless-Sleep", "true").getRequest();

        final StartPollTask startPollTask = new StartPollTask();
        final StartPollParamsTask startPollParamsTask = new StartPollParamsTask();
        final ThTask thNotificationReceivedTask = new ThTask();
        final Exchanger<ExchangeParameters> exchanger = new Exchanger<ExchangeParameters>();
        final long interval = 55 * DateUtil.SECONDS;
        final ExecutorService service = Executors.newFixedThreadPool(3);
        final CountDownLatch changeResourceLatch = new CountDownLatch(1);
        final CountDownLatch checkFirstPoll = new CountDownLatch(1);
        PrepareResourceUtil.prepareResource(uri, false);

        Runnable pollVerdicts = new Runnable() {
            int requestId = 1;

            @Override
            public void run() {
                try {
                    Log.i(TAG, "Thread that is responsible for poll verdicts has started.");
                    checkMiss(request, requestId++, (int) (interval)); //1
                    checkFirstPoll.countDown();
                    checkMiss(request, requestId++, (int) (interval)); //2
                    checkMiss(request, requestId++, (int) (interval)); //3
                    checkHit(request, requestId++, (int) (interval));  //4
                    setResourceDelay(65, request);
                    changeResourceLatch.countDown();
                    checkHit(request, requestId++, (int) (interval));  //5
                    checkHit(request, requestId++, (int) (interval));  //6
                    checkHit(request, requestId++, (int) (interval));  //6.1
                    checkHit(request, requestId++, (int) (interval));  //7
                    checkMiss(request, requestId++, (int) (interval)); //8
                } catch (AssertionFailedError e) {
                    Log.e(TAG, "Thread that is responsible for poll verdicts has failed.");
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                    if (service != null) service.shutdownNow();
                    error = e;
                } catch (Exception e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } finally {
                    PrepareResourceUtil.invalidateResourceSafely(uri);
                    logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
                }
            }
        };

        Runnable clientLogChecks = new Runnable() {
            LogcatUtil logcatUtil = null;

            @Override
            public void run() {
                logcatUtil = new LogcatUtil(getContext(), startPollTask, startPollParamsTask);
                try {
                    checkFirstPoll.await(operationTimeOut, TimeUnit.SECONDS);
                    Log.i(TAG, "Thread that is responsible for client log checks has started. Checking start poll...");
                    logcatUtil.start();
                    logSleeping(2 * interval);
                    logcatUtil.stop();
                    advancedCheckOfStartPoll(startPollTask, startPollParamsTask, 0, interval, 1, 55, 0, 0);
                    Log.i(TAG, "The start poll was checked successfully on client side.");
                    StartPollWrapper firstStartPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
                    StartPollParamsWrapper firstStartPollParameters = startPollParamsTask.getLogEntries().get(startPollParamsTask.getLogEntries().size() - 1);
                    exchanger.exchange(new ExchangeParameters(firstStartPoll, firstStartPollParameters));

                    changeResourceLatch.await(operationTimeOut, TimeUnit.SECONDS);
                    Log.i(TAG, "Checking client logcat in order to find IWOC...");
                    logcatUtil = new LogcatUtil(getContext(), thNotificationReceivedTask);
                    logcatUtil.start();
                    logSleeping(250 * DateUtil.SECONDS);
                    logcatUtil.stop();
                    assertTrue("Client should receive IWOC notification. Please, verify that your client has msisdn validation",
                            checkThEntriesListForReceivedInvalidateNotification(thNotificationReceivedTask, startPollTask) != null);
                    Log.i(TAG, "IWOC was checked successfully.");
                } catch (AssertionFailedError e) {
                    Log.e(TAG, "Thread that is responsible for client start polls has failed");
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                    if (service != null) service.shutdownNow();
                    error = e;
                } catch (Exception e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } finally {
                    stopLogcat(logcatUtil);
                }
            }
        };

        Runnable serverChecks = new Runnable() {

            public void run() {
                try {
                    ExchangeParameters exchangeParameters = exchanger.exchange(null, 200, TimeUnit.SECONDS);
                    StartPollWrapper startPollWrapper = exchangeParameters.getStartPollWrapper();
                    StartPollParamsWrapper startPollParamsWrapper = exchangeParameters.getStartPollParamsWrapper();

                    Log.i(TAG, String.format("Thread that is responsible for the server checks has started. StartPollWrapper: %s.", startPollWrapper.toString()));
                    String resourceKey = new HttpRequestMd5CalculatorUtil(request, Collections.singletonList((long) startPollParamsWrapper.getRi())).getRequestMd5();
                    Log.i(TAG, String.format("Resource key calculated by TF: %s", resourceKey));
                    checkPollingStarted(startPollWrapper.getSubscriptionId(), resourceKey);
                } catch (AssertionFailedError e) {
                    Log.e(TAG, "Thread that is responsible for server checks has failed");
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                    error = e;
                } catch (InterruptedException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } catch (URISyntaxException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } catch (IOException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } catch (TimeoutException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                }
            }
        };

        service.submit(pollVerdicts);
        service.submit(clientLogChecks);
        service.submit(serverChecks);

        service.awaitTermination(testTimeOut, TimeUnit.SECONDS);
        if (error != null) throw error;
    }

    /**
     * <h1>Verifying start_poll parameters and correctness of IWC notification receiving for LP.</h1>
     * <p>A test resource is needed for this test case that returns the same response for first 3 requests and another one for the rest.</p>
     * <p>Pattern: [0,85,85,85], Delays: [65,65,65,65].</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Thread that is responsible for poll verdicts should start.</li>
     * <li>Thread that is responsible for client log checks should start.</li>
     * <li>MISS, get response with expected delay, sleep rest of the request interval
     * (The RI interval for this request has to be for about 85 seconds, the delay is 65 seconds).</li>
     * <li>MISS, get response with expected delay, sleep rest of the request interval
     * (The RI interval for this request has to be for about 85 seconds, the delay is 65 seconds).</li>
     * <li>Checking start poll parameters during the interval on client side
     * We anticipate the following start poll parameters: package com.seven.asimov.it, poll class: 4, RI: 0, IT: 0, TO: 66, temp poll: 0, RP RI: 0</li>
     * <li>Thread that is responsible for the server checks should start.</li>
     * <li>Checking server points for the start poll:
     * - Check Start Poll Request on TH.
     * - Check Start Poll Response on TH.
     * - Check Subscription&Resource in Cassandra.</li>
     * <li>HIT, get response with expected delay, sleep rest of the request interval
     * (The RI interval for this request has to be for about 85 seconds, the delay is 65 seconds).</li>
     * <li>Thread that is responsible for the changing resource should start. Changing body of the corresponding resource.</li>
     * <li>Checking correctness of the IWC.</li>
     * <li>Request should be HITed with new response</li>
     * </ol>
     *
     * @throws Exception
     */
    @LargeTest
    public void test_007_E2E_RLP_Update() throws Exception {

        error = null;
        final String RESOURCE_URI = "asimov_e2e_rlp_007";
        final String uri = createTestResourceUri(RESOURCE_URI);
        final HttpRequest request = createRequest().setUri(uri).setMethod("GET").addHeaderField("X-OC-Stateless-Sleep", "true").getRequest();

        final StartPollTask startPollTask = new StartPollTask();
        final StartPollParamsTask startPollParamsTask = new StartPollParamsTask();
        final ThTask thNotificationReceivedTask = new ThTask();
        final Exchanger<ExchangeParameters> exchanger = new Exchanger<ExchangeParameters>();
        final long interval = 85 * DateUtil.SECONDS;
        final long delay = 65 * DateUtil.SECONDS;
        final ExecutorService service = Executors.newFixedThreadPool(4);
        final CountDownLatch changeResourceLatch = new CountDownLatch(1);
        PrepareResourceUtil.prepareResource(uri, false);

        Runnable pollVerdicts = new Runnable() {
            int requestId = 1;

            @Override
            public void run() {
                try {
                    Log.i(TAG, "Thread that is responsible for poll verdicts has started.");
                    setResourceDelay(delay / DateUtil.SECONDS, request);
                    checkMiss(request, requestId++, (int) (interval)); //1
                    checkMiss(request, requestId++, (int) (interval)); //2
                    checkHit(request, requestId++, (int) (interval)); //3
                    changeResourceLatch.countDown();
                    checkHit(request, requestId++, (int) (interval));  //4
                } catch (AssertionFailedError e) {
                    Log.e(TAG, "Thread that is responsible for poll verdicts has failed.");
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                    if (service != null) service.shutdownNow();
                    error = e;
                } catch (Exception e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } finally {
                    PrepareResourceUtil.invalidateResourceSafely(uri);
                    logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
                }
            }
        };

        Runnable clientLogChecks = new Runnable() {
            LogcatUtil logcatUtil = null;

            @Override
            public void run() {
                logcatUtil = new LogcatUtil(getContext(), startPollTask, startPollParamsTask);
                try {
                    Log.i(TAG, "Thread that is responsible for client log checks has started. Checking start poll...");
                    logcatUtil.start();
                    logSleeping(2 * interval + 15);
                    logcatUtil.stop();
                    advancedCheckOfStartPoll(startPollTask, startPollParamsTask, delay, 0, 4, 0, 0, 0);
                    Log.i(TAG, "The start poll was checked successfully on client side.");
                    StartPollWrapper firstStartPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
                    StartPollParamsWrapper firstStartPollParameters = startPollParamsTask.getLogEntries().get(startPollParamsTask.getLogEntries().size() - 1);
                    exchanger.exchange(new ExchangeParameters(firstStartPoll, firstStartPollParameters));

                    changeResourceLatch.await(operationTimeOut, TimeUnit.SECONDS);
                    Log.i(TAG, "Checking client logcat in order to find IWC...");
                    logcatUtil = new LogcatUtil(getContext(), thNotificationReceivedTask);
                    logcatUtil.start();
                    logSleeping(interval);
                    logcatUtil.stop();
                    assertTrue("Client should receive IWC notification. Please, verify that your client has msisdn validation",
                            checkThEntriesListForReceivedInvalidateNotification(thNotificationReceivedTask, startPollTask) != null);
                    Log.i(TAG, "IWC was checked successfully.");
                } catch (AssertionFailedError e) {
                    Log.e(TAG, "Thread that is responsible for client start polls has failed");
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                    if (service != null) service.shutdownNow();
                    error = e;
                } catch (Exception e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } finally {
                    stopLogcat(logcatUtil);
                }
            }
        };

        Runnable serverChecks = new Runnable() {

            public void run() {
                try {
                    ExchangeParameters exchangeParameters = exchanger.exchange(null, 200, TimeUnit.SECONDS);
                    StartPollWrapper startPollWrapper = exchangeParameters.getStartPollWrapper();
                    StartPollParamsWrapper startPollParamsWrapper = exchangeParameters.getStartPollParamsWrapper();

                    Log.i(TAG, String.format("Thread that is responsible for the server checks has started. StartPollWrapper: %s.", startPollWrapper.toString()));
                    String resourceKey = new HttpRequestMd5CalculatorUtil(request, Collections.singletonList(0L)).getRequestMd5();
                    Log.i(TAG, String.format("Resource key calculated by TF: %s", resourceKey));
                    checkPollingStarted(startPollWrapper.getSubscriptionId(), resourceKey);
                } catch (AssertionFailedError e) {
                    Log.e(TAG, "Thread that is responsible for server checks has failed");
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                    error = e;
                } catch (InterruptedException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } catch (URISyntaxException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } catch (IOException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } catch (TimeoutException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                }
            }
        };

        Runnable changeResource = new Runnable() {

            public void run() {
                try {
                    changeResourceLatch.await(operationTimeOut, TimeUnit.SECONDS);
                    logSleeping(15 * DateUtil.SECONDS);
                    Log.i(TAG, "Changing the resource...");
                    HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                            .addHeaderField("X-OC-ChangeResponseContent", INVALIDATED_RESPONSE)
                            .addHeaderField("X-OC-Stateless-Sleep", "true")
                            .addHeaderField("X-OC-ChangeSleep", String.valueOf(delay / DateUtil.SECONDS)).getRequest();
                    sendRequest2(request, false, true);
                } catch (AssertionFailedError e) {
                    Log.e(TAG, "Thread that is responsible for changing resource has failed");
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                    error = e;
                } catch (InterruptedException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                }
            }
        };

        service.submit(pollVerdicts);
        service.submit(clientLogChecks);
        service.submit(serverChecks);
        service.submit(changeResource);

        service.awaitTermination(testTimeOut, TimeUnit.SECONDS);
        if (error != null) throw error;
    }

    @Ignore
    //TODO Should be updated after an apropriate client/server fix
    /**
     * <h1>This test is ignored due to http://jira.seven.com/browse/ASMV-18590.</h1>
     * <h1>Verifying correctness of IWOC notification receiving for LP.</h1>
     * <p>A test resource is needed for this test case that returns the same response for all requests.</p>
     * <p>Pattern: [0,85,85,85,85], Delays: [65,65,65,95,95].</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Thread that is responsible for poll verdicts should start.</li>
     * <li>Thread that is responsible for client log checks should start.</li>
     * <li>MISS, get response with expected delay, sleep rest of the request interval.</li>
     * <li>MISS, get response with expected delay, sleep rest of the request interval.</li>
     * <li>Checking start poll parameters during the interval on client side.</li>
     * <li>Thread that is responsible for the server checks should start.</li>
     * <li>Checking server points for the start poll.</li>
     * <li>HIT, get response with expected delay, sleep rest of the request interval.</li>
     * <li>Thread that is responsible for the changing resource should start.</li>
     * <li>Checking correctness of the IWOC.</li>
     * <li>HIT, get response with expected delay, sleep rest of the request interval.</li>
     * <li>MISS, get response with expected delay, sleep rest of the request interval.</li>
     * <li></li>
     * </ol>
     *
     * @throws Exception
     */
    @LargeTest
    public void test_008_E2E_RLP_Update() throws Exception {

        error = null;
        final String RESOURCE_URI = "asimov_e2e_rlp_008";
        final String uri = createTestResourceUri(RESOURCE_URI);
        final HttpRequest request = createRequest().setUri(uri).setMethod("GET").addHeaderField("X-OC-Stateless-Sleep", "true").getRequest();

        final StartPollTask startPollTask = new StartPollTask();
        final StartPollParamsTask startPollParamsTask = new StartPollParamsTask();
        final ThTask thNotificationReceivedTask = new ThTask();
        final Exchanger<ExchangeParameters> exchanger = new Exchanger<ExchangeParameters>();
        final long interval = 85 * DateUtil.SECONDS;
        final long delay = 65 * DateUtil.SECONDS;
        final ExecutorService service = Executors.newFixedThreadPool(3);
        final CountDownLatch changeResourceLatch = new CountDownLatch(1);
        PrepareResourceUtil.prepareResource(uri, false);

        Runnable pollVerdicts = new Runnable() {
            int requestId = 1;

            @Override
            public void run() {
                try {
                    Log.i(TAG, "Thread that is responsible for poll verdicts has started.");
                    setResourceDelay(delay / DateUtil.SECONDS, request);
                    checkMiss(request, requestId++, (int) (interval)); //1
                    checkMiss(request, requestId++, (int) (interval)); //2
                    checkHit(request, requestId++, (int) (interval)); //3
                    setResourceDelay(95, request);
                    changeResourceLatch.countDown();
                    checkHit(request, requestId++, (int) (interval));  //4
                    checkMiss(request, requestId++, (int) (interval)); //5
                } catch (AssertionFailedError e) {
                    Log.e(TAG, "Thread that is responsible for poll verdicts has failed.");
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                    if (service != null) service.shutdownNow();
                    error = e;
                } catch (Exception e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } finally {
                    PrepareResourceUtil.invalidateResourceSafely(uri);
                    logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
                }
            }
        };

        Runnable clientLogChecks = new Runnable() {
            LogcatUtil logcatUtil = null;

            @Override
            public void run() {
                logcatUtil = new LogcatUtil(getContext(), startPollTask, startPollParamsTask);
                try {
                    Log.i(TAG, "Thread that is responsible for client log checks has started. Checking start poll...");
                    logcatUtil.start();
                    logSleeping(2 * interval);
                    logcatUtil.stop();
                    advancedCheckOfStartPoll(startPollTask, startPollParamsTask, delay, 0, 4, 0, 0, 0);
                    Log.i(TAG, "The start poll was checked successfully on client side.");
                    StartPollWrapper firstStartPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
                    StartPollParamsWrapper firstStartPollParameters = startPollParamsTask.getLogEntries().get(startPollParamsTask.getLogEntries().size() - 1);
                    exchanger.exchange(new ExchangeParameters(firstStartPoll, firstStartPollParameters));

                    changeResourceLatch.await(operationTimeOut, TimeUnit.SECONDS);
                    Log.i(TAG, "Checking client logcat in order to find IWOC...");
                    logcatUtil = new LogcatUtil(getContext(), thNotificationReceivedTask);
                    logcatUtil.start();
                    logSleeping(85 * DateUtil.SECONDS);
                    logcatUtil.stop();
                    assertTrue("Client should receive IWOC notification. Please, verify that your client has msisdn validation",
                            checkThEntriesListForReceivedInvalidateNotification(thNotificationReceivedTask, startPollTask) != null);
                    Log.i(TAG, "IWOC was checked successfully.");
                } catch (AssertionFailedError e) {
                    Log.e(TAG, "Thread that is responsible for client start polls has failed");
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                    if (service != null) service.shutdownNow();
                    error = e;
                } catch (Exception e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } finally {
                    stopLogcat(logcatUtil);
                }
            }
        };

        Runnable serverChecks = new Runnable() {

            public void run() {
                try {
                    ExchangeParameters exchangeParameters = exchanger.exchange(null, 200, TimeUnit.SECONDS);
                    StartPollWrapper startPollWrapper = exchangeParameters.getStartPollWrapper();
                    StartPollParamsWrapper startPollParamsWrapper = exchangeParameters.getStartPollParamsWrapper();

                    Log.i(TAG, String.format("Thread that is responsible for the server checks has started. StartPollWrapper: %s.", startPollWrapper.toString()));
                    String resourceKey = new HttpRequestMd5CalculatorUtil(request, Collections.singletonList(0L)).getRequestMd5();
                    Log.i(TAG, String.format("Resource key calculated by TF: %s", resourceKey));
                    checkPollingStarted(startPollWrapper.getSubscriptionId(), resourceKey);
                } catch (AssertionFailedError e) {
                    Log.e(TAG, "Thread that is responsible for server checks has failed");
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                    error = e;
                } catch (InterruptedException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } catch (URISyntaxException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } catch (IOException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                } catch (TimeoutException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                }
            }
        };

        service.submit(pollVerdicts);
        service.submit(clientLogChecks);
        service.submit(serverChecks);

        service.awaitTermination(testTimeOut, TimeUnit.SECONDS);
        if (error != null) throw error;
    }

    public void test_999_E2E_CleanUp() {
        clearProperties();
        try {
            notifyRestForTestEnd(TAG);
            PMSUtil.cleanPaths(new String[]{"@asimov@http"});
        } catch (Exception e) {
            Log.e(TAG, "Tests end REST notification failed");
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
    }
}
