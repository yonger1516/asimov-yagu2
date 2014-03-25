package com.seven.asimov.it.tests.caching.normalization;


import android.test.suitebuilder.annotation.LargeTest;
import com.seven.asimov.it.base.Base64;
import com.seven.asimov.it.base.HttpHeaderField;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.testcases.NormalizationTestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.date.DateUtil;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import com.seven.asimov.it.utils.sms.SmsUtil;
import junit.framework.AssertionFailedError;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.*;

import static com.seven.asimov.it.base.constants.TFConstantsIF.CRLF;


/**
 * Check uri, body, cookie, header normalization  ({@link NormalizationTests#testBodyNormalizationParam() Test 1},
 * {@link NormalizationTests#testCookieRuleAbsence() Test 2},
 * {@link NormalizationTests#testCookieRuleNormalization() Test 3},
 * {@link NormalizationTests#testNormalizationByBody2_NORMALIZATION_012() Test 4},
 * {@link NormalizationTests#testNormalizationByBody_NORMALIZATION_006() Test 5},
 * {@link NormalizationTests#testNormalizationByCookieOneParam_NORMALIZATION_007() Test 6},
 * {@link NormalizationTests#testNormalizationByCookieSimple_NORMALIZATION_010() Test 7},
 * {@link NormalizationTests#testNormalizationByCookieTwoParams2_NORMALIZATION_009() Test 8},
 * {@link NormalizationTests#testNormalizationByCookieTwoParams_NORMALIZATION_008() Test 9},
 * {@link NormalizationTests#testNormalizationByHeaders2_NORMALIZATION_004() Test 10},
 * {@link NormalizationTests#testNormalizationByHeaders_NORMALIZATION_003() Test 11},
 * {@link NormalizationTests#testNormalizationByUriPlusHeaders_NORMALIZATION_005() Test 12},
 * {@link NormalizationTests#testNormalizationByUriRfc2_NORMALIZATION_002() Test 13},
 * {@link NormalizationTests#testNormalizationByUriRfc_NORMALIZATION_001() Test 14},
 * {@link NormalizationTests#testYahooImProperty() Test 15},
 * {@link NormalizationTests#testYahooImPropertyWithRn() Test 16},
 * {@link NormalizationTests#testYahooIntProperty() Test 17},
 * {@link NormalizationTests#testNormalizationFB_NORMALIZATION_011() Test 18},
 */
public class NormalizationTests extends NormalizationTestCase {
    private static final Logger logger = LoggerFactory.getLogger(NormalizationTests.class.getSimpleName());

    private final long NOW = System.currentTimeMillis();
    private static final UUID guid = UUID.randomUUID();
    private static final String expectedBody = "for (;;); {\"t\":\"continue\",\"seq\":1}";
    private static final String TAG = NormalizationTests.class.getSimpleName();
    private static final String PATH_COOKIE = "@asimov@normalization@cookie@com.seven.asimov.it@%s";
    private static final String PATH_BODY = "@asimov@normalization@body@com.seven.asimov.it@%s";
    private static final String PATH_HEADER = "@asimov@normalization@header@com.seven.asimov.it@%s";
    private static final String PATH_URI = "@asimov@normalization@uri@com.seven.asimov.it@%s";

    private static final String RESPONSE_BODY_RULE = "response_body_rule";
    private static final String RESPONSE_HEADER_RULE = "response_header_rules";
    private static final String REQUEST_HEADER_RULE = "request_header_rules";
    private static final String URI_RULE = "patterns";

    private static String[] resourceUri = {
            "http://%s:80/asimov-it_cv_normalization_001" + guid.toString(),
            "HtTp://%s/asimov-it_cv_normalization_001" + guid.toString(),
            "http://%s/.//../../asimov-it_cv_normalization_001" + guid.toString(),
            "HtTp://%s:80/./.././asimov-it_cv_normalization_001" + guid.toString(),
            "http://%s/asimov-it_cv_normalization_001" + guid.toString(),
            "http://%s:80/asimov-it_cv_" + guid.toString() + "_normalization_002/a/c/%%7Bfoo%%7D/path?a=1&b=2&c=3",//5
            "http://%s/asimov-it_cv_" + guid.toString() + "_normalization_002/a/c/%%7Bfoo%%7D/path?c=3&b=2&a=1",//6
            "HTTP://%s:80/asimov-it_cv_" + guid.toString() + "_normalization_002/a/./b/../c/%%7bfoo%%7d/path?a=1&b=2&c=3",//7
            "http://%s/asimov-it_cv_" + guid.toString() + "_normalization_002/a/./b/../c/%%7bfoo%%7d/path?b=2&a=1&c=3",//8
            "HTTP://%s:80/asimov-it_cv_" + guid.toString() + "_normalization_002/a/c/%%7Bfoo%%7D/path?b=2&a=1&c=3", //9
            "http://%s:80/asimov-it_cv_normalization_003" + guid.toString(),
            "http://%s:80/asimov-it_cv_normalization_004" + guid.toString(),
            "http://%s:80/asimov-it_cv_normalization_009" + guid.toString(),     //12
            "HtTp://%s/asimov-it_cv_normalization_009" + guid.toString(),
            "http://%s/.//../../asimov-it_cv_normalization_009" + guid.toString(),
            "HtTp://%s:80/./.././asimov-it_cv_normalization_009" + guid.toString()};

    private final long DAY = 24 * 60 * 60 * 1000;

    @Override
    protected void runTest() throws Throwable {
        boolean isPassed;
        int numberOfAttempts = 0;
        List<String> counts = new ArrayList<String>();
        do {
            isPassed = true;
            numberOfAttempts++;
            try {

                super.runTest();

            } catch (AssertionFailedError assertionFailedError) {
                logger.error("Test failed due to " + ExceptionUtils.getStackTrace(assertionFailedError));
                isPassed = false;
                counts.add("Test failed due to AssertionFailedError in " + numberOfAttempts + " attempt");
                Thread.sleep(2 * 60 * 1000);
            }
        } while (!isPassed && numberOfAttempts < 3);

        assertTrue("The test was failed three times ", counts.size() != 3);
    }

