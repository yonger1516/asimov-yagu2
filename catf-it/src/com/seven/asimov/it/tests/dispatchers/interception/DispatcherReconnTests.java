package com.seven.asimov.it.tests.dispatchers.interception;


import android.util.Log;
import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.testcases.DispatcherReconnTestCase;
import com.seven.asimov.it.utils.IpTablesUtil;
import com.seven.asimov.it.utils.MobileNetworkUtil;
import com.seven.asimov.it.utils.OCUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.DSCTask;
import com.seven.asimov.it.utils.logcat.tasks.ServiceLogTask;
import com.seven.asimov.it.utils.logcat.wrappers.DSCWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.ServiceLogWrapper;
import com.seven.asimov.it.utils.pms.Policy;
import com.seven.asimov.it.utils.sms.SmsUtil;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.seven.asimov.it.utils.pms.PMSUtil.addPoliciesWithCheck;
import static com.seven.asimov.it.utils.pms.PMSUtil.cleanPaths;

public class DispatcherReconnTests extends DispatcherReconnTestCase {
    final String TAG = "DispatcherReconn";
    protected final int LITTLE_DELAY = 10 * 1000;


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
                Log.e(TAG, "Test failed due to " + ExceptionUtils.getStackTrace(assertionFailedError));
                isPassed = false;
                counts.add("Test failed due to AssertionFailedError in " + numberOfAttempts + " attempt");
                Thread.sleep(2 * 60 * 1000);
            }
        } while (!isPassed && numberOfAttempts < 3);

        assertTrue("The test was failed three times ", counts.size() != 3);
    }

    public void test_DSC_000_SetUpTests() throws Exception {
        switchRestartFailover(false);
    }


    private final String policyPath = "@asimov";
    private final String enabledPolicyName = "enabled";


    /**
     * End occ with kill cmd.
     * Check ServiceLog with explanation=started for all dispatchers.
     * Check DSC Startup
     *
     * @throws Exception
     */

    public void test_DSC_001() throws Exception {
        ServiceLogTask serviceStartedLog = new ServiceLogTask("service", "started", null, "startup");
        DSCTask startDSCSend = new DSCTask(true, null, DSCTask.DSCAction.STARTED.getValue(), DSCTask.DSCReason.STARTUP.getValue());
        DSCTask startDSCRecv = new DSCTask(false, null, DSCTask.DSCAction.STARTED.getValue(), DSCTask.DSCReason.STARTUP.getValue());

        LogcatUtil logcat = new LogcatUtil(getContext(), startDSCSend, startDSCRecv, serviceStartedLog);
        Map<String, Integer> ocProcesses = OCUtil.getOcProcesses(false);
        final int occ = ocProcesses.get("occ");
        final String killPattern = "kill %s";
        String[] killCmd = {"su", "-c", ""};

        try {

            logcat.start();
            killCmd[2] = String.format(killPattern, occ);
            Runtime.getRuntime().exec(killCmd).waitFor();
            Log.v("###DEBUG", "exec done");
            logSleeping(5 * LITTLE_DELAY);
            logcat.stop();

            Log.v("###DEBUG", "" + startDSCSend.getLogEntries().size());
            for (DSCWrapper wrapper : startDSCSend.getLogEntries())
                Log.v("###DEBUG", "wrapper: " + wrapper.toString());
            for (DSCWrapper wrapper : startDSCRecv.getLogEntries())
                Log.v("###DEBUG", "wrapper: " + wrapper.toString());

            checkAllDispatchersLogged(startDSCSend);
            checkAllDispatchersLogged(startDSCRecv);
            checkAllDispatchersLogged(serviceStartedLog);
        } finally {
            logcat.stop();
        }
    }

    /**
     * Set policy Asimov@enabled=0
     * Check DSC ENDED-DISABLED for all dispatchers
     * Set policy Asimov@enabled=1
     * Check DSC STARTED-ENABLED for all dispatchers
     *
     * @throws Exception
     */

    public void test_DSC_002() throws Exception {
        DSCTask disabledDSCSend = new DSCTask(true, null, DSCTask.DSCAction.ENDED.getValue(), DSCTask.DSCReason.DISABLED.getValue());
        DSCTask disabledDSCRecv = new DSCTask(false, null, DSCTask.DSCAction.ENDED.getValue(), DSCTask.DSCReason.DISABLED.getValue());
        DSCTask enableDSCSend = new DSCTask(true, null, DSCTask.DSCAction.STARTED.getValue(), DSCTask.DSCReason.ENABLED.getValue());
        DSCTask enableDSCRecv = new DSCTask(false, null, DSCTask.DSCAction.STARTED.getValue(), DSCTask.DSCReason.ENABLED.getValue());

        LogcatUtil logcat = new LogcatUtil(getContext(), disabledDSCSend, disabledDSCRecv);
        try {
            logcat.start();
            addPoliciesWithCheck(new Policy[]{new Policy(enabledPolicyName, "0", policyPath, true)});
            logSleeping(LITTLE_DELAY);
            logcat.stop();

            Log.v("###DEBUG", "" + disabledDSCSend.getLogEntries().size());
            for (DSCWrapper wrapper : disabledDSCSend.getLogEntries())
                Log.v("###DEBUG", "wrapper: " + wrapper.toString());

            Log.v("###DEBUG", "" + disabledDSCRecv.getLogEntries().size());
            for (DSCWrapper wrapper : disabledDSCRecv.getLogEntries())
                Log.v("###DEBUG", "wrapper: " + wrapper.toString());


            checkAllDispatchersLogged(disabledDSCSend);
            checkAllDispatchersLogged(disabledDSCRecv);

            logcat = new LogcatUtil(getContext(), enableDSCSend, enableDSCRecv);
            logcat.start();
            addPoliciesWithCheck(new Policy[]{new Policy(enabledPolicyName, "1", policyPath, true)});
            logSleeping(3 * LITTLE_DELAY);
            logcat.stop();
            Log.v("###DEBUG", "" + enableDSCSend.getLogEntries().size());
            for (DSCWrapper wrapper : enableDSCSend.getLogEntries())
                Log.v("###DEBUG", "wrapper: " + wrapper.toString());

            Log.v("###DEBUG", "" + enableDSCRecv.getLogEntries().size());
            for (DSCWrapper wrapper : enableDSCRecv.getLogEntries())
                Log.v("###DEBUG", "wrapper: " + wrapper.toString());

            checkAllDispatchersLogged(enableDSCSend);
            checkAllDispatchersLogged(enableDSCRecv);

        } finally {
            logcat.stop();
            cleanPaths(new String[]{policyPath});
        }
    }

    /**
     * Kill all dispatchers
     * Check DSC STARTED-RESTART for all dispatchers
     *
     * @throws Exception
     */

    public void test_DSC_003() throws Exception {
        DSCTask restartedDSCSend = new DSCTask(true, null, DSCTask.DSCAction.STARTED.getValue(), DSCTask.DSCReason.RESTART.getValue());
        DSCTask restartedDSCRecv = new DSCTask(false, null, DSCTask.DSCAction.STARTED.getValue(), DSCTask.DSCReason.RESTART.getValue());

        LogcatUtil logcat = new LogcatUtil(getContext(), restartedDSCSend, restartedDSCRecv);
        Map<String, Integer> ocProcesses = OCUtil.getOcProcesses(false);
        final int ochttpd = ocProcesses.get("ochttpd");
        final int ocshttpd = ocProcesses.get("ocshttpd");
        final int octcpd = ocProcesses.get("octcpd");
        final int ocdnsd = ocProcesses.get("ocdnsd");
        final String killPattern = "kill %s";
        String[] killCmd = {"su", "-c", ""};


        try {
            logcat.start();
            killCmd[2] = String.format(killPattern, ochttpd);
            Runtime.getRuntime().exec(killCmd).waitFor();

            killCmd[2] = String.format(killPattern, ocshttpd);
            Runtime.getRuntime().exec(killCmd).waitFor();

            killCmd[2] = String.format(killPattern, octcpd);
            Runtime.getRuntime().exec(killCmd).waitFor();

            killCmd[2] = String.format(killPattern, ocdnsd);
            Runtime.getRuntime().exec(killCmd).waitFor();

            Log.v("###DEBUG", "exec done");
            logSleeping(3 * LITTLE_DELAY);
            logcat.stop();

            Log.v("###DEBUG", "" + restartedDSCSend.getLogEntries().size());
            for (DSCWrapper wrapper : restartedDSCSend.getLogEntries())
                Log.v("###DEBUG", "wrapper: " + wrapper.toString());

            Log.v("###DEBUG", "" + restartedDSCRecv.getLogEntries().size());
            for (DSCWrapper wrapper : restartedDSCRecv.getLogEntries())
                Log.v("###DEBUG", "wrapper: " + wrapper.toString());

            checkAllDispatchersLogged(restartedDSCSend);
            checkAllDispatchersLogged(restartedDSCRecv);

        } finally {
            logcat.stop();
        }
    }

    private final String ocdnsdPath = "@asimov@interception@ocdnsd";
    private final String ochttpdPath = "@asimov@interception@ochttpd";
    private final String ocshttpdPath = "@asimov@interception@ocshttpd";
    private final String octcpdPath = "@asimov@interception@octcpd";
    private final String dispatcherProperty = "z_order";

    /**
     * 1.Set policy z_order for each dispatcher
     * 2.Check DSC ENDED SHUTDOWN for all dispatchers
     *
     * @throws Exception
     */

    public void test_DSC_004() throws Exception {
        DSCTask shutdownDSCSend = new DSCTask(true, null, DSCTask.DSCAction.ENDED.getValue(), DSCTask.DSCReason.SHUTDOWN.getValue());
        DSCTask shutdownDSCRecv = new DSCTask(false, null, DSCTask.DSCAction.ENDED.getValue(), DSCTask.DSCReason.SHUTDOWN.getValue());

        LogcatUtil logcat = new LogcatUtil(getContext(), shutdownDSCSend, shutdownDSCRecv);
        try {
            logcat.start();
            addPoliciesWithCheck(new Policy[]{
                    new Policy(dispatcherProperty, "1", ocdnsdPath, true),
                    new Policy(dispatcherProperty, "1", ochttpdPath, true),
                    new Policy(dispatcherProperty, "1", ocshttpdPath, true),
                    new Policy(dispatcherProperty, "254", octcpdPath, true)});
            logSleeping(LITTLE_DELAY);
            logcat.stop();

            Log.v("###DEBUG", "" + shutdownDSCSend.getLogEntries().size());
            for (DSCWrapper wrapper : shutdownDSCSend.getLogEntries())
                Log.v("###DEBUG", "wrapper: " + wrapper.toString());

            Log.v("###DEBUG", "" + shutdownDSCRecv.getLogEntries().size());
            for (DSCWrapper wrapper : shutdownDSCRecv.getLogEntries())
                Log.v("###DEBUG", "wrapper: " + wrapper.toString());

            checkAllDispatchersLogged(shutdownDSCSend);
            checkAllDispatchersLogged(shutdownDSCRecv);

        } finally {
            logcat.stop();
            cleanPaths(new String[]{ocdnsdPath, ochttpdPath, ocshttpdPath, octcpdPath});
        }
    }

    /**
     * 1.End each dispatcher with kill cmd.
     * 2.Check DSC ENDED-UNEXPECTED for all dispatchers
     *
     * @throws Exception
     */

    public void test_DSC_008() throws Exception {
        DSCTask endedDSCSend = new DSCTask(true, null, DSCTask.DSCAction.ENDED.getValue(), DSCTask.DSCReason.UNEXPECTED.getValue());
        DSCTask endedDSCRecv = new DSCTask(false, null, DSCTask.DSCAction.ENDED.getValue(), DSCTask.DSCReason.UNEXPECTED.getValue());

        LogcatUtil logcat = new LogcatUtil(getContext(), endedDSCSend, endedDSCRecv);
        Map<String, Integer> ocProcesses = OCUtil.getOcProcesses(false);
        final int ochttpd = ocProcesses.get("ochttpd");
        final int ocshttpd = ocProcesses.get("ocshttpd");
        final int octcpd = ocProcesses.get("octcpd");
        final int ocdnsd = ocProcesses.get("ocdnsd");
        final String killPattern = "kill %s";
        String[] killCmd = {"su", "-c", ""};


        try {
            logcat.start();
            killCmd[2] = String.format(killPattern, ochttpd);
            Runtime.getRuntime().exec(killCmd).waitFor();

            killCmd[2] = String.format(killPattern, ocshttpd);
            Runtime.getRuntime().exec(killCmd).waitFor();

            killCmd[2] = String.format(killPattern, octcpd);
            Runtime.getRuntime().exec(killCmd).waitFor();

            killCmd[2] = String.format(killPattern, ocdnsd);
            Runtime.getRuntime().exec(killCmd).waitFor();

            Log.v("###DEBUG", "exec done");
            logSleeping(3 * LITTLE_DELAY);
            logcat.stop();

            Log.v("###DEBUG", "" + endedDSCSend.getLogEntries().size());
            for (DSCWrapper wrapper : endedDSCSend.getLogEntries())
                Log.v("###DEBUG", "wrapper: " + wrapper.toString());

            Log.v("###DEBUG", "" + endedDSCRecv.getLogEntries().size());
            for (DSCWrapper wrapper : endedDSCRecv.getLogEntries())
                Log.v("###DEBUG", "wrapper: " + wrapper.toString());

            checkAllDispatchersLogged(endedDSCSend);
            checkAllDispatchersLogged(endedDSCRecv);

        } finally {
            logcat.stop();
        }
    }

    private final String failoverPath = "@asimov@failovers@wifi";
    private final String enabledProperty = "enabled";


    /**
     * Start wifi failover
     * Check DSC with explanation ENDED-FLO_START for each dispatcher
     * End wifi failover
     * Check DSC with explanation STARTED-FLO_END for each dispatcher
     *
     * @throws Exception
     */

    public void test_DSC_009() throws Exception {
        DSCTask floStartDSCSend = new DSCTask(true, null, DSCTask.DSCAction.ENDED.getValue(), DSCTask.DSCReason.FLO_START.getValue());
        DSCTask floStartDSCRecv = new DSCTask(false, null, DSCTask.DSCAction.ENDED.getValue(), DSCTask.DSCReason.FLO_START.getValue());

        DSCTask floEndDSCSend = new DSCTask(true, null, DSCTask.DSCAction.STARTED.getValue(), DSCTask.DSCReason.FLO_END.getValue());
        DSCTask floEndDSCRecv = new DSCTask(false, null, DSCTask.DSCAction.STARTED.getValue(), DSCTask.DSCReason.FLO_END.getValue());


        LogcatUtil logcat = new LogcatUtil(getContext(), floStartDSCSend, floStartDSCRecv);
        try {
            addPoliciesWithCheck(new Policy[]{new Policy(enabledProperty, "true", failoverPath, true)});
            logcat.start();
            MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());
            mobileNetworkUtil.on3gOnly();
            IpTablesUtil.banRelayServer(true);
            mobileNetworkUtil.onWifiOnly();
            logSleeping(LITTLE_DELAY * 4);
            logcat.stop();
            Log.v("###DEBUG", "" + floStartDSCSend.getLogEntries().size());
            for (DSCWrapper wrapper : floStartDSCSend.getLogEntries())
                Log.v("###DEBUG", "wrapper: " + wrapper.toString());
            for (DSCWrapper wrapper : floStartDSCRecv.getLogEntries())
                Log.v("###DEBUG", "wrapper: " + wrapper.toString());

            checkAllDispatchersLogged(floStartDSCSend);
            checkAllDispatchersLogged(floStartDSCRecv);

            logcat = new LogcatUtil(getContext(), floEndDSCSend, floEndDSCRecv);
            logcat.start();
            IpTablesUtil.banRelayServer(false);
            mobileNetworkUtil.on3gOnly();
            mobileNetworkUtil.onWifiOnly();
            (new SmsUtil(getContext())).sendPolicyUpdate((byte) 1);
            logSleeping(LITTLE_DELAY * 4);
            logcat.stop();
            Log.v("###DEBUG", "" + floEndDSCSend.getLogEntries().size());
            for (DSCWrapper wrapper : floEndDSCSend.getLogEntries())
                Log.v("###DEBUG", "wrapper: " + wrapper.toString());

            for (DSCWrapper wrapper : floEndDSCRecv.getLogEntries())
                Log.v("###DEBUG", "wrapper: " + wrapper.toString());

            checkAllDispatchersLogged(floEndDSCSend);
            checkAllDispatchersLogged(floEndDSCRecv);

        } finally {
            logcat.stop();
            IpTablesUtil.banRelayServer(false);
            cleanPaths(new String[]{failoverPath});
        }
    }

    /**
     * End com.seven.asimov with kill cmd.
     * Check ServiceLog with explanation=restarted for ocengine.
     * Check ServiceLog with explanation=started for all dispatchers.
     * Check ServiceLog with explanation=started for all dispatchers.
     *
     * @throws Exception
     */
    @Ignore //TODO: Obsolete TestCase
    public void test_DSC_010() throws Exception {
        ServiceLogTask serviceStartedLog = new ServiceLogTask("service", "started", null, "startup");
        ServiceLogTask occStartedLog = new ServiceLogTask("service", "started", "occ", "connect");
        ServiceLogTask engineRestartedLog = new ServiceLogTask("service", "restarted", "ocengine", null);

        LogcatUtil logcat = new LogcatUtil(getContext(), serviceStartedLog, occStartedLog, engineRestartedLog);
        Map<String, Integer> ocProcesses = OCUtil.getOcProcesses(false);
        final int asimov = ocProcesses.get("com.seven.asimov");
        final String killPattern = "kill %s";
        String[] killCmd = {"su", "-c", ""};

        try {
            logcat.start();
            killCmd[2] = String.format(killPattern, asimov);
            Runtime.getRuntime().exec(killCmd).waitFor();
            Log.v("###DEBUG", "exec done");
            logSleeping(5 * LITTLE_DELAY);
            logcat.stop();

            Log.v("###DEBUG", "" + serviceStartedLog.getLogEntries().size());
            for (ServiceLogWrapper wrapper : serviceStartedLog.getLogEntries())
                Log.v("###DEBUG", "wrapper: " + wrapper.toString());

            checkAllDispatchersLogged(serviceStartedLog);
            assertTrue("Expected ServiceLog startup for occ", !occStartedLog.getLogEntries().isEmpty());
            assertTrue("Expected ServiceLog restarted for ocengine", !engineRestartedLog.getLogEntries().isEmpty());
        } finally {
            logcat.stop();
        }
    }

    /**
     * End occ with kill cmd.
     * Check ServiceLog with explanation=disconnected for occ.
     * Check ServiceLog with explanation=started for all dispatchers.
     *
     * @throws Exception
     */
    @Ignore //TODO: Obsolete TestCase
    public void test_DSC_011() throws Exception {
        ServiceLogTask occDisconnectLog = new ServiceLogTask("service", "ended", "occ", "disconnect");
        ServiceLogTask serviceStartedLog = new ServiceLogTask("service", "started", null, "startup");
        DSCTask startDSCSend = new DSCTask(true, null, DSCTask.DSCAction.STARTED.getValue(), DSCTask.DSCReason.STARTUP.getValue());
        DSCTask startDSCRecv = new DSCTask(false, null, DSCTask.DSCAction.ENDED.getValue(), DSCTask.DSCReason.STARTUP.getValue());

        LogcatUtil logcat = new LogcatUtil(getContext(), startDSCSend, startDSCRecv, occDisconnectLog, serviceStartedLog);
        Map<String, Integer> ocProcesses = OCUtil.getOcProcesses(false);
        final int occ = ocProcesses.get("occ");
        final String killPattern = "kill %s";
        String[] killCmd = {"su", "-c", ""};

        try {

            logcat.start();
            killCmd[2] = String.format(killPattern, occ);
            Runtime.getRuntime().exec(killCmd).waitFor();
            Log.v("###DEBUG", "exec done");
            logSleeping(5 * LITTLE_DELAY);
            logcat.stop();

            Log.v("###DEBUG", "" + startDSCSend.getLogEntries().size());
            for (DSCWrapper wrapper : startDSCSend.getLogEntries())
                Log.v("###DEBUG", "wrapper: " + wrapper.toString());
            for (DSCWrapper wrapper : startDSCRecv.getLogEntries())
                Log.v("###DEBUG", "wrapper: " + wrapper.toString());

            checkAllDispatchersLogged(startDSCSend);
            checkAllDispatchersLogged(startDSCRecv);
            checkAllDispatchersLogged(serviceStartedLog);
            assertTrue("Expected ServiceLog disconnect for occ", !occDisconnectLog.getLogEntries().isEmpty());

        } finally {
            logcat.stop();
        }
    }


    private final String transparentPolicyName = "transparent";

    /**
     * Set policy asimov@transparent=1
     * Check DSC ENDED-DISABLED for all dispatchers.
     * Check DSC STARTED-ENABLED for all dispatchers.
     * Set policy asimov@transparent=0
     * Check ServiceLog with explanation=disabled for all dispatchers.
     * Check ServiceLog with explanation=enabled for all dispatchers.
     *
     * @throws Exception
     */

    public void test_DSC_012() throws Exception {
        DSCTask disabledDSCSend = new DSCTask(true, null, DSCTask.DSCAction.ENDED.getValue(), DSCTask.DSCReason.DISABLED.getValue());
        DSCTask disabledDSCRecv = new DSCTask(false, null, DSCTask.DSCAction.ENDED.getValue(), DSCTask.DSCReason.DISABLED.getValue());

        DSCTask enabledDSCSend = new DSCTask(true, null, DSCTask.DSCAction.STARTED.getValue(), DSCTask.DSCReason.ENABLED.getValue());
        DSCTask enabledDSCRecv = new DSCTask(false, null, DSCTask.DSCAction.STARTED.getValue(), DSCTask.DSCReason.ENABLED.getValue());

        LogcatUtil logcat = new LogcatUtil(getContext(), disabledDSCSend, disabledDSCRecv, enabledDSCSend, enabledDSCRecv);
        try {
            logcat.start();
            addPoliciesWithCheck(new Policy[]{new Policy(transparentPolicyName, "1", policyPath, true)});
            logSleeping(3 * LITTLE_DELAY);
            logcat.stop();

            Log.v("###DEBUG", "" + disabledDSCSend.getLogEntries().size());
            for (DSCWrapper wrapper : disabledDSCSend.getLogEntries())
                Log.v("###DEBUG", "wrapper: " + wrapper.toString());
            for (DSCWrapper wrapper : disabledDSCRecv.getLogEntries())
                Log.v("###DEBUG", "wrapper: " + wrapper.toString());

            checkAllDispatchersLogged(disabledDSCSend);
            checkAllDispatchersLogged(disabledDSCRecv);
            checkAllDispatchersLogged(enabledDSCSend);
            checkAllDispatchersLogged(enabledDSCRecv);

            disabledDSCSend.reset();
            disabledDSCRecv.reset();
            enabledDSCSend.reset();
            enabledDSCRecv.reset();

            logcat = new LogcatUtil(getContext(), disabledDSCSend, disabledDSCRecv, enabledDSCSend, enabledDSCRecv);
            logcat.start();
            addPoliciesWithCheck(new Policy[]{new Policy(transparentPolicyName, "0", policyPath, true)});
            logSleeping(3 * LITTLE_DELAY);
            logcat.stop();
            Log.v("###DEBUG", "" + enabledDSCSend.getLogEntries().size());
            for (DSCWrapper wrapper : enabledDSCSend.getLogEntries())
                Log.v("###DEBUG", "wrapper: " + wrapper.toString());
            for (DSCWrapper wrapper : enabledDSCRecv.getLogEntries())
                Log.v("###DEBUG", "wrapper: " + wrapper.toString());

            checkAllDispatchersLogged(enabledDSCSend);
            checkAllDispatchersLogged(enabledDSCRecv);

        } finally {
            logcat.stop();
            cleanPaths(new String[]{policyPath});
        }
    }

    public void test_DSC_099_EndTests() throws Exception {
        switchRestartFailover(true);
    }
}

