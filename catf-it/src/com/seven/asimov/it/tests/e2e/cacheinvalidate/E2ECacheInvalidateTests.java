package com.seven.asimov.it.tests.e2e.cacheinvalidate;

import android.test.suitebuilder.annotation.LargeTest;
import com.seven.asimov.it.testcases.E2ECacheInvalidateTestCase;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class E2ECacheInvalidateTests extends E2ECacheInvalidateTestCase {
    private static final Logger logger = LoggerFactory.getLogger(E2ECacheInvalidateTests.class.getSimpleName());

    @LargeTest
    public void test_000_E2E_CacheInvalidate() {
        try {
            notifyRestForTestsStart(SUITE_NAME);
        } catch (Exception e) {
            logger.debug("Tests start REST notification failed");
            e.printStackTrace();
        }
    }


    /**
     * Set cache_invalidate_aggressiveness to 0.
     * For all test keep screen OFF and radio UP
     * Send 3 requests with interval of 65s. They should MISS.
     * Check client log for start of polling.
     * Check server side for poll request, poll answer and subscription in Cassandra DB.
     * Send one more request, it should HIT.
     * Modify resource to cause IWC.
     * Check client log for IWC message from server.
     * Client should retrieve cached response from traffic harmonizer. Check client logs for cache request.
     * Send next request, it should HIT.
     * Check testrunner resource access intervals.
     *
     * @throws Exception
     */

    public void test_001_E2E_CacheInvalidate() throws Throwable {
        checkE2ECacheInvIWC("test_001_E2E_CacheInvalidate", true, false, 0, new int[]{0, 65, 65, 65, 65},
                new int[]{0, 0, 0, 0, 0},
                new ScreenState[]{ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF},
                new RadioState[]{RadioState.RADIO_UP, RadioState.RADIO_UP, RadioState.RADIO_UP, RadioState.RADIO_UP, RadioState.RADIO_UP},
                new boolean[]{true, true, true, false, false}, 2, 4, 2);
    }

    /**
     * Set cache_invalidate_aggressiveness to 1.
     * Set screen OFF and radio DOWN
     * Send 3 requests with interval of 65s. They should MISS.
     * Check client log for start of polling.
     * Check server side for poll request, poll answer and subscription in Cassandra DB.
     * Send one more request, it should HIT.
     * Modify resource to cause IWC.
     * Check client log for IWC message from server.
     * Check client log to be sure Client NOT retrieving cached response from traffic harmonizer due to aggressiveness 1 and screen off.
     * Send next request, it should HIT.
     * Force radio UP
     * Send next request, it should HIT.
     * No cache retrieval should be observed in client log due to screen off.
     * Check testrunner resource access intervals.
     *
     * @throws Exception
     */

    public void test_002_E2E_CacheInvalidate() throws Throwable {
        checkE2ECacheInvIWC("test_002_E2E_CacheInvalidate", false, false, 1, new int[]{0, 65, 65, 65, 65, 65},
                new int[]{0, 0, 0, 0, 0, 0},
                new ScreenState[]{ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF},
                new RadioState[]{RadioState.RADIO_DOWN, RadioState.RADIO_DOWN, RadioState.RADIO_DOWN, RadioState.RADIO_DOWN, RadioState.RADIO_DOWN, RadioState.RADIO_UP},
                new boolean[]{true, true, true, false, false, false}, 2, 4, 2);
    }

    /**
     * Set cache_invalidate_aggressiveness to 2.
     * Set screen OFF and radio DOWN
     * Send 3 requests with interval of 65s. They should MISS.
     * Check client log for start of polling.
     * Check server side for poll request, poll answer and subscription in Cassandra DB.
     * Send one more request, it should HIT.
     * Modify resource to cause IWC.
     * Check client log for IWC message from server.
     * Check client log to be sure Client NOT retrieving cached response from traffic harmonizer due to aggressiveness 2 and screen off and radio down.
     * Send next request, it should HIT.
     * Force radio UP
     * Send next request, it should HIT.
     * No cache retrieval should be observed in client log due to screen off.
     * Check testrunner resource access intervals.
     *
     * @throws Exception
     */

    public void test_003_E2E_CacheInvalidate() throws Throwable {
        checkE2ECacheInvIWC("test_003_E2E_CacheInvalidate", false, false, 2, new int[]{0, 65, 65, 65, 65, 65},
                new int[]{0, 0, 0, 0, 0, 0},
                new ScreenState[]{ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF},
                new RadioState[]{RadioState.RADIO_DOWN, RadioState.RADIO_DOWN, RadioState.RADIO_DOWN, RadioState.RADIO_DOWN, RadioState.RADIO_DOWN, RadioState.RADIO_UP},
                new boolean[]{true, true, true, false, false, false}, 2, 4, 2);
    }


    /**
     * Set cache_invalidate_aggressiveness to 3.
     * During whole test keep screen ON and radio UP
     * Send 3 requests with interval of 65s. They should MISS.
     * Check client log for start of polling.
     * Check server side for poll request, poll answer and subscription in Cassandra DB.
     * Send one more request, it should HIT.
     * Modify resource to cause IWC.
     * Check client log for IWC message from server.
     * Check client log to be sure Client NOT retrieving cached response from traffic harmonizer due to aggressiveness 3 despite the screen oт and radio up.
     * Send next request, it should HIT.
     * Send next request, it should HIT.
     * No cache retrieval should be observed in client log.
     * Check testrunner resource access intervals.
     *
     * @throws Exception
     */

    public void test_004_E2E_CacheInvalidate() throws Throwable {
        checkE2ECacheInvIWC("test_004_E2E_CacheInvalidate", false, false, 3, new int[]{0, 65, 65, 65, 65, 65},
                new int[]{0, 0, 0, 0, 0, 0},
                new ScreenState[]{ScreenState.SCREEN_ON, ScreenState.SCREEN_ON, ScreenState.SCREEN_ON, ScreenState.SCREEN_ON, ScreenState.SCREEN_ON, ScreenState.SCREEN_ON},
                new RadioState[]{RadioState.RADIO_UP, RadioState.RADIO_UP, RadioState.RADIO_UP, RadioState.RADIO_UP, RadioState.RADIO_UP, RadioState.RADIO_UP},
                new boolean[]{true, true, true, false, false, false}, 2, 4, 2);
    }

    /**
     * Set cache_invalidate_aggressiveness to 0.
     * For all test keep screen OFF and radio UP
     * Prepare test resource with 65s delayed responses.
     * Send 2 requests with interval of 100s. They should MISS.
     * Check client log for start of polling.
     * Check server side for poll request, poll answer and subscription in Cassandra DB.
     * Send one more request, it should HIT.
     * Modify resource to cause IWC.
     * Check client log for IWC message from server.
     * Client should retrieve cached response from traffic harmonizer. Check client logs for cache request.
     * Send next request, it should HIT.
     * Check testrunner resource access intervals.
     *
     * @throws Exception
     */

    public void test_005_E2E_CacheInvalidate() throws Throwable {
        checkE2ECacheInvIWC("test_005_E2E_CacheInvalidate", true, true, 0, new int[]{0, 100, 100, 100},
                new int[]{65, 65, 65, 65},
                new ScreenState[]{ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF},
                new RadioState[]{RadioState.RADIO_UP, RadioState.RADIO_UP, RadioState.RADIO_UP, RadioState.RADIO_UP},
                new boolean[]{true, true, false, false}, 1, 3, 2);
    }

    /**
     * Set cache_invalidate_aggressiveness to 1.
     * Set screen OFF and radio DOWN
     * Prepare test resource with 65s delayed responses.
     * Send 2 requests with interval of 100s. They should MISS.
     * Check client log for start of polling.
     * Check server side for poll request, poll answer and subscription in Cassandra DB.
     * Send one more request, it should HIT.
     * Modify resource to cause IWC.
     * Check client log for IWC message from server.
     * Check client log to be sure Client NOT retrieving cached response from traffic harmonizer due to aggressiveness 1 and screen off.
     * Send next request, it should HIT.
     * Force radio UP
     * Send next request, it should HIT.
     * No cache retrieval should be observed in client log due to screen off.
     * Check testrunner resource access intervals.
     *
     * @throws Exception
     */

    public void test_006_E2E_CacheInvalidate() throws Throwable {
        checkE2ECacheInvIWC("test_006_E2E_CacheInvalidate", false, true, 1, new int[]{0, 100, 100, 100, 100},
                new int[]{65, 65, 65, 65, 65},
                new ScreenState[]{ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF},
                new RadioState[]{RadioState.RADIO_DOWN, RadioState.RADIO_DOWN, RadioState.RADIO_DOWN, RadioState.RADIO_DOWN, RadioState.RADIO_UP},
                new boolean[]{true, true, false, false, false}, 1, 3, 2);
    }

    /**
     * Set cache_invalidate_aggressiveness to 2.
     * Set screen OFF and radio DOWN
     * Prepare test resource with 65s delayed responses.
     * Send 2 requests with interval of 100s. They should MISS.
     * Check client log for start of polling.
     * Check server side for poll request, poll answer and subscription in Cassandra DB.
     * Send one more request, it should HIT.
     * Modify resource to cause IWC.
     * Check client log for IWC message from server.
     * Check client log to be sure Client NOT retrieving cached response from traffic harmonizer due to aggressiveness 2 and screen off and radio down.
     * Send next request, it should HIT.
     * Force radio UP
     * Send next request, it should HIT.
     * No cache retrieval should be observed in client log due to screen off.
     * Check testrunner resource access intervals.
     *
     * @throws Exception
     */

    public void test_007_E2E_CacheInvalidate() throws Throwable {
        checkE2ECacheInvIWC("test_007_E2E_CacheInvalidate", false, true, 2, new int[]{0, 100, 100, 100, 100},
                new int[]{65, 65, 65, 65, 65},
                new ScreenState[]{ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF},
                new RadioState[]{RadioState.RADIO_DOWN, RadioState.RADIO_DOWN, RadioState.RADIO_DOWN, RadioState.RADIO_DOWN, RadioState.RADIO_UP},
                new boolean[]{true, true, false, false, false}, 1, 3, 2);
    }

    /**
     * Set cache_invalidate_aggressiveness to 3.
     * During whole test keep screen ON and radio UP
     * Prepare test resource with 65s delayed responses.
     * Send 2 requests with interval of 100s. They should MISS.
     * Check client log for start of polling.
     * Check server side for poll request, poll answer and subscription in Cassandra DB.
     * Send one more request, it should HIT.
     * Modify resource to cause IWC.
     * Check client log for IWC message from server.
     * Check client log to be sure Client NOT retrieving cached response from traffic harmonizer due to aggressiveness 3 despite the screen on and radio up.
     * Send next request, it should HIT.
     * Send next request, it should HIT.
     * No cache retrieval should be observed in client log.
     * Check testrunner resource access intervals.
     *
     * @throws Exception
     */

    public void test_008_E2E_CacheInvalidate() throws Throwable {
        checkE2ECacheInvIWC("test_008_E2E_CacheInvalidate", false, true, 3, new int[]{0, 100, 100, 100, 100},
                new int[]{65, 65, 65, 65, 65},
                new ScreenState[]{ScreenState.SCREEN_ON, ScreenState.SCREEN_ON, ScreenState.SCREEN_ON, ScreenState.SCREEN_ON, ScreenState.SCREEN_ON},
                new RadioState[]{RadioState.RADIO_UP, RadioState.RADIO_UP, RadioState.RADIO_UP, RadioState.RADIO_UP, RadioState.RADIO_UP},
                new boolean[]{true, true, false, false, false}, 1, 3, 2);
    }

    /**
     * Set no_cache_invalidate_aggressiveness to 0.
     * For all test keep screen OFF and radio UP
     * Send 3 requests with interval of 65s. They should MISS.
     * Check client log for start of polling.
     * Check server side for poll request, poll answer and subscription in Cassandra DB.
     * Send one more request, it should HIT.
     * Modify resource to cause IWOC.
     * Send 3 more requests they should HIT
     * Check client log for IWOC message from server.
     * Check testrunner resource access intervals.
     *
     * @throws Exception
     */
    public void test_009_E2E_CacheInvalidate() throws Throwable {
        checkE2ECacheInvIWOC("test_009_E2E_CacheInvalidate", 0, new int[]{0, 65, 65, 65, 65, 65, 65},
                new int[]{0, 0, 0, 0, 0, 0, 0},
                new ScreenState[]{ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF},
                new RadioState[]{RadioState.RADIO_UP, RadioState.RADIO_UP, RadioState.RADIO_UP, RadioState.RADIO_UP, RadioState.RADIO_UP, RadioState.RADIO_UP, RadioState.RADIO_UP},
                new boolean[]{true, true, true, false, false, false, false}, 2, 4, true, 4);
    }

    /**
     * Set no_cache_invalidate_aggressiveness to 1.
     * Set screen OFF and radio DOWN
     * Send 3 requests with interval of 65s. They should MISS.
     * Check client log for start of polling.
     * Check server side for poll request, poll answer and subscription in Cassandra DB.
     * Send one more request, it should HIT.
     * Modify resource to cause IWOC.
     * Send 3 more requests they should HIT
     * Check client log for IWOC message from server.
     * Check client log to be sure Client NOT retrieving cached response from traffic harmonizer due to aggressiveness 1 and screen off.
     * Send next request, it should HIT.
     * No cache retrieval should be observed in client log.
     * Check testrunner resource access intervals.
     *
     * @throws Exception
     */
    public void test_010_E2E_CacheInvalidate() throws Throwable {
        checkE2ECacheInvIWOC("test_010_E2E_CacheInvalidate", 1, new int[]{0, 65, 65, 65, 65, 65, 65, 65},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0},
                new ScreenState[]{ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF},
                new RadioState[]{RadioState.RADIO_DOWN, RadioState.RADIO_DOWN, RadioState.RADIO_DOWN, RadioState.RADIO_DOWN, RadioState.RADIO_DOWN, RadioState.RADIO_DOWN, RadioState.RADIO_UP, RadioState.RADIO_UP},
                new boolean[]{true, true, true, false, false, false, false, false}, 2, 4, false, 4);
    }

    /**
     * Set no_cache_invalidate_aggressiveness to 2.
     * Set screen OFF and radio DOWN
     * Send 3 requests with interval of 65s. They should MISS.
     * Check client log for start of polling.
     * Check server side for poll request, poll answer and subscription in Cassandra DB.
     * Send one more request, it should HIT.
     * Modify resource to cause IWOC.
     * Send 3 more requests they should HIT
     * Check client log for IWOC message from server.
     * Check client log to be sure Client NOT retrieving cached response from traffic harmonizer due to aggressiveness 2 and screen off despite of radio UP.
     * Force radio UP.
     * Send next request, it should HIT.
     * No cache retrieval should be observed in client log.
     * Check testrunner resource access intervals.
     *
     * @throws Exception
     */

    public void test_011_E2E_CacheInvalidate() throws Throwable {
        checkE2ECacheInvIWOC("test_011_E2E_CacheInvalidate", 2, new int[]{0, 65, 65, 65, 65, 65, 65, 65},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0},
                new ScreenState[]{ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF},
                new RadioState[]{RadioState.RADIO_DOWN, RadioState.RADIO_DOWN, RadioState.RADIO_DOWN, RadioState.RADIO_DOWN, RadioState.RADIO_DOWN, RadioState.RADIO_DOWN, RadioState.RADIO_UP, RadioState.RADIO_UP},
                new boolean[]{true, true, true, false, false, false, false, false}, 2, 4, false, 4);
    }

    /**
     * Set no_cache_invalidate_aggressiveness to 3.
     * Set screen ON and radio UP
     * Send 3 requests with interval of 65s. They should MISS.
     * Check client log for start of polling.
     * Check server side for poll request, poll answer and subscription in Cassandra DB.
     * Send one more request, it should HIT.
     * Modify resource to cause IWOC.
     * Send 3 more requests they should HIT
     * Check client log for IWOC message from server.
     * Check client log to be sure Client NOT retrieving cached response from traffic harmonizer due to aggressiveness 3 despite the screen on and radio up.
     * Send next request, it should HIT.
     * No cache retrieval should be observed in client log.
     * Check testrunner resource access intervals.
     *
     * @throws Exception
     */

    public void test_012_E2E_CacheInvalidate() throws Throwable {
        checkE2ECacheInvIWOC("test_012_E2E_CacheInvalidate", 3, new int[]{0, 65, 65, 65, 65, 65, 65, 65},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0},
                new ScreenState[]{ScreenState.SCREEN_ON, ScreenState.SCREEN_ON, ScreenState.SCREEN_ON, ScreenState.SCREEN_ON, ScreenState.SCREEN_ON, ScreenState.SCREEN_ON, ScreenState.SCREEN_ON, ScreenState.SCREEN_ON},
                new RadioState[]{RadioState.RADIO_UP, RadioState.RADIO_UP, RadioState.RADIO_UP, RadioState.RADIO_UP, RadioState.RADIO_UP, RadioState.RADIO_UP, RadioState.RADIO_UP, RadioState.RADIO_UP},
                new boolean[]{true, true, true, false, false, false, false, false}, 2, 4, false, 4);
    }

    @LargeTest
    public void test_099_E2E_CacheInvalidate() {
        try {
            notifyRestForTestEnd(SUITE_NAME);
        } catch (Exception e) {
            logger.debug("Tests end REST notification failed");
            e.printStackTrace();
        }
    }

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
                logger.debug("Test failed due to " + ExceptionUtils.getStackTrace(assertionFailedError));
                isPassed = false;
                counts.add("Test failed due to AssertionFailedError in " + numberOfAttempts + " attempt");
                Thread.sleep(2 * 60 * 1000);
            }
        } while (!isPassed && numberOfAttempts < 3);

        assertTrue("The test was failed three times ", counts.size() != 3);
    }
}
