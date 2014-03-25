package com.seven.asimov.it.testcases;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.base.TestCaseThread;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.TestUtil;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RmpTestCase extends TcpDumpTestCase {
    private static final Logger logger = LoggerFactory.getLogger(RmpTestCase.class.getSimpleName());

    public static final long MIN_NON_RMP_PERIOD = 67000;
    public static final long RMP_EXPIRATION_TIME = 2 * 60 * 1000;
    public static final long MIN_RMP_PERIOD = 5000;
    public static long MIN_RMP_CACHING_PERIOD = 5000L;
    private final static int NON_HIT_INTERVAL = 31 * 1000;

    protected void logInfo(String msg) {
        String threadName = getShortThreadName();
        logger.info("INFO", "Thread " + threadName + ": " + msg);
    }


    public void setThreadName(String name) {
        logger.info("INFO", "Starting thread " + name);
        Thread.currentThread().setName(name);
    }

    private void send4Requests(int statusCode) throws Exception {
        final String RESOURCE_URI = "production_asimov_it_cv_invalid";
        String uri = createTestResourceUri(RESOURCE_URI + statusCode);
        PrepareResourceUtil.prepareResource(uri, false);
        try {
            HttpRequest request = createRequest()
                    .setUri(uri)
                    .setMethod("GET")
                    .addHeaderField("X-OC-ResponseStatus", String.valueOf(statusCode)).getRequest();

            // Responses 1 - 3 should be from network
            for (int j = 0; j < 3; j++) {
                checkMiss(request, j + 1, statusCode, null);
                TestUtil.sleep(MIN_RMP_CACHING_PERIOD);
            }
            checkHit(request, 4, statusCode, null);
        } finally {
            PrepareResourceUtil.prepareResource(uri, true);
        }
    }

    protected void startTempRmp(final int threadId, final HttpRequest request) throws Throwable {
        TestCaseThread rmpThread = new TestCaseThread() {
            @Override
            public void run() throws Throwable {
                Thread.currentThread().setName("" + threadId);
                Thread.sleep(NON_HIT_INTERVAL);
                for (int i = 1; i < 3; i++) {
                    HttpResponse response = checkMiss(request, i);
                    logSleeping(MIN_RMP_PERIOD - response.getDuration());
                }
                checkHit(request, 3);
            }
        };

        Throwable t = rmpThread.call();
        Thread.currentThread().setName("1");
        if (t != null)
            throw t;
    }

    protected void testStatusCodeInternal(int firstStatusCode, int lastStatusCode, boolean runFor307StatusCode) throws Exception {
        for (int statusCode = firstStatusCode; statusCode <= lastStatusCode; statusCode++) {
            send4Requests(statusCode);
        }
        if (runFor307StatusCode) {
            send4Requests(307);
        }
    }

    protected void checkStatusCode(int statusCode, boolean checkPoll) throws Throwable {
        logInfo(String.format("Start checking of %d status code", statusCode));
        String uri = createTestResourceUri(String.format(
                "asimov_it_testDifferentStatusCodes_%d", statusCode));
        HttpRequest request = createRequest().setUri(uri)
                .setMethod(HttpGet.METHOD_NAME)
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-ResponseStatus", statusCode + "")
                .getRequest();
        PrepareResourceUtil.prepareResource(uri, false);
        try {
            if (checkPoll)
                checkPoll(request, 1, 1, MIN_NON_RMP_PERIOD, statusCode, null);
            else {
                HttpResponse response;
                for (int i = 0; i < 4; i++) {
                    response = checkMiss(request, i + 1, statusCode, null);
                    logSleeping(MIN_NON_RMP_PERIOD - response.getDuration());
                }
            }
        } finally {
            PrepareResourceUtil.prepareResource(uri, true);
        }
        logInfo(String.format("Finish checking of %d status code", statusCode));
    }

    protected void send4RequestRmp(HttpRequest request) throws Exception {
        sendMiss(1, request, HttpStatus.SC_OK, VALID_RESPONSE);
        TestUtil.sleep(MIN_RMP_CACHING_PERIOD);
        sendMiss(2, request, HttpStatus.SC_OK, VALID_RESPONSE);
        TestUtil.sleep(MIN_RMP_CACHING_PERIOD);
        sendMiss(3, request, HttpStatus.SC_OK, VALID_RESPONSE);
        TestUtil.sleep(MIN_RMP_CACHING_PERIOD);
        sendHit(4, request, HttpStatus.SC_OK, VALID_RESPONSE);
    }

    protected void checkRapidPollTTL(String resourceUri, long ttl) throws Exception {
        final long requestInterval = ttl / 6L;
        String uri = createTestResourceUri(resourceUri);
        PrepareResourceUtil.prepareResource(uri, false);
        try {
            HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity")
                    .getRequest();
            long delay = System.currentTimeMillis();
            int requestId = 0;

            // 0.1
            HttpResponse response = checkMiss(request, ++requestId, VALID_RESPONSE);
            delay = System.currentTimeMillis() - delay;
            logSleeping(requestInterval - delay);
            delay = System.currentTimeMillis();

            // 0.2
            response = checkMiss(request, ++requestId, VALID_RESPONSE);
            delay = System.currentTimeMillis() - delay;
            logSleeping(requestInterval - delay);
            delay = System.currentTimeMillis();

            // 0.3
            response = checkMiss(request, ++requestId, VALID_RESPONSE);
            delay = System.currentTimeMillis() - delay;
            logSleeping(requestInterval - delay);
            delay = System.currentTimeMillis();

            // 0.4
            response = checkHit(request, ++requestId, VALID_RESPONSE);
            delay = System.currentTimeMillis() - delay;
            logSleeping(2L * requestInterval - delay);
            delay = System.currentTimeMillis();

            // 0.5
            response = checkHit(request, ++requestId, VALID_RESPONSE);
            delay = System.currentTimeMillis() - delay;
            logSleeping(2L * requestInterval - delay);
            delay = System.currentTimeMillis();

            // 0.6
            response = checkHit(request, ++requestId, VALID_RESPONSE);
            delay = System.currentTimeMillis() - delay;
            logSleeping(2L * requestInterval - delay);
            delay = System.currentTimeMillis();

            // 0.7
            response = checkMiss(request, ++requestId, VALID_RESPONSE);
            delay = System.currentTimeMillis() - delay;
            logSleeping(requestInterval - delay);

            // 0.8
            response = checkMiss(request, ++requestId, VALID_RESPONSE);
        } finally {
            PrepareResourceUtil.prepareResource(uri, true);
        }
    }
}
