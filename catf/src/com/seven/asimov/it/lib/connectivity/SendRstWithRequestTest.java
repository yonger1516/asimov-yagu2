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
import static com.seven.asimov.it.utils.conn.ConnTestResult.StateCode.CONNECTED;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnType;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnType.CLIENT;

/**
 * <p>
 * Pre-requisite: OC startups and works fine.
 * </p>
 * Steps:
 * <ul>
 * <li>1. App setup connection with OC.</li>
 * <li>2. App sends request data and RST.</li>
 * <li>3. Check connection in logs.</li>
 * </ul>
 * Expected Result:
 * <ul>
 * <li>1. OC may or may not setup client connection depends on how fast it processes the request.</li>
 * <li>2.1. if OC didn't process the request while RST received
 * - OC receives RST and no more actions.</li>
 * <li>2.2. if OC processed the request while RST received
 * - OC forwards the request and closes both side connections by reset.</li>
 * </ul>
 * <p>Excepted Test Records:</p>
 * Case 1: OC didn't process the request while RST received.
 * <ul>
 * <li>1. XX : CLIENT : CONNECTING</li>
 * <li>2. XX : CLIENT : CONNECTED</li>
 * <li>3. XX : CLIENT : DATA_SENT</li>
 * <li>4. XX : CLIENT : RST_SENT</li>
 * <li>5. XX : CLIENT : DONE</li>
 * </ul>
 * Case 2: OC processed the request while RST received.
 * <ul>
 * <li>1. XX : CLIENT : CONNECTING</li>
 * <li>2. XX : CLIENT : CONNECTED</li>
 * <li>3. XX : CLIENT : DATA_SENT</li>
 * <li>4. XX : CLIENT : RST_SENT</li>
 * <li>5. XX : SERVER : ACCEPTED</li>
 * <li>6. XX : SERVER : DATA_RECEIVED</li>
 * <li>7: XX : SERVER : RST_RECEIVED</li>
 * <li>8: XX : SERVER : DONE</li>
 * </ul>
 * </ul>
 */
public class SendRstWithRequestTest extends SimpleTest {
    private static final String TAG = SendRstWithRequestTest.class.getSimpleName();
    private boolean isAccepted;

    public SendRstWithRequestTest(ConnSelector selector, HttpServer server) {
        super(TAG, selector, server);
    }

    @Override
    public void onConnected(SelectableChannel channel) {
        ConnLogger.debug(TAG, getName() + ": client connected");
        addResult(mClient.getConnId(), CLIENT, CONNECTED);
        try {
            HttpRequest simpleRequest = buildSimpleRequest();
            mClient.sendRequest(simpleRequest);
        } catch (IOException e) {
            ConnLogger.error(TAG, getName() + ": Send data fail", e);
            this.fail(mClient.getConnId(), ConnType.CLIENT, StateCode.SEND_FAILED);
        }
    }

    @Override
    public void onDataSent(SelectableChannel channel) {
        super.onDataSent(channel);
        if (mClient.getChannel().equals(channel)) {
            try {
                mClient.sendRst();
                ConnLogger.debug(TAG, getName() + ": client sends RST");
            } catch (IOException e) {
                fail(ConnTestResult.CONN_ID_NEW, ConnType.CLIENT, StateCode.SEND_FAILED);
            }
        }
    }

    @Override
    public void onAccepted(ServerSocketChannel serverChannel, SocketChannel channel) {
        super.onAccepted(serverChannel, channel);
        isAccepted = true;
    }

    @Override
    public void onRstReceived(SelectableChannel channel) {
        super.onRstReceived(channel);
        if (isAccepted && isServerReceivedRst) {
            pass();
        }
    }

    @Override
    public void onRequestReceived(int connId, HttpRequest request) {
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
            if (!isAccepted) {
                pass(ConnTestResult.CONN_ID_NEW, ConnType.CLIENT, StateCode.DONE);
            } else {
                if (!isServerReceivedRst) {
                    fail("server didn't receive RST - may cause socket leak");
                }
            }
        }
    }
}
