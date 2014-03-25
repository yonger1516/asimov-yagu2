package com.seven.asimov.test.tool.utils;

import android.content.Context;
import android.util.Log;
import com.seven.asimov.it.utils.ShellUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SmokeLogcatUtil {
    private final String TAG = SmokeLogcatUtil.class.getName();
    private Context context;
    private List<Integer> initialLogcatPids;

    public SmokeLogcatUtil(Context context) {
        this.context = context;
    }

    public void start(int suite) {
        long startTimestamp = System.currentTimeMillis();
        Log.i(TAG, "StartTimestamp=" + startTimestamp);
        try {
            grantReadLogsPermissions();
            initialLogcatPids = getLogcatPids();
//            Runtime.getRuntime().exec(new String[]{"su", "-c", "logcat -c"}).waitFor();
            Runtime.getRuntime().exec(new String[]{"su", "-c", "logcat -v time > " + Z7TestUtil.TOTAL_RESULT + Z7TestUtil.SUITES[suite] + "log.txt &"}).waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        logcatThread.start();
    }

    public void stop() throws InterruptedException {
        Log.w(TAG, "Stopping logcat thread");
        try {
            killLogcatProcess(initialLogcatPids);
            Runtime.getRuntime().exec(new String[]{"su", "-c", "logcat -c"}).waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        logcatThread.interrupt();
//        logcatThread.join();
    }

//    private final Thread logcatThread = new Thread(new Runnable() {
//        @Override
//        public void run() {
//            Log.w(TAG, "Logcat thread started");
//            grantReadLogsPermissions();
//            List<Integer> initialLogcatPids = getLogcatPids();
////            BufferedReader reader = null;
//            try {
//                Runtime.getRuntime().exec(new String[]{"su", "-c", "logcat -v time > /sdcard/OCIntegrationTestsResults/log.txt &"});
//                while (!logcatThread.isInterrupted()) {
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                Log.e(TAG, "LogCat reading error: " + e.getMessage());
//            } finally {
////                com.seven.asimov.it.util.IOUtils.safeClose(reader);
//                try {
//                    killLogcatProcess(initialLogcatPids);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    });

    private void grantReadLogsPermissions() {
        String packageName = context.getPackageName();
        String[] cmdlineGrantPermissions = {"su", "-c", null};
        if (context.getPackageManager().checkPermission(android.Manifest.permission.READ_LOGS, packageName) != 0) {
            Log.d(TAG, "we do not have the READ_LOGS permission!");
            if (android.os.Build.VERSION.SDK_INT >= 16) {
                Log.d(TAG, "Working around JellyBeans 'feature'...");
                try {
                    cmdlineGrantPermissions[2] = String.format("pm grant %s android.permission.READ_LOGS", packageName);
                    Process p = Runtime.getRuntime().exec(cmdlineGrantPermissions);
                    int res = p.waitFor();
                    Log.d(TAG, "exec returned: " + res);
                    if (res != 0)
                        throw new Exception("failed to become root");
                } catch (Exception e) {
                    Log.d(TAG, "exec(): " + e);
                    Log.e(TAG, "Failed to obtain READ_LOGS permission");
                }
            }
        } else {
            Log.d(TAG, "we have the READ_LOGS permission already!");
        }
    }

    public static List<Integer> getLogcatPids() {
        List<Integer> logcatPids = new ArrayList<Integer>();
        String output = ShellUtil.execSimple("ps logcat");
        String[] lines = output.split("\n");
        for (String line : lines) {
            if (line.startsWith("USER")) continue; // skip ps table header
            String[] tokens = line.split("[\\W]+");
            logcatPids.add(Integer.valueOf(tokens[1]));
        }
        return logcatPids;
    }

    public static void killLogcatProcess(List<Integer> initialLogcatPids) throws IOException, InterruptedException {
        List<Integer> pidsToKill = getLogcatPids();
        pidsToKill.removeAll(initialLogcatPids);
        for (int pid : pidsToKill) {
            Runtime.getRuntime().exec(new String[]{"su", "-c", "kill -9 " + pid});
        }
        Runtime.getRuntime().exec(new String[]{"su", "-c", "logcat -c"}).waitFor();
    }
}
