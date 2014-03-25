package com.seven.asimov.it.tests.caching.rfc.revalidation;

import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.base.Base64;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.testcases.CachingRevalidationTestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.date.DateUtil;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.seven.asimov.it.base.constants.TFConstantsIF.CRLF;

/**
 * IGNORED due to OC version 2.4, where revalidation is not enabled. Should be un-ignored and checked in release 3.0
 * Logic of the test wasn't checked from release 2.3 because revalidation had switched off.
 */
@Ignore
public class RfcTests extends CachingRevalidationTestCase {

    public void test_001_RfcCheckExpiresOfCache() throws Throwable {
        long now = System.currentTimeMillis();
        final String path = "asimov_it_http_rfc_0001";
        String uri = createTestResourceUri(path);

        String expected = "HTTP/1.1 200 OK" + CRLF
                + "Connection: close" + CRLF
                + "Date: " + DateUtil.format(new Date(now)) + CRLF
                + "Expires: " + DateUtil.format(new Date(now + 180000)) + CRLF
                + "Accept-Ranges: bytes" + CRLF
                + "Content-Length: 50" + CRLF
                + CRLF
                + "11111111111111111111111111111111111111111111111111";

        String expectedEncoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));

        HttpRequest request1 = createRequest()
                .setUri(uri)
                .setMethod("GET")
                .addHeaderField("X-OC-Raw", expectedEncoded)
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("User-Agent", "Android/TWC")
                .addHeaderField(
                        "Accept",
                        "text/html, application/xml;q=0.9, application/xhtml+xml, image/png, image/webp, image/jpeg, image/gif, image/x-xbitmap, */*;q=0.1")
                .addHeaderField("Accept-Language", "ru-RU,ru;q=0.9,en;q=0.8").getRequest();

        // second resource
        final String path2 = "asimov_it_http_rfc_0002";
        uri = createTestResourceUri(path2);

        expected = "HTTP/1.1 200 OK" + CRLF
                + "Connection: close" + CRLF
                + "Date: " + DateUtil.format(new Date(now)) + CRLF
                + "Accept-Ranges: bytes" + CRLF
                + "Cache-Control: max-age=180" + CRLF
                + "Content-Length: 50" + CRLF
                + CRLF
                + "22222222222222222222222222222222222222222222222222";

        expectedEncoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));

        HttpRequest request2 = createRequest()
                .setUri(uri)
                .setMethod("GET")
                .addHeaderField("X-OC-Raw", expectedEncoded)
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("User-Agent", "Android/TWC")
                .addHeaderField(
                        "Accept",
                        "text/html, application/xml;q=0.9, application/xhtml+xml, image/png, image/webp, image/jpeg, image/gif, image/x-xbitmap, */*;q=0.1")
                .addHeaderField("Accept-Language", "ru-RU,ru;q=0.9,en;q=0.8").getRequest();

        List<HttpRequest> requests = new ArrayList<HttpRequest>();
        requests.add(request1);
        requests.add(request2);

        /**
         * 1.1 Miss 1.2 Hit 2.1 Miss 2.2 Hit
         */
        checkMissHit(requests, 1);

        logSleeping(180000);

        /**
         * Cache expires 1.3 Miss 2.3 Miss
         */
        checkMiss(request1, 5);
        checkMiss(request2, 6);

    }

    public void test_002_RfcToPoll() throws Exception {
        radioKeep.start();
        final String RESOURCE_URI = "asimov_it_cv_rfc_to_poll";
        String uri = createTestResourceUri(RESOURCE_URI);
        final String expires1 = DateUtil.format(new Date(new Date().getTime() + 50000));

        String rawHeadersDef = "ETag: " + ETAG_DEFAULT + CRLF
                + "Expires: " + expires1 + CRLF
                + "Content-Encoding: identity" + CRLF
                + "Cache-Control: max-age=" + 20 + CRLF
                + "Accept-Ranges: bytes" + CRLF
                + "Content-Length: 4";

        String encodedRawHeadersDef = base64Encode(rawHeadersDef);

        HttpRequest request1 = buildDefaultRequest(uri).getRequest();
        PrepareResourceUtil.prepareResource200(uri, false, encodedRawHeadersDef);

        try {
            // R1.1 miss, cached by RFC
            HttpResponse response = checkMiss(request1, 1, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            // R1.2 miss, revalidate
            response = checkMiss(request1, 2, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            // R1.3 miss, revalidate, start RMP
            response = checkMiss(request1, 3, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            // R1.4 hit
            response = checkHit(request1, 4, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            // R1.5 hit
            response = checkHit(request1, 5, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            // R1.6 hit
            response = checkHit(request1, 6, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            // R1.7 hit
            checkHit(request1, 7, VALID_RESPONSE);

        } finally {
            // invalidate resource to stop server polling
            PrepareResourceUtil.invalidateResourceSafely(uri);
            radioKeep.interrupt();
        }
    }

    public void test_003_RfcToPollBreakZ7TP() throws Exception {

        final String RESOURCE_URI = "asimov_it_cv_rfc_break_start_poll";
        String uri = createTestResourceUri(RESOURCE_URI);

        final String expires = DateUtil.format(new Date(new Date().getTime() + 100000));

        String rawHeadersDef = "ETag: " + ETAG_DEFAULT + CRLF
                + "Expires: " + expires + CRLF
                + "Content-Encoding: identity" + CRLF
                + "Cache-Control: max-age=" + MAX_AGE_DEFAULT + CRLF
                + "Accept-Ranges: bytes" + CRLF
                + "Content-Length: 4";

        String encodedRawHeadersDef = base64Encode(rawHeadersDef);

        HttpRequest request1 = buildDefaultRequest(uri).getRequest();
        Runtime.getRuntime().exec(LOCK_Z7TP).waitFor();
        PrepareResourceUtil.prepareResource200(uri, false, encodedRawHeadersDef);

        try {
            // R1.1 miss, cached by RFC
            HttpResponse response = checkMiss(request1, 1, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            //  R1.2 hit
            response = checkHit(request1, 2, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            //  R1.3 hit
            response = checkHit(request1, 3, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            // R1.4 hit
            response = checkHit(request1, 4, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            // R1.5 hit, but send revalidation request
            response = checkMiss(request1, 5, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            // R1.6 hit by revalidation subscription
            response = checkHit(request1, 6, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            // R1.7 hit
            checkHit(request1, 7, VALID_RESPONSE);

        } finally {
            // Stop server polling
            Runtime.getRuntime().exec(UNLOCK_Z7TP).waitFor();
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    public void test_004_RfcToPollZ7TPAvailable() throws Exception {
        radioKeep.start();
        final String RESOURCE_URI = "asimov_it_cv_rfc_start_poll_available";
        String uri = createTestResourceUri(RESOURCE_URI);

        final String expires = DateUtil.format(new Date(new Date().getTime() + 100000));

        String rawHeadersDef = "ETag: " + ETAG_DEFAULT + CRLF
                + "Expires: " + expires + CRLF
                + "Content-Encoding: identity" + CRLF
                + "Cache-Control: max-age=" + MAX_AGE_DEFAULT + CRLF
                + "Accept-Ranges: bytes" + CRLF
                + "Content-Length: 4";

        String encodedRawHeadersDef = base64Encode(rawHeadersDef);

        HttpRequest request1 = buildDefaultRequest(uri).getRequest();
        Runtime.getRuntime().exec(LOCK_Z7TP).waitFor();
        PrepareResourceUtil.prepareResource200(uri, false, encodedRawHeadersDef);

        try {
            // R1.1 miss, cached by RFC
            HttpResponse response = checkMiss(request1, 1, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            //  R1.2 hit
            response = checkHit(request1, 2, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            //  R1.3 hit
            response = checkHit(request1, 3, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            // R1.4 hit
            response = checkHit(request1, 4, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            // R1.5 hit, but send revalidation request
            response = checkMiss(request1, 5, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            // R1.6 hit by rfc
            response = checkHit(request1, 6, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            // R1.7 hit
            response = checkHit(request1, 7, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            // R1.8 hit
            response = checkHit(request1, 8, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            Runtime.getRuntime().exec(UNLOCK_Z7TP).waitFor();
            // R1.9 hit, but revalidate
            response = checkMiss(request1, 9, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            // R1.10 hit
            checkHit(request1, 10, VALID_RESPONSE);

        } finally {
            // Stop server polling
            Runtime.getRuntime().exec(UNLOCK_Z7TP).waitFor();
            PrepareResourceUtil.invalidateResourceSafely(uri);
            radioKeep.interrupt();
        }
    }
}
