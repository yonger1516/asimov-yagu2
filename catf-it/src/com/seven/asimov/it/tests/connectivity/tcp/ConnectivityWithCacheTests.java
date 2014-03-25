package com.seven.asimov.it.tests.connectivity.tcp;

import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import com.seven.asimov.it.lib.connectivity.*;
import com.seven.asimov.it.testcases.ConnectivityTcpTestCase;
import com.seven.asimov.it.utils.conn.ConnSelector;

/**
 * Integration tests for connectivity with cache.
 */
public class ConnectivityWithCacheTests extends ConnectivityTcpTestCase {
    @Override
    protected void setUp() throws Exception {
        MultiConnectionsTest.init(getContext());

        mSelector = new ConnSelector();
        if (!mSelector.start()) {
            System.err.println("ERROR: can't start selector");
        }
    }

    @Override
    protected void tearDown() throws Exception {
        mSelector.stop();
    }

    /**
     * <p>Verify, that OC sends RST both for server and client side in case client sends FIN after all receiving data.
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Send 4 simple requests to the one URL
     * 2. Send FIN after received all response data
     * 3. Observe client log
     * <p/>
     * <p>Expected reults:
     * 1.OC should setup client connection after receive request data, then setup client connection and send request to network side.
     * 2.The third response should be saved into cache due to rapid manual poll start.
     * 3.OC should receive response from network side and forwards this response to client. OC sends FIN to network after receives FIN from client.
     * 4.OC should send RST to network side and client after receives FIN/ACK from network side.
     * </p>
     *
     * @throws Exception
     * @see SendFinInRapidPollTest
     */
    // TODO Verify logic in testlab and in code
    @MediumTest
    public void test_014_TC_SendFinInRapidPollTest() throws Exception {
        executeSendFinInRapidPollTest(SendFinInRapidPollTest.class, 60000,
                new Class<?>[]{ConnSelector.class, String.class, long.class},
                mSelector, VALID_RESPONSE, MIN_RMP_PERIOD);
    }

    /**
     * <p>Verify, that OC sends RST both for server and client side in case client sends RST after all receiving data.
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Send 4 simple requests to the one URL
     * 2. Send RST after received all response data
     * 3. Observe client log
     * <p/>
     * <p>Expected reults:
     * 1.OC should setup client connection after receive request data, then setup client connection and send request to network side.
     * 2.The third response should be saved into cache due to rapid manual poll start.
     * 3.OC should receive response from network side and forwards this response to client. OC sends FIN to network after receives FIN from client.
     * 4.OC should send RST to network after receives RST from client.
     * </p>
     *
     * @throws Exception
     * @see SendRstInRapidPollTest
     */
    // TODO Verify logic in testlab and in code
    //@Execute
    @MediumTest
    public void test_017_TC_SendRstInRapidPollTest() throws Exception {
        executeSendFinInRapidPollTest(SendRstInRapidPollTest.class, 60000,
                new Class<?>[]{ConnSelector.class, String.class, long.class},
                mSelector, VALID_RESPONSE, MIN_RMP_PERIOD);
    }

    /**
     * <p>Verify case, when client keep inactivity timeout after long poll start
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Start long poll
     * 2. Wait for 6 minutes
     * 3. Observe client log
     * <p/>
     * <p>Expected reults:
     * 1. The content should be served from cache due to long poll start. No activity leads to inactivity timer timeout.
     * 2. OC should send RST to network side due to long poll start.
     * 3. OC should send RST to client after inactivity timeout
     * </p>
     *
     * @throws Exception
     * @see com.seven.asimov.it.lib.connectivity.InactivityTOInLongPollTest
     */
    @LargeTest
    public void test_032_TC_InactivityTOInLongPollTest() throws Exception {
        executeSendFinInRapidPollTest(InactivityTOInLongPollTest.class, 4 * 80 * 1000 + 40 * 1000,
                new Class<?>[]{ConnSelector.class, String.class},
                mSelector, VALID_RESPONSE);
    }

