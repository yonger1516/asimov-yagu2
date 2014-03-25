package com.seven.asimov.it.testcases;

import com.seven.asimov.it.base.*;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.tcpdump.DbAdapter;
import com.seven.asimov.it.utils.tcpdump.Direction;
import com.seven.asimov.it.utils.tcpdump.Interface;
import com.seven.asimov.it.utils.tcpdump.TcpPacket;
import junit.framework.Assert;
import org.apache.http.HttpStatus;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.seven.asimov.it.base.constants.TFConstantsIF.CRLF;

public class ProxyGATestCase extends TcpDumpTestCase{

    public static long MIN_RAPID_CACHING_PERIOD_GA = 5000L;
    protected static final String TAB = "\t";

    protected void getMultilineHeaderResponse(boolean bypassOc) throws Throwable {
        final String RESOURCE_URI = "MultyHeaders_asimov_0003" + (bypassOc ? 1 : 2);
        final String uri = createTestResourceUri(RESOURCE_URI);
        String expectedBody = "body";
        String expected = "HTTP/1.0 200 OK" + CRLF + "Content-Type: text/plain" + CRLF
                + "Cache-Control: private, no-store, no-cache, must-revalidate, post-check=0, pre-check=0" + CRLF
                + "Pragma: no-cache" + CRLF + "Connection: close" + CRLF + "DefaultHeader: value1;" + CRLF + TAB
                + "value2;" + CRLF + TAB + "value3;" + CRLF + TAB + "value4;" + CRLF + "DefaultHeader2: value;" + CRLF
                + CRLF + expectedBody;
        HttpRequest request = createRequest()
                .setMethod("GET")
                .setUri(uri)
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Raw",
                        URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT))).getRequest();

        HttpResponse response = sendRequest2(request, false, bypassOc);
        Assert.assertTrue(("value1;" + "   " + "value2;" + "   " + "value3;" + "   " + "value4;")
                .equalsIgnoreCase(response.getHeaderField("DefaultHeader")));

    }

    protected void checkPipeliningReturnedExpected(HttpResponse response, int contentLength1, int contentLength2,
                                                   String body1, String body2) {
        Pattern pattern = Pattern.compile("HTTP");
        Matcher matcher = pattern.matcher(response.getRawContent());
        int[] start = new int[2];
        int index = 0;
        while (matcher.find()) {
            if (index < start.length)
                start[index++] = matcher.start();
        }

        HttpResponse response1 = buildResponse(new StringBuilder(response.getRawContent().substring(start[0],
                start[1])));
        HttpResponse response2 = buildResponse(new StringBuilder(response.getRawContent().substring(start[1])));

        assertEquals(HttpStatus.SC_OK, response1.getStatusCode());
        assertEquals("keep-alive", response1.getHeaderField("Connection"));
        assertEquals(Integer.toString(contentLength1), response1.getHeaderField("Content-Length"));
        assertEquals(body1, response1.getBody());
        assertEquals(HttpStatus.SC_OK, response2.getStatusCode());
        assertEquals("close", response2.getHeaderField("Connection"));
        assertEquals(Integer.toString(contentLength2), response2.getHeaderField("Content-Length"));
        assertEquals(body2, response2.getBody());
    }

    protected boolean didRequestGoToNetwork(long startTime, long endTime, String uri) {
        boolean isNetwork = false;
        List<TcpPacket> packets = DbAdapter.getInstance(getContext()).getTcpPackets(startTime, endTime, true);
        for (TcpPacket tcpPacket: packets) {
            if (tcpPacket.getDataBytes() != null && tcpPacket.isPsh() && tcpPacket.getDirection() == Direction.FROM_US
                    && new String(tcpPacket.getDataBytes()).contains(uri)) {
                isNetwork = isNetwork || tcpPacket.getInterface() == Interface.NETWORK;
            }
        }
        return isNetwork;
    }

    protected void checkLargePost(int size) throws Exception {
        String uri = createTestResourceUri("https_asimov_it_cv_007_" + size , true);
        PrepareResourceUtil.prepareResource(uri, false);

        char expectedBody = 'c';

        String addHeaders = "Age: 1" + CRLF + "Server: Apache-Coyote/1.1" + CRLF + "Vary: Accept-Encoding" + CRLF
                + "Header1: header1" + CRLF + "Header2: header2";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            sb.append(expectedBody);
        }

        final HttpRequest request = createRequest().setUri(uri).setMethod("POST")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-AddHeader:", addHeadersEncoded).setBody(sb.toString())
                .addHeaderField("X-OC-BodyMirror", "true").getRequest();

        HttpResponse response = sendRequest(request, this, false, false, AsimovTestCase.Body.BODY_HASH);
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals(Integer.toString(size), response.getHeaderField("Content-Length"));
        assertEquals("1", response.getHeaderField("Age"));
        assertEquals("Apache-Coyote/1.1", response.getHeaderField("Server"));
        assertEquals("Accept-Encoding", response.getHeaderField("Vary"));
        assertEquals("header1", response.getHeaderField("Header1"));
        assertEquals("header2", response.getHeaderField("Header2"));
        byte[] expectedHash = TestUtil.getStreamedHash(expectedBody, size);
        assertTrue(Arrays.equals(expectedHash, response.getBodyHash()));
    }
}
