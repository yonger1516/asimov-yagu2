package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.base.HttpHeaderField;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.utils.conn.ConnSelector;

import java.nio.channels.SelectableChannel;

/**
 * <p>TC38</p>
 * <p/>
 * <p>
 * HTTP closure as "CONNECTION: close" in request header and the content is servered from cache due to long poll start.
 * </p>
 * <p/>
 * <p>
 * Pre-requisite: OC startups and works fine.
 * </p>
 * <p/>
 * Steps:
 * <ul>
 * <li>1. App sends request and setup connection with OC.
 * <li>2. App sends same request 3 times and Host sends response lead to start long poll.
 * <li>3. App sends same request to OC.
 * </ul>
 * <p/>
 * Expected Result:
 * <ul>
 * <li>1. Before start long poll, every time OC receives response and RST from host at first, then sends response and RST to App.
 * <li>2. The response should be saved in cache.
 * <li>3. OC submits START_POLL task and get long poll  start response.
 * <li>4. OC sends the response from cache and RST to App.
 * </ul>
 */
public class CloseInRequestLongPollTest extends CloseInRequestRapidPollTest {
    private static final String TAG = CloseInRequestLongPollTest.class.getSimpleName();

    public CloseInRequestLongPollTest(ConnSelector selector, String expectedBody) {
        this(TAG, selector, "asimov_it_close_request_long_poll", expectedBody, 0);
    }

    public CloseInRequestLongPollTest(String name, ConnSelector selector, String testPath, String expectedBody, long pollPeriod) {
        super(name, selector, testPath, expectedBody, pollPeriod);
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
