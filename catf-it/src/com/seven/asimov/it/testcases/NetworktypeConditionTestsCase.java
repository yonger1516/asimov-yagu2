package com.seven.asimov.it.testcases;

import android.content.Context;
import android.net.wifi.WifiManager;
import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.MobileNetworkUtil;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.conditionTasks.NetworktypeConditionTask;
import com.seven.asimov.it.utils.logcat.tasks.policyTasks.PolicyAddedTask;
import com.seven.asimov.it.utils.logcat.wrappers.PolicyWrapper;
import com.seven.asimov.it.utils.pms.PMSUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NetworktypeConditionTestsCase extends TcpDumpTestCase {
    private static final Logger logger = LoggerFactory.getLogger(NetworktypeConditionTestsCase.class.getSimpleName());

    protected static LogcatUtil logcatUtil;
    protected final List<String> properties = new ArrayList<String>();
    private String dropSessionProperty;
    protected List<Property> managedProperties = new ArrayList<Property>();
    int policyApplied = 0;
    ArrayList<PolicyWrapper> filteredLogEntries = new ArrayList<PolicyWrapper>();

    HttpRequest request;
    protected static final int WIFI_NETWORK = 1;
    protected static final int MOBILE_NETWORK = 2;
    protected static final int WIMAX_NETWORK = 3;
    protected static final int MOBILE_3GPP_NETWORK = 4;
    protected static final int MOBILE_3GPP2_NETWORK = 5;
    protected static final int MOBILE_LTE_NETWORK = 6;
    protected static final int MOBILE_IDEN_NETWORK = 7;

    protected static final long MINUTE = 60 * 1000;
    protected static final long MIN_PERIOD = 30 * 1000;


    protected enum TimerState {
        DISABLED_EXITED,
        EXITED_ENTERED,
        ENTERED_EXITED,
        EXITED_DISABLED
    }

    protected class State {
        private TimerState timerState;
        private boolean present;

        public State(TimerState timerState, boolean present) {
            this.timerState = timerState;
            this.present = present;
        }
    }

    protected void networktypeConditionTest(List<Property> properties) throws Throwable {

        for (int i = 0; i < properties.size(); i++) {
            switchNetwork(properties.get(i).getNetworkType());
            switch (properties.get(i).getNetworkType()) {
                case 1:
                    logger.info("WIFI_NETWORK");
                    networktypeConditionTest(properties.get(i), properties.get(i).getApplied(), true);
                    break;
                case 2:
                    logger.info("MOBILE_NETWORK");
                    networktypeConditionTest(properties.get(i), properties.get(i).getApplied(), true);
                    break;
                case 3:
                    logger.info("WIMAX_NETWORK");
                    networktypeConditionTest(properties.get(i), properties.get(i).getApplied(), false);
                    break;
                case 4:
                    logger.info("MOBILE_3GPP_NETWORK");
                    networktypeConditionTest(properties.get(i), properties.get(i).getApplied(), false);
                    break;
                case 5:
                    logger.info("MOBILE_3GPP2_NETWORK");
                    networktypeConditionTest(properties.get(i), properties.get(i).getApplied(), false);
                    break;
                case 6:
                    logger.info("MOBILE_LTE_NETWORK");
                    networktypeConditionTest(properties.get(i), properties.get(i).getApplied(), false);
                    break;
                case 7:
                    logger.info("MOBILE_IDEN_NETWORK");
                    networktypeConditionTest(properties.get(i), properties.get(i).getApplied(), false);
                    break;
                default:
                    logger.info("Wrong value of network type");
                    networktypeConditionTest(properties.get(i), properties.get(i).getApplied(), true);
                    break;
            }
        }
    }

    protected void networktypeConditionTest(Property property, boolean applied, boolean networktypeCondition) throws Throwable {
        int entries = 0;

        if (applied) {
            entries = 1;
        }
        PolicyAddedTask policyAddedTask = new PolicyAddedTask();
        NetworktypeConditionTask networktypeConditionTask = new NetworktypeConditionTask();

        logcatUtil = new LogcatUtil(getContext(),
                policyAddedTask,
                networktypeConditionTask);
        logcatUtil.start();
        try {
            logger.info("Start logcatUtil");
            sendPolicy(property, MINUTE * 2);
            if (networktypeCondition) {
                checkNetworkCondition(entries, networktypeConditionTask);
            }


        } finally {
            clearProperties();
            managedProperties.clear();
            logcatUtil.stop();
            filteredLogEntries.clear();
            policyApplied = 0;
        }

    }


    private void sendPolicy(Property p, long SLEEP_TIME) throws Throwable {
        logger.info("send policy");
        properties.add(PMSUtil.createPersonalScopeProperty(p.getName(), p.getPath(), p.getValue(), true, true));
        Thread.sleep(SLEEP_TIME);
    }

    private void checkNetworkCondition(int entries, NetworktypeConditionTask networktypeConditionTask) throws Throwable {
        logger.info(networktypeConditionTask.getLogEntries().size() + " - the count of networktype condition entries in log");
        assertEquals("The networktype condition didn't activate", entries, networktypeConditionTask.getLogEntries().size());
        tryToConnect(1, false);
    }

    protected void operateDropSessionProperty(boolean add) throws Throwable {
        if (add) {
            dropSessionProperty = PMSUtil.createPersonalScopeProperty("drop_sessions", "@asimov@application@com.seven.asimov.it@scripts@script_networktype@actions", "port_range=80:100 package=com\\.android.*", true);
        } else {
            deleteProperty(dropSessionProperty);
        }
        Thread.sleep(MIN_PERIOD);
    }

    protected void switchNetwork(int type) throws Throwable {
        switchNetwork(type, true);
    }

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
                    Thread.sleep(MINUTE);
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
                    Thread.sleep(MINUTE);
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

    protected void tryToConnect(int number, boolean largeRequest) throws Exception {
        String[] characters = {"This is the house that Jack built.",
                "This is the malt",
                "That lay in the house that Jack built.",
                "This is the rat",
                "That ate the malt",
                "That lay in the house that Jack built",
                "This is the cat",
                "That killed the rat",
                "That ate the malt",
                "That lay in the house that Jack built"};
        Random rng = new Random();

        String pathEnd = "test_asimov";
        String uri = createTestResourceUri(pathEnd);

        try {
            PrepareResourceUtil.prepareResource(uri, false);
            for (int i = 1; i < number + 1; i++) {
                String header = characters[rng.nextInt(10)];

                if (largeRequest) {
                    request = createRequest().setUri(createTestResourceUri(uri))
                            .setMethod(HttpGet.METHOD_NAME).addHeaderField("X-OC-ContentEncoding", "identity")
                            .addHeaderField("X-OC-AddHeader_Date", "GMT")
                            .addHeaderField("X-OC-ResponseContentSize", "50000").getRequest();
                } else {
                    request = createRequest()
                            .setMethod("GET")
                            .setUri(uri)
                            .addHeaderField("X-OC-ContentEncoding", "identity")
                            .addHeaderField("SomeHeader", header)
                            .getRequest();
                }

                sendMiss(i, request);
            }
        } catch (SocketTimeoutException socketTimeoutException) {
            logger.debug("Connection failed due to " + ExceptionUtils.getStackTrace(socketTimeoutException));
        } catch (Exception e) {
            logger.debug("Response duration");
        } finally {
            PrepareResourceUtil.prepareResource(uri, true);
        }
    }

    protected void clearProperties() {
        for (String property : properties) {
            deleteProperty(property);
        }
        properties.clear();
        TestUtil.sleep(10 * 1000);
    }

    private void deleteProperty(String id) {
        try {
            PMSUtil.deleteProperty(id);
        } catch (Throwable t) {
            logger.debug(ExceptionUtils.getStackTrace(t));
        }
    }

    protected class Property {

        private String name;
        private String value;
        private String path;
        private boolean push;
        private boolean applied;
        private int networkType;
        private State[] state;
        private long delay;
        private int policyToDetete;
        private boolean before;

        public Property(String name, String path, String value) {
            this.name = name;
            this.value = value;
            this.path = path;
        }

        public Property(String name, String path, String value, boolean push) {
            this.name = name;
            this.value = value;
            this.path = path;
            this.push = push;
        }

        public Property(String name, String path, String value, boolean applied, int networkType) {
            this.name = name;
            this.value = value;
            this.path = path;
            this.applied = applied;
            this.networkType = networkType;
        }

        public Property(String name, String path, String value, long delay, int policyToDetete, boolean before, State... states) {
            this.name = name;
            this.value = value;
            this.path = path;
            this.state = states;
            this.delay = delay;
            this.policyToDetete = policyToDetete;
            this.before = before;
        }

        public String getName() {
            return this.name;
        }

        public String getValue() {
            return this.value;
        }

        public String getPath() {
            return this.path;
        }

        public boolean getApplied() {
            return this.applied;
        }

        public int getNetworkType() {
            return this.networkType;
        }

        public long getDelay() {
            return this.delay;
        }

    }
}
