package com.seven.asimov.it.tests.dispatchers.proxy;

import android.test.suitebuilder.annotation.LargeTest;
import com.seven.asimov.it.base.Base64;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.testcases.ProxyGATestCase;

import java.net.URLEncoder;

import static com.seven.asimov.it.base.constants.TFConstantsIF.CRLF;

public class MultilineHeadersProduction  extends ProxyGATestCase {

    @LargeTest
    public void testMultilineHeaderRequest() throws Throwable {
        final String RESOURCE_URI = "MultyHeaders_asimov_0002";
        final String uri = createTestResourceUri(RESOURCE_URI);

        String cookie = " __utma=1.1353845118.1327314935.1327314935.1327314935.1;" + CRLF + TAB
                + "__utmb=1.4.10.1327314935;" + CRLF + TAB + "__utmc=1;" + CRLF + TAB
                + "__utmz=1.1327314935.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none);";
        String expectedBody = "body";
        String expected = "HTTP/1.0 200 OK" + CRLF + CRLF + expectedBody;
        HttpRequest request = createRequest()
                .setMethod("GET")
                .setUri(uri)
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("Cookie", cookie)
                .addHeaderField("X-OC-Raw",
                        URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT))).getRequest();

        checkMiss(request, 1, expectedBody);
    }

    public void testMultilineHeaderResponse() throws Throwable {
        getMultilineHeaderResponse(false);
    }

    public void testMultilineHeaderResponseBypassOc() throws Throwable {
        getMultilineHeaderResponse(true);
    }
}
