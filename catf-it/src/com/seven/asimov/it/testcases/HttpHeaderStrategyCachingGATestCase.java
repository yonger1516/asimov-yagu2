package com.seven.asimov.it.testcases;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class HttpHeaderStrategyCachingGATestCase extends TcpDumpTestCase {

    protected static final long MILLIS_IN_SECOND = 1000L;
    protected static final String METHOD_GET = "GET";
    protected static final String METHOD_POST = "POST";
    protected static final String TIMEZONE_GMT = "GMT";
    protected static final String HEADER_X_OC_ADD_HEADER_DATE = "X-OC-AddHeader_Date";

    protected void assertMissHit2(HttpRequest request) throws MalformedURLException, IOException,
            URISyntaxException {
        // this request shall be served from network
        int responseId = 0;
        checkMiss(request, ++responseId);
        // this request shall be served from cache
        checkHit(request, ++responseId);
    }

    protected void assertMissMiss2(HttpRequest request) throws MalformedURLException, IOException,
            URISyntaxException {
        int responseId = 0;

        checkMiss(request, ++responseId);
        checkMiss(request, ++responseId);
    }

    protected HttpRequest createPostRequest(String path, boolean addDate, String... headers) throws Exception {
        return createRequest(METHOD_POST, createTestResourcePath(path), addDate, headers);
    }

    protected HttpRequest createGetRequest(String path, boolean addDate, String... headers) throws Exception {
        PrepareResourceUtil.prepareResource(createTestResourceUri(path), false);
        return createRequest(METHOD_GET, createTestResourcePath(path), addDate, headers);
    }

    protected HttpRequest createRequest(String method, String path, boolean addDate, String... headers) {
        HttpRequest.Builder builder = createRequest().setUri(String.format("http://" + TEST_RESOURCE_HOST + "/%s", path)).setMethod(method);
        if (addDate) {
            builder = builder.addHeaderField(HEADER_X_OC_ADD_HEADER_DATE, TIMEZONE_GMT);
        }

        if (headers != null && headers.length > 0) {
            for (String header : headers) {
                String[] parts = header.split(":");
                builder = builder.addHeaderField(parts[0], parts.length > 1 ? parts[1] : null);
            }
        }
        return builder.getRequest();
    }
}
