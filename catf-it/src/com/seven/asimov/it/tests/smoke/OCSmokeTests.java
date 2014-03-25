package com.seven.asimov.it.tests.smoke;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.test.AssertionFailedError;
import android.test.suitebuilder.annotation.LargeTest;
import com.seven.asimov.it.annotation.DeviceOnly;
import com.seven.asimov.it.asserts.CATFAssert;
import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.Base64;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.constants.BaseConstantsIF;
import com.seven.asimov.it.base.constants.StabilityConstantsIF;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.testcases.SmokeTestCase;
import com.seven.asimov.it.utils.*;
import com.seven.asimov.it.utils.date.DateUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.crcsTasks.NetlogTask;
import com.seven.asimov.it.utils.logcat.tasks.msisdnTasks.MsisdnSendingSmsValidationTask;
import com.seven.asimov.it.utils.logcat.tasks.msisdnTasks.MsisdnValidationSuccessTask;
import com.seven.asimov.it.utils.logcat.tasks.policyTasks.PolicyAddedTask;
import com.seven.asimov.it.utils.logcat.tasks.pollingTasks.StartPollTask;
import com.seven.asimov.it.utils.logcat.wrappers.*;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.seven.asimov.it.base.constants.BaseConstantsIF.CRLF;

/**
 * <b>Smoke Test suite</b>
 * {@link com.seven.asimov.it.tests.smoke.OCSmokeTests#test_000_Init(), Test1}
 * {@link com.seven.asimov.it.tests.smoke.OCSmokeTests#test_001_MSISDNValidation(), Test2}
 * {@link com.seven.asimov.it.tests.smoke.OCSmokeTests#test_002_PolicyUpdate(), Test3}
 * {@link com.seven.asimov.it.tests.smoke.OCSmokeTests#test_003_DnsTrafficLoad(), Test4}
 * {@link com.seven.asimov.it.tests.smoke.OCSmokeTests#test_004_HttpTrafficLoad(), Test5}
 * {@link com.seven.asimov.it.tests.smoke.OCSmokeTests#test_005_HttpsTrafficLoad(), Test6}
 * {@link com.seven.asimov.it.tests.smoke.OCSmokeTests#test_006_TcpTrafficLoad, Test7}
 * {@link com.seven.asimov.it.tests.smoke.OCSmokeTests#test_007_RfcCaching, Test8}
 * {@link com.seven.asimov.it.tests.smoke.OCSmokeTests#test_008_IncreasingPolling(), Test9}
 * {@link com.seven.asimov.it.tests.smoke.OCSmokeTests#test_009_RapidManualPolling, Test10}
 * {@link com.seven.asimov.it.tests.smoke.OCSmokeTests#test_010_LongPolling(), Test11}
 * {@link com.seven.asimov.it.tests.smoke.OCSmokeTests#test_011_ComplexPolling(), Test12}
 * {@link com.seven.asimov.it.tests.smoke.OCSmokeTests#test_012_RevalidationClientSide(), Test13}
 * {@link com.seven.asimov.it.tests.smoke.OCSmokeTests#test_013_InvalidateWithCache(), Test14}
 * {@link com.seven.asimov.it.tests.smoke.OCSmokeTests#test_014_InvalidateWithoutCache(), Test15}
 * {@link com.seven.asimov.it.tests.smoke.OCSmokeTests#test_015_NormalizationByUri(), Test16}
 * {@link com.seven.asimov.it.tests.smoke.OCSmokeTests#test_016_HttpsRfcCaching(), Test17}
 * {@link com.seven.asimov.it.tests.smoke.OCSmokeTests#test_017_HttpsRapidManualPolling, Test18}
 * {@link com.seven.asimov.it.tests.smoke.OCSmokeTests#test_018_LargePostHttps1024kb(), Test19}
 * {@link com.seven.asimov.it.tests.smoke.OCSmokeTests#test_019_CheckingCorrectReconnectionOfDispatchers(), Test20}
 * {@link com.seven.asimov.it.tests.smoke.OCSmokeTests#test_020_IncorrectOptimizationFlagNetLog(), Test21}
 * {@link com.seven.asimov.it.tests.smoke.OCSmokeTests#test_999_CleanUp(), Test20}
 * <p/>
 * <b>All smoke tests provide check for OC Client crash, dispatchers crash</b>
 */
public class OCSmokeTests extends SmokeTestCase {

