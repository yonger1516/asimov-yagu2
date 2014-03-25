package com.seven.asimov.it.testcases;

import android.content.Context;
import android.net.wifi.WifiManager;
import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.MobileNetworkUtil;
import com.seven.asimov.it.utils.OCUtil;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.logcat.tasks.failoverTasks.FailoverStartTask;
import com.seven.asimov.it.utils.logcat.tasks.failoverTasks.FailoverStopTask;
import com.seven.asimov.it.utils.logcat.tasks.failoverTasks.FloIpcMessageTask;
import com.seven.asimov.it.utils.logcat.wrappers.FailoverStartWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.FailoverStopWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.FloIpcMessageWrapper;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;

public class FailoverTestCase extends TcpDumpTestCase {
    private static final Logger logger = LoggerFactory.getLogger(FailoverTestCase.class.getSimpleName());
    private MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());

    protected static final String WIFI_FAILOVER_POLICY_PATH = "@asimov@failovers@wifi";
    protected static final String MOBILE_NETWORKS_FAILOVER_POLICY_PATH = "@asimov@failovers@mobile_networks";
    protected static final String WIFI_FAILOVER_TYPE = "Wifi";
    protected static final String MOBILE_FAILOVER_TYPE = "Mobile Networks";

    protected int MIN_PERIOD = 60 * 1000;
    protected int WIFI_NETWORK = 1;
    protected int MOBILE_NETWORK = 2;

    /**
     * Overloaded function for testing disabled failover
     *
     * @param name       - policy name
     * @param path       - branch in server for policy
     * @param value      - policy value
     * @param isAnactive - should dispatchers start(if should start - true, else - false)
     * @throws Throwable
     */
    protected void startTestForDisabledFailover(String name, String path, String value, boolean isAnactive) throws Throwable {
        Policy[] policies = {new Policy(name, value, path, true)};
        try {
            PMSUtil.cleanPaths(new String[]{path});
            PMSUtil.addPolicies(policies);
            Thread.sleep(2 * 60 * 1000);
            logger.info("assertControllerCrash.. ");
            assertControllerCrash(isAnactive);
        } finally {
            logger.info("Clear properties.. ");
            PMSUtil.cleanPaths(new String[]{path});
        }
    }

    /**
     * Function for testing disabled failover
     *
     * @param name       - policy name
     * @param path       - branch in server for policy
     * @param value      - policy value
     * @param isAnactive - should dispatchers start(if should start - true, else - false)
     * @param timeout    - delay for thread sleeping
     * @param pathUri    - uri for request
     * @throws Throwable
     */
    protected void startTestForDisabledFailover(String name, String path, String value, boolean isAnactive, long timeout, String pathUri) throws Throwable {

        String uri = createTestResourceUri(pathUri);
        PrepareResourceUtil.prepareResource(uri, false);

        final HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("Cache-control", "max-age=100")
                .getRequest();
        try {
            checkMiss(request, 1);
            startTestForDisabledFailover(name, path, value, isAnactive);
            Thread.sleep(timeout);
            checkMiss(request, 2);
        } finally {
            PrepareResourceUtil.prepareResource(uri, true);
        }
    }

    /**
     * Function for checking dispatchers in processes
     *
     * @param isAnactive - if true - dispatchers should restart
     * @throws Throwable
     */
    protected void assertControllerCrash(boolean isAnactive) throws Throwable {
        int expected = 2;
        if (isAnactive) {
            expected = 5;
        }
        ArrayList<String> result = processesParser();
        logger.info("the size is " + result.size());
        assertEquals("The dispatchers", expected, result.size());
    }

    private static ArrayList<String> processesParser() {
        logger.info("Process parser");
        Map<String, Integer> processes = OCUtil.getOcProcesses(false);
        ArrayList<String> result = new ArrayList<String>();
        if (processes.get("ocdnsd") != null) {
            logger.info("ocdnsd=" + processes.get("ocdnsd"));
            result.add("ocdnsd=" + processes.get("ocdnsd"));

            logger.info("ochttpd=" + processes.get("ochttpd"));
            result.add("ochttpd=" + processes.get("ochttpd"));

            logger.info("ocshttpd=" + processes.get("ocshttpd"));
            result.add("ocshttpd=" + processes.get("ocshttpd"));

        }
        result.add("occ=" + processes.get("occ"));
        result.add("com.seven.asimov=" + processes.get("com.seven.asimov"));
        return result;
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

    protected void checkFLOMessage(FloIpcMessageTask floIpcMessageTask, boolean shouldBeSend) {
        boolean result = false;
        if (!floIpcMessageTask.getLogEntries().isEmpty()) {
            for (FloIpcMessageWrapper floIpcMessageWrapper : floIpcMessageTask.getLogEntries()) {
                if (floIpcMessageWrapper.getMessage().equals("FLO") && floIpcMessageWrapper.getActionFLO() == 1) {
                    result = true;
                }
            }
        }
        if (shouldBeSend) {
            assertTrue("FLO message should be sent from Engine to Controller", result);
        } else {
            assertFalse("FLO message should not be sent from Engine to Controller", result);
        }
    }

    protected void checkIPCMessage(FloIpcMessageTask floIpcMessageTask, boolean shouldBeSend) {
        boolean result = false;
        if (!floIpcMessageTask.getLogEntries().isEmpty()) {
            for (FloIpcMessageWrapper floIpcMessageWrapper : floIpcMessageTask.getLogEntries()) {
                if (floIpcMessageWrapper.getMessage().equals("IPC")) {
                    result = true;
                }
            }
        }
        if (shouldBeSend) {
            assertTrue("IPC message should be sent from Engine to Controller", result);
        } else {
            assertFalse("IPC message should not be sent from Engine to Controller", result);
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

    protected void switchNetwork(int type, boolean sendRequest) throws Throwable {
        switchNetwork(type, sendRequest, MIN_PERIOD);
    }

    /**
     * Overloading function for switching between WiFi ang 3G
     *
     * @param type        - network type: WIFI_NETWORK or MOBILE_NETWORK
     * @param sendRequest - if true - the trial request should be sent
     * @throws Throwable
     */
    protected void switchNetwork(int type, boolean sendRequest, long sleepTime) throws Throwable {
        logger.info("Enter to SwitchNetwork");
        WifiManager wifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        logger.info("Step 1");
        switch (type) {
            case 1:
                logger.info("WIFI");
                if (!wifiManager.isWifiEnabled()) {
                    mobileNetworkUtil.switchMobileDataOnOff(false);
                    logger.info("Activate WIFI");
                    wifiManager.setWifiEnabled(true);
                    Thread.sleep(sleepTime);
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
                    mobileNetworkUtil.switchMobileDataOnOff(true);
                    Thread.sleep(sleepTime);
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

    protected void sendRequest() throws Exception {
        int numberConnect = 5;
        while (!isConnectionPossible() && numberConnect > 0) {
            Thread.sleep(10 * 1000);
            numberConnect--;
        }
    }

    protected boolean isConnectionPossible() throws Exception {
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
            logger.debug("Failed to connect to" + uri);
        } catch (ClientProtocolException e) {
            connectionPossible = false;
            logger.debug("Failed to connect to" + uri);
        } catch (IOException e) {
            connectionPossible = false;
            logger.debug("Failed to connect to" + uri);
        }
        return connectionPossible;
    }
}
