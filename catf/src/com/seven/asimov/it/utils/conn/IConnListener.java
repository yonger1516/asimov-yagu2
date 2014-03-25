package com.seven.asimov.it.utils.conn;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.utils.conn.ConnEvent.ActionType;
import com.seven.asimov.it.utils.conn.TcpConnection.ConnType;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * The <code>IConnListener</code> interface represents a http connection listener.
 *
 * @author Daniel Xie (dxie@seven.com)
 */

public interface IConnListener {
    /**
     * Called when a socket channel is connected
     *
     * @param channel
     * @param e
     */
    public void onConnected(SelectableChannel channel);

    /**
     * Called when a socket channel is accepted on a server socket channel
     *
     * @param channel
     * @param e
     */
    public void onAccepted(ServerSocketChannel serverChannel, SocketChannel acceptedChannel);

    /**
     * Called when a socket channel is ready to write
     *
     * @param channel
     */
    public void onWritable(SelectableChannel channel);

    /**
     * Called when a socket channel is ready to read
     *
     * @param channel
     */
    public void onReadable(SelectableChannel channel);

    /**
     * Called when data was received on a socket channel
     *
     * @param channel
     * @param byteBuffer
     */
    public void onDataReceived(SelectableChannel channel, ByteBuffer byteBuffer);

    /**
     * Called when FIN received on a socket channel
     *
     * @param channel
     */
    public void onFinReceived(SelectableChannel channel);

    /**
     * Called when RST received on a socket channel
     *
     * @param channel
     */
    public void onRstReceived(SelectableChannel channel);

    /**
     * Called when data was sent on a socket channel
     *
     * @param channel
     */
    public void onDataSent(SelectableChannel channel);

    /**
     * Called when FIN sent on a socket channel
     *
     * @param channel
     */
    public void onFinSent(SelectableChannel channel);

    /**
     * Called when RST sent on a socket channel
     *
     * @param channel
     */
    public void onRstSent(SelectableChannel channel);

    /**
     * Called when IOException was caught on SocketChannel/ServerSocketChannel
     *
     * @param channel
     * @param e
     * @param eventType
     */
    public void onSocketException(SelectableChannel channel, IOException e, ActionType actionType);

    public void onResponseReceived(int connId, HttpResponse response);

    public void onRequestReceived(int connId, HttpRequest request);

    public void onHttpException(int connId, ConnType type, Exception e);
}