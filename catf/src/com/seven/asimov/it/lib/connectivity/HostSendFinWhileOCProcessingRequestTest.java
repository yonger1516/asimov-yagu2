package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.utils.conn.ConnLogger;
import com.seven.asimov.it.utils.conn.ConnSelector;
import com.seven.asimov.it.utils.conn.HttpServer;
import com.seven.asimov.it.utils.conn.TcpConnection;

import java.io.IOException;
import java.nio.channels.SelectableChannel;

import static com.seven.asimov.it.utils.conn.ConnTestResult.StateCode.SEND_FAILED;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnType.CLIENT;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnType.SERVER;

/**
 * <p>
 * Pre-requisite: OC startups and works fine.
 * </p>
 * Steps:
 * <ul>
 * <li>1. App setup connection with OC, then App sends request to OC.</li>
 * <li>2. Host sends FIN while OC is processing the next request.</li>
 * <li>3. Check connection in logs.</li>
 * </ul>
 * Expected Result:
 * <ul>
 * 1. OC setup client connection after receive the 1st request data.</li>
 * 2. OC forwards the 1st request to Network side.</li>
 * 3. OC forwards the 1st response to App side.</li>
 * 4. OC receives the 2nd request and FIN from Network side.</li>
 * 5. OC reset both App and Network side.</li>
 * </ul>
 * Expected Test Records:
 * <ul>
 * <li>1. XX : CLIENT : CONNECTING</li>
 * <li>2. XX : CLIENT : CONNECTED</li>
 * <li>3. XX : CLIENT : DATA_SENT</li>
 * <li>4. XX : SERVER : ACCEPTED</li>
 * <li>5. XX : SERVER : DATA_RECEIVED</li>
 * <li>6. XX : SERVER : DATA_SENT</li>
 * <li>7. XX : CLIENT : DATA_RECEIVED</li>
 * <li>8. XX : SERVER : FIN_SENT</li>
 * <li>9. XX : SERVER : DATA_RECEIVED</li>
 * <li>a. XX : SERVER : RST_RECEIVED</li>
 * <li>b. XX : CLIENT : RST_RECEIVED</li>
 * <li>c. XX : CLIENT : DONE</li>
 * </ul>
 */
public class HostSendFinWhileOCProcessingRequestTest extends SimpleTest {
    private static final String TAG = HostSendFinWhileOCProcessingRequestTest.class.getSimpleName();
    private boolean isServerFinSent;

    public HostSendFinWhileOCProcessingRequestTest(ConnSelector selector, HttpServer server) {
        super(TAG, selector, server);
    }

    @Override
    public void onRstReceived(SelectableChannel channel) {
        super.onRstReceived(channel);
        if (isClientReceivedRst && isServerReceivedRst) {
            this.pass();
        }
    }

    @Override
    public void onRequestReceived(int connId, HttpRequest request) {
        if (!isServerFinSent) {
            try {
                HttpResponse response = buildSimpleResponse(request);
                mServer.sendResponse(connId, response);
                ConnLogger.debug(TAG, getName() + ": server sends response:\r\n" + response.getFullResponse());
            } catch (IOException e) {
                ConnLogger.error(TAG, getName() + ": Send response fail", e);
                this.fail(mClient.getConnId(), SERVER, SEND_FAILED);
            }
        }
    }

    @Override
    public void onResponseReceived(int connId, HttpResponse response) {
        if (!isServerFinSent) {
            try {
                HttpRequest request = buildSimpleRequest();
                mClient.sendRequest(request);
                ConnLogger.debug(TAG, getName() + ": client sends 2nd request:\r\n" + request.getFullRequest());
            } catch (IOException e) {
                ConnLogger.error(TAG, getName() + ": Send request fail", e);
                this.fail(mClient.getConnId(), CLIENT, SEND_FAILED);
            }
        }
    }

    @Override
    public void onDataSent(SelectableChannel channel) {
        super.onDataSent(channel);

        if (mClient.getChannel().equals(channel)
                && mClient.hasRequestPending()
                && mClient.getRequestId() == 2) {
            try {
                int connId = mClient.getConnId();
                TcpConnection conn = mServer.getConnection(connId);
                ConnLogger.debug(TAG, getName() + ": server sends FIN");
                mServer.sendFin(conn.getChannel());
                isServerFinSent = true;
            } catch (IOException e) {
                ConnLogger.error(TAG, getName() + ": Send FIN fail", e);
                this.fail(mClient.getConnId(), CLIENT, SEND_FAILED);
            }
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
            if (!isClientReceivedRst) {
                this.fail("client didn't receive RST - may cause socket leak");
            } else if (!isServerReceivedRst) {
                this.fail("server didn't receive RST - may cause socket leak");
            }
        }
    }
}
