package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.base.Base64;
import com.seven.asimov.it.base.HttpHeaderField;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.conn.ConnLogger;
import com.seven.asimov.it.utils.conn.ConnSelector;
import com.seven.asimov.it.utils.conn.HttpClient;

import java.net.URLEncoder;
import java.nio.channels.SelectableChannel;


/**
 * <p>
 * TC36
 * </p>
 * <p/>
 * <p>
 * HTTP closure as "CONNECTION: close" in response header and the content is servered from cache due to rapid manual
 * poll.
 * </p>
 * <p/>
 * <p>
 * Pre-requisite: OC startups and works fine.
 * </p>
 * <p/>
 * Steps:
 * <ul>
 * <li>1. App sends request and setup connection with OC.
 * <li>2. App sends same request 3 times and Host sends response lead to start rapid manual poll.
 * <li>3. App sends same request to OC.
 * </ul>
 * <p/>
 * Expected Result:
 * <ul>
 * <li>1. The response with "CONNECTION: close" should be saved in cache. Then OC sends RST to App, and client
 * connection has been closed by host.
 * <li>2. OC submits START_POLL task and get rapid manual poll start response.
 * <li>3. OC sends the response and RST to App.
 * </ul>
 * <p/>
 * ATTENTION:
 * <p/>
 * We can't detect if OC submits START_POLL task. So, we just check if cache marks exist in headers
 */
public class CloseInResponseRapidPollTest extends SendFinInRapidPollTest {
    private static final String TAG = CloseInResponseRapidPollTest.class.getSimpleName();
    private String mExpectedBody;

    public CloseInResponseRapidPollTest(ConnSelector selector, String expectedBody, long pollPeriod) {
        this(TAG, selector, "asimov_it_close_response_rapid_poll", expectedBody, pollPeriod);
    }

    public CloseInResponseRapidPollTest(String name, ConnSelector selector, String testPath, String expectedBody,
                                        long pollPeriod) {
        super(name, selector, testPath, expectedBody, pollPeriod);
        mExpectedBody = expectedBody;

        // In this case, host maybe closed OC<->Host connection when app received response,
        // so, we did not check new socket
        this.setExpectedHostNewSocketCount(-1);
        this.setClientExpectedRstFinCount(MAX_REQUEST_ID - 1);
    }

    @Override
    protected HttpRequest prepareRequest() {
        HttpRequest request = super.prepareRequest();
        addExpectedResponse(request);

        return request;
    }

    @Override
    protected void onResponseHandled(HttpClient client) {
        // Do nothing
    }

    @Override
    protected void onRstHandled(HttpClient client) {
        // Only setup new connection and send request after receive Rst
        // because OC does not treat response is complete, if "connection" field is set to close in response header,
        // even though it gets the body as "content-length" limit. Refer to
        // This is caused by the fix for ASMV-3687.

        // So, in order to ensure OC can cache response in the third time, we do next connect in here, NOT
        // onResponseHandledl

        nextConnect();
    }

    public boolean verifyClientExpectedRstFin(int clientReceivedRstCount, int clientReceivedFinCount) {
        return getClientExpectedRstFinCount() < 0
                || (clientReceivedRstCount + clientReceivedFinCount) == getClientExpectedRstFinCount();
    }

    @Override
    public void onRstReceived(SelectableChannel channel) {
        super.onRstReceived(channel);
    }

    @Override
    protected boolean checkIfFinished(String where) {
        if (mRequestId < MAX_REQUEST_ID) {
            return false;
        }

        if ((getExpectedResponseCount() < 0 || mRecevicedResponseCount == getExpectedResponseCount())
                && (getExpectedHostNewSocketCount() < 0 || mSocketsMonitor.getTotalNewHostSockets().size() == getExpectedHostNewSocketCount())
                && verifyClientExpectedRstFin(mClientReceivedRstCount, mClientReceivedFinCount)
                // Currently app<->OC socket always keep in long/rapid poll, even though inactivity timeout.
                // So, it is enough that only checking if there is OC<->host sockets leak
                && mSocketsMonitor.getCurrentLeakedHostSockets().isEmpty()) {

            ConnLogger.debug(TAG, "Test passed in " + where);
            this.pass();
            return true;
        }

        return false;
    }

    private void addExpectedResponse(HttpRequest request) {
        String expectedResponse = "HTTP/1.1 200 OK" + TFConstantsIF.CRLF + TFConstantsIF.HEADER_CONNECTION + ": "
                + TFConstantsIF.HEADER_CONNECTION_CLOSE + TFConstantsIF.CRLF + "Content-Length: " + mExpectedBody.length()
                + TFConstantsIF.CRLF + TFConstantsIF.CRLF + mExpectedBody;
        String encodedResponse = URLEncoder.encode(Base64.encodeToString(expectedResponse.getBytes(), Base64.DEFAULT));

        request.addHeaderField(new HttpHeaderField("X-OC-Raw:", encodedResponse));
    }

}
