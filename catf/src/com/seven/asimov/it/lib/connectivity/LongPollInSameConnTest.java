package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.base.HttpHeaderField;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.utils.conn.ConnLogger;
import com.seven.asimov.it.utils.conn.ConnSelector;

import java.nio.channels.SelectableChannel;

/**
 * <p>TC57</p>
 * <p/>
 * <p>
 * Pre-requisite: OC startups and works fine.
 * </p>
 * Steps:
 * <ul>
 * <li>1. APP setup connection with OC, then APP sends request to OC.</li>
 * <li>2. App sends same request 3 times per 5 minutes then the content is served from cache due to long poll start.</li>
 * <li>3. Check connection in logs.</li>
 * </ul>
 * Expected Result:
 * <ul>
 * <li>1. OC should setup client connection after receive request data, then setup client connection and send request to network side.</li>
 * <li>2. OC sends RST to host after long poll start and keeps server connection.</li>
 * </ul>
 */

public class LongPollInSameConnTest extends RapidPollInSameConnTest {
    private static final String TAG = LongPollInSameConnTest.class.getSimpleName();

    public LongPollInSameConnTest(ConnSelector selector, String expectedBody) {
        super(TAG, selector, "asimov_it_long_poll_in_same_conn", expectedBody, 0);
    }

    @Override
    protected void onServingFromCache() {
        ConnLogger.debug(TAG, "onServingFromCache");
        if (!this.mSocketsMonitor.getCurrentLeakedHostSockets().isEmpty()) {
            fail("OC<->Host socket should be closed");
        }

        clientSendFin(mClient);
    }

    @Override
    protected HttpRequest prepareRequest() {
        HttpRequest request = super.prepareRequest();
        request.addHeaderField(new HttpHeaderField("X-OC-Sleep", DEFAULT_SLEEP_FOR_LONG_POLL));

        return request;
    }

    @Override
    public void onRstReceived(SelectableChannel channel) {
        super.onRstReceived(channel);
    }


}
