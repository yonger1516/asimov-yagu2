package com.seven.asimov.it.tests.dispatchers.bwlists;


import com.seven.asimov.it.annotation.DeviceOnly;
import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.testcases.GWListTestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.date.DateUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.dispatcherTasks.OCCrashTask;
import com.seven.asimov.it.utils.logcat.tasks.policyTasks.ThreadPoolTask;
import com.seven.asimov.it.utils.logcat.wrappers.InterfaceType;
import com.seven.asimov.it.utils.logcat.wrappers.OCCrashWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.ThreadPoolWrapper;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import junit.framework.AssertionFailedError;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Check white and grey list for HTTPS: ({@link GWListHttpsTests#test_001_Https() Test 1},
 * {@link GWListHttpsTests#test_002_Https() Test 2},
 * {@link GWListHttpsTests#test_003_Https() Test 3},
 * {@link GWListHttpsTests#test_004_Https() Test 4},
 * {@link GWListHttpsTests#test_005_Https() Test 5},
 * {@link GWListHttpsTests#test_006_Https() Test 6},
 * {@link GWListHttpsTests#test_007_Https() Test 7},
 */
public class GWListHttpsTests extends GWListTestCase {
    private static final Logger logger = LoggerFactory.getLogger(GWListHttpsTests.class.getSimpleName());
    private final static String PATH = "@asimov@application@com.seven.asimov.it";
    private final static String NAME = "ssl";
    private final static String FULL_PATH = PATH + "@" + NAME;

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

            } catch (Throwable assertionFailedError) {
                logger.info("Test failed due to " + ExceptionUtils.getStackTrace(assertionFailedError));
                isPassed = false;
                counts.add("Test failed due to AssertionFailedError in " + numberOfAttempts + " attempt");
                Thread.sleep(2 * 60 * 1000);
            }
        } while (!isPassed && numberOfAttempts < 3);

        assertTrue("The test was failed three times ", counts.size() != 3);
    }

    /**
     * <h1>Testing that HTTPS traffic is white-listed by default for WiFi.</h1>
     * <p>The test checks that 5 response will be MISS.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Delete an appropriate namespace</li>
     * <li>Adding personal policy to obtain policy immediately</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>Create an appropriate namespace and delete the personal policy<li/>
     * </ol>
     *
     * @throws Throwable
     */
    @DeviceOnly
    public void test_001_Https() throws Throwable {
        final Policy enabled = new Policy("enabled", "false", FULL_PATH, true);
        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{enabled});


            checkPolling("asimov_it_https_001", false, InterfaceType.WIFI, true, (int) MIN_RMP_PERIOD, (int) DateUtil.MINUTES, false);
        } finally {
            PMSUtil.cleanPaths(new String[]{FULL_PATH});
        }
    }

    /**
     * <h1>Testing that HTTPS traffic is allowed for WiFi with a help of namespace</h1>
     * <p>The test checks that 4 response will be HIT.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Create an appropriate namespace</li>
     * <li>Adding personal policy to obtain policy immediately</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>HIT, get response immediately, sleep on 5 seconds</li>
     * <li>Delete an appropriate policy</li>
     * </ol>
     *
     * @throws Throwable
     */
    @DeviceOnly
    public void test_002_Https() throws Throwable {
        final Policy enabled = new Policy("enabled", "true", FULL_PATH, true);
        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{enabled});


            checkPolling("asimov_it_https_002", false, InterfaceType.WIFI, true, (int) MIN_RMP_PERIOD, (int) DateUtil.MINUTES, true);

        } finally {
            PMSUtil.cleanPaths(new String[]{FULL_PATH});
        }
    }

    /**
     * <h1>Testing that HTTPS traffic is white-listed by default for 3G</h1>
     * <p>The test checks that 5 response will be MISS.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Stop WiFi</li>
     * <li>Check for 3G <li/>
     * <li>Delete an appropriate namespace</li>
     * <li>Adding personal policy to obtain policy immediately</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>Create an appropriate namespace and delete the personal policy<li/>
     * <p/>
     * </ol>
     *
     * @throws Throwable
     */
    @DeviceOnly
    public void test_003_Https() throws Throwable {
        final Policy enabled = new Policy("enabled", "false", FULL_PATH, true);
        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{enabled});


            checkPolling("asimov_it_https_003", false, InterfaceType.MOBILE, true, (int) MIN_RMP_PERIOD, (int) DateUtil.MINUTES, false);
        } finally {
            PMSUtil.cleanPaths(new String[]{FULL_PATH});
        }
    }

    /**
     * <h1>Testing that HTTPS traffic is allowed for 3G with a help of namespace</h1>
     * <p>The test checks that 4 response will be HIT.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Stop WiFi</li>
     * <li>Check for 3G <li/>
     * <li>Create an appropriate namespace</li>
     * <li>Adding personal policy to obtain policy immediately</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>HIT, get response immediately, sleep on 5 seconds</li>
     * <li>Delete an appropriate policy</li>
     * </ol>
     *
     * @throws Throwable
     */
    @DeviceOnly
    public void test_004_Https() throws Throwable {
        final Policy enabled = new Policy("enabled", "true", FULL_PATH, true);
        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{enabled});


            checkPolling("asimov_it_https_004", false, InterfaceType.MOBILE, true, (int) MIN_RMP_PERIOD, (int) DateUtil.MINUTES, true);
        } finally {
            PMSUtil.cleanPaths(new String[]{FULL_PATH});
        }
    }

    /**
     * <h1>Testing that there are no deadlocks caused by https transaction</h1>
     * <p>The test checks that amount of pending task decrease with time</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Add policy except_domains for our test framework</li>
     * <li>Send requests 20 times without any delay</li>
     * <li>Retrieving number of pending task</li>
     * <li>Sleep for about 1 minute</li>
     * <li>Verifying that amount of pending task has already decreased</li>
     * <li>Send a request again. Check that response duration is as expected</li>
     * <li>Delete an appropriate policy</li>
     * </ol>
     *
     * @throws Throwable TODO Ignored - fails due to no ThreadPoolTask being captured from log - needs investigation.
     */
    @Ignore
    @DeviceOnly
    public void test_005_Https() throws Throwable {
        final Policy enabled = new Policy("enabled", "true", FULL_PATH, true);
        final String uri = createTestResourceUri("asimov_it_except_domains_006", true);
        try {
            final Policy exceptDomains = new Policy("except_domains", ".*domains_006", FULL_PATH, true);
            PMSUtil.addPoliciesWithCheck(new Policy[]{enabled, exceptDomains});

            checkThreadPoolDeadlock(uri, 45, 150 * 1000, 10 * 1000);
        } finally {
            PMSUtil.cleanPaths(new String[]{FULL_PATH});
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * <h1>Testing that there are no deadlocks caused by https transaction</h1>
     * <p>The test checks that no com.seven.asimov crash and no huge amount of pending tasks</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Add policy except_domains</li>
     * <li>Send 5 request to another host. OC should optimize this traffic.</li>
     * <li>Sleep for some time</li>
     * <li>Send 5 request to the host in except_domains. OC should not optimize this traffic.</li>
     * <li>Performing checks that depend on logcat.</li>
     * <li>Delete an appropriate policy</li>
     * </ol>
     *
     * @throws Throwable
     */
    @DeviceOnly
    public void test_006_Https() throws Throwable {
        final String host = getHostForExcept();
        final String uriForOptimization = createTestResourceUri("asimov_it_except_domains_007", true);
        final String uriWithoutOptimization = createTestResourceCustomUri(host, "except_domains_007", true);
        final ThreadPoolTask threadPoolTask = new ThreadPoolTask();
        final OCCrashTask ocCrashTask = new OCCrashTask();
        final LogcatUtil logcatUtil = new LogcatUtil(getContext(), threadPoolTask, ocCrashTask);

        final Policy enabled = new Policy("enabled", "true", FULL_PATH, true);
        try {
            final Policy exceptDomains = new Policy("except_domains", host, FULL_PATH, true);
            PMSUtil.addPoliciesWithCheck(new Policy[]{enabled, exceptDomains});
            final HttpRequest requestForOptimization = createRequest().setUri(uriForOptimization).setMethod("GET").getRequest();
            final HttpRequest requestWithoutOptimization = createRequest().setUri(uriWithoutOptimization).setMethod("GET").getRequest();
            logcatUtil.start();
            checkMiss(requestForOptimization, 1, 5 * 1000);
            checkMiss(requestForOptimization, 2, 5 * 1000);
            checkMiss(requestForOptimization, 3, 5 * 1000);
            checkHit(requestForOptimization, 4, 5 * 1000);
            checkHit(requestForOptimization, 5, 5 * 1000);
            logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
            checkMiss(requestWithoutOptimization, 1, 5 * 1000);
            checkMiss(requestWithoutOptimization, 2, 5 * 1000);
            checkMiss(requestWithoutOptimization, 3, 5 * 1000);
            checkMiss(requestWithoutOptimization, 4, 5 * 1000);
            logSleeping(2 * TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
            logcatUtil.stop();
            List<ThreadPoolWrapper> firstThreadPoolEntries = threadPoolTask.getLogEntries();
            assertTrue("Pending tasks detected!", firstThreadPoolEntries.size() == 0);
        } catch (Exception e) {
            logcatUtil.stop();
            logSleeping(2 * TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
            List<OCCrashWrapper> ocCrashWrappers = ocCrashTask.getLogEntriesWoFilteringByTimeStamp();
            assertTrue("OC crash!", ocCrashWrappers.size() == 0);
            throw new AssertionFailedError("Ssl exception detected. Check logs!");
        } finally {
            PMSUtil.cleanPaths(new String[]{FULL_PATH});
            PrepareResourceUtil.invalidateResourceSafely(uriForOptimization);
            PrepareResourceUtil.invalidateResourceSafely(uriWithoutOptimization);
        }
    }

    /**
     * <h1>Testing that there are no deadlocks caused by https transaction</h1>
     * <p>The test checks that no com.seven.asimov crash and no huge amount of pending tasks</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Add policy except_domains.</li>
     * <li>Send 5 request to another host. OC should optimize this traffic.</li>
     * <li>Clean except_domains policy.</li>
     * <li>Emulate sms notification.</li>
     * <li>Sleep for some time</li>
     * <li>Send 5 request to the host that was in the except_domains. OC should optimize this traffic.</li>
     * <li>Performing checks that depend on logcat.</li>
     * <li>Delete an appropriate policy</li>
     * </ol>
     *
     * @throws Throwable
     */
    @DeviceOnly
    public void test_007_Https() throws Throwable {
        final String host = getHostForExcept();
        final String uriForOptimization = createTestResourceUri("asimov_it_except_domains_008", true);
        final String uriForExceptHost = createTestResourceCustomUri(host, "except_domains_008", true);
        final ThreadPoolTask threadPoolTask = new ThreadPoolTask();
        final OCCrashTask ocCrashTask = new OCCrashTask();
        final LogcatUtil logcatUtil = new LogcatUtil(getContext(), threadPoolTask, ocCrashTask);

        final Policy enabled = new Policy("enabled", "true", FULL_PATH, true);
        try {
            final Policy exceptDomains = new Policy("except_domains", host, FULL_PATH, true);
            PMSUtil.addPoliciesWithCheck(new Policy[]{enabled, exceptDomains});

            final HttpRequest requestForOptimization = createRequest().setUri(uriForOptimization).setMethod("GET").getRequest();
            final HttpRequest requestToExceptHost = createRequest().setUri(uriForExceptHost).setMethod("GET").getRequest();
            logcatUtil.start();
            checkMiss(requestForOptimization, 1, 5 * 1000);
            checkMiss(requestForOptimization, 2, 5 * 1000);
            checkMiss(requestForOptimization, 3, 5 * 1000);
            checkHit(requestForOptimization, 4, 5 * 1000);
            checkHit(requestForOptimization, 5, 5 * 1000);

            final Policy exceptDomainsRemove = new Policy("except_domains", host, FULL_PATH, false);
            PMSUtil.addPoliciesWithCheck(new Policy[]{exceptDomainsRemove});

            checkMiss(requestToExceptHost, 1, 5 * 1000);
            checkMiss(requestToExceptHost, 2, 5 * 1000);
            checkMiss(requestToExceptHost, 3, 5 * 1000);
            checkHit(requestToExceptHost, 4, 5 * 1000);
            checkHit(requestToExceptHost, 5, 5 * 1000);
            logcatUtil.stop();
            logSleeping(2 * TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
            List<ThreadPoolWrapper> firstThreadPoolEntries = threadPoolTask.getLogEntries();
            assertTrue("Pending tasks detected!", firstThreadPoolEntries.size() == 0);
        } catch (Exception e) {
            logcatUtil.stop();
            logSleeping(2 * TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
            List<OCCrashWrapper> ocCrashWrappers = ocCrashTask.getLogEntriesWoFilteringByTimeStamp();
            assertTrue("OC crash!", ocCrashWrappers.size() == 0);
            throw new AssertionFailedError("Ssl exception detected. Check logs!");
        } finally {
            PMSUtil.cleanPaths(new String[]{FULL_PATH});
            PMSUtil.cleanPaths(new String[]{"@asimov@http"});
            logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
            PrepareResourceUtil.invalidateResourceSafely(uriForOptimization);
            PrepareResourceUtil.invalidateResourceSafely(uriForExceptHost);
        }
    }
}
