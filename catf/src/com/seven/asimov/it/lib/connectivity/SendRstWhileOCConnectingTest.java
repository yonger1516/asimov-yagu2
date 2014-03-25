package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.utils.conn.ConnLogger;
import com.seven.asimov.it.utils.conn.ConnSelector;
import com.seven.asimov.it.utils.conn.ConnTestResult;
import com.seven.asimov.it.utils.conn.HttpServer;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import static com.seven.asimov.it.utils.conn.ConnTestResult.StateCode;
import static com.seven.asimov.it.utils.conn.ConnTestResult.StateCode.ACCEPTED;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnType;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnType.SERVER;

/**
 * <p>
 * Pre-requisite: OC startups and works fine.
 * </p>
 * Steps:
 * <ul>
 * <li>1. App setup connection with OC, then App sends request to OC.</li>
 * <li>2. App sends RST while OC is connecting.</li>
 * <li>3. Check connection in logs.</li>
 * </ul>
 * Expected Result:
 * <ul>
 * <li>1. OC should setup client connection after receive request data, then setup client connection and send request to
 * network side.</li>
 * <li>2. OC sends RST to network after receives RST from App.</li>
 * </ul>
 * Expected Test Records:
 * <ul>
 * <li>1. XX : CLIENT : CONNECTING</li>
 * <li>2. XX : CLIENT : CONNECTED</li>
 * <li>3. XX : CLIENT : DATA_SENT</li>
 * <li>4. XX : SERVER : ACCEPTED</li>
 * <li>5. XX : CLIENT : RST_SENT</li>
 * <li>6. XX : SERVER : DATA_RECEIVED</li>
 * <li>7. XX : SERVER : RST_RECEIVED</li>
 * <li>8. XX : SERVER : DONE</li>
 * </ul>
 */
public class SendRstWhileOCConnectingTest extends SimpleTest {
    private static final String TAG = SendRstWhileOCConnectingTest.class.getSimpleName();

    public SendRstWhileOCConnectingTest(ConnSelector selector, HttpServer server) {
        super(TAG, selector, server);
    }

    @Override
    public void onAccepted(ServerSocketChannel serverChannel, SocketChannel newChannel) {
        ConnLogger.debug(TAG, getName() + "  onAccepted, channel:" + newChannel);
        addResult(ConnTestResult.CONN_ID_NEW, SERVER, ACCEPTED);
        try {
            mClient.sendRst();
            ConnLogger.debug(TAG, getName() + ": client sends RST");
        } catch (Exception e) {
            ConnLogger.error(TAG, getName() + ": Send Rst fail", e);
            fail(mClient.getConnId(), ConnType.CLIENT, StateCode.SEND_FAILED);
        }
    }

    @Override
    public void onRstReceived(SelectableChannel channel) {
        super.onRstReceived(channel);
        if (isServerReceivedRst) {
            pass();
        }
    }

    @Override
    public void onRequestReceived(int connId, HttpRequest request) {
    }

    @Override
    public void onDone(Status status) {
        try {
            ConnLogger.debug(TAG, getName() + ": Close client");
            mClient.close();
        } catch (IOException e) {
            // ignore
        }

        if (status == Status.TIMEDOUT) {
            pass();
        }
    }
}
