package com.seven.asimov.it.testcases;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.InSocketTask;
import com.seven.asimov.it.utils.logcat.tasks.cachingTasks.AbortedTask;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class PreventInfiniteParkingTestCase extends OverlappingPollingTestCase {
    protected List<Thread> inSocket = new ArrayList<Thread>();
    protected List<Thread> aborted = new ArrayList<Thread>();
    protected List<Thread> usual = new ArrayList<Thread>();
    protected String policyName = "max_parked_trxs_in_err_count";
    protected String policyPath = "@asimov@http";
    HttpResponse response;
    public final static int WAIT_FOR_POLICY_UPDATE = 25 * 1000;

    protected String path = "@asimov@interception";

    /**
     * @param value          - policy value - count of parked transactions with in sockets after which parent transaction finish with ERR
     * @param uri
     * @param inSockCount    - count of transactions with IN Socket after sending requests
     * @param abortCount     - count of requests which should be aborted by OC
     * @param inSockStart    - time point to start send requests with IN sockets
     * @param abortStart     - time point to start send requests with ABRT
     * @param expectedInSock - count of expected In Sockets that should be observed  in log
     * @param expectedAbort  - count of expected ABRT that should be observed  in log
     * @param negativeCase   - should start feature or not
     * @throws Exception
     */
    protected void testForPreventInfinitiveParking(String value, String uri, int inSockCount, int abortCount,
                                                   int inSockStart, int abortStart,
                                                   int expectedInSock, int expectedAbort, boolean negativeCase) throws Exception {

        AbortedTask abortedTask = new AbortedTask();
        InSocketTask inSocketTask = new InSocketTask();

        LogcatUtil logcatUtil = new LogcatUtil(getContext(), abortedTask, inSocketTask);
        HttpRequest httpRequest = createRequest().setUri(uri)
                .addHeaderField("X-OC-Sleep", "61")
                .addHeaderField("X-OC-Stateless-Sleep", "true").getRequest();

        prepareThreads(inSockCount, abortCount, httpRequest, inSockStart, abortStart, negativeCase);


        int DELAY = 70 * 1000;
        int counter = 0;
        try {
            PMSUtil.addPolicies(new Policy[]{new Policy(policyName, value, policyPath, true)});
            logSleeping(WAIT_FOR_POLICY_UPDATE);

            startThreads(negativeCase);
            logcatUtil.start();

            response = checkMiss(httpRequest, ++counter);
            logSleeping(DELAY - response.getDuration());

            response = checkMiss(httpRequest, ++counter);
            logSleeping(DELAY - response.getDuration());
            try {
                response = (httpRequest.getUri().startsWith("https") ? sendHttpsRequest(httpRequest) : sendRequest2(httpRequest));
            } catch (SocketTimeoutException e) {
                //in case of sending HTTPS request test can failed because we doesn't receive response after ERR. But OC logic is correct, so, STE can be caught but not reported
            }
            logSleeping(DELAY - response.getDuration());

            assertEquals("IN socket error should be observed for " + expectedInSock + " parked transactions", expectedInSock, inSocketTask.getLogEntries().size());
            assertEquals("verdict ABRT should be send for " + abortCount + " parked transactions after parent ERR", expectedAbort, abortedTask.getLogEntries().size());

        } finally {
            PMSUtil.cleanPaths(new String[]{path});
            PrepareResourceUtil.invalidateResourceSafely(uri);
            interruptThreads(negativeCase);
            clearLists();
            if (logcatUtil.isRunning()) {
                logcatUtil.stop();
            }
        }
    }

    /**
     * @param inSockCount
     * @param abortCount
     * @param httpRequest
     * @param inSockStart
     * @param abortStart
     * @param negativeCase
     */
    protected void prepareThreads(int inSockCount, int abortCount, HttpRequest httpRequest, int inSockStart, int abortStart, boolean negativeCase) {
        if (negativeCase) {
            for (int i = 0; i < inSockCount; i++) {
                usual.add(new Thread(new Parking(httpRequest, inSockStart + i * 2000, TIMEOUT, ParkedAction.HIT)));
            }
        } else {
            for (int i = 0; i < inSockCount; i++) {
                inSocket.add(new Thread(new Parking(httpRequest, inSockStart + i * 2000, 5 * 1000, ParkedAction.IN_SOC)));
            }
            for (int i = 0; i < abortCount; i++) {
                aborted.add(new Thread(new Parking(httpRequest, abortStart + i * 1000, TIMEOUT, ParkedAction.ABRT)));
            }
        }
    }

    protected void startThreads(boolean negativeCase) {
        if (negativeCase) {
            startThread(usual, true);
        } else {
            startThread(inSocket, true);
            startThread(aborted, true);
        }
    }

    protected void startThread(List<Thread> threads, boolean start) {
        if (threads != null) {
            for (Thread p : threads) {
                if (start) {
                    p.start();
                } else {
                    p.interrupt();
                }
            }
        }
    }

    protected void interruptThreads(boolean negativeCase) {
        if (negativeCase) {
            startThread(usual, false);
        } else {
            startThread(inSocket, false);
            startThread(aborted, false);
        }
    }

    protected void clearLists() {
        inSocket.clear();
        aborted.clear();
        usual.clear();
    }
}
