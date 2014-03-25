package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.HttpHeaderField;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.conn.*;
import com.seven.asimov.it.utils.conn.ConnTestResult.StateCode;
import com.seven.asimov.it.utils.conn.TcpConnection.ConnType;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.SelectableChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * TC14
 * </p>
 * <p/>
 * <p>
 * "Pre-requisite: OC startups and works fine.
 * </p>
 * Steps:
 * <ul>
 * <li>1. App setup connection with OC, then App sends request to OC.
 * <li>2. App sends FIN after received all response data and the content is served from cache due to rapid manual poll
 * start.
 * <li>3. Check connection in logs.
 * </ul>
 * <p/>
 * Expected Result:
 * <ul>
 * <li>1.OC should setup client connection after receive request data, then setup client connection and send request to
 * network side.
 * <li>2.OC receives response from network side and forwards this response to app. OC sends FIN to network after
 * receives FIN from App.
 * <li>3.OC sends RST to network side and app after receives FIN/ACK from network side.
 * </ul>
 * <p/>
 * Attention:
 * <p/>
 * We can't ensure if OC sends FIN/RST to network side in device. But, we check if there is socket leak between OC and
 * host after start poll. If no socket leak, we treat OC works as expected.
 * <p/>
 * <p/>
 * <p>
 * Summary the implementation
 * </p>
 * <p/>
 * This is the base class of connectivity with cache test cases.
 * <p/>
 * <p>
 * Lifecycle
 * </p>
 * <ul>
 * <li>onStart, prepare server resources and setup new connection
 * <li>onConnected, connection setup and send request in such connection
 * <li>onResponseReceived, receive response and send Fin. onResponseHandled is called.
 * <li>onRstReceived, receive Rst and setup new connection until max request reach
 * <li>onDone, test finish
 * </ul>
 * <p/>
 * Subclasses implement different test case by override {@link #prepareRequest}, {@link #onResponseHandled},
 * {@link #onRstReceived}, {@link #onRstHandled}, and etc.
 */
public class SendFinInRapidPollTest extends MultiConnectionsTest implements IConnListener {
    private static final String TAG = SendFinInRapidPollTest.class.getSimpleName();

    public static final int MAX_REQUEST_ID = 4;
    public static final String DEFAULT_SLEEP_FOR_LONG_POLL = "80";
    public static final String TEST_PATH = "asimov_it_send_fin_in_rapid_poll";

    protected int mRequestId;

    protected String mTestUrl;
    private long mLastResponseDuration;
    private long mPollPeriod, mResponseDelay;
    private String mExpectedBody;
    private int mExpectedHostNewSocketCount = MAX_REQUEST_ID - 1;

    private Map<HttpResponse, Boolean> isNetwork = new HashMap<HttpResponse, Boolean>();

    public SendFinInRapidPollTest(String name, ConnSelector selector, String testPath, String expectedBody,
                                  long pollPeriod) {
        super(name, selector, null);
        mPollPeriod = pollPeriod;
        mExpectedBody = expectedBody;

        // Currently OC send Rst to app when get Fin from network side, so set client expected Fin to zero
        // this.setClientExpectedFinCount(0);
        // this.setClientExpectedRstCount(MAX_REQUEST_ID);
        this.setClientExpectedFinCount(MAX_REQUEST_ID);
        this.setClientExpectedRstCount(0);
        this.setExpectedResponseCount(MAX_REQUEST_ID);

        mTestUrl = AsimovTestCase.createTestResourceUri(testPath);
    }

    public SendFinInRapidPollTest(ConnSelector selector, String expectedBody, long pollPeriod) {
        this(TAG, selector, TEST_PATH, expectedBody, pollPeriod);
    }

    protected final void sendRequest(HttpClient client) {
        try {
            HttpRequest request = prepareRequest();

            mRequestId++;
            client.sendRequest(request, true);
            ConnLogger
                    .debug(TAG,
                            getName() + ": client sends request[mRequestId=" + mRequestId + "]:\r\n"
                                    + request.getFullRequest());
        } catch (Exception e) {
            ConnLogger.error(TAG, getName() + ": Send requests failed", e);
            this.fail(client.getConnId(), ConnType.CLIENT, StateCode.SEND_FAILED);
        }
    }

    protected HttpClient sendRequestInNewConn(boolean isFirstTime) {
        if (!isFirstTime && mPollPeriod > 0 && mPollPeriod > mLastResponseDuration) {
            ConnLogger.debug(TAG, getName() + ": mPollPeriod:" + mPollPeriod + ", mLastResponseDuration:"
                    + mLastResponseDuration);
            TestUtil.sleep(mPollPeriod - mLastResponseDuration);
        }
        String ip = TFConstantsIF.OC_HTTP_PROXY_ADDRESS;
        try {
            InetAddress address = InetAddress.getByName(AsimovTestCase.TEST_RESOURCE_HOST);
            ip = address.getHostAddress();
        } catch (UnknownHostException e) {
            System.out.println("Failed to resolve ip from hostname = " + AsimovTestCase.TEST_RESOURCE_HOST);
        }
        System.out.println(" IP (TEST_RESOURCE_HOST) = " + ip);
        return setupConnection(true, ip, 80);
        // return setupConnection(true);
    }

    /**
     * It is called after handle response
     *
     * @param client
     */
    protected void onResponseHandled(HttpClient client) {
        clientSendFin(client);
    }

    /**
     * It is called after handle Rst
     *
     * @param client
     */
    protected void onRstHandled(HttpClient client) {
        nextConnect();
    }

    /**
     * Setup next new connection if needs
     */
    protected void nextConnect() {
        if (this.getAllClients().size() < MAX_REQUEST_ID && mRequestId < MAX_REQUEST_ID) {
            sendRequestInNewConn(false);
        } else {
            ConnLogger.warn(TAG, "Current clients[" + this.getAllClients().size() + ", or request [" + mRequestId
                    + "] has beyonded the max request limit [" + MAX_REQUEST_ID + "]");
        }
    }

    protected final int getExpectedHostNewSocketCount() {
        return mExpectedHostNewSocketCount;
    }

    public final void setExpectedHostNewSocketCount(int count) {
        mExpectedHostNewSocketCount = count;
    }

    /**
     * Prepare the request. Http header can be changed here.
     *
     * @return
     */
    protected HttpRequest prepareRequest() {
        HttpRequest request = buildSimpleRequest(true);
        return request;
    }

    /**
     * Construct simple request
     *
     * @param keepAlive
     * @return
     */
    protected final HttpRequest buildSimpleRequest(boolean keepAlive) {
        HttpRequest request = buildSimpleRequest(this.mTestUrl, null, keepAlive);
        request.addHeaderField(new HttpHeaderField("X-OC-ContentEncoding", "identity"));
        request.addHeaderField(new HttpHeaderField("X-OC-ResponseStatus", String.valueOf(HttpStatus.SC_OK)));
        request.addHeaderField(new HttpHeaderField("Host", AsimovTestCase.TEST_RESOURCE_HOST));

        return request;
    }

    protected void setResponseDelay(int responseDelay) {
        this.mResponseDelay = responseDelay;
    }

    /**
     * It is called when response serviced from cache
     */
    protected void onServingFromCache() {
        ConnLogger.debug(TAG, getName() + ": onServingFromCache");
    }

    // ============================================================================

    @Override
    protected final boolean onStart() {
        try {
            PrepareResourceUtil.prepareResource(this.mTestUrl, false, this.mResponseDelay);
        } catch (Exception e) {
            ConnLogger.error(TAG, getName() + ": prepareResource", e);
            return false;
        }

        return (sendRequestInNewConn(true) != null);
    }

    @Override
    public final void onConnected(SelectableChannel channel) {
        HttpClient client = getClient(channel);
        if (client == null) {
            ConnLogger.debug(TAG, getName() + ": Get unknown channel");
            return;
        }
        mClientConnectedCount++;
        this.addResult(client.getConnId(), ConnType.CLIENT, StateCode.CONNECTED);

        sendRequest(client);
    }

    @Override
    public final void onResponseReceived(int connId, HttpResponse response) {
        HttpClient client = getClient(connId);
        if (client == null) {
            ConnLogger.error(TAG, getName() + ": Can't find client for conn:" + connId);
            return;
        }

        mRecevicedResponseCount++;
        ConnLogger
                .debug(TAG, getName() + ": connId:" + connId + ", mRecevicedResponseCount:" + mRecevicedResponseCount);

        if (HttpStatus.SC_OK != response.getStatusCode()) {
            fail("HttpStatus [" + response.getStatusCode() + "] is not exptected [" + HttpStatus.SC_OK + "]");
            return;
        }

        if (mExpectedBody != null && !mExpectedBody.equals(response.getBody())) {
            fail("Current reponse body [" + response.getBody() + "] is not exptected [" + mExpectedBody + "]");
            return;
        }

        mSocketsMonitor.saveCurrentHostSockets();

        if (mRecevicedResponseCount < MAX_REQUEST_ID) {
            // request 1~3
            if (response.getHeaderField("X-OC-Cache") != null) {
                fail("OC should not return response from cache for request:" + mRequestId);
                return;
            }
        }
        // response 4, should from cache
        else if (mRecevicedResponseCount == MAX_REQUEST_ID) {
            if (verifyCachedResponseHeader(response)) {
                onServingFromCache();
            }
        }

        mLastResponseDuration = response.getDuration();

        if (!checkIfFinished("onResponseReceived")) {
            onResponseHandled(client);
        }

    }

    private boolean verifyCachedResponseHeader(HttpResponse response) {
        ConnLogger.debug(TAG, getName() + ": verifyCachedResponseHeader");

        // if (!"HIT".equals(response.getHeaderField("X-OC-Cache"))) {
        // fail("Not hit cache in the fourth request");
        // return false;
        // }
        //
        // if (!"YES".equals(response.getHeaderField("X-OC-Cache-Polled"))) {
        // fail("Not polled cache in the fourth request");
        // return false;
        // }

        return true;
    }

    protected boolean checkIfFinished(String where) {
        if (mRequestId < MAX_REQUEST_ID) {
            return false;
        }

        if ((getExpectedResponseCount() < 0 || mRecevicedResponseCount == getExpectedResponseCount())
                && (getExpectedHostNewSocketCount() < 0 || mSocketsMonitor.getTotalNewHostSockets().size() == getExpectedHostNewSocketCount())
                && verifyClientExpectedRst(mClientReceivedRstCount)
                // Currently app<->OC socket always keep in long/rapid poll, even though inactivity timeout.
                // So, it is enough that only checking if there is OC<->host sockets leak
                && mSocketsMonitor.getCurrentLeakedHostSockets().isEmpty()) {

            ConnLogger.debug(TAG, "Test passed in " + where);
            this.pass();
            return true;
        }

        return false;
    }

    @Override
    public void onRstReceived(SelectableChannel channel) {
        HttpClient client = getClient(channel);
        if (client != null) {
            int connId = client.getConnId();
            this.addResult(connId, ConnType.CLIENT, StateCode.RST_RECEIVED);
            removeClient(client);
            mClientReceivedRstCount++;

            ConnLogger.debug(TAG, getName() + ": onRstReceived, connId:" + connId + ", mClientReceivedRstCount:"
                    + mClientReceivedRstCount);
            try {
                client.close();
            } catch (IOException e) {
                ConnLogger.debug(TAG, getName() + ": Close client connection excepton", e);
            }

            if (!checkIfFinished("onRstReceived")) {
                onRstHandled(client);
            }

        } else {
            ConnLogger.debug(TAG, getName() + ": onRstReceived, can't find client for channel:" + channel);
        }
    }

    @Override
    public void onFinReceived(SelectableChannel channel) {
        HttpClient client = getClient(channel);

        if (client != null) {
            int connId = client.getConnId();
            this.addResult(connId, ConnType.CLIENT, StateCode.FIN_RECEIVED);
            removeClient(client);
            mClientReceivedFinCount++;

            ConnLogger.debug(TAG, getName() + ": mClientReceivedFinCount, connId:" + connId
                    + ", mClientReceivedFinCount:" + mClientReceivedFinCount);
            try {
                client.close();
            } catch (IOException e) {
                ConnLogger.debug(TAG, getName() + ": Close client connection excepton", e);
            }

            if (!checkIfFinished("onFinReceived")) {
                onRstHandled(client);
            }
        } else {
            ConnLogger.debug(TAG, getName() + ": mClientReceivedFinCount, can't find client for channel:" + channel);
        }
    }

    @Override
    protected void onDone(Status status) {
        ConnLogger.debug(TAG, getName() + ": onDone, status:" + status);
        try {
            //AsimovCachingTestCase.invalidateResourceSafely(this.mTestUrl);
            PrepareResourceUtil.invalidateLongPoll(this.mTestUrl);
        } catch (Exception e) {
            ConnLogger.error(TAG, getName() + ": prepareResource", e);
        }

        if (status == Status.TIMEDOUT) {
            TestUtil.sleep(1000);// sleep 1 seconds to let OC close the host socket

            List<SysSocketDescriptor> leakedSockets = mSocketsMonitor.getCurrentLeakedHostSockets();
            ConnLogger.debug(TAG,
                    this.getName() + ": leakedSockets\n" + mSocketsMonitor.getSocketsDescription(leakedSockets));
            if (!leakedSockets.isEmpty()) {
                ConnLogger.debug(TAG, this.getName() + ":\n" + mSocketsMonitor.dumpSocketsStatus());
                fail("Leaked sockets are found: \n" + mSocketsMonitor.getSocketsDescription(leakedSockets));
            } else if (getExpectedHostNewSocketCount() > 0
                    && mSocketsMonitor.getTotalNewHostSockets().size() != getExpectedHostNewSocketCount()) {
                fail("OC did not setup extra new host connection. Expected [" + getExpectedHostNewSocketCount()
                        + "] new sockets, but only get sockets:\n"
                        + mSocketsMonitor.getSocketsDescription(mSocketsMonitor.getTotalNewHostSockets()));
            } else if (!verifyClientExpectedRst(mClientReceivedRstCount)) {
                fail("App did not get expected Rst, expected [" + getClientExpectedRstCount() + "], but received ["
                        + mClientReceivedRstCount + "]");
            } else if (getExpectedResponseCount() > 0 && mRecevicedResponseCount != getExpectedResponseCount()) {
                this.fail("Client received [" + mRecevicedResponseCount + "] reponses, expected ["
                        + getExpectedResponseCount() + "]");
            } else {
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