package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.utils.conn.ConnLogger;
import com.seven.asimov.it.utils.conn.ConnSelector;
import com.seven.asimov.it.utils.conn.ConnTestResult;
import com.seven.asimov.it.utils.conn.HttpServer;
import org.apache.commons.lang.exception.ExceptionUtils;

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
 * <li>2. App sends FIN without request data.</li>
 * <li>3. Check connection in logs.</li>
 * </ul>
 * Expected Result:
 * <ul>
 * <li>1.Make sure OC don't setup client connection.</li>
 * <li>2.OC receive FIN then send RST to app as no client connection.</li>
 * </ul>
 * Expected Test Records:
 * <ul>
 * <li>1. XX : CLIENT : CONNECTING</li>
 * <li>2. XX : CLIENT : CONNECTED</li>
 * <li>3. XX : CLIENT : FIN_SENT</li>
 * <li>4. XX : CLIENT : RST_RECEIVED</li>
 * <li>5. XX : CLIENT : DONE</li>
 * </ul>
 */
public class SendFinWithoutRequestTest extends SimpleTest {
    private static final String TAG = SendFinWithoutRequestTest.class.getSimpleName();

    public SendFinWithoutRequestTest(ConnSelector selector, HttpServer server) {
        super(TAG, selector, server);
    }

    @Override
    public void onConnected(SelectableChannel channel) {
        ConnLogger.debug(TAG, getName() + ": client connected");
        addResult(mClient.getConnId(), CLIENT, CONNECTED);
        try {
            mClient.sendFin();
            ConnLogger.debug(TAG, getName() + ": client sends FIN");
        } catch (IOException e) {
            this.fail(ConnTestResult.CONN_ID_NEW, ConnType.CLIENT, StateCode.SEND_FAILED);
        }
    }

    @Override
    public void onAccepted(ServerSocketChannel serverChannel, SocketChannel newChannel) {
        ConnLogger.debug(TAG, getName() + "  onAccepted, channel:" + newChannel);
        this.fail(ConnTestResult.CONN_ID_NEW, ConnType.SERVER, StateCode.ACCEPTED);
    }

    @Override
    public void onFinReceived(SelectableChannel channel) {
        super.onFinReceived(channel);
        if (isClientReceivedFin) {
            this.pass();
        }
    }

    @Override
    protected void onDone(Status status) {
        try {
            ConnLogger.debug(TAG, getName() + ": Close client");
            mClient.close();
        } catch (IOException e) {
            ConnLogger.error(TAG, ExceptionUtils.getStackTrace(e));
        }

        if (status == Status.TIMEDOUT) {
            if (!isClientReceivedRst) {
                this.fail("client didn't receive RST - may cause socket leak");
            }
        }
    }
}
