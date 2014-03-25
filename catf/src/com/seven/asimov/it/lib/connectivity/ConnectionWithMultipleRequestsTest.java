package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.utils.conn.ConnLogger;
import com.seven.asimov.it.utils.conn.ConnSelector;
import com.seven.asimov.it.utils.conn.HttpServer;

import java.io.IOException;
import java.nio.channels.SelectableChannel;

import static com.seven.asimov.it.utils.conn.ConnTestResult.StateCode.CONNECTED;
import static com.seven.asimov.it.utils.conn.ConnTestResult.StateCode.SEND_FAILED;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnType.CLIENT;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnType.SERVER;

/**
 * "Pre-requisite: OC startups and works fine.
 * Steps:
 * 1. App setup connection with OC, then App sends request to OC.
 * 2. Host sends FIN while OC is processing the next request.
 * 3. Check connection in logs."
 * <p/>
 * Expected result:
 * 1. OC setup OC<>Host connection after receive the 1st request data.
 * 2. OC forwards the 1st request to Host.
 * 3. OC forwards the 1st response to App.
 * 4. OC receives the next 2 requests and forwards them to Host.
 * 5. OC receives the next 2 responses and forwards them to App.
 * 6. App sends FIN to OC and OC forwards this Fin to Host.
 * 7. OC receives FIN from Host and reset OC<>Host connection.
 * 8. OC reset App<>OC connection.
 */
public class ConnectionWithMultipleRequestsTest extends SimpleTest {
    private static final String TAG = ConnectionWithMultipleRequestsTest.class.getSimpleName();

    public ConnectionWithMultipleRequestsTest(ConnSelector selector, HttpServer server) {
        super(TAG, selector, server);
    }

    @Override
    public void onConnected(SelectableChannel channel) {
        if (mClient.getChannel().equals(channel)) {
            ConnLogger.debug(TAG, getName() + ": client connected");
            this.addResult(mClient.getConnId(), CLIENT, CONNECTED);
            try {
                HttpRequest request = buildSimpleRequest();
                mClient.sendRequest(request);
                ConnLogger.debug(TAG, getName() + ": client sends 1st request:\r\n" + request.getFullRequest());
            } catch (IOException e) {
                ConnLogger.error(TAG, getName() + ": Send data fail", e);
                this.fail(mClient.getConnId(), CLIENT, SEND_FAILED);
            }
        }
    }

    @Override
    public void onRequestReceived(int connId, HttpRequest request) {
        try {
            isServerReceivedRequest = true;

            HttpResponse response = buildSimpleResponse(request);
            mServer.sendResponse(connId, response);
            ConnLogger.debug(TAG, getName() + ": server sends response:\r\n" + response.getFullResponse());
        } catch (IOException e) {
            ConnLogger.error(TAG, getName() + ": Send response fail", e);
            this.fail(mClient.getConnId(), SERVER, SEND_FAILED);
        }
    }

    @Override
    public void onResponseReceived(int connId, HttpResponse response) {
        // received the first response, send 2nd, 3rd requests together
        if (mClient.getConnId() == connId) {
            if (mClient.getRequestId() == 2) {
                try {
                    HttpRequest request = buildSimpleRequest();
                    mClient.sendRequest(request);
                    ConnLogger.debug(TAG, getName() + ": client sends 2nd request:\r\n" + request.getFullRequest());
                    request = buildSimpleRequest();
                    mClient.sendRequest(request);
                    ConnLogger.debug(TAG, getName() + ": client sends 3rd request:\r\n" + request.getFullRequest());
                } catch (IOException e) {
                    ConnLogger.error(TAG, getName() + ": Send requests failed", e);
                    this.fail(mClient.getConnId(), CLIENT, SEND_FAILED);
                }
            } else if (mClient.getRequestId() == 4) {
                try {
                    isClientReceivedResponse = true;
                    mClient.sendFin();
                    ConnLogger.debug(TAG, getName() + ": client sends FIN");
                } catch (IOException e) {
                    ConnLogger.error(TAG, getName() + ": Send FIN fail", e);
                    this.fail(mClient.getConnId(), CLIENT, SEND_FAILED);
                }
            }
        }
    }

    @Override
    public void onRstReceived(SelectableChannel channel) {
        super.onRstReceived(channel);
    }

}
