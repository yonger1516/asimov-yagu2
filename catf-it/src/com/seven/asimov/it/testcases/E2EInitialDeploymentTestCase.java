package com.seven.asimov.it.testcases;

import android.os.Build;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.FirewallPolicyMgmtDataRequestTask;
import com.seven.asimov.it.utils.logcat.tasks.FirstTimePoliciesRetrievalTask;
import com.seven.asimov.it.utils.logcat.tasks.MsisdnTask;
import com.seven.asimov.it.utils.logcat.tasks.e2eTasks.ClientRegistrationWithServerTask;
import com.seven.asimov.it.utils.logcat.tasks.e2eTasks.OCInitializationTask;
import com.seven.asimov.it.utils.logcat.tasks.firewallTasks.FirewallPolicyMgmtDataResponseTask;
import com.seven.asimov.it.utils.logcat.tasks.firewallTasks.ResponseFirewallPolicyReceivedTask;
import com.seven.asimov.it.utils.logcat.tasks.firewallTasks.StartingFirewallTask;
import com.seven.asimov.it.utils.pms.PMSUtil;
import junit.framework.AssertionFailedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Date;
import java.util.TimeZone;

public class E2EInitialDeploymentTestCase extends E2ETestCase {
    private static final Logger logger = LoggerFactory.getLogger(E2EInitialDeploymentTestCase.class.getSimpleName());
    private static boolean isOCInstalled = false;
    protected static final String SUITE_NAME = "E2E-Initial-deployment";
    protected static final long POLICY_STORAGE_VERSION = -1L;
    protected static final String OC_APK_FILENAME = "/sdcard/asimov-signed.apk";
    protected static MsisdnTask msisdnTask = new MsisdnTask();
    protected static OCInitializationTask ocInitializationTask = new OCInitializationTask();
    protected static FirstTimePoliciesRetrievalTask policiesRetrievalTask = new FirstTimePoliciesRetrievalTask();
    protected static ClientRegistrationWithServerTask clientRegistrationWithServerTask = new ClientRegistrationWithServerTask();
    protected static FirewallPolicyMgmtDataRequestTask firewallPolicyMgmtDataRequestTask = new FirewallPolicyMgmtDataRequestTask();
    protected static FirewallPolicyMgmtDataResponseTask firewallPolicyMgmtDataResponseTask = new FirewallPolicyMgmtDataResponseTask();
    protected static ResponseFirewallPolicyReceivedTask responseFirewallPolicyReceivedTask = new ResponseFirewallPolicyReceivedTask();
    protected static StartingFirewallTask startingFirewallTask = new StartingFirewallTask();
    protected static final int OC_INSTALL_TIME = 5 * 60 * 1000;

    @Override
    public void setUp() throws Exception {
        logger.info("setUp work");
        if (!isOCInstalled) {
            TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
            LogcatUtil logcatUtil = new LogcatUtil(getContext(), msisdnTask, ocInitializationTask, policiesRetrievalTask, clientRegistrationWithServerTask, firewallPolicyMgmtDataRequestTask, firewallPolicyMgmtDataResponseTask, responseFirewallPolicyReceivedTask, startingFirewallTask);
            logcatUtil.start();
            logger.info("Logcat Util has started sinse " + new Date());
            logger.info("Current device is: " + Build.MODEL);
            logger.info("Current SDK is: " + Build.VERSION.SDK_INT);

            File f = new File(OC_APK_FILENAME);
            if (f.exists()) {
                logger.info("OC APK exists.");
            } else {
                logger.info("OC APK not exists.");
            }
            String[] uninstallOc = {"su", "-c", "pm uninstall com.seven.asimov"};
            String[] killAllOcc = {"su", "-c", "killall -9 occ"};
            String[] installOc = {"su", "-c", "pm install -r " + OC_APK_FILENAME};
            String[] startService = new String[]{"su", "-c", "am startservice com.seven.asimov/.ocengine.OCEngineService"};
            Runtime.getRuntime().exec(uninstallOc).waitFor();
            Runtime.getRuntime().exec(killAllOcc).waitFor();
            logSleeping(30 * 1000);
            Runtime.getRuntime().exec(installOc).waitFor();
            Runtime.getRuntime().exec(startService).waitFor();
            logSleeping(OC_INSTALL_TIME);
            logcatUtil.stop();
            logger.info("Last entry: " + logcatUtil.getLastEntry());
            logger.info("First entry: " + logcatUtil.getFirstEntry());
            isOCInstalled = true;
            if (PMSUtil.getDeviceZ7TpAddress() != null &&
                    PMSUtil.getDeviceZ7TpAddress().length() > 2) {
                z7TpId = PMSUtil.getDeviceZ7TpAddress().substring(2, PMSUtil.getDeviceZ7TpAddress().length());
            } else {
                throw new AssertionFailedError("Some problems with OC. File transport_settings not found or corrupted.");
            }
        }
        super.setUp();
    }

    @Override
    protected void runTest() throws Throwable {
        try {
            super.runTest();
        } catch (Exception e) {
            throw new AssertionFailedError(e.getMessage());
        }
    }
}
