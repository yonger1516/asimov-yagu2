package com.seven.asimov.it.tests.communication.apptraffic;

import android.test.suitebuilder.annotation.LargeTest;
import com.seven.asimov.it.annotation.DeviceOnly;
import com.seven.asimov.it.annotation.Execute;
import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.testcases.AssignTrafficToAppTestCase;
import com.seven.asimov.it.utils.IpTablesUtil;
import com.seven.asimov.it.utils.MobileNetworkUtil;
import com.seven.asimov.it.utils.TestUtil;

/**
 * This class tests correctness of traffic assigning between different apps
 */
public class AssignTrafficToAppTests extends AssignTrafficToAppTestCase {

    private MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());


//    @Override
//    protected void setUp() {
//        Log.v(TAG, "setUp()");
//        banAllAppsButOC(true);
//        on3gOnly(getContext());
//        onWifiOnly(getContext());
//    }
//
//    @Override
//    protected void tearDown() {
//        Log.v(TAG, "tearDown()");
//        banAllAppsButOC(false);
//        TestUtils.sleep(10 * 1000);
//    }

//    @Override
//    protected void runTest() {
//        super.runTest();
//    }

    /**
     * Policy, which was updated on server, should be included in OCs traffic during 3G session
     */
    @Execute
    @DeviceOnly
    @LargeTest
    public void test_001_DATATRAFFSPLIT_02() throws Exception {
        uri = "traffic_to_app_test_resource_1";
        IpTablesUtil.banAllAppsButOC(true, getContext());
//        banAllAppsButOC(true);
        mobileNetworkUtil.onWifiOnly();
        TestUtil.sleep(60 * 1000);
        mobileNetworkUtil.on3gOnly();
        TestUtil.sleep(60 * 1000);
        doTrafficTest(getContext(), Action.ADD_POLICY, 100);
    }

    /**
     * Reports, which were sent to server, should be included in OCs traffic during 3G session
     */
    @Execute
    @DeviceOnly
    @LargeTest
    public void test_002_DATATRAFFSPLIT_04() throws Exception {
        uri = "traffic_to_app_test_resource_2";
        doTrafficTest(getContext(), Action.CRCS_REPORT_UPLOAD, 100);
    }

    /**
     * Whitelisted HTTPS traffic should be calculated separately from OCs traffic during 3g session
     * Ignore reason: ASMV-22657
     */
    @Ignore
    @DeviceOnly
    @LargeTest
    public void test_003_DATATRAFFSPLIT_06() throws Exception {
        uri = "traffic_to_app_test_resource_3";
        mobileNetworkUtil.on3gOnly();
        TestUtil.sleep(60 * 1000);
        doTrafficTest(getContext(), Action.USE_HTTPS_WHITELIST, 100);
    }

    /**
     * Blacklisted HTTPS traffic should be calculated separately from OCs traffic during 3G session
     * Ignore reason: ASMV-22657
     */
    @Ignore
    @DeviceOnly
    @LargeTest
    public void test_004_DATATRAFFSPLIT_08() throws Exception {
        uri = "traffic_to_app_test_resource_4";
        mobileNetworkUtil.on3gOnly();
        TestUtil.sleep(60 * 1000);
        doTrafficTest(getContext(), Action.USE_HTTPS_BLACKLIST, 100);
    }

    /**
     * MISS should be included in apps traffic, but HIT shouldn't be included in OCs and apps during polling
     */
    @Execute
    @DeviceOnly
    @LargeTest
    public void test_005_DATATRAFFSPLIT_16() throws Exception {
        uri = "traffic_to_app_test_resource_12_1";
        doTrafficTest(getContext(), Action.CHECK_MISS, 100);
        uri = "traffic_to_app_test_resource_12_2";
        doTrafficTest(getContext(), Action.CHECK_HIT, 100);
    }

    /**
     * HTTP traffic should be calculated separately from OCs traffic during 3G session
     */
    @Execute
    @DeviceOnly
    @LargeTest
    public void test_006_DATATRAFFSPLIT_10() throws Exception {
        uri = "traffic_to_app_test_resource_5";
        doTrafficTest(getContext(), Action.USE_HTTP, 100);
    }

    /**
     * Policy, which was updated on server, should be included in OCs traffic during WiFi session
     */
    @Execute
    @DeviceOnly
    @LargeTest
    public void test_007_DATATRAFFSPLIT_01() throws Exception {
        uri = "traffic_to_app_test_resource_6";
        mobileNetworkUtil.onWifiOnly();
        TestUtil.sleep(60 * 1000);
        doTrafficTest(getContext(), Action.ADD_POLICY, 100);
    }

    /**
     * Reports, which were sent to server, should be included in OCs traffic during WiFi session
     */
    @Execute
    @DeviceOnly
    @LargeTest
    public void test_008_DATATRAFFSPLIT_03() throws Exception {
        uri = "traffic_to_app_test_resource_7";
        doTrafficTest(getContext(), Action.CRCS_REPORT_UPLOAD, 100);
    }

    /**
     * Whitelisted HTTPS traffic should be calculated separately from OCs traffic during Wi-Fi session
     * Ignore reason: ASMV-22657
     */
    @Ignore
    @DeviceOnly
    @LargeTest
    public void test_009_DATATRAFFSPLIT_05() throws Exception {
        uri = "traffic_to_app_test_resource_8";
        mobileNetworkUtil.onWifiOnly();
        TestUtil.sleep(60 * 1000);
        doTrafficTest(getContext(), Action.USE_HTTPS_WHITELIST, 100);
    }

    /**
     * Blacklisted HTTPS traffic should be calculated separately from OCs traffic during Wi-Fi session
     * Ignore reason: ASMV-22657
     */
    @Ignore
    @DeviceOnly
    @LargeTest
    public void test_010_DATATRAFFSPLIT_07() throws Exception {
        uri = "traffic_to_app_test_resource_9";
        mobileNetworkUtil.onWifiOnly();
        TestUtil.sleep(60 * 1000);
        doTrafficTest(getContext(), Action.USE_HTTPS_BLACKLIST, 100);
    }

    /**
     * HTTP traffic should be calculated separately from OCs traffic during Wi-Fi session
     */
    @Execute
    @DeviceOnly
    @LargeTest
    public void test_011_DATATRAFFSPLIT_09() throws Exception {
        uri = "traffic_to_app_test_resource_10";
        doTrafficTest(getContext(), Action.USE_HTTP, 100);
    }


    /**
     * OCs traffic shouldnt include CE and policies reloaded from db after OCs "Clear data"
     * the test is not actual
     * in that case OC reloads policies from db
     */
    //@Execute
    @Ignore
    @DeviceOnly
    @LargeTest
    public void test_012_DATATRAFFSPLIT_15() throws Exception {
        uri = "traffic_to_app_test_resource_11";
        doTrafficTest(getContext(), Action.CLEAR_OC_DATA, 100);
    }

    /**
     * Apps traffic should be calculated separately from OCs traffic after switching from one type of network to another
     */
    @Execute
    @DeviceOnly
    @LargeTest
    public void test_013_DATATRAFFSPLIT_18() throws Exception {
        uri = "traffic_to_app_test_resource_14";
        doTrafficTest(getContext(), Action.SWITCH_NETWORK, 100);
        IpTablesUtil.banAllAppsButOC(false, getContext());
    }
}



