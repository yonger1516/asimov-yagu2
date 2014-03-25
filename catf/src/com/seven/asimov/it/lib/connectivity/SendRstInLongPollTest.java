package com.seven.asimov.it.lib.connectivity;


import com.seven.asimov.it.utils.conn.ConnSelector;
import com.seven.asimov.it.utils.conn.HttpClient;

/**
 * <p>TC16</p>
 * <p>
 * "Pre-requisite: OC startups and works fine.
 * </p>
 * Steps:
 * <ul>
 * <li>1. App setup connection with OC, then App sends request to OC.
 * <li>2. App sends Rst after received all response data and the content is served from cache due to long poll start.
 * <li>3. Check connection in logs."
 * </ul>
 * <p/>
 * Expected Result:
 * <ul>
 * <li>1.OC should setup client connection after receive request data, then setup client connection and send request to network side.
 * <li>2.OC receives response from network side and forwards this response to app. OC sends RST to network side due to long poll start and keep server connection.
 * <li>3.OC is no action after receives RST from App."
 * </ul>
 */
public class SendRstInLongPollTest extends SendFinInLongPollTest {
    private static final String TAG = SendRstInLongPollTest.class.getSimpleName();

    public SendRstInLongPollTest(ConnSelector selector, String expectedBody) {
        super(TAG, selector, "asimov_it_send_rst_in_long_poll", expectedBody);

        this.setClientExpectedRstCount(0);
    }

    @Override
    protected void onResponseHandled(HttpClient client) {
        clientSendRst(client);
        try {
            Thread.sleep(2000); //sleep a while to let OC Rst
        } catch (InterruptedException e) {
        }

        nextConnect();
    }

}
