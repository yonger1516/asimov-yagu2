package com.seven.asimov.test.tool.tests.smoke;

import android.test.AssertionFailedError;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;
import com.seven.asimov.it.annotation.DeviceOnly;
import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.base.Base64;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.utils.OCUtil;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.date.DateUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.pollingTasks.IWCTask;
import com.seven.asimov.it.utils.logcat.tasks.pollingTasks.StartPollTask;
import com.seven.asimov.it.utils.logcat.wrappers.StartPollWrapper;
import com.seven.asimov.it.utils.sms.SmsUtil;
import com.seven.asimov.test.tool.activity.AutomationTestsTab;
import com.seven.asimov.test.tool.testcase.SmokeTestCase;
import com.seven.asimov.test.tool.utils.SmokeHelperUtil;
import com.seven.asimov.test.tool.utils.Z7TestUtil;
import com.seven.asimov.test.tool.utils.logcat.wrapper.TestCaseEvents;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Date;

/**
 * <b>Smoke Test suite</b>
 * {@link com.seven.asimov.test.tool.tests.smoke.SmokeTests#test_001_HttpTrafficLoad(), Test1}
 * {@link com.seven.asimov.test.tool.tests.smoke.SmokeTests#test_002_HttpsTrafficLoad(), Test2}
 * {@link com.seven.asimov.test.tool.tests.smoke.SmokeTests#test_003_HttpRfcCaching, Test3}
 * {@link com.seven.asimov.test.tool.tests.smoke.SmokeTests#test_004_IncreasingPolling(), Test4}
 * {@link com.seven.asimov.test.tool.tests.smoke.SmokeTests#test_005_HttpRapidManualPolling, Test5}
 * {@link com.seven.asimov.test.tool.tests.smoke.SmokeTests#test_006_LongPolling(), Test6}
 * {@link com.seven.asimov.test.tool.tests.smoke.SmokeTests#test_007_RevalidationClientSide(), Test8}
 * {@link com.seven.asimov.test.tool.tests.smoke.SmokeTests#test_008_InvalidateWithCache(), Test9}
 * {@link com.seven.asimov.test.tool.tests.smoke.SmokeTests#test_009_InvalidateWithoutCache(), Test10}
 * {@link com.seven.asimov.test.tool.tests.smoke.SmokeTests#test_010_NormalizationByUri(), Test11}
 * {@link com.seven.asimov.test.tool.tests.smoke.SmokeTests#test_011_HttpsRfcCaching(), Test12}
 * {@link com.seven.asimov.test.tool.tests.smoke.SmokeTests#test_012_HttpsRapidManualPolling, Test13}
 * {@link com.seven.asimov.test.tool.tests.smoke.SmokeTests#test_013_LargePostHttps1024kb(), Test14}
 * <p/>
 * <b>All smoke tests provide check for OC Client crash, dispatchers crash</b>
 */
public class SmokeTests extends SmokeTestCase {
    private final long SLEEP_TIME = 10 * 1000;
    private final long TEST_TIME = 60 * 1000 * 3;
    private final String TAG = SmokeTests.class.getSimpleName();
    private final String CRLF = "\r\n";

    private HttpRequest request;
    private HttpResponse response;

    /**
     * <h1>Method for checking that equipment is ready</h1>
     * <p>The method check that equipment is ready for start tests</p>
     */
    @Ignore
    public void invCheck() {
        SmokeHelperUtil smokeHelper = SmokeHelperUtil.getInstance(AutomationTestsTab.context);
        smokeHelper.initialGlobalCheck();
    }

