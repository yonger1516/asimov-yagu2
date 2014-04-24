package com.seven.asimov.it;

import android.content.Context;
import android.os.Build;
import android.test.AssertionFailedError;
import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.OCUtil;
import com.seven.asimov.it.utils.SmokeUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.*;
import com.seven.asimov.it.utils.logcat.wrappers.MsisdnWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.ResponseFirewallPolicyReceivedWrapper;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import static com.seven.asimov.it.base.AsimovTestCase.*;

/**
 * <b>This class dedicate to provide checks before start OC Test Framework, and includes following methods:</b>
 * {@link com.seven.asimov.it.OnStartChecks#checkOcAndDispathcersCrash}
 * {@link com.seven.asimov.it.OnStartChecks#checkTestRunnerAvailable}
 * {@link com.seven.asimov.it.OnStartChecks#checkTestPort}
 * {@link com.seven.asimov.it.OnStartChecks#checkClientRegistrationWithServer}
 * {@link com.seven.asimov.it.OnStartChecks#checkMSISDNvalidation}
 * {@link com.seven.asimov.it.OnStartChecks#checkFirstTimePolicyRetrieved}
 * <p/>
 * <b>These checks are called {@link IntegrationTestRunnerGa} by:</b>
 * {@link com.seven.asimov.it.OnStartChecks#fullStartCheck}
 */
public enum OnStartChecks {

    INSTACE;

    private static final Logger logger = LoggerFactory.getLogger(OnStartChecks.class.getSimpleName());
    private static final long POLICY_STORAGE_VERSION = -1L;
    private static final String OC_APK_FILENAME = "/sdcard/asimov-signed.apk";
    private LogcatUtil logcatUtil;
    private static MsisdnTask msisdnTask = new MsisdnTask();
    private static OCInitializationTask ocInitializationTask = new OCInitializationTask();
    private static FirstTimePoliciesRetrievalTask policiesRetrievalTask = new FirstTimePoliciesRetrievalTask();
    private static ClientRegistrationWithServerTask clientRegistrationWithServerTask = new ClientRegistrationWithServerTask();
    private static FirewallPolicyMgmtDataRequestTask firewallPolicyMgmtDataRequestTask = new FirewallPolicyMgmtDataRequestTask();
    private static FirewallPolicyMgmtDataResponseTask firewallPolicyMgmtDataResponseTask = new FirewallPolicyMgmtDataResponseTask();
    private static ResponseFirewallPolicyReceivedTask responseFirewallPolicyReceivedTask = new ResponseFirewallPolicyReceivedTask();
    private static StartingFirewallTask startingFirewallTask = new StartingFirewallTask();
    private static final int OC_INSTALL_TIME = 7 * 60 * 1000;

    private static volatile boolean runThread;
    private static Thread radioKeep;

    /**
     * <h1>Method which provide all checks for running tests</h1>
     *
     * @throws Exception
     */
    public void fullStartCheck() throws Exception {
        checkClientRegistrationWithServer();
        checkMSISDNvalidation();

        checkSchemaVersion();
        //checkFirstTimePolicyRetrieved();
        //  checkFirstFirewallPolicyRetrieved();
        checkOcAndDispathcersCrash();
        checkTestRunnerAvailable();
        checkTestPort();
    }

    private void checkSchemaVersion() throws IOException, InterruptedException {

        String version=OCUtil.getSchemaVersion();

        if (version.toString().equals("")){
            logger.error("Can't get client schema version");
        }else{
            logger.debug("Client schema version :" + version.toString());
        }

    }

    /**
     * <h1>Client startup client installs and OC processes start up /</h1>
     * <p>The test checks that OC was installed and started up correctly.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Checking that all process are started.</li>
     * <li>"occ", "com.seven.asimov", "dns", "https", "http".</li>
     * <li>or "occ", "com.seven.asimov", "ocdnsd", "ocshttpd", "ochttpd".</li>
     * </ol>
     *
     * @throws Exception
     */
    private void checkOcAndDispathcersCrash() throws Exception {
        ArrayList<String> processes;
        processes = SmokeUtil.processesParser();
        SmokeUtil.assertOCCrash("Check OC live", "OC running", true, OCUtil.isOpenChannelRunning());
        SmokeUtil.assertControllerCrash("Check dispatchers", "Dispatchers crash", processes, SmokeUtil.processesParser());

    }

