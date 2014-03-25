package com.seven.asimov.test.tool.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import com.seven.asimov.it.utils.IOUtil;
import com.seven.asimov.test.tool.preferences.FilePrefs;
import com.seven.asimov.test.tool.preferences.SharedPrefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

public class Z7ShellUtil {

    public static final String BIN_ASSETS_PATH = "bin";

    public static final String TCPDUMP_FULL_FILENAME = "tcpdump-arm";
    public static final String PCAP_BASE_FILENAME = "7test-tcpdump";

    public static final String LOGCAT_FULL_FILENAME = "7test-logcat-arm";
    public static final String LOGCAT_BASE_FILENAME = "7test-logcat";
    /**
     * Sleep 200 msec second to ensure that the new process is listed by the system.
     */
    public static final int WAIT_PROCCESS_STARTED = 200;
    /**
     * Sleep 200 msec second to ensure that the killed process is not listed by the system.
     */
    public static final int WAIT_PROCCESS_KILLED = 200;
    /**
     * Sleep 200 msec second to ensure that the new process is granted exec rights.
     */
    public static final int WAIT_PROCCESS_INSTALLED = 200;

    private static final Logger LOG = LoggerFactory.getLogger(Z7ShellUtil.class.getSimpleName());

    private static Context sContext;

    public static void init(Context context) {
        sContext = context;
    }

    private static boolean sInitiated;

    public static void setInitiated(boolean initiated) {
        sInitiated = initiated;
    }

    public static boolean isInitiated() {
        return sInitiated;
    }

    public static boolean isTcpDumpInstalled() {
        // This filter only returns directories
        FileFilter fileFilter = new FileFilter() {
            public boolean accept(File file) {
                return file.getName().contains(PCAP_BASE_FILENAME);
            }
        };
        File[] files = sContext.getFilesDir().listFiles(fileFilter);
        if (files.length == 0) {
            return false;
        }
        return true;
    }

    public static void killTcpDump() {

        // Store to shared prefs
        SharedPrefs.saveIsTcpDumpIsLaunched(false);

        String[] pids = getProcessPids(TCPDUMP_FULL_FILENAME);

        for (String pid : pids) {
            killProcess(pid);
            movePcapLogs();
        }
    }


    public static void killLogCat() {
        // Store to shared prefs
        SharedPrefs.saveIsLogCatIsLaunched(false);
        String[] pids = getProcessPids(LOGCAT_FULL_FILENAME);

        for (String pid : pids) {
            killProcess(pid);
        }
    }


    public static void launchTcpDump() {
        try {
            File dir = new File(sContext.getFilesDir() + File.separator + Z7FileUtils.INTERNAL_DIR_PCAP_LOG);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String command = sContext.getFilesDir() + File.separator + PCAP_BASE_FILENAME
                    + " -s 0 -C 1 -W 100 -i any -w " + sContext.getFilesDir() + File.separator
                    + Z7FileUtils.INTERNAL_DIR_PCAP_LOG + File.separator + PCAP_BASE_FILENAME;

            execShellSuCommand(command);

            Thread.sleep(WAIT_PROCCESS_STARTED);

            // Store config
            SharedPrefs.saveIsTcpDumpIsLaunched(true);

            // Log pids
            showRunningProcessesByName(PCAP_BASE_FILENAME);
        } catch (Exception e) {
            LOG.error(e.toString());
        }
    }

    public static void showRunningProcessesByName(String processesName) {
        String[] pids = getProcessPids(processesName);

        for (String pid : pids) {
            LOG.info("%s launched, pid=%s", processesName, pid);
        }
    }

