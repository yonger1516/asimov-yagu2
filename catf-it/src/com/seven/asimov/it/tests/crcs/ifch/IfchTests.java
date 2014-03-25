package com.seven.asimov.it.tests.crcs.ifch;

import com.seven.asimov.it.annotation.DeviceOnly;
import com.seven.asimov.it.testcases.IfchTestCase;
import com.seven.asimov.it.utils.MobileNetworkUtil;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.TestUtil;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h2>This class tests OCC's IFCH functionality.</h2>
 */
public class IfchTests extends IfchTestCase {

    private static final Logger logger = LoggerFactory.getLogger(IfchTests.class.getSimpleName());

    /**
     * <h3>The test generates IFCH on CSN and CSP messages in case wifi interface is connected.</h3>
     * actions:
     * <ol>
     * <li>switch wifi connection ON</li>
     * <li>test resource that returns the same responses for first 3 requests, and another one for next 1 request is sent. Pattern [0,65,65]</li>
     * </ol>
     * checks:
     * <ol>
     * <li>for the first transaction verdict CSD should be received</li>
     * <li>CSN task should be executed</li>
     * <li>IFCH: Resolved interface to wifi with time stamp should be appeared in client log</li>
     * <li>for the 3rd transaction verdict CSA should be received for this transaction</li>
     * <li>CSP task should be executed</li>
     * <li>IFCH: Resolved interface to wifi with time stamp should be appear in client log</li>
     * </ol>
     *
     * @throws Throwable
     */
    @DeviceOnly
    public void test_001_IFCH() throws Throwable {
        //ClientUtil.reinstallOCC("/sdcard/apks/asimov-signed-r489141-eng004_nozip_ga_test_it_rooted_wo_msisdn.apk");
        executeIFCHTest_CSN_CSP("asimov_ifch_01", false);
    }

    /**
     * <h3>The test generates IFCH on CSN and CSP messages in case mobile interface is connected.</h3>
     * actions:
     * <ol>
     * <li>switch 3g connection ON</li>
     * <li>test resource that returns the same responses for first 3 requests, and another one for next 1 request is sent. Pattern [0,65,65]</li>
     * </ol>
     * checks:
     * <ol>
     * <li>for the first transaction verdict CSD should be received</li>
     * <li>CSN task should be executed</li>
     * <li>IFCH: Resolved interface to wifi with time stamp should be appeared in client log</li>
     * <li>for the 3rd transaction verdict CSA should be received for this transaction</li>
     * <li>CSP task should be executed</li>
     * <li>IFCH: Resolved interface to wifi with time stamp should be appear in client log</li>
     * </ol>
     *
     * @throws Throwable
     */
    @DeviceOnly
    public void test_002_IFCH() throws Throwable {
        executeIFCHTest_CSN_CSP("asimov_ifch_02", true);
    }

    /**
     * <h3>The test generates IFCH on ERR messages in case mobile interface is connected.</h3>
     * actions:
     * <ol>
     * <li>switch wifi connection ON</li>
     * <li>test resource that contains 1 request and 1 response with Response sleep time (sec) set 150 is sent</li>
     * </ol>
     * checks:
     * <ol>
     * <li>this transaction should be with failed</li>
     * <li>ERR from HTTP Dispatcher should be generated</li>
     * </ol>
     *
     * @throws Throwable
     */
    @DeviceOnly
    public void test_003_IFCHMobileHttpErrorTransaction() throws Throwable {
        executeIFCHTest_ERR("asimov_ifch_03", true);
    }

    /**
     * <h3>The test generates IFCH on ERR messages in case wifi interface is connected.</h3>
     * actions:
     * <ol>
     * <li>switch wifi connection ON</li>
     * <li>test resource that contains 1 request and 1 response with Response sleep time (sec) set 150 is sent</li>
     * </ol>
     * checks:
     * <ol>
     * <li>this transaction should be with failed</li>
     * <li>ERR from HTTP Dispatcher should be generated</li>
     * </ol>
     *
     * @throws Throwable
     */
    @DeviceOnly
    public void test_004_IFCHWifiHttpErrorTransaction() throws Throwable {
        executeIFCHTest_ERR("asimov_ifch_04", false);
    }

    /**
     * <h3>The test switches from wifi to mobile interface and back.</h3>
     * actions:
     * <ol>
     * <li>switch wifi connection ON</li>
     * <li>switch wifi connection OFF</li>
     * <li>switch 3g connection ON</li>
     * </ol>
     * checks:
     * <ol>
     * <li>table for connection state should be logged after network type wifi is detected</li>
     * <li>after changing interface, table for connection state should be logged; the record contains current (mobile) state and previous one (wifi)</li>
     * <li>after changing interface back to mobile, table for connection state should be logged; the record contains current (mobile) state and all previous</li>
     * </ol>
     *
     * @throws Throwable
     */
    @DeviceOnly
    public void test_005_IFCHSwitchingWifiMobile() throws Throwable {
        MobileNetworkUtil mnh = MobileNetworkUtil.init(getContext());
        mnh.switchWifiOnOff(false);
        TestUtil.sleep(WAITING_SWITCHING_TIME);

        resource = "asimov_ifch_05_wifi";
        uri = createTestResourceUri(resource);
        request = createRequest().setMethod(HttpGet.METHOD_NAME).setUri(uri).getRequest();

        prepareTasks();
        prepareLogcatUtil(getContext(), ifchTableTask);
        logcatUtil.start();
        try {
            sendRequest2(request);

            mnh.switchWifiOnOff(true);
            TestUtil.sleep(WAITING_SWITCHING_TIME);
            mnh.switchWifiOnOff(false);
            TestUtil.sleep(WAITING_SWITCHING_TIME);
            logger.trace("Mobile interface must be enabled.");

            sendRequest2(request);
            TestUtil.sleep(WAITING_SWITCHING_TIME);
        } finally {
            logcatUtil.stop();
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }

        checkWifiInTableTask();
    }

