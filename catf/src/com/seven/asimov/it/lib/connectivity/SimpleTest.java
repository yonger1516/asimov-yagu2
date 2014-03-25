package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.conn.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import static com.seven.asimov.it.utils.conn.ConnTestResult.StateCode;
import static com.seven.asimov.it.utils.conn.ConnTestResult.StateCode.*;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnType;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnType.CLIENT;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnType.SERVER;

/**
 * <p>
 * Pre-requisite: OC startups and works fine.
 * </p>
 * Steps:
 * <ul>
 * <li>1. Launch http request and the content is not served from cache.</li>
 * <li>2. Check connection for the request in logs.</li>
 * </ul>
 * Expected Result:
 * <ul>
 * <li>1.Should setup server connection and client connection successfully between app and server as OC is a proxy.</li>
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
 * <li>8. XX : CLIENT : FIN_SENT</li>
 * <li>9. XX : SERVER : FIN_RECEIVED</li>
 * <li>a. XX : SERVER : FIN_SENT</li>
 * <li>b. XX : CLIENT : RST_RECEIVED</li>
 * <li>c. XX : CLIENT : DONE</li>
 * </ul>
 */
public class SimpleTest extends AbstractConnTest implements IConnListener {
    private static final String TAG = SimpleTest.class.getSimpleName();

    private ConnSelector mSelector;
    protected HttpClient mClient;
    public HttpServer mServer;

    protected boolean isServerReceivedFin, isClientReceivedFin;
    protected boolean isServerReceivedRst, isClientReceivedRst;
    protected boolean isServerReceivedRequest, isClientReceivedResponse;

    public SimpleTest(ConnSelector selector, HttpServer server) {
        this(TAG, selector, server);
    }

    protected SimpleTest(String name, ConnSelector selector, HttpServer server) {
        super(name);
        mSelector = selector;
        mServer = server;
    }

    protected HttpRequest buildSimpleRequest(String Uri, boolean keepAlive) {
        return buildSimpleRequest(Uri, mServer.getCurrentAddress().getAddress().getHostAddress() + ":"
                + mServer.getCurrentAddress().getPort(), keepAlive);
    }

    protected HttpRequest buildSimpleRequest(String Uri) {
        return buildSimpleRequest(Uri, true);
    }

    protected HttpRequest buildSimpleRequest() {
        return buildSimpleRequest(null);
    }

    @Override
    protected boolean init() {
        try {
            mClient = new HttpClient(mSelector);
        } catch (IOException e1) {
            ConnLogger.error(TAG, "Failed to create HttpClient", e1);
            return false;
        }

        mClient.setListener(this);
        mServer.setListener(this);

        try {
            addResult(mClient.getConnId(), CLIENT, CONNECTING);
            mClient.connect(TFConstantsIF.OC_HTTP_PROXY_ADDRESS, TFConstantsIF.OC_HTTP_PROXY_PORT);
        } catch (IOException e) {
            ConnLogger.error(TAG, getName() + ": failed to connect tcp server on port:" + TFConstantsIF.OC_HTTP_PROXY_PORT,
                    e);
            this.fail(ConnTestResult.CONN_ID_NEW, CLIENT, CONNECT_FAILED);
            return false;
        }

        return true;
    }

    @Override
    protected void onDone(Status status) {
        super.onDone(status);

        try {
            ConnLogger.debug(TAG, getName() + ": Close client");
            mClient.close();
        } catch (IOException e) {
            // ignore
        }

        if (status == Status.TIMEDOUT) {
            if (!isServerReceivedRequest) {
                this.fail("server didn't receive request");
            } else if (!isClientReceivedResponse) {
                this.fail("client didn't receive response");
            } else

                // if (!isServerReceivedRst) {
                // this.fail("server didn't receive RST - may cause socket leak");
                // } else
                if (!isClientReceivedFin) {
                    this.fail("client didn't receive FIN - may cause socket leak");
                }
        }
    }

    @Override
    public void onConnected(SelectableChannel channel) {
        ConnLogger.debug(TAG, getName() + ": client connected");
        try {
            this.addResult(mClient.getConnId(), CLIENT, CONNECTED);
            HttpRequest simpleRequest = buildSimpleRequest();
            mClient.sendRequest(simpleRequest);
            ConnLogger.debug(TAG, getName() + ": client sends request:\r\n" + simpleRequest.getFullRequest());
        } catch (IOException e) {
            ConnLogger.error(TAG, getName() + ": Send data fail", e);
            this.fail(mClient.getConnId(), CLIENT, SEND_FAILED);
        }
    }

    @Override
    public void onDataReceived(SelectableChannel channel, ByteBuffer byteBuffer) {
        if (mClient.getChannel().equals(channel)) {
            ConnLogger.debug(TAG,
                    getName() + ": client received response:\r\n" + ConnUtils.ByteBufferToString(byteBuffer));
            this.addResult(mClient.getConnId(), CLIENT, DATA_RECEIVED);

            mClient.onDataReceived(byteBuffer);
        } else {
            TcpConnection conn = mServer.getConnection(channel);
            if (conn != null) {
                ConnLogger.debug(TAG,
                        getName() + ": server received request:\r\n" + ConnUtils.ByteBufferToString(byteBuffer));
                this.addResult(conn.getConnId(), SERVER, DATA_RECEIVED);

                mServer.onDataReceived(channel, byteBuffer);
            }
        }
    }