    public static void copyPcapLogs() {
        FileFilter fileFilter = new FileFilter() {

            public boolean accept(File file) {
                return file.getName().contains(PCAP_BASE_FILENAME);

            }

        };

        File sourceDir = new File(sContext.getFilesDir() + File.separator + Z7FileUtils.INTERNAL_DIR_PCAP_LOG);
        File[] files = sourceDir.listFiles(fileFilter);

        if (files.length == 0) {
            return;
        }

        Arrays.sort(files, new Comparator<File>() {

            public int compare(File o1, File o2) {
                if (((File) o1).lastModified() > ((File) o2).lastModified()) {
                    return +1;
                } else if (((File) o1).lastModified() < ((File) o2).lastModified()) {
                    return -1;
                } else {
                    return 0;
                }

            }


        });

        File sdcard = Environment.getExternalStorageDirectory();
        File destDir = new File(sdcard.getAbsolutePath() + Z7FileUtils.EXTERNAL_DIR_PCAP_LOG);

        if (destDir.exists()) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];

                InputStream in = null;
                OutputStream out = null;

                try {
                    in = new FileInputStream(file);

                    SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                    // sdFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                    out = new FileOutputStream(destDir + File.separator + sdFormatter.format(file.lastModified())
                            + "-trace.pcap");

                    IOUtil.transfer(in, out, new byte[4096]);

                    // delete all files except current
                    if (i != (files.length - 1)) {
                        file.delete();
                    }

