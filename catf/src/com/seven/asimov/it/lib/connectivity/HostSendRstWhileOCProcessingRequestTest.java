package com.seven.asimov.it.lib.connectivity;

import android.util.Log;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.utils.conn.ConnLogger;
import com.seven.asimov.it.utils.conn.ConnSelector;
import com.seven.asimov.it.utils.conn.HttpServer;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;

import static com.seven.asimov.it.utils.conn.ConnTestResult.StateCode.SEND_FAILED;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnType.CLIENT;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnType.SERVER;

/**
 * <p>
 * Pre-requisite: OC startups and works fine.
 * </p>
 * Steps:
 * <ul>
 * <li>1. App setup connection with OC, then App sends request to OC.
 * <li>2. Host sends FIN while OC is processing the next request.
 * <li>3. Check connection in logs."
 * </ul>
 * Expected Result:
 * <ul>
 * <li>1.OC setup client connection after receive the 1st request data.
 * <li>2.OC forwards the 1st request to Network side.
 * <li>3.OC forwards the 1st response to App side.
 * <li>4.OC receives the 2nd request and Rst from Network side.
 * <li>5.OC forwards the 2nd request to the Network side over new OUT socket.</li>
 * <li>6.The App receives second response without errors.</li>
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
 * <li>8. XX : CLIENT : DATA_SENT</li>
 * <li>9. XX : SERVER : RST_SENT</li>
 * <li>10. XX : SERVER : ACCEPTED</li>
 * <li>11. XX : SERVER : DATA_RECEIVED</li>
 * <li>12. XX : SERVER : DATA_SENT</li>
 * <li>13. XX : CLIENT : DATA_RECEIVED</li>
 */
public class HostSendRstWhileOCProcessingRequestTest extends SimpleTest {
    private static final String TAG = HostSendRstWhileOCProcessingRequestTest.class.getSimpleName();

    private boolean isServerRstSent, isClientSent2ndRequest;
    private int port1, port2;

    public HostSendRstWhileOCProcessingRequestTest(ConnSelector selector, HttpServer server) {
        super(TAG, selector, server);
    }

    @Override
    public void onRequestReceived(int connId, HttpRequest request) {
        try {
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
        if (isServerRstSent && port1 > 0 && port2 > 0 && port1 != port2) { //server sent reset and new socket was made
            this.pass();
        } else {
            isClientSent2ndRequest = true;
            try {
                HttpRequest request = buildSimpleRequest();
                mClient.sendRequest(request);
                ConnLogger.debug(TAG, getName() + ": client sends 2nd request:\r\n" + request.getFullRequest());

                mServer.sendRst(connId);
                ConnLogger.debug(TAG, getName() + ": server sends RST");
                isServerRstSent = true;
            } catch (IOException e) {
                ConnLogger.error(TAG, getName() + ": Send request fail", e);
                this.fail(mClient.getConnId(), CLIENT, SEND_FAILED);
            }
        }
    }

    @Override
    public void onDataSent(SelectableChannel channel) {
        super.onDataSent(channel);

        if (!mClient.getChannel().equals(channel)) {
            if (!isServerRstSent) {
                port1 = ((SocketChannel) channel).socket().getPort();
            } else {
                port2 = ((SocketChannel) channel).socket().getPort();
            }
        }
        Log.v(TAG, "port1=" + port1 + " port2=" + port2);
    }

    @Override
    protected void onDone(Status status) {
        try {
            ConnLogger.debug(TAG, getName() + ": Close client");
            mClient.close();
        } catch (IOException e) {
            Log.w(TAG, ExceptionUtils.getStackTrace(e));
        }
        if (status == Status.TIMEDOUT) {
            if (!isClientSent2ndRequest) {
                this.fail("client didn't sent 2nd request");
            }
        }
    }
}
