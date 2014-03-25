package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.utils.conn.*;

import java.nio.channels.SelectableChannel;

import static com.seven.asimov.it.utils.conn.ConnTestResult.StateCode.RST_RECEIVED;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnType.CLIENT;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnType.SERVER;

/**
 * <p>
 * TC62
 * </p>
 * <p/>
 * "Pre-requisite: OC startups and works fine. Steps: 1. Launch about 50 connections at the same time. 2. App sends FINs
 * to close all connections, then launches 25 connections again. 3. Check connections in logs."
 * <p/>
 * Expected result: 1.No connection leak.
 */
public class ConnLeakAfterSendFinTest extends MultiConnectionsTest implements IConnListener {
    private static final String TAG = ConnLeakAfterSendFinTest.class.getSimpleName();
    protected boolean isAllFirstTestConnClosed;

    protected static final int FIRST_TEST_CONN_COUNT = 50;
    protected static final int SECOND_TEST_CONN_COUNT = 25;

    public ConnLeakAfterSendFinTest(ConnSelector selector, HttpServer server) {
        this(TAG, selector, server);
    }

    public ConnLeakAfterSendFinTest(String name, ConnSelector selector, HttpServer server) {
        super(name, selector, server);
        this.setExpectedResponseCount(FIRST_TEST_CONN_COUNT + SECOND_TEST_CONN_COUNT);
        // this.setClientExpectedRstCount(FIRST_TEST_CONN_COUNT + SECOND_TEST_CONN_COUNT);
        this.setClientExpectedRstCount(0);
        this.setClientExpectedFinCount(FIRST_TEST_CONN_COUNT + SECOND_TEST_CONN_COUNT);

        // Server sends Fin after receives Fin, so it can't receive rst.
        this.setServerExpectedRstCount(0);
    }

    protected void onAllFirstTestConnClosed() {
        setupConnections(SECOND_TEST_CONN_COUNT);
    }

    @Override
    protected boolean onStart() {
        return setupConnections(FIRST_TEST_CONN_COUNT);
    }

    @Override
    public void onFinReceived(SelectableChannel channel) {
        HttpClient client = getClient(channel);
        if (client != null) {
            System.out.println(getName() + ": Client onFinReceived, connId = " + client.getConnId());
            int connId = client.getConnId();
            this.addResult(connId, CLIENT, RST_RECEIVED);

            this.removeClient(client);
            mClientReceivedFinCount++;

            if (!isAllFirstTestConnClosed && mClientReceivedFinCount == FIRST_TEST_CONN_COUNT) {
                isAllFirstTestConnClosed = true;
                onAllFirstTestConnClosed();
            }
        } else {
            TcpConnection conn = mServer.getConnection(channel);
            if (conn != null) {
                System.out.println(getName() + ": Server onFinReceived, connId = " + conn.getConnId());
                this.addResult(conn.getConnId(), SERVER, RST_RECEIVED);

                mServer.getServerConnection().removeConnection(channel);
                mServerReceivedFinCount++;

                if (!isAllFirstTestConnClosed && mServerReceivedRstCount == FIRST_TEST_CONN_COUNT) {
                    isAllFirstTestConnClosed = true;
                    onAllFirstTestConnClosed();
                }
            }
        }

        if (verifyClientExpectedRst(mClientReceivedRstCount)
                && (mServerReceivedRstCount == getServerExpectedRstCount())
                && (mRecevicedResponseCount == getExpectedResponseCount())) {
            this.pass();
        }
    }

    @Override
    public void onRstReceived(SelectableChannel channel) {
        HttpClient client = getClient(channel);
        if (client != null) {
            System.out.println(getName() + ": Client onRstReceived, connId = " + client.getConnId());
            int connId = client.getConnId();
            this.addResult(connId, CLIENT, RST_RECEIVED);

            this.removeClient(client);
            mClientReceivedRstCount++;

            if (!isAllFirstTestConnClosed && mClientReceivedRstCount == FIRST_TEST_CONN_COUNT) {
                isAllFirstTestConnClosed = true;
                onAllFirstTestConnClosed();
            }
        } else {
            TcpConnection conn = mServer.getConnection(channel);
            if (conn != null) {
                System.out.println(getName() + ": Server onRstReceived, connId = " + conn.getConnId());
                this.addResult(conn.getConnId(), SERVER, RST_RECEIVED);

                mServer.getServerConnection().removeConnection(channel);
                mServerReceivedRstCount++;

                if (!isAllFirstTestConnClosed && mServerReceivedRstCount == FIRST_TEST_CONN_COUNT) {
                    isAllFirstTestConnClosed = true;
                    onAllFirstTestConnClosed();
                }
            }
        }

        if (verifyClientExpectedRst(mClientReceivedRstCount)
                && (getServerExpectedRstCount() < 0 || mServerReceivedRstCount == getServerExpectedRstCount())
                && (getExpectedResponseCount() < 0 || mRecevicedResponseCount == getExpectedResponseCount())) {
            this.pass();
        }
    }
}