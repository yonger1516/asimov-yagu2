package com.seven.asimov.it.base;

import android.util.Log;
import com.seven.asimov.it.base.interfaces.HttpUrlConnectionIF;
import com.seven.asimov.it.utils.MobileNetworkUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.tcpdump.HttpSession;
import com.seven.asimov.it.utils.tcpdump.Interface;
import com.seven.asimov.it.utils.tcpdump.TcpDumpHelper;
import com.seven.asimov.it.utils.tcpdump.TcpDumpUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.HttpStatus;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.*;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.seven.asimov.it.asserts.CATFAssert.assertResponseBody;
import static com.seven.asimov.it.asserts.CATFAssert.assertStatusCode;
import static com.seven.asimov.it.base.constants.TFConstantsIF.*;


public class TcpDumpTestCase extends AsimovTestCase implements HttpUrlConnectionIF {
    private static final String TAG = TcpDumpTestCase.class.getSimpleName();

    private static final Logger logger = LoggerFactory.getLogger(TcpDumpTestCase.class.getSimpleName());

    protected javax.net.ssl.SSLSocketFactory sslSocketFactory;

    protected List<Check> checks = new ArrayList<Check>();

    protected TcpDumpUtil tcpDump;

    protected long testStartTimestamp = 0;
    protected long testEndTimestamp = 0;
    protected static String VALID_RESPONSE = "tere";
    protected static String INVALIDATED_RESPONSE = "eret";
    public static final long MIN_NON_RMP_PERIOD = 67000;
    public static final long RMP_EXPIRATION_TIME = 2 * 60 * 1000;
    public static final long MIN_RMP_PERIOD = 5000;

    private static final String HTTPS_URI_SCHEME = "https://";

    protected int Z7TP_SESSION_COUNT = 0;
    protected int HTTP_SESSION_COUNT = 0;
    protected int requestId = 0;

    private int[] intervals;
    private int[] delays;
    private int[] timeouts;
    private boolean[] changedBody;
    private String[] expectedResults;
    private LogcatUtil logcatUtil;
    private final long THREADS_TIMEOUT = 30000l;

    //Constants for expected results
    protected static final String MISS = "MISS";
    protected static final String HIT = "HIT";
    protected static final String ERR = "ERR";
    protected static final String ABORTED = "ABORTED";
    protected static final String IWC = "IWC";
    protected static final String IWoC = "IWoC";
    protected static final String NEW_RESPONSE_BODY = "eret";

    protected int getZ7TpSessionCount() {
        //logger.trace("testStartTimestamp=" + testStartTimestamp + "   " + new Date(testStartTimestamp).toString());
        //logger.trace("testEndTimestamp=" + testEndTimestamp + "   " + new Date(testEndTimestamp).toString());
        Z7TP_SESSION_COUNT = tcpDump.getZ7TPSession(testStartTimestamp, testEndTimestamp).size();
        logger.trace("getZ7TPSession.size=" + Z7TP_SESSION_COUNT);
        return Z7TP_SESSION_COUNT;
    }

    protected int getHttpSessionCount() {
        HTTP_SESSION_COUNT = tcpDump.getHttpSessionCount(testStartTimestamp, testEndTimestamp);
        return HTTP_SESSION_COUNT;
    }

    protected HttpResponse checkMiss304(HttpRequest request, int requestId)
            throws IOException, URISyntaxException {
        return checkMiss(request, requestId, 304, "", false, TIMEOUT);
    }

    protected HttpResponse checkMiss(HttpRequest request, int requestId, int sleepTime) throws IOException, URISyntaxException {
        long startTime = System.currentTimeMillis();
        HttpResponse response = checkMiss(request, requestId);
        TestUtil.sleep(sleepTime - System.currentTimeMillis() + startTime);
        return response;
    }

    protected HttpResponse checkMiss(HttpRequest request, int requestId) throws IOException, URISyntaxException {
        return checkMiss(request, requestId, null);
    }

    protected HttpResponse checkMiss(HttpRequest request, int requestId, String expectedBody)
            throws IOException, URISyntaxException {
        return checkMiss(request, requestId, HttpStatus.SC_OK, expectedBody);
    }

    protected HttpResponse checkMiss(HttpRequest request, int requestId, String expectedBody, int sleepTime)
            throws IOException, URISyntaxException {
        long startTime = System.currentTimeMillis();
        HttpResponse response = checkMiss(request, requestId, HttpStatus.SC_OK, expectedBody);
        TestUtil.sleep(sleepTime - System.currentTimeMillis() + startTime);
        return response;
    }

    protected HttpResponse checkMiss(HttpRequest request, int requestId, int statusCode, String expectedBody)
            throws IOException, URISyntaxException {
        return checkMiss(request, requestId, statusCode, expectedBody, false, TIMEOUT);
    }

