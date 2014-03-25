package com.seven.asimov.it.tests.dispatchers.bwlists;


import android.test.suitebuilder.annotation.LargeTest;
import com.seven.asimov.it.annotation.DeviceOnly;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.testcases.GWListTestCase;
import com.seven.asimov.it.utils.logcat.wrappers.InterfaceType;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import com.seven.asimov.it.utils.sms.SmsUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * Check private networks and black lists: ({@link ZWhiteBlackListingTests#test_001_Blacklist() Test 1},
 * {@link ZWhiteBlackListingTests#test_002_PrivateNetwork() Test 2},
 * {@link ZWhiteBlackListingTests#test_003_PrivateNetworkBlackList() Test 3},
 * {@link ZWhiteBlackListingTests#test_004_PrivateNetworkWhitelist() Test 4},
 * {@link ZWhiteBlackListingTests#test_005_GlobalBlackListing() Test 5},
 */
@DeviceOnly
public class ZWhiteBlackListingTests extends GWListTestCase {
    private static final Logger logger = LoggerFactory.getLogger(ZWhiteBlackListingTests.class.getSimpleName());
    private static final String host1 = "www.httpwatch.com";
    private static final String host2 = "www.ukr.net:*";
    private static final String host3 = "www.bigmir.net:*";
    private static final String host4 = "www.httpwatch.com:*";
    private static final String uri1 = "http://www.httpwatch.com/httpgallery/caching/imageB/";
    private static final String uri2 = "http://www.ukr.net/";
    private static final String uri3 = "http://www.bigmir.net/";
    private static final String uri4 = "asimov_it_cv_global_blacklist";

    public static final String BLACKLIST = "blacklist";
    public static final String HTTP_PATH = "@asimov@http";
    public static final String PRIVATE_NETWORK = "private_networks";
    public static final String PN_PATH = "@asimov@pn";
    public static final String WHITELIST = "whitelist";

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
     * <h1>Testing that HTTP traffic can be bypassed with a help of policy.</h1>
     * <p>The test checks that 4 response will be MISS.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Adding appropriate policy for HTTP white list</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void test_001_Blacklist() throws Throwable {
        final Policy blacklist = new Policy(BLACKLIST, host1, HTTP_PATH, true);
        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{blacklist});
            logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
            checkPolling(uri1, true, InterfaceType.MOBILE, false, (int) MIN_RMP_PERIOD, TFConstantsIF.WAIT_FOR_POLICY_UPDATE, false);
        } finally {
            PMSUtil.cleanPaths(new String[]{HTTP_PATH});
        }
    }

    /**
     * <h1>Testing traffic for private networks.</h1>
     * <p>The test checks that 4 response will be MISS.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Adding appropriate policy for private networks</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void test_002_PrivateNetwork() throws Throwable {
        final Policy privateNetwork = new Policy(PRIVATE_NETWORK, host2, PN_PATH, true);
        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{privateNetwork});
            logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
            checkPolling(uri2, true, InterfaceType.MOBILE, false, (int) MIN_RMP_PERIOD, TFConstantsIF.WAIT_FOR_POLICY_UPDATE, false);
        } finally {
            PMSUtil.cleanPaths(new String[]{PN_PATH});
        }
    }

    /**
     * <h1>Testing traffic for private networks.</h1>
     * <p>The test checks that 4 response will be MISS.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Adding appropriate policy blacklist</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void test_003_PrivateNetworkBlackList() throws Throwable {
        final Policy blacklist = new Policy(BLACKLIST, host3, PN_PATH, true);
        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{blacklist});
            logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
            checkPolling(uri3, true, InterfaceType.MOBILE, false, (int) MIN_RMP_PERIOD, TFConstantsIF.WAIT_FOR_POLICY_UPDATE, false);
        } finally {
            PMSUtil.cleanPaths(new String[]{PN_PATH});
        }
    }

    /**
     * <h1>Testing traffic for private networks.</h1>
     * <p>The test checks that 4 response will be HIT.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Adding appropriate policy whitelist</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>HIT, get response immediately, sleep on 5 seconds</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void test_004_PrivateNetworkWhitelist() throws Throwable {
        final Policy whitelist = new Policy(WHITELIST, host4, PN_PATH, true);
        final Policy blacklist = new Policy(BLACKLIST, host4, HTTP_PATH, false);
        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{whitelist, blacklist});
            logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
            checkPolling(uri1, true, InterfaceType.MOBILE, false, (int) MIN_RMP_PERIOD, TFConstantsIF.WAIT_FOR_POLICY_UPDATE, true);
        } finally {
            PMSUtil.cleanPaths(new String[]{HTTP_PATH, PN_PATH});
        }
    }

    /**
     * <h1>Testing traffic for HTTP.</h1>
     * <p>The test checks that 4 response will be MISS but after cleaning policies 8 response will be HIT.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Adding appropriate policy blacklist for HTTP</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>Cleaning policy blacklist for HTTP</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>HIT, get response immediately, sleep on 5 seconds</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void test_005_GlobalBlackListing() throws Throwable {
        final Policy blacklist = new Policy(BLACKLIST, TEST_RESOURCE_HOST, HTTP_PATH, true);
        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{blacklist});
            checkPolling(uri4, false, InterfaceType.MOBILE, false, (int) MIN_RMP_PERIOD, TFConstantsIF.WAIT_FOR_POLICY_UPDATE, false);
            PMSUtil.cleanPaths(new String[]{HTTP_PATH});
            logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
            SmsUtil.sendPolicyUpdate(getContext(), (byte) 1);
            checkPolling(uri4, false, InterfaceType.MOBILE, false, (int) MIN_RMP_PERIOD, TFConstantsIF.WAIT_FOR_POLICY_UPDATE, true);
        } finally {
            PMSUtil.cleanPaths(new String[]{HTTP_PATH});
        }
    }
}
