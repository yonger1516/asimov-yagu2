package com.seven.asimov.it.tests.connectivity.tcp;

import android.content.Context;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import com.seven.asimov.it.annotation.MultiConnectivity;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.lib.connectivity.*;
import com.seven.asimov.it.testcases.ConnectivityTcpTestCase;
import com.seven.asimov.it.utils.OCUtil;
import com.seven.asimov.it.utils.ShellUtil;
import com.seven.asimov.it.utils.conn.ConnSelector;
import com.seven.asimov.it.utils.conn.ConnUtils;
import com.seven.asimov.it.utils.conn.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Integration test cases for the connectivity.
 */
public class ConnectivityTests extends ConnectivityTcpTestCase {
    private static final Logger logger = LoggerFactory.getLogger(ConnectivityTests.class.getSimpleName());

    @Override
    protected void setUp() throws Exception {
        MultiConnectionsTest.init(getContext());

        mSelector = new ConnSelector();
        if (!mSelector.start()) {
            System.err.println("ERROR: can't start selector");
            return;
        }

        InetAddress address = ConnUtils.getLocalIpAddress();
        mTestServer = new HttpServer(mSelector);
        try {
            logger.info("Start test server, address:" + address + ", port:" + TEST_SERVER_PORT);
            mTestServer.listen(address, TEST_SERVER_PORT);
        } catch (IOException e) {
            logger.debug(": failed to start tcp server on port:" + TEST_SERVER_PORT, e);
        }

        connSelectorAndHttpServerClasses = new Class<?>[]{ConnSelector.class, HttpServer.class};
        connSelectorAndHttpServerObjects = new Object[]{mSelector, mTestServer};
    }

    @Override
    protected void tearDown() throws Exception {
        if (mTestServer != null) {
            mTestServer.stop();
        }

        mSelector.stop();
    }

    /*
     * *******************************************************************************************************
     * Connectivity Integration test cases defined in Client_Connectivity_Integration_TestCases_Template_v1 *
     * ********************************************************************************************************
     */

    /**
     * /**
     * <p>Add test server port and redirect port to IP table of OC
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. OC gets application id.
     * 2. OC adds to IP table test server port and redirection port for this application.
     * <p/>
     * <p>Expected reults:
     * 1. IP table should be updated correctly, ports should be added.
     * </p>
     *
     * @throws Exception
     */
    @SmallTest
    public void test_000_TC_SetupIPTables() throws Exception {
        // Update iptables to redirect server port through OC
        List<String> command = new ArrayList<String>();
        String uid = Integer.toString(OCUtil.getAsimovUid(getContext()));
        command.add(TFConstantsIF.IPTABLES_PATH + " -t nat -A Z7BASECHAIN -p 6 --dport " + TEST_SERVER_PORT
                + " -m owner \\! --uid-owner " + uid + " -j REDIRECT --to-ports " + TFConstantsIF.DEFUALT_HTTP_SERVER_PORT);
        ShellUtil.execWithCompleteResult(command, true);
    }

    /**
     * <p>Verify, that OC setup connection with client and server corretly
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Establish connection client – OC and OC – server
     * <p/>
     * <p>Expected reults:
     * 1.OC should setup server connection and client connection successfully
     * </p>
     *
     * @throws Exception
     * @see SimpleTest
     */

    @SmallTest
    public void test_001_TC_ServerClientConnectionTest() throws Exception {
        executeAbstractConnTest(SimpleTest.class, isLogResultDescription, 10000,
                connSelectorAndHttpServerClasses, connSelectorAndHttpServerObjects);
    }


    /**
     * <p>Verify, that OC setup connection with client and server corretly
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Establish 200 connections with OC
     * 2. Observe client log
     * <p/>
     * <p>Expected reults:
     * 1. All connections should be passed throw OC
     * </p>
     *
     * @throws Exception
     * @see MultiConnWithoutRequestTest
     */

    @MultiConnectivity
    public void test_003_TC_MultiConnWithoutRequestTest() throws Exception {
        executeAbstractConnTest(MultiConnWithoutRequestTest.class, isLogResultDescription, 180000,
                connSelectorAndHttpServerClasses, connSelectorAndHttpServerObjects);
    }

