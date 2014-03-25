package com.seven.asimov.it.tests.caching.polling.overlapping.base;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.testcases.OverlappingPollingTestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.pollingTasks.StartPollTask;
import com.seven.asimov.it.utils.logcat.wrappers.StartPollWrapper;
import com.seven.asimov.it.utils.sms.SmsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class OverlappingPollingTests extends OverlappingPollingTestCase {

    private static final Logger logger = LoggerFactory.getLogger(OverlappingPollingTests.class.getSimpleName());

    private static int counter = 0;

    /**
     * Steps:
     * 1. Start periodic request:
     * Pattern [0,50,50,10]
     * Delay [45,45,45,45]
     * .
     * Expected results
     * 1. 1st - 3rd  requests should be MISSed
     * 2. 4th  requests should be PARKED
     * 3. In socket error should appear in 30 sec after 3rd request.
     * 4. 4th  request should be aborted by OC (verdict ABRT)
     *
     * @throws Throwable
     */
    public void test_001_OverlappingPolling() throws Throwable {

        String uri = createTestResourceUri("test_001", false);
        HttpRequest httpRequest = createRequest().setUri(uri)
                .addHeaderField("X-OC-Sleep", "45")
                .addHeaderField("X-OC-Stateless-Sleep", "true").getRequest();
        int DELAY = 50 * 1000;
        HttpResponse httpResponse;

        Thread parking = new Thread(new Parking(httpRequest, 120 * 1000, TIMEOUT, ParkedAction.ABRT));
        PrepareResourceUtil.prepareResource(uri, false);
        parking.start();
        try {
            httpResponse = checkMiss(httpRequest, ++counter);
            logSleeping(DELAY - httpResponse.getDuration());

            httpResponse = checkMiss(httpRequest, ++counter);
            logSleeping(DELAY - httpResponse.getDuration());
            sendRequest2(httpRequest, false, false, 30 * 1000, DEFAULT_READ_BUFFER, SLEEP_AFTER_HANDSHAKE, VERSION);
            while (!markerListen) {
                Thread.sleep(1000);
            }
            assertTrue("The request should be aborted", markerABRT);
        } finally {
            parking.interrupt();
            PrepareResourceUtil.invalidateResourceSafely(uri);
            counter = 0;
            markerListen = false;
            markerABRT = false;
        }
    }

    /**
     * Steps:
     * 1. Start periodic request:
     * Pattern [0,50,50,10]
     * Delay [45,45,45,45]
     * <p/>
     * Expected results:
     * 1. RLP should be detected after 3-rd request and polling should start after receiving response.
     * 2. 4th  requests should be HITed with delay = 45 sec.
     * 3. 5th  requests should be PARKED
     * 3. In socket error should appear in 30 sec after 4th request.
     * 4. 5th  request should be aborted by OC (verdict ABRT)
     *
     * @throws Throwable
     */
    public void test_002_OverlappingPolling() throws Throwable {

        String uri = createTestResourceUri("test_002", false);
        HttpRequest httpRequest = createRequest().setUri(uri)
                .addHeaderField("X-OC-Sleep", "45")
                .addHeaderField("X-OC-Stateless-Sleep", "true").getRequest();
        int DELAY = 50 * 1000;
        HttpResponse httpResponse;

        Thread parking = new Thread(new Parking(httpRequest, 160 * 1000, TIMEOUT, ParkedAction.ABRT));
        PrepareResourceUtil.prepareResource(uri, false);
        parking.start();
        try {
            httpResponse = checkMiss(httpRequest, ++counter);
            logSleeping(DELAY - httpResponse.getDuration());

            httpResponse = checkMiss(httpRequest, ++counter);
            logSleeping(DELAY - httpResponse.getDuration());

            httpResponse = checkMiss(httpRequest, ++counter);
            logSleeping(DELAY - httpResponse.getDuration());

            sendRequest2(httpRequest, false, false, 30 * 1000, DEFAULT_READ_BUFFER, SLEEP_AFTER_HANDSHAKE, VERSION);
            while (!markerListen) {
                Thread.sleep(1000);
            }
            assertTrue("The request should be aborted", markerABRT);
        } finally {
            parking.interrupt();
            PrepareResourceUtil.invalidateResourceSafely(uri);
            counter = 0;
            markerListen = false;
            markerABRT = false;
        }
    }

    /**
     * Steps:
     * 1. Start periodic request:
     * A test resource is needed for this test case that returns the same responses for 1st, 2nd  requests, another response for 3rd - 5th requests.
     * Pattern [0,50,50,20,30]
     * Delay [45,45,45,45,45]
     * Expected results:
     * 1. 1st ? 3rd  requests should be MISSed
     * 2. 4th  requests should be PARKED
     * 3. Response hash mismatch while developing a pattern should appear after 3rd response
     * 4. 4th  request should be aborted by OC (verdict ABRT)
     * 5. 5th request should be MISSed and polling should start after receiving response.
     *
     * @throws Throwable
     */
    public void test_003_OverlappingPolling() throws Throwable {
        String uri = createTestResourceUri("test_003", false);
        HttpRequest httpRequest = createRequest().setUri(uri)
                .addHeaderField("X-OC-Sleep", "45")
                .addHeaderField("X-OC-Stateless-Sleep", "true").getRequest();
        int DELAY = 50 * 1000;
        HttpResponse httpResponse;

        Thread parking = new Thread(new Parking(httpRequest, 120 * 1000, TIMEOUT, ParkedAction.ABRT));
        PrepareResourceUtil.prepareResource(uri, false);
        parking.start();
        try {
            httpResponse = checkMiss(httpRequest, ++counter);
            logSleeping(DELAY - httpResponse.getDuration());

            httpResponse = checkMiss(httpRequest, ++counter);
            logSleeping(DELAY - httpResponse.getDuration());

            PrepareResourceUtil.prepareResource(uri, true);

            httpResponse = checkMiss(httpRequest, ++counter);
            logSleeping(DELAY - httpResponse.getDuration());

            httpResponse = checkMiss(httpRequest, ++counter);
            logSleeping(DELAY - httpResponse.getDuration());

            while (!markerListen) {
                Thread.sleep(1000);
            }
            assertTrue("The request should be aborted", markerABRT);

        } finally {
            parking.interrupt();
            PrepareResourceUtil.invalidateResourceSafely(uri);
            counter = 0;
            counter = 0;
            markerListen = false;
            markerABRT = false;
        }
    }

    /**
     * Steps:
     * 1. Start periodic request:
     * Pattern [0,50,50,50,20]
     * Delay [45,45,45,45,45]
     * <p/>
     * Expected results:
     * 1. RLP should be detected after 3-rd request and polling should start after receiving response.
     * 2. 4th  requests should be HITed with hit delay = 45 sec.
     * 3. 5th  requests should be PARKED
     * 4. 5th  request should be HITed after receiving response by 4th request with hit delay = 0 sec.
     *
     * @throws Throwable
     */
    public void test_004_OverlappingPolling() throws Throwable {

        String uri = createTestResourceUri("test_004", false);
        HttpRequest httpRequest = createRequest().setUri(uri)
                .addHeaderField("X-OC-Sleep", "45")
                .addHeaderField("X-OC-Stateless-Sleep", "true").getRequest();

        int DELAY = 50 * 1000;
        HttpResponse httpResponse;

        Thread parking = new Thread(new Parking(httpRequest, 170 * 1000, TIMEOUT, ParkedAction.HIT));
        PrepareResourceUtil.prepareResource(uri, false);
        parking.start();
        try {
            httpResponse = checkMiss(httpRequest, ++counter);
            logSleeping(DELAY - httpResponse.getDuration());

            httpResponse = checkMiss(httpRequest, ++counter);
            logSleeping(DELAY - httpResponse.getDuration());

            httpResponse = checkMiss(httpRequest, ++counter);
            logSleeping(DELAY - httpResponse.getDuration());

            httpResponse = checkHit(httpRequest, ++counter);
            logSleeping(DELAY - httpResponse.getDuration());

        } finally {
            parking.interrupt();
            PrepareResourceUtil.invalidateResourceSafely(uri);
            counter = 0;
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * Steps:
     * 1. Start periodic request:
     * Pattern [0,50,50,20,35]
     * Delay [45,45,45,45,45]
     * <p/>
     * Expected results:
     * 1. 1st ? 3rd  requests should be MISSed
     * 2. 4th  request should be PARKED
     * 3. 3rd response should be saved into cache and 4th  request should be HITed at once after this with hit delay = 0
     *
     * @throws Throwable
     */
    public void test_005_OverlappingPolling() throws Throwable {

        String uri = createTestResourceUri("test_005", false);
        HttpRequest httpRequest = createRequest().setUri(uri)
                .addHeaderField("X-OC-Sleep", "45")
                .addHeaderField("X-OC-Stateless-Sleep", "true").getRequest();

        int DELAY = 50 * 1000;
        HttpResponse httpResponse;

        Thread parking = new Thread(new Parking(httpRequest, 120 * 1000, TIMEOUT, ParkedAction.HIT));
        PrepareResourceUtil.prepareResource(uri, false);
        parking.start();
        try {
            httpResponse = checkMiss(httpRequest, ++counter);
            logSleeping(DELAY - httpResponse.getDuration());

            httpResponse = checkMiss(httpRequest, ++counter);
            logSleeping(DELAY - httpResponse.getDuration());

            httpResponse = checkMiss(httpRequest, ++counter);
            logSleeping(DELAY - httpResponse.getDuration());

        } finally {
            parking.interrupt();
            counter = 0;
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * Steps:
     * 1. Start periodic request:
     * Pattern [0,50,50,10,5,5,30]
     * Delay [45,45,45,45,45,45,45]
     * <p/>
     * Expected results:
     * 1. 1st ? 3rd  requests should be MISSed
     * 2. 4th ? 6th  requests should be PARKED
     * 3. 3rd response should be saved into cache, polling (RLP) should start and 4th ? 6th  requests should be HITed at once after this with hit delay = 0
     * 4. 7th request should be HITed with hit delay = 45.
     *
     * @throws Throwable
     */
    public void test_006_OverlappingPolling() throws Throwable {

        String uri = createTestResourceUri("test_006", false);
        HttpRequest httpRequest = createRequest().setUri(uri)
                .addHeaderField("X-OC-Sleep", "45")
                .addHeaderField("X-OC-Stateless-Sleep", "true").getRequest();

        int DELAY = 50 * 1000;
        HttpResponse httpResponse;

        Thread parking1 = new Thread(new Parking(httpRequest, 110 * 1000, TIMEOUT, ParkedAction.HIT));
        Thread parking2 = new Thread(new Parking(httpRequest, 115 * 1000, TIMEOUT, ParkedAction.HIT));
        Thread parking3 = new Thread(new Parking(httpRequest, 120 * 1000, TIMEOUT, ParkedAction.HIT));

        PrepareResourceUtil.prepareResource(uri, false);
        parking1.start();
        parking2.start();
        parking3.start();
        try {
            httpResponse = checkMiss(httpRequest, ++counter);
            logSleeping(DELAY - httpResponse.getDuration());

            httpResponse = checkMiss(httpRequest, ++counter);
            logSleeping(DELAY - httpResponse.getDuration());

            httpResponse = checkMiss(httpRequest, ++counter);
            logSleeping(DELAY - httpResponse.getDuration());

            checkHit(httpRequest, ++counter);

        } finally {
            parking1.interrupt();
            parking2.interrupt();
            parking3.interrupt();
            counter = 0;
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * Steps:
     * 1. Start periodic request:
     * Pattern [0,70,70,5]
     * Delay [65,65,20,65]
     * A test resource is needed for this test case that returns the same responses for  1st -2nd  requests and another response for 3rd  ? 4th requests
     * <p/>
     * Expected results:
     * 1. LP should be detected after 2nd  request and polling should start after receiving response.
     * 2. 3rd  request should be HITed with hit delay = 65
     * 3. 4th  requests should be PARKED
     * 5. IWC should be received from server and 3rd request should be force HITed.
     * 6. 4th  request should be MISSed after receiving response for 3rd request, LP should be detected and polling should start after receiving response
     *
     * @throws Throwable
     */
    public void test_007_OverlappingPolling() throws Throwable {

        String uri = createTestResourceUri("test_007", false);
        HttpRequest httpRequest1 = createRequest().setUri(uri)
                .addHeaderField("X-OC-Sleep", "20")
                .addHeaderField("X-OC-Stateless-Sleep", "true").getRequest();

        HttpRequest httpRequest2 = createRequest().setUri(uri)
                .addHeaderField("X-OC-Sleep", "65")
                .addHeaderField("X-OC-Stateless-Sleep", "true").getRequest();


        int DELAY = 70 * 1000;
        HttpResponse httpResponse;

        Thread parking = new Thread(new Parking(httpRequest2, 145 * 1000, TIMEOUT, ParkedAction.MISS));
        final StartPollTask startPollTask = new StartPollTask();
        final LogcatUtil logcatUtil = new LogcatUtil(getContext(), startPollTask);

        PrepareResourceUtil.prepareResource(uri, false);
        logcatUtil.start();
        parking.start();
        try {
            httpResponse = checkMiss(httpRequest2, ++counter, VALID_RESPONSE);
            logSleeping(DELAY - httpResponse.getDuration());

            httpResponse = checkMiss(httpRequest2, ++counter, VALID_RESPONSE);

            PrepareResourceUtil.prepareResource(uri, true);
            logSleeping(DELAY - httpResponse.getDuration());

            if (startPollTask.getLogEntries().size() > 0) {
                StartPollWrapper startPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
                SmsUtil.sendInvalidationSms(getContext(), Integer.parseInt(startPoll.getSubscriptionId()),
                        SmsUtil.InvalidationType.INVALIDATE_WITH_CACHE.byteVal);

                checkHit(httpRequest1, ++counter, INVALIDATED_RESPONSE);
            } else {
                logger.warn("IWC wasn't received from server and we didn't cat record with START_POLL, so test should be executed with alternative logic");
                parking.interrupt();
                checkMiss(httpRequest1, ++counter);
            }

        } finally {
            parking.interrupt();
            counter = 0;
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * Steps:
     * 1. Start periodic request:
     * Pattern [0,70,70,5,1]
     * A test resource is needed for this test case that returns the same responses for  1st -2nd  requests and another response for 3rd  ? 5th requests
     * Delay [65,65,20,65,0]
     * <p/>
     * Expected results:
     * 1. LP should be detected after 2nd  request and polling should start after receiving response.
     * 2. 3rd  request should be HITed with hit delay = 65
     * 3. 4th and 5th  requests should be PARKED
     * 4. IWC should be received from server and 3rd request should be force HITed.
     * 5. 4th  request should be MISSed after receiving response for 3rd request, LP should be detected and polling should start after receiving response
     * 6. 5th  request should be HITed after receiving response by 4th request with hit delay = 0 sec.
     *
     * @throws Throwable
     */
    public void test_008_OverlappingPolling() throws Throwable {
        String uri = createTestResourceUri("test_008", false);
        int[] D = {65, 65, 20, 65, 0};
        int DELAY = 70 * 1000;
        HttpResponse httpResponse;

        List<HttpRequest> requests = new ArrayList<HttpRequest>();
        for (int i = 0; i < D.length; i++) {
            requests.add(createRequest().setUri(uri)
                    .addHeaderField("X-OC-Sleep", D[i] + "")
                    .addHeaderField("X-OC-Stateless-Sleep", "true").getRequest());
        }

        Thread parking1 = new Thread(new Parking(requests.get(3), 145 * 1000, TIMEOUT, ParkedAction.MISS));
        Thread parking2 = new Thread(new Parking(requests.get(4), 146 * 1000, TIMEOUT, ParkedAction.HIT));
        final StartPollTask startPollTask = new StartPollTask();
        final LogcatUtil logcatUtil = new LogcatUtil(getContext(), startPollTask);

        PrepareResourceUtil.prepareResource(uri, false);
        logcatUtil.start();
        parking1.start();
        parking2.start();
        try {
            httpResponse = checkMiss(requests.get(0), ++counter, VALID_RESPONSE);
            logSleeping(DELAY - httpResponse.getDuration());

            httpResponse = checkMiss(requests.get(1), ++counter, VALID_RESPONSE);

            PrepareResourceUtil.prepareResource(uri, true);
            logSleeping(DELAY - httpResponse.getDuration());

            logger.info("SubscriptionId found - " + startPollTask.getLogEntries().toString());

            if (startPollTask.getLogEntries().size() > 0) {
                StartPollWrapper startPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
                SmsUtil.sendInvalidationSms(getContext(), Integer.parseInt(startPoll.getSubscriptionId()),
                        SmsUtil.InvalidationType.INVALIDATE_WITH_CACHE.byteVal);

                httpResponse = checkHit(requests.get(2), ++counter, INVALIDATED_RESPONSE);
                logSleeping(DELAY - httpResponse.getDuration());
            } else {
                logger.warn("IWC wasn't received from server and we didn't cat record with START_POLL, so test should be executed with alternative logic");
                parking1.interrupt();
                parking2.interrupt();
                checkMiss(requests.get(2), ++counter);
            }

        } finally {
            parking1.interrupt();
            parking2.interrupt();
            counter = 0;
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * Steps:
     * 1. Start periodic request:
     * A test resource is needed for this test case that returns the same responses for  1st -2nd  requests and another response for 3rd  ? 5th requests and socket
     * should be closed in 75 sec after 4th request
     * Pattern [0,70,70,5,1]
     * Delay [65,65,20,65,0]
     * <p/>
     * Expected results:
     * 1. LP should be detected after 2nd  request and polling should start after receiving response.
     * 2. 3rd  request should be HITed with hit delay = 65
     * 3. 4th and 5th  requests should be PARKED
     * 4. IWC should be received from server and 3rd request should be force HITed.
     * 5. 4th  request should be MISSed after receiving response for 3rd request but In socket error should appear.
     * 6. 5th   request should be aborted by OC (verdict ABRT)
     *
     * @throws Throwable
     */
    public void test_009_OverlappingPolling() throws Throwable {

        String uri = createTestResourceUri("test_009", false);

        int[] D = {65, 65, 20, 65, 0};

        List<HttpRequest> requests = new ArrayList<HttpRequest>();
        for (int i = 0; i < D.length; i++) {
            requests.add(createRequest().setUri(uri)
                    .addHeaderField("X-OC-Sleep", D[i] + "")
                    .addHeaderField("X-OC-Stateless-Sleep", "true").getRequest());
        }

        int DELAY = 70 * 1000;
        HttpResponse httpResponse;

        Thread parking1 = new Thread(new Parking(requests.get(3), 145 * 1000, 75 * 1000, ParkedAction.MISS));
        Thread parking2 = new Thread(new Parking(requests.get(4), 146 * 1000, TIMEOUT, ParkedAction.ABRT));

        PrepareResourceUtil.prepareResource(uri, false);
        parking1.start();
        parking2.start();
        try {

            httpResponse = checkMiss(requests.get(0), ++counter, VALID_RESPONSE);
            logSleeping(DELAY - httpResponse.getDuration());

            httpResponse = checkMiss(requests.get(1), ++counter, VALID_RESPONSE);
            logSleeping(DELAY - httpResponse.getDuration());

            PrepareResourceUtil.prepareResource(uri, true);

            httpResponse = checkHit(requests.get(2), ++counter);
            if(httpResponse.getD() == 0) {
                parking1.interrupt();
                parking2.interrupt();
                logger.info("HIT duration is not enogh to park some requests");
            }

        } finally {
            parking1.interrupt();
            parking2.interrupt();
            counter = 0;
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * Steps:
     * 1. Start periodic request:
     * A test resource is needed for this test case that returns the same response for all requests
     * Pattern [0,70,2,68,2,68,2]
     * Delay [65,65,65,65,65,65,65]
     * <p/>
     * Expected results:
     * 1. 1st ? 2nd  requests should be MISSed
     * 2. 3rd  request should be PARKED
     * 3. 2nd  response should be saved into cache and 3rd  request should be HITed at once after this with hit delay = 0
     * 4. 4th request should be HITed with hit delay = 65.
     * 5. 5th  request should be PARKED at first and HITed with hit delay = 0 after serving from cache of 4th request
     * 6. 6th request should be HITed with hit delay = 65.
     * 7. 7th  request should be PARKED at first and HITed with hit delay = 0 after serving from cache of 5th request
     *
     * @throws Throwable
     */
    public void test_010_OverlappingPolling() throws Throwable {

        String uri = createTestResourceUri("test_010", false);
        HttpRequest httpRequest = createRequest().setUri(uri)
                .addHeaderField("X-OC-Sleep", "65")
                .addHeaderField("X-OC-Stateless-Sleep", "true").getRequest();

        int DELAY = 70 * 1000;
        HttpResponse httpResponse;

        Thread parking1 = new Thread(new Parking(httpRequest, 72 * 1000, TIMEOUT, ParkedAction.HIT));
        Thread parking2 = new Thread(new Parking(httpRequest, 142 * 1000, TIMEOUT, ParkedAction.HIT));
        Thread parking3 = new Thread(new Parking(httpRequest, 212 * 1000, TIMEOUT, ParkedAction.HIT));

        PrepareResourceUtil.prepareResource(uri, false);
        parking1.start();
        parking2.start();
        parking3.start();
        try {

            httpResponse = checkMiss(httpRequest, ++counter, VALID_RESPONSE);
            logSleeping(DELAY - httpResponse.getDuration());

            httpResponse = checkMiss(httpRequest, ++counter, VALID_RESPONSE);
            logSleeping(DELAY - httpResponse.getDuration());

            httpResponse = checkHit(httpRequest, ++counter, VALID_RESPONSE);
            logSleeping(DELAY - httpResponse.getDuration());

            checkHit(httpRequest, ++counter, VALID_RESPONSE);

        } finally {
            parking1.interrupt();
            parking2.interrupt();
            parking3.interrupt();
            counter = 0;
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }
}
