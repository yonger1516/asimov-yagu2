package com.seven.asimov.it.tests.caching.largecache;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.testcases.LargeCacheTestCase;
import org.apache.http.client.methods.HttpGet;

import java.util.ArrayList;
import java.util.List;

public class CacheFullProductionTests extends LargeCacheTestCase {

    /**
     * Steps
     *
     *  1. All 20 resources should be cached by RFC.
     *  2. Get 8 requests to 1st resource.
     *  3. Get 4 requests to 2nd resource.
     *  4. Get 9 requests to 3rd resource.
     *  5. Get 6 requests to 4th resource.
     *
     *  Expected results
     *
     *  1. We expect all first responses come from network.
     *  2. We expect all others responses come from OC cache.
     *  3. We expect R0.126-R0.128  from network. On R0.128 at 4 min we expect to start poll so no new requests/responses from network.
     *  4. We expected R0.130-R1.150 from cache.
     *  5. We expect IWC before R0.150. Polling should be stopped and deleted from cache
     *  6. We expect all responses from cache.
     */
    public void test_001_CacheFullInvalidate() throws Throwable {

        final String RESOURCE_URI_INVALIDATE = "asimov_it_full_cache_invalidate_200";

        final int countRequest = 20;
        final int size = 524030;

        List<HttpRequest> requests = new ArrayList<HttpRequest>(countRequest);

        int[] repeatRequests = new int[]{8, 4, 9, 6, 1, 5, 8, 7, 5, 2, 6, 4, 5, 3, 7, 4, 6, 8, 3, 4, 6};
        String[] bodies = new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "j", "k", "l", "m", "n", "o", "p",
                "q", "r", "s", "t", "v", "w", "x", "y", "z"};

        for (int i = 0; i < countRequest; i++){
            requests.add(createRequest().setUri(createTestResourceUri(RESOURCE_URI_INVALIDATE + i))
                    .setMethod(HttpGet.METHOD_NAME).addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("Cache-Control", "max-age=10000")
                    .addHeaderField("X-OC-AddHeader_Date", "GMT")
                    .addHeaderField("X-OC-ResponseContentSize", size + "," + bodies[i]).getRequest());
        }

        HttpRequest requestStart = createRequest()
                .setUri(createTestResourceUri(RESOURCE_URI_INVALIDATE + countRequest)).setMethod(HttpGet.METHOD_NAME)

                .getRequest();
        largeCacheTest(requests, null, requestStart, countRequest, repeatRequests, true);
    }

    /**
     * /*
     * Steps
     * 1. All 20 resources should be cached by RFC.
     * 2. Get 2 requests to every resource. (Should fill cache for 1 mb)
     * 3. Get 1 request with another requestId.
     *
     * Expected results
     *
     * 1. We expect all first responses come from network.
     * 2. We expect all others responses come from OC cache.
     * 3. We expect respone on last request comes from network.

     */
    public void test_002_CheckCachingWithFullCacheProduction() throws Throwable {

        String RESOURCE_URI = "regression_asimov_it_full_cache_200";
        int size = 524288;
        int countRequest = 20;

        List<String> uris = new ArrayList<String>(countRequest);

        for (int i = 0; i < countRequest; i++) {
            uris.add(createTestResourceUri(RESOURCE_URI + i));
        }

        HttpRequest request = createRequest().setUri(uris.get(0)).setMethod(HttpGet.METHOD_NAME)
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("Cache-Control", "max-age=1000").addHeaderField("X-OC-AddHeader_Date", "GMT")
                .addHeaderField("X-OC-ResponseContentSize", size + "," + "a").getRequest();

        largeCacheTest(null, uris, request, countRequest, null, false);
    }
}
