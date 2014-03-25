package com.seven.asimov.it.tests.caching.dns;


import android.test.suitebuilder.annotation.LargeTest;
import android.util.Base64;
import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.testcases.DnsTestCase;
import com.seven.asimov.it.utils.date.DateUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.Security;
import java.util.Date;

/**
 * TODO @Ignore by ASMV-21650
 */
@Ignore
public class ZHttpsExpiresTests extends DnsTestCase {
    private static final Logger logger = LoggerFactory.getLogger(ZHttpsExpiresTests.class.getSimpleName());

    @LargeTest
    public void testAndroidTestCaseSetupProperlyCacheExpired() {
        System.setProperty("networkaddress.cache.ttl", "0");
        Security.setProperty("networkaddress.cache.ttl", "0");
        System.setProperty("networkaddress.cache.negative.ttl", "0");
        Security.setProperty("networkaddress.cache.negative.ttl", "0");
    }

    /**
     * Pattern [0,60,60]
     * Time shifting pattern [0,2,0]
     * Expected results: [MISS, HIT, MISS]
     * <p/>
     * DNS CE is saved as non-rfc cache. After 2 hours, it must get expired
     */

    @LargeTest
    public void test_001_ZhttpsDnsExpires() throws InterruptedException {
        final int[] intervals = new int[]{0, 70, 70};
        final int[] timeShifts = new int[]{0, 2, 0};
        final String[] expectedResults = new String[]{MISS, HIT, MISS};
        final String uri = createTestResourceUri("asimov_https_cert_expires", true);

        try {
            runDnsTestWithParameters(uri, intervals, timeShifts, expectedResults);
        } catch (InterruptedException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        } catch (IOException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        } catch (InstantiationException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        } catch (IllegalAccessException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        logger.info("test ended");
    }

    /**
     * Pattern [0,60,60]
     * Time shifting pattern [0,2,0]
     * Expected results: [MISS, HIT, MISS]
     * <p/>
     * No polling is started. Only a single cache entry is saved. After 2 hours, CE should get stale.
     * Third reqeust should be served from network
     */


    @LargeTest
    public void test_002_ZhttpsDnsExpires() {
        int[] intervals = new int[]{0, 60, 60};
        int[] timeShifts = new int[]{0, 2, 0};
        String[] expectedResults = new String[]{MISS, HIT, MISS};
        String uri = createTestResourceUri("asimov_https_cert_expires", true);

        long expire = System.currentTimeMillis() + DateUtil.CURRENT_DEVICE_TZ_OFFSET + 2 * DateUtil.HOURS;
        String expected = "HTTP/1.0 200 OK" + TFConstantsIF.CRLF + "Connection: close" + TFConstantsIF.CRLF + "Date: "
                + DateUtil.format(new Date(System.currentTimeMillis())) + TFConstantsIF.CRLF + "Expires: "
                + DateUtil.format(new Date(expire)) + TFConstantsIF.CRLF + "Content-Length: 4" + TFConstantsIF.CRLF + TFConstantsIF.CRLF + VALID_RESPONSE;
        String expectedEncoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));
        HttpRequest request1 = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("X-OC-Raw", expectedEncoded)
                .getRequest();
        sendRequest2(request1);

        try {
            runDnsTestWithParameters(uri, intervals, timeShifts, expectedResults);
        } catch (InterruptedException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        } catch (IOException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        } catch (InstantiationException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        } catch (IllegalAccessException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * Pattern [0,70,70,70,70,70]
     * Time shifting pattern [0,0,0,0,0,24,0]
     * Expected results: [MISS, HIT, HIT, HIT, HIT,MISS]
     * <p/>
     * After polling is started, set time to +24 h. Polling should expire.
     * Polling should be expired, and request must be missed.
     */

    @LargeTest
    public void test_003_ZhttpsDnsExpires() {
        int[] intervals = new int[]{0, 70, 70, 70, 70, 70};
        int[] timeShifts = new int[]{0, 0, 0, 0, 24, 0};
        String[] expectedResults = new String[]{MISS, HIT, HIT, HIT, HIT, MISS};
        String uri = createTestResourceUri("asimov_https_cert_expires", true);

        try {
            runDnsTestWithParameters(uri, intervals, timeShifts, expectedResults);
        } catch (InterruptedException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        } catch (IOException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        } catch (InstantiationException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        } catch (IllegalAccessException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * Pattern [0,70,70,70,70,70,70]
     * Time shifting pattern [0,0,0,0,23,2,0]
     * Expected results: [MISS, HIT, HIT, HIT, HIT,MISS]
     * <p/>
     * After polling is started, set time to +23 h. Polling should not expire. After that, set time to +2 hours (total 25).
     * Polling should get expired, and request must be missed.
     */

    public void test_004_ZhttpsDnsExpires() {
        int[] intervals = new int[]{0, 70, 70, 70, 70, 70, 70};
        int[] timeShifts = new int[]{0, 0, 0, 0, 23, 2, 0};
        String[] expectedResults = new String[]{MISS, HIT, HIT, HIT, HIT, MISS, HIT};
        String uri = createTestResourceUri("asimov_https_cert_expires", true);

        try {
            runDnsTestWithParameters(uri, intervals, timeShifts, expectedResults);
        } catch (InterruptedException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        } catch (IOException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        } catch (InstantiationException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        } catch (IllegalAccessException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * Pattern [0,70,70,70,70,70]
     * Time shifting pattern [0,0,0,0,25,0]
     * Expected results: [MISS, HIT, HIT, HIT, HIT,MISS]
     * <p/>
     * After RFC polling is started, set time to +23 h. Polling should not expire. After that, set time to +6 hours (total 29).
     * Polling should get expired, and request must be missed.
     */

    public void test_005_ZhttpsDnsExpires() {
        int[] intervals = new int[]{0, 70, 70, 70, 70, 70, 70};
        int[] timeShifts = new int[]{0, 0, 0, 0, 23, 6, 0};
        String[] expectedResults = new String[]{MISS, HIT, HIT, HIT, HIT, MISS, HIT};
        String uri = createTestResourceUri("asimov_https_cert_expires", true);

        long expire = System.currentTimeMillis() + DateUtil.CURRENT_DEVICE_TZ_OFFSET + DateUtil.DAYS + DateUtil.HOURS;
        String expected = "HTTP/1.0 200 OK" + TFConstantsIF.CRLF + "Connection: close" + TFConstantsIF.CRLF + "Date: "
                + DateUtil.format(new Date(System.currentTimeMillis())) + TFConstantsIF.CRLF + "Expires: "
                + DateUtil.format(new Date(expire)) + TFConstantsIF.CRLF + "Content-Length: 4" + TFConstantsIF.CRLF + TFConstantsIF.CRLF + VALID_RESPONSE;
        String expectedEncoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));
        HttpRequest request1 = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("X-OC-Raw", expectedEncoded)
                .getRequest();
        sendRequest2(request1);

        try {
            runDnsTestWithParameters(uri, intervals, timeShifts, expectedResults);
        } catch (InterruptedException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        } catch (IOException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        } catch (InstantiationException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        } catch (IllegalAccessException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

}

