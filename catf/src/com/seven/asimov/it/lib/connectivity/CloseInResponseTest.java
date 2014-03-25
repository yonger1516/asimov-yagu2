package com.seven.asimov.it.lib.connectivity;


import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.conn.ConnLogger;
import com.seven.asimov.it.utils.conn.ConnSelector;
import com.seven.asimov.it.utils.conn.HttpServer;

import java.io.IOException;
import java.nio.channels.SelectableChannel;

import static com.seven.asimov.it.utils.conn.ConnTestResult.StateCode.FIN_SENT;
import static com.seven.asimov.it.utils.conn.ConnTestResult.StateCode.SEND_FAILED;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnType.SERVER;

/**
 * <p>
 * Pre-requisite: OC startups and works fine.
 * </p>
 * Steps:
 * <ul>
 * <li>1. App setup connection with OC and then sends a request to OC.</li>
 * <li>2. Host receives this request and response with "CONNECTION: close" in the response header.</li>
 * <li>3. Host closes the connection after the response.</li>
 * <li>4. Check connection in logs.</li>
 * </ul>
 * Expected Result:
 * <ul>
 * <li>1. OC should setup client connection after receive the request data and then send the request to Host.</li>
 * <li>2. OC forwards the response to App. </li>
 * <li>3. OC sends RST to App. </li>
 * </ul>
 * Expected Test Records:
 * <ul>
 * <li>1. XX : CLIENT : CONNECTING</li>
 * <li>2. XX : CLIENT : CONNECTED</li>
 * <li>3. XX : CLIENT : DATA_SENT</li>
 * <li>4. XX : SERVER : ACCEPTED</li>
 * <li>5. XX : SERVER : DATA_RECEIVED</li>
 * <li>6. XX : SERVER : DATA_SENT</li>
 * <li>7. XX : SERVER : FIN_SENT</li>
 * <li>8. XX : CLIENT : DATA_RECEIVED</li>
 * <li>9. XX : CLIENT : RST_RECEIVED</li>
 * </ul>
 */

public class CloseInResponseTest extends SimpleTest {
    private static final String TAG = CloseInResponseTest.class.getSimpleName();
    private boolean hasConnectionCloseInResponse;

    public CloseInResponseTest(ConnSelector selector, HttpServer server) {
        super(TAG, selector, server);
    }

    @Override
    public void onResponseReceived(int connId, HttpResponse response) {
        isClientReceivedResponse = true;
        if (TFConstantsIF.HEADER_CONNECTION_CLOSE.equals(response.getHeaderField(TFConstantsIF.HEADER_CONNECTION))) {
            hasConnectionCloseInResponse = true;
        }
    }

    @Override
    public void onRequestReceived(int connId, HttpRequest request) {
        isServerReceivedRequest = true;

        try {
            //set close in response(keep alive ==false)
            HttpResponse response = buildSimpleResponse(request, false, true);
            mServer.sendResponse(connId, response);
        } catch (IOException e) {
            ConnLogger.error(TAG, getName() + ": server sends response failed", e);
            this.fail(connId, SERVER, SEND_FAILED);
        }
    }

    @Override
    public void onDataSent(SelectableChannel channel) {
        super.onDataSent(channel);
        int connId = mServer.getConnId(channel);
        if (connId != -1) {
            try {
                mServer.close(connId);
                addResult(connId, SERVER, FIN_SENT);
            } catch (IOException e) {
                ConnLogger.error(TAG, getName() + ": Close server socket failed", e);
                this.fail(connId, SERVER, FIN_SENT);
            }
        }
    }

    @Override
    public void onRstReceived(SelectableChannel channel) {
        super.onRstReceived(channel);
        if (isServerReceivedRequest && isClientReceivedResponse && hasConnectionCloseInResponse && isClientReceivedRst) {
            pass();
        }
    }

    @Override
    protected void onDone(Status status) {
        try {
            ConnLogger.debug(TAG, getName() + ": Close client");
            mClient.close();
        } catch (IOException e) {
            //ignore
        }

        if (status == Status.TIMEDOUT) {
            if (!isServerReceivedRequest) {
                this.fail("server didn't receive request");
            } else if (!isClientReceivedResponse) {
                this.fail("client didn't receive response");
            } else if (!hasConnectionCloseInResponse) {
                this.fail("response didn't contain CONNECTION: close");
            } else if (!isClientReceivedRst) {
                this.fail("client didn't receive RST - may cause socket leak");
            }
        }
    }
}
