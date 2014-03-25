package com.seven.asimov.it.tests.caching.aggressive.expiration;


import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.testcases.DnsTestCase;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * TODO @Ignore by ASMV-21650
 */
@Ignore
public class ExpirationTests extends DnsTestCase {
    private static final Logger logger = LoggerFactory.getLogger(ExpirationTests.class.getSimpleName());

    private static final String RESOLVED_FROM_NETWORK_MSG = "Host should be resolved from network";
    private static final String RESOLVED_FROM_CACHE_MSG = "Host should be resolved from cache";

    private static final int MS_IN_SEC = 1000;
    private static final int TTL_OVERHEAD_MS = 10 * MS_IN_SEC;
    private static final int ANDROID_CACHE_EXPIRATION_TIMEOUT_MS = 610 * MS_IN_SEC;
    private static final int ANDROID_CACHE_EXPIRATION_TIMEOUT_SEC = ANDROID_CACHE_EXPIRATION_TIMEOUT_MS / MS_IN_SEC;
    private static final int DNS_TTL_SEC = 300;
    private static final int HTTP_RESPONSE_RFC_TTL_SEC = ANDROID_CACHE_EXPIRATION_TIMEOUT_SEC + TTL_OVERHEAD_MS / MS_IN_SEC; // TTL for CE cached by RFC, sec

    private static final String DNS_REST_PROPERTY_PATH = "@asimov@dns";
    private static final String TTL_REST_PROPERTY_NAME = "default_cache_ttl";


    public void test_001_ExpirationTests() throws InterruptedException {
        final int[] intervals = new int[]{0, 70};
        final int[] timeShifts = new int[]{1, 0};
        final String[] expectedResults = new String[]{MISS, HIT};
        final String uri = createTestResourceUri("ExpirationTests001", false);

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

    public void test_002_ExpirationTests() throws InterruptedException {
        final int[] intervals = new int[]{0, 70, 70, 70, 70};
        final int[] timeShifts = new int[]{0, 0, 0, 25, 0};
        final String[] expectedResults = new String[]{MISS, HIT, HIT, HIT, MISS};
        final String uri = createTestResourceUri("ExpirationTests002", false);

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

    public void test_003_ExpirationTests() throws InterruptedException {
        final int[] intervals = new int[]{0, 70, 70, 70, 25, 25};
        final int[] timeShifts = new int[]{0, 0, 0, 25, 0, 0};
        final String[] expectedResults = new String[]{MISS, HIT, HIT, HIT, HIT, MISS};
        final String uri = createTestResourceUri("ExpirationTests002", false);

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
}

