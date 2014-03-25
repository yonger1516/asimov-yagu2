package com.seven.asimov.it.utils.conn;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.utils.conn.ConnEvent.ActionType;
import com.seven.asimov.it.utils.conn.ConnEvent.EventType;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

/**
 * The <code>TcpConnection</code> instance represents a CONNECTED TCP connection. It comes from TcpClient or TcpServer
 * and can be used to read and write data.
 *
 * @author Daniel Xie (dxie@seven.com)
 */

public class TcpConnection implements IConnListener {
    private static final String TAG = "TcpConnection";

    public enum ConnType {
        NONE, CLIENT, SERVER;
    }

    public enum ConnState {
        NEW, BOUND, LISTENING, CONNECTING, CONNECTED, SHUTDOWN, CLOSE_WAIT, CLOSED;
    }

    private boolean mFinPending;
    private ArrayList<ByteBuffer> mSendBuffers;
    private ByteBuffer mReceiveBuffer;

    protected IConnListener mListener;
    protected ConnType mConnType;
    protected SocketChannel mChannel;
    protected ConnState mState;
    protected ConnSelector mSelector;

    private int mConnId = ConnTestResult.CONN_ID_NEW;

    /**
     * Construct a client(connect out) TcpConnection
     *
     * @param selector
     */
    public TcpConnection(ConnSelector selector) {
        mSelector = selector;
        mChannel = null;
        mConnType = ConnType.CLIENT;

        mFinPending = false;
        mState = ConnState.NEW;
        mSendBuffers = new ArrayList<ByteBuffer>();
        mReceiveBuffer = ByteBuffer.allocate(8192);
    }

    /**
     * Construct an server(accepted) TcpConnection
     *
     * @param selector
     * @param channel
     */
    public TcpConnection(ConnSelector selector, SocketChannel channel) {
        mSelector = selector;
        mChannel = channel;
        mConnType = ConnType.SERVER;

        mFinPending = false;
        mState = ConnState.CONNECTED;
        mSendBuffers = new ArrayList<ByteBuffer>();
        mReceiveBuffer = ByteBuffer.allocate(8192);
    }

    /**
     * Set connection id
     *
     * @param connId
     */
    protected void setConnId(int connId) {
        mConnId = connId;
    }

    /**
     * Get connection id
     *
     * @return connid
     */
    public final int getConnId() {
        return mConnId;
    }

    /**
     * Set connection listener
     *
     * @param listener
     */
    public void setListener(IConnListener listener) {
        mListener = listener;
    }

    /**
     * Get connection listener
     *
     * @return listener
     */
    public IConnListener getListener() {
        return mListener;
    }

    /**
     * Get connection state
     *
     * @return state
     */
    public ConnState getState() {
        return mState;
    }

    /**
     * Get socket channel
     *
     * @return socketchannel
     */
    public SocketChannel getChannel() {
        return mChannel;
    }

    /**
     * Get receive buffer
     *
     * @return
     */
    public ByteBuffer getReceiveBuffer() {
        return mReceiveBuffer;
    }

    /**
     * call to send data - the data is written out in onWritable()
     *
     * @param buffer
     * @throws IOException
     */
    protected void sendData(ByteBuffer buffer) throws IOException {
        if (mChannel == null) {
            throw new IOException("sendData() on non-exist channel");
        } else if (mState != ConnState.CONNECTED) {
            throw new IOException("sendData() on not connected channel, current state is:" + mState);
        } else {
            mSendBuffers.add(buffer);
            mSelector.register(mChannel, SelectionKey.OP_WRITE, this);
        }
    }

    /**
     * call to close the socket channel
     * <p/>
     * //* @param channel
     *
     * @throws IOException
     */
    protected void close() throws IOException {
        if (mChannel != null) {
            mSelector.unregister(mChannel);
            mState = ConnState.CLOSED;
        } else {
            throw new IOException("close() on non-exist channel");
        }
    }

    /**
     * call to send RST - socket is force-closed
     *
     * @throws IOException
     */
    protected void sendRst() throws IOException {
        if (mChannel == null) {
            throw new IOException("sendRst() on non-exist channel");
        } else if (mState != ConnState.CONNECTED) {
            throw new IOException("sendRst() on not connected channel, state:" + mState);
        } else {
            onRstSent(mChannel);
            // we always send RST immediately
            mChannel.socket().setSoLinger(true, 0);
            close();
        }
    }

    /**
     * call to send FIN - socket is shutdown
     *
     * @throws IOException
     */
    protected void sendFin() throws IOException {
        if (mChannel == null) {
            throw new IOException("sendFin() on non-exist channel");
        } else if (mState == ConnState.CLOSED) {
            throw new IOException("sendFin() on closed channel");
        } else {
            if (mSendBuffers.isEmpty()) {
                mState = (mState == ConnState.CLOSE_WAIT ? ConnState.CLOSED : ConnState.SHUTDOWN);
                _sendFin();
            } else {
                // enqueue this until data sent
                mFinPending = true;
            }
        }
    }

    private void _sendFin() throws IOException {
        onFinSent(mChannel);
        if (mState == ConnState.CLOSED) {
            mSelector.unregister(mChannel);
        } else {
            ConnLogger.debug(TAG, "shutdownOutput");
            mChannel.socket().shutdownOutput();
            mSelector.register(mChannel, SelectionKey.OP_READ, this);
        }
    }

