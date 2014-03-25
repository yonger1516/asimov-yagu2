package com.seven.asimov.it.testcases;

import com.seven.asimov.it.asserts.CATFAssert;
import com.seven.asimov.it.base.*;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectivitySocketTestCase extends TcpDumpTestCase {
    private static final Logger logger = LoggerFactory.getLogger(ConnectivitySocketTestCase.class.getSimpleName());

    protected static final int SLEEP_TIME = 10 * 1000;
    protected static final int WAIT_FOR_INACTIVITY_TRIGGER = 180 * 1000;

    public void executeCheckSocketProductionGA(String firstRequestUri, String radioUpUri, final int socketCloseTimeout) throws Throwable {
        final String uri = createTestResourceUri(firstRequestUri);
        final HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .getRequest();

        PrepareResourceUtil.prepareResource(uri, false);

        HttpResponse response = checkMiss(request, 1, VALID_RESPONSE, true, 5 * 60 * 1000); // TODO CheckMissTimeout to global constants
        NetStat netStat = new NetStat();
        assertTrue("Socket opened", netStat.socketWithDistanationExists(response.getSocketInfo().getForeignAdress(),
                response.getSocketInfo().getForeignPort()));
        logSleeping(socketCloseTimeout);

        request.setUri(radioUpUri);
        sendRequest(request);
        netStat = new NetStat();
        assertFalse("Socket closed", netStat.socketWithDistanationExists(response.getSocketInfo().getForeignAdress(),
                response.getSocketInfo().getForeignPort()));
    }

    protected void executeCheckSocketNotClosedProduction(String firstResourceUri, String radioUpUri, final int responseSleep, int radioUpRequestDelay, long executeThreadsDelay) throws Throwable {
        String uri = createTestResourceUri(firstResourceUri);
        PrepareResourceUtil.prepareResource(uri, false);

        final HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Sleep", "720")
                .getRequest();
        final HttpRequest radioUpRequest = createRequest().setUri(radioUpUri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();

        TestCaseThread checkSocketThread = new TestCaseThread() {
            @Override
            public void run() throws Throwable {
                HttpResponse response = sendRequest2(request, true, false, responseSleep);
                NetStat ns = new NetStat();
                CATFAssert.assertStatusCode(1, HttpStatus.SC_OK, response);
                assertTrue("Socket still opened", ns.socketWithDistanationExists(
                        response.getSocketInfo().getForeignAdress(), response.getSocketInfo().getForeignPort()));
            }
        };

        TestCaseThread radioUpThread = new TestCaseThread(radioUpRequestDelay) {
            @Override
            public void run() throws Throwable {
                HttpResponse response = sendRequest2(radioUpRequest);
                CATFAssert.assertStatusCode(2, HttpStatus.SC_OK, response);
            }
        };

        executeThreads(executeThreadsDelay, checkSocketThread, radioUpThread);
    }

    private void assertZ7TPtrafficIn(int responseId, boolean present) {
        String message;
        if (present) {
            message = "Response R" + getShortThreadName() + "." + responseId + " - Z7TP incoming traffic is present";
            assertTrue(message, getZ7TpSessionCount() > 0);
        } else {
            message = "Response R" + getShortThreadName() + "." + responseId
                    + " - Z7TP incoming traffic is not present";
            assertTrue(message, getZ7TpSessionCount() == 0);
        }
    }

    public void executeTestZ7TPInactivityTrigger(String resourceUri, int sleepTime, int waitForInactivityTrigger) throws Throwable {
        String uri = createTestResourceUri(resourceUri);
        PrepareResourceUtil.prepareResource(uri, false);
        HttpRequest request = createRequest().setUri(uri).addHeaderField("X-OC-Encoding", "identity").getRequest();

        try {
            for (int i = 1; i <= 3; i++) {
                HttpResponse response = checkMiss(request, i);
                logSleeping(sleepTime - response.getDuration());
            }
            logSleeping(waitForInactivityTrigger);

            PrepareResourceUtil.prepareResource(uri, true);
            int waitForInvalidate = 30 * 1000;
            logSleeping(waitForInvalidate);

            assertZ7TPtrafficIn(3, false);
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    public void executeTestZ7TPFloatClosure(String resourceUri1, String resourceUri2, int waitForInactivityTrigger, int sleepTime) throws Throwable {
        String uri1 = createTestResourceUri(resourceUri1);
        String uri2 = createTestResourceUri(resourceUri2);

        PrepareResourceUtil.prepareResource(uri1, false);
        PrepareResourceUtil.prepareResource(uri2, false);

        HttpRequest request1 = createRequest().setUri(uri1).addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
        HttpRequest request2 = createRequest().setUri(uri2).addHeaderField("X-OC-ContentEncoding", "identity").getRequest();

        try {
            logSleeping(waitForInactivityTrigger);
            checkZ7tpSession(request1, sleepTime);
            logSleeping(waitForInactivityTrigger);
            checkZ7tpSession(request2, sleepTime);
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri1);
            PrepareResourceUtil.invalidateResourceSafely(uri2);
        }
    }

    protected void checkZ7tpSession(HttpRequest request, int sleepTime) throws Throwable {
        HttpResponse response;
        testStartTimestamp = System.currentTimeMillis();
        for (int i = 1; i <= 4; i++) {
            if (i == 4) {
                logger.info("checkHit start" + " i=" + i);
                checkHit(request, i);
                logger.info("checkHit end" + " i=" + i);
                break;
            } else {
                logger.info("checkMiss start" + " i=" + i);
                response = checkMiss(request, i);
                logger.info("checkMiss end" + " i=" + i);
            }
            //Log.v(TAG, "sleepTime=" + sleepTime);
            //Log.v(TAG, "response.getDuration=" + response.getDuration());
            logSleeping(sleepTime - response.getDuration());
        }
        testEndTimestamp = System.currentTimeMillis();
        /*
        first z7tp session to eng0XX is created  during policy update and lives until new z7tp session is established.
        second z7tp session to testrunner is created for polling
        so it could be 1 or 2 z7tp sessions
        */
        int sessionCount = getZ7TpSessionCount();
        boolean sessionCountCorrect = (sessionCount == 1 || sessionCount == 2);
        assertTrue("There should be 1 or 2 Z7TP session", sessionCountCorrect);
    }
}