    /**
     * <h1>Method which provide check test runner available</h1>
     *
     * @throws Exception
     */
    private void checkTestRunnerAvailable() throws Exception {
        final String RESOURCE_URI = "checkTestRunnerAvailable";
        try {
            HttpRequest request = createRequest().setUri(createTestResourceUri(RESOURCE_URI)).setMethod("GET").getRequest();
            HttpResponse response = sendRequest(request);
            if (response.getStatusCode() == -1 || response.getDuration() > (3 * 60 * 1000)) {
                throw new Exception("Test runner are unavailable" + AsimovTestCase.TEST_RESOURCE_HOST);
            }
        } catch (Exception e) {
            logger.error("Test runner are unavailable" + AsimovTestCase.TEST_RESOURCE_HOST + ExceptionUtils.getFullStackTrace(e));
            throw new Exception("Test runner are unavailable " + e.getMessage());
        }
    }

    /**
     * <h1>Method which check</h1>
     *
     * @throws Exception
     */
    private void checkTestPort() throws Exception {
        Socket socket = new Socket(TFConstantsIF.EXTERNAL_IP, TFConstantsIF.RELAY_PORT);
        InetAddress inetAddress = socket.getInetAddress();
        if (inetAddress == null) {
            logger.error("Relay " + TFConstantsIF.EXTERNAL_IP + " are unavailable on " + TFConstantsIF.RELAY_PORT + " port");
            throw new Exception("Relay in " + TFConstantsIF.RELAY_PORT + " are unavailable");
        }
    }

    /**
     * <h1>Client registration with server</h1>
     * <p>The test checks that after OC was installed and all processes are online OC will send request for</p>
     * <p>registration on server and z7tp address obtaining</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Parsing logs about <--- Received an unauthenticated challenge.</li>
     * <li>Parsing logs about Diffie-Hellman request sending should be reported in client.</li>
     * <li>Parsing logs about Diffie-Hellman response sending should be reported in client.</li>
     * <li>Parsing logs about z7tp address should not be zero after Diffie-Hellman response.</li>
     * </ol>
     *
     * @throws Exception
     */
    private void checkClientRegistrationWithServer() throws Exception {
        if (clientRegistrationWithServerTask.getLogEntries().size() == 0) {
            throw new Exception("Client registration with server was not success");
        } else {
            logger.info(clientRegistrationWithServerTask.getLogEntries().get(0).toString());
            if (clientRegistrationWithServerTask.getLogEntries().get(0).getTimestamps()[1] == 0L) {
                throw new Exception("Unauthenticated challenge receiving should be reported in client log or z7tp address in not zero");
            }
            if (clientRegistrationWithServerTask.getLogEntries().get(0).getTimestamps()[2] == 0L) {
                throw new Exception("Diffie-Hellman request sending should be reported in client log");
            }
            if (clientRegistrationWithServerTask.getLogEntries().get(0).getTimestamps()[3] == 0L) {
                throw new Exception("Diffie-Hellman response sending should be reported in client log");
            }
            if (clientRegistrationWithServerTask.getLogEntries().get(0).getZ7tpAddress().equals("0"))
                throw new Exception("z7tp address should not be zero after Diffie-Hellman response");
        }
    }

