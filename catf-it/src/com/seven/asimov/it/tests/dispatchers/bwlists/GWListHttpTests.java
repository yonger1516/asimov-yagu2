package com.seven.asimov.it.tests.dispatchers.bwlists;


import com.seven.asimov.it.annotation.DeviceOnly;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.testcases.GWListTestCase;
import com.seven.asimov.it.utils.logcat.wrappers.InterfaceType;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * Check white and grey list for HTTP: ({@link GWListHttpTests#test_001_Http() Test 1},
 * {@link GWListHttpTests#test_002_Http() Test 2},
 * {@link GWListHttpTests#test_003_Http() Test 3},
 * {@link GWListHttpTests#test_004_Http() Test 4},
 */
public class GWListHttpTests extends GWListTestCase {
    private static final Logger logger = LoggerFactory.getLogger(GWListHttpTests.class.getSimpleName());
    final String HTTP_PATH = "@asimov@http";

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
     * <h1>Testing that HTTP traffic is grey-listed by default for WiFi</h1>
     * <p>The test checks that 5 response will be HIT.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>HIT, get response immediately, sleep on 5 seconds</li>
     * </ol>
     *
     * @throws Throwable
     */
    @DeviceOnly
    public void test_001_Http() throws Throwable {
        final Policy blacklist = new Policy("blacklist", TEST_RESOURCE_HOST, HTTP_PATH, false);
        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{blacklist});
            logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
            checkPolling("asimov_it_black_white_http_001", false, InterfaceType.WIFI, false, (int) MIN_RMP_PERIOD, TFConstantsIF.WAIT_FOR_POLICY_UPDATE, true);
        } finally {
            PMSUtil.cleanPaths(new String[]{HTTP_PATH});
        }
    }


    /**
     * <h1>Testing white list for HTTP using WiFi connection</h1>
     * <p>The test checks that 5 response will be MISS.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Adding personal policy with name: blacklist, path: @asimov@http, value: hki-dev-testrunner2.7sys.eu</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * </ol>
     *
     * @throws Throwable
     */
    @DeviceOnly
    public void test_002_Http() throws Throwable {
        final Policy blacklist = new Policy("blacklist", TEST_RESOURCE_HOST, HTTP_PATH, true);
        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{blacklist});
            logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
            checkPolling("asimov_it_black_white_http_002", false, InterfaceType.WIFI, false, (int) MIN_RMP_PERIOD, TFConstantsIF.WAIT_FOR_POLICY_UPDATE, false);
        } finally {
            PMSUtil.cleanPaths(new String[]{HTTP_PATH});
        }
    }


    /**
     * <h1>Testing that HTTP traffic is grey-listed by default for 3G</h1>
     * <p>The test checks that 5 response will be HIT.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Stop WiFi</li>
     * <li>Check for 3G <li/>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>HIT, get response immediately, sleep on 5 seconds</li>
     * </ol>
     *
     * @throws Throwable
     */
    @DeviceOnly
    public void test_003_Http() throws Throwable {
        final Policy blacklist = new Policy("blacklist", TEST_RESOURCE_HOST, HTTP_PATH, false);
        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{blacklist});
            logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
            checkPolling("asimov_it_black_white_http_003", false, InterfaceType.MOBILE, false, (int) MIN_RMP_PERIOD, TFConstantsIF.WAIT_FOR_POLICY_UPDATE, true);
        } finally {
            PMSUtil.cleanPaths(new String[]{HTTP_PATH});
        }
    }


    /**
     * <h1>Testing white list for HTTP using 3G connection</h1>
     * <p>The test checks that 5 response will be MISS.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Stop WiFi</li>
     * <li>Check for 3G <li/>
     * <li>Adding personal policy with name: blacklist, path: @asimov@http, value: hki-dev-testrunner2.7sys.eu</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * </ol>
     *
     * @throws Throwable
     */
    @DeviceOnly
    public void test_004_Http() throws Throwable {
        final Policy blacklist = new Policy("blacklist", TEST_RESOURCE_HOST, HTTP_PATH, true);
        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{blacklist});
            logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
            checkPolling("asimov_it_black_white_http_004", false, InterfaceType.MOBILE, false, (int) MIN_RMP_PERIOD, TFConstantsIF.WAIT_FOR_POLICY_UPDATE, false);
            PMSUtil.cleanPaths(new String[]{"@asimov@http"});
        } finally {
            PMSUtil.cleanPaths(new String[]{HTTP_PATH});
        }
    }
}