    @Override
    public void onFinReceived(SelectableChannel channel) {
        if (mClient.getChannel().equals(channel)) {
            // mark client received fin
            isClientReceivedFin = true;
            ConnLogger.debug(TAG, getName() + ": client received fin");
            this.addResult(mClient.getConnId(), CLIENT, FIN_RECEIVED);
        } else {
            // mark server received fin
            isServerReceivedFin = true;
            TcpConnection conn = mServer.getConnection(channel);
            ConnLogger.debug(TAG, getName() + ": server received fin");
            this.addResult(mServer.getConnId(channel), SERVER, FIN_RECEIVED);

            // send fin
            try {
                ConnLogger.debug(TAG, getName() + ": server sends fin");
                mServer.sendFin((SocketChannel) channel);
            } catch (IOException e) {
                ConnLogger.error(TAG, getName() + ": Send FIN fail", e);
                this.fail(conn.getConnId(), SERVER, SEND_FAILED);
            }
        }

        // Changed in case of OC closes socket with server by RST flag but not FIN
        // and OC closes socket with client by FIN flag but not RST
        // if (isServerReceivedRequest && isClientReceivedResponse && isServerReceivedFin && isClientReceivedRst) {
        if (isServerReceivedRequest && isClientReceivedResponse && isClientReceivedFin) {
            this.pass();
        }
    }

    @Override
    public void onAccepted(ServerSocketChannel serverChannel, SocketChannel channel) {
        ConnLogger.debug(TAG, getName() + "  onAccepted, local port:" + ((SocketChannel) channel).socket().getPort());
        // Can't get connId in accept
        this.addResult(ConnTestResult.CONN_ID_NEW, SERVER, ACCEPTED);
    }

    @Override
    public void onWritable(SelectableChannel channel) {
        ConnLogger.debug(TAG, getName() + "  onWritable, channel:" + channel);
    }

    @Override
    public void onReadable(SelectableChannel channel) {
        ConnLogger.debug(TAG, getName() + "  onReadable, channel:" + channel);
    }

    @Override
    public void onDataSent(SelectableChannel channel) {
        // ConnLogger.debug(TAG, getName() + "  onDataSent, channel:"+ channel);
        addResult(channel, StateCode.DATA_SENT);
    }

    @Override
    public void onFinSent(SelectableChannel channel) {
        addResult(channel, StateCode.FIN_SENT);
    }

    @Override
    public void onRstSent(SelectableChannel channel) {
        addResult(channel, StateCode.RST_SENT);
    }

    @Override
    public void onSocketException(SelectableChannel channel, IOException e, ConnEvent.ActionType type) {
        if (TFConstantsIF.SOCKET_EXCEPTION_RST_MESSAGE.equals(e.getMessage())) {
            // RST received
            onRstReceived(channel);
        } else {
            ConnLogger.debug(TAG, getName() + " isClient:" + (mClient.getChannel().equals(channel)) + ", type:" + type,
                    e);
        }
    }

    @Override
    public void onRstReceived(SelectableChannel channel) {
        addResult(channel, StateCode.RST_RECEIVED);
        if (mClient.getChannel().equals(channel)) {
            isClientReceivedRst = true;
            ConnLogger.debug(TAG, getName() + ": client received rst");
        } else {
            isServerReceivedRst = true;
            ConnLogger.debug(TAG, getName() + ": server received rst");
        }

        if (isServerReceivedRequest && isClientReceivedResponse && isServerReceivedRst && isClientReceivedFin) {
            this.pass();
        }
    }

    protected void addResult(SelectableChannel channel, StateCode resultCode) {
        ConnType connType = ConnType.CLIENT;
        int connId = ConnTestResult.CONN_ID_NEW;

        if (mClient.getChannel().equals(channel)) {
            connId = mClient.getConnId();
        } else {
            connType = ConnType.SERVER;
            TcpConnection conn = mServer.getConnection(channel);
            if (conn != null) {
                connId = conn.getConnId();
            }
        }

        this.addResult(connId, connType, resultCode);
    }

    @Override
    public void onResponseReceived(int connId, HttpResponse response) {
        isClientReceivedResponse = true;
        try {
            ConnLogger.debug(TAG, getName() + ": client sends fin");
            mClient.sendFin();
        } catch (IOException e) {
            ConnLogger.error(TAG, getName() + ": Send FIN fail", e);
            this.fail(mClient.getConnId(), ConnType.CLIENT, StateCode.SEND_FAILED);
        }
    }

    @Override
    public void onRequestReceived(int connId, HttpRequest request) {
        isServerReceivedRequest = true;
        try {
            HttpResponse response = buildSimpleResponse(request);
            mServer.sendResponse(connId, response);
        } catch (IOException e) {
            ConnLogger.error(TAG, getName() + ": Send data fail", e);
            this.fail(connId, ConnType.SERVER, StateCode.SEND_FAILED);
        }
    }

    @Override
    public void onHttpException(int connId, ConnType type, Exception e) {
        // TODO Auto-generated method stub

    }
}
