package com.seven.asimov.it.tests.failover.disabled;

import com.seven.asimov.it.testcases.FailoverTestCase;
import com.seven.asimov.it.utils.MobileNetworkUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;

public class DisabledFailoverTests extends FailoverTestCase {
    private MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());

    /**
     * 1. Install HelloSocket.apk
     * 2. Open HelloSocket and turn OC OFF
     * <p/>
     * 1. Install OC
     * 2. Download the file sys.log form device
     * 3. Analyze this file
     * 4. Observe logcat
     * <p/>
     * Verify in logcat that OC enters failovers state: all traffic bypass OC. OC doesnt record entries in CRCS for tcp and udp traffic Verify on sys.log that dispatchers are stopped.
     */

    public void test_002_Enabled() throws Throwable {
        mobileNetworkUtil.onWifiOnly();
        try {
            startTestForDisabledFailover("enabled", "@asimov", "0", false);
        } finally {
            PMSUtil.addPolicies(new Policy[]{new Policy("enabled", "1", "@asimov", true)});
            TestUtil.sleep(2 * 60 * 1000);
        }

    }

    /**
     * 1. Install HelloSocket.apk
     * 2. Open HelloSocket and turn OC ON
     * Expected mode:  OC should work in optimization mode.
     * <p/>
     * 1. Install OC
     * 2. Observe logcat
     */

    public void test_003_Enabled() throws Throwable {
        mobileNetworkUtil.onWifiOnly();
        startTestForDisabledFailover("enabled", "@asimov", "1", true);

    }

    /**
     * 1. Install OC
     * 2. Create property "enabled" and set its value to 0 in asimov branch of PMS
     * <p/>
     * 1. Be sure that policies arrive and they are applied in logcat
     * 2. Change value of "enabled" to 1
     * 3. Be sure that policies arrive and they are applied in logcat
     * 4. Observe logcat
     * <p/>
     * OC should work in optimization mode.
     */

    public void test_008_Enabled() throws Throwable {
        mobileNetworkUtil.onWifiOnly();
        try {
            startTestForDisabledFailover("enabled", "@asimov", "0", false);
        } finally {
            PMSUtil.addPolicies(new Policy[]{new Policy("enabled", "1", "@asimov", true)});
            TestUtil.sleep(2 * 60 * 1000);
        }
        startTestForDisabledFailover("enabled", "@asimov", "1", true);

    }

    /**
     * 1. Make a single request to any host.
     * 2. Send enabled=0 policy, and wait for android cache expiration ( about 10 minutes).
     * 3. Make second request to same resource.
     * 4. Check logcat.
     * <p/>
     * <p/>
     * 1. DNS entry is stored into OC cache.
     * 2. Enabled=0 policy is received.
     * 3. DNS request is sent to network.
     */

    public void test_018_Enabled() throws Throwable {
        mobileNetworkUtil.onWifiOnly();
        String path = "disabledFailover";

        long TIMEOUT = 10 * 60 * 1000;
        try {
            startTestForDisabledFailover("enabled", "@asimov", "0", false, TIMEOUT, path);
        } finally {
            PMSUtil.addPolicies(new Policy[]{new Policy("enabled", "1", "@asimov", true)});
            TestUtil.sleep(2 * 60 * 1000);
        }
    }
}
