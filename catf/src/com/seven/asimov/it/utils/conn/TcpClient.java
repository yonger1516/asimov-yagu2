package com.seven.asimov.it.utils.conn;

import com.seven.asimov.it.utils.conn.TcpConnection.ConnState;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * The <code>TcpClient</code> instance represents a TCP client which sends and
 * receives data through a TcpConnection.
 *
 * @author Daniel Xie (dxie@seven.com)
 */
public class TcpClient {
    private static final String TAG = "TcpClient";
    private static volatile int mNextConnId = 0;

    private TcpClientConnection mConnection;

    public TcpClient(ConnSelector selector) throws IOException {
        mConnection = new TcpClientConnection(selector);
        mConnection.setConnId(++mNextConnId);
    }

    public void setListener(IConnListener listener) {
        mConnection.setListener(listener);
    }

    public IConnListener getListener() {
        return mConnection.getListener();
    }

    public int getConnId() {
        return mConnection.getConnId();
    }

    public TcpConnection getConnection() {
        return mConnection;
    }

    public SocketChannel getChannel() {
        return mConnection.getChannel();
    }

    public ConnState getState() {
        return mConnection.getState();
    }

    /**
     * call to connect out with remote address and port
     *
     * @param address
     * @param port
     * @throws IOException
     */
    public void connect(String address, int port) throws IOException {
        connect(new InetSocketAddress(address, port));
    }

    /**
     * call to connect out with remote socket address
     *
     * @param socketAddress
     * @throws IOException
     */
    public void connect(InetSocketAddress socketAddress) throws IOException {
        mConnection.connect(socketAddress);
//        ConnLogger.debug(TAG, "TcpClient: connecting to " + socketAddress);
    }

    /**
     * Called to send data
     *
     * @param buffer
     * @throws IOException
     */
    public void sendData(ByteBuffer buffer) throws IOException {
        mConnection.sendData(buffer);
    }

    /**
     * call to close the client
     *
     * @throws IOException
     */
    public void close() throws IOException {
        mConnection.close();
    }

    /**
     * Called to send Fin
     *
     * @throws IOException
     */
    public void sendFin() throws IOException {
        mConnection.sendFin();
    }

    /**
     * Called to send Rst
     *
     * @throws IOException
     */
    public void sendRst() throws IOException {
        mConnection.sendRst();
    }
}