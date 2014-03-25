package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.utils.conn.ConnLogger;
import com.seven.asimov.it.utils.conn.ConnSelector;
import com.seven.asimov.it.utils.conn.HttpServer;
import com.seven.asimov.it.utils.conn.IConnListener;

import java.nio.channels.SelectableChannel;

/**
 * <p>TC63</p>
 * "Pre-requisite: OC startups and works fine.
 * Steps:
 * 1. Launch about 100 connections at the same time.
 * 2. App sends RSTs to close all connections, then launches 50 connections again.
 * 3. Check connections in logs."
 * <p/>
 * Expected result:
 * 1.No connection leak.
 */
public class ConnLeakAfterSendRstTest extends ConnLeakAfterSendFinTest implements IConnListener {
    private static final String TAG = ConnLeakAfterSendRstTest.class.getSimpleName();

    public ConnLeakAfterSendRstTest(ConnSelector selector, HttpServer server) {
        super(TAG, selector, server);
        this.setClientExpectedRstCount(0);
    }

    @Override
    public void onResponseReceived(int connId, HttpResponse response) {
        ConnLogger.debug(TAG, appendLogPrefix(connId, "onResponseReceived"));
        mRecevicedResponseCount++;

        clientSendRst(connId);
    }

    @Override
    public void onRstReceived(SelectableChannel channel) {
        super.onRstReceived(channel);
    }

}