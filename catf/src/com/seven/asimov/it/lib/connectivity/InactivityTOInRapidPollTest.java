package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.utils.conn.ConnLogger;
import com.seven.asimov.it.utils.conn.ConnSelector;

import java.nio.channels.SelectableChannel;

/**
 * <p>TC33</p>
 * <p/>
 * <p>
 * Inactivity Timer T.O. when the content is served from cache due to rapid manual poll start
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
 * <li>3. The content is served from cache due to rapid manual poll start.
 * <li>4. No activity leads to inactivity timer timeout.
 * </ul>
 * <p/>
 * Expected Result:
 * <ul>
 * <li>1. OC sends RST to network side and App after inactivity timer T.O.
 * </ul>
 */
public class InactivityTOInRapidPollTest extends RapidPollInSameConnTest {
    private static final String TAG = InactivityTOInRapidPollTest.class.getSimpleName();

    public InactivityTOInRapidPollTest(ConnSelector selector, String expectedBody, long pollPeriod) {
        this(TAG, selector, "asimov_it_time_out_rapid_poll", expectedBody, pollPeriod);
    }

    public InactivityTOInRapidPollTest(String name, ConnSelector selector, String testPath, String expectedBody, long pollPeriod) {
        super(name, selector, testPath, expectedBody, pollPeriod);

        //Socket Inactivity timeout did not care app<->OC sockets. 
        //It is enough that only checking if there is OC<->host sockets leak
        this.setClientExpectedRstCount(-1);
    }

    @Override
    protected void onServingFromCache() {
        ConnLogger.debug(TAG, getName() + ": onServingFromCache, do nothing");
    }

    @Override
    public void onRstReceived(SelectableChannel channel) {
        super.onRstReceived(channel);
    }


}