    /**
     * <h1>MSISDNValidation</h1>
     * <p>The test checks that after OC succesfully rqistered with server it will send</p>
     * <p>MSISDN validation sms and obtains MSISDN validation success response</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Parsing logs about MSISDN Validation is enabled and not done yet - so it is required.</li>
     * <li>Parsing logs about imsi was detected.</li>
     * <li>Parsing logs about MSISDN validation success.</li>
     * <li>Parsing logs about MSISDN_VALIDATION_MSISDN is obtained and getting its value.</li>
     * </ol>
     *
     * @throws Exception
     */
    private void checkMSISDNvalidation() throws Exception {
        Integer validation = TFConstantsIF.MSISDN_VALIDATION_STATE;
        if (validation != null && validation == 1) {
            if (msisdnTask.getLogEntries().size() == 0) {
                throw new Exception("Msisdn validation not success.");
            } else {
                MsisdnWrapper msisdnWrapper = msisdnTask.getLogEntries().get(0);
                logger.info(msisdnWrapper.toString());
                if (msisdnWrapper.getImsi() == -1) {
                    throw new Exception("New IMSI not detected");
                }
                if (!msisdnWrapper.isValidationNeeded()) {
                    throw new Exception("Validation needed not detected in logs.");
                }
                if (!msisdnWrapper.isMsisdnSuccess()) {
                    throw new Exception("Msisdn validation not success.");
                }
            }
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
     * @throws Exception
     */
    private void checkFirstTimePolicyRetrieved() throws Exception {
        if (policiesRetrievalTask.getLogEntries().size() == 0) {
            throw new Exception("First time policy retrieval not success.");
        } else {
            logger.info(policiesRetrievalTask.getLogEntries().get(0).toString());
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
        }
    }

    /**
     * <h1>FirstFirewallPolicyRetrieved</h1>
     * <p>The test checks that after success MSISDN validation OC Client send request for firewall policy update and getting them</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Parsing logs about Sending a firewall policy mgmt data request.</li>
     * <li>Parsing logs about Received a firewall policy mgmt server response.</li>
     * <li>Parsing logs about Response 'firewall policy' received, error and status code states.</li>
     * <li>Parsing logs about Starting Firewall.</li>
     * </ol>
     *
     * @throws Exception
     */

    private void checkFirstFirewallPolicyRetrieved() throws Exception {
        if (firewallPolicyMgmtDataRequestTask.getLogEntries().size() == 0)
            throw new AssertionFailedError("Firewall policy mgmt data request was not sent to server");
        if (firewallPolicyMgmtDataResponseTask.getLogEntries().size() == 0)
            throw new AssertionFailedError("Firewall policy mgmt data response was not received from server");
        if (!responseFirewallPolicyReceivedTask.getLogEntries().isEmpty()) {
            for (ResponseFirewallPolicyReceivedWrapper wrapper : responseFirewallPolicyReceivedTask.getLogEntries()) {
                if (!wrapper.getErrorCode().equals("0"))
                    logger.warn("One of firewall policies response was with error");
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        if (startingFirewallTask.getLogEntries().size() == 0) {
            if (!responseFirewallPolicyReceivedTask.getLogEntries().isEmpty()) {
                stringBuilder.append("During test there was a firewall responses with errors: ");
                for (ResponseFirewallPolicyReceivedWrapper wrapper : responseFirewallPolicyReceivedTask.getLogEntries()) {
                    stringBuilder.append(wrapper.toString()).append("\n");
                }
            }
            throw new AssertionFailedError("Firewall did not started " + stringBuilder.toString());
        }
    }

    /**
     * Method which install OC from sdcard, and write logcat
     *
     * @throws Exception
     */
    public void installOC(final Context context, boolean check) throws Exception {
        try {
            String[] uninstallOc = {"su", "-c", "pm uninstall com.seven.asimov"};
            String[] installOc = {"su", "-c", "pm install -r " + OC_APK_FILENAME};
            String[] killAllOcc = {"su", "-c", "killall -9 occ"};
            String[] startService = new String[]{"su", "-c", "am startservice com.seven.asimov/.ocengine.OCEngineService"};
            Runtime.getRuntime().exec(uninstallOc).waitFor();
//            Runtime.getRuntime().exec(killAllOcc).waitFor();
            TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
            logcatUtil = new LogcatUtil(context, msisdnTask, ocInitializationTask, policiesRetrievalTask, clientRegistrationWithServerTask, firewallPolicyMgmtDataRequestTask, firewallPolicyMgmtDataResponseTask, responseFirewallPolicyReceivedTask, startingFirewallTask);
            logcatUtil.start();
            logger.info("Logcat Util has started sinse " + new Date() + " Current device is: " + Build.MODEL + " Current SDK is: " + Build.VERSION.SDK_INT);
            File file = new File(OC_APK_FILENAME);
            if (!file.exists()) {
                throw new FileNotFoundException("OC apk doesn't exist ob SD card or name should be asimov-signed.apk");
            }
            startRadioKeepUpThread();
            logger.debug("Installing OC Client");
            Runtime.getRuntime().exec(installOc).waitFor();
            Thread.sleep(10000);
            logger.debug("Sending intent for start OC Engine");
            Runtime.getRuntime().exec(startService).waitFor();

            if (check) {
                Thread.sleep(OC_INSTALL_TIME);
            }


        } finally {
            logcatUtil.stop();
            stopRadioKeepUpThread();
        }
    }

    private static void createRadioKeepUpThread() {
        radioKeep = new Thread(new Runnable() {
            @Override
            public void run() {
                String resource = "ping";
                String uri = createTestResourceUri(resource);

                HttpRequest request;
                HttpResponse response;
                while (runThread) {
                    try {
                        request = createRequest()
                                .setUri(uri)
                                .setMethod(HttpGet.METHOD_NAME)
                                .addHeaderField("X-OC-ContentEncoding", "identity")
                                .addHeaderField("Random", TestUtil.generationRandomString()).getRequest();
                        logger.debug("PING!!!");
                        response = sendRequestWithoutLogging(request);
                        TestUtil.sleep(3 * 1000 - response.getDuration());
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
                logger.debug("Thread stop");
            }
        });
    }

    public static void startRadioKeepUpThread() {
        if (!runThread) {
            createRadioKeepUpThread();
            runThread = true;
            logger.debug("Start PING Thread");
            radioKeep.start();
        }
    }

    public static void stopRadioKeepUpThread() {
        logger.debug("Stop PING Thread");
        runThread = false;
        try {
            radioKeep.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
