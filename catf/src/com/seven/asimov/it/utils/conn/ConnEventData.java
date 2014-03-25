package com.seven.asimov.it.utils.conn;

import com.seven.asimov.it.utils.conn.ConnEvent.ActionType;

import java.nio.channels.SelectableChannel;

class ConnEventData {
    private SelectableChannel mChannel;
    private IConnListener mListener;
    private Object mData;
    private ActionType mOriginalAction = ActionType.NONE;

    public ConnEventData(SelectableChannel channel, Object data, IConnListener listener) {
        mChannel = channel;
        mData = data;
        mListener = listener;
    }

    public ConnEventData(SelectableChannel channel, Object data, IConnListener listener, ActionType originalAction) {
        mChannel = channel;
        mData = data;
        mListener = listener;
        mOriginalAction = originalAction;
    }

    public SelectableChannel getChannel() {
        return mChannel;
    }

    public Object getData() {
        return mData;
    }

    public IConnListener getListener() {
        return mListener;
    }

    public ActionType getOriginalAction() {
        return mOriginalAction;
    }
}