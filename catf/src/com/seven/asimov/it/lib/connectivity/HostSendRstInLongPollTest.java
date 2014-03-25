package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.base.HttpHeaderField;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.utils.conn.ConnSelector;

import java.nio.channels.SelectableChannel;

/**
 * <p>TC26</p>
 * <p>
 * "Pre-requisite: OC startups and works fine.
 * </p>
 * Steps:
 * <ul>
 * <li>1. App setup connection with OC, then App sends request to OC.
 * <li>2. Host sends RST after response data and the content is served from cache due to long poll start.
 * <li>3. Check connection in logs.
 * </ul>
 * <p/>
 * Expected Result:
 * <ul>
 * <li>1. OC should setup client connection after receive request data, then setup client connection and send request to network side.
 * <li>2. OC receives RST before close client connection due to long poll start, then sends RST to App.
 * </ul>
 */
public class HostSendRstInLongPollTest extends HostSendRstInRapidPollTest {
    private static final String TAG = HostSendRstInLongPollTest.class.getSimpleName();

    public HostSendRstInLongPollTest(ConnSelector selector, String expectedBody) {
        super(TAG, selector, "asimov_it_host_send_rst_in_long_poll", expectedBody, 0);
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