    protected HttpResponse checkMiss(HttpRequest request, int requestId, String expectedBody, boolean keepAlive,
                                     int timeout) throws IOException, URISyntaxException {
        return checkMiss(request, requestId, HttpStatus.SC_OK, expectedBody, keepAlive, timeout);
    }

    protected HttpResponse checkMiss(HttpRequest request, int requestId, int statusCode, String expectedBody,
                                     boolean keepAlive, int timeout) throws IOException, URISyntaxException {
        boolean isSslModeOn = false;
        String uri = request.getUri();
        if (uri.startsWith(HTTPS_URI_SCHEME)) isSslModeOn = true; // we are going to send HTTPS request
        logRequest(requestId, uri);
        HttpResponse response;
        long startTime = System.currentTimeMillis();
        logger.warn("Session start:" + startTime);
        if (isSslModeOn) {
            response = sendHttpsRequest(request, this);
        } else {
            response = sendRequest2(request, keepAlive, false, timeout);
        }
        long endTime = System.currentTimeMillis();
        logger.warn("Session end:" + endTime);
        logResponse(requestId, ResponseLocation.NETWORK, response);
        assertStatusCode(requestId, statusCode, response);
        if (expectedBody != null) {
            assertResponseBody(requestId, expectedBody, response);
        }
        addMissCheck(requestId, request.getUri(), startTime, endTime);
        return response;
    }

    protected HttpResponse checkHit(HttpRequest request, int requestId, int sleepTime) throws IOException, URISyntaxException {
        long startTime = System.currentTimeMillis();
        HttpResponse response = checkHit(request, requestId);
        TestUtil.sleep(sleepTime - System.currentTimeMillis() + startTime);
        return response;
    }

    protected HttpResponse checkHit304(HttpRequest request, int requestId)
            throws IOException, URISyntaxException {
        return checkHit(request, requestId, 304, "", false, TIMEOUT);
    }

    protected HttpResponse checkHit(HttpRequest request, int requestId) throws IOException, URISyntaxException {
        return checkHit(request, requestId, null);
    }

    protected HttpResponse checkHit(HttpRequest request, int requestId, String expectedBody)
            throws IOException, URISyntaxException {
        return checkHit(request, requestId, HttpStatus.SC_OK, expectedBody);
    }

    protected HttpResponse checkHit(HttpRequest request, int requestId, String expectedBody, int sleepTime)
            throws IOException, URISyntaxException {
        long startTime = System.currentTimeMillis();
        HttpResponse response = checkHit(request, requestId, HttpStatus.SC_OK, expectedBody);
        TestUtil.sleep(sleepTime - System.currentTimeMillis() + startTime);
        return response;
    }


    protected HttpResponse checkHit(HttpRequest request, int requestId, int statusCode, String expectedBody)
            throws IOException, URISyntaxException {
        return checkHit(request, requestId, statusCode, expectedBody, false, TIMEOUT);
    }

    protected HttpResponse checkHit(HttpRequest request, int requestId, String expectedBody, boolean keepAlive,
                                    int timeout) throws IOException, URISyntaxException {
        return checkHit(request, requestId, HttpStatus.SC_OK, expectedBody, keepAlive, timeout);
    }

    protected HttpResponse checkHit(HttpRequest request, int requestId, int statusCode, String expectedBody,
                                    boolean keepAlive, int timeout) throws IOException, URISyntaxException {
        boolean isSslModeOn = false;
        String uri = request.getUri();
        if (uri.startsWith(HTTPS_URI_SCHEME)) isSslModeOn = true; // we are going to send HTTPS request
        logRequest(requestId, request.getUri());
        HttpResponse response;
        long startTime = System.currentTimeMillis();
        logger.warn("Session start:" + startTime);
        if (isSslModeOn) {
            response = sendHttpsRequest(request, this);
        } else {
            response = sendRequest2(request, keepAlive, false, timeout);
        }
        long endTime = System.currentTimeMillis();
        logger.warn("Session end:" + endTime);
        logResponse(requestId, ResponseLocation.NETWORK, response);
        assertStatusCode(requestId, statusCode, response);
        if (expectedBody != null) {
            assertResponseBody(requestId, expectedBody, response);
        }
        addHitCheck(requestId, request.getUri(), startTime, endTime);
        return response;
    }

    protected HttpResponse checkTransient(HttpRequest request, int requestId, String expectedBody, long startTime)
            throws IOException, URISyntaxException {
        return checkTransient(request, requestId, HttpStatus.SC_OK, expectedBody, startTime);
    }

    protected HttpResponse checkTransient(HttpRequest request, int requestId, int statusCode, String expectedBody,
                                          long startTime) throws IOException, URISyntaxException {
        // There should be no http activity
        HttpResponse response = checkHit(request, requestId, statusCode, expectedBody);
        addCheckTransient(requestId, startTime, System.currentTimeMillis());
        return response;
    }

