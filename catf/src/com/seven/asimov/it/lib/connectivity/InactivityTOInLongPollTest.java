package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.base.HttpHeaderField;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.utils.conn.ConnSelector;

import java.nio.channels.SelectableChannel;

/**
 * <p>TC33</p>
 * <p/>
 * <p>
 * Inactivity Timer T.O. when the content is served from cache due to long poll start
 * </p>
 * <p/>
 * <p>
 * Pre-requisite: OC startups and works fine.
 * </p>
 * <p/>
 * Steps:
 * <ul>
 * <li>1. App setup connection with OC, then App sends request to OC.
 * <li>2. Setup client connection then get response from network side.
 * <li>3. The content is served from cache due to long poll start.
 * <li>4. No activity leads to inactivity timer timeout.
 * </ul>
 * <p/>
 * Expected Result:
 * <ul>
 * <li>1. OC sends RST to network side due to long poll start.
 * <li>2. OC sends RST to App after inactivity timer T.O.
 * </ul>
 */
public class InactivityTOInLongPollTest extends InactivityTOInRapidPollTest {
    private static final String TAG = InactivityTOInLongPollTest.class.getSimpleName();

    public InactivityTOInLongPollTest(ConnSelector selector, String expectedBody) {
        super(TAG, selector, "asimov_it_time_out_long_poll", expectedBody, 0);
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
