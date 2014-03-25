package com.seven.asimov.it.utils.conn;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.utils.conn.TcpConnection.ConnType;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

public class HttpServer extends TcpServer {
    private static final String TAG = "HttpServer";

    private HashMap<Integer, TcpConnection> mHttpConnections;

    public HttpServer(ConnSelector selector) {
        super(selector);
        mHttpConnections = new HashMap<Integer, TcpConnection>();
    }

    public TcpConnection getConnection(int connId) {
        return mHttpConnections.get(connId);
    }

    public int getConnId(SelectableChannel channel) {
        TcpConnection conn = getConnection(channel);
        if (conn != null) {
            return conn.getConnId();
        }
        return -1;
    }

    public void close(int connId) throws IOException {
        TcpConnection connection = mHttpConnections.get(connId);
        if (connection != null) {
            connection.close();
        }
    }

    public void sendRst(int connId) throws IOException {
        TcpConnection connection = mHttpConnections.get(connId);
        if (connection != null) {
            connection.sendRst();
        }
    }

    public void sendFin(int connId) throws IOException {
        TcpConnection connection = mHttpConnections.get(connId);
        if (connection != null) {
            connection.sendFin();
        }
    }

    public void sendResponse(int connId, HttpResponse response) throws IOException {
        // get connection by connId
        TcpConnection conn = mHttpConnections.get(connId);
        if (conn == null) {
            ConnLogger.warn(TAG, "sendResponse() - connection doesn't exist");
            return;
        }

        // get channel
        SocketChannel channel = conn.getChannel();
        if (channel == null) {
            conn.onHttpException(connId, ConnType.SERVER, new Exception("channel doesn't exist"));
            return;
        }
        super.sendData(channel, ConnUtils.stringToByteBuffer(response.getFullResponseWithoutCLRF()));
    }

    public void onDataReceived(SelectableChannel channel, ByteBuffer buffer) {
        // get connection by channel
        TcpConnection conn = super.getConnection(channel);
        if (conn == null) {
            ConnLogger.warn(TAG, "onDataReceived() - connection doesn't exist");
            return;
        }

        ByteBuffer data = conn.getReceiveBuffer();
        data.mark();
        data.put(buffer).reset();

        HttpRequest request = ConnUtils.parseHttpRequest(data);
        while (request != null) {
            String strConnId = request.getHeaderField(HttpClient.OC_HEADER_FIELD_CONN_ID);
            if (strConnId == null) {
                ConnLogger.warn(TAG, "onDataReceived() - no conn id");
                break;
            } else {
                int connId = conn.getConnId();
                if (connId == ConnTestResult.CONN_ID_NEW) {
                    // get and set connId
                    connId = Integer.parseInt(strConnId);
                    conn.setConnId(connId);
                    // add it into mHttpConnections
                    mHttpConnections.put(connId, conn);
                }
                conn.onRequestReceived(connId, request);
                request = ConnUtils.parseHttpRequest(data);
            }
        }
    }
}