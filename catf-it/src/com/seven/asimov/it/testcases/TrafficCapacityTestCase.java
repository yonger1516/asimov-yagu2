package com.seven.asimov.it.testcases;

import com.seven.asimov.it.asserts.CATFAssert;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.TcpDumpTestCase;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class TrafficCapacityTestCase extends TcpDumpTestCase {

    protected final long SIZE = 5242880;
    protected final int ITERATION = 2;

    protected HttpResponse sendHttpMiss(int requestId, HttpRequest request)
            throws MalformedURLException, IOException, URISyntaxException {
        HttpResponse response;
        boolean isSslModeOn = false;
        String uri = request.getUri();
        if (uri.startsWith("https://")) isSslModeOn = true; // we are going to send HTTPS request
        logRequest(requestId, uri);
        if (isSslModeOn) {
            response = sendHttpsRequest(request, this);
        } else {
            response = sendRequest2(request, false, false, 5 * 60 * 1000);
        }
        CATFAssert.assertEquals("StatusCode ", HttpStatus.SC_OK, response.getStatusCode());
        return response;
    }

    protected HttpResponse sendHttpsMiss(int requestId, HttpRequest request) throws Throwable {
        logRequest(requestId, request.getUri());
        HttpResponse response = sendHttpsRequest(request, this);
        CATFAssert.assertEquals("StatusCode ", HttpStatus.SC_OK, response.getStatusCode());
        return response;
    }
}
