package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.utils.conn.ConnLogger;
import com.seven.asimov.it.utils.conn.ConnSelector;
import com.seven.asimov.it.utils.conn.ConnTestResult;
import com.seven.asimov.it.utils.conn.HttpServer;

import java.io.IOException;
import java.nio.channels.SelectableChannel;

import static com.seven.asimov.it.utils.conn.ConnTestResult.StateCode.CONNECTED;
import static com.seven.asimov.it.utils.conn.ConnTestResult.StateCode.SEND_FAILED;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnType.CLIENT;

/**
 * <p>
 * Pre-requisite: OC startups and works fine.
 * </p>
 * Steps:
 * <ul>
 * <li>1. App setup connection with OC.</li>
 * <li>2. App sends FIN as well as request data.</li>
 * <li>3. Check connection in logs.</li>
 * </ul>
 * Expected Result:
 * <ul>
 * <li>1.OC should setup client connection after receive FIN with request data, then send request and FIN to network
 * side.</li>
 * <li>2.OC receive response from network side and forward this response to app.</li>
 * <li>3.OC send RST to network side and app after receive FIN from network side.</li>
 * </ul>
 * Expected Test Records:
 * <ul>
 * <li>1. XX : CLIENT : CONNECTING</li>
 * <li>2. XX : CLIENT : CONNECTED</li>
 * <li>3. XX : CLIENT : DATA_SENT</li>
 * <li>4. XX : CLIENT : FIN_SENT</li>
 * <li>5. XX : SERVER : ACCEPTED</li>
 * <li>6. XX : SERVER : DATA_RECEIVED</li>
 * <li>--- record sequence may not same start ---</li>
 * <li>7. XX : SERVER : DATA_SENT</li>
 * <li>8. XX : CLIENT : DATA_RECEIVED</li>
 * <li>9. XX : SERVER : FIN_RECEIVED</li>
 * <li>a. XX : SERVER : FIN_SENT</li>
 * <li>--- record sequence may not same end ---</li>
 * <li>b. XX : CLIENT : RST_RECEIVED</li>
 * <li>c. XX : CLIENT : DONE</li>
 * </ul>
 */
public class SendFinWithRequestTest extends SimpleTest {
    private static final String TAG = SendFinWithRequestTest.class.getSimpleName();

    public SendFinWithRequestTest(ConnSelector selector, HttpServer server) {
        super(TAG, selector, server);
    }

    @Override
    public void onConnected(SelectableChannel channel) {
        ConnLogger.debug(TAG, getName() + ": client connected");
        addResult(mClient.getConnId(), CLIENT, CONNECTED);
        try {
            // HttpRequest simpleRequest = buildSimpleRequest();
            HttpRequest simpleRequest = buildSimpleRequest(null, false);
            mClient.sendRequest(simpleRequest);
        } catch (IOException e) {
            ConnLogger.error(TAG, getName() + ": Send request failed", e);
            this.fail(mClient.getConnId(), CLIENT, SEND_FAILED);
        }
    }

    @Override
    public void onDataSent(SelectableChannel channel) {
        super.onDataSent(channel);
        if (mClient.getChannel().equals(channel)) {
            try {
                mClient.sendFin();
                ConnLogger.debug(TAG, getName() + ": client sends FIN");
            } catch (IOException e) {
                this.fail(ConnTestResult.CONN_ID_NEW, CLIENT, SEND_FAILED);
            }
        }
    }

    @Override
    public void onResponseReceived(int connId, HttpResponse response) {
        isClientReceivedResponse = true;
    }
}
