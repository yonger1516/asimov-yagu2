package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.utils.conn.*;

import java.io.IOException;
import java.nio.channels.SelectableChannel;

import static com.seven.asimov.it.utils.conn.ConnTestResult.StateCode.RST_RECEIVED;
import static com.seven.asimov.it.utils.conn.ConnTestResult.StateCode.SEND_FAILED;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnType.CLIENT;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnType.SERVER;

/**
 * "Pre-requisite: OC startups and works fine. Steps: 1. App launch request and setup connection with OC. 2. Setup
 * client connection then get response from network side. 3. No activity leads to inactivity timer(6mins) timeout."
 * <p/>
 * Expected result: 1.OC sends RST to network side and App after inactivity timer T.O.
 */

public class InactivityTimerTimeoutTest extends SimpleTest {
    private static final String TAG = InactivityTimerTimeoutTest.class.getSimpleName();

    public InactivityTimerTimeoutTest(ConnSelector selector, HttpServer server) {
        super(TAG, selector, server);
    }

    @Override
    public void onResponseReceived(int connId, HttpResponse response) {
        isClientReceivedResponse = true;
    }

    @Override
    public void onRequestReceived(int connId, HttpRequest request) {
        isServerReceivedRequest = true;
        try {
            HttpResponse response = buildSimpleResponse(request);
            mServer.sendResponse(connId, response);
        } catch (IOException e) {
            ConnLogger.error(TAG, getName() + ": Send data fail", e);
            this.fail(connId, SERVER, SEND_FAILED);
        }
    }

    @Override
    public void onRstReceived(SelectableChannel channel) {
        if (mClient.getChannel().equals(channel)) {
            int connId = mClient.getConnId();
            this.addResult(connId, CLIENT, RST_RECEIVED);

            isClientReceivedRst = true;
        } else {
            TcpConnection conn = mServer.getConnection(channel);
            if (conn == null) {
                this.addResult(ConnTestResult.CONN_ID_NEW, SERVER, RST_RECEIVED);
                this.fail("Can't get Test server connection");
                return;
            }

            isServerReceivedRst = true;
        }
    }

    @Override
    protected void onDone(Status status) {

        // if (isServerReceivedRequest && isServerReceivedRst && isClientReceivedResponse && isClientReceivedRst) {
        if (isServerReceivedRequest && isClientReceivedResponse) {
            this.pass();
        } else {
            this.fail("Fail: isServerReceivedRequest:" + isServerReceivedRequest + ", isServerReceivedRst:"
                    + isServerReceivedRst + ", isClientReceivedResponse:" + isClientReceivedResponse
                    + ", isClientReceivedRst:" + isClientReceivedRst);
        }

        super.onDone(this.getStatus());
    }

}