    protected HttpResponse checkTransient(HttpRequest request, int requestId, long startTime) throws IOException, URISyntaxException {
        return checkTransient(request, requestId, null, startTime);
    }

    protected int checkMissHit(List<HttpRequest> requests, int startRequestId) throws Throwable {
        return checkMissHit(requests, startRequestId, null);
    }

    protected int checkMissHit(List<HttpRequest> requests, int startRequestId, String expectedBody) throws Throwable {
        return checkMissHit(requests, startRequestId, expectedBody, false);
    }

    protected int checkMissHit(List<HttpRequest> requests, int startRequestId, String expectedBody,
                               boolean checkDnsActivity) throws Throwable {
        return checkMissHit(requests, startRequestId, HttpStatus.SC_OK, expectedBody, checkDnsActivity);
    }

    protected int checkMissHit(List<HttpRequest> requests, int startRequestId, int statusCode, String expectedBody,
                               boolean checkDnsActivity) throws Throwable {
        for (HttpRequest request : requests) {
            startRequestId = checkMissHit(request, startRequestId, statusCode, expectedBody, checkDnsActivity);
        }
        return startRequestId;
    }

    protected int checkMissHit(HttpRequest request, int startRequestId, int statusCode, String expectedBody,
                               boolean checkDnsActivity) throws Throwable {
        // this request shall be cached
        HttpResponse resp = checkMiss(request, startRequestId, statusCode, expectedBody);
        if (checkDnsActivity) {
//            addDnsReqActivityCheck(startRequestId, resp, true);  // TODO:
        }
        startRequestId++;
        // this request shall be returned from cache
        resp = checkHit(request, startRequestId, statusCode, expectedBody);
        if (checkDnsActivity) {
//            addDnsReqActivityCheck(startRequestId, resp, false); // TODO:
        }
        startRequestId++;
        return startRequestId;
    }

    protected int checkMissHit(HttpRequest request, List<String> uris, int startRequestId, String expectedBody,
                               boolean checkDnsActivity) throws Throwable {
        for (String uri : uris) {
            request.setUri(uri);
            startRequestId = checkMissHit(request, startRequestId, HttpStatus.SC_OK, expectedBody, checkDnsActivity);
        }
        return startRequestId;
    }

    protected int checkPoll(HttpRequest request, int startRequestId, int hitcounts, long sleepTime) throws Throwable {
        return checkPoll(request, startRequestId, hitcounts, sleepTime, null);
    }

    protected int checkPoll(HttpRequest request, int startRequestId, int hitcounts, long sleepTime, String expectedBody)
            throws Throwable {
        return checkPoll(request, startRequestId, hitcounts, sleepTime, HttpStatus.SC_OK, expectedBody);
    }

    protected int checkPoll(HttpRequest request, int startRequestId, int hitcounts, long sleepTime, int statusCode,
                            String expectedBody) throws Throwable {
        HttpResponse response = checkMiss(request, startRequestId++, statusCode, expectedBody);
        logSleeping(sleepTime - response.getDuration());

        response = checkMiss(request, startRequestId++, statusCode, expectedBody);
        logSleeping(sleepTime - response.getDuration());

        response = checkMiss(request, startRequestId++, statusCode, expectedBody);
        logSleeping(sleepTime - response.getDuration());

        for (int i = 0; i < hitcounts; i++) {
            // this request shall be returned from cache
            response = checkHit(request, startRequestId++, statusCode, expectedBody);
            logSleeping(sleepTime - response.getDuration());
        }
        return startRequestId;
    }

    protected void checkMultipleSessions(long startTime, long endTime, int sessionsCount) {
        addCheckMultipleSessions(startTime, endTime, sessionsCount);
    }

    /**
     * Method to check response duration less than 60s otherwise it throws exception
     *
     * @throws Exception
     */
    public void responseCheckDuration(int responseId, HttpResponse response) throws Exception {
        if (response.getDuration() > 60 * 1000) {
            logger.info("Response duration was bigger than 60 sec");
            logger.info("Response R" + getShortThreadName() + "." + responseId + " duration bigger than expected - "
                    + response.getDuration());
            throw new Exception("Response duration is bigger than expected");
        }
        logger.info("Response R" + getShortThreadName() + "." + responseId + " duration is acceptable");
    }