    /**
     * <p>Verify, that OC send RST for FIN from the client
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Connect to OC
     * 2. Send FIN (withot any request)
     * <p/>
     * <p>Expected reults:
     * 1. OC should send RST after receiving FIN
     * 2. OC shouldn't try to do any action after RST is sent
     * </p>
     *
     * @throws Exception
     * @see SendRstWithoutRequestTest
     */
    // TODO Change usecase in testlab
    //@Execute
    @SmallTest
    public void test_004_TC_SendFinWithoutRequestTest() throws Exception {
        executeAbstractConnTest(SendFinWithoutRequestTest.class, isLogResultDescription, 10000,
                connSelectorAndHttpServerClasses, connSelectorAndHttpServerObjects);
    }


    /**
     * <p>Verify, that OC send RST for RST from the client
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Connect to OC
     * 2. Send RST (withot any request)
     * <p/>
     * <p>Expected reults:
     * 1. OC should send RST after receiving RST
     * 2. OC shouldn't try to do any action after RST is sent
     * </p>
     *
     * @throws Exception
     * @see SendFinWithoutRequestTest
     */

    @SmallTest
    public void test_005_TC_SendRstWithoutRequestTest() throws Exception {
        executeAbstractConnTest(SendRstWithoutRequestTest.class, isLogResultDescription, 10000,
                connSelectorAndHttpServerClasses, connSelectorAndHttpServerObjects);
    }

    /**
     * <p>Verify the case, when OC setup or not client connection depends on how fast it processes the request
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Send simple request
     * 2. Send RST
     * <p/>
     * <p>Expected reults:
     * 1. If OC didn't process the request while RST received  - OC should receive RST and no more actions
     * 2. If OC processed the request while RST received - OC should forward the request and closes both side connections by reset.
     * </p>
     *
     * @throws Exception
     * @see SendRstWithRequestTest
     */
    @SmallTest
    public void test_007_TC_SendRstWithRequestTest() throws Exception {
        executeAbstractConnTest(SendRstWithRequestTest.class, isLogResultDescription, 30000,
                connSelectorAndHttpServerClasses, connSelectorAndHttpServerObjects);
    }

    /**
     * <p>Verify, that OC correctly provides simple client-server connection
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Send simple request
     * 2. Send RST to OC
     * 3. Observe client log
     * <p/>
     * <p>Expected reults:
     * 1. OC should setup client connection after receive request data, then setup client connection and send request to network side.
     * 2. OC should send RST to network after receives RST
     * </p>
     *
     * @throws Exception
     * @see SendRstWhileOCConnectingTest
     */
    @SmallTest
    public void test_009_TC_SendRstWhileOCConnectingTest() throws Exception {
        executeAbstractConnTest(SendRstWhileOCConnectingTest.class, isLogResultDescription, 10000,
                connSelectorAndHttpServerClasses, connSelectorAndHttpServerObjects);
    }

    /**
     * <p>Verify the case, when client send FIN while OC receiving response
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Send simple request to OC
     * 2. Send FIN while OC receiving response
     * 3. Observe client log
     * <p/>
     * <p>Expected reults:
     * 1. OC should setup client connection after receive request data and send request to network side.
     * 2. OC should receive the response from server while FIN is received from client
     * 3. OC should forward this FIN to server
     * 4. OC should send RST to both side (server and client) after FIN/ACK received from server.
     * </p>
     *
     * @throws Exception
     * @see SendRstWhileOCReceivingResponseTest
     */
    @SmallTest
    public void test_011_TC_SendRstWhileOCReceivingResponseTest() throws Exception {
        executeAbstractConnTest(SendRstWhileOCReceivingResponseTest.class, isLogResultDescription, 10000,
                connSelectorAndHttpServerClasses, connSelectorAndHttpServerObjects);
    }

    /**
     * <p>Verify, that OC correctly provides simple client-server connection in case of client send FIN after receiving response
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Send simple request
     * 2. Receive response from server
     * 3. Send FIN
     * 4. Observe client log
     * <p/>
     * <p>Expected reults:
     * 1. OC should setup client connection after receive request data and send request to network side.
     * 2. OC should receive response from network side and forwards this response to client.
     * 3. OC should receive FIN from client and forwards it to network side.
     * 4. OC should send RST to network side and client after receives FIN/ACK from network side
     * </p>
     *
     * @throws Exception
     * @see SendFinAfterReceivedResponseTest
     */
    @SmallTest
    public void test_012_TC_SendFinAfterReceivedResponseTest() throws Exception {
        executeAbstractConnTest(SendFinAfterReceivedResponseTest.class, isLogResultDescription, 10000,
                connSelectorAndHttpServerClasses, connSelectorAndHttpServerObjects);
    }

