package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.base.*;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.conn.ConnLogger;
import com.seven.asimov.it.utils.conn.ConnSelector;
import com.seven.asimov.it.utils.conn.HttpClient;
import com.seven.asimov.it.utils.conn.SysSocketDescriptor;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.channels.SelectableChannel;
import java.util.Hashtable;
import java.util.List;

import static com.seven.asimov.it.utils.conn.ConnTestResult.StateCode;
import static com.seven.asimov.it.utils.conn.ConnTestResult.StateCode.CONNECTED;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnType.CLIENT;

/**
 * <p>TC69</p>
 * <p/>
 * <p>Connection with two requests are served from cache</p>
 * <p/>
 * <p>
 * Pre-requisite: OC startups and works fine. Request B and C are served from cache.
 * </p>
 * <p/>
 * Steps:
 * <p/>
 * <ul>
 * <li>1. App setup connection with OC, then App sends request A to OC.</li>
 * <li>2. App sends request B, then request C.</li>
 * <li>3. Host sends response for request A because request B, C are servered from cache.</li>
 * <li>4. Check connections in logs.
 * </ul>
 * <p/>
 * Expected Result:
 * <p/>
 * <ul>
 * <li>1. OC should setup client connection after receive request data, then setup client connection and send request to network side.</li>
 * <li>2. OC is receiveing response from network side and forward it to app.</li>
 * <li>3. OC should send responses for request A, B, C in sequence. </li>
 * </ul>
 */

public class TwoRequestsFromCacheTest extends MultiConnectionsTest {
    private static final String TAG = TwoRequestsFromCacheTest.class.getSimpleName();
    private static final String TEST_PATH = "asimov_it_multi_request_";
    private static final long PERIOD = 5;
    protected HttpClient mClient;
    private String mExpectedBody;

    private Hashtable<String, RequestWrapper> mRequestTable = new Hashtable<String, RequestWrapper>();
    private static final String REQUEST_ID_A = "a", REQUEST_ID_B = "b", REQUEST_ID_C = "c";
    private static final int MAX_REQUEST_ID = 4;

    public TwoRequestsFromCacheTest(ConnSelector selector, String expectedBody) {
        this(TAG, selector, TEST_PATH, expectedBody, PERIOD);
    }

    public TwoRequestsFromCacheTest(String name, ConnSelector selector, String testPath, String expectedBody, long pollPeriod) {
        super(name, selector, null);
        this.setClientExpectedRstCount(1);
        mExpectedBody = expectedBody;

        RequestWrapper requestA = buildRequest(REQUEST_ID_A, expectedBody);
        requestA.isSingleRequest = true;
        mRequestTable.put(requestA.mRequestId, requestA);

        RequestWrapper requestB = buildRequest(REQUEST_ID_B, expectedBody);
        mRequestTable.put(requestB.mRequestId, requestB);

        RequestWrapper requestC = buildRequest(REQUEST_ID_C, expectedBody);
        mRequestTable.put(requestC.mRequestId, requestC);
    }

    @Override
    public void onConnected(SelectableChannel channel) {
        HttpClient client = getClient(channel);
        if (client == null) {
            ConnLogger.debug(TAG, getName() + ": Get unknown channel");
            return;
        }
        mClientConnectedCount++;
        this.addResult(client.getConnId(), CLIENT, CONNECTED);

        sendRequest(client, mRequestTable.get(REQUEST_ID_A));
        sendRequest(client, mRequestTable.get(REQUEST_ID_B));
    }

    private void sendRequest(HttpClient client, RequestWrapper requestWrapper) {
        requestWrapper.mSentRequestCount++;
        try {
            client.sendRequest(requestWrapper.mRequest, true);
            ConnLogger.debug(TAG, getName() + ": client sends request[id=" + requestWrapper.mRequestId
                    + ", sentRequestCount=" + requestWrapper.mSentRequestCount + "]:\r\n" + requestWrapper.mRequest.getFullRequest());
        } catch (Exception e) {
            ConnLogger.error(TAG, getName() + ": Send requests failed", e);
            this.fail(client.getConnId(), CLIENT, StateCode.SEND_FAILED);
        }
    }

    protected static final RequestWrapper buildRequest(String requestId, String expectedBody) {
        String requestUri = AsimovTestCase.createTestResourceUri(TEST_PATH + "requestID");
        HttpRequest request = buildSimpleRequest(requestUri, null, true);
        request.addHeaderField(new HttpHeaderField("X-OC-ContentEncoding", "identity"));
        request.addHeaderField(new HttpHeaderField("X-OC-ResponseStatus", String.valueOf(HttpStatus.SC_OK)));

        addExpectedResponse(request, requestId, expectedBody);

        RequestWrapper requestWrapper = new RequestWrapper();
        requestWrapper.mRequestId = requestId;
        requestWrapper.mRequestUri = requestUri;
        requestWrapper.mRequest = request;

        return requestWrapper;
    }

    private static void addExpectedResponse(HttpRequest request, String requestId, String expectedBody) {
        String expectedResponse = "HTTP/1.1 200 OK" + TFConstantsIF.CRLF
                + TFConstantsIF.HEADER_CONNECTION + ": " + TFConstantsIF.HEADER_CONNECTION_CLOSE + TFConstantsIF.CRLF
                + HttpClient.OC_HEADER_FIELD_REQ_ID + ": " + requestId + TFConstantsIF.CRLF
                + "Content-Length: " + expectedBody.length() + TFConstantsIF.CRLF + TFConstantsIF.CRLF
                + expectedBody;
        String encodedResponse = URLEncoder.encode(Base64.encodeToString(expectedResponse.getBytes(), Base64.DEFAULT));

        request.addHeaderField(new HttpHeaderField("X-OC-Raw:", encodedResponse));
    }