    protected HttpResponse sendMiss(int requestId, HttpRequest request, int expectedStatusCode, String expectedBody,
                                    Integer expectedDuration, Boolean bypassOC) throws Exception {
        ResourceType rType = getResourceType(request.getUri());
        logRequest(requestId, request.getUri());
        HttpResponse response;
        if (bypassOC == null || bypassOC == false) {
            response = sendRequest2(request);
        } else {
            response = sendRequest2(request, false, true);
        }
        logResponse(requestId, ResponseLocation.NETWORK, response);
        assertStatusCode(requestId, expectedStatusCode, response);
        if (expectedBody != null) {
            assertResponseBody(requestId, expectedBody, response);
        }
        if (rType == ResourceType.TEST_RUNNER && expectedDuration == null) {
            responseCheckDuration(requestId, response);
        } else if (expectedDuration != null) {
            int delta = 8000;
            assertTrue("Response duration is about " + expectedDuration, (expectedDuration > response.getDuration()
                    - delta)
                    && (expectedDuration < response.getDuration() + delta));
        }
        return response;
    }

    protected HttpResponse sendHit(int requestId, HttpRequest request, int expectedStatusCode, String expectedBody,
                                   Integer expectedDuration) throws Exception {
        ResourceType rType = getResourceType(request.getUri());
        logRequest(requestId, request.getUri());
        HttpResponse response = sendRequest2(request);
        logResponse(requestId, ResponseLocation.CACHE, response);
        assertStatusCode(requestId, expectedStatusCode, response);
        if (expectedBody != null) {
            assertResponseBody(requestId, expectedBody, response);
        }
        if (rType == ResourceType.TEST_RUNNER && expectedDuration == null) {
            responseCheckDuration(requestId, response);
        } else if (expectedDuration != null) {
            int delta = 5000;
            assertTrue("Response duration is about " + expectedDuration, (expectedDuration > response.getDuration()
                    - delta)
                    && (expectedDuration < response.getDuration() + delta));
        }
        return response;
    }

    protected HttpResponse sendHit(int requestId, HttpRequest request, int expectedStatusCode, String expectedBody)
            throws Exception {
        return sendHit(requestId, request, expectedStatusCode, expectedBody, null);
    }

    protected HttpResponse sendHit(int requestId, HttpRequest request, String expectedBody) throws Exception {
        return sendHit(requestId, request, HttpStatus.SC_OK, expectedBody);
    }

    protected HttpResponse sendHit(int requestId, HttpRequest request, int expectedDuration) throws Exception {
        return sendHit(requestId, request, HttpStatus.SC_OK, null, expectedDuration);
    }

    protected HttpResponse sendHit(int requestId, HttpRequest request) throws Exception {
        return sendHit(requestId, request, HttpStatus.SC_OK, null);
    }

    protected HttpResponse sendMiss(int requestId, HttpRequest request, Boolean bypassOC) throws Exception {
        return sendMiss(requestId, request, HttpStatus.SC_OK, null, null, bypassOC);
    }

    protected HttpResponse sendMiss(int requestId, HttpRequest request, int expectedStatusCode, String expectedBody,
                                    Integer expectedDuration) throws Exception {
        return sendMiss(requestId, request, expectedStatusCode, expectedBody, expectedDuration, null);
    }

    protected HttpResponse sendMiss(int requestId, HttpRequest request, int expectedStatusCode, String expectedBody)
            throws Exception {
        return sendMiss(requestId, request, expectedStatusCode, expectedBody, null);
    }

    protected HttpResponse sendMiss(int requestId, HttpRequest request, String expectedBody) throws Exception {
        return sendMiss(requestId, request, HttpStatus.SC_OK, expectedBody);
    }

    protected HttpResponse sendMiss(int requestId, HttpRequest request, int expectedDuration) throws Exception {
        return sendMiss(requestId, request, HttpStatus.SC_OK, null, expectedDuration);
    }

    protected HttpResponse sendMiss(int requestId, HttpRequest request) throws Exception {
        return sendMiss(requestId, request, HttpStatus.SC_OK, null);
    }

    protected void addMissCheck(int requestId, String uri, long startTime, long endTime) {
        checks.add(new MissCheck(requestId, uri, startTime, endTime));
    }

    protected void addHitCheck(int requestId, String uri, long startTime, long endTime) {
        checks.add(new HitCheck(requestId, uri, startTime, endTime));
    }

    protected void addNetworkActivityCheck(long startTime, long endTime, boolean wasNetworkActivity) {
        checks.add(new NetworkActivityCheck(startTime, endTime, wasNetworkActivity));
    }

    protected void addCheckMultipleSessions(long startTime, long endTime, int sessionsCount) {
        checks.add(new MultipleSessionsCheck(startTime, endTime, sessionsCount));
    }

    protected void addCheckTransient(int responseId, long startTime, long endTime) {
        checks.add(new TransientCheck(responseId, startTime, endTime));
    }

    protected void addCheckSocketCount(int responseId, int socketCount) {
        checks.add(new SocketCountCheck(responseId, socketCount));
    }

