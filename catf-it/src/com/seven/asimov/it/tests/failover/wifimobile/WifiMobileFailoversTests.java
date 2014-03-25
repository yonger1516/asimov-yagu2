package com.seven.asimov.it.tests.failover.wifimobile;


import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.testcases.FailoverTestCase;
import com.seven.asimov.it.utils.AirplaneModeUtil;
import com.seven.asimov.it.utils.IpTablesUtil;
import com.seven.asimov.it.utils.ShellUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.failoverTasks.FailoverStartTask;
import com.seven.asimov.it.utils.logcat.tasks.failoverTasks.FailoverStopTask;
import com.seven.asimov.it.utils.logcat.tasks.failoverTasks.FloIpcMessageTask;
import com.seven.asimov.it.utils.logcat.tasks.failoverTasks.TryingToConnectToRelayTask;
import com.seven.asimov.it.utils.logcat.tasks.firewallTasks.FirewallDisabledTask;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import junit.framework.AssertionFailedError;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class WifiMobileFailoversTests extends FailoverTestCase {
    private static final Logger logger = LoggerFactory.getLogger(WifiMobileFailoversTests.class.getSimpleName());

    /**
     * <h3>WiFi failover shouldn't start in case it is disable.</h3>
     * <p>Pre-requests
     * 1. Install OC client
     * 2. Active interface is wifi.
     * 3. Add policies:
     * asimov@failovers@wifi@enabled=0
     * 4. Relay should be unreachable for client during all test. (iptables -A INPUT -s <relay IP> -j REJECT)
     * </p>
     * <p>Steps
     * 1. Observe client log.
     * </p>
     * <p>Result
     * 1. After an attempt to connect to Relay client shouldn't start wifi failover.
     * </p>
     *
     * @throws Throwable
     */

    public void test_001_WFLO() throws Throwable {
        FailoverStartTask failoverStartTask = new FailoverStartTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), failoverStartTask);
        try {
            switchNetwork(WIFI_NETWORK);
            PMSUtil.addPolicies(new Policy[]{new Policy("enabled", "false", WIFI_FAILOVER_POLICY_PATH, true)});
            Thread.sleep(60 * 1000);
            switchNetwork(MOBILE_NETWORK);
            logcatUtil.start();
            IpTablesUtil.banRelayServer(true);
            switchNetwork(WIFI_NETWORK);
            Thread.sleep(45 * 1000);
            logcatUtil.stop();
            checkStartFailover(false, failoverStartTask, WIFI_FAILOVER_TYPE);
        } finally {
            logcatUtil.stop();
            switchNetwork(MOBILE_NETWORK);
            IpTablesUtil.banRelayServer(false);
            switchNetwork(WIFI_NETWORK);
            PMSUtil.cleanPaths(new String[]{WIFI_FAILOVER_POLICY_PATH});
        }
    }

    /**
     * <h3>WiFi failover should start, firewall rules should be disabled in case WiFi failover is enable and action=1.</h3>
     * <p>Pre-requests
     * 1. Install OC client
     * 2. Active interface is wifi.
     * 3. Add policies:
     * asimov@failovers@wifi@enabled=1
     * asimov@failovers@wifi@actions=1
     * 4. Relay should be unreachable for client during all test. (iptables -A INPUT -s <relay IP> -j REJECT)
     * </p>
     * <p>Steps
     * 1. Observe client log.
     * </p>
     * <p>Result
     * 1. After one negative attempt to connect to Relay client should start wifi failover.
     * 2. FLO for switching off dispatchers should be sent from Engine to Controller.
     * 3. IPC message for disabling firewall rules should be sent from Engine to Controller.
     * 4. Firewall rules should be deleted.
     * 5. Client shouldn't make retries to connect to Relay.
     * </p>
     *
     * @throws Throwable
     */

    public void test_002_WFLO() throws Throwable {
        FailoverStartTask failoverStartTask = new FailoverStartTask();
        FloIpcMessageTask floIpcMessageTask = new FloIpcMessageTask();
        FirewallDisabledTask firewallDisabledTask = new FirewallDisabledTask();
        TryingToConnectToRelayTask tryingToConnectToRelayTask = new TryingToConnectToRelayTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), failoverStartTask, floIpcMessageTask, firewallDisabledTask);
        try {
            switchNetwork(WIFI_NETWORK);
            PMSUtil.addPolicies(new Policy[]{new Policy("enabled", "true", WIFI_FAILOVER_POLICY_PATH, true),
                    new Policy("actions", "1", WIFI_FAILOVER_POLICY_PATH, true)});
            Thread.sleep(60 * 1000);
            switchNetwork(MOBILE_NETWORK);
            logcatUtil.start();
            IpTablesUtil.banRelayServer(true);
            switchNetwork(WIFI_NETWORK);
            Thread.sleep(60 * 1000);
            logcatUtil.stop();
            checkStartFailover(true, failoverStartTask, WIFI_FAILOVER_TYPE);
            logcatUtil = new LogcatUtil(getContext(), tryingToConnectToRelayTask);
            logcatUtil.start();
            checkFLOMessage(floIpcMessageTask, true);
            checkIPCMessage(floIpcMessageTask, true);
            assertFalse("Firewall rules should be deleted", firewallDisabledTask.getLogEntries().isEmpty());
            Thread.sleep(30 * 1000);
            logcatUtil.stop();
            assertTrue("Client shouldn't make retries to connect to Relay", tryingToConnectToRelayTask.getLogEntries().isEmpty());
        } finally {
            logcatUtil.stop();
            switchNetwork(MOBILE_NETWORK);
            IpTablesUtil.banRelayServer(false);
            switchNetwork(WIFI_NETWORK);
            PMSUtil.cleanPaths(new String[]{WIFI_FAILOVER_POLICY_PATH});
        }
    }

    /**
     * <h3>WiFi failover should start, cache should be purged, in case WiFi failover is enable and action=2.</h3>
     * <p>Pre-requests
     * 1. Install OC client
     * 2. Active interface is wifi.
     * 3. Add policies:
     * asimov@failovers@wifi@enabled=1
     * asimov@failovers@wifi@actions=2
     * 4. Relay should be unreachable for client during all test. (iptables -A INPUT -s <relay IP> -j REJECT)
     * </p>
     * <p>Steps
     * 1. Observe client log.
     * </p>
     * <p>Result
     * 1. After one negative attempt to connect to Relay, client should start WiFi failover.
     * 2. FLO for switching off dispatchers and purging cache should be sent from Engine to Controller.
     * 3. Go to /data/misc/openchannel/httpcache. Cache should be cleaned up.
     * 4. Client shouldn't make retries to connect to Relay.
     * </p>
     *
     * @throws Throwable
     */

    public void test_003_WFLO() throws Throwable {
        FailoverStartTask failoverStartTask = new FailoverStartTask();
        FloIpcMessageTask floIpcMessageTask = new FloIpcMessageTask();
        FirewallDisabledTask firewallDisabledTask = new FirewallDisabledTask();
        TryingToConnectToRelayTask tryingToConnectToRelayTask = new TryingToConnectToRelayTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), failoverStartTask, floIpcMessageTask, firewallDisabledTask);
        try {
            switchNetwork(WIFI_NETWORK);
            PMSUtil.addPolicies(new Policy[]{new Policy("enabled", "true", WIFI_FAILOVER_POLICY_PATH, true),
                    new Policy("actions", "2", WIFI_FAILOVER_POLICY_PATH, true)});
            Thread.sleep(60 * 1000);
            switchNetwork(MOBILE_NETWORK);
            logcatUtil.start();
            IpTablesUtil.banRelayServer(true);
            switchNetwork(WIFI_NETWORK);
            Thread.sleep(60 * 1000);
            checkStartFailover(true, failoverStartTask, WIFI_FAILOVER_TYPE);
            logcatUtil = new LogcatUtil(getContext(), tryingToConnectToRelayTask);
            logcatUtil.start();
            checkFLOMessage(floIpcMessageTask, true);
            Thread.sleep(30 * 1000);
            logcatUtil.stop();
            assertTrue("Client shouldn't make retries to connect to Relay", tryingToConnectToRelayTask.getLogEntries().isEmpty());
        } finally {
            logcatUtil.stop();
            switchNetwork(MOBILE_NETWORK);
            IpTablesUtil.banRelayServer(false);
            switchNetwork(WIFI_NETWORK);
            PMSUtil.cleanPaths(new String[]{WIFI_FAILOVER_POLICY_PATH});
        }
    }

    /**
     * <h3>WiFi failover should start, cache should be purged, firewall rules should be disabled in case WiFi
     * failover is enable and action=3.</h3>
     * <p>Pre-requests
     * 1. Install OC client
     * 2. Active interface is wifi.
     * 3. Add policies:
     * asimov@failovers@wifi@enabled=1
     * asimov@failovers@wifi@actions=3
     * 4. Relay should be unreachable for client during all test. (iptables -A INPUT -s <relay IP> -j REJECT)
     * </p>
     * <p>Steps
     * 1. Observe client log.
     * </p>
     * <p>Result
     * 1. After one negative attempt to connect to Relay, client should start WiFi failover.
     * 2. FLO for switching off dispatchers and purging cache should be sent from Engine to Controller.
     * 3. IPC message for disabling firewall rules should be sent from Engine to Controller.
     * 4. Go to /data/misc/openchannel/httpcache. Cache should be cleaned up.
     * 5. Firewall rules should be deleted.
     * 6. Client shouldn't make retries to connect to Relay.
     * </p>
     *
     * @throws Throwable
     */

    public void test_004_WFLO() throws Throwable {
        FailoverStartTask failoverStartTask = new FailoverStartTask();
        FloIpcMessageTask floIpcMessageTask = new FloIpcMessageTask();
        FirewallDisabledTask firewallDisabledTask = new FirewallDisabledTask();
        TryingToConnectToRelayTask tryingToConnectToRelayTask = new TryingToConnectToRelayTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), failoverStartTask, floIpcMessageTask, firewallDisabledTask);
        try {
            switchNetwork(WIFI_NETWORK);
            PMSUtil.addPolicies(new Policy[]{new Policy("enabled", "true", WIFI_FAILOVER_POLICY_PATH, true),
                    new Policy("actions", "3", WIFI_FAILOVER_POLICY_PATH, true)});
            Thread.sleep(60 * 1000);
            switchNetwork(MOBILE_NETWORK);
            logcatUtil.start();
            IpTablesUtil.banRelayServer(true);
            switchNetwork(WIFI_NETWORK);
            Thread.sleep(60 * 1000);
            logcatUtil.stop();
            checkStartFailover(true, failoverStartTask, WIFI_FAILOVER_TYPE);
            logcatUtil = new LogcatUtil(getContext(), tryingToConnectToRelayTask);
            logcatUtil.start();
            checkFLOMessage(floIpcMessageTask, true);
            checkIPCMessage(floIpcMessageTask, true);
            assertFalse("Firewall rules should be deleted", firewallDisabledTask.getLogEntries().isEmpty());
            Thread.sleep(30 * 1000);
            logcatUtil.stop();
            assertTrue("Client shouldn't make retries to connect to Relay", tryingToConnectToRelayTask.getLogEntries().isEmpty());
        } finally {
            logcatUtil.stop();
            switchNetwork(MOBILE_NETWORK);
            IpTablesUtil.banRelayServer(false);
            switchNetwork(WIFI_NETWORK);
            PMSUtil.cleanPaths(new String[]{WIFI_FAILOVER_POLICY_PATH});
        }
    }

    /**
     * <h3>"WiFi failover should start, firewall rules should be disabled in case WiFi failover is
     * enable and action =1. Mobile failover shouldn't start in case it is disable.</h3>
     * <p>Pre-requests
     * 1. Install OC client
     * 2. Active interface is wifi.
     * 3. Add policies:
     * asimov@failovers@wifi@enabled=1
     * asimov@failovers@wifi@actions=1
     * asimov@failovers@mobile_networks@enabled=0
     * 4. Relay should be unreachable for client during all test. (iptables -A INPUT -s <relay IP> -j REJECT)
     * </p>
     * <p>Steps
     * 1. After Wifi failover is started switch mobile on.
     * 2. Observe client log.
     * </p>
     * <p>Result
     * 1. After one negative attempt to connect to Relay client should start WiFi failover.
     * 2. FLO for switching off dispatchers should be sent from Engine to Controller.
     * 3. IPC message for disabling firewall rules should be sent from Engine to Controller.
     * 4. Firewall rules should be deleted.
     * 5. Client shouldn't make retries to connect to Relay.
     * 6. After mobile is connected, WiFi failover should be stopped.
     * 7. Attempt to connect to Relay on mobile interface should be done, mobile failover shouldn't start due to mobile failover is disable in client branding.
     * </p>
     *
     * @throws Throwable
     */

    public void test_005_WFLO() throws Throwable {
        FailoverStartTask failoverStartTask = new FailoverStartTask();
        FailoverStopTask failoverStopTask = new FailoverStopTask();
        FloIpcMessageTask floIpcMessageTask = new FloIpcMessageTask();
        FirewallDisabledTask firewallDisabledTask = new FirewallDisabledTask();
        TryingToConnectToRelayTask tryingToConnectToRelayTask = new TryingToConnectToRelayTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), failoverStartTask, floIpcMessageTask, firewallDisabledTask);
        try {
            switchNetwork(WIFI_NETWORK);
            PMSUtil.addPolicies(new Policy[]{new Policy("enabled", "true", WIFI_FAILOVER_POLICY_PATH, true),
                    new Policy("actions", "1", WIFI_FAILOVER_POLICY_PATH, true)});
            PMSUtil.addPolicies(new Policy[]{new Policy("enabled", "false", MOBILE_NETWORKS_FAILOVER_POLICY_PATH, true)});
            Thread.sleep(60 * 1000);
            switchNetwork(MOBILE_NETWORK);
            logcatUtil.start();
            IpTablesUtil.banRelayServer(true);
            switchNetwork(WIFI_NETWORK);
            Thread.sleep(60 * 1000);
            logcatUtil.stop();
            checkStartFailover(true, failoverStartTask, WIFI_FAILOVER_TYPE);
            logcatUtil = new LogcatUtil(getContext(), tryingToConnectToRelayTask);
            logcatUtil.start();
            Thread.sleep(30 * 1000);
            logcatUtil.stop();
            assertTrue("Client shouldn't make retries to connect to Relay", tryingToConnectToRelayTask.getLogEntries().isEmpty());
            logcatUtil = new LogcatUtil(getContext(), failoverStartTask, failoverStopTask);
            logcatUtil.start();
            switchNetwork(MOBILE_NETWORK);
            Thread.sleep(60 * 1000);
            logcatUtil.stop();
            checkStopFailover(true, failoverStopTask, WIFI_FAILOVER_TYPE);
            checkStartFailover(false, failoverStartTask, MOBILE_FAILOVER_TYPE);
        } finally {
            logcatUtil.stop();
            IpTablesUtil.banRelayServer(false);
            switchNetwork(WIFI_NETWORK);
            PMSUtil.cleanPaths(new String[]{WIFI_FAILOVER_POLICY_PATH});
            PMSUtil.cleanPaths(new String[]{MOBILE_NETWORKS_FAILOVER_POLICY_PATH});
        }
    }

    /**
     * <h3>WiFi failover should stop in case switching to mobile failover.</h3>
     * <p>Pre-requests
     * 1. Install OC client.
     * 2. Active interface is wifi.
     * 3. Add policies:
     * asimov@failovers@wifi@enabled=1
     * asimov@failovers@wifi@actions=2
     * asimov@failovers@mobile_networks@enabled=1
     * asimov@failovers@mobile_networks@actions=1
     * asimov@failovers@mobile_networks@attempt_interval=30
     * asimov@failovers@mobile_networks@retries=5
     * asimov@failovers@mobile_networks@timeout=3
     * 4. Relay should be unreachable for client during all test. (iptables -A INPUT -s <relay IP> -j REJECT)
     * </p>
     * <p>Steps
     * 1. After Wifi failover is started switch mobile on.
     * 2. Observe client log.
     * </p>
     * <p>Result
     * 1. After one negative attempt of connection to Relay client should start wifi failover.
     * 2. FLO for switching off dispatchers and purging cache should be sent from Engine to Controller.
     * 3. Go to /data/misc/openchannel/httpcache. Cache should be cleaned up.
     * 4. After switching mobile on an attempt to connect to Relay should be done.
     * 5. WiFi failover should stop.
     * 6. Mobile failovets should start.
     * </p>
     *
     * @throws Throwable
     */

    public void test_006_WFLO() throws Throwable {
        FailoverStartTask failoverStartTask = new FailoverStartTask();
        FailoverStopTask failoverStopTask = new FailoverStopTask();
        FloIpcMessageTask floIpcMessageTask = new FloIpcMessageTask();
        FirewallDisabledTask firewallDisabledTask = new FirewallDisabledTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), failoverStartTask, floIpcMessageTask, firewallDisabledTask);
        try {
            switchNetwork(WIFI_NETWORK);
            PMSUtil.addPolicies(new Policy[]{new Policy("enabled", "true", WIFI_FAILOVER_POLICY_PATH, true),
                    new Policy("actions", "2", WIFI_FAILOVER_POLICY_PATH, true)});
            PMSUtil.addPolicies(new Policy[]{new Policy("enabled", "true", MOBILE_NETWORKS_FAILOVER_POLICY_PATH, true),
                    new Policy("actions", "1", MOBILE_NETWORKS_FAILOVER_POLICY_PATH, true),
                    new Policy("attempt_interval", "30", MOBILE_NETWORKS_FAILOVER_POLICY_PATH, true),
                    new Policy("retries", "5", MOBILE_NETWORKS_FAILOVER_POLICY_PATH, true),
                    new Policy("timeout", "3", MOBILE_NETWORKS_FAILOVER_POLICY_PATH, true)});
            Thread.sleep(60 * 1000);
            switchNetwork(MOBILE_NETWORK);
            Thread.sleep(60 * 1000);
            logcatUtil.start();
            IpTablesUtil.banRelayServer(true);
            switchNetwork(WIFI_NETWORK);
            Thread.sleep(60 * 1000);
            logcatUtil.stop();
            checkStartFailover(true, failoverStartTask, WIFI_FAILOVER_TYPE);
            checkFLOMessage(floIpcMessageTask, true);
            logcatUtil = new LogcatUtil(getContext(), failoverStartTask, failoverStopTask);
            logcatUtil.start();
            switchNetwork(MOBILE_NETWORK);
            Thread.sleep(10 * 60 * 1000);
            logcatUtil.stop();
            checkStartFailover(true, failoverStartTask, MOBILE_FAILOVER_TYPE);
            checkStopFailover(true, failoverStopTask, WIFI_FAILOVER_TYPE);
        } finally {
            logcatUtil.stop();
            IpTablesUtil.banRelayServer(false);
            switchNetwork(WIFI_NETWORK);
            PMSUtil.cleanPaths(new String[]{WIFI_FAILOVER_POLICY_PATH});
            PMSUtil.cleanPaths(new String[]{MOBILE_NETWORKS_FAILOVER_POLICY_PATH});
        }
    }

    /**
     * <h3>WiFi failover should be continued after switching Airplane Mode on and should be continued
     * after Airplane Mode is switched off.</h3>
     * <p>Pre-requests
     * 1. Install OC client.
     * 2. Active interface is wifi.
     * 3. Add policies:
     * asimov@failovers@wifi@enabled=1
     * asimov@failovers@wifi@actions=1
     * 4. Relay should be unreachable for client during all test. (iptables -A INPUT -s <relay IP> -j REJECT)
     * </p>
     * <p>Steps
     * 1. After Wifi failover is started switch Airplane Mode on.
     * 2. Observe client log.
     * 3. Switch Airplane Mode off.
     * 4. 3G should be disabled.
     * 5. Observe client log.
     * </p>
     * <p>Result
     * 1. After one negative attempt of connection to Relay client should start wifi failover.
     * 2. FLO for switching off dispatchers should be sent from Engine to Controller.
     * 3. IPC message for disabling firewall rules should be sent from Engine to Controller.
     * 4. Firewall rules should be deleted.
     * 5. There should not be any messages for switching dispatchers on after Airplane Mode is switched on.
     * 6. There should not be any messages for switching dispatchers on after Airplane Mode is switched off.
     * </p>
     *
     * @throws Throwable
     */

    public void test_007_WFLO() throws Throwable {
        FailoverStartTask failoverStartTask = new FailoverStartTask();
        FloIpcMessageTask floIpcMessageTask = new FloIpcMessageTask();
        FirewallDisabledTask firewallDisabledTask = new FirewallDisabledTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), failoverStartTask, floIpcMessageTask, firewallDisabledTask);
        try {
            switchNetwork(WIFI_NETWORK);
            PMSUtil.addPolicies(new Policy[]{new Policy("enabled", "true", WIFI_FAILOVER_POLICY_PATH, true),
                    new Policy("actions", "1", WIFI_FAILOVER_POLICY_PATH, true)});
            Thread.sleep(60 * 1000);
            switchNetwork(MOBILE_NETWORK);
            logcatUtil.start();
            IpTablesUtil.banRelayServer(true);
            switchNetwork(WIFI_NETWORK);
            Thread.sleep(60 * 1000);
            logcatUtil.stop();
            checkStartFailover(true, failoverStartTask, WIFI_FAILOVER_TYPE);
            checkFLOMessage(floIpcMessageTask, true);
            checkIPCMessage(floIpcMessageTask, true);
            assertFalse("Firewall rules should be deleted", firewallDisabledTask.getLogEntries().isEmpty());
            logcatUtil = new LogcatUtil(getContext(), floIpcMessageTask);
            logcatUtil.start();
            AirplaneModeUtil airplaneModeUtil = new AirplaneModeUtil(getContext());
            airplaneModeUtil.setEnabled(true);
            airplaneModeUtil.setEnabled(false);
            logcatUtil.stop();
            checkFLOMessage(floIpcMessageTask, false);
            checkIPCMessage(floIpcMessageTask, false);
        } finally {
            switchNetwork(MOBILE_NETWORK);
            IpTablesUtil.banRelayServer(false);
            switchNetwork(WIFI_NETWORK);
            PMSUtil.cleanPaths(new String[]{WIFI_FAILOVER_POLICY_PATH});
        }
    }

    /**
     * <h3>WiFi failover should be continued after reboot.</h3>
     * <p>Pre-requests
     * 1. Install OC client.
     * 2. Active interface is wifi.
     * 3. Add policies:
     * asimov@failovers@wifi@enabled=1
     * asimov@failovers@wifi@actions=1
     * 4. Relay should be unreachable for client during all test. (iptables -A INPUT -s <relay IP> -j REJECT)
     * </p>
     * <p>Steps
     * 1. After WiFi failover is started reboot the device.
     * 2. Observe client log.
     * </p>
     * <p>Result
     * 1. After one negative attempt to connect to Relay, client should start WiFi failover.
     * 2. FLO for switching off dispatchers should be sent from Engine to Controller.
     * 3. IPC message for disabling firewall rules should be sent from Engine to Controller.
     * 3. Firewall rules should be deleted.
     * 4. Client shouldn't make retries to connect to Relay.
     * 5. After reboot client should make one attempt to connect to Relay.
     * 6. WiFi failover should start due to absence connection to Relay.
     * </p>
     *
     * @throws Throwable
     */

    public void test_008_WFLO() throws Throwable {
        FailoverStartTask failoverStartTask = new FailoverStartTask();
        FloIpcMessageTask floIpcMessageTask = new FloIpcMessageTask();
        FirewallDisabledTask firewallDisabledTask = new FirewallDisabledTask();
        TryingToConnectToRelayTask tryingToConnectToRelayTask = new TryingToConnectToRelayTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), failoverStartTask, floIpcMessageTask, firewallDisabledTask);
        try {
            switchNetwork(WIFI_NETWORK);
            PMSUtil.addPolicies(new Policy[]{new Policy("enabled", "true", WIFI_FAILOVER_POLICY_PATH, true),
                    new Policy("actions", "1", WIFI_FAILOVER_POLICY_PATH, true)});
            Thread.sleep(60 * 1000);
            switchNetwork(MOBILE_NETWORK);
            logcatUtil.start();
            IpTablesUtil.banRelayServer(true);
            switchNetwork(WIFI_NETWORK);
            Thread.sleep(60 * 1000);
            logcatUtil.stop();
            checkStartFailover(true, failoverStartTask, WIFI_FAILOVER_TYPE);
            logcatUtil = new LogcatUtil(getContext(), tryingToConnectToRelayTask);
            logcatUtil.start();
            checkFLOMessage(floIpcMessageTask, true);
            checkIPCMessage(floIpcMessageTask, true);
            Thread.sleep(60 * 1000);
            logcatUtil.stop();
            assertTrue("Client shouldn't make retries to connect to Relay", tryingToConnectToRelayTask.getLogEntries().isEmpty());
            logcatUtil = new LogcatUtil(getContext(), failoverStartTask);
            logcatUtil.start();
            ShellUtil.killAll(TFConstantsIF.OC_PROCESS_NAME);
            ShellUtil.killAll("occ");
            Thread.sleep(2 * 60 * 1000);
            logcatUtil.stop();
            checkStartFailover(true, failoverStartTask, WIFI_FAILOVER_TYPE);
        } finally {
            logcatUtil.stop();
            switchNetwork(MOBILE_NETWORK);
            IpTablesUtil.banRelayServer(false);
            switchNetwork(WIFI_NETWORK);
            PMSUtil.cleanPaths(new String[]{WIFI_FAILOVER_POLICY_PATH});
        }
    }

    /**
     * <h3>Mobile failover should stop and WiFi failover should start in case switching interfaces and connection to Relay is disable and the same actions.     </h3>
     * <p>Pre-requests
     * 1. Install OC client.
     * 2. Active interface is 3G.
     * 3. Add policies:
     * asimov@failovers@wifi@enabled=1
     * asimov@failovers@wifi@actions=3
     * asimov@failovers@mobile_networks@enabled=1
     * asimov@failovers@mobile_networks@actions=3
     * asimov@failovers@mobile_networks@attempt_interval=30
     * asimov@failovers@mobile_networks@retries=5
     * asimov@failovers@mobile_networks@timeout=3
     * 4. Relay should be unreachable for client during all test. (iptables -A INPUT -s <relay IP> -j REJECT)
     * </p>
     * <p>Steps
     * 1. Switch mobile on.
     * 2. After Mobile failover is started switch wifi on.
     * 3. Observe client log.
     * </p>
     * <p>Result
     * 1. After one negative attempt of connection to Relay should be done 5 attempts to connect.
     * 2. After last attempt mobile failover should start.
     * 3. FLO for switching off dispatchers and purging cache should be sent from Engine to Controller.
     * 4. IPC message for disabling firewall rules should be sent from Engine to Controller.
     * 5. Firewall rules should be deleted.
     * 6. Go to /data/misc/openchannel/httpcache. Cache should be cleaned up.
     * 7. After switching WiFi on, an attempt to connect to Relay should be done.
     * 8. WiFi failover should start due to absence connection to Relay.
     * 9. Mobile failover should stop.
     * </p>
     *
     * @throws Throwable
     */

    public void test_009_WFLO() throws Throwable {
        FailoverStartTask failoverStartTask = new FailoverStartTask();
        FailoverStopTask failoverStopTask = new FailoverStopTask();
        FloIpcMessageTask floIpcMessageTask = new FloIpcMessageTask();
        FirewallDisabledTask firewallDisabledTask = new FirewallDisabledTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), failoverStartTask, floIpcMessageTask, firewallDisabledTask);
        try {
            switchNetwork(WIFI_NETWORK);
            PMSUtil.addPolicies(new Policy[]{new Policy("enabled", "true", WIFI_FAILOVER_POLICY_PATH, true),
                    new Policy("actions", "3", WIFI_FAILOVER_POLICY_PATH, true)});
            PMSUtil.addPolicies(new Policy[]{new Policy("enabled", "true", MOBILE_NETWORKS_FAILOVER_POLICY_PATH, true),
                    new Policy("actions", "3", MOBILE_NETWORKS_FAILOVER_POLICY_PATH, true),
                    new Policy("attempt_interval", "30", MOBILE_NETWORKS_FAILOVER_POLICY_PATH, true),
                    new Policy("retries", "5", MOBILE_NETWORKS_FAILOVER_POLICY_PATH, true),
                    new Policy("timeout", "3", MOBILE_NETWORKS_FAILOVER_POLICY_PATH, true)});
            Thread.sleep(60 * 1000);
            logcatUtil.start();
            switchNetwork(MOBILE_NETWORK);
            Thread.sleep(60 * 1000);
            IpTablesUtil.banRelayServer(true);
            switchNetwork(WIFI_NETWORK, false, 10 * 1000);
            switchNetwork(MOBILE_NETWORK);
            Thread.sleep(25 * 60 * 1000);
            logcatUtil.stop();
            checkStartFailover(true, failoverStartTask, MOBILE_FAILOVER_TYPE);
            checkFLOMessage(floIpcMessageTask, true);
            checkIPCMessage(floIpcMessageTask, true);
            assertFalse("Firewall rules should be deleted", firewallDisabledTask.getLogEntries().isEmpty());
            logcatUtil = new LogcatUtil(getContext(), failoverStartTask, failoverStopTask);
            logcatUtil.start();
            switchNetwork(WIFI_NETWORK);
            Thread.sleep(2 * 60 * 1000);
            logcatUtil.stop();
            checkStartFailover(true, failoverStartTask, WIFI_FAILOVER_TYPE);
            checkStopFailover(true, failoverStopTask, MOBILE_FAILOVER_TYPE);
        } finally {
            logcatUtil.stop();
            IpTablesUtil.banRelayServer(false);
            switchNetwork(MOBILE_NETWORK);
            switchNetwork(WIFI_NETWORK);
            PMSUtil.cleanPaths(new String[]{WIFI_FAILOVER_POLICY_PATH});
            PMSUtil.cleanPaths(new String[]{MOBILE_NETWORKS_FAILOVER_POLICY_PATH});
        }
    }

    /**
     * <h3>Mobile failover should stop and WiFi failover should start in case switching interfaces and connection
     * to Relay is disable and different actions.</h3>
     * <p>Pre-requests
     * 1. Install OC client.
     * 2. Active interface is 3G.
     * 3. Add policies:
     * asimov@failovers@wifi@enabled=1
     * asimov@failovers@wifi@actions=2
     * asimov@failovers@mobile_networks@enabled=1
     * asimov@failovers@mobile_networks@actions=1
     * asimov@failovers@mobile_networks@attempt_interval=30
     * asimov@failovers@mobile_networks@retries=5
     * asimov@failovers@mobile_networks@timeout=3
     * 4. Relay should be unreachable for client during all test. (iptables -A INPUT -s <relay IP> -j REJECT)
     * </p>
     * <p>Steps
     * 1. Switch mobile on.
     * 2. After Mobile failover is started switch wifi on.
     * 3. Observe client log.
     * </p>
     * <p>Result
     * 1. After one negative attempt to connect to Relay should be done 5 attempts to connect.
     * 2. After last attempt mobile failover should start.
     * 3. FLO for switching off dispatchers should be sent from Engine to Controller.
     * 4. IPC message for disabling firewall rules should be sent from Engine to Controller.
     * 4. Firewall rules should be deleted.
     * 5. After switching wifi on, an attempt to connect to Relay should be done.
     * 6. WiFi failover should start due to absence connection to Relay.
     * 7. Mobile failover should stop.
     * </p>
     *
     * @throws Throwable
     */

    public void test_010_WFLO() throws Throwable {
        FailoverStartTask failoverStartTask = new FailoverStartTask();
        FailoverStopTask failoverStopTask = new FailoverStopTask();
        FloIpcMessageTask floIpcMessageTask = new FloIpcMessageTask();
        FirewallDisabledTask firewallDisabledTask = new FirewallDisabledTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), failoverStartTask, floIpcMessageTask, firewallDisabledTask);
        try {
            switchNetwork(WIFI_NETWORK);
            PMSUtil.addPolicies(new Policy[]{new Policy("enabled", "true", WIFI_FAILOVER_POLICY_PATH, true),
                    new Policy("actions", "2", WIFI_FAILOVER_POLICY_PATH, true)});
            PMSUtil.addPolicies(new Policy[]{new Policy("enabled", "true", MOBILE_NETWORKS_FAILOVER_POLICY_PATH, true),
                    new Policy("actions", "1", MOBILE_NETWORKS_FAILOVER_POLICY_PATH, true),
                    new Policy("attempt_interval", "30", MOBILE_NETWORKS_FAILOVER_POLICY_PATH, true),
                    new Policy("retries", "5", MOBILE_NETWORKS_FAILOVER_POLICY_PATH, true),
                    new Policy("timeout", "3", MOBILE_NETWORKS_FAILOVER_POLICY_PATH, true)});
            Thread.sleep(60 * 1000);
            logcatUtil.start();
            switchNetwork(MOBILE_NETWORK);
            Thread.sleep(60 * 1000);
            IpTablesUtil.banRelayServer(true);
            switchNetwork(WIFI_NETWORK, false, 10 * 1000);
            switchNetwork(MOBILE_NETWORK);
            Thread.sleep(25 * 60 * 1000);
            logcatUtil.stop();
            checkStartFailover(true, failoverStartTask, MOBILE_FAILOVER_TYPE);
            checkFLOMessage(floIpcMessageTask, true);
            checkIPCMessage(floIpcMessageTask, true);
            assertFalse("Firewall rules should be deleted", firewallDisabledTask.getLogEntries().isEmpty());
            logcatUtil = new LogcatUtil(getContext(), failoverStartTask, failoverStopTask);
            logcatUtil.start();
            switchNetwork(WIFI_NETWORK);
            Thread.sleep(2 * 60 * 1000);
            logcatUtil.stop();
            checkStartFailover(true, failoverStartTask, WIFI_FAILOVER_TYPE);
            checkStopFailover(true, failoverStopTask, MOBILE_FAILOVER_TYPE);
        } finally {
            logcatUtil.stop();
            IpTablesUtil.banRelayServer(false);
            switchNetwork(MOBILE_NETWORK);
            switchNetwork(WIFI_NETWORK);
            PMSUtil.cleanPaths(new String[]{WIFI_FAILOVER_POLICY_PATH});
            PMSUtil.cleanPaths(new String[]{MOBILE_NETWORKS_FAILOVER_POLICY_PATH});
        }
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
                logger.debug("Test failed due to " + ExceptionUtils.getStackTrace(assertionFailedError));
                isPassed = false;
                counts.add("Test failed due to AssertionFailedError in " + numberOfAttempts + " attempt");
                Thread.sleep(2 * 60 * 1000);
            }
        } while (!isPassed && numberOfAttempts < 3);
        assertTrue("The test was failed three times ", counts.size() != 3);
    }
}
