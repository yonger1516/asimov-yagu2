package com.seven.asimov.test.tool.core;

import android.content.Context;
import android.content.Intent;
import com.seven.asimov.it.utils.PropertyLoadUtil;
import com.seven.asimov.test.tool.preferences.FilePrefs;
import com.seven.asimov.test.tool.preferences.SharedPrefs;
import com.seven.asimov.test.tool.services.MainService;
import com.seven.asimov.test.tool.utils.Z7LoggerUtil;
import com.seven.asimov.test.tool.utils.Z7ShellUtil;

/**
 * @author mselivanov
 */
public abstract class Starter {

    private static Context sContext;

    /**
     *
     */
    public static void init(Context context) {

        sContext = context;

        // Initialize Z7Logger
        if (!Z7LoggerUtil.isInitiated()) {
            Z7LoggerUtil.init(sContext);
            Z7LoggerUtil.setInitiated(true);
        }

        // Initialize Shared preferences
        if (!SharedPrefs.isInitiated()) {
            SharedPrefs.init(sContext);
            SharedPrefs.setInitiated(true);
        }

        // Initialize File preferences
        if (!FilePrefs.isInitiated()) {
            FilePrefs.init();
            FilePrefs.setInitiated(true);
        }

        // Start Main Service
        if (!MainService.isServiceStarted()) {
            sContext.startService(new Intent(sContext, MainService.class));
            MainService.setServiceStarted(true);
        }

        PropertyLoadUtil.init(sContext);

        // Initialize Command shell
        if (!Z7ShellUtil.isInitiated()) {
            Z7ShellUtil.init(sContext);
            Z7ShellUtil.setInitiated(true);

            // Install TcpDump
            if (!Z7ShellUtil.isTcpDumpInstalled()) {
                Z7ShellUtil.installTcpDump();
            }
            // Launch TcpDump if needed
            if (SharedPrefs.isTcpDumpIsLaunched() || FilePrefs.isTcpDumpIsLaunched()) {
                if (Z7ShellUtil.isTcpDumpIsRunning()) {
                    Z7ShellUtil.showRunningProcessesByName(Z7ShellUtil.TCPDUMP_FULL_FILENAME);
                } else {
                    Z7ShellUtil.launchTcpDump();
                }
            }

            // Install LogCat
            if (!Z7ShellUtil.isLogCatInstalled()) {
                Z7ShellUtil.installLogCat();
            }
            // Launch LogCat if needed
            if (SharedPrefs.isLogCatIsLaunched() || FilePrefs.isLogCatIsLaunched()) {
                if (Z7ShellUtil.isLogCatIsRunning()) {
                    Z7ShellUtil.showRunningProcessesByName(Z7ShellUtil.LOGCAT_FULL_FILENAME);
                } else {
                    Z7ShellUtil.launchLogCat(sContext, null);
                }
            }
        }

        // Initialize TestFactory
        if (!TestFactory.isInitiated()) {
            TestFactory.init(sContext);
            TestFactory.setInitiated(true);
        }

    }

}
