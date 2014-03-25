package com.seven.asimov.it.tests.connectivity.socketclosure;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.testcases.SocketClosureFeatureTestCase;
import com.seven.asimov.it.utils.IpTablesUtil;
import com.seven.asimov.it.utils.MobileNetworkUtil;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.preemptiveSocketClosureTasks.PreemptiveTask;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketClosureFeatureTests extends SocketClosureFeatureTestCase {
    private static final Logger logger = LoggerFactory.getLogger(SocketClosureFeatureTests.class.getSimpleName());
    private MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());
    public static int csa;
    public static int csat;

    public void test_000_OutSocketClosure() throws Throwable {
        mobileNetworkUtil.onWifiOnly();
        propertyId[0] = PMSUtil.createPersonalScopeProperty("log_levels", "@asimov@debug_data@logcat", "C6", true);
        propertyId[1] = PMSUtil.createPersonalScopeProperty("log_levels", "@asimov@debug_data@logcat", "D6", true);
        propertyId[2] = PMSUtil.createPersonalScopeProperty("log_levels", "@asimov@debug_data@logcat", "E6", true);
        logSleeping(60 * 1000);
    }

    /**
     * <p>Cooldown interval did not started
     * </p>
     * <p>Pre-requisites:
     * 3. On PMS set related policy for com.seven.asimov.test.tool:
     * asimov@application@<package_name>@preemptive@*@80:443@detection_time=10
     * asimov@application@<package_name>@preemptive@*@80:443@cooldown_time=110
     * 4. Use mobile network connection
     * 5. Install OC
     * 6. Start logcat
     * 7. Start TCPDUMP
     * </p>
     * <p>Steps:
     * 1. Validate preemptive policy applying
     * 2. Send request
     * 3. Send 2nd request after 15seconds
     * 4. Observe logcat
     * </p>
     * <p>Expected reults:
     * 1. Correct PMS policies were downloaded;
     * Preemptive configuration for 7testtool application was applied with correct properties:
     * 'Updating configuration for app_uid 10206';
     * 'Preemptive configuration updated to ip_number=0, port_range='80:443', detection_time=10, cooldown_time=110'
     * PSC was generated for 7testtool application with correct properties:
     * '[PSC] UID: 10206, cooldown_time: 110, detection_time: 10, ports: [80:443]'
     * <p/>
     * 2. After first transaction radio state is 'radio is CONNECTED';
     * TCPDUMP includes [RST, ACK] packet sent by OC from the netport to server to close OUT socket;
     * OUT socket was closed with 'Closing all OUT connections' in logcat;
     * NetLog should include operate type:'preemptive_socket_closure'
     * ( e.g. 'NetLog(loport: 49830, netport: 49831): 2013-11-25 13:30:56.370,netlog,13,93,69,69,93,0,0,
     * hki-qa-testrunner1.7sys.eu,com.seven.asimov.test.tool,foreground,preemptive_socket_closure,-/-/http/tcp4,,mobile_edge,
     * 5684,0,52935110001c8003,4073845326,-1,200,0,unknown,62,7,97A9758F,X[1C0108/1/3]E[0/0]L[2/1]S[0/6]N[49830/49831],
     * 1,192.130.77.187,80,,0,662,5,not_aware' ;
     * Detection of next transaction was started with 'entered Detection Interval for 10 seconds' in logcat
     * <p/>
     * 3. After second transaction radio state is 'radio is CONNECTED';
     * TCPDUMP includes [RST, ACK] packet sent by OC from the netport to server to close OUT socket  because transaction
     * was processed after detection interval;
     * OUT socket was closed with 'Closing all OUT connections' and NetLog should include operate type:
     * 'preemptive_socket_closure' in logcat;
     * detection of next transaction was started with 'entered Detection Interval for 10 seconds' in logcat
     * Cooldown interval was not started because transaction was processed after detection interval;
     * logcat doesn't include 'entered into CoolDown Interval for 110 seconds'
     * </p>
     *
     * @throws Throwable
     */
    public void test_001_OutSocketClosure() throws Throwable {
        banNetworkForAllApp(true);
        mobileNetworkUtil.onWifiOnly();
        PrepareResourceUtil.prepareResource(uri, false);
        PreemptiveTask preemptiveIpTask = new PreemptiveTask();
        PreemptiveTask preemptiveTask = new PreemptiveTask("80:443", "110", "10");
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), preemptiveIpTask, preemptiveTask);
        try {
            logcatUtil.start();
     /*       PMSUtil.addPolicies(new Policy[]{new Policy("detection_time", "10", "@asimov@application@com.seven.asimov.it@preemptive@*@80:443", true),
                    new Policy("cooldown_time", "110", "@asimov@application@com.seven.asimov.it@preemptive@*@80:443", true)
            });
            TestUtil.sleep(2 * 60 * 1000);*/
            mobileNetworkUtil.on3gOnly();
            TestUtil.sleep(15 * 1000);
            logcatUtil.stop();
           /* assertTrue("Preemptive configuration for application was applied with correct properties",
                    preemptiveIpTask.getLogEntries().size() > 0 && preemptiveTask.getLogEntries().size() > 1);*/
            logger.info("Transaction #1");
            checkSocketClosure(10, 110, true);
            TestUtil.sleep(15 * 1000 - getDuration());
            logger.info("Transaction #2");
            checkSocketClosure(10, 110, true);
            TestUtil.sleep(3 * 1000);
        } finally {
            PMSUtil.cleanPaths(new String[]{"@asimov@application@com.seven.asimov.it@preemptive@*@80:443"});
            logcatUtil.stop();
            PrepareResourceUtil.prepareResource(uri, true);
            banNetworkForAllApp(false);
        }
    }

    /**
     * <p>Cooldown interval started
     * </p>
     * <p>Pre-requisites:
     * 1. Install 7testtool application
     * 2. Create suite for 7testtool with 2 HTTP requests and 2 responses with different body.
     * 3. On PMS set related policy for com.seven.asimov.test.tool:
     * asimov@application@com.seven.asimov.test.tool@preemptive@*@80:443@detection_time=10
     * asimov@application@com.seven.asimov.test.tool@preemptive@*@80:443@cooldown_time=110
     * 4. Use mobile network connection
     * 5. Install OC
     * 6. Start logcat
     * 7. Start TCPDUMP
     * </p>
     * <p>Steps:
     * 1. Validate preemptive policy applying
     * 2. Send request using 7testtool
     * 3. Send 2nd request after 6seconds
     * 4. Observe logcat
     * </p>
     * <p>Expected reults:
     * 1. Correct PMS policies were downloaded;
     * Preemptive configuration for 7testtool application was applied with correct properties:
     * 'Updating configuration for app_uid 10206';
     * 'Preemptive configuration updated to ip_number=0, port_range='80:443', detection_time=10, cooldown_time=110'
     * PSC was generated for 7testtool application with correct properties:
     * '[PSC] UID: 10206, cooldown_time: 110, detection_time: 10, ports: [80:443]'
     * 2. After first transaction radio state is 'radio is CONNECTED';
     * TCPDUMP includes [RST, ACK] packet sent by OC from the netport to server to close OUT socket;
     * OUT socket was closed with 'Closing all OUT connections' and NetLog should include operate type:
     * 'preemptive_socket_closure' in logcat;
     * detection of next transaction was started with 'entered Detection Interval for 10 seconds' in logcat
     * 3. After second request OUT socket should remain open: no [RST, ACK] packet sent by OC from the netport to server
     * in TCPDUMP and no 'Closing all OUT connections' in logcat
     * Coldown interval was started with 'entered into CoolDown Interval for 110 seconds' in logcat
     * </p>
     *
     * @throws Throwable
     */
    public void test_002_OutSocketClosure() throws Throwable {
        banNetworkForAllApp(true);
        mobileNetworkUtil.onWifiOnly();
        PrepareResourceUtil.prepareResource(uri, false);
        PreemptiveTask preemptiveIpTask = new PreemptiveTask();
        PreemptiveTask preemptiveTask = new PreemptiveTask("80:443", "110", "10");
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), preemptiveIpTask, preemptiveTask);
        try {
            logcatUtil.start();
            PMSUtil.addPolicies(new Policy[]{new Policy("detection_time", "10", "@asimov@application@com.seven.asimov.it@preemptive@*@80:443", true),
                    new Policy("cooldown_time", "110", "@asimov@application@com.seven.asimov.it@preemptive@*@80:443", true)
            });
            TestUtil.sleep(2 * 60 * 1000);
            mobileNetworkUtil.on3gOnly();
            TestUtil.sleep(15 * 1000);
            logcatUtil.stop();
            assertTrue("Preemptive configuration for application was applied with correct properties",
                    preemptiveIpTask.getLogEntries().size() > 0 && preemptiveTask.getLogEntries().size() > 1);
            logger.info("Transaction #1");
            checkSocketClosure(10, 110, true);
            TestUtil.sleep(6 * 1000 - getDuration());
            logger.info("Transaction #2");
            checkSocketClosure(10, 110, false, true, true);
            TestUtil.sleep(3 * 1000);
        } finally {
            PMSUtil.cleanPaths(new String[]{"@asimov@application@com.seven.asimov.it@preemptive@*@80:443"});
            logcatUtil.stop();
            PrepareResourceUtil.prepareResource(uri, true);
            banNetworkForAllApp(false);
        }
    }

    /**
     * <p>Full preemptive cycle
     * </p>
     * <p>Pre-requisites:
     * 1. Install 7testtool application
     * 2. Create suite for 7testtool with 4 HTTP requests and 4 responses with different body.
     * 3. On PMS set related policy for com.seven.asimov.test.tool:
     * asimov@application@com.seven.asimov.test.tool@preemptive@*@80:443@detection_time=10
     * asimov@application@com.seven.asimov.test.tool@preemptive@*@80:443@cooldown_time=110
     * 4. Use mobile network connection
     * 5. Install OC
     * 6. Start logcat
     * 7. Start TCPDUMP
     * </p>
     * <p>Steps:
     * 1. Validate preemptive policy applying
     * 2. Send request using 7testtool
     * 3. Send 2nd request after 6seconds
     * 4. Send 3rd request after 30seconds
     * 5. Send 4th request after 90seconds
     * 6. Observe logcat
     * </p>
     * <p>Expected reults:
     * 1. Correct PMS policies were downloaded;
     * Preemptive configuration for 7testtool application was applied with correct properties:
     * 'Updating configuration for app_uid 10206';
     * 'Preemptive configuration updated to ip_number=0, port_range='80:443', detection_time=10, cooldown_time=110'
     * PSC was generated for 7testtool application with correct properties:
     * '[PSC] UID: 10206, cooldown_time: 110, detection_time: 10, ports: [80:443]'
     * 2. After first transaction radio state is 'radio is CONNECTED';
     * TCPDUMP includes [RST, ACK] packet sent by OC from the netport to server to close OUT socket;
     * OUT socket was closed with 'Closing all OUT connections' and NetLog should include operate type:'preemptive_socket_closure' in logcat;
     * detection of next transaction was started with 'entered Detection Interval for 10 seconds' in logcat
     * 3. After second transaction OUT socket should remain open: no [RST, ACK] packet sent by OC from the netport to server in TCPDUMP and no 'Closing all OUT connections' in logcat
     * Cooldown interval was started with 'entered into CoolDown Interval for 110 seconds' in logcat
     * 4. After third transaction OUT socket should remain open: no [RST, ACK] packet sent by OC from the netport to server in TCPDUMP and no 'Closing all OUT connections' in logcat
     * Cooldown interval remains active with 'CoolDown Interval in action' in logcat
     * 5.  After fourth transaction radio state is 'radio is CONNECTED';
     * TCPDUMP includes [RST, ACK] packet sent by OC from the netport to server to close OUT socket;
     * OUT socket was closed with 'Closing all OUT connections' and NetLog should include operate type:'preemptive_socket_closure' in logcat;
     * detection of next transaction was started with 'entered Detection Interval for 10 seconds' in logcat
     * </p>
     *
     * @throws Throwable
     */
    public void test_003_OutSocketClosure() throws Throwable {
        banNetworkForAllApp(true);
        mobileNetworkUtil.onWifiOnly();
        PrepareResourceUtil.prepareResource(uri, false);
        PreemptiveTask preemptiveIpTask = new PreemptiveTask();
        PreemptiveTask preemptiveTask = new PreemptiveTask("80:443", "110", "10");
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), preemptiveIpTask, preemptiveTask);
        try {
            logcatUtil.start();
            PMSUtil.addPolicies(new Policy[]{new Policy("detection_time", "10", "@asimov@application@com.seven.asimov.it@preemptive@*@80:443", true),
                    new Policy("cooldown_time", "110", "@asimov@application@com.seven.asimov.it@preemptive@*@80:443", true)
            });
            TestUtil.sleep(2 * 60 * 1000);
            mobileNetworkUtil.on3gOnly();
            TestUtil.sleep(15 * 1000);
            logcatUtil.stop();
            assertTrue("Preemptive configuration for application was applied with correct properties",
                    preemptiveIpTask.getLogEntries().size() > 0 && preemptiveTask.getLogEntries().size() > 1);
            logger.info("Transaction #1");
            checkSocketClosure(10, 110, true);
            TestUtil.sleep(6 * 1000 - getDuration());
            logger.info("Transaction #2");
            checkSocketClosure(10, 110, false, true, true);
            TestUtil.sleep(30 * 1000 - getDuration());
            logger.info("Transaction #3");
            checkSocketClosure(10, 110, false, false, true);
            TestUtil.sleep(90 * 1000 - getDuration());
            logger.info("Transaction #4");
            checkSocketClosure(10, 110, true);
            TestUtil.sleep(3 * 1000);
        } finally {
            PMSUtil.cleanPaths(new String[]{"@asimov@application@com.seven.asimov.it@preemptive@*@80:443"});
            logcatUtil.stop();
            PrepareResourceUtil.prepareResource(uri, true);
            banNetworkForAllApp(false);
        }
    }

    /**
     * <p>Preemptive cycle without response during cooldown
     * </p>
     * <p>Pre-requisites:
     * 1. Install 7testtool application
     * 2. Create suite for 7testtool with 4 HTTP requests and 4 responses with different body.
     * 3. On PMS set related policy for com.seven.asimov.test.tool:
     * asimov@application@com.seven.asimov.test.tool@preemptive@*@80:443@detection_time=10
     * asimov@application@com.seven.asimov.test.tool@preemptive@*@80:443@cooldown_time=110
     * 4. Use mobile network connection
     * 5. Install OC
     * 6. Start logcat
     * 7. Start TCPDUMP
     * </p>
     * <p>Steps:
     * 1. Validate preemptive policy applying
     * 2. Send request using 7testtool
     * 3. Send 2nd request after 6seconds
     * 4. Wait for 120 seconds
     * 5. Observe logcat
     * </p>
     * <p>Expected reults:
     * 1. Correct PMS policies were downloaded;
     * Preemptive configuration for 7testtool application was applied with correct properties:
     * 'Updating configuration for app_uid 10206';
     * 'Preemptive configuration updated to ip_number=0, port_range='80:443', detection_time=10, cooldown_time=110'
     * PSC was generated for 7testtool application with correct properties:
     * '[PSC] UID: 10206, cooldown_time: 110, detection_time: 10, ports: [80:443]'
     * 2. After first transaction radio state is 'radio is CONNECTED';
     * TCPDUMP includes [RST, ACK] packet sent by OC from the netport to server to close OUT socket;
     * OUT socket was closed with 'Closing all OUT connections' and NetLog should include operate type:'preemptive_socket_closure' in logcat;
     * detection of next transaction was started with 'entered Detection Interval for 10 seconds' in logcat
     * 3. After second transaction OUT socket should remain open: no [RST, ACK] packet sent by OC from the netport to server in TCPDUMP and no 'Closing all OUT connections' in logcat
     * Cooldown interval was started with 'entered into CoolDown Interval for 110 seconds' in logcat
     * 4. After 120 seconds OUT socket should remain open: no [RST, ACK] packet sent by OC from the netport to server in TCPDUMP and no 'Closing all OUT connections' in logcat
     * </p>
     *
     * @throws Throwable
     */
    public void test_004_OutSocketClosure() throws Throwable {
        banNetworkForAllApp(true);
        mobileNetworkUtil.onWifiOnly();
        PrepareResourceUtil.prepareResource(uri, false);
        PreemptiveTask preemptiveIpTask = new PreemptiveTask();
        PreemptiveTask preemptiveTask = new PreemptiveTask("80:443", "110", "10");
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), preemptiveIpTask, preemptiveTask);
        try {
            logcatUtil.start();
            PMSUtil.addPolicies(new Policy[]{new Policy("detection_time", "10", "@asimov@application@com.seven.asimov.it@preemptive@*@80:443", true),
                    new Policy("cooldown_time", "110", "@asimov@application@com.seven.asimov.it@preemptive@*@80:443", true)
            });
            TestUtil.sleep(2 * 60 * 1000);
            mobileNetworkUtil.on3gOnly();
            TestUtil.sleep(15 * 1000);
            logcatUtil.stop();
            assertTrue("Preemptive configuration for application was applied with correct properties",
                    preemptiveIpTask.getLogEntries().size() > 0 && preemptiveTask.getLogEntries().size() > 1);
            logger.info("Transaction #1");
            checkSocketClosure(10, 110, true);
            TestUtil.sleep(6 * 1000 - getDuration());
            logger.info("Transaction #2");
            checkSocketClosure(10, 110, false, true, true);
            long startTime = System.currentTimeMillis();
            TestUtil.sleep(60 * 1000 - getDuration() * 2);
            long endTime = System.currentTimeMillis();
            assertFalse("After 60 seconds OUT socket should remain open: no [RST, ACK] packet sent", isRstSent(startTime, endTime));
            TestUtil.sleep(5 * 1000);
        } finally {
            PMSUtil.cleanPaths(new String[]{"@asimov@application@com.seven.asimov.it@preemptive@*@80:443"});
            logcatUtil.stop();
            PrepareResourceUtil.prepareResource(uri, true);
            banNetworkForAllApp(false);
        }
    }

    /**
     * <p>Preemptive cycles in sequence
     * </p>
     * <p>Pre-requisites:
     * 1. Install 7testtool application
     * 2. Create suite for 7testtool with 7 HTTP requests and 7 responses with different body.
     * 3. On PMS set related policy for com.seven.asimov.test.tool:
     * asimov@application@com.seven.asimov.test.tool@preemptive@*@80:443@detection_time=10
     * asimov@application@com.seven.asimov.test.tool@preemptive@*@80:443@cooldown_time=110
     * 4. Use mobile network connection
     * 5. Install OC
     * 6. Start logcat
     * 7. Start TCPDUMP
     * </p>
     * <p>Steps:
     * 1. Validate preemptive policy applying
     * 2. Send request using 7testtool
     * 3. Send 2nd request after 6seconds
     * 4. Send 3rd request after 30seconds
     * 5. Send 4th request after 90seconds
     * 6. Send 5nd request after 6seconds
     * 7. Send 6rd request after 30seconds
     * 8. Send 7th request after 90seconds
     * 9. Observe logcat
     * </p>
     * <p>Expected reults:
     * 1. Correct PMS policies were downloaded;
     * Preemptive configuration for 7testtool application was applied with correct properties:
     * 'Updating configuration for app_uid 10206';
     * 'Preemptive configuration updated to ip_number=0, port_range='80:443', detection_time=10, cooldown_time=110'
     * PSC was generated for 7testtool application with correct properties:
     * '[PSC] UID: 10206, cooldown_time: 110, detection_time: 10, ports: [80:443]'
     * 2. After first transaction radio state is 'radio is CONNECTED';
     * TCPDUMP includes [RST, ACK] packet sent by OC from the netport to server to close OUT socket;
     * OUT socket was closed with 'Closing all OUT connections' and NetLog should include operate type:
     * 'preemptive_socket_closure' in logcat;
     * detection of next transaction was started with 'entered Detection Interval for 10 seconds' in logcat
     * 3. After second transaction OUT socket should remain open: no [RST, ACK] packet sent by OC from the netport to
     * server in TCPDUMP and no 'Closing all OUT connections' in logcat
     * Cooldown interval was started with 'entered into CoolDown Interval for 110 seconds' in logcat
     * 4. After third transaction OUT socket should remain open: no [RST, ACK] packet sent by OC from the netport to
     * server in TCPDUMP and no 'Closing all OUT connections' in logcat
     * Cooldown interval remains active with 'CoolDown Interval in action' in logcat
     * 5.  After fourth transaction radio state is 'radio is CONNECTED';
     * TCPDUMP includes [RST, ACK] packet sent by OC from the netport to server to close OUT socket;
     * OUT socket was closed with 'Closing all OUT connections' and NetLog should include operate type:
     * 'preemptive_socket_closure' in logcat;
     * detection of next transaction was started with 'entered Detection Interval for 10 seconds' in logcat
     * 6. After fifth transaction OUT socket should remain open: no [RST, ACK] packet sent by OC from the netport to
     * server in TCPDUMP and no 'Closing all OUT connections' in logcat
     * Coldown interval was started with 'entered into CoolDown Interval for 110 seconds' in logcat
     * 7. After sixth transaction OUT socket should remain open: no [RST, ACK] packet sent by OC from the netport to
     * server in TCPDUMP and no 'Closing all OUT connections' in logcat
     * Cooldown interval remains active with 'CoolDown Interval in action' in logcat
     * 8.  After seventh transaction radio state is 'radio is CONNECTED';
     * TCPDUMP includes [RST, ACK] packet sent by OC from the netport to server to close OUT socket;
     * OUT socket was closed with 'Closing all OUT connections' and NetLog should include operate type:
     * 'preemptive_socket_closure' in logcat;
     * detection of next transaction was started with 'entered Detection Interval for 10 seconds' in logcat
     * </p>
     *
     * @throws Throwable
     */
    public void test_005_OutSocketClosure() throws Throwable {
        banNetworkForAllApp(true);
        mobileNetworkUtil.onWifiOnly();
        PrepareResourceUtil.prepareResource(uri, false);
        PreemptiveTask preemptiveIpTask = new PreemptiveTask();
        PreemptiveTask preemptiveTask = new PreemptiveTask("80:443", "110", "10");
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), preemptiveIpTask, preemptiveTask);
        try {
            logcatUtil.start();
            PMSUtil.addPolicies(new Policy[]{new Policy("detection_time", "10", "@asimov@application@com.seven.asimov.it@preemptive@*@80:443", true),
                    new Policy("cooldown_time", "110", "@asimov@application@com.seven.asimov.it@preemptive@*@80:443", true)
            });
            TestUtil.sleep(2 * 60 * 1000);
            mobileNetworkUtil.on3gOnly();
            TestUtil.sleep(15 * 1000);
            logcatUtil.stop();
            assertTrue("Preemptive configuration for application was applied with correct properties",
                    preemptiveIpTask.getLogEntries().size() > 0 && preemptiveTask.getLogEntries().size() > 1);
            logger.info("Transaction #1");
            checkSocketClosure(10, 110, true);
            TestUtil.sleep(6 * 1000 - getDuration());
            logger.info("Transaction #2");
            checkSocketClosure(10, 110, false, true, true);
            TestUtil.sleep(30 * 1000 - getDuration());
            logger.info("Transaction #3");
            checkSocketClosure(10, 110, false, false, true);
            TestUtil.sleep(90 * 1000 - getDuration());
            logger.info("Transaction #4");
            checkSocketClosure(10, 110, true);
            TestUtil.sleep(6 * 1000 - getDuration());
            logger.info("Transaction #5");
            checkSocketClosure(10, 110, false, true, true);
            TestUtil.sleep(30 * 1000 - getDuration());
            logger.info("Transaction #6");
            checkSocketClosure(10, 110, false, false, true);
            TestUtil.sleep(90 * 1000 - getDuration());
            logger.info("Transaction #7");
            checkSocketClosure(10, 110, true);
            TestUtil.sleep(3 * 1000);
        } finally {
            PMSUtil.cleanPaths(new String[]{"@asimov@application@com.seven.asimov.it@preemptive@*@80:443"});
            logcatUtil.stop();
            PrepareResourceUtil.prepareResource(uri, true);
            banNetworkForAllApp(false);
        }
    }

    /**
     * <p>Preemptive cycle interruption
     * </p>
     * <p>Pre-requisites:
     * 1. Install 7testtool application
     * 2. Create suite for 7testtool with 2 HTTP requests and 2 responses with different body.
     * 3. On PMS set related policy for com.seven.asimov.test.tool:
     * asimov@application@com.seven.asimov.test.tool@preemptive
     * asimov@application@com.seven.asimov.test.tool@preemptive@*@80:443@detection_time=10
     * asimov@application@com.seven.asimov.test.tool@preemptive@*@80:443@cooldown_time=210
     * 4. Use mobile network connection
     * 5. Install OC
     * 6. Start logcat
     * 7. Start TCPDUMP
     * </p>
     * <p>Steps:
     * 1. Validate preemptive policy applying
     * 2. Send request using 7testtool
     * 3. Send 2nd request after 6seconds
     * 4. Update PMS settings to disable OC: @asimov@enabled=0
     * 5. Observe logcat
     * </p>
     * <p>Expected reults:
     * 1. Correct PMS policies were downloaded;
     * Preemptive configuration for 7testtool application was applied with correct properties:
     * 'Updating configuration for app_uid 10206';
     * 'Preemptive configuration updated to ip_number=0, port_range='80:443', detection_time=10, cooldown_time=210'
     * PSC was generated for 7testtool application with correct properties:
     * '[PSC] UID: 10206, cooldown_time: 210, detection_time: 10, ports: [80:443]'
     * 2. After first transaction radio state is 'radio is CONNECTED';
     * TCPDUMP includes [RST, ACK] packet sent by OC from the netport to server to close OUT socket;
     * OUT socket was closed with 'Closing all OUT connections' and NetLog should include operate type:
     * 'preemptive_socket_closure' in logcat;
     * detection of next transaction was started with 'entered Detection Interval for 10 seconds' in logcat
     * 3. After second transaction OUT socket should remain open: no [RST, ACK] packet sent by OC from the netport to
     * server in TCPDUMP and no 'Closing all OUT connections' in logcat
     * Cooldown interval was started with 'entered into CoolDown Interval for 210 seconds' in logcat
     * 4. After policy updating preemptive cycle is interrupted because OC became disabled;
     * OUT socket should remain open
     * </p>
     *
     * @throws Throwable
     */
    public void test_006_OutSocketClosure() throws Throwable {
        banNetworkForAllApp(true);
        mobileNetworkUtil.onWifiOnly();
        PrepareResourceUtil.prepareResource(uri, false);
        PreemptiveTask preemptiveIpTask = new PreemptiveTask();
        PreemptiveTask preemptiveTask = new PreemptiveTask("80:443", "210", "10");
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), preemptiveIpTask, preemptiveTask);
        try {
            logcatUtil.start();
            PMSUtil.addPolicies(new Policy[]{new Policy("detection_time", "10", "@asimov@application@com.seven.asimov.it@preemptive@*@80:443", true),
                    new Policy("cooldown_time", "210", "@asimov@application@com.seven.asimov.it@preemptive@*@80:443", true)
            });
            TestUtil.sleep(2 * 60 * 1000);
            mobileNetworkUtil.on3gOnly();
            TestUtil.sleep(15 * 1000);
            logcatUtil.stop();
            assertTrue("Preemptive configuration for application was applied with correct properties",
                    preemptiveIpTask.getLogEntries().size() > 0 && preemptiveTask.getLogEntries().size() > 1);
            logger.info("Transaction #1");
            checkSocketClosure(10, 210, true);
            TestUtil.sleep(6 * 1000 - getDuration());
            logger.info("Transaction #2");
            checkSocketClosure(10, 210, false, true, true);
            PMSUtil.addPolicies(new Policy[]{new Policy("enabled", "0", "@asimov", true)});
            TestUtil.sleep(2 * 60 * 1000);
            logger.info("Transaction #3");
            HttpRequest request = createRequest().setUri(uri).setMethod("GET").addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("X-OC-ResponseContent", "" + Math.random() * 1000).getRequest();
            setStartTime(System.currentTimeMillis());
            sendRequest(request, true);
            setEndTime(System.currentTimeMillis());
            assertFalse("OUT socket should remain open", isRstSent(getStartTime(), getEndTime()));
            TestUtil.sleep(3 * 1000);
        } finally {
            PMSUtil.cleanPaths(new String[]{"@asimov@application@com.seven.asimov.it@preemptive@*@80:443", "@asimov"});
            logcatUtil.stop();
            PrepareResourceUtil.prepareResource(uri, true);
            banNetworkForAllApp(false);
        }
    }

    /**
     * <p>Full preemptive cycle for HTTPS transactions
     * </p>
     * <p>Pre-requisites:
     * 1. Install 7testtool application
     * 2. Create suite for 7testtool with 4 HTTP requests and 4 responses with different body.
     * 3. On PMS set related policy for com.seven.asimov.test.tool:
     * asimov@application@com.seven.asimov.test.tool@preemptive@*@443@detection_time=10
     * asimov@application@com.seven.asimov.test.tool@preemptive@*@443@cooldown_time=110
     * 4. Use mobile network connection
     * 5. Install OC
     * 6. Start logcat
     * 7. Start TCPDUMP
     * </p>
     * <p>Steps:
     * 1. Validate preemptive policy applying
     * 2. Modify URI in 7testtool: change 'http' to 'https' to send HTTPS requests
     * 3. Send request using 7testtool
     * 4. Send 2nd request after 6seconds
     * 5. Send 3rd request after 30seconds
     * 6. Send 4th request after 90seconds
     * 7. Observe logcat
     * </p>
     * <p>Expected reults:
     * 1. Correct PMS policies were downloaded;
     * Preemptive configuration for 7testtool application was applied with correct properties:
     * 'Updating configuration for app_uid 10206';
     * 'Preemptive configuration updated to ip_number=0, port_range='443', detection_time=10, cooldown_time=110'
     * PSC was generated for 7testtool application with correct properties:
     * '[PSC] UID: 10206, cooldown_time: 110, detection_time: 10, ports: [443]'
     * 2. After first request secure connection was established successfully
     * 3. After first response radio state is 'radio is CONNECTED';
     * TCPDUMP includes [RST, ACK] packet sent by OC from the netport to server to close OUT socket;
     * OUT socket was closed with 'Closing all OUT connections' and NetLog should include operate type:'preemptive_socket_closure' in logcat;
     * detection of next transaction was started with 'entered Detection Interval for 10 seconds' in logcat
     * 3. After second transaction OUT socket should remain open: no [RST, ACK] packet sent by OC from the netport to server in TCPDUMP and no 'Closing all OUT connections' in logcat
     * Cooldown interval was started with 'entered into CoolDown Interval for 110 seconds' in logcat
     * 4. After third transaction OUT socket should remain open: no [RST, ACK] packet sent by OC from the netport to server in TCPDUMP and no 'Closing all OUT connections' in logcat
     * Cooldown interval remains active with 'CoolDown Interval in action' in logcat
     * 5.  After fourth transaction radio state is 'radio is CONNECTED';
     * TCPDUMP includes [RST, ACK] packet sent by OC from the netport to server to close OUT socket;
     * OUT socket was closed with 'Closing all OUT connections' and NetLog should include operate type:'preemptive_socket_closure' in logcat;
     * detection of next transaction was started with 'entered Detection Interval for 10 seconds' in logcat
     * </p>
     *
     * @throws Throwable
     */
    public void test_007_OutSocketClosure() throws Throwable {
        banNetworkForAllApp(true);
        mobileNetworkUtil.onWifiOnly();
        PrepareResourceUtil.prepareResource(httpsUri, false);
        PreemptiveTask preemptiveIpTask = new PreemptiveTask();
        PreemptiveTask preemptiveTask = new PreemptiveTask("443:0", "110", "15");
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), preemptiveIpTask, preemptiveTask);
        try {
            logcatUtil.start();
            PMSUtil.addPolicies(new Policy[]{new Policy("detection_time", "15", "@asimov@application@com.seven.asimov.it@preemptive@*@443", true),
                    new Policy("cooldown_time", "110", "@asimov@application@com.seven.asimov.it@preemptive@*@443", true)
            });
            TestUtil.sleep(2 * 60 * 1000);
            mobileNetworkUtil.on3gOnly();
            TestUtil.sleep(15 * 1000);
            logcatUtil.stop();
            assertTrue("Preemptive configuration for application was applied with correct properties",
                    preemptiveIpTask.getLogEntries().size() > 0 && preemptiveTask.getLogEntries().size() > 1);
            HttpRequest request = createRequest().setUri(httpsUri).setMethod("GET").addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("X-OC-ResponseContent", "" + Math.random() * 1000).getRequest();
            logger.info("Transaction #1");
            checkSocketClosure(15, 110, true, false, false, request, true);
            TestUtil.sleep(6 * 1000 - getDuration());
            request = createRequest().setUri(httpsUri).setMethod("GET").addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("X-OC-ResponseContent", "" + Math.random() * 1000).getRequest();
            logger.info("Transaction #2");
            checkSocketClosure(15, 110, false, true, true, request, true);
            TestUtil.sleep(30 * 1000 - getDuration());
            request = createRequest().setUri(httpsUri).setMethod("GET").addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("X-OC-ResponseContent", "" + Math.random() * 1000).getRequest();
            logger.info("Transaction #3");
            checkSocketClosure(15, 110, false, false, true, request, true);
            TestUtil.sleep(90 * 1000 - getDuration());
            request = createRequest().setUri(httpsUri).setMethod("GET").addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("X-OC-ResponseContent", "" + Math.random() * 1000).getRequest();
            logger.info("Transaction #4");
            checkSocketClosure(15, 110, true, false, false, request, true);
            TestUtil.sleep(3 * 1000);
        } finally {
            PMSUtil.cleanPaths(new String[]{"@asimov@application@com.seven.asimov.it@preemptive@*@443"});
            logcatUtil.stop();
            PrepareResourceUtil.prepareResource(httpsUri, true);
            banNetworkForAllApp(false);
        }
    }

    /**
     * <p>Application port is not matched port range settings. Preemptive cycle was not started
     * </p>
     * <p>Pre-requisites:
     * 1. Install 7testtool application
     * 2. Create suite for 7testtool with 2 HTTP requests and 2 responses with different body.
     * 3. On PMS set related policy for com.seven.asimov.test.tool:
     * asimov@application@com.seven.asimov.test.tool@preemptive@*@443@detection_time=10
     * asimov@application@com.seven.asimov.test.tool@preemptive@*@443@cooldown_time=110
     * 4. Use mobile network connection
     * 5. Install OC
     * 6. Start logcat
     * 7. Start TCPDUMP
     * </p>
     * <p>Steps:
     * 1. Validate preemptive policy applying
     * 2. Send request using 7testtool
     * 3. Send 2nd request after 15seconds
     * 4. Observe logcat
     * </p>
     * <p>Expected reults:
     * 1. Correct PMS policies were downloaded;
     * Preemptive configuration for 7testtool application was applied with correct properties:
     * 'Updating configuration for app_uid 10206';
     * 'Preemptive configuration updated to ip_number=0, port_range='443', detection_time=10, cooldown_time=110'
     * PSC was generated for 7testtool application with correct properties:
     * '[PSC] UID: 10206, cooldown_time: 110, detection_time: 10, ports: [443]'
     * 2. After first transaction preemptive cycle was not started because port_range='443'
     * did not match application_port='80';
     * OUT socket should remain open: no [RST, ACK] packet sent by OC from the netport to server in TCPDUMP and
     * no 'Closing all OUT connections' in logcat
     * Detection of next transaction was not started; no 'entered Detection Interval for 10 seconds' in logcat
     * 3. After second transaction preemptive cycle was not started because port_range='443' did not match
     * application_port='80';
     * OUT socket should remain open: no [RST, ACK] packet sent by OC from the netport to server in TCPDUMP and no
     * 'Closing all OUT connections' in logcat
     * Detection of next transaction was not started; no 'entered Detection Interval for 10 seconds' in logcat
     * </p>
     *
     * @throws Throwable
     */
    public void test_008_OutSocketClosure() throws Throwable {
        banNetworkForAllApp(true);
        mobileNetworkUtil.onWifiOnly();
        PrepareResourceUtil.prepareResource(uri, false);
        PreemptiveTask preemptiveIpTask = new PreemptiveTask();
        PreemptiveTask preemptiveTask = new PreemptiveTask("443:0", "110", "10");
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), preemptiveIpTask, preemptiveTask);
        try {
            logcatUtil.start();
            PMSUtil.addPolicies(new Policy[]{new Policy("detection_time", "10", "@asimov@application@com.seven.asimov.it@preemptive@*@443", true),
                    new Policy("cooldown_time", "110", "@asimov@application@com.seven.asimov.it@preemptive@*@443", true)
            });
            TestUtil.sleep(2 * 60 * 1000);
            mobileNetworkUtil.on3gOnly();
            TestUtil.sleep(15 * 1000);
            logcatUtil.stop();
            assertTrue("Preemptive configuration for application was applied with correct properties",
                    preemptiveIpTask.getLogEntries().size() > 0 && preemptiveTask.getLogEntries().size() > 1);
            logger.info("Transaction #1");
            checkSocketClosure(10, 110, false);
            TestUtil.sleep(15 * 1000 - getDuration());
            logger.info("Transaction #2");
            checkSocketClosure(10, 110, false);
            TestUtil.sleep(3 * 1000);
        } finally {
            PMSUtil.cleanPaths(new String[]{"@asimov@application@com.seven.asimov.it@preemptive@*@443"});
            logcatUtil.stop();
            PrepareResourceUtil.prepareResource(uri, true);
            banNetworkForAllApp(false);
        }
    }

    /**
     * <p>Preemptive cycle is not active on Wi-Fi
     * </p>
     * <p>Pre-requisites:
     * 1. Install 7testtool application
     * 2. Create suite for 7testtool with 2 HTTP requests and 2 responses with different body.
     * 3. On PMS set related policy for com.seven.asimov.test.tool:
     * asimov@application@com.seven.asimov.test.tool@preemptive@*@80:443@detection_time=10
     * asimov@application@com.seven.asimov.test.tool@preemptive@*@80:443@cooldown_time=110
     * 4. Use Wi-Fi network connection
     * 5. Install OC
     * 6. Start logcat
     * 7. Start TCPDUMP
     * </p>
     * <p>Steps:
     * 1. Validate preemptive policy applying
     * 2. Send request using 7testtool
     * 3. Send 2nd request after 6seconds
     * 4. Observe logcat
     * </p>
     * <p>Expected reults:
     * 1. Correct PMS policies were downloaded;
     * Preemptive configuration for 7testtool application was applied with correct properties:
     * 'Updating configuration for app_uid 10206';
     * 'Preemptive configuration updated to ip_number=0, port_range='80:443', detection_time=10, cooldown_time=110'
     * PSC was generated for 7testtool application with correct properties:
     * '[PSC] UID: 10206, cooldown_time: 110, detection_time: 10, ports: [80:443]'
     * 2. After first transaction OUT socket should remain open: no [RST, ACK] packet sent by OC from the netport to
     * server in TCPDUMP and no 'Closing all OUT connections' in logcat
     * Detection of next transaction was not started with 'entered Detection Interval for 10 seconds' in logcat
     * 3. After second transaction OUT socket should remain open: no [RST, ACK] packet sent by OC from the netport to
     * server in TCPDUMP and no 'Closing all OUT connections' in logcat
     * Detection of next transaction was not started with 'entered Detection Interval for 10 seconds' in logcat
     * </p>
     *
     * @throws Throwable
     */
    public void test_009_OutSocketClosure() throws Throwable {
        banNetworkForAllApp(true);
        mobileNetworkUtil.onWifiOnly();
        PrepareResourceUtil.prepareResource(uri, false);
        PreemptiveTask preemptiveIpTask = new PreemptiveTask();
        PreemptiveTask preemptiveTask = new PreemptiveTask("80:443", "110", "10");
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), preemptiveIpTask, preemptiveTask);
        try {
            logcatUtil.start();
            PMSUtil.addPolicies(new Policy[]{new Policy("detection_time", "10", "@asimov@application@com.seven.asimov.it@preemptive@*@80:443", true),
                    new Policy("cooldown_time", "110", "@asimov@application@com.seven.asimov.it@preemptive@*@80:443", true)
            });
            TestUtil.sleep(2 * 60 * 1000);
            mobileNetworkUtil.on3gOnly();
            mobileNetworkUtil.onWifiOnly();
            logcatUtil.stop();
            assertTrue("Preemptive configuration for application was applied with correct properties",
                    preemptiveIpTask.getLogEntries().size() > 0 && preemptiveTask.getLogEntries().size() > 1);
            logger.info("Transaction #1");
            checkSocketClosure(10, 110, false);
            TestUtil.sleep(6 * 1000 - getDuration());
            logger.info("Transaction #2");
            checkSocketClosure(10, 110, false);
            TestUtil.sleep(3 * 1000);
        } finally {
            PMSUtil.cleanPaths(new String[]{"@asimov@application@com.seven.asimov.it@preemptive@*@80:443"});
            logcatUtil.stop();
            PrepareResourceUtil.prepareResource(uri, true);
            banNetworkForAllApp(false);
        }
    }

    /**
     * <p>Preemptive cycle is not active in transparent mode
     * </p>
     * <p>Pre-requisites:
     * 1. Install 7testtool application
     * 2. Create suite for 7testtool with 2 HTTP requests and 2 responses with different body.
     * 3. On PMS set related policy for com.seven.asimov.test.tool:
     * asimov@application@com.seven.asimov.test.tool@preemptive@*@80:443@detection_time=10
     * asimov@application@com.seven.asimov.test.tool@preemptive@*@80:443@cooldown_time=110
     * On PMS turn on transparent mode:
     * asimov@transparent=1
     * 4. Use Wi-Fi network connection
     * 5. Install OC
     * 6. Start logcat
     * 7. Start TCPDUMP
     * </p>
     * <p>Steps:
     * 1. Validate preemptive policy applying
     * 2. Send request using 7testtool
     * 3. Send 2nd request after 6seconds
     * 4. Observe logcat
     * </p>
     * <p>Expected reults:
     * 1. Correct PMS policies were downloaded;
     * Preemptive configuration for 7testtool application was applied with correct properties:
     * 'Updating configuration for app_uid 10206';
     * 'Preemptive configuration updated to ip_number=0, port_range='80:443', detection_time=10, cooldown_time=110'
     * PSC was generated for 7testtool application with correct properties:
     * '[PSC] UID: 10206, cooldown_time: 110, detection_time: 10, ports: [80:443]'
     * 2. After first transaction OUT socket should remain open: no [RST, ACK] packet sent by OC from the netport to
     * server in TCPDUMP and no 'Closing all OUT connections' in logcat
     * Detection of next transaction was not started with 'entered Detection Interval for 10 seconds' in logcat
     * 3. After second transaction OUT socket should remain open: no [RST, ACK] packet sent by OC from the netport to
     * server in TCPDUMP and no 'Closing all OUT connections' in logcat
     * Detection of next transaction was not started with 'entered Detection Interval for 10 seconds' in logcat
     * </p>
     *
     * @throws Throwable
     */
    public void test_010_OutSocketClosure() throws Throwable {
        banNetworkForAllApp(true);
        mobileNetworkUtil.onWifiOnly();
        PrepareResourceUtil.prepareResource(uri, false);
        PreemptiveTask preemptiveIpTask = new PreemptiveTask();
        PreemptiveTask preemptiveTask = new PreemptiveTask("80:443", "110", "10");
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), preemptiveIpTask, preemptiveTask);
        try {
            logcatUtil.start();
            PMSUtil.addPolicies(new Policy[]{new Policy("detection_time", "10", "@asimov@application@com.seven.asimov.it@preemptive@*@80:443", true),
                    new Policy("cooldown_time", "110", "@asimov@application@com.seven.asimov.it@preemptive@*@80:443", true),
                    new Policy("transparent", "1", "@asimov", true)
            });
            TestUtil.sleep(2 * 60 * 1000);
            mobileNetworkUtil.on3gOnly();
            mobileNetworkUtil.onWifiOnly();
            logcatUtil.stop();
            assertTrue("Preemptive configuration for application was applied with correct properties",
                    preemptiveIpTask.getLogEntries().size() > 0 && preemptiveTask.getLogEntries().size() > 1);
            logger.info("Transaction #1");
            checkSocketClosure(10, 110, false);
            TestUtil.sleep(6 * 1000 - getDuration());
            logger.info("Transaction #2");
            checkSocketClosure(10, 110, false);
            TestUtil.sleep(3 * 1000);
        } finally {
            PMSUtil.cleanPaths(new String[]{"@asimov@application@com.seven.asimov.it@preemptive@*@80:443", "@asimov"});
            logcatUtil.stop();
            PMSUtil.addPolicies(new Policy[]{new Policy("transparent", "0", "@asimov", true)});
            TestUtil.sleep(60 * 1000);
            PrepareResourceUtil.prepareResource(uri, true);
            banNetworkForAllApp(false);
        }
    }

    /**
     * <p>Application force stop during preemptive cycle
     * </p>
     * <p>Pre-requisites:
     * 1. Install 7testtool application
     * 2. Create suite for 7testtool with 2 HTTP requests and 2 responses with different body.
     * 3. On PMS set related policy for com.seven.asimov.test.tool:
     * asimov@application@com.seven.asimov.test.tool@preemptive
     * asimov@application@com.seven.asimov.test.tool@preemptive@*@80:443@detection_time=10
     * asimov@application@com.seven.asimov.test.tool@preemptive@*@80:443@cooldown_time=110
     * 4. Use mobile network connection
     * 5. Install OC
     * 6. Start logcat
     * 7. Start TCPDUMP
     * </p>
     * <p>Steps:
     * 1. Validate preemptive policy applying
     * 2. Send request using 7testtool
     * 3. Send 2nd request after 6seconds
     * 4. Go to device settings to force stop 7testtool application
     * 5. Observe logcat
     * </p>
     * <p>Expected reults:
     * 1. Correct PMS policies were downloaded;
     * Preemptive configuration for 7testtool application was applied with correct properties:
     * 'Updating configuration for app_uid 10206';
     * 'Preemptive configuration updated to ip_number=0, port_range='80:443', detection_time=10, cooldown_time=110'
     * PSC was generated for 7testtool application with correct properties:
     * '[PSC] UID: 10206, cooldown_time: 110, detection_time: 10, ports: [80:443]'
     * 2. After first transaction radio state is 'radio is CONNECTED';
     * TCPDUMP includes [RST, ACK] packet sent by OC from the netport to server to close OUT socket;
     * OUT socket was closed with 'Closing all OUT connections' and NetLog should include operate type:
     * 'preemptive_socket_closure' in logcat;
     * detection of next transaction was started with 'entered Detection Interval for 10 seconds' in logcat
     * 3. After second transaction OUT socket should remain open: no [RST, ACK] packet sent by OC from the netport to
     * server in TCPDUMP and no 'Closing all OUT connections' in logcat
     * Cooldown interval was started with 'entered into CoolDown Interval for 110 seconds' in logcat
     * 4. After 7testtool's force stop  radio state is 'radio is CONNECTED';
     * TCPDUMP includes [FIN, ACK] packet sent by OC from localport to server and [RST, ACK] packet sent by OC from the
     * netport to server to close OUT socket
     * </p>
     *
     * @throws Throwable
     */
    public void test_011_OutSocketClosure() throws Throwable {
        banNetworkForAllApp(true);
        mobileNetworkUtil.onWifiOnly();
        PrepareResourceUtil.prepareResource(uri, false);
        PreemptiveTask preemptiveIpTask = new PreemptiveTask();
        PreemptiveTask preemptiveTask = new PreemptiveTask("80:443", "110", "10");
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), preemptiveIpTask, preemptiveTask);
        try {
            logcatUtil.start();
            PMSUtil.addPolicies(new Policy[]{new Policy("detection_time", "10", "@asimov@application@com.seven.asimov.it@preemptive@*@80:443", true),
                    new Policy("cooldown_time", "110", "@asimov@application@com.seven.asimov.it@preemptive@*@80:443", true)
            });
            TestUtil.sleep(2 * 60 * 1000);
            mobileNetworkUtil.on3gOnly();
            TestUtil.sleep(15 * 1000);
            logcatUtil.stop();
            assertTrue("Preemptive configuration for application was applied with correct properties",
                    preemptiveIpTask.getLogEntries().size() > 0 && preemptiveTask.getLogEntries().size() > 1);
            logger.info("Transaction #1");
            checkSocketClosure(10, 110, true);
            TestUtil.sleep(6 * 1000 - getDuration());
            logger.info("Transaction #2");
            checkSocketClosure(10, 110, false, true, true);
            logger.info("Transaction #3");
            HttpRequest request = createRequest().setUri(uri).setMethod("GET").addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("X-OC-ResponseContent", "" + Math.random() * 1000).getRequest();
            checkSocketClosure(10, 110, true, false, false, request, false, true);
            TestUtil.sleep(3 * 1000);
        } finally {
            PMSUtil.cleanPaths(new String[]{"@asimov@application@com.seven.asimov.it@preemptive@*@80:443"});
            logcatUtil.stop();
            PrepareResourceUtil.prepareResource(uri, true);
            banNetworkForAllApp(false);
        }
    }

    /**
     * <p>Deferred close preemptive cycle
     * </p>
     * <p>Pre-requisites:
     * 1. Install 7testtool application
     * 2. Create suite for 7testtool with6 HTTP requests, 4 responses with the same body for Polling
     * 3. On PMS set related policy for com.seven.asimov.test.tool:
     * asimov@application@com.seven.asimov.test.tool@preemptive@*@80:443@detection_time=20
     * asimov@application@com.seven.asimov.test.tool@preemptive@*@80:443@cooldown_time=15
     * 4. Use mobile network connection
     * 5. Install OC
     * 6. Start logcat
     * 7. Start TCPDUMP
     * </p>
     * <p>Steps:
     * 1. Validate preemptive policy applying
     * 2. Send request using 7testtool
     * 3. Send 2nd request after 10seconds
     * 4. Send 3rd request after 10seconds
     * 5. Send 4th request after 10seconds
     * 6. Change Radio to UP
     * 7. Observe logcat
     * </p>
     * <p>Expected reults:
     * 1. Correct PMS policies were downloaded;
     * Preemptive configuration for 7testtool application was applied with correct properties:
     * 'Updating configuration for app_uid 10206';
     * 'Preemptive configuration updated to ip_number=0, port_range='80:443', detection_time=20, cooldown_time=15'
     * PSC was generated for 7testtool application with correct properties:
     * '[PSC] UID: 10206, cooldown_time: 25, detection_time: 20, ports: [80:443]'
     * 2. After first transaction radio state is 'radio is CONNECTED';
     * TCPDUMP includes [RST, ACK] packet sent by OC from the netport to server to close OUT socket;
     * OUT socket was closed with 'Closing all OUT connections' and NetLog should include operate type:
     * 'preemptive_socket_closure' in logcat;
     * detection of next transaction was started with 'entered Detection Interval for 20 seconds' in logcat
     * 3. After second transaction OUT socket should remain open: no [RST, ACK] packet sent by OC from the netport to
     * server in TCPDUMP and no 'Closing all OUT connections' in logcat
     * Cooldown interval was started with 'entered into CoolDown Interval for 15 seconds' in logcat
     * 4. After third transaction OUT socket should remain open: no [RST, ACK] packet sent by OC from the netport to
     * server in TCPDUMP and no 'Closing all OUT connections' in logcat
     * Cooldown interval remains active with 'CoolDown Interval in action' in logcat
     * 5. Fourth transaction has verdict HIT because of Polling; Radio is down
     * After fourth transaction OUT socket should remain open: no [RST, ACK] packet sent by OC from the netport to
     * server in TCPDUMP
     * OUT socket was closed with 'Closing all OUT connections' and NetLog should include operate type:
     * 'deferred_preemptive_socket_closure' in logcat;
     * 6. After changing Radio to UP:
     * TCPDUMP includes [RST, ACK] packet sent by OC from the netport to server to close OUT socket;
     * <p/>
     *
     * @throws Throwable
     */
    public void test_012_OutSocketClosure() throws Throwable {
        banNetworkForAllApp(true);
        IpTablesUtil.allowNetworkForApplication(true, csa);
        IpTablesUtil.allowNetworkForApplication(true, csat);

        mobileNetworkUtil.onWifiOnly();
        PrepareResourceUtil.prepareResource(uri, false);
        PreemptiveTask preemptiveIpTask = new PreemptiveTask();
        PreemptiveTask preemptiveTask = new PreemptiveTask("80:443", "15", "20");
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), preemptiveIpTask, preemptiveTask);
        try {
            logcatUtil.start();
            PMSUtil.addPolicies(new Policy[]{new Policy("detection_time", "20", "@asimov@application@com.seven.asimov.it@preemptive@*@80:443", true),
                    new Policy("cooldown_time", "15", "@asimov@application@com.seven.asimov.it@preemptive@*@80:443", true)
            });
            TestUtil.sleep(2 * 60 * 1000);
            mobileNetworkUtil.on3gOnly();
            TestUtil.sleep(20 * 1000);
            logcatUtil.stop();
            assertTrue("Preemptive configuration for application was applied with correct properties",
                    preemptiveIpTask.getLogEntries().size() > 0 && preemptiveTask.getLogEntries().size() > 1);
            HttpRequest request = createRequest().setUri(uri).setMethod("GET").addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("X-OC-ResponseContent", "Body").getRequest();
            logger.info("Transaction #1");
            checkSocketClosure(20, 15, true, false, false, request);
            TestUtil.sleep(10 * 1000 - getDuration());
            logger.info("Transaction #2");
            checkSocketClosure(20, 15, false, true, true, request);
            TestUtil.sleep(10 * 1000 - getDuration());
            logger.info("Transaction #3");
            checkSocketClosure(20, 15, false, false, true, request);
            TestUtil.sleep(15 * 1000 - getDuration());
            logger.info("Transaction #4");
            checkSocketClosure(20, 15, false, false, false, request, false, false, DEFERRED_PREEMPTIVE_SOCKET_CLOSURE);
            TestUtil.sleep(10 * 1000 - getDuration());
            logger.info("Transaction #5");
            changeRadioUP(15);
            TestUtil.sleep(10 * 1000);
            setEndTime(System.currentTimeMillis());
            boolean resultRstSend = isRstSent(getStartTime(), getEndTime());
            assertEquals("[RST, ACK] packet sent by OC: " + resultRstSend + " but expected true", true, resultRstSend);
            TestUtil.sleep(3 * 1000);
        } finally {
            PMSUtil.cleanPaths(new String[]{"@asimov@application@com.seven.asimov.it@preemptive@*@80:443"});
            logcatUtil.stop();
            PrepareResourceUtil.prepareResource(uri, true);
            banNetworkForAllApp(false);
        }
    }

    /**
     * <p>Deferred close initiated by server
     * </p>
     * <p>Pre-requisites:
     * 1. Install 7testtool application
     * 2. Create suite for 7testtool with HTTP requests with such header:
     * 'X-OC-Sleep: 30' and response
     * 3. On PMS set related policy for com.seven.asimov.test.tool:
     * asimov@application@com.seven.asimov.test.tool@preemptive@*@80:443@detection_time=20
     * asimov@application@com.seven.asimov.test.tool@preemptive@*@80:443@cooldown_time=25
     * 4. Use mobile network connection
     * 5. Install OC
     * 6. Start logcat
     * 7. Start TCPDUMP
     * </p>
     * <p>Steps:
     * 1. Validate preemptive policy applying
     * 2. Send request using 7testtool
     * 3. Observe logcat
     * </p>
     * <p>Expected reults:
     * 1. Correct PMS policies were downloaded;
     * Preemptive configuration for 7testtool application was applied with correct properties:
     * 'Updating configuration for app_uid 10206';
     * 'Preemptive configuration updated to ip_number=0, port_range='80:443', detection_time=20, cooldown_time=25'
     * PSC was generated for 7testtool application with correct properties:
     * '[PSC] UID: 10206, cooldown_time: 25, detection_time: 20, ports: [80:443]'
     * 2. After first transaction TCPDUMP includes [FIN, ACK] packet sent by server to close connection;
     * OUT socket was closed with 'Closing all OUT connections' and NetLog should include operate type:
     * 'deferred_preemptive_socket_closure' in logcat;
     * detection of next transaction was started with 'entered Detection Interval for 20 seconds' in logcat
     * </p>
     *
     * @throws Throwable
     */
    public void test_013_OutSocketClosure() throws Throwable {
        banNetworkForAllApp(true);
        mobileNetworkUtil.onWifiOnly();
        PrepareResourceUtil.prepareResource(uri, false);
        PreemptiveTask preemptiveIpTask = new PreemptiveTask();
        PreemptiveTask preemptiveTask = new PreemptiveTask("80:443", "25", "20");
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), preemptiveIpTask, preemptiveTask);
        try {
            logcatUtil.start();
            PMSUtil.addPolicies(new Policy[]{new Policy("detection_time", "20", "@asimov@application@com.seven.asimov.it@preemptive@*@80:443", true),
                    new Policy("cooldown_time", "25", "@asimov@application@com.seven.asimov.it@preemptive@*@80:443", true)
            });
            TestUtil.sleep(2 * 60 * 1000);
            mobileNetworkUtil.on3gOnly();
            TestUtil.sleep(15 * 1000);
            logcatUtil.stop();
            assertTrue("Preemptive configuration for application was applied with correct properties",
                    preemptiveIpTask.getLogEntries().size() > 0 && preemptiveTask.getLogEntries().size() > 1);
            logger.info("Transaction #1");
            HttpRequest request = createRequest().setUri(uri).setMethod("GET").addHeaderField("X-OC-Sleep", "30")
                    .addHeaderField("X-OC-ResponseContent", "" + Math.random() * 1000).getRequest();
            long startTime = System.currentTimeMillis();
            checkSocketClosure(20, 25, false, false, false, request, false, false, DEFERRED_PREEMPTIVE_SOCKET_CLOSURE);
            long endTime = System.currentTimeMillis();
            assertTrue("After first transaction TCPDUMP includes [FIN, ACK] packet", isFinSent(startTime, endTime));
            TestUtil.sleep(3 * 1000);
        } finally {
            PMSUtil.cleanPaths(new String[]{"@asimov@application@com.seven.asimov.it@preemptive@*@80:443"});
            logcatUtil.stop();
            PrepareResourceUtil.prepareResource(uri, true);
            banNetworkForAllApp(false);
        }
    }

    /**
     * <p>Invalid port settings. Preemptive cycle was not started
     * </p>
     * <p>Pre-requisites:
     * 1. Install 7testtool application
     * 2. Create suite for 7testtool with 2 HTTP requests and 2 responses with different body.
     * 3. On PMS set policy with invalid port range for com.seven.asimov.test.tool:
     * asimov@application@com.seven.asimov.test.tool@preemptive@*@80:0@detection_time=10
     * asimov@application@com.seven.asimov.test.tool@preemptive@*@80:0@cooldown_time=110
     * 4. Use mobile network connection
     * 5. Install OC
     * 6. Start logcat
     * 7. Start TCPDUMP
     * </p>
     * <p>Steps:
     * 1. Validate preemptive policy applying
     * 2. Send request using 7testtool
     * 3. Send 2nd request after 15seconds
     * 4. Observe logcat
     * </p>
     * <p>Expected reults:
     * 1. PMS policies were downloaded;
     * Preemptive configuration for 7testtool application wasn't applied because of wrong port values:
     * Updating configuration for app_uid 10206
     * Updating preemptive configuration for app_uid = 10206
     * Adding preemptive ip configuration for '*'
     * Failed to update preemptive port configuration: Invalid preemptive conf: port_from is bigger than port_to
     * - 80:0. Will be ignored...
     * Invalid preemptive data
     * 2. After first transaction preemptive cycle was not started because port_range='80:0' isn't valid
     * OUT socket should remain open: no [RST, ACK] packet sent by OC from the netport to server in TCPDUMP and no
     * 'Closing all OUT connections' in logcat
     * Detection of next transaction was not started; no 'entered Detection Interval for 10 seconds' in logcat
     * 3. After second transaction preemptive cycle was not started  because port_range='80:0' isn't valid
     * OUT socket should remain open: no [RST, ACK] packet sent by OC from the netport to server in TCPDUMP and no
     * 'Closing all OUT connections” in logcat
     * Detection of next transaction was not started; no 'entered Detection Interval for 10 seconds' in logcat
     * </p>
     *
     * @throws Throwable
     */
    public void test_014_OutSocketClosure() throws Throwable {
        banNetworkForAllApp(true);
        mobileNetworkUtil.onWifiOnly();
        PrepareResourceUtil.prepareResource(uri, false);
        PreemptiveTask preemptiveIpTask = new PreemptiveTask();
        PreemptiveTask preemptiveTask = new PreemptiveTask("80:0", "110", "10");
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), preemptiveIpTask, preemptiveTask);
        try {
            logcatUtil.start();
            PMSUtil.addPolicies(new Policy[]{new Policy("detection_time", "10", "@asimov@application@com.seven.asimov.it@preemptive@*@80:0", true),
                    new Policy("cooldown_time", "110", "@asimov@application@com.seven.asimov.it@preemptive@*@80:0", true)
            });
            TestUtil.sleep(2 * 60 * 1000);
            mobileNetworkUtil.on3gOnly();
            TestUtil.sleep(15 * 1000);
            logcatUtil.stop();
            assertFalse("Preemptive configuration for application was applied with correct properties",
                    preemptiveIpTask.getLogEntries().size() > 0 && preemptiveTask.getLogEntries().size() > 1);
            logger.info("Transaction #1");
            checkSocketClosure(10, 110, false);
            TestUtil.sleep(15 * 1000 - getDuration());
            logger.info("Transaction #2");
            checkSocketClosure(10, 110, false);
            TestUtil.sleep(3 * 1000);
        } finally {
            PMSUtil.cleanPaths(new String[]{"@asimov@application@com.seven.asimov.it@preemptive@*@80:0"});
            logcatUtil.stop();
            PrepareResourceUtil.prepareResource(uri, true);
            banNetworkForAllApp(false);
        }
    }

    /**
     * <p>Detection time is more than cooldown time
     * </p>
     * <p>Pre-requisites:
     * 1. Install 7testtool application
     * 2. Create suite for 7testtool with 4 HTTP requests and 4 responses with different body.
     * 3. On PMS set related policy for com.seven.asimov.test.tool:
     * asimov@application@com.seven.asimov.test.tool@preemptive@*@80:443@detection_time=29
     * asimov@application@com.seven.asimov.test.tool@preemptive@*@80:443@cooldown_time=20
     * 4. Use mobile network connection
     * 5. Install OC
     * 6. Start logcat
     * 7. Start TCPDUMP
     * </p>
     * <p>Steps:
     * 1. Validate preemptive policy applying
     * 2. Send request using 7testtool
     * 3. Send 2nd request after 6seconds
     * 4. Send 3rd request after 20seconds
     * 5. Send 4th request after 90seconds
     * 6. Observe logcat
     * </p>
     * <p>Expected reults:
     * 1. Correct PMS policies were downloaded;
     * Preemptive configuration for 7testtool application was applied with correct properties:
     * 'Updating configuration for app_uid 10206';
     * 'Preemptive configuration updated to ip_number=0, port_range='80:443', detection_time=29, cooldown_time=20'
     * PSC was generated for 7testtool application with correct properties:
     * '[PSC] UID: 10206, cooldown_time: 20, detection_time: 29, ports: [80:443]'
     * 2. After first transaction radio state is 'radio is CONNECTED';
     * TCPDUMP includes [RST, ACK] packet sent by OC from the netport to server to close OUT socket;
     * OUT socket was closed with 'Closing all OUT connections' and NetLog should include operate type:
     * 'preemptive_socket_closure' in logcat;
     * detection of next transaction was started with 'entered Detection Interval for 10 seconds' in logcat
     * 3. After second transaction OUT socket should remain open: no [RST, ACK] packet sent by OC from the netport to
     * server in TCPDUMP and no 'Closing all OUT connections' in logcat
     * Cooldown interval was started with 'entered into CoolDown Interval for 20 seconds' in logcat
     * 4. After third transaction OUT socket should remain open: no [RST, ACK] packet sent by OC from the netport to
     * server in TCPDUMP and no 'Closing all OUT connections' in logcat
     * Cooldown interval remains active with 'CoolDown Interval in action' in logcat
     * 5.  After fourth transactionradio state is 'radio is CONNECTED';
     * TCPDUMP includes [RST, ACK] packet sent by OC from the netport to server to close OUT socket;
     * OUT socket was closed with 'Closing all OUT connections' and NetLog should include operate type:
     * 'preemptive_socket_closure' in logcat;
     * detection of next transaction was started with 'entered Detection Interval for 29 seconds' in logcat
     * </p>
     *
     * @throws Throwable
     */
    public void test_015_OutSocketClosure() throws Throwable {
        banNetworkForAllApp(true);
        mobileNetworkUtil.onWifiOnly();
        PrepareResourceUtil.prepareResource(uri, false);
        PreemptiveTask preemptiveIpTask = new PreemptiveTask();
        PreemptiveTask preemptiveTask = new PreemptiveTask("80:443", "20", "29");
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), preemptiveIpTask, preemptiveTask);
        try {
            logcatUtil.start();
            PMSUtil.addPolicies(new Policy[]{new Policy("detection_time", "29", "@asimov@application@com.seven.asimov.it@preemptive@*@80:443", true),
                    new Policy("cooldown_time", "20", "@asimov@application@com.seven.asimov.it@preemptive@*@80:443", true)
            });
            TestUtil.sleep(2 * 60 * 1000);
            mobileNetworkUtil.on3gOnly();
            TestUtil.sleep(15 * 1000);
            logcatUtil.stop();
            assertTrue("Preemptive configuration for application was applied with correct properties",
                    preemptiveIpTask.getLogEntries().size() > 0 && preemptiveTask.getLogEntries().size() > 1);
            logger.info("Transaction #1");
            checkSocketClosure(29, 20, true);
            TestUtil.sleep(6 * 1000 - getDuration());
            logger.info("Transaction #2");
            checkSocketClosure(29, 20, false, true, true);
            TestUtil.sleep(20 * 1000 - getDuration() * 2);
            logger.info("Transaction #3");
            checkSocketClosure(29, 20, false, false, true);
            TestUtil.sleep(90 * 1000 - getDuration());
            logger.info("Transaction #4");
            checkSocketClosure(29, 20, true);
            TestUtil.sleep(3 * 1000);
        } finally {
            PMSUtil.cleanPaths(new String[]{"@asimov@application@com.seven.asimov.it@preemptive@*@80:443"});
            logcatUtil.stop();
            PrepareResourceUtil.prepareResource(uri, true);
            banNetworkForAllApp(false);
        }
    }

    /**
     * <p>Policy updating during preemptive cycle
     * </p>
     * <p>Pre-requisites:
     * 1. Install 7testtool application
     * 2. Create suite for 7testtool with 4 HTTP requests and 4 responses with different body.
     * 3. On PMS set related policy for com.seven.asimov.test.tool:
     * asimov@application@com.seven.asimov.test.tool@preemptive@*@80:443@detection_time=10
     * asimov@application@com.seven.asimov.test.tool@preemptive@*@80:443@cooldown_time=110
     * 4. Use mobile network connection
     * 5. Install OC
     * 6. Start logcat
     * 7. Start TCPDUMP
     * </p>
     * <p>Steps:
     * 1. Validate preemptive policy applying
     * 2. Send request using 7testtool
     * 3. Send 2nd request after 6seconds
     * 4. Make any changes in preemptive configuration for 7testtool on PMS to get PMS policy update
     * 5. Send 3rd request after PMS policy were received
     * 6. Observe logcat
     * </p>
     * <p>Expected reults:
     * 1. Correct PMS policies were downloaded;
     * Preemptive configuration for 7testtool application was applied with correct properties:
     * 'Updating configuration for app_uid 10206';
     * 'Preemptive configuration updated to ip_number=0, port_range='80:443', detection_time=10, cooldown_time=110'
     * PSC was generated for 7testtool application with correct properties:
     * '[PSC] UID: 10206, cooldown_time: 110, detection_time: 10, ports: [80:443]'
     * 2. After first transaction radio state is 'radio is CONNECTED';
     * TCPDUMP includes [RST, ACK] packet sent by OC from the netport to server to close OUT socket;
     * OUT socket was closed with 'Closing all OUT connections' and NetLog should include operate type:
     * 'preemptive_socket_closure' in logcat;
     * detection of next transaction was started with 'entered Detection Interval for 10 seconds' in logcat
     * 3. After second transaction OUT socket should remain open: no [RST, ACK] packet sent by OC from the netport to
     * server in TCPDUMP and no 'Closing all OUT connections' in logcat
     * Cooldown interval was started with 'entered into CoolDown Interval for 110 seconds' in logcat
     * 4. Updated preemptive configuration for 7testtool application was applied correctly;
     * New preemptive cycle should be started after next transaction
     * 5. After third transaction radio state is 'radio is CONNECTED';
     * TCPDUMP includes [RST, ACK] packet sent by OC from the netport to server to close OUT socket;
     * OUT socket was closed with 'Closing all OUT connections' and NetLog should include operate type:
     * 'preemptive_socket_closure' in logcat;
     * detection of next transaction was started with 'entered Detection Interval for 10 seconds' in logcat
     * </p>
     *
     * @throws Throwable
     */
    public void test_016_OutSocketClosure() throws Throwable {
        banNetworkForAllApp(true);
        mobileNetworkUtil.onWifiOnly();
        PrepareResourceUtil.prepareResource(uri, false);
        PreemptiveTask preemptiveIpTask = new PreemptiveTask();
        PreemptiveTask preemptiveTask = new PreemptiveTask("80:443", "110", "10");
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), preemptiveIpTask, preemptiveTask);
        try {
            logcatUtil.start();
            PMSUtil.addPolicies(new Policy[]{new Policy("detection_time", "10", "@asimov@application@com.seven.asimov.it@preemptive@*@80:443", true),
                    new Policy("cooldown_time", "110", "@asimov@application@com.seven.asimov.it@preemptive@*@80:443", true)
            });
            TestUtil.sleep(2 * 60 * 1000);
            mobileNetworkUtil.on3gOnly();
            TestUtil.sleep(15 * 1000);
            logcatUtil.stop();
            assertTrue("Preemptive configuration for application was applied with correct properties",
                    preemptiveIpTask.getLogEntries().size() > 0 && preemptiveTask.getLogEntries().size() > 1);
            logger.info("Transaction #1");
            checkSocketClosure(10, 110, true);
            TestUtil.sleep(6 * 1000 - getDuration());
            logger.info("Transaction #2");
            checkSocketClosure(10, 110, false, true, true);
            PMSUtil.addPolicies(new Policy[]{new Policy("cooldown_time", "120", "@asimov@application@com.seven.asimov.it@preemptive@*@80:443", true)});
            TestUtil.sleep(2 * 60 * 1000);
            checkSocketClosure(10, 120, true);
            TestUtil.sleep(3 * 1000);
        } finally {
            PMSUtil.cleanPaths(new String[]{"@asimov@application@com.seven.asimov.it@preemptive@*@80:443"});
            logcatUtil.stop();
            PrepareResourceUtil.prepareResource(uri, true);
            banNetworkForAllApp(false);
        }
    }

    /**
     * <p>Preemptive cycle with delayed response during detection interval
     * </p>
     * <p>Pre-requisites:
     * 1. Install 7testtool application
     * 2. Create suite for 7testtool with 3 HTTP requests and 3 responses with different body.
     * 3. On PMS set related policy for com.seven.asimov.test.tool:
     * asimov@application@com.seven.asimov.test.tool@preemptive@*@80:443@detection_time=20
     * asimov@application@com.seven.asimov.test.tool@preemptive@*@80:443@cooldown_time=110
     * 4. Use mobile network connection
     * 5. Install OC
     * 6. Start logcat
     * 7. Start TCPDUMP"
     * </p>
     * <p>Steps:
     * 1. Validate preemptive policy applying
     * 2. Send request using 7testtool
     * 3. Send 2nd request with 'Response sleep time = 30seconds' settings in 7testtool after 35 seconds
     * 4. Send 3rd request after 5seconds
     * 5. Observe logcat
     * </p>
     * <p>Expected reults:
     * 1. Correct PMS policies were downloaded;
     * Preemptive configuration for 7testtool application was applied with correct properties:
     * 'Updating configuration for app_uid 10206';
     * 'Preemptive configuration updated to ip_number=0, port_range='80:443', detection_time=20, cooldown_time=110'
     * PSC was generated for 7testtool application with correct properties:
     * '[PSC] UID: 10206, cooldown_time: 110, detection_time: 20, ports: [80:443]'
     * 2. After first transaction radio state is 'radio is CONNECTED';
     * TCPDUMP includes [RST, ACK] packet sent by OC from the netport to server to close OUT socket;
     * OUT socket was closed with 'Closing all OUT connections' and NetLog should include operate type:
     * 'preemptive_socket_closure' in logcat;
     * detection of next transaction was started with 'entered Detection Interval for 20 seconds' in logcat
     * 3. After third request OUT socket should remain open: no [RST, ACK] packet sent by OC from the netport to
     * server in TCPDUMP and no 'Closing all OUT connections' in logcat
     * Cooldown interval was started with 'entered into CoolDown Interval for 110 seconds' in logcat
     * </p>
     *
     * @throws Throwable
     */

    public void test_017_OutSocketClosure() throws Throwable {
        banNetworkForAllApp(true);
        mobileNetworkUtil.onWifiOnly();
        PrepareResourceUtil.prepareResource(uri, false);
        PreemptiveTask preemptiveIpTask = new PreemptiveTask();
        PreemptiveTask preemptiveTask = new PreemptiveTask("80:443", "110", "20");
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), preemptiveIpTask, preemptiveTask);
        try {
            logcatUtil.start();
            PMSUtil.addPolicies(new Policy[]{new Policy("detection_time", "20", "@asimov@application@com.seven.asimov.it@preemptive@*@80:443", true),
                    new Policy("cooldown_time", "110", "@asimov@application@com.seven.asimov.it@preemptive@*@80:443", true)
            });
            TestUtil.sleep(2 * 60 * 1000);
            mobileNetworkUtil.on3gOnly();
            TestUtil.sleep(20 * 1000);
            logcatUtil.stop();
            assertTrue("Preemptive configuration for application was applied with correct properties",
                    preemptiveIpTask.getLogEntries().size() > 0 && preemptiveTask.getLogEntries().size() > 1);
            checkTest(20, 110);
        } finally {
            PMSUtil.cleanPaths(new String[]{"@asimov@application@com.seven.asimov.it@preemptive@*@80:443"});
            logcatUtil.stop();
            PrepareResourceUtil.prepareResource(uri, true);
            banNetworkForAllApp(false);
        }
    }

    public void test_OutSocketClosure_18() throws Throwable {
        for (String aPropertyId : propertyId) {
            try {
                if (aPropertyId != null) {
                    PMSUtil.deleteProperty(aPropertyId);
                }
            } catch (Throwable t) {
                //Ignored
            }
        }
        logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
    }

/*
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
                logger.debug("Test failed due to " + ExceptionUtils.getStackTrace(assertionFailedError));
                isPassed = false;
                counts.add("Test failed due to AssertionFailedError in " + numberOfAttempts + " attempt");
                Thread.sleep(2 * 60 * 1000);
            }
        } while (!isPassed && numberOfAttempts < 3);
        assertTrue("The test was failed three times ", counts.size() != 3);
    }*/
}