    /**
     * <h1>Testing that uri normalization applies correctly.</h1>
     * <p>There are some examples of safe uri normalization:</p>
     * <ol>
     * <li>Convert scheme and host to lower-case</li>
     * <li>Remove default port</li>
     * <li>Remove dot-segments</li>
     * </ol>
     * <p>The test checks that 4 response will be HIT.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>HIT, get response immediately, sleep on 5 seconds</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testNormalizationByUriRfc_NORMALIZATION_001() throws Throwable {
        logger.info("Started");
        PrepareResourceUtil.prepareResource(String.format(resourceUri[0], TEST_RESOURCE_HOST), false);
        try {
            //1.1 from network
            checkMiss(createRequest().setUri(String.format(resourceUri[0], TEST_RESOURCE_HOST)).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").getRequest(), 1, (int) MIN_RMP_PERIOD);
            //1.2 from network
            checkMiss(createRequest().setUri(String.format(resourceUri[1], TEST_RESOURCE_HOST)).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").getRequest(), 2, (int) MIN_RMP_PERIOD);
            //1.3 from network
            checkMiss(createRequest().setUri(String.format(resourceUri[2], TEST_RESOURCE_HOST)).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").getRequest(), 3, (int) MIN_RMP_PERIOD);
            //1.4 from cache
            checkHit(createRequest().setUri(String.format(resourceUri[3], TEST_RESOURCE_HOST)).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").getRequest(), 4, (int) MIN_RMP_PERIOD);
            //1.5 from cache
            checkHit(createRequest().setUri(String.format(resourceUri[4], TEST_RESOURCE_HOST)).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").getRequest(), 5);
            logger.info("Finish");
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(String.format(resourceUri[0], TEST_RESOURCE_HOST));
        }
    }

    /**
     * <h1>Testing that uri normalization applies correctly.</h1>
     * <p>There are some examples of safe uri normalization:</p>
     * <ol>
     * <li>Convert scheme and host to lower-case</li>
     * <li>Remove default port</li>
     * <li>Remove dot-segments</li>
     * </ol>
     * <p>The test checks that 4 response will be HIT.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>HIT, get response immediately, sleep on 5 seconds</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testNormalizationByUriRfc2_NORMALIZATION_002() throws Throwable {
        logger.info("Started");
        PrepareResourceUtil.prepareResource(String.format(resourceUri[5], TEST_RESOURCE_HOST), false);
        try {
            //1.1 from network
            checkMiss(createRequest().setUri(String.format(resourceUri[5], TEST_RESOURCE_HOST)).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").getRequest(), 1, (int) MIN_RMP_PERIOD);
            //1.2 from network
            checkMiss(createRequest().setUri(String.format(resourceUri[6], TEST_RESOURCE_HOST)).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").getRequest(), 2, (int) MIN_RMP_PERIOD);
            //1.3 from network
            checkMiss(createRequest().setUri(String.format(resourceUri[7], TEST_RESOURCE_HOST)).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").getRequest(), 3, (int) MIN_RMP_PERIOD);
            //1.4 from cache
            checkHit(createRequest().setUri(String.format(resourceUri[8], TEST_RESOURCE_HOST)).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").getRequest(), 4, (int) MIN_RMP_PERIOD);
            //1.5 from cache
            checkHit(createRequest().setUri(String.format(resourceUri[9], TEST_RESOURCE_HOST)).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").getRequest(), 5);
            logger.info("Finish");
        } finally {
            PrepareResourceUtil.prepareResource(String.format(resourceUri[5], TEST_RESOURCE_HOST), true);
        }
    }

    /**
     * <h1>Testing that header normalization applies correctly.</h1>
     * <p>Test requires appropriate policy for X-OC-ContentEncoding and X-OC-Raw headers</p>
     * <p>The test checks that 4 response will be HIT.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>HIT, get response immediately, sleep on 5 seconds</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testNormalizationByHeaders_NORMALIZATION_003() throws Throwable {
        logger.info("Started");

        final Policy requestHeaderRule = new Policy(REQUEST_HEADER_RULE, "X-OC-Raw:.*", String.format(PATH_HEADER, TEST_RESOURCE_HOST) + "@.*", true);
        final Policy responseBodyRule = new Policy(RESPONSE_BODY_RULE, ".*'\\r\\n", String.format(PATH_BODY, TEST_RESOURCE_HOST) + "@.*", true);

        PMSUtil.addPolicies(new Policy[]{requestHeaderRule, responseBodyRule});
        logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);

        PrepareResourceUtil.prepareResource(String.format(resourceUri[10], TEST_RESOURCE_HOST), false);
        try {
            final String expected1 = "HTTP/1.0 200 OK" + CRLF + "Date: " + DateUtil.format(new Date(NOW)) + CRLF + "Age: 3"
                    + TFConstantsIF.CRLF + "Content-Type: application/x-javascript" + CRLF + "Pragma: no-cache" + CRLF
                    + "Accept-Ranges: bytes" + CRLF + "Content-Length: 4" + CRLF + CRLF + "tere";
            final String expectedEncoded1 = URLEncoder.encode(Base64.encodeToString(expected1.getBytes(), Base64.DEFAULT));

            // 1.1 from network
            checkMiss(createRequest().setUri(String.format(resourceUri[10], TEST_RESOURCE_HOST)).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("X-OC-Raw", expectedEncoded1)
                    .getRequest(), 1, (int) MIN_RMP_PERIOD);

            final String expected2 = "HTTP/1.0 200 OK" + CRLF + "Date: " + DateUtil.format(new Date(NOW)) + CRLF
                    + "Content-Type: application/x-javascript" + CRLF + "Pragma: no-cache" + CRLF
                    + "Accept-Ranges: bytes" + CRLF + "Content-Length: 4" + CRLF + CRLF + "tere";
            final String expectedEncoded2 = URLEncoder.encode(Base64.encodeToString(expected2.getBytes(), Base64.DEFAULT));

            // 1.2 from network
            checkMiss(createRequest().setUri(String.format(resourceUri[10], TEST_RESOURCE_HOST)).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("X-OC-Raw", expectedEncoded2)
                    .getRequest(), 2, (int) MIN_RMP_PERIOD);

            final String expected3 = "HTTP/1.0 200 OK" + CRLF + "Age: 20" + CRLF + "Content-Type: application/x-javascript" + CRLF
                    + "Pragma: no-cache" + CRLF + "Accept-Ranges: bytes" + CRLF + "Content-Length: 4" + CRLF + CRLF + "tere";
            final String expectedEncoded3 = URLEncoder.encode(Base64.encodeToString(expected3.getBytes(), Base64.DEFAULT));

            // 1.3 from network
            checkMiss(createRequest().setUri(String.format(resourceUri[10], TEST_RESOURCE_HOST)).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("X-OC-Raw", expectedEncoded3)
                    .getRequest(), 3, (int) MIN_RMP_PERIOD);

            final String expected4 = "HTTP/1.0 200 OK" + CRLF + "Content-Type: application/x-javascript" + CRLF + "Pragma: no-cache"
                    + CRLF + "Accept-Ranges: bytes" + CRLF + "Content-Length: 4" + CRLF + CRLF + "tere";
            final String expectedEncoded4 = URLEncoder.encode(Base64.encodeToString(expected4.getBytes(), Base64.DEFAULT));

            // 1.4 from cache
            checkHit(createRequest().setUri(String.format(resourceUri[10], TEST_RESOURCE_HOST)).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("X-OC-Raw", expectedEncoded4)
                    .getRequest(), 4);

            logger.info("Finish");
        } finally {
            PrepareResourceUtil.prepareResource(String.format(resourceUri[10], TEST_RESOURCE_HOST), true);
        }
    }

    /**
     * <h1>Testing that header and URI normalization applies correctly.</h1>
     * <p>Test requires appropriate policy for X-OC-ContentEncoding and X-OC-Raw headers</p>
     * <p>Also there are some examples of safe uri normalization:</p>
     * <ol>
     * <li>Convert scheme and host to lower-case</li>
     * <li>Remove default port</li>
     * <li>Remove dot-segments</li>
     * </ol>
     * <p>The test checks that 4 response will be HIT.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>HIT, get response immediately, sleep on 5 seconds</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testNormalizationByHeaders2_NORMALIZATION_004() throws Throwable {
        logger.info("Started");

        final Policy requestHeaderRule = new Policy(REQUEST_HEADER_RULE, "X-OC-Raw:.*", String.format(PATH_HEADER, TEST_RESOURCE_HOST) + "@.*", true);

        PMSUtil.addPolicies(new Policy[]{requestHeaderRule});
        logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);

        PrepareResourceUtil.prepareResource(String.format(resourceUri[11], TEST_RESOURCE_HOST), false);
        try {
            final String etag1 = "4087675836";

            final String expected1 = "HTTP/1.0 200 OK" + CRLF + "Content-Type: application/x-javascript" + CRLF + "Etag: "
                    + etag1 + CRLF + "Last-Modified: " + DateUtil.format(new Date(NOW - 3 * DAY)) + CRLF + "Age: 3"
                    + CRLF + "Pragma: no-cache" + CRLF + "Accept-Ranges: bytes" + CRLF + "Content-Length: 4" + CRLF + CRLF + "tere";
            final String expectedEncoded1 = URLEncoder.encode(Base64.encodeToString(expected1.getBytes(), Base64.DEFAULT));

            // 1.1 from network
            checkMiss(createRequest().setUri(String.format(resourceUri[11], TEST_RESOURCE_HOST)).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("X-OC-Raw", expectedEncoded1)
                    .getRequest(), 1, (int) MIN_RMP_PERIOD);

            final String etag2 = "409936736";

            final String expected2 = "HTTP/1.0 200 OK" + CRLF + "Content-Type: application/x-javascript" + CRLF + "Etag: " + etag2
                    + CRLF + "Last-Modified: " + DateUtil.format(new Date(NOW - 2 * DAY)) + CRLF + "Pragma: no-cache"
                    + CRLF + "Accept-Ranges: bytes" + CRLF + "Content-Length: 4" + CRLF + CRLF + "tere";
            final String expectedEncoded2 = URLEncoder.encode(Base64.encodeToString(expected2.getBytes(), Base64.DEFAULT));

            // 1.2 from network
            checkMiss(createRequest().setUri(String.format(resourceUri[11], TEST_RESOURCE_HOST)).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("X-OC-Raw", expectedEncoded2)
                    .getRequest(), 2, (int) MIN_RMP_PERIOD);

            final String etag3 = "405545836";

            final String expected3 = "HTTP/1.0 200 OK" + CRLF + "Content-Type: application/x-javascript" + CRLF + "Etag: " + etag3
                    + CRLF + "Last-Modified: " + DateUtil.format(new Date(NOW - DAY)) + CRLF + "Age: 20" + CRLF
                    + "Pragma: no-cache" + CRLF + "Accept-Ranges: bytes" + CRLF + "Content-Length: 4" + CRLF + CRLF + "tere";
            final String expectedEncoded3 = URLEncoder.encode(Base64.encodeToString(expected3.getBytes(), Base64.DEFAULT));

            // 1.3 from network
            checkMiss(createRequest().setUri(String.format(resourceUri[11], TEST_RESOURCE_HOST)).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("X-OC-Raw", expectedEncoded3)
                    .getRequest(), 3, (int) MIN_RMP_PERIOD);

            final String expected4 = "HTTP/1.0 200 OK" + CRLF + "Content-Type: application/x-javascript" + CRLF + "Pragma: no-cache"
                    + CRLF + "Accept-Ranges: bytes" + CRLF + "Content-Length: 4" + CRLF + CRLF + "tere";
            final String expectedEncoded4 = URLEncoder.encode(Base64.encodeToString(expected4.getBytes(), Base64.DEFAULT));

            // 1.4 from cache
            checkHit(createRequest().setUri(String.format(resourceUri[11], TEST_RESOURCE_HOST)).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("X-OC-Raw", expectedEncoded4)
                    .getRequest(), 4);

            logger.info("Finish");
        } finally {
            PrepareResourceUtil.prepareResource(String.format(resourceUri[11], TEST_RESOURCE_HOST), true);
        }
    }

    /**
     * <h1>Testing that header and URI normalization applies correctly.</h1>
     * <p>Test requires appropriate policy for X-OC-ContentEncoding and X-OC-Raw headers</p>
     * <p>Also there are some examples of safe uri normalization:</p>
     * <ol>
     * <li>Convert scheme and host to lower-case</li>
     * <li>Remove default port</li>
     * <li>Remove dot-segments</li>
     * </ol>
     * <p>The test checks that 4 response will be HIT.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>HIT, get response immediately, sleep on 5 seconds</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testNormalizationByUriPlusHeaders_NORMALIZATION_005() throws Throwable {
        logger.info("Started");

        final Policy requestHeaderRule = new Policy(REQUEST_HEADER_RULE, "X-OC-Raw:.*", String.format(PATH_HEADER, TEST_RESOURCE_HOST) + "@.*", true);
        final Policy responseBodyRule = new Policy(RESPONSE_BODY_RULE, ".*\\r\\n", String.format(PATH_BODY, TEST_RESOURCE_HOST) + "@.*", true);

        PMSUtil.addPolicies(new Policy[]{requestHeaderRule, responseBodyRule});
        logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);

        PrepareResourceUtil.prepareResource(String.format(resourceUri[12], TEST_RESOURCE_HOST), false);
        try {
            final String etag1 = "4087675836";

            final String expected1 = "HTTP/1.0 200 OK" + CRLF + "Date: " + DateUtil.format(new Date(NOW)) + CRLF
                    + "Expires: " + DateUtil.format(new Date(NOW + 3 * DAY)) + CRLF + "Etag: " + etag1 + CRLF
                    + "Content-Type: application/x-javascript" + CRLF + "Age: 3" + CRLF + "Pragma: no-cache" + CRLF
                    + "Accept-Ranges: bytes" + CRLF + "Content-Length: 4" + CRLF + CRLF + "tere";
            final String expectedEncoded1 = URLEncoder.encode(Base64.encodeToString(expected1.getBytes(), Base64.DEFAULT));

            // 1.1 from network
            checkMiss(createRequest().setUri(String.format(resourceUri[12], TEST_RESOURCE_HOST)).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("X-OC-Raw", expectedEncoded1)
                    .getRequest(), 1, (int) MIN_RMP_PERIOD);

            final String etag2 = "409936736";

            final String expected2 = "HTTP/1.0 200 OK" + CRLF + "Etag: " + etag2 + CRLF + "Content-Type: application/x-javascript"
                    + CRLF + "Expires: " + DateUtil.format(new Date(NOW + 2 * DAY)) + CRLF + "Pragma: no-cache" + CRLF
                    + "Accept-Ranges: bytes" + CRLF + "Content-Length: 4" + CRLF + CRLF + "tere";
            final String expectedEncoded2 = URLEncoder.encode(Base64.encodeToString(expected2.getBytes(), Base64.DEFAULT));

            // 1.2 from network
            checkMiss(createRequest().setUri(String.format(resourceUri[13], TEST_RESOURCE_HOST)).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("X-OC-Raw", expectedEncoded2)
                    .getRequest(), 2, (int) MIN_RMP_PERIOD);

            final String etag3 = "405545836";

            final String expected3 = "HTTP/1.0 200 OK" + CRLF + "Content-Type: application/x-javascript" + CRLF + "Etag: " + etag3
                    + CRLF + "Expires: " + DateUtil.format(new Date(NOW + 3 * DAY)) + CRLF + "Pragma: no-cache" + CRLF
                    + "Age: 20" + CRLF + "Accept-Ranges: bytes" + CRLF + "Content-Length: 4" + CRLF + CRLF + "tere";
            final String expectedEncoded3 = URLEncoder.encode(Base64.encodeToString(expected3.getBytes(), Base64.DEFAULT));

            // 1.3 from network
            checkMiss(createRequest().setUri(String.format(resourceUri[14], TEST_RESOURCE_HOST)).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("X-OC-Raw", expectedEncoded3)
                    .getRequest(), 3, (int) MIN_RMP_PERIOD);

            final String expected4 = "HTTP/1.0 200 OK" + CRLF + "Content-Type: application/x-javascript" + CRLF + "Pragma: no-cache"
                    + CRLF + "Accept-Ranges: bytes" + CRLF + "Content-Length: 4" + CRLF + CRLF + "tere";
            final String expectedEncoded4 = URLEncoder.encode(Base64.encodeToString(expected4.getBytes(), Base64.DEFAULT));

            // 1.4 from cache
            checkHit(createRequest().setUri(String.format(resourceUri[15], TEST_RESOURCE_HOST)).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("X-OC-Raw", expectedEncoded4)
                    .getRequest(), 4);

            logger.info("Finish");
        } finally {
            PrepareResourceUtil.prepareResource(String.format(resourceUri[12], TEST_RESOURCE_HOST), true);
        }
    }

    /**
     * <h1>Testing that body normalization applies correctly.</h1>
     * <p>Test requires specific policies for body normalization.</p>
     * <p>The test checks that 4 response will be HIT.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>HIT, get response immediately, sleep on 5 seconds</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testNormalizationByBody_NORMALIZATION_006() throws Throwable {
        logger.info("Started");

        PMSUtil.cleanPaths(new String[]{String.format(PATH_COOKIE, TEST_RESOURCE_HOST) + "@.*"});

        final Policy requestHeaderRule = new Policy(REQUEST_HEADER_RULE, "X-OC-Raw:.*", String.format(PATH_HEADER, TEST_RESOURCE_HOST) + "@.*", true);
//        final Policy responseHeaderRule = new Policy(RESPONSE_HEADER_RULE, "Content-Length:.*", String.format(PATH_HEADER, TEST_RESOURCE_HOST) + "@.*", true);
        final Policy responseBodyRule = new Policy(RESPONSE_BODY_RULE, "param1=.*;\\sparam2=.*;\\sparam3=.*;", String.format(PATH_BODY, TEST_RESOURCE_HOST) + "@.*", true);

        PMSUtil.addPolicies(new Policy[]{requestHeaderRule, responseBodyRule});
        logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);

        final String RESOURCE_URI = "asimov_normalization_body_10";
        final String uri = createTestResourceUri(RESOURCE_URI);
        PrepareResourceUtil.prepareResource(uri, false);
        try {
            final String expectedPart1 = "HTTP/1.1 200 OK" + CRLF + "Content-Type: text/xml; charset=utf-8" + CRLF + "Content-Length: 52" + CRLF + "Connection: close" + CRLF + CRLF + "%s";
            final String expectedEncoded1 = URLEncoder.encode(Base64.encodeToString(
                    String.format(expectedPart1, "param1=dgserygvrw; param2=kureyhcfd%; param3=ggregg;").getBytes(),
                    Base64.DEFAULT));

            // 1.1 from network
            checkMiss(createRequest().setUri(uri).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("X-OC-Raw", expectedEncoded1)
                    .getRequest(), 1, (int) MIN_RMP_PERIOD);

            final String expectedPart2 = "HTTP/1.1 200 OK" + CRLF + "Content-Type: text/xml; charset=utf-8" + CRLF + "Content-Length: 53" + CRLF + "Connection: close" + CRLF + CRLF + "%s";
            final String expectedEncoded2 = URLEncoder.encode(Base64.encodeToString(
                    String.format(expectedPart2, "param1=dgsgrgrreegvrw; param2=sherre%; param3=gggreg;").getBytes(),
                    Base64.DEFAULT));

            // 1.2 from network
            checkMiss(createRequest().setUri(uri).setMethod("GET").addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("X-OC-Raw", expectedEncoded2).getRequest(), 2, (int) MIN_RMP_PERIOD);

            final String expectedPart3 = "HTTP/1.1 200 OK" + CRLF + "Content-Type: text/xml; charset=utf-8" + CRLF + "Content-Length: 47" + CRLF + "Connection: close" + CRLF + CRLF + "%s";
            final String expectedEncoded3 = URLEncoder.encode(Base64.encodeToString(
                    String.format(expectedPart3, "param1=gewgerher; param2=g5g6yd%; param3=geggg;").getBytes(),
                    Base64.DEFAULT));

            // 1.3 from network
            checkMiss(createRequest().setUri(uri).setMethod("GET").addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("X-OC-Raw", expectedEncoded3).getRequest(), 3, (int) MIN_RMP_PERIOD);

            // 1.4 from cache
            checkHit(createRequest().setUri(uri).setMethod("GET").addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("X-OC-Raw", expectedEncoded3).getRequest(), 4);

            logger.info("Finish");
        } finally {
            PrepareResourceUtil.prepareResource(uri, true);
        }
    }

    /**
     * <h1>Testing that cookie normalization applies correctly.</h1>
     * <p>Test doesn't require specific policies for cookie normalization.</p>
     * <p>The test checks that 4 response will be HIT.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>HIT, get response immediately, sleep on 5 seconds</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testNormalizationByCookieOneParam_NORMALIZATION_007() throws Throwable {
        logger.info("Started");

        PMSUtil.cleanPaths(new String[]{String.format(PATH_COOKIE, TEST_RESOURCE_HOST) + "@.*"});
        logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);

        final String RESOURCE_URI = "asimov_normalization_201";
        final String uri = createTestResourceUri(RESOURCE_URI);
        PrepareResourceUtil.prepareResource(uri, false);
        try {

            checkMiss(createRequest().setUri(uri).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("Cookie", "m_user=dgfewgfwe;")
                    .getRequest(), 1, (int) MIN_RMP_PERIOD);

            checkMiss(createRequest().setUri(uri).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("Cookie", "m_user=dgfewgfwe;")
                    .getRequest(), 2, (int) MIN_RMP_PERIOD);

            checkMiss(createRequest().setUri(uri).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("Cookie", "m_user=dgfewgfwe;")
                    .getRequest(), 3, (int) MIN_RMP_PERIOD);

            checkHit(createRequest().setUri(uri).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("Cookie", "m_user=dgfewgfwe;")
                    .getRequest(), 4, (int) MIN_RMP_PERIOD);

            logger.info("Finish");
        } finally {
            PrepareResourceUtil.prepareResource(uri, true);
            logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
        }
    }

    /**
     * <h1>Testing that cookie normalization applies correctly.</h1>
     * <p>Test doesn't require specific policies for cookie normalization.</p>
     * <p>The test checks that 4 response will be HIT.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>HIT, get response immediately, sleep on 5 seconds</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testNormalizationByCookieTwoParams_NORMALIZATION_008() throws Throwable {
        logger.info("Started");

        PMSUtil.cleanPaths(new String[]{String.format(PATH_COOKIE, TEST_RESOURCE_HOST) + "@.*"});
        logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);

        final String RESOURCE_URI = "asimov_normalization_301";
        final String uri = createTestResourceUri(RESOURCE_URI);
        PrepareResourceUtil.prepareResource(uri, false);
        try {

            checkMiss(createRequest().setUri(uri).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("Cookie", "m_user=sss; param=sdvgdsgvwer;").getRequest(), 1, (int) MIN_RMP_PERIOD);

            checkMiss(createRequest().setUri(uri).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("Cookie", "m_user=sss; param=sdvgdsgvwer;").getRequest(), 2, (int) MIN_RMP_PERIOD);

            checkMiss(createRequest().setUri(uri).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("Cookie", "m_user=sss; param=sdvgdsgvwer;").getRequest(), 3, (int) MIN_RMP_PERIOD);

            checkHit(createRequest().setUri(uri).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("Cookie", "m_user=sss; param=sdvgdsgvwer;").getRequest(), 4, (int) MIN_RMP_PERIOD);

            logger.info("Finish");
        } finally {
            PrepareResourceUtil.prepareResource(uri, true);
            logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
        }
    }

    /**
     * <h1>Testing that cookie normalization applies correctly.</h1>
     * <p>Test doesn't require specific policies for cookie normalization.</p>
     * <p>The test checks that 4 response will be HIT.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>HIT, get response immediately, sleep on 5 seconds</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testNormalizationByCookieTwoParams2_NORMALIZATION_009() throws Throwable {
        logger.info("Started");

        PMSUtil.cleanPaths(new String[]{String.format(PATH_COOKIE, TEST_RESOURCE_HOST) + "@.*"});
        logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);

        final String RESOURCE_URI = "asimov_normalization_501";
        final String uri = createTestResourceUri(RESOURCE_URI);
        PrepareResourceUtil.prepareResource(uri, false);
        try {

            checkMiss(createRequest().setUri(uri).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("Cookie", "m_user=sss; param=fst;").getRequest(), 1, (int) MIN_RMP_PERIOD);

            checkMiss(createRequest().setUri(uri).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("Cookie", "m_user=sss; param=rhgerre;").getRequest(), 2, (int) MIN_RMP_PERIOD);

            checkMiss(createRequest().setUri(uri).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("Cookie", "m_user=sss; param=sdvgdsgvwer;").getRequest(), 3, (int) MIN_RMP_PERIOD);

            checkHit(createRequest().setUri(uri).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("Cookie", "m_user=sss; param=sdvgdsgvwer;").getRequest(), 4);

            logger.info("Finish");
        } finally {
            PrepareResourceUtil.prepareResource(uri, true);
            logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
        }
    }

    /**
     * <h1>Testing that cookie normalization applies correctly.</h1>
     * <p>Test requires specific policies for cookie normalization.</p>
     * <p>The test checks that 4 response will be HIT and 5 response will be MISS.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>HIT, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testNormalizationByCookieSimple_NORMALIZATION_010() throws Throwable {
        logger.info("Started");

        final Policy cookie = new Policy("cookie_rules", ".*", String.format(PATH_COOKIE, TEST_RESOURCE_HOST) + "@.*", true);
        PMSUtil.addPolicies(new Policy[]{cookie});
        logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);

        final String RESOURCE_URI = "asimov_normalization_401";
        final String uri = createTestResourceUri(RESOURCE_URI);
        PrepareResourceUtil.prepareResource(uri, false);
        try {

            checkMiss(createRequest().setUri(uri).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("Cookie", "asdagwe6587Mg,m")
                    .getRequest(), 1, (int) MIN_RMP_PERIOD);

            checkMiss(createRequest().setUri(uri).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("Cookie", "asdagwe6587Mg,m")
                    .getRequest(), 2, (int) MIN_RMP_PERIOD);

            checkMiss(createRequest().setUri(uri).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("Cookie", "asdagwe6587Mg,m")
                    .getRequest(), 3, (int) MIN_RMP_PERIOD);

            checkHit(createRequest().setUri(uri).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("Cookie", "asdagwe6587Mg,m")
                    .getRequest(), 4, (int) MIN_RMP_PERIOD);

            checkMiss(createRequest().setUri(uri).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("Cookie", "basdagwe6587Mg,m")
                    .getRequest(), 5, (int) MIN_RMP_PERIOD);

            logger.info("Finish");
        } finally {
            PrepareResourceUtil.prepareResource(uri, true);
        }
    }

    /**
     * <h1>Testing that body and uri normalization applies correctly.</h1>
     * <p>Test requires specific policies for body and uri normalization.</p>
     * <p>The test checks that 4,5,6,7,8 response will be HIT.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>HIT, get response immediately, sleep on 5 seconds</li>
     * <li>HIT, get response immediately, sleep on 5 seconds</li>
     * <li>HIT, get response immediately, sleep on 5 seconds</li>
     * <li>HIT, get response immediately, sleep on 5 seconds</li>
     * <li>HIT, get response immediately, sleep on 5 seconds</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testNormalizationFB_NORMALIZATION_011() throws Throwable {

        PMSUtil.cleanPaths(new String[]{String.format("%s%s", String.format(PATH_COOKIE, TEST_RESOURCE_HOST), "@.*")});

        final Policy responseBodyRule = new Policy(RESPONSE_BODY_RULE, ".*", String.format(PATH_BODY, TEST_RESOURCE_HOST) + "@.*", true);
        final Policy requestHeaderRule = new Policy(REQUEST_HEADER_RULE, "X-OC-Raw:.*", String.format(PATH_HEADER, TEST_RESOURCE_HOST) + "@.*", true);
        final Policy responseHeaderRule = new Policy(RESPONSE_HEADER_RULE, "X-FB-Server:.*", String.format(PATH_HEADER, TEST_RESOURCE_HOST) + "@.*", true);
        final Policy uriRule = new Policy(URI_RULE, "(cb=.*?(&amp;|#|$))|(idle=.*?(&amp;|#|$))", String.format(PATH_URI, TEST_RESOURCE_HOST) + "@.*", true);
        final HashSet<Policy> policies = new HashSet<Policy>();
        policies.add(responseBodyRule);
        policies.add(requestHeaderRule);
        policies.add(uriRule);
        policies.add(responseHeaderRule);
        PMSUtil.preparePmsServer(policies);
        logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
        SmsUtil.sendPolicyUpdate(getContext(), (byte) 1);
        logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);


        final String RESOURCE_URI = "asimov_normalization_101";
        final String uri = createTestResourceUri(RESOURCE_URI);

        final String expectedPart1 = "HTTP/1.0 200 OK" + CRLF + "Content-Type: text/plain" + CRLF + "Content-Length: 34" + CRLF
                + "Cache-Control: private" + CRLF
                + "Pragma: no-cache" + CRLF;
        final String expectedPart2 = CRLF + "Date: Tue, 20 Mar 2012 10:18:14 GMT" + CRLF + CRLF + expectedBody;

        PrepareResourceUtil.prepareResource(uri + "_", false);
        try {
            String cookiePart1 = "c_user=100002551305553; datr=tws9T1SYsfrSu4cM2KYHS9SQ; ";
            String cookiePart2 = " m_user=0%3A0%3A0%3A0%3Av_1%2Cajax_1%2Cwidth_1280%2Cpxr_1%2Cgps_1%3A1332237697%3A2; xs=123%3Ac64c3fcf052e438ef75afbedcc1d23c5%3A0%3A1332237697; wd=1280x933; p=1; presence=EM332238733EuserFA21B02551305553A2EstateFDutF0Et2F_5b_5dEuct2F1332238043BElm2FnullEtrFnullEtwF542125506Esb2F0H0EblcF0EsndF1CEchFDp_5f1B02551305553F1CC; sub=1";

            checkMiss(createRequest().setUri(uri + "_cb=bt5k&idle=0").setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("Cookie", cookiePart1 + "lu=MU6ziJdi7GecKFSfdloPYKyw;" + cookiePart2)
                    .addHeaderField("X-OC-Raw", URLEncoder.encode(Base64.encodeToString((expectedPart1 + "X-FB-Server: 10.30.216.159" + expectedPart2).getBytes(), Base64.DEFAULT)))
                    .getRequest(), 1, (int) MIN_RMP_PERIOD);

            checkMiss(createRequest().setUri(uri + "_cb=bt5k&idle=25").setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("Cookie", cookiePart1 + "lu=Az6ziJdi7GecKFSfdloPYKyw;" + cookiePart2)
                    .addHeaderField("X-OC-Raw", URLEncoder.encode(Base64.encodeToString((expectedPart1 + "X-FB-Server: 10.30.216.148" + expectedPart2).getBytes(), Base64.DEFAULT)))
                    .getRequest(), 2, (int) MIN_RMP_PERIOD);

            checkMiss(createRequest().setUri(uri + "_cb=bk91&idle=50").setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("Cookie", cookiePart1 + "lu=Dl6ziJdi7GecKFSfdloPYKyw;" + cookiePart2)
                    .addHeaderField("X-OC-Raw", URLEncoder.encode(Base64.encodeToString((expectedPart1 + "X-FB-Server: 10.30.216.148" + expectedPart2).getBytes(), Base64.DEFAULT)))
                    .getRequest(), 3, (int) MIN_RMP_PERIOD);

            checkHit(createRequest().setUri(uri + "_cb=i9v1&idle=75").setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("Cookie", cookiePart1 + "lu=Vf6ziJdi7GecKFSfdloPYKyw;" + cookiePart2)
                    .addHeaderField("X-OC-Raw", URLEncoder.encode(Base64.encodeToString((expectedPart1 + "X-FB-Server: 10.30.216.186" + expectedPart2).getBytes(), Base64.DEFAULT)))
                    .getRequest(), 4, (int) MIN_RMP_PERIOD);

            checkHit(createRequest().setUri(uri + "_cb=j9ye&idle=100").setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("Cookie", cookiePart1 + "lu=Sg6ziJdi7GecHjSfdloPYKyw;" + cookiePart2)
                    .addHeaderField("X-OC-Raw", URLEncoder.encode(Base64.encodeToString((expectedPart1 + "X-FB-Server: 10.30.216.155" + expectedPart2).getBytes(), Base64.DEFAULT)))
                    .getRequest(), 5, (int) MIN_RMP_PERIOD);

            checkHit(createRequest().setUri(uri + "_cb=60sq&idle=125").setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("Cookie", cookiePart1 + "lu=Sg6ziJdi7Ge678SfdloPYKyw;" + cookiePart2)
                    .addHeaderField("X-OC-Raw", URLEncoder.encode(Base64.encodeToString((expectedPart1 + "X-FB-Server: 10.30.216.181" + expectedPart2).getBytes(), Base64.DEFAULT)))
                    .getRequest(), 6, (int) MIN_RMP_PERIOD);

            checkHit(createRequest().setUri(uri + "_cb=20bk&idle=150").setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("Cookie", cookiePart1 + "lu=Sg2569874fghKFSfdloPYKyw;" + cookiePart2)
                    .addHeaderField("X-OC-Raw", URLEncoder.encode(Base64.encodeToString((expectedPart1 + "X-FB-Server: 10.30.216.184" + expectedPart2).getBytes(), Base64.DEFAULT)))
                    .getRequest(), 7, (int) MIN_RMP_PERIOD);

            checkHit(createRequest().setUri(uri + "_cb=k2oh&idle=175").setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("Cookie", cookiePart1 + "lu=Sg6ziJdi7G2568SfdloPYKyw;" + cookiePart2)
                    .addHeaderField("X-OC-Raw", URLEncoder.encode(Base64.encodeToString((expectedPart1 + "X-FB-Server: 10.30.216.198" + expectedPart2).getBytes(), Base64.DEFAULT)))
                    .getRequest(), 8, (int) MIN_RMP_PERIOD);

        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * <h1>Testing that body normalization applies correctly.</h1>
     * <p>Test requires specific policies for body normalization.</p>
     * <p>The test checks that 4 response will be HIT.</p>
     * <p>The test checks that 5 response will be HIT.</p>
     * <p>The test checks that 6 response will be HIT.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>HIT, get response immediately, sleep on 5 seconds</li>
     * <li>HIT, get response immediately, sleep on 5 seconds</li>
     * <li>HIT, get response immediately, sleep on 5 seconds</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testNormalizationByBody2_NORMALIZATION_012() throws Throwable {
        final Policy responseBodyRule = new Policy(RESPONSE_BODY_RULE, ".*", String.format(PATH_BODY, TEST_RESOURCE_HOST) + "@.*", true);
        final Policy requestHeaderRule = new Policy(REQUEST_HEADER_RULE, "X-OC-Raw:.*", String.format(PATH_HEADER, TEST_RESOURCE_HOST) + "@.*", true);
        PMSUtil.addPolicies(new Policy[]{responseBodyRule, requestHeaderRule});
        logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);

        final String uri = createTestResourceUri("asimov_it_cv_normalization_body2");
        PrepareResourceUtil.prepareResource(uri, false);
        try {
            final String modified1 = "HTTP/1.0 200 OK" + CRLF
                    + "Content-Length: 10" + CRLF + CRLF
                    + "1111111111";
            final String encoded1 = URLEncoder.encode(Base64.encodeToString(modified1.getBytes(), Base64.DEFAULT));

            checkMiss(createRequest().setUri(uri)
                    .addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("X-OC-Raw", encoded1).getRequest(), 1, (int) MIN_RMP_PERIOD);

            final String modified2 = "HTTP/1.0 200 OK" + CRLF
                    + "Content-Length: 10" + CRLF + CRLF
                    + "2222222222";

            final String encoded2 = URLEncoder.encode(Base64.encodeToString(modified2.getBytes(), Base64.DEFAULT));

            checkMiss(createRequest().setUri(uri)
                    .addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("X-OC-Raw", encoded2).getRequest(), 2, (int) MIN_RMP_PERIOD);

            final String modified3 = "HTTP/1.0 200 OK" + CRLF
                    + "Content-Length: 10" + CRLF + CRLF
                    + "3333333333";

            final String encoded3 = URLEncoder.encode(Base64.encodeToString(modified3.getBytes(), Base64.DEFAULT));

            checkMiss(createRequest().setUri(uri)
                    .addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("X-OC-Raw", encoded3).getRequest(), 3, (int) MIN_RMP_PERIOD);

            final String modified4 = "HTTP/1.0 200 OK" + CRLF
                    + "Content-Length: 10" + CRLF + CRLF
                    + "4444444444";

            final String encoded4 = URLEncoder.encode(Base64.encodeToString(modified4.getBytes(), Base64.DEFAULT));

            checkHit(createRequest().setUri(uri)
                    .addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("X-OC-Raw", encoded4).getRequest(), 4, (int) MIN_RMP_PERIOD);

            final String modified5 = "HTTP/1.0 200 OK" + CRLF
                    + "Content-Length: 10" + CRLF + CRLF
                    + "5555555555";

            final String encoded5 = URLEncoder.encode(Base64.encodeToString(modified5.getBytes(), Base64.DEFAULT));

            checkHit(createRequest().setUri(uri)
                    .addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("X-OC-Raw", encoded5).getRequest(), 5, (int) MIN_RMP_PERIOD);

            final String modified6 = "HTTP/1.0 200 OK" + CRLF
                    + "Content-Length: 10" + CRLF + CRLF
                    + "6666666666";
            final String encoded6 = URLEncoder.encode(Base64.encodeToString(modified6.getBytes(), Base64.DEFAULT));

            checkHit(createRequest().setUri(uri)
                    .addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("X-OC-Raw", encoded6).getRequest(), 6, (int) MIN_RMP_PERIOD);
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * <h1>Testing that cookie normalization applies correctly.</h1>
     * <p>Test doesn't require specific policies for cookie normalization.</p>
     * <p>The test checks MD5 of 2 requests. They must be the same.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>Checking MD5 of requests<li/>
     * </ol>
     *
     * @throws Exception
     */
    @LargeTest
    public void testCookieRuleAbsence() throws Exception {
        PMSUtil.cleanPaths(new String[]{String.format(PATH_COOKIE, TEST_RESOURCE_HOST) + "@.*"});
        logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
        compareMD5("normalization_ym_cookie_rules_no", new HttpHeaderField[]{new HttpHeaderField("Cookie", "IM=asdflkngak;")},
                new HttpHeaderField[]{new HttpHeaderField("Cookie", "IM=hfgjhghkjj;")}, true, true);
    }