    protected void addCheckSocketClose(NetStat.SocketInfo mSocketInfo) {
        checks.add(new SocketCloseCheck(mSocketInfo));
    }

    protected void addCheckSocketClose() {
        checks.add(new SocketCloseCheck());
    }

    private abstract class Check {
        abstract void doCheck();

        protected String getTimeLimits(long startTime, long endTime) {
            return " from " + startTime + " till " + endTime;
        }
    }

    private class MissCheck extends Check {

        private int requestId;
        private String uri;
        private long startTime;
        private long endTime;
        private boolean isSslModeOn;

        public MissCheck(int requestId, String uri, long startTime, long endTime) {
            this.requestId = requestId;
            this.uri = uri;
            this.startTime = startTime;
            this.endTime = endTime;
            if (uri.startsWith(HTTPS_URI_SCHEME)) isSslModeOn = true;
        }

        @Override
        void doCheck() {
            List<HttpSession> sessions;
            if (isSslModeOn) {
                sessions = tcpDump.getHttpsSessions(startTime, endTime);
            } else {
                sessions = tcpDump.getHttpSessions(uri, startTime, endTime);
                if (sessions.size() == 0) {
                    logger.debug("No sessions found for URI: " + uri + ". Trying to check using timestamps only");
                    sessions = tcpDump.getHttpSessions(startTime, endTime);
                }
            }
            boolean wasSessionViaOc = false;
            boolean wasNetworkSession = false;
            for (HttpSession session : sessions) {
                //logger.trace("doCheck: session: "+session.toString());
                //logger.trace("MIN_DISPATCHER_PORT: "+MIN_DISPATCHER_PORT);
                if (session.getInterface() == Interface.LOOPBACK
                        && TcpDumpHelper.isKnownServerPort(session.getServerPort())) {
                    wasSessionViaOc = true;
                }
                if (session.getInterface() == Interface.NETWORK) wasNetworkSession = true;
            }
            assertTrue("Request " + requestId + " should be sent via OC", wasSessionViaOc);
            assertTrue("Response " + requestId + " should be received from network", wasNetworkSession);
        }
    }

    private class HitCheck extends Check {

        private int requestId;
        private String uri;
        private long startTime;
        private long endTime;
        private boolean isSslModeOn;

        public HitCheck(int requestId, String uri, long startTime, long endTime) {
            this.requestId = requestId;
            this.uri = uri;
            this.startTime = startTime;
            this.endTime = endTime;
            if (uri.startsWith(HTTPS_URI_SCHEME)) isSslModeOn = true;
        }

        @Override
        void doCheck() {
            List<HttpSession> sessions;
            if (isSslModeOn) {
                sessions = tcpDump.getHttpsSessions(startTime, endTime);
            } else {
                sessions = tcpDump.getHttpSessions(uri, startTime, endTime);
                if (sessions.size() == 0) {
                    logger.debug("No sessions found for URI: " + uri + ". Trying to check using timestamps only");
                    sessions = tcpDump.getHttpSessions(startTime, endTime);
                }
            }
            Log.e("TCDTC", "Sessions count = " + sessions);
            for (HttpSession session1 : sessions) {
                Log.e("TCDTC", session1.toString());
            }
            boolean wasSessionViaOc = false;
            boolean isHit = true;
            for (HttpSession session : sessions) {
                //logger.trace("doCheck: session: "+session.toString());
                //logger.trace("MIN_DISPATCHER_PORT: "+MIN_DISPATCHER_PORT);
                if (session.getInterface() == Interface.LOOPBACK
                        && (TcpDumpHelper.isKnownServerPort(session.getServerPort()))) {
                    wasSessionViaOc = true;
                }
                if (session.getInterface() == Interface.NETWORK) isHit = false;
            }
            assertTrue("Request " + requestId + " should be sent via OC", wasSessionViaOc);
            assertTrue("Response " + requestId + " should be received from cache", isHit);
        }
    }

    private class MultipleSessionsCheck extends Check {
        private long startTime;
        private long endTime;
        int sessionsCount;

        MultipleSessionsCheck(long startTime, long endTime, int sessionsCount) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.sessionsCount = sessionsCount;
        }

