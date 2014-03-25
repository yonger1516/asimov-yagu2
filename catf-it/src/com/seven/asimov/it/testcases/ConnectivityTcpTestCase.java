package com.seven.asimov.it.testcases;

import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.Base64;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.lib.connectivity.HostSendRstWhileOCProcessingRequestTest;
import com.seven.asimov.it.lib.connectivity.MultiConnectionsTest;
import com.seven.asimov.it.lib.connectivity.OrdinaryNoStartPollNegative;
import com.seven.asimov.it.lib.connectivity.SendFinInRapidPollTest;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.conn.AbstractConnTest;
import com.seven.asimov.it.utils.conn.ConnSelector;
import com.seven.asimov.it.utils.conn.HttpServer;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static com.seven.asimov.it.base.constants.TFConstantsIF.CRLF;
import static com.seven.asimov.it.base.constants.TFConstantsIF.MIN_CACHING_PERIOD;

public class ConnectivityTcpTestCase extends TcpDumpTestCase {
    private static final Logger logger = LoggerFactory.getLogger(ConnectivityTcpTestCase.class.getSimpleName());

    private static final String RESOURCE_URI1 = "asimov_it_conn_cache01";
    private static final String RESOURCE_URI2 = "asimov_it_conn_cache02";

    protected List<Boolean> testTC21Results = new ArrayList<Boolean>();
    protected static final int TEST_SERVER_PORT = 8088;
    protected ConnSelector mSelector;
    protected HttpServer mTestServer;
    protected Class<?>[] connSelectorAndHttpServerClasses;
    protected Object[] connSelectorAndHttpServerObjects;
    protected boolean isLogResultDescription = true;

    public <P extends AbstractConnTest> void executeAbstractConnTest(
            Class<P> clazz, boolean isLogResultDescription, int timeout,
            Class<?>[] parameterClasses, Object[] parameters) {
        String tag = clazz.getSimpleName();
        logger.info("Class for test selected : " + tag);
        P test = null;
        try {
//            Class<?>[] parameterClasses = new Class<?>[parameters.length];
//            int index = 0;
//            for (Object object : parameters) {
//                parameterClasses[index++] = object.getClass();
//            }
            test = clazz.getDeclaredConstructor(parameterClasses).newInstance(parameters);
            test.run(timeout);

            TestUtil.sleep(20 * 1000);

            if (test.getClass().getSimpleName().equals("HostSendRstWhileOCProcessingRequestTest")) {
                testTC21Results.add(test.isPassed());
            }

            if (isLogResultDescription && test.isPassed()) {
                if (test instanceof MultiConnectionsTest) {
                    logger.info(tag, ((MultiConnectionsTest) test).getConnsDescripton());
                }
                logger.info(tag, test.getResultDescription());
            }
        } catch (Exception e) {
            logger.debug(tag, "Message: " + e.getMessage() + ", StackTrace:" + ExceptionUtils.getStackTrace(e));
        }
        assertNotNull("Test " + tag + " was not executed. Please take a look at log errors.", test);

        if (!test.getClass().getSimpleName().equals("HostSendRstWhileOCProcessingRequestTest")) {
            assertTrue(test.getResultDescription(), test.isPassed());
        }
    }

    public void executeOrdinaryNoStartPollNegative() throws Exception {
        OrdinaryNoStartPollNegative testCase = new OrdinaryNoStartPollNegative();
        testCase.runTest();
    }

    public void executeHostSendRstWhileOCProcessingRequestTest(
            Class<HostSendRstWhileOCProcessingRequestTest> clazz, boolean isLogResultDescription, int timeout,
            Class<?>[] parameterClasses, Object[] parameters) {
        int waitingTimeout = 20 * 1000;
        int i = 0;

        while (i < 5) {
            executeAbstractConnTest(clazz, isLogResultDescription, timeout,
                    parameterClasses, parameters);

            TestUtil.sleep(waitingTimeout);

            while (testTC21Results.size() != i + 1) TestUtil.sleep(1000);

            logger.info("Counter value: " + i);
            logger.info("Is test passed: " + testTC21Results.get(i));

            if (testTC21Results.get(i++)) break;
        }

        assertTrue("Test workflow must be reproduced, but isn't", testTC21Results.get(--i));
    }

    public <P extends SendFinInRapidPollTest> void executeSendFinInRapidPollTest(
            Class<P> clazz, Integer timeout, Class<?>[] parametersClasses,
            Object... parameters) {
        executeSendFinInRapidPollTest(clazz, timeout, null, parametersClasses, parameters);
    }

    public <P extends SendFinInRapidPollTest> void executeSendFinInRapidPollTest(
            Class<P> clazz, Integer timeout, Integer expectedHostNewSocketCount,
            Class<?>[] parametersClasses, Object... parameters) {
        String tag = clazz.getSimpleName();
        P test = null;
        assertNotNull("Test " + tag + " was not executed due to incorrect input parameters.", parameters);
        try {
            test = clazz.getDeclaredConstructor(parametersClasses).newInstance(parameters);
            if (expectedHostNewSocketCount != null) {
                test.setExpectedHostNewSocketCount(expectedHostNewSocketCount);
            }
            test.run(timeout);

            assertTrue(test.getResultDescription(), test.isPassed());
        } catch (Exception e) {
            logger.info("Message: " + e.getMessage() + ", StackTrace:" + ExceptionUtils.getStackTrace(e));
        }
        assertNotNull("Test " + tag + " was not executed. Please take a look at log errors.", test);
        assertTrue(test.getResultDescription(), test.isPassed());
    }