    /**
     * <h1>Testing that cookie normalization applies correctly.</h1>
     * <p>Test requires specific policies for cookie normalization.</p>
     * <p>The test checks MD5 of 2 requests. They must be different.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>Checking MD5 of requests<li/>
     * </ol>
     *
     * @throws Exception
     */
    @LargeTest
    public void testCookieRuleNormalization() throws Exception {
        final Policy cookie = new Policy("cookie_rules", ".*", String.format(PATH_COOKIE, TEST_RESOURCE_HOST) + "@.*", true);
        PMSUtil.addPolicies(new Policy[]{cookie});
        logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
        compareMD5("normalization_ym_cookie_rules_all", new HttpHeaderField[]{new HttpHeaderField("Cookie", "IM=asdflkngak;")},
                new HttpHeaderField[]{new HttpHeaderField("Cookie", "IM=hfgjhghkjj;")}, false, true);
    }

    /**
     * <h1>Testing that cookie normalization applies correctly.</h1>
     * <p>Test requires specific policies for cookie normalization.</p>
     * <p>The test checks MD5 of 2 requests. They must be different.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>Checking MD5 of requests<li/>
     * </ol>
     *
     * @throws Exception
     */
    @LargeTest
    public void testYahooImProperty() throws Exception {
        final Policy cookie = new Policy("cookie_rules", ".*", String.format(PATH_COOKIE, TEST_RESOURCE_HOST) + "@.*", true);
        PMSUtil.addPolicies(new Policy[]{cookie});
        logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
        compareMD5("normalization_ym_cookie_rules_im", new HttpHeaderField[]{new HttpHeaderField("Cookie", "IM=asdflkngak;")},
                new HttpHeaderField[]{new HttpHeaderField("Cookie", "IM=hfgjhghkjj;")}, false, true);
    }