        @Override
        void doCheck() {
            List<HttpSession> allSessions = tcpDump.getHttpSessions(startTime, endTime);
            List<Integer> knownPorts = Arrays.asList(HTTP_PORT, HTTPS_PORT, HTTPS_SPLIT_PORT);
            int sessionsCount = 0;
            for (HttpSession session : allSessions) {
                if ((knownPorts.contains(session.getServerPort()) || session.getServerPort() >= MIN_DISPATCHER_PORT) && session.getInterface() == Interface.NETWORK) {
                    sessionsCount++;
                }
            }
            assertEquals("Sessions count = " + this.sessionsCount + getTimeLimits(startTime, endTime),
                    this.sessionsCount, sessionsCount);
        }
    }

    private class TransientCheck extends Check {

        private int requestId;
        private long startTime;
        private long endTime;

        TransientCheck(int requestId, long startTime, long endTime) {
            this.requestId = requestId;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        @Override
        void doCheck() {
            assertTrue("Should be Z7TP traffic near response " + requestId + getTimeLimits(startTime, endTime),
                    tcpDump.getZ7TpPacketsCount(startTime, endTime) > 0);
        }
    }

    private class SocketCountCheck extends Check {

        private int responseId;
        private int socketCount;

        SocketCountCheck(int responseId, int socketCount) {
            this.responseId = responseId;
            this.socketCount = socketCount;
        }

        @Override
        void doCheck() {
            List<NetStat.SocketInfo> socketsList = new ArrayList<NetStat.SocketInfo>();
            for (HttpSession session : tcpDump.getHttpSessions(testStartTimestamp, testEndTimestamp)) {
                int serverPort = session.getServerPort();
                Interface anInterface = session.getInterface();
                if ((serverPort == HTTP_PORT) && (anInterface == Interface.NETWORK)) {

                    socketsList.add(new NetStat.SocketInfo(session.getClientAddress(), session.getClientPort(),
                            session.getServerAddress(), session.getServerPort()));
                }
            }
            assertEquals("Response R" + responseId + " - Expected socket count = " + socketCount, socketCount,
                    socketsList.size());
        }
    }

    private class SocketCloseCheck extends Check {

        private NetStat.SocketInfo mSocketInfo = null;

        SocketCloseCheck(NetStat.SocketInfo socketInfo) {
            mSocketInfo = socketInfo;
        }

        SocketCloseCheck() {
        }

        @Override
        void doCheck() {
            List<NetStat.SocketInfo> socketsList = new ArrayList<NetStat.SocketInfo>();
            for (HttpSession session : tcpDump.getHttpSessions(testStartTimestamp, testEndTimestamp)) {
                if (session.getServerPort() >= MIN_DISPATCHER_PORT || session.getServerPort() == HTTP_PORT) {
                    socketsList.add(new NetStat.SocketInfo(session.getClientAddress(), session.getClientPort(),
                            session.getServerAddress(), session.getServerPort()));
                }
            }
            NetStat ns = new NetStat();
            if (mSocketInfo != null) {
                assertTrue("Socket " + mSocketInfo.toString() + " should be closed", !ns.getSockets()
                        .contains(mSocketInfo));
            } else {
                for (NetStat.SocketInfo si : socketsList) {
                    assertTrue("Socket " + si.toString() + " should be closed", !ns.getSockets().contains(si));
                }
            }
        }
    }

    private class NetworkActivityCheck extends Check {
        private long mStartTime;
        private long mStopTime;
        boolean wasNetworkActivity;

        NetworkActivityCheck(long startTime, long endTime, boolean wasNetworkActivity) {
            mStartTime = startTime;
            mStopTime = endTime;
            this.wasNetworkActivity = wasNetworkActivity;
        }

        @Override
        public void doCheck() {
            if (wasNetworkActivity) {
                assertTrue("There should be network activity " + getTimeLimits(mStartTime, mStopTime),
                        tcpDump.getHttpSessions(mStartTime, mStopTime).size() > 0);
            } else {
                assertTrue("There should not be network activity " + getTimeLimits(mStartTime, mStopTime),
                        tcpDump.getHttpSessions(mStartTime, mStopTime).size() == 0);
            }
        }
    }

    protected static class RequestResponse {
        private HttpRequest request;
        private HttpResponse response;

        private boolean keepAlive;

        private int requestCurrentNumber;
        private int requestInterval;

        private int sessionCounts;

        private int responseTimeout;

        private int responseExpectedStatusCode;
        private String responseExpectedBody;

        private HttpRequest invaildateRequestAfterResponse;
        private int invaildateResponseTimeout;

        public RequestResponse(HttpRequest request,
                               int requestCurrentNumber,
                               int responseTimeout,
                               int requestInterval,
                               int responseExpectedStatusCode,
                               String responseExpectedBody,
                               int sessionCounts,
                               HttpRequest invaildateRequestAfterResponse,
                               int invaildateResponseTimeout) {
            this.request = request;
            this.requestCurrentNumber = requestCurrentNumber;
            this.keepAlive = false; // by default
            this.responseTimeout = responseTimeout;
            this.requestInterval = requestInterval;
            this.responseExpectedStatusCode = responseExpectedStatusCode;
            this.responseExpectedBody = responseExpectedBody;
            this.sessionCounts = sessionCounts;
            this.invaildateRequestAfterResponse = invaildateRequestAfterResponse;
            this.invaildateResponseTimeout = invaildateResponseTimeout;
        }

        public RequestResponse(HttpRequest request,
                               int requestCurrentNumber,
                               int responseTimeout,
                               int requestInterval,
                               int responseExpectedStatusCode,
                               String responseExpectedBody,
                               int sessionCounts) {
            this.request = request;
            this.requestCurrentNumber = requestCurrentNumber;
            this.keepAlive = false; // by default
            this.responseTimeout = responseTimeout;
            this.requestInterval = requestInterval;
            this.responseExpectedStatusCode = responseExpectedStatusCode;
            this.responseExpectedBody = responseExpectedBody;
            this.sessionCounts = sessionCounts;
        }

        public RequestResponse(HttpRequest request,
                               int requestCurrentNumber,
                               boolean keepAlive,
                               int responseTimeout,
                               int requestInterval,
                               int responseExpectedStatusCode,
                               String responseExpectedBody,
                               int sessionCounts) {
            this.request = request;
            this.requestCurrentNumber = requestCurrentNumber;
            this.keepAlive = keepAlive;
            this.responseTimeout = responseTimeout;
            this.requestInterval = requestInterval;
            this.responseExpectedStatusCode = responseExpectedStatusCode;
            this.responseExpectedBody = responseExpectedBody;
            this.sessionCounts = sessionCounts;
        }
    }

    protected HttpResponse executeConncetion(RequestResponse requestResponse) throws Throwable {
        if (requestResponse.sessionCounts == 1) {
            requestResponse.response = checkMiss(requestResponse.request, requestResponse.requestCurrentNumber,
                    requestResponse.responseExpectedStatusCode, requestResponse.responseExpectedBody,
                    requestResponse.keepAlive, requestResponse.responseTimeout);
        } else if (requestResponse.sessionCounts == 0) {
            requestResponse.response = checkHit(requestResponse.request, requestResponse.requestCurrentNumber,
                    requestResponse.responseExpectedStatusCode, requestResponse.responseExpectedBody,
                    requestResponse.keepAlive, requestResponse.responseTimeout);
        } else {

            logRequest(requestResponse.requestCurrentNumber, requestResponse.request.getUri());
            long start = System.currentTimeMillis();
            requestResponse.response = sendRequest2(requestResponse.request, requestResponse.keepAlive, false,
                    requestResponse.responseTimeout);
            logResponse(requestResponse.requestCurrentNumber, ResponseLocation.NETWORK, requestResponse.response);
            assertStatusCode(requestResponse.requestCurrentNumber, requestResponse.responseExpectedStatusCode,
                    requestResponse.response);
            if (requestResponse.responseExpectedBody != null) {
                assertResponseBody(requestResponse.requestCurrentNumber, requestResponse.responseExpectedBody,
                        requestResponse.response);
            }
            if (requestResponse.sessionCounts > 1) {
                checkMultipleSessions(start, System.currentTimeMillis(), requestResponse.sessionCounts);
            }
        }

        if (requestResponse.invaildateRequestAfterResponse != null) {
            HttpResponse resonseInvalidation = sendRequest(requestResponse.invaildateRequestAfterResponse, null, false,
                    true, Body.BODY, requestResponse.invaildateResponseTimeout, null);
            if (resonseInvalidation.getStatusCode() == -1) {
                logSleeping(requestResponse.requestInterval - requestResponse.response.getDuration() -
                        requestResponse.invaildateResponseTimeout);
            } else {
                logSleeping(requestResponse.requestInterval - requestResponse.response.getDuration() -
                        resonseInvalidation.getDuration());
            }
        } else {
            logSleeping(requestResponse.requestInterval - requestResponse.response.getDuration());
        }
        return requestResponse.response;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        SSLContext mSslContext = SSLContext.getInstance("SSL");
        TrustManager[] tm = new TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        }};
        mSslContext.init(null, tm, null);
        sslSocketFactory = mSslContext.getSocketFactory();
        Class c = Class.forName(org.apache.http.conn.ssl.SSLSocketFactory.class.getName());
        Constructor<SSLSocketFactory> con = c.getConstructor(javax.net.ssl.SSLSocketFactory.class);
        SSLSocketFactory apacheSslSocketFactory = con.newInstance(sslSocketFactory);
        apacheSslSocketFactory.setHostnameVerifier(new AllowAllHostnameVerifier());
    }

    @Override
    protected void runTest() throws Throwable {
        //logger.trace("runTest start");
        tcpDump = TcpDumpUtil.getInstance(getContext());
        tcpDump.start();
        testStartTimestamp = System.currentTimeMillis();
        //logger.trace("testStartTimestamp=" + testStartTimestamp + "  " + new Date(testStartTimestamp).toString());
        //super.setUp();

        super.runTest();
        tcpDump.stop();
        testEndTimestamp = System.currentTimeMillis();
        //logger.trace("testEndTimestamp=" + testEndTimestamp + "  " + new Date(testEndTimestamp).toString());
        executeChecks();
    }

    @Override
    protected void tearDown() throws Exception {
        tcpDump.stop();
        super.tearDown();
    }

    @Override
    public void decorate(HttpURLConnection conn) throws IOException {
        if (conn instanceof HttpsURLConnection) {
            logger.info("Https decorator applied");
            HttpsURLConnection httpsConnection = (HttpsURLConnection) conn;
            httpsConnection.setSSLSocketFactory(sslSocketFactory);
            httpsConnection.setHostnameVerifier(new AllowAllHostnameVerifier());
        }
    }

    protected HttpResponse sendHttpsRequest(HttpRequest request, int timeOut) throws IOException, URISyntaxException {
        return sendRequest(request, this, false, false, Body.NOBODY, timeOut, null);
    }

    protected HttpResponse sendHttpsRequest(HttpRequest request) throws IOException, URISyntaxException {
        return sendRequest(request, this, false, false, Body.BODY);
    }

    protected HttpResponse sendHttpsRequest(HttpRequest request, boolean keepAlive) throws IOException, URISyntaxException {
        return sendRequest(request, this, keepAlive, false, Body.BODY);
    }

    protected HttpResponse sendHttpsRequest(HttpRequest request, HttpUrlConnectionIF decorator)
            throws IOException, URISyntaxException {
        return sendRequest(request, decorator, false, false, Body.BODY);
    }

    protected HttpResponse sendHttpsRequest(byte[] request, String host) {
        return sendRequest(request, host, sslSocketFactory);
    }

    protected void executeChecks() {
        for (Check check : checks) {
            check.doCheck();
        }
    }

    protected void resolveHost(String host) throws IOException {
        SimpleResolver resolver = new SimpleResolver();
        Message msg = resolver.send(createDnsQuery(host + ".", Type.A));
        //Log.v(TAG, "resolveHost: message:" + msg.toString()) ;
    }

    protected Message createDnsQuery(String host, int type) {
        Message msg = null;
        try {
            Name name = Name.fromString(host);
            msg = Message.newQuery(Record.newRecord(name, type, DClass.IN));
        } catch (TextParseException tpe) {
            Log.e(TAG, ExceptionUtils.getStackTrace(tpe));
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        return msg;
    }

    protected long getDirSize(File dir) {
        long size = 0;
        if (dir.isFile()) {
            size = dir.length();
        } else {
            File[] subFiles = dir.listFiles();
            for (File file : subFiles) {
                if (file.isFile()) {
                    size += file.length();
                } else {
                    size += getDirSize(file);
                }
            }
        }
        return size;
    }

    protected static void checkPcapUploadingInterval(Calendar startData, Calendar endData) throws IOException, InterruptedException {

        int previousTimeInMinute = 0;
        int currentTimeInMinute;
        int diff;

        GregorianCalendar currentFileTime = new GregorianCalendar();
        currentFileTime.setTime(new Date());
        int counter = 0;

        String neededFileName = "capture_[0-9]*_(201[2-9])-([0-9]*)-([0-9]*)-([0-9]{2})([0-9]{2})[0-9]{2}\\.pcap";
        Pattern pattern = Pattern.compile(neededFileName);
        Matcher matcher;

        File F = new File("/sdcard/OpenChannel/Logs/");
        File[] fList = F.listFiles();
        String name = "";
        for (int i = 0; i < fList.length; i++) {
            if (fList[i].isFile()) {
                name = fList[i].getName();
                matcher = pattern.matcher(name);
                if (matcher.find()) {
                    currentFileTime.set(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)) - 1, Integer.parseInt(matcher.group(3)), Integer.parseInt(matcher.group(4)) + 3, Integer.parseInt(matcher.group(5)));
                    if (currentFileTime.after(startData) && currentFileTime.before(endData)) {
                        Log.i(TAG, "Found file within the interval: " + currentFileTime.toString());
                        currentTimeInMinute = currentFileTime.get(Calendar.MINUTE);
                        if (currentTimeInMinute >= previousTimeInMinute)
                            diff = currentTimeInMinute - previousTimeInMinute;
                        else diff = 60 - previousTimeInMinute + currentTimeInMinute;
                        previousTimeInMinute = currentTimeInMinute;
                        if (counter != 0 && counter != 1)
                            assertTrue("TCP dump pcap files should be uploaded to the card each 15 min, but were uploaded after " + diff + "min. ", ((diff >= 14) && (diff <= 16)));
                        else counter++;
                    }
                }
            }
        }
    }

//Moved overlapping and communication with rela functionality to decorator
}
