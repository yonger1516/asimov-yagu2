package com.seven.asimov.it.testcases;

import com.seven.asimov.it.base.HttpHeaderField;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.TestCaseThread;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.*;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.e2eTasks.SubscriptionStatusTask;
import com.seven.asimov.it.utils.logcat.tasks.pollingTasks.GetCachedResponseTask;
import com.seven.asimov.it.utils.logcat.tasks.pollingTasks.StartPollParamsTask;
import com.seven.asimov.it.utils.logcat.tasks.pollingTasks.StartPollTask;
import com.seven.asimov.it.utils.logcat.wrappers.GetCachedResponseWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.StartPollParamsWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.StartPollWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.SubscriptionStatusWrapper;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import junit.framework.AssertionFailedError;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class E2ECacheInvalidateTestCase extends E2ETestCase {
    private static final Logger logger = LoggerFactory.getLogger(E2ECacheInvalidateTestCase.class.getSimpleName());
    protected static final String SUITE_NAME = "E2E-CacheInvalidate";
    protected static final int RADIO_KEEPER_DELAY_MS = 5 * 1000;
    private static final long SERVER_LONG_POLLING_INTERVAL = 0L;
    protected final String HTTP_PROXY_PATH = "@asimov@http";
    protected final String CACHE_INVALIDATE_AGGRESSIVENESS_NAME = "cache_invalidate_aggressiveness";
    protected final String NO_CACHE_INVALIDATE_AGGRESSIVENESS_NAME = "no_cache_invalidate_aggressiveness";
    protected int ocUid;
    protected int tfUid;


    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private static Future<Boolean> serverCheckerResult;
    private static int startPollRI;
    private static int startPollTO;
    private static List<Long> testrunnerAccessTimes = new ArrayList<Long>();

    public enum RadioState {
        RADIO_UP, RADIO_DOWN
    }

    public enum ScreenState {
        SCREEN_ON, SCREEN_OFF
    }

    protected TestCaseThread createRadioUpKeeperThread() throws UnknownHostException {
        return new TestCaseThread() {
            private String pingableHost;

            {
                InetAddress[] hostToPing = InetAddress.getAllByName(PMSUtil.getPmsServerIp());
                assertTrue("Failed to resolve IPv4 address of PMS server to ping with radio keeper thread.",
                        hostToPing.length != 0);
                for (InetAddress addr : hostToPing) {
                    if (addr instanceof Inet4Address) {
                        pingableHost = addr.getHostAddress();
                    }
                }
            }

            @Override
            public void run() throws Throwable {
                while (!isInterruptedSoftly()) {
                    long startTime = System.currentTimeMillis();
                    pingHost(pingableHost);
                    long sleepMS = RADIO_KEEPER_DELAY_MS > System.currentTimeMillis() - startTime ?
                            RADIO_KEEPER_DELAY_MS - (System.currentTimeMillis() - startTime) : 0;
                    TestUtil.sleep(sleepMS);
                }
            }
        };
    }

    protected void addNetworkRul(boolean addRules) throws Throwable {
        if (addRules) {
            ocUid = IpTablesUtil.getApplicationUid(getContext(), TFConstantsIF.OC_PACKAGE_NAME);
            tfUid = IpTablesUtil.getApplicationUid(getContext(), TFConstantsIF.IT_PACKAGE_NAME);
            IpTablesUtil.banNetworkForAllApplications(true);
            IpTablesUtil.allowNetworkForApplication(true, ocUid);
            IpTablesUtil.allowNetworkForApplication(true, tfUid);
        } else {
            IpTablesUtil.banNetworkForAllApplications(false);
            IpTablesUtil.allowNetworkForApplication(false, ocUid);
            IpTablesUtil.allowNetworkForApplication(false, tfUid);
        }
    }

    protected String cacheInvResourceKey;
    final int littleDELAY = 10 * 1000;

    protected void checkPolling(LogcatUtil logcat, HttpRequest testRequest, StartPollParamsTask startPollParamsTask, StartPollTask startPollTask, int currentStep, int checkStep, boolean isLongPoll) throws Exception {
        if (currentStep == checkStep) {
            TestUtil.sleep(littleDELAY / 2);
            logcat.start();
        } else if (currentStep == (checkStep + 2)) {
            TestUtil.sleep(littleDELAY / 2);
            logcat.stop();
            assertTrue("Start of polling should be reported in client log", !startPollTask.getLogEntries().isEmpty());
            StartPollWrapper startPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
            logger.info("Start poll wrapper object" + startPoll);
            StartPollParamsWrapper startPollParams = startPollParamsTask.getLogEntries().get(startPollParamsTask.getLogEntries().size() - 1);

            if (isLongPoll) {
                cacheInvResourceKey = new HttpRequestMd5CalculatorUtil(testRequest, new ArrayList<Long>(
                        Arrays.asList((long) SERVER_LONG_POLLING_INTERVAL))).getRequestMd5();
            } else {
                cacheInvResourceKey = new HttpRequestMd5CalculatorUtil(testRequest, new ArrayList<Long>(
                        Arrays.asList((long) startPollParams.getRi()))).getRequestMd5();
            }

            serverCheckerResult = executorService.submit(new PollingStartedChecker(startPoll.getSubscriptionId(), cacheInvResourceKey));
            startPollRI = startPollParams.getRi();
            startPollTO = startPollParams.getTo();
        }
    }

    protected void checkIWOC(LogcatUtil logcat, boolean finalCheck, int iwocDelay, StartPollTask startPoll, SubscriptionStatusTask subscriptionStatusTask, int currentStep, int checkStep) throws Exception {
        if (!finalCheck && (currentStep == checkStep)) {
            assertTrue("checkIWOC should be called after polling started", !startPoll.getLogEntries().isEmpty());
            StartPollWrapper wrapper = startPoll.getLogEntries().get(0);
            subscriptionStatusTask.configureTask(wrapper.getSubscriptionId(), wrapper.getResourceId(),
                    SubscriptionStatusWrapper.SubscriptionStatus.INVALIDATED_WO_CACHE.toString());

            TestUtil.sleep(littleDELAY / 2);
            logcat.start();
        } else if (finalCheck) {
            if (iwocDelay > 0)
                TestUtil.sleep(iwocDelay * 1000);

            TestUtil.sleep(littleDELAY / 2);

            logcat.stop();
            for (SubscriptionStatusWrapper wrapper : subscriptionStatusTask.getLogEntries()) {
                logger.info("checkIWOC wrapper:" + wrapper.toString());
            }
            assertTrue("Expected IWOC log entry", ((!subscriptionStatusTask.getLogEntries().isEmpty()) &&
                    (subscriptionStatusTask.getLogEntries().get(0).getStatus() == SubscriptionStatusWrapper.SubscriptionStatus.INVALIDATED_WO_CACHE)));
        }
    }


    protected void checkIWC(LogcatUtil logcat, boolean finalCheck, StartPollTask startPoll, SubscriptionStatusTask subscriptionStatusTask, GetCachedResponseTask getCachedResponseTask, int currentStep, int checkStep, boolean shouldRequestCache) throws Exception {
        if (!finalCheck && (currentStep == checkStep)) {
            assertTrue("checkIWC should be called after polling started", !startPoll.getLogEntries().isEmpty());
            StartPollWrapper wrapper = startPoll.getLogEntries().get(0);
            subscriptionStatusTask.configureTask(wrapper.getSubscriptionId(), wrapper.getResourceId(),
                    SubscriptionStatusWrapper.SubscriptionStatus.INVALIDATED_W_CACHE.toString());
            getCachedResponseTask.configureTask(wrapper.getSubscriptionId());
            TestUtil.sleep(littleDELAY / 2);
            logcat.start();
        } else if (finalCheck) {
            TestUtil.sleep(littleDELAY / 2);
            logcat.stop();
            for (SubscriptionStatusWrapper wrapper : subscriptionStatusTask.getLogEntries()) {
                logger.info("checkIWC wrapper:" + wrapper.toString());
            }
            assertTrue("Expected IWC log entry", ((!subscriptionStatusTask.getLogEntries().isEmpty()) &&
                    (subscriptionStatusTask.getLogEntries().get(0).getStatus() == SubscriptionStatusWrapper.SubscriptionStatus.INVALIDATED_W_CACHE)));

            checkCacheRequested(getCachedResponseTask, shouldRequestCache);
        }
    }

    private void checkCacheRequested(GetCachedResponseTask getCachedResponseTask, boolean shouldRequestCache) {
        logger.info("checkCacheRequested wrappers count=" + getCachedResponseTask.getLogEntries().size());
        for (GetCachedResponseWrapper wrapper : getCachedResponseTask.getLogEntries()) {
            logger.info("checkCacheRequested wrapper:" + wrapper.toString());
        }
        assertTrue("Cached response should" + (shouldRequestCache ? " be " : " not be ") + "requested",
                (shouldRequestCache && !getCachedResponseTask.getLogEntries().isEmpty()) ||
                        (!shouldRequestCache && getCachedResponseTask.getLogEntries().isEmpty()));
    }

    class ResourceModificationThread extends Thread {
        HttpRequest testRequest;

        public ResourceModificationThread(HttpRequest testRequest) {
            this.testRequest = testRequest.copy();
        }

        @Override
        public void run() {
            try {
                recordTestrunnerAccessTime();
                sendRequest2(testRequest, false, true);
            } catch (Throwable t) {
                logger.debug(ExceptionUtils.getStackTrace(t));
            }
        }
    }

    protected int modifyResponse(HttpRequest testRequest, boolean changeDelay, int delaySeconds, boolean changeResource, boolean isIWC) {
        if (!changeDelay && !changeResource)
            return delaySeconds;
        Thread modificationThread;
        HttpRequest modificationRequest = testRequest.copy();
        if (changeDelay && !(changeResource && !isIWC)) {    //Do not set delay for IWOC, we will set it later.
            modificationRequest.addHeaderField(new HttpHeaderField("X-OC-Stateless-Sleep", "true"));
            modificationRequest.addHeaderField(new HttpHeaderField("X-OC-ChangeSleep", Integer.toString(delaySeconds)));
            modificationThread = new ResourceModificationThread(modificationRequest);
            modificationThread.setDaemon(true);
            executorService.submit(modificationThread);
            TestUtil.sleep(littleDELAY / 2);
        }
        if (changeResource) {
            if (isIWC) {
                modificationRequest.addHeaderField(new HttpHeaderField("X-OC-ChangeResponseContent", INVALIDATED_RESPONSE));
                modificationThread = new ResourceModificationThread(modificationRequest);
                modificationThread.setDaemon(true);
                executorService.submit(modificationThread);
                TestUtil.sleep(littleDELAY / 2);
            } else {
                modificationRequest.addHeaderField(new HttpHeaderField("X-OC-Stateless-Sleep", "true"));
                modificationRequest.addHeaderField(new HttpHeaderField("X-OC-ChangeSleep", Integer.toString(10000)));
                modificationThread = new ResourceModificationThread(modificationRequest);
                modificationThread.setDaemon(true);
                executorService.submit(modificationThread);
                TestUtil.sleep(littleDELAY / 2);
            }
        }

        return delaySeconds;
    }

    protected long requestToServer(HttpRequest testRequest, boolean expectedFromNetwork, int requestNumber, long previousRequestTime, int expectedInterval, String expectedAnswer) throws Exception {
        assertTrue("Request interval skew. Expected interval:" + expectedInterval + " currentInterval:" +
                Long.toString((System.currentTimeMillis() - previousRequestTime) / 1000),
                (previousRequestTime == 0) || (System.currentTimeMillis() - previousRequestTime < expectedInterval * 1000));

        if (previousRequestTime > 0)
            TestUtil.sleep(expectedInterval * 1000 - (System.currentTimeMillis() - previousRequestTime));

        long requestTime = System.currentTimeMillis();
        if (expectedFromNetwork) {
            recordTestrunnerAccessTime();
            checkMiss(testRequest, requestNumber, expectedAnswer);
        } else {
            checkHit(testRequest, requestNumber, expectedAnswer);
        }

        return requestTime;
    }

    class PollingStartedChecker implements Callable<Boolean> {
        String subscriptionID;
        String resourceKey;

        public PollingStartedChecker(String subscriptionID, String resourceKey) {
            this.subscriptionID = subscriptionID;
            this.resourceKey = resourceKey;
        }

        @Override
        public Boolean call() throws Exception {
            logger.info("PollingStartedChecker start");
            checkPollingStarted(subscriptionID, resourceKey);
            logger.info("PollingStartedChecker passed");
            return true;
        }
    }

    protected void recordTestrunnerAccessTime() {
        synchronized (testrunnerAccessTimes) {
            testrunnerAccessTimes.add(System.currentTimeMillis() / 1000);
        }
    }

    protected void checkPollingAtTestrunner(HttpRequest testRequest, boolean isLongPoll, StartPollTask startPollTask, int ukkoPollCount) throws Exception {
        StringBuilder intervalString = new StringBuilder();
        StartPollWrapper startPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
        Long ukkoPollStartTime = startPoll.getTimestamp() / 1000 + (isLongPoll ? 0 : ((int) (startPollRI * 0.73)));
        testrunnerAccessTimes.add(ukkoPollStartTime);
        for (int i = 1; i < ukkoPollCount; i++) {
            testrunnerAccessTimes.add(ukkoPollStartTime + i * (isLongPoll ? startPollTO : startPollRI));
        }
        Collections.sort(testrunnerAccessTimes);

        Long prevAccessTime = testrunnerAccessTimes.get(0);
        for (int i = 1; i < testrunnerAccessTimes.size(); i++) {
            intervalString.append((int) ((testrunnerAccessTimes.get(i) - prevAccessTime))).append("@");
            prevAccessTime = testrunnerAccessTimes.get(i);
        }

        intervalString.deleteCharAt(intervalString.length() - 1);
        checkTestrunnerPolling(testrunnerAccessTimes.size() - 1, intervalString.toString(), testRequest.getUri());
    }

    protected void checkE2ECacheInvIWC(String address, boolean shouldRequestCache, boolean isLongPoll, int aggressiveness,
                                       int[] requestInterval, int[] responseDelay, ScreenState[] screenStates,
                                       RadioState[] radioStates, boolean[] expectedFromNetwork, int serverPollCheckStep, int invCheckStep, int ukkoPollCountExpected) throws Throwable {
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final int littleDELAY = 10 * 1000;
        final String URI = createTestResourceUri(address);
        final HttpRequest testRequest = createRequest().setUri(URI).setMethod("GET").getRequest();


        final StartPollParamsTask startPollParamsTask = new StartPollParamsTask();
        final StartPollTask startPollTask = new StartPollTask();
        final GetCachedResponseTask getCachedResponseTask = new GetCachedResponseTask(null);
        final LogcatUtil pollingLogcat = new LogcatUtil(getContext(), startPollTask, startPollParamsTask);
        final SubscriptionStatusTask subscriptionStatusTask = new SubscriptionStatusTask(null, null, null);
        final LogcatUtil invalidateLogcat = new LogcatUtil(getContext(), subscriptionStatusTask, getCachedResponseTask);
        String expectedAnswer = VALID_RESPONSE;
        long intervalStart = 0L;
        int lastResponseDelay = 0;
        cacheInvResourceKey = "";
        startPollRI = 0;
        startPollTO = 0;
        testrunnerAccessTimes.clear();

        ScreenState lastScreenState = null;
        RadioState lastRadioState = null;
        TestCaseThread radioUpKeeperThread = createRadioUpKeeperThread();


        PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(CACHE_INVALIDATE_AGGRESSIVENESS_NAME, Integer.toString(aggressiveness), HTTP_PROXY_PATH, true)});

        try {
            for (int i = 0; i < screenStates.length; i++) {
                if (screenStates[i] != lastScreenState) {
                    lastScreenState = screenStates[i];
                    if (screenStates[i] == ScreenState.SCREEN_ON)
                        ScreenUtils.screenOn();
                    else
                        ScreenUtils.screenOff();
                }

                if (radioStates[i] != lastRadioState) {
                    lastRadioState = radioStates[i];
                    if (radioStates[i] == RadioState.RADIO_UP) {
                        addNetworkRul(false);
                        radioUpKeeperThread = createRadioUpKeeperThread();
                        executorService.submit(radioUpKeeperThread);
                    } else {
                        radioUpKeeperThread.interruptSoftly();
                        addNetworkRul(true);
                    }
                }

                if (shouldRequestCache && (i == screenStates.length - 1))
                    expectedAnswer = INVALIDATED_RESPONSE;


                checkPolling(pollingLogcat, testRequest, startPollParamsTask, startPollTask, i, serverPollCheckStep, isLongPoll);

                checkIWC(invalidateLogcat, false, startPollTask, subscriptionStatusTask, getCachedResponseTask, i, invCheckStep, shouldRequestCache);

                lastResponseDelay = modifyResponse(testRequest, lastResponseDelay != responseDelay[i], responseDelay[i], i == invCheckStep, true);

                intervalStart = requestToServer(testRequest, expectedFromNetwork[i], i, intervalStart, requestInterval[i], expectedAnswer);

            }
            TestUtil.sleep(littleDELAY);
            checkIWC(invalidateLogcat, true, startPollTask, subscriptionStatusTask, getCachedResponseTask, screenStates.length, invCheckStep, shouldRequestCache);

            assertTrue("Expected server to start polling", serverCheckerResult.get());//Will throw exception reported by PollingStartedChecker if there is one

            checkUkkoPolling(cacheInvResourceKey, ukkoPollCountExpected);

            checkPollingAtTestrunner(testRequest, isLongPoll, startPollTask, ukkoPollCountExpected);
        } finally {
            pollingLogcat.stop();
            if ((invalidateLogcat != null) && (invalidateLogcat.isRunning()))
                invalidateLogcat.stop();
            ScreenUtils.screenOn();
            PMSUtil.cleanPaths(new String[]{HTTP_PROXY_PATH});
            radioUpKeeperThread.interruptSoftly();
            PrepareResourceUtil.invalidateResourceSafely(URI);

            addNetworkRul(false);
            TestUtil.sleep(littleDELAY / 2);
        }
    }

    protected void checkE2ECacheInvIWOC(String address, int aggressiveness,
                                        int[] requestInterval, int[] responseDelay, ScreenState[] screenStates,
                                        RadioState[] radioStates, boolean[] expectedFromNetwork, int serverPollCheckStep, int invStep, boolean waitForIWOC, int ukkoPollCountExpected) throws Throwable {
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final int littleDELAY = 10 * 1000;
        final String URI = createTestResourceUri(address);
        final HttpRequest testRequest = createRequest().setUri(URI).setMethod("GET").getRequest();
        final StartPollParamsTask startPollParamsTask = new StartPollParamsTask();
        final StartPollTask startPollTask = new StartPollTask();
        final LogcatUtil pollingLogcat = new LogcatUtil(getContext(), startPollTask, startPollParamsTask);
        final SubscriptionStatusTask subscriptionStatusTask = new SubscriptionStatusTask(null, null, null);
        final LogcatUtil invalidateLogcat = new LogcatUtil(getContext(), subscriptionStatusTask);
        long intervalStart = 0L;
        int lastResponseDelay = 0;
        cacheInvResourceKey = "";
        startPollRI = 0;
        startPollTO = 0;
        testrunnerAccessTimes.clear();
        ScreenState lastScreenState = null;
        RadioState lastRadioState = null;
        TestCaseThread radioUpKeeperThread = createRadioUpKeeperThread();

        PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(NO_CACHE_INVALIDATE_AGGRESSIVENESS_NAME, Integer.toString(aggressiveness), HTTP_PROXY_PATH, true)});

        try {
            for (int i = 0; i < screenStates.length; i++) {
                if (screenStates[i] != lastScreenState) {
                    lastScreenState = screenStates[i];
                    if (screenStates[i] == ScreenState.SCREEN_ON)
                        ScreenUtils.screenOn();
                    else
                        ScreenUtils.screenOff();
                }

                if (radioStates[i] != lastRadioState) {
                    lastRadioState = radioStates[i];
                    if (radioStates[i] == RadioState.RADIO_UP) {
                        addNetworkRul(false);
                        radioUpKeeperThread = createRadioUpKeeperThread();
                        executorService.submit(radioUpKeeperThread);
                    } else {
                        radioUpKeeperThread.interruptSoftly();
                        addNetworkRul(true);
                    }
                }

                checkPolling(pollingLogcat, testRequest, startPollParamsTask, startPollTask, i, serverPollCheckStep, false);

                checkIWOC(invalidateLogcat, false, 0, startPollTask, subscriptionStatusTask, i, invStep);

                lastResponseDelay = modifyResponse(testRequest, lastResponseDelay != responseDelay[i], responseDelay[i], i == invStep, false);

                intervalStart = requestToServer(testRequest, expectedFromNetwork[i], i, intervalStart, requestInterval[i], VALID_RESPONSE);

            }
            checkIWOC(invalidateLogcat, true, waitForIWOC ? (int) (requestInterval[requestInterval.length - 1] * 1.5) : 0, startPollTask, subscriptionStatusTask, screenStates.length, invStep);

            assertTrue("Expected server to start polling", serverCheckerResult.get());//Will throw exception reported by PollingStartedChecker if there is one

            checkUkkoPolling(cacheInvResourceKey, ukkoPollCountExpected);

            checkPollingAtTestrunner(testRequest, false, startPollTask, ukkoPollCountExpected);

        } finally {
            pollingLogcat.stop();
            if ((invalidateLogcat != null) && (invalidateLogcat.isRunning()))
                invalidateLogcat.stop();
            ScreenUtils.screenOn();
            PMSUtil.cleanPaths(new String[]{HTTP_PROXY_PATH});
            radioUpKeeperThread.interruptSoftly();
            PrepareResourceUtil.invalidateResourceSafely(URI);
            executorService.shutdown();
            executorService.awaitTermination(10, TimeUnit.SECONDS);
            executorService.shutdownNow();

            addNetworkRul(false);
            TestUtil.sleep(littleDELAY / 2);
        }
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        if (PMSUtil.getDeviceZ7TpAddress() != null &&
                PMSUtil.getDeviceZ7TpAddress().length() > 2) {
            z7TpId = PMSUtil.getDeviceZ7TpAddress().substring(2, PMSUtil.getDeviceZ7TpAddress().length());
        } else {
            throw new AssertionFailedError("Some problems with OC. File transport_settings not found or corrupted.");
        }
    }

    @Override
    protected void runTest() throws Throwable {
        try {
            super.runTest();
        } catch (Exception e) {
            throw new AssertionFailedError(e.getMessage());
        }
    }
}
