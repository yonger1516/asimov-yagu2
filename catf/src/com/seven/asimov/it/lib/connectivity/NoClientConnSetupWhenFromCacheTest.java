package com.seven.asimov.it.lib.connectivity;

import com.seven.asimov.it.utils.conn.ConnSelector;

import java.nio.channels.SelectableChannel;

/**
 * <p>TC02,TC61</p>
 * <p/>
 * <p>
 * Pre-requisite: OC startups and works fine. The content is served from cache.
 * </p>
 * Steps:
 * <ul>
 * <li>1. APP setup connection with OC, then APP sends request to OC.</li>
 * <li>2. The content is served from cache.</li>
 * <li>3. Check connection in logs.</li>
 * </ul>
 * Expected Result:
 * <ul>
 * <li>1. OC should not setup client connection due to the content is served from cache.</li>
 * </ul>
 */
public class NoClientConnSetupWhenFromCacheTest extends SendFinInRapidPollTest {
    private static final String TAG = NoClientConnSetupWhenFromCacheTest.class.getSimpleName();

    public NoClientConnSetupWhenFromCacheTest(ConnSelector selector, String expectedBody, long pollPeriod) {
        super(TAG, selector, "asimov_it_no_conn_setup_when_from_cache", expectedBody, pollPeriod);
    }

    @Override
    public void onRstReceived(SelectableChannel channel) {
        super.onRstReceived(channel);
    }


}
