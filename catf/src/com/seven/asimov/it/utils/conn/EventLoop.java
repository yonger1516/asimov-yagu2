package com.seven.asimov.it.utils.conn;

import com.seven.asimov.it.utils.conn.ConnEvent.EventType;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public final class EventLoop implements Runnable {
    private static final String TAG = "EventLoop";

    private volatile boolean mRunning;
    private ArrayList<ConnEvent> mEventQueue;

    public EventLoop() {
        mEventQueue = new ArrayList<ConnEvent>();
    }

    @Override
    public void run() {
//        ConnLogger.debug(TAG, "Starting eventloop...");

        while (mRunning) {
            synchronized (mEventQueue) {
                while (mEventQueue.isEmpty()) {
                    try {
                        mEventQueue.wait();
                    } catch (InterruptedException e) {
                        // ignore?
                    }
                }

                try {
                    ConnEvent event = mEventQueue.remove(0);
                    handleEvent(event);
                } catch (Exception e) {
                    ConnLogger.error(TAG, "handleEvent encounter exception", e);
                }
            }
        }

//        ConnLogger.debug(TAG, "Eventloop stopped");
    }

    public void start() {
        if (!mRunning) {
            mRunning = true;
            Thread eventLoop = new Thread(this, "EventLoop");
            eventLoop.start();
        }
    }

    public void stop() {
//        ConnLogger.debug(TAG, "Stopping eventloop...");
        post(EventType.STOP, null);
    }

    public void post(EventType type, SelectableChannel channel, Object data, IConnListener listener) {
        ConnEventData eventData = new ConnEventData(channel, data, listener);
        post(type, eventData);
    }

    public void post(EventType type, ConnEventData data) {
        // ConnLogger.debug(TAG, "EventLoop::post " + type);
        synchronized (mEventQueue) {
            mEventQueue.add(new ConnEvent(type, data));
            mEventQueue.notify();
        }
    }

    private void handleEvent(ConnEvent event) {
        ConnLogger.debug(TAG, "HandleEvent [ event = " + event + "]");
        ConnEventData eventData = event.getData();
        IConnListener listener = eventData == null ? null : eventData.getListener();
        SelectableChannel channel = eventData == null ? null : eventData.getChannel();

        if (event.getType() == EventType.STOP) {
            mRunning = false;
        } else if (listener != null) {
            switch (event.getType()) {
                case TCP_CONNECTED:
                    listener.onConnected(channel);
                    break;
                case TCP_ACCEPTED:
                    SocketChannel newChannel = (SocketChannel) eventData.getData();
                    listener.onAccepted((ServerSocketChannel) channel, newChannel);
                    break;
                case TCP_WRITABLE:
                    listener.onWritable(channel);
                    break;
                case TCP_READABLE:
                    listener.onReadable(channel);
                    break;
                case TCP_DATA_RECEIVED:
                    listener.onDataReceived(channel, (ByteBuffer) eventData.getData());
                    break;
                case TCP_DATA_SENT:
                    listener.onDataSent(channel);
                    break;
                case TCP_FIN_RECEIVED:
                    listener.onFinReceived(channel);
                    break;
                case TCP_RST_RECEIVED:
                    listener.onRstReceived(channel);
                case SOCKET_EXCEPTION:
                    listener.onSocketException(channel, (IOException) eventData.getData(), eventData.getOriginalAction());
                    break;
                default:
                    break;
            }
        }
    }

}