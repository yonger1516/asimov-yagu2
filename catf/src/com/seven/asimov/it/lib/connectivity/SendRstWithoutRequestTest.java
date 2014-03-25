package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.utils.conn.*;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import static com.seven.asimov.it.utils.conn.ConnTestResult.StateCode.*;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnType.CLIENT;

/**
 * <p>
 * Pre-requisite: OC startups and works fine.
 * </p>
 * Steps:
 * <ul>
 * <li>1. App setup connection with OC.</li>
 * <li>2. App sends RST without request data.</li>
 * <li>3. Check connection in logs.</li>
 * </ul>
 * Expected Result:
 * <ul>
 * <li>1.Make sure OC don't setup client connection.</li>
 * <li>2.OC receive RST and no next action as no client connection.</li>
 * </ul>
 * Expected Test Records:
 * <ul>
 * <li>1. XX : CLIENT : CONNECTING</li>
 * <li>2. XX : CLIENT : CONNECTED</li>
 * <li>3. XX : CLIENT : RST_SENT</li>
 * <li>4. XX : CLIENT : DONE</li>
 * </ul>
 */
public class SendRstWithoutRequestTest extends SimpleTest {
    private static final String TAG = SendRstWithoutRequestTest.class.getSimpleName();

    public SendRstWithoutRequestTest(ConnSelector selector, HttpServer server) {
        super(TAG, selector, server);
    }

    @Override
    public void onConnected(SelectableChannel channel) {
        ConnLogger.debug(TAG, getName() + ": client connected");
        addResult(mClient.getConnId(), CLIENT, CONNECTED);
        try {
            mClient.sendRst();
            ConnLogger.debug(TAG, getName() + ": client sends Rst");
        } catch (IOException e) {
            this.fail(ConnTestResult.CONN_ID_NEW, TcpConnection.ConnType.CLIENT, SEND_FAILED);
        }
    }

    @Override
    public void onAccepted(ServerSocketChannel serverChannel, SocketChannel newChannel) {
        ConnLogger.debug(TAG, getName() + "  onAccepted, channel:" + newChannel);
        this.fail(ConnTestResult.CONN_ID_NEW, TcpConnection.ConnType.SERVER, ACCEPTED);
    }

    @Override
    protected void onDone(Status status) {
        try {
            ConnLogger.debug(TAG, getName() + ": Close client");
            mClient.close();
        } catch (IOException e) {
            //ignore
        }
        if (status == Status.TIMEDOUT) {
            ConnLogger.debug(TAG, getName() + " onDone, timed out");
            this.pass(ConnTestResult.CONN_ID_NEW, TcpConnection.ConnType.CLIENT, DONE);
        }
    }
}