    /**
     * <h1>Test stability HTTP load</h1>
     * <p>The test checks stability HTTP request/response</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>send random HTTP requests over 3 min</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    @DeviceOnly
    public void test_001_HttpTrafficLoad() throws Throwable {
        processes = SmokeHelperUtil.processesParser();

        long testStartTime = System.currentTimeMillis();
        int requestId = 1;
        final String RESOURCE_URI = "asimov_smoke_tests_HTTP";
        final String URI = createTestResourceUri(RESOURCE_URI);

        try {
            PrepareResourceUtil.prepareResource(URI, false);
            while (true) {
                request = createRequest().setUri(URI).setMethod("GET")
                        .addHeaderField("X-OC-ContentEncoding", "identity")
                        .addHeaderField("Random", SmokeHelperUtil.generationRandomHeader())
                        .getRequest();
                response = sendMiss(requestId++, request);
                logSleeping(SLEEP_TIME - response.getDuration());

                if (System.currentTimeMillis() - testStartTime >= TEST_TIME) {
                    break;
                }
            }
            SmokeHelperUtil.assertOCCrash(TAG, "OC running ", true, OCUtil.isOpenChannelRunning());
            SmokeHelperUtil.assertControllerCrash(TAG, DISPATCHER_MESSAGE, DISPATCHER_MESSAGE_PREFIX,
                    processes, SmokeHelperUtil.processesParser());
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
            throw new AssertionFailedError(e.getMessage());
        } finally {
            processes.clear();
        }
    }

    /**
     * <h1>Test stability HTTPS load</h1>
     * <p>The test checks stability HTTPS request/response</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>send random HTTP requests over 3 min, over SSL</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    @DeviceOnly
    public void test_002_HttpsTrafficLoad() throws Throwable {
        setUp();
        processes = SmokeHelperUtil.processesParser();

        int requestId = 1;
        long testStartTime = System.currentTimeMillis();
        final String RESOURCE_URI = "asimov_smoke_tests_HTTPS";
        final String URI = createTestResourceUri(RESOURCE_URI, true);

        try {
            PrepareResourceUtil.prepareResource(URI, false);
            while (true) {
                request = createRequest().setUri(URI).setMethod("GET")
                        .addHeaderField("X-OC-ContentEncoding", "identity")
                        .addHeaderField("Random", SmokeHelperUtil.generationRandomHeader())
                        .getRequest();
                response = sendMiss(requestId++, request);
                logSleeping(SLEEP_TIME - response.getDuration());

                if (System.currentTimeMillis() - testStartTime >= TEST_TIME) {
                    break;
                }
            }
            SmokeHelperUtil.assertOCCrash(TAG, "OC running ", true, OCUtil.isOpenChannelRunning());
            SmokeHelperUtil.assertControllerCrash(TAG, DISPATCHER_MESSAGE, DISPATCHER_MESSAGE_PREFIX,
                    processes, SmokeHelperUtil.processesParser());
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
            throw new AssertionFailedError(e.getMessage());
        } finally {
            processes.clear();
        }
    }

    /**
     * <h1>Test RFC caching</h1>
     * <p>The test check http caching by RFC</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Send MISS requests, check response, sleep 200 milliseconds</li>
     * <li>Send HIT request, check response, sleep 200 milliseconds</li>
     * <li>Check start caching by RFC</li>
     * <li>Invalidate resource</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    @DeviceOnly
    public void test_003_HttpRfcCaching() throws Throwable {
        processes = SmokeHelperUtil.processesParser();

        try {
            final String resource = "asimov_it_smoke_cache";
            final HttpRequest request = (createRequest().setUri(createTestResourceUri(resource))
                    .setMethod(HttpGet.METHOD_NAME).addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("X-OC-AddHeader_Last-Modified", "-86400")
                    .addHeaderField("X-OC-AddHeader_Date", "GMT")
                    .getRequest());

            checkMiss(request, 1);
            logSleeping(200);

            checkHit(request, 2);
            logSleeping(200);

            SmokeHelperUtil.assertOCCrash(TAG, "OC running ", true, OCUtil.isOpenChannelRunning());
            SmokeHelperUtil.assertControllerCrash(TAG, DISPATCHER_MESSAGE, DISPATCHER_MESSAGE_PREFIX,
                    processes, SmokeHelperUtil.processesParser());
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
            throw new AssertionFailedError(e.getMessage());
        } finally {
            processes.clear();
        }
    }

    /**
     * <h1>Test increasing polling</h1>
     * <p>The test check increasing polling</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Send MISS request, check response, sleep 67 seconds</li>
     * <li>Send MISS request, check response, sleep 67 seconds</li>
     * <li>Send MISS request, check response, sleep 67 seconds</li>
     * <li>Send HIT request, check response</li>
     * <li>Check start Increasing polling</li>
     * <li>Invalidate resource</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    @DeviceOnly
    public void test_004_IncreasingPolling() throws Throwable {
        processes = SmokeHelperUtil.processesParser();

        int requestId = 1;
        String uri = createTestResourceUri("asimov_it_cv_smoke_increasing");
        HttpRequest request = createRequest().setUri(uri)
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
        try {
            PrepareResourceUtil.prepareResource(uri, false);

            checkMiss(request, requestId++);
            logSleeping(MIN_NON_RMP_PERIOD);

            checkMiss(request, requestId++);
            logSleeping(MIN_NON_RMP_PERIOD);

            checkMiss(request, requestId++);
            logSleeping(MIN_NON_RMP_PERIOD);

            checkHit(request, requestId);
            SmokeHelperUtil.assertOCCrash(TAG, "OC running ", true, OCUtil.isOpenChannelRunning());
            SmokeHelperUtil.assertControllerCrash(TAG, DISPATCHER_MESSAGE, DISPATCHER_MESSAGE_PREFIX,
                    processes, SmokeHelperUtil.processesParser());
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
            throw new AssertionFailedError(e.getMessage());
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            processes.clear();
        }
    }

    /**
     * <h1>Test rapid manual polling</h1>
     * <p>The test check RMP</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Send three MISS requests, check response, sleep 5 second after each request </li>
     * <li>Send HIT request, check response, sleep 5 seconds</li>
     * <li>Check start RMP</li>
     * <li>Invalidate resource</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    @DeviceOnly
    public void test_005_HttpRapidManualPolling() throws Throwable {
        processes = SmokeHelperUtil.processesParser();

        String uri = createTestResourceUri("asimov_it_cv_smoke_rapid_manual");
        HttpRequest request = createRequest().setUri(uri)
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
        try {
            PrepareResourceUtil.prepareResource(uri, false);
            int requestId = 1;

            response = checkMiss(request, requestId++);
            logSleeping(MIN_RMP_PERIOD);

            checkMiss(request, requestId++);
            logSleeping(MIN_RMP_PERIOD);

            checkMiss(request, requestId++);
            logSleeping(MIN_RMP_PERIOD);

//            checkHit(request, requestId);
            checkHit(request, requestId);

            SmokeHelperUtil.assertOCCrash(TAG, "OC running ", true, OCUtil.isOpenChannelRunning());
            SmokeHelperUtil.assertControllerCrash(TAG, DISPATCHER_MESSAGE, DISPATCHER_MESSAGE_PREFIX,
                    processes, SmokeHelperUtil.processesParser());
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
            throw new AssertionFailedError(e.getMessage());
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            processes.clear();
        }
    }

    /**
     * <h1>Test Long polling</h1>
     * <p>The test check start long polling pattern</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Send two MISS requests, check response, sleep 120 seconds </li>
     * <li>Send two HIT requests, check response sleep 120 second</li>
     * <li>Check that long polling start</li>
     * <li>Invalidate resource</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    @DeviceOnly
    public void test_006_LongPolling() throws Throwable {
        processes = SmokeHelperUtil.processesParser();

        int requestId = 1;
        final long SLEEP = 120 * 1000;
        final String RESOURCE_URI = "asimov_long_pol_000";
        final String uri = createTestResourceUri(RESOURCE_URI);

        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Sleep", "61").getRequest();
        try {
            PrepareResourceUtil.prepareResource(uri, false);
            for (int i = 0; i < 2; i++) {
                response = checkMiss(request, requestId++);
                logSleeping(SLEEP - response.getDuration());
            }
            for (int i = 0; i < 2; i++) {
                response = checkHit(request, requestId++);
                logSleeping(SLEEP - response.getDuration());
            }
            SmokeHelperUtil.assertOCCrash(TAG, "OC running ", true, OCUtil.isOpenChannelRunning());
            SmokeHelperUtil.assertControllerCrash(TAG, DISPATCHER_MESSAGE, DISPATCHER_MESSAGE_PREFIX,
                    processes, SmokeHelperUtil.processesParser());
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            processes.clear();
        }
    }

//    /**
//     * <h1>Test complex polling</h1>
//     * <p>The test check complex polling</p>
//     * <p>The test is implemented as follows:</p>
//     * <ol>
//     * <li>Send MISS request, check response, sleep 100 second</li>
//     * <li>Send MISS request, check response, sleep 31 second</li>
//     * <li>Send MISS request, check response, sleep 100 second</li>
//     * <li>Send MISS request, check response, sleep 31 second</li>
//     * <li>Send MISS request, check response, sleep 100 second</li>
//     * <li>Send HIT request, check response</li>
//     * <li>Check start complex polling</li>
//     * <li>Invalidate resource</li>
//     * </ol>
//     *
//     * @throws Throwable
//     */
//    @LargeTest
//    @DeviceOnly
//    public void test_007_ComplexPolling() throws Throwable {
//        processes = SmokeHelperUtil.processesParser();
//
//        int requestId = 1;
//        int sleepFirstTime = 100 * 1000;
//        int sleepSecondaryTime = 31 * 1000;
//        String uri = createTestResourceUri("asimov_it_cv_smoke_complex2");
//        HttpRequest request = createRequest().setUri(uri)
//                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
//        try {
//            PrepareResourceUtil.prepareResource(uri, false);
//
//            response = checkMiss(request, requestId++);
//            logSleeping(sleepFirstTime - response.getDuration());
//
//            response = checkMiss(request, requestId++);
//            logSleeping(sleepSecondaryTime - response.getDuration());
//
//            response = checkMiss(request, requestId++);
//            logSleeping(sleepFirstTime - response.getDuration());
//
//            response = checkMiss(request, requestId++);
//            logSleeping(sleepSecondaryTime - response.getDuration());
//
//            response = checkMiss(request, requestId++);
//            logSleeping(sleepFirstTime - response.getDuration());
//
//            checkHit(request, requestId);
//
//            SmokeHelperUtil.assertOCCrash(TAG, "OC running ", true, OCUtil.isOpenChannelRunning());
//            SmokeHelperUtil.assertControllerCrash(TAG, DISPATCHER_MESSAGE, DISPATCHER_MESSAGE_PREFIX,
//                    processes, SmokeHelperUtil.processesParser());
//        } catch (Exception e) {
//            Log.e(TAG, ExceptionUtils.getStackTrace(e));
//            throw new AssertionFailedError(e.getMessage());
//        } finally {
//            PrepareResourceUtil.invalidateResourceSafely(uri);
//            processes.clear();
//        }
//    }

