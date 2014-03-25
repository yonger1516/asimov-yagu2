package com.seven.asimov.it.utils.conn;

import com.seven.asimov.it.utils.conn.ConnEvent.ActionType;
import com.seven.asimov.it.utils.conn.ConnEvent.EventType;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Hashtable;

/**
 * The <code>TcpServerConnection</code> instance represents a server TCP connection.
 * It contains a set of accepted TCP connection.
 *
 * @author Daniel Xie (dxie@seven.com)
 */

public class TcpServerConnection extends TcpConnection {
    private static final String TAG = "TcpServerConnection";

    private ServerSocketChannel mServerChannel;
    private InetSocketAddress mServerAddress;
    private ConnState mServerState;
    private Hashtable<SocketChannel, TcpConnection> mConnections;

    /**
     * Construct a TcpClientConnection
     *
     * @param selector
     * @param loop
     */
    public TcpServerConnection(ConnSelector selector) {
        super(selector);
        mServerState = ConnState.NEW;
        mConnections = new Hashtable<SocketChannel, TcpConnection>();
    }

    public TcpConnection getConnection(SelectableChannel channel) {
        return mConnections.get(channel);
    }

    public Hashtable<SocketChannel, TcpConnection> getAllConnections() {
        return mConnections;
    }

    public void removeConnection(SelectableChannel channel) {
        mConnections.remove(channel);
    }

    public InetSocketAddress getServerAddress() {
        return mServerAddress;
    }

    public void listen(InetAddress address, int port) throws IOException {
        ConnLogger.debug(TAG, "Tcp server start listening on " + address + ":" + port);
        listen(new InetSocketAddress(address, port));
    }

    public void listen(InetSocketAddress socketAddress) throws IOException {
        if (mServerState == ConnState.NEW) {
            bind(socketAddress);
        }

        if (mServerState == ConnState.BOUND) {
            mSelector.register(mServerChannel, SelectionKey.OP_ACCEPT, this);
            mServerState = ConnState.LISTENING;
        }
    }

    private void bind(InetSocketAddress socketAddress) throws IOException {
        mServerAddress = socketAddress;

        mServerChannel = ServerSocketChannel.open();
        mServerChannel.configureBlocking(false);
        mServerChannel.socket().bind(socketAddress);
        mServerState = ConnState.BOUND;
    }

    public void close() throws IOException {
        if (mServerChannel != null) {
            mSelector.unregister(mServerChannel);
            mServerState = ConnState.CLOSED;
            //
            for (TcpConnection conn : mConnections.values()) {
                try {
                    conn.close();
                } catch (IOException e) {
                    ConnLogger.debug(TAG, "connection close failed");
                }
            }
        } else {
            throw new IOException("close() on non-exist channel");
        }
    }

    /**
     * IConnListener interface
     */

    @Override
    public void onConnected(SelectableChannel channel) {
        // it's a server connection, do nothing
    }

    @Override
    public void onAccepted(ServerSocketChannel serverChannel, SocketChannel channel) {
        //ConnLogger.debug(TAG, "TcpConnection::onAccepted");
        try {
            // create a new TcpConnection
            TcpConnection newConn = new TcpConnection(mSelector, channel);
            newConn.setListener(mListener);
            mSelector.register(channel, SelectionKey.OP_READ, newConn);
            mConnections.put(channel, newConn);
            if (mListener != null) {
                mListener.onAccepted(serverChannel, channel);
            }
        } catch (IOException e) {
            mSelector.post(EventType.SOCKET_EXCEPTION, new ConnEventData(channel, e, this, ActionType.TCP_ACCEPT));
        }
    }

    @Override
    public void onReadable(SelectableChannel channel) {
        //Do nothing in TcpServerConnection
    }
}