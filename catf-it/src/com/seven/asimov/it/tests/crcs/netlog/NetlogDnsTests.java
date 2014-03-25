package com.seven.asimov.it.tests.crcs.netlog;

import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.testcases.NetlogDnsTestCase;
import com.seven.asimov.it.utils.DnsUtil;
import com.seven.asimov.it.utils.IpTablesUtil;
import com.seven.asimov.it.utils.MobileNetworkUtil;
import com.seven.asimov.it.utils.TestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Message;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

import java.net.SocketTimeoutException;

/**
 * <h2>This class tests OCC's Netlog functionality for dns requests.</h2>
 */
public class NetlogDnsTests extends NetlogDnsTestCase {

    private static final Logger logger = LoggerFactory.getLogger(NetlogDnsTests.class.getSimpleName());

    private final String DNS_SERVER = "8.8.8.8";
    private final String DNS_HOST = AsimovTestCase.TEST_RESOURCE_HOST;
    private final String ERROR_CODE = "-56024";

    private Message message;

    /**
     * <p>Verify that value of parameters in NetLog are correctly in case dns request that is MISS</p>
     * <p>Action:</p>
     * <ul>
     *     <li>Install policies enabled failover after restart OC processes to 'false'</li>
     *     <li>Clean OC DNS cache</li>
     *     <li>Send DNS-request</li>
     *     <li>Delete installed policies</li>
     * </ul>
     * <p>Checks:</p>
     * <ul>
     *     <li>Check in netlog that correspond DNS-transaction target values</li>
     * </ul>
     */
    public void test_001_DnsRequestIsMiss() throws Exception {
        DnsUtil dnsUtil = new DnsUtil();
        long timeStart;
        long timeStop;
        switchRestartFailover(false);

        try {
            dnsUtil.cleanOCDnsCache();
            SimpleResolver resolver = new SimpleResolver(DNS_SERVER);
            resolver.setTimeout(90);
            timeStart = System.currentTimeMillis();
            message = resolver.send(dnsUtil.createDnsQuery(DNS_HOST, Type.A));
            logger.info(message.toString());
            TestUtil.sleep(5 * 1000);
            timeStop = System.currentTimeMillis();
        } finally {
            switchRestartFailover(true);
        }
        addDnsCheck(timeStart, timeStop, AsimovTestCase.TEST_RESOURCE_HOST, false, false, true, false, null);
    }

    /**
     * <p>VVerify that value of parameters in NetLog are correctly in case dns request that is HIT</p>
     * <p>Action:</p>
     * <ul>
     *     <li>Install policies enabled failover after restart OC processes to 'false'</li>
     *     <li>Clean OC DNS cache</li>
     *     <li>Send DNS-request</li>
     *     <li>Delete installed policies</li>
     * </ul>
     * <p>Checks:</p>
     * <ul>
     *     <li>Check in netlog that correspond DNS-transaction target values</li>
     * </ul>
     */
    public void test_002_DnsRequestIsHit() throws Exception {
        DnsUtil dnsUtil = new DnsUtil();
        long timeStart;
        long timeStop;
        switchRestartFailover(false);

        try {
            dnsUtil.cleanOCDnsCache();
            SimpleResolver resolver = new SimpleResolver(DNS_SERVER);
            resolver.setTimeout(90);

            message = resolver.send(dnsUtil.createDnsQuery(DNS_HOST, Type.A));
            logger.info(message.toString());
            TestUtil.sleep(5 * 1000);

            timeStart = System.currentTimeMillis();

            message = resolver.send(dnsUtil.createDnsQuery(DNS_HOST, Type.A));
            logger.info(message.toString());

            timeStop = System.currentTimeMillis();
        } finally {
            switchRestartFailover(true);
        }
        addDnsCheck(timeStart, timeStop, AsimovTestCase.TEST_RESOURCE_HOST, false, true, false, true, null);
    }

    /**
     * <p>Verify that value of parameters in NetLog are correctly in case dns error</p>
     * <p>Action:</p>
     * <ul>
     *     <li>Install policies enabled failover after restart OC processes to 'false'</li>
     *     <li>Clean OC DNS cache</li>
     *     <li>Send DNS-request</li>
     *     <li>Delete installed policies</li>
     * </ul>
     * <p>Checks:</p>
     * <ul>
     *     <li>Check in netlog that correspond DNS-transaction target values</li>
     * </ul>
     */
    public void test_003_DnsError() throws Exception {
        DnsUtil dnsUtil = new DnsUtil();
        long timeStart = 0;
        long timeStop = 0;
        MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());
        switchRestartFailover(false);

        try {
            dnsUtil.cleanOCDnsCache();
            SimpleResolver resolver = new SimpleResolver(DNS_SERVER);
            resolver.setTimeout(90);
            logger.info("Ban by iptables");
            IpTablesUtil.banAddress(DNS_SERVER);
            mobileNetworkUtil.on3gOnly();
            mobileNetworkUtil.onWifiOnly();
            timeStart = System.currentTimeMillis();
            resolver.send(dnsUtil.createDnsQuery(DNS_HOST, Type.A));
        } catch (SocketTimeoutException e) {
            timeStop = System.currentTimeMillis();
            logger.info("SocketTimeoutException was happend as expected.");
        } finally {
            IpTablesUtil.unbanAddress(DNS_SERVER);
            mobileNetworkUtil.on3gOnly();
            mobileNetworkUtil.onWifiOnly();
            switchRestartFailover(true);
        }
        addDnsCheck(timeStart, timeStop, AsimovTestCase.TEST_RESOURCE_HOST, false, false, true, true, ERROR_CODE);
    }
}