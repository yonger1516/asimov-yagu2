package com.seven.asimov.it.tests.dispatchers.proxy;

import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.base.*;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.ScreenUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import java.net.URLEncoder;

import static com.seven.asimov.it.base.constants.TFConstantsIF.CRLF;

public class HttpProxyGATest extends TcpDumpTestCase {

    public static long MIN_RAPID_CACHING_PERIOD_GA = 5000L;

    @MediumTest
    public void testHttpGetRequest() throws Exception {
        String uri = createTestResourceUri("ga_asimov_it_proxy1");
        PrepareResourceUtil.prepareResource(uri, false);

        checkMiss(createRequest().setUri(uri).setMethod("GET").getRequest(), 1);
    }

    @MediumTest
    public void testHttpPostRequest() throws Exception {
        String uri = createTestResourceUri("ga_asimov_it_proxy2");
        PrepareResourceUtil.prepareResource(uri, false);

        checkMiss(createRequest().setUri(uri).setMethod("POST").getRequest(), 1);
    }

    @MediumTest
    public void testHttpPutRequest() throws Exception {
        String uri = createTestResourceUri("ga_asimov_it_proxy3");
        PrepareResourceUtil.prepareResource(uri, false);

        checkMiss(createRequest().setUri(uri).setMethod("PUT").setBody("simple_request").getRequest(), 1);
    }

    /**
     * Sends chunked POST request with body "simple_chunked_request" and size 4. <br>
     * URI: /...asimov_it_proxy4 <br>
     * Expectation: Result code 200 OK.
     *
     * @throws Exception
     */
    @MediumTest
    public void testHttpChunkedRequest() throws Exception {
        String uri = createTestResourceUri("ga_asimov_it_proxy4");
        PrepareResourceUtil.prepareResource(uri, false);

        checkMiss(createRequest().setUri(uri).setMethod("POST").setBody("simple_chunked_request").setChunkSize(4).getRequest(), 1);
    }

    @MediumTest
    public void testLongPolledHttpRequest() throws Exception {
        String uri = createTestResourceUri("ga_asimov_it_proxy5");
        PrepareResourceUtil.prepareResource(uri, false);

        checkMiss(createRequest().setUri(uri).setMethod("GET").addHeaderField("X-OC-Sleep", "15").getRequest(), 1);
    }

