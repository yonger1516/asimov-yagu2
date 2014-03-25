package com.seven.asimov.it.tests.firewall.icmp;

import com.seven.asimov.it.testcases.ICMPBlockingTestCase;
import com.seven.asimov.it.utils.MobileNetworkUtil;
import com.seven.asimov.it.utils.ScreenUtils;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.firewallTasks.FirewallLogTask;
import com.seven.asimov.it.utils.pcf.*;
import com.seven.asimov.it.utils.sms.SmsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class ICMPBlockingTests extends ICMPBlockingTestCase {
    private static final Logger logger = LoggerFactory.getLogger(ICMPBlockingTests.class.getSimpleName());
    private MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());

    /**
     * Check icmpv6 policies provisioning and applying
     *
     * @throws Exception
     */
    public void test_001_ICMP_blocking() throws Exception {
        User currentUser = PcfHelper.retrieveUserBy7tp();
        assertNotNull("Not found user by 7tp, check your OC Client has msisdn validation enabled.", currentUser);
        cleanPCFUserGroup(pcfGroupName);

        logSleeping(20 * 1000);
        (new SmsUtil(getContext())).sendPCFUpdate((byte) 1);
        logSleeping(20 * 1000);
        assertTrue("ping " + HOST + " failed", pingHost(HOST));
        assertTrue("ping " + HOST1 + " failed", pingHost(HOST1));

        FirewallLogTask firewallLogTask = new FirewallLogTask();
        LogcatUtil logcat = new LogcatUtil(getContext(), firewallLogTask);
        logcat.start();

        ArrayList<Restriction> listRestrictions = new ArrayList<Restriction>();
        listRestrictions.add(new Restriction("icmp", PcfHelper.InterfaceType.ALL, null, null, null, "128"));

        Service pcfService = new Service("", pcfServiceName, listRestrictions, TF_PACKAGENAME, "", PcfHelper.Version.SAVED, PcfHelper.Type.SERVICE, null, "", System.currentTimeMillis(), PcfHelper.Status.UPTODATE);
        Trigger trigger = new Trigger(false, 0, false, 0, 0, false, 0, 0, false, true);
        PcfPolicy pcfPolicy = new PcfPolicy(pcfPolicyName, PcfHelper.Version.SAVED, "", trigger, null, PcfHelper.Status.UPTODATE, PcfHelper.InterfaceType.ALL, PcfHelper.Type.APPLICATION, true);

        PcfPolicy createdPolicy = PcfHelper.createNewPolicy(pcfPolicy);
        Service createdService = PcfHelper.createNewService(pcfService);
        PcfHelper.assignServicesToPolicy(createdPolicy.getId(), new GenericId(createdService.getId(), createdService.getName(), createdService.getStatus()));
        UserGroup userGroup = PcfHelper.createNewUserGroup(pcfGroupName);
        PcfHelper.assignPoliciesToGroup(userGroup.getId(), new GenericId(createdPolicy.getId(), createdPolicy.getName(), createdPolicy.getStatus()));

        PcfHelper.assignUsersToGroup(userGroup.getId(), currentUser.getId());
        provisionPCFChanges();
        (new SmsUtil(getContext())).sendPCFUpdate((byte) 1);
        logSleeping(20 * 1000);

        logcat.stop();
        logSleeping(10 * 1000);
        long id = checkFirewallRuleApplied(pcfPolicyName, firewallLogTask, 1);
        logger.info(ip6tables());
        assertTrue("wrong rule", isIp6tables(id, DESCRIPTION, "ipv6-icmptype 128"));
    }

    /**
     * Check icmpv6 protocol blocking with service
     * try to ping any host, should be blocked
     *
     * @throws Exception
     */
    public void test_002_ICMP_blocking() throws Exception {
        try {
            assertFalse("ping host " + HOST + " in test_ICMP_blocking_01()", pingHost(HOST));
        } finally {
            cleanServicesFromPCFPolicy(pcfPolicyName);
            logSleeping(20 * 1000);
            (new SmsUtil(getContext())).sendPCFUpdate((byte) 1);
            logSleeping(20 * 1000);
        }
    }

    /**
     * Check icmpv6 protocol blocking with specified ipv6
     * block specific host and trying to ping it,only this host should be blocked
     *
     * @throws Exception
     */
    public void test_003_ICMP_blocking() throws Exception {
        try {
            FirewallLogTask firewallLogTask = new FirewallLogTask();
            LogcatUtil logcat = new LogcatUtil(getContext(), firewallLogTask);
            logcat.start();

            Restriction restriction = new Restriction("icmp", PcfHelper.InterfaceType.ALL, null, null, HOST, "128");
            addNewRestriction(restriction);
            logSleeping(20 * 1000);
            (new SmsUtil(getContext())).sendPCFUpdate((byte) 1);
            logSleeping(20 * 1000);

            logcat.stop();
            long id = checkFirewallRuleApplied(pcfPolicyName, firewallLogTask, 1);
            logger.info(ip6tables());
            assertTrue("wrong rule", isIp6tables(id, DESCRIPTION, "ipv6-icmptype 128"));

            assertFalse("host ping 02 false ", pingHost(HOST));
            assertTrue("host ping 02 true ", pingHost(HOST1));

        } finally {
            cleanServicesFromPCFPolicy(pcfPolicyName);
            logSleeping(20 * 1000);
            (new SmsUtil(getContext())).sendPCFUpdate((byte) 1);
            logSleeping(20 * 1000);
        }
    }

    /**
     * Check icmpv6 protocol blocking with specified ipv4
     * block specific host and trying to ping it,only this host should be blocked
     *
     * @throws Exception
     */
    public void test_004_ICMP_blocking() throws Exception {
        try {
            FirewallLogTask firewallLogTask = new FirewallLogTask();
            LogcatUtil logcat = new LogcatUtil(getContext(), firewallLogTask);
            logcat.start();

            Restriction restriction = new Restriction("icmp", PcfHelper.InterfaceType.ALL, null, null, "74.125.129.103", "128");
            addNewRestriction(restriction);
            logSleeping(20 * 1000);
            (new SmsUtil(getContext())).sendPCFUpdate((byte) 1);
            logSleeping(20 * 1000);

            logcat.stop();
            logger.info(ip6tables());
            assertFalse("wrong rule", pingHost("74.125.129.103"));
        } finally {
            cleanServicesFromPCFPolicy(pcfPolicyName);
            logSleeping(20 * 1000);
            (new SmsUtil(getContext())).sendPCFUpdate((byte) 1);
            logSleeping(20 * 1000);
        }
    }

    /**
     * Check icmpv6 protocol blocking with service when screen is off
     * <p/>
     * 1. Insure that policies received and applied
     * 2. Screen OFF
     * 3. Check ip6tables rules - should be inactive
     * 4. Screen ON
     * 5. Check ip6tables rules - should be active
     *
     * @throws Exception
     */
    public void test_005_ICMP_blocking() throws Exception {
        FirewallLogTask firewallLogTask;
        LogcatUtil logcat;
        try {
            firewallLogTask = new FirewallLogTask();
            logcat = new LogcatUtil(getContext(), firewallLogTask);
            logcat.start();
            // prepare resources due polices
            PcfPolicy policy = PcfHelper.retrievePolicyByName(pcfPolicyName);
            Trigger trigger = new Trigger(true, 0, false, 0, 0, false, 0, 0, false, false);
            policy.setTrigger(trigger);
            PcfHelper.updatePolicy(policy);
            addNewRestriction(new Restriction("icmp", PcfHelper.InterfaceType.ALL, null, null, null, "128"));
            logSleeping(20 * 1000);
            (new SmsUtil(getContext())).sendPCFUpdate((byte) 1);
            logSleeping(20 * 1000);

            ScreenUtils.ScreenSpyResult spyOn = ScreenUtils.switchScreenAndSpy(getContext(), true);
            assertTrue("Screen off ( ", spyOn.isScreenAsExpected());
            logSleeping(10 * 1000);
            logcat.stop();
            logSleeping(10 * 1000);
            logger.info(ip6tables());
            long id1 = checkFirewallRuleApplied(pcfPolicyName, firewallLogTask, 0);

            firewallLogTask = new FirewallLogTask();
            logcat = new LogcatUtil(getContext(), firewallLogTask);
            logcat.start();
            ScreenUtils.ScreenSpyResult spy = ScreenUtils.switchScreenAndSpy(getContext(), false);
            assertTrue("Screen on ( ", spy.isScreenAsExpected());
            logSleeping(15 * 1000);
            logger.info(ip6tables());
            logcat.stop();
            long id2 = checkFirewallRuleApplied(pcfPolicyName, firewallLogTask, 1);
            assertTrue("not the same rule", id1 == id2);
        } finally {
            cleanServicesFromPCFPolicy(pcfPolicyName);
            logSleeping(20 * 1000);
            (new SmsUtil(getContext())).sendPCFUpdate((byte) 1);
            logSleeping(20 * 1000);
        }
    }

    /**
     * Check icmpv6 protocol blocking with service on mobile interface
     * <p/>
     * Should be run on mobile ipv6
     *
     * @throws Exception
     */
    public void test_006_ICMP_blocking() throws Exception {
//            prepare resources due polices
        PcfPolicy policy = PcfHelper.retrievePolicyByName(pcfPolicyName);
        Trigger trigger = new Trigger(false, 0, false, 0, 0, false, 0, 0, false, true);
        try {
            policy.setTrigger(trigger);
            policy.setNetworkInterface(PcfHelper.InterfaceType.MOBILE);
            PcfHelper.updatePolicy(policy);
            addNewRestriction(new Restriction("icmp", PcfHelper.InterfaceType.ALL, null, null, null, "128"));
            logSleeping(20 * 1000);
            (new SmsUtil(getContext())).sendPCFUpdate((byte) 1);
            logSleeping(20 * 1000);
            assertTrue("should be true", pingHost(HOST));

            mobileNetworkUtil.on3gOnly();
            logSleeping(15 * 1000);
            assertFalse("should be false", pingHost(HOST));
            logger.info(ip6tables());
            mobileNetworkUtil.onWifiOnly();
        } finally {
            mobileNetworkUtil.onWifiOnly();
            cleanServicesFromPCFPolicy(pcfPolicyName);
            logSleeping(20 * 1000);
            (new SmsUtil(getContext())).sendPCFUpdate((byte) 1);
            logSleeping(20 * 1000);
        }
    }

    /**
     * Check icmpv6 protocol blocking with service on wifi interface
     * <p/>
     * Should be run on mobile ipv6
     *
     * @throws Exception
     */
    public void test_007_ICMP_blocking() throws Exception {
//            prepare resources due polices
        PcfPolicy policy = PcfHelper.retrievePolicyByName(pcfPolicyName);
        Trigger trigger = new Trigger(false, 0, false, 0, 0, false, 0, 0, false, true);
        try {
            policy.setTrigger(trigger);
            policy.setNetworkInterface(PcfHelper.InterfaceType.ALL);
            PcfHelper.updatePolicy(policy);
            addNewRestriction(new Restriction("icmp", PcfHelper.InterfaceType.ALL, null, null, null, "128"));
            logSleeping(20 * 1000);
            (new SmsUtil(getContext())).sendPCFUpdate((byte) 1);
            logSleeping(20 * 1000);
            assertFalse("should be false", pingHost(HOST));

            mobileNetworkUtil.on3gOnly();
            logSleeping(20 * 1000);
            assertFalse("should be false", pingHost(HOST));
            logger.info(ip6tables());
            mobileNetworkUtil.onWifiOnly();
        } finally {
            mobileNetworkUtil.onWifiOnly();
            cleanServicesFromPCFPolicy(pcfPolicyName);
            logSleeping(20 * 1000);
            (new SmsUtil(getContext())).sendPCFUpdate((byte) 1);
            logSleeping(20 * 1000);
        }
    }

    /**
     * Check icmpv6 protocol blocking with service on all interfaces
     * Should be run on mobile ipv6
     *
     * @throws Exception
     */
    public void test_008_ICMP_blocking() throws Exception {
//            prepare resources due polices
        PcfPolicy policy = PcfHelper.retrievePolicyByName(pcfPolicyName);
        Trigger trigger = new Trigger(false, 0, false, 0, 0, false, 0, 0, false, true);
        try {
            policy.setTrigger(trigger);
            policy.setNetworkInterface(PcfHelper.InterfaceType.WIFI);
            PcfHelper.updatePolicy(policy);
            addNewRestriction(new Restriction("icmp", PcfHelper.InterfaceType.ALL, null, null, null, "128"));
            logSleeping(20 * 1000);
            (new SmsUtil(getContext())).sendPCFUpdate((byte) 1);
            logSleeping(20 * 1000);
            assertFalse("should be false", pingHost(HOST));

            mobileNetworkUtil.on3gOnly();
            logSleeping(20 * 1000);
            assertTrue("should be true", pingHost(HOST));
            logger.info(ip6tables());
            mobileNetworkUtil.onWifiOnly();
        } finally {
            mobileNetworkUtil.onWifiOnly();
            cleanServicesFromPCFPolicy(pcfPolicyName);
            logSleeping(20 * 1000);
            (new SmsUtil(getContext())).sendPCFUpdate((byte) 1);
            logSleeping(20 * 1000);
        }
    }


    /**
     * Verify applying  common icmp types
     *
     * @throws Exception
     */
    public void test_009_ICMP_blocking() throws Exception {
        PcfPolicy policy = PcfHelper.retrievePolicyByName(pcfPolicyName);
        Trigger trigger = new Trigger(false, 0, false, 0, 0, false, 0, 0, false, true);
        try {
            FirewallLogTask firewallLogTask = new FirewallLogTask();
            LogcatUtil logcat = new LogcatUtil(getContext(), firewallLogTask);
            logcat.start();
            policy.setNetworkInterface(PcfHelper.InterfaceType.ALL);
            policy.setTrigger(trigger);
            logger.info(policy.toString());
            PcfHelper.updatePolicy(policy);
            logSleeping(20 * 1000);
            (new SmsUtil(getContext())).sendPCFUpdate((byte) 1);
            logSleeping(20 * 1000);
            Restriction restriction = new Restriction("icmp", PcfHelper.InterfaceType.ALL, null, null, null, "31");
            addNewRestriction(restriction);
            logSleeping(20 * 1000);
            (new SmsUtil(getContext())).sendPCFUpdate((byte) 1);
            logSleeping(20 * 1000);
            logcat.stop();

            long id = checkFirewallRuleApplied(pcfPolicyName, firewallLogTask, 1);
            logger.info(ip6tables());
            assertTrue("wrong rule", isIp6tables(id, DESCRIPTION, "ipv6-icmptype 31"));

        } finally {
            cleanServicesFromPCFPolicy(pcfPolicyName);
            logSleeping(20 * 1000);
            (new SmsUtil(getContext())).sendPCFUpdate((byte) 1);
            logSleeping(20 * 1000);
        }
    }


    /**
     * Verify icmp type and icmp code blocking
     *
     * @throws Exception
     */
    public void test_010_ICMP_blocking() throws Exception {
        try {
            FirewallLogTask firewallLogTask = new FirewallLogTask();
            LogcatUtil logcat = new LogcatUtil(getContext(), firewallLogTask);
            logcat.start();
            Restriction restriction = new Restriction("icmp", PcfHelper.InterfaceType.ALL, null, null, null, "0");
            addNewRestriction(restriction);
            logSleeping(20 * 1000);
            (new SmsUtil(getContext())).sendPCFUpdate((byte) 1);
            logSleeping(20 * 1000);
            logcat.stop();

            long id = checkFirewallRuleApplied(pcfPolicyName, firewallLogTask, 1);
            logger.info(ip6tables());
            assertTrue("wrong rule", isIp6tables(id, DESCRIPTION, "ipv6-icmptype 0"));

        } finally {
            cleanServicesFromPCFPolicy(pcfPolicyName);
            logSleeping(20 * 1000);
            (new SmsUtil(getContext())).sendPCFUpdate((byte) 1);
            logSleeping(20 * 1000);
        }
    }

    /**
     * Check icmpv6 protocol blocking with specific application
     *
     * @throws Exception
     */
    public void test_011_ICMP_blocking() throws Exception {
        try {
            FirewallLogTask firewallLogTask = new FirewallLogTask();
            LogcatUtil logcat = new LogcatUtil(getContext(), firewallLogTask);
            logcat.start();
            Restriction restriction = new Restriction("icmp", PcfHelper.InterfaceType.ALL, null, TF_PACKAGENAME, null, "128");
            addNewRestriction(restriction);
            logSleeping(20 * 1000);
            (new SmsUtil(getContext())).sendPCFUpdate((byte) 1);
            logSleeping(20 * 1000);
            logcat.stop();

            long id = checkFirewallRuleApplied(pcfPolicyName, firewallLogTask, 1);
            logger.info(ip6tables());
            assertTrue("wrong rule", isIp6tables(id, DESCRIPTION, "UID match"));

        } finally {
            cleanServicesFromPCFPolicy(pcfPolicyName);
            logSleeping(20 * 1000);
            (new SmsUtil(getContext())).sendPCFUpdate((byte) 1);
            logSleeping(20 * 1000);
        }
    }


    /**
     * Check icmpv6 protocol blocking with hostname
     *
     * @throws Exception
     */
    public void test_012_ICMP_blocking() throws Exception {
        try {
            FirewallLogTask firewallLogTask = new FirewallLogTask();
            LogcatUtil logcat = new LogcatUtil(getContext(), firewallLogTask);
            logcat.start();

            Restriction restriction = new Restriction("icmp", PcfHelper.InterfaceType.ALL, null, null, "www.google.com", "128");
            addNewRestriction(restriction);
            logSleeping(20 * 1000);
            (new SmsUtil(getContext())).sendPCFUpdate((byte) 1);
            logSleeping(20 * 1000);

            logcat.stop();
            checkFirewallRuleApplied(pcfPolicyName, firewallLogTask, 1);
            logger.info(ip6tables());
            assertFalse("wrong rule", pingHost("www.google.com"));

        } finally {
            cleanServicesFromPCFPolicy(pcfPolicyName);
            logSleeping(20 * 1000);
            (new SmsUtil(getContext())).sendPCFUpdate((byte) 1);
            logSleeping(20 * 1000);
        }
    }


    /**
     * Check icmpv6 blocking with restricted 2 or more types
     *
     * @throws Exception
     */
    public void test_013_ICMP_blocking() throws Exception {
        try {
            FirewallLogTask firewallLogTask = new FirewallLogTask();
            LogcatUtil logcat = new LogcatUtil(getContext(), firewallLogTask);
            logcat.start();

            Restriction restriction1 = new Restriction("icmp", PcfHelper.InterfaceType.ALL, null, null, null, "128");
            Restriction restriction2 = new Restriction("icmp", PcfHelper.InterfaceType.ALL, null, null, null, "136");
            addNewRestriction(restriction1, restriction2);
            logSleeping(20 * 1000);
            (new SmsUtil(getContext())).sendPCFUpdate((byte) 1);
            logSleeping(20 * 1000);

            logcat.stop();
            long id = checkFirewallRuleApplied(pcfPolicyName, firewallLogTask, 1);
            logger.info(ip6tables());
            assertTrue("wrong rule", isIp6tables(id, DESCRIPTION, "ipv6-icmptype 128"));
            assertTrue("wrong rule", isIp6tables(id, DESCRIPTION, "ipv6-icmptype 136"));
        } finally {
            cleanServicesFromPCFPolicy(pcfPolicyName);
            logSleeping(20 * 1000);
            (new SmsUtil(getContext())).sendPCFUpdate((byte) 1);
            logSleeping(20 * 1000);
        }
    }
}