    /**
     * <p>Verify case, when client keep inactivity timeout after rapid manual poll start
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Start rapid manual poll
     * 2. Wait for 6 minutes
     * 3. Observe client log
     * <p/>
     * <p>Expected reults:
     * 1. The content is served from cache due to rapid manual poll start.
     * 2. No activity leads to inactivity timer timeout
     * 3. OC should send RST to network side and client after inactivity timedout
     * </p>
     *
     * @throws Exception
     * @see InactivityTOInRapidPollTest
     */
    @MediumTest
    public void test_033_TC_InactivityTOInRapidPollTest() throws Exception {
        executeSendFinInRapidPollTest(InactivityTOInRapidPollTest.class, 8 * 60000,
                new Class<?>[]{ConnSelector.class, String.class, long.class},
                mSelector, VALID_RESPONSE, MIN_RMP_PERIOD);
    }

    /**
     * <p>Verify case, when server close connection after long poll start
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Start long poll
     * 2. Send the same request
     * 3. Observe client log
     * <p/>
     * <p>Expected reults:
     * 1. The response with "CONNECTION: close" header should be saved in cache.
     * 2. OC should should send RST to client, and client connection should be closed.
     * OC should submit START_POLL task and get long poll start response.
     * 3. OC should send the response and RST to client.
     * </p>
     *
     * @throws Exception
     * @see com.seven.asimov.it.lib.connectivity.CloseInResponseLongPollTest
     */
    @SmallTest
    public void test_035_TC_CloseInResponseLongPollTest() throws Exception {
        executeSendFinInRapidPollTest(CloseInResponseLongPollTest.class, 4 * (80 + 120) * 1000 + 40 * 1000,
                new Class<?>[]{ConnSelector.class, String.class},
                mSelector, VALID_RESPONSE);
    }

    /**
     * <p>Verify case, when server close connection after rapid manual poll start
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Start rapid manual poll
     * 2. Send the same request
     * 3. Observe client log
     * <p/>
     * <p>Expected reults:
     * 1. The response with "CONNECTION: close" header should be saved in cache.
     * 2. OC should send RST to client, and client connection should be closed.
     * 3. OC should submit START_POLL task and get long poll start response.
     * 4. OC should send the response and RST to client.
     * </p>
     *
     * @throws Exception
     * @see com.seven.asimov.it.lib.connectivity.CloseInResponseRapidPollTest
     */
    @MediumTest
    public void test_036_TC_CloseInResponseRapidPollTest() throws Exception {
        executeSendFinInRapidPollTest(CloseInResponseRapidPollTest.class, 9 * 60000,
                new Class<?>[]{ConnSelector.class, String.class, long.class},
                mSelector, VALID_RESPONSE, MIN_RMP_PERIOD);
    }

    /**
     * <p>Verify, that the connection to network is closed by OC for the next request after long poll start
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Send one simple request
     * 2. Start long poll
     * 3. Send the same request
     * 4. Observe client log
     * <p/>
     * <p>Expected reults:
     * 1. Before start long poll, every time OC receives response and RST from host at first, then sends response and RST to client.
     * 2. Response should be saved into cache.
     * 3. OC should submit START_POLL task and get long poll start response.
     * 4. OC should send the response from cache and RST to client.
     * </p>
     *
     * @throws Exception
     * @see com.seven.asimov.it.lib.connectivity.CloseInRequestLongPollTest
     */
    @LargeTest
    public void test_038_TC_CloseInRequestLongPollTest() throws Exception {
        executeSendFinInRapidPollTest(CloseInRequestLongPollTest.class, 4 * 80 * 1000 + 40 * 1000,
                new Class<?>[]{ConnSelector.class, String.class},
                mSelector, VALID_RESPONSE);
    }

    /**
     * <p>Verify, that the connection to network is closed by OC for the next request after rapid manual poll start
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Start rapid manual poll
     * 2. Send the same request
     * 3. Observe client log
     * <p/>
     * <p>Expected reults:
     * 1. Before start rapid manual poll, every time OC receives response and RST from host at first, then sends response and RST to Client. The response should be saved into cache.
     * 2. OC should submit START_POLL task and get long poll  start response.
     * 3. OC should send the response from cache and RST to client.
     * </p>
     *
     * @throws Exception
     * @see com.seven.asimov.it.lib.connectivity.CloseInRequestRapidPollTest
     */
    @MediumTest
    public void test_039_TC_CloseInRequestRapidPollTest() throws Exception {
        executeSendFinInRapidPollTest(CloseInRequestRapidPollTest.class, 60000,
                new Class<?>[]{ConnSelector.class, String.class, long.class},
                mSelector, VALID_RESPONSE, MIN_RMP_PERIOD);
    }

