package com.seven.asimov.it.utils.logcat;

import android.content.Context;
import android.test.AssertionFailedError;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.IOUtil;
import com.seven.asimov.it.utils.ShellUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.logcat.stopconditions.LogcatStopCondition;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.LogEntryWrapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static junit.framework.Assert.assertTrue;

public class LogcatUtil {

    enum LogcatState {
        NotStarted,
        Running,
        Stopped
    }

    private static final Logger logger = LoggerFactory.getLogger(LogcatUtil.class.getSimpleName());

    private static final String TAG = "LogcatUtil";
    private final List<Task> tasks;
    private Context context;

    private String firstEntry;
    private String lastEntry;
    private BufferBackedInputStream logcatStream;
    private int BLOCK_SIZE = 256 * 1024;
    private int MEM_LIMIT = 2 * 1024 * 1024;

    private BufferedWriter debugWriter;
    private LogcatState logcatState = LogcatState.NotStarted;
    private LogcatStopCondition stopCondition = null;
    private Thread callerThread = null;

    public LogcatStopCondition getStopCondition() {
        return stopCondition;
    }

    public void setStopCondition(LogcatStopCondition stopCondition) {
        this.stopCondition = stopCondition;
    }

    public LogcatUtil(Context context, Task... tasks) {
        this.context = context;
        this.tasks = Arrays.asList(tasks);
        registerTasksLogcat();
    }

    public LogcatUtil(Context context, LogcatStopCondition stopCondition, Task... tasks) {
        this(context, tasks);
        setStopCondition(stopCondition);
    }

    public LogcatUtil(Context context, List<Task> tasks) {
        this.context = context;
        this.tasks = tasks;
        registerTasksLogcat();
    }

    public LogcatUtil(Context context, Set<Task> tasks) {
        this.context = context;
        this.tasks = new ArrayList<Task>(tasks);
        registerTasksLogcat();
    }

    private void registerTasksLogcat() {
        for (Task task : tasks) {
            task.setOwnerLogcat(this);
        }
    }


    public void stopOnCondition(int timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            logger.info("Interrupted while waiting for timeout");
        } finally {
            try {
                stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        assertTrue("LogcatUtil object can't be reused.", logcatThread.getState() != Thread.State.TERMINATED);
        long startTimestamp = System.currentTimeMillis();
        logger.info("Starting logcat thread [Time = " + startTimestamp + "  ,DateTime = " + new Date(startTimestamp) + "]");

        for (Task task : tasks) {
            task.setStartTimestamp(startTimestamp);
        }
        grantReadLogsPermissions();
        clearCache();
        SimpleDateFormat dt1 = new SimpleDateFormat("yyyy_MMM_dd_(HH_mm_ss)S");
        String fileName = dt1.format(new Date()) + ".log";
        File logDebugFile = new File(TFConstantsIF.LOGCAT_UTIL_DEBUG);
        File swapFolder = new File(TFConstantsIF.LOGCAT_UTIL_SWAP);
        logDebugFile.mkdirs();
        swapFolder.mkdirs();
        try {
            debugWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(TFConstantsIF.LOGCAT_UTIL_DEBUG + fileName)));
        } catch (FileNotFoundException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        callerThread = Thread.currentThread();
        logcatState = LogcatState.Running;
        logcatThread.start();
    }

    public void stop() throws InterruptedException {
        if (logcatState != LogcatState.Running)
            return;
        logger.info("Stopping logcat thread [Time = " + System.currentTimeMillis() + "  ,DateTime = " + new Date(System.currentTimeMillis()) + "]");
        if (logcatStream != null)
            logcatStream.stopReading();
        logcatThread.interrupt();
        logcatThread.join();
        logcatState = LogcatState.Stopped;
    }

    public static void clearCache() {
        try {
            String[] clearLogcatBuffer = {"su", "-c", "logcat -c"};
            Runtime.getRuntime().exec(clearLogcatBuffer).waitFor();
        } catch (Exception e) {
            logger.error("Logcat reading error: " + ExceptionUtils.getStackTrace(e));
        }
    }

