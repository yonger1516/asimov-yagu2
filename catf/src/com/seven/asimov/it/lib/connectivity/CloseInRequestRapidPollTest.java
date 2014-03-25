package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.utils.conn.ConnSelector;
import com.seven.asimov.it.utils.conn.HttpClient;

import java.nio.channels.SelectableChannel;

/**
 * <p>TC39</p>
 * <p/>
 * <p>
 * HTTP closure as "CONNECTION: close" in request header and the content is servered from cache due to rapid manual poll.
 * </p>
 * <p/>
 * <p>
 * Pre-requisite: OC startups and works fine.
 * </p>
 * <p/>
 * Steps:
 * <ul>
 * <li>1. App sends request and setup connection with OC.
 * <li>2. App sends same request 3 times and Host sends response lead to start rapid manual poll.
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
public class CloseInRequestRapidPollTest extends SendFinInRapidPollTest {
    private static final String TAG = CloseInRequestRapidPollTest.class.getSimpleName();

    public CloseInRequestRapidPollTest(ConnSelector selector, String expectedBody, long pollPeriod) {
        this(TAG, selector, "asimov_it_close_request_rapid_poll", expectedBody, pollPeriod);
    }

    public CloseInRequestRapidPollTest(String name, ConnSelector selector, String testPath, String expectedBody, long pollPeriod) {
        super(name, selector, testPath, expectedBody, pollPeriod);

        //In this case, host maybe closed OC<->Host connection when app received response, 
        //so, we did not check new socket 
        this.setExpectedHostNewSocketCount(-1);
    }

    protected HttpRequest prepareRequest() {
        return buildSimpleRequest(false);
    }

    @Override
    protected void onResponseHandled(HttpClient client) {
        nextConnect();
    }

    @Override
    protected void onRstHandled(HttpClient client) {
        //Do nothing
    }

    @Override
    public void onRstReceived(SelectableChannel channel) {
        super.onRstReceived(channel);
    }

}
