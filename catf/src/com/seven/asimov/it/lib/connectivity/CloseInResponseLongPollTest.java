package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.base.HttpHeaderField;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.utils.conn.ConnSelector;

import java.nio.channels.SelectableChannel;

/**
 * <p>
 * TC35
 * </p>
 * <p/>
 * <p>
 * HTTP closure as "CONNECTION: close" in response header and the content is servered from cache due to long poll start.
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
 * <li>1. The response with "CONNECTION: close" should be saved in cache. Then OC sends RST to App, and client
 * connection has been closed by host.
 * <li>2. OC submits START_POLL task and get long poll start response.
 * <li>3. OC sends the response and RST to App.
 * </ul>
 * <p/>
 * ATTENTION:
 * <p/>
 * We can't detect if OC submits START_POLL task. So, we just check if cache marks exist in headers
 */
public class CloseInResponseLongPollTest extends CloseInResponseRapidPollTest {
    private static final String TAG = CloseInResponseLongPollTest.class.getSimpleName();

    public CloseInResponseLongPollTest(ConnSelector selector, String expectedBody) {
        super(TAG, selector, "asimov_it_close_response_long_poll", expectedBody, 0);
        this.setExpectedHostNewSocketCount(2);
        this.setClientExpectedRstCount(-1);
        this.setExpectedResponseCount(4);
        this.setClientExpectedRstFinCount(2);
        this.setClientExpectedFinCount(2);
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
