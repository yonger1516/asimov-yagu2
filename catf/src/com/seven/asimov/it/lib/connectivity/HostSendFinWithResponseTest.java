package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.base.HttpResponse;
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
 * <li>1. App setup connection with OC, then App sends request to OC.</li>
 * <li>2. Host sends FIN after response data and the content is not served from cache.</li>
 * <li>3. Check connection in logs.</li>
 * </ul>
 * Expected result:
 * <ul>
 * <li>1. OC should setup client connection after receive request data, then setup client connection and send request to network side.
 * <li>2. OC receives FIN after response data and the content is not served from cache, forward the response to App and then sends RST to both App and network side."
 * </ul>
 * Expected Test Records:
 * <ul>
 * <li>1. XX : CLIENT : CONNECTING</li>
 * <li>2. XX : CLIENT : CONNECTED</li>
 * <li>3. XX : CLIENT : DATA_SENT</li>
 * <li>4. XX : SERVER : ACCEPTED</li>
 * <li>5. XX : SERVER : DATA_RECEIVED</li>
 * <li>6. XX : SERVER : DATA_SENT</li>
 * <li>7. XX : SERVER : FIN_SENT</li>
 * <li>8. XX : CLIENT : DATA_RECEIVED</li>
 * <li>9. XX : SERVER : RST_RECEIVED</li>
 * <li>a. XX : CLIENT : RST_RECEIVED</li>
 * <li>b. XX : CLIENT : DONE</li>
 * </ul>
 */
public class HostSendFinWithResponseTest extends SimpleTest {
    private static final String TAG = HostSendFinWithResponseTest.class.getSimpleName();

    public HostSendFinWithResponseTest(ConnSelector selector, HttpServer server) {
        super(TAG, selector, server);
    }

    @Override
    public void onDataSent(SelectableChannel channel) {
        super.onDataSent(channel);
        int connId = mServer.getConnId(channel);
        if (connId != -1) {
            try {
                mServer.sendFin(connId);
            } catch (IOException e) {
                ConnLogger.error(TAG, getName() + ": server sends FIN failed", e);
                this.fail(connId, SERVER, SEND_FAILED);
            }
        }
    }

    @Override
    public void onResponseReceived(int connId, HttpResponse response) {
        isClientReceivedResponse = true;
    }

    @Override
    public void onRstReceived(SelectableChannel channel) {
        super.onRstReceived(channel);
        if (isServerReceivedRst && isClientReceivedResponse && isClientReceivedRst) {
            this.pass();
        }
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
            if (!isClientReceivedRst) {
                this.fail("client didn't receive RST - may cause socket leak");
            } else if (!isServerReceivedRst) {
                this.fail("server didn't receive RST - may cause socket leak");
            } else if (!isClientReceivedResponse) {
                this.fail("client didn't receive response");
            }
        }
    }
}
