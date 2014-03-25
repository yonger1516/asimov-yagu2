package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.base.HttpHeaderField;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.utils.conn.ConnSelector;

import java.nio.channels.SelectableChannel;

/**
 * <p>TC13</p>
 * <p>
 * "Pre-requisite: OC startups and works fine.
 * </p>
 * Steps:
 * <ul>
 * <li>1. App setup connection with OC, then App sends request to OC.
 * <li>2. App sends FIN after received all response data and the content is served from cache due to long poll start.
 * <li>3. Check connection in logs."
 * </ul>
 * <p/>
 * Expected Result:
 * <ul>
 * <li>1.OC should setup client connection after receive request data, then setup client connection and send request to network side.
 * <li>2.OC receives response from network side and forwards this response to app. OC sends RST to network side due to long poll start and keep server connection.
 * <li>3.OC sends RST to app after receives FIN from app."
 * </ul>
 */
public class SendFinInLongPollTest extends SendFinInRapidPollTest {
    private static final String TAG = SendFinInLongPollTest.class.getSimpleName();

    public static final String TEST_PATH = "asimov_it_send_fin_in_long_poll";

    public SendFinInLongPollTest(ConnSelector selector, String expectedBody) {
        this(TAG, selector, "asimov_it_send_fin_in_long_poll", expectedBody);
    }

    public SendFinInLongPollTest(String name, ConnSelector selector, String testPath, String expectedBody) {
        super(name, selector, testPath, expectedBody, 0l);
        this.setExpectedHostNewSocketCount(2);
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
