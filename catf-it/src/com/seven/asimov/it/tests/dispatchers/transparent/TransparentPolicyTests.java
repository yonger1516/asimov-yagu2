package com.seven.asimov.it.tests.dispatchers.transparent;


import com.seven.asimov.it.testcases.TransparentTestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransparentPolicyTests extends TransparentTestCase {
    private static final Logger logger = LoggerFactory.getLogger(TransparentPolicyTests.class.getSimpleName());

    private final static String STORAGE_TRIGGERS_PATH = "@asimov@reporting@triggers@storage";
    private final static String STORAGE_TRIGGERS_MIN_ENTRIES = "min_entries";
    private static final String ASIMOV_HOME_PATH = "@asimov";
    private static final String TRANSPARENT = "transparent";
    private final String TRUE_NUM = "1";
    private final String FALSE_NUM = "0";

    /**
     * 1. Turn PMS transparent mode on, using policy.
     * 2. Make some requests.
     * 3. Client goes to transparent mode as soon, as policy is received.
     */
    public void test_001_Transparent() throws Throwable {
        final String uri = createTestResourceUri("testTransparent_001", true);
        final int numRequests = 4;
        final int interval = 15 * 1000;
        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(TRANSPARENT, TRUE_NUM, ASIMOV_HOME_PATH, true)});

            checkTransparent(uri, numRequests, interval, true, getContext(), TRANSPARENT_CHECK);
        } finally {
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(TRANSPARENT, FALSE_NUM, ASIMOV_HOME_PATH, true)});
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }


    /**
     * 1. Turn PMS transparent mode on, using policy.
     * 2. Make some requests.
     * 3. All CRCS logs must be still sent to server.
     */

    public void test_002_Transparent() throws Throwable {
        final String uri = createTestResourceUri("testTransparent_002", true);
        final int numRequests = 10;
        final int interval = 1000;
        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(TRANSPARENT, TRUE_NUM, ASIMOV_HOME_PATH, true),
                    new Policy(STORAGE_TRIGGERS_MIN_ENTRIES, "10", STORAGE_TRIGGERS_PATH, true)});

            assertTrue("Expected traffic to bypass OC.", checkTransparent(uri, numRequests, interval, true, getContext(), CRCS_SENT));
        } finally {
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(TRANSPARENT, FALSE_NUM, ASIMOV_HOME_PATH, true),
                    new Policy(STORAGE_TRIGGERS_MIN_ENTRIES, "10", STORAGE_TRIGGERS_PATH, false)});

            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * 1. Turn PMS transparent mode on, using policy.
     * 2. Make some requests.
     * 3. No optimization is being made - client must not make any optimization attempts.
     */

    public void test_003_Transparent() throws Throwable {
        final String uri = createTestResourceUri("testTransparent_003", true);
        final int numRequests = 4;
        final int interval = 15 * 1000;
        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(TRANSPARENT, TRUE_NUM, ASIMOV_HOME_PATH, true)});

            assertTrue("Expected traffic to bypass OC.", checkTransparent(uri, numRequests, interval, true, getContext(), NO_POLLING));
        } finally {
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(TRANSPARENT, FALSE_NUM, ASIMOV_HOME_PATH, true)});
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * 1. Turn PMS transparent mode on, using policy.
     * 2. Make some requests.
     * 3. No cache is being stored by OCE.
     */

    public void test_004_Transparent() throws Throwable {
        final String uri = createTestResourceUri("testTransparent_004", true);
        final int numRequests = 4;
        final int interval = 15 * 1000;
        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(TRANSPARENT, TRUE_NUM, ASIMOV_HOME_PATH, true)});

            assertTrue("Expected traffic to bypass OC.", checkTransparent(uri, numRequests, interval, true, getContext(), NO_CACHING));
        } finally {
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(TRANSPARENT, FALSE_NUM, ASIMOV_HOME_PATH, true)});
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * 1. Turn PMS transparent mode on, using policy.
     * 2. Make some requests.
     * 3. - OCE starts optimizing traffic.
     * - Traffic is cached only after fully transparent is turned off.
     * - soft-close and deferred close for sockets is enabled.
     * - OUT closed sockets are parked.
     */

    public void test_008_Transparent() throws Throwable {
        final String uri = createTestResourceUri("testTransparent_008", true);
        final int numRequests = 4;
        final int interval = 15 * 1000;
        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(TRANSPARENT, TRUE_NUM, ASIMOV_HOME_PATH, true)});
            assertTrue("Expected traffic to bypass OC", checkTransparent(uri, numRequests, interval, true, getContext(), NO_CACHING));

            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(TRANSPARENT, FALSE_NUM, ASIMOV_HOME_PATH, true)});
            assertTrue("Expected traffic to be intercepted by OC", checkTransparent(uri, numRequests, interval, false, getContext(), TRANSPARENT_CHECK));
        } finally {
            PMSUtil.addPolicies(new Policy[]{new Policy(TRANSPARENT, FALSE_NUM, ASIMOV_HOME_PATH, true)});
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }
}
