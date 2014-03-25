package com.seven.asimov.test.tool.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.utils.TestUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class SmokeOpenChannelHelperUtil {

    private static boolean isHttpsRoutedOnLA = false;
    private static Boolean squidEnabled = null;

    public static String getOcCachingResourceDir() {
        return ocCachingResourceDir;
    }

    public static enum ClientType {
        LA, GA
    }

    private static String targetName = "com.seven.asimov";

    /*
     * Processes of LA and GA client.
     */
    private static String[] oldProcessNamesArr = {"occ", "ochttpd", "ocdnsd", "ocshttpd"};
    private static String[] newProcessNamesArr = {"occ", "http", "dns", "https"};

    private static List<String> oldProcessNames = new ArrayList<String>(Arrays.asList(oldProcessNamesArr));
    private static List<String> newProcessNames = new ArrayList<String>(Arrays.asList(newProcessNamesArr));

    /*
     * Variable hold current client type
     */
    private static ClientType currentClientType = null;

    private static long waitTimeout = 90 * 1000;
    private static long delayIfNotStarted = 5 * 1000;

    private static final String targetDir = "/sdcard/OCIntegrationTestsResults";
    private static final String tcpDumpTargetDir = targetDir + "/tcpdump-logs";
    private static final String topUtilTargetDir = targetDir + "/top-results";
    private static final String htmlReportTargetDir = topUtilTargetDir + "/res";
    private static final String ocCachingResourceDir = "/data/misc/openchannel/httpcache";

    private SmokeOpenChannelHelperUtil() {

    }

    /**
     * Checks type of currently running client - verifies that all pre-defined processes for La or Ga client are
     * running.
     *
     * @return currently running client type or null if none is running.
     */
    private static ClientType getCurrentRunningClientType() {
        List<String> runningProcesses = getRunningProcesses();
        if (runningProcesses != null && !runningProcesses.isEmpty()) {
//            for (String runningProcess : runningProcesses) {
//                Log.e("SOH", runningProcess);
//            }
//            for (String gaProcessName : oldProcessNames) {
//                Log.e("SOH", gaProcessName);
//            }
            if (runningProcesses.containsAll(newProcessNames)) {
                return ClientType.LA;
            } else if (runningProcesses.containsAll(oldProcessNames)) {
                return ClientType.GA;
            }
        }
        System.out.println("LA or GA are not running");
        return null;
    }

    // TODO a better way to get currentClientType is to set it from IntegrationTestRunnerE2E?
    public static ClientType getCurrentClientType() {
        if (currentClientType == null) {
            currentClientType = getCurrentRunningClientType();
            System.out.println("Running tests on OC " + currentClientType);
        }
        return currentClientType;
    }

    public static void setCurrentClientType(ClientType clientType) {
        currentClientType = clientType;
    }

    /**
     * Checks if OC client is running.
     *
     * @return true if OC is running, else false
     */
    public static boolean isOpenChannelRunning() {
        ClientType type = getCurrentRunningClientType();
        boolean result = (type != null && type == getCurrentClientType());
        System.out.println("isOpenChannelRunning = " + result);
        return result;
    }

    /**
     * Invalidates OC cache.
     *
     * @return count of ms spent on invalidate
     */
    public static long invalidateCache() {
        ClientType clientType = getCurrentClientType();
        if (clientType == null) {
            System.out.println("Failed to invalidate cache LA or GA are not running");
            return 0;
        }

        long startTime = System.currentTimeMillis();
        switch (clientType) {
            case LA:
                invalidateLACache();
                break;
            case GA:
                // invalidateGACache();
                break;
        }

        long delay = System.currentTimeMillis() - startTime;
        System.out.println("invalidateCache took " + delay + "ms");
        return delay;
    }

    /**
     * Invalidates LA cache.
     */

    private static void invalidateLACache() {
        try {
            System.out.println("Invalidating LA cache");
            String invalidateCache = "http://localhost/oc/invalidate";
            HttpRequest invalidateRequest = AsimovTestCase.createRequest().setUri(invalidateCache).setMethod("GET")
                    .getRequest();
            AsimovTestCase.sendRequest(invalidateRequest);
            System.out.println("Successfully invalidated cache");
        } catch (Exception e) {
            System.out.println("Failed to invalidate cache");
        }
    }

    /**
     * Restarts GA client. Currently if restarting GA it clears it's cache.
     */
    private static boolean restartGA() {
        boolean success = true;
        try {
            System.out.println("Restarting GA");
            Runtime.getRuntime().exec("am broadcast -a android.intent.action.OC_ENGINE_STOP");
            // TODO: get pids from ps, and execute kill -9?
            executeSu("kill -1 $(cat /data/misc/openchannel/pids/occ)");
            executeSu("kill -1 $(cat /data/misc/openchannel/pids/ochttpd)");
            executeSu("kill -1 $(cat /data/misc/openchannel/pids/ocdnsd)");

            // Due to kill -1 we should wait for oc to stop correctly.
            TestUtil.sleep(30 * 1000);
            executeSu("/data/misc/openchannel/ocd");
            Runtime.getRuntime().exec("am broadcast -a android.intent.action.OC_ENGINE");
        } catch (Exception e) {
            success = false;
            System.out.println("Failed to restart GA");
        }

        return success;
    }

    /**
     * Invalidates GA cache.
     */
    private static void invalidateGACache() {
        boolean restarted = restartGA();
        if (restarted) {
            // verify that GA processes are up
            TestUtil.sleep(10 * 1000);
            long startTime = System.currentTimeMillis();
            while (!isOpenChannelRunning()) {
                if (System.currentTimeMillis() - startTime > waitTimeout) {
                    restarted = false;
                    break;
                }
                TestUtil.sleep(delayIfNotStarted);
            }
        }

        if (!restarted) {
            // Oops OC GA failed to restart.
            System.out.println("Failed to invalidate GA cache");
        } else {
            // Speep for 20 sec for OC to start.
            TestUtil.sleep(20 * 1000);
            System.out.println("GA cache invalidated successfully");
        }
    }

    /**
     * Returns <code>Map</code> filled with names and PIDs of currently running processes
     *
     * @param output if <code>true</code> write processes list into logcat
     * @return <code>Map<String, Integer></code> where keys are processes names and values are PIDs
     */
    private static Map<String, Integer> getRunningProcesses(boolean output) {
        Map<String, Integer> processes = new HashMap<String, Integer>();
        BufferedReader br = null;
        try {
            String[] a = {"ps"};
            Process process = Runtime.getRuntime().exec(a);
            br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String name = line.trim().split("\\s+")[8];
                int pid = Integer.parseInt(line.trim().split("\\s+")[1]);
                processes.put(name, pid);
            }
        } catch (Exception e) {
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
        }
        if (output) {
            System.out.println("Running processes:");
            for (String name : processes.keySet()) {
                System.out.println(name + ": " + processes.get(name));
            }
        }
        return processes;
    }

    /**
     * Returns <code>Map</code> filled with names and PIDs of currently running OC processes
     *
     * @param output if <code>true</code> write processes list into logcat
     * @return <code>Map<String, Integer></code> where keys are OC processes names and values are PIDs
     */
    public static Map<String, Integer> getOcProcesses(boolean output) {
        Map<String, Integer> ocProcesses = new HashMap<String, Integer>();
        Map<String, Integer> allProcesses = getRunningProcesses(false);
        if (output) System.out.println("Running OC processes:");
        for (String name : allProcesses.keySet()) {
            if (oldProcessNames.contains(name) || newProcessNames.contains(name)) {
                ocProcesses.put(name, allProcesses.get(name));
                if (output) System.out.println(name + ": " + allProcesses.get(name));
            }
        }
        return ocProcesses;
    }

    /**
     * Returns names of currently running processes (executes ps)
     */
    private static List<String> getRunningProcesses() {
        return new ArrayList<String>(getRunningProcesses(false).keySet());
    }

    /**
     * Executes shell command as super user.
     */
    private static void executeSu(String command) throws IOException {
        try {
            String[] a = {"su", "-c", command};
            Process process = Runtime.getRuntime().exec(a);
        } catch (IOException e) {
            System.out.println("Failed to execute: " + command);
            throw e;
        }
    }

    public static void routeHttpsTrafficThrowLA() throws IOException {
        if (!isHttpsRoutedOnLA) {
            BufferedReader br = null;
            try {
                String[] a = {"su", "-c", "ps |grep com.seven.asimov.it|cut -c1-6"};
                Process process = Runtime.getRuntime().exec(a);
                br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String uid = br.readLine();
                System.out.println("com.seven.asimov.it UID = " + uid);
                br.close();
                a[2] = "/system/bin/iptables -t nat -A Z7HTTPSCHAIN -p 6 --destination-port 443 -m owner --uid-owner "
                        + uid + " -j DNAT --to 127.0.0.1:8074";
                process = Runtime.getRuntime().exec(a);
                process.waitFor();
                isHttpsRoutedOnLA = true;
            } catch (IOException e) {
                System.out.println("Failed to route https traffic through OC");
                throw e;
            } catch (InterruptedException e) {
                System.out.println("Failed to route https traffic through OC");
            } finally {
                if (br != null) {
                    br.close();
                }
            }
        }
    }

    /**
     * @return list of processes ga
     */
    public static String[] getOldProcessNamesArr() {
        return oldProcessNamesArr;
    }

    /**
     * @return list of processes la
     */
    public static String[] getNewProcessNamesArr() {
        return newProcessNamesArr;
    }

    /**
     * @return path to target dir
     */
    public static String getTargetDir() {
        return targetDir;
    }

    /**
     * @return path to save tcpdump results
     */

    public static String getTcpDumpTargetDir() {
        return tcpDumpTargetDir;
    }

    /**
     * @return path to save toputil results
     */
    public static String getTopUtilTargetDir() {
        return topUtilTargetDir;
    }

    /**
     * @return path to save htmlreport results
     */
    public static String getHtmlReportTargetDir() {
        return htmlReportTargetDir;
    }

    /**
     * Returns uid of process "com.seven.asimov" or 0 if it's not running.
     *
     * @param context context
     * @return result
     */
    public static int getAsimovUid(Context context) {
        int result = 0;
        final PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.equals(targetName)) {
                result = packageInfo.uid;
                break;
            }
        }
        return result;
    }
}