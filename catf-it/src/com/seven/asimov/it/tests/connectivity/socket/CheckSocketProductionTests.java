package com.seven.asimov.it.tests.connectivity.socket;

import android.test.suitebuilder.annotation.LargeTest;
import com.seven.asimov.it.testcases.ConnectivitySocketTestCase;

public class CheckSocketProductionTests extends ConnectivitySocketTestCase {

    /**
     * /**
     * <p>Verify that OC close socket as inactive if client doesn't send any requests for the 6 minutes.
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Send simple request. Socket parameter “keep alive”- true.
     * 2. Get response from server side. Socket is opened.
     * 3. Wait for 12 minutes.
     * 4. Send another request through OC by this socket
     * </p>
     * <p>Expected reults:
     * 1. Socket is  closed  for the 6 minutes.
     * 2. OC detects that socket is closed.
     * </p>
     *
     * @throws Throwable
     */
    @LargeTest
    public void test_001_CheckSocketProductionGA() throws Throwable {

        final String firstRequestUri = "sockets_asimov_it_cv_0001";
        final String radioUpUri = "http://lurkmore.so/images/4/4d/Klassiki5.png";
        final int socketCloseTimeout = 12 * 60 * 1000;

        executeCheckSocketProductionGA(firstRequestUri, radioUpUri, socketCloseTimeout);
    }

    /**
     * /**
     * <p>Verify that the time of waiting response has higher priority that lifetime of the socket.
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * 2. Max time for waiting
     * </p>
     * <p>Steps:
     * 1. Send simple request and establish the lifetime of the socket 5 m.
     * Socket is opened.
     * 2. Send 1 request with parameter for response: wait for the 13 m.
     * <p/>
     * <p>Expected reults:
     * 1. Socket should be opened for 13 minutes.
     * </p>
     *
     * @throws Throwable
     */
    @LargeTest
    public void test_002_CheckSocketNotClosedProduction() throws Throwable {

        final String FIRST_RESOURCE_URI = "sockets_asimov_it_cv_07123";
        final String radioUpUri = "http://lurkmore.so/images/1/14/Pedocensored.jpg";
        final int responseSleep = (12 + 1) * 60 * 1000;
        final int radioUpRequestDelay = 12 * 60 * 1000;
        final long executeThreadsDelay = 15 * 60 * 1000L;

        executeCheckSocketNotClosedProduction(FIRST_RESOURCE_URI, radioUpUri, responseSleep, radioUpRequestDelay, executeThreadsDelay);
    }

}
