package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.conn.ConnLogger;
import com.seven.asimov.it.utils.conn.ConnSelector;
import com.seven.asimov.it.utils.conn.HttpClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.SelectableChannel;

/**
 * <p>
 * TC59
 * </p>
 * <p/>
 * <p>
 * Pre-requisite: OC startups and works fine.
 * </p>
 * Steps:
 * <ul>
 * <li>1. APP setup connection with OC, then APP sends request to OC.</li>
 * <li>2. APP sends same request 3 times per 1 minutes then the content is served from cache due to rapid manual poll
 * start.</li>
 * <li>3. Check connection in logs.</li>
 * </ul>
 * Expected Result:
 * <ul>
 * <li>1. OC should setup client connection after receive request data, then setup client connection and send request to
 * network side.</li>
 * <li>2. OC keeps client and server connection after rapid manual poll start.</li>
 * </ul>
 */

public class RapidPollInSameConnTest extends SendFinInRapidPollTest {
    private static final String TAG = RapidPollInSameConnTest.class.getSimpleName();
    private static final long PERIOD = 60 / 3;
    protected HttpClient mClient;

    public RapidPollInSameConnTest(ConnSelector selector, String expectedBody) {
        this(TAG, selector, "asimov_it_rapid_poll_in_same_conn", expectedBody, PERIOD);
    }

    public RapidPollInSameConnTest(String name, ConnSelector selector, String testPath, String expectedBody,
                                   long pollPeriod) {
        super(name, selector, testPath, expectedBody, pollPeriod);
        // this.setClientExpectedRstCount(1);
        this.setClientExpectedRstFinCount(1);
        this.setExpectedHostNewSocketCount(1);
    }

    public boolean verifyClientExpectedRstFin(int clientReceivedRstFinCount) {
        return getClientExpectedRstCount() < 0 || clientReceivedRstFinCount == getClientExpectedRstFinCount();
    }

    @Override
    protected boolean checkIfFinished(String where) {
        if (mRequestId < MAX_REQUEST_ID) {
            return false;
        }

        if ((getExpectedResponseCount() < 0 || mRecevicedResponseCount == getExpectedResponseCount())
                && (getExpectedHostNewSocketCount() < 0 || mSocketsMonitor.getTotalNewHostSockets().size() == getExpectedHostNewSocketCount())
                && verifyClientExpectedRstFin(mClientReceivedRstCount + mClientReceivedFinCount)
                // Currently app<->OC socket always keep in long/rapid poll, even though inactivity timeout.
                // So, it is enough that only checking if there is OC<->host sockets leak
                && mSocketsMonitor.getCurrentLeakedHostSockets().isEmpty()) {

            ConnLogger.debug(TAG, "Test passed in " + where);
            this.pass();
            return true;
        }

        return false;
    }

    @Override
    protected void onServingFromCache() {
        ConnLogger.debug(TAG, "onServingFromCache");
        if (!this.mSocketsMonitor.getCurrentLeakedHostSockets().isEmpty()) {
            fail("OC<->Host socket not should be keep");
        }

        clientSendFin(mClient);
    }

    @Override
    protected void onResponseHandled(HttpClient client) {
        nextConnect();
    }

    @Override
    protected void onRstHandled(HttpClient client) {
        // Do nothing
    }

    @Override
    protected void nextConnect() {
        if (mRequestId < MAX_REQUEST_ID) {
            sendRequest(mClient);
        }
    }

    @Override
    protected HttpClient sendRequestInNewConn(boolean isFirstTime) {
        if (isFirstTime) {
            mClient = setupConnection(true);
            return mClient;
        } else {
            ConnLogger.error(TAG, "Only support setup one connection in current test:" + getName());
            return null;
        }
    }

    protected final HttpClient setupConnection(boolean ignoreConnId) {
        String ip = TFConstantsIF.OC_HTTP_PROXY_ADDRESS;
        try {
            InetAddress address = InetAddress.getByName(AsimovTestCase.TEST_RESOURCE_HOST);
            ip = address.getHostAddress();
        } catch (UnknownHostException e) {
            System.out.println("Failed to resolve ip from hostname = " + AsimovTestCase.TEST_RESOURCE_HOST);
        }
        System.out.println(" IP (TEST_RESOURCE_HOST) = " + ip);
        return setupConnection(true, ip, 80);
    }

    @Override
    public void onRstReceived(SelectableChannel channel) {
        super.onRstReceived(channel);
    }

}