    static class RequestWrapper {
        String mRequestId;
        String mRequestUri;
        HttpRequest mRequest;

        int mSentRequestCount;
        int mReceivedResponseCount;
        boolean isPass;
        boolean isSingleRequest;
    }

    //=============================
    @Override
    protected final boolean onStart() {
        try {
            PrepareResourceUtil.prepareResource(mRequestTable.get(REQUEST_ID_A).mRequestUri, false);
            PrepareResourceUtil.prepareResource(mRequestTable.get(REQUEST_ID_B).mRequestUri, false);
            PrepareResourceUtil.prepareResource(mRequestTable.get(REQUEST_ID_C).mRequestUri, false);
        } catch (Exception e) {
            ConnLogger.error(TAG, getName() + ": prepareResource", e);
            return false;
        }

        return (setupConnection(true) != null);
    }


    @Override
    public final void onResponseReceived(int connId, HttpResponse response) {
        HttpClient client = getClient(connId);
        if (client == null) {
            ConnLogger.error(TAG, getName() + ": Can't find client for conn:" + connId);
            return;
        }

        if (HttpStatus.SC_OK != response.getStatusCode()) {
            fail("HttpStatus [" + response.getStatusCode() + "] is not exptected [" + HttpStatus.SC_OK + "]");
            return;
        }

        if (mExpectedBody != null && !mExpectedBody.equals(response.getBody())) {
            fail("Current reponse body [" + response.getBody() + "] is not exptected [" + mExpectedBody + "]");
            return;
        }

        onResponseHandled(client, response);
    }

    protected void onResponseHandled(HttpClient client, HttpResponse response) {
        String requestId = response.getHeaderField(HttpClient.OC_HEADER_FIELD_REQ_ID);
        ConnLogger.debug(TAG, "Get response:" + requestId);

        RequestWrapper request = mRequestTable.get(requestId);

        request.mReceivedResponseCount++;
        if (request.mReceivedResponseCount < MAX_REQUEST_ID) {
            //request 1~3
            if (response.getHeaderField("X-OC-Cache") != null) {
                fail("OC should not return response from cache for request:" + request.mReceivedResponseCount);
                return;
            }
        }
        //response 4, should from cache
        else if (request.mReceivedResponseCount == MAX_REQUEST_ID) {
            if (verifyCachedResponseHeader(response)) {
                onServingFromCache(request);
            }
        }

        checkIfFinished("onResponseReceived", request, response.getDuration());
    }

    private boolean verifyCachedResponseHeader(HttpResponse response) {
        ConnLogger.debug(TAG, getName() + ": verifyCachedResponseHeader");

        if (!"HIT".equals(response.getHeaderField("X-OC-Cache"))) {
            fail("Not hit cache in the fourth request");
            return false;
        }

        if (!"YES".equals(response.getHeaderField("X-OC-Cache-Polled"))) {
            fail("Not polled cache in the fourth request");
            return false;
        }

        return true;
    }

    protected void onServingFromCache(RequestWrapper request) {
        ConnLogger.debug(TAG, "onServingFromCache");
        if (request.mSentRequestCount == request.mReceivedResponseCount) {
            request.isPass = true;
        }
    }

    private boolean checkIfFinished(String where, RequestWrapper request, long duration) {
        if (!request.isSingleRequest) {
            if (request.mSentRequestCount < MAX_REQUEST_ID) {
                long sleepTime = PERIOD - duration;
                TestUtil.sleep(sleepTime);
            } else {
                if (isPass()) {
                    this.pass();
                    return true;
                }
            }
        } else if (request.mSentRequestCount == request.mReceivedResponseCount) {
            request.isPass = true;
            ConnLogger.debug(TAG, "Test passed in " + where);

            if (isPass()) {
                this.pass();
                return true;
            }
        }

        return false;
    }

    private boolean isPass() {
        for (RequestWrapper request : mRequestTable.values()) {
            if (!request.isSingleRequest && !request.isPass) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected void onDone(Status status) {
        ConnLogger.debug(TAG, getName() + ": onDone, status:" + status);
        try {
            PrepareResourceUtil.prepareResource(mRequestTable.get(REQUEST_ID_A).mRequestUri, true);
            PrepareResourceUtil.prepareResource(mRequestTable.get(REQUEST_ID_B).mRequestUri, true);
            PrepareResourceUtil.prepareResource(mRequestTable.get(REQUEST_ID_C).mRequestUri, true);
        } catch (Exception e) {
            ConnLogger.error(TAG, getName() + ": prepareResource", e);
        }

        if (status == Status.TIMEDOUT) {
            TestUtil.sleep(1000);//sleep 1 seconds to let OC close the host socket

            List<SysSocketDescriptor> leakedSockets = mSocketsMonitor.getCurrentLeakedHostSockets();
            ConnLogger.debug(TAG, this.getName() + ": leakedSockets\n" + mSocketsMonitor.getSocketsDescription(leakedSockets));
            if (isPass()) {
                pass();
            }
        }

        for (HttpClient client : this.getAllClients()) {
            try {
                client.close();
            } catch (IOException e) {
            }
        }
    }
}
