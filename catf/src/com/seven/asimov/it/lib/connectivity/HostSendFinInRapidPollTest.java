package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.base.HttpHeaderField;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.utils.conn.ConnSelector;
import com.seven.asimov.it.utils.conn.HttpClient;

import java.nio.channels.SelectableChannel;

/**
 * <p>TC24</p>
 * <p>
 * "Pre-requisite: OC startups and works fine.
 * </p>
 * Steps:
 * <ul>
 * <li>1. App setup connection with OC, then App sends request to OC.
 * <li>2. Host sends FIN after response data and the content is served from cache due to rapid manual poll start.
 * <li>3. Check connection in logs.
 * </ul>
 * <p/>
 * Expected Result:
 * <ul>
 * <li>1. OC should setup client connection after receive request data, then setup client connection and send request to network side.
 * <li>2. OC receives FIN after response data, forward the request to App and the content is served from cache due to rapid manul poll, then sends RST to App and network side."
 * </ul>
 */
public class HostSendFinInRapidPollTest extends SendFinInRapidPollTest {
    private static final String TAG = HostSendFinInRapidPollTest.class.getSimpleName();

    public HostSendFinInRapidPollTest(ConnSelector selector, String expectedBody, long pollPeriod) {
        this(TAG, selector, "asimov_it_host_send_fin_in_rapid_poll", expectedBody, pollPeriod);
    }

    public HostSendFinInRapidPollTest(String name, ConnSelector selector, String testPath, String expectedBody, long pollPeriod) {
        super(name, selector, testPath, expectedBody, pollPeriod);
        this.setClientExpectedRstCount(MAX_REQUEST_ID - 1);

        //Host maybe closed OC<->Host connection when app received response, 
        //so, we did not check new socket count
        this.setExpectedHostNewSocketCount(-1);
    }

    @Override
    protected HttpRequest prepareRequest() {
        HttpRequest request = super.prepareRequest();
        request.addHeaderField(new HttpHeaderField("X-OC-CloseServerSocket", "FIN"));

        return request;
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
    public boolean verifyClientExpectedRst(int clientReceivedRstCount) {
        //Fix ASMV-6996, if polling starts before handle input code from host, we will not close app<->OC
        return clientReceivedRstCount >= MAX_REQUEST_ID - 2;
    }

    @Override
    public void onRstReceived(SelectableChannel channel) {
        super.onRstReceived(channel);
    }


}
