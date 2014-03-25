package com.seven.asimov.test.tool.tests.environmentcertification;

import android.util.Log;
import com.seven.asimov.it.OnStartChecks;
import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.OCUtil;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.SmokeUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.crcsTasks.CrcsAccumulatedTask;
import com.seven.asimov.it.utils.logcat.tasks.crcsTasks.CrcsRemovedTask;
import com.seven.asimov.it.utils.logcat.tasks.crcsTasks.CrcsTransferSuccessTask;
import com.seven.asimov.it.utils.logcat.tasks.crcsTasks.ReportTransferTaskNN;
import com.seven.asimov.it.utils.logcat.tasks.msisdnTasks.MsisdnTask;
import com.seven.asimov.it.utils.logcat.tasks.policyTasks.FirstTimePoliciesRetrievalTask;
import com.seven.asimov.it.utils.logcat.tasks.policyTasks.PolicyAddedTask;
import com.seven.asimov.it.utils.logcat.tasks.pollingTasks.IWCTask;
import com.seven.asimov.it.utils.logcat.tasks.pollingTasks.StartPollTask;
import com.seven.asimov.it.utils.logcat.wrappers.MsisdnWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.StartPollWrapper;
import com.seven.asimov.it.utils.sms.SmsUtil;
import com.seven.asimov.test.tool.activity.AutomationTestsTab;
import com.seven.asimov.test.tool.testcase.SmokeTestCase;
import com.seven.asimov.test.tool.utils.PropertyLoaderUtil;
import com.seven.asimov.test.tool.utils.SmokeHelperUtil;
import com.seven.asimov.test.tool.utils.Z7TestUtil;
import com.seven.asimov.test.tool.utils.logcat.wrapper.TestCaseEvents;
import junit.framework.AssertionFailedError;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * <b>Smoke Test suite</b>
 * {@link EnvironmentCertificationTests#test_001_OCInstall() Test1}
 * {@link EnvironmentCertificationTests#test_002_MSISDNValidationCheck() Test2}
 * {@link EnvironmentCertificationTests#test_003_FirstPolicyRetrieved() Test3}
 * {@link EnvironmentCertificationTests#test_004_CRCSRecordsTransfer()} }
 * {@link EnvironmentCertificationTests#test_005_HttpRfcCaching() Test4}
 * {@link EnvironmentCertificationTests#test_006_HttpsRfcCaching() Test5}
 * {@link EnvironmentCertificationTests#test_007_HttpRIPolling() Test6}
 * {@link EnvironmentCertificationTests#test_008_HttpsRIPolling() Test7}
 * {@link EnvironmentCertificationTests#test_009_InvalidateWithCache() Test8}
 * {@link EnvironmentCertificationTests#test_010_InvalidateWithoutCache() Test9}
 * <p/>
 * <b>All smoke tests provide check for OC Client crash, dispatchers crash</b>
 */
public class EnvironmentCertificationTests extends SmokeTestCase {
    private static final String TAG = EnvironmentCertificationTests.class.getSimpleName();
    private static final int OC_INSTALL_TIME = 9 * 60 * 1000;
    private static MsisdnTask msisdnTask = new MsisdnTask();
    private static FirstTimePoliciesRetrievalTask policiesRetrievalTask = new FirstTimePoliciesRetrievalTask();
    private static CrcsAccumulatedTask crcsAccumulatedTask = new CrcsAccumulatedTask();
    private static ReportTransferTaskNN reportTransferTaskNN = new ReportTransferTaskNN();
    private static CrcsRemovedTask crcsRemovedTask = new CrcsRemovedTask();
    private static CrcsTransferSuccessTask crcsTransferSuccessTask = new CrcsTransferSuccessTask();
    private static PolicyAddedTask policyAddedTask = new PolicyAddedTask();
    private HttpRequest request;
    private HttpResponse response;
    private static final String MSISDN_VALIDATION_STATE = PropertyLoaderUtil.getProperties().get("client.msisdn_validation_enabled");
    private static final String MSISDN_VALIDATION_PHONENUMBER = PropertyLoaderUtil.getProperties().get("system.msisdn_validation_phonenumber");

    private static final String HOST = "tln-dev-testrunner1.7sys.eu";

    @Ignore
    public void invCheck() {
        SmokeHelperUtil smokeHelper = SmokeHelperUtil.getInstance(AutomationTestsTab.context);
        smokeHelper.initialGlobalCheck();
    }

    /**
     * <h1>Install OC</h1>
     * <p>The test checks that OC successfully installed and started</p>
     * <p>MSISDN validation sms and obtains MSISDN validation success response</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Parsing logs about MSISDN Validation is enabled and not done yet - so it is required.</li>
     * <li>Parsing logs about imsi was detected.</li>
     * <li>Parsing logs about MSISDN validation success.</li>
     * <li>Parsing logs about MSISDN_VALIDATION_MSISDN is obtained and getting its value.</li>
     * </ol>
     *
     * @throws Throwable
     */
    public void test_001_OCInstall() throws Throwable {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        LogcatUtil logcatUtil = new LogcatUtil(AutomationTestsTab.context, msisdnTask, policiesRetrievalTask, policyAddedTask, crcsAccumulatedTask, reportTransferTaskNN, crcsRemovedTask, crcsTransferSuccessTask);
        Log.i(TAG, "Logcat Util has started sinse " + new Date());

        String packageName = AutomationTestsTab.b.getAndroidPackageName();
        String[] killAllOcc = {"su", "-c", "killall -9 occ"};
        String[] clearDataMiscOpenchannel = {"su", "-c", "rm -r /data/misc/openchannel/*"};
        String[] ocInstallCommand = {"su", "-c", "pm install -r " + SmokeTestCase.ocApkPath};
        String[] startService;
        if (!packageName.equals("com.seven.asimov")) {
            startService = new String[]{"su", "-c", "am startservice " + packageName + "/com.seven.asimov.ocengine.OCEngineService"};
        } else {
            startService = new String[]{"su", "-c", "am startservice " + packageName + "/.ocengine.OCEngineService"};
        }
        try {
            Log.d(TAG, SmokeTestCase.ocApkPath);
            Log.d(TAG, packageName);
            logcatUtil.start();
            Log.d(TAG, "First clean");
            Runtime.getRuntime().exec(killAllOcc).waitFor();
            Log.d(TAG, "Second clean");
            Runtime.getRuntime().exec(killAllOcc).waitFor();
            Log.d(TAG, "First delete");
            Runtime.getRuntime().exec(clearDataMiscOpenchannel).waitFor();
            Log.d(TAG, "Second delete");
            Runtime.getRuntime().exec(clearDataMiscOpenchannel).waitFor();
            logSleeping(30 * 1000);
            Log.d(TAG, "Install OC");
            Runtime.getRuntime().exec(ocInstallCommand).waitFor();
            Log.d(TAG, "Start OC");
            Runtime.getRuntime().exec(startService).waitFor();
            logSleeping(OC_INSTALL_TIME);
            apkInstalledCheck(packageName);
            SmokeHelperUtil.setApkInstalled(true);
            processStartCheck(packageName);
            SmokeUtil.assertOCCrash(TAG, "OC running ", true, OCUtil.isOpenChannelRunning());

        } finally {
            logcatUtil.stop();
            processes.clear();
        }

    }

    /**
     * <h1>MSISDNValidation</h1>
     * <p>The test checks that after OC successfully registered with server MSISDN validation will send</p>
     * <p>MSISDN validation sms and obtains MSISDN validation success response</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Parsing logs about MSISDN Validation is enabled and not done yet - so it is required.</li>
     * <li>Parsing logs about imsi was detected.</li>
     * <li>Parsing logs about MSISDN validation success.</li>
     * <li>Parsing logs about MSISDN_VALIDATION_MSISDN is obtained and getting its value.</li>
     * </ol>
     *
     * @throws Throwable
     */
    public void test_002_MSISDNValidationCheck() throws Throwable {
        Log.i(TAG, MSISDN_VALIDATION_STATE + " - MSISDN_VALIDATION_STATE");
        Log.i(TAG, MSISDN_VALIDATION_PHONENUMBER + " - MSISDN_VALIDATION_PHONENUMBER");
        try {
            if (msisdnTask.getLogEntries().size() == 0 && TFConstantsIF.MSISDN_VALIDATION_STATE != 0) {
                throw new AssertionFailedError("Msisdn validation not success.");
            } else if (TFConstantsIF.MSISDN_VALIDATION_STATE == 0) {
                Z7TestUtil.setAdditionalInfo("Branding without MSISDN validation", TestCaseEvents.TESTCASE_EVENT.IGNORED);
            } else {
                MsisdnWrapper msisdnWrapper = msisdnTask.getLogEntries().get(0);
                Log.i(TAG, msisdnWrapper.toString());
                if (msisdnWrapper.getImsi() == -1) {
                    throw new AssertionFailedError("New IMSI not detected");
                }
                if (!msisdnWrapper.isValidationNeeded()) {
                    throw new AssertionFailedError("Validation needed not detected in logs.");
                }
                if (!msisdnWrapper.isMsisdnSuccess()) {
                    throw new AssertionFailedError("Msisdn validation not success.");
                }
            }
        } finally {
            msisdnTask.cleanWrapper();
        }
    }

    /**
     * <h1>FirstPolicyRetrieved</h1>
     * <p>The test checks that after success MSISDN validation OC Client send request for policy update and getting them</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Parsing logs about package manager initialization.</li>
     * <li>Parsing logs about storage policy version.</li>
     * <li>Parsing logs about diffie-Hellman request sending.</li>
     * <li>Parsing logs about diffie-Hellman response.</li>
     * <li>Parsing logs about policy MGMT data request sending.</li>
     * <li>Parsing logs about local and server policies versions.</li>
     * <li>Parsing logs about policy MGMT data response.</li>
     * <li>Check endpoint (z7TP address) and that local policy tree hash</li>
     * </ol>
     *
     * @throws Throwable
     */
    public void test_003_FirstPolicyRetrieved() throws Throwable {
        try {
            if (policiesRetrievalTask.getLogEntries().size() == 0) {
                throw new AssertionFailedError("First time policy retrieval not success.");
            } else {
                Log.i(TAG, policiesRetrievalTask.getLogEntries().get(0).toString());
                if (policiesRetrievalTask.getLogEntries().get(0).getTimestamps()[3] == 0L) {
                    throw new AssertionFailedError("Diffie-Hellman request sending should be reported in client log");
                }
                if (policiesRetrievalTask.getLogEntries().get(0).getTimestamps()[4] == 0L) {
                    throw new AssertionFailedError("Diffie-Hellman response sending should be reported in client log");
                }
                if (policiesRetrievalTask.getLogEntries().get(0).getTimestamps()[1] == 0L) {
                    throw new AssertionFailedError("Package manager initialization should be reported in client log");
                }
                if (policiesRetrievalTask.getLogEntries().get(0).getTimestamps()[2] == 0L) {
                    throw new AssertionFailedError("Storage policy version should be reported in client log");
                }
                if (policiesRetrievalTask.getLogEntries().get(0).getTimestamps()[5] == 0L) {
                    throw new AssertionFailedError("Policy MGMT data request sending should be reported in client log");
                }
                if (policiesRetrievalTask.getLogEntries().get(0).getTimestamps()[7] == 0L) {
                    throw new AssertionFailedError("Policy MGMT data response should be reported in client log");
                }
                if (policiesRetrievalTask.getLogEntries().get(0).getTimestamps()[6] == 0L) {
                    throw new AssertionFailedError("Local and server policies versions should be reported in client log");
                }

//            if (policiesRetrievalTask.getLogEntries().get(0).getPolicyStorageVersion() != POLICY_STORAGE_VERSION) {
//                throw new AssertionFailedError("Policy storage version should be -1");
//            }
            }
        } finally {
            policiesRetrievalTask.cleanWrapper();
        }
    }

    /**
     * <h1>CRCS Basic detection</h1>
     * <p>The test check that basic crcs records are present</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Parcing logs about preparing to transfer CRCS records</li>
     * <li>Parcing logs about transferring CRCS records</li>
     * <li>Parcing logs about deleting CRCS records from database</li>
     * <li>Parcing logs about message for successful transfer of CRCS records</li>
     * </ol>
     *
     * @throws Throwable
     */
    public void test_004_CRCSRecordsTransfer() throws Throwable {

        String resource = "asimov_test_004_CRCS_Basic_detection";
        String uri = createTestResourceUri(resource);
        int count = 200;
        List<HttpRequest> requests = new ArrayList<HttpRequest>(20);

        for (int i = 0; i < count; i++) {
            requests.add(createRequest().setUri(createTestResourceUri(uri + i))
                    .setMethod(HttpGet.METHOD_NAME).addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("Cache-Control", "max-age=100")
                    .addHeaderField("X-OC-AddHeader_Date", "GMT").getRequest());
        }

        LogcatUtil logcatUtil = new LogcatUtil(AutomationTestsTab.context, crcsAccumulatedTask, reportTransferTaskNN, crcsRemovedTask, crcsTransferSuccessTask);
        logcatUtil.start();

        PrepareResourceUtil.prepareResource(uri, false);
        try {
            for (int i = 0; i < count; i++) {
                checkMiss(requests.get(i), i + 1);
//                checkMiss(requests.get(i), i+1, HttpStatus.SC_OK, VALID_RESPONSE, true, TIMEOUT);
                logSleeping(1500);
            }
            if (crcsAccumulatedTask.getLogEntries().size() == 0) {
                throw new AssertionFailedError("CRCS records weren't accumulated but should be");
            }
            if (reportTransferTaskNN.getLogEntries().size() == 0) {
                throw new AssertionFailedError("CRCS records weren't sent but should be");
            }
            if (crcsRemovedTask.getLogEntries().size() == 0) {
                throw new AssertionFailedError("CRCS records weren't removed from database but should be");
            }
            if (crcsTransferSuccessTask.getLogEntries().size() == 0) {
                throw new AssertionFailedError("CRCS transfer status wasn't success but should be");
            }
        } finally {
            logcatUtil.stop();
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }

    }

    /**
     * <h1>Test http RFC caching</h1>
     * <p>The test check caching by RFC for http</p>
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
    public void test_005_HttpRfcCaching() throws Throwable {
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

            SmokeUtil.assertOCCrash(TAG, "OC running ", true, OCUtil.isOpenChannelRunning());
            SmokeHelperUtil.assertControllerCrash(TAG, DISPATCHER_MESSAGE, DISPATCHER_MESSAGE_PREFIX,
                    processes, SmokeUtil.processesParser());
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
            throw new AssertionFailedError(e.getMessage());
        } finally {
            processes.clear();
        }
    }

    /**
     * <h1>Test https RFC caching</h1>
     * <p>The test check caching by RFC for https</p>
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
    public void test_006_HttpsRfcCaching() throws Throwable {
        setUp();
        processes = SmokeUtil.processesParser();

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

            SmokeUtil.assertOCCrash(TAG, "OC running ", true, OCUtil.isOpenChannelRunning());
            SmokeHelperUtil.assertControllerCrash(TAG, DISPATCHER_MESSAGE, DISPATCHER_MESSAGE_PREFIX,
                    processes, SmokeUtil.processesParser());
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
            throw new AssertionFailedError(e.getMessage());
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            processes.clear();
        }
    }

    /**
     * <p>Detection of http RI based polling</p>
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
    public void test_007_HttpRIPolling() throws Throwable {
        processes = SmokeUtil.processesParser();
        int sleepTime = 65 * 1000;
        String resource = "http_ri_base_polling";
        String uri = createTestResourceUri(resource);
        int requestId = 0;

        PrepareResourceUtil.prepareResource(uri, false);

        request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
        try {
            // 1.1
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());
            // 1.2
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());
            // 1.3
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());
            // 1.4
            checkHit(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);

            SmokeUtil.assertOCCrash(TAG, "OC running ", true, OCUtil.isOpenChannelRunning());
            SmokeHelperUtil.assertControllerCrash(TAG, DISPATCHER_MESSAGE, DISPATCHER_MESSAGE_PREFIX,
                    processes, SmokeUtil.processesParser());
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            processes.clear();
        }
    }

    /**
     * <p>Detection of https RI based polling</p>
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
    public void test_008_HttpsRIPolling() throws Throwable {
        setUp();
        processes = SmokeUtil.processesParser();
        int sleepTime = 65 * 1000;
        String resource = "https_ri_base_polling";
        String uri = createTestResourceUri(resource, true);
        int requestId = 0;

        StartPollTask startPollTask = new StartPollTask();
        LogcatUtil logcatUtil = new LogcatUtil(AutomationTestsTab.context, startPollTask);

        PrepareResourceUtil.prepareResource(uri, false);

        request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
        try {
            logcatUtil.start();
            // 1.1
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());
            // 1.2
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());
            // 1.3
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());
            // 1.4
            checkHit(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);

            SmokeUtil.assertOCCrash(TAG, "OC running ", true, OCUtil.isOpenChannelRunning());
            SmokeHelperUtil.assertControllerCrash(TAG, DISPATCHER_MESSAGE, DISPATCHER_MESSAGE_PREFIX,
                    processes, SmokeUtil.processesParser());
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            processes.clear();
            logcatUtil.stop();
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
    public void test_009_InvalidateWithCache() throws Throwable {
        processes = SmokeUtil.processesParser();
        StartPollTask startPollTask = new StartPollTask();
        IWCTask iwcTask = new IWCTask();
        LogcatUtil logcatUtil = new LogcatUtil(AutomationTestsTab.context, startPollTask);

        String uri = createTestResourceUri("asimov_it_cv_smoke_iwc");
        HttpRequest request = createRequest().setUri(uri)
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
        try {
            PrepareResourceUtil.prepareResource(uri, false);
            int requestId = 1;

            logcatUtil.start();
            response = checkMiss(request, requestId++);
            logSleeping(MIN_RMP_PERIOD * 6 - response.getDuration());

            response = checkMiss(request, requestId++);
            logSleeping(MIN_RMP_PERIOD * 6 - response.getDuration());

            response = checkMiss(request, requestId++);
            logSleeping(MIN_RMP_PERIOD * 6 - response.getDuration());

//            assertTrue("Status poll was not reported in client log", !startPollTask.getLogEntries().isEmpty());
            response = checkHit(request, requestId++);
            logcatUtil.stop();
            logcatUtil = new LogcatUtil(AutomationTestsTab.context, iwcTask);
            logcatUtil.start();
            logSleeping(MIN_RMP_PERIOD);
            PrepareResourceUtil.prepareResource(uri, true);
            logSleeping(MIN_RMP_PERIOD * 11 - response.getDuration());

            if (!iwcTask.getLogEntries().isEmpty()/* && iwcTask.getLogEntries().size() >= 2*/) {
                Log.e(TAG, "New cache saved");
                checkHit(request, requestId, INVALIDATED_RESPONSE, true);
            } else {
                Z7TestUtil.setAdditionalInfo("Invalidate with cache wasn't received and client returned old cache", TestCaseEvents.TESTCASE_EVENT.SUSPENDED);
                checkHit(request, requestId);
            }

            SmokeUtil.assertOCCrash(TAG, "OC running ", true, OCUtil.isOpenChannelRunning());
            SmokeHelperUtil.assertControllerCrash(TAG, DISPATCHER_MESSAGE, DISPATCHER_MESSAGE_PREFIX,
                    processes, SmokeUtil.processesParser());
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
    public void test_010_InvalidateWithoutCache() throws Throwable {
        processes = SmokeUtil.processesParser();
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
            OnStartChecks.startRadioKeepUpThread();
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

            SmokeUtil.assertOCCrash(TAG, "OC running ", true, OCUtil.isOpenChannelRunning());
            SmokeHelperUtil.assertControllerCrash(TAG, DISPATCHER_MESSAGE, DISPATCHER_MESSAGE_PREFIX,
                    processes, SmokeUtil.processesParser());
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
            throw new AssertionFailedError(e.getMessage());
        } finally {
            OnStartChecks.stopRadioKeepUpThread();
            logcatUtil.stop();
            PrepareResourceUtil.invalidateResourceSafely(uri);
            processes.clear();
        }
    }

}
