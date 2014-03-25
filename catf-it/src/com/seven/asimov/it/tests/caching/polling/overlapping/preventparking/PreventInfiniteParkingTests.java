package com.seven.asimov.it.tests.caching.polling.overlapping.preventparking;


import com.seven.asimov.it.testcases.PreventInfiniteParkingTestCase;

/**
 * Tests for feature http://jira.seven.com/browse/ASMV-21660
 * Overlapping feature enhancement - workaround to prevent infinite parking for RR
 */
public class PreventInfiniteParkingTests extends PreventInfiniteParkingTestCase {

    public String TAG = PreventInfiniteParkingTests.class.getSimpleName();

    /**
     * Steps:
     * 1. Add policy "max_parked_trxs_in_err_count" with value "4" to "@asimov@http"
     * 2. Send 2 same HTTP requests to detect LP(req1-req2).
     * 4. More new requests req3-req10 send to the network (they should be parked on outstanding req2);
     * 5. End with IN socket parked req3-req7;
     * 6. Send req11;
     * <p/>
     * Expected result:
     * 1. Max parked trxs IN socket err count reached;
     * 2. Service log should be sent with message above;
     * 3. req1 (parent) should be finished with err and req8-req10 (parked) should be aborted;
     * 4. req11 should be missed to the network
     *
     * @throws Throwable
     */
    public void test_001_ERR_on_MISS_HTTP() throws Throwable {

        String value = "4";
        String uri = createTestResourceUri("test_001", false);
        int inSockCount = 5;
        int abortCount = 2;
        int inSockStart = 75 * 1000;
        int abortStart = 85 * 1000;
        int expectedInSock = 5;
        int expectedAbort = 2;

        testForPreventInfinitiveParking(value, uri, inSockCount, abortCount, inSockStart, abortStart, expectedInSock, expectedAbort, false);
    }

    /**
     * Steps:
     * 1. Add policy "max_parked_trxs_in_err_count" with value "4" to "@asimov@http"
     * 2. Send 2 same HTTP requests to detect LP(req1-req2).
     * 4. More new requests req3-req10 send to the network (they should be parked on outstanding req2);
     * 5. End with IN socket parked req3-req7;
     * 6. Send req11;
     * <p/>
     * Expected result:
     * 1. Max parked trxs IN socket err count reached;
     * 2. Service log should be sent with message above;
     * 3. req1 (parent) should be finished with err and req8-req10 (parked) should be aborted;
     * 4. req11 should be missed to the network
     *
     * @throws Throwable
     */
    public void test_002_ERR_on_HIT_HTTP() throws Throwable {

        String value = "4";
        String uri = createTestResourceUri("test_002", false);
        int inSockCount = 5;
        int abortCount = 2;
        int inSockStart = 149 * 1000;
        int abortStart = 159 * 1000;
        int expectedInSock = 5;
        int expectedAbort = 2;

        testForPreventInfinitiveParking(value, uri, inSockCount, abortCount, inSockStart, abortStart, expectedInSock, expectedAbort, false);
    }


    public void test_003_HIT_after_ERR() throws Throwable {

        String value = "9";
        String uri = createTestResourceUri("test_003", false);
        int inSockCount = 9;
        int abortCount = 0;
        int inSockStart = 149 * 1000;
        int abortStart = 0;
        int expectedInSock = 9;
        int expectedAbort = 0;

        testForPreventInfinitiveParking(value, uri, inSockCount, abortCount, inSockStart, abortStart, expectedInSock, expectedAbort, false);
    }

    /**
     * Steps:
     * 1. Add policy "max_parked_trxs_in_err_count" with value "4" to "@asimov@http"
     * 2. Send 2 same HTTPS requests to detect LP(req1-req2).
     * 4. More new requests req3-req10 send to the network (they should be parked on outstanding req2);
     * 5. End with IN socket parked req3-req7;
     * 6. Send req11;
     * <p/>
     * Expected result:
     * 1. Max parked trxs IN socket err count reached;
     * 2. Service log should be sent with message above;
     * 3. req1 (parent) should be finished with err and req8-req10 (parked) should be aborted;
     * 4. req11 should be missed to the network
     *
     * @throws Throwable
     */
    public void test_004_ERR_on_MISS_HTTPS() throws Throwable {

        String value = "4";
        String uri = createTestResourceUri("test_004", true);
        int inSockCount = 5;
        int abortCount = 2;
        int inSockStart = 75 * 1000;
        int abortStart = 85 * 1000;
        int expectedInSock = 5;
        int expectedAbort = 2;

        testForPreventInfinitiveParking(value, uri, inSockCount, abortCount, inSockStart, abortStart, expectedInSock, expectedAbort, false);
    }

    /**
     * Steps:
     * 1. Add policy "max_parked_trxs_in_err_count" with value "4" to "@asimov@http"
     * 2. Send 2 same HTTPS requests to detect LP(req1-req2).
     * 4. More new requests req3-req10 send to the network (they should be parked on outstanding req2);
     * 5. End with IN socket parked req3-req7;
     * 6. Send req11;
     * <p/>
     * Expected result:
     * 1. Max parked trxs IN socket err count reached;
     * 2. Service log should be sent with message above;
     * 3. req1 (parent) should be finished with err and req8-req10 (parked) should be aborted;
     * 4. req11 should be missed to the network
     *
     * @throws Throwable
     */
    public void test_005_ERR_on_HIT_HTTPS() throws Throwable {

        String value = "4";
        String uri = createTestResourceUri("test_005", true);
        int inSockCount = 5;
        int abortCount = 2;
        int inSockStart = 149 * 1000;
        int abortStart = 159 * 1000;
        int expectedInSock = 6;
        int expectedAbort = 2;

        testForPreventInfinitiveParking(value, uri, inSockCount, abortCount, inSockStart, abortStart, expectedInSock, expectedAbort, false);
    }

    /**
     * Steps:
     * 1. Add policy "max_parked_trxs_in_err_count" with value "6" to "@asimov@http"
     * 2. Send 3 same HTTP requests to detect LP(req1-req2).
     * 4. More new requests req4-req12 send to the network (they should be parked on outstanding req3);
     * 5. Don't end with IN socket parked req4-req12;
     * <p/>
     * Expected result:
     * 1. Max parked trxs IN socket err count reached;
     * 2. Service log should be sent with message above;
     * 3. req3 (parent) shouldn't be finished with err
     * 4. req4 - req12 should be HITed     *
     *
     * @throws Throwable
     */
    public void test_006_NO_ERR_without_IN_Socket() throws Throwable {

        String value = "6";
        String uri = createTestResourceUri("test_006", false);
        int inSockCount = 9;
        int abortCount = 0;
        int inSockStart = 148 * 1000;
        int abortStart = 0;
        int expectedInSock = 0;
        int expectedAbort = 0;

        testForPreventInfinitiveParking(value, uri, inSockCount, abortCount, inSockStart, abortStart, expectedInSock, expectedAbort, true);
    }
}
