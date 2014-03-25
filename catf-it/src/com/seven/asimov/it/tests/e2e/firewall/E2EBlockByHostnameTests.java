package com.seven.asimov.it.tests.e2e.firewall;


import android.accounts.NetworkErrorException;
import android.util.Log;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.testcases.E2EFirewallTestCase;
import com.seven.asimov.it.utils.MobileNetworkUtil;
import com.seven.asimov.it.utils.ScreenUtils;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.pcf.PcfHelper;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class E2EBlockByHostnameTests extends E2EFirewallTestCase {
    private static final Logger logger = LoggerFactory.getLogger(E2EBlockByHostnameTests.class.getSimpleName());
    private MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());
    //TODO Notepad++ regex: (verdict HIT|verdict MISS|Test start|Received policy size:|passed:|due to|logcat thread|StartTimestamp|FirewallLog|###DEBUG|firewall policy mgmt)

    public void test_000_BBH() throws Exception {
        cleanPCFUserGroup(pcfGroupName);
        PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(interceptionPolicy, interceptionPorts, interceptionPath, true)});
    }

    /**
     * Create PCF service blocking testrunner
     * Check that testrunner is not accessible for both com.seven.asimov and com.seven.asimov.it
     */

    public void test_001_BBH() {
        final String resource = "test_BBH_001";
        init(resource);
        try {
            Log.v("###DEBUG", "P1");
            JSONObject expectedDelta = BlockByHostnamePreparePCF(new String[]{blockedHost}, false, "", PcfHelper.InterfaceType.ALL, true, false, false);
            checkPCFDeltaAtServer(expectedDelta.toString());
            Log.v("###DEBUG", "P3");
            printIptables();
            checkNetworkResourcesAccessible(resource, false, false, false);
            Log.v("###DEBUG", "P4");
            checkNetworkResourcesAccessible(resource, false, true, false);
            Log.v("###DEBUG", "P5");
        } finally {
            clean(resource);
            printIptables();
            cleanPCFUserGroup(pcfGroupName);
        }
    }

    /**
     * Create PCF application blocking testrunner for com.seven.asimov
     * Check that testrunner is not accessible for com.seven.asimov
     * Check that testrunner is accessible for com.seven.asimov.it
     */

    public void test_002_BBH() {
        final String resource = "test_BBH_002";
        init(resource);
        try {
            Log.v("###DEBUG", "P1");
            JSONObject expectedDelta = BlockByHostnamePreparePCF(new String[]{blockedHost}, false, "", PcfHelper.InterfaceType.ALL, false, false, false);
            checkPCFDeltaAtServer(expectedDelta.toString());
            Log.v("###DEBUG", "P2");
            printIptables();
            checkNetworkResourcesAccessible(resource, false, false, false);
            Log.v("###DEBUG", "P3");
            checkNetworkResourcesAccessible(resource, false, true, true);
            Log.v("###DEBUG", "P4");
        } finally {
            printIptables();
            cleanPCFUserGroup(pcfGroupName);
        }
    }

    /**
     * Create PCF service blocking testrunner
     * Check that testrunner is accessible for com.seven.asimov and com.seven.asimov.it
     * Turn screen off
     * Check that testrunner is not accessible for com.seven.asimov and com.seven.asimov.it
     */

    public void test_003_BBH() {
        final String resource = "test_BBH_003";
        init(resource);
        try {
            Log.v("###DEBUG", "P1");
            JSONObject expectedDelta = BlockByHostnamePreparePCF(new String[]{blockedHost}, false, "", PcfHelper.InterfaceType.ALL, true, false, true);
            checkPCFDeltaAtServer(expectedDelta.toString());
            Log.v("###DEBUG", "P2");
            printIptables();
            checkNetworkResourcesAccessible(resource, false, false, true);
            Log.v("###DEBUG", "P3");
            checkNetworkResourcesAccessible(resource, false, true, true);
            Log.v("###DEBUG", "P4");
            ScreenUtils.screenOff();
            Log.v("###DEBUG", "P5");
            TestUtil.sleep(LITTLE_DELAY);
            printIptables();
            Log.v("###DEBUG", "P6");
            checkNetworkResourcesAccessible(resource, false, false, false);
            Log.v("###DEBUG", "P7");
            checkNetworkResourcesAccessible(resource, false, true, false);
            Log.v("###DEBUG", "P8");

        } finally {
            clean(resource);
            printIptables();
            ScreenUtils.screenOn();
            TestUtil.sleep(LITTLE_DELAY);
            cleanPCFUserGroup(pcfGroupName);
        }
    }

    /**
     * Create PCF service blocking testrunner for com.seven.asimov
     * Check that testrunner is accessible for com.seven.asimov and com.seven.asimov.it
     * Turn screen off
     * Check that testrunner is not accessible for com.seven.asimov
     * Check that testrunner is accessible for com.seven.asimov.it
     */

    public void test_004_BBH() {
        final String resource = "test_BBH_004";
        init(resource);
        try {
            Log.v("###DEBUG", "P1");
            JSONObject expectedDelta = BlockByHostnamePreparePCF(new String[]{blockedHost}, false, "", PcfHelper.InterfaceType.ALL, false, false, true);
            checkPCFDeltaAtServer(expectedDelta.toString());
            Log.v("###DEBUG", "P2");
            printIptables();
            checkNetworkResourcesAccessible(resource, false, false, true);
            Log.v("###DEBUG", "P3");
            checkNetworkResourcesAccessible(resource, false, true, true);
            Log.v("###DEBUG", "P4");
            ScreenUtils.screenOff();
            Log.v("###DEBUG", "P5");
            TestUtil.sleep(LITTLE_DELAY);
            printIptables();
            Log.v("###DEBUG", "P6");
            checkNetworkResourcesAccessible(resource, false, false, false);
            Log.v("###DEBUG", "P7");
            checkNetworkResourcesAccessible(resource, false, true, true);
            Log.v("###DEBUG", "P8");

        } finally {
            clean(resource);
            printIptables();
            ScreenUtils.screenOn();
            TestUtil.sleep(LITTLE_DELAY);
            cleanPCFUserGroup(pcfGroupName);
        }
    }

    /**
     * Create PCF application blocking port 443 on testrunner for com.seven.asimov
     * Check that port 443 on testrunner is not accessible for com.seven.asimov
     * Check that port 80 on testrunner is accessible for com.seven.asimov
     * Check that port 443 on testrunner is accessible for com.seven.asimov.it
     * Check that port 80 on testrunner is accessible for com.seven.asimov.it
     */

    public void test_005_BBH() throws Exception {
        final String resource = "test_BBH_005";
        init(resource);
        trustAllHosts(true);
        try {
            Log.v("###DEBUG", "P1");
            JSONObject expectedDelta = BlockByHostnamePreparePCF(new String[]{blockedHost}, false, Integer.toString(TFConstantsIF.HTTPS_PORT), PcfHelper.InterfaceType.ALL, false, false, false);
            checkPCFDeltaAtServer(expectedDelta.toString());
            Log.v("###DEBUG", "P2");
            printIptables();
            checkNetworkResourcesAccessible(resource, false, false, true);
            Log.v("###DEBUG", "P3");
            checkNetworkResourcesAccessible(resource, true, false, false);
            Log.v("###DEBUG", "P4");
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(interceptionEnabled, "false", ocshttpdPath, true)});
            Log.v("###DEBUG", "P5");
            printIptables("mangle");
            TestUtil.sleep(LITTLE_DELAY);
            Log.v("###DEBUG", "P6");
            checkNetworkResourcesAccessible(resource, false, false, true);
            Log.v("###DEBUG", "P7");
            checkNetworkResourcesAccessible(resource, true, false, true);
            Log.v("###DEBUG", "P8");
        } finally {
            clean(resource);
            printIptables();
            trustAllHosts(false);
            cleanPCFUserGroup(pcfGroupName);
            PMSUtil.cleanPaths(new String[]{ocshttpdPath});
        }
    }

    /**
     * Create PCF service blocking port 443 on testrunner
     * Check that port 443 on testrunner is not accessible for com.seven.asimov
     * Check that port 80 on testrunner is accessible for com.seven.asimov
     * Check that port 443 on testrunner is not accessible for com.seven.asimov.it
     * Check that port 80 on testrunner is accessible for com.seven.asimov.it
     */

    public void test_006_BBH() throws Exception {
        final String resource = "test_BBH_006";
        init(resource);
        try {
            Log.v("###DEBUG", "P1");
            JSONObject expectedDelta = BlockByHostnamePreparePCF(new String[]{blockedHost}, false, Integer.toString(TFConstantsIF.HTTPS_PORT), PcfHelper.InterfaceType.ALL, true, false, false);
            checkPCFDeltaAtServer(expectedDelta.toString());
            Log.v("###DEBUG", "P2");
            printIptables();
            checkNetworkResourcesAccessible(resource, false, false, true);
            Log.v("###DEBUG", "P3");
            checkNetworkResourcesAccessible(resource, true, false, false);
            Log.v("###DEBUG", "P4");
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(interceptionEnabled, "false", ocshttpdPath, true)});
            Log.v("###DEBUG", "P5");
            TestUtil.sleep(LITTLE_DELAY);
            printIptables("mangle");
            Log.v("###DEBUG", "P6");
            checkNetworkResourcesAccessible(resource, false, false, true);
            Log.v("###DEBUG", "P7");
            checkNetworkResourcesAccessible(resource, true, false, false);
            Log.v("###DEBUG", "P8");
        } finally {
            clean(resource);
            printIptables();
            cleanPCFUserGroup(pcfGroupName);
            PMSUtil.cleanPaths(new String[]{ocshttpdPath});
        }
    }

    /**
     * Create PCF service blocking testrunner on MOBILE interface
     * Switch network to MOBILE
     * Check that testrunner is not accessible for com.seven.asimov
     * Check that testrunner is not accessible for com.seven.asimov.it
     * Switch network to WIFI
     * Check that testrunner is accessible for com.seven.asimov
     * Check that testrunner is accessible for com.seven.asimov.it
     */

    public void test_008_BBH() throws NetworkErrorException {
        final String resource = "test_BBH_008";
        init(resource);
        try {
            Log.v("###DEBUG", "P1");
            JSONObject expectedDelta = BlockByHostnamePreparePCF(new String[]{blockedHost}, false, "", PcfHelper.InterfaceType.MOBILE, true, false, false);
            checkPCFDeltaAtServer(expectedDelta.toString());
            Log.v("###DEBUG", "P2");
            mobileNetworkUtil.on3gOnly();
            printIptables();
            Log.v("###DEBUG", "P3");
            checkNetworkResourcesAccessible(resource, false, false, false);
            Log.v("###DEBUG", "P4");
            checkNetworkResourcesAccessible(resource, false, true, false);
            Log.v("###DEBUG", "P5");

            mobileNetworkUtil.onWifiOnly();
            printIptables();
            Log.v("###DEBUG", "P6");
            checkNetworkResourcesAccessible(resource, false, false, true);
            Log.v("###DEBUG", "P7");
            checkNetworkResourcesAccessible(resource, false, true, true);
            Log.v("###DEBUG", "P8");

        } finally {
            clean(resource);
            printIptables();
            mobileNetworkUtil.on3gOnly();
            mobileNetworkUtil.onWifiOnly();
            TestUtil.sleep(LITTLE_DELAY);
            cleanPCFUserGroup(pcfGroupName);
        }
    }

    /**
     * Create PCF application blocking testrunner on MOBILE interface for com.seven.asimov
     * Switch network to MOBILE
     * Check that testrunner is not accessible for com.seven.asimov
     * Check that testrunner is accessible for com.seven.asimov.it
     * Switch network to WIFI
     * Check that testrunner is accessible for com.seven.asimov
     * Check that testrunner is accessible for com.seven.asimov.it
     */

    public void test_009_BBH() throws NetworkErrorException {
        final String resource = "test_BBH_009";
        init(resource);
        try {
            Log.v("###DEBUG", "P1");
            JSONObject expectedDelta = BlockByHostnamePreparePCF(new String[]{blockedHost}, false, "", PcfHelper.InterfaceType.MOBILE, false, false, false);
            checkPCFDeltaAtServer(expectedDelta.toString());
            Log.v("###DEBUG", "P2");
            mobileNetworkUtil.on3gOnly();
            printIptables();
            Log.v("###DEBUG", "P3");
            checkNetworkResourcesAccessible(resource, false, false, false);
            Log.v("###DEBUG", "P4");
            checkNetworkResourcesAccessible(resource, false, true, true);
            Log.v("###DEBUG", "P5");
            mobileNetworkUtil.onWifiOnly();
            printIptables();
            Log.v("###DEBUG", "P6");
            checkNetworkResourcesAccessible(resource, false, false, true);
            Log.v("###DEBUG", "P7");
            checkNetworkResourcesAccessible(resource, false, true, true);
            Log.v("###DEBUG", "P8");

        } finally {
            clean(resource);
            printIptables();
            mobileNetworkUtil.on3gOnly();
            mobileNetworkUtil.onWifiOnly();
            TestUtil.sleep(LITTLE_DELAY);
            cleanPCFUserGroup(pcfGroupName);
        }
    }

    /**
     * Create PCF service blocking testrunner on WIFI interface
     * Switch network to MOBILE
     * Check that testrunner is accessible for com.seven.asimov
     * Check that testrunner is accessible for com.seven.asimov.it
     * Switch network to WIFI
     * Check that testrunner is not accessible for com.seven.asimov
     * Check that testrunner is not accessible for com.seven.asimov.it
     */

    public void test_010_BBH() throws NetworkErrorException {
        final String resource = "test_BBH_010";
        init(resource);
        try {
            Log.v("###DEBUG", "P1");
            JSONObject expectedDelta = BlockByHostnamePreparePCF(new String[]{blockedHost}, false, "", PcfHelper.InterfaceType.WIFI, true, false, false);
            checkPCFDeltaAtServer(expectedDelta.toString());
            Log.v("###DEBUG", "P2");
            mobileNetworkUtil.on3gOnly();
            printIptables();
            Log.v("###DEBUG", "P3");
            checkNetworkResourcesAccessible(resource, false, false, true);
            Log.v("###DEBUG", "P4");
            checkNetworkResourcesAccessible(resource, false, true, true);
            Log.v("###DEBUG", "P5");
            mobileNetworkUtil.onWifiOnly();
            printIptables();
            Log.v("###DEBUG", "P6");
            checkNetworkResourcesAccessible(resource, false, false, false);
            Log.v("###DEBUG", "P7");
            checkNetworkResourcesAccessible(resource, false, true, false);
            Log.v("###DEBUG", "P8");
        } finally {
            clean(resource);
            printIptables();
            mobileNetworkUtil.on3gOnly();
            mobileNetworkUtil.onWifiOnly();
            TestUtil.sleep(LITTLE_DELAY);
            cleanPCFUserGroup(pcfGroupName);
        }
    }

    /**
     * Create PCF application blocking testrunner on WIFI interface for com.seven.asimov
     * Switch network to MOBILE
     * Check that testrunner is accessible for com.seven.asimov
     * Check that testrunner is accessible for com.seven.asimov.it
     * Switch network to WIFI
     * Check that testrunner is not accessible for com.seven.asimov
     * Check that testrunner is accessible for com.seven.asimov.it
     */

    public void test_011_BBH() throws NetworkErrorException {
        final String resource = "test_BBH_011";
        init(resource);
        try {
            Log.v("###DEBUG", "P1");
            JSONObject expectedDelta = BlockByHostnamePreparePCF(new String[]{blockedHost}, false, "", PcfHelper.InterfaceType.WIFI, false, false, false);
            checkPCFDeltaAtServer(expectedDelta.toString());
            Log.v("###DEBUG", "P2");
            mobileNetworkUtil.on3gOnly();
            printIptables();
            Log.v("###DEBUG", "P3");
            checkNetworkResourcesAccessible(resource, false, false, true);
            Log.v("###DEBUG", "P4");
            checkNetworkResourcesAccessible(resource, false, true, true);
            Log.v("###DEBUG", "P5");
            mobileNetworkUtil.onWifiOnly();
            printIptables();
            Log.v("###DEBUG", "P6");
            checkNetworkResourcesAccessible(resource, false, false, false);
            Log.v("###DEBUG", "P7");
            checkNetworkResourcesAccessible(resource, false, true, true);
            Log.v("###DEBUG", "P8");
        } finally {
            clean(resource);
            printIptables();
            mobileNetworkUtil.on3gOnly();
            mobileNetworkUtil.onWifiOnly();
            TestUtil.sleep(LITTLE_DELAY);
            cleanPCFUserGroup(pcfGroupName);
        }
    }

    /**
     * Create PCF service blocking all IP exception IP of Relay
     * Check that Relay is accessible for both com.seven.asimov and com.seven.asimov.it
     * Check that testrunner is not accessible for both com.seven.asimov and com.seven.asimov.it
     *
     * @throws android.accounts.NetworkErrorException
     */
    public void test_014_BBH() throws NetworkErrorException {
        final String resource = "test_BBH_014";
        final String allowedHost = TFConstantsIF.EXTERNAL_IP;
        final int bypassPort = 8080;
        init(resource);

        try {
            Log.v("###DEBUG", "P1");
            JSONObject expectedDelta = BlockByHostnamePreparePCF(new String[]{"!" + allowedHost}, true, "", PcfHelper.InterfaceType.ALL, true, false, false);
            checkPCFDeltaAtServer(expectedDelta.toString());
            Log.v("###DEBUG", "P2");
            printIptables();
            checkNetworkResourcesAccessible(allowedHost, resource, false, false, bypassPort, true, true);
            Log.v("###DEBUG", "P3");
            checkNetworkResourcesAccessible(allowedHost, resource, false, true, bypassPort, true, true);
            Log.v("###DEBUG", "P4");
            checkNetworkResourcesAccessible(resource, false, false, false);
            Log.v("###DEBUG", "P5");
            checkNetworkResourcesAccessible(resource, false, true, false);
            Log.v("###DEBUG", "P6");
        } finally {
            clean(resource);
            printIptables();
            cleanPCFUserGroup(pcfGroupName);
        }
    }

    /**
     * Create PCF application blocking all IP exception IP of Relay for com.seven.asimov
     * Check that Relay is accessible for both com.seven.asimov and com.seven.asimov.it
     * Check that testrunner is not accessible for com.seven.asimov
     * Check that testrunner is accessible for com.seven.asimov.it
     *
     * @throws android.accounts.NetworkErrorException
     */

    public void test_015_BBH() throws NetworkErrorException {
        final String resource = "test_BBH_015";
        String allowedHost = TFConstantsIF.EXTERNAL_IP;
        final int bypassPort = 8080;
        init(resource);

        try {
            Log.v("###DEBUG", "P1");
            JSONObject expectedDelta = BlockByHostnamePreparePCF(new String[]{"!" + allowedHost}, true, "", PcfHelper.InterfaceType.ALL, false, false, false);
            checkPCFDeltaAtServer(expectedDelta.toString());
            Log.v("###DEBUG", "P2");
            printIptables();
            checkNetworkResourcesAccessible(allowedHost, resource, false, false, bypassPort, true, true);
            Log.v("###DEBUG", "P3");
            checkNetworkResourcesAccessible(allowedHost, resource, false, true, bypassPort, true, true);
            Log.v("###DEBUG", "P4");
            checkNetworkResourcesAccessible(resource, false, false, false);
            Log.v("###DEBUG", "P5");
            checkNetworkResourcesAccessible(resource, false, true, true);
            Log.v("###DEBUG", "P6");
        } finally {
            clean(resource);
            printIptables();
            cleanPCFUserGroup(pcfGroupName);
        }
    }

    /**
     * Create PCF service blocking testrunner, protocol=tcp
     * Check that testrunner is not accessible for both com.seven.asimov and com.seven.asimov.it
     */

    public void test_016_BBH() {
        final String resource = "test_BBH_016";
        init(resource);
        try {
            Log.v("###DEBUG", "P1");
            JSONObject expectedDelta = BlockByHostnamePreparePCF(new String[]{blockedHost}, true, "", PcfHelper.InterfaceType.ALL, true, false, false);
            checkPCFDeltaAtServer(expectedDelta.toString());
            Log.v("###DEBUG", "P2");
            printIptables();
            checkNetworkResourcesAccessible(resource, false, false, false);
            Log.v("###DEBUG", "P3");
            checkNetworkResourcesAccessible(resource, false, true, false);
            Log.v("###DEBUG", "P4");
        } finally {
            clean(resource);
            printIptables();
            cleanPCFUserGroup(pcfGroupName);
        }
    }

    /**
     * Create PCF application blocking testrunner for com.seven.asimov  protocol=tcp
     * Check that testrunner is not accessible for com.seven.asimov
     * Check that testrunner is accessible for com.seven.asimov.it
     */

    public void test_017_BBH() {
        final String resource = "test_BBH_017";
        init(resource);
        try {
            Log.v("###DEBUG", "P1");
            JSONObject expectedDelta = BlockByHostnamePreparePCF(new String[]{blockedHost}, true, "", PcfHelper.InterfaceType.ALL, false, false, false);
            checkPCFDeltaAtServer(expectedDelta.toString());
            Log.v("###DEBUG", "P2");
            printIptables();
            checkNetworkResourcesAccessible(resource, false, false, false);
            Log.v("###DEBUG", "P3");
            checkNetworkResourcesAccessible(resource, false, true, true);
            Log.v("###DEBUG", "P4");
        } finally {
            clean(resource);
            printIptables();
            cleanPCFUserGroup(pcfGroupName);
        }
    }

    /**
     * Create PCF service blocking testrunner and IPv6 testrunner
     * Check that testrunner and IPv6 testrunner are not accessible for both com.seven.asimov and com.seven.asimov.it
     */
    public void test_018_BBH() {
        final String resource = "test_BBH_018";
        init(resource);
        try {
            Log.v("###DEBUG", "P1");
            JSONObject expectedDelta = BlockByHostnamePreparePCF(new String[]{blockedHost, ipv6BlockedIP}, false, "", PcfHelper.InterfaceType.ALL, true, false, false);
            checkPCFDeltaAtServer(expectedDelta.toString());
            Log.v("###DEBUG", "P2");
            printIptables();
            checkNetworkResourcesAccessible(resource, false, false, false);
            Log.v("###DEBUG", "P3");
            checkNetworkResourcesAccessible(resource, false, true, false);
            Log.v("###DEBUG", "P4");
            checkNetworkResourcesAccessible(ipv6BlockedHost, resource, false, false, false);
            Log.v("###DEBUG", "P5");
            checkNetworkResourcesAccessible(ipv6BlockedHost, resource, false, true, false);
            Log.v("###DEBUG", "P6");
        } finally {
            clean(resource);
            printIptables();
            cleanPCFUserGroup(pcfGroupName);
        }
    }

    /**
     * Create PCF service blocking testrunner and IPv6 testrunner
     * Check that testrunner and IPv6 testrunner are not accessible for com.seven.asimov
     * Check that testrunner and IPv6 testrunner are accessible for com.seven.asimov.it
     */
    public void test_019_BBH() {
        final String resource = "test_BBH_019";
        init(resource);
        try {
            Log.v("###DEBUG", "P1");
            JSONObject expectedDelta = BlockByHostnamePreparePCF(new String[]{blockedHost, ipv6BlockedIP}, false, "", PcfHelper.InterfaceType.ALL, false, false, false);
            checkPCFDeltaAtServer(expectedDelta.toString());
            Log.v("###DEBUG", "P2");
            printIptables();
            checkNetworkResourcesAccessible(resource, false, false, false);
            Log.v("###DEBUG", "P3");
            checkNetworkResourcesAccessible(resource, false, true, true);
            Log.v("###DEBUG", "P4");
            checkNetworkResourcesAccessible(ipv6BlockedHost, resource, false, false, false);
            Log.v("###DEBUG", "P5");
            checkNetworkResourcesAccessible(ipv6BlockedHost, resource, false, true, true);
            Log.v("###DEBUG", "P6");
        } finally {
            clean(resource);
            printIptables();
            cleanPCFUserGroup(pcfGroupName);
        }
    }

    public void test_099_BBH() throws Exception {
        PMSUtil.cleanPaths(new String[]{interceptionPath});
    }

    @Override
    protected void runTest() throws Throwable {
        boolean isPassed;
        int numberOfAttempts = 0;
        List<String> counts = new ArrayList<String>();
        do {
            isPassed = true;
            numberOfAttempts++;
            try {
                super.runTest();
            } catch (Throwable error) {
                logger.debug("Test failed due to " + ExceptionUtils.getStackTrace(error));
                isPassed = false;
                counts.add("Test failed due to AssertionFailedError in " + numberOfAttempts + " attempt");
                Thread.sleep(2 * 60 * 1000);
            }
        } while (!isPassed && numberOfAttempts < 3);

        assertTrue("The test was failed three times ", counts.size() != 3);
    }
}
