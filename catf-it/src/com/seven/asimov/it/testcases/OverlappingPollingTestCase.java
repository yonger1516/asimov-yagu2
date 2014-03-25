package com.seven.asimov.it.testcases;

import android.test.AssertionFailedError;
import android.util.Log;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.InSocketTask;
import com.seven.asimov.it.utils.logcat.tasks.cachingTasks.AbortedTask;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class OverlappingPollingTestCase extends TcpDumpTestCase {
    private static final String TAG = OverlappingPollingTestCase.class.getSimpleName();

    protected static int counter = 0;
    protected static boolean markerABRT;
    protected static boolean markerListen;


    protected List<Thread> inSocket = new ArrayList<Thread>();
    protected List<Thread> aborted = new ArrayList<Thread>();
    protected List<Thread> usual = new ArrayList<Thread>();
    protected String policyName = "max_parked_trxs_in_err_count";
    protected String policyPath = "@asimov@http";
    HttpResponse response;

    protected enum ParkedAction {
        MISS,
        HIT,
        ABRT,
        IN_SOC
    }

    protected class Parking implements Runnable {

        final HttpRequest parkedRequest;
        final int timeout;
        final int socketTimeout;
        ParkedAction parkedAction;

        public Parking(HttpRequest httpRequest, int inTimeout, int sTimeout) {
            parkedRequest = httpRequest;
            timeout = inTimeout;
            socketTimeout = sTimeout;
            parkedAction = ParkedAction.IN_SOC;
        }

        public Parking(HttpRequest httpRequest, int inTimeout, int sTimeout, ParkedAction action) {
            parkedRequest = httpRequest;
            timeout = inTimeout;
            socketTimeout = sTimeout;
            parkedAction = action;
        }

        @Override
        public void run() {

            AbortedTask abortedTask = new AbortedTask();
            LogcatUtil logcatUtil = new LogcatUtil(getContext(), abortedTask);
            logcatUtil.start();
            try {
                Thread.sleep(timeout);
                Log.i(TAG, "Parked request ");
                if (parkedAction == ParkedAction.MISS) {
                    checkMiss(parkedRequest, counter);
                } else if (parkedAction == ParkedAction.HIT) {
                    checkHit(parkedRequest, counter);
                } else {
                    sendRequest2(parkedRequest, false, false, socketTimeout, DEFAULT_READ_BUFFER, SLEEP_AFTER_HANDSHAKE, VERSION);
                    if (parkedAction == ParkedAction.ABRT) {
                        markerListen = true;
                        markerABRT = (abortedTask.getLogEntries().size() != 0);
                    }
                }
            } catch (InterruptedException e) {
                ExceptionUtils.getStackTrace(e);
            } catch (URISyntaxException e) {
                ExceptionUtils.getStackTrace(e);
            } catch (IOException e) {
                ExceptionUtils.getStackTrace(e);
            } catch (AssertionFailedError e) {
                Log.e(TAG, "AssertionFailedError in parked thread! Check TC or overlapping polling logic");
            }
        }
    }

    /**
     * @param value          - policy value - count of parked transactions with in sockets after which parrent transaction finish with ERR
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
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(policyName, value, policyPath, true)});
            Log.i(TAG, "метка");
            startThreads(negativeCase);
            logcatUtil.start();

            response = checkMiss(httpRequest, ++counter);
            logSleeping(DELAY - response.getDuration());

            response = checkMiss(httpRequest, ++counter);
            logSleeping(DELAY - response.getDuration());
            try {
                response = (httpRequest.getUri().startsWith("https") ? sendHttpsRequest(httpRequest) : sendRequest2(httpRequest));
            } catch (SocketTimeoutException e) {
                Log.e(TAG, "SocketTimeoutException in case of ERR HTTPS request");
                //in case of sending HTTPS request test can failed because we doesn't receive response after ERR. But OC logic is correct, so, STE can be caught but not reported
            }
            logSleeping(DELAY - response.getDuration());

            assertEquals("IN socket error should be observed for " + expectedInSock + " parked transactions", expectedInSock, inSocketTask.getLogEntries().size());
            assertEquals("verdict ABRT should be send for " + abortCount + " parked transactions after parent ERR", expectedAbort, abortedTask.getLogEntries().size());

        } finally {
            PMSUtil.cleanPaths(new String[]{policyPath});
            PrepareResourceUtil.invalidateResourceSafely(uri);
            interruptThreads(negativeCase);
            clearLists();
            if (logcatUtil.isRunning()) {
                logcatUtil.stop();
            }
        }
    }

    /**
     *
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
                aborted.add(new Thread(new Parking(httpRequest, abortStart + i * 2000, TIMEOUT, ParkedAction.ABRT)));
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