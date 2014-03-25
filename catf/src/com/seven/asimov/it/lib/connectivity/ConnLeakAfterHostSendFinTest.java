package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.utils.conn.*;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map.Entry;

import static com.seven.asimov.it.utils.conn.ConnTestResult.StateCode;
import static com.seven.asimov.it.utils.conn.ConnTestResult.StateCode.DONE;
import static com.seven.asimov.it.utils.conn.ConnTestResult.StateCode.SEND_FAILED;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnState.CLOSED;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnType.CLIENT;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnType.SERVER;

/**
 * <p>
 * TC64
 * </p>
 * "Pre-requisite: OC startups and works fine. Steps: 1. Launch about 100 connections at the same time. 2. Host sends
 * FINs to close all connections, then launches 50 connections again. 3. Check connections in logs."
 * <p/>
 * Expected result: 1.No connection leak.
 */
public class ConnLeakAfterHostSendFinTest extends ConnLeakAfterSendFinTest implements IConnListener {
    private static final String TAG = ConnLeakAfterHostSendFinTest.class.getSimpleName();

    private int mClientExpectedRstFinCount = 0;

    public ConnLeakAfterHostSendFinTest(ConnSelector selector, HttpServer server) {
        super(TAG, selector, server);

        this.setServerExpectedRstCount(FIRST_TEST_CONN_COUNT + SECOND_TEST_CONN_COUNT);

        // this.setClientExpectedRstCount(FIRST_TEST_CONN_COUNT + SECOND_TEST_CONN_COUNT);
        // this.setClientExpectedFinCount(0);
        this.setClientExpectedRstFinCount(FIRST_TEST_CONN_COUNT + SECOND_TEST_CONN_COUNT);

    }

    @Override
    public void onResponseReceived(int connId, HttpResponse response) {
        ConnLogger.debug(TAG, appendLogPrefix(connId, "onResponseReceived"));
        mRecevicedResponseCount++;
    }

    @Override
    public void onDataSent(SelectableChannel channel) {
        TcpConnection conn = mServer.getConnection(channel);
        if (conn != null) {
            try {
                ConnLogger.debug(TAG, getName() + ": Server sends Fin");

                mServer.sendFin(conn.getConnId());
            } catch (IOException e) {
                ConnLogger.error(TAG, getName() + ": Send Fin fail", e);
                this.fail(conn.getConnId(), SERVER, SEND_FAILED);
            }
        }
    }

    @Override
    protected void onDone(Status status) {
        ConnLogger.debug(TAG, this.getName() + ": stopped, status:" + status);
        List<SysSocketDescriptor> leakedSockets = mSocketsMonitor.getCurrentNewSockets();
        ConnLogger.debug(TAG,
                this.getName() + ": leakedSockets\n" + mSocketsMonitor.getSocketsDescription(leakedSockets));

        closeLeakClientConn();
        closeLeakServerConn();

        if (status == Status.TIMEDOUT) {

            if (!mLeakedClientConns.isEmpty()) {
                this.fail("Client sockets leak, conns:" + mLeakedClientConns.size());
            } else if (!mLeakedServerConns.isEmpty()) {
                this.fail("Server sockets leak, conns:" + mLeakedClientConns.size());
            } else if (!leakedSockets.isEmpty()) {
                this.fail("Sockets leak");
            } else if (!verifyClientExpectedRstFin(mClientReceivedRstCount, mClientReceivedFinCount)) {
                this.fail("Client received [" + mClientReceivedRstCount + "," + mClientReceivedFinCount
                        + "] rst,fin, expected [" + getClientExpectedRstFinCount() + "]");
            } else if (getServerExpectedRstCount() > 0 && mServerReceivedRstCount != getServerExpectedRstCount()) {
                this.fail("Server received [" + mServerReceivedRstCount + "] rst, expected ["
                        + getServerExpectedRstCount() + "]");
            } else if (getExpectedResponseCount() > 0 && mRecevicedResponseCount != getExpectedResponseCount()) {
                this.fail("Client received [" + mRecevicedResponseCount + "] reponses, expected ["
                        + getExpectedResponseCount() + "]");
            } else {
                this.pass(ConnTestResult.CONN_ID_NEW, CLIENT, DONE);
            }
        }

        ConnLogger.debug(TAG, this.getName() + ": " + mSocketsMonitor.getCurrentNewSocketsDescription());
    }

