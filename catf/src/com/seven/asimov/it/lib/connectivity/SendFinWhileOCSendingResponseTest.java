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
import static com.seven.asimov.it.utils.conn.ConnTestResult.StateCode.DATA_RECEIVED;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnType;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnType.CLIENT;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnType.SERVER;

/**
 * <p>
 * Pre-requisite: OC startups and works fine.
 * </p>
 * Steps:
 * <ul>
 * <li>1. App setup connection with OC, then App sends request to OC.</li>
 * <li>2. App sends FIN while OC is sending response data.</li>
 * <li>3. Check connection in logs.</li>
 * </ul>
 * Expected result:
 * <ul>
 * <li>1. OC should setup client connection after receive request data and send request to network side.</li>
 * <li>2. OC is receiving the response from Host and forwarding it to App while FIN is received from App.</li>
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
 * <li>7. XX : CLIENT : DATA_RECEIVED
 * <li>8. XX : CLIENT : FIN_SENT
 * <li>--- record sequence may not same start ---</li>
 * <li>9. XX : SERVER : DATA_SENT
 * <li>a. XX : CLIENT : DATA_RECEIVED
 * <li>b. XX : CLIENT : RST_RECEIVED
 * <li>c. XX : SERVER : FIN_RECEIVED
 * <li>d. XX : SERVER : FIN_SENT
 * <li>--- record sequence may not same end ---</li>
 * <li>e. XX : CLIENT : DONE
 * </ul>
 */
public class SendFinWhileOCSendingResponseTest extends SimpleTest {
    private static final String TAG = SendFinWhileOCSendingResponseTest.class.getSimpleName();

    public SendFinWhileOCSendingResponseTest(ConnSelector selector, HttpServer server) {
        super(TAG, selector, server);
    }

    private HttpResponse mResponse;
    private boolean mClientFinSent;
    private SocketChannel mChannel;

    @Override
    public void onDataReceived(SelectableChannel channel, ByteBuffer byteBuffer) {
        if (mClient.getChannel().equals(channel)) {
            ConnLogger.debug(TAG, getName() + ": client received data:\r\n" + ConnUtils.ByteBufferToString(byteBuffer));
            this.addResult(mClient.getConnId(), CLIENT, DATA_RECEIVED);
            mClient.onDataReceived(byteBuffer);
            if (!mClientFinSent) {
                try {
                    mClient.sendFin();
                } catch (IOException e) {
                    ConnLogger.error(TAG, getName() + ": Send FIN failed", e);
                    fail(mClient.getConnId(), CLIENT, StateCode.SEND_FAILED);
                }
            }
        } else {
            TcpConnection conn = mServer.getConnection(channel);
            if (conn != null) {
                ConnLogger.debug(TAG, getName() + ": server received data:\r\n" + ConnUtils.ByteBufferToString(byteBuffer));
                this.addResult(conn.getConnId(), SERVER, StateCode.DATA_RECEIVED);
                mServer.onDataReceived(channel, byteBuffer);
            }
        }
    }

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
                ConnLogger.error(TAG, getName() + ": send response header failed", e);
                this.fail(connId, ConnType.SERVER, StateCode.SEND_FAILED);
            }
        }
    }

    @Override
    public void onFinSent(SelectableChannel channel) {
        super.onFinSent(channel);
        if (mClient.getChannel().equals(channel)) {
            mClientFinSent = true;
            if (mChannel != null) {
                // send body
                try {
                    mServer.sendData(mChannel, ConnUtils.stringToByteBuffer(mResponse.getBody()));
                    ConnLogger.debug(TAG, getName() + ": client sends body");
                } catch (IOException e) {
                    ConnLogger.error(TAG, getName() + ": send response body failed", e);
                    fail(mClient.getConnId(), ConnType.CLIENT, StateCode.SEND_FAILED);
                }
            }
        }
    }

    @Override
    public void onResponseReceived(int connId, HttpResponse response) {
        isClientReceivedResponse = true;
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
