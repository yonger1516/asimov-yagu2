package com.seven.asimov.it.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.test.AssertionFailedError;
import com.seven.asimov.it.IntegrationTestRunnerGa;
import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.tcpdump.HttpSession;
import com.seven.asimov.it.utils.tcpdump.TcpDumpUtil;
import junit.framework.Assert;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public final class OCUtil {
    private static Boolean squidEnabled = null;
    private static boolean isHttpsRoutedOnLA = false;
    private static final String targetDir = IntegrationTestRunnerGa.getInstance().getFilesDir();
    private static final String tcpDumpTargetDir = targetDir + "/tcpdump-logs";
    private static final String topUtilTargetDir = IntegrationTestRunnerGa.RESULTS_DIR + "top-results";
    private static final String htmlReportTargetDir = IntegrationTestRunnerGa.RESULTS_DIR + "res";
    private static final String ocCachingResourceDir = "/data/misc/openchannel/httpcache";
    private static final Logger logger = LoggerFactory.getLogger(OCUtil.class.getSimpleName());

    public static List<String> getGaProcessNamesArr() {
        return processNames;
    }

    public static List<String> getProcessNamesByPolicy() {
        return processNamesByPolicy;
    }

    private static List<String> processNames = Arrays.asList("occ", "com.seven.asimov", "ocdnsd", "ocshttpd", "ochttpd");
    private static List<String> processNamesByPolicy = Arrays.asList("occ", "http", "dns", "com.seven.asimov", "https");

    private OCUtil() {
    }

    /**
     * Checks if OC client is running.
     *
     * @return true if OC is running, else false
     */
    public static boolean isOpenChannelRunning() {
        boolean result = getOcProcesses(true) != null;
        logger.debug("isOpenChannelRunning = " + result);
        return result;
    }

    /**
     * Returns <code>Map</code> filled with names and PIDs of currently running processes
     *
     * @param output if <code>true</code> write processes list into logcat
     * @return <code>Map<String, Integer></code> where keys are processes names and values are PIDs
     */
    public static Map<String, Integer> getRunningProcesses(boolean output) {
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
            logger.error(ExceptionUtils.getStackTrace(e));
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    logger.error("Тot enough space in memory" + ExceptionUtils.getStackTrace(e));
                }
            }
        }
        if (output) {
            logger.debug("Running processes:");
            for (String name : processes.keySet()) {
                logger.debug(name + ": " + processes.get(name));
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
        if (output) {
            logger.info("Running OC processes:");
        }
        for (String name : allProcesses.keySet()) {
            if (processNames.contains(name) || processNamesByPolicy.contains(name)) {
                ocProcesses.put(name, allProcesses.get(name));
                if (output) {
                    logger.info(name + ": " + allProcesses.get(name));
                }
            }
        }
        return ocProcesses.isEmpty() ? null : ocProcesses;
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
            if (packageInfo.packageName.equals(TFConstantsIF.TARGET_NAME)) {
                result = packageInfo.uid;
                break;
            }
        }
        logger.trace("UID(com.seven.asimov) = " + result);
        return result;
    }

    /**
     * <p>
     * Only for testrunner resources.
     * </p>
     *
     * @return true if squid enabled otherwise false
     * @throws Exception
     */
    @Deprecated
    public static Boolean isSquidEnabled(Context context) throws Exception {
        if (squidEnabled == null) {
            squidEnabled = false;
            TcpDumpUtil tcpDumpUtil = TcpDumpUtil.getInstance(context);
            try {
                tcpDumpUtil.start();
                long startTime = System.currentTimeMillis();
                String RESOURCE_URI = "asimov_it_ga_squid";
                String uri = AsimovTestCase.createTestResourceUri(RESOURCE_URI);
                HttpRequest request = AsimovTestCase.createRequest().setUri(uri).setMethod("GET")
                        .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();

                HttpResponse response = AsimovTestCase.sendRequest(request, false);
                long endTime = System.currentTimeMillis();
                tcpDumpUtil.stop();
                for (HttpSession socketInfo : tcpDumpUtil.getHttpSessions(startTime, endTime)) {
                    if (socketInfo.getClientPort() == Integer.parseInt(TFConstantsIF.TC_REDIRECTION_PORT) ||
                            socketInfo.getServerPort() == Integer.parseInt(TFConstantsIF.TC_REDIRECTION_PORT)) {
                        squidEnabled = true;
                    }
                }
            } catch (IOException e) {
                logger.error(ExceptionUtils.getStackTrace(e));
                throw (e);
            } finally {
                tcpDumpUtil.stop();
            }
        }
        return squidEnabled;
    }

    /**
     * @return names of currently running processes (executes ps)
     */
    public static List<String> getRunningProcesses() {
        return new ArrayList<String>(getRunningProcesses(false).keySet());
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
     * @return path to OC cache directory
     */
    public static String getOcCachingResourceDir() {
        return ocCachingResourceDir;
    }

    /**
     * Removes the OC client from device.
     *
     * @throws Exception
     */
    public static void removeOCClient(boolean removeDataFolders) throws Exception {
        Runtime.getRuntime().exec(new String[]{"su", "-c", "killall -9 occ"}).waitFor();
        Runtime.getRuntime().exec(new String[]{"su", "-c", "killall -9 occ"}).waitFor();
        Runtime.getRuntime().exec(new String[]{"su", "-c", "killall -9 occ"}).waitFor();
        Runtime.getRuntime().exec(new String[]{"su", "-c", "pm uninstall com.seven.asimov"}).waitFor();
        if (removeDataFolders) {
            Runtime.getRuntime().exec(new String[]{"su", "-c", "rm -r /data/misc/openchannel/*"}).waitFor();
        }
    }

    public static void removeOCClient() throws Exception {
        removeOCClient(true);
    }

    /**
     * Installs the OC client file.
     *
     * @param apkFileName Path to apk file that will be installed. The file should exist on the path.
     * @throws Exception
     */
    public static void installOCClient(String apkFileName) throws Exception {
        String[] startService = new String[]{"su", "-c", "am startservice com.seven.asimov/.ocengine.OCEngineService"};
        File f = new File(apkFileName);
        if (!f.exists()) {
            throw new AssertionFailedError("apk file " + apkFileName + " doesn't exist!");
        }
        Runtime.getRuntime().exec(new String[]{"su", "-c", "pm install -r " + apkFileName}).waitFor();
        Thread.sleep(10 * 1000);
        logger.debug("Sending intent for start OC Engine");
        Runtime.getRuntime().exec(startService).waitFor();
        TestUtil.sleep(30 * 1000);
    }

    /**
     * Installs and verifies the OC client.
     * @param apkFileName Path to apk file that will be installed. The file should exist on the path.
     * @throws Exception
     */
    public static void installOCClientWithVerifying(String apkFileName) throws Exception {
        int counter = 0;
        installOCClient(apkFileName);
        while (!isOpenChannelRunning()) {
            TestUtil.sleep(30 * 1000);
            if (++counter > 12) { // in total wait for 6 min while OC will be installed
                throw new Exception("Open Channel client wasn't installed");
            }
        }
        TestUtil.sleep(90 * 1000); // wait for validation
    }

    public static void clearOCData() throws Exception {
        Runtime.getRuntime().exec(new String[]{"su", "-c", "pm clear " + TFConstantsIF.OC_PACKAGE_NAME}).waitFor();
    }

    /**
     * Reinstalls the OC client.
     *
     * @param apkFileName Path to apk file that will be installed. The file should exist on the path.
     * @throws Exception
     */
    public static void reinstallOCC(String apkFileName) throws Exception {
        logger.info("Before removeOCClient");
        removeOCClient();
        logger.info("After removeOCClient");
        logger.info("Before installOCClient");
        installOCClient(apkFileName);
        logger.info("After installOCClient");
    }

    public static void restartOc() throws Exception{
        final String asimovProcess = "com.seven.asimov";
        Integer asimovPID;
        Map<String, Integer> ocProcesses = getOcProcesses(true);
        asimovPID = ocProcesses.get(asimovProcess);
        Assert.assertTrue("Engine isn't running", asimovPID != null);
        List<String> command = new ArrayList<String>();
        command.add("kill " + asimovPID);
        ShellUtil.execWithCompleteResult(command, true);
        TestUtil.sleep(30 * 1000);

        for(int i = 0; i < 3; i++) {
            ocProcesses = getOcProcesses(true);
            asimovPID = ocProcesses.get(asimovProcess);
            if (asimovPID != null)
                break;

            TestUtil.sleep(15 * 1000);
        }
        asimovPID = ocProcesses.get(asimovProcess);
        Assert.assertTrue("Engine wasn't restarted", asimovPID != null);
    }

}