    private void closeLeakClientConn() {
        for (HttpClient client : mClientChannels.values()) {
            if (client.getState() != CLOSED) {
                ConnectionState connSate = new ConnectionState();
                connSate.conn = client.getConnection();
                connSate.state = client.getState();

                mLeakedClientConns.add(connSate);

                try {
                    client.close();
                    sleepForOC();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // TODO: this is duplicated to closeLeakClientConn. Need to remove it.
    private void closeLeakServerConn() {
        if (mServer == null) {
            return;
        }

        for (Entry<SocketChannel, TcpConnection> connEntry : mServer.getServerConnection().getAllConnections()
                .entrySet()) {
            if (connEntry.getValue().getState() != CLOSED) {
                ConnectionState connSate = new ConnectionState();
                connSate.conn = connEntry.getValue();
                connSate.state = connEntry.getValue().getState();

                mLeakedServerConns.add(connSate);

                try {
                    mServer.close((SocketChannel) connEntry.getKey());
                    sleepForOC();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onFinReceived(SelectableChannel channel) {
        HttpClient client = getClient(channel);
        if (client != null) {
            System.out.println(getName() + ": Client onFinReceived, connId = " + client.getConnId());
            int connId = client.getConnId();
            this.addResult(connId, CLIENT, StateCode.RST_RECEIVED);

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
                this.addResult(conn.getConnId(), SERVER, StateCode.RST_RECEIVED);

                mServer.getServerConnection().removeConnection(channel);
                mServerReceivedFinCount++;

                // if (!isAllFirstTestConnClosed && mServerReceivedRstCount == FIRST_TEST_CONN_COUNT) {
                if (!isAllFirstTestConnClosed
                        && (mClientReceivedRstCount + mClientExpectedRstFinCount) == FIRST_TEST_CONN_COUNT) {
                    isAllFirstTestConnClosed = true;
                    onAllFirstTestConnClosed();
                }
            }
        }

        if (verifyClientExpectedRstFin(mClientReceivedRstCount, mClientReceivedFinCount)
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
            this.addResult(connId, CLIENT, StateCode.RST_RECEIVED);

            this.removeClient(client);
            mClientReceivedRstCount++;

            // if (!isAllFirstTestConnClosed && mClientReceivedRstCount == FIRST_TEST_CONN_COUNT) {
            if (!isAllFirstTestConnClosed
                    && (mClientReceivedRstCount + mClientExpectedRstFinCount) == FIRST_TEST_CONN_COUNT) {
                isAllFirstTestConnClosed = true;
                onAllFirstTestConnClosed();
            }
        } else {
            TcpConnection conn = mServer.getConnection(channel);
            if (conn != null) {
                System.out.println(getName() + ": Server onRstReceived, connId = " + conn.getConnId());
                this.addResult(conn.getConnId(), SERVER, StateCode.RST_RECEIVED);

                mServer.getServerConnection().removeConnection(channel);
                mServerReceivedRstCount++;

                if (!isAllFirstTestConnClosed && mServerReceivedRstCount == FIRST_TEST_CONN_COUNT) {
                    isAllFirstTestConnClosed = true;
                    onAllFirstTestConnClosed();
                }
            }
        }

        if (verifyClientExpectedRstFin(mClientReceivedRstCount, mClientReceivedFinCount)
                && (getServerExpectedRstCount() < 0 || mServerReceivedRstCount == getServerExpectedRstCount())
                && (getExpectedResponseCount() < 0 || mRecevicedResponseCount == getExpectedResponseCount())) {
            this.pass();
        }
    }

    public boolean verifyClientExpectedRstFin(int clientReceivedRstCount, int clientReceivedFinCount) {
        return getClientExpectedRstCount() < 0
                || (clientReceivedRstCount + clientReceivedFinCount) == getClientExpectedRstFinCount();
    }

    @Override
    public void setClientExpectedRstFinCount(int clientReceivedRstFinCount) {
        this.mClientExpectedRstFinCount = clientReceivedRstFinCount;
    }

    @Override
    public final int getClientExpectedRstFinCount() {
        return mClientExpectedRstFinCount;
    }
}