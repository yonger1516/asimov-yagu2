package com.seven.asimov.it.tests.dispatchers.proxy;

import android.test.suitebuilder.annotation.LargeTest;
import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.base.Base64;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.testcases.ProxyGATestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.pollingTasks.StartPollTask;
import com.seven.asimov.it.utils.logcat.wrappers.StartPollWrapper;
import org.apache.http.HttpStatus;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.seven.asimov.it.base.constants.TFConstantsIF.CRLF;
import static com.seven.asimov.it.utils.SmokeUtil.sendInvSms;

public class PipeliningTest extends ProxyGATestCase {

    public void testHttpHEADPipelinedRequest() throws Exception {

        String uri = createTestResourceUri("ga_asimov_it_proxy_pipeline_head_1", false, 80);
        String uri2 = createTestResourceUri("ga_asimov_it_proxy_pipeline_head_2", false, 80);
        String expected1 = "HTTP/1.1 200 OK" + CRLF + "Connection: keep-alive" + CRLF + "Content-length: 0" + CRLF + CRLF;

        String expected2 = "HTTP/1.1 200 OK" + CRLF + "Connection: close" + CRLF + "Content-Length: 12" + CRLF + CRLF
                + "response two";

        String encoded1 = URLEncoder.encode(Base64.encodeToString(expected1.getBytes(), Base64.DEFAULT));
        String encoded2 = URLEncoder.encode(Base64.encodeToString(expected2.getBytes(), Base64.DEFAULT));

        String request = "HEAD " + uri + " HTTP/1.1" + CRLF + "Connection: keep-alive" + CRLF + "X-OC-Raw: " + encoded1
                + CRLF + CRLF + "HEAD " + uri2 + " HTTP/1.1" + CRLF + "Connection: close" + CRLF + "X-OC-Raw: "
                + encoded2 + CRLF + CRLF;

        HttpResponse response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);

        HttpResponse response1 = buildResponse(new StringBuilder(response.getRawContent().substring(0,
                response.getRawContent().indexOf("HTTP", 5))));
        HttpResponse response2 = buildResponse(new StringBuilder(response.getRawContent().substring(
                response.getRawContent().indexOf("HTTP", 5))));

        assertEquals("Expected Status-Code for first response 200 but was " + response1.getStatusCode()
                , HttpStatus.SC_OK, response1.getStatusCode());
        assertEquals("Expected connection for first response keep-alive but was " + response1.getHeaderField("Connection"),
                "keep-alive", response1.getHeaderField("Connection"));
        assertEquals("Expected first response without body but was with body " + response1.getBody()
                , "", response1.getBody());

