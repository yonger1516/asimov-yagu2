package com.seven.asimov.it.tests.dispatchers.proxy;

import com.seven.asimov.it.annotation.DeviceOnly;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.testcases.DnsProxyTestCase;
import com.seven.asimov.it.utils.MobileNetworkUtil;
import com.seven.asimov.it.utils.date.DateUtil;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;

/**
 * Check the correctness of dns policies: ({@link DnsProxyTests#test_001_DnsProxy() Test 1},
 * {@link DnsProxyTests#test_002_DnsProxy() Test 2},
 * {@link DnsProxyTests#test_003_DnsProxy() Test 3},
 * {@link DnsProxyTests#test_004_DnsProxy() Test 4},
 */
public class DnsProxyTests extends DnsProxyTestCase {

    private static final String DNS_URI_1 = "http://www.ukr.net/";
    private static final String DNS_HOST_1 = "www.ukr.net";
    private static final String DNS_URI_2 = "http://www.bigmir.net/";
    private static final String DNS_HOST_2 = "www.bigmir.net";
    private static final String DNS_URI_3 = "http://www.ebay.com/";
    private static final String DNS_HOST_3 = "www.ebay.com";
    private final static String TAG = DnsProxyTests.class.getSimpleName();

    /**
     * <h1>Testing that the policy cache_enabled works correctly.</h1>
     * <p>Expected results: MISS, HIT.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Create some test uri</li>
     * <li>Create an appropriate policy with the parameters. Path: DNS_POLICY_PATH. Name: CACHE_ENABLED. Value: 1</li>
     * <li>Wait for the policy update</li>
     * <li>Verify that we have MISS, HIT. The delay between requests 30 seconds</li>
     * </ol>
     *
     * @throws Exception
     */
    @DeviceOnly
    public void test_001_DnsProxy() throws Exception {
        MobileNetworkUtil mobileNetworkHelper = MobileNetworkUtil.init(getContext());
        mobileNetworkHelper.on3gOnly();
        logSleeping(1 * DateUtil.MINUTES);

        final long[] intervals = new long[]{0, 30};
        final long[] timeShifts = new long[]{0, 1 * DateUtil.HOURS};
        final String[] expectedResults = new String[]{MISS, HIT};
        final Policy defaultCacheTtl = new Policy(TFConstantsIF.DEFAULT_CACHE_TTL, "4000", TFConstantsIF.DNS_POLICY_PATH, true);
        final Policy cacheEnabled = new Policy(TFConstantsIF.CACHE_ENABLED, "1", TFConstantsIF.DNS_POLICY_PATH, true);
        PMSUtil.addPolicies(new Policy[]{defaultCacheTtl, cacheEnabled});
        logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
        testDnsVerdicts(DNS_URI_1, intervals, timeShifts, expectedResults, DNS_HOST_1);
    }

    /**
     * <h1>Testing that the policy cache_enabled works correctly.</h1>
     * <p>Expected results: MISS, MISS.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Create some test uri</li>
     * <li>Create an appropriate policy with the parameters. Path: DNS_POLICY_PATH. Name: CACHE_ENABLED. Value: 0</li>
     * <li>Wait for the policy update</li>
     * <li>Verify that we have MISS, MISS. The delay between requests 10 seconds</li>
     * </ol>
     *
     * @throws Exception
     */
    @DeviceOnly
    public void test_002_DnsProxy() throws Exception {
        final long[] intervals = new long[]{0, 30};
        final long[] timeShifts = new long[]{0, 1 * DateUtil.HOURS};
        final String[] expectedResults = new String[]{MISS, MISS};
        final Policy defaultCacheTtl = new Policy(TFConstantsIF.DEFAULT_CACHE_TTL, "4000", TFConstantsIF.DNS_POLICY_PATH, true);
        final Policy cacheEnabled = new Policy(TFConstantsIF.CACHE_ENABLED, "0", TFConstantsIF.DNS_POLICY_PATH, true);
        PMSUtil.addPolicies(new Policy[]{defaultCacheTtl, cacheEnabled});
        logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
        testDnsVerdicts(DNS_URI_2, intervals, timeShifts, expectedResults, DNS_HOST_2);
    }

    /**
     * <h1>Testing that the policy default_cache_ttl works correctly.</h1>
     * <p>Expected results: MISS, MISS.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Create some test uri</li>
     * <li>Create an appropriate policy with the parameters. Path: DNS_POLICY_PATH. Name: DEFAULT_CACHE_TTL. Value: 600</li>
     * <li>Wait for the policy update</li>
     * <li>Verify that we have MISS</li>
     * <li>Shift time forward for 1 our</li>
     * <li>Verify that we have MISS</li>
     * </ol>
     *
     * @throws Exception
     */
    @DeviceOnly
    public void test_003_DnsProxy() throws Exception {
        final long[] intervals = new long[]{0, 30};
        final long[] timeShifts = new long[]{0, 1 * DateUtil.HOURS};
        final String[] expectedResults = new String[]{MISS, MISS};
        final Policy defaultCacheTtl = new Policy(TFConstantsIF.DEFAULT_CACHE_TTL, "600", TFConstantsIF.DNS_POLICY_PATH, true);
        final Policy cacheEnabled = new Policy(TFConstantsIF.CACHE_ENABLED, "0", TFConstantsIF.DNS_POLICY_PATH, false);
        PMSUtil.addPolicies(new Policy[]{defaultCacheTtl, cacheEnabled});
        logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
        testDnsVerdicts(DNS_URI_3, intervals, timeShifts, expectedResults, DNS_HOST_3);
    }

    /**
     * <h1>Testing that the policy dns_packet_timeout works correctly.</h1>
     * <p>Expected results: DNS request timeout should be equal 15 seconds.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Create some test uri</li>
     * <li>Create an appropriate policy with the parameters. Path: DNS_POLICY_PATH. Name: DNS_PACKET_TIMEOUT. Value: 15</li>
     * <li>Wait for the policy update</li>
     * <li>Verify that we have appropriate netlog with the error code -8</li>
     * </ol>
     *
     * @throws Exception
     */
    @DeviceOnly
    public void test_004_DnsProxy() throws Exception {
        final Policy dnsPacketTimeout = new Policy(TFConstantsIF.DNS_PACKET_TIMEOUT, "15", TFConstantsIF.DNS_POLICY_PATH, true);
        PMSUtil.addPolicies(new Policy[]{dnsPacketTimeout});
        logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
        checkDnsTimeOut("response.type.a.host.com.", "-8");
        PMSUtil.cleanPaths(new String[]{TFConstantsIF.DNS_POLICY_PATH});
        logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
    }
}
