package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.utils.conn.*;
import com.seven.asimov.it.utils.conn.ConnTestResult.StateCode;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.util.ArrayList;
import java.util.List;

import static com.seven.asimov.it.utils.conn.TcpConnection.ConnState;

/**
 * <p>TC03</p>
 * <p/>
 * "Pre-requisite: OC startups and works fine.
 * Steps:
 * 1. Launch more than 200 connections at the same time.
 * 2. Check connection for the request in logs."
 * <p/>
 * Expected result:
 * 1.Can setup more than 200 connections through OC.
 */
public class MultiConnWithoutRequestTest extends MultiConnectionsTest implements IConnListener {
    private static final String TAG = MultiConnWithoutRequestTest.class.getSimpleName();
    private static final int TEST_CONN_COUNT = 200;
    private List<HttpClient> mNotConnectedClientConns;

    public MultiConnWithoutRequestTest(ConnSelector selector, HttpServer server) {
        super(TAG, selector, server);
    }

    @Override
    protected boolean onStart() {
        return setupConnections(TEST_CONN_COUNT);
    }

    //wrong check
//    @Override
//    public void onAccepted(ServerSocketChannel serverChannel, SocketChannel newChannel) {
//        this.fail("Server can't accept new connection because no request sends to OC from app");
//    }

    @Override
    public void onConnected(SelectableChannel channel) {
        HttpClient client = getClient(channel);
        if (client == null) {
            ConnLogger.error(TAG, "Get unknown channel");
            return;
        }

        this.addResult(client.getConnId(), TcpConnection.ConnType.CLIENT, StateCode.CONNECTED);

        mClientConnectedCount++;
        if (mClientConnectedCount == TEST_CONN_COUNT) {
            pass();
        }
    }

    @Override
    protected void onDone(Status status) {
        ConnLogger.debug(TAG, this.getName() + " stopped, status:" + status);

        mNotConnectedClientConns = closeOpenedClientConns();
        List<SysSocketDescriptor> leakedSockets = mSocketsMonitor.getCurrentNewSockets();
        ConnLogger.debug(TAG, this.getName() + ": leakedSockets\n" + mSocketsMonitor.getSocketsDescription(leakedSockets));

        if (status == Status.TIMEDOUT) {
            if (mClientConnectedCount != TEST_CONN_COUNT) {
                this.fail("Expected [" + mNotConnectedClientConns.size() + "] conns, but only [" + mClientConnectedCount + "]  were setup.");
            } else {
                pass();
            }
        }
    }

    @Override
    protected String getOpenedConns() {
        StringBuilder description = new StringBuilder();
        for (TcpClient conn : mNotConnectedClientConns) {
            description.append("       Client connection[")
                    .append("id=")
                    .append(conn.getConnId())
                    .append("] is not connected, state:")
                    .append(conn.getState())
                    .append("\n");
        }

        description.append(" Total:")
                .append(getAllClients().size())
                .append(", not connected conns:")
                .append(mNotConnectedClientConns.size())
                .append("\n");

        return description.toString();
    }

    private List<HttpClient> closeOpenedClientConns() {
        ConnLogger.debug(TAG, "closeOpenedClientConns, all channels:" + getAllClients().size());

        List<HttpClient> connections = new ArrayList<HttpClient>();
        for (HttpClient client : getAllClients()) {
            if (client.getState() != ConnState.CONNECTED) {
                connections.add(client);
            }

            if (client.getState() != ConnState.CLOSED) {
                try {
                    ConnLogger.debug(TAG, "close client id:" + client.getConnId());
                    client.close();
                    sleepForOC();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return connections;
    }

}