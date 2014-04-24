package com.seven.asimov.it.tests.caching.rfc.revalidation;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.testcases.CachingRevalidationTestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.date.DateUtil;

import java.util.Date;

import static com.seven.asimov.it.base.constants.TFConstantsIF.CRLF;

/**
 * IGNORED due to OC version 2.4, where revalidation is not enabled. Should be un-ignored and checked in release 3.0
 * Logic of the test wasn't checked from release 2.3 because revalidation had switched off.
 */

public class RevalidationProductionTests extends CachingRevalidationTestCase {

    public void test_001_RevalidationByEtag() throws Exception {
        final String RESOURCE_URI = "asimov_it_cv_revalidation_etag_simple";
        String uri = createTestResourceUri(RESOURCE_URI);

        final String expires = DateUtil.format(new Date(new Date().getTime() + 100000));
        final String expires2 = DateUtil.format(new Date(new Date().getTime() + 200000));

        String rawHeadersDef = "ETag: " + ETAG_DEFAULT + CRLF
                + "Expires: " + expires + CRLF
                + "Content-Encoding: identity" + CRLF
                + "Cache-Control: max-age=" + MAX_AGE_DEFAULT + CRLF
                + "Accept-Ranges: bytes" + CRLF
                + "Content-Length: 4";

        String rawHeadersReval = "ETag: " + ETAG_DEFAULT + CRLF
                + "Expires: " + expires2 + CRLF
                + "Cache-Control: max-age=" + MAX_AGE_DEFAULT;

        String encodedRawHeadersDef = base64Encode(rawHeadersDef);
        String encodedRawHeadersReval = base64Encode(rawHeadersReval);

        HttpRequest request1 = buildDefaultRequest(uri).getRequest();

        PrepareResourceUtil.prepareResource200(uri, false, encodedRawHeadersDef);

        executeRevalidation(uri, null, encodedRawHeadersReval, request1, request1);
    }

    public void test_002_RevalidationByEtagValidationRequest() throws Exception {

        final String RESOURCE_URI = "asimov_it_cv_etag_validation_request";
        String uri = createTestResourceUri(RESOURCE_URI);

        final String expires = DateUtil.format(new Date(new Date().getTime() + 100000));
        final String expires2 = DateUtil.format(new Date(new Date().getTime() + 200000));

        String rawHeadersDef = "ETag: " + ETAG_DEFAULT + CRLF
                + "Expires: " + expires + CRLF
                + "Content-Encoding: identity" + CRLF
                + "Cache-Control: max-age=" + MAX_AGE_DEFAULT + CRLF
                + "Accept-Ranges: bytes" + CRLF
                + "Content-Length: 4";

        String rawHeadersReval = "ETag: " + ETAG_DEFAULT + CRLF
                + "Expires: " + expires2 + CRLF
                + "Cache-Control: max-age=" + MAX_AGE_DEFAULT;

        String encodedRawHeadersDef = base64Encode(rawHeadersDef);
        String encodedRawHeadersReval = base64Encode(rawHeadersReval);

        final HttpRequest request1 = buildDefaultRequest(uri).getRequest();
        final HttpRequest request2 = buildDefaultRequest(uri)
                .addHeaderField("If-None-Match", ETAG_DEFAULT)
                .addHeaderField("Cache-Control", "max-age=0")
                .getRequest();

        PrepareResourceUtil.prepareResource200(uri, false, encodedRawHeadersDef);

        executeRevalidation(uri, null, encodedRawHeadersReval, request1, request2);
    }

