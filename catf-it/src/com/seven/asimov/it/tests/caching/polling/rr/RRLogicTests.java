package com.seven.asimov.it.tests.caching.polling.rr;

import android.util.Log;
import com.seven.asimov.it.annotation.DeviceOnly;
import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.pollingTasks.StartPollTask;
import com.seven.asimov.it.utils.logcat.wrappers.StartPollWrapper;
import com.seven.asimov.it.utils.sms.SmsUtil;
import org.apache.http.HttpStatus;

public class RRLogicTests extends TcpDumpTestCase {

    private final static String TAG = RRLogicTests.class.getSimpleName();
    private LogcatUtil logcatUtil;


    /**
     * <p>Detection of Rapid Manual Poll</p>
     * <p>A test resource is needed for this test case that returns the same response for all requests. </p>
     * <p>Pattern [0,35,35,35]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After 3rd request Rapid Manual Poll should be detected. </li>
     * <li>After receiving of 3rd response RR should be activated, polling should start.</li>
     * <li>4th request should be HITed with delay 0.</li>
     * </ol>
     *
     * @throws Throwable
     */
    public void test_001_RR_logic() throws Throwable {
        int sleepTime = 35 * 1000;
        String resource = "test_asimov_rr_001";
        String uri = createTestResourceUri(resource);
        int requestId = 0;

        PrepareResourceUtil.prepareResource(uri, false);

        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
        try {
            // 1.1
            HttpResponse response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());
            // 1.2
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());
            // 1.3
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());
            // 1.4
            checkHit(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * <p>Detection of Rapid Long Poll</p>
     * <p>A test resource is needed for this test case that returns the same response for all requests.</p>
     * <p>Pattern [0,35,35,35]</p>
     * <p>Delay [24,21,21,21]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After 3rd request Rapid Long Poll should be detected. </li>
     * <li>After receiving of 3rd response RR should be activated, polling should start.</li>
     * <li>4th request should be HITed with delay 20.</li>
     * </ol>
     *
     * @throws Throwable
     */
    public void test_002_RR_logic() throws Throwable {
        String resource = "test_asimov_rr_002";
        String uri = createTestResourceUri(resource);
        PrepareResourceUtil.prepareResource(uri, false, 24L);
        int sleepTime = 35 * 1000;
        int requestId = 0;

        HttpRequest request1 = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Sleep", "24").getRequest();
        HttpRequest request2 = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Sleep", "21").getRequest();

        try {
            // 1.1
            HttpResponse response = checkMiss(request1, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());
            // 1.2
            response = checkMiss(request2, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());
            // 1.3
            response = checkMiss(request2, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());
            // 1.4
            checkHit(request2, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * <p>Detection of Long Poll</p>
     * <p>A test resource is needed for this test case that returns the same response for all requests. </p>
     * <p>Pattern [0,70,70]</p>
     * <p>Delay [68,68,68]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After 2nd request Long poll pattern should be detected with next value getRecentTO: recent TO = 68</li>
     * <li>After receiving of 2nd response RR should be activated, polling should start.</li>
     * <li>3th request should be HITed with delay 68.</li>
     * </ol>
     *
     * @throws Throwable
     */
    public void test_003_RR_logic() throws Throwable {
        int sleepTime = 70 * 1000;
        String resource = "test_asimov_rr_003";
        String uri = createTestResourceUri(resource);
        int requestId = 0;
        int expectedDuration = 68 * 1000;

        PrepareResourceUtil.prepareResource(uri, false);

        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Sleep", "68").getRequest();

        try {
            // 1.1
            HttpResponse response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            checkResponseDurationWithinThePermitted(response, requestId, expectedDuration);
            logSleeping(sleepTime - response.getDuration());
            // 1.2
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            checkResponseDurationWithinThePermitted(response, requestId, expectedDuration);
            logSleeping(sleepTime - response.getDuration());
            // 1.3
            checkHit(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            checkResponseDurationWithinThePermitted(response, requestId, expectedDuration);
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * <p>Detection of RI with delay</p>
     * <p>A test resource is needed for this test case that returns the same response for all requests. </p>
     * <p>Pattern [0,70,70,70]</p>
     * <p>Delay [20,10,31]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After 3rd request RI based pattern should be detected.</li>
     * <li>After receiving of 3rd response RR should be activated, polling should start.</li>
     * <li>4th request should be HITed with delay 0.</li>
     * </ol>
     *
     * @throws Throwable
     */
    public void test_004_RR_logic() throws Throwable {
        int sleepTime = 70 * 1000;
        String resource = "test_asimov_rr_004";
        String uri = createTestResourceUri(resource);
        int requestId = 0;

        PrepareResourceUtil.prepareResource(uri, false);

        HttpRequest request1 = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Sleep", "20").getRequest();
        HttpRequest request2 = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Sleep", "10").getRequest();
        HttpRequest request3 = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Sleep", "31").getRequest();
        try {
            // 1.1
            HttpResponse response = checkMiss(request1, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());
            // 1.2
            response = checkMiss(request2, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());
            // 1.3
            response = checkMiss(request3, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());
            // 1.4
            checkHit(request3, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * <p>Detection of RI based polling</p>
     * <p>A test resource is needed for this test case that returns the same response for all requests. </p>
     * <p>Pattern [0,65,65,65]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After 3rd request RI based polling should be detected. </li>
     * <li>After receiving of 3rd response RR should be activated, polling should start.</li>
     * <li>4th request should be HITed with delay 0.</li>
     * </ol>
     *
     * @throws Throwable
     */
    public void test_005_RR_logic() throws Throwable {
        int sleepTime = 65 * 1000;
        String resource = "test_asimov_rr_005";
        String uri = createTestResourceUri(resource);
        int requestId = 0;

        PrepareResourceUtil.prepareResource(uri, false);

        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
        try {
            // 1.1
            HttpResponse response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());
            // 1.2
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());
            // 1.3
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());
            // 1.4
            checkHit(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * <p>OC should redetect RR from Long Poll to RI</p>
     * <p>A test resource is needed for this test case that returns the same response for all requests.</p>
     * <p>Pattern [0,69,71,70,70]</p>
     * <p>Delay [66,11,3,0]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After 2nd request Long poll should be detected.</li>
     * <li>Response delay of 2nd response with value 11 doesnt match the current pattern.</li>
     * <li>3rd request should be sent to TC for server side revalidation. After 3rd request RI based polling detected with interval: 68.</li>
     * <li>After receiving of 3rd response RR should be activated, polling should start.</li>
     * <li>4th response should be HITed with delay 0.</li>
     * </ol>
     * <p/>
     * IGNORED: Test not valid now due to change logic of oc
     *
     * @throws Throwable
     * @deprecated IGNORED: Test not valid now due to change logic of oc
     */
    @Ignore
    public void test_006_RR_logic() throws Throwable {
        int sleepTime[] = {69 * 1000, 71 * 1000, 70 * 1000};
        String resource = "test_asimov_rr_006";
        String uri = createTestResourceUri(resource);
        int requestId = 0;
        int expectedDuration = 66 * 1000;

        PrepareResourceUtil.prepareResource(uri, false);

        HttpRequest request1 = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Sleep", "66").getRequest();
        HttpRequest request2 = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Sleep", "11").getRequest();
        HttpRequest request3 = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Sleep", "3").getRequest();
        HttpRequest request4 = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
        try {
            // 1.1
            HttpResponse response = checkMiss(request1, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            checkResponseDurationWithinThePermitted(response, requestId, expectedDuration);
            logSleeping(sleepTime[0] - response.getDuration());
            // 1.2
            response = checkMiss(request2, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime[1] - response.getDuration());
            // 1.3
            response = checkMiss(request3, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime[2] - response.getDuration());
            // 1.4
            checkHit(request4, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * <p>OC shouldnt redetect RR from Long Poll to RI</p>
     * <p>A test resource is needed for this test case that returns the same response for all requests.</p>
     * <p>Pattern [0,65,65,65]</p>
     * <p>Delay [35,35,34]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After 3rd request RI based polling should be detected.</li>
     * <li>After receiving of 3rd response RR should be activated, polling should start.</li>
     * <li>4th request should be HITed with delay 0.</li>
     * </ol>
     *
     * @throws Throwable
     */
    //pattern [0, 65, 65, 65]
    //delay [35, 35, 34]
    public void test_007_RR_logic() throws Throwable {
        int sleepTime = 65 * 1000;
        String resource = "test_asimov_rr_007";
        String uri = createTestResourceUri(resource);
        int requestId = 0;

        PrepareResourceUtil.prepareResource(uri, false);

        HttpRequest request1 = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Sleep", "35").getRequest();
        HttpRequest request2 = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Sleep", "34").getRequest();
        try {
            // 1.1
            HttpResponse response = checkMiss(request1, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());
            // 1.2
            response = checkMiss(request1, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());
            // 1.3
            response = checkMiss(request2, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());
            // 1.4
            checkHit(request2, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * <p>OC should redetect RR from Rapid Long Poll to Long Poll</p>
     * <p>A test resource is needed for this test case that returns the same response for all requests.</p>
     * <p>Pattern [0,35,35,35,65,65,65,65]</p>
     * <p>Delay [21,21,25,25,65,65,65,65]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After 3rd request Rapid Long Poll should be detected.</li>
     * <li>After receiving of 3rd response RR should be activated, polling should start.</li>
     * <li>4th request should be HITed with delay 25.</li>
     * <li>5th request should be HITed with delay 25.</li>
     * <li>Before 6th request INVALIDATE_WO_CACHE should be received.</li>
     * <li>6th request should be sent to TC for server side revalidation. RI should be detected with interval: 70.</li>
     * <li>7th request should be sent to TC for server side revalidation. Long Poll should be detected.</li>
     * <li>After 7th response polling should start.</li>
     * <li>8th request should be HITed.</li>
     * </ol>
     *
     * @throws Throwable
     */
    @DeviceOnly
    public void test_008_RR_logic() throws Throwable {
        int[] sleepTime = {35 * 1000, 65 * 1000};
        String resource = "test_asimov_rr_008";
        String uri = createTestResourceUri(resource);
        StartPollTask startPollTask = new StartPollTask();
        int requestId = 0;
        PrepareResourceUtil.prepareResource(uri, false);

        HttpRequest request1 = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Stateless-Sleep", "true")
                .addHeaderField("X-OC-Sleep", "21").getRequest();
        HttpRequest request2 = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Stateless-Sleep", "true")
                .addHeaderField("X-OC-Sleep", "25").getRequest();
        HttpRequest request3 = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Stateless-Sleep", "true")
                .addHeaderField("X-OC-Sleep", "65").getRequest();

        try {
            logcatUtil = new LogcatUtil(getContext(), startPollTask);
            logcatUtil.start();
            //1.1
            HttpResponse response = checkMiss(request1, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime[0] - response.getDuration());
            //1.2
            response = checkMiss(request1, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime[0] - response.getDuration());
            //1.3
            response = checkMiss(request2, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime[0] - response.getDuration());
            //1.4
            response = checkHit(request2, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            long startTime = System.currentTimeMillis();
            logcatUtil.stop();
            assertTrue("Start of polling should be reported in client log", !startPollTask.getLogEntries().isEmpty());
            StartPollWrapper startPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
            Log.i(TAG, "Start poll wrapper object" + startPoll);
            long endTime = System.currentTimeMillis();
            logSleeping(sleepTime[1] - response.getDuration() - (endTime - startTime));
            //1.5
            response = checkHit(request3, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            prepareResourceWithDelayedChange(uri);
            SmsUtil.sendInvalidationSms(getContext(), Integer.parseInt(startPoll.getSubscriptionId()),
                    SmsUtil.InvalidationType.INVALIDATE_WITHOUT_CACHE.byteVal);
            logSleeping(sleepTime[1] - response.getDuration() - 5 * 1000);
            //1.6
            response = checkMiss(request3, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime[1] - response.getDuration());
            //1.7
            response = checkMiss(request3, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime[1] - response.getDuration());
            //1.8
            checkHit(request3, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
        } finally {
            logcatUtil.stop();
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * <p>OC behavior for requests with pattern</p>
     * <p>A test resource is needed for this test case that returns the same response for all requests.</p>
     * <p>Pattern[0,65,100,35,45,65,70,80,45,67,65,100,35,45,65,70,80,45,67]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After 3rd request RI should be detected with period 47.</li>
     * <li>After receiving of 3rd response RR should be activated, polling should start with interval 60.</li>
     * <li>4th request should be sent to TC for server side revalidation.</li>
     * <li>5th request should be HITed.</li>
     * <li>6th request should be HITed.</li>
     * <li>7th request should be HITed.</li>
     * <li>8th request should be HITed.</li>
     * <li>9th request should be sent to TC for server side revalidation.</li>
     * <li>10th request should be HITed.</li>
     * <li>11th request should be HITed.</li>
     * <li>12th request should be HITed.</li>
     * <li>13th request should be sent to TC for server side revalidation.</li>
     * <li>14th-17th request should be HITed.</li>
     * <li>18th request should be sent to TC for server side revalidation.</li>
     * <li>19th request should be HITed.</li>
     * </ol>
     *
     * @throws Throwable
     */
    public void test_009_RR_logic() throws Throwable {
        int[] sleepTime1 = {65 * 1000, 100 * 1000, 35 * 1000, 45 * 1000, 65 * 1000};
        int[] sleepTime2 = {70 * 1000, 80 * 1000, 45 * 1000, 67 * 1000};
        String resource = "test_asimov_rr_009";
        String uri = createTestResourceUri(resource);
        int requestId = 0;

        PrepareResourceUtil.prepareResource(uri, false);

        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();

        try {
            HttpResponse response;
            //R1-R4
            for (int i = 0; i < 4; i++) {
                response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
                logSleeping(sleepTime1[i] - response.getDuration());
            }
            //R5
            response = checkHit(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime1[4] - response.getDuration());
            //R6-R8
            for (int i = 0; i < 3; i++) {
                response = checkHit(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
                logSleeping(sleepTime2[i] - response.getDuration());
            }
            //R9
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime2[3] - response.getDuration());
            //R10-R12
            for (int i = 0; i < 3; i++) {
                response = checkHit(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
                logSleeping(sleepTime1[i] - response.getDuration());
            }
            //R13
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime1[3] - response.getDuration());
            //R14
            response = checkHit(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime1[4] - response.getDuration());
            //R15-R17
            for (int i = 0; i < 3; i++) {
                response = checkHit(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
                logSleeping(sleepTime2[i] - response.getDuration());
            }
            //R18
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime2[3] - response.getDuration());
            //R19
            checkHit(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * <p>OC behavior for requests with pattern</p>
     * <p>A test resource is needed for this test case that returns the same response for all requests.</p>
     * <p>Pattern [0,70,2,100,35,45,10,45,35,25,67,70,2,100,35,45,10,45,35,25,67]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After 3rd request RI should be detected with period 70.</li>
     * <li>After receiving of 3rd response RR should be activated, polling should start with interval 70.</li>
     * <li>4th request should be HITed.</li>
     * <li>5th request should be sent to TC for server side revalidation. Polling should start with new interval 60.</li>
     * <li>6th request should be HITed.</li>
     * <li>7th request should be HITed due to it were sent in short period of time.</li>
     * <li>8th request should be HITed.</li>
     * <li>9th request should be sent to TC for server side revalidation, temporary RMP should start.</li>
     * <li>10th request should be HITed.</li>
     * <li>11th  request should be HITed.</li>
     * <li>12th  request should be HITed.</li>
     * <li>13th  request should be HITed due to it were sent in short period of time.</li>
     * <li>14th  request should be HITed.</li>
     * <li>15th request should be sent to TC for server side revalidation. RMP expired.</li>
     * <li>16th request should be HITed.</li>
     * <li>17th request should be HITed due to it were sent in short period of time.</li>
     * <li>18th request should be HITed.</li>
     * <li>19th request should be sent to TC for server side revalidation. Temporary RMP should start.</li>
     * <li>20th request should be HITed.</li>
     * <li>21 th  request should be HITed.</li>
     * </ol>
     * <p/>
     * IGNORED: due to ASMV-21735
     *
     * @throws Throwable
     */
    @Ignore
    public void test_010_RR_logic() throws Throwable {
        int[] sleepTime1 = {70 * 1000, 2 * 1000, 100 * 1000, 35 * 1000, 45 * 1000, 10 * 1000, 45 * 1000,
                35 * 1000, 25 * 1000, 67 * 1000};
        String resource = "test_asimov_rr_010";
        String uri = createTestResourceUri(resource);
        int requestId = 0;

        PrepareResourceUtil.prepareResource(uri, false);

        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();

        try {
            HttpResponse response;
            //R1-R5
            for (int i = 0; i < 5; i++) {
                response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
                logSleeping(sleepTime1[i] - response.getDuration());
            }
            //R6-R8
            for (int i = 0; i < 3; i++) {
                response = checkHit(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
                logSleeping(sleepTime1[i + 5] - response.getDuration());
            }
            //R9
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime1[8] - response.getDuration());
            //R10
            response = checkHit(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime1[9] - response.getDuration());
            //R11-R14
            for (int i = 0; i < 4; i++) {
                response = checkHit(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
                logSleeping(sleepTime1[i] - response.getDuration());
            }
            //R15
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime1[4] - response.getDuration());
            //R16-R18
            for (int i = 0; i < 3; i++) {
                response = checkHit(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
                logSleeping(sleepTime1[i + 5] - response.getDuration());
            }
            //R19
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime1[8] - response.getDuration());
            //R20
            response = checkHit(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime1[9] - response.getDuration());
            //R21
            checkHit(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * <p>OC behavior for requests with pattern</p>
     * <p>A test resource is needed for this test case that returns the same response for all requests.</p>
     * <p>Pattern [0,155,200,80,80,200,70,70,153,31,30,170,202,155,200,80,80,200,70,70,153,31,30,170,202]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After 3rd request RI should be detected with period 132.</li>
     * <li>After receiving of 3rd response RR should be activated, polling should start.</li>
     * <li>4th request should be MISSed.Polling should start with new interval 60.</li>
     * <li>5th request should be HITed.</li>
     * <li>6th request should be HITed.</li>
     * <li>7th request should be HITed.</li>
     * <li>8th request should be HITed.</li>
     * <li>9th request should be HITed. </li>
     * <li>10th request should be MISSed - out of order.</li>
     * <li>11th request should be HITed.</li>
     * <li>12th -21th requests should be HITed.</li>
     * <li>22th request should be MISSed - out of order.</li>
     * <li>23th-25th requests should be HITed.</li>
     * </ol>
     *
     * @throws Throwable
     */
    public void test_011_RR_logic() throws Throwable {
        int[] sleepTime = {0, 155, 200, 80, 80, 200, 70, 70, 153, 31, 30, 170, 202, 155, 200, 80, 80, 200, 70, 70, 153, 31, 30, 170, 202};
        String resource = "test_asimov_rr_011";
        String uri = createTestResourceUri(resource);
        int requestId = 0;

        PrepareResourceUtil.prepareResource(uri, false);

        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();

        try {
            HttpResponse response;
            for (int i = 1; i <= 25; i++) {
                if (i <= 4 || 10 == i || 22 == i) {
                    //R1-R4, R10, R22
                    response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
                } else {
                    //R5-R9, R11-R21, R23-R25
                    response = checkHit(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
                }
                if (i != 25) {
                    logSleeping(sleepTime[i] * 1000 - response.getDuration());
                }
            }
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * <p>OC behavior for requests with pattern</p>
     * <p>A test resource is needed for this test case that returns the same response for all requests.</p>
     * <p>Pattern [0,350,325,400,323,345,360,300,398,378,350,325,400,323,345,360,300,398,378]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After 3rd request RI should be detected with period 313.</li>
     * <li>After receiving of 3rd response RR should be activated, polling should start.</li>
     * <li>4th-19th requests should be HITed.</li>
     * </ol>
     *
     * @throws Throwable
     */
    @DeviceOnly
    public void test_012_RR_logic() throws Throwable {
        int[] sleepTime1 = {350, 325, 400, 323, 345, 360, 300, 398, 378};
        String resource = "test_asimov_rr_012";
        String uri = createTestResourceUri(resource);
        int requestId = 0;

        PrepareResourceUtil.prepareResource(uri, false);

        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();

        try {
            HttpResponse response;
            //R1-R3
            for (int i = 0; i < 3; i++) {
                response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
                logSleeping(sleepTime1[i] * 1000 - response.getDuration());
            }
            //R4-R9
            for (int i = 0; i < sleepTime1.length - 3; i++) {
                response = checkHit(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
                logSleeping(sleepTime1[i + 3] * 1000 - response.getDuration());
            }
            //R10-R18
            for (int i = 0; i < sleepTime1.length; i++) {
                response = checkHit(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
                logSleeping(sleepTime1[i] * 1000 - response.getDuration());
            }
            //R19
            checkHit(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * <p>OC behavior for requests with pattern </p>
     * <p>A test resource is needed for this test case that returns the same response for all requests.</p>
     * <p>Pattern [0,300,310,296,303,299,300,308,307,301,296,299,305,301,294,302]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After 3rd request RI should be detected with period 295.</li>
     * <li>After receiving of 3rd response RR should be activated, polling should start.</li>
     * <li>4th-16th requests should be HITed.</li>
     * </ol>
     *
     * @throws Throwable
     */
    @DeviceOnly
    public void test_013_RR_logic() throws Throwable {
        int[] sleepTime1 = {300, 310, 296, 303, 299, 300, 308, 307, 301, 296, 299, 305, 301, 294, 302};
        String resource = "test_asimov_rr_013";
        String uri = createTestResourceUri(resource);
        int requestId = 0;

        PrepareResourceUtil.prepareResource(uri, false);

        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();

        try {
            HttpResponse response;
            //R1-R3
            for (int i = 0; i < 3; i++) {
                response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
                logSleeping(sleepTime1[i] * 1000 - response.getDuration());
            }
            //R4-R15
            for (int i = 0; i < sleepTime1.length - 3; i++) {
                response = checkHit(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
                logSleeping(sleepTime1[i + 3] * 1000 - response.getDuration());
            }
            //R16
            checkHit(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * <p> Prepare resource that as result will receive INVALIDATE_WO_CACHE</p>
     * <p> Set socket timeout in value 5 min, and change response delay in 700 sec. Obviously receive IWOC</p>
     */
    private void prepareResourceWithDelayedChange(String uri) throws Exception {
        System.out.println("Preparing test resource...");
        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Stateless-Sleep", "true")
                .addHeaderField("X-OC-ChangeSleep", "700")
                .getRequest();
        sendRequest2(request, false, true, 5 * 1000);
    }

    private void checkResponseDurationWithinThePermitted(HttpResponse response, int responseID, int expectedDuration) {
        int delta = 5 * 1000;
        assertTrue("Response " + responseID + " duration should be about " + expectedDuration + ", but was " +
                response.getDuration(),
                (expectedDuration > response.getDuration() - delta) && (expectedDuration < response.getDuration() + delta));
    }
}