package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.utils.conn.ConnLogger;
import com.seven.asimov.it.utils.conn.ConnSelector;
import com.seven.asimov.it.utils.conn.HttpServer;

import java.io.IOException;
import java.nio.channels.SelectableChannel;

import static com.seven.asimov.it.utils.conn.ConnTestResult.StateCode.SEND_FAILED;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnType.SERVER;

/**
 * <p>
 * Pre-requisite: OC startups and works fine.
 * </p>
 * Steps:
 * <ul>
 * <li>1. App setup connection with OC and then App sends request to OC.</li>
 * <li>2. Host sends FIN after received request data.
 * <li>3. Check connection in logs.</li>
 * </ul>
 * Expected result:
 * <ul>
 * <li>1. OC should setup client connection after receive request data and send request to Host.</li>
 * <li>2. OC receives FIN from Host without response data.</li>
 * <li>4. OC sends RST to both side (Host and App).</li>
 * </ul>
 * <ul>
 * <li>1. XX : CLIENT : CONNECTING
 * <li>2. XX : CLIENT : CONNECTED
 * <li>3. XX : CLIENT : DATA_SENT
 * <li>4. XX : SERVER : ACCEPTED
 * <li>5. XX : SERVER : DATA_RECEIVED
 * <li>6. XX : SERVER : FIN_SENT
 * <li>7. XX : SERVER : RST_RECEIVED
 * <li>8. XX : CLIENT : RST_RECEIVED
 * <li>9. XX : CLIENT : DONE
 * </ul>
 */
public class HostSendFinWithoutResponseTest extends SimpleTest {
    private static final String TAG = HostSendFinWithoutResponseTest.class.getSimpleName();

    public HostSendFinWithoutResponseTest(ConnSelector selector, HttpServer server) {
        super(TAG, selector, server);
    }

    @Override
    public void onRequestReceived(int connId, HttpRequest request) {
        isServerReceivedRequest = true;
        try {
            mServer.sendFin(connId);
        } catch (IOException e) {
            ConnLogger.error(TAG, getName() + ": server sends FIN failed", e);
            this.fail(connId, SERVER, SEND_FAILED);
        }
    }

    @Override
    public void onRstReceived(SelectableChannel channel) {
        super.onRstReceived(channel);
        // if (isServerReceivedRst && isClientReceivedRst) {
        if (isServerReceivedRst) {
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
            if (!isServerReceivedRst) {
                fail("server didn't receive RST - may cause socket leak");
            } else if (!isClientReceivedRst) {
                fail("client didn't receive RST - may cause socket leak");
            }
        }
    }
}
