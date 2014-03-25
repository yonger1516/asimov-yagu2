package com.seven.asimov.test.tool.utils;

import android.content.Context;
import android.os.Environment;
import com.seven.asimov.it.utils.IOUtil;
import com.seven.asimov.test.tool.preferences.FilePrefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * LogCatRunner.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */
public class LogcatRunnerUtil extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(LogcatRunnerUtil.class.getSimpleName());

    private final Context mContext;

    private boolean mContinue = true;
    private Map<String, LogCat> mLogCatList;
    ;

    public LogcatRunnerUtil(Context context, String... processNames) {

        this.mContext = context;
        mLogCatList = new HashMap<String, LogCat>();

        for (String processName : processNames) {
            mLogCatList.put(processName, new LogCat(processName));
        }
    }

    public void stopRunner() {
        mContinue = false;
    }

    @Override
    public void run() {

        BufferedReader reader = null;

        try {

            for (String processName : mLogCatList.keySet()) {
                LOG.info("Starting logcat for %s", processName);
            }

            File dir = null;
            if (FilePrefs.isLogCatLogsStoredOnSD()) {
                File sdcard = Environment.getExternalStorageDirectory();
                dir = new File(sdcard.getAbsolutePath() + Z7FileUtils.EXTERNAL_DIR_LOGCAT_LOG);
            } else {
                dir = new File(mContext.getFilesDir() + File.separator + Z7FileUtils.INTERNAL_DIR_LOGCAT_LOG);
            }

            if (!dir.exists()) {
                dir.mkdirs();
            }

            String command = mContext.getFilesDir() + File.separator + Z7ShellUtil.LOGCAT_FULL_FILENAME + " -v time";

            Process process = Runtime.getRuntime().exec(command);
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {

                // Check if logcat should be stopped
                if (!mContinue) {
                    break;
                }

                for (LogCat logcat : mLogCatList.values()) {

                    // Create file
                    if (logcat.getFos() == null) {

                        String[] columns = line.trim().split(" ");

                        if (columns.length > 5) {

                            String dateTimeString = columns[0] + " " + columns[1];

                            DateFormat formatter = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
                            Date localTime = (Date) formatter.parse(dateTimeString);
                            localTime.setYear(Calendar.getInstance().getTime().getYear());

                            formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                            String utcTimeString = formatter.format(localTime);

                            File file = new File(dir.getPath() + File.separator + logcat.getProcessName() + "_"
                                    + utcTimeString + ".txt");

                            int i = 1;
                            while (true) {
                                if (file.exists()) {
                                    i++;
                                    file = new File(dir.getPath() + File.separator + logcat.getProcessName() + "_"
                                            + utcTimeString + "_" + i + ".txt");
                                } else {
                                    break;
                                }
                            }

                            logcat.setFos(new FileOutputStream(file));

                        } else {
                            if (!firstLine) {
                                LOG.warn("Could not create a file for logcat line:");
                                LOG.warn(line);
                            }
                            firstLine = false;
                            continue;
                        }
                    }

                    // Log here
                    logcat.log(line);

                }

                // Check if logcat should be stopped
                if (!mContinue) {
                    break;
                }

            }
        } catch (Exception e) {
            LOG.error(e.toString());
        } finally {
            for (LogCat logcat : mLogCatList.values()) {
                IOUtil.safeClose(logcat.getFos());
            }
            IOUtil.safeClose(reader);

            for (String processName : mLogCatList.keySet()) {
                LOG.info("Logcat for %s was stopped!", processName);
            }
        }

    }
}