    /**
     * <p>Verify, that OC correctly provides simple client-server connection in case of Client send RST after receiving response
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. 1. Send simple request
     * 2. Receive response from server
     * 3. Send RST
     * 4. Observe client log
     * <p/>
     * <p>Expected reults:
     * 1. OC should setup client connection after receive request data and send request to network side.
     * 2. OC should receive response from network side and forwards this response to client.
     * 3. OC should receive RST from client and reset network side.
     * </p>
     *
     * @throws Exception
     */
    @SmallTest
    public void test_015_TC_SendRstAfterReceivedResponseTest() throws Exception {
        executeAbstractConnTest(SendRstAfterReceivedResponseTest.class, isLogResultDescription, 10000,
                connSelectorAndHttpServerClasses, connSelectorAndHttpServerObjects);
    }


    /**
     * <p>Verify the case, when server send FIN after received request
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Send simple request
     * 2. Send FIN from server after received request data.
     * 3. Observe client log
     * <p/>
     * <p>Expected reults:
     * 1. OC should setup client connection after receive request data and send request to server.
     * 2. OC should receive FIN from server without response data.
     * 3. OC sends RST to both side (client and server).
     * </p>
     *
     * @throws Exception
     * @see com.seven.asimov.it.lib.connectivity.HostSendFinWithoutResponseTest
     */
    @SmallTest
    public void test_018_TC_HostSendFinWithoutResponseTest() throws Exception {
        executeAbstractConnTest(HostSendFinWithoutResponseTest.class, isLogResultDescription, 10000,
                connSelectorAndHttpServerClasses, connSelectorAndHttpServerObjects);
    }

    /**
     * <p>Verify the case, when server send RST after received request
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Send simple request
     * 2. Send RST from server after received request data.
     * 3. Observe client log
     * <p/>
     * <p>Expected reults:
     * 1. OC should setup client connection after receive request data and send request to server.
     * 2. OC should receive RST from server without response data.
     * 3. OC sends RST to both side (client and server).
     * </p>
     *
     * @throws Exception
     * @see com.seven.asimov.it.lib.connectivity.HostSendRstWithoutResponseTest
     */
    @SmallTest
    public void test_019_TC_HostSendRstWithoutResponseTest() throws Exception {
        executeAbstractConnTest(HostSendRstWithoutResponseTest.class, isLogResultDescription, 10000,
                connSelectorAndHttpServerClasses, connSelectorAndHttpServerObjects);
    }


    /**
     * <p>Verify the case, when server send FIN while OC processing the next request
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Send 2 simple requests
     * 2. Send FIN from server while OC processing the next request
     * 3. Observe client log
     * <p/>
     * <p>Expected reults:
     * 1.OC setup client connection after receive the 1st request data.
     * 2.OC forwards the 1st request to server side.
     * 3.OC forwards the 1st response to client side.
     * 4.OC receives the 2nd request and RST from server side.
     * 5.OC forwards the 2nd request to the Network side over new OUT socket.
     * 6.The client receives second response.
     * </p>
     *
     * @throws Exception
     * @see com.seven.asimov.it.lib.connectivity.HostSendRstWhileOCProcessingRequestTest
     */
    @SmallTest
    public void test_021_TC_HostSendRstWhileOCProcessingRequestTest() throws Exception {
        executeHostSendRstWhileOCProcessingRequestTest(HostSendRstWhileOCProcessingRequestTest.class, isLogResultDescription, 10000,
                connSelectorAndHttpServerClasses, connSelectorAndHttpServerObjects);
    }

    /**
     * <p>Verify the case, when server send RST after sending response to client
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Send simple request
     * 2. Send RST from server after sending response to client
     * 3. Observe client log
     * <p/>
     * <p>Expected reults:
     * 1. OC should setup client connection after receive request data, then setup client connection and send request to network side.
     * 2. OC should receive RST after response data and forward the response to client
     * 3. OC should send RST to client.
     * </p>
     *
     * @throws Exception
     * @see HostSendRstWithResponseTest
     */
    @SmallTest
    public void test_025_TC_HostSendRstWithResponseTest() throws Exception {
        executeAbstractConnTest(HostSendRstWithResponseTest.class, isLogResultDescription, 10000,
                connSelectorAndHttpServerClasses, connSelectorAndHttpServerObjects);
    }

