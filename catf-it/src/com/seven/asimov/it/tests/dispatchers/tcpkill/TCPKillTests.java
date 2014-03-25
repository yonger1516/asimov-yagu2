package com.seven.asimov.it.tests.dispatchers.tcpkill;

import com.seven.asimov.it.annotation.Execute;
import com.seven.asimov.it.testcases.TcpKillTestCase;
import com.seven.asimov.it.utils.OCUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.dispatcherTasks.TcpKillTask;
import com.seven.asimov.it.utils.logcat.tasks.failoverTasks.FailoverStartTask;
import com.seven.asimov.it.utils.logcat.tasks.failoverTasks.FailoverStopTask;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import junit.framework.AssertionFailedError;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TCPKillTests extends TcpKillTestCase {
    private static final Logger logger = LoggerFactory.getLogger(TCPKillTests.class.getSimpleName());
    private final String apkPath = "/sdcard" + File.separator + "apk" + File.separator + "asimov-signed.apk";

    @Execute
    public void test_000_Tcpkill() throws Throwable {
        switchNetwork(WIFI_NETWORK);
        PMSUtil.addPolicies(new Policy[]{new Policy("enabled", "false", "@asimov@failovers@restart@engine@enabled", true),
                new Policy("enabled", "false", "@asimov@failovers@restart@controller@enabled", true),
                new Policy("enabled", "false", "@asimov@failovers@restart@dispatchers@enabled", true)});
        TestUtil.sleep(60 * 1000);
    }

    /**
     * <p>Verify that tcpkill is started on reinstall of the client
     * </p>
     * <p>Pre-requisites:
     * 1. Make sure that gtalk, google maps are installed on you device
     * </p>
     * <p>Steps:
     * 1. Install new version of OC client
     * 2. Start tcpdump
     * </p>
     * <p>Expected reults:
     * 1. Make sure from log that tcpkill was started with PID before iptables configuration:
     * 2. Make sure from log that SIGCHLD was received from corresponding PID
     * 3. Make sure from log that tcpkill was killed ports
     * </p>
     *
     * @throws Throwable
     */
    @Execute
    public void test_Tcpkill_01() throws Throwable {
        TcpKillTask tcpKillTask = new TcpKillTask(TcpKillTask.TCP_KILL_REGEX_NUMBER);
        TcpKillTask sigchldTask = new TcpKillTask(TcpKillTask.SIGCHLD_REGEX_NUMBER);
        TcpKillTask portsTask = new TcpKillTask("port 5228");
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), tcpKillTask, sigchldTask, portsTask);
        PMSUtil.addPolicies(new Policy[]{new Policy("interception_ports", "5228", "@asimov@interception@octcpd", true),
                new Policy("tcpkill_ports", "5228", "@asimov@interception@octcpd", true)});
        TestUtil.sleep(2 * 60 * 1000);
        try {
            logcatUtil.start();
            TestUtil.sleep(5 * 1000);
            logger.info("Before installOCClient");
            OCUtil.installOCClientWithVerifying(apkPath);
            logger.info("After installOCClient");
            TestUtil.sleep(60 * 1000);
            logcatUtil.stop();
            checkTest(tcpKillTask, sigchldTask, portsTask, true);
        } finally {
            logcatUtil.stop();
            cleanAllPolicy(new String[]{"@asimov@interception@octcpd"});
        }
    }

    /**
     * <p>Verify that tcpkill is started after OCEngine restart
     * </p>
     * <p>Pre-requisites:
     * 1. OC client should be preinstalled on rooted device
     * 2. Make sure that gtalk, google maps are installed on you device
     * </p>
     * <p>Steps:
     * 1. Start tcpdump
     * 2. Kill OCEngine
     * </p>
     * <p>Expected reults:
     * 1. Make sure from sys logs that tcpkill was started with PID before iptables configuration
     * 2. Make sure from sys logs that SIGCHLD was received from corresponding PID
     * 3. Make sure from log that tcpkill was killed ports
     * </p>
     *
     * @throws Throwable
     */
    @Execute
    public void test_Tcpkill_02() throws Throwable {
        TcpKillTask tcpKillTask = new TcpKillTask(TcpKillTask.TCP_KILL_REGEX_NUMBER);
        TcpKillTask sigchldTask = new TcpKillTask(TcpKillTask.SIGCHLD_REGEX_NUMBER);
        TcpKillTask portsTask = new TcpKillTask("port 5228");
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), tcpKillTask, sigchldTask, portsTask);
        logcatUtil.start();
        PMSUtil.addPolicies(new Policy[]{new Policy("interception_ports", "5228", "@asimov@interception@octcpd", true),
                new Policy("tcpkill_ports", "5228", "@asimov@interception@octcpd", true)});
        TestUtil.sleep(2 * 60 * 1000);
        try {
            logger.info("Before killing OCEngine");
            killProcess("com.seven.asimov", "occ");
            logger.info("After killing OCEngine");
            TestUtil.sleep(3 * 60 * 1000);
            logger.info("3 minutes after killing OCEngine");
            logcatUtil.stop();
            checkTest(tcpKillTask, sigchldTask, portsTask, true);
        } finally {
            logcatUtil.stop();
            cleanAllPolicy(new String[]{"@asimov@interception@octcpd"});
        }

    }

    /**
     * <p>Verify that tcpkill is not started after Dispatcher restart
     * </p>
     * <p>Pre-requisites:
     * 1. OC client should be preinstalled on rooted device
     * 2. Make sure that gtalk, google maps are installed on you device
     * </p>
     * <p>Steps:
     * 1. Start tcpdump
     * 2. Kill TCP dispatcher
     * </p>
     * <p>Expected reults:
     * 1. Make sure from sys logs that dispatchers reconnect is successful and tcpkill was not started
     * 2. Make sure from log that tcpkill was not killed ports
     * </p>
     *
     * @throws Throwable
     */
    @Execute
    public void test_Tcpkill_03() throws Throwable {
        TcpKillTask tcpKillTask = new TcpKillTask(TcpKillTask.TCP_KILL_REGEX_NUMBER);
        TcpKillTask sigchldTask = new TcpKillTask(TcpKillTask.SIGCHLD_REGEX_NUMBER);
        TcpKillTask portsTask = new TcpKillTask("null");
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), tcpKillTask, sigchldTask, portsTask);
        logcatUtil.start();
        try {
            logger.info("Before killing TCP dispatcher");
            killProcess("octcpd");
            logger.info("After killing TCP dispatcher");
            TestUtil.sleep(2 * 60 * 1000);
            logger.info("2 minutes after killing TCP dispatcher");
            logcatUtil.stop();
            checkTest(tcpKillTask, sigchldTask, portsTask, false);
        } finally {
            logcatUtil.stop();
        }

    }

    /**
     * <p>Verify that tcpkill is not started after Dispatcher configuration updated but without real changes
     * </p>
     * <p>Pre-requisites:
     * 1. OC client should be preinstalled on rooted device
     * 2. Make sure that gtalk, google maps are installed on you device
     * </p>
     * <p>Steps:
     * 1. Install new version of OC client
     * 2. Start tcpdump
     * 3. Edit the interception policy on PMS but without real changes
     * </p>
     * <p>Expected reults:
     * 1. Make sure from sys logs that Configure dispatchers is done but as no updates of dispatchers configuration
     * was found so tcpkill was not started
     * 2. Make sure from log that tcpkill was not killed ports
     * </p>
     *
     * @throws Throwable
     */
    @Execute
    public void test_Tcpkill_04() throws Throwable {
        TcpKillTask tcpKillTask = new TcpKillTask(TcpKillTask.TCP_KILL_REGEX_NUMBER);
        TcpKillTask sigchldTask = new TcpKillTask(TcpKillTask.SIGCHLD_REGEX_NUMBER);
        TcpKillTask portsTask = new TcpKillTask("null");
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), tcpKillTask, sigchldTask, portsTask);
        logcatUtil.start();
        try {
            PMSUtil.addPolicies(new Policy[]{new Policy("interception_ports", "value", "@asimov@interception@octcpd", true)});
            TestUtil.sleep(2 * 60 * 1000);
            logcatUtil.stop();
            checkTest(tcpKillTask, sigchldTask, portsTask, false);
        } finally {
            logcatUtil.stop();
            cleanAllPolicy(new String[]{"@asimov@interception@octcpd"});
        }


    }

    /**
     * <p>Verify that tcpkill is started after Dispatcher configuration update
     * </p>
     * <p>Pre-requisites:
     * 1. OC client should be preinstalled on rooted device
     * 2. Make sure that gtalk, google maps are installed on you device
     * <p/>
     * <p>Steps:
     * 1. Install new version of OC client
     * 2. Start tcpdump
     * 3. Update the interception policy on PMS for TCP Dispatcher
     * </p>
     * <p>Expected reults:
     * 1. Make sure from sys logs that tcpkill was started with PID after Configure dispatchers
     * 2. Make sure from sys logs that SIGCHLD was received from corresponding PID
     * 3. Make sure from log that tcpkill was killed ports
     * </p>
     *
     * @throws Throwable
     */
    @Execute
    public void test_Tcpkill_05() throws Throwable {
        TcpKillTask tcpKillTask = new TcpKillTask(TcpKillTask.TCP_KILL_REGEX_NUMBER);
        TcpKillTask sigchldTask = new TcpKillTask(TcpKillTask.SIGCHLD_REGEX_NUMBER);
        TcpKillTask portsTask = new TcpKillTask("port 5228");
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), tcpKillTask, sigchldTask, portsTask);
        logcatUtil.start();
        try {
            PMSUtil.addPolicies(new Policy[]{new Policy("interception_ports", "5228", "@asimov@interception@octcpd", true),
                    new Policy("tcpkill_ports", "5228", "@asimov@interception@octcpd", true)});
            TestUtil.sleep(2 * 60 * 1000);
            logcatUtil.stop();
            checkTest(tcpKillTask, sigchldTask, portsTask, true);
        } finally {
            logcatUtil.stop();
            cleanAllPolicy(new String[]{"@asimov@interception@octcpd"});
        }
    }

    /**
     * <p>Verify that tcpkill is not started on ending of mobile failover
     * </p>
     * <p>Pre-requisites:
     * 1. OC client should be preinstalled on rooted device
     * 2. Make sure that gtalk, google maps are installed on you device
     * </p>
     * <p>Steps:
     * 1. Start tcpdump
     * 2. Make OC start failover on mobile network
     * 3. Make OC stop failover on mobile network
     * </p>
     * <p>Expected reults:
     * 1. Make sure from sys logs that dispatchers reconnect is successful and tcpkill was not started
     * 2. Make sure from log that tcpkill was not killed ports
     * </p>
     *
     * @throws Throwable
     */
    @Execute
    public void test_Tcpkill_06() throws Throwable {
        FailoverStartTask failoverStartTask = new FailoverStartTask();
        FailoverStopTask failoverStopTask = new FailoverStopTask();
        TcpKillTask tcpKillTask = new TcpKillTask(TcpKillTask.TCP_KILL_REGEX_NUMBER);
        TcpKillTask sigchldTask = new TcpKillTask(TcpKillTask.SIGCHLD_REGEX_NUMBER);
        TcpKillTask portsTask = new TcpKillTask("null");
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), failoverStartTask);
        try {
            switchNetwork(WIFI_NETWORK);
            PMSUtil.addPolicies(new Policy[]{new Policy("enabled", "true", MOBILE_NETWORKS_FAILOVER_POLICY_PATH, true),
                    new Policy("actions", "3", MOBILE_NETWORKS_FAILOVER_POLICY_PATH, true),
                    new Policy("attempt_interval", "30", MOBILE_NETWORKS_FAILOVER_POLICY_PATH, true),
                    new Policy("retries", "5", MOBILE_NETWORKS_FAILOVER_POLICY_PATH, true),
                    new Policy("timeout", "3", MOBILE_NETWORKS_FAILOVER_POLICY_PATH, true)});
            Thread.sleep(60 * 1000);
            switchNetwork(MOBILE_NETWORK);
            logcatUtil.start();
            execute(unreacheableRelayServer);
            switchNetwork(WIFI_NETWORK);
            switchNetwork(MOBILE_NETWORK);
            Thread.sleep(20 * 60 * 1000);
            logcatUtil.stop();
            checkStartFailover(true, failoverStartTask, MOBILE_FAILOVER_TYPE);
            logcatUtil = new LogcatUtil(getContext(), failoverStopTask, tcpKillTask, sigchldTask, portsTask);
            logcatUtil.start();
            switchNetwork(WIFI_NETWORK);
            execute(enableRelayServer);
            switchNetwork(MOBILE_NETWORK);
            logcatUtil.stop();
            checkStopFailover(true, failoverStopTask, MOBILE_FAILOVER_TYPE);
            checkTest(tcpKillTask, sigchldTask, portsTask, false);
        } finally {
            logcatUtil.stop();
            execute(enableRelayServer);
            switchNetwork(WIFI_NETWORK);
            cleanAllPolicy(new String[]{MOBILE_NETWORKS_FAILOVER_POLICY_PATH});
        }
    }

    /**
     * <p>Verify that tcpkill is not started after Dispatcher configuration updated with empty value
     * </p>
     * <p>Pre-requisites:
     * 1. OC client should be preinstalled on rooted device
     * 2. Make sure that gtalk, google maps are installed on you device
     * </p>
     * <p>Steps:
     * 1. Install new version of OC client
     * 2. Start tcpdump
     * 3. Edit the interception policy on PMS, value should be empty
     * </p>
     * <p>Expected reults:
     * 1. Make sure from sys logs that client started successfuly and tcpkill was not started
     * 2. Make sure from log that tcpkill was not killed ports
     * </p>
     *
     * @throws Throwable
     */
    @Execute
    public void test_Tcpkill_08() throws Throwable {
        TcpKillTask tcpKillTask = new TcpKillTask(TcpKillTask.TCP_KILL_REGEX_NUMBER);
        TcpKillTask sigchldTask = new TcpKillTask(TcpKillTask.SIGCHLD_REGEX_NUMBER);
        TcpKillTask portsTask = new TcpKillTask("null");
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), tcpKillTask, sigchldTask, portsTask);
        logcatUtil.start();
        try {
            PMSUtil.addPolicies(new Policy[]{new Policy("interception_ports", "", "@asimov@interception@octcpd", true),
                    new Policy("tcpkill_ports", "", "@asimov@interception@octcpd", true)});
            TestUtil.sleep(2 * 60 * 1000);
            logcatUtil.stop();
            checkTest(tcpKillTask, sigchldTask, portsTask, false);
        } finally {
            logcatUtil.stop();
            cleanAllPolicy(new String[]{"@asimov@interception@octcpd"});
        }
    }

    /**
     * <p>Verify that tcpkill is started with port range
     * </p>
     * <p>Pre-requisites:
     * 1. OC client should be preinstalled on rooted device
     * 2. Make sure that gtalk, google maps, facebook are installed on you device
     * </p>
     * <p>Steps:
     * 1. Install new version of OC client
     * 2. Start tcpdump
     * 3. Add port range for tcp_kill policy on PMS
     * 4. Update the interception policy on PMS for TCP Dispatcher
     * </p>
     * <p>Expected reults:
     * 1. Make sure from sys logs that tcpkill was started with PID after Configure dispatchers
     * 2. Make sure from sys logs that SIGCHLD was received from corresponding PID
     * 3. Make sure from log that tcpkill was killed ports
     * </p>
     *
     * @throws Throwable
     */
    @Execute
    public void test_Tcpkill_09() throws Throwable {
        TcpKillTask tcpKillTask = new TcpKillTask(TcpKillTask.TCP_KILL_REGEX_NUMBER);
        TcpKillTask sigchldTask = new TcpKillTask(TcpKillTask.SIGCHLD_REGEX_NUMBER);
        TcpKillTask portsTask = new TcpKillTask("portrange 5224-5228");
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), tcpKillTask, sigchldTask, portsTask);
        logcatUtil.start();
        try {
            PMSUtil.addPolicies(new Policy[]{new Policy("interception_ports", "5224:5228", "@asimov@interception@octcpd", true),
                    new Policy("tcpkill_ports", "5224:5228", "@asimov@interception@octcpd", true)});
            TestUtil.sleep(2 * 60 * 1000);
            logcatUtil.stop();
            checkTest(tcpKillTask, sigchldTask, portsTask, true);
        } finally {
            logcatUtil.stop();
            cleanAllPolicy(new String[]{"@asimov@interception@octcpd"});
        }
    }

    /**
     * <p>Verify that tcpkill is started with list of ports separated by comma
     * </p>
     * <p>Pre-requisites:
     * 1. OC client should be preinstalled on rooted device
     * 2. Make sure that gtalk, google maps, facebook are installed on you device
     * </p>
     * <p>Steps:
     * 1. Install new version of OC client
     * 2. Start tcpdump
     * 3. Add list of ports separated by comma for tcp_kill policy on PMS
     * 4. Update the interception policy on PMS for TCP Dispatcher
     * </p>
     * <p>Expected reults:
     * 1. Make sure from sys logs that tcpkill was started with PID after Configure dispatchers
     * 2. Make sure from sys logs that SIGCHLD was received from corresponding PID
     * 3. Make sure from log that tcpkill was killed ports
     * </p>
     *
     * @throws Throwable
     */
    @Execute
    public void test_Tcpkill_10() throws Throwable {
        TcpKillTask tcpKillTask = new TcpKillTask(TcpKillTask.TCP_KILL_REGEX_NUMBER);
        TcpKillTask sigchldTask = new TcpKillTask(TcpKillTask.SIGCHLD_REGEX_NUMBER);
        TcpKillTask portsTask = new TcpKillTask("port 5224 or port 5225 or port 5226 or port 5227 or port 5228");
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), tcpKillTask, sigchldTask, portsTask);
        logcatUtil.start();
        try {
            PMSUtil.addPolicies(new Policy[]{new Policy("interception_ports", "5224,5225,5226,5227,5228", "@asimov@interception@octcpd", true),
                    new Policy("tcpkill_ports", "5224,5225,5226,5227,5228", "@asimov@interception@octcpd", true)});
            TestUtil.sleep(2 * 60 * 1000);
            logcatUtil.stop();
            checkTest(tcpKillTask, sigchldTask, portsTask, true);
        } finally {
            logcatUtil.stop();
            cleanAllPolicy(new String[]{"@asimov@interception@octcpd"});
        }
    }

    /**
     * <p>Verify that tcpkill is started with list of ports separated by commas with port range
     * </p>
     * <p>Pre-requisites:
     * 1. OC client should be preinstalled on rooted device
     * 2. Make sure that gtalk, google maps, facebook are installed on you device
     * </p>
     * <p>Steps:
     * 1. Install new version of OC client
     * 2. Start tcpdump
     * 3. Add list of ports separated by commas with port range for tcp_kill policy on PMS
     * 4. Update the interception policy on PMS for TCP Dispatcher
     * </p>
     * <p>Expected reults:
     * 1. Make sure from sys logs that tcpkill was started with PID after Configure dispatchers
     * 2. Make sure from sys logs that SIGCHLD was received from corresponding PID
     * 3. Make sure from log that tcpkill was killed ports
     * </p>
     *
     * @throws Throwable
     */
    @Execute
    public void test_Tcpkill_11() throws Throwable {
        TcpKillTask tcpKillTask = new TcpKillTask(TcpKillTask.TCP_KILL_REGEX_NUMBER);
        TcpKillTask sigchldTask = new TcpKillTask(TcpKillTask.SIGCHLD_REGEX_NUMBER);
        TcpKillTask portsTask = new TcpKillTask("portrange 5220-5221 or portrange 5227-5228");
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), tcpKillTask, sigchldTask, portsTask);
        logcatUtil.start();
        try {
            PMSUtil.addPolicies(new Policy[]{new Policy("interception_ports", "5220:5221,5227:5228", "@asimov@interception@octcpd", true),
                    new Policy("tcpkill_ports", "5220:5221,5227:5228", "@asimov@interception@octcpd", true)});
            TestUtil.sleep(2 * 60 * 1000);
            logcatUtil.stop();
            checkTest(tcpKillTask, sigchldTask, portsTask, true);
        } finally {
            logcatUtil.stop();
            cleanAllPolicy(new String[]{"@asimov@interception@octcpd"});
        }
    }

    @Execute
    public void test_Tcpkill_12() throws Throwable {
        switchNetwork(WIFI_NETWORK);
        cleanAllPolicy(new String[]{"@asimov@failovers@restart@dispatchers@enabled",
                "@asimov@failovers@restart@controller@enabled",
                "@asimov@failovers@restart@engine@enabled"});
        TestUtil.sleep(60 * 1000);
    }

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
                logger.info("Test failed due to " + ExceptionUtils.getStackTrace(assertionFailedError));
                isPassed = false;
                counts.add("Test failed due to AssertionFailedError in " + numberOfAttempts + " attempt");
                Thread.sleep(2 * 60 * 1000);
            }
        } while (!isPassed && numberOfAttempts < 3);
        assertTrue("The test was failed three times ", counts.size() != 3);
    }
}
