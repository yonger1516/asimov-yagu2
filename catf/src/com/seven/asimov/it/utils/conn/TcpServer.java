package com.seven.asimov.it.utils.conn;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;

public class TcpServer {
    private static final String TAG = "TcpServer";

    private TcpServerConnection mServerConnection;

    public TcpServer(ConnSelector selector) {
        mServerConnection = new TcpServerConnection(selector);
    }

    public TcpServerConnection getServerConnection() {
        return mServerConnection;
    }

    public TcpConnection getConnection(SelectableChannel channel) {
        return mServerConnection.getConnection(channel);
    }

    public void setListener(IConnListener listener) {
        mServerConnection.setListener(listener);
    }

    public InetSocketAddress getCurrentAddress() {
        return mServerConnection.getServerAddress();
    }

    public void listen(InetAddress address, int port) throws IOException {
        mServerConnection.listen(address, port);
    }

    public void stop() throws IOException {
        //TODO: close all accepted sockets as well as the server socket channel
        ConnLogger.debug(TAG, "TCPServer stop");
        mServerConnection.close();
    }

    /**
     * Called to send data
     *
     * @param buffer
     * @throws IOException
     */
    public void sendData(SocketChannel channel, ByteBuffer buffer) throws IOException {
        TcpConnection connection = mServerConnection.getConnection(channel);
        if (connection != null) {
            connection.sendData(buffer);
        }
    }

    /**
     * call to close the accepted channel
     *
     * @param channel
     * @throws IOException
     */
    public void close(SocketChannel channel) throws IOException {
        TcpConnection connection = mServerConnection.getConnection(channel);
        if (connection != null) {
            connection.close();
        }
    }

    /**
     * Called to send Fin
     *
     * @throws IOException
     */
    public void sendFin(SocketChannel channel) throws IOException {
        TcpConnection connection = mServerConnection.getConnection(channel);
        if (connection != null) {
            connection.sendFin();
        }
    }

    /**
     * Called to send Rst
     *
     * @throws IOException
     */
    public void sendRst(SocketChannel channel) throws IOException {
        TcpConnection connection = mServerConnection.getConnection(channel);
        if (connection != null) {
            connection.sendRst();
        }
    }
}