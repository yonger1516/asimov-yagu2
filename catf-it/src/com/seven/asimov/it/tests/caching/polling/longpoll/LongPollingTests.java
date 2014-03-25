package com.seven.asimov.it.tests.caching.polling.longpoll;

import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.SmallTest;
import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.TestCaseThread;
import com.seven.asimov.it.testcases.LongPollingTestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.date.DateUtil;
import org.apache.http.HttpStatus;

import java.util.Date;

public class LongPollingTests extends LongPollingTestCase {

    /**
     * 1Req=======>A[MISS]
     * |          /    2Req=======>A[MISS]
     * |          /    |          /    3Req=======>A[HIT]
     * |          /    |          /    |          /    4Req=======>A[HIT]
     * |          /    |          /    |          /    |          /
     * |          /    |          /    |          /    |          /
     * ------------------------------------------------------------------------>
     */
    @LargeTest
    public void test_001_LongPolling() throws Throwable {

        final long SLEEP = 90 * 1000;

        final String RESOURCE_URI = "asimov_long_pol_000";

        final String uri = createTestResourceUri(RESOURCE_URI);

        PrepareResourceUtil.prepareResource(uri, false);

        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Sleep", "61").getRequest();

        try {
            int requestId = 1;
            HttpResponse response;

            for (int i = 0; i < 2; i++) {
                // this response shall be returned from network
                response = checkMiss(request, requestId++, HttpStatus.SC_OK, VALID_RESPONSE);
                logSleeping(SLEEP - response.getDuration());
            }

            for (int i = 0; i < 2; i++) {
                // this response shall be returned from cache
                response = checkHit(request, requestId++, HttpStatus.SC_OK, VALID_RESPONSE);
                logSleeping(SLEEP - response.getDuration());
            }
        } finally {
            // invalidate resource to stop server polling
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }


    /**
     * 1Req1========>A[MISS]- - - - - -|
     * |            /  2Req2========>A[MISS]- - - - - -|
     * |            /  |            /  3Req1========>A[MISS]- - - - - -|
     * |            /  |            /  |            /  4Req2========>A[MISS]- - - - - -|
     * |            /  |            /  |            /  |            /  5Req1========>A[HIT]
     * |            /  |            /  |            /  |            /  |            /  6Req2========>A[HIT]
     * |            /  |            /  |            /  |            /  |            /  |            /
     * ------------------------------------------------------------------------------------------------------------------------------>
     */
    @LargeTest
    public void test_002_TwoThreadsLongPolling() throws Throwable {

        final int SLEEP = 50 * 1000;
        final int RESPONSE_TIMEOUT = 110 * 1000;

        String res1 = "asimov_long_pol_001";
        String res2 = "asimov_long_pol_002";

        String uri1 = createTestResourceUri(res1);
        String uri2 = createTestResourceUri(res2);

        PrepareResourceUtil.prepareResource(uri1, false);
        PrepareResourceUtil.prepareResource(uri2, false);

        HttpRequest request1 = createRequest().setUri(uri1).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Sleep", "67").getRequest();
        HttpRequest request2 = createRequest().setUri(uri2).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Sleep", "67").getRequest();

        try {
            int requestId = 0;
            // R0.1
            checkMiss(request1, ++requestId, VALID_RESPONSE, true, RESPONSE_TIMEOUT);
            logSleeping(SLEEP);
            // R0.2
            checkMiss(request2, ++requestId, VALID_RESPONSE, true, RESPONSE_TIMEOUT);
            logSleeping(SLEEP);
            // R0.3
            checkMiss(request1, ++requestId, VALID_RESPONSE, true, RESPONSE_TIMEOUT);
            logSleeping(SLEEP);
            // R0.4
            checkMiss(request2, ++requestId, VALID_RESPONSE, true, RESPONSE_TIMEOUT);
            logSleeping(SLEEP);
            // R0.5
            checkHit(request1, ++requestId, VALID_RESPONSE, true, RESPONSE_TIMEOUT);
            logSleeping(SLEEP);
            // R0.6
            checkHit(request2, ++requestId, VALID_RESPONSE, true, RESPONSE_TIMEOUT);
        } finally {
            PrepareResourceUtil.prepareResource(uri1, true);
            PrepareResourceUtil.prepareResource(uri2, true);
        }
    }

    /**
     * 1Req===========>A[MISS]
     * |              /2Req===========>A[MISS]
     * |              /|              /3Req==========>A[HIT]
     * |              /|              /|              /4Req=====>A[Force HIT]
     * |              /|              /|              /|        /5Req==========>A[MISS]
     * |              /|              /|              /|  inv > /|              /6Req==========>A[HIT]
     * ----------------------------------------------------------------------------------------------------->
     */
    @LargeTest
    public void test_003_LongPollingInvalidate() throws Throwable {

        final int LONG_POLL_INTERVAL = 80 * 1000;
        final int RESPONSE_TIMEOUT = 110 * 1000;
        final String uri = createTestResourceUri("asimov_long_poll_invalidate_ga");
        final HttpRequest request = createRequest()
                .setUri(uri)
                .setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Stateless-Sleep", "true")
                .addHeaderField("X-OC-Sleep", Integer.toString(LONG_POLL_INTERVAL / 1000))
                .getRequest();

        TestCaseThread mainThread = new TestCaseThread() {
            @Override
            public void run() throws Throwable {
                PrepareResourceUtil.prepareResource(uri, false);

                // 1.1 - 1.2 from network
                int requestId = 0;
                for (int i = 0; i < 2; i++) {
                    checkMiss(request, ++requestId, VALID_RESPONSE, true, RESPONSE_TIMEOUT);
                }
                // 1.3 from cache
                checkHit(request, ++requestId, VALID_RESPONSE, true, RESPONSE_TIMEOUT);
                // 1.4 from cache with new body
                HttpResponse response = checkHit(request, ++requestId, INVALIDATED_RESPONSE, true, RESPONSE_TIMEOUT);
                assertTrue(
                        "Response received at the same time as cache invalidate",
                        (response.getDuration() < (LONG_POLL_INTERVAL / 2 + 10000)));
                // 1.5 from network
                checkMiss(request, ++requestId, INVALIDATED_RESPONSE, true, RESPONSE_TIMEOUT);
                // 1.6 from cache
                checkHit(request, ++requestId, INVALIDATED_RESPONSE, true, RESPONSE_TIMEOUT);
            }
        };

        TestCaseThread invalidateThread = new TestCaseThread((LONG_POLL_INTERVAL * 3 + LONG_POLL_INTERVAL / 2)) {
            @Override
            public void run() throws Throwable {
                PrepareResourceUtil.prepareResource(uri, true);
            }
        };

        try {
            executeThreads(mainThread, invalidateThread);
        } finally {
            PrepareResourceUtil.prepareResource(uri, false);
        }
    }


    /**
     * 1Req===========>A[MISS]
     * |              /2Req===========>A[MISS]
     * |              /|              /3Req==========>A[HIT]
     * |              /|              /|              /4Req=====>A[Force HIT]
     * |              /|              /|              /|        /5Req==========>A[MISS]
     * |              /|              /|              /|  inv > /|              /6Req==========>A[HIT]
     * ----------------------------------------------------------------------------------------------------->
     * <p/>
     * IGNORED: due to ASMV-21735
     */
    @LargeTest
    public void test_004_LongPollingInvalidateCheckNoRMP() throws Throwable {

        final int LONG_POLL_INTERVAL = 61 * 1000;
        final int RESPONSE_TIMEOUT = 90 * 1000;
        final String uri = createTestResourceUri("asimov_long_poll_invalidate_checknormp_ga");
        final HttpRequest request = createRequest()
                .setUri(uri)
                .setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Stateless-Sleep", "true")
                .addHeaderField("X-OC-Sleep", Integer.toString(LONG_POLL_INTERVAL / 1000))
                .getRequest();

        TestCaseThread mainThread = new TestCaseThread() {
            @Override
            public void run() throws Throwable {
                PrepareResourceUtil.prepareResource(uri, false);

                // 1.1 - 1.2 from network
                int requestId = 0;
                for (int i = 0; i < 2; i++) {
                    checkMiss(request, ++requestId, VALID_RESPONSE, true, RESPONSE_TIMEOUT);
                }
                // 1.3 from cache
                checkHit(request, ++requestId, VALID_RESPONSE, true, RESPONSE_TIMEOUT);
                // 1.4 from cache with new body
                HttpResponse response = checkHit(request, ++requestId, INVALIDATED_RESPONSE, true, RESPONSE_TIMEOUT);
                assertTrue(
                        "Response received at the same time as cache invalidate",
                        (response.getDuration() < (LONG_POLL_INTERVAL / 2 + 10000)));
                // 1.5 from network
                checkMiss(request, ++requestId, INVALIDATED_RESPONSE, true, RESPONSE_TIMEOUT);
                // 1.6 from cache
                checkHit(request, ++requestId, INVALIDATED_RESPONSE, true, RESPONSE_TIMEOUT);
            }
        };

        TestCaseThread invalidateThread = new TestCaseThread((LONG_POLL_INTERVAL * 3 + LONG_POLL_INTERVAL / 2)) {
            @Override
            public void run() throws Throwable {
                PrepareResourceUtil.prepareResource(uri, true);
            }
        };

        try {
            executeThreads(mainThread, invalidateThread);
        } finally {
            PrepareResourceUtil.prepareResource(uri, false);
        }
    }

    /**
     * 1Req===========>A[MISS]
     * |              /2Req===========>A[MISS]
     * |              /|              /3Req==========>A[HIT]
     * |              /|              /|              /4Req=====>A[Force HIT]
     * |              /|              /|              /|        /      5Req==========>A[MISS]
     * |              /|              /|              /|  inv > /      |              /6Req==========>A[HIT]
     * --------------------------------------------------------------------------------------------------------->
     */
    @LargeTest
    public void test_005_LongPollingInvalidateWithReqPause() throws Throwable {

        final int LONG_POLL_INTERVAL = 80 * 1000;
        final int RESPONSE_TIMEOUT = 110 * 1000;
        final String uri = createTestResourceUri("asimov_long_poll_invalidate_with_req_pause_ga");
        final HttpRequest request = createRequest()
                .setUri(uri)
                .setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Stateless-Sleep", "true")
                .addHeaderField("X-OC-Sleep", Integer.toString(LONG_POLL_INTERVAL / 1000))
                .getRequest();

        TestCaseThread mainThread = new TestCaseThread() {
            @Override
            public void run() throws Throwable {
                PrepareResourceUtil.prepareResource(uri, false);

                // 1.1 - 1.2 from network
                int requestId = 0;
                for (int i = 0; i < 2; i++) {
                    checkMiss(request, ++requestId, VALID_RESPONSE, true, RESPONSE_TIMEOUT);
                }
                // 1.3 from cache
                checkHit(request, ++requestId, VALID_RESPONSE, true, RESPONSE_TIMEOUT);
                // 1.4 from cache with new body
                HttpResponse response = checkHit(request, ++requestId, INVALIDATED_RESPONSE, true, RESPONSE_TIMEOUT);
                assertTrue(
                        "Response received at the same time as cache invalidate",
                        (response.getDuration() < (LONG_POLL_INTERVAL / 2 + 10000)));
                logSleeping(LONG_POLL_INTERVAL - response.getDuration());
                // 1.5 from network
                checkMiss(request, ++requestId, INVALIDATED_RESPONSE, true, RESPONSE_TIMEOUT);
                // 1.6 from cache
                checkHit(request, ++requestId, INVALIDATED_RESPONSE, true, RESPONSE_TIMEOUT);
            }
        };

        TestCaseThread invalidateThread = new TestCaseThread((LONG_POLL_INTERVAL * 3 + LONG_POLL_INTERVAL / 2)) {
            @Override
            public void run() throws Throwable {
                PrepareResourceUtil.prepareResource(uri, true);
            }
        };

        try {
            executeThreads(mainThread, invalidateThread);
        } finally {
            PrepareResourceUtil.prepareResource(uri, false);
        }
    }


    /**
     * 1Req===========>A[MISS]
     * |              /2R=>A[MISS]
     * |              /|  /3Req===========>A[MISS]
     * |              /|  /|              /4R==========>A[HIT]
     * |              /|  /|              /|             /5Req===========>A[HIT]
     * |              /|  /|              /|             /|              /6R=============>A[HIT]
     * |              /|  /|              /|             /|              /|              /|
     * |              /|  /|              /|             /|              /|              /|
     * ------------------------------------------------------------------------------------------->
     * <p/>
     * When we send alternatively two requests, we get two different polls
     * <p/>
     * IGNORED: due to incorrect logic
     */
    @Ignore
    @LargeTest
    public void test_006_LongOrdinaryPolling() throws Throwable {

        final int RESPONSE_TIMEOUT = 90 * 1000;

        final String RESOURCE_URI = "asimov_long_pol_003";

        final String uri = createTestResourceUri(RESOURCE_URI);

        HttpRequest request1 = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Sleep", "67").getRequest();

        HttpRequest request2 = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Sleep", "0").getRequest();

        PrepareResourceUtil.prepareResource(uri, false);

        int requestId = 0;

        try {
            checkMiss(request1, ++requestId, VALID_RESPONSE, false, RESPONSE_TIMEOUT);
            checkMiss(request2, ++requestId, VALID_RESPONSE, false, RESPONSE_TIMEOUT);
            checkMiss(request1, ++requestId, VALID_RESPONSE, false, RESPONSE_TIMEOUT);
            checkHit(request2, ++requestId, VALID_RESPONSE, false, RESPONSE_TIMEOUT);
            checkHit(request1, ++requestId, VALID_RESPONSE, false, RESPONSE_TIMEOUT);
            checkHit(request2, ++requestId, VALID_RESPONSE, false, RESPONSE_TIMEOUT);
        } finally {
            invalidateLongPoll(uri);
        }

    }

    @SmallTest
    public void test_007_LongPollRLPcannotStartLongPoll() throws Throwable {

        final long sleepTime = 40 * 1000;
        final int LONG_POLL_INTERVAL = 67 * 1000;
        final String uri = createTestResourceUri("asimov_long_poll_004");
        final HttpRequest request1 = createRequest()
                .setUri(uri)
                .setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Stateless-Sleep", "true")
                .addHeaderField("X-OC-Sleep",
                        Integer.toString(LONG_POLL_INTERVAL / 1000))
                .getRequest();

        final HttpRequest request2 = createRequest().setUri(uri)
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .setMethod("GET").getRequest();

        TestCaseThread mainThread = new TestCaseThread() {
            @Override
            public void run() throws Throwable {

                // 1.1 - 1.2 from network
                int requestId = 0;
                for (int i = 0; i < 2; i++) {
                    checkMiss(request1, ++requestId, VALID_RESPONSE);
                }

                // 1.3 from cache
                checkHit(request1, ++requestId, VALID_RESPONSE);

                // 1.4 from cache with new body
                HttpResponse response = checkHit(request1, ++requestId, "tere1");
                assertTrue(
                        "Response received at the same time as cache invalidate",
                        (response.getDuration() < 50 * 1000));

                logSleeping(sleepTime - response.getDuration());
                // 1.5 from network
                response = checkMiss(request2, ++requestId, "tere1");
                logSleeping(sleepTime - response.getDuration());

                // 1.6 from network
                response = checkMiss(request2, ++requestId, "tere1");
                logSleeping(sleepTime - response.getDuration());

                // 1.7 from network
                checkHit(request2, ++requestId, "tere1");

            }
        };

        TestCaseThread invalidateThread = new TestCaseThread((LONG_POLL_INTERVAL * 3 + LONG_POLL_INTERVAL / 2)) {
            @Override
            public void run() throws Throwable {
                PrepareResourceUtil.prepareDiffResource(uri, "tere1");
            }
        };

        try {
            PrepareResourceUtil.prepareResource(uri, false);
            executeThreads(15 * 60 * 1000, mainThread, invalidateThread);
        } finally {
            PrepareResourceUtil.prepareResource(uri, true);
        }
    }

    /**
     * 1Req===========>A[MISS]
     * |              /2Req===========>A[MISS]
     * |              /|              /3Req==========>A[HIT]
     * |              /|              /|              /4Req=====>A[Force HIT]
     * |              /|              /|              /|        /5Req==========>A[MISS]
     * |              /|              /|              /|  inv > /|   valid>    /6Req==========>A[HIT]
     * ----------------------------------------------------------------------------------------------------->
     */
    @LargeTest
    public void test_008_LongPollingRespChangedTwoTimes() throws Throwable {

        final int LONG_POLL_INTERVAL = 100 * 1000;
        final int RESPONSE_TIMEOUT = 150 * 1000;
        final String uri1 = createTestResourceUri("asimov_long_poll_invalidate_validate");
        final HttpRequest request1 = createRequest()
                .setUri(uri1)
                .setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Stateless-Sleep", "true")
                .addHeaderField("X-OC-Sleep", Integer.toString(LONG_POLL_INTERVAL / 1000))
                .getRequest();

        TestCaseThread mainThread1 = new TestCaseThread() {
            @Override
            public void run() throws Throwable {
                PrepareResourceUtil.prepareResource(uri1, false);

                // 1.1 - 1.2 from network
                int requestId = 0;
                for (int i = 0; i < 2; i++) {
                    checkMiss(request1, ++requestId, VALID_RESPONSE, false, RESPONSE_TIMEOUT);

                }
                // 1.3 from cache
                checkHit(request1, ++requestId, VALID_RESPONSE, false, RESPONSE_TIMEOUT);

                // 1.4 from cache with new body
                HttpResponse response = checkHit(request1, ++requestId, INVALIDATED_RESPONSE, false, RESPONSE_TIMEOUT);

                assertTrue(
                        "Response received at the same time as cache invalidate",
                        (response.getDuration() < 40 * 1000));

                PrepareResourceUtil.prepareResource(uri1, false);

                // 1.5 from network
                checkMiss(request1, ++requestId, VALID_RESPONSE, false, RESPONSE_TIMEOUT);

                // 1.6 from cache
                checkHit(request1, ++requestId, VALID_RESPONSE, false, RESPONSE_TIMEOUT);

            }
        };

        TestCaseThread invalidateThread = new TestCaseThread(330 * 1000) {
            @Override
            public void run() throws Throwable {
                PrepareResourceUtil.prepareResource(uri1, true);
            }
        };

        try {
            executeThreads(mainThread1, invalidateThread);
        } finally {
            PrepareResourceUtil.prepareResource(uri1, true);
        }
    }

    /**
     * 1Req===========>A[MISS]
     * |              /2Req====================>A[MISS]
     * |              /|                       /3Req====================>A[HIT]
     * |              /|                       /|                       /
     * ---------------------------------------------------------------------------------------->
     * <p/>
     * IGNORED: There is no increase in long polling. Old logic.
     */
    @Ignore
    @LargeTest
    public void test_009_LongPollingIncreasing() throws Throwable {

        int long_poll_interval_incr = 100 * 1000;
        int response_timeout = long_poll_interval_incr + 30000;
        final String uri1 = createTestResourceUri("asimov_long_poll_increasing");
        final HttpRequest request1 = createRequest()
                .setUri(uri1)
                .setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Stateless-Sleep", "true")
                .addHeaderField("X-OC-Sleep", Integer.toString(long_poll_interval_incr / 1000))
                .getRequest();

        try {
            PrepareResourceUtil.prepareResource(uri1, false);
            int requestId = 0;

            // 1.1 from network
            checkMiss(request1, ++requestId, VALID_RESPONSE, false, response_timeout);


            long_poll_interval_incr *= 1.5;
            response_timeout = long_poll_interval_incr + 30000;
            final HttpRequest request2 = createRequest()
                    .setUri(uri1)
                    .setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("X-OC-Stateless-Sleep", "true")
                    .addHeaderField("X-OC-Sleep", Integer.toString(long_poll_interval_incr / 1000))
                    .addHeaderField("X-OC-AddHeader_Date", "GMT")
                    .addHeaderField("Date", DateUtil.format(new Date(System.currentTimeMillis())))
                    .getRequest();

            // 1.2 from network
            checkMiss(request2, ++requestId, VALID_RESPONSE, false, response_timeout);


            long_poll_interval_incr *= 1.5;
            response_timeout = long_poll_interval_incr + 30000;
            final HttpRequest request3 = createRequest()
                    .setUri(uri1)
                    .setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("X-OC-Stateless-Sleep", "true")
                    .addHeaderField("X-OC-Sleep", Integer.toString(long_poll_interval_incr / 1000))
                    .addHeaderField("X-OC-AddHeader_Date", "GMT")
                    .addHeaderField("Date", DateUtil.format(new Date(System.currentTimeMillis())))
                    .getRequest();

            // 1.3 from cache
            checkHit(request3, ++requestId, VALID_RESPONSE, false, response_timeout);


        } finally {
            PrepareResourceUtil.prepareResource(uri1, true);
        }
    }

    /**
     * 1Req====================>A[MISS]
     * |                       /2Req===============>A[MISS]
     * |                       /|                  /3Req==========>A[MISS]
     * |                       /|                  /|             /
     * ---------------------------------------------------------------------------------------->
     * <p/>
     * IGNORED: Decreasing already  does not belongs to long polling. Even despite this fact logic incorrect.
     */
    @Ignore
    @LargeTest
    public void test_010_LongPollingDecreasing() throws Throwable {

        int long_poll_interval_incr = 150 * 1000;
        int response_timeout = long_poll_interval_incr + 30000;
        final String uri1 = createTestResourceUri("asimov_long_poll_decreasing");
        final HttpRequest request1 = createRequest()
                .setUri(uri1)
                .setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Stateless-Sleep", "true")
                .addHeaderField("X-OC-Sleep", Integer.toString(long_poll_interval_incr / 1000))
                .getRequest();

        try {
            PrepareResourceUtil.prepareResource(uri1, false);
            int requestId = 0;

            // 1.1 from network
            checkMiss(request1, ++requestId, VALID_RESPONSE, false, response_timeout);


            long_poll_interval_incr *= 0.8;
            response_timeout = long_poll_interval_incr + 30000;
            final HttpRequest request2 = createRequest()
                    .setUri(uri1)
                    .setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("X-OC-Stateless-Sleep", "true")
                    .addHeaderField("X-OC-Sleep", Integer.toString(long_poll_interval_incr / 1000))
                    .addHeaderField("X-OC-AddHeader_Date", "GMT")
                    .addHeaderField("Date", DateUtil.format(new Date(System.currentTimeMillis())))
                    .getRequest();

            // 1.2 from network
            checkMiss(request2, ++requestId, VALID_RESPONSE, false, response_timeout);


            long_poll_interval_incr *= 0.8;
            response_timeout = long_poll_interval_incr + 30000;
            final HttpRequest request3 = createRequest()
                    .setUri(uri1)
                    .setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("X-OC-Stateless-Sleep", "true")
                    .addHeaderField("X-OC-Sleep", Integer.toString(long_poll_interval_incr / 1000))
                    .addHeaderField("X-OC-AddHeader_Date", "GMT")
                    .addHeaderField("Date", DateUtil.format(new Date(System.currentTimeMillis())))
                    .getRequest();

            // 1.3 from network
            checkMiss(request3, ++requestId, VALID_RESPONSE, false, response_timeout);


        } finally {
            PrepareResourceUtil.prepareResource(uri1, true);
        }
    }

}
