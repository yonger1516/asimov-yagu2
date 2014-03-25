package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.utils.conn.ConnSelector;
import com.seven.asimov.it.utils.conn.HttpClient;

/**
 * <p>TC17</>
 * <p>
 * "Pre-requisite: OC startups and works fine.
 * </p>
 * Steps:
 * <ul>
 * <li>1. App setup connection with OC, then App sends request to OC.
 * <li>2. App sends RST after received all response data and the content is served from cache due to rapid manual poll start.
 * <li>3. Check connection in logs.
 * </ul>
 * <p/>
 * Expected Result:
 * <ul>
 * <li>1.OC should setup client connection after receive request data, then setup client connection and send request to network side.
 * <li>2.OC receives response from network side and forwards this response to app. OC sends FIN to network after receives FIN from App.
 * <li>3.OC sends RST to network after receives RST from App.
 * </ul>
 */
public class SendRstInRapidPollTest extends SendFinInRapidPollTest {
    private static final String TAG = SendRstInRapidPollTest.class.getSimpleName();

    public SendRstInRapidPollTest(ConnSelector selector, String expectedBody, long pollPeriod) {
        super(TAG, selector, "asimov_it_send_rst_in_rapid_poll", expectedBody, pollPeriod);
        this.setClientExpectedRstCount(0);
    }

    @Override
    protected void onResponseHandled(HttpClient client) {
        clientSendRst(client);
        nextConnect();
    }

}
