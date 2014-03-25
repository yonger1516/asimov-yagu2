package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.utils.conn.ConnLogger;
import com.seven.asimov.it.utils.conn.ConnSelector;
import com.seven.asimov.it.utils.conn.ConnTestResult;
import com.seven.asimov.it.utils.conn.HttpServer;

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
 * <li>2. App sends FIN while OC is connecting.</li>
 * <li>3. Check connection in logs.</li>
 * </ul>
 * Expected Result:
 * <ul>
 * <li>1. OC should setup client connection after receive request data.</li>
 * <li>2. OC sends FIN to network after receives FIN from App.</li>
 * <li>3. OC receives response from network side and forwards this response to App.</li>
 * <li>4. OC sends RST to network side and App after receives FIN/ACK from network side.</li>
 * </ul>
 * Expected Test Records:
 * <ul>
 * <li>1. XX : CLIENT : CONNECTING</li>
 * <li>2. XX : CLIENT : CONNECTED</li>
 * <li>3. XX : CLIENT : DATA_SENT</li>
 * <li>4. XX : SERVER : ACCEPTED</li>
 * <li>5. XX : CLIENT : FIN_SENT</li>
 * <li>6. XX : SERVER : DATA_RECEIVED</li>
 * <li>--- record sequence may not same start ---</li>
 * <li>7. XX : SERVER : DATA_SENT</li>
 * <li>8. XX : CLIENT : DATA_RECEIVED</li>
 * <li>9. XX : SERVER : FIN_RECEIVED</li>
 * <li>a. XX : SERVER : FIN_SENT</li>
 * <li>--- record sequence may not same start ---</li>
 * <li>b. XX : CLIENT : RST_RECEIVED</li>
 * <li>c. XX : CLIENT : DONE</li>
 * </ul>
 */
public class SendFinWhileOCConnectingTest extends SimpleTest {
    private static final String TAG = SendFinWhileOCConnectingTest.class.getSimpleName();

    public SendFinWhileOCConnectingTest(ConnSelector selector, HttpServer server) {
        super(TAG, selector, server);
    }

    @Override
    public void onAccepted(ServerSocketChannel serverChannel, SocketChannel newChannel) {
        ConnLogger.debug(TAG, getName() + "  onAccepted, channel:" + newChannel);
        addResult(ConnTestResult.CONN_ID_NEW, SERVER, ACCEPTED);
        try {
            mClient.sendFin();
            ConnLogger.debug(TAG, getName() + ": client sends Fin");
        } catch (Exception e) {
            ConnLogger.error(TAG, getName() + ": Send Fin fail", e);
            this.fail(mClient.getConnId(), ConnType.CLIENT, StateCode.SEND_FAILED);
        }
    }

    @Override
    public void onResponseReceived(int connId, HttpResponse response) {
        isClientReceivedResponse = true;
    }

    @Override
    public void onRstReceived(SelectableChannel channel) {
        super.onRstReceived(channel);
    }


}
