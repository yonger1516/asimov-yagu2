package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.utils.conn.ConnLogger;
import com.seven.asimov.it.utils.conn.ConnSelector;
import com.seven.asimov.it.utils.conn.HttpServer;

import java.io.IOException;
import java.nio.channels.SelectableChannel;

import static com.seven.asimov.it.utils.conn.ConnTestResult.StateCode.SEND_FAILED;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnType.CLIENT;

/**
 * <p>
 * Pre-requisite: OC startups and works fine.
 * </p>
 * Steps:
 * <ul>
 * <li>1. App setup connection with OC, then App sends request to OC.
 * <li>2. App sends RST after received all response data and the content is not served from cache.
 * <li>3. Check connection in logs."
 * </ul>
 * Expected Result:
 * <ul>
 * <li>1. OC should setup client connection after receive request data and send request to network side.
 * <li>2. OC receives response from network side and forwards this response to App.
 * <li>3. OC receives RST from App and reset network side.
 * </ul>
 * Expected Test Records:
 * <ul>
 * <li>1. XX : CLIENT : CONNECTING</li>
 * <li>2. XX : CLIENT : CONNECTED</li>
 * <li>3. XX : CLIENT : DATA_SENT</li>
 * <li>4. XX : SERVER : ACCEPTED</li>
 * <li>5. XX : SERVER : DATA_RECEIVED</li>
 * <li>6. XX : SERVER : DATA_SENT</li>
 * <li>7. XX : CLIENT : DATA_RECEIVED</li>
 * <li>8. XX : CLIENT : RST_SENT</li>
 * <li>9. XX : SERVER : RST_RECEIVED</li>
 * <li>a. XX : CLIENT : DONE</li>
 * </ul>
 */

public class SendRstAfterReceivedResponseTest extends SimpleTest {
    private static final String TAG = SendRstAfterReceivedResponseTest.class.getSimpleName();

    public SendRstAfterReceivedResponseTest(ConnSelector selector, HttpServer server) {
        super(TAG, selector, server);
    }

    @Override
    public void onResponseReceived(int connId, HttpResponse response) {
        isClientReceivedResponse = true;
        try {
            ConnLogger.debug(TAG, getName() + ": client sends rst");
            mClient.sendRst();
        } catch (IOException e) {
            ConnLogger.error(TAG, getName() + ": Send FIN fail", e);
            this.fail(mClient.getConnId(), CLIENT, SEND_FAILED);
        }
    }

    @Override
    public void onRstReceived(SelectableChannel channel) {
        super.onRstReceived(channel);
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
            if (isClientReceivedResponse)
                this.pass();
        }
    }
}