    /**
     * Scenario: Send request
     * <p/>
     * <pre>
     * GET http://tln-dev-testrunner1.7sys.eu/...asimov_it_proxy_continue HTTP/1.1
     * X-OC-Raw: SFRUUC8x...
     * </pre>
     * <p/>
     * <code>X-OC-Raw</code> contains encoded expected response:
     * <p/>
     * <pre>
     * HTTP/1.1 100 Continue
     *
     * HTTP/1.1 200 OK
     * Connection: close
     * Content-Length: 4
     *
     * tere
     * </pre>
     * <p/>
     * Expectations: Response should have code <code>200 OK</code> and body <code>tere</code>
     * <p/>
     * IGNORED: due to ASMV-11760 (CONTINUE is not implemented in the current version of OC)
     *
     * @throws Exception
     */
    @Ignore
    public void testHttpContinue() throws Exception {

        String uri = createTestResourceUri("ga_asimov_it_proxy_continue");
        PrepareResourceUtil.prepareResource(uri, false);

        String expected = "HTTP/1.1 100 Continue" + CRLF + "Connection: close" + CRLF + CRLF + "HTTP/1.1 200 OK"
                + CRLF + "Connection: close" + CRLF + "Content-Length: 4" + CRLF + CRLF + "tere";
        String encoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));

        HttpResponse response = sendRequestParallel(createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-Raw", encoded).getRequest());
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals(VALID_RESPONSE, response.getBody());
    }

    public void testHttpContAnother() throws Throwable {
        String uri = createTestResourceUri(String.format(
                "asimov_it_testDifferentStatusCodes_%d", 100));
        HttpRequest request = createRequest().setUri(uri)
                .setMethod(HttpGet.METHOD_NAME)
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-ResponseStatus", 100 + "")
                .getRequest();
        PrepareResourceUtil.prepareResource(uri, false);
        try {
            HttpResponse response;
            for (int i = 0; i < 4; i++) {
                response = checkMiss(request, i + 1, 100, null, true, TIMEOUT);
                logSleeping(MIN_NON_RMP_PERIOD - response.getDuration());
            }
        } finally {
            PrepareResourceUtil.prepareResource(uri, true);
        }
    }

    public void testHttpOptionsRequest() throws Exception {
        String uri = createTestResourceUri("ga_asimov_it_proxy7");
        PrepareResourceUtil.prepareResource(uri, false);

        String expected = "HTTP/1.1 200 OK" + CRLF + "Connection: close" + CRLF + "Content-Length: 4" + CRLF + CRLF;
        String encoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));

        HttpResponse response = sendRequestParallel(createRequest().setUri(uri).setMethod("OPTIONS")
                .addHeaderField("X-OC-Raw", encoded).getRequest());
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("", response.getBody());
    }

    public void testHttpHeaderRequest() throws Exception {
        String uri = createTestResourceUri("ga_asimov_it_proxy8");
        PrepareResourceUtil.prepareResource(uri, false);

        String expected = "HTTP/1.1 200 OK" + CRLF + "Connection: close" + CRLF + "Content-Length: 4" + CRLF + CRLF;
        String encoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));

        HttpResponse response = sendRequestParallel(createRequest().setUri(uri).setMethod("HEAD")
                .addHeaderField("X-OC-Raw", encoded).getRequest());
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("", response.getBody());
    }

    public void testHttpGETRequestLength0() throws Exception {
        String uri = createTestResourceUri("ga_asimov_it_head_request_lenght_0");
        PrepareResourceUtil.prepareResource(uri, false);

        String expected = "HTTP/1.1 200 OK" + CRLF + "Connection: close" + CRLF + "Accept-Encoding: gzip" + CRLF
                + "Content-Length: 0" + CRLF + CRLF;
        String encoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));

        HttpResponse response = sendRequestParallel(createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-Raw", encoded).addHeaderField("Accept-Encoding", "gzip").getRequest());
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("", response.getBody());
    }

    public void testHttpHEADRequest401() throws Exception {
        String uri = createTestResourceUri("ga_asimov_it_head_request_401");
        PrepareResourceUtil.prepareResource(uri, false);

        String expected = "HTTP/1.1 401 Unauthorized " + CRLF + "Connection: close" + CRLF + "Content-Length: 0" + CRLF
                + "Accept-Encoding: gzip" + CRLF + CRLF;
        String encoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));

        HttpResponse response = sendRequestParallel(createRequest().setUri(uri).setMethod("HEAD")
                .addHeaderField("X-OC-Raw", encoded).addHeaderField("Accept-Encoding", "gzip").getRequest());
        assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatusCode());
        assertEquals("", response.getBody());
    }

    public void testHttpTraceRequest() throws Exception {
        String uri = createTestResourceUri("ga_asimov_it_proxy17");
        PrepareResourceUtil.prepareResource(uri, false);

        String expected = "HTTP/1.1 200 OK" + CRLF + "Connection: close" + CRLF + "Content-Length: 4" + CRLF + CRLF;
        String encoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));

        HttpResponse response = sendRequestParallel(createRequest().setUri(uri).setMethod("TRACE")
                .addHeaderField("X-OC-Raw", encoded).getRequest());
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("", response.getBody());
    }

    public void testHttpDeleteRequest() throws Exception {
        String uri = createTestResourceUri("ga_asimov_it_proxy9");
        PrepareResourceUtil.prepareResource(uri, false);

        String expected = "HTTP/1.1 200 OK" + CRLF + "Connection: close" + CRLF + "Content-Length: 4" + CRLF + CRLF;
        String encoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));

        HttpResponse response = sendRequestParallel(createRequest().setUri(uri).setMethod("DELETE")
                .addHeaderField("X-OC-Raw", encoded).getRequest());
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
    }

    public void testSimpleGetHttpRequst() throws Exception {
        String uri = createTestResourceUri("ga_asimov_it_proxy10");
        PrepareResourceUtil.prepareResource(uri, false);

        String expected = "HTTP/1.0 200 OK" + CRLF + "Connection: close" + CRLF + "Content-Length: 7" + CRLF + CRLF
                + "get1b..";
        String encoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));

        String request = "GET " + uri + " HTTP/1.0" + CRLF + "X-OC-Raw: " + encoded + CRLF + "Connection: close" + CRLF
                + CRLF;
        HttpResponse response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("get1b..", response.getBody());
    }

    /**
     * Sends request with specified port 80:
     * <p/>
     * <pre>
     *  GET http://tln-dev-testrunner1.7sys.eu:80/...asimov_it_proxy18 HTTP/1.0
     *  X-OC-Raw: SFRUUC8xL...
     *  Connection: close
     * </pre>
     * <p/>
     * <code>X-OC-Raw</code> contains encoded 200 OK response. <br>
     * <br>
     * <p/>
     * Expectations: Expected result should be received.
     *
     * @throws Exception
     */
    public void testSimpleGetHttpRequestWithPort80() throws Exception {
        String path = createTestResourcePath("ga_asimov_it_proxy18");
        String uri = createTestResourceUri("ga_asimov_it_proxy18");
        PrepareResourceUtil.prepareResource(uri, false);

        String expected = "HTTP/1.0 200 OK" + CRLF + "Connection: close" + CRLF + "Content-Length: 7" + CRLF + CRLF
                + "get1b..";
        String encoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));

        String request = "GET http://" + TEST_RESOURCE_HOST + ":80/" + path + " HTTP/1.0" + CRLF + "X-OC-Raw: "
                + encoded + CRLF + "Connection: close" + CRLF + CRLF;
        HttpResponse response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("get1b..", response.getBody());
    }

    @MediumTest
    public void testSimpleGetHttpRequestWithHost() throws Exception {
        String path = createTestResourcePath("ga_asimov_it_proxy11");
        String uri = createTestResourceUri("ga_asimov_it_proxy11");
        PrepareResourceUtil.prepareResource(uri, false);

        String expected = "HTTP/1.1 200 OK" + CRLF + "Connection: close" + CRLF + "Content-Length: 7" + CRLF + CRLF
                + "get1b..";
        String encoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));

        String request = "GET /" + path + " HTTP/1.1" + CRLF + "Host: " + TEST_RESOURCE_HOST + CRLF + "X-OC-Raw: "
                + encoded + CRLF + "Connection: close" + CRLF + CRLF;
        HttpResponse response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("get1b..", response.getBody());
    }

    @MediumTest
    public void testSimpleGetHttpRequestWithHostAndPort() throws Exception {
        String path = createTestResourcePath("ga_asimov_it_proxy12");
        String uri = createTestResourceUri("ga_asimov_it_proxy12");
        PrepareResourceUtil.prepareResource(uri, false);
        String expected = "HTTP/1.1 200 OK" + CRLF + "Connection: close" + CRLF + "Content-Length: 7" + CRLF + CRLF
                + "get1b..";
        String encoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));

        String request = "GET /" + path + " HTTP/1.1" + CRLF + "Host: " + TEST_RESOURCE_HOST + ":80" + CRLF
                + "X-OC-Raw: " + encoded + CRLF + "Connection: close" + CRLF + CRLF;
        HttpResponse response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("get1b..", response.getBody());
    }

    @MediumTest
    public void testSimplePostHttpRequestWithHostAndPort() throws Exception {
        String path = createTestResourcePath("ga_asimov_it_proxy13");
        String uri = createTestResourceUri("ga_asimov_it_proxy13");
        PrepareResourceUtil.prepareResource(uri, false);

        String expected = "HTTP/1.1 200 OK" + CRLF + "Connection: close" + CRLF + CRLF;
        String encoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));

        String request = "POST /" + path + " HTTP/1.1" + CRLF + "Host: " + TEST_RESOURCE_HOST + ":80" + CRLF
                + "X-OC-Raw: " + encoded + CRLF + "Content-length: 16" + CRLF + "Connection: close" + CRLF + CRLF
                + "Request content.";
        HttpResponse response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
    }

    @MediumTest
    public void testSimplePostHttpRequestWithPort() throws Exception {
        String uri = createTestResourceUri("ga_asimov_it_proxy14", false, 80);
        PrepareResourceUtil.prepareResource(uri, false);

        String expected = "HTTP/1.1 200 OK" + CRLF + "Connection: close" + CRLF + CRLF;
        String encoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));

        String request = "POST " + uri + " HTTP/1.1" + CRLF + "X-OC-Raw: " + encoded + CRLF + "Content-length: 16"
                + CRLF + "Connection: close" + CRLF + CRLF + "Request content.";
        HttpResponse response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
    }

    @MediumTest
    public void testSimplePostHttpRequestWithContentType() throws Exception {
        String uri = createTestResourceUri("ga_asimov_it_proxy15", false, 80);
        PrepareResourceUtil.prepareResource(uri, false);

        String expected = "HTTP/1.1 200 OK" + CRLF + "Connection: close" + CRLF + CRLF;
        String encoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));

        String request = "POST " + uri + " HTTP/1.1" + CRLF + "X-OC-Raw: " + encoded + CRLF
                + "Content-Type: text/plain" + CRLF + "Content-Length: 22" + CRLF + "Connection: close" + CRLF + CRLF
                + "Simple content (post1)";
        HttpResponse response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
    }

    @MediumTest
    public void testComplexGetHttpRequest() throws Exception {
        String uri = createTestResourceUri("ga_asimov_it_proxy16");
        PrepareResourceUtil.prepareResource(uri, false);

        String expected = "HTTP/1.1 200 OK" + CRLF + "Connection: close" + CRLF + "Content-Length: 7" + CRLF
                + "Accept-Charset: utf-8, iso-8859-1, utf-16, *;q=0.7" + CRLF + "Accept-Language: en-US" + CRLF
                + "Accept-Encoding: gzip" + CRLF
                + "User-Agent: curl/7.19.0 (i586-pc-mingw32msvc) libcurl/7.19.0 zlib/1.2.3" + CRLF
                + "x-network-type: IS2000" + CRLF + CRLF + "get1b..";
        String encoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));

        HttpResponse response = sendRequestParallel(createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-Raw", encoded).getRequest());

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("get1b..", response.getBody());
        assertEquals("utf-8, iso-8859-1, utf-16, *;q=0.7", response.getHeaderField("Accept-Charset"));
        assertEquals("IS2000", response.getHeaderField("x-network-type"));
        assertEquals("gzip", response.getHeaderField("Accept-Encoding"));
        assertEquals("en-US", response.getHeaderField("Accept-Language"));
        assertEquals("close", response.getHeaderField("Connection"));
        assertEquals("curl/7.19.0 (i586-pc-mingw32msvc) libcurl/7.19.0 zlib/1.2.3",
                response.getHeaderField("User-Agent"));

    }

    @MediumTest
    public void testHttpChunkedRMPNotCaching() throws Throwable {

        String uri = createTestResourceUri("ga_asimov_it_http_chunked_rmp_caching", false, 80);
        PrepareResourceUtil.prepareResource(uri, false);

        String expected1 = "HTTP/1.1 200 OK" + CRLF + "Content-Type: text/plain" + CRLF + "Connection: close" + CRLF
                + "Transfer-Encoding: chunked" + CRLF + CRLF +

                "7" + CRLF + "Lets " + CRLF + CRLF + "6" + CRLF + "eat " + CRLF + CRLF + "9" + CRLF + "Grandma" + CRLF
                + CRLF + "1" + CRLF + "!" + CRLF + "0" + CRLF + CRLF;

        String expected2 = "HTTP/1.1 200 OK" + CRLF + "Content-Type: text/plain" + CRLF + "Connection: close" + CRLF
                + "Transfer-Encoding: chunked" + CRLF + CRLF +

                "4" + CRLF + "Lets" + CRLF + "9" + CRLF + " " + CRLF + "eat " + CRLF + CRLF + "9" + CRLF + "Grandma"
                + CRLF + CRLF + "1" + CRLF + "!" + CRLF + "0" + CRLF + CRLF;

        String expected3 = "HTTP/1.1 200 OK" + CRLF + "Content-Type: text/plain" + CRLF + "Connection: close" + CRLF
                + "Transfer-Encoding: chunked" + CRLF + CRLF +

                "7" + CRLF + "Lets " + CRLF + CRLF + "6" + CRLF + "eat " + CRLF + CRLF + "A" + CRLF + "Grandma" + CRLF
                + "!" + CRLF + "0" + CRLF + CRLF;

        String expected4 = "HTTP/1.1 200 OK" + CRLF + "Content-Type: text/plain" + CRLF + "Connection: close" + CRLF
                + "Transfer-Encoding: chunked" + CRLF + CRLF +

                "4" + CRLF + "Lets" + CRLF + "9" + CRLF + " " + CRLF + "eat " + CRLF + CRLF + "9" + CRLF + "Grandma"
                + CRLF + CRLF + "1" + CRLF + "!" + CRLF + "0" + CRLF + CRLF;

        String expectedBody = "Lets " + CRLF + "eat " + CRLF + "Grandma" + CRLF + "!";

        String encoded1 = URLEncoder.encode(Base64.encodeToString(expected1.getBytes(), Base64.DEFAULT));
        String encoded2 = URLEncoder.encode(Base64.encodeToString(expected2.getBytes(), Base64.DEFAULT));
        String encoded3 = URLEncoder.encode(Base64.encodeToString(expected3.getBytes(), Base64.DEFAULT));
        String encoded4 = URLEncoder.encode(Base64.encodeToString(expected4.getBytes(), Base64.DEFAULT));

        HttpRequest request1 = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("Connection", "close").addHeaderField("X-OC-Raw", encoded1).getRequest();

        long startTime = System.currentTimeMillis();
        HttpResponse response1 = checkMiss(request1, 1, HttpStatus.SC_OK, expectedBody);
        assertEquals(HttpStatus.SC_OK, response1.getStatusCode());
        logSleeping(MIN_RAPID_CACHING_PERIOD_GA - System.currentTimeMillis() + startTime);

        startTime = System.currentTimeMillis();
        HttpResponse response2 = checkMiss(request1, 2, HttpStatus.SC_OK, expectedBody);
        assertEquals(HttpStatus.SC_OK, response2.getStatusCode());
        logSleeping(MIN_RAPID_CACHING_PERIOD_GA - System.currentTimeMillis() + startTime);

        startTime = System.currentTimeMillis();
        HttpResponse response3 = checkMiss(request1, 3, HttpStatus.SC_OK, expectedBody);
        assertEquals(HttpStatus.SC_OK, response3.getStatusCode());
        logSleeping(MIN_RAPID_CACHING_PERIOD_GA - System.currentTimeMillis() + startTime);

        startTime = System.currentTimeMillis();
        HttpResponse response4 = checkHit(request1, 4, HttpStatus.SC_OK, expectedBody);
        assertEquals(HttpStatus.SC_OK, response4.getStatusCode());
    }

    /**
     * Switches screen off and sends POST request, Content-Length has value less than real size of request body:
     * <p/>
     * <pre>
     *   POST /..._asimov_it_proxy19 HTTP/1.1
     *   Host: tln-dev-testrunner1.7sys.eu
     *   Connection: close
     *   Content-Length: 5
     *
     *   Some body with some length
     * </pre>
     * <p/>
     * Expectation: Result code 200 OK.
     * <p/>
     * IGNORED: due to ASMV-21669
     *
     * @throws Exception
     */
    @Ignore
    public void testHttpRequestContentLenghtLess() throws Exception {
        String uri = createTestResourceUri("ga_asimov_it_proxy19");
        String path = createTestResourcePath("ga_asimov_it_proxy19");
        PrepareResourceUtil.prepareResource(uri, false);
        ScreenUtils.ScreenSpyResult spy = ScreenUtils.switchScreenAndSpy(getContext(), false);

        try {

            String request = "POST /" + path + " HTTP/1.1" + CRLF + "Host: " + TEST_RESOURCE_HOST + CRLF
                    + "Connection: close" + CRLF + "Content-Length: 5" + CRLF + CRLF + "Some body with some length";

            HttpResponse response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST, true);
            assertEquals(HttpStatus.SC_OK, response.getStatusCode());
            assertEquals(VALID_RESPONSE, response.getBody());
            assertTrue(spy.isScreenAsExpected());

        } finally {
            ScreenUtils.finishSpyAndResetScreen(getContext(), spy);
        }
    }

    /**
     * Sends GET request and receives response where Content-Length has value less than real body size. Response is:
     * <p/>
     * <pre>
     *  HTTP/1.0 200 OK
     *  Connection: close
     *  Content-Length: 5
     *
     *  Some body with some length
     * </pre>
     * <p/>
     * Expectations: Described response should be received.
     *
     * @throws Exception
     */
    public void testHttpResponseContentLenghtLess() throws Exception {
        String uri = createTestResourceUri("ga_asimov_it_proxy20");
        PrepareResourceUtil.prepareResource(uri, false);

        String body = "Some body with some length";
        String expectedBody = "Some ";

        String expected = "HTTP/1.1 200 OK" + CRLF + "Connection: close" + CRLF + "Content-Length: 5" + CRLF + CRLF
                + body;
        String encoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));

        String request = "GET " + uri + " HTTP/1.1" + CRLF + "X-OC-Raw: " + encoded + CRLF + "Connection: close" + CRLF
                + CRLF;

        HttpResponse response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST, true);
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("5", response.getHeaderField("Content-Length"));
        assertEquals(expectedBody, response.getBody());
    }

    /**
     * Sends GET request and receives response where Content-Length has value greater than real body size. Response is:
     * <p/>
     * <pre>
     *  HTTP/1.0 200 OK
     *  Connection: close
     *  Content-Length: 500
     *
     *  Some body with some length
     * </pre>
     * <p/>
     * Expectations: Described response should be received.
     *
     * @throws Exception
     */
    public void testHttpResponseContentLenghtGreater() throws Exception {
        String uri = createTestResourceUri("ga_asimov_it_proxy21");
        PrepareResourceUtil.prepareResource(uri, false);

        String body = "Some body with some length";

        String expected = "HTTP/1.0 200 OK" + CRLF + "Connection: close" + CRLF + "Content-Length: 500" + CRLF + CRLF
                + body;
        String encoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));

        String request = "GET " + uri + " HTTP/1.0" + CRLF + "X-OC-Raw: " + encoded + CRLF + "Connection: close" + CRLF
                + CRLF;

        HttpResponse response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST, true);
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("500", response.getHeaderField("Content-Length"));
        assertEquals(body, response.getBody());
    }

    @SmallTest
    public void testContentEncodingGzipDeflateCompressIdentity() throws Throwable {

        String RESOURCE_URI1 = "asimov_ga_it_test_content_encoding_gzip";
        String RESOURCE_URI2 = "asimov_ga_it_test_content_encoding_deflate";
        String RESOURCE_URI3 = "asimov_ga_it_test_content_encoding_compress";
        String RESOURCE_URI4 = "asimov_ga_it_test_content_encoding_identity";

        String uri1 = createTestResourceUri(RESOURCE_URI1);
        String uri2 = createTestResourceUri(RESOURCE_URI2);
        String uri3 = createTestResourceUri(RESOURCE_URI3);
        String uri4 = createTestResourceUri(RESOURCE_URI4);

        String body1 = "testEncodingGzip";
        String body2 = "testEncodingDeflate";
        String body3 = "testEncodingCompress";
        String body4 = "testEncodingIdentity";

        String expectedHeader1 = "Content-Encoding: gzip";
        String encodedHeader1 = URLEncoder.encode(Base64.encodeToString(expectedHeader1.getBytes(), Base64.DEFAULT));

        HttpRequest request1 = createRequest().setUri(uri1).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "gzip").addHeaderField("X-OC-ChangeResponseContent", body1)
                .addHeaderField("X-OC-AddRawHeaders", encodedHeader1).getRequest();

        checkMiss(request1, 1);

        String expectedHeader2 = "Content-Encoding: Deflate";
        String encodedHeader2 = URLEncoder.encode(Base64.encodeToString(expectedHeader2.getBytes(), Base64.DEFAULT));

        HttpRequest request2 = createRequest().setUri(uri2).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "Deflate").addHeaderField("X-OC-ChangeResponseContent", body2)
                .addHeaderField("X-OC-AddRawHeaders", encodedHeader2).getRequest();

        checkMiss(request2, 2);

        String expectedHeader3 = "Content-Encoding: Compress";
        String encodedHeader3 = URLEncoder.encode(Base64.encodeToString(expectedHeader3.getBytes(), Base64.DEFAULT));

        HttpRequest request3 = createRequest().setUri(uri3).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "Compress").addHeaderField("X-OC-ChangeResponseContent", body3)
                .addHeaderField("X-OC-AddRawHeaders", encodedHeader3).getRequest();

        checkMiss(request3, 3);

        String expectedHeader4 = "Content-Encoding: Identity";
        String encodedHeader4 = URLEncoder.encode(Base64.encodeToString(expectedHeader4.getBytes(), Base64.DEFAULT));

        HttpRequest request4 = createRequest().setUri(uri4).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "Identity").addHeaderField("X-OC-ChangeResponseContent", body4)
                .addHeaderField("X-OC-AddRawHeaders", encodedHeader4).getRequest();

        checkMiss(request4, 4);
    }

    @LargeTest
    public void testLinkUnlinkMethods() throws Throwable {

        String RESOURCE_URI = "asimov_ga_test_method_link_unlink/profiles/john";

        String uri = createTestResourceUri(RESOURCE_URI);
        HttpRequest requestLink = createRequest().setUri(uri).setMethod("LINK")
                .addHeaderField("Link", "<http://" + TEST_RESOURCE_HOST + "/profiles/marry>; rel=\"friend\"")
                .getRequest();

        HttpRequest requestUnlink = createRequest().setUri(uri).setMethod("UNLINK")
                .addHeaderField("Link", "<http://" + TEST_RESOURCE_HOST + "/profiles/marry>; rel=\"friend\"")
                .getRequest();

        sendMiss(1, requestLink);
        logSleeping(MIN_RAPID_CACHING_PERIOD_GA);
        sendMiss(2, requestUnlink);
    }

    @LargeTest
    public void testHttp12Version() throws Throwable {

        String uri = "ga_asimov_it_proxy_future_version";
        String expected = "HTTP/1.2 200 OK" + CRLF + "Connection: close" + CRLF + "Content-length: 4" + CRLF + CRLF
                + "body";

        String encoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));
        String request = "GET /" + uri + " HTTP/1.2" + CRLF + "Connection: close" + CRLF
                + "Host: " + TEST_RESOURCE_HOST + CRLF + "X-OC-Raw: " + encoded + CRLF + CRLF;

        HttpResponse response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("body", response.getBody());
        assertEquals("close", response.getHeaderField("Connection"));
        logSleeping(MIN_RAPID_CACHING_PERIOD_GA - response.getDuration());

        response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("body", response.getBody());
        assertEquals("close", response.getHeaderField("Connection"));
        logSleeping(MIN_RAPID_CACHING_PERIOD_GA - response.getDuration());

        response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("body", response.getBody());
        assertEquals("close", response.getHeaderField("Connection"));
        logSleeping(MIN_RAPID_CACHING_PERIOD_GA - response.getDuration());

        response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("body", response.getBody());
        assertEquals("close", response.getHeaderField("Connection"));
    }

    @LargeTest
    public void testHttp09Version() throws Throwable {

        String uri = "ga_asimov_it_proxy_past_version";
        String expected = "HTTP/0.9 200 OK" + CRLF + "Connection: close" + CRLF + "Content-length: 4" + CRLF + CRLF
                + "body";

        String encoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));
        String request = "GET /" + uri + " HTTP/0.9" + CRLF + "Connection: close" + CRLF
                + "Host: " + AsimovTestCase.TEST_RESOURCE_HOST + CRLF + "X-OC-Raw: " + encoded + CRLF + CRLF;

        HttpResponse response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("body", response.getBody());
        assertEquals("close", response.getHeaderField("Connection"));
        logSleeping(MIN_RAPID_CACHING_PERIOD_GA - response.getDuration());

        response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("body", response.getBody());
        assertEquals("close", response.getHeaderField("Connection"));
        logSleeping(MIN_RAPID_CACHING_PERIOD_GA - response.getDuration());

        response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("body", response.getBody());
        assertEquals("close", response.getHeaderField("Connection"));
        logSleeping(MIN_RAPID_CACHING_PERIOD_GA - response.getDuration());

        response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("body", response.getBody());
        assertEquals("close", response.getHeaderField("Connection"));
    }
}