    private final Thread logcatThread = new Thread(new Runnable() {
        @Override
        public void run() {
            logger.info("Logcat thread started");
            List<Integer> initialLogcatPids = getLogcatPids();
            BufferedReader reader = null;
            try {
                Process logcatProcess = Runtime.getRuntime().exec(new String[]{"su", "-c", "logcat -v time"});
                logcatStream = new BufferBackedInputStream(logcatProcess.getInputStream(), BLOCK_SIZE, MEM_LIMIT,
                        TFConstantsIF.LOGCAT_UTIL_SWAP);
                reader = new BufferedReader(new InputStreamReader(logcatStream));
                String line;
                int lineNumber = 0;
                while ((line = reader.readLine()) != null) {
                    debugWriter.write(line);
                    debugWriter.newLine();
                    lastEntry = line;
                    if (firstEntry == null || firstEntry.contains("beginning of"))
                        firstEntry = line;
                    for (Task task : tasks) {
                        try {
                            LogEntryWrapper wrapper = task.process(line, ++lineNumber);
                            if ((wrapper != null) && (!onNewWrapper(tasks, wrapper))) {
                                if (logcatStream != null)
                                    logcatStream.stopReading();
                                logger.info("Stopping logcat thread by Stop Condition [Time = " + System.currentTimeMillis() + "  ,DateTime = " + new Date(System.currentTimeMillis()) + "]");
                                logcatState = LogcatState.Stopped;
                                TestUtil.sleep(3000);   //Delay before returning just to be sure OC executed things it just logged.
                                if (callerThread != null)
                                    callerThread.interrupt();
                                return;
                            }
                        } catch (Exception e) {
                            logger.info(" Logcat reading error for task:[" + task.getClass().getSimpleName() + "]." +
                                    " Exception.message = " + ExceptionUtils.getStackTrace(e));
                            throw new AssertionFailedError("Failed to parse logcat due to task ["
                                    + task.getClass().getSimpleName() + "] has exception = " +
                                    ExceptionUtils.getStackTrace(e) + ".");
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("LogCat reading error: " + ExceptionUtils.getStackTrace(e));
            } finally {
                logger.info("Finished parsing logcat records [Time = " + System.currentTimeMillis() + "  ,DateTime = " +
                        new Date(System.currentTimeMillis()) + "]");
                IOUtil.safeClose(reader);

                try {
                    debugWriter.flush();
                    debugWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    killLogcatProcess(initialLogcatPids);
                } catch (IOException e) {
                    logger.error(ExceptionUtils.getStackTrace(e));
                }
            }
        }
    });

    private boolean onNewWrapper(List<Task> tasks, LogEntryWrapper wrapper) {
        if (stopCondition != null)
            return stopCondition.onNewWrapper(tasks, wrapper);

        return true;
    }

    public void clear() {
        for (int i = 0; i < tasks.size(); i++) {
            tasks.get(i).getLogEntries().clear();
            logger.info("Remove entry " + i);
        }
    }

    private void grantReadLogsPermissions() {
        String packageName = context.getPackageName();
        String[] cmdlineGrantPermissions = {"su", "-c", null};
        if (context.getPackageManager().checkPermission(android.Manifest.permission.READ_LOGS, packageName) != 0) {
            logger.warn("we do not have the READ_LOGS permission!");
            if (android.os.Build.VERSION.SDK_INT >= 16) {
                logger.info("Working around JellyBeans 'feature'...");
                try {
                    cmdlineGrantPermissions[2] = String.format("pm grant %s android.permission.READ_LOGS", packageName);
                    Process p = Runtime.getRuntime().exec(cmdlineGrantPermissions);
                    int res = p.waitFor();
                    logger.debug("exec returned: " + res);
                    if (res != 0)
                        throw new Exception("failed to become root");
                } catch (Exception e) {
                    logger.debug("exec(): " + e);
                    logger.error("Failed to obtain READ_LOGS permission");
                }
            }
        } else {
            logger.debug("we have the READ_LOGS permission already!");
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

    public static void killLogcatProcess(List<Integer> initialLogcatPids) throws IOException {
        List<Integer> pidsToKill = getLogcatPids();
        pidsToKill.removeAll(initialLogcatPids);
        for (int pid : pidsToKill) {
            Runtime.getRuntime().exec(new String[]{"su", "-c", "kill -9 " + pid});
        }
    }

    public String getLastEntry() {
        return lastEntry;
    }

    public String getFirstEntry() {
        return firstEntry;
    }

    @Deprecated
    //use non-static method
    public static void logTasks(Collection<Task> tasks) {
        for (Task task : tasks) {
            if (task != null)
                logger.trace(task.toString());
        }
    }

    public void logTasks() {
        for (Task task : tasks) {
            if (task != null)
                logger.trace(task.toString());
        }
    }

    public static void cleanup() {
        try {
            final File swapFolder = new File(TFConstantsIF.LOGCAT_UTIL_SWAP);
            final File debugFolder = new File(TFConstantsIF.LOGCAT_UTIL_DEBUG);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.DATE, -5);
            final Date borderDate = calendar.getTime();

            try {
                FileUtils.cleanDirectory(swapFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (debugFolder.exists())
                for (File file : debugFolder.listFiles())
                    if (FileUtils.isFileOlder(file, borderDate))
                        file.delete();

        } catch (Throwable e) {
            logger.error("Logcat cleanup failed: " + e.getStackTrace());
        }
    }

    public boolean isRunning() {
        return logcatState == LogcatState.Running;
    }
}