    /**
     * <h1>Testing that cookie normalization applies correctly.</h1>
     * <p>Test doesn't require specific policies for cookie normalization.</p>
     * <p>The test checks MD5 of 2 requests. They must be the same. Also it cleans all policies for normalization tests</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>Checking MD5 of requests<li/>
     * </ol>
     *
     * @throws Exception
     */
    @LargeTest
    public void testYahooIntProperty() throws Exception {
        PMSUtil.cleanPaths(new String[]{String.format(PATH_COOKIE, TEST_RESOURCE_HOST) + "@.*"});
        logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
        compareMD5("normalization_ym_cookie_rules_int", new HttpHeaderField[]{new HttpHeaderField("Cookie", "IM=asdflkngak;")},
                new HttpHeaderField[]{new HttpHeaderField("Cookie", "IM=hfgjhghkjj;")}, true, true);
    }

    /**
     * <h1>Testing that cookie normalization applies correctly.</h1>
     * <p>Test doesn't require specific policies for cookie normalization.</p>
     * <p>The test checks MD5 of 2 requests. They must be the same.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>Checking MD5 of requests<li/>
     * </ol>
     *
     * @throws Exception
     */
    @LargeTest
    public void testYahooImPropertyWithRn() throws Exception {
        PMSUtil.cleanPaths(new String[]{String.format(PATH_COOKIE, TEST_RESOURCE_HOST) + "@.*"});
        logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
        compareMD5("normalization_ym_cookie_rules_im_rn", new HttpHeaderField[]{new HttpHeaderField("Cookie", "IM=asdflkngak;")},
                new HttpHeaderField[]{new HttpHeaderField("Cookie", "IM=hfgjhghkjj;")}, true, true);
    }