    private static final Logger logger = LoggerFactory.getLogger(OCSmokeTests.class.getSimpleName());

    /**
     * <h1>Prepare test</h1>
     * <p>Preparing Test Framework for smoke testing</p>
     * <p>Creating namespace on server, at path <b>@asimov@ssl</b></p>
     * <p>Creating default property for caching and polling on HTTPS protocol</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Check available internet connection</li>
     * <li>Check available test server and PMS</li>
     * <li>Create namespace at path<b>@asimov@ssl</b></li>
     * <li>Create default property for caching and polling over <b>HTTPS</b> protocol</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    @DeviceOnly
    public void test_000_Init() throws Throwable {
        try {
            String message = "Test server is unavailable ";
            InetAddress inetAddress = InetAddress.getByName(TFConstantsIF.EXTERNAL_IP);
            if (!inetAddress.isReachable(10000)) {
                logger.error(message + inetAddress, new AssertionFailedError(message));
                throw new AssertionFailedError(message + inetAddress);
            }
            PMSUtil.createNameSpace(PATH, NAME);
            logger.info("Namescpace created at " + PATH + " on " + TFConstantsIF.EXTERNAL_IP + " for " + NAME);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new AssertionFailedError(e.getMessage());
        }
    }

    /**
     * <h1>MSISDN Validation test</h1>
     * <p>Check that OC client passed MSISDN validation</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Installing OC Client app</li>
     * <li>Check for MSISDN validation</li>
     * <li>For checking. apk must be exist on sdcard eith name - asimov-signed-eng002_MSISDN_no_validation_protocol.apk</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    @DeviceOnly
    public void test_001_MSISDNValidation() throws Throwable {
        logger.debug("MSISDN validation phone number " + TFConstantsIF.MSISDN_VALIDATION_PHONENUMBER);
        if (TFConstantsIF.MSISDN_VALIDATION_STATE == 1) {
            MsisdnSendingSmsValidationTask msisdnSendingSmsValidationTask = new MsisdnSendingSmsValidationTask();
            MsisdnValidationSuccessTask msisdnValidationSuccessTask = new MsisdnValidationSuccessTask();
            LogcatUtil logcatUtil = new LogcatUtil(getContext(), msisdnSendingSmsValidationTask, msisdnValidationSuccessTask);
            logcatUtil.start();
            try {
                long timestamp = System.currentTimeMillis();
                logger.trace("Before removeOCClient");
                OCUtil.removeOCClient();
                logger.trace("After removeOCClient");
                logger.trace("Before installOCClient");
                OCUtil.installOCClient(BaseConstantsIF.SD_CARD + "/asimov-signed.apk");
                logger.trace("After installOCClient");
                TestUtil.sleep(120 * 1000);
                logcatUtil.stop();

                logger.trace(msisdnSendingSmsValidationTask.toString());
                logger.trace(msisdnValidationSuccessTask.toString());

                MsisdnSendingSmsValidationWrapper msisdnSendingSmsValidationLogEntry = msisdnSendingSmsValidationTask.getEntryAfterTimestamp(timestamp);
                String phoneToSend = msisdnSendingSmsValidationLogEntry.getPhoneToSend();
                assertEquals("Msisdn validation: phonenumber is wrong! Must be " + TFConstantsIF.MSISDN_VALIDATION_PHONENUMBER + " but is " + phoneToSend,
                        TFConstantsIF.MSISDN_VALIDATION_PHONENUMBER, phoneToSend);
                logger.trace("test_001_MSISDNValidation" + msisdnSendingSmsValidationLogEntry);
                MsisdnValidationSuccessWrapper msisdnValidationSuccessLogEntry = msisdnValidationSuccessTask.getEntryAfter(msisdnSendingSmsValidationLogEntry.getEntryNumber());
                assertNotNull("Msisdn validation has not been done!", msisdnValidationSuccessLogEntry);
                logger.trace("test_001_MSISDNValidation" + msisdnValidationSuccessLogEntry);
            } catch (Exception e) {
                logger.error(ExceptionUtils.getStackTrace(e));
                throw new AssertionFailedError(e.getMessage());
            } finally {
                logcatUtil.stop();
            }
        } else {
            logger.info("Branding doesn't required MSISDN validation");
        }
    }

    /**
     * <h1>OC should send policy request</h1>
     * <p>The test checks that policy updated after creating personal scope.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Start logcat.</li>
     * <li>Create such policy: asimov@normalization@header@com.seven.asimov.test.tool@*@.*@	response_header_rules=SVRNAME.</li>
     * <li>Parse logs about policy should read from Storage.</li>
     * <li>Parse logs about added app should be defined.</li>
     * <li>Clear all personal policies</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    @DeviceOnly
    public void test_002_PolicyUpdate() throws Throwable {
        processes = SmokeUtil.processesParser();
        PolicyAddedTask addedTask = new PolicyAddedTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), addedTask);
        logcatUtil.start();
        PMSUtil.createNameSpace(POLICY_PATH, NAME);
        logSleeping(10000);
        PMSUtil.createNameSpace(FULL_POLICY_PATH, "test");
        logSleeping(10000);
        PMSUtil.createPersonalScopeProperty(RESPONSE_HEADER_RULES, FULL_POLICY_PATH + "@test", RESPONSE_HEADER_RULES_VALUE, true);
        try {
            logSleeping(2 * 60 * 1000);
            boolean result = false;
            if (!addedTask.getLogEntries().isEmpty()) {
                for (PolicyWrapper wrapper : addedTask.getLogEntries()) {
                    if (wrapper.getName().contains(RESPONSE_HEADER_RULES) && wrapper.getValue().contains(RESPONSE_HEADER_RULES_VALUE)) {
                        result = true;
                        break;
                    }
                }
            }
            assertTrue("Policies shoud be reached by client", result);
            SmokeUtil.assertOCCrash("test_002_PolicyUpdate", "OC running", true, OCUtil.isOpenChannelRunning());
            SmokeUtil.assertControllerCrash("test_002_PolicyUpdate", "Controller crash", processes, SmokeUtil.processesParser());
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new AssertionFailedError(e.getMessage());
        } finally {
            processes.clear();
            logcatUtil.stop();
        }
    }

    /**
     * <h1>Test stability DNS load</h1>
     * <p>The test checks stability DNS resolving hosts</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>resolving unique host over 5 min</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    @DeviceOnly
    public void test_003_DnsTrafficLoad() throws Throwable {
        processes = SmokeUtil.processesParser();

        long testTimeDns = System.currentTimeMillis();
        InetAddress address;

        try {
            for (String uri : StabilityConstantsIF.TOP_HTTP_RESOURCES) {
                if (uri != null) {
                    java.net.URI uriSmoke = new URI(uri);
                    address = InetAddress.getByName(uriSmoke.getHost());
                    host = address.toString();
                    assertTrue(host.length() > uriSmoke.getHost().length());
                }
                if (System.currentTimeMillis() - testTimeDns >= TEST_TIME) {
                    break;
                }
            }
            SmokeUtil.assertOCCrash("test_004_DnsTrafficLoad", "OC running", true, OCUtil.isOpenChannelRunning());
            SmokeUtil.assertControllerCrash("test_004_DnsTrafficLoad", "Controller crash", processes, SmokeUtil.processesParser());
        } catch (UnknownHostException e) {
            logger.error("Host cannot resolve " + host + ExceptionUtils.getStackTrace(e));
            throw new AssertionFailedError(e.getMessage());
        } finally {
            processes.clear();
        }
    }

    /**
     * <h1>Test stability HTTP load</h1>
     * <p>The test checks stability HTTP request/response</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>send random HTTP requests over 5 min</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    @DeviceOnly
    public void test_004_HttpTrafficLoad() throws Throwable {
        processes = SmokeUtil.processesParser();

        long testStartTime = System.currentTimeMillis();
        int requestId = 1;
        final String RESOURCE_URI = "asimov_smoke_tests_HTTP";
        final String URI = createTestResourceUri(RESOURCE_URI);

        try {
            while (testing) {
                request = createRequest().setUri(URI).setMethod("GET")
                        .addHeaderField("X-OC-ContentEncoding", "identity")
                        .addHeaderField("Random", TestUtil.generationRandomString())
                        .getRequest();
                response = checkMiss(request, requestId++);

                CATFAssert.assertStatusCode(requestId, 200, response);
                logSleeping(SLEEP_TIME - response.getDuration());

                if (System.currentTimeMillis() - testStartTime >= TEST_TIME) {
                    break;
                }
            }
            SmokeUtil.assertOCCrash("test_005_HttpTrafficLoad", "OC running", true, OCUtil.isOpenChannelRunning());
            SmokeUtil.assertControllerCrash("test_005_HttpTrafficLoad", "Controller crash", processes, SmokeUtil.processesParser());
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
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
     * <li>send random HTTP requests over 5 min, over SSL</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    @DeviceOnly
    public void test_005_HttpsTrafficLoad() throws Throwable {
        processes = SmokeUtil.processesParser();

        int requestId = 1;
        long testStartTime = System.currentTimeMillis();
        final String RESOURCE_URI = "asimov_smoke_tests_HTTPS";
        final String URI = createTestResourceUri(RESOURCE_URI, true);

        try {
            while (testing) {
                PrepareResourceUtil.prepareResource(URI, true);
                request = createRequest().setUri(URI).setMethod("GET")
                        .addHeaderField("X-OC-ContentEncoding", "identity")
                        .addHeaderField("Random", TestUtil.generationRandomString())
                        .getRequest();
                response = checkMiss(request, requestId++);

                CATFAssert.assertStatusCode(requestId, 200, response);
                logSleeping(SLEEP_TIME - response.getDuration());

                if (System.currentTimeMillis() - testStartTime >= TEST_TIME) {
                    break;
                }
            }
            SmokeUtil.assertOCCrash("test_006_HttpsTrafficLoad", "OC running", true, OCUtil.isOpenChannelRunning());
            SmokeUtil.assertControllerCrash("test_006_HttpsTrafficLoad", "Controller crash", processes, SmokeUtil.processesParser());
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new AssertionFailedError(e.getMessage());
        } finally {
            processes.clear();
        }
    }

    /**
     * <h1>Test stability TCP load</h1>
     * <p>The test checks stability TCP stream</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>open TCP stream, check channel over 5 min</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    @DeviceOnly
    public void test_006_TcpTrafficLoad() throws Throwable {
        processes = SmokeUtil.processesParser();

        long testStartTime = System.currentTimeMillis();
        int connectTime = 300, trafficSpeed = 250;

        try {
            while (testing) {
                StreamUtil.getStreamTcp(trafficSpeed, connectTime, getContext());
                if (System.currentTimeMillis() - testStartTime >= TEST_TIME) {
                    break;
                }
            }
            SmokeUtil.assertOCCrash("test_007_TcpTrafficLoad", "OC running", true, OCUtil.isOpenChannelRunning());
            SmokeUtil.assertControllerCrash("test_007_TcpTrafficLoad", "Controller crash", processes, SmokeUtil.processesParser());
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new AssertionFailedError(e.getMessage());
        } finally {
            processes.clear();
        }
    }

    /**
     * <h1>Test RFC caching</h1>
     * <p>The test check caching by RFC</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Send MISS requests, check response, sleep 300 milliseconds</li>
     * <li>Send HIT request, check response, sleep 300 milliseconds</li>
     * <li>Check start caching by RFC</li>
     * <li>Invalidate resource</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    @DeviceOnly
    public void test_007_RfcCaching() throws Throwable {
        processes = SmokeUtil.processesParser();

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

            SmokeUtil.assertOCCrash("test_008_RfcCaching", "OC running", true, OCUtil.isOpenChannelRunning());
            SmokeUtil.assertControllerCrash("test_008_RfcCaching", "Controller crash", processes, SmokeUtil.processesParser());
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
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
     * <li>Send MISS request, check response, sleep 20 seconds</li>
     * <li>Send MISS request, check response, sleep 30 seconds</li>
     * <li>Send MISS request, check response, sleep 40 seconds</li>
     * <li>Send HIT request, check response</li>
     * <li>Check start Increasing polling</li>
     * <li>Invalidate resource</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    @DeviceOnly
    public void test_008_IncreasingPolling() throws Throwable {
        processes = SmokeUtil.processesParser();

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
            SmokeUtil.assertOCCrash("test_009_IncreasingPolling", "OC running", true, OCUtil.isOpenChannelRunning());
            SmokeUtil.assertControllerCrash("test_009_IncreasingPolling", "Controller crash", processes, SmokeUtil.processesParser());
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
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
    public void test_009_RapidManualPolling() throws Throwable {
        processes = SmokeUtil.processesParser();

        String uri = createTestResourceUri("asimov_it_cv_smoke_rarpid_manual");
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

            checkHit(request, requestId);

            SmokeUtil.assertOCCrash("test_010_RapidManualPolling", "OC running", true, OCUtil.isOpenChannelRunning());
            SmokeUtil.assertControllerCrash("test_010_RapidManualPolling", "Controller crash", processes, SmokeUtil.processesParser());
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
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
    public void test_010_LongPolling() throws Throwable {
        processes = SmokeUtil.processesParser();

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
            SmokeUtil.assertOCCrash("test_011_LongPolling", "OC running", true, OCUtil.isOpenChannelRunning());
            SmokeUtil.assertControllerCrash("test_011_LongPolling", "Controller crash", processes, SmokeUtil.processesParser());
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            processes.clear();
        }
    }

    /**
     * <h1>Test complex polling</h1>
     * <p>The test check complex polling</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Send MISS request, check response, sleep 100 second</li>
     * <li>Send MISS request, check response, sleep 31 second</li>
     * <li>Send MISS request, check response, sleep 100 second</li>
     * <li>Send MISS request, check response, sleep 31 second</li>
     * <li>Send MISS request, check response, sleep 100 second</li>
     * <li>Send HIT request, check response</li>
     * <li>Check start complex polling</li>
     * <li>Invalidate resource</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    @DeviceOnly
    public void test_011_ComplexPolling() throws Throwable {
        processes = SmokeUtil.processesParser();

        int requestId = 1;
        int sleepFirstTime = 100 * 1000;
        int sleepSecondaryTime = 31 * 1000;
        String uri = createTestResourceUri("asimov_it_cv_smoke_complex2");
        HttpRequest request = createRequest().setUri(uri)
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
        try {
            PrepareResourceUtil.prepareResource(uri, false);

            response = checkMiss(request, requestId++);
            logSleeping(sleepFirstTime - response.getDuration());

            response = checkMiss(request, requestId++);
            logSleeping(sleepSecondaryTime - response.getDuration());

            response = checkMiss(request, requestId++);
            logSleeping(sleepFirstTime - response.getDuration());

            response = checkMiss(request, requestId++);
            logSleeping(sleepSecondaryTime - response.getDuration());

            response = checkMiss(request, requestId++);
            logSleeping(sleepFirstTime - response.getDuration());

            checkHit(request, requestId);

            SmokeUtil.assertOCCrash("test_012_ComplexPolling", "OC running", true, OCUtil.isOpenChannelRunning());
            SmokeUtil.assertControllerCrash("test_012_ComplexPolling", "Controller crash", processes, SmokeUtil.processesParser());
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new AssertionFailedError(e.getMessage());
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            processes.clear();
        }
    }

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
    public void test_012_RevalidationClientSide() throws Throwable {
        processes = SmokeUtil.processesParser();

        final String RESOURCE_URI = "asimov_it_cv_srv_revalidation_type_5";
        final String uri = createTestResourceUri(RESOURCE_URI);
        final String expires = DateUtil.format(new Date(new Date().getTime() + 100000));
        final String ETAG_DEFAULT = "42f8-4bccc642f14c0";
        String rawHeadersDef = "Expires: " + expires + CRLF
                + "Content-Encoding: identity" + CRLF
                + "Cache-Control: max-age=" + 100 + CRLF
                + "Accept-Ranges: bytes" + CRLF
                + "Content-Length: 4" + CRLF
                + "ETag: " + ETAG_DEFAULT;
        try {
            executeServerRevalidation(uri, rawHeadersDef, false, 30 * 1000);
            SmokeUtil.assertOCCrash("test_013_RevalidationClientSide", "OC running", true, OCUtil.isOpenChannelRunning());
            SmokeUtil.assertControllerCrash("test_013_RevalidationClientSide", "Controller crash", processes, SmokeUtil.processesParser());
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
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
    public void test_013_InvalidateWithCache() throws Throwable {
        processes = SmokeUtil.processesParser();
        StartPollTask startPollTask = new StartPollTask();
        StartPollWrapper startPoll;
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), startPollTask);
        logcatUtil.start();

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

            response = checkHit(request, requestId);

            logcatUtil.stop();
            long checkStart = System.currentTimeMillis();
            logSleeping(MIN_RMP_PERIOD * 6 - response.getDuration());
            startPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
            logSleeping(MIN_RMP_PERIOD);
            SmokeUtil.sendInvSms(true, startPoll);
            long checkEnd = System.currentTimeMillis();
            logSleeping(MIN_RMP_PERIOD * 6 - (checkEnd - checkStart));
            SmokeUtil.assertOCCrash("test_014_InvalidateWithCache", "OC running", true, OCUtil.isOpenChannelRunning());
            SmokeUtil.assertControllerCrash("test_014_InvalidateWithCache", "Controller crash", processes, SmokeUtil.processesParser());
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
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
    public void test_014_InvalidateWithoutCache() throws Throwable {
        processes = SmokeUtil.processesParser();
        StartPollTask startPollTask = new StartPollTask();
        StartPollWrapper startPoll;
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), startPollTask);
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
            logSleeping(MIN_RMP_PERIOD * 6 - response.getDuration());

            response = checkHit(request, requestId);

            logcatUtil.stop();
            long checkStart = System.currentTimeMillis();
            logSleeping(MIN_RMP_PERIOD * 6 - response.getDuration());
            startPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
            logSleeping(MIN_RMP_PERIOD);
            SmokeUtil.sendInvSms(false, startPoll);
            long checkEnd = System.currentTimeMillis();
            logSleeping(MIN_RMP_PERIOD * 6 - (checkEnd - checkStart));
            SmokeUtil.assertOCCrash("test_014_InvalidateWithCache", "OC running", true, OCUtil.isOpenChannelRunning());
            SmokeUtil.assertControllerCrash("test_014_InvalidateWithCache", "Controller crash", processes, SmokeUtil.processesParser());
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new AssertionFailedError(e.getMessage());
        } finally {
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
    public void test_015_NormalizationByUri() throws Throwable {
        processes = SmokeUtil.processesParser();

        int requestId = 1;
        String unNormalizationUri = "HtTp://TlN-DeV-TesTRUNNer1.7sys.eu:80/./.././asimov-it_cv_normalization_022" + ((int) (Math.random() * 1000));
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
            logSleeping(MIN_RMP_PERIOD - response.getDuration());

            expected = "HTTP/1.0 200 OK" + CRLF + "Date: " + CRLF
                    + "Content-Type: application/x-javascript" + CRLF + "Pragma: no-cache" + CRLF
                    + "Accept-Ranges: bytes" + CRLF + "Content-Length: 4" + CRLF + CRLF + "tere";
            expectedEncoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));
            request = createRequest().setUri(unNormalizationUri).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("X-OC-Raw", expectedEncoded)
                    .getRequest();

            response = checkMiss(request, requestId++);
            logSleeping(MIN_RMP_PERIOD - response.getDuration());

            expected = "HTTP/1.0 200 OK" + CRLF + "Date: " + CRLF
                    + "Content-Type: application/x-javascript" + CRLF + "Pragma: no-cache" + CRLF
                    + "Accept-Ranges: bytes" + CRLF + "Content-Length: 4" + CRLF + CRLF + "tere";
            expectedEncoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));
            request = createRequest().setUri(unNormalizationUri).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("X-OC-Raw", expectedEncoded)
                    .getRequest();

            response = checkMiss(request, requestId++);
            logSleeping(MIN_RMP_PERIOD - response.getDuration());

            expected = "HTTP/1.0 200 OK" + CRLF + "Date: " + CRLF
                    + "Content-Type: application/x-javascript" + CRLF + "Pragma: no-cache" + CRLF
                    + "Accept-Ranges: bytes" + CRLF + "Content-Length: 4" + CRLF + CRLF + "tere";
            expectedEncoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));
            request = createRequest().setUri(unNormalizationUri).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("X-OC-Raw", expectedEncoded)
                    .getRequest();

            checkHit(request, requestId);
            SmokeUtil.assertOCCrash("test_016_NormalizationByUri", "OC running", true, OCUtil.isOpenChannelRunning());
            SmokeUtil.assertControllerCrash("test_016_NormalizationByUri", "Controller crash", processes, SmokeUtil.processesParser());
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
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
    public void test_016_HttpsRfcCaching() throws Throwable {
        processes = SmokeUtil.processesParser();

        final String uri = createTestResourceUri("https_production_asimov_it_cv_005", true);
        final HttpRequest request = createRequest().setUri(uri)
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-AddHeader_Last-Modified", "-86400")
                .addHeaderField("X-OC-AddHeader_Date", "GMT").getRequest();
        try {
            checkMiss(request, 1);
            logSleeping(200);
            checkHit(request, 2);
            logSleeping(200);
            SmokeUtil.assertOCCrash("SmokeDNS", "OC running", true, OCUtil.isOpenChannelRunning());
            SmokeUtil.assertControllerCrash("SmokeDNS", "Controller crash", processes, SmokeUtil.processesParser());
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
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
    public void test_017_HttpsRapidManualPolling() throws Throwable {
        processes = SmokeUtil.processesParser();
        final String uri = createTestResourceUri("https_production_asimov_it_cv_005", true);
        final HttpRequest request = createRequest().setUri(uri)
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .getRequest();
        try {
            for (int i = 1; i <= 3; i++) {
                checkMiss(request, i);
                logSleeping(45 * 1000);
            }
            logSleeping(MIN_RMP_PERIOD);
            checkHit(request, 4);

            SmokeUtil.assertOCCrash("SmokeDNS", "OC running", true, OCUtil.isOpenChannelRunning());
            SmokeUtil.assertControllerCrash("SmokeDNS", "Controller crash", processes, SmokeUtil.processesParser());
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
    public void test_018_LargePostHttps1024kb() throws Throwable {
        processes = SmokeUtil.processesParser();

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
            logger.info("Test Life: " + Arrays.toString(expectedHash));
            assertTrue(Arrays.equals(expectedHash, response.getBodyHash()));
            SmokeUtil.assertOCCrash("SmokeDNS", "OC running", true, OCUtil.isOpenChannelRunning());
            SmokeUtil.assertControllerCrash("SmokeDNS", "Controller crash", processes, SmokeUtil.processesParser());
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new AssertionFailedError(e.getMessage());
        } finally {
            processes.clear();
        }
    }

    /**
     * <h1>Check correct reconnect dispatchers after enter to failover mode</h1>Pre-requests:
     * <p>Pre requirements</p>
     * <li>Set branding parameters:</li>
     * <li>client.openchannel.roaming_wifi_failover.enabled=1</li>
     * <li>client.openchannel.roaming_wifi_failover.actions=1</li>
     * <li>Connection to Relay should be unavailable</li>
     * <p>The test is implemented as follows</p>
     * <ol>
     * <li>Switch mobile interface on</li>
     * <li>Turn on wifi, if wi-fi are disabled</li>
     * <li>Establish connection to Relay</li>
     * <li>Switch mobile interface off, wifi - on</li>
     * <li>Connect to test wi-fi network</li>
     * <li>Expected results: dispatchers should be reconnected correctly</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    @DeviceOnly
    public void test_019_CheckingCorrectReconnectionOfDispatchers() throws Throwable {
        Context context = getContext();
        ConnectivityManager connectivityManager = null;
        if (context != null) {
            connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        WifiManager wifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        SmokeUtil sm = SmokeUtil.getInstance(getContext());
        LogcatUtil logcatUtil = new LogcatUtil(getContext());
        String RESOURCE_URI = "testCheckingCorrectReconnectionOfDispatchers", pOne = null, pTwo = null;
        String uri = createTestResourceUri(RESOURCE_URI);
        PrepareResourceUtil.prepareResource(uri, false);
        try {
            request = createRequest().setMethod("GET").setUri(uri).addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("X-OC-SomeHeader", "" + "header").getRequest();

            for (int i = 1; i < 5; i++) {
                try {
                    if (i == 4) {
                        pOne = PMSUtil.createPersonalScopeProperty(ROAMING_WIFI_FAILOVER_ENABLED, REST_FAILOVERS_PATH, "1", true);
                        pTwo = PMSUtil.createPersonalScopeProperty(ROAMING_WIFI_FAILOVER_ACTIONS, REST_FAILOVERS_PATH, "1", true);
                    }
                    response = checkMiss(request, i);
                } finally {
                    PrepareResourceUtil.invalidateResourceSafely(uri);
                }
            }

            sm.blockRelayServer();
            logSleeping(60 * 1000);
            wifiManager.setWifiEnabled(false);
            logSleeping(30 * 1000);
            NetworkInfo networkInfo;
            if (connectivityManager != null) {
                networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null) {
                    logger.info(networkInfo.getExtraInfo());
                }
            }
            sm.unblockRelayServer();
            wifiManager.setWifiEnabled(true);
            logSleeping(60 * 1000);
            if (connectivityManager != null) {
                networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null) {
                    logger.info(networkInfo.getExtraInfo());
                }
            }
            logSleeping(30 * 1000);

            processes = SmokeUtil.processesParser();
            SmokeUtil.assertOCCrash("test_003_CheckingCorrectReconnectionOfDispatchers", "OC running", true, OCUtil.isOpenChannelRunning());
            SmokeUtil.assertControllerCrash("test_003_CheckingCorrectReconnectionOfDispatchers", "Controller crash", processes, SmokeUtil.processesParser());
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new AssertionFailedError(e.getMessage());
        } finally {
            PMSUtil.deleteProperty(pOne);
            PMSUtil.deleteProperty(pTwo);
            logcatUtil.stop();
            processes.clear();
        }
    }

    /**
     * <p>Incorrect optimization flag reporting the CRCS</p>
     * <p>Steps:</p>
     * <ol>
     * <li>Send requests</li>
     * <li>Check optimization flag</li>
     * <li>Set up transparent mode ON</li>
     * <li>Send requests</li>
     * <li>Check optimization flag</li>
     * <li>Set up transparent mode OFF</li>
     * <li>Send requests</li>
     * <li>Check optimization flag</li>
     * </ol>
     * <p>Expected result:</p>
     * <ol>
     * <li>For first-3rd requests optimization flag of netlog should be "1"</li>
     * <li>For 4th-6th requests optimization flag of netlog should be "o", because transparent mode ON</li>
     * <li>For 7th-9th requests optimization flag of netlog should be "1"</li>
     * </ol>
     *
     * @throws Throwable
     */
    public void test_020_IncorrectOptimizationFlagNetLog() throws Throwable {
        final String uri = createTestResourceUri("asmv_17078_incorrect_optimization_flag_reporting_crcs");
        final HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-Encoding", "identity").getRequest();
        int requestId = 1;
        NetlogTask netlogTask = new NetlogTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), netlogTask);
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        try {
            logcatUtil.start();
            requestId = checkPoll(request, requestId, 0, 30 * 1000);
            logcatUtil.stop();
            checkOptimizationFlag(netlogTask, 1);
            netlogTask.reset();
            logcatUtil = new LogcatUtil(getContext(), netlogTask, policyAddedTask);
            logcatUtil.start();
            transparentOn(true);
            logSleeping(30 * 1000);
            requestId = checkPoll(request, requestId, 0, 30 * 1000);
            logcatUtil.stop();
            PMSUtil.checkPolicyReceived(policyAddedTask, Policy.Policies.TRANSPARENT.getName(), "1");
            checkOptimizationFlag(netlogTask, 0);
            transparentOn(false);
            netlogTask.reset();
            logcatUtil = new LogcatUtil(getContext(), netlogTask);
            logcatUtil.start();
            checkPoll(request, requestId, 0, 30 * 1000);
            logcatUtil.stop();
            checkOptimizationFlag(netlogTask, 1);
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            transparentOn(false);
        }
    }

    /**
     * <h1>Final smoke test</h1>
     * <p>Clean all namespaces and properties, which was created during executing smoke tests</p>
     *
     * @throws Throwable
     */
    @LargeTest
    @DeviceOnly
    public void test_999_CleanUp() throws Throwable {
        processes = SmokeUtil.processesParser();
        SmokeUtil.assertOCCrash("SmokeDNS", "OC running", true, OCUtil.isOpenChannelRunning());
        SmokeUtil.assertControllerCrash("SmokeDNS", "Controller crash", processes, SmokeUtil.processesParser());
        processes.clear();
    }

    private void checkOptimizationFlag(NetlogTask netlogTask, int expectedValue) {
        List<NetlogEntry> list = netlogTask.getLogEntries();
        assertFalse("NetlogEntry should not be empty", list.isEmpty());
        for (NetlogEntry entry : list) {
            if (entry.getHost().equals(AsimovTestCase.TEST_RESOURCE_HOST) && entry.getDstPort() == 80) {
                assertEquals("Incorrect optimization flag in NetLog ", expectedValue, entry.getOptimization());
            }
        }
    }

    private void transparentOn(final boolean on) {
        if (on) {
            propertyId = PMSUtil.createPersonalScopeProperty(Policy.Policies.TRANSPARENT.getName(),
                    Policy.Policies.TRANSPARENT.getPath(), "1", true);
            logSleeping(120 * 1000);
        } else {
            if (propertyId != null) PMSUtil.deleteProperty(propertyId);
            propertyId = PMSUtil.createPersonalScopeProperty(Policy.Policies.TRANSPARENT.getName(),
                    Policy.Policies.TRANSPARENT.getPath(), "0", true);
            logSleeping(1000);
            PMSUtil.deleteProperty(propertyId);
            logSleeping(60 * 1000);
        }
    }

}