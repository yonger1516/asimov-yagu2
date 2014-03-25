package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.utils.conn.ConnLogger;
import com.seven.asimov.it.utils.conn.ConnSelector;
import com.seven.asimov.it.utils.conn.HttpServer;
import com.seven.asimov.it.utils.conn.TcpConnection;

import java.io.IOException;
import java.nio.channels.SelectableChannel;

import static com.seven.asimov.it.utils.conn.ConnTestResult.StateCode.SEND_FAILED;

/**
 * <p>
 * Pre-requisite: OC startups and works fine.
 * </p>
 * Steps:
 * <ul>
 * <li>1. App setup connection with OC and then App sends a request to OC.</li>
 * <li>2. Host sends RST after received the request data.</li>
 * <li>3. Check connection in logs.</li>
 * </ul>
 * Expected result:
 * <ul>
 * <li>1. OC should setup client connection after receive the request data and then send the request to Host.</li>
 * <li>2. OC receives RST from Host without response data after received the request.</li>
 * <li>3. OC sends RST to App.</li>
 * </ul>
 * <ul>
 * <li>1. XX : CLIENT : CONNECTING
 * <li>2. XX : CLIENT : CONNECTED
 * <li>3. XX : CLIENT : DATA_SENT
 * <li>4. XX : SERVER : ACCEPTED
 * <li>5. XX : SERVER : DATA_RECEIVED
 * <li>6. XX : SERVER : RST_SENT
 * <li>7. XX : CLIENT : RST_RECEIVED
 * <li>8. XX : CLIENT : DONE
 * </ul>
 */
public class HostSendRstWithoutResponseTest extends SimpleTest {
    private static final String TAG = HostSendRstWithoutResponseTest.class.getSimpleName();

    public HostSendRstWithoutResponseTest(ConnSelector selector, HttpServer server) {
        super(TAG, selector, server);
    }

    @Override
    public void onRequestReceived(int connId, HttpRequest request) {
        isServerReceivedRequest = true;
        try {
            mServer.sendRst(connId);
        } catch (IOException e) {
            ConnLogger.error(TAG, getName() + ": server sends RST failed", e);
            this.fail(connId, TcpConnection.ConnType.SERVER, SEND_FAILED);
        }
    }

    @Override
    public void onRstReceived(SelectableChannel channel) {
        super.onRstReceived(channel);
        if (isClientReceivedRst) {
            this.pass();
        }
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

            if (isServerReceivedRequest)
                this.pass();
        }
    }
}