    /**
     * <p>Verify the case, when OC keeps connection with server after long poll start
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Send same request 3 times per 5 minutes then the content is served from cache due to long poll start.
     * 2. Observe client log
     * <p/>
     * <p>Expected reults:
     * 1. OC should setup client connection after receive request data, then setup client connection and send request to network side.
     * 2. OC should send RST to server after long poll start and keeps server connection
     * </p>
     *
     * @throws Exception
     * @see LongPollInSameConnTest
     */
    @LargeTest
    public void test_057_TC_LongPollInSameConnTest() throws Exception {
        executeSendFinInRapidPollTest(LongPollInSameConnTest.class, 4 * 80 * 1000 + 40 * 1000,
                new Class<?>[]{ConnSelector.class, String.class},
                mSelector, VALID_RESPONSE);
    }

    /**
     * <p>Verify the case, when OC keeps connection with server after rapid manual poll start
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Send same request 4 times per 1 minutes then the content is served from cache due to long poll start.
     * 2. Observe client log
     * <p/>
     * <p>Expected reults:
     * 1. OC should setup client connection after receive request data, then setup client connection and send request to network side.
     * 2. OC should send RST to server after rapid manual poll start and keeps server connection
     * </p>
     *
     * @throws Exception
     * @see RapidPollInSameConnTest
     */
    @MediumTest
    public void test_059_TC_RapidPollInSameConnTest() throws Exception {
        executeSendFinInRapidPollTest(RapidPollInSameConnTest.class, 60000,
                new Class<?>[]{ConnSelector.class, String.class},
                mSelector, VALID_RESPONSE);
    }

    /**
     * <p>Verify, that OC doesn't setup client connection due to the content is served from cache
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Setup client connection
     * 2. Observe client log
     * <p/>
     * <p>Expected reults:
     * OC should not setup client connection due to the content is served from cache.
     * </p>
     *
     * @throws Exception
     * @see NoClientConnSetupWhenFromCacheTest
     */
    // TODO Verify logic in testlab and in code
    //@Execute
    @MediumTest
    public void test_061_TC_NoClientConnSetupWhenFromCacheTest() throws Exception {
        executeSendFinInRapidPollTest(NoClientConnSetupWhenFromCacheTest.class, 60000,
                new Class<?>[]{ConnSelector.class, String.class, long.class},
                mSelector, VALID_RESPONSE, MIN_RMP_PERIOD);

    }


    /**
     * <p>Verify, that OC should send responses for two requests in sequence in case of first response is received from cache
     * </p>
     * <p>Pre-requisites:
     * 1. OC client was installed
     2. The response for request1 is served from cache
     * </p>
     * <p>Steps:
     * 1. Send request1 and request2 one by one
     2. Observe client log

     *  <p>Expected reults:
     *OC should send responses for request1 and request2 in sequence
     * </p>
     */
    /**
     * @throws Exception
     * @see ...
     * <p/>
     * TCP Dump outdated logic
     */
    @MediumTest
    public void test_067_TC_CheckRespWithCacheNoCacheRequests() throws Exception {
        checkResposeSequence(true);
    }

    /**
     * <p>Verify, that OC should send responses for two requests in sequence in case of second response is received from cache
     * </p>
     * <p>Pre-requisites:
     * 1. OC client was installed
     * 2. The response for request2 is served from cache
     * </p>
     * <p>Steps:
     * 1. Send request1 and request2 one by one
     * 2. Observe client log
     * <p/>
     * <p>Expected reults:
     * OC should send responses for request1 and request2 in sequence
     */
    @MediumTest
    public void test_068_TC_CheckRespWithNoCacheCacheRequests() throws Exception {
        checkResposeSequence(false);
    }


}
