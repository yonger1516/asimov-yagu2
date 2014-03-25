package com.seven.asimov.it.tests.crcs.netlog;

import android.test.suitebuilder.annotation.SmallTest;
import com.seven.asimov.it.annotation.Execute;
import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.testcases.NetlogZDnsTestCase;
import com.seven.asimov.it.utils.logcat.tasks.crcsTasks.NetlogTask;

/**
 * <h2>This class tests OCC's Netlog functionality for DNS.</h2>
 * ASMV-21417 Fix Netlog suite
 */
@Ignore
public class NetlogZDnsTest extends NetlogZDnsTestCase {

    private static final String DESTINATION_PORT_FIELD_NAME = "DstPort";
    private static final String DESTINATION_PORT_ERROR_MESSAGE = " Destination port from netlog is incorrect : ";
    private static final String NETWORK_PROTOCOL_STACK_FIELD_NAME = "NetworkProtocolStack";
    private static final String NETWORK_PROTOCOL_STACK_ERROR_MESSAGE = "Network Protocol Stack from netlog is incorrect : ";
    private static final String LOCAL_PROTOCOL_STACK_FIELD_NAME = "LocalProtocolStack";
    private static final String LOCAL_PROTOCOL_STACK_ERROR_MESSAGE = "Local Protocol Stack from netlog is incorrect : ";

    private static final String[] hosts = new String[] {"google.com", "ukr.net" , "not.existing.host.com.kiev.ua" , "not.existing.host" ,
            "facebook.com" , "twitter.com"};

    /**
     * <h3>Verify OC works correctly with Destination Port.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog Destination Port is correct</li>
     * </ol>
     * @throws Throwable
     */
    @Execute
    @SmallTest
    public void test_001_DnsDestinationPortCheck() throws Exception {
        NetlogTask netlogTask = getNetlogTask(hosts[0]);
        executeGeneralCheck_NetlogZDns(hosts[0], DESTINATION_PORT_FIELD_NAME, "53", DESTINATION_PORT_ERROR_MESSAGE, netlogTask.getLogEntries());
    }

    /**
     * <h3>Verify OC works correctly with Destination Port.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Throwable
     */
    @Execute
    @SmallTest
    public void test_002_DnsCrcsDataCheck() throws Throwable {
        NetlogTask netlogTask = getNetlogTask(hosts[1]);
        executeDataCheck_NetlogZDns(hosts[1], netlogTask.getLogEntries(), false);
    }

    /**
     * <h3>Verify OC works correctly if host doesnt exists.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Throwable
     */
    @Execute
    @SmallTest
    public void test_003_DnsNotExistingHost1() throws Throwable {
        NetlogTask netlogTask = getNetlogTask(hosts[2]);
        executeDataCheck_NetlogZDns(hosts[2], netlogTask.getLogEntries(), false);
    }

    /**
     * <h3>Verify OC works correctly if host doesnt exists.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Throwable
     */
    @Execute
    @SmallTest
    public void test_004_DnsNotExistingHost2() throws Throwable {
        NetlogTask netlogTask = getNetlogTask(hosts[3]);
        executeDataCheck_NetlogZDns(hosts[3], netlogTask.getLogEntries(), false);
    }

    /**
     * <h3>Verify OC works correctly with Local Protocol Stack.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data and Local Protocol Stack is correct</li>
     * </ol>
     * @throws Throwable
     */
    @Execute
    @SmallTest
    public void test_005_DnsLocalProtocolStackCheck() throws Throwable {
        NetlogTask netlogTask = getNetlogTask(hosts[4]);
        executeGeneralCheck_NetlogZDns(hosts[4], LOCAL_PROTOCOL_STACK_FIELD_NAME, "-/-/dns/udp", LOCAL_PROTOCOL_STACK_ERROR_MESSAGE, netlogTask.getLogEntries());
    }

    /**
     * <h3>Verify OC works correctly with Network Protocol Stack.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data and Network Protocol Stack is correct</li>
     * </ol>
     * @throws Throwable
     */
    @Execute
    @SmallTest
    public void test_006_DnsNetworkProtocolStackCheck() throws Throwable {
        NetlogTask netlogTask = getNetlogTask(hosts[5]);
        executeGeneralCheck_NetlogZDns(hosts[5], NETWORK_PROTOCOL_STACK_FIELD_NAME, "-/-/dns/udp", NETWORK_PROTOCOL_STACK_ERROR_MESSAGE, netlogTask.getLogEntries());
    }

    //================== Local Dns server (work fine without network) ============================

    /**
     * <h3>Verify OC works correctly with Data response type A.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump</li>
     * </ol>
     * @throws Throwable
     */
    @SmallTest
    public void test_007_DataCheckResponseTypeA() throws Exception {
        final String host = "response.type.a.host.com.";
        final String hostShort = "response.type.a.host.com";

        //testDnsQueryResponse(host, "DnsTestQueryResponseTypeA", Type.A, true, false);
        executeDataCheck_NetlogZDns(hostShort, netlogTask.getLogEntries(), true);
    }

    /**
     * <h3>Verify OC works correctly with Data response type AAAA.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump</li>
     * </ol>
     * @throws Throwable
     */
    @SmallTest
    public void test_008_DataCheckQueryAAAA() throws Exception {
        String host = "query.type.aaaa.host.com.";
        String hostShort = "query.type.aaaa.host.com";

        //testDnsQueryResponse(host, "DnsTestQueryResponseTypeAAAA", Type.AAAA, false, true);
        executeDataCheck_NetlogZDns(hostShort, netlogTask.getLogEntries(), true);
    }

    /**
     * <h3>Verify OC works correctly with Data response type NS.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump</li>
     * </ol>
     * @throws Throwable
     */
    @SmallTest
    public void test_009_DataCheckQueryNS() throws Exception {
        String host = "query.type.ns.host.com.";
        String hostShort = "query.type.ns.host.com";

        //testDnsQueryResponse(host, "DnsTestQueryResponseTypeNS", Type.NS, false, true);
        executeDataCheck_NetlogZDns(hostShort, netlogTask.getLogEntries(), true);
    }

    /**
     * <h3>Verify OC works correctly with Data response type CNAME.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump</li>
     * </ol>
     * @throws Throwable
     */
    @SmallTest
    public void test_010_DataCheckQueryCHNAME() throws Exception {
        String host = "query.type.cname.host.com.";
        String hostShort = "query.type.cname.host.com";

        //testDnsQueryResponse(host, "DnsTestQueryResponseTypeCNAME", Type.CNAME, false, true);
        executeDataCheck_NetlogZDns(hostShort, netlogTask.getLogEntries(), true);
    }

    /**
     * <h3>Verify OC works correctly with Data response type SOA.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump</li>
     * </ol>
     * @throws Throwable
     */
    @SmallTest
    public void test_011_DataCheckQuerySOA() throws Exception {

        String host = "query.type.soa.host.com.";
        String hostShort = "query.type.soa.host.com";

        //testDnsQueryResponse(host, "DnsTestQueryNS", Type.SOA, false, true);
        executeDataCheck_NetlogZDns(hostShort, netlogTask.getLogEntries(), true);
    }
}