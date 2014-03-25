package com.seven.asimov.test.tool.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import com.seven.asimov.test.tool.activity.AdminTab;
import com.seven.asimov.test.tool.activity.RootTab;
import com.seven.asimov.test.tool.activity.Tabs;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;
import java.util.logging.Formatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Z7LoggerUtil {
    private static final Logger LOG = LoggerFactory.getLogger(Z7LoggerUtil.class.getSimpleName());

    private static MemoryHandler sLogHandler = null;
    private static File mExternalLogDir = new File(getExternalLogsDir());

    public static final String INTERNAL_DIR_DDMS_LOG = "logs/ddms";
    public static final String EXTERNAL_DIR_DDMS_LOG = "/7Test/logs/ddms/";
    public static final int MAX_KEEP_DAYS_INTERNAL = 3;
    public static final int MAX_KEEP_DAYS_EXTERNAL = 5; // Days by default
    public static final long MAX_TOTAL_SIZE_INTERNAL = 2 * 1024 * 1024; // 2MB
    public static final long MAX_TOTAL_SIZE_EXTERNAL = 100 * 1024 * 1024;

    private static String sLogFileName;
    private static Handler sHandler;
    private static java.util.logging.Logger sLogger;
    private static Context sContext;
    private static Resources sResources;
    private static int sBufferedCount = 0;
    private static boolean sIsLogToSystem;
    private static int sLogStatusCheckCount = 0;
    private static File sLastLogFile;
    private static boolean sFileLimitNotificationSent = false;

    private static final int LOG_BUFFER_SIZE = 100;
    private static final int MAX_LOG_FILE_SIZE = 980000;

    // On reaching MAX_LOG_FILE_COUNT, log files will be zipped.
    private static final int MAX_LOG_FILE_COUNT = 8;

    private static Calendar sCalendar = Calendar.getInstance();
    private static TimeZone sDefaultTimeZone = TimeZone.getDefault();

    private static boolean mWriteToExt = true;

    public static void init(Context c) {
        sContext = c;
        sResources = c.getResources();
        sIsLogToSystem = true;
        sLogFileName = "Client";
        resetLogFileHandler();
        sLogger.fine("Logs initialized.");
    }

    public static void flushLogFile() {
        if (sLogHandler != null) {
            sLogHandler.push();
            sLogHandler.flush();
        }
    }

    private static boolean sInitiated;

    public static void setInitiated(boolean initiated) {
        sInitiated = initiated;
    }

    public static boolean isInitiated() {
        return sInitiated;
    }

    public static File getInternalLogsDir() {
        File dir = new File(sContext.getFilesDir(), INTERNAL_DIR_DDMS_LOG);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    private static String getExternalLogsDir() {
        return Environment.getExternalStorageDirectory() + EXTERNAL_DIR_DDMS_LOG;
    }

    public static File[] getLogFiles() {
        File[] logFiles = getInternalLogsDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                String f = filename.toLowerCase();
                return (f.contains(".txt") || f.contains(".log")) && !f.endsWith(".lck");
            }
        });
        return logFiles;
    }

    private static String getInternalZipLogsDir() {
        return sContext.getFilesDir() + File.separator + INTERNAL_DIR_DDMS_LOG;
    }

    private static String rollLogsToZip() {

        String result = null;

        flushLogFile();
        File[] logFiles = getLogFiles();

        if (logFiles.length == 0) {
            LOG.info("No logs found!");
            return null;
        }

        SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        sdFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        long currTime = System.currentTimeMillis();
        String fileName = "7Test-" + sdFormatter.format(new Date(currTime)) + ".zip";

        byte[] buf = new byte[1024];
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            File outputFile = new File(getInternalZipLogsDir() + File.separator + fileName);
            fos = new FileOutputStream(outputFile);
            zos = new ZipOutputStream(fos);
            for (File logFile : logFiles) {
                String logFileName = "7Test-" + sdFormatter.format(new Date(currTime)) + "-" + logFile.getName();
                FileInputStream fis = null;
                try {
                    ZipEntry entry = new ZipEntry(logFileName);
                    zos.putNextEntry(entry);
                    fis = new FileInputStream(logFile);
                    int len = 0;
                    while ((len = fis.read(buf)) > 0) {
                        zos.write(buf, 0, len);
                    }
                } catch (IOException e) {
                    LOG.error("Failed to compress log file " + logFile, e);
                } finally {
                    try {
                        zos.closeEntry();
                    } catch (IOException e) {
                        // ignored
                    }
                    Util.close(fis);
                }

                LOG.info("File " + logFileName + " added to zip and removed: " + logFile.delete());

                File lockFile = new File(getInternalLogsDir() + File.separator + logFile.getName() + ".lck");
                if (lockFile.exists()) {
                    LOG.info("File " + lockFile.getName() + " removed: " + lockFile.delete());
                }
            }
            result = outputFile.getPath();
        } catch (Exception e) {
            LOG.error("Attachment zip failed. ", e);
        } finally {
            Util.close(zos);
            Util.close(fos);
        }

        resetLogFileHandler();
        // Z7Servant.dispatchCallback(SystemCallbackType.Z7_CALLBACK_LOG_LEVEL_CHANGED, null);
        return result;
    }

    public static void resetLogFileHandler() {
        sLogger = java.util.logging.Logger.getLogger(sLogFileName);
        // disable default system log when android_record_system_log=1, to avoid duplicated log printed
        if (sIsLogToSystem) {
            sLogger.setUseParentHandlers(false);
        }

        if (sLogHandler != null) {
            sLogger.removeHandler(sLogHandler);
            sLogHandler.push();
            sLogHandler.flush();
            sLogHandler.close();
        }

        try {
            File f = new File(getInternalLogsDir(), sLogFileName + ".%g.log");
            sLastLogFile = new File(getInternalLogsDir(), String.format("%s.%d.log", sLogFileName,
                    MAX_LOG_FILE_COUNT - 1));

            FileHandler fh = new FileHandler(f.getAbsolutePath(), MAX_LOG_FILE_SIZE, MAX_LOG_FILE_COUNT, true);
            fh.setFormatter(new Formatter() {

                @Override
                public String format(LogRecord r) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(getTime(r.getMillis())).append(' ');
                    sb.append(r.getLevel());
                    sb.append('[').append(r.getThreadID()).append(']');
                    sb.append('[').append(r.getLoggerName()).append(']');
                    sb.append(r.getMessage()).append('\n');
                    if (r.getThrown() != null) {
                        sb.append(Log.getStackTraceString(r.getThrown()));
                    }
                    return sb.toString();
                }

                private StringBuffer mDtBuffer = new StringBuffer(42);

                // "yyyy-MM-dd HH:mm:ss.SSS UTC [GMT +02:00]" 42 is enough;

                private String getTime(long millis) {
                    sCalendar.setTimeZone(TimeZone.getTimeZone("UTC"));
                    sCalendar.setTimeInMillis(millis);
                    mDtBuffer.delete(0, mDtBuffer.length());
                    mDtBuffer.append(sCalendar.get(Calendar.YEAR)).append("-");
                    mDtBuffer.append(fillZero(sCalendar.get(Calendar.MONTH) + 1, 2)).append("-");
                    mDtBuffer.append(fillZero(sCalendar.get(Calendar.DAY_OF_MONTH), 2)).append(" ");
                    mDtBuffer.append(fillZero(sCalendar.get(Calendar.HOUR_OF_DAY), 2)).append(":");
                    mDtBuffer.append(fillZero(sCalendar.get(Calendar.MINUTE), 2)).append(":");
                    mDtBuffer.append(fillZero(sCalendar.get(Calendar.SECOND), 2)).append(".");
                    mDtBuffer.append(fillZero(sCalendar.get(Calendar.MILLISECOND), 3)).append(" UTC ");
                    mDtBuffer
                            .append("[")
                            .append(sDefaultTimeZone.getDisplayName(sDefaultTimeZone.inDaylightTime(new Date()),
                                    TimeZone.SHORT)).append("]");
                    return mDtBuffer.toString();
                }

                // HARD CODE!! It is only used for generate time string.make sure it is fast.
                private StringBuilder mBuffer = new StringBuilder(3);

                private String fillZero(int n, int l) {
                    mBuffer.delete(0, mBuffer.length());
                    if (l == 2 && n >= 10) {
                        return String.valueOf(n);
                    }
                    if (l == 3 && n >= 100) {
                        return String.valueOf(n);
                    }
                    if (l == 2 && n < 10) {
                        return mBuffer.append("0").append(n).toString();
                    }
                    if (l == 3 && n < 100 && n >= 10) {
                        return mBuffer.append("0").append(n).toString();
                    }
                    if (l == 3 && n < 10) {
                        return mBuffer.append("00").append(n).toString();
                    }
                    return StringUtils.EMPTY;
                }
            });

            MemoryHandler handler = new MemoryHandler(fh, LOG_BUFFER_SIZE, Level.ALL);
            sLogHandler = handler;

            sLogger.addHandler(sLogHandler);

        } catch (IOException e) {
            System.out.println("resetLogFileHandler" + e.getMessage());
        }

        resetLogLevel();
    }

    public static final int SYSTEM = 0;
    public static final int FATAL = 1;
    public static final int ERROR = 2;
    public static final int WARN = 3;
    public static final int INFO = 4;
    public static final int DEBUG = 5;
    public static final int TRACE = 6;
    public static final int FINETRACE = 7;

    public static void resetLogLevel() {
        setLogLevel(FINETRACE);
    }

    /**
     * Argument is expected to be Z7 log level (ranging 1-7 == FATAL-FINETRACE).
     */
    public static void setLogLevel(int newLevel) {
        if (sLogger == null) {
            sLogger.warning("log level not set " + newLevel);
        } else {
            Level level = sLogger.getLevel();
            switch (newLevel) {
                case SYSTEM:
                case FATAL:
                case ERROR:
                    level = Level.SEVERE;
                    break;
                case WARN:
                    level = Level.WARNING;
                    break;
                case INFO:
                    level = Level.INFO;
                    break;
                case DEBUG:
                    level = Level.FINE;
                    break;
                case TRACE:
                    level = Level.FINER;
                    break;
                case FINETRACE:
                    level = Level.ALL;
                    break;
                default:
                    break;
            }
            sLogger.setLevel(level);
            sLogger.fine("log level set :" + level);
        }
    }

    public static void rollLogs(boolean uploadLogs) {

        String zipFilePath = rollLogsToZip();

        boolean isCopyFailed = false;
        boolean isSDCardFull = false;

        int zipFileCount = 0;

        if (zipFilePath != null) {

            if (mWriteToExt && Util.isSDCardAvailable()) {
                // take all files
                try {
                    File logsDir = new File(getInternalZipLogsDir());
                    File[] zipLogFiles = logsDir.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String filename) {
                            return filename.toLowerCase().endsWith(".zip");
                        }
                    });

                    zipFileCount = zipLogFiles.length;

                    for (File zipLogFile : zipLogFiles) {

                        // Transfer log to server
                        if (uploadLogs) {
                            Z7IOUtil.transferFileToServer(zipLogFile.getPath());
                        }

                        File dest = new File(mExternalLogDir, zipLogFile.getName());
                        LOG.debug(zipLogFile.getName() + "," + zipLogFile.length() + " -> " + dest.getAbsolutePath());

                        if (Util.getAvailableSpaceSDCard() < zipLogFile.length()) {
                            LOG.warn("SD card full.");
                            isSDCardFull = true;
                            break;
                        }

                        try {
                            Util.copyFile(zipLogFile, dest);
                            zipLogFile.delete();
                        } catch (Exception e) {
                            LOG.warn("Copy failed.", e);
                            isCopyFailed = true;
                        }
                    }
                } catch (Exception e) {
                    // failed. can log it normally
                    LOG.warn("Failed to roll logs. Kept in private data dir.", e);
                    isCopyFailed = true;
                }

                LOG.debug("Cleaning " + mExternalLogDir.getAbsolutePath());
                clearLogs(mExternalLogDir, MAX_KEEP_DAYS_EXTERNAL, MAX_TOTAL_SIZE_EXTERNAL);
            }

            if (!Util.isSDCardAvailable() || isSDCardFull || isCopyFailed) {
                LOG.debug("Performing maintenance on data dir ");
                clearLogs(getInternalLogsDir(), MAX_KEEP_DAYS_INTERNAL, MAX_TOTAL_SIZE_INTERNAL);
            }
        }

        if (!isCopyFailed) {
            displayLogFileHelperInfo(zipFileCount + " zip file(s) moved to " + mExternalLogDir.getPath());
        } else {
            displayLogFileHelperInfo("Failed to zip logs!");
        }
    }

    public static void clearLogs(File dir, int days, long maxSize) {
        // list all .zip files
        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().toLowerCase().endsWith(".zip");
            }
        });
        // sort by last modified date DESC
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File obj1, File obj2) {
                return Long.valueOf(obj2.lastModified()).compareTo(Long.valueOf(obj1.lastModified()));
            }
        });

        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -days);
        long timeStamp = c.getTimeInMillis();
        long totalSize = 0;
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            long fileLen = file.length();
            // Increment size after to leave at least one not outdated file
            if ((totalSize > 0 && totalSize + fileLen > maxSize) || file.lastModified() < timeStamp) {
                LOG.debug("Removing log file " + file + " (" + (new Date(file.lastModified())) + ")");
                file.delete();
            }
            totalSize += fileLen;
        }
    }

    public static final String MESSAGE = "logFileHelperMessage";

    private static void displayLogFileHelperInfo(String info) {
        if (RootTab.getCurrentTab() == Tabs.ADMIN_TAB) {
            Intent intent = new Intent(AdminTab.ACTION_DISPLAY);
            intent.putExtra(MESSAGE, StringUtils.EMPTY);
            intent.putExtra("Toast", info);
            broadcastIntent(intent);
        }
    }

    private static void broadcastIntent(Intent intent) {
        // Log.v(LOG, "displayIntent()");
        sContext.sendBroadcast(intent);
    }
}
