package com.seven.asimov.it.tests.generic.application.handler.scripts;


import android.util.Log;
import com.seven.asimov.it.testcases.ScriptsTestCase;
import com.seven.asimov.it.utils.ScreenUtils;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.conditionTasks.ConditionStateTask;
import com.seven.asimov.it.utils.logcat.tasks.firewallTasks.FirewallLogTask;
import com.seven.asimov.it.utils.logcat.wrappers.ConditionState;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import junit.framework.AssertionFailedError;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.List;

public class ScriptsTests extends ScriptsTestCase {
    final String TAG = "EnabledPolicy";
    final String scriptName = "script_enable";
    final String pcfPolicyName = scriptName + "_policy";
    final String pcfGroupName = scriptName + "_group";
    final String pcfServiceName = scriptName + "_service";
    final String scriptPath = "@asimov@application@com.seven.asimov.it@scripts@" + scriptName;
    final String actionsPath = scriptPath + "@actions";
    final String exitActionsPath = scriptPath + "@exit_actions";
    final String conditionsPath = scriptPath + "@conditions";
    final String exitConditionsPath = scriptPath + "@exit_conditions";
    final String enableGroupAction = "enable_fw_group";
    final String disableGroupAction = "disable_fw_group";
    final String timerCondition = "timer";
    final String screenCondition = "screen";
    final String scriptEnable = "enabled";

