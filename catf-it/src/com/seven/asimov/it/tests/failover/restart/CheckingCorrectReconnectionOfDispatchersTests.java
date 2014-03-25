package com.seven.asimov.it.tests.failover.restart;


import com.seven.asimov.it.testcases.FailoverTestCase;
import com.seven.asimov.it.utils.IpTablesUtil;
import com.seven.asimov.it.utils.MobileNetworkUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.failoverTasks.FailoverStartTask;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;

/**
 * Failover Test for https://jira.seven.com/requests/browse/ASMV-14495:
 * ({@link CheckingCorrectReconnectionOfDispatchersTests#test_001_CheckingCorrectReconnectionOfDispatchers()  Test1)
 */
public class CheckingCorrectReconnectionOfDispatchersTests extends FailoverTestCase {
    private MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());

    /**
     * Pre-requests:
     * 1.Active interface is WiFi
     * 2. Set branding parameters:
     * <p/>
     * client.openchannel.roaming_wifi_failover.enabled=1
     * client.openchannel.roaming_wifi_failover.actions=1
     * 3. Connection to Relay should be unavailable.
     * <p/>
     * Steps:
     * 1. Switch mobile interface on
     * 2. Establish connection to Relay
     * 3. Switch mobile interface off, wifi - on
     * Expected results: dispatchers shold be reconnected correctly
     */
    public void test_001_CheckingCorrectReconnectionOfDispatchers() throws Throwable {
        mobileNetworkUtil.onWifiOnly();
        PMSUtil.addPolicies(new Policy[]{new Policy("enabled", "true", WIFI_FAILOVER_POLICY_PATH, true),
                new Policy("actions", "1", WIFI_FAILOVER_POLICY_PATH, true)});
        FailoverStartTask failoverStartTask = new FailoverStartTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), failoverStartTask);
        logcatUtil.start();
        try {
            assertControllerCrash(true);
            switchNetwork(MOBILE_NETWORK);
            IpTablesUtil.banRelayServer(true);
            switchNetwork(WIFI_NETWORK);
            TestUtil.sleep(60 * 1000);
            assertControllerCrash(false);
            logcatUtil.stop();
            checkStartFailover(true, failoverStartTask, WIFI_FAILOVER_TYPE);
            IpTablesUtil.banRelayServer(false);
            switchNetwork(MOBILE_NETWORK);
            switchNetwork(WIFI_NETWORK);
            Thread.sleep(60 * 1000);
            assertControllerCrash(true);
        } finally {
            logcatUtil.stop();
            PMSUtil.cleanPaths(new String[]{WIFI_FAILOVER_POLICY_PATH});
        }
    }
}
