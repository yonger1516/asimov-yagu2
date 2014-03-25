package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.utils.conn.ConnSelector;
import com.seven.asimov.it.utils.conn.HttpServer;

import java.nio.channels.SelectableChannel;

/**
 * <p>
 * Pre-requisite: OC startups and works fine.
 * </p>
 * Steps:
 * <ul>
 * <li>1. App setup connection with OC, then App sends request to OC.
 * <li>2. App sends FIN after received all response data and the content is not served from cache.
 * <li>3. Check connection in logs."
 * </ul>
 * Expected Result:
 * <ul>
 * <li>1. OC should setup client connection after receive request data and send request to network side.
 * <li>2. OC receives response from network side and forwards this response to App.
 * <li>3. OC receives FIN from App and forwards it to network side.
 * <li>4. OC sends RST to network side and App after receives FIN/ACK from network side.
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
 * <li>8. XX : CLIENT : FIN_SENT</li>
 * <li>9. XX : SERVER : FIN_RECEIVED</li>
 * <li>a. XX : SERVER : FIN_SENT</li>
 * <li>b. XX : CLIENT : RST_RECEIVED</li>
 * <li>c. XX : CLIENT : DONE</li>
 * </ul>
 */
public class SendFinAfterReceivedResponseTest extends SimpleTest {
    private static final String TAG = SendFinAfterReceivedResponseTest.class.getSimpleName();

    public SendFinAfterReceivedResponseTest(ConnSelector selector, HttpServer server) {
        super(TAG, selector, server);
    }

    @Override
    public void onRstReceived(SelectableChannel channel) {
        super.onRstReceived(channel);
    }

}
