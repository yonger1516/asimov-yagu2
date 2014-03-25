package com.seven.asimov.it.tests.caching.rfc.revalidation;

import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.base.Base64;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.testcases.CachingRevalidationTestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.date.DateUtil;

import java.net.URLEncoder;
import java.util.Date;

import static com.seven.asimov.it.base.constants.TFConstantsIF.CRLF;

/**
 * IGNORED due to OC version 2.4, where revalidation is not enabled. Should be un-ignored and checked in release 3.0
 * Logic of the test wasn't checked from release 2.3 because revalidation had switched off.
 */
@Ignore
public class HttpsRfcProdutionTests extends CachingRevalidationTestCase {

    public void test_001_RevalidationExpiresHttps() throws Throwable {
        long now = System.currentTimeMillis();

        final String path = "asimov_it_https_rfc_testCheckExpiresOfExpires";
        String uri = createTestResourceUri(path, true);

        String expectedBody = "11111111111111111111111111111111111111111111111111";
        String expected = "HTTP/1.1 200 OK" + CRLF + "Connection: close" + CRLF + "Date: " + DateUtil.format(new Date(now)) + CRLF + "Expires: " + DateUtil.format(new Date(now + 180000)) + CRLF
                + "Accept-Ranges: bytes" + CRLF + "Content-Length: 50" + CRLF + CRLF + expectedBody;

        String expectedEncoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));

        HttpRequest request = createRequest().setUri(uri).setMethod("GET").addHeaderField("X-OC-Raw", expectedEncoded).addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("User-Agent", "Android/TWC")
                .addHeaderField("Accept", "text/html, application/xml;q=0.9, application/xhtml+xml, image/png, image/webp, image/jpeg, image/gif, image/x-xbitmap, */*;q=0.1")
                .addHeaderField("Accept-Language", "ru-RU,ru;q=0.9,en;q=0.8").getRequest();

        PrepareResourceUtil.prepareResource(uri, false);
        try {
            checkMiss(request, 1);
            checkHit(request, 2);
            logSleeping(180000);
            checkMiss(request, 3);
        } finally {
            PrepareResourceUtil.prepareResource(uri, true);
        }
    }

    public void test_002_RevalidationCacheControlHttps() throws Throwable {
        long now = System.currentTimeMillis();

        final String path = "asimov_it_https_rfc_testCheckExpiresOfCacheControl";
        String uri = createTestResourceUri(path, true);

        String expectedBody = "22222222222222222222222222222222222222222222222222";
        String expected = "HTTP/1.1 200 OK" + CRLF + "Connection: close" + CRLF + "Date: " + DateUtil.format(new Date(now)) + CRLF + "Accept-Ranges: bytes" + CRLF + "Cache-Control: max-age=180"
                + CRLF + "Content-Length: 50" + CRLF + CRLF + expectedBody;

        String expectedEncoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));

        HttpRequest request = createRequest().setUri(uri).setMethod("GET").addHeaderField("X-OC-Raw", expectedEncoded).addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("User-Agent", "Android/TWC")
                .addHeaderField("Accept", "text/html, application/xml;q=0.9, application/xhtml+xml, image/png, image/webp, image/jpeg, image/gif, image/x-xbitmap, */*;q=0.1")
                .addHeaderField("Accept-Language", "ru-RU,ru;q=0.9,en;q=0.8").getRequest();

        PrepareResourceUtil.prepareResource(uri, false);
        try {
            checkMiss(request, 1);
            checkHit(request, 2);
            logSleeping(180000);
            checkMiss(request, 3);
        } finally {
            PrepareResourceUtil.prepareResource(uri, true);
        }
    }

    public void test_003_RevalidationLMHttps() throws Throwable {
        final String RESOURCE_URI = "asimov_it_cv_https_rfc_testRevalidationByLM";
        String uri = createTestResourceUri(RESOURCE_URI, true);

        long startTime = System.currentTimeMillis() + 10000;
        String date1 =   DateUtil.format(new Date(startTime));
        String date2 =   DateUtil.format(new Date(startTime + 120000));
        String expires = DateUtil.format(new Date(startTime + 100000));

        String expected = "Date: " + date1 + CRLF
                + "Expires: " + expires + CRLF
                + "Last-Modified: " + LAST_MODIFIED_DEFAULT + CRLF
                + "Content-Encoding: identity" + CRLF
                + "Accept-Ranges: bytes" + CRLF
                + "Cache-Control: max-age=" + MAX_AGE_DEFAULT + CRLF
                + "Content-Length: 4";

        String expectedNotModified = "Date: " + date2 + CRLF
                + "Expires: " + expires + CRLF
                + "Last-Modified: " + LAST_MODIFIED_DEFAULT + CRLF
                + "Cache-Control: max-age=" + MAX_AGE_DEFAULT;

        String encoded = base64Encode(expected);
        String encodedNotModified = base64Encode(expectedNotModified);

        HttpRequest request1 = buildDefaultRequest(uri).getRequest();
        HttpRequest request2 = buildLMRequest(uri, LAST_MODIFIED_DEFAULT)
                .addHeaderField("Cache-Control", "max-age=0")
                .getRequest();
        executeRevalidation(uri, encoded, encodedNotModified, request1, request2);
    }

    public void test_004_RevalidationEtagHttps() throws Throwable {

        final String RESOURCE_URI = "asimov_it_cv_https_rfc_testRevalidationByEtag";
        String uri = createTestResourceUri(RESOURCE_URI, true);

        final String expires =  DateUtil.format(new Date(new Date().getTime() + 100000));
        final String expires2 = DateUtil.format(new Date(new Date().getTime() + 200000));

        String rawHeadersDef = "ETag: " + ETAG_DEFAULT + CRLF + "Expires: " + expires + CRLF + "Content-Encoding: identity" + CRLF + "Cache-Control: max-age=" + MAX_AGE_DEFAULT + CRLF
                + "Accept-Ranges: bytes" + CRLF + "Content-Length: 4";

        String rawHeadersReval = "ETag: " + ETAG_DEFAULT + CRLF + "Expires: " + expires2 + CRLF + "Cache-Control: max-age=" + MAX_AGE_DEFAULT;

        String encodedRawHeadersDef = base64Encode(rawHeadersDef);
        String encodedRawHeadersReval = base64Encode(rawHeadersReval);

        HttpRequest request = buildDefaultRequest(uri).getRequest();

        PrepareResourceUtil.prepareResource200(uri, false, encodedRawHeadersDef);

        executeRevalidationThreadEtagHttps(uri, encodedRawHeadersDef, encodedRawHeadersReval, request);
    }

}
