package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.base.HttpHeaderField;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.utils.conn.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;

import static com.seven.asimov.it.utils.conn.ConnTestResult.StateCode;
import static com.seven.asimov.it.utils.conn.ConnTestResult.StateCode.SEND_FAILED;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnType;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnType.SERVER;

/**
 * <p>
 * Pre-requisite: OC startups and works fine.
 * </p>
 * Steps:
 * <ul>
 * <li>1. App setup connection with OC, then App sends request to OC.</li>
 * <li>2. App sends FIN while OC receiving response data.</li>
 * <li>3. Check connection in logs.</li>
 * </ul>
 * Expected result:
 * <ul>
 * <li>1. OC should setup client connection after receive request data and send request to network side.</li>
 * <li>2. OC is receiving the response from Host while FIN is received from App.</li>
 * <li>3. OC forwards this FIN to Host.</li>
 * <li>4. OC sends RST to both side (Host and App) after FIN/ACK received from Host.</li>
 * </ul>
 * <ul>
 * <li>1. XX : CLIENT : CONNECTING
 * <li>2. XX : CLIENT : CONNECTED
 * <li>3. XX : CLIENT : DATA_SENT
 * <li>4. XX : SERVER : ACCEPTED
 * <li>5. XX : SERVER : DATA_RECEIVED
 * <li>6. XX : SERVER : DATA_SENT
 * <li>7. XX : CLIENT : FIN_SENT
 * <li>8. XX : SERVER : DATA_SENT
 * <li>--- record sequence may not same start ---</li>
 * <li>9. XX : CLIENT : DATA_RECEIVED
 * <li>a. XX : CLIENT : DATA_RECEIVED
 * <li>b. XX : CLIENT : RST_RECEIVED
 * <li>c. XX : SERVER : FIN_RECEIVED
 * <li>d. XX : SERVER : FIN_SENT
 * <li>--- record sequence may not same end ---</li>
 * <li>e. XX : CLIENT : DONE
 * </ul>
 */
public class SendRstWhileOCReceivingResponseTest extends SimpleTest {
    private static final String TAG = SendRstWhileOCReceivingResponseTest.class.getSimpleName();

    public SendRstWhileOCReceivingResponseTest(ConnSelector selector, HttpServer server) {
        super(TAG, selector, server);
    }

    private HttpResponse mResponse;
    private boolean mClientRstSent;
    private SocketChannel mChannel;

    @Override
    public void onRequestReceived(int connId, HttpRequest request) {
        isServerReceivedRequest = true;
        mResponse = buildSimpleResponse(request);
        mResponse.addHeaderField(new HttpHeaderField("Content-Length", "10"));
        mResponse.setBody("0123456789");

        TcpConnection conn = mServer.getConnection(connId);
        if (conn != null) {
            mChannel = conn.getChannel();
            // send header
            try {
                mServer.sendData(conn.getChannel(), getResponseHeader(mResponse));
            } catch (IOException e) {
                ConnLogger.error(TAG, getName() + ": server sends response header failed", e);
                this.fail(connId, SERVER, SEND_FAILED);
            }
        }
    }

    @Override
    public void onDataSent(SelectableChannel channel) {
        super.onDataSent(channel);
        if (!mClient.getChannel().equals(channel) && !mClientRstSent) {
            try {
                mClient.sendRst();
                ConnLogger.debug(TAG, getName() + ": client sends RST");
            } catch (IOException e) {
                ConnLogger.error(TAG, getName() + ": client sends RST failed", e);
                fail(mClient.getConnId(), ConnType.CLIENT, StateCode.SEND_FAILED);
            }
        }
    }

    @Override
    public void onRstSent(SelectableChannel channel) {
        super.onRstSent(channel);
        if (mClient.getChannel().equals(channel)) {
            mClientRstSent = true;
            if (mChannel != null) {
                // send body
                try {
                    mServer.sendData(mChannel, ConnUtils.stringToByteBuffer(mResponse.getBody()));
                    ConnLogger.debug(TAG, getName() + ": server sends response body");
                } catch (IOException e) {
                    ConnLogger.error(TAG, getName() + ": server sends response body failed", e);
                    fail(mClient.getConnId(), ConnType.CLIENT, StateCode.SEND_FAILED);
                }
            }
        }
    }

    @Override
    public void onRstReceived(SelectableChannel channel) {
        super.onRstReceived(channel);
        if (isServerReceivedRst) {
            this.pass();
        }
    }

    @Override
    public void onResponseReceived(int connId, HttpResponse response) {
    }

    @Override
    public void onDone(Status status) {
        try {
            ConnLogger.debug(TAG, getName() + ": Close client");
            mClient.close();
        } catch (IOException e) {
            // ignore
        }
        if (status == Status.TIMEDOUT) {
            pass();
        }
    }

    private ByteBuffer getResponseHeader(HttpResponse response) {
        StringBuilder sb = new StringBuilder().append(response.getStatusLine()).append("\r\n");

        for (HttpHeaderField field : response.getHeaderFields()) {
            sb.append(field.getName()).append(": ").append(field.getValue()).append("\r\n");
        }

        sb.append("\r\n");

        return ConnUtils.stringToByteBuffer(sb.toString());
    }
}
