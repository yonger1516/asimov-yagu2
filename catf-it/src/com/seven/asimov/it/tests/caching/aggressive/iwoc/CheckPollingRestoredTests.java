package com.seven.asimov.it.tests.caching.aggressive.iwoc;

import android.test.suitebuilder.annotation.LargeTest;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.testcases.IWOCTestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.pollingTasks.StartPollTask;
import com.seven.asimov.it.utils.logcat.wrappers.StartPollWrapper;
import android.util.Log;
import org.apache.http.HttpStatus;

public class CheckPollingRestoredTests extends IWOCTestCase{

    private static final String TAG = CheckPollingRestoredTests.class.getSimpleName();
    public static final int LP_REQUEST_INTERVAL = 110 * 1000;
    public static final int RI_REQUEST_INTERVAL = 65 * 1000;
    public static final int LP_REQUEST_INTERVAL_MAX = 110 * 1000;

    /*
     * <p>OC should reactivate RR immediately if response hash of first MISSed transaction after  INVALIDATED_WO_CACHE
     * is equal to response hash that was used for start polling of this RR.</p>
     * <p>A test resource is needed for this test case that returns the same responses for all requests. </p>
     * <p>Pattern [0,100,100,100,100]</p>
     * <p>Delay [75,75,90,75,75]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After receiving of 2nd  response polling should start.</li>
     * <li>3rd request should be HITed.</li>
     * <li>INVALIDATED_WO_CACHE  should be received from server before 3rd  response. 3Rd response should be force-hitted. RR should be deactivated, but CE shouldnt be deleted. </li>
     * <li>4th request should be send to network.</li>
     * <li>Response hash of 4th  transaction is equal to response hash that was used for start polling of this RR, so RR should be activated.</li>
     * <li>5th  response should be served from cache.</li>
     * </ol>
     *
     * @throws Throwable
     */