        assertEquals("Expected Status-Code for second response 200 but was " + response2.getStatusCode()
                , HttpStatus.SC_OK, response2.getStatusCode());
        assertEquals("Expected connection for second response close but was " + response2.getHeaderField("Connection"),
                "close", response2.getHeaderField("Connection"));
        assertEquals("Expected value of Content-length = 12 for second response but was" + response2.getHeaderField("Content-length"),
                "12", response2.getHeaderField("Content-length"));
        assertEquals("Expected second response without body but was with body " + response1.getBody()
                , "", response2.getBody());
    }

    public void testHttpPipeLine() throws Exception {
        String uri = createTestResourceUri("ga_asimov_it_proxy_pipeline1", false, 80);
        PrepareResourceUtil.prepareResourceParallel(uri, false);
        String uri2 = createTestResourceUri("ga_asimov_it_proxy_pipeline2", false, 80);
        PrepareResourceUtil.prepareResourceParallel(uri2, false);

        String expected1 = "HTTP/1.1 200 OK" + CRLF + "Connection: keep-alive" + CRLF + "Content-Length: 12" + CRLF
                + CRLF + "response one";

        String expected2 = "HTTP/1.1 200 OK" + CRLF + "Connection: close" + CRLF + "Content-Length: 12" + CRLF + CRLF
                + "response two";

        String encoded1 = URLEncoder.encode(Base64.encodeToString(expected1.getBytes(), Base64.DEFAULT));
        String encoded2 = URLEncoder.encode(Base64.encodeToString(expected2.getBytes(), Base64.DEFAULT));

        String request = "GET " + uri + " HTTP/1.1" + CRLF + "Connection: keep-alive" + CRLF + "X-OC-Raw: " + encoded1
                + CRLF + CRLF + "GET " + uri2 + " HTTP/1.1" + CRLF + "Connection: close" + CRLF + "X-OC-Raw: "
                + encoded2 + CRLF + CRLF;

        HttpResponse response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);

        HttpResponse response1 = buildResponse(new StringBuilder(response.getRawContent().substring(0,
                response.getRawContent().indexOf("HTTP", 5))));
        HttpResponse response2 = buildResponse(new StringBuilder(response.getRawContent().substring(
                response.getRawContent().indexOf("HTTP", 5))));

        assertEquals(response1.getStatusCode(), HttpStatus.SC_OK);
        assertEquals(response1.getHeaderField("Connection"), "keep-alive");
        assertEquals(response1.getHeaderField("Content-Length"), "12");
        assertEquals(response1.getBody(), "response one");
        assertEquals(response2.getStatusCode(), HttpStatus.SC_OK);
        assertEquals(response2.getHeaderField("Connection"), "close");
        assertEquals(response2.getHeaderField("Content-Length"), "12");
        assertEquals(response2.getBody(), "response two");
    }

    /**
     * Sends double request:
     * <p/>
     * <pre>
     *  GET http://tln-dev-testrunner1.7sys.eu:80/...asimov_it_proxy_pipeline_chunked1 HTTP/1.1
     *  Connection: Keep-Alive
     *  X-OC-Raw:
     *
     *  GET http://tln-dev-testrunner1.7sys.eu:80/...asimov_it_proxy_pipeline_chunked2 HTTP/1.1
     *  Connection: close
     *  X-OC-Raw: SFRUUC8xLjEgMjAw...
     * </pre>
     * <p/>
     * <code>X-OC-Raw</code> contains encoded expected response:
     * <p/>
     * <pre>
     *  HTTP/1.1 200 OK
     *  Content-Type: text/plain
     *  Connection: Keep-Alive
     *  Transfer-Encoding: chunked
     *
     *  9
     *  11 Resp
     *
     *  25
     *  This is the data in the first chunk
     *
     *  1C
     *  and this is the second one
     *
     *  3
     *  con
     *  8
     *  sequence
     *  0
     *
     *  HTTP/1.1 200 OK
     *  Content-Type: text/plain
     *  Connection: close
     *  Transfer-Encoding: chunked
     *
     *  8
     *  2 Resp
     *
     *  25
     *  This is the data in the first chunk
     *
     *  1C
     *  and this is the second one
     *
     *  3
     *  con
     *  8
     *  sequence
     *  0
     *
     * </pre>
     * <p/>
     * Expectations: Described response should be received.
     *
     * @throws Exception
     */
    public void testHttpChunkedPipeLine() throws Exception {
        String uri = createTestResourceUri("ga_asimov_it_proxy_pipeline_chunked1", false, 80);
        PrepareResourceUtil.prepareResource(uri, false);
        String uri2 = createTestResourceUri("ga_asimov_it_proxy_pipeline_chunked2", false, 80);
        PrepareResourceUtil.prepareResource(uri2, false);

        String expected1 = "HTTP/1.1 200 OK" + CRLF + "Content-Type: text/plain" + CRLF + "Connection: keep-alive"
                + CRLF + "Transfer-Encoding: chunked" + CRLF + CRLF + "9" + CRLF + "11 Resp" + CRLF + CRLF +

                "25" + CRLF + "This is the data in the first chunk" + CRLF + CRLF +

                "1C" + CRLF + "and this is the second one" + CRLF + CRLF +

                "3" + CRLF + "con" + CRLF + "8" + CRLF + "sequence" + CRLF + "0" + CRLF + CRLF;

        String body1 = "11 Resp" + CRLF + "This is the data in the first chunk" + CRLF + "and this is the second one"
                + CRLF + "consequence";

        String expected2 = "HTTP/1.1 200 OK" + CRLF + "Content-Type: text/plain" + CRLF + "Connection: close" + CRLF
                + "Transfer-Encoding: chunked" + CRLF + CRLF +

                "8" + CRLF + "2 Resp" + CRLF + CRLF +

                "25" + CRLF + "This is the data in the first chunk" + CRLF + CRLF +

                "1C" + CRLF + "and this is the second one" + CRLF + CRLF +

                "3" + CRLF + "con" + CRLF + "8" + CRLF + "sequence" + CRLF + "0" + CRLF + CRLF;
        String body2 = "2 Resp" + CRLF + "This is the data in the first chunk" + CRLF + "and this is the second one"
                + CRLF + "consequence";

        String encoded1 = URLEncoder.encode(Base64.encodeToString(expected1.getBytes(), Base64.DEFAULT));
        String encoded2 = URLEncoder.encode(Base64.encodeToString(expected2.getBytes(), Base64.DEFAULT));

        String request = "GET " + uri + " HTTP/1.1" + CRLF + "Connection: keep-alive" + CRLF + "X-OC-Raw: " + encoded1
                + CRLF + CRLF + "GET " + uri2 + " HTTP/1.1" + CRLF + "Connection: close" + CRLF + "X-OC-Raw: "
                + encoded2 + CRLF + CRLF;

        HttpResponse response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST, true);

        ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
        ByteArrayOutputStream stream2 = new ByteArrayOutputStream();

        int httpIndex = response.getRawContent().indexOf("HTTP", 5);
        assertFalse("Expected two responses but was one", httpIndex == -1);

        stream1.write(new StringBuilder(response.getRawContent().substring(0, httpIndex)).toString().getBytes());
        stream2.write(new StringBuilder(response.getRawContent().substring(httpIndex)).toString().getBytes());

        HttpResponse response1 = buildResponse(stream1);
        HttpResponse response2 = buildResponse(stream2);

        assertEquals(HttpStatus.SC_OK, response1.getStatusCode());
        assertEquals("keep-alive", response1.getHeaderField("Connection"));
        assertEquals("text/plain", response1.getHeaderField("Content-Type"));
        assertEquals("chunked", response1.getHeaderField("Transfer-Encoding"));

        assertEquals(body1, response1.getBody());

        assertEquals(HttpStatus.SC_OK, response2.getStatusCode());
        assertEquals("close", response2.getHeaderField("Connection"));
        assertEquals("text/plain", response2.getHeaderField("Content-Type"));
        assertEquals(body2, response2.getBody());

    }

    /**
     * Sends double request:
     * <p/>
     * <pre>
     *  GET http://tln-dev-testrunner1.7sys.eu:80/...asimov_it_proxy_pipeline_chunked_big1 HTTP/1.1
     *  Connection: Keep-Alive
     *
     *  GET http://tln-dev-testrunner1.7sys.eu:80/...asimov_it_proxy_pipeline_chunked_big2 HTTP/1.1
     *  Connection: close
     * </pre>
     * <p/>
     * <p/>
     * Expected chunked responses are 50 kbytes long for each request.
     *
     * @throws Exception
     */
    public void testHttpBigChunkedPipeLine() throws Exception {
        String uri1 = createTestResourceUri("ga_asimov_it_proxy_pipeline_chunked_big1", false, 80);
        PrepareResourceUtil.prepareResource(uri1, false);
        String uri2 = createTestResourceUri("ga_asimov_it_proxy_pipeline_chunked_big2", false, 80);
        PrepareResourceUtil.prepareResource(uri2, false);

        String expectedKa = "Connection: keep-alive";
        String expectedC = "Connection: close";
        String encodedHKa = URLEncoder.encode(Base64.encodeToString(expectedKa.getBytes(), Base64.DEFAULT));
        String encodedC = URLEncoder.encode(Base64.encodeToString(expectedC.getBytes(), Base64.DEFAULT));

        String request = "GET " + uri1 + " HTTP/1.1" + CRLF + "Connection: keep-alive" + CRLF + "X-OC-AddRawHeaders: " + encodedHKa + CRLF + "X-OC-Chunked: 200" + CRLF
                + CRLF + "GET " + uri2 + " HTTP/1.1" + CRLF + "Connection: close" + CRLF + "X-OC-AddRawHeaders: " + encodedC + CRLF + "X-OC-Chunked: 200"
                + CRLF + CRLF;

        HttpRequest requestPrepare1 = createRequest().setUri(uri1).setMethod("GET")
                .addHeaderField("X-OC-ChangeResponseContentSize", 50000 + ", abcdefghigklmnopqrst")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .getRequest();

        HttpRequest requestPrepare2 = createRequest().setUri(uri2).setMethod("GET")
                .addHeaderField("X-OC-ChangeResponseContentSize", 50000 + ", abcdefghigklmnopqrst")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .getRequest();

        sendRequest2(requestPrepare1, false, true);
        sendRequest2(requestPrepare2, false, true);

        logSleeping(4 * 1000);

        HttpResponse response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST, true);

        ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
        ByteArrayOutputStream stream2 = new ByteArrayOutputStream();

        int httpIndex = response.getRawContent().indexOf("HTTP", 5);
        assertTrue("Expected two responses but was one", httpIndex != -1);

        stream1.write(new StringBuilder(response.getRawContent().substring(0, httpIndex)).toString().getBytes());
        stream2.write(new StringBuilder(response.getRawContent().substring(httpIndex)).toString().getBytes());

        HttpResponse response1 = buildResponse(stream1);
        HttpResponse response2 = buildResponse(stream2);

        assertEquals(HttpStatus.SC_OK, response1.getStatusCode());
        assertEquals("keep-alive", response1.getHeaderField("Connection"));

        assertEquals(HttpStatus.SC_OK, response2.getStatusCode());
        assertEquals("close", response2.getHeaderField("Connection"));

        assertEquals("chunked", response2.getHeaderField("Transfer-Encoding"));
    }

    public void testHttpPipeLineFourRequests() throws Exception {
        String uri1 = createTestResourceUri("ga_asimov_it_proxy_pipeline0_1", false, 80);
        PrepareResourceUtil.prepareResource(uri1, false);
        String uri2 = createTestResourceUri("ga_asimov_it_proxy_pipeline0_2", false, 80);
        PrepareResourceUtil.prepareResource(uri2, false);
        String uri3 = createTestResourceUri("ga_asimov_it_proxy_pipeline0_3", false, 80);
        PrepareResourceUtil.prepareResource(uri3, false);
        String uri4 = createTestResourceUri("ga_asimov_it_proxy_pipeline0_4", false, 80);
        PrepareResourceUtil.prepareResource(uri4, false);

        String expected1 = "HTTP/1.1 200 OK" + CRLF + "Connection: keep-alive" + CRLF + "Content-Length: 10" + CRLF
                + CRLF + "response 1";

        String expected2 = "HTTP/1.1 200 OK" + CRLF + "Connection: keep-alive" + CRLF + "Content-Length: 10" + CRLF
                + CRLF + "response 2";

        String expected3 = "HTTP/1.1 200 OK" + CRLF + "Connection: keep-alive" + CRLF + "Content-Length: 10" + CRLF
                + CRLF + "response 3";

        String expected4 = "HTTP/1.1 200 OK" + CRLF + "Connection: close" + CRLF + "Content-Length: 10" + CRLF + CRLF
                + "response 4";

        String encoded1 = URLEncoder.encode(Base64.encodeToString(expected1.getBytes(), Base64.DEFAULT));
        String encoded2 = URLEncoder.encode(Base64.encodeToString(expected2.getBytes(), Base64.DEFAULT));
        String encoded3 = URLEncoder.encode(Base64.encodeToString(expected3.getBytes(), Base64.DEFAULT));
        String encoded4 = URLEncoder.encode(Base64.encodeToString(expected4.getBytes(), Base64.DEFAULT));

        String request = "GET " + uri1 + " HTTP/1.1" + CRLF + "Connection: keep-alive" + CRLF + "X-OC-Raw: " + encoded1
                + CRLF + CRLF + "GET " + uri2 + " HTTP/1.1" + CRLF + "Connection: keep-alive" + CRLF + "X-OC-Raw: "
                + encoded2 + CRLF + CRLF + "GET " + uri3 + " HTTP/1.1" + CRLF + "Connection: keep-alive" + CRLF
                + "X-OC-Raw: " + encoded3 + CRLF + CRLF + "GET " + uri4 + " HTTP/1.1" + CRLF + "Connection: close"
                + CRLF + "X-OC-Raw: " + encoded4 + CRLF + CRLF;

        HttpResponse response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);

        Pattern pattern = Pattern.compile("HTTP");
        Matcher matcher = pattern.matcher(response.getRawContent());
        int[] start = new int[4];
        int index = 0;
        while (matcher.find()) {
            if (index < start.length)
                start[index++] = matcher.start();
        }

        HttpResponse response1 = buildResponse(new StringBuilder(response.getRawContent().substring(start[0], start[1])));
        HttpResponse response2 = buildResponse(new StringBuilder(response.getRawContent().substring(start[1], start[2])));
        HttpResponse response3 = buildResponse(new StringBuilder(response.getRawContent().substring(start[2], start[3])));
        HttpResponse response4 = buildResponse(new StringBuilder(response.getRawContent().substring(start[3])));

        assertEquals(HttpStatus.SC_OK, response1.getStatusCode());
        assertEquals("keep-alive", response1.getHeaderField("Connection"));
        assertEquals("10", response1.getHeaderField("Content-Length"));
        assertEquals("response 1", response1.getBody());
        assertEquals(HttpStatus.SC_OK, response2.getStatusCode());
        assertEquals("keep-alive", response2.getHeaderField("Connection"));
        assertEquals("10", response2.getHeaderField("Content-Length"));
        assertEquals("response 2", response2.getBody());
        assertEquals(HttpStatus.SC_OK, response3.getStatusCode());
        assertEquals("keep-alive", response3.getHeaderField("Connection"));
        assertEquals("10", response3.getHeaderField("Content-Length"));
        assertEquals("response 3", response3.getBody());
        assertEquals(HttpStatus.SC_OK, response4.getStatusCode());
        assertEquals("close", response4.getHeaderField("Connection"));
        assertEquals("10", response4.getHeaderField("Content-Length"));
        assertEquals("response 4", response4.getBody());
    }

    public void testHttpPipeLineCaching() throws Exception {
        String uri1 = createTestResourceUri("ga_asimov_it_proxy_pipeline_caching_1", false, 80);
        PrepareResourceUtil.prepareResourceParallel(uri1, false);
        String uri2 = createTestResourceUri("ga_asimov_it_proxy_pipeline_caching_2", false, 80);
        PrepareResourceUtil.prepareResourceParallel(uri2, false);

        String body1 = "response 1";
        String body2 = "response 2";

        String expected1 = "HTTP/1.1 200 OK" + CRLF + "Connection: keep-alive" + CRLF + "Content-Length: 10" + CRLF
                + CRLF + body1;

        String expected2 = "HTTP/1.1 200 OK" + CRLF + "Connection: close" + CRLF + "Content-Length: 10" + CRLF + CRLF
                + body2;

        String encoded1 = URLEncoder.encode(Base64.encodeToString(expected1.getBytes(), Base64.DEFAULT));
        String encoded2 = URLEncoder.encode(Base64.encodeToString(expected2.getBytes(), Base64.DEFAULT));

        String request = "GET " + uri1 + " HTTP/1.1" + CRLF + "Connection: keep-alive" + CRLF + "X-OC-Raw: " + encoded1
                + CRLF + CRLF + "GET " + uri2 + " HTTP/1.1" + CRLF + "Connection: close" + CRLF + "X-OC-Raw: "
                + encoded2 + CRLF + CRLF;

        int requestId = 1;
        try {
            // 1 request
            long startTimeC = System.currentTimeMillis();
            long startTime = System.currentTimeMillis();
            HttpResponse response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);
            long endTime = System.currentTimeMillis();

            assertTrue("Request " + requestId++ + " should be MISSed", didRequestGoToNetwork(startTime, endTime, uri1));
            assertTrue("Request " + requestId++ + " should be MISSed", didRequestGoToNetwork(startTime, endTime, uri2));

            checkPipeliningReturnedExpected(response, body1.length(), body2.length(), body1, body2);


            logSleeping(MIN_NON_RMP_PERIOD - (System.currentTimeMillis() - startTimeC));

            // 2 request
            startTimeC = System.currentTimeMillis();
            startTime = System.currentTimeMillis();
            response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);
            endTime = System.currentTimeMillis();

            assertTrue("Request " + requestId++ + " should be MISSed", didRequestGoToNetwork(startTime, endTime, uri1));
            assertTrue("Request " + requestId++ + " should be MISSed", didRequestGoToNetwork(startTime, endTime, uri2));

            checkPipeliningReturnedExpected(response, body1.length(), body2.length(), body1, body2);

            logSleeping(MIN_NON_RMP_PERIOD - (System.currentTimeMillis() - startTimeC));

            // 3 request
            startTimeC = System.currentTimeMillis();
            startTime = System.currentTimeMillis();
            response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);
            endTime = System.currentTimeMillis();

            assertTrue("Request " + requestId++ + " should be MISSed", didRequestGoToNetwork(startTime, endTime, uri1));
            assertTrue("Request " + requestId++ + " should be MISSed", didRequestGoToNetwork(startTime, endTime, uri2));

            checkPipeliningReturnedExpected(response, body1.length(), body2.length(), body1, body2);

            logSleeping(MIN_NON_RMP_PERIOD - (System.currentTimeMillis() - startTimeC));

            // 4 request
            startTimeC = System.currentTimeMillis();
            startTime = System.currentTimeMillis();
            response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);
            endTime = System.currentTimeMillis();

            assertFalse("Request " + requestId++ + " should be HITed", didRequestGoToNetwork(startTime, endTime, uri1));
            assertFalse("Request " + requestId++ + " should be HITed", didRequestGoToNetwork(startTime, endTime, uri2));

            checkPipeliningReturnedExpected(response, body1.length(), body2.length(), body1, body2);
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri1);
            PrepareResourceUtil.invalidateResourceSafely(uri2);
        }
    }

    @LargeTest
    public void testHttpPipeliningWithFirstResponseCaching() throws Throwable {
        String uri1 = createTestResourceUri("ga_asimov_it_proxy_pipeline_with_cache_first_response_1", false, 80);
        String uri2 = createTestResourceUri("ga_asimov_it_proxy_pipeline_with_cache_first_response_2", false, 80);

        String expected1 = "HTTP/1.1 200 OK" + CRLF + "Connection: keep-alive" + CRLF + "Content-length: 5" + CRLF
                + CRLF + "body1";
        String expected2 = "HTTP/1.1 200 OK" + CRLF + "Connection: close" + CRLF + "Content-Length: 5" + CRLF + CRLF
                + "body2";
        String encoded1 = URLEncoder.encode(Base64.encodeToString(expected1.getBytes(), Base64.DEFAULT));
        String encoded2 = URLEncoder.encode(Base64.encodeToString(expected2.getBytes(), Base64.DEFAULT));

        HttpRequest request1 = createRequest().setUri(uri1).setMethod("GET")
                .addHeaderField("Connection", "keep-alive").addHeaderField("X-OC-Raw", encoded1).getRequest();

        int requestId = 0;
        try {
            for (int i = 0; i < 3; i++) {
                checkMiss(request1, ++requestId, (int) MIN_RAPID_CACHING_PERIOD_GA);
            }

            checkHit(request1, ++requestId, (int) MIN_RAPID_CACHING_PERIOD_GA);

            String requestPipilining = "GET " + uri1 + " HTTP/1.1" + CRLF + "Connection: keep-alive" + CRLF
                    + "X-OC-Raw: " + encoded1 + CRLF + CRLF + "GET " + uri2 + " HTTP/1.1" + CRLF + "Connection: close"
                    + CRLF + "X-OC-Raw: " + encoded2 + CRLF + CRLF;

            long startTime = System.currentTimeMillis();
            HttpResponse response = sendRequest(requestPipilining.getBytes(), TEST_RESOURCE_HOST);
            long endTime = System.currentTimeMillis();

            assertFalse("Request " + requestId++ + " should be HITed", didRequestGoToNetwork(startTime, endTime, uri1));
            assertTrue("Request " + requestId++ + " should be MISSed", didRequestGoToNetwork(startTime, endTime, uri2));

            int httpIndex = response.getRawContent().indexOf("HTTP", 5);
            assertTrue("Expected two responses but was one", httpIndex != -1);

            HttpResponse response1 = buildResponse(new StringBuilder(response.getRawContent().substring(0, httpIndex)));
            HttpResponse response2 = buildResponse(new StringBuilder(response.getRawContent().substring(httpIndex)));

            assertEquals(HttpStatus.SC_OK, response1.getStatusCode());
            assertEquals("keep-alive", response1.getHeaderField("Connection"));
            assertEquals("body1", response1.getBody());

            assertEquals(HttpStatus.SC_OK, response2.getStatusCode());
            assertEquals("close", response2.getHeaderField("Connection"));
            assertEquals("body2", response2.getBody());

        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri1);
            PrepareResourceUtil.invalidateResourceSafely(uri2);
        }
    }

    @LargeTest
    public void testHttpPipeliningWithSecondResponseCaching() throws Throwable {
        String uri1 = createTestResourceUri("ga_asimov_it_proxy_pipeline_with_cache_second_response_1", false, 80);
        String uri2 = createTestResourceUri("ga_asimov_it_proxy_pipeline_with_cache_second_response_2", false, 80);

        String expected1 = "HTTP/1.1 200 OK" + CRLF + "Connection: keep-alive" + CRLF + "Content-length: 5" + CRLF
                + CRLF + "body1";
        String expected2 = "HTTP/1.1 200 OK" + CRLF + "Connection: close" + CRLF + "Content-Length: 5" + CRLF + CRLF
                + "body2";
        String encoded1 = URLEncoder.encode(Base64.encodeToString(expected1.getBytes(), Base64.DEFAULT));
        String encoded2 = URLEncoder.encode(Base64.encodeToString(expected2.getBytes(), Base64.DEFAULT));

        HttpRequest request1 = createRequest().setUri(uri2).setMethod("GET")
                .addHeaderField("Connection", "close").addHeaderField("X-OC-Raw", encoded2).getRequest();

        int requestId = 0;
        try {
            for (int i = 0; i < 3; i++) {
                checkMiss(request1, ++requestId, (int) MIN_RAPID_CACHING_PERIOD_GA);
            }

            checkHit(request1, ++requestId);

            String requestPipilining = "GET " + uri1 + " HTTP/1.1" + CRLF + "Connection: keep-alive" + CRLF
                    + "X-OC-Raw: " + encoded1 + CRLF + CRLF + "GET " + uri2 + " HTTP/1.1" + CRLF + "Connection: close"
                    + CRLF + "X-OC-Raw: " + encoded2 + CRLF + CRLF;

            long startTime = System.currentTimeMillis();
            HttpResponse response = sendRequest(requestPipilining.getBytes(), TEST_RESOURCE_HOST);
            long endTime = System.currentTimeMillis();

            assertTrue("Request " + requestId++ + " should be MISSed", didRequestGoToNetwork(startTime, endTime, uri1));
            assertFalse("Request " + requestId++ + " should be HITed", didRequestGoToNetwork(startTime, endTime, uri2));

            int httpIndex = response.getRawContent().indexOf("HTTP", 5);
            assertTrue("Expected two responses but was one", httpIndex != -1);

            HttpResponse response1 = buildResponse(new StringBuilder(response.getRawContent().substring(0, httpIndex)));
            HttpResponse response2 = buildResponse(new StringBuilder(response.getRawContent().substring(httpIndex)));

            assertEquals(HttpStatus.SC_OK, response1.getStatusCode());
            assertEquals("keep-alive", response1.getHeaderField("Connection"));
            assertEquals("body1", response1.getBody());

            assertEquals(HttpStatus.SC_OK, response2.getStatusCode());
            assertEquals("close", response2.getHeaderField("Connection"));
            assertEquals("body2", response2.getBody());

        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri1);
            PrepareResourceUtil.invalidateResourceSafely(uri2);
        }
    }

    @LargeTest
    public void testHttpPipeliningNotIdempotentMethodPost() throws Throwable {
        String uri = createTestResourceUri("ga_asimov_it_proxy_pipeline_not_idempotent_method_1", false, 80);
        String uri2 = createTestResourceUri("ga_asimov_it_proxy_pipeline_not_idempotent_method_2", false, 80);

        String expected1 = "HTTP/1.1 200 OK" + CRLF + "Connection: keep-alive" + CRLF + "Content-length: 5" + CRLF
                + CRLF + "body1";
        String expected2 = "HTTP/1.1 200 OK" + CRLF + "Connection: close" + CRLF + "Content-Length: 5" + CRLF + CRLF
                + "body2";

        String encoded1 = URLEncoder.encode(Base64.encodeToString(expected1.getBytes(), Base64.DEFAULT));
        String encoded2 = URLEncoder.encode(Base64.encodeToString(expected2.getBytes(), Base64.DEFAULT));

        String request = "POST " + uri + " HTTP/1.1" + CRLF + "Connection: keep-alive" + CRLF + "X-OC-Raw: " + encoded1
                + CRLF + "Content-length: 0" + CRLF + CRLF + "POST " + uri2 + " HTTP/1.1" + CRLF + "Connection: close"
                + CRLF + "X-OC-Raw: " + encoded2 + CRLF + "Content-length: 4" + CRLF + CRLF + "post";

        HttpResponse response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);

        int httpIndex = response.getRawContent().indexOf("HTTP", 5);
        assertTrue("Expected two responses but was one", httpIndex != -1);

        HttpResponse response1 = buildResponse(new StringBuilder(response.getRawContent().substring(0, httpIndex)));
        HttpResponse response2 = buildResponse(new StringBuilder(response.getRawContent().substring(httpIndex)));

        assertEquals(HttpStatus.SC_OK, response1.getStatusCode());
        assertEquals("keep-alive", response1.getHeaderField("Connection"));
        assertEquals("body1", response1.getBody());

        assertEquals(HttpStatus.SC_OK, response2.getStatusCode());
        assertEquals("close", response2.getHeaderField("Connection"));
        assertEquals("body2", response2.getBody());
    }

    @LargeTest
    public void testHttpPipeliningInvalidVersion() throws Throwable {
        String uri = createTestResourceUri("ga_asimov_it_proxy_pipeline_invalid_version_1", false, 80);
        String uri2 = createTestResourceUri("ga_asimov_it_proxy_pipeline_invalid_version_2", false, 80);

        String expected1 = "HTTP/1.0 200 OK" + CRLF + "Connection: keep-alive" + CRLF + "Content-length: 5" + CRLF
                + CRLF + "body1";
        String expected2 = "HTTP/1.0 200 OK" + CRLF + "Connection: close" + CRLF + "Content-Length: 5" + CRLF + CRLF
                + "body2";

        String encoded1 = URLEncoder.encode(Base64.encodeToString(expected1.getBytes(), Base64.DEFAULT));
        String encoded2 = URLEncoder.encode(Base64.encodeToString(expected2.getBytes(), Base64.DEFAULT));

        String request = "POST " + uri + " HTTP/1.0" + CRLF + "Connection: keep-alive" + CRLF + "X-OC-Raw: " + encoded1
                + CRLF + "Content-length: 0" + CRLF + CRLF + "POST " + uri2 + " HTTP/1.0" + CRLF + "Connection: close"
                + CRLF + "X-OC-Raw: " + encoded2 + CRLF + "Content-length: 4" + CRLF + CRLF + "post";

        HttpResponse response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);

        int httpIndex = response.getRawContent().indexOf("HTTP", 5);
        assertTrue("Expected two responses but was one", httpIndex != -1);

        HttpResponse response1 = buildResponse(new StringBuilder(response.getRawContent().substring(0, httpIndex)));
        HttpResponse response2 = buildResponse(new StringBuilder(response.getRawContent().substring(httpIndex)));

        assertEquals(HttpStatus.SC_OK, response1.getStatusCode());
        assertEquals("keep-alive", response1.getHeaderField("Connection"));
        assertEquals("body1", response1.getBody());

        assertEquals(HttpStatus.SC_OK, response2.getStatusCode());
        assertEquals("close", response2.getHeaderField("Connection"));
        assertEquals("body2", response2.getBody());
    }

    @LargeTest
    public void testHttpAndNotHttpTraffic() throws Throwable {
        String uri = createTestResourceUri("ga_asimov_it_proxy_pipeline_http_and_not_http_traffic_1", false, 80);
        String uri2 = createTestResourceUri("ga_asimov_it_proxy_pipeline_http_and_not_http_traffic_2", false, 80);

        String expected1 = "HTTP/1.1 200 OK" + CRLF + "Connection: keep-alive" + CRLF + "Content-length: 4" + CRLF
                + CRLF + "bodyLostData1";
        String expected2 = "HTTP/1.1 200 OK" + CRLF + "Connection: close" + CRLF + "Content-Length: 5" + CRLF + CRLF
                + "body2";

        String encoded1 = URLEncoder.encode(Base64.encodeToString(expected1.getBytes(), Base64.DEFAULT));
        String encoded2 = URLEncoder.encode(Base64.encodeToString(expected2.getBytes(), Base64.DEFAULT));

        String request = "GET " + uri + " HTTP/1.1" + CRLF + "Connection: keep-alive" + CRLF + "X-OC-Raw: " + encoded1
                + CRLF + CRLF + "GET " + uri2 + " HTTP/1.1" + CRLF + "Connection: close" + CRLF + "X-OC-Raw: "
                + encoded2 + CRLF + CRLF;

        HttpResponse response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);

        int httpIndex = response.getRawContent().indexOf("HTTP", 5);
        if (httpIndex != -1) {
            System.out.println("Index=" + httpIndex);
            HttpResponse response1 = buildResponse(new StringBuilder(response.getRawContent().substring(0, httpIndex)));
            HttpResponse response2 = buildResponse(new StringBuilder(response.getRawContent().substring(httpIndex)));

            assertEquals(HttpStatus.SC_OK, response1.getStatusCode());
            assertEquals("keep-alive", response1.getHeaderField("Connection"));
            assertEquals("bodyLostData1", response1.getBody());

            assertEquals(HttpStatus.SC_OK, response2.getStatusCode());
            assertEquals("close", response2.getHeaderField("Connection"));
            assertEquals("body2", response2.getBody());

        } else {
            System.out.println("IndexElse=" + httpIndex);
            assertEquals(HttpStatus.SC_OK, response.getStatusCode());
            assertEquals("keep-alive", response.getHeaderField("Connection"));
            assertEquals("body", response.getBody());
        }
    }

    /**
     * IGNORED: DEVIT-380
     */
    @Ignore
    public void testPipeliningWithSimultaneousCacheInvalidate() throws Throwable {

        final StartPollTask startPollTask = new StartPollTask();

        String RESOURCE1 = "ga_asimov_it_simultaneous_cache_invalidate_1";
        String RESOURCE2 = "ga_asimov_it_simultaneous_cache_invalidate_2";
        String uri1 = createTestResourceUri(RESOURCE1, false, 80);
        String uri2 = createTestResourceUri(RESOURCE2, false, 80);

        String body1 = "body1";
        String body2 = "body2";
        String body1Invalidate = "changedBody1";
        String body2Invalidate = "changedBody2";

        String expected1 = "HTTP/1.1 200 OK" + CRLF + "Connection: keep-alive" + CRLF + "Content-Length: " + body1.length() + CRLF
                + CRLF + body1;

        String expected2 = "HTTP/1.1 200 OK" + CRLF + "Connection: close" + CRLF + "Content-Length: " + body2.length() + CRLF + CRLF
                + body2;

        String encoded1 = URLEncoder.encode(Base64.encodeToString(expected1.getBytes(), Base64.DEFAULT));
        String encoded2 = URLEncoder.encode(Base64.encodeToString(expected2.getBytes(), Base64.DEFAULT));

        String request = "GET " + uri1 + " HTTP/1.1" + CRLF + "Connection: keep-alive" + CRLF + "X-OC-Raw: " + encoded1
                + CRLF + CRLF + "GET " + uri2 + " HTTP/1.1" + CRLF + "Connection: close" + CRLF + "X-OC-Raw: "
                + encoded2 + CRLF + CRLF;

        int requestId = 1;

        LogcatUtil logcatUtil = new LogcatUtil(getContext(), startPollTask);

        try {
            PrepareResourceUtil.prepareDiffResource(uri1, body1);
            PrepareResourceUtil.prepareDiffResource(uri2, body2);

            logcatUtil.start();
            // 1-6
            for (int i = 0; i < 3; i++) {
                long startTime = System.currentTimeMillis();
                HttpResponse response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);
                long endTime = System.currentTimeMillis();

                assertTrue("Request " + requestId++ + " should be MISSed", didRequestGoToNetwork(startTime, endTime, uri1));
                assertTrue("Request " + requestId++ + " should be MISSed", didRequestGoToNetwork(startTime, endTime, uri2));

                checkPipeliningReturnedExpected(response, body1.length(), body2.length(), body1, body2);

                logSleeping(MIN_NON_RMP_PERIOD - System.currentTimeMillis() + startTime);
            }

            logcatUtil.stop();
            assertTrue("Start of polling should be reported in client log", !startPollTask.getLogEntries().isEmpty());

            long startTimeC = System.currentTimeMillis();

            // 7,8
            long startTime = System.currentTimeMillis();
            HttpResponse response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);
            long endTime = System.currentTimeMillis();

            assertFalse("Request " + requestId++ + " should be HITed", didRequestGoToNetwork(startTime, endTime, uri1));
            assertFalse("Request " + requestId++ + " should be HITed", didRequestGoToNetwork(startTime, endTime, uri2));

            checkPipeliningReturnedExpected(response, body1.length(), body2.length(), body1, body2);

            PrepareResourceUtil.prepareDiffResource(uri1, body1Invalidate);
            PrepareResourceUtil.prepareDiffResource(uri2, body2Invalidate);

            logSleeping(50 * 1000);
            for (StartPollWrapper startPollWrapper : startPollTask.getLogEntries()) {
                sendInvSms(true, startPollWrapper);
            }

            logSleeping(MIN_NON_RMP_PERIOD - System.currentTimeMillis() + startTimeC);

            // 9,10
            startTime = System.currentTimeMillis();
            response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);
            endTime = System.currentTimeMillis();

            assertFalse("Request " + requestId++ + " should be HITed", didRequestGoToNetwork(startTime, endTime, uri1));
            assertFalse("Request " + requestId++ + " should be HITed", didRequestGoToNetwork(startTime, endTime, uri2));

            checkPipeliningReturnedExpected(response, body1.length(), body2.length(), body1Invalidate, body2Invalidate);

            // 11,12
            startTimeC = System.currentTimeMillis();
            startTime = System.currentTimeMillis();
            response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);
            endTime = System.currentTimeMillis();

            assertFalse("Request " + requestId++ + " should be HITed", didRequestGoToNetwork(startTime, endTime, uri1));
            assertFalse("Request " + requestId++ + " should be HITed", didRequestGoToNetwork(startTime, endTime, uri2));

            checkPipeliningReturnedExpected(response, body1.length(), body2.length(), body1Invalidate, body2Invalidate);
            logSleeping(MIN_NON_RMP_PERIOD - System.currentTimeMillis() + startTimeC);

            // 13,14
            startTime = System.currentTimeMillis();
            response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);
            endTime = System.currentTimeMillis();

            assertFalse("Request " + requestId++ + " should be HITed", didRequestGoToNetwork(startTime, endTime, uri1));
            assertFalse("Request " + requestId++ + " should be HITed", didRequestGoToNetwork(startTime, endTime, uri2));
            ;

            checkPipeliningReturnedExpected(response, body1.length(), body2.length(), body1Invalidate, body2Invalidate);

        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri1);
            PrepareResourceUtil.invalidateResourceSafely(uri2);
            if (logcatUtil.isRunning()) logcatUtil.stop();
        }
    }

    /**
     * IGNORED: DEVIT-380
     */
    @LargeTest
    @Ignore
    public void testPipeliningWithCacheInvalidate() throws Throwable {
        final StartPollTask startPollTask = new StartPollTask();

        String RESOURCE1 = "ga_asimov_it_cache_invalidate_1";
        String RESOURCE2 = "ga_asimov_it_cache_invalidate_2";
        String uri1 = createTestResourceUri(RESOURCE1, false, 80);
        String uri2 = createTestResourceUri(RESOURCE2, false, 80);

        String body1 = "body1";
        String body2 = "body2";
        String body1Invalidate = "changedBody1";
        String body2Invalidate = "changedBody2";

        String expected1 = "HTTP/1.1 200 OK" + CRLF + "Connection: keep-alive" + CRLF + "Content-Length: " + body1.length() + CRLF
                + CRLF + body1;

        String expected2 = "HTTP/1.1 200 OK" + CRLF + "Connection: close" + CRLF + "Content-Length: " + body2.length() + CRLF + CRLF
                + body2;

        String encoded1 = URLEncoder.encode(Base64.encodeToString(expected1.getBytes(), Base64.DEFAULT));
        String encoded2 = URLEncoder.encode(Base64.encodeToString(expected2.getBytes(), Base64.DEFAULT));

        String request = "GET " + uri1 + " HTTP/1.1" + CRLF + "Connection: keep-alive" + CRLF + "X-OC-Raw: " + encoded1
                + CRLF + CRLF + "GET " + uri2 + " HTTP/1.1" + CRLF + "Connection: close" + CRLF + "X-OC-Raw: "
                + encoded2 + CRLF + CRLF;

        int requestId = 1;

        try {
            PrepareResourceUtil.prepareDiffResource(uri1, body1);
            PrepareResourceUtil.prepareDiffResource(uri2, body2);

            LogcatUtil logcatUtil = new LogcatUtil(getContext(), startPollTask);

            logcatUtil.start();
            // 1-6
            for (int i = 0; i < 3; i++) {
                long startTime = System.currentTimeMillis();
                HttpResponse response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);
                long endTime = System.currentTimeMillis();

                assertTrue("Request " + requestId++ + " should be MISSed", didRequestGoToNetwork(startTime, endTime, uri1));
                assertTrue("Request " + requestId++ + " should be MISSed", didRequestGoToNetwork(startTime, endTime, uri2));

                checkPipeliningReturnedExpected(response, body1.length(), body2.length(), body1, body2);

                logSleeping(MIN_NON_RMP_PERIOD - System.currentTimeMillis() + startTime);
            }

            logcatUtil.stop();
            assertTrue("Start of polling should be reported in client log", !startPollTask.getLogEntries().isEmpty());

            long startTimeC = System.currentTimeMillis();

            // 7,8
            long startTime = System.currentTimeMillis();
            HttpResponse response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);
            long endTime = System.currentTimeMillis();

            assertFalse("Request " + requestId++ + " should be HITed", didRequestGoToNetwork(startTime, endTime, uri1));
            assertFalse("Request " + requestId++ + " should be HITed", didRequestGoToNetwork(startTime, endTime, uri2));

            PrepareResourceUtil.prepareDiffResource(uri1, body1Invalidate);

            checkPipeliningReturnedExpected(response, body1.length(), body2.length(), body1, body2);

            logSleeping(55 * 1000);
            sendInvSms(true, startPollTask.getLogEntries().get(0));

            logSleeping(MIN_NON_RMP_PERIOD - System.currentTimeMillis() + startTimeC);

            // 9,10
            startTime = System.currentTimeMillis();
            response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);
            endTime = System.currentTimeMillis();

            assertFalse("Request " + requestId++ + " should be HITed", didRequestGoToNetwork(startTime, endTime, uri1));
            assertFalse("Request " + requestId++ + " should be HITed", didRequestGoToNetwork(startTime, endTime, uri2));

            checkPipeliningReturnedExpected(response, body1.length(), body2.length(), body1Invalidate, body2);

            // 11,12
            startTimeC = System.currentTimeMillis();
            startTime = System.currentTimeMillis();
            response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);
            endTime = System.currentTimeMillis();

            assertTrue("Request " + requestId++ + " should be MISSed", didRequestGoToNetwork(startTime, endTime, uri1));
            assertFalse("Request " + requestId++ + " should be HITed", didRequestGoToNetwork(startTime, endTime, uri2));

            checkPipeliningReturnedExpected(response, body1.length(), body2.length(), body1Invalidate, body2Invalidate);
            logSleeping(MIN_NON_RMP_PERIOD - System.currentTimeMillis() + startTimeC);

            // 13,14
            startTimeC = System.currentTimeMillis();
            startTime = System.currentTimeMillis();
            response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);
            endTime = System.currentTimeMillis();

            assertFalse("Request " + requestId++ + " should be HITed", didRequestGoToNetwork(startTime, endTime, uri1));
            assertTrue("Request " + requestId++ + " should be MISSed", didRequestGoToNetwork(startTime, endTime, uri2));

            checkPipeliningReturnedExpected(response, body1.length(), body2.length(), body1Invalidate, body2Invalidate);
            logSleeping(MIN_NON_RMP_PERIOD - System.currentTimeMillis() + startTimeC);

            // 15,16
            startTime = System.currentTimeMillis();
            response = sendRequest(request.getBytes(), TEST_RESOURCE_HOST);
            endTime = System.currentTimeMillis();

            assertFalse("Request " + requestId++ + " should be HITed", didRequestGoToNetwork(startTime, endTime, uri1));
            assertFalse("Request " + requestId++ + " should be HITed", didRequestGoToNetwork(startTime, endTime, uri2));

            checkPipeliningReturnedExpected(response, body1.length(), body2.length(), body1Invalidate, body2Invalidate);

        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri1);
            PrepareResourceUtil.invalidateResourceSafely(uri2);
        }
    }

    public void testPipeliningRMPPollCaching() throws Throwable {

        String RESOURCE1 = "ga_asimov_it_pp1";
        String RESOURCE2 = "ga_asimov_it_pp2";
        String uri1 = createTestResourceUri(RESOURCE1, false, 80);
        String uri2 = createTestResourceUri(RESOURCE2, false, 80);

        String body1 = "body1";
        String body2 = "body2";

        String expected1Close = "HTTP/1.1 200 OK" + CRLF + "Connection: close" + CRLF + "Content-Length: " + body1.length() + CRLF
                + CRLF + body1;
        String expected1 = "HTTP/1.1 200 OK" + CRLF + "Connection: keep-alive" + CRLF + "Content-Length: " + body1.length() + CRLF
                + CRLF + body1;

        String expected2 = "HTTP/1.1 200 OK" + CRLF + "Connection: close" + CRLF + "Content-Length: " + body2.length() + CRLF + CRLF
                + body2;

        String encoded1 = URLEncoder.encode(Base64.encodeToString(expected1.getBytes(), Base64.DEFAULT));
        String encoded2 = URLEncoder.encode(Base64.encodeToString(expected2.getBytes(), Base64.DEFAULT));
        String encoded3 = URLEncoder.encode(Base64.encodeToString(expected1Close.getBytes(), Base64.DEFAULT));

        String request1Close = "GET " + uri1 + " HTTP/1.1" + CRLF + "Connection: close" + CRLF + "X-OC-Raw: " + encoded3
                + CRLF + CRLF;
        String request1 = "GET " + uri1 + " HTTP/1.1" + CRLF + "Connection: keep-alive" + CRLF + "X-OC-Raw: " + encoded1
                + CRLF + CRLF;
        String request2 = "GET " + uri2 + " HTTP/1.1" + CRLF + "Connection: close" + CRLF + "X-OC-Raw: "
                + encoded2 + CRLF + CRLF;

        String requestCombined = request1 + request2;

        int requestId = 1;
        try {

            long startTime = System.currentTimeMillis();
            HttpResponse response = sendRequest(request1Close.getBytes(), TEST_RESOURCE_HOST);
            long endTime = System.currentTimeMillis();

            assertTrue("Request " + requestId++ + " should be MISSed", didRequestGoToNetwork(startTime, endTime, uri1));

            logSleeping(MIN_RMP_PERIOD - (response.getDuration() + 1 * 1000));

            for (int i = 0; i < 2; i++) {
                startTime = System.currentTimeMillis();
                response = sendRequest(requestCombined.getBytes(), TEST_RESOURCE_HOST);
                endTime = System.currentTimeMillis();

                assertTrue("Request " + requestId++ + " should be MISSed", didRequestGoToNetwork(startTime, endTime, uri1));
                assertTrue("Request " + requestId++ + " should be MISSed", didRequestGoToNetwork(startTime, endTime, uri2));

                checkPipeliningReturnedExpected(response, body1.length(), body2.length(), body1, body2);

                logSleeping(MIN_RMP_PERIOD - (response.getDuration() + 1 * 1000));
            }

            startTime = System.currentTimeMillis();
            response = sendRequest(requestCombined.getBytes(), TEST_RESOURCE_HOST);
            endTime = System.currentTimeMillis();

            assertFalse("Request " + requestId++ + " should be HITed", didRequestGoToNetwork(startTime, endTime, uri1));
            assertTrue("Request " + requestId++ + " should be MISSed", didRequestGoToNetwork(startTime, endTime, uri2));

            checkPipeliningReturnedExpected(response, body1.length(), body2.length(), body1, body2);

            logSleeping(MIN_RMP_PERIOD - (response.getDuration() + 1 * 1000));

            startTime = System.currentTimeMillis();
            response = sendRequest(requestCombined.getBytes(), TEST_RESOURCE_HOST);
            endTime = System.currentTimeMillis();

            assertFalse("Request " + requestId++ + " should be HITed", didRequestGoToNetwork(startTime, endTime, uri1));
            assertFalse("Request " + requestId++ + " should be HITed", didRequestGoToNetwork(startTime, endTime, uri2));

            checkPipeliningReturnedExpected(response, body1.length(), body2.length(), body1, body2);
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri1);
            PrepareResourceUtil.invalidateResourceSafely(uri2);
        }
    }
}