    /**
     * <h1>Test revalidation client side</h1>
     * <p>The test check client side revalidation</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Send MISS request with ETAG in headers, check response, put in cache by RFC </li>
     * <li>Send three HIT requests, check response</li>
     * <li>Resource expired by RFC</li>
     * <li>Send MISS request, check response, put in cache by RFC</li>
     * <li>Send two HIT requests, check response</li>
     * <li>Invalidate resource</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    @DeviceOnly
    public void test_007_RevalidationClientSide() throws Throwable {

        processes = SmokeHelperUtil.processesParser();

        final String RESOURCE_URI = "asimov_it_cv_srv_revalidation_type_5";
        final String uri = createTestResourceUri(RESOURCE_URI);
        long revalidationTime = System.currentTimeMillis() + 100000;
        final String expires = DateUtil.format(new Date(revalidationTime));
        final String ETAG_DEFAULT = "42f8-4bccc642f14c0";
        SmokeHelperUtil sm = SmokeHelperUtil.getInstance(AutomationTestsTab.context);
        String rawHeadersDef = "Expires: " + expires + CRLF
                + "Content-Encoding: identity" + CRLF
                + "Cache-Control: max-age=" + 100 + CRLF
                + "Accept-Ranges: bytes" + CRLF
                + "Content-Length: 4" + CRLF
                + "ETag: " + ETAG_DEFAULT;
        try {
            sm.executeServerRevalidation(uri, rawHeadersDef, false, 30 * 1000, revalidationTime);
            SmokeHelperUtil.assertOCCrash(TAG, "OC running ", true, OCUtil.isOpenChannelRunning());
            SmokeHelperUtil.assertControllerCrash(TAG, DISPATCHER_MESSAGE, DISPATCHER_MESSAGE_PREFIX,
                    processes, SmokeHelperUtil.processesParser());
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
            throw new AssertionFailedError(e.getMessage());
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            processes.clear();
        }
    }

    /**
     * <h1>Test invalidate with cache</h1>
     * <p>The test check cache invalidate with cache</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Send three MISS requests, check response, sleep 5 second after each request </li>
     * <li>Send HIT request, check response, sleep 5 seconds</li>
     * <li>Check start RMP</li>
     * <li>Invalidate resource</li>
     * <li>Simulate sms with invalidate with cache</li>
     * <li>Check invalidated cache</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    @DeviceOnly
    public void test_008_InvalidateWithCache() throws Throwable {
        processes = SmokeHelperUtil.processesParser();
        IWCTask iwcTask = new IWCTask();
        LogcatUtil logcatUtil = new LogcatUtil(AutomationTestsTab.context, iwcTask);

        String uri = createTestResourceUri("asimov_it_cv_smoke_iwc");
        HttpRequest request = createRequest().setUri(uri)
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
        try {
            PrepareResourceUtil.prepareResource(uri, false);
            int requestId = 1;

            response = checkMiss(request, requestId++);
            logSleeping(MIN_RMP_PERIOD * 6 - response.getDuration());

            response = checkMiss(request, requestId++);
            logSleeping(MIN_RMP_PERIOD * 6 - response.getDuration());

            response = checkMiss(request, requestId++);
            logSleeping(MIN_RMP_PERIOD * 6 - response.getDuration());

            response = checkHit(request, requestId++);

            logcatUtil.start();
            PrepareResourceUtil.prepareResource(uri, true);
            logSleeping(MIN_RMP_PERIOD * 6 - response.getDuration());

            if (!iwcTask.getLogEntries().isEmpty()) {
                checkHit(request, requestId, INVALIDATED_RESPONSE, true);
            } else {
                checkHit(request, requestId);
            }

            SmokeHelperUtil.assertOCCrash(TAG, "OC running ", true, OCUtil.isOpenChannelRunning());
            SmokeHelperUtil.assertControllerCrash(TAG, DISPATCHER_MESSAGE, DISPATCHER_MESSAGE_PREFIX,
                    processes, SmokeHelperUtil.processesParser());
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
            throw new AssertionFailedError(e.getMessage());
        } finally {
            logcatUtil.stop();
            PrepareResourceUtil.invalidateResourceSafely(uri);
            processes.clear();
        }
    }

    /**
     * <h1>Test invalidate without cache</h1>
     * <p>The test check cache invalidate without cache</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Send three MISS requests, check response, sleep 5 second after each request </li>
     * <li>Send HIT request, check response, sleep 5 seconds</li>
     * <li>Check start RMP</li>
     * <li>Invalidate resource</li>
     * <li>Simulate sms with invalidate without cache</li>
     * <li>Check invalidated cache</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    @DeviceOnly
    public void test_009_InvalidateWithoutCache() throws Throwable {
        processes = SmokeHelperUtil.processesParser();
        SmokeHelperUtil sm = SmokeHelperUtil.getInstance(AutomationTestsTab.context);
        StartPollTask startPollTask = new StartPollTask();
        StartPollWrapper startPoll;
        LogcatUtil logcatUtil = new LogcatUtil(AutomationTestsTab.context, startPollTask);
        logcatUtil.start();

        String uri = createTestResourceUri("asimov_it_cv_smoke_iwoc");
        HttpRequest request = createRequest().setUri(uri)
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
        try {
            PrepareResourceUtil.prepareResource(uri, false);
            int requestId = 1;

            response = checkMiss(request, requestId++);
            logSleeping(MIN_RMP_PERIOD * 6 - response.getDuration());

            response = checkMiss(request, requestId++);
            logSleeping(MIN_RMP_PERIOD * 6 - response.getDuration());

            response = checkMiss(request, requestId++);
            SmokeHelperUtil.startRadioKeepUpThread();
            logSleeping(MIN_RMP_PERIOD * 6 - response.getDuration());

            response = checkHit(request, requestId++);

            logSleeping(MIN_RMP_PERIOD);
            if (startPollTask.getLogEntries().isEmpty()) {
                logSleeping(MIN_RMP_PERIOD * 6 - response.getDuration());
                Z7TestUtil.setAdditionalInfo("Invalidate without cache wasn't received and client returned old cache", TestCaseEvents.TESTCASE_EVENT.SUSPENDED);
                response = checkHit(request, requestId);
            } else {
                startPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
                Log.e(TAG, startPoll != null ? "detect" : "null");
                SmsUtil.sendInvalidationSms(getContext(), Integer.parseInt(startPoll.getSubscriptionId()),
                        SmsUtil.InvalidationType.INVALIDATE_WITHOUT_CACHE.byteVal);
                logSleeping(MIN_RMP_PERIOD * 18 - response.getDuration());
                response = checkMiss(request, requestId);
            }
//            startPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
//            Log.e(TAG, startPoll != null ? "detect" : "null");
//            sm.sendInvSms(false, startPoll);
//            logSleeping(MIN_RMP_PERIOD * 18 - response.getDuration());
//
//            response = checkMiss(request, requestId);

            SmokeHelperUtil.assertOCCrash(TAG, "OC running ", true, OCUtil.isOpenChannelRunning());
            SmokeHelperUtil.assertControllerCrash(TAG, DISPATCHER_MESSAGE, DISPATCHER_MESSAGE_PREFIX,
                    processes, SmokeHelperUtil.processesParser());
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
            throw new AssertionFailedError(e.getMessage());
        } finally {
            SmokeHelperUtil.stopRadioKeepUpThread();
            logcatUtil.stop();
            PrepareResourceUtil.invalidateResourceSafely(uri);
            processes.clear();
        }
    }

    /**
     * <h1>Test normalization by uri</h1>
     * <p>The test checks uri normalization</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Send MISS 3 times with not normalize uri</li>
     * <li>Send HIT, check for polling start</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    @DeviceOnly
    public void test_010_NormalizationByUri() throws Throwable {
        processes = SmokeHelperUtil.processesParser();

        int requestId = 1;
        String unNormalizationUri = "HtTp://TlN-DeV-TesTRUNNer1.7sys.eu:80/./." + SmokeHelperUtil.generationRandomString() +
                "././asimov-it_cv_normalization_0012";
        PrepareResourceUtil.prepareResource(unNormalizationUri, false);
        try {
            String expected = "HTTP/1.0 200 OK" + CRLF + "Date: " + CRLF
                    + "Content-Type: application/x-javascript" + CRLF + "Pragma: no-cache" + CRLF
                    + "Accept-Ranges: bytes" + CRLF + "Content-Length: 4" + CRLF + CRLF + "tere";
            String expectedEncoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));
            request = createRequest().setUri(unNormalizationUri).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("X-OC-Raw", expectedEncoded)
                    .getRequest();

            response = checkMiss(request, requestId++);
            long SLEEP_INTERVAL = MIN_RMP_PERIOD;
            logSleeping(SLEEP_INTERVAL - response.getDuration());

            expected = "HTTP/1.0 200 OK" + CRLF + "Date: " + CRLF
                    + "Content-Type: application/x-javascript" + CRLF + "Pragma: no-cache" + CRLF
                    + "Accept-Ranges: bytes" + CRLF + "Content-Length: 4" + CRLF + CRLF + "tere";
            expectedEncoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));
            request = createRequest().setUri(unNormalizationUri).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("X-OC-Raw", expectedEncoded)
                    .getRequest();

            response = checkMiss(request, requestId++);
            logSleeping(SLEEP_INTERVAL - response.getDuration());

            expected = "HTTP/1.0 200 OK" + CRLF + "Date: " + CRLF
                    + "Content-Type: application/x-javascript" + CRLF + "Pragma: no-cache" + CRLF
                    + "Accept-Ranges: bytes" + CRLF + "Content-Length: 4" + CRLF + CRLF + "tere";
            expectedEncoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));
            request = createRequest().setUri(unNormalizationUri).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("X-OC-Raw", expectedEncoded)
                    .getRequest();

            response = checkMiss(request, requestId++);
            logSleeping(SLEEP_INTERVAL - response.getDuration());

            expected = "HTTP/1.0 200 OK" + CRLF + "Date: " + CRLF
                    + "Content-Type: application/x-javascript" + CRLF + "Pragma: no-cache" + CRLF
                    + "Accept-Ranges: bytes" + CRLF + "Content-Length: 4" + CRLF + CRLF + "tere";
            expectedEncoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));
            request = createRequest().setUri(unNormalizationUri).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("X-OC-Raw", expectedEncoded)
                    .getRequest();

            checkHit(request, requestId);
            SmokeHelperUtil.assertOCCrash(TAG, "OC running ", true, OCUtil.isOpenChannelRunning());
            SmokeHelperUtil.assertControllerCrash(TAG, DISPATCHER_MESSAGE, DISPATCHER_MESSAGE_PREFIX,
                    processes, SmokeHelperUtil.processesParser());
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
            throw new AssertionFailedError(e.getMessage());
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(unNormalizationUri);
            processes.clear();
        }
    }

    /**
     * <h1>Test RFC caching via HTTPS protocol</h1>
     * <p>The test checks that 1 response will be MISS, and 1 response will be HIT</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>MISS, get response immediately, sleep on 200 milliseconds</li>
     * <li>HIT, get response immediately, sleep on 200 milliseconds</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    @DeviceOnly
    public void test_011_HttpsRfcCaching() throws Throwable {
        setUp();
        processes = SmokeHelperUtil.processesParser();

        final String uri = createTestResourceUri("https_rfc_caching", true);
        PrepareResourceUtil.prepareResource(uri, false);
        final HttpRequest request = createRequest().setUri(uri)
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-AddHeader_Last-Modified", "-86400")
                .addHeaderField("X-OC-AddHeader_Date", "GMT").getRequest();

        try {
            checkMiss(request, 1);
            logSleeping(200);
            checkHit(request, 2);
            logSleeping(200);

            SmokeHelperUtil.assertOCCrash(TAG, "OC running ", true, OCUtil.isOpenChannelRunning());
            SmokeHelperUtil.assertControllerCrash(TAG, DISPATCHER_MESSAGE, DISPATCHER_MESSAGE_PREFIX,
                    processes, SmokeHelperUtil.processesParser());
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
            throw new AssertionFailedError(e.getMessage());
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            processes.clear();
        }
    }

    /**
     * <h1>Test RMP polling via HTTPS protocol</h1>
     * <p>The test checks that 3 response will be MISS, and 1 response will be HIT</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>MISS, get response immediately, sleep on 45 seconds</li>
     * <li>MISS, get response immediately, sleep on 45 seconds</li>
     * <li>MISS, get response immediately, sleep on 45 seconds</li>
     * <li>HIT, get response immediately, sleep on 45 seconds</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    @DeviceOnly
    public void test_012_HttpsRapidManualPolling() throws Throwable {
        setUp();
        processes = SmokeHelperUtil.processesParser();

        final String uri = createTestResourceUri("https_rapid_manual_polling", true);
        PrepareResourceUtil.prepareResource(uri, false);
        final HttpRequest request = createRequest().setUri(uri)
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .getRequest();
        try {
            request.setUri(uri);

            for (int i = 1; i <= 3; i++) {
                checkMiss(request, i);
                logSleeping(45 * 1000);
            }
            logSleeping(MIN_RMP_PERIOD);
            checkHit(request, 4);

            SmokeHelperUtil.assertOCCrash(TAG, "OC running ", true, OCUtil.isOpenChannelRunning());
            SmokeHelperUtil.assertControllerCrash(TAG, DISPATCHER_MESSAGE, DISPATCHER_MESSAGE_PREFIX,
                    processes, SmokeHelperUtil.processesParser());
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            processes.clear();
        }
    }

    /**
     * <h1>Test large POST method via HTTPS protocol</h1>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>POST 1 request with body 1024 bytes. Check for correct response</li>
     * </ol>
     *
     * @throws Throwable
     * @throws Exception
     */
    @LargeTest
    @DeviceOnly
    public void test_013_LargePostHttps1024kb() throws Throwable {
        setUp();
        processes = SmokeHelperUtil.processesParser();

        int size = 1024 * 1024;

        try {
            String uri = createTestResourceUri("https_regression_large_post", true);
            PrepareResourceUtil.prepareResource(uri, false);

            char expectedBody = 'c';

            String addHeaders = "Age: 1" + CRLF + "Server: Apache-Coyote/1.1" + CRLF + "Vary: Accept-Encoding" + CRLF
                    + "Header1: header1" + CRLF + "Header2: header2";
            String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

            StringBuilder sb = new StringBuilder(size);
            for (int i = 0; i < size; i++) {
                sb.append(expectedBody);
            }

            final HttpRequest request = createRequest().setUri(uri).setMethod("POST")
                    .addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("X-OC-AddHeader:", addHeadersEncoded).setBody(sb.toString())
                    .addHeaderField("X-OC-BodyMirror", "true").getRequest();

            HttpResponse response = sendRequest(request, this, false, false, Body.BODY_HASH);
            assertEquals(HttpStatus.SC_OK, response.getStatusCode());
            assertEquals(Integer.toString(size), response.getHeaderField("Content-Length"));
            assertEquals("1", response.getHeaderField("Age"));
            assertEquals("Apache-Coyote/1.1", response.getHeaderField("Server"));
            assertEquals("Accept-Encoding", response.getHeaderField("Vary"));
            assertEquals("header1", response.getHeaderField("Header1"));
            assertEquals("header2", response.getHeaderField("Header2"));
            byte[] expectedHash = TestUtil.getStreamedHash(expectedBody, size);
            Log.i(TAG, "Test life: " + Arrays.toString(expectedHash));
            assertTrue(Arrays.equals(expectedHash, response.getBodyHash()));
            SmokeHelperUtil.assertOCCrash(TAG, "OC running ", true, OCUtil.isOpenChannelRunning());
            SmokeHelperUtil.assertControllerCrash(TAG, DISPATCHER_MESSAGE, DISPATCHER_MESSAGE_PREFIX,
                    processes, SmokeHelperUtil.processesParser());
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
            throw new AssertionFailedError(e.getMessage());
        } finally {
            processes.clear();
        }
    }

}