    @LargeTest
    public void test_001_RestoreLPAfterIWOC() throws Throwable {
        setDefaultAggressivenessProperty();
        final String RESOURCE_URI = "asimov_it_cv_test_001_RestoreLPAfterIWOC";
        final String uri = createTestResourceUri(RESOURCE_URI);
        PrepareResourceUtil.prepareResource(uri, false);
        final StartPollTask startPollTask = new StartPollTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), startPollTask);

        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-Stateless-Sleep", "true")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Sleep", "75").getRequest();

        logcatUtil.start();
        try {
            int requestId = 0;
            HttpResponse response;

            // 1.1 MISS
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(LP_REQUEST_INTERVAL - response.getDuration());

            // 1.2 MISS
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            long preparationStart = System.currentTimeMillis();
            logSleeping((long) (15 * 1000));
            assertTrue("Start of polling should be reported in client log", !startPollTask.getLogEntries().isEmpty());
            StartPollWrapper startPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
            Log.i(TAG, "Start poll wrapper object" + startPoll);
            logcatUtil.clearCache();
            long preparationDelay = System.currentTimeMillis() - preparationStart;
            logSleeping(LP_REQUEST_INTERVAL - response.getDuration() - preparationDelay);

            startThreadIWOCToCome(startPollTask, 30);

            //1.3 HIT
            response = checkHit(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(LP_REQUEST_INTERVAL - response.getDuration());

            // 1.4 MISS
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            preparationStart = System.currentTimeMillis();
            logSleeping((long) (15 * 1000));
            logcatUtil.stop();
            assertTrue("Start of polling should be reported in client log", !startPollTask.getLogEntries().isEmpty());
            StartPollWrapper startPoll2 = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
            Log.i(TAG, "Start poll wrapper object" + startPoll2);
            preparationDelay = System.currentTimeMillis() - preparationStart;
            logSleeping(LP_REQUEST_INTERVAL - response.getDuration() - preparationDelay);

            //1.5 HIT
            checkHit(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);

        } finally {
            // invalidate resource to stop server polling
            Log.i(TAG, "Invalidating resource");
            PrepareResourceUtil.invalidateResourceSafely(uri);
            logcatUtil.stop();
        }
    }

        /*
     * <p>OC should not reactivate RR immediately   if response hash of first MISSed transaction after
     * INVALIDATED_WO_CACHE isnt equal to Response hash that was used for start polling of this RR.</p>
     * <p> A test resource is needed for this test case that returns the same responses for first 3 requests, and another one for rest 3 requests.</p>
     * <p>Pattern [0,100,100,100,100]</p>
     * <p>Delay [75,75,90,75,75]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After receiving of 2nd  response polling should start.</li>
     * <li>3rd request should be HITed. </li>
     * <li>INVALIDATED_WO_CACHE  should be received from server before 3rd  response. 3Rd response should be force-hitted. RR should be deactivated, but CE shouldnt be deleted.</li>
     * <li>4th request should be send to network. </li>
     * <li>Response hash of 4th  transaction is not equal to response hash that was used for start polling of this RR, so RR should not be activated. CE should be removed. </li>
     * <li>5th response should be served from network.</li>
     * <li>After receiving of 5th response it should be saved into cache and polling should start.</li>
     * <li>6th response should be HITted.</li>
     * </ol>
     *
     * @throws Throwable
     */

    @LargeTest
    public void test_002_RestoreLPAfterIWOCWithOtherContent() throws Throwable {
        final String RESOURCE_URI = "test_002_RestoreLPAfterIWOCWithOtherContent";
        final String uri = createTestResourceUri(RESOURCE_URI);
        PrepareResourceUtil.prepareResource(uri, false);
        final StartPollTask startPollTask = new StartPollTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), startPollTask);

        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-Stateless-Sleep", "true")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Sleep", "75").getRequest();

        logcatUtil.start();
        try {
            int requestId = 0;
            HttpResponse response;

            // 1.1 MISS
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(LP_REQUEST_INTERVAL - response.getDuration());

            // 1.2 MISS
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            long preparationStart = System.currentTimeMillis();
            logSleeping((long) (15 * 1000));
            assertTrue("Start of polling should be reported in client log", !startPollTask.getLogEntries().isEmpty());
            StartPollWrapper startPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
            Log.i(TAG, "Start poll wrapper object" + startPoll);
            logcatUtil.clearCache();
            long preparationDelay = System.currentTimeMillis() - preparationStart;
            logSleeping(LP_REQUEST_INTERVAL - response.getDuration() - preparationDelay);

            startThreadIWOCToCome(startPollTask,30,uri);

            //1.3 HIT
            response = checkHit(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(LP_REQUEST_INTERVAL - response.getDuration());

            // 1.4 MISS
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, INVALIDATED_RESPONSE);
            logSleeping(LP_REQUEST_INTERVAL - response.getDuration());

            //1.5 MISS
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, INVALIDATED_RESPONSE);
            logSleeping((long) (15 * 1000));
            logcatUtil.stop();
            assertTrue("Start of polling should be reported in client log", !startPollTask.getLogEntries().isEmpty());
            startPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
            Log.i(TAG, "Start poll wrapper object" + startPoll);

        } finally {
            // invalidate resource to stop server polling
            Log.i(TAG, "Invalidating resource");
            PrepareResourceUtil.invalidateResourceSafely(uri);
            logcatUtil.stop();
        }
    }
     /*
     * <p>OC shouldnt reactivate RR immediately   if response hash of first MISSed transaction after
     * INVALIDATED_WO_CACHE is equal to response hash that was used for start polling of this RR but delay doesnt much current RR .</p>
     * <p>A test resource is needed for this test case that returns the same responses for all requests.</p>
     * <p>Pattern [0,100,100,100,70,70] </p>
     * <p>Delay [75,75,90,65,65,65] </p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After receiving of 2nd  response polling should start.</li>
     * <li>3rd request should be HITed.</li>
     * <li>INVALIDATED_WO_CACHE  should be received from server before 3rd  response. 3Rd    response should be force-hitted. RR should be deactivated, but CE shouldnt be deleted.</li>
     * <li>4th request should be send to network. </li>
     * <li>Response hash of 4th  transaction is equal to response hash that was used for start polling of this RR, but delay doesnt much current RR. So RR should not be activated. </li>
     * <li>5th response should be served from network.</li>
     * <li>After receiving of 5th response polling should start.</li>
     * <li>6th response should be HITted.</li>
     * </ol>
     *
     * @throws Throwable
     */


    @LargeTest
    public void test_003_RestoreLPAfterIWOCWithSmallerDelay() throws Throwable{
        final int LP_REQUEST_INTERVAL_70 = 70 * 1000;
        final String RESOURCE_URI = "test_003_RestoreLPAfterIWOCWithSmallerDelay";
        final String uri = createTestResourceUri(RESOURCE_URI);
        PrepareResourceUtil.prepareResource(uri, false);
        final StartPollTask startPollTask = new StartPollTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), startPollTask);

        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-Stateless-Sleep", "true")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Sleep", "90").getRequest();

        HttpRequest request2 = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-Stateless-Sleep", "true")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Sleep", "65").getRequest();

        logcatUtil.start();
        try {
            int requestId = 0;
            HttpResponse response;

            // 1.1 MISS
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(LP_REQUEST_INTERVAL_MAX - response.getDuration());

            // 1.2 MISS
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            long preparationStart = System.currentTimeMillis();
            logSleeping((long) (15 * 1000));
            assertTrue("Start of polling should be reported in client log", !startPollTask.getLogEntries().isEmpty());
            StartPollWrapper startPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
            Log.i(TAG, "Start poll wrapper object" + startPoll);
            long preparationDelay = System.currentTimeMillis() - preparationStart;
            logcatUtil.clearCache();
            logSleeping(LP_REQUEST_INTERVAL_MAX - response.getDuration() - preparationDelay);

            startThreadIWOCToCome(startPollTask, 80);

            //1.3 HIT
            response = checkHit(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(LP_REQUEST_INTERVAL_MAX - response.getDuration());


            // 1.4 MISS
            response = checkMiss(request2, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(LP_REQUEST_INTERVAL_70 - response.getDuration());

            //1.5 MISS
            response = checkMiss(request2, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            preparationStart = System.currentTimeMillis();
            logSleeping((long) (15 * 1000));
            logcatUtil.stop();
            assertTrue("Start of polling should be reported in client log", !startPollTask.getLogEntries().isEmpty());
            startPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
            Log.i(TAG, "Start poll wrapper object" + startPoll);
            preparationDelay = System.currentTimeMillis() - preparationStart;
            logSleeping( LP_REQUEST_INTERVAL_70 - response.getDuration() - preparationDelay);

            //1.5 HIT
            checkHit(request2, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);

        } finally {
            // invalidate resource to stop server polling
            Log.i(TAG, "Invalidating resource");
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }

    }

     /*
     * <p>OC should reactivate RR immediately   if response hash of first MISSed transaction after
     * INVALIDATED_WO_CACHE is equal to response hash that was used for start polling of this RR in case of ordinary polling</p>
     * <p> A test resource is needed for this test case that returns the same responses for all requests.</p>
     * <p>Pattern [0,65,65,65,65,65]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After receiving of 3rd  response polling should start. </li>
     * <li>4th  request should be HITed. </li>
     * <li>INVALIDATED_WO_CACHE  should be received from server.  RR should be deactivated, but CE shouldnt be deleted.</li>
     * <li>5th request should be send to network.</li>
     * <li>Response hash of 5th  transaction is equal to response hash that was used for start polling of this RR, so RR should be activated.</li>
     * <li>6th response should be HITted. </li>
     * </ol>
     * @throws Throwable
     */

    @LargeTest
    public void test_004_RestoreRIAfterIWOC() throws Throwable {
        final String RESOURCE_URI = "asimov_it_cv_test_004_RestoreRIAfterIWOC";
        final String uri = createTestResourceUri(RESOURCE_URI);
        PrepareResourceUtil.prepareResource(uri, false);
        final StartPollTask startPollTask = new StartPollTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), startPollTask);

        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-Stateless-Sleep", "true")
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();

        logcatUtil.start();
        try {

            int requestId = 0;
            HttpResponse response;

            // 1.1 MISS
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(RI_REQUEST_INTERVAL - response.getDuration());

            // 1.2 MISS
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(RI_REQUEST_INTERVAL - response.getDuration());

            // 1.3 MISS
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            long preparationStart = System.currentTimeMillis();
            logSleeping((long) (15 * 1000));
            assertTrue("Start of polling should be reported in client log", !startPollTask.getLogEntries().isEmpty());
            StartPollWrapper startPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
            Log.i(TAG, "Start poll wrapper object" + startPoll);
            logcatUtil.clearCache();
            long preparationDelay = System.currentTimeMillis() - preparationStart;
            logSleeping(RI_REQUEST_INTERVAL - response.getDuration() - preparationDelay);

            startThreadIWOCToCome(startPollTask, 30);

            //1.4 HIT
            response = checkHit(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(RI_REQUEST_INTERVAL - response.getDuration());

            // 1.5 MISS
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            preparationStart = System.currentTimeMillis();
            logSleeping((long) (15 * 1000));
            logcatUtil.stop();
            assertTrue("Start of polling should be reported in client log", !startPollTask.getLogEntries().isEmpty());
            StartPollWrapper startPoll2 = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
            Log.i(TAG, "Start poll wrapper object" + startPoll2);
            preparationDelay = System.currentTimeMillis() - preparationStart;
            logSleeping(RI_REQUEST_INTERVAL - response.getDuration() - preparationDelay);

            //1.6 HIT
            checkHit(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);

        } finally {
            // invalidate resource to stop server polling
            Log.i(TAG, "Invalidating resource");
            PrepareResourceUtil.invalidateResourceSafely(uri);
            logcatUtil.stop();
        }
    }
}