    /**
     * <h1>Testing that cookie normalization applies correctly.</h1>
     * <p>Test doesn't require specific policies for cookie normalization. Set up all policies for normalization tests</p>
     * <p>The test checks MD5 of 2 requests. They must be the same.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>MISS, get response immediately, sleep on 5 seconds</li>
     * <li>Checking MD5 of requests<li/>
     * </ol>
     *
     * @throws Exception
     */
    @LargeTest
    public void testBodyNormalizationParam() throws Exception {
        PMSUtil.cleanPaths(new String[]{"@asimov@http"});
        logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
        final Policy responseBodyRule = new Policy(RESPONSE_BODY_RULE, ".*", String.format(PATH_BODY, TEST_RESOURCE_HOST) + "@.*", true);
        final Policy responseHeaders = new Policy(RESPONSE_HEADER_RULE, "Content-Length:.*", String.format(PATH_HEADER, TEST_RESOURCE_HOST) + "@.*", true);
        final Policy requestHeaders = new Policy(REQUEST_HEADER_RULE, "X-OC-.*:.*\\r\\n", String.format(PATH_HEADER, TEST_RESOURCE_HOST) + "@.*", true);
        PMSUtil.addPoliciesWithCheck(new Policy[]{responseBodyRule, responseHeaders, requestHeaders});
        logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
        compareMD5("normalization_body_md5_check", null, new HttpHeaderField[]{new HttpHeaderField("X-OC-ChangeResponseContent", "blahblahblah")}, true, true);
    }
}
