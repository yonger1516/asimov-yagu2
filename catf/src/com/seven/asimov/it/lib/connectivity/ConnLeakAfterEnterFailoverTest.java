package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.conn.ConnLogger;
import com.seven.asimov.it.utils.conn.ConnSelector;
import com.seven.asimov.it.utils.conn.HttpServer;
import com.seven.asimov.it.utils.conn.IConnListener;

import java.nio.channels.SelectableChannel;

/**
 * <p>TC56</p>
 * <p>Close all sockets once enter failover </p>
 * <p/>
 * "Pre-requisite: OC startups and works fine under mobile network.
 * Steps:
 * <ul>
 * <li>1. App sends request and setups connection with OC.
 * <li>2. Let OC enter failover status."
 * </ul>
 * <p/>
 * Expected result:
 * <ul>
 * <li>1. OC sends RST to all connections.
 * </ul>
 */
public class ConnLeakAfterEnterFailoverTest extends ConnLeakAfterSendFinTest implements IConnListener {
    private static final String TAG = ConnLeakAfterEnterFailoverTest.class.getSimpleName();

    private static final int TEST_CONN_COUNT = 15;
    private static final int RESPONSE_COUNT_BEFORE_INOVKE_FAILOVER = 15;

    private boolean isInvokedFailover;

    public ConnLeakAfterEnterFailoverTest(ConnSelector selector, HttpServer server) {
        super(TAG, selector, server);

        //Not care rst, response in this case. Just check socket leak is enough
        this.setClientExpectedRstCount(-1);
        this.setServerExpectedRstCount(-1);
        this.setExpectedResponseCount(-1);
    }

    @Override
    protected void onAllFirstTestConnClosed() {
    }

    @Override
    protected boolean onStart() {
        return setupConnections(TEST_CONN_COUNT);
    }

    @Override
    public void onResponseReceived(int connId, HttpResponse response) {
        ConnLogger.debug(TAG, appendLogPrefix(connId, "onResponseReceived"));
        mRecevicedResponseCount++;

        if (!isInvokedFailover &&
                mRecevicedResponseCount >= RESPONSE_COUNT_BEFORE_INOVKE_FAILOVER) {
            isInvokedFailover = true;

            TestUtil.sendOCControlCommand("set?tempFailover&reason=dnsfail");
        }
    }

    @Override
    protected void onDone(Status status) {
        //Wait for failover start
        TestUtil.sleep(30 * 1000);

        super.onDone(status);

        // Wait for Failover to end
        TestUtil.sleep(2 * 60 * 1000);
    }

    @Override
    public void onRstReceived(SelectableChannel channel) {
        super.onRstReceived(channel);
    }


}               