    /**
     * <p>Verify, that OC send RST in case client send FIN when OC sending response
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Send simple request
     * 2. Send FIN when OC sending response
     * 3. Observe client log
     * <p/>
     * <p>Expected reults:
     * 1. OC should setup client connection after receive request data and send request to network side.
     * 2. OC should receive the response from server and should forward it to client while FIN is received from client.]
     * 3. OC should forward this FIN to server
     * 4. OC should send RST to both side after FIN/ACK received from server
     * </p>
     *
     * @throws Exception
     * @see SendFinWhileOCSendingResponseTest
     */
    @SmallTest
    public void test_028_TC_SendFinWhenOCSendingResponseTest() throws Exception {
        executeAbstractConnTest(SendFinWhileOCSendingResponseTest.class, isLogResultDescription, 10000,
                connSelectorAndHttpServerClasses, connSelectorAndHttpServerObjects);
    }

    /**
     * <p>Verify case, when OC close connection after inactivity timeout
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Send simple request
     * 2. Get response from server
     * 3. Wait for 6 minutes
     * <p/>
     * <p>Expected reults:
     * 1.OC should send RST to network side and client after inactivity timeout
     * </p>
     *
     * @throws Exception
     * @see InactivityTimerTimeoutTest
     */
    @LargeTest
    public void test_031_TC_InactivityTimerTimeoutTest() throws Exception {
        executeAbstractConnTest(InactivityTimerTimeoutTest.class, isLogResultDescription, 750000,
                connSelectorAndHttpServerClasses, connSelectorAndHttpServerObjects);
    }

    /**
     * <p>Verify case, when OC close connection with client after server close connection with OC
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Send simple request
     * 2. Send response with "CONNECTION: close" in the response header.
     * 3. Close connection by server after response is sent
     * 4. Observe client log
     * <p/>
     * <p>Expected reults:
     * 1. OC should setup client connection after receive the request data and then send the request to server.
     * 2. OC should forward the response to client.
     * 3. OC should send FIN to client because the response header didn't contain the content length.
     * </p>
     *
     * @throws Exception
     * @see com.seven.asimov.it.lib.connectivity.CloseInResponseWithoutContentLenghtTest
     */

    @SmallTest
    public void test_037_TCb_CloseInResponseHeaderWithoutContentLenghtTest() throws Exception {
        executeAbstractConnTest(CloseInResponseWithoutContentLenghtTest.class, isLogResultDescription, 10000,
                connSelectorAndHttpServerClasses, connSelectorAndHttpServerObjects);
    }

    /**
     * <p>Verify, that Rapid Poll failed in case of there are no access from Z7 server to destination server
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Send the same requests every 30 seconds. 5 times totally.
     * 2. Observe client log
     * <p/>
     * <p>Expected reults:
     * 1. OC should setup connection after first request received.
     * 2. OC should forwards the first 3 requests to Server but not the 4th and 5th request.
     * 3. OC should start poll but failed because the server can't be accessed by Z7 server
     * 4. OC should keep client – OC  and OC - server connection when start poll failed.
     * 5. OC should serve the 4th and 5th request from cache.
     * 6. Client should receive total 5 responses, all are same.
     * </p>
     *
     * @throws Exception
     * @see RapidPollStartPollFailedTest
     */
    @LargeTest
    public void test_060_TC_RapidPollStartPollFailedTest() throws Exception {
        executeAbstractConnTest(RapidPollStartPollFailedTest.class, isLogResultDescription, 300000,
                new Class<?>[]{ConnSelector.class, HttpServer.class, Context.class},
                new Object[]{mSelector, mTestServer, getContext()});
    }

    /**
     * <p>Verify, that OC reset client – OC and OC – server connection after receiving all data between client and server
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Send 2 simple requests
     * 2. Send FIN from Server while OC is processing the next request.
     * 3. Observe client log
     * <p/>
     * <p>Expected reults:
     * 1. OC should setup connection after receive the 1st request data.
     * 2. OC should forward the 1st request to server.
     * 3. OC should forward the 1st response to client.
     * 4. OC should receive the next 2 requests and forwards them to server.
     * 5. OC should receive the next 2 responses and forwards them to client.
     * 6. Client should send FIN to OC and OC forwards this FIN to server.
     * 7. OC should receive FIN from server and reset OC - server connection.
     * 8. OC should reset client – OC connection
     * </p>
     *
     * @throws Exception
     * @see com.seven.asimov.it.lib.connectivity.ConnectionWithMultipleRequestsTest
     */

    @MediumTest
    public void test_066_TC_ConnectioWithMultipleRequestsTest() throws Exception {
        executeAbstractConnTest(ConnectionWithMultipleRequestsTest.class, isLogResultDescription, 30000,
                connSelectorAndHttpServerClasses, connSelectorAndHttpServerObjects);
    }
}