    /**
     * IConnListener interface - TCP callbacks
     */

    @Override
    public void onConnected(SelectableChannel channel) {
        // do nothing
    }

    @Override
    public void onAccepted(ServerSocketChannel serverChannel, SocketChannel acceptedChannel) {
        // do nothing
    }

    @Override
    public void onWritable(SelectableChannel channel) {
        // ConnLogger.debug(TAG, "TcpConnection::onWritable");
        if (mState != ConnState.CONNECTED && mState != ConnState.CLOSE_WAIT) {
            throw new RuntimeException("onWritable on not connected channel, current state is:" + mState + ", type:"
                    + mConnType + ", connId:" + mConnId);
        }

        while (!mSendBuffers.isEmpty()) {
            ByteBuffer buffer = mSendBuffers.get(0);
            try {
                while (buffer.hasRemaining()) {
                    if (mChannel.write(buffer) == 0) {
                        break;
                    }
                }
                if (!buffer.hasRemaining()) {
                    mSendBuffers.remove(0);
                } else {
                    mSelector.register(mChannel, SelectionKey.OP_WRITE, this);
                    break;
                }
            } catch (IOException e) {
                mSelector.post(ConnEvent.EventType.SOCKET_EXCEPTION, new ConnEventData(channel, e, this, ActionType.TCP_WRITE));
                e.printStackTrace();
                break;
            }
        }

        if (mSendBuffers.isEmpty()) {
            // post TCP_DATA_SENT
            mSelector.post(EventType.TCP_DATA_SENT, mChannel, null, this);
            if (mFinPending) {
                try {
                    mState = (mState == ConnState.CLOSE_WAIT ? ConnState.CLOSED : ConnState.SHUTDOWN);
                    _sendFin();
                } catch (IOException e) {
                    mSelector.post(ConnEvent.EventType.SOCKET_EXCEPTION,
                            new ConnEventData(channel, e, this, ActionType.TCP_WRITE));
                }
            }
        }
    }

    @Override
    public void onReadable(SelectableChannel channel) {
        // ConnLogger.debug(TAG, "TcpConnection::onReadable");
        int nBytes = 0;
        do {
            try {
                ByteBuffer buffer = ByteBuffer.allocate(8192);
                nBytes = mChannel.read(buffer);
                if (nBytes == -1) {
                    // post TCP_FIN_RECEIVED
                    mSelector.post(EventType.TCP_FIN_RECEIVED, mChannel, null, this);
                } else if (nBytes > 0) {
                    buffer.flip();
                    // post TCP_DATA_RECEIVED
                    mSelector.post(EventType.TCP_DATA_RECEIVED, mChannel, buffer, this);
                }
            } catch (IOException e) {
                ConnLogger.debug(TAG, "Exception while reading selectable channel [ Exception.stacktrace = " + ExceptionUtils.getStackTrace(e) + " ]");
                //mSelector.post(EventType.SOCKET_EXCEPTION, new ConnEventData(channel, e, this, ActionType.TCP_READ));
                mSelector.post(EventType.TCP_RST_RECEIVED, new ConnEventData(channel, e, this, ActionType.TCP_SHUTDOWN));
                return;
            }
        } while (nBytes > 0);

        if (nBytes >= 0) {
            try {
                mSelector.register(mChannel, SelectionKey.OP_READ, this);
            } catch (IOException e) {
                mSelector.post(EventType.SOCKET_EXCEPTION, new ConnEventData(channel, e, this, ActionType.TCP_READ));
            }
        }
    }

    @Override
    public void onDataReceived(SelectableChannel channel, ByteBuffer byteBuffer) {
        if (mListener != null) {
            mListener.onDataReceived(mChannel, byteBuffer);
        }
    }

    @Override
    public void onFinReceived(SelectableChannel channel) {
        if (mState == ConnState.SHUTDOWN) {
            mState = ConnState.CLOSED;
        } else {
            mState = ConnState.CLOSE_WAIT;
        }
        if (mListener != null) {
            mListener.onFinReceived(mChannel);
        }
    }

    @Override
    public void onRstReceived(SelectableChannel channel) {
        if (mListener != null) {
            mListener.onRstReceived(channel);
        }
    }

    @Override
    public void onDataSent(SelectableChannel channel) {
        if (mListener != null) {
            mListener.onDataSent(mChannel);
        }
    }

    @Override
    public void onFinSent(SelectableChannel channel) {
        if (mListener != null) {
            mListener.onFinSent(mChannel);
        }
    }

    @Override
    public void onRstSent(SelectableChannel channel) {
        if (mListener != null) {
            mListener.onRstSent(mChannel);
        }
    }

    @Override
    public void onSocketException(SelectableChannel channel, IOException e, ActionType actionType) {
        if (mListener != null) {
            mListener.onSocketException(mChannel, e, actionType);
        }
    }

    /**
     * IConnListener interface - HTTP callbacks
     */

    @Override
    public void onRequestReceived(int connId, HttpRequest request) {
        if (mListener != null) {
            mListener.onRequestReceived(connId, request);
        }
    }

    @Override
    public void onResponseReceived(int connId, HttpResponse response) {
        if (mListener != null) {
            mListener.onResponseReceived(connId, response);
        }
    }

    @Override
    public void onHttpException(int connId, ConnType type, Exception e) {
        if (mListener != null) {
            mListener.onHttpException(connId, type, e);
        }
    }
}