    /**
     * <h3>The test switches from wifi to airplane mode and back.</h3>
     * actions:
     * <ol>
     * <li>switch wifi connection ON</li>
     * <li>switch airplane mode ON</li>
     * <li>Wait for 30 sec</li>
     * <li>switch airplane mode OFF</li>
     * </ol>
     * checks:
     * <ol>
     * <li>table for connection state should be logged after network type wifi is detected</li>
     * <li>after changing interface, table for connection state should be logged; the record contains current (none) state and previous one (wifi)</li>
     * </ol>
     *
     * @throws Throwable
     */
    @DeviceOnly
    public void test_006_IFCHSwitchingWifiAirplane() throws Throwable {
        executeIFCHTest_airplane("asimov_ifch_06_wifi_airplane_mode", false);
    }

    /**
     * <h3>The test switches from mobile to airplane mode and back.</h3>
     * actions:
     * <ol>
     * <li>switch mobile connection ON</li>
     * <li>switch airplane mode ON</li>
     * <li>Wait for 30 sec</li>
     * <li>switch airplane mode OFF</li>
     * </ol>
     * checks:
     * <ol>
     * <li>table for connection state should be logged after network type mobile is detected</li>
     * <li>after changing interface, table for connection state should be logged; the record contains current (none) state and previous one (mobile)</li>
     * </ol>
     *
     * @throws Throwable
     */
    @DeviceOnly
    public void test_007_IFCHSwitchingMobileAirplane() throws Throwable {
        executeIFCHTest_airplane("asimov_ifch_07_wifi_airplane_mode", true);
    }

    /**
     * <h3>The test deletes extra-records in case wifi ON.</h3>
     * actions:
     * <ol>
     * <li>switch connection from wifi to mobile and back several times</li>
     * <li>start logcat</li>
     * <li>switch connection from wifi to mobile and back</li>
     * </ol>
     * checks:
     * <ol>
     * <li>check if 20 IFCH records are persent in log; time diff from previous to next record should be less than 10 msec</li>
     * </ol>
     *
     * @throws Throwable
     */
    @DeviceOnly
    public void test_008_IFCHDeletingExtraRecordsWifi() throws Throwable {

        final int count = 11;
        MobileNetworkUtil mnh = MobileNetworkUtil.init(getContext());

        for (int i = 0; i < count - 1; i++) {
            mnh.switchWifiOnOff(false);
            mnh.switchWifiOnOff(true);
            TestUtil.sleep(WAITING_SWITCHING_TIME);
        }

        prepareTasks();
        prepareLogcatUtil(getContext(), ifchTableTask);
        logcatUtil.start();

        try {
            mnh.switchWifiOnOff(false);
            mnh.switchWifiOnOff(true);
            TestUtil.sleep(WAITING_SWITCHING_TIME);
        } finally {
            logcatUtil.stop();
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
        sortAndCheckIfchTableTask();
    }

    /**
     * <h3>The test deletes extra-records in case wifi ON.</h3>
     * actions:
     * <ol>
     * <li>switch wifi connection ON</li>
     * <li>start logcat</li>
     * <li>http request is sent</li>
     * <li>kill ochttpd process</li>
     * <li>one more http request is sent</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>IFCH table should not appear</li>
     * </ol>
     *
     * @throws Throwable
     */
    @DeviceOnly
    public void test_009_IFCHKillingHttpDispatcher() throws Throwable {
        MobileNetworkUtil mnh = MobileNetworkUtil.init(getContext());
        mnh.switchWifiOnOff(true);
        TestUtil.sleep(WAITING_SWITCHING_TIME);

        resource = "asimov_ifch_09_kill_http_dispatcher";
        uri = createTestResourceUri(resource);
        request = createRequest().setMethod(HttpGet.METHOD_NAME).setUri(uri).getRequest();

        prepareTasks();
        prepareLogcatUtil(getContext(), ifchTableTask);
        logcatUtil.start();

        try {
            sendRequest2(request);
            TestUtil.sleep(WAITING_SWITCHING_TIME);

            killHttpDispatcher();
            TestUtil.sleep(WAITING_SWITCHING_TIME);

            sendRequest2(request);
            TestUtil.sleep(WAITING_SWITCHING_TIME);
        } finally {
            logcatUtil.stop();
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
        checkIfchTableTaskIsEmpty();
    }

}