    protected void checkResposeSequence(boolean bCacheRequestFirst) throws Exception {

        final String strCacheBody = "cache..";
        final String CONNECTION_CLOSE = "Connection: close";
        final String CONNECTION_KEEPALIVE = "Connection: Keep-Alive";

        String strCacheUri = createTestResourceUri(RESOURCE_URI2 + bCacheRequestFirst);
        PrepareResourceUtil.prepareResource(strCacheUri, false);
        String strUri1 = createTestResourceUri(RESOURCE_URI1 + bCacheRequestFirst);
        PrepareResourceUtil.prepareResource(strUri1, false);
        String strCacheReqExpected = "HTTP/1.1 200 OK" + CRLF + CONNECTION_CLOSE + CRLF + "Content-Length: 7" + CRLF
                + CRLF + strCacheBody;
        String strCacheEncoded = URLEncoder
                .encode(Base64.encodeToString(strCacheReqExpected.getBytes(), Base64.DEFAULT));
        String strCacheRequest = "GET " + strCacheUri + " HTTP/1.1" + CRLF + "X-OC-Raw: " + strCacheEncoded + CRLF
                + CONNECTION_CLOSE + CRLF + CRLF;

        String strReq1Expected = "HTTP/1.1 200 OK" + CRLF + CONNECTION_KEEPALIVE + CRLF + "Content-Length: 7" + CRLF
                + CRLF + "req11..";
        String strReq1Encoded = URLEncoder.encode(Base64.encodeToString(strReq1Expected.getBytes(), Base64.DEFAULT));
        String strReq1AtFirst = "GET " + strUri1 + " HTTP/1.1" + CRLF + "X-OC-Raw: " + strReq1Encoded + CRLF
                + CONNECTION_KEEPALIVE + CRLF + CRLF;
        String strReq1AtEnd = "GET " + strUri1 + " HTTP/1.1" + CRLF + "X-OC-Raw: " + strReq1Encoded + CRLF
                + CONNECTION_CLOSE + CRLF + CRLF;
        String strCombineRequest;
        // connection for the last URL should be close
        if (bCacheRequestFirst) {
            strCombineRequest = strCacheRequest + strReq1AtEnd;
        } else {
            strCombineRequest = strReq1AtFirst + strCacheRequest;
        }

        try {
            long startTime = System.currentTimeMillis();
            HttpResponse response = sendRequest(strCacheRequest.getBytes(), AsimovTestCase.TEST_RESOURCE_HOST);

            assertEquals(HttpStatus.SC_OK, response.getStatusCode());
            TestUtil.sleep(MIN_CACHING_PERIOD - System.currentTimeMillis() + startTime);

            startTime = System.currentTimeMillis();

            response = sendRequest(strCacheRequest.getBytes(), AsimovTestCase.TEST_RESOURCE_HOST);

            assertEquals(HttpStatus.SC_OK, response.getStatusCode());
            TestUtil.sleep(MIN_CACHING_PERIOD - System.currentTimeMillis() + startTime);

            startTime = System.currentTimeMillis();

            response = sendRequest(strCacheRequest.getBytes(), AsimovTestCase.TEST_RESOURCE_HOST);

            assertEquals(HttpStatus.SC_OK, response.getStatusCode());
            TestUtil.sleep(MIN_CACHING_PERIOD - System.currentTimeMillis() + startTime);

            startTime = System.currentTimeMillis();

            response = sendRequest(strCacheRequest.getBytes(), AsimovTestCase.TEST_RESOURCE_HOST);

            assertEquals(HttpStatus.SC_OK, response.getStatusCode());
            TestUtil.sleep(MIN_CACHING_PERIOD - System.currentTimeMillis() + startTime);


            response = sendRequest(strCombineRequest.getBytes(), AsimovTestCase.TEST_RESOURCE_HOST);

            assertEquals(HttpStatus.SC_OK, response.getStatusCode());
            logger.info("----getRawContent:" + response.getRawContent());
            logger.info("----getBody:" + response.getBody());
            boolean bCacheBodyFirst = response.getBody().indexOf(strCacheBody) == 0;
            // if cache request is the first, cache body is first, if cache request is not first, the cache body is not
            // first.
            assertFalse(bCacheBodyFirst != bCacheRequestFirst);

        } finally {

            // invalidate resource just in case to stop possible polling by server
            PrepareResourceUtil.invalidateResourceSafely(strCacheUri);
            PrepareResourceUtil.invalidateResourceSafely(strCacheUri);
            PrepareResourceUtil.invalidateResourceSafely(strUri1);
        }
    }
}
