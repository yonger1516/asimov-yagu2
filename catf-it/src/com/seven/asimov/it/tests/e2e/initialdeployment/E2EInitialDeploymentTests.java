package com.seven.asimov.it.tests.e2e.initialdeployment;

import android.test.AssertionFailedError;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;
import com.seven.asimov.it.annotation.DeviceOnly;
import com.seven.asimov.it.testcases.E2EInitialDeploymentTestCase;
import com.seven.asimov.it.utils.OCUtil;
import com.seven.asimov.it.utils.logcat.wrappers.FirstTimePoliciesRetrievalWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.MsisdnWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.OCInitializationWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.ResponseFirewallPolicyReceivedWrapper;
import com.seven.asimov.it.utils.pms.PMSUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * !!!WARNING!!! ALL TESTS FROM THIS SUITE SHOULD BE EXECUTED ONE BY ONE!!!
 *
 * @author yrushchak, amykytenko
 */
public class E2EInitialDeploymentTests extends E2EInitialDeploymentTestCase {

    private static final String TAG = E2EInitialDeploymentTests.class.getSimpleName();

    @LargeTest
    @DeviceOnly
    public void test_000_E2E_Init() throws IOException, InterruptedException {
        PMSUtil.setRestServerDetected(true);
        try {
            notifyRestForTestsStart(SUITE_NAME);
        } catch (Exception e) {
            Log.e(TAG, "Tests start REST notification failed");
            e.printStackTrace();
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
    @LargeTest
    @DeviceOnly
    public void test_001_ClientStartUp() throws Exception {
        List<OCInitializationWrapper> ocInitializationWrapper = ocInitializationTask.getLogEntries();
        if (ocInitializationWrapper.isEmpty()) {
            throw new AssertionFailedError("OC com.seven.asimov not started.");
        }
        OCUtil.isOpenChannelRunning();
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
    @LargeTest
    @DeviceOnly
    public void test_002_ClientRegistrationWithServer() throws Exception {
        if (clientRegistrationWithServerTask.getLogEntries().size() == 0) {
            throw new AssertionFailedError("Client registration with server was not success");
        } else {
            Log.i(TAG, clientRegistrationWithServerTask.getLogEntries().get(0).toString());
            if (clientRegistrationWithServerTask.getLogEntries().get(0).getTimestamps()[1] == 0L) {
                throw new AssertionFailedError("Unauthenticated challenge receiving should be reported in client log or z7tp address in not zero");
            }
            if (clientRegistrationWithServerTask.getLogEntries().get(0).getTimestamps()[2] == 0L) {
                throw new AssertionFailedError("Diffie-Hellman request sending should be reported in client log");
            }
            if (clientRegistrationWithServerTask.getLogEntries().get(0).getTimestamps()[3] == 0L) {
                throw new AssertionFailedError("Diffie-Hellman response sending should be reported in client log");
            }
            if (clientRegistrationWithServerTask.getLogEntries().get(0).getZ7tpAddress().equals("0"))
                throw new AssertionFailedError("z7tp address should not be zero after Diffie-Hellman response");
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
     * <li>Check MSISDN validation at server side. {@link E2EInitialDeploymentTestCase#checkMsisdnInServer}</li>
     * </ol>
     *
     * @throws Exception
     */
    @LargeTest
    @DeviceOnly
    public void test_003_MSISDNValidation() throws Exception {
        if (msisdnTask.getLogEntries().size() == 0) {
            throw new AssertionFailedError("Msisdn validation not success.");
        } else {
            MsisdnWrapper msisdnWrapper = msisdnTask.getLogEntries().get(0);
            for (MsisdnWrapper wrapper : msisdnTask.getLogEntries()) {
                Log.i(TAG, String.format("wrapper: %s", wrapper));
            }
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
            checkMsisdnInServer(msisdnWrapper.getMsisdn());
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
     * <li>equals server policy tree hash on server side. {@link E2EInitialDeploymentTestCase#checkEndpointAndPolicyDataHash}
     * </ol>
     *
     * @throws Exception
     */
    @LargeTest
    @DeviceOnly
    public void test_004_FirstPolicyRetrieved() throws Exception {
        if (policiesRetrievalTask.getLogEntries().size() == 0) {
            throw new AssertionFailedError("First time policy retrieval not success.");
        } else {

            Log.i(TAG, policiesRetrievalTask.getLogEntries().get(0).toString());
            for (FirstTimePoliciesRetrievalWrapper wrapper : policiesRetrievalTask.getLogEntries()) {
                Log.i(TAG, Arrays.toString(wrapper.getTimestamps()));
            }

            if (policiesRetrievalTask.getLogEntries().get(0).getTimestamps()[1] == 0L) {
                throw new AssertionFailedError("Package manager initialization should be reported in client log");
            }
            if (policiesRetrievalTask.getLogEntries().get(0).getTimestamps()[2] == 0L) {
                throw new AssertionFailedError("Storage policy version should be reported in client log");
            }
            if (policiesRetrievalTask.getLogEntries().get(0).getTimestamps()[3] == 0L) {
                throw new AssertionFailedError("Diffie-Hellman request sending should be reported in client log");
            }
            if (policiesRetrievalTask.getLogEntries().get(0).getTimestamps()[4] == 0L) {
                throw new AssertionFailedError("Diffie-Hellman response sending should be reported in client log");
            }
            if (policiesRetrievalTask.getLogEntries().get(0).getTimestamps()[5] == 0L) {
                throw new AssertionFailedError("Policy MGMT data request sending should be reported in client log");
            }
            if (policiesRetrievalTask.getLogEntries().get(0).getTimestamps()[6] == 0L) {
                throw new AssertionFailedError("Local and server policies versions should be reported in client log");
            }
            if (policiesRetrievalTask.getLogEntries().get(0).getTimestamps()[7] == 0L) {
                throw new AssertionFailedError("Policy MGMT data response should be reported in client log");
            }
            if (policiesRetrievalTask.getLogEntries().get(0).getPolicyStorageVersion() != POLICY_STORAGE_VERSION) {
                throw new AssertionFailedError("Policy storage version should be -1");
            }
            checkEndpointAndPolicyDataHash(policiesRetrievalTask.getLogEntries().get(0).getLocalPolicyHash());
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
    @LargeTest
    @DeviceOnly
    public void test_005_FirstFirewallPolicyRetrieved() throws Exception {
        if (firewallPolicyMgmtDataRequestTask.getLogEntries().size() == 0)
            throw new AssertionFailedError("Firewall policy mgmt data request was not sent to server");
        if (firewallPolicyMgmtDataResponseTask.getLogEntries().size() == 0)
            throw new AssertionFailedError("Firewall policy mgmt data response was not received from server");
        if (!responseFirewallPolicyReceivedTask.getLogEntries().isEmpty()) {
            for (ResponseFirewallPolicyReceivedWrapper wrapper : responseFirewallPolicyReceivedTask.getLogEntries()) {
                if (!wrapper.getErrorCode().equals("0"))
                    Log.w(TAG, "One of firewall policies response was with error");
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

    @LargeTest
    public void test_999_E2E_CleanUp() {
        try {
            notifyRestForTestEnd(SUITE_NAME);
        } catch (Exception e) {
            Log.e(TAG, "Tests end REST notification failed");
            e.printStackTrace();
        }
    }
}
