package com.seven.asimov.it.asserts;

import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.HttpResponse;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CATFAssert {
    private static final Logger logger = LoggerFactory.getLogger(AsimovTestCase.class.getSimpleName());

    public static void assertStatusCode(int responseId, int expected, HttpResponse response) {
        String message = "Response R" + AsimovTestCase.getShortThreadName() + "." + responseId + " status code is " + response.getStatusCode() + " This can be caused by network issue. Please re-execute this test case";
        assertEquals(message, expected, response.getStatusCode());
    }

    public static void assertResponseBody(int responseId, String expected, HttpResponse response) {
        String message = "Response R" + AsimovTestCase.getShortThreadName() + "." + responseId + " body is " + expected;
        assertEquals(message, expected, response.getBody());
    }

    public static void assertEquals(String message, String expected, String actual) {
        assertEquals(message, AsimovTestCase.getShortThreadName(), expected, actual);
    }

    public static void assertEquals(String message, String threadName, String expected, String actual) {
        message = "Thread " + threadName + ": " + message;
        try {
            Assert.assertEquals(message, expected, actual);
            logger.info("ASSERT", message + ": Passed");
        } catch (AssertionFailedError error) {
            logger.info("ASSERT", message + ": Failed");
            throw error;
        }
    }

    public static void assertEquals(String message, int expected, int actual) {
        assertEquals(message, AsimovTestCase.getShortThreadName(), expected, actual);
    }

    public static void assertEquals(String message, String threadName, int expected, int actual) {
        message = "Thread " + threadName + ": " + message;
        try {
            Assert.assertEquals(message, expected, actual);
            logger.info("ASSERT", message + ": Passed");
        } catch (AssertionFailedError error) {
            logger.info("ASSERT", message + ": Failed");
            throw error;
        }
    }

    public static void assertNotNull(String message, Object object) {
        message = "Thread " + AsimovTestCase.getShortThreadName() + ": " + message;
        try {
            Assert.assertNotNull(message, object);
            logger.info("ASSERT", message + ": Passed");
        } catch (AssertionFailedError error) {
            logger.info("ASSERT", message + ": Failed");
            throw error;
        }
    }

    public static void assertTrue(String message, boolean condition) {
        assertTrue(message, AsimovTestCase.getShortThreadName(), condition);
    }

    public static void assertTrue(String message, String threadName, boolean condition) {
        message = "Thread " + threadName + ": " + message;
        try {
            Assert.assertTrue(message + ": expected <TRUE> but was <FALSE>", condition);
            logger.info("ASSERT", message + ": Passed");
        } catch (AssertionFailedError error) {
            logger.info("ASSERT", message + ": Failed");
            throw error;
        }
    }
}
