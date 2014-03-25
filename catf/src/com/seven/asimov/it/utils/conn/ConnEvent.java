package com.seven.asimov.it.utils.conn;

public class ConnEvent {
    public enum EventType {
        NONE,
        SOCKET_EXCEPTION,
        TCP_CONNECTED,
        TCP_ACCEPTED,
        TCP_WRITABLE,
        TCP_READABLE,
        TCP_DATA_RECEIVED,
        TCP_DATA_SENT,
        TCP_FIN_RECEIVED,
        TCP_RST_RECEIVED,
        STOP;
    }

    public enum ActionType {
        NONE,
        TCP_LISTEN,
        TCP_CONNECT,
        TCP_ACCEPT,
        TCP_WRITE,
        TCP_READ,
        TCP_SHUTDOWN,
        TCP_CLOSE;
    }

    private EventType mType;
    private ConnEventData mData;

    ConnEvent(EventType type, ConnEventData data) {
        mType = type;
        mData = data;
    }

    ConnEventData getData() {
        return mData;
    }

    EventType getType() {
        return mType;
    }

    @Override
    public String toString() {
        return "ConnEvent[type=" + mType + ",mData=" + mData + "]";
    }
}