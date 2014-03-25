package com.seven.asimov.it.utils;

import android.util.Log;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class SysLogUtil {
    private static final String TAG = SysLogUtil.class.getSimpleName();
    private static final String RESULTS_PATH = TFConstantsIF.OC_INTEGRATION_TESTS_RESULTS_DIR;
    private final List<Task> taskList;
    private final String HOME_PATH = "/data/misc/openchannel/sys_log/";
    private final String FILE_NAME = "sys.log";

    public SysLogUtil(Task... tasks) {
        this.taskList = Arrays.asList(tasks);
    }

    public SysLogUtil(List<Task> tasks) {
        this.taskList = tasks;
    }

    public void parseLog() {
        setPermission();
        copyLogToSdCard();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(new File(RESULTS_PATH + FILE_NAME)));
            String line;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                for (Task task : taskList) {
                    task.process(line, ++lineNumber);
                }
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        } catch (IOException e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        } finally {
            IOUtil.safeClose(br);
        }
    }

    private void copyLogToSdCard() {
        File file = new File(RESULTS_PATH + FILE_NAME);
        if (file.exists()) {
            file.delete();
        }
        try {
            String[] permission = {"su", "-c", "cat " + HOME_PATH + FILE_NAME + " > " + RESULTS_PATH + FILE_NAME};
            Runtime.getRuntime().exec(permission).waitFor();
        } catch (IOException e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        } catch (InterruptedException e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
    }

    private void setPermission() {
        try {
            String[] permission = {"su", "-c", "chmod -R 777 " + HOME_PATH};
            Runtime.getRuntime().exec(permission).waitFor();
        } catch (IOException e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        } catch (InterruptedException e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
    }
}