    public void test_003_RevalidationByEtagNoServerSupport() throws Exception {
        radioKeep.start();
        final String RESOURCE_URI = "asimov_it_cv_revalidation_no_server_support";
        String uri = createTestResourceUri(RESOURCE_URI);
        final String expires = DateUtil.format(new Date(new Date().getTime() + 100000));

        String rawHeadersDef = "ETag: " + ETAG_DEFAULT + CRLF
                + "Expires: " + expires + CRLF
                + "Content-Encoding: identity" + CRLF
                + "Cache-Control: max-age=" + MAX_AGE_DEFAULT + CRLF
                + "Accept-Ranges: bytes" + CRLF
                + "Content-Length: 4";

        String encodedRawHeadersDef = base64Encode(rawHeadersDef);

        final HttpRequest request1 = buildDefaultRequest(uri).getRequest();
        final HttpRequest request3 = buildDefaultRequest(uri)
                .addHeaderField("If-None-Match", ETAG_DEFAULT)
                .addHeaderField("Cache-Control", "max-age=0")
                .getRequest();
        request1.getHeaderFields();
        PrepareResourceUtil.prepareResource200(uri, false, encodedRawHeadersDef);

        try {
            // R1.1 miss, cached by RFC
            HttpResponse response = checkMiss(request1, 1, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            // R1.2 hit
            response = checkHit(request3, 2, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            // R1.3 hit
            response = checkHit(request3, 3, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            // R1.4 after this request revalidation time expires
            response = checkHit(request3, 4, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            // R1.5 on this request must be HTTP activity
            response = checkMiss(request3, 5, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            // R1.6 hit
            response = checkHit(request3, 6, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            // R1.7 hit
            checkHit(request3, 7, VALID_RESPONSE);

        } finally {
            // invalidate resource to stop server polling
            PrepareResourceUtil.invalidateResourceSafely(uri);
            radioKeep.interrupt();
        }
    }

    public void test_004_RevalidationByEtagWithCacheInvalidate() throws Exception {
        radioKeep.start();
        final String RESOURCE_URI = "asimov_it_cv_revalidation_etag_cache_invalidate";
        String uri = createTestResourceUri(RESOURCE_URI);
        final String expires1 = DateUtil.format(new Date(new Date().getTime() + 100000));
        final String expires2 = DateUtil.format(new Date(new Date().getTime() + 200000));
        final String expires3 = DateUtil.format(new Date(new Date().getTime() + 280000));

        String rawHeadersDef = "ETag: " + ETAG_DEFAULT + CRLF
                + "Expires: " + expires1 + CRLF
                + "Content-Encoding: identity" + CRLF
                + "Cache-Control: max-age=" + MAX_AGE_DEFAULT + CRLF
                + "Accept-Ranges: bytes" + CRLF
                + "Content-Length: 4";

        String rawHeadersReval = "ETag: " + ETAG_DEFAULT + CRLF
                + "Expires: " + expires2 + CRLF
                + "Cache-Control: max-age=" + MAX_AGE_DEFAULT;

        String ETAG_INVALIDATED = "8f56-7gcc423bce4e9";
        String rawHeadersInval = "ETag: " + ETAG_INVALIDATED + CRLF
                + "Expires: " + expires3 + CRLF
                + "Content-Encoding: identity" + CRLF
                + "Cache-Control: max-age=" + MAX_AGE_DEFAULT + CRLF
                + "Accept-Ranges: bytes" + CRLF
                + "Content-Length: 4";

        String encodedRawHeadersDef = base64Encode(rawHeadersDef);
        String encodedRawHeadersReval = base64Encode(rawHeadersReval);
        String encodedRawHeadersInval = base64Encode(rawHeadersInval);

        HttpRequest request1 = buildDefaultRequest(uri).getRequest();
        PrepareResourceUtil.prepareResource200(uri, false, encodedRawHeadersDef);

        try {
            // R1.1 miss, cached by RFC
            HttpResponse response = checkMiss(request1, 1, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            // R1.2 hit
            response = checkHit(request1, 2, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            // R1.3 hit
            response = checkHit(request1, 3, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            // R1.4 after this request revalidation time expires
            response = checkHit(request1, 4, VALID_RESPONSE);
            PrepareResourceUtil.prepareResource304(uri, encodedRawHeadersReval);
            logSleeping(DELAY - response.getDuration());

            // R1.5 on this request must be HTTP activity
            response = checkMiss(request1, 5, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            // R1.6 hit
            response = checkHit(request1, 6, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            // R1.7 hit
            response = checkHit(request1, 7, VALID_RESPONSE);
            PrepareResourceUtil.prepareResource200(uri, true, encodedRawHeadersInval);
            logSleeping(DELAY - response.getDuration() + 120 * 1000);

            // R1.8 miss
            response = checkMiss(request1, 8, INVALIDATED_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            // R1.9 hit
            response = checkHit(request1, 9, INVALIDATED_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            // R1.10 hit
            checkHit(request1, 10, INVALIDATED_RESPONSE);

        } finally {
            // invalidate resource to stop server polling
            PrepareResourceUtil.invalidateResourceSafely(uri);
            radioKeep.interrupt();
        }
    }

    public void test_005_RevalidationByLM() throws Exception {

        final String RESOURCE_URI = "asimov_it_cv_revalidation_testRevalidationByLM";
        String uri = createTestResourceUri(RESOURCE_URI);

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
        PrepareResourceUtil.prepareResource200(uri, false, encoded);

        executeRevalidation(uri, null, encodedNotModified, request1, request2);
    }

    public void test_006_RevalidationByLMAndET() throws Exception {

        final String RESOURCE_URI = "asimov_it_cv_revalidation_testRevalidationByLMAndET";
        String uri = createTestResourceUri(RESOURCE_URI);

        long startTime = System.currentTimeMillis() + 10000;
        String date1 =   DateUtil.format(new Date(startTime));
        String date2 =   DateUtil.format(new Date(startTime + 120000));
        String expires = DateUtil.format(new Date(startTime + 100000));

        String expected = "Date: " + date1 + CRLF
                + "Expires: " + expires + CRLF
                + "Last-Modified: " + LAST_MODIFIED_DEFAULT + CRLF
                + "ETag: " + ETAG_DEFAULT + CRLF
                + "Content-Encoding: identity" + CRLF
                + "Accept-Ranges: bytes" + CRLF
                + "Cache-Control: max-age=" + MAX_AGE_DEFAULT + CRLF
                + "Content-Length: 4";

        String expectedNotModified = "Date: " + date2 + CRLF
                + "Expires: " + expires + CRLF
                + "Last-Modified: " + LAST_MODIFIED_DEFAULT + CRLF
                + "ETag: " + ETAG_DEFAULT + CRLF
                + "Cache-Control: max-age=" + MAX_AGE_DEFAULT;

        String encoded = base64Encode(expected);
        String encodedNotModified = base64Encode(expectedNotModified);

        HttpRequest request1 = buildDefaultRequest(uri).getRequest();
        HttpRequest request2 = buildLMRequest(uri, LAST_MODIFIED_DEFAULT)
                .addHeaderField("If-None-Match", ETAG_DEFAULT)
                .addHeaderField("Cache-Control", "max-age=0")
                .getRequest();

        PrepareResourceUtil.prepareResource200(uri, false, encoded);

        executeRevalidation(uri,null, encodedNotModified, request1, request2);
    }

    public void test_007_RevalidationByLMandMSandMF() throws Exception {

        final String RESOURCE_URI = "asimov_it_cv_revalidation_testRevalidationByLMandMSandMF";
        String uri = createTestResourceUri(RESOURCE_URI);

        String date1 = DateUtil.format(new Date(new Date().getTime()));
        String date2 = DateUtil.format(new Date(new Date().getTime() - 700000));

        String expected = "Date: " + date1 + CRLF
                + "Last-Modified: " + date2 + CRLF
                + "Content-Encoding: identity" + CRLF
                + "Accept-Ranges: bytes" + CRLF
                + "Content-Length: 4";

        String expectedNotModified = "Date: " + date1 + CRLF
                + "Last-Modified: " + date2 + CRLF
                + "Cache-Control: max-age=86400";

        String encoded = base64Encode(expected);
        String encodedNotModified = base64Encode(expectedNotModified);

        HttpRequest request1 = buildDefaultRequest(uri)
                .addHeaderField("Cache-Control", "max-stale=400, min-fresh=100")
                .getRequest();

        HttpRequest request2 = buildLMRequest(uri, date2)
                .addHeaderField("Cache-Control", "max-age=0")
                .getRequest();

        PrepareResourceUtil.prepareResource200(uri, false, encoded);
        executeRevalidation(uri, null, encodedNotModified, request1, request2);
    }

    public void test_008_RevalidationByLMandMS() throws Exception {

        final String RESOURCE_URI = "asimov_it_cv_revalidation_testRevalidationByLMandMS";
        String uri = createTestResourceUri(RESOURCE_URI);

        String date1 = DateUtil.format(new Date(new Date().getTime()));
        String date2 = DateUtil.format(new Date(new Date().getTime() - 700000));

        String expected = "Date: " + date1 + CRLF
                + "Last-Modified: " + date2 + CRLF
                + "Accept-Ranges: bytes" + CRLF
                + "Content-Length: 4";

        String expectedNotModified = "Date: " + date1 + CRLF
                + "Last-Modified: " + date2 + CRLF
                + "Cache-Control: max-age=86400";

        String encoded = base64Encode(expected);
        String encodedNotModified = base64Encode(expectedNotModified);

        HttpRequest request1 = buildDefaultRequest(uri)
                .addHeaderField("Cache-Control", "max-stale=300")
                .getRequest();

        HttpRequest request2 = buildLMRequest(uri, date2)
                .addHeaderField("Cache-Control", "max-age=0")
                .getRequest();

        PrepareResourceUtil.prepareResource200(uri, false, encoded);
        executeRevalidation(uri, null, encodedNotModified, request1, request2);
    }

    public void test_009_RevalidationByLMandMF() throws Exception {

        final String RESOURCE_URI = "asimov_it_cv_revalidation_testRevalidationByLMandMF";
        String uri = createTestResourceUri(RESOURCE_URI);

        String date1 = DateUtil.format(new Date(new Date().getTime()));
        String date2 = DateUtil.format(new Date(new Date().getTime() - 1100000));

        String expected = "Date: " + date1 + CRLF
                + "Last-Modified: " + date2 + CRLF
                + "Accept-Ranges: bytes" + CRLF
                + "Content-Length: 4";

        String expectedNotModified = "Date: " + date1 + CRLF
                + "Last-Modified: " + date2 + CRLF
                + "Cache-Control: max-age=86400";

        String encoded = base64Encode(expected);
        String encodedNotModified = base64Encode(expectedNotModified);

        HttpRequest request1 = buildLMRequest(uri, date2)
                .addHeaderField("Cache-Control", "min-fresh=100")
                .getRequest();

        HttpRequest request2 = buildLMRequest(uri, date2)
                .addHeaderField("Cache-Control", "max-age=0")
                .getRequest();

        PrepareResourceUtil.prepareResource200(uri, false, encoded);
        executeRevalidation(uri, null, encodedNotModified, request1, request2);
    }

    /* Revalidation type 1 - X-OC-ETag. We have no revalidation subscription after 304 - only RMP */
    public void test_010_RevalidationType1() throws Exception {
        final String RESOURCE_URI = "asimov_it_cv_srv_revalidation_type_1";
        final String uri = createTestResourceUri(RESOURCE_URI);
        final String expires = DateUtil.format(new Date(new Date().getTime() + 100000));

        String rawHeadersDef = "Expires: " + expires + CRLF
                + "Content-Encoding: identity" + CRLF
                + "Cache-Control: max-age=" + MAX_AGE_DEFAULT + CRLF
                + "Accept-Ranges: bytes" + CRLF
                + "Content-Length: 4";

        executeServerRevalidation(uri, rawHeadersDef, KEEP_ALIVE, DELAY);
    }

    /* Revalidation type 3 - X-OC-ETag, Last-Modified. Polling class 16 should start after revalidation. */
    public void test_011_RevalidationType3() throws Exception {
        final String RESOURCE_URI = "asimov_it_cv_srv_revalidation_type_3";
        final String uri = createTestResourceUri(RESOURCE_URI);
        final String expires = DateUtil.format(new Date(new Date().getTime() + 100000));

        String rawHeadersDef = "Expires: " + expires + CRLF
                + "Content-Encoding: identity" + CRLF
                + "Cache-Control: max-age=" + MAX_AGE_DEFAULT + CRLF
                + "Accept-Ranges: bytes" + CRLF
                + "Content-Length: 4" + CRLF
                + "Last-Modified: " + LAST_MODIFIED_DEFAULT;

        executeServerRevalidation(uri, rawHeadersDef, KEEP_ALIVE, DELAY);
    }

    /* Revalidation type 5 - X-OC-ETag, ETag. Polling class 16 should start after revalidation. */
    public void test_012_RevalidationType5() throws Exception {
        final String RESOURCE_URI = "asimov_it_cv_srv_revalidation_type_5";
        final String uri = createTestResourceUri(RESOURCE_URI);
        final String expires = DateUtil.format(new Date(new Date().getTime() + 100000));

        String rawHeadersDef = "Expires: " + expires + CRLF
                + "Content-Encoding: identity" + CRLF
                + "Cache-Control: max-age=" + MAX_AGE_DEFAULT + CRLF
                + "Accept-Ranges: bytes" + CRLF
                + "Content-Length: 4" + CRLF
                + "ETag: " + ETAG_DEFAULT;

        executeServerRevalidation(uri, rawHeadersDef, KEEP_ALIVE, DELAY);
    }

    /* Revalidation type 7 - X-OC-ETag, ETag, Last-Modified. Polling class 16 should start after revalidation. */
    public void test_013_RevalidationType7() throws Exception {
        final String RESOURCE_URI = "asimov_it_cv_srv_revalidation_type_7";
        final String uri = createTestResourceUri(RESOURCE_URI);
        final String expires = DateUtil.format(new Date(new Date().getTime() + 100000));

        String rawHeadersDef = "Expires: " + expires + CRLF
                + "Content-Encoding: identity" + CRLF
                + "Cache-Control: max-age=" + MAX_AGE_DEFAULT + CRLF
                + "Accept-Ranges: bytes" + CRLF
                + "Content-Length: 4" + CRLF
                + "ETag: " + ETAG_DEFAULT + CRLF
                + "Last-Modified: " + LAST_MODIFIED_DEFAULT;

        executeServerRevalidation(uri, rawHeadersDef, KEEP_ALIVE, DELAY);
    }
}
