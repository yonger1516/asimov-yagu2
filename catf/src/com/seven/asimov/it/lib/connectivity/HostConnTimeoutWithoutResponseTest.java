package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.utils.conn.ConnLogger;
import com.seven.asimov.it.utils.conn.ConnSelector;
import com.seven.asimov.it.utils.conn.HttpServer;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.util.Timer;

import static com.seven.asimov.it.utils.conn.ConnTestResult.StateCode.SEND_FAILED;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnType.SERVER;

/**
 * <p>
 * Pre-requisite: OC startups and works fine.
 * </p>
 * Steps:
 * <ul>
 * <li>1. App setup connection with OC and then App sends a request to OC.</li>
 * <li>2. Host receives request data and doesn't send response back till connection timeout (10 seconds)</li>
 * <li>3. Check connection in logs.</li>
 * </ul>
 * Expected result:
 * <ul>
 * <li>1. OC should setup client connection after receive the request data and then send the request to Host.</li>
 * <li>2. Host sends FIN as connection timeout.</li>
 * <li>3. OC sends RST to both App and Host.</li>
 * </ul>
 * <ul>
 * <li>1. XX : CLIENT : CONNECTING</li>
 * <li>2. XX : CLIENT : CONNECTED</li>
 * <li>3. XX : CLIENT : DATA_SENT</li>
 * <li>4. XX : SERVER : ACCEPTED</li>
 * <li>5. XX : SERVER : DATA_RECEIVED</li>
 * <li>6. XX : SERVER : FIN_SENT</li>
 * <li>7. XX : SERVER : RST_RECEIVED</li>
 * <li>8. XX : CLIENT : RST_RECEIVED</li>
 * <li>9. XX : CLIENT : DONE</li>
 * </ul>
 */

public class HostConnTimeoutWithoutResponseTest extends SimpleTest {
    private static final String TAG = HostConnTimeoutWithoutResponseTest.class.getSimpleName();
    private static final int TIMEOUT = 10000;
    private Timer timer = new Timer();
    private int mConnId;

    public HostConnTimeoutWithoutResponseTest(ConnSelector selector, HttpServer server) {
        super(TAG, selector, server);
        mConnId = -1;
    }

    @Override
    public void onRequestReceived(int connId, HttpRequest request) {
        isServerReceivedRequest = true;
        mConnId = connId;
        timer.schedule(new SendFinTask(), TIMEOUT);
    }

    @Override
    public void onRstReceived(SelectableChannel channel) {
        super.onRstReceived(channel);
        if (isClientReceivedRst && isServerReceivedRst) {
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
            }
        }
    }

    class SendFinTask extends java.util.TimerTask {
        @Override
        public void run() {
            try {
                mServer.sendFin(mConnId);
                ConnLogger.debug(TAG, getName() + " Test server time out: server sends FIN");
            } catch (IOException e) {
                ConnLogger.error(TAG, getName() + ": server sends FIN failed", e);
                fail(mConnId, SERVER, SEND_FAILED);
            }
        }
    }

}