    final String TF_PACKAGENAME = "com.seven.asimov.it";

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
                Log.e(TAG, "Test failed due to " + ExceptionUtils.getStackTrace(assertionFailedError));
                isPassed = false;
                counts.add("Test failed due to AssertionFailedError in " + numberOfAttempts + " attempt");
                Thread.sleep(2 * 60 * 1000);
            }
        } while (!isPassed && numberOfAttempts < 3);

        assertTrue("The test was failed three times ", counts.size() != 3);
    }


    public void test_000() throws Exception {
        cleanPCFUserGroup(pcfGroupName);
        setUpPcfPolicy(pcfGroupName, pcfPolicyName, pcfServiceName, TF_PACKAGENAME, true);
    }

    /**
     * Configure rule group script_enable_group that blocks tcp traffic on all interfaces for application com.seven.asimov.it (Screen is off , active flag should be true)
     * Configure such personal policies on PMS:
     * asimov@application@com.seven.asimov.it@scripts@script_enable:
     * actions@enable_fw_group: script_enable_group
     * conditions@timer: 500
     * exit_actions@disable_fw_group: script_enable_group
     * exit_conditions @timer: 500
     * <p/>
     * Check PMS and PCF policy received
     * Check script_enable switched to Exited state
     * Check iptables for firewall rule refcount 0
     * Set active flag to false for PCF script_enable_policy.
     * Set enabled=false for script_enable script.
     * Check script_enabled switched to Disabled state.
     * Check iptables for firewall rule refcount 0
     * Check network resources are reachable
     * Turn screen off
     * Check iptables for firewall rule refcount 0
     * Check network resources are reachable
     * Turn screen on
     * Check iptables for firewall rule refcount 0
     * Check network resources are reachable
     */

    public void test_001_disableExited() throws Throwable {
        final FirewallLogTask fwallLogTask = new FirewallLogTask();
        final ConditionStateTask exitedStateTask = new ConditionStateTask(TF_PACKAGENAME, scriptName, null, ConditionState.Exited);
        final ConditionStateTask disabledStateTask = new ConditionStateTask(TF_PACKAGENAME, scriptName, null, ConditionState.Disabled);
        LogcatUtil logcat = new LogcatUtil(getContext(), exitedStateTask, fwallLogTask);
        final String testResource = "test_001_disableExited";
        long ruleID;
        try {
            //Point 1
            Log.v("###DEBUG", "P1");
            logcat.start();
            modifyActiveState(pcfPolicyName, true);
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(timerCondition, "500", conditionsPath, true),
                    new Policy(timerCondition, "500", exitConditionsPath, true),
                    new Policy(enableGroupAction, pcfPolicyName, actionsPath, true),
                    new Policy(disableGroupAction, pcfPolicyName, exitActionsPath, true)});

            TestUtil.sleep(LITTLE_DELAY);
            logcat.stop();
            Log.v("###DEBUG", "P2");
            assertFalse("Expected " + scriptName + " script to switch state to Exited", exitedStateTask.getLogEntries().isEmpty());
            ruleID = checkFirewallRuleApplied(pcfPolicyName, fwallLogTask, 0);
            //Point 2
            Log.v("###DEBUG", "P3");
            fwallLogTask.reset();
            logcat = new LogcatUtil(getContext(), disabledStateTask, fwallLogTask);
            logcat.start();
            Log.v("###DEBUG", "P4");
            modifyActiveState(pcfPolicyName, false);
            Log.v("###DEBUG", "P5");
            TestUtil.sleep(LITTLE_DELAY);
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(scriptEnable, "false", scriptPath, true)});
            TestUtil.sleep(LITTLE_DELAY);
            logcat.stop();
            Log.v("###DEBUG", "P6");
            assertFalse("Expected " + scriptName + " script to switch state to Disabled", disabledStateTask.getLogEntries().isEmpty());
            ruleID = checkFirewallRuleApplied(pcfPolicyName, fwallLogTask, 0);
            //Point 3
            Log.v("###DEBUG", "P7");

            checkNetworkResourcesAccessible(testResource, true);

            //Point 4
            TestUtil.sleep(LITTLE_DELAY);
            ScreenUtils.screenOff();
            TestUtil.sleep(LITTLE_DELAY);
            assertEquals("Expected to have rule:" + pcfPolicyName + ", refcount:0", getRefCountForRule(ruleID), 0);
            Log.v("###DEBUG", "P8");
            //Point 5
            checkNetworkResourcesAccessible(testResource, true);
            TestUtil.sleep(LITTLE_DELAY);
            ScreenUtils.screenOn();
            TestUtil.sleep(LITTLE_DELAY);
            Log.v("###DEBUG", "P9");
            checkNetworkResourcesAccessible(testResource, true);
            assertEquals("Expected to have rule:" + pcfPolicyName + ", refcount:0", getRefCountForRule(ruleID), 0);
            Log.v("###DEBUG", "P10");
        } finally {
            logcat.stop();
            ScreenUtils.screenOn();
            cleanServicesFromPCFPolicy(pcfPolicyName);
            PMSUtil.cleanPaths(new String[]{scriptPath, actionsPath, exitActionsPath, conditionsPath, exitConditionsPath});
        }
    }

    /**
     * Configure rule group script_enable_group that blocks tcp traffic on all interfaces for application com.seven.asimov.it (Screen is off , active flag should be true)
     * Configure such personal policies on PMS:
     * asimov@application@com.seven.asimov.it@scripts@script_enable:
     * actions@enable_fw_group: script_enable_group
     * conditions@screen: off
     * exit_actions@disable_fw_group: script_enable_group
     * exit_conditions @timer: 500
     * <p/>
     * Check PMS and PCF policy received
     * Check script_enable switched to Exited state
     * Check iptables for firewall rule refcount 0
     * Check network resources are accessible
     * Turn screen off
     * Script script_enable should switch state to Entered
     * Check iptables rule refcount 1
     * Set active flag for script_enable_policy in pcf to false
     * Set enabled=false for script_enable script
     * Check script switched state to disabled
     * Check iptables rule refcount 0
     * Check network resources are accessible
     * Set screen on
     * Check iptables rule refcount 0
     * Set screen off
     * Check iptables rule refcount 0
     * Check network resources are accessible
     *
     * @throws Throwable
     */
    public void test_002_disableEntered() throws Throwable {
        final FirewallLogTask fwallLogTask = new FirewallLogTask();
        final ConditionStateTask exitedStateTask = new ConditionStateTask(TF_PACKAGENAME, scriptName, null, ConditionState.Exited);
        final ConditionStateTask enteredStateTask = new ConditionStateTask(TF_PACKAGENAME, scriptName, null, ConditionState.Entered);
        final ConditionStateTask disabledStateTask = new ConditionStateTask(TF_PACKAGENAME, scriptName, null, ConditionState.Disabled);
        LogcatUtil logcat = new LogcatUtil(getContext(), exitedStateTask, fwallLogTask);
        final String testResource = "test_002_disableEntered";
        long ruleID;
        try {
            //Point 1
            Log.v("###DEBUG", "P1");
            logcat.start();
            //modifyActiveState(pcfPolicyName, true);
            setUpServicesForPolicy(pcfPolicyName, pcfServiceName, TF_PACKAGENAME, true);
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(screenCondition, "off", conditionsPath, true),
                    new Policy(timerCondition, "500", exitConditionsPath, true),
                    new Policy(enableGroupAction, pcfPolicyName, actionsPath, true),
                    new Policy(disableGroupAction, pcfPolicyName, exitActionsPath, true)});

            TestUtil.sleep(LITTLE_DELAY);
            logcat.stop();
            Log.v("###DEBUG", "P2");
            assertFalse("Expected " + scriptName + " script to switch state to Exited", exitedStateTask.getLogEntries().isEmpty());
            ruleID = checkFirewallRuleApplied(pcfPolicyName, fwallLogTask, 0);
            //Point 2

            checkNetworkResourcesAccessible(testResource, true);

            //Point 3
            Log.v("###DEBUG", "P3");
            logcat = new LogcatUtil(getContext(), enteredStateTask);
            logcat.start();
            TestUtil.sleep(LITTLE_DELAY);
            ScreenUtils.screenOff();
            TestUtil.sleep(LITTLE_DELAY);
            logcat.stop();
            Log.v("###DEBUG", "P4");
            assertFalse("Expected " + scriptName + " script to switch state to Entered", enteredStateTask.getLogEntries().isEmpty());
            assertEquals("Expected to have rule:" + pcfPolicyName + ", refcount:1", getRefCountForRule(ruleID), 1);
            Log.v("###DEBUG", "P5");

            //Point 4
            fwallLogTask.reset();
            logcat = new LogcatUtil(getContext(), disabledStateTask, fwallLogTask);
            logcat.start();
            modifyActiveState(pcfPolicyName, false);
            Log.v("###DEBUG", "P6");
            provisionPCFChanges();
            Log.v("###DEBUG", "P7");
            TestUtil.sleep(LITTLE_DELAY);
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(scriptEnable, "false", scriptPath, true)});
            TestUtil.sleep(LITTLE_DELAY);
            logcat.stop();
            Log.v("###DEBUG", "P8");
            assertFalse("Expected " + scriptName + " script to switch state to Disabled", disabledStateTask.getLogEntries().isEmpty());
            ruleID = checkFirewallRuleApplied(pcfPolicyName, fwallLogTask, 0);

            checkNetworkResourcesAccessible(testResource, true);
            Log.v("###DEBUG", "P9");
            //Point 5
            TestUtil.sleep(LITTLE_DELAY);
            ScreenUtils.screenOn();
            assertEquals("Expected to have rule:" + pcfPolicyName + ", refcount:0", getRefCountForRule(ruleID), 0);
            TestUtil.sleep(30 * 1000);
            ScreenUtils.screenOff();
            TestUtil.sleep(LITTLE_DELAY);
            assertEquals("Expected to have rule:" + pcfPolicyName + ", refcount:0", getRefCountForRule(ruleID), 0);
            checkNetworkResourcesAccessible(testResource, true);
            Log.v("###DEBUG", "P10");

        } finally {
            logcat.stop();
            ScreenUtils.screenOn();
            cleanServicesFromPCFPolicy(pcfPolicyName);
            PMSUtil.cleanPaths(new String[]{scriptPath, actionsPath, exitActionsPath, conditionsPath, exitConditionsPath});

        }
    }

    /**
     * Configure rule group script_enable_group that blocks tcp traffic on all interfaces for application com.seven.asimov.it (Screen is off , active flag should be false)
     * Configure such personal policies on PMS:
     * asimov@application@com.seven.asimov.it@scripts@script_enable:
     * actions@enable_fw_group: script_enable_group
     * conditions@timer: 180
     * exit_actions@disable_fw_group: script_enable_group
     * exit_conditions @timer: 500
     * enabled: false
     * <p/>
     * <p/>
     * Check network resources accessible
     * Set screen off
     * Check script_enable script not switched state
     * <p/>
     * Check network resources accessible
     * Set enabled=true for script_enable script
     * Check script_enable script switched to Exited
     * Check network resources are accessible
     * Wait for 180 s.
     * Check script_enable script switched to Entered
     * Check iptables rule refcount 1
     * Check network not accessible
     *
     * @throws Throwable
     */
    public void test_003_enableDisabled() throws Throwable {
        final FirewallLogTask fwallLogTask = new FirewallLogTask();
        final ConditionStateTask exitedStateTask = new ConditionStateTask(TF_PACKAGENAME, scriptName, null, ConditionState.Exited);
        final ConditionStateTask enteredStateTask = new ConditionStateTask(TF_PACKAGENAME, scriptName, null, ConditionState.Entered);
        final ConditionStateTask switchStateTask = new ConditionStateTask(TF_PACKAGENAME, scriptName, null, null);
        LogcatUtil logcat = new LogcatUtil(getContext(), fwallLogTask);
        final String testResource = "test_003_disableExited";
        final Integer timerValue = 180;
        long ruleID;
        try {
            //Point 1
            Log.v("###DEBUG", "P1");
            logcat.start();
            //modifyActiveState(pcfPolicyName, false);
            setUpServicesForPolicy(pcfPolicyName, pcfServiceName, TF_PACKAGENAME, false);
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(scriptEnable, "false", scriptPath, true),
                    new Policy(timerCondition, timerValue.toString(), conditionsPath, true),
                    new Policy(timerCondition, "500", exitConditionsPath, true),
                    new Policy(enableGroupAction, pcfPolicyName, actionsPath, true),
                    new Policy(disableGroupAction, pcfPolicyName, exitActionsPath, true)});

            TestUtil.sleep(LITTLE_DELAY);
            logcat.stop();
            Log.v("###DEBUG", "P2");

            //Point 2
            checkNetworkResourcesAccessible(testResource, true);
            Log.v("###DEBUG", "P3");
            //Point3
            logcat = new LogcatUtil(getContext(), switchStateTask);
            logcat.start();
            TestUtil.sleep(LITTLE_DELAY);
            ScreenUtils.screenOff();
            TestUtil.sleep(LITTLE_DELAY);
            logcat.stop();
            assertTrue("Not expected " + scriptName + " script to switch state", switchStateTask.getLogEntries().isEmpty());

            Log.v("###DEBUG", "P4");
            checkNetworkResourcesAccessible(testResource, true);
            Log.v("###DEBUG", "P5");
            //Point 4
            exitedStateTask.reset();
            fwallLogTask.reset();
            logcat = new LogcatUtil(getContext(), exitedStateTask, fwallLogTask);
            logcat.start();

            TestUtil.sleep(LITTLE_DELAY);
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(scriptEnable, "true", scriptPath, true)});
            TestUtil.sleep(LITTLE_DELAY);
            logcat.stop();

            Log.v("###DEBUG", "P8");
            assertFalse("Expected " + scriptName + " script to switch state to Exited", exitedStateTask.getLogEntries().isEmpty());
            checkNetworkResourcesAccessible(testResource, true);

            Log.v("###DEBUG", "P9");
            //Point 5
            fwallLogTask.reset();
            logcat = new LogcatUtil(getContext(), enteredStateTask, fwallLogTask);
            logcat.start();
            waitForTimerConditionTrigger(exitedStateTask.getLogEntries().get(exitedStateTask.getLogEntries().size() - 1), timerValue);
            logcat.stop();
            assertFalse("Expected " + scriptName + " script to switch state to Entered", enteredStateTask.getLogEntries().isEmpty());
            ruleID = checkFirewallRuleApplied(pcfPolicyName, fwallLogTask, 1);
            Log.v("###DEBUG", "P10");
            checkNetworkResourcesAccessible(testResource, false);
            Log.v("###DEBUG", "P11");
        } finally {
            logcat.stop();
            ScreenUtils.screenOn();
            cleanServicesFromPCFPolicy(pcfPolicyName);
            PMSUtil.cleanPaths(new String[]{scriptPath, actionsPath, exitActionsPath, conditionsPath, exitConditionsPath});
        }

    }

    /**
     * Configure rule group script_enable_group that blocks tcp traffic on all interfaces for application com.seven.asimov.it (Screen is off , active flag should be false)
     * Configure such personal policies on PMS:
     * asimov@application@com.seven.asimov.it@scripts@script_enable:
     * actions@enable_fw_group: script_enable_group
     * conditions@screen: off
     * exit_actions@disable_fw_group: script_enable_group
     * exit_conditions @timer: 60
     * enabled: false
     * <p/>
     * Check script_enable script not switched state to Exited
     * <p/>
     * Check network resources are accessible
     * Turn screen off
     * <p/>
     * Check network resources are accessible
     * Wait for 60 seconds
     * Check script_enable script not switched state to Exited
     * <p/>
     * Check network resources are accessible
     *
     * @throws Throwable
     */
    public void test_004_createDisabled() throws Throwable {
        final ConditionStateTask changedStateTask = new ConditionStateTask(TF_PACKAGENAME, scriptName, null, null);
        final ConditionStateTask exitedStateTask = new ConditionStateTask(TF_PACKAGENAME, scriptName, null, ConditionState.Exited);
        LogcatUtil logcat = new LogcatUtil(getContext(), exitedStateTask);
        final Integer timerValue = 60;
        final String testResource = "test_004_disableEntered";

        try {
            //Point 1
            Log.v("###DEBUG", "P1");
            logcat.start();
            //modifyActiveState(pcfPolicyName, false);
            setUpServicesForPolicy(pcfPolicyName, pcfServiceName, TF_PACKAGENAME, false);
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(scriptEnable, "false", scriptPath, true),
                    new Policy(screenCondition, "off", conditionsPath, true),
                    new Policy(timerCondition, timerValue.toString(), exitConditionsPath, true),
                    new Policy(enableGroupAction, pcfPolicyName, actionsPath, true),
                    new Policy(disableGroupAction, pcfPolicyName, exitActionsPath, true)});

            TestUtil.sleep(LITTLE_DELAY);
            logcat.stop();
            Log.v("###DEBUG", "P2");
            assertTrue("Not expected " + scriptName + " script to switch state to Exited", exitedStateTask.getLogEntries().isEmpty());

            Log.v("###DEBUG", "P3");
            checkNetworkResourcesAccessible(testResource, true);
            Log.v("###DEBUG", "P4");
            //Point 2
            ScreenUtils.screenOff();
            TestUtil.sleep(LITTLE_DELAY);
            Log.v("###DEBUG", "P5");

            Log.v("###DEBUG", "P6");
            checkNetworkResourcesAccessible(testResource, true);
            Log.v("###DEBUG", "P7");
            //Point 3
            logcat = new LogcatUtil(getContext(), changedStateTask);
            logcat.start();
            TestUtil.sleep(timerValue);
            logcat.stop();
            Log.v("###DEBUG", "P8");
            assertTrue("Not expected " + scriptName + " script to switch state", changedStateTask.getLogEntries().isEmpty());
            Log.v("###DEBUG", "P9");

            checkNetworkResourcesAccessible(testResource, true);
            Log.v("###DEBUG", "P10");
        } finally {
            logcat.stop();
            ScreenUtils.screenOn();
            cleanServicesFromPCFPolicy(pcfPolicyName);
            PMSUtil.cleanPaths(new String[]{scriptPath, actionsPath, exitActionsPath, conditionsPath, exitConditionsPath});
        }
    }

    /**
     * Configure rule group script_enable_group that blocks tcp traffic on all interfaces for application com.seven.asimov.it (Screen is off , active flag should be true)
     * Configure such personal policies on PMS:
     * asimov@application@com.seven.asimov.it@scripts@script_enable:
     * actions@enable_fw_group: script_enable_group
     * conditions@screen: off
     * exit_actions@disable_fw_group: script_enable_group
     * exit_conditions @timer: 120
     * <p/>
     * Check script_enable script switched state to Exited
     * Check firewall rule has refcount 0
     * Check network resources are accessible
     * Turn screen off
     * Check script_enable script switched state to Entered
     * Check iptables rule has refcount 1
     * Check network resources are NOT accessible
     * Turn screen on
     * Check iptables rule has refcount 0
     * Check network resources are accessible
     * Turn screen off
     * Check iptables rule has refcount 1
     * Check network resources are NOT accessible
     * Wait for 120 s
     * Check script_enabled script switched state to Exited
     * Check iptables rule has refcount 0
     * Check network resources are accessible
     *
     * @throws Throwable
     */
    public void test_005_createEnabled() throws Throwable {
        final FirewallLogTask fwallLogTask = new FirewallLogTask();
        final ConditionStateTask enteredStateTask = new ConditionStateTask(TF_PACKAGENAME, scriptName, null, ConditionState.Entered);
        final ConditionStateTask exitedStateTask = new ConditionStateTask(TF_PACKAGENAME, scriptName, null, ConditionState.Exited);
        LogcatUtil logcat = new LogcatUtil(getContext(), exitedStateTask, fwallLogTask);
        final Integer timerValue = 120;
        final String testResource = "test_005_disableEntered";
        final long ruleID;
        try {
            //Point 1
            Log.v("###DEBUG", "P1");
            logcat.start();
            //modifyActiveState(pcfPolicyName, true);
            setUpServicesForPolicy(pcfPolicyName, pcfServiceName, TF_PACKAGENAME, true);
            PMSUtil.addPoliciesWithCheck(new Policy[]{
                    new Policy(screenCondition, "off", conditionsPath, true),
                    new Policy(timerCondition, timerValue.toString(), exitConditionsPath, true),
                    new Policy(enableGroupAction, pcfPolicyName, actionsPath, true),
                    new Policy(disableGroupAction, pcfPolicyName, exitActionsPath, true)});

            TestUtil.sleep(LITTLE_DELAY);
            logcat.stop();
            Log.v("###DEBUG", "P2");
            assertFalse("Expected " + scriptName + " script to switch state to Exited", exitedStateTask.getLogEntries().isEmpty());
            ruleID = checkFirewallRuleApplied(pcfPolicyName, fwallLogTask, 0);
            Log.v("###DEBUG", "P3");
            checkNetworkResourcesAccessible(testResource, true);
            Log.v("###DEBUG", "P4");
            //Point 2
            logcat = new LogcatUtil(getContext(), enteredStateTask, fwallLogTask);
            logcat.start();
            TestUtil.sleep(LITTLE_DELAY);
            ScreenUtils.screenOff();
            TestUtil.sleep(LITTLE_DELAY);
            logcat.stop();
            Log.v("###DEBUG", "P5");
            assertFalse("Expected " + scriptName + " script to switch state to Entered", enteredStateTask.getLogEntries().isEmpty());
            assertEquals("Expected to have rule:" + pcfPolicyName + ", refcount:1", getRefCountForRule(ruleID), 1);
            checkNetworkResourcesAccessible(testResource, false);
            Log.v("###DEBUG", "P6");
            //Point 3
            TestUtil.sleep(LITTLE_DELAY);
            ScreenUtils.screenOn();
            TestUtil.sleep(LITTLE_DELAY);
            Log.v("###DEBUG", "P7");
            assertEquals("Expected to have rule:" + pcfPolicyName + ", refcount:0", getRefCountForRule(ruleID), 0);
            Log.v("###DEBUG", "P8");
            checkNetworkResourcesAccessible(testResource, true);
            Log.v("###DEBUG", "P9");
            //Point 4
            TestUtil.sleep(LITTLE_DELAY);
            ScreenUtils.screenOff();
            TestUtil.sleep(LITTLE_DELAY);
            logcat.stop();
            Log.v("###DEBUG", "P9");
            assertEquals("Expected to have rule:" + pcfPolicyName + ", refcount:1", getRefCountForRule(ruleID), 1);
            Log.v("###DEBUG", "P10");
            checkNetworkResourcesAccessible(testResource, false);
            Log.v("###DEBUG", "P11");
            //Point 5
            exitedStateTask.reset();
            logcat = new LogcatUtil(getContext(), exitedStateTask);
            logcat.start();
            waitForTimerConditionTrigger(enteredStateTask.getLogEntries().get(enteredStateTask.getLogEntries().size() - 1), timerValue);
            logcat.stop();
            Log.v("###DEBUG", "P12");
            assertFalse("Expected " + scriptName + " script to switch state to Exited", exitedStateTask.getLogEntries().isEmpty());
            assertEquals("Expected to have rule:" + pcfPolicyName + ", refcount:0", getRefCountForRule(ruleID), 0);
            Log.v("###DEBUG", "P13");
            checkNetworkResourcesAccessible(testResource, true);
            Log.v("###DEBUG", "P14");

        } finally {
            logcat.stop();
            ScreenUtils.screenOn();
            cleanServicesFromPCFPolicy(pcfPolicyName);
            PMSUtil.cleanPaths(new String[]{scriptPath, actionsPath, exitActionsPath, conditionsPath, exitConditionsPath});
        }
    }

    /**
     * Configure rule group script_enable_group that blocks tcp traffic on all interfaces for application com.seven.asimov.it (Screen is off , active flag should be true)
     * Configure such personal policies on PMS:
     * asimov@application@com.seven.asimov.it@scripts@script_enable:
     * actions@enable_fw_group: script_enable_group
     * conditions@screen: off
     * exit_actions@disable_fw_group: script_enable_group
     * exit_conditions @screen : on
     * <p/>
     * Check script_enable script switched state to Exited
     * Check iptables rule refcount 0
     * Set policy enabled=true for script_enable script
     * Check network resources are accessible
     * Turn screen off
     * Check script_enable script changed state to Entered
     * Check iptables rule refcount 1
     * Check network resources are NOT accessible
     *
     * @throws Throwable
     */
    public void test_006_enablingExited() throws Throwable {
        final FirewallLogTask fwallLogTask = new FirewallLogTask();
        final ConditionStateTask enteredStateTask = new ConditionStateTask(TF_PACKAGENAME, scriptName, null, ConditionState.Entered);
        final ConditionStateTask exitedStateTask = new ConditionStateTask(TF_PACKAGENAME, scriptName, null, ConditionState.Exited);
        LogcatUtil logcat = new LogcatUtil(getContext(), exitedStateTask, fwallLogTask);
        final String testResource = "test_006_disableEntered";
        final long ruleID;
        try {
            //Point 1
            Log.v("###DEBUG", "P1");
            logcat.start();
            //modifyActiveState(pcfPolicyName, true);
            setUpServicesForPolicy(pcfPolicyName, pcfServiceName, TF_PACKAGENAME, true);
            PMSUtil.addPoliciesWithCheck(new Policy[]{
                    new Policy(screenCondition, "off", conditionsPath, true),
                    new Policy(screenCondition, "on", exitConditionsPath, true),
                    new Policy(enableGroupAction, pcfPolicyName, actionsPath, true),
                    new Policy(disableGroupAction, pcfPolicyName, exitActionsPath, true)});

            TestUtil.sleep(LITTLE_DELAY);
            logcat.stop();
            Log.v("###DEBUG", "P2");
            assertFalse("Expected " + scriptName + " script to switch state to Exited", exitedStateTask.getLogEntries().isEmpty());
            ruleID = checkFirewallRuleApplied(pcfPolicyName, fwallLogTask, 0);
            Log.v("###DEBUG", "P3");
            //Point 2

            TestUtil.sleep(LITTLE_DELAY);
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(scriptEnable, "true", scriptPath, true)});
            TestUtil.sleep(LITTLE_DELAY);
            Log.v("###DEBUG", "P4");

            //Point 3
            checkNetworkResourcesAccessible(testResource, true);
            Log.v("###DEBUG", "P5");
            logcat = new LogcatUtil(getContext(), enteredStateTask);
            logcat.start();
            TestUtil.sleep(LITTLE_DELAY);
            ScreenUtils.screenOff();
            TestUtil.sleep(LITTLE_DELAY);
            logcat.stop();
            Log.v("###DEBUG", "P6");
            assertFalse("Expected " + scriptName + " script to switch state to Entered", enteredStateTask.getLogEntries().isEmpty());
            assertEquals("Expected to have rule:" + pcfPolicyName + ", refcount:1", getRefCountForRule(ruleID), 1);
            Log.v("###DEBUG", "P7");
            checkNetworkResourcesAccessible(testResource, false);
            Log.v("###DEBUG", "P8");
        } finally {
            logcat.stop();
            ScreenUtils.screenOn();
            cleanServicesFromPCFPolicy(pcfPolicyName);
            PMSUtil.cleanPaths(new String[]{scriptPath, actionsPath, exitActionsPath, conditionsPath, exitConditionsPath});
        }
    }

    /**
     * Configure rule group script_enable_group that blocks tcp traffic on all interfaces for application com.seven.asimov.it (Screen is off , active flag should be true)
     * Configure such personal policies on PMS:
     * asimov@application@com.seven.asimov.it@scripts@script_enable:
     * actions@enable_fw_group: script_enable_group
     * conditions@screen: off
     * exit_actions@disable_fw_group: script_enable_group
     * exit_conditions @timer : 500
     * <p/>
     * Check script_enable script switched state to Exited
     * Check iptables rule refcount 0
     * Check network resources are accessible
     * Turn screen off
     * Check script_enable script switched state to Entered
     * Check iptables rule refcount 1
     * Check network resources are not accessible
     * Set policy enabled=true for script_enable script
     * Check that script_enable script not changed state
     * Check iptables rule refcount 1
     * Check network resources are not accessible
     *
     * @throws Throwable
     */
    public void test_007_enablingEntered() throws Throwable {
        final FirewallLogTask fwallLogTask = new FirewallLogTask();
        final ConditionStateTask enteredStateTask = new ConditionStateTask(TF_PACKAGENAME, scriptName, null, ConditionState.Entered);
        final ConditionStateTask changedStateTask = new ConditionStateTask(TF_PACKAGENAME, scriptName, null, null);
        final ConditionStateTask exitedStateTask = new ConditionStateTask(TF_PACKAGENAME, scriptName, null, ConditionState.Exited);
        LogcatUtil logcat = new LogcatUtil(getContext(), exitedStateTask, fwallLogTask);
        final Integer timerValue = 180;
        final String testResource = "test_007_disableEntered";
        final long ruleID;
        try {
            //Point 1
            Log.v("###DEBUG", "P1");
            logcat.start();
            //modifyActiveState(pcfPolicyName, true);
            setUpServicesForPolicy(pcfPolicyName, pcfServiceName, TF_PACKAGENAME, true);
            PMSUtil.addPoliciesWithCheck(new Policy[]{
                    new Policy(screenCondition, "off", conditionsPath, true),
                    new Policy(timerCondition, "500", exitConditionsPath, true),
                    new Policy(enableGroupAction, pcfPolicyName, actionsPath, true),
                    new Policy(disableGroupAction, pcfPolicyName, exitActionsPath, true)});

            TestUtil.sleep(LITTLE_DELAY);
            logcat.stop();
            Log.v("###DEBUG", "P2");
            assertFalse("Expected " + scriptName + " script to switch state to Exited", exitedStateTask.getLogEntries().isEmpty());
            Log.v("###DEBUG", "P3");
            ruleID = checkFirewallRuleApplied(pcfPolicyName, fwallLogTask, 0);
            Log.v("###DEBUG", "P4");
            checkNetworkResourcesAccessible(testResource, true);
            Log.v("###DEBUG", "P5");

            //Point 2
            logcat = new LogcatUtil(getContext(), enteredStateTask);
            logcat.start();
            TestUtil.sleep(LITTLE_DELAY);
            ScreenUtils.screenOff();
            TestUtil.sleep(LITTLE_DELAY);
            logcat.stop();
            Log.v("###DEBUG", "P6");
            assertFalse("Expected " + scriptName + " script to switch state to Entered", enteredStateTask.getLogEntries().isEmpty());
            assertEquals("Expected to have rule:" + pcfPolicyName + ", refcount:1", getRefCountForRule(ruleID), 1);
            Log.v("###DEBUG", "P7");
            checkNetworkResourcesAccessible(testResource, false);
            Log.v("###DEBUG", "P8");

            logcat = new LogcatUtil(getContext(), changedStateTask);
            //Point 3
            Log.v("###DEBUG", "P9");
            TestUtil.sleep(LITTLE_DELAY);
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(scriptEnable, "true", scriptPath, true)});
            TestUtil.sleep(LITTLE_DELAY);
            Log.v("###DEBUG", "P10");
            assertTrue("Not expected " + scriptName + " script to switch state", changedStateTask.getLogEntries().isEmpty());
            assertEquals("Expected to have rule:" + pcfPolicyName + ", refcount:1", getRefCountForRule(ruleID), 1);
            Log.v("###DEBUG", "P11");
            checkNetworkResourcesAccessible(testResource, false);
            Log.v("###DEBUG", "P12");
        } finally {
            logcat.stop();
            ScreenUtils.screenOn();
            cleanServicesFromPCFPolicy(pcfPolicyName);
            PMSUtil.cleanPaths(new String[]{scriptPath, actionsPath, exitActionsPath, conditionsPath, exitConditionsPath});
        }
    }

    public void test_099() throws Exception {
        cleanPCFUserGroup(pcfGroupName);
    }
}
