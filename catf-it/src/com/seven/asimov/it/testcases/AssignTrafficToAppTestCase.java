package com.seven.asimov.it.testcases;

import android.content.Context;
import android.content.pm.PackageManager;
import android.test.AssertionFailedError;
import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.MobileNetworkUtil;
import com.seven.asimov.it.utils.OCUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.conn.ConnUtils;
import com.seven.asimov.it.utils.datausage.ApplicationDataDifference;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.crcsTasks.NetlogTask;
import com.seven.asimov.it.utils.logcat.tasks.crcsTasks.ReportPackDumpExecutedAndSentTask;
import com.seven.asimov.it.utils.logcat.tasks.policyTasks.PolicyAddedTask;
import com.seven.asimov.it.utils.logcat.wrappers.PolicyWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.ReportPackDumpExecutedAndSentWrapper;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.sms.SmsUtil;
import com.seven.asimov.it.utils.tcpdump.Direction;
import com.seven.asimov.it.utils.tcpdump.Interface;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AssignTrafficToAppTestCase extends TcpDumpTestCase {

    private static final Logger logger = LoggerFactory.getLogger(AssignTrafficToAppTestCase.class.getSimpleName());

    protected String uri;
    private Throwable throwable;
    protected Context context;
    private ApplicationDataDifference dataDifference_OC;
    private ApplicationDataDifference dataDifference_IT;
    private PolicyWrapper policyLogEntry;
    private NetlogTask netlogTask;
    private PolicyAddedTask policyAddedTask;
    private ReportPackDumpExecutedAndSentTask reportPackDumpExecutedAndSentTask;
    private ReportPackDumpExecutedAndSentWrapper reportPackDumpExecutedAndSentLogEntry;
    protected LogcatUtil logcatUtil;
    private long startTest;
    private long endTest;
    private Map<String, String> properties = new HashMap<String, String>();
    long tcpBytes = 0;
    private MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());

    protected enum Action {
        ADD_POLICY,
        CRCS_REPORT_UPLOAD,
        USE_HTTP,
        USE_HTTPS_WHITELIST,
        USE_HTTPS_BLACKLIST,
        REMOVE_OC,
        CLEAR_OC_DATA,
        CHECK_MISS,
        CHECK_HIT,
        SWITCH_NETWORK;
    }

    /**
     * Returns traffic difference between netlog and tcpdump in percents.
     *
     * @param context
     * @return Traffic difference in percents
     */
    protected void doTrafficTest(Context context, Action action, long differenceLimit) throws Exception {
        throwable = null;
        this.context = context;
        try {

            TestUtil.switchRadioUpStart();
            prepareDataUsage(context, action);
            prepareLogging();
            logger.info("startLogging");
            logcatUtil.start();

            startDataUsage();
            logger.info("doAction");
            doAction(action, context);
            TestUtil.sleep(10 * 1000); //wait for tcpdump writing to db
            endtDataUsage();

            tcpBytes = getTcpTraffic(action);
            logger.info("doTrafficTest: startTest=" + startTest + " " + new Date(startTest) + " ; endTest=" + endTest + " " + new Date(endTest));
            logger.info("endLogging");

            logcatUtil.stop();
            logTrafficData();

            logger.info(policyAddedTask.toString(false));
            logger.info(netlogTask.toString(false));
            logger.info(reportPackDumpExecutedAndSentTask.toString(false));
            logger.info("checkActionSuccess");

            checkActionSuccess(action);
            logger.info("checkTrafficDiff");
            checkTrafficDiff2(action, differenceLimit);

        } catch (Throwable e) {
            throwable = e;
        } finally {
            logcatUtil.stop();
            deleteAllProperties();
            TestUtil.switchRadioUpStop();
        }
        if (throwable != null) {
            logger.error("Message: " + throwable.getMessage() + ", StackTrace:" + ExceptionUtils.getStackTrace(throwable));
            throw new AssertionFailedError(throwable.getMessage());
        }
    }

    private void prepareDataUsage(Context context, Action action) throws Exception {
        //GET_SHARED_LIBRARY_FILES
        switch (action) {
//            case ADD_POLICY:
//                dataDifference_OC = new ApplicationDataDifference(context, OC_PACKAGE_NAME, PackageManager.GET_META_DATA);
//                dataDifference_IT = new ApplicationDataDifference(context, IT_PACKAGE_NAME, PackageManager.GET_META_DATA);
//                break;
            case REMOVE_OC:
                dataDifference_OC = new ApplicationDataDifference(context, TFConstantsIF.OC_PACKAGE_NAME, PackageManager.GET_UNINSTALLED_PACKAGES);
                dataDifference_IT = new ApplicationDataDifference(context, TFConstantsIF.IT_PACKAGE_NAME, PackageManager.GET_UNINSTALLED_PACKAGES);
                break;
            default:
                dataDifference_OC = new ApplicationDataDifference(context, TFConstantsIF.OC_PACKAGE_NAME, PackageManager.GET_META_DATA);
                dataDifference_IT = new ApplicationDataDifference(context, TFConstantsIF.IT_PACKAGE_NAME, PackageManager.GET_META_DATA);
        }
    }

    private void prepareLogging() {
        policyAddedTask = new PolicyAddedTask();
        netlogTask = new NetlogTask();
        reportPackDumpExecutedAndSentTask = new ReportPackDumpExecutedAndSentTask();
        policyAddedTask.setChangeTimestampToGMT(false);
        netlogTask.setChangeTimestampToGMT(false);
        reportPackDumpExecutedAndSentTask.setChangeTimestampToGMT(false);
        logcatUtil = new LogcatUtil(getContext(), policyAddedTask, netlogTask, reportPackDumpExecutedAndSentTask);
    }

    private void startDataUsage() {
        startTest = System.currentTimeMillis();
        dataDifference_OC.startDataCollecting();
        dataDifference_IT.startDataCollecting();
    }

    private void endtDataUsage() {
        endTest = System.currentTimeMillis();
        dataDifference_OC.endDataCollecting();
        dataDifference_IT.endDataCollecting();
    }

    protected void doAction(Action action, Context context) throws Exception {
        switch (action) {
            case ADD_POLICY:
                properties.put(TFConstantsIF.MIN_ENTRIES_PROPERTY_NAME, addProperty(TFConstantsIF.MIN_ENTRIES_PROPERTY_NAME,
                        TFConstantsIF.MIN_ENTRIES_PROPERTY_VALUE_40, TFConstantsIF.MIN_ENTRIES_PROPERTY_PATH));
                break;
            case CRCS_REPORT_UPLOAD:
                properties.put(TFConstantsIF.MIN_ENTRIES_PROPERTY_NAME, addProperty(TFConstantsIF.MIN_ENTRIES_PROPERTY_NAME,
                        TFConstantsIF.MIN_ENTRIES_PROPERTY_VALUE_5, TFConstantsIF.MIN_ENTRIES_PROPERTY_PATH));
                startDataUsage();
                doHttpActivity(3, action);
                break;
            case USE_HTTPS_WHITELIST:
                PMSUtil.createNameSpace(TFConstantsIF.HTTPS_BLACKLIST_PATH, TFConstantsIF.IT_PACKAGE_NAME);
                TestUtil.sleep(60 * 1000);
                doHttpsActivity();
                break;
            case USE_HTTPS_BLACKLIST:
                PMSUtil.deleteNamespace(TFConstantsIF.HTTPS_BLACKLIST_PATH + "@" + TFConstantsIF.IT_PACKAGE_NAME);
                TestUtil.sleep(60 * 1000);
                doHttpsActivity();
                break;
            case USE_HTTP:
                doHttpActivity(1, action);
                //doHttpActivity(1);
                break;
            case REMOVE_OC:
                doHttpActivity(1, action);
                OCUtil.removeOCClient();
                break;
            case CLEAR_OC_DATA:
                OCUtil.clearOCData();
                TestUtil.sleep(2 * 60 * 1000);
                break;
            case CHECK_MISS:
                doHttpActivity(1, action);
                break;
            case CHECK_HIT:
                doHit();
                break;
            case SWITCH_NETWORK:
                doHttpActivity(1, action);
                mobileNetworkUtil.on3gOnly();
                //onWifiOnly(getContext());
                break;
        }
        TestUtil.sleep(30 * 1000);
    }

    protected void doHttpActivity(int quantityOfRequests, Action action) throws Exception {
        int timeout = 60 * 1000;
        int retryCount = 0;
        for (int i = 0; i < quantityOfRequests; i++) {
            int index = 0;
            uri = createTestResourceUri(uri);
            HttpRequest.Builder rBuilder = createRequest().setUri(uri).setMethod("GET");
            if (action != Action.CHECK_HIT)
                rBuilder = rBuilder.addHeaderField("X-OC-ContentEncoding", "identity")
                        .addHeaderField("X-OC-ResponseContentSize", "10000," + "a");
            final HttpRequest request = rBuilder.getRequest();
            logger.info("Sending request to " + uri);
            if (action == Action.CHECK_HIT) {
                checkMiss(request, ++index, HttpStatus.SC_OK, null, false, timeout);
                checkMiss(request, ++index, HttpStatus.SC_OK, null, false, timeout);
                checkMiss(request, ++index, HttpStatus.SC_OK, null, false, timeout);
                checkHit(request, ++index, HttpStatus.SC_OK, null, false, timeout);
            } else {
                checkMiss(request, ++index, HttpStatus.SC_OK, null, false, timeout);
            }
            logger.info("doHttpActivity: uri: " + uri);
        }
    }

    private void doHttpsActivity() throws Exception {
        int interval = 60 * 1000;
        //uri = "https://upload.wikimedia.org/wikipedia/commons/e/e6/Europavilla.jpg";
        uri = "https://commons.wikimedia.org/wiki/Main_Page";
        HttpRequest request = AsimovTestCase.createRequest().setUri(uri).setMethod("GET").getRequest();
        HttpResponse response = sendRequest(request, false, interval);
        //uri = "upload.wikimedia.org";
        uri = "commons.wikimedia.org";
        logger.info("doHttpsActivity: uri: " + uri);
    }

    private void doHit() throws Exception {
        logger.info("doHit: uri: " + uri);
        HttpRequest request = AsimovTestCase.createRequest().setUri("http://" + uri).setMethod("GET").getRequest();
        int timeout = 30 * 1000;
        HttpResponse response = sendRequest(request, false, timeout);
        //checkHit(request, 555);
    }

    private void logTrafficData() {
        logger.info("tcpBytes=" + tcpBytes);
        logger.info("dataDifference_OC: " + dataDifference_OC);
        logger.info("dataDifference_IT: " + dataDifference_IT);
    }

    private long getTcpTraffic(Action action) throws Exception {
        switch (action) {
            case ADD_POLICY:
            case CLEAR_OC_DATA:
                return getTcpDumpTraffic_Policy();
            case CRCS_REPORT_UPLOAD:
                return getTcpDumpTraffic_Report();
            case SWITCH_NETWORK:
            case USE_HTTP:
                return getTcpDumpTraffic_Http();
            case USE_HTTPS_WHITELIST:
            case USE_HTTPS_BLACKLIST:
                return getTcpDumpTraffic_Https();
            case CHECK_MISS:
                return getTcpDumpTraffic_Miss_Hit();
            case CHECK_HIT:
                return getTcpDumpTraffic_Miss_Hit();
            default:
                return 0;
        }
    }

    private long getTcpDumpTraffic_Policy() {
        return tcpDump.getTcpTraffic(startTest, endTest, Interface.NETWORK, Direction.TO_US, TFConstantsIF.EXTERNAL_IP, null, null, null);
    }

    private long getTcpDumpTraffic_Report() {
        return tcpDump.getTcpTraffic(startTest, endTest, Interface.NETWORK, Direction.FROM_US, null, TFConstantsIF.EXTERNAL_IP, null, TFConstantsIF.Z7TP_RELAY_PORT);
    }

    private long getTcpDumpTraffic_Http() throws UnknownHostException {
        return tcpDump.getTcpTraffic(startTest, endTest, Interface.NETWORK, Direction.TO_US, null, null, null, null);
    }

    private long getTcpDumpTraffic_Https() throws UnknownHostException {
        return tcpDump.getTcpTraffic(startTest, endTest, Interface.NETWORK, Direction.TO_US, null, null, 443, null);
    }

    private long getTcpDumpTraffic_Miss_Hit() throws UnknownHostException {
        return tcpDump.getTcpTraffic(startTest, endTest, Interface.NETWORK, Direction.BOTH, ConnUtils.getHostAddress(uri), null, null, null);
    }

    protected void checkReportPackSent() throws Exception {
        reportPackDumpExecutedAndSentLogEntry = reportPackDumpExecutedAndSentTask.getEntryAfter(policyLogEntry.getEntryNumber());
        assertNotNull("Crcs report was not sent!", reportPackDumpExecutedAndSentLogEntry);
    }

    protected void checkActionSuccess(Action action) throws Exception {
        switch (action) {
            case ADD_POLICY:
                checkPolicyUpdated(TFConstantsIF.MIN_ENTRIES_PROPERTY_NAME, TFConstantsIF.MIN_ENTRIES_PROPERTY_VALUE_40);
                break;
            case CRCS_REPORT_UPLOAD:
                checkPolicyUpdated(TFConstantsIF.MIN_ENTRIES_PROPERTY_NAME, TFConstantsIF.MIN_ENTRIES_PROPERTY_VALUE_5);
                checkReportPackSent();
                assertTrue("TCP traffic not found!", tcpBytes != 0);
                break;
            default:
                assertTrue("TCP traffic not found!", tcpBytes != 0);
        }
    }

    protected void checkTrafficDiff2(Action action, long differenceLimit) {
        long differenceInPercentIT = 0;
        long differenceInPercentOC = 0;
        switch (action) {
            case ADD_POLICY:
                differenceInPercentOC = getDifferenceInPercent(tcpBytes, dataDifference_OC.getInDataDifference());
                assertTrue("In data usage for IT " + dataDifference_IT.getInDataDifference() + " bigger than report traffic " + tcpBytes + "!", dataDifference_IT.getInDataDifference() < tcpBytes);
                assertTrue("Data usage difference for OC " + differenceInPercentOC + "% bigger than " + differenceLimit + "% !", differenceInPercentOC < differenceLimit);
                break;
            case CRCS_REPORT_UPLOAD:
                differenceInPercentOC = getDifferenceInPercent(tcpBytes, dataDifference_OC.getClearOutDataDifference());
                assertTrue("Data usage difference for OC " + differenceInPercentOC + "% bigger than " + differenceLimit + "% !", differenceInPercentOC < differenceLimit);
                break;
            case USE_HTTPS_WHITELIST:
            case USE_HTTPS_BLACKLIST:
                differenceInPercentOC = getDifferenceInPercent(dataDifference_OC.getInDataDifference(), dataDifference_OC.getOutDataDifference());
                differenceInPercentIT = getDifferenceInPercent(tcpBytes, dataDifference_IT.getInDataDifference());
                assertTrue("Data usage difference for OC " + differenceInPercentOC + "% bigger than " + differenceLimit + "% !", differenceInPercentOC < differenceLimit);
                assertTrue("Data usage difference for IT " + differenceInPercentIT + "% bigger than " + differenceLimit + "% !", differenceInPercentIT < differenceLimit);
                break;
            case SWITCH_NETWORK:
            case USE_HTTP:
                differenceInPercentIT = getDifferenceInPercent(tcpBytes, dataDifference_IT.getInDataDifference());
                differenceInPercentOC = getDifferenceInPercent(dataDifference_OC.getInDataDifference(), dataDifference_OC.getOutDataDifference());
                assertTrue("Data usage difference for OC " + differenceInPercentOC + "% bigger than " + differenceLimit + "% !", differenceInPercentOC < differenceLimit);
                assertTrue("Data usage difference for IT " + differenceInPercentIT + "% bigger than " + differenceLimit + "% !", differenceInPercentIT < differenceLimit);
                break;
            case CLEAR_OC_DATA:
                assertTrue("Data usage for IT must be zero bytes!", dataDifference_IT.getInPlusOutDataDifference() == 0);
                assertTrue("Data usage for OC bigger than " + differenceLimit + " bytes!", dataDifference_OC.getInDataDifference() < differenceLimit);
                break;
            case CHECK_MISS:
                differenceInPercentIT = getDifferenceInPercent(tcpBytes, dataDifference_IT.getInPlusOutDataDifference());
                assertTrue("Data usage difference for IT " + differenceInPercentIT + "% bigger than " + differenceLimit + "% !", differenceInPercentIT < differenceLimit);
                break;
            case CHECK_HIT:
                differenceInPercentIT = getDifferenceInPercent(tcpBytes, dataDifference_IT.getInPlusOutDataDifference());
                assertTrue("Data usage difference for IT " + differenceInPercentIT + "% bigger than " + differenceLimit + "% !", differenceInPercentIT < differenceLimit);
                break;
        }
        logger.info("Data usage difference IT = " + differenceInPercentIT);
        logger.info("Data usage difference OC = " + differenceInPercentOC);
    }

    private long getDifferenceInPercent(long arg1, long arg2) {
        return Math.abs((arg1 - arg2) * 100 / (arg1 < arg2 ? arg1 : arg2));
    }

    protected void checkPolicyUpdated(String name, String value) throws Exception {
        int entryNumber = 0;
        do {
            policyLogEntry = policyAddedTask.getEntryAfter(entryNumber);
            assertNotNull("Policy update is not found!", policyLogEntry);
            entryNumber = policyLogEntry.getEntryNumber();
        } while (!(name.equals(policyLogEntry.getName()) && value.equals(policyLogEntry.getValue())));
    }

    protected void deleteAllProperties() {
        for (Map.Entry<String, String> en : properties.entrySet()) {
            if (en.getValue() != null) {
                logger.info("Before deleting property name:" + en.getKey() + " value:" + en.getValue());
                deleteProperty(en.getValue());
            }
        }
        properties = new HashMap<String, String>();
    }

    protected String addProperty(String name, String value, String path) throws Exception {
        String propId = null;
        logger.info("addProperty start");
        propId = PMSUtil.createPersonalScopeProperty(name, path, value, true);
        assertTrue("Property name:" + name + " hasn't been added!", (propId != null && !propId.equals("")));
        logger.info("Property name:" + name + " id:" + propId + " has been added.");
        startDataUsage();
        //wait for policy to be applied
        TestUtil.sleep(60 * 1000);
        logger.info("addProperty end");
        SmsUtil smsUtil = new SmsUtil(getContext());
        smsUtil.sendPolicyUpdate((byte) 0);
        TestUtil.sleep(60 * 1000);
        return propId;
    }

    protected void deleteProperty(String id) {
        PMSUtil.deleteProperty(id);
        logger.info("Property " + id + " has been deleted.");
        TestUtil.sleep(60 * 1000);
        logger.info("deleteProperty end");
    }
}
