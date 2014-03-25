package com.seven.asimov.it.tests.caching.polling.ordinary;

import android.test.suitebuilder.annotation.LargeTest;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;

public class OrdinaryPollingTests extends TcpDumpTestCase {


    @LargeTest
    public void test_001_OrdinaryPollingUpdateSocketClosedProduction() throws Throwable {
        final String RESOURCE_URI = "production_asimov_it_cv_0021";
        long sleepTime = 3 * 60 * 1000;

        String uri = createTestResourceUri(RESOURCE_URI);
        PrepareResourceUtil.prepareResource(uri, false);

        final HttpRequest request = createRequest().setUri(uri).setMethod("GET").getRequest();

        int requestId = 0;

        try {
            // 1.1
            HttpResponse response = checkMiss(request, ++requestId, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());

            // 1.2
            response = checkMiss(request, ++requestId, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());

            // 1.3 this request shall be cached but response from network
            response = checkMiss(request, ++requestId, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());

            // 1.4 this request shall be returned from cache
            response = checkHit(request, ++requestId, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());

            // adjust time to 1 minute
            sleepTime = 60 * 1000;

            // 1.5 this request shall be returned from cache
            response = checkHit(request, ++requestId, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());

            // adjust time to 2 minutes
            sleepTime = 2 * 60 * 1000;

            // 1.6 out of period
            response = checkMiss(request, ++requestId, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());

            // adjust to 3 min
            sleepTime = 3 * 60 * 1000;

            // 1.7 this request shall be returned from cache
            response = checkHit(request, ++requestId, VALID_RESPONSE);
            logSleeping(360000);

            addCheckSocketClose();

        } finally {
            // invalidate resource to stop server polling
            PrepareResourceUtil.prepareResource(uri, true);
        }
    }

    @LargeTest
    public void test_002_OrdinaryPollingWithCacheInvalidateProduction() throws Throwable {

        final String RESOURCE_URI = "asimov_it_cv_0016";
        String uri = createTestResourceUri(RESOURCE_URI);
        PrepareResourceUtil.prepareResource(uri, false);
        final HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-Stateless-Sleep", "true").getRequest();

        try {
            // 1.1
            HttpResponse response = checkMiss(request, 1, VALID_RESPONSE);
            logSleeping(MIN_NON_RMP_PERIOD - response.getDuration());

            // 1.2
            response = checkMiss(request, 2, VALID_RESPONSE);
            logSleeping(MIN_NON_RMP_PERIOD - response.getDuration());

            // 1.3 this request shall be cached but response from network
            response = checkMiss(request, 3, VALID_RESPONSE);
            logSleeping(MIN_NON_RMP_PERIOD - response.getDuration());

            // 1.4 this request shall be returned from cache
            response = checkHit(request, 4, VALID_RESPONSE);
            logSleeping(MIN_NON_RMP_PERIOD - response.getDuration());

            // 1.5 this request shall be returned from cache
            response = checkHit(request, 5, VALID_RESPONSE);
            logSleeping(MIN_NON_RMP_PERIOD - response.getDuration());

            // 1.6 this request shall be returned from cache
            response = checkHit(request, 6, VALID_RESPONSE);

            long duration = response.getDuration();
            long start = System.currentTimeMillis();
            response = PrepareResourceUtil.prepareResource(uri, true);
            logSleeping(MIN_NON_RMP_PERIOD - response.getDuration() - duration);

            // 1.7 this response shall be returned from cache with new body
            response = checkTransient(request, 7, INVALIDATED_RESPONSE, start);
            logSleeping(MIN_NON_RMP_PERIOD - response.getDuration());

            // 1.8 this request shall be returned from network
            response = checkMiss(request, 8, INVALIDATED_RESPONSE);
            logSleeping(MIN_NON_RMP_PERIOD - response.getDuration());

            // 1.9 - 1.12 this request shall be returned from cache
            int count = 0;
            while (count < 4) {
                int id = count + 9;
                response = checkHit(request, id, INVALIDATED_RESPONSE);
                logSleeping(MIN_NON_RMP_PERIOD - response.getDuration());
                count++;
            }
        } finally {
            // invalidate resource to stop server polling
            PrepareResourceUtil.prepareResource(uri, false);
        }
    }

    /**
     * 1:Rn(t=0) 1:N=A(t=0) 2:Rn(t=N1) 2:N=A(t=N1) 3:Rn(t=2N1) 3:N=A(t=2N1) 4:Rc(t=3N1) 4:C=A(t=3N1) 5:Rc(t=4N1)
     * 5:C=A(t=4N1) 6:Rn(t=N2) 6:N=A(t=N2) 7:Rc(t=5N1) 7:C=A(5N1) 8:Rc(t=6N1) 8:C=A(t=6N1) N1 = 1 minutes N2 = 4,5
     * minutes
     */
    @LargeTest
    public void test_003_OrdinaryPollingWithUpdateProduction() throws Throwable {
        long sleepTime = 2 * 60 * 1000;

        String RESOURCE_URI = "production_asimov_it_cv_0015";
        String uri = createTestResourceUri(RESOURCE_URI);

        PrepareResourceUtil.prepareResource(uri, false);

        final HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();

        int requestId = 0;

        try {

            // 1.1
            HttpResponse response = checkMiss(request, ++requestId, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());

            // 1.2
            response = checkMiss(request, ++requestId, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());

            // 1.3 this request shall be cached but response from network
            response = checkMiss(request, ++requestId, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());

            // 1.4 this request shall be returned from cache
            response = checkHit(request, ++requestId, VALID_RESPONSE);
            logSleeping(60 * 1000 - response.getDuration());

            // 1. 4,5 out of period
            response = checkMiss(request, ++requestId, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());

            // 1.5 this request shall be returned from cache
            response = checkHit(request, ++requestId, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());

            // 1.6 this request shall be returned from cache
            response = checkHit(request, ++requestId, VALID_RESPONSE);

        } finally {
            // invalidate resource to stop server polling
            PrepareResourceUtil.prepareResource(uri, true);
        }
    }

    @LargeTest
    public void test_004_OrdinaryPollingWithVariableIntervalProduction() throws Throwable {
        long sleepTime = 2 * 60 * 1000;

        final String RESOURCE_URI = "production_asimov_it_cv_0001";
        String uri = createTestResourceUri(RESOURCE_URI);

        PrepareResourceUtil.prepareResource(uri, false);

        final HttpRequest request = createRequest().setUri(uri).setMethod("GET").getRequest();

        int requestId = 0;

        try {
            // 1.1
            HttpResponse response = checkMiss(request, ++requestId, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());

            // 1.2
            response = checkMiss(request, ++requestId, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());

            // 1.3 this request shall be cached but response from network
            response = checkMiss(request, ++requestId, VALID_RESPONSE);

            sleepTime = 4 * 60 * 1000;
            logSleeping(sleepTime - response.getDuration());

            // 1.4 this request shall be returned from cache
            response = checkHit(request, ++requestId, VALID_RESPONSE);

            sleepTime = 180 * 1000;
            logSleeping(sleepTime - response.getDuration());

            // 1.5 this request shall be returned from cache
            response = checkHit(request, ++requestId, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());

            // 1.6 this request shall be returned from cache
            response = checkHit(request, ++requestId, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());

            // 1.7 this request shall be returned from cache
            response = checkHit(request, ++requestId, VALID_RESPONSE);
        } finally {
            // invalidate resource to stop server polling
            PrepareResourceUtil.prepareResource(uri, true);
        }
    }
}
