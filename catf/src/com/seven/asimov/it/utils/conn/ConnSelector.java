package com.seven.asimov.it.utils.conn;

import com.seven.asimov.it.utils.conn.ConnEvent.ActionType;
import com.seven.asimov.it.utils.conn.ConnEvent.EventType;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.*;
import java.util.Set;

public final class ConnSelector implements Runnable {

    private static final String TAG = "ConnSelector";

    private Selector mSelector;
    private EventLoop mEventLoop;
    private volatile boolean mRunning;

    private final Object mGate = new Object();

    public ConnSelector() {
    }

    public boolean start() {
        if (mRunning) {
            return mRunning;
        }

        try {
            mSelector = Selector.open();
        } catch (IOException e) {
            ConnLogger.error(TAG, "selector init failed", e);
            return false;
        }

        mEventLoop = new EventLoop();
        mEventLoop.start();

        mRunning = true;
        Thread selector = new Thread(this, "ConnSelector");
        selector.start();
        return true;
    }

    public void destroy() {
        stop();

        try {
            for (SelectionKey key : mSelector.keys()) {
                safeClose(key.channel());
            }
            mSelector.close();
        } catch (IOException e) {
            // ignored
        }
    }

    private static void safeClose(SelectableChannel channel) {
        if (channel != null) {
            try {
                channel.close();
            } catch (Exception e) {
                // Ignored
            }
        }
    }

    public void register(SelectableChannel c, int ops, IConnListener listener) throws ClosedChannelException {
        synchronized (mGate) {
            //ConnLogger.debug(TAG, "register " + ops + " on channel " + c);
            mSelector.wakeup();
            final SelectionKey key = c.keyFor(mSelector);
            if (key != null) {
                c.register(mSelector, key.interestOps() | ops, listener);
            } else {
                c.register(mSelector, ops, listener);
            }
        }
    }

    public void unregister(SelectableChannel c) {
        synchronized (mGate) {
            final SelectionKey key = c.keyFor(mSelector);
            if (key != null && key.isValid()) {
                key.cancel();
            }

            //ASMV-5381:Workaround to fix Android issue--
            //"Closing a socket from another thread doesn't generate IOException"
            //http://code.google.com/p/android/issues/detail?id=7933
            try {
                if (c instanceof SocketChannel) {
                    ((SocketChannel) c).socket().shutdownInput();
                }
            } catch (IOException e) {
                //ignore
            }

            safeClose(c);
            mSelector.wakeup();
        }
    }

    public void post(EventType type, SelectableChannel channel, Object data, IConnListener listener) {
        mEventLoop.post(type, channel, data, listener);
    }

    public void post(EventType type, ConnEventData data) {
        mEventLoop.post(type, data);
    }

    public void stop() {
        mRunning = false;
        wakeup();

        mEventLoop.stop();
    }

    public void wakeup() {
//        ConnLogger.debug(TAG, "wake up");
        mSelector.wakeup();
    }

    @Override
    public void run() {
//        ConnLogger.debug(TAG, "Starting selector...");

        while (mRunning) {
            try {
                synchronized (mGate) {
                }

                //ConnLogger.debug(TAG, "Selecting ...");
                final int num = mSelector.select();
                if (num > 0) {
                    final Set<SelectionKey> keys = mSelector.selectedKeys();
                    for (SelectionKey key : keys) {
                        if (key.isValid()) {
                            processKey(key);
                        }
                    }
                    keys.clear();
                }
            } catch (Exception e) {
                ConnLogger.error(TAG, "Selector exception:", e);
            }
        }

        ConnLogger.debug(TAG, "Selector stopped.");
    }

    private void processKey(SelectionKey key) throws IOException {
        //ConnLogger.debug(TAG, "processing key...");
        if (key.isConnectable()) {
            processConnect(key);
        } else if (key.isAcceptable()) {
            processAccept(key);
        } else if (key.isReadable()) {
            processRead(key);
        } else if (key.isWritable()) {
            processWrite(key);
        }
    }

    private void processConnect(SelectionKey key) {
        //remove OP_CONNECT from the interest set of the key
        key.interestOps(key.interestOps() & ~SelectionKey.OP_CONNECT);

        //get channel and attachment
        SocketChannel channel = (SocketChannel) key.channel();
        IConnListener listener = (IConnListener) key.attachment();

        try {
            if (channel.finishConnect()) {
                mEventLoop.post(EventType.TCP_CONNECTED, channel, null, listener);
            } else {
                throw new SocketException("not connected");
            }
        } catch (IOException e) {
            key.cancel();
            mEventLoop.post(EventType.SOCKET_EXCEPTION, new ConnEventData(channel, e, listener, ActionType.TCP_CONNECT));
        }
    }

    private void processAccept(SelectionKey key) {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        IConnListener listener = (IConnListener) key.attachment();

        try {
            SocketChannel newChannel = serverChannel.accept();
            newChannel.configureBlocking(false);
            mEventLoop.post(EventType.TCP_ACCEPTED, serverChannel, newChannel, listener);
        } catch (IOException e) {
            key.cancel();
            mEventLoop.post(EventType.SOCKET_EXCEPTION, new ConnEventData(null, e, listener, ActionType.TCP_ACCEPT));
        }
    }

    private void processRead(SelectionKey key) {
        //remove OP_READ from the interest set of the key
        key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);

        SocketChannel channel = (SocketChannel) key.channel();
        IConnListener listener = (IConnListener) key.attachment();
        mEventLoop.post(EventType.TCP_READABLE, channel, null, listener);
    }

    private void processWrite(SelectionKey key) throws IOException {
        //remove OP_WRITE from the interest set of the key
        key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);

        //get channel and attachment
        SocketChannel channel = (SocketChannel) key.channel();
        IConnListener listener = (IConnListener) key.attachment();

        mEventLoop.post(EventType.TCP_WRITABLE, channel, null, listener);
    }
}