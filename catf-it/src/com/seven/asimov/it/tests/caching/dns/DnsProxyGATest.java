package com.seven.asimov.it.tests.caching.dns;


import android.test.suitebuilder.annotation.SmallTest;
import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.testcases.DnsTestCase;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class DnsProxyGATest extends DnsTestCase {
    private static final Logger logger = LoggerFactory.getLogger(DnsProxyGATest.class.getSimpleName());

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

            } catch (Throwable throwable) {
                logger.info("Test failed due to " + ExceptionUtils.getStackTrace(throwable));
                isPassed = false;
                counts.add("Test failed due to Exception in " + numberOfAttempts + " attempt");
                cleanOCDnsCache();
                Thread.sleep(2 * 60 * 1000);
            }
        } while (!isPassed && numberOfAttempts < 3);

        assertTrue("The test was failed three times ", counts.size() != 3);
    }

    @SmallTest
    public void test_000_Dns() throws Exception {
        switchRestartFailover(true);
        switchRestartFailover(false);
        cleanOCDnsCache();
    }

    /**
     * Sends two dns requests for A record to check caching.
     *
     * @throws Exception
     */
    @SmallTest
    public void test_001_Dns_TC_02_03_06_CacheHitAndExpiration() throws Exception {
        doDnsCachingTest(DNSRR.CacheHitAndExpiration);
    }


    /**
     * Sends 5 requests for 5 different hosts twice  with A record request to check multiple records caching in parallel.
     *
     * @throws Exception
     */
    @SmallTest
    public void test_002_Dns_TC_47_MultipleQueriesCached() throws Exception {
        final DNSRR[] records = new DNSRR[]{DNSRR.Multi1Record, DNSRR.Multi2Record, DNSRR.Multi3Record, DNSRR.Multi4Record, DNSRR.Multi5Record};

        doDnsMultipleQueriesWithCheck(records, false);
        doDnsMultipleQueriesWithCheck(records, true);

    }


    /**
     * TODO: @Ignore //Real dns server  can't work with non-latin dns records.
     * Sends requests with non-latin characters to check caching.
     * Ignore Real dns server  can't work with non-latin dns records.
     *
     * @throws Exception
     */
    @SmallTest
    @Ignore //Real dns server  can't work with non-latin dns records.
    public void test_003_Dns_TC_48_NonLatinCharacterParsing() throws Exception {
        doDnsCachingTest(DNSRR.NonLatin1);
        doDnsCachingTest(DNSRR.NonLatin2);
    }


    /**
     * Checks caching of A record.
     *
     * @throws Exception
     */
    @SmallTest
    public void test_004_Dns_TC_49_ResponseTypeA() throws Exception {
        doDnsCachingTest(DNSRR.ATest);
    }


    /**
     * Check caching of AAAA record (IPv6 address)
     *
     * @throws Exception
     */
    @SmallTest
    public void test_005_Dns_TC_50_QueryAAAA() throws Exception {
        doDnsCachingTest(DNSRR.AAAATest);
    }


    /**
     * Checks caching of NS record.
     *
     * @throws Exception
     */
    @SmallTest
    public void test_006_Dns_TC_51_QueryNS() throws Exception {
        doNSRecordCachingTest();
    }


    /**
     * Checks caching of CNAME record.
     *
     * @throws Exception
     */
    @SmallTest
    public void test_007_Dns_TC_52_QueryCNAME() throws Exception {
        doDnsCachingTest(DNSRR.CNAMETest);
    }


    /**
     * Checks caching of SOA record.
     *
     * @throws Exception
     */
    @SmallTest
    public void test_008_Dns_TC_53_QuerySOA() throws Exception {
        doDnsCachingTest(DNSRR.SOATest);
    }


    /**
     * Checks caching of PTR record with A record Answer.
     *
     * @throws Exception
     */
    @SmallTest
    public void test_009_Dns_TC_55_ReverseDnsLookUpGateway() throws Exception {
        doDnsCachingTest(DNSRR.PTRATest);
    }


    /**
     * Checks caching of PTR record with PTR record Answer.
     *
     * @throws Exception
     */
    @SmallTest
    public void test_010_Dns_TC_57_ResponseTypePTR() throws Exception {
        doDnsCachingTest(DNSRR.PTR4Test);
    }


    /**
     * TODO: @Ignore Is clone of testDns_TC_57_ResponseTypePTR
     * Checks caching of PTR record.
     *
     * @throws Exception
     */
    @SmallTest
    @Ignore //Is clone of testDns_TC_57_ResponseTypePTR
    public void test_011_Dns_TC_56_ResponseTypePTR_Asmv5375() throws Exception {

//        DnsTestQueryResponseTypePTR_ASMV_5375 testCase = new DnsTestQueryResponseTypePTR_ASMV_5375();
//        testCase.run();
//        assertTrue(testCase.getName(), testCase.isPassed());
    }


    /**
     * Checks caching of MX record.
     *
     * @throws Exception
     */
    @SmallTest
    public void test_012_Dns_TC_58_ResponseTypeMX() throws Exception {
        doDnsCachingTest(DNSRR.MXTest);
    }


    /**
     * Checks caching of TXT record.
     *
     * @throws Exception
     */
    @SmallTest
    public void test_013_Dns_TC_59_ResponseTypeTXT() throws Exception {
        doDnsCachingTest(DNSRR.TXTTest);
    }

    /**
     * Checks caching of SRV record.
     *
     * @throws Exception
     */
    @SmallTest
    public void test_014_Dns_TC_60_ResponseTypeSRV() throws Exception {
        doDnsCachingTest(DNSRR.SRVTest);
    }

    /**
     * TODO: @Ignore Real DNS server doesn't send answer for request with empty Question field.
     * Checks caching when thre is no QUESTIOn block in dns request.
     *
     * @throws Exception
     */
    @SmallTest
    @Ignore //Real DNS server doesn't send answer for request with empty Question field.
    public void test_015_Dns_TC_61_ResponseTypeANoQuestion() throws Exception {
        doDnsCachingTest(DNSRR.ANOQuestionRecord);
    }


    /**
     * TODO: @Ignore Wrong test logic. We send dns requests directly. Android dns settings doesn't matter.
     * Checks caching when Android DNS is disabled.
     *
     * @throws Exception
     */
    @SmallTest
    @Ignore //Wrong test logic. We send dns requests directly. Android dns settings doesn't matter.
    public void test_016_Dns_TC_62_DisabledAndroidDns() throws Exception {

//        UniversalDnsTest test = null;
//        try {
//            test = new UniversalDnsTest("google.com");
//            test.disableAndroidDns = true;
//            test.runTests(new int[]{30, 30, 30});
//        } finally {
//            test.reset();
//        }
    }


    /**
     * TODO: @Ignore  Real DNS server doesn't send answer for mailformed request.
     * Checks caching of mailformed DNS request.
     *
     * @throws Exception
     */
    @SmallTest
    @Ignore  //Real DNS server doesn't send answer for mailformed request.
    public void test_017_Dns_TC_63_MalformedDNSRequest() throws Exception {
//        DnsTestMalformedQuery testCase = new DnsTestMalformedQuery();
//        testCase.run();
//        assertTrue(testCase.getName(), testCase.isPassed());
//        TestUtils.sleep(30 * 1000);
    }

    public void test_018_Dns_TC_99_FinishTests() throws Exception {
        switchRestartFailover(true);
    }

}

