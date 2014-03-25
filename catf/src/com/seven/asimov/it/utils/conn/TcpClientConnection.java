package com.seven.asimov.it.utils.conn;

import com.seven.asimov.it.utils.conn.ConnEvent.ActionType;
import com.seven.asimov.it.utils.conn.ConnEvent.EventType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * The <code>TcpClientConnection</code> instance represents a client TCP connection.
 * It comes from TcpClient or TcpServer and can be used to read and write data.
 *
 * @author Daniel Xie (dxie@seven.com)
 */

public class TcpClientConnection extends TcpConnection {
    private static final String TAG = "TcpClientConnection";

    /**
     * Construct a TcpClientConnection
     *
     * @param selector
     * @param loop
     * @throws IOException
     */
    public TcpClientConnection(ConnSelector selector) throws IOException {
        super(selector);

        // open a new client channel
        mChannel = SocketChannel.open();
        mChannel.configureBlocking(false);
    }

    /**
     * Calls to connect out to a remote address
     *
     * @param socketAddress
     * @throws IOException
     */
    protected void connect(InetSocketAddress socketAddress) throws IOException {
        if (mConnType != ConnType.CLIENT) {
            throw new IOException("connect() on non-client channel");
        } else {
            if (mChannel.connect(socketAddress)) {
                // connected immediately
                ConnLogger.error(TAG, "connected immediately");
                mSelector.post(EventType.TCP_CONNECTED, mChannel, null, this);
            } else {

                mState = ConnState.CONNECTING;
                // register OP_CONNECT and wait for it to be connected
                mSelector.register(mChannel, SelectionKey.OP_CONNECT, this);
            }
        }
    }

    /**
     * IConnListener interface
     */

    @Override
    public void onConnected(SelectableChannel channel) {
//    	ConnLogger.debug(TAG, "TcpClientConnection::onConnected " + channel.toString());
        try {
            mState = ConnState.CONNECTED;
            mSelector.register(mChannel, SelectionKey.OP_READ, this);
            if (mListener != null) {
                mListener.onConnected(mChannel);
            }
        } catch (IOException e) {
            mSelector.post(EventType.SOCKET_EXCEPTION, new ConnEventData(channel, e, this, ActionType.TCP_CONNECT));
        }
    }

    @Override
    public void onAccepted(ServerSocketChannel serverChannel, SocketChannel newChannel) {
        // it's a client connection, do nothing
    }
}