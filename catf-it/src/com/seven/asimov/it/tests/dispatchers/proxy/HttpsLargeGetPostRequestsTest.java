package com.seven.asimov.it.tests.dispatchers.proxy;

import android.test.suitebuilder.annotation.LargeTest;
import com.seven.asimov.it.base.Base64;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.testcases.ProxyGATestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.TestUtil;
import org.apache.http.HttpStatus;

import java.net.URLEncoder;
import java.util.Arrays;

import static com.seven.asimov.it.base.constants.TFConstantsIF.CRLF;

public class HttpsLargeGetPostRequestsTest extends ProxyGATestCase {

    @LargeTest
    public void testLargePostHttps100kb() throws Exception {
        checkLargePost(100 * 1024);
    }

    @LargeTest
    public void testLargePostHttps150kb() throws Exception {
        checkLargePost(150 * 1024);
    }

    @LargeTest
    public void testLargePostHttps200kb() throws Exception {
        checkLargePost(200 * 1024);
    }

    @LargeTest
    public void testLargePostHttps300kb() throws Exception {
        checkLargePost(300 * 1024);
    }

    @LargeTest
    public void testLargePostHttps400kb() throws Exception {
        checkLargePost(400 * 1024);
    }

    @LargeTest
    public void testLargePostHttps512kb() throws Exception {
        checkLargePost(512 * 1024);
    }

    @LargeTest
    public void testLargePostHttps1024kb() throws Exception {
        checkLargePost(1024 * 1024);
    }
    @LargeTest
    public void testLargeGetHttps() throws Exception {
        String uri = createTestResourceUri("https_asimov_it_cv_006", true);
        PrepareResourceUtil.prepareResource(uri, false);

        final int expectedResponseLength = 1024 * 1024;
        char expectedBody = 'a';

        String addHeaders = "Age: 1" + CRLF + "Server: Apache-Coyote/1.1" + CRLF + "Vary: Accept-Encoding" + CRLF
                + "Header1: header1" + CRLF + "Header2: header2";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        final HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-ResponseContentSize", expectedResponseLength + "," + expectedBody)
                .addHeaderField("X-OC-AddHeader:", addHeadersEncoded).getRequest();

        HttpResponse response = sendRequest(request, this, false, false, Body.BODY_HASH);
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals(Integer.toString(expectedResponseLength), response.getHeaderField("Content-Length"));
        assertEquals("1", response.getHeaderField("Age"));
        assertEquals("Apache-Coyote/1.1", response.getHeaderField("Server"));
        assertEquals("Accept-Encoding", response.getHeaderField("Vary"));
        assertEquals("header1", response.getHeaderField("Header1"));
        assertEquals("header2", response.getHeaderField("Header2"));
        byte[] expectedHash = TestUtil.getStreamedHash(expectedBody, expectedResponseLength);
        assertTrue(Arrays.equals(expectedHash, response.getBodyHash()));
    }
}
