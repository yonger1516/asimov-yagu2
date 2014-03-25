package com.seven.asimov.it.tests.dispatchers.bwlists;


import com.seven.asimov.it.testcases.BWListHttpsTestCase;
import com.seven.asimov.it.utils.customservice.CustomService;
import com.seven.asimov.it.utils.customservice.CustomServiceUtil;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class BWListHostsAndPortsTests extends BWListHttpsTestCase {
    private static final Logger logger = LoggerFactory.getLogger(BWListHttpsTestCase.class.getSimpleName());
    private static final String PORTS_PATH = "ports";
    private static final String SSL_PATH = "@asimov@application@com.seven.asimov.it@ssl";
    private static final String DEFAULT_SSL = ",,,,,,";
    private static final String CUSTOM_SERVICES_PORTS_RANGE = "11400:11500";
    private static final String OTHER_PORT = "50000";
    private static final String EMPTY_PORTS = "";
    private static final int MIN_RMP_PERIOD = 5000;
    final String SSL_ENABLE_NAME = "enabled";
    final String SSL_ENABLE_VALUE = "true";

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

            } catch (Throwable assertionFailedError) {
                logger.info("Test failed due to " + ExceptionUtils.getStackTrace(assertionFailedError));
                isPassed = false;
                counts.add("Test failed due to AssertionFailedError in " + numberOfAttempts + " attempt");
                Thread.sleep(2 * 60 * 1000);
            }
        } while (!isPassed && numberOfAttempts < 3);

        assertTrue("The test was failed three times ", counts.size() != 3);
    }

    /**
     * <P>1. Add namespace "com.seven.asimov.it" to ssl-branch of PMS, with "ports" property value
     * allowing optimization for connection to Custom Service. Port value is set based on value
     * returned by Custom Service initialization routine, port will be in range of 11400-11500.</P>
     * <P>2. Be sure that policies arrive and they are applied to this package in logcat.
     * All handshake types for process com.seven.asimov.it for all ports should be seen from log.
     * Traffic for process com.seven.asimov.it through allowed port should be optimized.</P>
     *
     * @throws Exception
     */
    public void test_001_BWListHostsAndPorts() throws Exception {
        final String pathEnd = "testBWListHostsAndPorts_001";
        final int requestCount = 4;
        final CustomService service = CustomServiceUtil.reserveAndStartCustomService(DEFAULT_SSL);
        try {
            logger.info("Started custom service at port:" + service.getPort() + " session ID:" + service.getSession());
            logger.info("Adding ssl policy:ports=" + service.getPort());

            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(SSL_ENABLE_NAME, SSL_ENABLE_VALUE, SSL_PATH, true), new Policy(PORTS_PATH, Integer.toString(service.getPort()), SSL_PATH, true)});

            BWListHostsAndPortsTester(pathEnd, service, new String[]{service.getSession()}, requestCount, MIN_RMP_PERIOD, true);
        } finally {
            CustomServiceUtil.unreserveCustomService(service);
            PMSUtil.cleanPaths(new String[]{SSL_PATH});
        }
    }

    /**
     * <P>1. Add namespace "com.seven.asimov.it" to ssl-branch of PMS, with "ports" property value
     * allowing optimization for on of two connections to Custom Service. Port value is set based on value
     * returned by Custom Service initialization routine, port will be in range of 11400 - 11500.</P>
     * <P>2. Be sure that policies arrive and they are applied to this package in logcat.
     * All handshake types for process com.seven.asimov.it for first Custom Service port should be seen from log.
     * Traffic for process com.seven.asimov.it through first Custom Service port should be optimized.
     * Traffic through second Custom Service port should not be optimized.</P>
     *
     * @throws Exception
     */
    public void test_002_BWListHostsAndPorts() throws Exception {
        final String pathEnd = "testBWListHostsAndPorts_002";
        final int requestCount = 4;

        final CustomService service1 = CustomServiceUtil.reserveAndStartCustomService(DEFAULT_SSL);
        final CustomService service2 = CustomServiceUtil.reserveAndStartCustomService(DEFAULT_SSL);
        try {
            logger.info("Started custom service 1 at port:" + service1.getPort() + " session ID:" + service1.getSession());
            logger.info("Started custom service 2 at port:" + service2.getPort() + " session ID:" + service2.getPort());
            logger.info("Adding ssl policy:ports=" + service1.getPort());

            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(SSL_ENABLE_NAME, SSL_ENABLE_VALUE, SSL_PATH, true), new Policy(PORTS_PATH, Integer.toString(service1.getPort()), SSL_PATH, true)});

            BWListHostsAndPortsTester(pathEnd, service1, new String[]{service1.getSession(), service2.getSession()}, requestCount, MIN_RMP_PERIOD, true);
            BWListHostsAndPortsTester(pathEnd, service2, new String[]{service2.getSession()}, requestCount, MIN_RMP_PERIOD, false);
        } finally {
            CustomServiceUtil.unreserveCustomService(service1);
            CustomServiceUtil.unreserveCustomService(service2);
            PMSUtil.cleanPaths(new String[]{SSL_PATH});
        }
    }

    /**
     * <P>1. Add namespace "com.seven.asimov.it" to ssl-branch of PMS, with "ports" property value
     * allowing optimization for all possible Custom Service ports. Ports value is set to
     * full range of possible Custom Service ports 11400:11500.</P>
     * <P>2. Be sure that policies arrive and they are applied to this package in logcat.
     * All handshake types for process com.seven.asimov.it for all ports should be seen from log.
     * Traffic for process com.seven.asimov.it through any allowed port should be optimized.</P>
     *
     * @throws Exception
     */
    public void test_003_BWListHostsAndPorts() throws Exception {
        final String pathEnd = "testBWListHostsAndPorts_003";
        final int requestCount = 4;
        final CustomService service1 = CustomServiceUtil.reserveAndStartCustomService(DEFAULT_SSL);
        final CustomService service2 = CustomServiceUtil.reserveAndStartCustomService(DEFAULT_SSL);
        try {
            logger.info("Started custom service 1 at port:" + service1.getPort() + " session ID:" + service1.getSession());
            logger.info("Started custom service 2 at port:" + service2.getPort() + " session ID:" + service2.getSession());
            logger.info("Adding ssl policy:ports=" + CUSTOM_SERVICES_PORTS_RANGE);

            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(SSL_ENABLE_NAME, SSL_ENABLE_VALUE, SSL_PATH, true), new Policy(PORTS_PATH, CUSTOM_SERVICES_PORTS_RANGE, SSL_PATH, true)});

            BWListHostsAndPortsTester(pathEnd, service1, new String[]{service1.getSession(), service2.getSession()}, requestCount, MIN_RMP_PERIOD, true);
            BWListHostsAndPortsTester(pathEnd, service2, new String[]{service2.getSession()}, requestCount, MIN_RMP_PERIOD, true);
        } finally {
            CustomServiceUtil.unreserveCustomService(service1);
            CustomServiceUtil.unreserveCustomService(service2);
            PMSUtil.cleanPaths(new String[]{SSL_PATH});
        }
    }

    /**
     * <P>1. Add namespace "com.seven.asimov.it" to ssl-branch of PMS, with empty "ports" property value.</P>
     * <P>2. Be sure that policies arrive and they are applied to this package in logcat.
     * All handshake types for process com.seven.asimov.it for all ports should be seen from log.
     * Traffic for process com.seven.asimov.it through any port should be optimized.</P>
     *
     * @throws Exception
     */
    public void test_004_BWListHostsAndPorts() throws Exception {
        final String pathEnd = "testBWListHostsAndPorts_004";
        final int requestCount = 4;
        final CustomService service = CustomServiceUtil.reserveAndStartCustomService(DEFAULT_SSL);

        try {
            logger.info("Started custom service at port:" + service.getPort() + " session ID:" + service.getSession());
            logger.info("Adding ssl policy:ports=\"\"");

            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(SSL_ENABLE_NAME, SSL_ENABLE_VALUE, SSL_PATH, true), new Policy(PORTS_PATH, EMPTY_PORTS, SSL_PATH, true)});

            BWListHostsAndPortsTester(pathEnd, service, new String[]{service.getSession()}, requestCount, MIN_RMP_PERIOD, true);
        } finally {
            CustomServiceUtil.unreserveCustomService(service);
            PMSUtil.cleanPaths(new String[]{SSL_PATH});
        }
    }

    /**
     * <P>1. Add namespace "com.seven.asimov.it" to ssl-branch of PMS, with "ports" property value "50000" in it</P>
     * <P>2. Be sure that policies arrive and they are applied to this package in logcat.
     * All handshake types for process com.seven.asimov.it for all ports should be seen from log.
     * Traffic for process com.seven.asimov.it through any Custom Service port from range 11400:11500 should not be optimized.<P>
     *
     * @throws Exception
     */
    public void test_005_BWListHostsAndPorts() throws Exception {
        final String pathEnd = "testBWListHostsAndPorts_005";
        final int requestCount = 4;
        final CustomService service = CustomServiceUtil.reserveAndStartCustomService(DEFAULT_SSL);

        try {
            logger.info("Started custom service at port:" + service.getPort() + " session ID:" + service.getSession());
            logger.info("Adding ssl policy:ports=" + OTHER_PORT);

            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(SSL_ENABLE_NAME, SSL_ENABLE_VALUE, SSL_PATH, true), new Policy(PORTS_PATH, OTHER_PORT, SSL_PATH, true)});

            BWListHostsAndPortsTester(pathEnd, service, new String[]{service.getSession()}, requestCount, MIN_RMP_PERIOD, false);
        } finally {
            CustomServiceUtil.unreserveCustomService(service);
            PMSUtil.cleanPaths(new String[]{SSL_PATH});
        }
    }
}

