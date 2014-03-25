package com.seven.asimov.it.testcases;

import android.content.Context;
import android.net.wifi.WifiManager;
import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.MobileNetworkUtil;
import com.seven.asimov.it.utils.OCUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.logcat.tasks.dispatcherTasks.TcpKillTask;
import com.seven.asimov.it.utils.logcat.tasks.failoverTasks.FailoverStartTask;
import com.seven.asimov.it.utils.logcat.tasks.failoverTasks.FailoverStopTask;
import com.seven.asimov.it.utils.logcat.wrappers.FailoverStartWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.FailoverStopWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.TcpKillWrapper;
import com.seven.asimov.it.utils.pms.PMSUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TcpKillTestCase extends TcpDumpTestCase {
    private static final Logger logger = LoggerFactory.getLogger(TcpKillTestCase.class.getSimpleName());
    protected static final String MOBILE_FAILOVER_TYPE = "Mobile Networks";
    protected static final String MOBILE_NETWORKS_FAILOVER_POLICY_PATH = "@asimov@failovers@mobile_networks";
    protected int MIN_PERIOD = 60 * 1000;
    protected final String[] unreacheableRelayServer = {"su", "-c", TFConstantsIF.IPTABLES_PATH + " -t filter -I INPUT -m conntrack --ctorigdstport 7735 -j REJECT"};
    protected final String[] enableRelayServer = {"su", "-c", TFConstantsIF.IPTABLES_PATH + " -t filter -D INPUT -m conntrack --ctorigdstport 7735 -j REJECT"};

    protected int WIFI_NETWORK = 1;
    protected int MOBILE_NETWORK = 2;

    protected void checkTest(TcpKillTask tcpKillTask, TcpKillTask sigchldTask, TcpKillTask portsTask, boolean isTcpkillStart) throws Throwable {

        assertEquals("Going to kill ports", !isTcpkillStart, portsTask.getLogEntries().isEmpty());
        assertEquals("tcpkill was started with PID", !isTcpkillStart, tcpKillTask.getLogEntries().isEmpty());
        if (isTcpkillStart) {
            assertFalse("SIGCHLD was found", sigchldTask.getLogEntries().isEmpty());
            HashSet<Integer> tcpKillPids = new HashSet<Integer>();
            HashSet<Integer> sigchldPids = new HashSet<Integer>();
            int result = 0;
            for (TcpKillWrapper tcpKillEntry : tcpKillTask.getLogEntries()) {
                tcpKillPids.add(tcpKillEntry.getPID());
                logger.info("Tcp Kill PID: " + tcpKillEntry.getPID());
            }
            for (TcpKillWrapper sigchldEntry : sigchldTask.getLogEntries()) {
                sigchldPids.add(sigchldEntry.getPID());
                logger.info("SIGCHLD PID: " + sigchldEntry.getPID());
            }
            for (Integer tcpKillPid : tcpKillPids) {
                if (sigchldPids.contains(tcpKillPid)) {
                    ++result;
                }
            }
            assertTrue("SIGCHLD was received from corresponding PID", result > 0);
        }
    }

    protected void killProcess(final String... nProcess) throws Exception {
        Thread killProcessThread = new Thread() {
            @Override
            public void run() {
                Integer pidProcess;
                Map<String, Integer> processes = OCUtil.getRunningProcesses(true);
                for (String nameProcess : nProcess) {
                    if (nameProcess != null) {
                        pidProcess = processes.get(nameProcess);
                        logger.info("Process " + nameProcess + " pid = " + pidProcess);
                        String[] killPid = {"su", "-c", "kill " + pidProcess};
                        try {
                            Runtime.getRuntime().exec(killPid);
                        } catch (IOException io) {
                            logger.debug("Killing process is failed due to : " + ExceptionUtils.getStackTrace(io));
                        }
                    }
                }
            }
        };
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(killProcessThread);
        executorService.shutdown();
        if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
            executorService.shutdownNow();
        }
    }

    /**
     * function for switching between WiFi ang 3G
     *
     * @param type - network type: WIFI_NETWORK or MOBILE_NETWORK
     * @throws Throwable
     */
    protected void switchNetwork(int type) throws Throwable {
        switchNetwork(type, true);
    }

    /**
     * Overloading function for switching between WiFi ang 3G
     *
     * @param type        - network type: WIFI_NETWORK or MOBILE_NETWORK
     * @param sendRequest - if true - the trial request should be sent
     * @throws Throwable
     */
    protected void switchNetwork(int type, boolean sendRequest) throws Throwable {
        MobileNetworkUtil mobileNetworkHelper = MobileNetworkUtil.init(getContext());
        logger.info("Enter to SwitchNetwork");
        WifiManager wifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        logger.info("Step 1");
        switch (type) {
            case 1:
                logger.info("WIFI");
                if (!wifiManager.isWifiEnabled()) {
                    mobileNetworkHelper.switchMobileDataOnOff(false);
                    logger.info("Activate WIFI");
                    wifiManager.setWifiEnabled(true);
                    Thread.sleep(10 * 1000);
                }
                if (sendRequest) {
                    logger.info("WIFI request send");
                    sendRequest();
                }
                break;
            case 2:
                logger.info("3G");
                if (wifiManager.isWifiEnabled()) {
                    logger.info("Activate 3G");
                    wifiManager.setWifiEnabled(false);
                    mobileNetworkHelper.switchMobileDataOnOff(true);
                    Thread.sleep(MIN_PERIOD);
                }
                if (sendRequest) {
                    logger.info("3G request send");
                    sendRequest();
                }
                break;
            default:
                logger.info("Chosen network type is unreacheable in your region");
                break;
        }
    }

    private void sendRequest() throws Exception {
        int numberConnect = 5;
        while (!isConnectionPossible() && numberConnect > 0) {
            Thread.sleep(10 * 1000);
            numberConnect--;
        }
    }

    private boolean isConnectionPossible() throws Exception {
        boolean connectionPossible = true;
        URI uri = null;
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet();
            uri = new URI("http://" + AsimovTestCase.TEST_RESOURCE_HOST + "/");
            request.setURI(uri);
            client.execute(request);
        } catch (URISyntaxException e) {
            connectionPossible = false;
            logger.info("Failed to connect to" + uri);
        } catch (ClientProtocolException e) {
            connectionPossible = false;
            logger.info("Failed to connect to" + uri);
        } catch (IOException e) {
            connectionPossible = false;
            logger.info("Failed to connect to" + uri);
        }
        return connectionPossible;
    }

    protected void execute(String[] command) {
        logger.info("Runtime command execution");
        try {
            Runtime.getRuntime().exec(command).waitFor();
            TestUtil.sleep(MIN_PERIOD / 10);
        } catch (InterruptedException ie) {
            logger.info("Installing is failed due to : " + ExceptionUtils.getStackTrace(ie));
        } catch (IOException io) {
            logger.info("Updating IpTable is failed due to : " + ExceptionUtils.getStackTrace(io));
        }
    }

    protected void checkStartFailover(boolean shouldBe, FailoverStartTask failoverStartTask, String failoverType) throws Throwable {
        boolean result = false;
        if (!failoverStartTask.getLogEntries().isEmpty()) {
            for (FailoverStartWrapper failoverStartWrapper : failoverStartTask.getLogEntries()) {
                if (failoverStartWrapper.getFailoverType().equals(failoverType)) {
                    result = true;
                }
            }
        }
        if (shouldBe) {
            assertTrue("Failover didn't start but should be", result);
        } else {
            assertFalse("Failover started but shouldn't be", result);
        }
    }

    protected void checkStopFailover(boolean shouldBe, FailoverStopTask failoverStopTask, String failoverType) throws Throwable {
        boolean result = false;
        if (!failoverStopTask.getLogEntries().isEmpty()) {
            for (FailoverStopWrapper failoverStopWrapper : failoverStopTask.getLogEntries()) {
                if (failoverStopWrapper.getFailoverType().equals(failoverType)) {
                    result = true;
                }
            }
        }
        if (shouldBe) {
            assertTrue("Failover didn't stop but should be", result);
        } else {
            assertFalse("Failover stoped but shouldn't be", result);
        }
    }

    protected void cleanAllPolicy(String[] policyPath) throws Exception {
        boolean isCleaned = false;
        int retry = 3;
        do {
            try {
                PMSUtil.cleanPaths(policyPath);
                isCleaned = true;
            } catch (Exception e) {
                logger.info("Failed to remove the Policy");
            }
            Thread.sleep(10 * 1000);
            retry--;
        } while (!isCleaned && retry >= 0);
    }
}