                    out.close();
                    in.close();

                } catch (Exception e) {

                } finally {
                    IOUtil.safeClose(in);
                    IOUtil.safeClose(out);
                }
            }
        }
    }

    private static HashMap<String, String> sActiveProcesses = new HashMap<String, String>();


    public static String checkActiveProcessByName(String processName) {

        return sActiveProcesses.get(processName);

    }


    public static boolean checkIfPidIsActive(String pid) {

        return sActiveProcesses.containsValue(pid);

    }


    public static void addActiveProcess(String processName, String pid) {

        sActiveProcesses.put(processName, pid);

    }


    public static void removeActiveProcess(String processName) {

        sActiveProcesses.remove(processName);

    }


    public static void clearActiveProcesses() {

        sActiveProcesses.clear();

    }

    public static void refreshActiveProcesses() {


        sActiveProcesses.clear();


        // ActivityManager manager = (ActivityManager) sContext.getSystemService(Context.ACTIVITY_SERVICE);

        // List<RunningAppProcessInfo> processes = manager.getRunningAppProcesses();

        // for (RunningAppProcessInfo runningAppProcessInfo : processes) {

        // if (runningAppProcessInfo.processName.equals("com.seven.asimov")) {

        // sActiveProcesses.put("com.seven.asimov", String.valueOf(runningAppProcessInfo.pid));

        // break;

        // }

        // }


        try {

            String command = "/system/bin/ps";

            boolean firstLine = true;

            int pidColumn = -1;

            int nameColumn = -1;

            ArrayList<String> commandOutput = execShellCommand(command);

            for (String line : commandOutput) {


                try {

                    if (firstLine) {

                        firstLine = false;

                        String[] columns = line.trim().split(" +");

                        for (int i = 0; i < columns.length; i++) {

                            String column = columns[i];

                            if (column.equalsIgnoreCase("PID")) {

                                pidColumn = i;

                            }

                            if (column.equalsIgnoreCase("NAME")) {

                                nameColumn = i + 1;

                            }

                            if (pidColumn != -1 && nameColumn != -1) {

                                break;

                            }

                        }

                        if (pidColumn == -1) {

                            throw new Exception("PID column not found: " + line);

                        }

                        if (nameColumn == -1) {

                            throw new Exception("NAME column not found: " + line);

                        }

                        continue;

                    }


                    String[] process = line.trim().split(" +");

                    String pid = process[pidColumn];

                    String name = process[nameColumn];


                    addActiveProcess(name, pid);


                } catch (Exception e) {

                    throw new Exception("Could not add PID from line: " + line + " Error: " + e.getMessage());

                }

            }

        } catch (Exception e) {

            LOG.error(e.toString());

        }

    }

    public static boolean isLogCatInstalled() {

        // This filter only returns directories

        FileFilter fileFilter = new FileFilter() {

            public boolean accept(File file) {

                return file.getName().contains(LOGCAT_FULL_FILENAME);

            }

        };

        File[] files = sContext.getFilesDir().listFiles(fileFilter);

        if (files.length == 0) {

            return false;

        }

        return true;

    }


    public static void installLogCat() {


        String dest = sContext.getFilesDir() + File.separator + LOGCAT_FULL_FILENAME;

        AssetManager assetManager = sContext.getAssets();

        InputStream in = null;

        OutputStream out = null;

        try {

            in = assetManager.open(BIN_ASSETS_PATH + File.separator + LOGCAT_FULL_FILENAME);

            out = new FileOutputStream(dest);

            IOUtil.transfer(in, out, new byte[4096]);

            out.close();

            in.close();


            grantExecRights(sContext.getFilesDir() + File.separator + LOGCAT_FULL_FILENAME);


            LOG.info("LogCat installed!");


        } catch (Exception e) {

        } finally {

            IOUtil.safeClose(in);

            IOUtil.safeClose(out);

        }

    }


    public static void movePcapLogs() {
        FileFilter fileFilter = new FileFilter() {

            public boolean accept(File file) {

                return file.getName().contains(PCAP_BASE_FILENAME);

            }

        };

        File sourceDir = new File(sContext.getFilesDir() + File.separator + Z7FileUtils.INTERNAL_DIR_PCAP_LOG);
        File[] files = sourceDir.listFiles(fileFilter);
        if (files.length == 0) {
            return;
        }

        Arrays.sort(files, new Comparator<File>() {

            public int compare(File o1, File o2) {
                if (((File) o1).lastModified() > ((File) o2).lastModified()) {
                    return +1;

                } else if (((File) o1).lastModified() < ((File) o2).lastModified()) {
                    return -1;
                } else {
                    return 0;
                }

            }


        });

        File sdcard = Environment.getExternalStorageDirectory();
        File destDir = new File(sdcard.getAbsolutePath() + Z7FileUtils.EXTERNAL_DIR_PCAP_LOG);

        if (!destDir.exists()) {
            destDir.mkdirs();

        }

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            InputStream in = null;
            OutputStream out = null;

            try {
                in = new FileInputStream(file);

                SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                // sdFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                out = new FileOutputStream(destDir + File.separator + sdFormatter.format(file.lastModified())
                        + "-trace.pcap");

                IOUtil.transfer(in, out, new byte[4096]);
                file.delete();
                out.close();
                in.close();
            } catch (Exception e) {
            } finally {
                IOUtil.safeClose(in);
                IOUtil.safeClose(out);
            }
        }

    }

    public static void launchLogCat(Context context, String[] processNames) {
        if (processNames == null) {
            processNames = FilePrefs.getLogCatProcesses();
            if (processNames == null) {
                processNames = new String[2];
                processNames[0] = new String("com.seven.asimov");
                processNames[1] = new String("com.seven.Z7.service");
            }
        }

        try {
            LogcatRunnerUtil lcRunner = new LogcatRunnerUtil(context, processNames);
            lcRunner.start();

            Thread.sleep(WAIT_PROCCESS_STARTED);

            // Store config
            SharedPrefs.saveIsLogCatIsLaunched(true);

            // Log pids
            showRunningProcessesByName(LOGCAT_FULL_FILENAME);

        } catch (Exception e) {
            LOG.error(e.toString());
        }
    }

    public static void killAll(String processName, int signal) {
        String[] psOutput = getProcessPids(processName);
        for (String entry : psOutput) {
            kill(Integer.parseInt(entry), signal);
        }

    }


    public static boolean isTcpDumpIsRunning() {
        String[] pids = getProcessPids(TCPDUMP_FULL_FILENAME);
        if (pids.length != 0) {
            return true;
        }

        return false;
    }


    public static boolean isLogCatIsRunning() {
        String[] pids = getProcessPids(LOGCAT_FULL_FILENAME);
        if (pids.length != 0) {
            return true;
        }

        return false;
    }

    public static String[] getProcessPids(String processName) {
        ArrayList<String> result = new ArrayList<String>();

        try {
            String command = "/system/bin/ps";
            boolean firstLine = true;
            int pidColumn = -1;
            ArrayList<String> commandOutput = execShellCommand(command);
            for (String line : commandOutput) {
                if (firstLine) {
                    firstLine = false;
                    String[] columns = line.trim().split(" +");
                    for (int i = 0; i < columns.length; i++) {
                        String column = columns[i];
                        if (column.equalsIgnoreCase("PID")) {
                            pidColumn = i;
                            break;
                        }
                    }

                    if (pidColumn == -1) {
                        throw new Exception("PID column not found: " + line);
                    }

                }

                if (line.endsWith(processName)) {
                    String[] process = line.trim().split(" +");
                    String pid = process[pidColumn];
                    try {
                        Integer.valueOf(pid);
                        result.add(pid);

                    } catch (Exception e) {
                        throw new Exception("Could not find PID from line: " + line + " Error: " + e.getMessage());

                    }
                }
            }

        } catch (Exception e) {
            LOG.error(e.toString());
        }

        return result.toArray(new String[result.size()]);
    }

    public static ArrayList<String> execShellCommand(String command) {
        ArrayList<String> result = new ArrayList<String>();

        // String separator = System.getProperty("line.separator");
        BufferedReader reader = null;
        try {
            Process process = Runtime.getRuntime().exec(command);
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                result.add(line);
            }

        } catch (Exception e) {
            LOG.error(e.toString());
        } finally {
            IOUtil.safeClose(reader);
        }

        return result;

    }

    public static void killProcess(String pid) {
        try {
            String command = "kill " + pid;
            execShellSuCommand(command);
            Thread.sleep(WAIT_PROCCESS_KILLED);
            LOG.info("Process killed, pid=" + pid);
        } catch (Exception e) {
            LOG.error("killProcess()" + e.getMessage());
        }

    }


    public static void kill(int PID, int signal) {
        LOG.info("Killing PID: " + PID + " with signal: " + signal);
        try {
            if (PID > 0)
                Runtime.getRuntime().exec(new String[]{"su", "-c", "kill -" + signal + " " + PID}).waitFor();
        } catch (IOException e) {
            LOG.error("Shell", "Not killed!");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void grantFullRights(String destination) {
        String command = "chmod 777 " + destination;
        execShellSuCommand(command);
        try {
            Thread.sleep(WAIT_PROCCESS_INSTALLED);
        } catch (Exception e) {
            LOG.error(e.toString());
        }
    }

    public static void grantExecRights(String destination) {
        String command = "chmod 775 " + destination;
        execShellSuCommand(command);
        try {
            Thread.sleep(WAIT_PROCCESS_INSTALLED);
        } catch (Exception e) {
            LOG.error(e.toString());
        }

    }

    public static boolean execShellSuCommand(String command) {
        // String separator = System.getProperty("line.separator");
        DataOutputStream os = null;

        try {
            Process process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.flush();
            os.writeBytes("exit\n");
            os.flush();

            return true;
        } catch (Exception e) {
            LOG.error(e.toString());
            return false;
        } finally {
            IOUtil.safeClose(os);
        }

    }

    public static void installTcpDump() {
        String dest = sContext.getFilesDir() + File.separator + PCAP_BASE_FILENAME;
        String dest1 = sContext.getFilesDir() + File.separator + "tcpdump-new";
        AssetManager assetManager = sContext.getAssets();
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(BIN_ASSETS_PATH + File.separator + TCPDUMP_FULL_FILENAME);
            out = new FileOutputStream(dest);
            IOUtil.transfer(in, out, new byte[4096]);
            out.close();
            in.close();
            in = assetManager.open(BIN_ASSETS_PATH + File.separator + "tcpdump-arm-new");
            out = new FileOutputStream(dest1);
            IOUtil.transfer(in, out, new byte[4096]);
            out.close();
            in.close();

            grantExecRights(sContext.getFilesDir() + File.separator + PCAP_BASE_FILENAME);
            grantFullRights(sContext.getFilesDir() + File.separator + "tcpdump-new");

            LOG.info("TcpDump installed!");

        } catch (Exception e) {
        } finally {
            IOUtil.safeClose(in);
            IOUtil.safeClose(out);
        }
    }
}
