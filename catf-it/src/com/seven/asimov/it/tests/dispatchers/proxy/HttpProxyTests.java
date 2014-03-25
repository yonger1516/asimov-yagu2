package com.seven.asimov.it.tests.dispatchers.proxy;


import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.pollingTasks.RMPApplicabilityTask;
import com.seven.asimov.it.utils.logcat.tasks.pollingTasks.StartPollParamsTask;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import junit.framework.AssertionFailedError;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class HttpProxyTests extends TcpDumpTestCase {
    private static final Logger logger = LoggerFactory.getLogger(HttpProxyTests.class.getSimpleName());
    private static final String HTTP_PATH = "@asimov@http";

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
                logger.info("Test failed due to " + ExceptionUtils.getStackTrace(assertionFailedError));
                isPassed = false;
                counts.add("Test failed due to AssertionFailedError in " + numberOfAttempts + " attempt");
                Thread.sleep(2 * 60 * 1000);
            }
        } while (!isPassed && numberOfAttempts < 3);

        assertTrue("The test was failed three times ", counts.size() != 3);
    }

    /**
     * 1. Create resource on testrunner that should return the same response for all requests
     * 2. Configure policy with such parametres:
     * asimov@http@blacklist = tln-dev-testrunner1.7sys.eu
     * 3. Verify that polling does not start after 3-rd request.
     */
    public void testHttpProxyTests_002() throws Exception {
        final String PROPERTY_NAME = "blacklist";
        final String PROPERTY_VALUE = TEST_RESOURCE_HOST;
        final String URI = createTestResourceUri("testHttpProxyTests_002");
        final int REQUEST_INTERVAL = 65 * 1000;
        final HttpRequest request = createRequest().setUri(URI)
                .setMethod("GET").getRequest();
        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(PROPERTY_NAME, PROPERTY_VALUE, HTTP_PATH, true)});

            PrepareResourceUtil.prepareResource(URI, false);

            checkMiss(request, 1, REQUEST_INTERVAL);
            checkMiss(request, 2, REQUEST_INTERVAL);
            checkMiss(request, 3, REQUEST_INTERVAL);
            checkMiss(request, 4, REQUEST_INTERVAL);

        } finally {
            PrepareResourceUtil.invalidateResourceSafely(URI);
            PMSUtil.cleanPaths(new String[]{HTTP_PATH});
        }
    }


    /**
     * TODO: Ignore - periodic_requests_count policy is deprecated<br/>
     * 1. Create resource on testrunner that should return the same response for all requests
     * 2. Configure policy with such parametres:
     * asimov@http@periodic_requests_count = 4
     * 3. Verify that polling starts after 4-th request.
     * 4. 5th request expected to be served from cash with verdict HIT.
     */
    @Ignore
    public void testHttpProxyTests_003() throws Exception {
        final String PROPERTY_NAME = "periodic_requests_count";
        final String PROPERTY_VALUE = "4";
        final String URI = createTestResourceUri("testHttpProxyTests_003");
        final int REQUEST_INTERVAL = 65 * 1000;
        final HttpRequest request = createRequest().setUri(URI)
                .setMethod("GET").getRequest();
        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(PROPERTY_NAME, PROPERTY_VALUE, HTTP_PATH, true)});

            PrepareResourceUtil.prepareResource(URI, false);

            checkMiss(request, 1, REQUEST_INTERVAL);
            checkMiss(request, 2, REQUEST_INTERVAL);
            checkMiss(request, 3, REQUEST_INTERVAL);
            checkMiss(request, 4, REQUEST_INTERVAL);

            checkHit(request, 5, REQUEST_INTERVAL);

        } finally {
            PrepareResourceUtil.invalidateResourceSafely(URI);
            PMSUtil.cleanPaths(new String[]{HTTP_PATH});
        }
    }

    /**
     * 1. Create resource on testrunner that should return the same response for all requests
     * 2. Configure policy with such parametres:
     * asimov@http@rapid_poll_and_pattern_interval_boundary_in_seconds = 50
     * 3. Send 4 requests every 55 sec.
     * 4. Verify that regular polling starts after 3-th request.
     * 5. 4th request expected to be served from cash with verdict HIT.
     */
    public void testHttpProxyTests_004() throws Exception {
        final StartPollParamsTask sppTask = new StartPollParamsTask();
        final LogcatUtil logcat = new LogcatUtil(getContext(), sppTask);
        final String PROPERTY_NAME = "rapid_poll_and_pattern_interval_boundary_in_seconds";
        final String PROPERTY_VALUE = "50";
        final String URI = createTestResourceUri("testHttpProxyTests_004");
        final int REQUEST_INTERVAL = 55 * 1000;
        final HttpRequest request = createRequest().setUri(URI)
                .setMethod("GET").getRequest();
        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(PROPERTY_NAME, PROPERTY_VALUE, HTTP_PATH, true)});

            PrepareResourceUtil.prepareResource(URI, false);

            logcat.start();
            checkMiss(request, 1, REQUEST_INTERVAL);
            checkMiss(request, 2, REQUEST_INTERVAL);
            checkMiss(request, 3, REQUEST_INTERVAL);

            checkHit(request, 4, REQUEST_INTERVAL);
            logcat.stop();

            assertTrue("Start poll parameters log entry not found", !sppTask.getLogEntries().isEmpty());
            assertTrue("Regular polling expected class==1, RP RI==0, but found class: " +
                    sppTask.getLogEntries().get(0).getPollClass() + ", RP RI: " +
                    sppTask.getLogEntries().get(0).getRpRi(),
                    (sppTask.getLogEntries().get(0).getPollClass() == 1) &&
                            (sppTask.getLogEntries().get(0).getRpRi() == 0));
        } finally {
            logcat.stop();
            PrepareResourceUtil.invalidateResourceSafely(URI);
            PMSUtil.cleanPaths(new String[]{HTTP_PATH});
        }
    }

    /**
     * 1. Create resource on testrunner that should return the same response for all requests
     * 2. Configure policy with such parametres:
     * asimov@http@rapid_poll_and_pattern_interval_boundary_in_seconds = 50
     * Send 4 requests every 45 sec.
     * 2. Verify that RMP starts after 3-th request.
     * 3. 4th request expected to be served from cash with verdict HIT.
     */
    public void testHttpProxyTests_005() throws Exception {
        final StartPollParamsTask sppTask = new StartPollParamsTask();
        final LogcatUtil logcat = new LogcatUtil(getContext(), sppTask);
        final String PROPERTY_NAME = "rapid_poll_and_pattern_interval_boundary_in_seconds";
        final String PROPERTY_VALUE = "50";
        final String URI = createTestResourceUri("testHttpProxyTests_005");
        final int REQUEST_INTERVAL = 45 * 1000;
        final HttpRequest request = createRequest().setUri(URI)
                .setMethod("GET").getRequest();
        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(PROPERTY_NAME, PROPERTY_VALUE, HTTP_PATH, true)});

            PrepareResourceUtil.prepareResource(URI, false);

            logcat.start();
            checkMiss(request, 1, REQUEST_INTERVAL);
            checkMiss(request, 2, REQUEST_INTERVAL);
            checkMiss(request, 3, REQUEST_INTERVAL);

            checkHit(request, 4, REQUEST_INTERVAL);
            logcat.stop();

            assertTrue("Start poll parameters log entry not found", !sppTask.getLogEntries().isEmpty());
            assertTrue("RMP polling expected class==1, RP RI!=0, but found class: " +
                    sppTask.getLogEntries().get(0).getPollClass() + ", RP RI: " +
                    sppTask.getLogEntries().get(0).getRpRi(),
                    (sppTask.getLogEntries().get(0).getPollClass() == 1) &&
                            (sppTask.getLogEntries().get(0).getRpRi() != 0));
        } finally {
            logcat.stop();
            PrepareResourceUtil.invalidateResourceSafely(URI);
            PMSUtil.cleanPaths(new String[]{HTTP_PATH});
        }
    }


    /**
     * 1. Create resource on testrunner that should return the same response for all requests
     * 2. Configure policy with such parametres:
     * asimov@http@rapid_polling_num_requests = 4
     * 3. Send 5 requests every 50 sec.
     * 4. Verify that RMP starts after 4-th request.
     * 5. 5th request expected to be served from cash with verdict HIT.
     */
    public void testHttpProxyTests_006() throws Exception {
        final StartPollParamsTask sppTask = new StartPollParamsTask();
        final LogcatUtil logcat = new LogcatUtil(getContext(), sppTask);
        final String PROPERTY_NAME = "rapid_polling_num_requests";
        final String PROPERTY_VALUE = "4";
        final String URI = createTestResourceUri("testHttpProxyTests_006");
        final int REQUEST_INTERVAL = 50 * 1000;
        final HttpRequest request = createRequest().setUri(URI)
                .setMethod("GET").getRequest();
        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(PROPERTY_NAME, PROPERTY_VALUE, HTTP_PATH, true)});

            PrepareResourceUtil.prepareResource(URI, false);
            logcat.start();
            checkMiss(request, 1, REQUEST_INTERVAL);
            checkMiss(request, 2, REQUEST_INTERVAL);
            checkMiss(request, 3, REQUEST_INTERVAL);
            checkMiss(request, 4, REQUEST_INTERVAL);

            checkHit(request, 5, REQUEST_INTERVAL);
            logcat.stop();

            assertTrue("Start poll parameters log entry not found", !sppTask.getLogEntries().isEmpty());
            assertTrue("RMP polling expected class==1, RP RI!=0, but found class: " +
                    sppTask.getLogEntries().get(0).getPollClass() + ", RP RI: " +
                    sppTask.getLogEntries().get(0).getRpRi(),
                    (sppTask.getLogEntries().get(0).getPollClass() == 1) &&
                            (sppTask.getLogEntries().get(0).getRpRi() != 0));

        } finally {
            logcat.stop();
            PrepareResourceUtil.invalidateResourceSafely(URI);
            PMSUtil.cleanPaths(new String[]{HTTP_PATH});
        }
    }

    /**
     * 1. Create resource on testrunner that should return the same response for all requests
     * 2. Configure policy with such parametres:
     * asimov@http@periodic_window_size_in_percent = 30
     * 3. Send 5 requests with such intervals: 65, 65, 65, 47, 40 sec.
     * 4. Verify that regular polling starts after 3-th request.
     * 5. 4th request expected to be served from cash with verdict HIT.
     * 6. 5th request expected to be served from network with verdict MISS.
     */
    public void testHttpProxyTests_007() throws Exception {
        final StartPollParamsTask sppTask = new StartPollParamsTask();
        final LogcatUtil logcat = new LogcatUtil(getContext(), sppTask);
        final String PROPERTY_NAME = "periodic_window_size_in_percent";
        final String PROPERTY_VALUE = "30";
        final String URI = createTestResourceUri("testHttpProxyTests_007");
        final int REQUEST_INTERVAL = 65 * 1000;
        final int REQUEST4_INTERVAL = 47 * 1000;
        final int REQUEST5_INTERVAL = 40 * 1000;
        final HttpRequest request = createRequest().setUri(URI)
                .setMethod("GET").getRequest();
        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(PROPERTY_NAME, PROPERTY_VALUE, HTTP_PATH, true)});

            PrepareResourceUtil.prepareResource(URI, false);
            logcat.start();
            checkMiss(request, 1, REQUEST_INTERVAL);
            checkMiss(request, 2, REQUEST_INTERVAL);
            checkMiss(request, 3, REQUEST_INTERVAL);

            checkHit(request, 4, REQUEST4_INTERVAL);

            checkMiss(request, 5, REQUEST5_INTERVAL);
            logcat.stop();
            assertTrue("Start poll parameters log entry not found", !sppTask.getLogEntries().isEmpty());
            assertTrue("Regular polling expected class==1, RP RI==0, but found class: " +
                    sppTask.getLogEntries().get(0).getPollClass() + ", RP RI: " +
                    sppTask.getLogEntries().get(0).getRpRi(),
                    (sppTask.getLogEntries().get(0).getPollClass() == 1) &&
                            (sppTask.getLogEntries().get(0).getRpRi() == 0));
        } finally {
            logcat.stop();
            PrepareResourceUtil.invalidateResourceSafely(URI);
            PMSUtil.cleanPaths(new String[]{HTTP_PATH});
        }
    }


    /**
     * 1. Set such polices on PMS server: asimov@http@periodic_min_d_long_interval_in_seconds=50
     * 2. Create resource on testrunner that should return the same response for all requests with delay 56 sec.
     * 3. After 2nd response long poll should be detected.
     */
    public void testHttpProxyTests_008() throws Exception {
        final StartPollParamsTask sppTask = new StartPollParamsTask();
        final LogcatUtil logcat = new LogcatUtil(getContext(), sppTask);
        final String PROPERTY_NAME = "periodic_min_d_long_interval_in_seconds";
        final String PROPERTY_VALUE = "50";
        final long RESPONSE_DELAY = 56;
        final String URI = createTestResourceUri("testHttpProxyTests_008");
        final int REQUEST_INTERVAL = 65 * 1000;
        final HttpRequest request = createRequest().setUri(URI)
                .setMethod("GET").getRequest();
        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(PROPERTY_NAME, PROPERTY_VALUE, HTTP_PATH, true)});

            PrepareResourceUtil.prepareResource(URI, false, RESPONSE_DELAY);
            logcat.start();
            checkMiss(request, 1, REQUEST_INTERVAL);
            checkMiss(request, 2, REQUEST_INTERVAL);

            checkHit(request, 3, REQUEST_INTERVAL);
            logcat.stop();
            assertTrue("Start poll parameters log entry not found", !sppTask.getLogEntries().isEmpty());
            assertTrue("Long polling expected class==4, RP RI==0, but found class: " +
                    sppTask.getLogEntries().get(0).getPollClass() + ", RP RI: " +
                    sppTask.getLogEntries().get(0).getRpRi(),
                    (sppTask.getLogEntries().get(0).getPollClass() == 4) &&
                            (sppTask.getLogEntries().get(0).getRpRi() == 0));

        } finally {
            logcat.stop();
            PrepareResourceUtil.invalidateResourceSafely(URI);
            PMSUtil.cleanPaths(new String[]{HTTP_PATH});
        }
    }


    /**
     * 1. Set such polices on PMS server:
     * Asimov@http@rapid_polling_ttl_multiplier=6
     * asimov@http@rapid_polling_min_validity_period_in_seconds=90
     * asimov@http@rapid_polling_max_validity_period_in_seconds=180
     * 2. Create resource on testrunner that should return the same response for all requests
     * 3. Send 4 requests every 20 sec.
     * 3. Verify that RMP starts after 3-th request.
     * 4. 4th request expected to be served from cash with verdict HIT and TTL=120.
     */
    public void testHttpProxyTests_009() throws Exception {
        final StartPollParamsTask sppTask = new StartPollParamsTask();
        final RMPApplicabilityTask rmpApplTask = new RMPApplicabilityTask();
        final LogcatUtil logcat = new LogcatUtil(getContext(), sppTask, rmpApplTask);
        final String PROPERTY1_NAME = "rapid_polling_ttl_multiplier";
        final String PROPERTY1_VALUE = "6";
        final String PROPERTY2_NAME = "rapid_polling_min_validity_period_in_seconds";
        final String PROPERTY2_VALUE = "90";
        final String PROPERTY3_NAME = "rapid_polling_max_validity_period_in_seconds";
        final String PROPERTY3_VALUE = "180";
        final String URI = createTestResourceUri("testHttpProxyTests_009");
        final int REQUEST_INTERVAL = 20 * 1000;
        final Long RMP_TTL_EXPECTED = 120L;
        final HttpRequest request = createRequest().setUri(URI)
                .setMethod("GET").getRequest();
        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(PROPERTY1_NAME, PROPERTY1_VALUE, HTTP_PATH, true),
                    new Policy(PROPERTY2_NAME, PROPERTY2_VALUE, HTTP_PATH, true),
                    new Policy(PROPERTY3_NAME, PROPERTY3_VALUE, HTTP_PATH, true)});

            PrepareResourceUtil.prepareResource(URI, false);
            logcat.start();
            checkMiss(request, 1, REQUEST_INTERVAL);
            checkMiss(request, 2, REQUEST_INTERVAL);
            checkMiss(request, 3, REQUEST_INTERVAL);

            checkHit(request, 4, REQUEST_INTERVAL);
            logcat.stop();
            assertTrue("Start poll parameters log entry not found", !sppTask.getLogEntries().isEmpty());
            assertTrue("RMP polling expected class==1, RP RI!=0, but found class: " +
                    sppTask.getLogEntries().get(0).getPollClass() + ", RP RI: " +
                    sppTask.getLogEntries().get(0).getRpRi(),
                    (sppTask.getLogEntries().get(0).getPollClass() == 1) &&
                            (sppTask.getLogEntries().get(0).getRpRi() != 0));
            assertTrue("'Checking RMP applicability' log entry not found", !rmpApplTask.getLogEntries().isEmpty());

            assertTrue("Expected RMP TTL: " + RMP_TTL_EXPECTED + " found TTL: " +
                    rmpApplTask.getLogEntries().get(0).getTTL(),
                    rmpApplTask.getLogEntries().get(0).getTTL() == RMP_TTL_EXPECTED);
        } finally {
            logcat.stop();
            PrepareResourceUtil.invalidateResourceSafely(URI);
            PMSUtil.cleanPaths(new String[]{HTTP_PATH});
        }
    }


    /**
     * 1) Set such polices on PMS server:
     * Asimov@http@rapid_polling_ttl_multiplier=3
     * asimov@http@rapid_polling_min_validity_period_in_seconds=90
     * asimov@http@rapid_polling_max_validity_period_in_seconds=180
     * 3. Create resource on testrunner that should return the same response for all requests
     * 4. Send 4 requests every 20 sec.
     * 3. Verify that RMP starts after 3-th request.
     * 5. 4th request expected to be served from cash with verdict HIT and TTL=90.
     */
    public void testHttpProxyTests_010() throws Exception {
        final StartPollParamsTask sppTask = new StartPollParamsTask();
        final RMPApplicabilityTask rmpApplTask = new RMPApplicabilityTask();
        final LogcatUtil logcat = new LogcatUtil(getContext(), sppTask, rmpApplTask);
        final String PROPERTY1_NAME = "rapid_polling_ttl_multiplier";
        final String PROPERTY1_VALUE = "3";
        final String PROPERTY2_NAME = "rapid_polling_min_validity_period_in_seconds";
        final String PROPERTY2_VALUE = "90";
        final String PROPERTY3_NAME = "rapid_polling_max_validity_period_in_seconds";
        final String PROPERTY3_VALUE = "180";
        final String URI = createTestResourceUri("testHttpProxyTests_010");
        final int REQUEST_INTERVAL = 20 * 1000;
        final Long RMP_TTL_EXPECTED = 90L;
        final HttpRequest request = createRequest().setUri(URI)
                .setMethod("GET").getRequest();
        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(PROPERTY1_NAME, PROPERTY1_VALUE, HTTP_PATH, true),
                    new Policy(PROPERTY2_NAME, PROPERTY2_VALUE, HTTP_PATH, true),
                    new Policy(PROPERTY3_NAME, PROPERTY3_VALUE, HTTP_PATH, true)});

            PrepareResourceUtil.prepareResource(URI, false);
            logcat.start();
            checkMiss(request, 1, REQUEST_INTERVAL);
            checkMiss(request, 2, REQUEST_INTERVAL);
            checkMiss(request, 3, REQUEST_INTERVAL);

            checkHit(request, 4, REQUEST_INTERVAL);
            logcat.stop();
            assertTrue("Start poll parameters log entry not found", !sppTask.getLogEntries().isEmpty());
            assertTrue("RMP polling expected class==1, RP RI!=0, but found class: " +
                    sppTask.getLogEntries().get(0).getPollClass() + ", RP RI: " +
                    sppTask.getLogEntries().get(0).getRpRi(),
                    (sppTask.getLogEntries().get(0).getPollClass() == 1) &&
                            (sppTask.getLogEntries().get(0).getRpRi() != 0));

            assertTrue("'Checking RMP applicability' log entry not found", !rmpApplTask.getLogEntries().isEmpty());
            assertTrue("Expected RMP TTL: " + RMP_TTL_EXPECTED + " found TTL: " +
                    rmpApplTask.getLogEntries().get(0).getTTL(),
                    rmpApplTask.getLogEntries().get(0).getTTL() == RMP_TTL_EXPECTED);
        } finally {
            logcat.stop();
            PrepareResourceUtil.invalidateResourceSafely(URI);
            PMSUtil.cleanPaths(new String[]{HTTP_PATH});
        }
    }

    /**
     * 1) Set such polices on PMS server:
     * Asimov@http@rapid_polling_ttl_multiplier=6
     * asimov@http@rapid_polling_min_validity_period_in_seconds=90
     * asimov@http@rapid_polling_max_validity_period_in_seconds=180
     * 2. Create resource on testrunner that should return the same response for all requests
     * 3. Send 4 requests every 40 sec.
     * 4. Verify that RMP starts after 3-th request .
     * 5. 4th request expected to be served from cash with verdict HIT and TTL=180.
     */
    public void testHttpProxyTests_011() throws Exception {
        final StartPollParamsTask sppTask = new StartPollParamsTask();
        final RMPApplicabilityTask rmpApplTask = new RMPApplicabilityTask();
        final LogcatUtil logcat = new LogcatUtil(getContext(), sppTask, rmpApplTask);
        final String PROPERTY1_NAME = "rapid_polling_ttl_multiplier";
        final String PROPERTY1_VALUE = "6";
        final String PROPERTY2_NAME = "rapid_polling_min_validity_period_in_seconds";
        final String PROPERTY2_VALUE = "90";
        final String PROPERTY3_NAME = "rapid_polling_max_validity_period_in_seconds";
        final String PROPERTY3_VALUE = "180";
        final String URI = createTestResourceUri("testHttpProxyTests_011");
        final int REQUEST_INTERVAL = 40 * 1000;
        final Long RMP_TTL_EXPECTED = 180L;
        final HttpRequest request = createRequest().setUri(URI)
                .setMethod("GET").getRequest();
        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(PROPERTY1_NAME, PROPERTY1_VALUE, HTTP_PATH, true),
                    new Policy(PROPERTY2_NAME, PROPERTY2_VALUE, HTTP_PATH, true),
                    new Policy(PROPERTY3_NAME, PROPERTY3_VALUE, HTTP_PATH, true)});

            PrepareResourceUtil.prepareResource(URI, false);
            logcat.start();
            checkMiss(request, 1, REQUEST_INTERVAL);
            checkMiss(request, 2, REQUEST_INTERVAL);
            checkMiss(request, 3, REQUEST_INTERVAL);

            checkHit(request, 4, REQUEST_INTERVAL);
            logcat.stop();
            assertTrue("Start poll parameters log entry not found", !sppTask.getLogEntries().isEmpty());
            assertTrue("RMP polling expected class==1, RP RI!=0, but found class: " +
                    sppTask.getLogEntries().get(0).getPollClass() + ", RP RI: " +
                    sppTask.getLogEntries().get(0).getRpRi(),
                    (sppTask.getLogEntries().get(0).getPollClass() == 1) &&
                            (sppTask.getLogEntries().get(0).getRpRi() != 0));

            assertTrue("'Checking RMP applicability' log entry not found", !rmpApplTask.getLogEntries().isEmpty());
            assertTrue("Expected RMP TTL: " + RMP_TTL_EXPECTED + " found TTL: " +
                    rmpApplTask.getLogEntries().get(0).getTTL(),
                    rmpApplTask.getLogEntries().get(0).getTTL() == RMP_TTL_EXPECTED);
        } finally {
            logcat.stop();
            PrepareResourceUtil.invalidateResourceSafely(URI);
            PMSUtil.cleanPaths(new String[]{HTTP_PATH});
        }
    }
}


